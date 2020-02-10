package io.engi.clipboards.item

import io.engi.clipboards.CLIPBOARD_GUI_ITEM
import io.engi.clipboards.entity.ClipboardBlockEntity
import net.fabricmc.fabric.api.container.ContainerProviderRegistry
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUsageContext
import net.minecraft.nbt.CompoundTag
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class ClipboardItem(block: Block?, settings: Settings?) : BlockItem(block, settings) {
    override fun use(world: World?, user: PlayerEntity?, hand: Hand?): TypedActionResult<ItemStack> {
        if (user?.isSneaking == true) {
            return super.use(world, user, hand)
        }
        val held = user!!.getStackInHand(hand)
        if (world!!.isClient) return TypedActionResult.success(held)

        var slot: Int? = null
        for (invSlot in 0 until user.inventory.invSize) {
            if (user.inventory.getInvStack(invSlot) === held) {
                slot = invSlot
            }
        }
        if (slot == null) {
            return TypedActionResult.fail(held)
        }

        ContainerProviderRegistry.INSTANCE.openContainer(
            CLIPBOARD_GUI_ITEM, user
        ) { buf ->
            run {
                buf.writeInt(slot)
                buf.writeCompoundTag(held.orCreateTag.getCompound("BlockEntityTag") ?: CompoundTag())
            }
        }

        return TypedActionResult.success(held)
    }

    override fun useOnBlock(context: ItemUsageContext?): ActionResult {
        if (context?.player?.isSneaking == true) {
            return super.useOnBlock(context)
        }
        return use(context!!.world, context.player, context.hand).result
    }

    override fun postPlacement(
        pos: BlockPos?,
        world: World?,
        player: PlayerEntity?,
        stack: ItemStack?,
        state: BlockState?
    ): Boolean {
        val result = super.postPlacement(pos, world, player, stack, state)
        if (world!!.isClient) return true
        if (!stack!!.hasCustomName()) return result
        if (!result) return false
        val entity = world.getBlockEntity(pos)
        if (entity !is ClipboardBlockEntity) return true
        entity.name = stack.name
        return true
    }
}