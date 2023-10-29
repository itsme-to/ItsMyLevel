package ru.oftendev.itsmylevel.commands

import com.willfp.eco.core.EcoPlugin
import com.willfp.eco.core.command.impl.Subcommand
import org.bukkit.entity.Player
import ru.oftendev.itsmylevel.listeners.isXPGainSoundEnabled
import ru.oftendev.itsmylevel.listeners.toggleXPGainSound

class CommandToggleXpGainSound(plugin: EcoPlugin) : Subcommand(
    plugin, "togglexpgainsound", "itsmylevel.togglexpgainsound", true
) {

    override fun onExecute(player: Player, args: List<String>) {
        when (player.isXPGainSoundEnabled) {
            true -> {
                player.sendMessage(plugin.langYml.getMessage("disabled-xp-gain-sound"))
            }

            false -> player.sendMessage(plugin.langYml.getMessage("enabled-xp-gain-sound"))
        }

        player.toggleXPGainSound()
    }
}
