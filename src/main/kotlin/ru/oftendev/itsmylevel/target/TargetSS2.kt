package ru.oftendev.itsmylevel.target

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI
import com.willfp.eco.core.placeholder.StaticPlaceholder
import com.willfp.eco.util.savedDisplayName
import org.bukkit.OfflinePlayer

class TargetSS2(
    override val player: OfflinePlayer,
    override val targetType: TargetType = TargetType.SS2
) : ITarget {
    override fun getUniqueId(): String {
        return SuperiorSkyblockAPI.getPlayer(player.uniqueId).island?.uniqueId?.toString() ?: "emptyisland"
    }

    override fun getApplicablePlayers(): Collection<OfflinePlayer> {
        return SuperiorSkyblockAPI.getPlayer(player.uniqueId).island
            ?.getIslandMembers(true)?.mapNotNull { it.asOfflinePlayer() } ?: emptyList()
    }

    override fun getName(): String {
        val name = SuperiorSkyblockAPI.getPlayer(player.uniqueId).island?.name

        return if (name != null && name.isEmpty()) {
            SuperiorSkyblockAPI.getPlayer(player.uniqueId).island?.owner?.name ?: "Null"
        } else {
            SuperiorSkyblockAPI.getPlayer(player.uniqueId).island?.name ?: "Null"
        }
    }
}