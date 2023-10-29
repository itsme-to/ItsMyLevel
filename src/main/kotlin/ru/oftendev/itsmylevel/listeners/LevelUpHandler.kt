package ru.oftendev.itsmylevel.listeners

import com.willfp.eco.core.EcoPlugin
import com.willfp.eco.core.sound.PlayableSound
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import ru.oftendev.itsmylevel.api.event.PlayerLevelLevelUpEvent
import ru.oftendev.itsmylevel.levels.Levels

class LevelUpHandler(private val plugin: EcoPlugin) : Listener {
    @EventHandler
    fun handle(event: PlayerJoinEvent) {
        Levels.values().forEach { it.leaderboardCache.invalidate(true) }
    }

    @EventHandler
    fun handle(event: PlayerLevelLevelUpEvent) {
        val player = event.player
        val theLevel = event.level
        val level = event.theLevel
        val sound = if (theLevel.config.getBool("level-up.sound.enabled")) {
            PlayableSound.create(
                theLevel.config.getSubsection("level-up.sound")
            )
        } else null

        if (theLevel.config.getBool("level-up.message.enabled")) {
            val rawMessage = theLevel.config.getStrings("level-up.message.message")

            val formatted = theLevel.addPlaceholdersInto(
                rawMessage,
                player,
                level = level
            )

            formatted.forEach { player.sendMessage(it) }
        }

        sound?.playTo(player)
    }
}