package ru.oftendev.itsmylevel

import com.willfp.eco.core.command.impl.PluginCommand
import com.willfp.libreforge.effects.Effects
import com.willfp.libreforge.loader.LibreforgePlugin
import com.willfp.libreforge.loader.configs.ConfigCategory
import org.bukkit.event.Listener
import ru.oftendev.itsmylevel.commands.MainCommand
import ru.oftendev.itsmylevel.levels.Levels
import ru.oftendev.itsmylevel.levels.placeholder.ItsMyLevelLevelTopPlaceholder
import ru.oftendev.itsmylevel.levels.placeholder.ItsMyLevelTopPlaceholder
import ru.oftendev.itsmylevel.libreforge.EffectGiveLevelXp
import ru.oftendev.itsmylevel.libreforge.EffectGiveLevelXpNaturally
import ru.oftendev.itsmylevel.libreforge.EffectLevelXpMultiplier
import ru.oftendev.itsmylevel.listeners.GainXPDisplay
import ru.oftendev.itsmylevel.listeners.LevelUpHandler
import ru.oftendev.itsmylevel.listeners.RewardHandler
import ru.oftendev.itsmylevel.listeners.TemporaryBossBarHandler

lateinit var plugin: ItsMyLevel
    private set

class ItsMyLevel : LibreforgePlugin() {
    init {
        plugin = this
    }

    override fun handleEnable() {
        ItsMyLevelTopPlaceholder(this).register()
        ItsMyLevelLevelTopPlaceholder(this).register()

        regadd

        Effects.register(EffectGiveLevelXp)
        Effects.register(EffectGiveLevelXpNaturally)
        Effects.register(EffectLevelXpMultiplier)
    }

    override fun loadListeners(): MutableList<Listener> {
        return mutableListOf(
            GainXPDisplay(this),
            LevelUpHandler(this),
            RewardHandler()
        )
    }

    override fun loadPluginCommands(): MutableList<PluginCommand> {
        return mutableListOf(
            MainCommand(this)
        )
    }

    override fun loadConfigCategories(): List<ConfigCategory> {
        return listOf(
            Levels
        )
    }

    override fun createTasks() {
        TemporaryBossBarHandler(this).startTicking()
    }
}