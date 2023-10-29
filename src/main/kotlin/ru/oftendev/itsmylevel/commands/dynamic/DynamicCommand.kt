package ru.oftendev.itsmylevel.commands.dynamic

import com.willfp.eco.core.EcoPlugin
import com.willfp.eco.core.command.impl.PluginCommand
import com.willfp.eco.core.config.interfaces.Config
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import ru.oftendev.itsmylevel.gui.LevelGUI
import ru.oftendev.itsmylevel.levels.Levels

class DynamicCommand(plugin: EcoPlugin, val config: Config) : PluginCommand(
    plugin,
    config.getString("command"),
    config.getString("permission"),
    true
) {



    override fun onExecute(sender: CommandSender, args: MutableList<String>) {
    }
}