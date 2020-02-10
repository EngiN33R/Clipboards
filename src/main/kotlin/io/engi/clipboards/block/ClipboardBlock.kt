package io.engi.clipboards.block

import io.engi.clipboards.CLIPBOARD_GUI_ENTITY
import io.engi.clipboards.CLIPBOARD_ITEM
import io.engi.clipboards.entity.ClipboardBlockEntity
import net.fabricmc.fabric.api.container.ContainerProviderRegistry
import net.minecraft.block.Block
import net.minecraft.block.BlockEntityProvider
import net.minecraft.block.BlockState
import net.minecraft.block.HorizontalFacingBlock
import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.EntityContext
import net.minecraft.entity.ItemEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemPlacementContext
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.state.StateManager
import net.minecraft.state.property.DirectionProperty
import net.minecraft.state.property.EnumProperty
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.StringIdentifiable
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.shape.VoxelShape
import net.minecraft.world.BlockView
import net.minecraft.world.World
import net.minecraft.world.WorldView

enum class VerticalFacing: StringIdentifiable {
    UP,
    SIDE,
    DOWN;

    override fun asString(): String {
        return this.toString().toLowerCase()
    }

    fun asDirection(): Direction? {
        return when (this) {
            UP -> Direction.UP
            DOWN -> Direction.DOWN
            SIDE -> null
        }
    }
}

fun toVerticalFacing(dir: Direction): VerticalFacing {
    return when (dir) {
        Direction.UP -> VerticalFacing.UP
        Direction.DOWN -> VerticalFacing.DOWN
        else -> VerticalFacing.SIDE
    }
}

val FACING: DirectionProperty = HorizontalFacingBlock.FACING
val VERTICAL: EnumProperty<VerticalFacing> = EnumProperty.of("vertical", VerticalFacing::class.java)

fun getShape(facing: Direction, vertical: VerticalFacing): VoxelShape {
    return when (vertical) {
        VerticalFacing.UP -> when (facing) {
            Direction.NORTH, Direction.SOUTH -> Block.createCuboidShape(3.0, 0.0, 1.0, 13.0, 3.0, 15.0)
            else -> Block.createCuboidShape(1.0, 0.0, 3.0, 15.0, 3.0, 13.0)
        }
        VerticalFacing.DOWN -> when (facing) {
            Direction.NORTH, Direction.SOUTH -> Block.createCuboidShape(3.0, 13.0, 1.0, 13.0, 16.0, 15.0)
            else -> Block.createCuboidShape(1.0, 13.0, 3.0, 15.0, 16.0, 13.0)
        }
        VerticalFacing.SIDE -> when (facing) {
            Direction.NORTH -> Block.createCuboidShape(3.0, 1.0, 13.0, 13.0, 15.0, 16.0)
            Direction.EAST -> Block.createCuboidShape(0.0, 1.0, 3.0, 3.0, 15.0, 13.0)
            Direction.WEST -> Block.createCuboidShape(13.0, 1.0, 3.0, 16.0, 15.0, 13.0)
            else -> Block.createCuboidShape(3.0, 1.0, 0.0, 13.0, 15.0, 3.0)
        }
    }
}

@Suppress("MemberVisibilityCanBePrivate")
class ClipboardBlock(settings: Settings?) : Block(settings), BlockEntityProvider {
    init {
        defaultState = getStateManager().defaultState.with(FACING, Direction.NORTH).with(
            VERTICAL,
            VerticalFacing.SIDE
        )
    }

    override fun getOutlineShape(
        state: BlockState?,
        view: BlockView?,
        pos: BlockPos?,
        ePos: EntityContext?
    ): VoxelShape {
        return getShape(
            state!!.get(FACING),
            state.get(VERTICAL)
        )
    }

    override fun appendProperties(builder: StateManager.Builder<Block?, BlockState?>) {
        builder.add(
            FACING,
            VERTICAL
        )
    }

    override fun getPlacementState(ctx: ItemPlacementContext): BlockState? {
        val vertical = toVerticalFacing(ctx.side)
        val facing = if (vertical == VerticalFacing.SIDE) ctx.side else ctx.playerFacing.opposite
        return defaultState.with(FACING, facing).with(VERTICAL, vertical)
    }

    override fun createBlockEntity(view: BlockView?): BlockEntity? {
        return ClipboardBlockEntity()
    }

    override fun canPlaceAt(state: BlockState, world: WorldView, pos: BlockPos): Boolean {
        val verticalDir = state.get(VERTICAL).asDirection()
        val backingBlockPos = if (verticalDir != null) {
            pos.offset(verticalDir.opposite)
        } else {
            pos.offset((state.get(FACING) as Direction).opposite)
        }
        return world.getBlockState(backingBlockPos).material.isSolid
    }

    override fun canMobSpawnInside(): Boolean {
        return true
    }

    override fun onUse(
        state: BlockState,
        world: World,
        pos: BlockPos,
        player: PlayerEntity,
        hand: Hand,
        hit: BlockHitResult
    ): ActionResult {
        val entity = world.getBlockEntity(pos)
        if (entity !is ClipboardBlockEntity) return ActionResult.PASS
        if (world.isClient) return ActionResult.SUCCESS
        ContainerProviderRegistry.INSTANCE.openContainer(
            CLIPBOARD_GUI_ENTITY, player
        ) { buf -> buf.writeBlockPos(pos).writeCompoundTag(entity.toTag(CompoundTag())) }
        return ActionResult.SUCCESS
    }

    override fun onBreak(world: World?, pos: BlockPos?, state: BlockState?, player: PlayerEntity?) {
        super.onBreak(world, pos, state, player)
        if (world!!.isClient) return
        if (player!!.isCreative) return
        val entity = world.getBlockEntity(pos)
        if (entity !is ClipboardBlockEntity) return
        val itemStack = ItemStack(CLIPBOARD_ITEM)
        itemStack.setCustomName(if (entity.name?.asFormattedString()?.isEmpty() == false) entity.name else null)
        itemStack.putSubTag("BlockEntityTag", entity.toTag(CompoundTag()))
        val itemEntity =
            ItemEntity(world, pos!!.x.toDouble() + 0.5, pos.y.toDouble() + 0.5, pos.z.toDouble() + 0.5, itemStack)
        itemEntity.setToDefaultPickupDelay()
        world.spawnEntity(itemEntity)
    }


}