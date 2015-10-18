package cn.liutils.vis.test;

import java.lang.reflect.Field;

import cn.annoreg.core.Registrant;
import cn.liutils.cgui.gui.LIGuiScreen;
import cn.liutils.vis.editor.IVisPluginCommand;
import cn.liutils.vis.editor.common.widget.WindowHierarchy;
import cn.liutils.vis.editor.common.widget.WindowHierarchy.Folder;
import cn.liutils.vis.editor.property.IntegerProperty;
import cn.liutils.vis.editor.registry.RegVisPluginCommand;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.command.ICommandSender;

@SideOnly(Side.CLIENT)
@Registrant
@RegVisPluginCommand("hier")
public class HierWindowTest implements IVisPluginCommand {

	@Override
	public void onCommand(ICommandSender ics, String[] args) {
		Minecraft.getMinecraft().displayGuiScreen(new Gui());
	}
	
	static class Gui extends LIGuiScreen {
		
		WindowHierarchy window;
		
		public int theval;
		
		public Gui() {
			window = new WindowHierarchy();
			Folder A = new Folder("A");
			Folder B = new Folder("B");
			
			Folder A_child = new Folder("A_child");
			Folder A_child2 = new Folder("A_child2");
			
			Folder A_child_GAL = new Folder("GAL");
			
			try {
				Field field = Gui.class.getField("theval");
				field.setAccessible(true);
				IntegerProperty prop = new IntegerProperty("TheVal", field, this);
				A.addElement(prop);
			} catch(Exception e) {
				e.printStackTrace();
			}
			
			A_child.addElement(A_child_GAL);
			A.addElement(A_child);
			A.addElement(A_child2);
			
			window.addElement(A);
			window.addElement(B);
			
			gui.addWidget(window);
		}
		
	}
	
}
