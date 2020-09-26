package de.wulkanat.extensions

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import java.time.temporal.TemporalAccessor

fun embed(init: EmbedBuildKt.() -> Unit): MessageEmbed {
    val embed = EmbedBuildKt()
    embed.init()
    return embed.embedBuilder.build()
}

@DslMarker
annotation class EmbedBuilderMarker

@DslMarker
annotation class EmbedBuilderFeature

@EmbedBuilderMarker
class EmbedBuildKt {
    internal val embedBuilder = EmbedBuilder()

    var title: String = ""
        set(value) {
            embedBuilder.setTitle(value)
        }
    var description: String? = null
        set(value) {
            embedBuilder.setDescription(value)
        }
    var author: String = ""
        set(value) {
            embedBuilder.setAuthor(value)
        }
    var footer: String = ""
        set(value) {
            embedBuilder.setFooter(value)
        }
    var color: java.awt.Color = java.awt.Color.BLACK
        set(value) {
            embedBuilder.setColor(value)
        }
    var thumbnailUrl: String = ""
        set(value) {
            embedBuilder.setThumbnail(value)
        }
    var imageUrl: String = ""
        set(value) {
            embedBuilder.setImage(value)
        }
    var timestamp: TemporalAccessor? = null
        set(value) {
            embedBuilder.setTimestamp(value)
        }

    @EmbedBuilderFeature
    class Title {
        var title: String = ""
        var url: String? = null
    }

    fun title(init: Title.() -> Unit) {
        val title = Title()
        title.init()
        if (title.url == null) {
            embedBuilder.setTitle(title.title)
        } else {
            embedBuilder.setTitle(title.title, title.url)
        }
    }

    @EmbedBuilderFeature
    class Author {
        var name: String = ""
        var url: String? = null
        var iconUrl: String? = null
    }

    fun author(init: Author.() -> Unit) {
        val author = Author()
        author.init()
        when {
            author.url == null -> embedBuilder.setAuthor(author.name)
            author.iconUrl == null -> embedBuilder.setAuthor(author.name, author.url)
            else -> embedBuilder.setAuthor(author.name, author.url, author.iconUrl)
        }
    }

    @EmbedBuilderFeature
    class Footer {
        var footer: String = ""
        var url: String? = null
    }

    fun footer(init: Footer.() -> Unit) {
        val footer = Footer()
        footer.init()
        if (footer.url == null) {
            embedBuilder.setFooter(footer.footer)
        } else {
            embedBuilder.setFooter(footer.footer, footer.url)
        }
    }

    @EmbedBuilderFeature
    inner class Fields {
        inner class Field {
            var title: String? = null
            var description: String? = null
            var inline: Boolean = false
        }

        fun field(init: Field.() -> Unit) {
            val field = Field()
            field.init()
            if (field.title == null && field.description == null) {
                embedBuilder.addBlankField(field.inline)
            } else {
                embedBuilder.addField(field.title, field.description, field.inline)
            }
        }
    }

    fun fields(init: Fields.() -> Unit) {

    }
}
