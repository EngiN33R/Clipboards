package io.engi.clipboards.gui

import io.engi.clipboards.MODID
import net.minecraft.nbt.CompoundTag
import net.minecraft.util.Identifier
import spinnery.client.BaseRenderer
import spinnery.widget.WAbstractToggle
import spinnery.widget.api.WFocusedMouseListener
import spinnery.widget.api.WNetworked

val INACTIVE_BIG = Identifier(MODID, "textures/gui/box.png")
val ACTIVE_BIG = Identifier(MODID, "textures/gui/box_ticked.png")

@WFocusedMouseListener
class WClipboardBoxToggle: WAbstractToggle(), WNetworked {
    private var wSyncId: Int = 0

    override fun draw() {
        BaseRenderer.drawImage(x.toDouble(), y.toDouble(), z.toDouble(), width.toDouble(), height.toDouble(),
            if (toggleState) ACTIVE_BIG else INACTIVE_BIG)
    }

    fun setSyncId(syncId: Int): WClipboardBoxToggle {
        wSyncId = syncId
        return this
    }

    override fun getSyncId(): Int {
        return wSyncId
    }

    override fun onInterfaceEvent(p0: WNetworked.Event?, p1: CompoundTag?) {}
}