package aztech.modern_industrialization.recipe;

import aztech.modern_industrialization.machines.factory.MachineBlockEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.List;

public class MachineRecipe implements Recipe<MachineBlockEntity> {
    final Identifier id;
    final MachineRecipeType type;

    public int eu;
    public int duration;
    public List<ItemInput> itemInputs;
    public List<FluidInput> fluidInputs;
    public List<ItemOutput> itemOutputs;
    public List<FluidOutput> fluidOutputs;

    MachineRecipe(Identifier id, MachineRecipeType type) {
        this.id = id;
        this.type = type;
    }

    @Override
    public boolean isIgnoredInRecipeBook() {
        return true;
    }

    @Override
    public boolean matches(MachineBlockEntity inv, World world) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ItemStack craft(MachineBlockEntity inv) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean fits(int width, int height) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ItemStack getOutput() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Identifier getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return type;
    }

    @Override
    public RecipeType<?> getType() {
        return type;
    }

    public static class ItemInput {
        public final Item item;
        public final int amount;

        public ItemInput(Item item, int amount) {
            this.item = item;
            this.amount = amount;
        }
    }

    public static class FluidInput {
        public final Fluid fluid;
        public final int amount;

        public FluidInput(Fluid fluid, int amount) {
            this.fluid = fluid;
            this.amount = amount;
        }
    }

    public static class ItemOutput {
        public final Item item;
        public final int amount;

        public ItemOutput(Item item, int amount) {
            this.item = item;
            this.amount = amount;
        }
    }

    public static class FluidOutput {
        public final Fluid fluid;
        public final int amount;

        public FluidOutput(Fluid fluid, int amount) {
            this.fluid = fluid;
            this.amount = amount;
        }
    }
}