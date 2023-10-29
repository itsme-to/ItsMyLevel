package ru.oftendev.itsmylevel.levels

import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import ru.oftendev.itsmylevel.api.event.PlayerLevelLevelUpEvent
import ru.oftendev.itsmylevel.api.event.PlayerLevelXPGainEvent

class LevelMap(
    private val player: OfflinePlayer
) {
    operator fun get(level: Level): LevelLevel {
        return LevelLevel(
            level.getSavedLevel(player),
            level.getSavedXP(player)
        )
    }

    operator fun set(level: Level, levell: LevelLevel) {
        require(levell.level >= 0) { "Level must be positive" }
        require(levell.xp >= 0) { "XP must be positive" }

        level.setSavedLevel(player, levell.level)
        level.setSavedXP(player, levell.xp)
    }

    fun giveXP(level: Level, xp: Double) {
        require(xp >= 0) { "XP must be positive" }

        val current = this[level]

        val required = level.getXPRequired(current.level)

        return if (current.xp + xp >= required && current.level < level.maxLevel) {
            val overshoot = current.xp + xp - required

            this[level] = LevelLevel(
                current.level + 1,
                0.0
            )

            if (player is Player) {
                Bukkit.getPluginManager().callEvent(
                    PlayerLevelLevelUpEvent(
                        player,
                        level,
                        current.level + 1
                    )
                )
            }

            level.processRewards(player, current.level + 1)

            giveXP(level, overshoot) // For recursive level gains.
        } else {
            this[level] = LevelLevel(
                current.level,
                current.xp + xp
            )
        }
    }

    fun gainXP(skill: Level, xp: Double) {
        require(xp >= 0) { "XP must be positive" }

        if (player.player != null) {
            val event = PlayerLevelXPGainEvent(
                player.player!!,
                skill,
                xp * player.player!!.skillXPMultiplier
            )

            Bukkit.getPluginManager().callEvent(event)

            if (!event.isCancelled) {
                giveXP(skill, event.gainedXP)
            }
        } else {
            giveXP(skill, xp)
        }
    }

    fun reset(level: Level) {
        this[level] = LevelLevel(
            0,
            0.0
        )
    }
}
