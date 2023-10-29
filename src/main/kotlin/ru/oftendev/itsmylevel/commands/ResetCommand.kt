package ru.oftendev.itsmylevel.commands

import com.willfp.eco.core.EcoPlugin
import com.willfp.eco.core.command.impl.Subcommand
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.util.StringUtil
import ru.oftendev.itsmylevel.api.resetLevel
import ru.oftendev.itsmylevel.levels.Levels

class ResetCommand(plugin: EcoPlugin) : Subcommand(
    plugin,
    "reset",
    "itsmylevel.reset",
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

        val level = Levels.getByID(args.getOrNull(1) ?: run {
            sender.sendMessage(plugin.langYml.getMessage("requires-level"))
            return
        }
        ) ?: run {
            sender.sendMessage(plugin.langYml.getMessage("invalid-level"))
            return
        }

        player.resetLevel(level)

        sender.sendMessage(
            plugin.langYml.getMessage("level-reset")
                .replace("%playername%", player.name)
                .replace("%level%", level.id)
        )
    }

    override fun tabComplete(sender: CommandSender, args: MutableList<String>): MutableList<String> {
        return when (args.size) {
            1 -> StringUtil.copyPartialMatches(args.first(), Bukkit.getOnlinePlayers().map { it.name }, mutableListOf())
            2 -> StringUtil.copyPartialMatches(args[1], Levels.values().map { it.id }, mutableListOf())
            else -> mutableListOf()
        }
    }
}