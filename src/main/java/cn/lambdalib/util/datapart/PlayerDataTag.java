/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
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
public class PlayerDataTag extends DataPart<EntityPlayer> {
    
    public static PlayerDataTag get(EntityPlayer player) {
        return EntityData.get(player).getPart(PlayerDataTag.class);
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
