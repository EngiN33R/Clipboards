package io.engi.clipboards.entity

import io.engi.clipboards.CLIPBOARD_ENTITY_TYPE
import net.fabricmc.fabric.api.util.NbtType
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.Tag
import net.minecraft.text.LiteralText
import net.minecraft.text.Text

class ClipboardBlockEntity(type: BlockEntityType<*>?) : BlockEntity(type) {
    constructor() : this(CLIPBOARD_ENTITY_TYPE)

    var name: Text? = null
    var titles: MutableMap<Int, Text> = mutableMapOf()
    val notes: MutableMap<Int, Text> = mutableMapOf()
    val ticks: MutableMap<Int, Boolean> = mutableMapOf()

    override fun fromTag(tag: CompoundTag?) {
        super.fromTag(tag)
        if (!tag!!.contains("Notes") || !tag.contains("Titles")) return
        for (entry: Tag in tag.getList("Notes", NbtType.COMPOUND)) {
            val compound = entry as CompoundTag
            notes[compound.getInt("Slot")] = LiteralText(compound.getString("Text"))
            ticks[compound.getInt("Slot")] = compound.getBoolean("Ticked")
        }
        for (entry: Tag in tag.getList("Titles", NbtType.COMPOUND)) {
            val compound = entry as CompoundTag
            titles[compound.getInt("Page")] = LiteralText(compound.getString("Text"))
        }
    }

    override fun toTag(tag: CompoundTag?): CompoundTag {
        super.toTag(tag!!)
        val notesTag = ListTag()
        val titlesTag = ListTag()
        for (i in 0..69) {
            if (notes[i] == null && ticks[i] == null) continue
            val compound = CompoundTag()
            compound.putInt("Slot", i)
            compound.putString("Text", notes[i]?.asFormattedString() ?: "")
            compound.putBoolean("Ticked", ticks[i] ?: false)
            notesTag.add(compound)
        }
        for (index in titles) {
            val compound = CompoundTag()
            compound.putInt("Page", index.key)
            compound.putString("Text", index.value.asFormattedString())
            titlesTag.add(compound)
        }
        tag.put("Notes", notesTag)
        tag.put("Titles", titlesTag)
        return tag
    }
}