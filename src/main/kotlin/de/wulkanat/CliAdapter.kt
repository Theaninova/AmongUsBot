package de.wulkanat

import de.wulkanat.cli.discordUsageEmbed
import de.wulkanat.cli.makeCli
import de.wulkanat.discordui.ReactionsMessage
import de.wulkanat.extensions.hasMember
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.MessageReaction
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.VoiceChannel
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceSelfDeafenEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.events.message.guild.react.GenericGuildMessageReactionEvent
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class CliAdapter : ListenerAdapter() {
    private val messageIdToGame = mutableMapOf<Long, ReactionsMessage>()
    private val colorMessageIdToGame = mutableMapOf<Long, ReactionsMessage>()
    private val channelIdToGame = mutableMapOf<Long, ReactionsMessage>()

    val cli = makeCli<MessageReceivedEvent>(prefix = "!") {
        command name "asnew" with { required string "Game Code" } does "Create a new game" through
                { required, _, event -> newGame(event, required.first()) }
        command name "undeafen" does "Undeafen/Unmute yourself if the Among Us Bot was stupid" through
                { _, _, event -> undeafen(event) }
        command name "unmute" does "Undeafen/Unmute yourself if the Among Us Bot was stupid" through
                { _, _, event -> undeafen(event) }
    }

    private fun newGame(event: MessageReceivedEvent, gameCode: String) {
        val jda = event.jda
        val author = event.member ?: return

        val channel = jda.voiceChannels.hasMember(author) ?: run {
            event.message.channel.sendMessage("You have to be in a voice channel to use this command!").queue()
            return@newGame
        }

        event.message.delete().queue()
        val reactionsMessage = ReactionsMessage(channel, gameCode, event.message.textChannel)
        reactionsMessage.updatePlayers()
        reactionsMessage.updateMessage()
        messageIdToGame[reactionsMessage.controlMessage.idLong] = reactionsMessage
        colorMessageIdToGame[reactionsMessage.colorsMessage.idLong] = reactionsMessage
        channelIdToGame[channel.idLong] = reactionsMessage
        // Main.jda.presence.setPresence(Activity.playing("${messageIdToGame.size}x Among Us"), false)
    }

    private fun undeafen(event: MessageReceivedEvent) {
        event.member?.deafen(false)?.queue()
        event.member?.mute(false)?.queue()

        event.message.delete().queue()
    }

    override fun onMessageReceived(event: MessageReceivedEvent) {
        if (event.message.author.isBot) return
        val msg = event.message.contentRaw

        cli.parse(msg, event,
            commandMisuse = { command, message ->
                event.message.channel.sendMessage(command.discordUsageEmbed(message)).queue()
            },
            helpMessage = {
                event.message.channel.sendMessage(it.discordUsageEmbed()).queue()
            }
        )
    }

    /**
     * Only call with GuildMessageReactionAddEvent or GuildMessageReactionRemoveEvent
     */
    private fun onEmoteEvent(event: GenericGuildMessageReactionEvent, added: Boolean) {
        if (event.user!!.isBot) return
        val game = messageIdToGame[event.messageIdLong] ?: run {
            val game = colorMessageIdToGame[event.messageIdLong]
            if (event.member!!.user.isBot || game == null) return@onEmoteEvent

            game.setColor(event.reactionEmote, event.member as Member, added)

            return@onEmoteEvent
        }
        if (event.member!!.user.isBot) return

        game.execute(event.reactionEmote, event.member as Member, added) {
            deleteGame(game)
        }
    }

    override fun onGuildMessageReactionAdd(event: GuildMessageReactionAddEvent) {
        onEmoteEvent(event, true)
    }

    override fun onGuildMessageReactionRemove(event: GuildMessageReactionRemoveEvent) {
        onEmoteEvent(event, false)
    }

    private fun getGameFromMember(member: Member): ReactionsMessage? {
        val channel = member.voiceState?.channel ?: return null
        return channelIdToGame[channel.idLong]
    }

    private fun deleteGame(game: ReactionsMessage) {
        val messageId = game.controlMessage.idLong
        val channelId = game.channel.idLong
        messageIdToGame.remove(messageId)
        channelIdToGame.remove(channelId)
        // Main.jda.presence.setPresence(Activity.playing("${messageIdToGame.size}x Among Us"), false)
        game.deleteMessage()
    }

    override fun onGuildVoiceJoin(event: GuildVoiceJoinEvent) {
        if (event.voiceState.isGuildDeafened) event.member.deafen(false).queue()
        joinChannel(event.channelJoined, event.member)
    }

    private fun joinChannel(channel: VoiceChannel, member: Member) {
        val game = channelIdToGame[channel.idLong] ?: return
        game.addPlayer(member)
        game.updateMessage()
    }

    override fun onGuildVoiceLeave(event: GuildVoiceLeaveEvent) {
        if (event.voiceState.isGuildDeafened) event.member.deafen(false).queue()
        if (event.voiceState.isGuildMuted) event.member.mute(false).queue()
        leaveChannel(event.channelLeft, event.member)
    }

    private fun leaveChannel(channel: VoiceChannel, member: Member) {
        val game = channelIdToGame[channel.idLong] ?: return
        game.removePlayer(member)

        if (game.isInactive()) {
            deleteGame(game)
        } else {
            game.updateMessage()
        }
    }

    override fun onGuildVoiceMove(event: GuildVoiceMoveEvent) {
        if (event.voiceState.isGuildDeafened) event.member.deafen(false).queue()
        if (event.voiceState.isGuildMuted) event.member.mute(false).queue()

        leaveChannel(event.channelLeft, event.member)
        joinChannel(event.channelJoined, event.member)
    }

    override fun onGuildVoiceSelfDeafen(event: GuildVoiceSelfDeafenEvent) {
        val game = channelIdToGame[event.voiceState.channel?.idLong] ?: return

        if (event.isSelfDeafened) {
            game.deafenAll()
        } else {
            game.unmuteAll()
        }
    }
}