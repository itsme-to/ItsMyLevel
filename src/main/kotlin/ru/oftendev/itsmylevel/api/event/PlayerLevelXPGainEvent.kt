package ru.oftendev.itsmylevel.api.event

import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.HandlerList
import ru.oftendev.itsmylevel.levels.Level

class PlayerLevelXPGainEvent(
    who: Player,
    level: Level,
    var gainedXP: Double
) : PlayerLevelEvent(who, level), Cancellable {
    private var _cancelled = false

    override fun isCancelled() = _cancelled

    override fun setCancelled(cancel: Boolean) {
        _cancelled = cancel
    }

    override fun getHandlers(): HandlerList {
        return handlerList
    }

    companion object {
        private val handlerList = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList {
            return handlerList
        }
    }
}
