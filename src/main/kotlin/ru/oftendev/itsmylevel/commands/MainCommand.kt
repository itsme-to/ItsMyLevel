package ru.oftendev.itsmylevel.commands

import com.willfp.eco.core.EcoPlugin
import com.willfp.eco.core.command.impl.PluginCommand
import org.bukkit.command.CommandSender

class MainCommand(plugin: EcoPlugin) : PluginCommand(
    plugin,
    "itsmylevel",
    "itsmylevel.use",
    false
) {
    init {
        this.addSubcommand(GiveCommand(plugin))
            .addSubcommand(ReloadCommand(plugin))
            .addSubcommand(GiveCommand(plugin))
            .addSubcommand(CommandToggleXpGainSound(plugin))
            .addSubcommand(ResetCommand(plugin))
            .addSubcommand(ResetAllCommand(plugin))
    }

    override fun onExecute(sender: CommandSender, args: MutableList<String>) {
        sender.sendMessage(plugin.langYml.getMessage("invalid-command"))
    }

    override fun getAliases(): MutableList<String> {
        return mutableListOf(
            "iml",
            "levels"
        )
    }
}