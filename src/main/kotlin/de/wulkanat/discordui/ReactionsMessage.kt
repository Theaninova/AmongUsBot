package de.wulkanat.discordui

import de.wulkanat.extensions.*
import net.dv8tion.jda.api.entities.*
import kotlinx.coroutines.*

class ReactionsMessage(
    val channel: VoiceChannel,
    gameCode: String,
    textChannel: TextChannel,
    private val mute: Boolean,
) {
    /**
     * userIdLong -> User
     */
    private val members: MutableMap<Long, AmongUsPlayer> = mutableMapOf()

    val controlMessage: Message
    var gameCode: String = gameCode
        set(value) {
            field = value
            updateMessage()
        }

    @Suppress("JoinDeclarationAndAssignment")
    val colorsMessage: Message

    private var ongoingMeeting: Boolean = true

    init {
        colorsMessage = textChannel.sendMessage(ColorEmoji.generateOverviewEmbed()).complete()
        controlMessage = textChannel.sendMessage(generateMessageEmbed()).complete().addControlReactions(false, mute)
        colorsMessage.addColorReactions()
    }

    class AmongUsPlayer(val member: Member, val status: AmongUsStatus, val color: ColorEmoji) :
        Comparable<AmongUsPlayer> {
        fun toStringPair(): Pair<String, String> {
            return Pair(member.effectiveName, color.stringRepresentation())
        }

        override fun compareTo(other: AmongUsPlayer): Int {
            return this.member.effectiveName.compareTo(other.member.effectiveName.toLowerCase())
        }
    }

    enum class AmongUsStatus(val alive: Boolean, val displayName: String, val associatedEmote: String) {
        ALIVE(true, "Alive", ""),
        DEAD(false, "Dead", Emoji.SKULL.unicodeEmote),
        SPECTATOR(false, "Spectator", Emoji.OBSERVER.unicodeEmote)
    }


    fun deafenAll() {
        if (!mute) return
        ongoingMeeting = false

        // only deafen people who didn't deafen themselves 5 seconds after the meeting ended
        GlobalScope.launch {
            delay(5000L)
            if (ongoingMeeting) return@launch

            for (member in channel.members) {
                when (members[member.idLong]?.status?.alive) {
                    true -> if (member.voiceState?.isDeafened == false) {
                        member.deafen(true).queue()
                    }
                    false -> if (member.voiceState?.isMuted == true) member.mute(false).queue()
                }
            }

            controlMessage.refreshControlReactions(true, mute)
        }
    }

    fun unmuteAll() {
        ongoingMeeting = true

        for (member in channel.members) {
            /*when (members[member.idLong]?.status?.alive) {
                true -> {*/
            if (member.voiceState?.isDeafened == true) member.deafen(false).queue()
            if (member.voiceState?.isMuted == true) member.mute(false).queue()
            // }
            /*false -> {
                if (member.voiceState?.isDeafened == true) member.deafen(false).queue()
                if (member.voiceState?.isMuted == false) member.mute(true).queue()
            } disabled because of Discord rate limit */
            //}
        }

        controlMessage.refreshControlReactions(false, mute)
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

    fun removePlayer(user: Member) {
        val member = members[user.idLong] ?: return
        colorsMessage.reAddColor(member.color)
        members.remove(user.idLong)
    }

    private fun editPlayer(user: Member, status: AmongUsStatus) {
        val member = members[user.idLong] ?: return
        members[user.idLong] = AmongUsPlayer(member.member, status, member.color)
        if (ongoingMeeting) {
            unmuteAll()
        } else {
            deafenAll()
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
        for ((key, value) in members) {
            if (value.status == AmongUsStatus.DEAD) {
                members[key] = AmongUsPlayer(value.member, AmongUsStatus.ALIVE, value.color)
            }
        }

        unmuteAll()

        updateMessage()
        controlMessage.regenerateDeadMute(false, mute)
    }

    private fun generateMessageEmbed(): MessageEmbed {
        return embed {
            title = "Game Code: **${gameCode.toUpperCase()}**"
            author {
                name = "Among Us"
                url = "http://www.innersloth.com/gameAmongUs.php"
                iconUrl = "https://img.itch.zone/aW1nLzE3MzAzNDAucG5n/180x143%23c/dzoZ2W.png"
            }
            footer = "${Emoji.SPEAKER.unicodeEmote} ${channel.name} ${if (!mute) "(auto-mute disabled)" else ""}"

            description = members.entries.sortedBy { it.value }.joinToString("\n") {
                val (name, color) = it.value.toStringPair()
                "$color${it.value.status.associatedEmote} $name"
            }
        }
    }
}