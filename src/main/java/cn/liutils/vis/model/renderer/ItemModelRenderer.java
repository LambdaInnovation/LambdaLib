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
package cn.liutils.vis.model.renderer;


import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;

import org.lwjgl.opengl.GL11;

import cn.liutils.util.client.RenderUtils;
import cn.liutils.vis.editor.util.EditorHelper.VisEditable;
import cn.liutils.vis.model.CompTransform;
import cn.liutils.vis.model.PartedModel;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IItemRenderer;

/**
 * Note: The default implementation doesn't handle inventory icon. It is rendered with 
 *  item's default icon. Do it by yourself if you intend to.
 * @author WeAthFolD
 */
public class ItemModelRenderer implements IItemRenderer {

	@VisEditable("Standard")
	public CompTransform stdTransform = new CompTransform();
	@VisEditable("FirstPerson")
	public CompTransform fpTransform = new CompTransform();
	@VisEditable("ThirdPerson")
	public CompTransform tpTransform = new CompTransform();
	@VisEditable("EntityItem")
	public CompTransform entityItemTransform = new CompTransform();
	
	public PartedModel model;
	public ResourceLocation texture;
	
	public ItemModelRenderer() {
		this(null, null);
	}
	
	public ItemModelRenderer(PartedModel _model) {
		this(_model, null);
	}
	
	public ItemModelRenderer(PartedModel _model, ResourceLocation _texture) {
		model = _model;
		texture = _texture;
	}
	
	@Override
	public boolean handleRenderType(ItemStack item, ItemRenderType type) {
		switch(type) {
		case ENTITY:
		case EQUIPPED:
		case EQUIPPED_FIRST_PERSON:
			return true;
		default:
			return false;
		}
	}

	@Override
	public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack stack, ItemRendererHelper helper) {
		return false;
	}

	@Override
	public void renderItem(ItemRenderType type, ItemStack stack, Object... data) {
		switch(type) {
		case ENTITY:
			renderEntityItem(stack, (EntityItem) data[1]);
			break;
		case EQUIPPED_FIRST_PERSON:
			renderFirstPerson(stack, (EntityLivingBase) data[1]);
			break;
		case EQUIPPED:
			renderThirdPerson(stack, (EntityLivingBase) data[1]);
			break;
		default:
			//NOT HANDLED
		}
	}
	
	protected void renderFirstPerson(ItemStack stack, EntityLivingBase holder) {
		glPushMatrix();
		
		fpTransform.doTransform();
		doFixedTransform();
		renderStandard();
		
		glPopMatrix();
	}
	
	protected void renderThirdPerson(ItemStack stack, EntityLivingBase holder) {
		glPushMatrix();
		
		tpTransform.doTransform();
		doFixedTransform();
		renderStandard();
		
		glPopMatrix();
	}

	protected void renderEntityItem(ItemStack stack, EntityItem entity) {
		glPushMatrix();
		
		entityItemTransform.doTransform();
		renderStandard();
		
		glPopMatrix();
	}
	
	protected void renderStandard() {
		if(texture != null)
			RenderUtils.loadTexture(texture);
		
		stdTransform.doTransform();
		model.draw();
	}
	
	private void doFixedTransform() {
		GL11.glRotated(35, 0, 0, 1);
		GL11.glTranslated(0.8, -.12, 0);
	}
	
}
