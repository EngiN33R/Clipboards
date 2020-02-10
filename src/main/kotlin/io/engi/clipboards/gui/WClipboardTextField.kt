package io.engi.clipboards.gui

import net.fabricmc.fabric.api.network.ClientSidePacketRegistry
import net.minecraft.nbt.CompoundTag
import spinnery.registry.NetworkRegistry
import spinnery.widget.WTextField
import spinnery.widget.api.WFocusedKeyboardListener
import spinnery.widget.api.WNetworked

@WFocusedKeyboardListener
class WClipboardTextField: WTextField(), WNetworked {
    private var wSyncId: Int = 0
    var onTextChangeListener: (text: String) -> Unit = {}

    fun setSyncId(syncId: Int): WClipboardTextField {
        wSyncId = syncId
        return this
    }

    override fun getSyncId(): Int {
        return wSyncId
    }

    override fun onInterfaceEvent(p0: WNetworked.Event?, p1: CompoundTag?) {}

    override fun insertText(insert: String?) {
        super.insertText(insert)
        onTextChange(text)
    }

    override fun deleteText(start: Cursor?, end: Cursor?): String {
        val result = super.deleteText(start, end)
        onTextChange(text)
        return result
    }

    fun onTextChange(text: String) {
        val payload = CompoundTag()
        payload.putString("text", text)
        ClientSidePacketRegistry.INSTANCE.sendToServer(
            NetworkRegistry.SYNCED_WIDGET_PACKET,
            NetworkRegistry.createCustomInterfaceEventPacket(this, payload)
        )
        onTextChangeListener(text)
    }

    override fun appendPayload(event: WNetworked.Event?, payload: CompoundTag?) {
        if (event == WNetworked.Event.CHAR_TYPE) {
            payload?.putString("text", text)
        }
    }
}