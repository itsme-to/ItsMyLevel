package ru.oftendev.itsmylevel.commands

import com.willfp.eco.core.EcoPlugin
import com.willfp.eco.core.command.impl.Subcommand
import org.bukkit.command.CommandSender

class ReloadCommand(plugin: EcoPlugin) : Subcommand(
    plugin,
    "reload",
    "itsmylevel.reload",
    false
) {
    override fun onExecute(sender: CommandSender, args: MutableList<String>) {
        this.plugin.reload()
        sender.sendMessage(plugin.langYml.getMessage("reloaded"))
    }
}