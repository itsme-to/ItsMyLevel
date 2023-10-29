package ru.oftendev.itsmylevel.levels

import com.github.benmanes.caffeine.cache.Caffeine
import com.willfp.eco.core.config.interfaces.Config
import com.willfp.libreforge.loader.LibreforgePlugin
import org.bukkit.Bukkit
import org.bukkit.configuration.InvalidConfigurationException
import ru.oftendev.itsmylevel.CategoryWithRegistry
import ru.oftendev.itsmylevel.ItsMyLevel
import ru.oftendev.itsmylevel.api.totalLevelLevel
import java.util.*
import java.util.concurrent.TimeUnit

object Levels : CategoryWithRegistry<Level>("level", "levels") {
    private val leaderboardCache = Caffeine.newBuilder()
        .expireAfterWrite(1, TimeUnit.MINUTES)
        .build<Boolean, List<UUID>> {
            Bukkit.getOfflinePlayers().sortedByDescending {
                it.totalLevelLevel
            }.map { it.uniqueId }
        }

    fun getTop(position: Int): LeaderboardEntry? {
        require(position > 0) { "Position must be greater than 0" }

        val uuid = leaderboardCache.get(true).getOrNull(position - 1) ?: return null

        val player = Bukkit.getOfflinePlayer(uuid).takeIf { it.hasPlayedBefore() } ?: return null

        return LeaderboardEntry(
            player,
            player.totalLevelLevel
        )
    }

    override fun acceptConfig(plugin: LibreforgePlugin, id: String, config: Config) {
        try {
            registry.register(Level(id, config, plugin as ItsMyLevel))
        } catch (e: InvalidConfigurationException) {
            plugin.logger.warning("Failed to load level $id: ${e.message}")
        }
    }

    override fun clear(plugin: LibreforgePlugin) {
        registry.clear()
    }
}