package ru.oftendev.itsmylevel.api.event

import org.bukkit.entity.Player
import org.bukkit.event.HandlerList
import ru.oftendev.itsmylevel.levels.Level

class PlayerLevelLevelUpEvent(
    who: Player,
    level: Level,
    val theLevel: Int
) : PlayerLevelEvent(who, level) {
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
