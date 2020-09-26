package de.wulkanat.extensions

import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.entities.Member

fun Member.isPlaying(game: String): Boolean {
    return getPlayingGame(game) != null
}

fun Member.getPlayingGame(game: String): Activity? {
    return activities.find {
        val rpc = it.asRichPresence() ?: return@find false
        return@find rpc.applicationId == game
    }
}
