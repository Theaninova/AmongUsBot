package de.wulkanat

import de.wulkanat.discordui.ColorEmoji
import de.wulkanat.discordui.Emoji
import de.wulkanat.files.Config
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.User
import java.awt.Color

object Admin {
    val userId: Long = Config.botAdmin

    var jda: JDA? = null
        set(value) {
            field = value

            admin = value?.retrieveUserById(userId)?.complete()
            if (admin == null) {
                kotlin.io.println("Connection to de.wulkanat.Admin failed!")
            } else {
                kotlin.io.println("Connected to ${admin!!.name}. No further errors will be printed here.")
            }
        }
    var admin: User? = null

    fun println(msg: String) {
        sendDevMessage(
            EmbedBuilder()
                .setTitle(msg)
                .setColor(Color.WHITE)
                .build(),
            msg
        )
    }

    fun printlnBlocking(msg: String) {
        senDevMessageBlocking(
            EmbedBuilder()
                .setTitle(msg)
                .setColor(Color.WHITE)
                .build(),
            msg
        )
    }

    fun error(msg: String, error: String, author: User? = null) {
        sendDevMessage(
            EmbedBuilder()
                .setTitle(msg)
                .setDescription(error)
                .setColor(Color.RED)
                .run {
                    if (author == null) {
                        this
                    } else {
                        this.setAuthor(author.asTag, author.avatarUrl, author.avatarUrl)
                    }
                }
                .build(), "$msg\n\n${error}"
        )
    }

    fun errorBlocking(msg: String, error: Exception) {
        senDevMessageBlocking(
            EmbedBuilder()
                .setTitle(msg)
                .setDescription(error.message)
                .setColor(Color.RED)
                .build(), "$msg\n\n${error.message}"
        )
    }

    fun warning(msg: String) {
        sendDevMessage(
            EmbedBuilder()
                .setTitle(msg)
                .setColor(Color.YELLOW)
                .build(),
            msg
        )
    }

    fun info() {
        // TODO
        sendDevMessage(
            EmbedBuilder()
                .setTitle("Watching games")
                .also {
                    for (emote in Emoji.values()) {
                        it.addField(emote.unicodeEmote, emote.purpose, false)
                    }

                    for (emote in ColorEmoji.values()) {
                        it.addField(emote.amongUsName, emote.stringRepresentation(), true)
                    }
                }
                .setColor(Color.GREEN)
                .build(),
            "Wow, such empty."
        )
    }

    fun silent(msg: String) {
        kotlin.io.println(msg)
    }

    private fun senDevMessageBlocking(messageEmbed: MessageEmbed, fallback: String) {
        admin = jda!!.retrieveUserById(userId).complete()
        val devChannel = admin?.openPrivateChannel() ?: kotlin.run {
            kotlin.io.println(fallback)
            return
        }

        devChannel.complete()
            .sendMessage(messageEmbed).complete()
    }

    fun sendDevMessage(messageEmbed: MessageEmbed, fallback: String) {
        val devChannel = admin?.openPrivateChannel() ?: kotlin.run {
            kotlin.io.println(fallback)
            return
        }

        devChannel.queue {
            it.sendMessage(messageEmbed).queue()
        }
    }
}