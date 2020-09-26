package de.wulkanat.extensions

import de.wulkanat.Admin
import net.dv8tion.jda.api.entities.User

fun User.isBotAdmin(): Boolean {
    return idLong == Admin.userId
}