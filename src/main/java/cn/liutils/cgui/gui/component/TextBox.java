/**
 * Copyright (c) Lambda Innovation, 2013-2015
 * 本作品版权由Lambda Innovation所有。
 * http://www.li-dev.cn/
 *
 * This project is open-source, and it is distributed under  
 * the terms of GNU General Public License. You can modify
 * and distribute freely as long as you follow the license.
 * 本项目是一个开源项目，且遵循GNU通用公共授权协议。
 * 在遵照该协议的情况下，您可以自由传播和修改。
 * http://www.gnu.org/licenses/gpl.html
 */
package cn.liutils.cgui.gui.component;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import javax.vecmath.Vector2d;

import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.StatCollector;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import cn.liutils.cgui.gui.Widget;
import cn.liutils.cgui.gui.annotations.CopyIgnore;
import cn.liutils.cgui.gui.component.Transform.HeightAlign;
import cn.liutils.cgui.gui.component.Transform.WidthAlign;
import cn.liutils.cgui.gui.event.ChangeContentEvent;
import cn.liutils.cgui.gui.event.ConfirmInputEvent;
import cn.liutils.cgui.gui.event.FrameEvent;
import cn.liutils.cgui.gui.event.FrameEvent.FrameEventHandler;
import cn.liutils.cgui.gui.event.KeyEvent;
import cn.liutils.cgui.gui.event.KeyEvent.KeyEventHandler;
import cn.liutils.cgui.gui.event.MouseDownEvent;
import cn.liutils.cgui.gui.event.MouseDownEvent.MouseDownHandler;
import cn.liutils.util.helper.Color;
import cn.liutils.util.helper.Font;
import cn.liutils.util.helper.Font.Align;
import cn.liutils.util.helper.GameTimer;

/**
 * 事实证明UI底层是十分蛋疼的……
 * @author WeAthFolD
 */
public class TextBox extends Component {
	
	public String content = "";
	
	/**
	 * Only activated when doesn't allow edit. If activated, The display string will be StatCollector.translateToLocal(content).
	 */
	public boolean localized = false;
	
	public boolean allowEdit = true;
	
	public boolean doesEcho = false;
	public char echoChar = '*';
	
	public Color color = new Color(0xffffffff);
	
	/**
	 * Whether this textBox doesn't draw chars that are out of bounds.
	 */
	public boolean emit = false;
	
	public double size = 5;
	
	public double zLevel = 0;
	
	public WidthAlign widthAlign = WidthAlign.LEFT;
	
	public HeightAlign heightAlign = HeightAlign.BOTTOM;
	
	@CopyIgnore
	public int caretPos = 0;
	
	public TextBox setSize(double s) {
		size = s;
		return this;
	}
	
	public TextBox disallowEdit() {
		allowEdit = false;
		return this;
	}
	
	public TextBox setContent(String str) {
		content = str;
		return this;
	}
	
	private String getProcessedContent() {
		String str = content;
		if(!allowEdit && localized) {
			str = StatCollector.translateToLocal(str);
		}
		
		if(doesEcho) {
			StringBuilder sb = new StringBuilder();
			for(int i = 0; i < str.length(); ++i) {
				sb.append('*');
			}
			str = sb.toString();
		}
		
		return str;
	}
	
	private String getClipboardContent() {
		Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
		if(cb.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
			try {
				return (String) cb.getData(DataFlavor.stringFlavor);
			} catch (UnsupportedFlavorException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return "";
	}
	
	private void saveClipboardContent() {
		Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
		StringSelection ss = new StringSelection(content);
		cb.setContents(ss, ss);
	}
	
	private double[] getOffset(Widget w) {
		double x = 0, y = 0;
		Vector2d v = new Vector2d(Font.font.strLen(getProcessedContent(), size), size);
		
		switch(widthAlign) {
		case LEFT:
			x = 2;
			break;
		case CENTER:
			x = (w.transform.width - v.x) / 2;
			break;
		case RIGHT:
			x = w.transform.width - v.x;
			break;
		default:
			break;
		}
		
		switch(heightAlign) {
		case TOP:
			y = 0;
			break;
		case CENTER:
			y = (w.transform.height - v.y) / 2;
			break;
		case BOTTOM:
			y = (w.transform.height - v.y);
			break;
		default:
			break;
		}
		
		return new double[] { x, y };
	}
	
	public TextBox() {
		super("TextBox");
		addEventHandler(new KeyEventHandler() {
			
			@Override
			public void handleEvent(Widget w, KeyEvent event) {
				if(!allowEdit)
					return;
				checkCaret();
				
				int par2 = event.keyCode;
				
				if(par2 == Keyboard.KEY_RIGHT) {
					caretPos++;
				} else if(par2 == Keyboard.KEY_LEFT) {
					caretPos--;
				}
				
				if(caretPos < 0) caretPos = 0;
				if(caretPos > content.length()) caretPos = content.length();
				
				if(event.keyCode == Keyboard.KEY_V && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
					String str1 = content.substring(0, caretPos), str2 = getClipboardContent(), str3 = content.substring(caretPos);
					content = str1 + str2 + str3;
					w.postEvent(new ChangeContentEvent());
					return;
				}
				
				if(event.keyCode == Keyboard.KEY_C && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
					saveClipboardContent();
					return;
				}
				
				if (par2 == Keyboard.KEY_BACK && content.length() > 0) {
					if(caretPos > 0) {
						content = content.substring(0, caretPos - 1) + 
							(caretPos == content.length() ? "" : content.substring(caretPos, content.length()));
						--caretPos;
					}
					w.postEvent(new ChangeContentEvent());
				} else if(par2 == Keyboard.KEY_RETURN || par2 == Keyboard.KEY_NUMPADENTER) {
					w.postEvent(new ConfirmInputEvent());
				} else if(par2 == Keyboard.KEY_DELETE) {
					content = "";
					w.postEvent(new ChangeContentEvent());
				}
				if (ChatAllowedCharacters.isAllowedCharacter(event.inputChar)) {
					content = content.substring(0, caretPos) + event.inputChar +
							(caretPos == content.length() ? "" : content.substring(caretPos, content.length()));
					caretPos += 1;
					w.postEvent(new ChangeContentEvent());
				}
				
				checkCaret();
			}
			
		});
		
		addEventHandler(new MouseDownHandler() {
			@Override
			public void handleEvent(Widget w, MouseDownEvent event) {
				double len = 3;
				double[] offset = getOffset(w);
				double eventX = -offset[0] + event.x;
				
				for(int i = 0; i < content.length(); ++i) {
					double cw = Font.font.strLen(String.valueOf(content.charAt(i)), size);
					len += cw;
					
					if(len > eventX) {
						caretPos = (eventX - len + cw > cw / 2) ? i + 1 : i;
						return;
					}
				}
				caretPos = content.length();
			}
		});
		
		addEventHandler(new FrameEventHandler() {

			@Override
			public void handleEvent(Widget w, FrameEvent event) {
				
				double[] offset = getOffset(w);
				
				checkCaret();
				
				String str = getProcessedContent();
				
				GL11.glPushMatrix();
				GL11.glTranslated(0, 0, zLevel);
				
				if(emit)
					Font.font.drawTrimmed(str, offset[0], offset[1], size, color.asHexColor(), Align.LEFT, w.transform.width - 2, "...");
				else
					Font.font.draw(str, offset[0], offset[1], size, color.asHexColor(), Align.LEFT);
				
				GL11.glPopMatrix();
				
				if(allowEdit && w.isFocused() && GameTimer.getAbsTime() % 1000 < 500) {
					double len = Font.font.strLen(str.substring(0, caretPos), size);
					Font.font.draw("|", len + offset[0], offset[1], size, color.asHexColor());
				}
			}
			
		});
	}
	
	private void checkCaret() {
		if(caretPos > content.length())
			caretPos = content.length() - 1;
		if(caretPos < 0) caretPos = 0;
	}
	
	public static TextBox get(Widget w) {
		return w.getComponent("TextBox");
	}

}
