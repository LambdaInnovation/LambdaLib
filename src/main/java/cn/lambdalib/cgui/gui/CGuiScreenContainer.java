/**
 * Copyright (c) Lambda Innovation, 2013-2016
 * This file is part of LambdaLib modding library.
 * https://github.com/LambdaInnovation/LambdaLib
 * Licensed under MIT, see project root for more information.
 */
package cn.lambdalib.cgui.gui;

import cn.lambdalib.cgui.gui.component.TextBox;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.io.IOException;

/**
 * A simple wrapper for fast {@link CGui} deploy as GuiContainer.
 * @author WeAthFolD
 */
public class CGuiScreenContainer extends GuiContainer {

    protected CGui gui;

    public CGuiScreenContainer(Container c) {
        super(c);
        gui = new CGui();
    }

    public CGuiScreenContainer(Container c, CGui _gui) {
        super(c);
        gui = _gui;
    }

    public CGui getGui() {
        return gui;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float var1, int var2,
                                                   int var3) {
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        gui.resize(width, height);
        gui.draw(var2, var3);
    }

    @Override
    public void drawScreen(int a, int b, float c) {
        if(isSlotActive()) {
            super.drawScreen(a, b, c);
        } else {
            gui.resize(width, height);
            this.drawDefaultBackground();
            gui.draw(a, b);
        }
    }

    @Override
    protected void mouseClicked(int par1, int par2, int par3) throws IOException
    {
        if(isSlotActive()) super.mouseClicked(par1, par2, par3);
        gui.mouseClicked(par1, par2, par3);
    }

    @Override
    protected void mouseClickMove(int mx, int my, int btn, long time) {
        if(isSlotActive()) super.mouseClickMove(mx, my, btn, time);
        gui.mouseClickMove(mx, my, btn, time);
    }

    @Override
    public void onGuiClosed() {
        gui.dispose();
    }

    @Override
    protected void mouseReleased(int a, int b, int c) {
        if(isSlotActive()) {
            super.mouseReleased(a, b, c);
        }
    }

    @Override
    public void keyTyped(char ch, int key) throws IOException
    {
        gui.keyTyped(ch, key);
        if(containerAcceptsKey(key) || key == Keyboard.KEY_ESCAPE)
            super.keyTyped(ch, key);
    }

    /**
     * @return Whether the inventory itself receives key input. (This should be disabled when you are handling some user input)
     */
    protected boolean containerAcceptsKey(int key) {
        // Don't delegate key event if current editing TextBox. Surely dirty hack, find a better route later
        TextBox temp;
        return (gui.focus == null ||
                (temp = TextBox.get(gui.focus)) == null ||
                !temp.canEdit);
    }

    /**
     * @return Whether inventory slots should be renderered this frame.
     */
    public boolean isSlotActive() {
        return true;
    }
}
