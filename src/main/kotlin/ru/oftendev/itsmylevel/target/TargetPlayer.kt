package ru.oftendev.itsmylevel.target

import com.willfp.eco.util.savedDisplayName
import com.willfp.eco.util.toSingletonList
import org.bukkit.OfflinePlayer

class TargetPlayer(
    override val player: OfflinePlayer,
    override val targetType: TargetType = TargetType.PLAYER
) : ITarget {
    override fun getUniqueId(): String {
        return player.uniqueId.toString()
    }

    override fun getApplicablePlayers(): Collection<OfflinePlayer> {
        return player.toSingletonList()
    }

    override fun getName(): String {
        return player.savedDisplayName
    }
}