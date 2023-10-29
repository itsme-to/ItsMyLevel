package ru.oftendev.itsmylevel.gui.components

import com.willfp.eco.core.EcoPlugin
import com.willfp.eco.core.gui.menu.Menu
import com.willfp.eco.core.items.Items
import com.willfp.eco.core.items.builder.ItemStackBuilder
import com.willfp.eco.core.map.nestedMap
import com.willfp.eco.core.placeholder.context.placeholderContext
import com.willfp.eco.util.evaluateExpression
import com.willfp.eco.util.lineWrap
import com.willfp.ecomponent.components.LevelComponent
import com.willfp.ecomponent.components.LevelState
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import ru.oftendev.itsmylevel.api.getLevelLevel
import ru.oftendev.itsmylevel.levels.Level
import ru.oftendev.itsmylevel.levels.LevelInjectable
import kotlin.math.roundToInt

class LevelLevelComponent(
    private val plugin: EcoPlugin,
    private val theLevel: Level
) : LevelComponent() {
    override val pattern: List<String> = theLevel.config.getStrings("gui.progression-slots.pattern")
    override val maxLevel = theLevel.maxLevel

    private val itemCache = nestedMap<LevelState, Int, ItemStack>()

    override fun getLevelItem(player: Player, menu: Menu, level: Int, levelState: LevelState): ItemStack {
        val key = levelState.key

        fun item() = ItemStackBuilder(Items.lookup(theLevel.config.getString("gui.progression-slots.$key.item")))
            .setDisplayName(
                theLevel.config.getString("gui.progression-slots.$key.name")
                    .replace("%name%", theLevel.name)
                    .let { theLevel.addPlaceholdersInto(it, level, player) }
            )
            .addLoreLines(
                theLevel.addPlaceholdersInto(
                    theLevel.config.getStrings("gui.progression-slots.$key.lore"),
                    player,
                    level = level
                ).lineWrap(40)
            )
            .setAmount(
                evaluateExpression(
                    theLevel.config.getString("gui.progression-slots.item-amount"),
                    placeholderContext(
                        injectable = LevelInjectable(level)
                    )
                ).roundToInt()
            )
            .build()

        return if (levelState != LevelState.IN_PROGRESS) {
            itemCache[levelState].getOrPut(level) { item() }
        } else {
            item()
        }
    }

    override fun getLevelState(player: Player, level: Int): LevelState {
        return when {
            level <= player.getLevelLevel(theLevel) -> LevelState.UNLOCKED
            level == player.getLevelLevel(theLevel) + 1 -> LevelState.IN_PROGRESS
            else -> LevelState.LOCKED
        }
    }
}
