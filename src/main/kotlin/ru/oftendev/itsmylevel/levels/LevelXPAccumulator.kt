package ru.oftendev.itsmylevel.levels

import com.github.benmanes.caffeine.cache.Caffeine
import com.willfp.eco.core.EcoPlugin
import com.willfp.eco.core.integrations.afk.AFKManager
import com.willfp.libreforge.counters.Accumulator
import org.bukkit.GameMode
import org.bukkit.entity.Player
import ru.oftendev.itsmylevel.api.gainLevelXP
import java.util.concurrent.TimeUnit
import kotlin.math.max

class LevelXPAccumulator(
    private val plugin: EcoPlugin,
    private val skill: Level
) : Accumulator {
    override fun accept(player: Player, count: Double) {
        if (skill.config.getBool("prevent-levelling-while-afk") && AFKManager.isAfk(player)) {
            return
        }

        /*if (player.gameMode in setOf(GameMode.CREATIVE, GameMode.SPECTATOR)) {
            return
        }*/

        player.gainLevelXP(skill, count)
    }
}


private val xpMultiplierCache = Caffeine.newBuilder().expireAfterWrite(10, TimeUnit.SECONDS).build<Player, Double> {
    it.cacheSkillXPMultiplier()
}

val Player.skillXPMultiplier: Double
    get() = xpMultiplierCache.get(this)

private fun Player.cacheSkillXPMultiplier(): Double {
    if (this.hasPermission("itsmylevel.xpmultiplier.quadruple")) {
        return 4.0
    }

    if (this.hasPermission("itsmylevel.xpmultiplier.triple")) {
        return 3.0
    }

    if (this.hasPermission("itsmylevel.xpmultiplier.double")) {
        return 2.0
    }

    if (this.hasPermission("itsmylevel.xpmultiplier.50percent")) {
        return 1.5
    }

    return 1 + getNumericalPermission("itsmylevel.xpmultiplier", 0.0) / 100
}

fun Player.getNumericalPermission(permission: String, default: Double): Double {
    var highest: Double? = null

    for (permissionAttachmentInfo in this.effectivePermissions) {
        val perm = permissionAttachmentInfo.permission
        if (perm.startsWith(permission)) {
            val found = perm.substring(perm.lastIndexOf(".") + 1).toDoubleOrNull() ?: continue
            highest = max(highest ?: Double.MIN_VALUE, found)
        }
    }

    return highest ?: default
}
