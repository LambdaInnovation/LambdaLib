/**
 * 
 */
package cn.liutils.cgui.gui;

import java.io.File;

import net.minecraft.client.gui.GuiScreen;

import org.lwjgl.opengl.GL11;

/**
 * @author WeAthFolD
 *
 */
public class LIGuiScreen extends GuiScreen {
	protected LIGui gui;
	protected boolean drawBack = true;
	
	public LIGuiScreen(LIGui _gui) {
		gui = _gui;
	}
	
	public LIGuiScreen() {
		this(new LIGui());
	}
	
	public LIGuiScreen setDrawBack(boolean b) {
		drawBack = b;
		return this;
	}
	
	@Override
    public void drawScreen(int mx, int my, float w) {
    	gui.resize(width, height);
    	if(drawBack)
    		this.drawDefaultBackground();
    	GL11.glPushMatrix(); {
    		gui.draw(mx, my);
    	} GL11.glPopMatrix();
    }
    
    @Override
    protected void mouseClicked(int mx, int my, int btn) {
    	gui.mouseClicked(mx, my, btn);
    }
    
    @Override
    protected void mouseClickMove(int mx, int my, int btn, long time) {
    	gui.mouseClickMove(mx, my, btn, time);
    }
    
    @Override
    public void onGuiClosed() {
    	gui.dispose();
    }
    
    @Override
    protected void keyTyped(char par1, int par2) {
    	super.keyTyped(par1, par2);
    	gui.keyTyped(par1, par2);
    }
	
    public LIGui getGui() {
    	return gui;
    }
}
