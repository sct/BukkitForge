package keepcalm.mods.bukkit.bukkitAPI.inventory;

import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;

import net.minecraft.command.WrongUsageException;
import net.minecraft.inventory.IInventory;

import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class BukkitInventory implements Inventory {

	protected IInventory inventory;
	
	public BukkitInventory(IInventory inventory) {
		this.inventory = inventory;
	}
	

    public IInventory getInventory() {
        return inventory;
    }

    public int getSize() {
        return getInventory().getSizeInventory();
    }

    public String getName() {
        return getInventory().getInvName();
    }

    public ItemStack getItem(int index) {
        net.minecraft.item.ItemStack item = getInventory().getStackInSlot(index);
        return item == null ? null : new BukkitItemStack(item);
    }

    public ItemStack[] getContents() {
        ItemStack[] items = new ItemStack[getSize()];
        net.minecraft.item.ItemStack[] mcItems = new net.minecraft.item.ItemStack[getSize()];

        for (int i = 0; i < mcItems.length; i++) {
            items[i] = this.inventory.getStackInSlot(i) == null ? null : new BukkitItemStack(mcItems[i]);
        }

        return items;
    }
    protected net.minecraft.item.ItemStack[] getMCContents() {
    	net.minecraft.item.ItemStack[] mcItems = new net.minecraft.item.ItemStack[getSize()];
    	for (int i = 0; i < mcItems.length; i++) {
    		mcItems[i] = inventory.getStackInSlot(i);
    	}
    	return mcItems;
    }
    public void setContents(ItemStack[] items) {
        if (getContents().length < items.length) {
            throw new IllegalArgumentException("Invalid inventory size; expected " + getContents().length + " or less");
        }

        net.minecraft.item.ItemStack[] mcItems = getMCContents();

        for (int i = 0; i < mcItems.length; i++) {
            if (i >= items.length) {
                mcItems[i] = null;
            } else {
                mcItems[i] = BukkitItemStack.createNMSItemStack(items[i]);
            }
        }
    }

    public void setItem(int index, ItemStack item) {
        getInventory().setInventorySlotContents(index, ((item == null || item.getTypeId() == 0) ? null : BukkitItemStack.createNMSItemStack(item)));
    }

    public boolean contains(int materialId) {
        for (ItemStack item : getContents()) {
            if (item != null && item.getTypeId() == materialId) {
                return true;
            }
        }
        return false;
    }

    public boolean contains(Material material) {
        return contains(material.getId());
    }

    public boolean contains(ItemStack item) {
        if (item == null) {
            return false;
        }
        for (ItemStack i : getContents()) {
            if (item.equals(i)) {
                return true;
            }
        }
        return false;
    }

    public boolean contains(int materialId, int amount) {
        int amt = 0;
        for (ItemStack item : getContents()) {
            if (item != null && item.getTypeId() == materialId) {
                amt += item.getAmount();
            }
        }
        return amt >= amount;
    }

    public boolean contains(Material material, int amount) {
        return contains(material.getId(), amount);
    }

    public boolean contains(ItemStack item, int amount) {
        if (item == null) {
            return false;
        }
        int amt = 0;
        for (ItemStack i : getContents()) {
            if (item.equals(i)) {
                amt += item.getAmount();
            }
        }
        return amt >= amount;
    }

    public HashMap<Integer, ItemStack> all(int materialId) {
        HashMap<Integer, ItemStack> slots = new HashMap<Integer, ItemStack>();

        ItemStack[] inventory = getContents();
        for (int i = 0; i < inventory.length; i++) {
            ItemStack item = inventory[i];
            if (item != null && item.getTypeId() == materialId) {
                slots.put(i, item);
            }
        }
        return slots;
    }

    public HashMap<Integer, ItemStack> all(Material material) {
        return all(material.getId());
    }

    public HashMap<Integer, ItemStack> all(ItemStack item) {
        HashMap<Integer, ItemStack> slots = new HashMap<Integer, ItemStack>();
        if (item != null) {
            ItemStack[] inventory = getContents();
            for (int i = 0; i < inventory.length; i++) {
                if (item.equals(inventory[i])) {
                    slots.put(i, inventory[i]);
                }
            }
        }
        return slots;
    }

    public int first(int materialId) {
        ItemStack[] inventory = getContents();
        for (int i = 0; i < inventory.length; i++) {
            ItemStack item = inventory[i];
            if (item != null && item.getTypeId() == materialId) {
                return i;
            }
        }
        return -1;
    }

    public int first(Material material) {
        return first(material.getId());
    }

    public int first(ItemStack item) {
        return first(item, true);
    }

    public int first(ItemStack item, boolean withAmount) {
        if (item == null) {
            return -1;
        }
        ItemStack[] inventory = getContents();
        for (int i = 0; i < inventory.length; i++) {
            if (inventory[i] == null) continue;

            boolean equals = false;

            if (withAmount) {
                equals = item.equals(inventory[i]);
            } else {
                equals = item.getTypeId() == inventory[i].getTypeId() && item.getDurability() == inventory[i].getDurability() && item.getEnchantments().equals(inventory[i].getEnchantments());
            }

            if (equals) {
                return i;
            }
        }
        return -1;
    }

    public int firstEmpty() {
        ItemStack[] inventory = getContents();
        for (int i = 0; i < inventory.length; i++) {
            if (inventory[i] == null) {
                return i;
            }
        }
        return -1;
    }

    public int firstPartial(int materialId) {
        ItemStack[] inventory = getContents();
        for (int i = 0; i < inventory.length; i++) {
            ItemStack item = inventory[i];
            if (item != null && item.getTypeId() == materialId && item.getAmount() < item.getMaxStackSize()) {
                return i;
            }
        }
        return -1;
    }

    public int firstPartial(Material material) {
        return firstPartial(material.getId());
    }

    public int firstPartial(ItemStack item) {
        ItemStack[] inventory = getContents();
        ItemStack filteredItem = new BukkitItemStack(item);
        if (item == null) {
            return -1;
        }
        for (int i = 0; i < inventory.length; i++) {
            ItemStack cItem = inventory[i];
            if (cItem != null && cItem.getTypeId() == filteredItem.getTypeId() && cItem.getAmount() < cItem.getMaxStackSize() && cItem.getDurability() == filteredItem.getDurability() && cItem.getEnchantments().equals(filteredItem.getEnchantments())) {
                return i;
            }
        }
        return -1;
    }

    public HashMap<Integer, ItemStack> addItem(ItemStack... items) {
        HashMap<Integer, ItemStack> leftover = new HashMap<Integer, ItemStack>();

        /* TODO: some optimization
         *  - Create a 'firstPartial' with a 'fromIndex'
         *  - Record the lastPartial per Material
         *  - Cache firstEmpty result
         */

        for (int i = 0; i < items.length; i++) {
            ItemStack item = items[i];
            while (true) {
                // Do we already have a stack of it?
                int firstPartial = firstPartial(item);

                // Drat! no partial stack
                if (firstPartial == -1) {
                    // Find a free spot!
                    int firstFree = firstEmpty();

                    if (firstFree == -1) {
                        // No space at all!
                        leftover.put(i, item);
                        break;
                    } else {
                        // More than a single stack!
                        if (item.getAmount() > getMaxItemStack()) {
                            BukkitItemStack stack = new BukkitItemStack(item.getTypeId(), getMaxItemStack(), item.getDurability());
                            stack.addUnsafeEnchantments(item.getEnchantments());
                            setItem(firstFree, stack);
                            item.setAmount(item.getAmount() - getMaxItemStack());
                        } else {
                            // Just store it
                            setItem(firstFree, item);
                            break;
                        }
                    }
                } else {
                    // So, apparently it might only partially fit, well lets do just that
                    ItemStack partialItem = getItem(firstPartial);

                    int amount = item.getAmount();
                    int partialAmount = partialItem.getAmount();
                    int maxAmount = partialItem.getMaxStackSize();

                    // Check if it fully fits
                    if (amount + partialAmount <= maxAmount) {
                        partialItem.setAmount(amount + partialAmount);
                        break;
                    }

                    // It fits partially
                    partialItem.setAmount(maxAmount);
                    item.setAmount(amount + partialAmount - maxAmount);
                }
            }
        }
        return leftover;
    }

    public HashMap<Integer, ItemStack> removeItem(ItemStack... items) {
        HashMap<Integer, ItemStack> leftover = new HashMap<Integer, ItemStack>();

        // TODO: optimization

        for (int i = 0; i < items.length; i++) {
            ItemStack item = items[i];
            int toDelete = item.getAmount();

            while (true) {
                int first = first(item, false);

                // Drat! we don't have this type in the inventory
                if (first == -1) {
                    item.setAmount(toDelete);
                    leftover.put(i, item);
                    break;
                } else {
                    ItemStack itemStack = getItem(first);
                    int amount = itemStack.getAmount();

                    if (amount <= toDelete) {
                        toDelete -= amount;
                        // clear the slot, all used up
                        clear(first);
                    } else {
                        // split the stack and store
                        itemStack.setAmount(amount - toDelete);
                        setItem(first, itemStack);
                        toDelete = 0;
                    }
                }

                // Bail when done
                if (toDelete <= 0) {
                    break;
                }
            }
        }
        return leftover;
    }

    private int getMaxItemStack() {
        return getInventory().getInventoryStackLimit();
    }

    public void remove(int materialId) {
        ItemStack[] items = getContents();
        for (int i = 0; i < items.length; i++) {
            if (items[i] != null && items[i].getTypeId() == materialId) {
                clear(i);
            }
        }
    }

    public void remove(Material material) {
        remove(material.getId());
    }

    public void remove(ItemStack item) {
        ItemStack[] items = getContents();
        for (int i = 0; i < items.length; i++) {
            if (items[i] != null && items[i].equals(item)) {
                clear(i);
            }
        }
    }

    public void clear(int index) {
        setItem(index, null);
    }

    public void clear() {
        for (int i = 0; i < getSize(); i++) {
            clear(i);
        }
    }

    public ListIterator<ItemStack> iterator() {
        return new InventoryIterator(this);
    }

    public ListIterator<ItemStack> iterator(int index) {
        if (index < 0) {
            index += getSize() + 1; // ie, with -1, previous() will return the last element
        }
        return new InventoryIterator(this, index);
    }

    public List<HumanEntity> getViewers() {
    	throw new WrongUsageException("Some parts of the Bukkit API cannot presently be implemented in Forge. Sorry.");
        //return null;
    }

    public String getTitle() {
        return inventory.getInvName();
    }

    public InventoryType getType() {
        /*if (inventory instanceof InventoryCrafting) {
            return inventory.getSizeInventory() >= 9 ? InventoryType.WORKBENCH : InventoryType.CRAFTING;
        } else if (inventory instanceof PlayerInventory) {
            return InventoryType.PLAYER;
        } else if (inventory instanceof TileEntityDispenser) {
            return InventoryType.DISPENSER;
        } else if (inventory instanceof TileEntityFurnace) {
            return InventoryType.FURNACE;
        } else if (inventory instanceof ContainerEnchantTableInventory) {
            return InventoryType.ENCHANTING;
        } else if (inventory instanceof TileEntityBrewingStand) {
            return InventoryType.BREWING;
        } else if (inventory instanceof BukkitInventoryCustom.MinecraftInventory) {
            return ((BukkitInventoryCustom.MinecraftInventory) inventory).getType();
        } else if (inventory instanceof InventoryEnderChest) {
            return InventoryType.ENDER_CHEST;
        } else if (inventory instanceof InventoryMerchant) {
            return InventoryType.MERCHANT;
        } else {
            return InventoryType.CHEST;
        }*/
    	return InventoryType.CHEST;
    }

    public InventoryHolder getHolder() {
        return new BukkitInventoryHolder(this);
    }

    public int getMaxStackSize() {
        return inventory.getInventoryStackLimit();
    }

    public void setMaxStackSize(int size) {
    }


	@Override
	public boolean containsAtLeast(ItemStack item, int amount) {
		int totalFound = 0;
		for (int i = 0; i < getSize(); i++) {
			if (new BukkitItemStack(inventory.getStackInSlot(i)).isSimilar(item)) {
				totalFound++;
			}
		}
		return totalFound >= amount;
	}
}
