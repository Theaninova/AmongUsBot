package de.wulkanat.files.concept

interface AutoSaveSerializable {
    var parent: AutoSaveSerializable?
    fun save() {
        parent?.save() ?: println("Auto save failed on null parent!")
    }
    fun propagateParent()
}