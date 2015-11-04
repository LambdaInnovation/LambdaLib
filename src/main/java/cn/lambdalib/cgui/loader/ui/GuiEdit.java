package cn.lambdalib.cgui.loader.ui;

import java.io.File;
import java.io.FileOutputStream;

import javax.vecmath.Vector2d;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Configuration;

import org.lwjgl.opengl.GL11;

import cn.lambdalib.annoreg.core.Registrant;
import cn.lambdalib.cgui.client.CGUILang;
import cn.lambdalib.cgui.gui.LIGui;
import cn.lambdalib.cgui.gui.LIGuiScreen;
import cn.lambdalib.cgui.gui.Widget;
import cn.lambdalib.cgui.loader.xml.CGUIDocWriter;

/**
 * @author WeAthFolD
 */
@Registrant
public class GuiEdit extends LIGuiScreen {
	
	static final String CONFIG_PATH = "config/cgui_layout.conf";
	
	public Configuration cfg;
	
	LIGui toEdit = new LIGuiPlayground(this); //The edit playground gui!
	
	private Widget selectedEditor;
	
	public boolean toggleBlack = true;
	
	public static double
		COLOR[] = { 0.2, 0.4, 0.65, .9 };
	
	String path;
	
	public GuiEdit(String _path, LIGui gui) {
		this();
		toEdit.addAll(gui);
		path = _path;
		this.drawBack = false;
	}
	
	public static double[] COLOR_STYLE = new double[] { .3, .56, 1 };
	
	public static void bindColor(int n) {
		double c = COLOR[n];
		GL11.glColor4d(c * COLOR_STYLE[0], c * COLOR_STYLE[1], c * COLOR_STYLE[2], 0.7);
	}
	
	public void disposeSelectedEditor() {
		if(selectedEditor != null) {
			selectedEditor.dispose();
		}
	}
	
	public void changeSelectedEditor(Widget w) {
		disposeSelectedEditor();
		selectedEditor = w;
		getGui().addWidget(selectedEditor);
	}
	
	public GuiEdit() {
		this.drawBack = false;
		
		File f = new File(CONFIG_PATH);
		if(!f.isFile()) {
			if(f.isDirectory()) {
				f.delete();
			}
			try {
				FileOutputStream fos = new FileOutputStream(f);
				fos.close();
			} catch(Exception e) {}
		}
		cfg = new Configuration(f);
		
		gui.addWidget("toolbar", new Toolbar(this));
		gui.addWidget("hierarchy", new Hierarchy(this));
	}
	
	public Vector2d getDefaultPosition(String name, double[] def) {
		double[] r = cfg.get("layout", name, def).getDoubleList();
		return new Vector2d(r);
	}
	
	public void updateDefaultPosition(String name, double x, double y) {
		cfg.get("layout", name, new double[] { x, y }).set(new double[] { x, y });
	}
	
	@Override
    public void drawScreen(int mx, int my, float w) {
		if(toggleBlack)
			LIGui.drawBlackout();
		
		toEdit.resize(width, height);
		toEdit.draw(mx, my);
		super.drawScreen(mx, my, w);
	}
	
    @Override
    protected void mouseClicked(int mx, int my, int btn) {
    	if(!gui.mouseClicked(mx, my, btn)) {
    		//Fallthrough only if edit gui wasn't interrupting.
    		//toEdit.mouseClicked(mx, my, btn);
    	}
    }
    
    @Override
    protected void mouseClickMove(int mx, int my, int btn, long time) {
    	if(!gui.mouseClickMove(mx, my, btn, time)) {
    		toEdit.mouseClickMove(mx, my, btn, time);
    	}
    }
    
    @Override
    public void onGuiClosed() {
    	super.onGuiClosed();
    	
    	cfg.save();
    	saveResult("autosave");
    }
    
    public void saveResult() {
    	if(path == null)
    		throw new IllegalStateException("Null path!");
    	File file;
    	file = new File(path);
    	if(file.isFile()) file.delete();
    	
    	boolean res = CGUIDocWriter.save(toEdit, file);
    	Minecraft.getMinecraft().thePlayer.sendChatMessage(res ? 
    		CGUILang.commSaved() + file.getName() :
    		CGUILang.commSaveFailed() + file.getName());
    }
    
    private void saveResult(String name) {
    	File file;
    	file = new File("cgui/");
    	if(file.isFile()) file.delete();
    	if(!file.isDirectory()) file.mkdirs();
    	
    	int i = 0;
    	do {
    		file = new File("cgui/" + name + (i++) + ".xml");
    	} while(file.canRead() || file.isDirectory());
    	
    	if(CGUIDocWriter.save(toEdit, file))
    		Minecraft.getMinecraft().thePlayer.sendChatMessage(CGUILang.commSaved() + file.getName());
    	else
    		Minecraft.getMinecraft().thePlayer.sendChatMessage(CGUILang.commSaveFailed() + file.getName());
    }
    
    public boolean doesGuiPauseGame() {
        return false;
    }
	
	public static final ResourceLocation tex(String name) {
		return new ResourceLocation("liutils:textures/cgui/" + name + ".png");
	}
}
