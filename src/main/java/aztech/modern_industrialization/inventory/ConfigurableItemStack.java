/*
 * MIT License
 *
 * Copyright (c) 2020 Azercoco & Technici4n
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package aztech.modern_industrialization.inventory;

import aztech.modern_industrialization.util.NbtHelper;
import com.google.common.primitives.Ints;
import dev.technici4n.fasttransferlib.experimental.api.item.ItemKey;
import dev.technici4n.fasttransferlib.experimental.api.item.ItemPreconditions;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ResourceAmount;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.slot.Slot;

/**
 * An item stack that can be configured.
 */
public class ConfigurableItemStack extends SnapshotParticipant<ResourceAmount<ItemKey>> implements StorageView<ItemKey>, IConfigurableSlot {
    private ItemKey key = ItemKey.empty();
    private int count = 0;
    private Item lockedItem = null;
    private boolean playerLocked = false;
    private boolean machineLocked = false;
    private boolean playerLockable = true;
    private boolean playerInsert = false;
    private boolean playerExtract = true;
    private boolean pipesInsert = false;
    private boolean pipesExtract = false;

    public ConfigurableItemStack() {
    }

    public static ConfigurableItemStack standardInputSlot() {
        ConfigurableItemStack stack = new ConfigurableItemStack();
        stack.playerInsert = true;
        stack.pipesInsert = true;
        return stack;
    }

    public static ConfigurableItemStack standardOutputSlot() {
        ConfigurableItemStack stack = new ConfigurableItemStack();
        stack.pipesExtract = true;
        return stack;
    }

    public static ConfigurableItemStack standardIOSlot(boolean pipeIO) {
        ConfigurableItemStack stack = new ConfigurableItemStack();
        stack.playerInsert = true;
        if (pipeIO) {
            stack.pipesInsert = true;
            stack.pipesExtract = true;
        }
        return stack;
    }

    public ConfigurableItemStack(ConfigurableItemStack other) {
        this();
        this.key = other.key;
        this.count = other.count;
        this.lockedItem = other.lockedItem;
        this.playerLocked = other.playerLocked;
        this.machineLocked = other.machineLocked;
        this.playerLockable = other.playerLockable;
        this.playerInsert = other.playerInsert;
        this.playerExtract = other.playerExtract;
        this.pipesInsert = other.pipesInsert;
        this.pipesExtract = other.pipesExtract;
    }

    @Override
    public SlotConfig getConfig() {
        return new SlotConfig(playerLockable, playerInsert, playerExtract, pipesInsert, pipesExtract);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ConfigurableItemStack that = (ConfigurableItemStack) o;
        return playerLocked == that.playerLocked && machineLocked == that.machineLocked && playerLockable == that.playerLockable
                && playerInsert == that.playerInsert && playerExtract == that.playerExtract && pipesInsert == that.pipesInsert
                && pipesExtract == that.pipesExtract && lockedItem == that.lockedItem && count == that.count && key.equals(that.key);
    }

    /**
     * Create a copy of a list of configurable fluid stacks.
     */
    public static ArrayList<ConfigurableItemStack> copyList(List<ConfigurableItemStack> list) {
        ArrayList<ConfigurableItemStack> copy = new ArrayList<>(list.size());
        for (ConfigurableItemStack stack : list) {
            copy.add(new ConfigurableItemStack(stack));
        }
        return copy;
    }

    public ItemKey getItemKey() {
        return key;
    }

    public int getCount() {
        return count;
    }

    public Item getLockedItem() {
        return lockedItem;
    }

    public void setCount(int count) {
        this.count = count;
        if (count == 0) {
            this.key = ItemKey.empty();
        }
    }

    public void increment(int count) {
        setCount(this.count + count);
    }

    public void decrement(int count) {
        increment(-count);
    }

    public void setItemKey(ItemKey key) {
        this.key = key;
    }

    public boolean isValid(ItemStack stack) {
        return isValid(stack.getItem());
    }

    public boolean isValid(Item item) {
        return lockedItem == null || lockedItem == item;
    }

    public boolean isPlayerLocked() {
        return playerLocked;
    }

    public boolean isMachineLocked() {
        return machineLocked;
    }

    public void enableMachineLock(Item lockedItem) {
        if (this.lockedItem != null && lockedItem != this.lockedItem)
            throw new RuntimeException("Trying to override locked item");
        machineLocked = true;
        this.lockedItem = lockedItem;
    }

    public void disableMachineLock() {
        machineLocked = false;
        onToggleLock();
    }

    public void togglePlayerLock(ItemStack cursorStack) {
        if (playerLockable) {
            if (playerLocked && lockedItem == Items.AIR && !cursorStack.isEmpty()) {
                lockedItem = cursorStack.getItem();
            } else {
                playerLocked = !playerLocked;
            }
            onToggleLock();
        }
    }

    private void onToggleLock() {
        if (!machineLocked && !playerLocked) {
            lockedItem = null;
        } else if (lockedItem == null) {
            lockedItem = key.getItem();
        }
    }

    public boolean canPlayerLock() {
        return playerLockable;
    }

    public NbtCompound toNbt() {
        NbtCompound tag = new NbtCompound();
        tag.put("key", key.toNbt());
        tag.putInt("count", count);
        if (lockedItem != null) {
            NbtHelper.putItem(tag, "lockedItem", lockedItem);
        }
        // TODO: more efficient encoding?
        tag.putBoolean("machineLocked", machineLocked);
        tag.putBoolean("playerLocked", playerLocked);
        tag.putBoolean("playerLockable", playerLockable);
        tag.putBoolean("playerInsert", playerInsert);
        tag.putBoolean("playerExtract", playerExtract);
        tag.putBoolean("pipesInsert", pipesInsert);
        tag.putBoolean("pipesExtract", pipesExtract);
        return tag;
    }

    public static ConfigurableItemStack fromNbt(NbtCompound tag) {
        ConfigurableItemStack is = new ConfigurableItemStack();
        // compat
        if (tag.contains("key")) {
            is.key = ItemKey.fromNbt(tag.getCompound("key"));
            is.count = tag.getInt("count");
        } else {
            ItemStack stack = ItemStack.fromNbt(tag);
            is.key = ItemKey.of(stack);
            is.count = stack.getCount();
        }
        if (tag.contains("lockedItem")) {
            is.lockedItem = NbtHelper.getItem(tag, "lockedItem");
        }
        is.machineLocked = tag.getBoolean("machineLocked");
        is.playerLocked = tag.getBoolean("playerLocked");
        is.playerLockable = tag.getBoolean("playerLockable");
        is.playerInsert = tag.getBoolean("playerInsert");
        is.playerExtract = tag.getBoolean("playerExtract");
        is.pipesInsert = tag.getBoolean("pipesInsert");
        is.pipesExtract = tag.getBoolean("pipesExtract");
        return is;
    }

    /**
     * Try locking the slot to the given item, return true if it succeeded
     */
    public boolean playerLock(Item item) {
        if ((key.isEmpty() || key.getItem() == item) && (lockedItem == null || lockedItem == Items.AIR)) {
            lockedItem = item;
            playerLocked = true;
            return true;
        }
        return false;
    }

    public boolean canPipesExtract() {
        return pipesExtract;
    }

    public boolean canPipesInsert() {
        return pipesInsert;
    }

    @Override
    public long extract(ItemKey key, long longCount, Transaction transaction) {
        ItemPreconditions.notEmptyNotNegative(key, longCount);
        if (pipesExtract && key.equals(this.key)) {
            int maxCount = Ints.saturatedCast(longCount);
            int extracted = Math.min(count, maxCount);
            updateSnapshots(transaction);
            decrement(extracted);
            return extracted;
        }
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return resource().isEmpty();
    }

    @Override
    public ItemKey resource() {
        return key;
    }

    @Override
    public long amount() {
        return count;
    }

    @Override
    public long capacity() {
        return isEmpty() ? 64 : resource().getItem().getMaxCount();
    }

    @Override
    public ResourceAmount<ItemKey> createSnapshot() {
        return new ResourceAmount<>(key, count);
    }

    @Override
    public void readSnapshot(ResourceAmount<ItemKey> ra) {
        this.count = (int) ra.amount();
        this.key = ra.resource();
    }

    public class ConfigurableItemSlot extends Slot {
        private final Predicate<ItemStack> insertPredicate;
        private final Runnable markDirty;
        // Vanilla MC code modifies the stack returned by `getStack()` directly, but it
        // calls `markDirty()` when that happens, so we just cache the returned stack,
        // and set it when `markDirty()` is called.
        private ItemStack cachedReturnedStack = null;

        public ConfigurableItemSlot(ConfigurableItemSlot other) {
            this(other.markDirty, other.x, other.y, other.insertPredicate);

            this.id = other.id;
        }

        public ConfigurableItemSlot(Runnable markDirty, int x, int y, Predicate<ItemStack> insertPredicate) {
            super(null, 0, x, y);

            this.insertPredicate = insertPredicate;
            this.markDirty = markDirty;
        }

        @Override
        public boolean canInsert(ItemStack stack) {
            return playerInsert && ConfigurableItemStack.this.isValid(stack) && insertPredicate.test(stack);
        }

        @Override
        public boolean canTakeItems(PlayerEntity playerEntity) {
            return playerExtract;
        }

        public ConfigurableItemStack getConfStack() {
            return ConfigurableItemStack.this;
        }

        @Override
        public ItemStack getStack() {
            return cachedReturnedStack = key.toStack(count);
        }

        @Override
        public void setStack(ItemStack stack) {
            key = ItemKey.of(stack);
            count = stack.getCount();
            markDirty.run();
            cachedReturnedStack = stack;
        }

        @Override
        public void markDirty() {
            if (cachedReturnedStack != null) {
                setStack(cachedReturnedStack);
            }
        }

        @Override
        public int getMaxItemCount() {
            return 64;
        }

        @Override
        public ItemStack takeStack(int amount) {
            ItemStack stack = key.toStack(amount);
            decrement(amount);
            cachedReturnedStack = null;
            markDirty.run();
            return stack;
        }
    }
}
