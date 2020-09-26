package de.wulkanat.files.concept

import de.wulkanat.extensions.ensureExists
import de.wulkanat.files.Config
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import java.io.File

abstract class SerializableObject<T : AutoSaveSerializable>(
    fileName: String,
    defaultText: T? = null,
    private val childSerializer: KSerializer<T>
) : AutoSaveSerializable {
    private val json = Json { allowStructuredMapKeys = true }
    private val file = File(fileName).ensureExists(defaultText?.let { json.encodeToString(childSerializer, it) })
    lateinit var instance: T

    init {
        refresh()
    }

    fun refresh() {
        instance = json.decodeFromString(childSerializer, file.readText())

        propagateParent()
    }

    override fun propagateParent() {
        instance.parent = this
        instance.propagateParent()
    }

    override fun save() {
        file.writeText(json.encodeToString(childSerializer, instance))
    }
}