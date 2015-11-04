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
package cn.lambdalib.util.datapart;

import cn.lambdalib.annoreg.core.Registrant;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

/**
 * A data tag for storage-only, lightweight stuffs
 * @author WeAthFolD
 */
@Registrant
@RegDataPart("DataTag")
public class PlayerDataTag extends DataPart {
	
	public static PlayerDataTag get(EntityPlayer player) {
		return PlayerData.get(player).getPart(PlayerDataTag.class);
	}
	
	private NBTTagCompound theTag;

	@Override
	public void fromNBT(NBTTagCompound tag) {
		theTag = tag;
	}

	@Override
	public NBTTagCompound toNBT() {
		return theTag;
	}
	
	public NBTTagCompound getTag() {
		if(theTag == null)
			theTag = new NBTTagCompound();
		return theTag;
	}

}
