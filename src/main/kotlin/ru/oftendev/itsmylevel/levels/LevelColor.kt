package ru.oftendev.itsmylevel.levels

import com.willfp.eco.core.config.interfaces.Config

class LevelColor(
    val level: Int,
    val color: String
) {
    constructor(config: Config) : this(
        config.getInt("level"),
        config.getFormattedString("color")
    )
}