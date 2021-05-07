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

package com.github.klikli_dev.occultism.crafting.recipe;

import com.github.klikli_dev.occultism.registry.OccultismRecipes;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapelessRecipe;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nonnull;

public class RitualIngredientRecipe extends ShapelessRecipe {
    //region Fields
    public static Serializer SERIALIZER = new Serializer();

    private ResourceLocation pentacleId;
    private ItemStack ritual;
    private Ingredient activationItem;
    //endregion Fields

    //region Initialization
    public RitualIngredientRecipe(ResourceLocation id, String group, ResourceLocation pentacleId, ItemStack ritual,
                                  ItemStack result, Ingredient activationItem, NonNullList<Ingredient> input) {
        super(id, group, result, input);
        this.pentacleId = pentacleId;
        this.ritual = ritual;
        this.activationItem = activationItem;
    }
    //endregion Initialization

    //region Getter / Setter
    public ResourceLocation getPentacleId() {
        return pentacleId;
    }

    public void setPentacleId(ResourceLocation pentacleId) {
        this.pentacleId = pentacleId;
    }

    public ItemStack getRitual() {
        return ritual;
    }

    public void setRitual(ItemStack ritual) {
        this.ritual = ritual;
    }

    public Ingredient getActivationItem() {
        return activationItem;
    }

    public void setActivationItem(Ingredient activationItem) {
        this.activationItem = activationItem;
    }
    //endregion Getter / Setter

    //region Overrides
    @Override
    public IRecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public boolean matches(@Nonnull CraftingInventory inventory, @Nonnull World world) {
        return false;
    }

    @Override
    public ItemStack getCraftingResult(CraftingInventory inventoryCrafting) {
        //as we don't have an inventory this is ignored.
        return null;
    }

    @Override
    public IRecipeType<?> getType() {
        return OccultismRecipes.RITUAL_INGREDIENT_TYPE.get();
    }

    //endregion Overrides


    public static class Serializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<RitualIngredientRecipe> {
        //region Fields
        private static final ShapelessRecipe.Serializer serializer = new ShapelessRecipe.Serializer();
        //endregion Fields

        //region Overrides
        @Override
        public RitualIngredientRecipe read(ResourceLocation recipeId, JsonObject json) {
            ShapelessRecipe recipe = serializer.read(recipeId, json);
            JsonElement activationItemElement =
                    JSONUtils.isJsonArray(json, "activation_item") ? JSONUtils.getJsonArray(json,
                            "activation_item") : JSONUtils.getJsonObject(json, "activation_item");
            Ingredient activationItem = Ingredient.deserialize(activationItemElement);
            ResourceLocation pentacleId = new ResourceLocation(json.get("pentacle").getAsString());
            ItemStack ritual = CraftingHelper.getItemStack(JSONUtils.getJsonObject(json, "ritual"), true);
            return new RitualIngredientRecipe(recipe.getId(), recipe.getGroup(), pentacleId, ritual,
                    recipe.getRecipeOutput(), activationItem,
                    recipe.getIngredients());
        }

        @Override
        public RitualIngredientRecipe read(ResourceLocation recipeId, PacketBuffer buffer) {
            ShapelessRecipe recipe = serializer.read(recipeId, buffer);
            ResourceLocation pentacleId = buffer.readResourceLocation();
            ItemStack ritual = buffer.readItemStack();
            Ingredient activationItem = Ingredient.read(buffer);

            return new RitualIngredientRecipe(recipe.getId(), recipe.getGroup(), pentacleId, ritual,
                    recipe.getRecipeOutput(), activationItem,
                    recipe.getIngredients());
        }

        @Override
        public void write(PacketBuffer buffer, RitualIngredientRecipe recipe) {
            serializer.write(buffer, recipe);
            buffer.writeResourceLocation(recipe.pentacleId);
            buffer.writeItemStack(recipe.ritual);
            recipe.activationItem.write(buffer);
        }
        //endregion Overrides
    }
}
