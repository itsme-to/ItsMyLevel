package ru.oftendev.itsmylevel.rewards

import com.willfp.eco.core.config.interfaces.Config
import com.willfp.eco.core.data.keys.PersistentDataKey
import com.willfp.eco.core.data.keys.PersistentDataKeyType
import com.willfp.eco.core.data.profile
import com.willfp.eco.core.map.defaultMap
import com.willfp.eco.core.placeholder.context.placeholderContext
import com.willfp.eco.util.*
import com.willfp.libreforge.ViolationContext
import com.willfp.libreforge.effects.Chain
import com.willfp.libreforge.effects.EffectList
import com.willfp.libreforge.effects.Effects
import com.willfp.libreforge.effects.executors.impl.NormalExecutorFactory
import com.willfp.libreforge.triggers.TriggerData
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import ru.oftendev.itsmylevel.api.getFormattedRequiredXP
import ru.oftendev.itsmylevel.api.getLevelLevel
import ru.oftendev.itsmylevel.api.getLevelProgress
import ru.oftendev.itsmylevel.api.getLevelXP
import ru.oftendev.itsmylevel.levels.Level
import ru.oftendev.itsmylevel.levels.LevelInjectable
import ru.oftendev.itsmylevel.levels.loadDescriptionPlaceholders
import ru.oftendev.itsmylevel.plugin
import ru.oftendev.itsmylevel.target.TargetType

class LevelReward(val config: Config, val parent: Level) {
    val effects: Chain? = Effects.compileChain(
        config.getSubsections("effects"),
        NormalExecutorFactory.create(),
        ViolationContext(
            plugin,
            "${parent.id} reward"
        )
    )

    val messages = config.getStrings("message")

    val levels = config.getInt("levels")
    val startLevel = config.getIntOrNull("start-level")
    val endLevel = config.getIntOrNull("end-level")
    val isMutual = config.getBool("mutual")
    val every = config.getIntOrNull("every") ?: 1

    fun matches(level: Int): Boolean {
        return if (startLevel != null && endLevel != null) {
            level in startLevel..endLevel && ((level-startLevel) % every == 0)
        } else {
            val start = startLevel ?: 0
            val end = start + levels
            level in start..end && ((level-start) % every == 0)
        }
    }

    private fun addPlaceholdersInto(string: String, level: Int): String {
        // This isn't the best way to do this, but it works!
        return string
            .replace("%itsmylevel_${parent.id}_numeral%", level.toNumeral())
//            .replace("%itsmylevel_${parent.id}_description%", getDescription(level))
            .replace("%itsmylevel_${parent.id}%", level.toString())
            .replace("%level%", level.toString())
            .replace("%level_numeral%", level.toNumeral())
            .replace("%previous_level%", (level - 1).toString())
            .replace("%previous_level_numeral%", (level - 1).toNumeral())
    }

    private fun List<String>.addMargin(margin: Int): List<String> {
        return this.map { s -> " ".repeat(margin) + s }
    }

    /**
     * Get the reward messages for a certain [level].
     */
    fun getRewardMessages(
        level: Int,
        player: Player
    ): List<String> {
        val context = placeholderContext(
            injectable = LevelInjectable(level),
            player = player
        )

        for (placeholder in loadDescriptionPlaceholders(parent.config)) {
            val id = placeholder.id
            val value = evaluateExpression(placeholder.expr, context)

            messages.replaceAll { s -> s.replace("%$id%", value.toNiceString()) }
        }

        return messages
    }

    fun proceed(player: OfflinePlayer, level: Int) {
        if (!matches(level)) return

        val toProceedOn = if (!isMutual || !parent.partyMode.isMutual) {
            listOf(player)
        } else {
            parent.partyMode.getParty(player).getApplicablePlayers()
        }

        for (toProceed in toProceedOn) {
            proceedOn(toProceed, level)
        }
    }

    private fun proceedOn(player: OfflinePlayer, level: Int) {
        if (player.player == null) {
            val current = player.getRewardsToGive(parent)
            current += level
            player.setRewardsToGive(parent, current.distinct())
            return
        }
        val online = player.player ?: return
        /*getRewardMessages(level, online).forEach {
            online.sendMessage(it)
        }*/

        val data = TriggerData(
            player = online,
            text = parent.id,
            value = level.toDouble()
        )

        this.effects?.trigger(
            data.dispatch(online)
        )
    }
}

fun OfflinePlayer.getRewardsToGive(level: Level): MutableList<Int> {
    return this.profile.read(level.toGiveKey).mapNotNull { it.toIntOrNull() }.toMutableList()
}

fun OfflinePlayer.setRewardsToGive(level: Level, value: List<Int>) {
    this.profile.write(level.toGiveKey, value.map { it.toString() })
}

val TargetType.isMutual: Boolean
    get() = this !in listOf(TargetType.EMPTY, TargetType.PLAYER)