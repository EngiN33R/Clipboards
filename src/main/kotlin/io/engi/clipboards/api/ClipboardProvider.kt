package io.engi.clipboards.api

import io.engi.clipboards.entity.ClipboardBlockEntity
import net.fabricmc.fabric.api.util.NbtType
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.text.LiteralText
import net.minecraft.text.Text

abstract class ClipboardProvider {
    abstract fun setNote(index: Int, note: Text)
    abstract fun getNote(index: Int): Text?
    abstract fun setTitle(page: Int, note: Text)
    abstract fun getTitle(page: Int): Text?
    abstract fun setTicked(index: Int, ticked: Boolean)
    abstract fun getTicked(index: Int): Boolean
}

class EntityClipboardProvider(private val blockEntity: ClipboardBlockEntity): ClipboardProvider() {
    override fun setNote(index: Int, note: Text) {
        blockEntity.notes[index] = note
        blockEntity.markDirty()
    }

    override fun getNote(index: Int): Text? {
        return blockEntity.notes[index]
    }

    override fun setTitle(page: Int, note: Text) {
        blockEntity.titles[page] = note
        blockEntity.markDirty()
    }

    override fun getTitle(page: Int): Text? {
        return blockEntity.titles[page]
    }

    override fun setTicked(index: Int, ticked: Boolean) {
        blockEntity.ticks[index] = ticked
        blockEntity.markDirty()
    }

    override fun getTicked(index: Int): Boolean {
        return blockEntity.ticks[index] ?: false
    }
}

open class NbtClipboardProvider(val compoundTag: CompoundTag): ClipboardProvider() {
    override fun setNote(index: Int, note: Text) {
        val tag = compoundTag.getList("Notes", NbtType.COMPOUND) ?: return
        for (listIndex in 0 until tag.size) {
            val compound = tag[listIndex] as CompoundTag
            if (compound.getInt("Slot") == index) {
                compound.putString("Text", note.asFormattedString())
                tag[listIndex] = compound
                return
            }
        }
        val compound = CompoundTag()
        compound.putInt("Slot", index)
        compound.putString("Text", note.asFormattedString())
        compound.putBoolean("Ticked", false)
        tag.add(compound)
        compoundTag.put("Notes", tag)
    }

    override fun getNote(index: Int): Text? {
        val tag = compoundTag.getList("Notes", NbtType.COMPOUND) ?: return null
        for (item in tag) {
            val compound = item as CompoundTag
            if (compound.getInt("Slot") == index) return LiteralText(compound.getString("Text"))
        }
        return null
    }

    override fun setTitle(page: Int, note: Text) {
        val tag = compoundTag.getList("Titles", NbtType.COMPOUND) ?: return
        for (listIndex in 0 until tag.size) {
            val compound = tag[listIndex] as CompoundTag
            if (compound.getInt("Page") == page) {
                compound.putString("Text", note.asFormattedString())
                tag[listIndex] = compound
                return
            }
        }
        val compound = CompoundTag()
        compound.putInt("Page", page)
        compound.putString("Text", note.asFormattedString())
        tag.add(compound)
        compoundTag.put("Titles", tag)
    }

    override fun getTitle(page: Int): Text? {
        val tag = compoundTag.getList("Titles", NbtType.COMPOUND) ?: return null
        for (item in tag) {
            val compound = item as CompoundTag
            if (compound.getInt("Page") == page) return LiteralText(compound.getString("Text"))
        }
        return null
    }

    override fun setTicked(index: Int, ticked: Boolean) {
        val tag = compoundTag.getList("Notes", NbtType.COMPOUND) ?: return
        for (listIndex in 0 until tag.size) {
            val compound = tag[listIndex] as CompoundTag
            if (compound.getInt("Slot") == index) {
                compound.putBoolean("Ticked", ticked)
                tag[listIndex] = compound
                return
            }
        }
        val compound = CompoundTag()
        compound.putInt("Slot", index)
        compound.putString("Text", "")
        compound.putBoolean("Ticked", ticked)
        tag.add(compound)
        compoundTag.put("Notes", tag)
    }

    override fun getTicked(index: Int): Boolean {
        val tag = compoundTag.getList("Notes", NbtType.COMPOUND) ?: return false
        for (item in tag) {
            val compound = item as CompoundTag
            if (compound.getInt("Slot") == index) return compound.getBoolean("Ticked")
        }
        return false
    }
}

class ItemStackClipboardProvider(private val itemStack: ItemStack):
    NbtClipboardProvider(itemStack.orCreateTag.getCompound("BlockEntityTag") ?: CompoundTag()) {
    override fun setNote(index: Int, note: Text) {
        super.setNote(index, note)
        itemStack.orCreateTag.put("BlockEntityTag", compoundTag)
    }

    override fun setTitle(page: Int, note: Text) {
        super.setTitle(page, note)
        itemStack.orCreateTag.put("BlockEntityTag", compoundTag)
    }
}