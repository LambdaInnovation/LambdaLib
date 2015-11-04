/**
 * Copyright (c) Lambda Innovation, 2013-2015
 * 本作品版权由Lambda Innovation所有。
 * http://www.li-dev.cn/
 *
 * This project is open-source, and it is distributed under  
 * the terms of GNU General Public License. You can modify
 * and distribute freely as long as you follow the license.
 * 本项目是一个开源项目，且遵循GNU通用公共授权协议。
 * 在遵照该协议的情况下，您可以自由传播和修改。
 * http://www.gnu.org/licenses/gpl.html
 */
package cn.lambdalib.util.mc;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentTranslation;

/**
 * Utils that are built around a player.
 * @author WeAthFolD
 */
public class PlayerUtils {
	
	/**
	 * Try to merge an itemStack into the player inventory. The merging uses strict
	 * equality, that is, only when: <br>
	 *  * item instances are equal <br>
	 *  * NBT datas are equal <br>
	 *  * Damage values are equal <br>
	 * can this stack merge to an another stack in the inventory.
	 * @return The stack size that is not merged into the inventory.
	 */
	public static int mergeStackable(InventoryPlayer inv, ItemStack stack) {
		for(int i = 0; i < inv.getSizeInventory() - 4 && stack.stackSize > 0; ++i) {
			ItemStack is = inv.getStackInSlot(i);
			if(is != null && StackUtils.isStackDataEqual(stack, is) && is.getItemDamage() == stack.getItemDamage()) {
				is.stackSize += stack.stackSize;
				int left = Math.max(0, is.stackSize - is.getMaxStackSize());
				stack.stackSize = left;
				is.stackSize -= left;
			}
		}
		if(stack.stackSize > 0) {
			int id = inv.getFirstEmptyStack();
			if(id == -1) {
				return stack.stackSize;
			}
			inv.setInventorySlotContents(id, stack.copy());
			return 0;
		}
		return 0;
	}
	
	/**
	 * Try to find the index of a item in player's inventory. if fail, return -1.
	 */
	public static int getSlotByStack(ItemStack item, EntityPlayer player) {
		InventoryPlayer inv = player.inventory;
		for(int i = 0; i < inv.mainInventory.length; i++) {
			ItemStack is = inv.mainInventory[i];
			if(is != null && item == is)
				return i;
		}
		return -1;
	}
	
	/**
	 * Abbr for annoying addChatMessage(new ChatComponentTranslation(...)).
	 * @param ics Message sending target
	 * @param message Message
	 * @param pars Message parameters
	 */
	public static void sendChat(ICommandSender ics, String message, String... pars) {
		ics.addChatMessage(new ChatComponentTranslation(message, (Object[]) pars));
	}

	
}
