package ru.oftendev.itsmylevel.libreforge

import com.willfp.libreforge.effects.templates.MultiMultiplierEffect
import org.bukkit.event.EventHandler
import ru.oftendev.itsmylevel.api.event.PlayerLevelXPGainEvent
import ru.oftendev.itsmylevel.levels.Level
import ru.oftendev.itsmylevel.levels.Levels

object EffectLevelXpMultiplier : MultiMultiplierEffect<Level>("level_xp_multiplier") {
    override val key = "levels"

    override fun getElement(key: String): Level? {
        return Levels.getByID(key)
    }

    override fun getAllElements(): Collection<Level> {
        return Levels.values()
    }

    @EventHandler(ignoreCancelled = true)
    fun handle(event: PlayerLevelXPGainEvent) {
        val player = event.player

        event.gainedXP *= getMultiplier(player, event.level)
    }
}
