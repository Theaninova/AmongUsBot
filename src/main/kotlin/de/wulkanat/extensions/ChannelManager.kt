package de.wulkanat.extensions

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.IPermissionHolder
import net.dv8tion.jda.api.managers.ChannelManager

class PermissionOverrides {

}

fun ChannelManager.putPermissionOverride(
    role: IPermissionHolder,
    allow: Permission? = null,
    deny: Permission? = null
): ChannelManager {
    return putPermissionOverride(
        role, setOf(allow), setOf(deny)
    )
}