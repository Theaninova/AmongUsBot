package de.wulkanat.extensions

import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.VoiceChannel

fun List<VoiceChannel>.hasMember(member: Member): VoiceChannel? {
    return find { channel ->
        channel.members.find {
            it.idLong == member.idLong
        } != null
    }
}