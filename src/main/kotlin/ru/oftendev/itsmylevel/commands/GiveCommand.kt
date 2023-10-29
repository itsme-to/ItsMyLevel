package ru.oftendev.itsmylevel.commands

import com.willfp.eco.core.EcoPlugin
import com.willfp.eco.core.command.impl.Subcommand
import com.willfp.eco.util.toNiceString
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.util.StringUtil
import ru.oftendev.itsmylevel.api.giveLevelXP
import ru.oftendev.itsmylevel.levels.Levels

class GiveCommand(plugin: EcoPlugin) : Subcommand(
    plugin,
    "give",
    "itsmylevel.give",
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

        val amount = (args.getOrNull(2) ?: run {
            sender.sendMessage(plugin.langYml.getMessage("requires-value"))
            return
        }).toDoubleOrNull() ?: run {
            sender.sendMessage(plugin.langYml.getMessage("invalid-value"))
            return
        }

        player.giveLevelXP(level, amount)

        sender.sendMessage(
            plugin.langYml.getMessage("exp-given")
                .replace("%playername%", player.name)
                .replace("%level%", level.id)
                .replace("%amount%", amount.toNiceString())
        )
    }

    override fun tabComplete(sender: CommandSender, args: MutableList<String>): MutableList<String> {
        return when (args.size) {
            1 -> StringUtil.copyPartialMatches(args.first(), Bukkit.getOnlinePlayers().map { it.name }, mutableListOf())
            2 -> StringUtil.copyPartialMatches(args[1], Levels.values().map { it.id }, mutableListOf())
            3 -> mutableListOf("10", "100", "1000", "10000")
            else -> mutableListOf()
        }
    }
}