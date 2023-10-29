@file:JvmName("ItsMyLevelAPI")

package ru.oftendev.itsmylevel.api


import org.bukkit.OfflinePlayer
import ru.oftendev.itsmylevel.levels.Level
import ru.oftendev.itsmylevel.levels.Levels
import ru.oftendev.itsmylevel.levels.levels

/*

Levels

 */

fun OfflinePlayer.resetLevels() {
    for (level in Levels.values()) {
        this.levels.reset(level)
    }
}

fun OfflinePlayer.resetLevel(level: Level) {
    this.levels.reset(level)
}

fun OfflinePlayer.getLevelXP(level: Level): Double =
    this.levels[level].xp

fun OfflinePlayer.gainLevelXP(level: Level, xp: Double): Unit =
    this.levels.gainXP(level, xp)

fun OfflinePlayer.giveLevelXP(level: Level, xp: Double): Unit =
    this.levels.giveXP(level, xp)

fun OfflinePlayer.getRequiredXP(level: Level) =
    level.getXPRequired(this.getLevelLevel(level))

fun OfflinePlayer.getFormattedRequiredXP(level: Level) =
    level.getFormattedXPRequired(this.getLevelLevel(level))

fun OfflinePlayer.getLevelProgress(level: Level): Double {
    val currentXP = getLevelXP(level)
    val requiredXP = getRequiredXP(level)

    return currentXP / requiredXP
}

fun OfflinePlayer.getLevelLevel(level: Level): Int =
    this.levels[level].level

val OfflinePlayer.totalLevelLevel: Int
    get() = Levels.values().sumOf { this.getLevelLevel(it) }

val OfflinePlayer.averageLevelLevel: Double
    get() = this.totalLevelLevel.toDouble() / Levels.values().size
