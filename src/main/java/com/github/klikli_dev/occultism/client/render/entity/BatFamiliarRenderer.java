/*
 * MIT License
 *
 * Copyright 2021 vemerion
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

package com.github.klikli_dev.occultism.client.render.entity;

import com.github.klikli_dev.occultism.Occultism;
import com.github.klikli_dev.occultism.client.model.entity.BatFamiliarModel;
import com.github.klikli_dev.occultism.common.entity.familiar.BatFamiliarEntity;
import com.github.klikli_dev.occultism.registry.OccultismModelLayers;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class BatFamiliarRenderer extends MobRenderer<BatFamiliarEntity, BatFamiliarModel> {

    private static final ResourceLocation TEXTURES = new ResourceLocation(Occultism.MODID,
            "textures/entity/bat_familiar.png");

    public BatFamiliarRenderer(EntityRendererProvider.Context context) {
        super(context, new BatFamiliarModel(context.bakeLayer(OccultismModelLayers.FAMILIAR_BAT)), 0.3f);

    }

    @Override
    public void render(BatFamiliarEntity entityIn, float entityYaw, float partialTicks, PoseStack poseStack,
                       MultiBufferSource bufferIn, int packedLightIn) {
        poseStack.pushPose();
        if (!entityIn.isSitting() || entityIn.isPartying())
            poseStack.translate(0, entityIn.getAnimationHeight(partialTicks) * 0.1 + 0.1f, 0);
        super.render(entityIn, entityYaw, partialTicks, poseStack, bufferIn, packedLightIn);
        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(BatFamiliarEntity entity) {
        return TEXTURES;
    }
}
