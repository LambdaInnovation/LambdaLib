package cn.annoreg.mc;

import cn.annoreg.mc.impl.proxy.ClientProxy;
import cn.annoreg.mc.impl.proxy.ServerProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.world.World;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class SideHelper {
	//TODO load client on server?
    private static ThreadLocal<SideHelper> threadProxy = new ThreadLocal<SideHelper>() {
        @Override protected SideHelper initialValue() {
            Side s = FMLCommonHandler.instance().getEffectiveSide();
            if (s.isClient()) {
                return new SideHelper(getClientProxy());
            } else {
                return new SideHelper(new ServerProxy());
            }
        }
        
        // FIXME: TEMP WORKAROUND
        @SideOnly(Side.CLIENT)
        private ServerProxy getClientProxy() {
        	return new ClientProxy();
        }
    };
    
    public final ServerProxy proxy;
    
    private SideHelper(ServerProxy proxy) {
        this.proxy = proxy;
    }
    
    public static World getWorld(int dimension) {
        return threadProxy.get().proxy.getWorld(dimension);
    }
    
    public static Container getPlayerContainer(EntityPlayer player, int windowId) {
        return threadProxy.get().proxy.getPlayerContainer(player, windowId);
    }
    
    public static EntityPlayer getThePlayer() {
        return threadProxy.get().proxy.getThePlayer();
    }

    public static EntityPlayer getPlayerOnServer(String name) {
        return threadProxy.get().proxy.getPlayerOnServer(name);
    }
    
    public static Object[] getPlayerList() {
        return threadProxy.get().proxy.getPlayerList();
    }
    
    public static boolean isClient() {
        return FMLCommonHandler.instance().getEffectiveSide().isClient();
    }
}
