package de.wulkanat

import de.wulkanat.cli.Cli
import de.wulkanat.cli.discordUsageEmbed
import de.wulkanat.cli.makeCli
import de.wulkanat.extensions.isBotAdmin
import de.wulkanat.extensions.queueSelfDestruct
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import kotlin.system.exitProcess

class AdminCliAdapter : ListenerAdapter() {
    private val cli: Cli<PrivateMessageReceivedEvent> = makeCli(prefix = "!") {
        command name "stop" does "Stop the bot" through { _, _, _ -> exitProcess(1) }
        command name "info" does "Prints info" through { _, _, _ -> Admin.info() }
        command name "servers" does "Prints a list of all servers" through { _, _, _ -> Admin.servers() }
    }

    override fun onPrivateMessageReceived(event: PrivateMessageReceivedEvent) {
        if (!event.author.isBotAdmin()) return
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
}