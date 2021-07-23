/*
 * MIT License
 *
 * Copyright 2020 klikli-dev
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT
 * OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

package com.github.klikli_dev.occultism.common.block.storage;

import com.github.klikli_dev.occultism.common.tile.StorageControllerBlockEntity;
import com.github.klikli_dev.occultism.util.Math3DUtil;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.DirectionalBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.Player;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.BlockEntity.BlockEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.level.IBlockReader;
import net.minecraft.level.Level;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public class StorageStabilizerBlock extends Block {

    //region Fields
    private static final Map<Direction, VoxelShape> SHAPES = Maps.newEnumMap(
            ImmutableMap.<Direction, VoxelShape>builder()
                    .put(Direction.EAST, VoxelShapes.create(new AxisAlignedBB(0, 0, 0, 0.3, 1, 1)))
                    .put(Direction.WEST, VoxelShapes.create(new AxisAlignedBB(0.7, 0, 0, 1, 1, 1)))
                    .put(Direction.NORTH, VoxelShapes.create(new AxisAlignedBB(0, 0, 0.7, 1, 1, 1)))
                    .put(Direction.SOUTH, VoxelShapes.create(new AxisAlignedBB(0, 0, 0, 1, 1, 0.3)))
                    .put(Direction.UP, VoxelShapes.create(new AxisAlignedBB(0, 0, 0, 1, 0.3, 1)))
                    .put(Direction.DOWN, VoxelShapes.create(new AxisAlignedBB(0, 0.7, 0, 1, 1, 1))).build());

    //endregion Fields

    //region Initialization
    public StorageStabilizerBlock(Properties properties) {
        super(properties);

    }
    //endregion Initialization

    //region Overrides
    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return SHAPES.get(state.get(BlockStateProperties.FACING));
    }

    @Override
    public void onReplaced(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state != newState) {
            this.notifyStorageControllers(level, pos, state);
        }
        super.onReplaced(state, level, pos, newState, isMoving);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return this.getDefaultState().with(BlockStateProperties.FACING, context.getFace());
    }

    @Override
    public void harvestBlock(Level worldIn, Player player, BlockPos pos, BlockState state,
                             @Nullable BlockEntity te, ItemStack stack) {
        super.harvestBlock(worldIn, player, pos, state, te, stack);
    }

    @Override
    public void onBlockPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer,
                                ItemStack stack) {
        this.notifyStorageControllers(level, pos, state);

        super.onBlockPlacedBy(level, pos, state, placer, stack);
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.FACING);
        super.fillStateContainer(builder);
    }
    //endregion Overrides

    //region Methods
    public void notifyStorageControllers(Level level, BlockPos pos, BlockState state) {
        Direction facing = state.get(DirectionalBlock.FACING);

        //storage controller actually wants stabilizers to point at one block above it, so unless we are on y axis we trace one below
        BlockPos min = facing != Direction.DOWN && facing != Direction.UP ? pos.down() : pos;
        //trace a straight line for the possible controller positions
        List<BlockPos> blocks = Math3DUtil.simpleTrace(min, facing,
                StorageControllerBlockEntity.MAX_STABILIZER_DISTANCE);

        //we are also using the fake trace here, because players may have placed a block on this trace line.
        //The trace line is actually below the line the controller uses to check for valid stabilizers.
        //This is due to the stabilizers aiming for one block above the controller.
        for (BlockPos block : blocks) {
            BlockEntity blockEntity = level.getBlockEntity(block);
            if (BlockEntity instanceof StorageControllerBlockEntity) {
                StorageControllerBlockEntity controller = (StorageControllerBlockEntity) BlockEntity;
                controller.updateStabilizers(); //force controller to re-check available stabilizers.
            }
        }
    }
    //endregion Methods
}


