package io.engi.clipboards

import io.engi.clipboards.block.ClipboardBlock
import io.engi.clipboards.entity.ClipboardBlockEntity
import io.engi.clipboards.gui.ClipboardEntityContainer
import io.engi.clipboards.gui.ClipboardItemContainer
import io.engi.clipboards.gui.ClipboardScreen
import io.engi.clipboards.item.ClipboardItem
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.block.FabricBlockSettings
import net.fabricmc.fabric.api.client.screen.ScreenProviderRegistry
import net.fabricmc.fabric.api.container.ContainerProviderRegistry
import net.minecraft.block.Blocks
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemGroup
import net.minecraft.text.LiteralText
import net.minecraft.util.Identifier
import net.minecraft.util.PacketByteBuf
import net.minecraft.util.registry.Registry
import java.util.function.Supplier

const val MODID = "clipboards"

val CLIPBOARD_ID = Identifier(MODID, "clipboard")
val CLIPBOARD_GUI_ITEM = Identifier(MODID, "clipboard_item")
val CLIPBOARD_GUI_ENTITY = Identifier(MODID, "clipboard_entity")
val CLIPBOARD_BLOCK =
    ClipboardBlock(FabricBlockSettings.copy(Blocks.OAK_WALL_SIGN).build())
val CLIPBOARD_ITEM = ClipboardItem(
    CLIPBOARD_BLOCK,
    Item.Settings().group(ItemGroup.TOOLS).maxCount(1)
)
val CLIPBOARD_ENTITY_TYPE: BlockEntityType<ClipboardBlockEntity> = BlockEntityType.Builder.create<ClipboardBlockEntity>(
    Supplier { ClipboardBlockEntity() }, CLIPBOARD_BLOCK).build(null)

@Suppress("unused")
class Clipboards: ModInitializer {
    override fun onInitialize() {
        Registry.register(Registry.BLOCK, CLIPBOARD_ID, CLIPBOARD_BLOCK)
        Registry.register(Registry.ITEM, CLIPBOARD_ID, CLIPBOARD_ITEM)
        Registry.register(Registry.BLOCK_ENTITY_TYPE, CLIPBOARD_ID, CLIPBOARD_ENTITY_TYPE)

        ContainerProviderRegistry.INSTANCE.registerFactory(CLIPBOARD_GUI_ENTITY)
        { syncId: Int, _: Identifier?, player: PlayerEntity, buffer: PacketByteBuf? ->
            ClipboardEntityContainer(syncId, player.inventory, buffer!!.readBlockPos())
        }
        ContainerProviderRegistry.INSTANCE.registerFactory(CLIPBOARD_GUI_ITEM)
        { syncId: Int, _: Identifier?, player: PlayerEntity, buffer: PacketByteBuf? ->
            ClipboardItemContainer(syncId, player.inventory, buffer!!.readInt())
        }
    }
}

@Suppress("unused")
class ClipboardsClient: ClientModInitializer {
    override fun onInitializeClient() {
        ScreenProviderRegistry.INSTANCE.registerFactory(CLIPBOARD_GUI_ENTITY)
        { syncId: Int, _: Identifier?, player: PlayerEntity, buf: PacketByteBuf? ->
            ClipboardScreen(
                LiteralText("test"), ClipboardEntityContainer(syncId, player.inventory, buf!!.readBlockPos()), player, buf.readCompoundTag()!!
            )
        }
        ScreenProviderRegistry.INSTANCE.registerFactory(CLIPBOARD_GUI_ITEM)
        { syncId: Int, _: Identifier?, player: PlayerEntity, buf: PacketByteBuf? ->
            ClipboardScreen(
                LiteralText("test"), ClipboardItemContainer(syncId, player.inventory, buf!!.readInt()), player, buf.readCompoundTag()!!
            )
        }
    }
}