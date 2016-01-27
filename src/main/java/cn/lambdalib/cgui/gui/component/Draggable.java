/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.cgui.gui.component;

import cn.lambdalib.cgui.gui.event.DragEvent;

/**
 * This component simply updates the widget's position when it was dragged.
 * @author WeAthFolD
 */
public class Draggable extends Component {

    public Draggable() {
        super("Draggable");
        
        listen(DragEvent.class, (w, e) -> {
            w.getGui().updateDragWidget();
            w.dirty = true;
        });
    }

}
