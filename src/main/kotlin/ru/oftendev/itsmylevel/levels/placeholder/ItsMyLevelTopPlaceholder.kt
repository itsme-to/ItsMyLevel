package ru.oftendev.itsmylevel.levels.placeholder

import com.willfp.eco.core.EcoPlugin
import com.willfp.eco.core.placeholder.InjectablePlaceholder
import com.willfp.eco.core.placeholder.RegistrablePlaceholder
import com.willfp.eco.core.placeholder.context.PlaceholderContext
import com.willfp.eco.core.placeholder.templates.DynamicInjectablePlaceholder
import com.willfp.eco.util.formatEco
import com.willfp.eco.util.savedDisplayName
import org.bukkit.ChatColor
import ru.oftendev.itsmylevel.levels.Level
import ru.oftendev.itsmylevel.levels.LevelColor
import ru.oftendev.itsmylevel.levels.Levels
import ru.oftendev.itsmylevel.levels.asInjectable
import ru.oftendev.itsmylevel.plugin
import java.util.regex.Pattern

class ItsMyLevelLevelTopPlaceholder(
    private val plugin: EcoPlugin
) : RegistrablePlaceholder {
    private val pattern = Pattern.compile("[a-z]+_(leaderboard_|top_)[0-9]+_*[a-z]*")

    override fun getPattern(): Pattern = pattern
    override fun getPlugin(): EcoPlugin = plugin

    override fun getValue(params: String, ctx: PlaceholderContext): String? {
        val emptyposition: String = plugin.langYml.getString("empty.name")
        val emptyposition2: String = plugin.langYml.getString("empty.value")
        val args = params.split("_")
        
        val skillString = args.getOrNull(0) ?: return null
        val placeString = args.getOrNull(2) ?: return null
        val modeString = args.getOrNull(3) ?: "name"
        val colored = args.getOrElse(4) { "" }.equals("color", true)

        val skill = Levels.getByID(skillString) ?: return null

        val place = placeString.toIntOrNull() ?: return null

        val target = skill.getTop(place)

        val color = if (colored && target != null) {
            val lvl = skill.getSavedLevel(target.target)
            skill.config.getSubsections(
                "level-colors.colors"
            ).map { LevelColor(it) }.filter { it.level <= lvl }.maxByOrNull { it.level }?.color
                ?: skill.config.getFormattedString("level-colors.default")
        } else null

        val name = if (target != null) {
            if (colored) {
                color + target.target.getName().stripColors()
            } else target.target.getName()
        } else emptyposition

        val level = if (target != null) {
            if (colored) {
                color + target.level.toString().stripColors()
            } else target.level.toString()
        } else emptyposition2

        return when (modeString) {
            "name" -> name
            "level", "amount" -> level
            else -> null
        }
    }
}

fun levelTopInjectable(level: Level): InjectablePlaceholder {
    val th = ItsMyLevelLevelTopPlaceholder(plugin)

    return object : DynamicInjectablePlaceholder(
        Pattern.compile(
            th.pattern.toString().replace("[a-z]+_", "")
        )
    ) {
        override fun getValue(p0: String, p1: PlaceholderContext): String? {
            return "%itsmylevel_${level.id}_${p0}%".formatEco(formatPlaceholders = true)
        }

    }.apply {
        level.injectables.add(this)
    }
}

class ItsMyLevelTopPlaceholder(
    private val plugin: EcoPlugin
) : RegistrablePlaceholder {
    private val pattern = Pattern.compile("(leaderboard_|top_)[0-9]+_*[a-z]*")

    override fun getPattern(): Pattern = pattern
    override fun getPlugin(): EcoPlugin = plugin

    override fun getValue(params: String, ctx: PlaceholderContext): String? {
        val emptyposition: String = plugin.langYml.getString("empty.name")
        val emptyposition2: String = plugin.langYml.getString("empty.value")
        val args = params.split("_")

        if (args.size < 2) {
            return null
        }

        if (args[0] != "leaderboard") {
            return null
        }

        val place = args[1].toIntOrNull() ?: return null

        return when (args.lastOrNull() ?: "name") {
            "name" -> Levels.getTop(place)?.player?.savedDisplayName ?: emptyposition
            "level", "amount" -> Levels.getTop(place)?.level?.toString() ?: emptyposition2
            else -> null
        }
    }
}

fun String.stripColors(): String {
    return ChatColor.stripColor(this) ?: ""
}
