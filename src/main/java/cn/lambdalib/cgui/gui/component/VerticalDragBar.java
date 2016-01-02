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
package cn.lambdalib.cgui.gui.component;

import cn.lambdalib.cgui.gui.Widget;
import cn.lambdalib.cgui.gui.event.DragEvent;
import cn.lambdalib.cgui.gui.event.GuiEvent;

/**
 * Prefer using {@link DragBar} instead.
 * @author WeAthFolD
 */
@Deprecated
public class VerticalDragBar extends Component {
    
    public static class DraggedEvent implements GuiEvent {}
    
    public double y0, y1;

    public VerticalDragBar(double _y0, double _y1) {
        this();
        y0 = _y0;
        y1 = _y1;
    }

    public VerticalDragBar() {
        super("VerticalDragBar");
        
        listen(DragEvent.class, (w, event) -> {
            double originalX = w.transform.x;
            w.getGui().updateDragWidget();
            w.transform.x = originalX;
            w.getGui().updateWidget(w);
            
            if(w.transform.y > y1) {
                w.transform.y = y1;
            } else if(w.transform.y < y0) {
                w.transform.y = y0;
            }
            w.post(new DraggedEvent());
            w.dirty = true;
        });
    }
    
    public static VerticalDragBar get(Widget w) {
        return w.getComponent("VerticalDragBar");
    }
    
    public double getProgress() {
        return (widget.transform.y - y0) / (y1 - y0);
    }
    
    public void setProgress(double prg) {
        widget.transform.y = y0 + (y1 - y0) * prg;
        widget.dirty = true;
    }
    
    public VerticalDragBar setArea(double _y0,  double _y1) {
        y0 = _y0;
        y1 = _y1;
        
        return this;
    }
}
