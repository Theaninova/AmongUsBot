package de.wulkanat

import de.wulkanat.discordui.ColorEmoji
import de.wulkanat.discordui.Emoji
import de.wulkanat.extensions.embed
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
            embed {
                title = msg
                color = Color.WHITE
            }, msg
        )
    }

    fun printlnBlocking(msg: String) {
        senDevMessageBlocking(
            embed {
                title = msg
                color = Color.WHITE
            }, msg
        )
    }

    fun error(msg: String, error: String, author: User? = null) {
        sendDevMessage(
            embed {
                title = msg
                description = error
                color = Color.RED

                if (author != null) {
                    author {
                        name = author.asTag
                        url = author.avatarUrl
                        iconUrl = author.avatarUrl
                    }
                }

            }, "$msg\n\n${error}"
        )
    }

    fun errorBlocking(msg: String, error: Exception) {
        senDevMessageBlocking(
            embed {
                title = msg
                description = error.message
                color = Color.RED
            }, "$msg\n\n${error.message}"
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
        sendDevMessage(
            embed {
                title = "Watching games"
                color = Color.GREEN

                fields {
                    for (emote in Emoji.values()) {
                        field {
                            title = emote.unicodeEmote
                            description = emote.purpose
                        }
                    }

                    for (emote in ColorEmoji.values()) {
                        field {
                            title = emote.amongUsName
                            description = emote.stringRepresentation()
                            inline = true
                        }
                    }
                }
            }, "Wow, such empty."
        )
    }

    fun servers() {
        sendDevMessage(
            embed {
                title = "Joined Servers"
                description = jda?.guilds?.joinToString("\n") { "${it.name} (${it.memberCount} Members)" }
            }, "Joined ${jda?.guilds?.size} servers"
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

    private fun sendDevMessage(messageEmbed: MessageEmbed, fallback: String) {
        val devChannel = admin?.openPrivateChannel() ?: kotlin.run {
            kotlin.io.println(fallback)
            return
        }

        devChannel.queue {
            it.sendMessage(messageEmbed).queue()
        }
    }
}