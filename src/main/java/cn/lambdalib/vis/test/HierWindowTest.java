package cn.lambdalib.vis.test;

import java.lang.reflect.Field;

import cn.lambdalib.annoreg.core.Registrant;
import cn.lambdalib.cgui.gui.LIGuiScreen;
import cn.lambdalib.vis.curve.CubicCurve;
import cn.lambdalib.vis.editor.IVisPluginCommand;
import cn.lambdalib.vis.editor.animation.CurveView;
import cn.lambdalib.vis.editor.common.widget.WindowHierarchy;
import cn.lambdalib.vis.editor.common.widget.WindowHierarchy.Folder;
import cn.lambdalib.vis.editor.property.CompTransformProperty;
import cn.lambdalib.vis.editor.property.IntegerProperty;
import cn.lambdalib.vis.editor.property.Vec3Property;
import cn.lambdalib.vis.editor.registry.RegVisPluginCommand;
import cn.lambdalib.vis.model.CompTransform;
import cn.liutils.util.generic.VecUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.Vec3;

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
		
		public Vec3 position = VecUtils.vec(1, 2, 3);
		
		public CompTransform cp = new CompTransform();
		
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
				
				field = Gui.class.getField("position");
				field.setAccessible(true);
				Vec3Property propv = new Vec3Property("Position", field, this);
				A.addElement(propv);
				
				field = Gui.class.getField("cp");
				field.setAccessible(true);
				CompTransformProperty ctp = new CompTransformProperty("Composite", field, this);
				A.addElement(ctp);
			} catch(Exception e) {
				e.printStackTrace();
			}
			
			A_child.addElement(A_child_GAL);
			A.addElement(A_child);
			A.addElement(A_child2);
			
			window.addElement(A);
			window.addElement(B);
			
			gui.addWidget(window);
			
			CubicCurve curve = new CubicCurve();
			curve.addPoint(0, 10);
			curve.addPoint(10, 5);
			curve.addPoint(5, 0);
			
			CurveView view = new CurveView(curve);
			gui.addWidget(view);
		}
		
	}
	
}
