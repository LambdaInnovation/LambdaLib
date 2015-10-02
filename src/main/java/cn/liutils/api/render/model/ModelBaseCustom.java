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
package cn.liutils.api.render.model;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.model.ModelBase;
import net.minecraft.entity.Entity;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.model.IModelCustom;

/**
 * An simple model adaptor from IModelCustom to ModelBase
 * @author WeathFolD
 *
 */
public class ModelBaseCustom extends ModelBase {
	
	protected IModelCustom theModel;
	float scale;
	Vec3 offset = Vec3.createVectorHelper(0, 0, 0);

	public ModelBaseCustom(IModelCustom model) {
		theModel = model;
	}
	
	public ModelBaseCustom setScale(float f) {
		scale = f;
		return this;
	}
	
	public ModelBaseCustom setOffset(double x, double y, double z) {
		offset.xCoord = x;
		offset.yCoord = y;
		offset.zCoord = z;
		return this;
	}
	
    /**
     * Sets the models various rotation angles then renders the model.
     */
    @Override
	public final void render(Entity par1Entity, float par2, float par3, float par4, float par5, float par6, float par7) {
    	GL11.glPushMatrix(); {
    		GL11.glTranslated(offset.xCoord, offset.yCoord, offset.zCoord);
    		GL11.glScalef(scale, -scale, scale);
    		doRenderModel(par3, par5);
    	} GL11.glPopMatrix();
    }
    
    public void doRenderModel(float movingAmp, float headpar) {
    	theModel.renderAll();
    }

    /**
     * Sets the model's various rotation angles. For bipeds, par1 and par2 are used for animating the movement of arms
     * and legs, where par1 represents the time(so that arms and legs swing back and forth) and par2 represents how
     * "far" arms and legs can swing at most.
     */
    @Override
	public void setRotationAngles(float par1, float par2, float par3, float par4, float par5, float par6, Entity par7Entity) {
    	//Set the rotations here
    }

}
