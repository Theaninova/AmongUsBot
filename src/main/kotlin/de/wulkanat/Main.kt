package de.wulkanat

import de.wulkanat.discordui.ColorEmoji
import de.wulkanat.files.Config
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.requests.GatewayIntent

object Main {
    val jda = JDABuilder.createDefault(Config.token).apply {
        enableIntents(
            GatewayIntent.GUILD_MESSAGES,
            GatewayIntent.GUILD_VOICE_STATES,
        )
        setActivity(Activity.watching("!asnew [Game Code]"))
        addEventListeners(CliAdapter())
    }.build().awaitReady()
}

fun main() {
    ColorEmoji.initCustomEmotes(Main.jda)
    Admin.jda = Main.jda
    Admin.info()
}
