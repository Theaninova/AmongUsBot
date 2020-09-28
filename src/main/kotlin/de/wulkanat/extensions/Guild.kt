package de.wulkanat.extensions

import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Role

val Guild.everyoneRole: Role
    get() = this.publicRole