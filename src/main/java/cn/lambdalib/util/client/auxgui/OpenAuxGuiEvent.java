/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.util.client.auxgui;


import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Fired whenever an new AuxGui is opened.
 * @author WeAthFolD
 */
public class OpenAuxGuiEvent extends Event
{
    
    public final AuxGui gui;

    public OpenAuxGuiEvent(AuxGui _gui) {
        gui = _gui;
    }

}
