package cn.lambdalib.networkcall;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import cn.lambdalib.annoreg.mc.SideHelper;
import cn.lambdalib.core.LLModContainer;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;

public final class NetworkTerminal {
    
    private final String playerName;
    
    private NetworkTerminal(String playerName) {
        this.playerName = playerName;
    }
    
    public static NetworkTerminal create() {
        if (SideHelper.isClient()) {
            return new NetworkTerminal(SideHelper.getThePlayer().getCommandSenderName());
        }
        return new NetworkTerminal(null);
    }
    
    public void send(SimpleNetworkWrapper wrapper, IMessage msg) {
    	
        if (playerName == null) {
            //send to server
            if (!SideHelper.isClient()) {
            	LLModContainer.log.warn("Can not send to server from server");
            }
            wrapper.sendToServer(msg);
        } else {
            if (SideHelper.isClient()) {
                LLModContainer.log.warn("Can not send to client from client");
            }
            wrapper.sendTo(msg, (EntityPlayerMP) SideHelper.getPlayerOnServer(playerName));
        }
    }
    
    public NBTBase toNBT() {
        NBTTagCompound ret = new NBTTagCompound();
        if (playerName != null) {
            ret.setString("player", playerName);
        }
        return ret;
    }
    
    public static NetworkTerminal fromNBT(NBTBase nbt) {
        NBTTagCompound comp = (NBTTagCompound) nbt;
        if (comp.hasKey("player")) {
            return new NetworkTerminal(comp.getString("player"));
        }
        return new NetworkTerminal(null);
    }
    
}
