package cn.lambdalib.util.entityx.handlers;

import cn.lambdalib.util.entityx.MotionHandler;

public class Life extends MotionHandler {
	
	public int ticks;

	public Life(int ticks) {
		this.ticks = ticks;
	}

	@Override
	public String getID() {
		return "Life";
	}

	@Override
	public void onStart() {}

	@Override
	public void onUpdate() {
		if(getTarget().ticksExisted == ticks) {
			getTarget().setDead();
		}
	}

}
