package io.engi.clipboards.gui

import io.engi.clipboards.CLIPBOARD_ENTITY_TYPE
import io.engi.clipboards.api.ClipboardProvider
import io.engi.clipboards.api.EntityClipboardProvider
import io.engi.clipboards.api.ItemStackClipboardProvider
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.nbt.CompoundTag
import net.minecraft.text.LiteralText
import net.minecraft.util.math.BlockPos
import spinnery.common.BaseContainer
import spinnery.widget.api.WNetworked
import kotlin.math.abs

abstract class ClipboardContainer(synchronizationID: Int, linkedPlayerInventory: PlayerInventory?) :
    BaseContainer(synchronizationID, linkedPlayerInventory) {

    abstract var clipboardProvider: ClipboardProvider

    override fun onInterfaceEvent(widgetSyncId: Int, event: WNetworked.Event?, payload: CompoundTag?) {
        super.onInterfaceEvent(widgetSyncId, event, payload)
        if (event == WNetworked.Event.CUSTOM) {
            if (widgetSyncId < 0) {
                updateTitle(abs(widgetSyncId) - 1, LiteralText(payload!!.getString("text")))
            } else {
                updateNote(widgetSyncId, LiteralText(payload!!.getString("text")))
            }
        } else if (event == WNetworked.Event.MOUSE_CLICK && widgetSyncId > 69) {
            updateTicked(widgetSyncId - 70, !clipboardProvider.getTicked(widgetSyncId - 70))
        }
    }

    open fun updateNote(index: Int, text: LiteralText) {
        clipboardProvider.setNote(index, text)
    }

    open fun updateTitle(page: Int, text: LiteralText) {
        clipboardProvider.setTitle(page, text)
    }

    open fun updateTicked(index: Int, ticked: Boolean) {
        clipboardProvider.setTicked(index, ticked)
    }
}

class ClipboardItemContainer(syncId: Int, playerInv: PlayerInventory?, clipboardSlot: Int)
    : ClipboardContainer(syncId, playerInv) {

    override var clipboardProvider: ClipboardProvider =
        ItemStackClipboardProvider(
            playerInv!!.getInvStack(clipboardSlot)!!
        )
}

class ClipboardEntityContainer(syncId: Int, playerInv: PlayerInventory?, blockPos: BlockPos)
    : ClipboardContainer(syncId, playerInv) {

    override var clipboardProvider: ClipboardProvider =
        EntityClipboardProvider(
            CLIPBOARD_ENTITY_TYPE.get(playerInv!!.player.world, blockPos)!!
        )
}
