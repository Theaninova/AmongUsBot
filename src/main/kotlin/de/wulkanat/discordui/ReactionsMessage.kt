package de.wulkanat.discordui

import de.wulkanat.extensions.queueAllSafe
import de.wulkanat.extensions.queueSafe
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.requests.RestAction
import sun.plugin2.util.ColorUtil

class ReactionsMessage(
    val channel: VoiceChannel,
    private val gameCode: String,
    textChannel: TextChannel,
) {
    /**
     * userIdLong -> User
     */
    private val members: MutableMap<Long, AmongUsPlayer> = mutableMapOf()

    val controlMessage: Message

    @Suppress("JoinDeclarationAndAssignment")
    val colorsMessage: Message

    init {
        colorsMessage = textChannel.sendMessage(ColorEmoji.generateOverviewEmbed()).complete()
        controlMessage = textChannel.sendMessage(generateMessageEmbed()).complete().addControlReactions(false)
        colorsMessage.addColorReactions()
    }

    class AmongUsPlayer(val member: Member, val status: AmongUsStatus, val color: ColorEmoji) {
        fun toStringPair(): Pair<String, String> {
            return Pair(member.effectiveName, "${color.stringRepresentation()} ${status.displayName}".trim())
        }
    }

    enum class AmongUsStatus(val alive: Boolean, val displayName: String) {
        ALIVE(true, "Alive"),
        DEAD(false, "Dead"),
        SPECTATOR(false, "Spectator")
    }


    fun deafenAll() {
        for (member in channel.members) {
            when (members[member.idLong]?.status?.alive) {
                true -> if (member.voiceState?.isDeafened == false) {
                    member.deafen(true).queue()
                    member.mute(true).queue()
                }
                false -> if (member.voiceState?.isMuted == true) member.mute(false).queue()
            }
        }

        controlMessage.refreshControlReactions(true)
    }

    fun unmuteAll() {
        for (member in channel.members) {
            when (members[member.idLong]?.status?.alive) {
                true -> {
                    if (member.voiceState?.isDeafened == true) member.deafen(false).queue()
                    if (member.voiceState?.isMuted == true) member.mute(false).queue()
                }
                false -> {
                    if (member.voiceState?.isDeafened == true) member.deafen(false).queue()
                    if (member.voiceState?.isMuted == false) member.mute(true).queue()
                }
            }
        }

        controlMessage.refreshControlReactions(false)
    }

    fun deleteMessage() {
        for (member in channel.members) {
            member.deafen(false).queue()
            member.mute(false).queue()
        }
        controlMessage.clearReactions().queueSafe {
            controlMessage.delete().queueSafe()
        }
        colorsMessage.clearReactions().queueSafe {
            colorsMessage.delete().queueSafe()
        }
    }

    fun execute(
        emote: MessageReaction.ReactionEmote,
        user: Member,
        added: Boolean,
        deleteCallback: () -> Unit
    ) {
        when (emote.name) {
            Emoji.SKULL.unicodeEmote -> editPlayer(user, if (added) AmongUsStatus.DEAD else AmongUsStatus.ALIVE)
            Emoji.OBSERVER.unicodeEmote -> editPlayer(user, if (added) AmongUsStatus.SPECTATOR else AmongUsStatus.ALIVE)
            Emoji.SPEAKER.unicodeEmote -> unmuteAll()
            Emoji.MUTE.unicodeEmote -> deafenAll()
            Emoji.REPEAT.unicodeEmote -> reset()
            Emoji.STOP_BUTTON.unicodeEmote -> {
                deleteMessage()
                deleteCallback()
            }

            else -> return
        }

        updateMessage()
    }

    fun updateMessage() {
        controlMessage.editMessage(generateMessageEmbed()).queue()
    }

    fun updatePlayers() {
        for (member in channel.members) {
            addPlayer(member)
        }
    }

    fun removePlayer(member: Member) {
        members.remove(member.idLong)
    }

    private fun editPlayer(user: Member, status: AmongUsStatus) {
        val member = members[user.idLong] ?: return
        members[user.idLong] = AmongUsPlayer(member.member, status, member.color)
    }

    private fun Message.reAddColor(color: ColorEmoji) {
        clearReactions(ColorEmoji.NONE).queue {
            addReaction(color).queue {
                addReaction(ColorEmoji.NONE).queue()
            }
        }
    }

    private fun Message.addReaction(color: ColorEmoji): RestAction<Void> {
        return if (color.customEmoji != null) {
            addReaction(color.customEmoji!!)
        } else {
            addReaction(color.unicode)
        }
    }

    private fun Message.clearReactions(color: ColorEmoji): RestAction<Void> {
        return if (color.customEmoji != null) {
            clearReactions(color.customEmoji!!)
        } else {
            clearReactions(color.unicode)
        }
    }

    private fun editPlayer(user: Member, color: ColorEmoji) {
        val member = members[user.idLong] ?: return
        // we just assume that the color is available.
        if (color == ColorEmoji.NONE) {
            colorsMessage.reAddColor(member.color)
        } else {
            colorsMessage.clearReactions(color).queue()

            if (member.color != ColorEmoji.NONE) {
                colorsMessage.reAddColor(member.color)
            }
        }
        members[user.idLong] = AmongUsPlayer(member.member, member.status, color)
    }

    fun setColor(emote: MessageReaction.ReactionEmote, user: Member, added: Boolean) {
        val color =
            ColorEmoji.values()
                .find { emote.name == it.unicode || emote.name.endsWith(it.amongUsName.toLowerCase()) }
                ?: return

        editPlayer(user, if (added) color else ColorEmoji.NONE)
        updateMessage()
    }

    fun addPlayer(member: Member) {
        if (members[member.idLong] != null) return
        members[member.idLong] = AmongUsPlayer(member, AmongUsStatus.ALIVE, ColorEmoji.NONE)
        // member.applyEmojiPrefix()
    }

    fun isInactive(): Boolean {
        return members.isEmpty()
    }

    private fun reset() {
        unmuteAll()

        members.clear()
        updatePlayers()

        updateMessage()
        controlMessage.clearReactions().queue {
            controlMessage.addControlReactions(false)
        }
        colorsMessage.clearReactions().queue {
            colorsMessage.addColorReactions()
        }
    }

    private fun Message.refreshControlReactions(deafened: Boolean): Message {
        listOf(
            clearReactions(if (deafened) Emoji.MUTE.unicodeEmote else Emoji.SPEAKER.unicodeEmote),
            addReaction(if (deafened) Emoji.SPEAKER.unicodeEmote else Emoji.MUTE.unicodeEmote),
        ).queueAllSafe()

        return this
    }

    private fun Message.addControlReactions(deafened: Boolean): Message {
        listOf(
            addReaction(Emoji.STOP_BUTTON.unicodeEmote),
            addReaction(Emoji.REPEAT.unicodeEmote),
            addReaction(Emoji.OBSERVER.unicodeEmote),
            addReaction(Emoji.SKULL.unicodeEmote),
            clearReactions(if (deafened) Emoji.MUTE.unicodeEmote else Emoji.SPEAKER.unicodeEmote),
            addReaction(if (deafened) Emoji.SPEAKER.unicodeEmote else Emoji.MUTE.unicodeEmote)
        ).queueAllSafe()

        return this
    }

    private fun Message.addColorReactions(): Message {
        val list = mutableListOf<RestAction<Void>>()
        for (value in ColorEmoji.values()) {
            if (value.customEmoji == null) {
                list.add(addReaction(value.unicode))
            } else {
                list.add(addReaction(value.customEmoji!!))
            }
        }
        list.queueAllSafe()

        return this
    }

    private fun generateMessageEmbed(): MessageEmbed {
        return EmbedBuilder().apply {
            setTitle("Game Code: **${gameCode.toUpperCase()}**")
            setAuthor(
                "Among Us",
                "http://www.innersloth.com/gameAmongUs.php",
                "https://img.itch.zone/aW1nLzE3MzAzNDAucG5n/180x143%23c/dzoZ2W.png"
            )
            setFooter("${Emoji.SPEAKER.unicodeEmote} ${channel.name}")

            for (member in members) {
                val (title, field) = member.value.toStringPair()
                addField(title, field, true)
            }
        }.build()
    }
}