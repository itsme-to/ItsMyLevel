package ru.oftendev.itsmylevel.api.event

import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerEvent
import ru.oftendev.itsmylevel.levels.Level

abstract class PlayerLevelEvent(
    who: Player,
    override val level: Level
) : PlayerEvent(who), LevelEvent
