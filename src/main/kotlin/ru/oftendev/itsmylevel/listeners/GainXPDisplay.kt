package ru.oftendev.itsmylevel.listeners

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import com.willfp.eco.core.EcoPlugin
import com.willfp.eco.core.data.keys.PersistentDataKey
import com.willfp.eco.core.data.keys.PersistentDataKeyType
import com.willfp.eco.core.data.profile
import com.willfp.eco.core.placeholder.InjectablePlaceholder
import com.willfp.eco.core.placeholder.PlaceholderInjectable
import com.willfp.eco.core.placeholder.PlayerStaticPlaceholder
import com.willfp.eco.core.placeholder.context.placeholderContext
import com.willfp.eco.core.sound.PlayableSound
import com.willfp.eco.util.formatEco
import com.willfp.eco.util.namespacedKeyOf
import com.willfp.eco.util.toNiceString
import org.bukkit.attribute.Attribute
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import ru.oftendev.itsmylevel.api.event.PlayerLevelXPGainEvent
import ru.oftendev.itsmylevel.api.getFormattedRequiredXP
import ru.oftendev.itsmylevel.api.getLevelProgress
import ru.oftendev.itsmylevel.api.getLevelXP
import ru.oftendev.itsmylevel.levels.Level
import java.time.Duration

private val xpGainSoundEnabledKey = PersistentDataKey(
    namespacedKeyOf("itsmylevel", "gain_sound_enabled"),
    PersistentDataKeyType.BOOLEAN,
    true
)

fun Player.toggleXPGainSound() {
    this.profile.write(xpGainSoundEnabledKey, !this.profile.read(xpGainSoundEnabledKey))
}

val Player.isXPGainSoundEnabled: Boolean
    get() = this.profile.read(xpGainSoundEnabledKey)

class GainXPDisplay(
    private val plugin: EcoPlugin
) : Listener {
    private val gainCache: Cache<Level, Double> = Caffeine.newBuilder().expireAfterWrite(Duration.ofSeconds(3))
        .build()

    @EventHandler
    fun handle(event: PlayerLevelXPGainEvent) {
        val current = gainCache.get(event.level) { 0.0 }
        gainCache.put(event.level, current + event.gainedXP)

        val player = event.player

        val sound = if (event.level.config.getBool("gain-xp.sound.enabled")) {
            PlayableSound.create(
                event.level.config.getSubsection("gain-xp.sound")
            )
        } else null

        // Run next tick because level up calls before xp is added
        plugin.scheduler.run {
            handleActionBar(event)
            handleBossBar(event)

            if (player.isXPGainSoundEnabled) {
                sound?.playTo(player)
            }
        }
    }

    private fun handleBossBar(event: PlayerLevelXPGainEvent) {
        if (!event.level.config.getBool("gain-xp.boss-bar.enabled")) {
            return
        }

        val player = event.player
        val skill = event.level

        val message = event.level.config.getString("gain-xp.boss-bar.message")
            .formatMessage(event)

        player.sendTemporaryBossBar(
            message,
            event.level.id,
            event.level.config.getInt("gain-xp.boss-bar.duration"),
            BarColor.valueOf(event.level.config.getString("gain-xp.boss-bar.color").uppercase()),
            BarStyle.valueOf(event.level.config.getString("gain-xp.boss-bar.style").uppercase()),
            player.getLevelProgress(skill).coerceIn(0.0..1.0)
        )
    }

    private fun handleActionBar(event: PlayerLevelXPGainEvent) {
        if (!event.level.config.getBool("gain-xp.action-bar.enabled")) {
            return
        }

        val player = event.player

        val message = event.level.config.getString("gain-xp.action-bar.message")
            .formatMessage(event)

        player.sendCompatibleActionBarMessage(message)
    }

    private fun String.formatMessage(event: PlayerLevelXPGainEvent): String {
        return this.replace(
            "%name%",
            event.level.name
        )
            .replace("%current_xp%", event.player.getLevelXP(event.level).toNiceString())
            .replace("%required_xp%", event.player.getFormattedRequiredXP(event.level))
            .replace("%gained_xp%", gainCache.get(event.level) { 0.0 }.toNiceString())
            .formatEco(
                placeholderContext(
                    event.player,
                    injectable = PlayerHealthInjectable
                )
            )
    }

}

fun Player.sendCompatibleActionBarMessage(message: String) {
    // Have to use the shit method for compatibility.
    @Suppress("DEPRECATION")
    this.spigot().sendMessage(
        net.md_5.bungee.api.ChatMessageType.ACTION_BAR,
        *net.md_5.bungee.api.chat.TextComponent.fromLegacyText(message)
    )
}

object PlayerHealthInjectable : PlaceholderInjectable {
    private val injections = listOf(
        PlayerStaticPlaceholder(
            "health"
        ) { it.health.toInt().toString() },
        PlayerStaticPlaceholder(
            "max_health"
        ) { it.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.value?.toInt()?.toString() ?: "20" },
    )

    override fun getPlaceholderInjections(): List<InjectablePlaceholder> {
        return injections
    }

    override fun addInjectablePlaceholder(p0: Iterable<InjectablePlaceholder>) {
        return
    }

    override fun clearInjectedPlaceholders() {
        return
    }
}