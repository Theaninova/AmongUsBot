package de.wulkanat.discordui

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Emote
import net.dv8tion.jda.api.entities.MessageEmbed

enum class Emoji(val emoteName: String, val purpose: String, val unicodeEmote: String) {
    SKULL("skull", "Mark self as dead", "☠️"),
    MUTE("mute", "Mute everyone", "\uD83D\uDD07"),
    SPEAKER("speaker", "Unmute everyone", "\uD83D\uDD08"),
    REPEAT("repeat", "Start new game", "\uD83D\uDD04"),
    STOP_BUTTON("stop_button", "Stop playing", "⏹"),
    OBSERVER("spy", "Observe the game without taking part", "\uD83D\uDD75️"),
}

enum class ColorEmoji(
    val discordName: String,
    val amongUsName: String,
    val unicode: String,
    var customEmoji: Emote? = null,
) {
    RED("red_square", "Red", "\uD83D\uDFE5"),
    BLUE("blue_square", "Blue", "\uD83D\uDFE6"),
    GREEN("green_square", "Green", "\uD83D\uDFE9"),
    PINK("purse", "Pink", "\uD83D\uDC5B"),
    ORANGE("orange_square", "Orange", "\uD83D\uDFE7"),
    YELLOW("yellow_square", "Yellow", "\uD83D\uDFE8"),
    BLACK("black_large_square", "Black", "⬛"),
    WHITE("white_large_square", "White", "⬜"),
    PURPLE("purple_square", "Purple", "\uD83D\uDFEA"),
    BROWN("brown_square", "Brown", "\uD83D\uDFEB"),
    CYAN("regional_indicator_c", "Cyan", "\uD83C\uDDE8"),
    LIME("recycle", "Lime", "♻️"),
    TAN("cookie", "Tan", "\uD83C\uDF6A"),
    FORTEGREEN("evergreen_tree", "Fortegreen", "\uD83C\uDF32"),
    NONE("no_entry_sign", "No color", "\uD83D\uDEAB");

    fun stringRepresentation(): String {
        return if (customEmoji != null) "<:${customEmoji!!.name}:${customEmoji!!.id}>" else unicode
    }

    companion object {
        fun generateOverviewEmbed(): MessageEmbed {
            return EmbedBuilder().apply {
                setTitle("Choose your color")
                // setFooter("Not all colors are super accurate.")

                /*for (value in values()) {
                    addField(value.amongUsName, value.stringRepresentation(), true)
                }*/
            }.build()
        }

        fun initCustomEmotes(jda: JDA) {
            for (emoji in values()) {
                emoji.customEmoji = jda.getEmotesByName("amongus_${emoji.amongUsName}", true).firstOrNull()
            }
        }
    }
}
