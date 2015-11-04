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
package cn.lambdalib.multiblock;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import cn.lambdalib.annoreg.core.Registrant;
import cn.lambdalib.annoreg.mc.RegTileEntity;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * @author WeathFolD
 *
 */
@Registrant
@RegTileEntity
public class TileMulti extends TileEntity implements IMultiTile {
	
	InfoBlockMulti info = new InfoBlockMulti(this);
	
	@Override
	public void updateEntity() {
		if(info != null)
			info.update();
	}

	@Override
	public InfoBlockMulti getBlockInfo() {
		return info;
	}

	@Override
	public void setBlockInfo(InfoBlockMulti i) {
		info = i;
	}

	@Override
    public void readFromNBT(NBTTagCompound nbt) {
    	super.readFromNBT(nbt);
    	info = new InfoBlockMulti(this, nbt);
    }
    
	@Override
    public void writeToNBT(NBTTagCompound nbt) {
    	super.writeToNBT(nbt);
    	info.save(nbt);
    }
	
    @SideOnly(Side.CLIENT)
    @Override
    public AxisAlignedBB getRenderBoundingBox() {
    	Block block = getBlockType();
    	if(block instanceof BlockMulti) {
    		return ((BlockMulti) block).getRenderBB(xCoord, yCoord, zCoord, info.getDir());
    	} else {
    		return super.getRenderBoundingBox();
    	}
    }
	
}
