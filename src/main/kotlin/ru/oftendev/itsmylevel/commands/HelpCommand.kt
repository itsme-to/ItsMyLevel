package ru.oftendev.itsmylevel.commands

import com.willfp.eco.core.EcoPlugin
import com.willfp.eco.core.command.impl.Subcommand
import org.bukkit.command.CommandSender

class HelpCommand(plugin: EcoPlugin) : Subcommand(
    plugin,
    "help",
    "itsmylevel.help",
    false
) {
    override fun onExecute(sender: CommandSender, args: MutableList<String>) {
        plugin.langYml.getFormattedStrings("help")
            .forEach {
                sender.sendMessage(it)
            }
    }
}