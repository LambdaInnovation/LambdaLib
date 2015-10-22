package cn.liutils.vis.gson;

import java.io.IOException;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import cn.liutils.util.generic.VecUtils;
import cn.liutils.vis.model.CompTransform;
import cn.liutils.vis.model.renderer.ItemModelRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;

/**
 * Gson adapters used in info serialization/deserialization.
 * @author WeAthFolD
 */
public class GsonAdapters {

	public static TypeAdapter<Vec3> vec3Adapter =
		new TypeAdapter<Vec3>() {

			@Override
			public void write(JsonWriter out, Vec3 value) throws IOException {
				out.beginArray();
				out.value(value.xCoord);
				out.value(value.yCoord);
				out.value(value.zCoord);
				out.endArray();
			}

			@Override
			public Vec3 read(JsonReader in) throws IOException {
				in.beginArray();
				Vec3 ret = VecUtils.vec(
						in.nextDouble(),
						in.nextDouble(),
						in.nextDouble());
				in.endArray();
				return ret;
			}
			
	};
		
	public static TypeAdapter<CompTransform> compTransformAdapter =
		new TypeAdapter<CompTransform>() {

			@Override
			public void write(JsonWriter out, CompTransform value) throws IOException {
				out.beginObject();
				out.name("transform");
				vec3Adapter.write(out, value.transform);
				out.name("pivot");
				vec3Adapter.write(out, value.pivotPt);
				out.name("rotation");
				vec3Adapter.write(out, value.rotation);
				out.name("scale");
				out.value(value.scale);
				out.endObject();
			}

			@Override
			public CompTransform read(JsonReader in) throws IOException {
				CompTransform ret = new CompTransform();
				in.beginArray();
				JsonToken token;
				while((token = in.peek()) != JsonToken.END_OBJECT) {
					switch(in.nextName()) {
					case "transform":
						ret.transform = vec3Adapter.read(in);
						break;
					case "pivot":
						ret.pivotPt = vec3Adapter.read(in);
						break;
					case "rotation":
						ret.rotation = vec3Adapter.read(in);
						break;
					case "scale":
						ret.scale = in.nextDouble();
						break;
					}
				}
				return ret;
			}
		
	};
	
	public static TypeAdapter<ResourceLocation> resourceLocationAdapter =
		new TypeAdapter<ResourceLocation>() {

			@Override
			public void write(JsonWriter out, ResourceLocation value) throws IOException {
				out.value(value.toString());
			}

			@Override
			public ResourceLocation read(JsonReader in) throws IOException {
				return new ResourceLocation(in.nextString());
			}
		
	};

}
