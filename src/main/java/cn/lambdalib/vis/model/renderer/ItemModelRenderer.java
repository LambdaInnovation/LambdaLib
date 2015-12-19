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
package cn.lambdalib.vis.model.renderer;


import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;

import java.io.IOException;

import cn.lambdalib.vis.editor.VisProperty;
import org.lwjgl.opengl.GL11;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import cn.lambdalib.util.client.RenderUtils;
import cn.lambdalib.vis.gson.GsonAdapters;
import cn.lambdalib.vis.model.CompTransform;
import cn.lambdalib.vis.model.PartedModel;
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

	@VisProperty(name = "Standard")
	public CompTransform stdTransform = new CompTransform();
	@VisProperty(name = "FirstPerson")
	public CompTransform fpTransform = new CompTransform();
	@VisProperty(name = "ThirdPerson")
	public CompTransform tpTransform = new CompTransform();
	@VisProperty(name = "EntityItem")
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
	
	protected void readFromJson(String name, JsonReader in) throws IOException {
		TypeAdapter<CompTransform> cta = GsonAdapters.compTransformAdapter;
		switch(name) {
		case "t_std":
			stdTransform = cta.read(in);
			break;
		case "t_fp":
			fpTransform = cta.read(in);
			break;
		case "t_tp":
			tpTransform = cta.read(in);
			break;
		case "texture":
			texture = GsonAdapters.resourceLocationAdapter.read(in);
			break;
		}
	}
	
	protected void writeToJson(JsonWriter out) throws IOException {
		TypeAdapter cta = GsonAdapters.compTransformAdapter;
		out.name("t_std");
		cta.write(out, stdTransform);
		out.name("t_fp");
		cta.write(out, fpTransform);
		out.name("t_tp");
		cta.write(out, tpTransform);
//		if(value.texture != null) {
//			out.name("texture");
//			cta.write(out, value.texture.toString());
//		}
	}
	
	public static class Adapter<T extends ItemModelRenderer> extends TypeAdapter<T> {

		@Override
		public final void write(JsonWriter out, T value) throws IOException {
			out.beginObject();
			value.writeToJson(out);
			out.endObject();
		}

		@Override
		public final T read(JsonReader in) throws IOException {
			T ret = create();
			in.beginObject();
			JsonToken token;
			while((token = in.peek()) != JsonToken.END_ARRAY) {
				ret.readFromJson(in.nextString(), in);
			}
			in.endObject();
			return ret;
		}
		
		protected T create() { return (T) new ItemModelRenderer(); }
		
	}
	
	public static final Adapter baseAdapter = new Adapter();
	
}
