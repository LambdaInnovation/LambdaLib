package cn.liutils.util.client.renderhook;

import cn.liutils.util.helper.GameTimer;
import net.minecraft.entity.player.EntityPlayer;

/**
 * Register through DummyRenderData.get(player).addRenderHook(hook)
 * @author WeAthFolD
 */
public abstract class PlayerRenderHook {
	
	EntityPlayer player;
	boolean disposed;
	long createTime = GameTimer.getTime();
	
	public void renderHand(boolean firstPerson) {}
	
	public void dispose() {
		disposed = true;
	}
	
	public final EntityPlayer getPlayer() {
		return player;
	}
	
	protected long getDeltaTime() {
		return GameTimer.getTime() - createTime;
	}
	
}
