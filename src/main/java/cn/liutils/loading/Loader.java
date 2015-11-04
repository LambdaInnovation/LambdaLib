package cn.liutils.loading;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.util.ResourceLocation;

import org.apache.commons.io.IOUtils;

import cn.lambdalib.core.LambdaLib;
import cn.liutils.util.generic.RegistryUtils;

import com.google.common.collect.HashBiMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

public abstract class Loader<T> {
	
	//'default' element (if any): basic search element. 2nd fallback.
	//'parent' element specified in JsonObject: 1st fallback.
	
	Map<String, JsonObject> entries = new HashMap();
	
	HashBiMap<String, T> loadedObjects = HashBiMap.create();
	
	static final JsonParser parser = new JsonParser();
	
	public Loader() {}
	
	public void feed(ResourceLocation loc) {
		try {
			feed(IOUtils.toString(RegistryUtils.getResourceStream(loc)));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void feed(String json) {
		feed(parser.parse(json).getAsJsonObject());
	}
	
	public void feed(JsonObject root) {
		for(Map.Entry<String, JsonElement> entry : root.entrySet()) {
			feedEntry(entry.getKey(), entry.getValue().getAsJsonObject());
		}
	}
	
	public void feedEntry(String name, JsonObject element) {
		entries.put(name, element);
	}
	
	public void loadAll() {
		for(Map.Entry<String, JsonObject> entry : entries.entrySet()) {
			if(!entry.getKey().equals("default") && entry.getValue().get("ignore") == null)
				doLoad(entry.getKey(), entry.getValue());
		}
		
		for(Map.Entry<String, T> entry : loadedObjects.entrySet()) {
			finishedLoading(entry.getKey(), entry.getValue(), new ObjectNamespace(entry.getKey()));
		}
		
	}
	
	public Collection<T> getEnumeration() {
		return loadedObjects.values();
	}
	
	public String getName(T obj) {
		return loadedObjects.inverse().get(obj);
	}
	
	public boolean isLoaded(String name) {
		return loadedObjects.containsKey(name);
	}
	
	public T getObjectLazy(String name) {
		if(!isLoaded(name))
			load(name, new ObjectNamespace(name));
		return getObject(name);
	}
	
	public T getObject(String name) {
		return loadedObjects.get(name);
	}
	
	private void doLoad(String name, JsonObject object) {
		try {
			T obj = load(name, new ObjectNamespace(name));
			if(obj == null) {
				LambdaLib.log.error("Didn't load the element " + name + " correctly.");
				return;
			}
			loadedObjects.put(name, obj);
		} catch(Exception e) {
			LambdaLib.log.error("An error occured when loading element " + name + ".");
			e.printStackTrace();
		}
	}
	
	protected abstract T load(String name, ObjectNamespace ns);
	
	/**
	 * Called when all objects' loading are completed.
	 */
	protected abstract void finishedLoading(String name, T object, ObjectNamespace ns);
	
	/**
	 * @throws NumberFormatException
	 */
	public Double getDouble(String name, Object ...searchRule) {
		JsonPrimitive jp = getProp(name, searchRule);
		return jp == null ? null : jp.getAsDouble();
	}
	
	/**
	 * @throws NumberFormatException
	 */
	public Float getFloat(String name, Object ...searchRule) {
		JsonPrimitive jp = getProp(name, searchRule);
		return jp == null ? null : jp.getAsFloat();
	}
	
	/**
	 * @throws NumberFormatException
	 */
	public Integer getInt(String name, Object ...searchRule) {
		JsonPrimitive jp = getProp(name, searchRule);
		return jp == null ? null : jp.getAsInt();
	}
	
	public Boolean getBoolean(String name, Object ...searchRule) {
		JsonPrimitive jp = getProp(name, searchRule);
		return jp == null ? null : jp.getAsBoolean();
	}
	
	public String getString(String name, Object ...searchRule) {
		JsonPrimitive jp = getProp(name, searchRule);
		return jp == null ? null : jp.getAsString();
	}
	
	/**
	 * 
	 * @param name element name
	 * @param searchRule <br/>
	 * 	* Do a tree locating on the jsonObject. <br/>
	 *  * type=String: name to lookup, parent=JsonObject <br/>
	 *  * type=int: array index, parent=JsonArray <br/>
	 *  * finally the result must be a JsonPrimitive. otherwise you'll get a null. <br/>
	 *  * Parent fallback: Self->Parent(if any)->Default(if any)
	 * @return null if the object does not exist, or didn't find the primitive.
	 * @throws IllegalArgumentException if given a wrong search rule
	 */
	public JsonPrimitive getProp(String name, Object ...searchRule) {
		JsonObject object = entries.get(name);
		if(object == null)
			return null;
		
		JsonObject defObject = entries.get("default");
		JsonObject parObject = null;
		JsonPrimitive par = (JsonPrimitive) object.get("parent");
		if(par != null && par.isString()) {
			parObject = entries.get(par.getAsString());
		}
		JsonObject[] falls = new JsonObject[] { object, defObject, parObject };
		
		for(JsonObject o : falls) {
			if(o == null)
				continue;
			
			JsonElement current = o;
			
			int searchIndex = 0;
			boolean fail = false;
			while(searchIndex < searchRule.length && current != null) {
				Object obj = searchRule[searchIndex];
				if(obj instanceof String) {
					if(!current.isJsonObject()) {
						fail = true;
						break;
					}
					current = current.getAsJsonObject().get((String) obj);
				} else {
					int i = 0;
					try {
						i = (int) obj;
					} catch(Exception e) {
						throw new IllegalArgumentException("Not a string nor an int!");
					}
					
					if(!current.isJsonArray())
						break;
					current = current.getAsJsonArray().get(i);
				}
				++searchIndex;
			}
			
			if(!fail && current != null && current.isJsonPrimitive()) {
				return (JsonPrimitive) current;
			}
		}
		
		return null;
	}
	
	public class ObjectNamespace {
		
		public final String name;
		public final Loader<T> loader;
		
		ObjectNamespace(String n) {
			name = n;
			loader = Loader.this;
		}
		
		/**
		 * @throws NumberFormatException
		 */
		public Double getDouble(Object ...searchRule) {
			return Loader.this.getDouble(name, searchRule);
		}
		
		/**
		 * @throws NumberFormatException
		 */
		public Float getFloat(Object ...searchRule) {
			return Loader.this.getFloat(name, searchRule);
		}
		
		/**
		 * @throws NumberFormatException
		 */
		public Integer getInt(Object ...searchRule) {
			return Loader.this.getInt(name, searchRule);
		}
		
		public Boolean getBoolean(Object ...searchRule) {
			return Loader.this.getBoolean(name, searchRule);
		}
		
		public String getString(Object ...searchRule) {
			return Loader.this.getString(name, searchRule);
		}
		
	}
	
}
