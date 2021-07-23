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

package com.github.klikli_dev.occultism.common.item.storage;

import com.github.klikli_dev.occultism.api.common.data.GlobalBlockPos;
import com.github.klikli_dev.occultism.api.common.tile.IStorageController;
import com.github.klikli_dev.occultism.common.container.storage.StorageRemoteContainer;
import com.github.klikli_dev.occultism.util.CuriosUtil;
import com.github.klikli_dev.occultism.util.BlockEntityUtil;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.Player;
import net.minecraft.entity.player.Inventory;
import net.minecraft.entity.player.ServerPlayer;
import net.minecraft.inventory.container.AbstractContainerMenu;
import net.minecraft.inventory.container.MenuProvider;
import net.minecraft.item.*;
import net.minecraft.BlockEntity.BlockEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.InteractionResult;
import net.minecraft.util.InteractionHand;
import net.minecraft.util.Util;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.level.Level;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.List;

public class StorageRemoteItem extends Item implements MenuProvider {

    //region Initialization
    public StorageRemoteItem(Properties properties) {
        super(properties);
    }
    //endregion Initialization

    //region Overrides
    @Override
    public ITextComponent getDisplayName() {
        return new TranslationTextComponent(this.getDescriptionId());
    }

    @Override
    public InteractionResult onItemUse(ItemUseContext context) {
        if (!context.getLevel().isClientSide) {
            ItemStack stack = context.getItem();
            BlockEntity blockEntity = context.getLevel().getBlockEntity(context.getPos());
            if (BlockEntity instanceof IStorageController) {
                stack.setTagInfo("linkedStorageController", GlobalBlockPos.from(BlockEntity).serializeNBT());
                context.getPlayer()
                        .sendMessage(new TranslationTextComponent(this.getDescriptionId() + ".message.linked"),
                                Util.DUMMY_UUID);
            }
        }

        return super.onItemUse(context);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getHeldItem(hand);

        if (level.isClientSide || !stack.getOrCreateTag().contains("linkedStorageController"))
            return super.onItemRightClick(level, player, hand);

        GlobalBlockPos storageControllerPos = GlobalBlockPos.from(
                stack.getTag().getCompound("linkedStorageController"));
        Level storageControllerWorld = level.getServer().getWorld(storageControllerPos.getDimensionKey());

        //ensure TE is available
        if (!storageControllerWorld.isBlockLoaded(storageControllerPos.getPos())) {
            player.sendMessage(new TranslationTextComponent(this.getDescriptionId() + ".message.not_loaded"), Util.DUMMY_UUID);
            return super.onItemRightClick(level, player, hand);
        }

        //then access it and if it fits, open UI
        if (storageControllerWorld.getBlockEntity(storageControllerPos.getPos()) instanceof IStorageController) {
            NetworkHooks.openGui((ServerPlayer) player, this, buffer -> buffer.writeVarInt(player.inventory.currentItem));
            return new ActionResult<>(InteractionResult.SUCCESS, stack);
        }
        return super.onItemRightClick(level, player, hand);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<ITextComponent> tooltip,
                               ITooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);

        tooltip.add(new TranslationTextComponent(this.getDescriptionId() + ".tooltip"));
        if (stack.getOrCreateTag().contains("linkedStorageController")) {
            GlobalBlockPos pos = GlobalBlockPos.from(stack.getTag().getCompound("linkedStorageController"));
            tooltip.add(new TranslationTextComponent(this.getDescriptionId() + ".tooltip.linked", pos.toString()));
        }
    }

    @Override
    public Rarity getRarity(ItemStack stack) {
        return stack.getOrCreateTag().contains("linkedStorageController") ? Rarity.RARE : Rarity.COMMON;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {

        ItemStack storageRemoteStack = CuriosUtil.getStorageRemote(player);
        int selectedSlot = -1;
        //if not found, try to get from player inventory
        if (!(storageRemoteStack.getItem() instanceof StorageRemoteItem)) {
            selectedSlot = CuriosUtil.getFirstStorageRemoteSlot(player);
            storageRemoteStack = selectedSlot > 0 ? player.inventory.getStackInSlot(selectedSlot) : ItemStack.EMPTY;
        }
        //now, if we have a storage remote, proceed
        if (storageRemoteStack.getItem() instanceof StorageRemoteItem) {
            return new StorageRemoteContainer(id, playerInventory, selectedSlot);

        } else {
            return null;
        }
    }
    //endregion Overrides

    //region Static Methods
    public static IStorageController getStorageController(ItemStack stack, Level level) {
        //Invalid item or cannot not get hold of server instance

        if (stack.isEmpty()) {
            return null;
        }
        //no storage controller linked
        if (!stack.getOrCreateTag().contains("linkedStorageController"))
            return null;

        GlobalBlockPos globalPos = GlobalBlockPos.from(stack.getTag().getCompound("linkedStorageController"));
        BlockEntity blockEntity = BlockEntityUtil.get(level, globalPos);

        return BlockEntity instanceof IStorageController ? (IStorageController) BlockEntity : null;
    }
    //endregion Static Methods
}

