package cn.liutils.check;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import cn.lambdalib.annoreg.mc.SideHelper;
import cn.lambdalib.networkcall.RegNetworkCall;
import cn.lambdalib.networkcall.s11n.StorageOption.Data;
import cn.lambdalib.networkcall.s11n.StorageOption.Instance;
import cn.lambdalib.networkcall.s11n.StorageOption.Target;
import cn.liutils.util.generic.HashUtils;
import cn.liutils.util.generic.RegistryUtils;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ServerTickEvent;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;

/**
 * Provides resource verification when a client connected to server
 * @author EAirPeter
 */
public final class ResourceCheck {
	
	private static final ByteBuf buf = Unpooled.buffer();
	private static final HandlerServer handler = new HandlerServer();
	
	public static void init() {
		FMLCommonHandler.instance().bus().register(handler);
	}
	
	/**
	 * Add a resource which will be verified when client connected to server.
	 * @param res The resource to be verified
	 */
	public static void add(ResourceLocation res) {
		buf.writeInt(res.hashCode());
		buf.writeBytes(HashUtils.SHA1.hash(RegistryUtils.getResourceStream(res)));
	}
	
	@RegNetworkCall(side = Side.CLIENT)
	public static void sRequestCheck(@Target EntityPlayer player) {
		cChecklist(player, buf.array());
	}
	
	@RegNetworkCall(side = Side.SERVER)
	public static void cChecklist(@Instance EntityPlayer player, @Data byte[] data) {
		handler.processPlayer((EntityPlayerMP) player, Arrays.equals(buf.array(), data));
	}

}
