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
package cn.liutils.cgui.loader;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import cn.liutils.cgui.gui.Widget;
import cn.liutils.cgui.gui.WidgetContainer;
import cn.liutils.cgui.gui.annotations.GuiCallback;
import cn.liutils.cgui.gui.event.GuiEvent;
import cn.liutils.cgui.gui.event.IGuiEventHandler;
import cn.liutils.core.LIUtils;

/**
 * @author WeAthFolD
 */
public class EventLoader {

	public static void load(WidgetContainer widget, Object callbackProvider) {
		for(Method m : callbackProvider.getClass().getMethods()) {
			if(m.isAnnotationPresent(GuiCallback.class)) {
				//Check signature
				Class<?>[] pars = m.getParameterTypes();
				if(pars.length != 2) {
					throw new IllegalArgumentException("Invalid par size for callback method " + m.getName());
				}
				if(pars[0] != Widget.class) {
					throw new IllegalArgumentException("par1 not a widget for " + m.getName());
				}
				if(!GuiEvent.class.isAssignableFrom(pars[1])) {
					throw new IllegalArgumentException("par2 not a GuiEvent for " + m.getName());
				}
				
				String path = m.getAnnotation(GuiCallback.class).value();
				Widget target = (Widget) (path.equals("") ? (widget instanceof Widget ? widget : null) : widget.getWidget(path));
				if(target == null) {
					LIUtils.log.error("Didn't find widget named " + path + ".");
				} else {
					MethodWrapper wrapper =new MethodWrapper(m, callbackProvider);
					Class c = pars[1];
					target.<GuiEvent>listen((Class<? extends GuiEvent>) c, new MethodWrapper(m, callbackProvider));
				}
			}
		}
	}
	
	private static class MethodWrapper implements IGuiEventHandler {
		
		final Method method;
		final Object instance;

		public MethodWrapper(Method m, Object i) {
			method = m;
			instance = i;
		}

		@Override
		public void handleEvent(Widget w, GuiEvent event) {
			try {
				method.invoke(instance, w, event);
			} catch (Exception e) {
				LIUtils.log.error("Exception occured trying to do event callback");
				e.printStackTrace();
				
				if(e instanceof InvocationTargetException) {
					LIUtils.log.error("Target stack trace:");
					((InvocationTargetException)e).getTargetException().printStackTrace();
				}
			}
		}
		
	}
	
}
