package ru.oftendev.itsmylevel.levels

import com.github.benmanes.caffeine.cache.Caffeine
import com.willfp.eco.core.config.interfaces.Config
import com.willfp.eco.core.data.ServerProfile
import com.willfp.eco.core.data.keys.PersistentDataKey
import com.willfp.eco.core.data.keys.PersistentDataKeyType
import com.willfp.eco.core.placeholder.InjectablePlaceholder
import com.willfp.eco.core.placeholder.PlaceholderInjectable
import com.willfp.eco.core.placeholder.PlayerDynamicPlaceholder
import com.willfp.eco.core.placeholder.PlayerPlaceholder
import com.willfp.eco.core.placeholder.PlayerStaticPlaceholder
import com.willfp.eco.core.placeholder.PlayerlessPlaceholder
import com.willfp.eco.core.placeholder.StaticPlaceholder
import com.willfp.eco.core.placeholder.context.PlaceholderContext
import com.willfp.eco.core.placeholder.context.placeholderContext
import com.willfp.eco.core.placeholder.templates.DynamicInjectablePlaceholder
import com.willfp.eco.core.registry.KRegistrable
import com.willfp.eco.util.*
import com.willfp.libreforge.ViolationContext
import com.willfp.libreforge.counters.Counters
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import ru.oftendev.itsmylevel.ItsMyLevel
import ru.oftendev.itsmylevel.api.getFormattedRequiredXP
import ru.oftendev.itsmylevel.api.getLevelLevel
import ru.oftendev.itsmylevel.api.getLevelProgress
import ru.oftendev.itsmylevel.api.getLevelXP
import ru.oftendev.itsmylevel.commands.dynamic.DynamicCommand
import ru.oftendev.itsmylevel.levels.placeholder.levelTopInjectable
import ru.oftendev.itsmylevel.rewards.LevelReward
import ru.oftendev.itsmylevel.target.ITarget
import ru.oftendev.itsmylevel.target.TargetType
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

class Level(
    override val id: String,
    val config: Config,
    val plugin: ItsMyLevel
) : KRegistrable {
    val injectables = mutableListOf<InjectablePlaceholder>()

    val local = config.getBool("local")
    val partyMode = TargetType.getById(config.getString("team"))
    val name = config.getString("name")
        get() = field.formatEco(null, true)
    private val xpGainMethods = config.getSubsections("gain-xp.methods").mapNotNull {
        Counters.compile(it, ViolationContext(plugin, "Level $id methods"))
    }
    private val requirements = config.getDoublesOrNull("xp-requirements")
    private val xpFormula = config.getStringOrNull("xp-formula")
    val maxLevel = config.getIntOrNull("max-level") ?: requirements?.size ?: Int.MAX_VALUE
    val toGiveKey: PersistentDataKey<List<String>> = PersistentDataKey(
        namespacedKeyOf("itsmylevel", "${id}_togive${if (local) "_"+plugin.configYml.getString("server-id") else ""}"),
        PersistentDataKeyType.STRING_LIST,
        listOf()
    )
    private val rewards = config.getSubsections("level-up.rewards").mapNotNull {
        LevelReward(
            it,
            this
        )
    }
    val commands = mutableListOf<DynamicCommand>()

    init {
        // %itsmylevel_<level>%
        // %itsmylevel_<level>_name%
        // %itsmylevel_<level>_current_xp%
        // %itsmylevel_<level>_current_xp_formated%
        // %itsmylevel_<level>_required_xp%
        // %itsmylevel_<level>_required_xp_formated%
        // %itsmylevel_<level>_percentage_progress%
        // %itsmylevel_<level>_leaderboard_<1-100>%
        // %itsmylevel_<level>_leaderboard_<1-100>_name% #Or team name if team mode
        // %itsmylevel_<level>_leaderboard_position%

        injectables.add(levelTopInjectable(this))

        config.injectPlaceholders(
            PlayerStaticPlaceholder("level") {
                getActualLevel(it).toString()
            }
        )

        PlayerDynamicPlaceholder(
            plugin,
            Pattern.compile("${this.id}_(leaderboard|top)_position")
        ) { _, player ->
            getTopFor(player).toNiceString()
        }.register().asInjectable(this)

        PlayerPlaceholder(
            plugin,
            "${id}_color"
        ) {
            val lvl = this.getSavedLevel(it)
            val color = this.config.getSubsections(
                "level-colors.colors"
            ).map { cfg -> LevelColor(cfg) }.filter { level -> level.level <= lvl }.maxByOrNull { level
                -> level.level }?.color
                ?: this.config.getFormattedString("level-colors.default")
            color
        }.register().asInjectable(this)

        PlayerPlaceholder(
            plugin,
            id
        ) {
            it.getLevelLevel(this).toString()
        }.register().asInjectable(this)

        PlayerlessPlaceholder(
            plugin,
            "${id}_name"
        ) {
            name
        }.register().asInjectable(this)

        PlayerPlaceholder(
            plugin,
            "${id}_current_xp"
        ) {
            it.getLevelXP(this).toNiceString()
        }.register().asInjectable(this)

        PlayerPlaceholder(
            plugin,
            "${id}_current_xp_formatted"
        ) {
            it.getLevelXP(this).toNiceString() // TODO
        }.register().asInjectable(this)

        PlayerPlaceholder(
            plugin,
            "${id}_required_xp"
        ) {
            it.getFormattedRequiredXP(this)
        }.register().asInjectable(this)

        PlayerPlaceholder(
            plugin,
            "${id}_required_xp_formatted"
        ) {
            it.getFormattedRequiredXP(this) // TODO
        }.register().asInjectable(this)

        PlayerPlaceholder(
            plugin,
            "${id}_percentage_progress"
        ) {
            (it.getLevelProgress(this)*100.0).toNiceString()
        }.register().asInjectable(this)

        PlayerPlaceholder(
            plugin,
            "${id}_name"
        ) {
            this.name
        }.register().asInjectable(this)

        config.injectPlaceholders(*injectables.toTypedArray())
    }

    fun getLevelKeyForPlayer(player: OfflinePlayer): PersistentDataKey<Int> {
        return PersistentDataKey(
            namespacedKeyOf("itsmylevel", "${partyMode.getParty(player).getUniqueId()}_level" +
                    if (local) "_"+plugin.configYml.getString("server-id") else ""
            ),
            PersistentDataKeyType.INT,
            0
        )
    }

    fun getInjectable(): PlaceholderInjectable {
        return object: PlaceholderInjectable {
            override fun addInjectablePlaceholder(p0: MutableIterable<InjectablePlaceholder>) {
                // pass
            }

            override fun clearInjectedPlaceholders() {
                // pass
            }

            override fun getPlaceholderInjections(): MutableList<InjectablePlaceholder> {
                return this@Level.injectables
            }
        }
    }

    fun getLevelKeyForTarget(target: ITarget): PersistentDataKey<Int> {
        return PersistentDataKey(
            namespacedKeyOf("itsmylevel", "${target.getUniqueId()}_level" +
                    if (local) "_"+plugin.configYml.getString("server-id") else ""
            ),
            PersistentDataKeyType.INT,
            0
        )
    }

    fun getXpKeyForPlayer(player: OfflinePlayer): PersistentDataKey<Double> {
        return PersistentDataKey(
            namespacedKeyOf("itsmylevel", "${partyMode.getParty(player).getUniqueId()}_xp"),
            PersistentDataKeyType.DOUBLE,
            0.0
        )
    }

    override fun getID() = id

    // Not the best way to do this, but it works!
    val leaderboardCache = Caffeine.newBuilder()
        .expireAfterWrite(1, TimeUnit.MINUTES)
        .build<Boolean, List<ITarget>> {
            Bukkit.getOfflinePlayers().map { p -> partyMode.getParty(p) }.distinctBy { it.getUniqueId() }
                .sortedByDescending {
                getSavedLevel(it)
            }
        }

    private val unformattedDescription = config.getString("description")

    internal open fun getActualLevel(player: OfflinePlayer) = getSavedLevel(player)

    internal fun getSavedLevel(player: OfflinePlayer) = ServerProfile.load().read(getLevelKeyForPlayer(player))
    internal fun setSavedLevel(player: OfflinePlayer, level: Int) = ServerProfile.load().write(
        getLevelKeyForPlayer(player),
        level
    )

    fun getSavedLevel(target: ITarget) = ServerProfile.load().read(getLevelKeyForTarget(target))

    fun addPlaceholdersInto(
        strings: List<String>,
        player: Player,
        level: Int = player.getLevelLevel(this)
    ): List<String> {
        val theLevel = this // I just hate the @ notation kotlin uses
        fun String.addPlaceholders() = this
            .replace("%percentage_progress%", (player.getLevelProgress(theLevel) * 100).toNiceString())
            .replace("%current_xp%", player.getLevelXP(theLevel).toNiceString())
            .replace("%required_xp%", theLevel.getFormattedXPRequired(level))
            .replace("%description%", theLevel.getDescription(level))
            .replace("%name%", theLevel.name)
            .let { addPlaceholdersInto(it, level) }

        // Replace placeholders in the strings with their actual values.
        val withPlaceholders = strings.map { it.addPlaceholders() }

        // Replace multi-line placeholders.
        val processed = withPlaceholders.flatMap { s ->
            val margin = s.length - s.trimStart().length

            if (s.contains("%rewards%")) {
                getRewardMessages(player, level)
                    .addMargin(margin)
            } else if (s.contains("%gui_lore%")) {
                config.getStrings("gui.lore")
                    .addMargin(margin)
            } else {
                listOf(s)
            }
        }.map { it.addPlaceholders() }

        return processed.formatEco(
            placeholderContext(
                player = player,
                injectable = getInjectable()
            )
        )
    }

    private fun List<String>.addMargin(margin: Int): List<String> {
        return this.map { s -> " ".repeat(margin) + s }
    }

    fun addPlaceholdersInto(string: String, level: Int, player: Player? = null): String {
        // This isn't the best way to do this, but it works!
        return string
            .replace("%itsmylevel_${id}_numeral%", level.toNumeral())
            .replace("%itsmylevel_${id}_description%", getDescription(level))
            .replace("%itsmylevel_${id}%", level.toString())
            .replace("%level%", level.toString())
            .replace("%level_numeral%", level.toNumeral())
            .replace("%previous_level%", (level - 1).toString())
            .replace("%previous_level_numeral%", (level - 1).toNumeral())
            .formatEco(
                placeholderContext(
                    player = player,
                    injectable = getInjectable()
                )
            )
    }

    fun getTop(position: Int): TargetLeaderboardEntry? {
        require(position > 0) { "Position must be greater than 0" }

        val target = leaderboardCache.get(true).getOrNull(position - 1) ?: return null

        return TargetLeaderboardEntry(
            target,
            getSavedLevel(target)
        )
    }

    fun getTopFor(player: OfflinePlayer): Int {
//        println("Leaderboard ${leaderboardCache.get(true).map { it.getUniqueId() }}")
//        println("Player ${partyMode.getParty(player).getUniqueId()}")
        return leaderboardCache.get(true).indexOfFirst {
            it.getUniqueId().equals(partyMode.getParty(player).getUniqueId(), true)
        } + 1
    }

    override fun onRegister() {
        val accumulator = LevelXPAccumulator(plugin, this)

        for (counter in xpGainMethods) {
            counter.bind(accumulator)
        }

        commands.addAll(
            config.getStrings("commands").map { DynamicCommand(plugin, it, this.id) }
        )

        registerCommands()
    }

    override fun onRemove() {
        for (counter in xpGainMethods) {
            counter.unbind()
        }

        unregisterCommands()
    }

    fun getXPRequired(level: Int): Double {
        if (xpFormula != null) {
            return evaluateExpression(
                xpFormula,
                placeholderContext(
                    injectable = LevelInjectable(level)
                )
            )
        }

        if (requirements != null) {
            return requirements.getOrNull(level) ?: Double.POSITIVE_INFINITY
        }

        return Double.POSITIVE_INFINITY
    }

    fun getRewardMessages(player: Player, level: Int): List<String> {
        val result = mutableListOf<String>()
        rewards.filter { it.matches(level) }.forEach {
            result += it.getRewardMessages(level, player)
        }
        return result
    }

    fun getFormattedXPRequired(level: Int): String {
        val required = getXPRequired(level)
        return if (required.isInfinite()) {
            plugin.langYml.getFormattedString("infinity")
        } else {
            required.toNiceString()
        }
    }

    fun processRewards(player: OfflinePlayer, level: Int) {
        rewards.forEach { it.proceed(player, level) }
    }

    fun registerCommands() {
        commands.forEach { it.register() }
    }

    fun unregisterCommands() {
        commands.forEach { it.unregister() }
    }

    internal fun getSavedXP(player: OfflinePlayer): Double = ServerProfile.load().read(getXpKeyForPlayer(player))
    internal fun setSavedXP(player: OfflinePlayer, xp: Double) = ServerProfile.load().write(
        getXpKeyForPlayer(player),
        xp
    )

    override fun equals(other: Any?): Boolean {
        return other is Level && other.id == id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    fun getDescription(level: Int): String {
        var desc = unformattedDescription

        val context = placeholderContext(
            injectable = LevelInjectable(level)
        )

        for (placeholder in loadDescriptionPlaceholders(config)) {
            val id = placeholder.id
            val value = evaluateExpression(placeholder.expr, context)
            desc = desc.replace("%$id%", value.toNiceString())
        }

        return desc.formatEco(context)
    }
}

fun PlayerPlaceholder.asInjectable(level: Level): InjectablePlaceholder {
    return PlayerStaticPlaceholder(
        this.pattern.toString().replace("${level}_", "")
    ) {
        "%itsmylevel_${this@asInjectable.pattern}%".formatEco(it)
    }.apply {
        level.injectables.add(this)
    }
}

fun PlayerlessPlaceholder.asInjectable(level: Level): InjectablePlaceholder {
    return StaticPlaceholder(
        this.pattern.toString().replace("${level}_", "")
    ) {
        "%itsmylevel_${this@asInjectable.pattern}%".formatEco()
    }.apply {
        level.injectables.add(this)
    }
}

fun PlayerDynamicPlaceholder.asInjectable(level: Level): InjectablePlaceholder {
    return object : DynamicInjectablePlaceholder(
        Pattern.compile(
            this.pattern.toString().replace("${level.id}_", "")
        )
    ) {
        override fun getValue(p0: String, p1: PlaceholderContext): String? {
            val player = p1.player ?: return null

            return "%itsmylevel_${level.id}_${p0}%".formatEco(player)
        }

    }.apply {
        level.injectables.add(this)
    }
}

fun Player.asContext(): PlaceholderContext {
    return PlaceholderContext(this)
}

internal val OfflinePlayer.levels: LevelMap
    get() = LevelMap(this)

