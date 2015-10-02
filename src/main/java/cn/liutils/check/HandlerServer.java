package cn.liutils.check;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.RowFilter.Entry;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.common.gameevent.TickEvent.ServerTickEvent;

/**
 * @author EAirPeter
 */
public final class HandlerServer {
	
	private static final int TIMEOUT = 60;
	
	private final Map<String, Integer> map = new HashMap<String, Integer>();
	
	void processPlayer(EntityPlayerMP player, boolean passed) {
		if (passed)
			synchronized (map) {
				map.remove(player.getCommandSenderName());
			}
		else
			player.playerNetServerHandler.kickPlayerFromServer("Verification failed");
	}
	
	@SubscribeEvent
	public void onPlayerLoggedIn(PlayerLoggedInEvent event) {
		String name = event.player.getCommandSenderName();
		if (map.containsKey(name))
			throw new IllegalStateException("Duplicated login");
		synchronized (map) {
			map.put(name, TIMEOUT);
		}
		ResourceCheck.sRequestCheck(event.player);
	}
	
	@SubscribeEvent
	public void onPlayerLoggedOut(PlayerLoggedOutEvent event) {
		String name = event.player.getCommandSenderName();
		if (map.containsKey(name))
			synchronized (map) {
				map.remove(name);
			}
	}
	
	@SubscribeEvent
	public void onServerTickEvent(ServerTickEvent event) {
		if (event.phase == Phase.END)
			return;
		synchronized (map) {
			for (Iterator<String> i = map.keySet().iterator(); i.hasNext(); ) {
				String name = i.next();
				int timer = map.get(name);
				if (--timer == 0) {
					MinecraftServer.getServer().getConfigurationManager().func_152612_a(name).playerNetServerHandler.kickPlayerFromServer("Verification timeout");
					i.remove();
				}
				else
					map.put(name, timer);
			}
		}
		
	}
	
	private static HandlerServer INSTANCE = null;
	
	HandlerServer() {
		if (INSTANCE == null)
			INSTANCE = this;
		else
			throw new IllegalStateException("Only one HandlerServer is allowed");
	}
	
}
