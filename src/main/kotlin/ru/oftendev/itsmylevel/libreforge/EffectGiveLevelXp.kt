package ru.oftendev.itsmylevel.libreforge

import com.willfp.eco.core.config.interfaces.Config
import com.willfp.libreforge.NoCompileData
import com.willfp.libreforge.arguments
import com.willfp.libreforge.effects.Effect
import com.willfp.libreforge.getDoubleFromExpression
import com.willfp.libreforge.triggers.TriggerData
import com.willfp.libreforge.triggers.TriggerParameter
import ru.oftendev.itsmylevel.api.giveLevelXP
import ru.oftendev.itsmylevel.levels.Levels

object EffectGiveLevelXp : Effect<NoCompileData>("give_level_xp") {
    override val parameters = setOf(
        TriggerParameter.PLAYER
    )

    override val arguments = arguments {
        require("amount", "You must specify the amount of xp to give!")
        require("level", "You must specify the level to give xp for!")
    }

    override fun onTrigger(config: Config, data: TriggerData, compileData: NoCompileData): Boolean {
        val player = data.player ?: return false

        val skill = Levels.getByID(config.getString("level")) ?: return false

        player.giveLevelXP(skill, config.getDoubleFromExpression("amount", data))

        return true
    }
}
