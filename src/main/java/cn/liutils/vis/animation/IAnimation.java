package cn.liutils.vis.animation;

/**
 * Base interface for time-based animation. Provide a time point, and the animation modifies the value based on the parameter.
 * @author WeAthFolD
 */
public interface IAnimation {
	
	void perform(long timePoint);
	
}
