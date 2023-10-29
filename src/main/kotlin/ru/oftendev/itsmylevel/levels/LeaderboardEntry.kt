package ru.oftendev.itsmylevel.levels

import org.bukkit.OfflinePlayer
import ru.oftendev.itsmylevel.target.ITarget

data class LeaderboardEntry(
    val player: OfflinePlayer,
    val level: Int
)

data class TargetLeaderboardEntry(
    val target: ITarget,
    val level: Int
)
