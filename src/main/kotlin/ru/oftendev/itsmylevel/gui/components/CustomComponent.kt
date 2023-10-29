package ru.oftendev.itsmylevel.gui.components

import com.willfp.eco.core.config.interfaces.Config
import com.willfp.eco.core.gui.GUIComponent
import com.willfp.eco.core.gui.menu.Menu
import com.willfp.eco.core.gui.slot
import com.willfp.eco.core.gui.slot.Slot
import com.willfp.eco.core.items.Items
import com.willfp.eco.core.items.builder.ItemStackBuilder
import com.willfp.eco.core.placeholder.context.placeholderContext
import com.willfp.eco.util.evaluateExpression
import com.willfp.eco.util.lineWrap
import com.willfp.eco.util.toSingletonList
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import ru.oftendev.itsmylevel.levels.Level
import ru.oftendev.itsmylevel.levels.LevelInjectable
import kotlin.math.roundToInt

class CustomComponent(val config: Config, val level: Level): GUIComponent {
    private val clickTypes = mapOf(
        "left-click" to ClickType.LEFT,
        "right-click" to ClickType.RIGHT,
        "shift-left-click" to ClickType.SHIFT_LEFT,
        "shift-right-click" to ClickType.SHIFT_RIGHT
    )

    val commands = clickTypes.keys.associateWith {
        config.getStrings(it)
    }

    override fun getRows(): Int {
        return 1
    }

    override fun getColumns(): Int {
        return 1
    }

    override fun getSlotAt(row: Int, column: Int, player: Player, menu: Menu): Slot {
        return slot(
            ItemStackBuilder(Items.lookup(config.getString("item")))
                .addLoreLines(
                    level.addPlaceholdersInto(
                        config.getStrings("lore"),
                        player,
                        level = level.getActualLevel(player)
                    ).lineWrap(40)
                )
                .setDisplayName(
                    level.addPlaceholdersInto(
                        config.getString("name").toSingletonList(),
                        player,
                        level = level.getActualLevel(player)
                    ).first()
                )
                .build()
        ) {
            for (lst in commands) {
                if (lst.value.isNotEmpty()) {
                    onClick(
                        clickTypes[lst.key]!!
                    ) {
                        event, _, _ ->
                            lst.value.forEach {
                                dispatchCommand(it, event.whoClicked as Player)
                            }
                    }
                }
            }
        }
    }

    fun dispatchCommand(command: String, player: Player) {
        if (command.startsWith("console:")) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.removePrefix("console:")
                .replace("%player%", player.name))
        } else {
            Bukkit.dispatchCommand(player, command.replace("%player%", player.name))
        }
    }
}