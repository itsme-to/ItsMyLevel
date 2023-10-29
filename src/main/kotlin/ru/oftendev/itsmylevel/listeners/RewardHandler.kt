package ru.oftendev.itsmylevel.listeners

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import ru.oftendev.itsmylevel.levels.Levels
import ru.oftendev.itsmylevel.plugin
import ru.oftendev.itsmylevel.rewards.getRewardsToGive
import ru.oftendev.itsmylevel.rewards.setRewardsToGive

class RewardHandler : Listener {
    @EventHandler
    fun handle(event: PlayerJoinEvent) {
        plugin.scheduler.runLater(2) {
            Levels.values().forEach {
                event.player.getRewardsToGive(it).forEach { lvl ->
                    it.processRewards(event.player, lvl)
                }
                event.player.setRewardsToGive(it, listOf())
            }
        }
    }
}