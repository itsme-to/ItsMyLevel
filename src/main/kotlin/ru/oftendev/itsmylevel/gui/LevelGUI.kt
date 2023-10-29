package ru.oftendev.itsmylevel.gui

import com.willfp.eco.core.EcoPlugin
import com.willfp.eco.core.gui.menu
import com.willfp.eco.core.gui.menu.Menu
import com.willfp.eco.core.gui.page.PageChanger
import com.willfp.eco.core.gui.slot.ConfigSlot
import com.willfp.eco.core.gui.slot.FillerMask
import com.willfp.eco.core.gui.slot.MaskItems
import com.willfp.eco.core.items.Items
import com.willfp.eco.core.items.builder.ItemStackBuilder
import com.willfp.eco.core.placeholder.PlayerStaticPlaceholder
import com.willfp.eco.core.placeholder.StaticPlaceholder
import com.willfp.eco.util.formatEco
import com.willfp.eco.util.toNiceString
import org.bukkit.entity.Player
import ru.oftendev.itsmylevel.api.getLevelLevel
import ru.oftendev.itsmylevel.gui.components.CustomComponent
import ru.oftendev.itsmylevel.gui.components.LevelLevelComponent
import ru.oftendev.itsmylevel.levels.Level

class LevelGUI(
    plugin: EcoPlugin,
    private val level: Level
) {
    private val menu: Menu

    init {
        val maskPattern = level.config.getStrings("gui.mask.pattern").toTypedArray()
        val maskItems = MaskItems.fromItemNames(level.config.getStrings("gui.mask.materials"))

        val levelComponent = LevelLevelComponent(plugin, level)

        menu = menu(level.config.getInt("gui.rows")) {
            title = level.config.getString("gui.title")
                .replace("%level%", level.name)
                .formatEco()

            maxPages(levelComponent.pages)

            setMask(
                FillerMask(
                    maskItems,
                    *maskPattern
                )
            )

            addComponent(1, 1, levelComponent)

            defaultPage {
                levelComponent.getPageOf(it.getLevelLevel(level)).coerceAtLeast(1)
            }

            addComponent(
                level.config.getInt("gui.progression-slots.prev-page.location.row"),
                level.config.getInt("gui.progression-slots.prev-page.location.column"),
                PageChanger(
                    ItemStackBuilder(Items.lookup(level.config.getString("gui.progression-slots.prev-page.material")))
                        .setDisplayName(level.config.getString("gui.progression-slots.prev-page.name"))
                        .build(),
                    PageChanger.Direction.BACKWARDS
                )
            )

            addComponent(
                level.config.getInt("gui.progression-slots.next-page.location.row"),
                level.config.getInt("gui.progression-slots.next-page.location.column"),
                PageChanger(
                    ItemStackBuilder(Items.lookup(level.config.getString("gui.progression-slots.next-page.material")))
                        .setDisplayName(level.config.getString("gui.progression-slots.next-page.name"))
                        .build(),
                    PageChanger.Direction.FORWARDS
                )
            )

            /*setSlot(
                level.config.getInt("gui.progression-slots.close.location.row"),
                level.config.getInt("gui.progression-slots.close.location.column"),
                slot(
                    ItemStackBuilder(Items.lookup(level.config.getString("gui.progression-slots.close.material")))
                        .setDisplayName(level.config.getString("gui.progression-slots.close.name"))
                        .build()
                ) {
                    onLeftClick { event, _ ->
                        event.whoClicked.closeInventory()
                    }
                }
            )*/

            for (config in level.config.getSubsections("gui.custom-slots")) {
                addComponent(
                    config.getInt("row"),
                    config.getInt("column"),
                    CustomComponent(config.apply {
                        injectPlaceholders(
                            StaticPlaceholder("name") { level.name },
                            PlayerStaticPlaceholder("level") { it.getLevelLevel(level).toNiceString() }
                        )
                        injectPlaceholders(*level.injectables.toTypedArray())
                    }, level)
                )
            }
        }
    }

    fun open(player: Player) {
        menu.open(player)
    }
}