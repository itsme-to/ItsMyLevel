package ru.oftendev.itsmylevel.commands

import com.willfp.eco.core.EcoPlugin
import com.willfp.eco.core.command.impl.Subcommand
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.util.StringUtil
import ru.oftendev.itsmylevel.api.resetLevels

class ResetAllCommand(plugin: EcoPlugin) : Subcommand(
    plugin,
    "resetall",
    "itsmylevel.resetall",
    false
) {
    override fun onExecute(sender: CommandSender, args: MutableList<String>) {
        val player = Bukkit.getPlayer(args.firstOrNull() ?: run {
            sender.sendMessage(plugin.langYml.getMessage("requires-player"))
            return
        }
        ) ?: run {
            sender.sendMessage(plugin.langYml.getMessage("invalid-player"))
            return
        }

        player.resetLevels()

        sender.sendMessage(
            plugin.langYml.getMessage("levels-reset")
                .replace("%playername%", player.name)
        )
    }

    override fun tabComplete(sender: CommandSender, args: MutableList<String>): MutableList<String> {
        return when (args.size) {
            1 -> StringUtil.copyPartialMatches(args.first(), Bukkit.getOnlinePlayers().map { it.name }, mutableListOf())
            else -> mutableListOf()
        }
    }
}