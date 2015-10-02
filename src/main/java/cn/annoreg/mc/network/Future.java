package cn.annoreg.mc.network;

import io.netty.buffer.ByteBuf;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import cn.annoreg.core.Registrant;
import cn.annoreg.mc.RegInit;
import cn.annoreg.mc.s11n.DataSerializer;
import cn.annoreg.mc.s11n.InstanceSerializer;
import cn.annoreg.mc.s11n.SerializationManager;
import cn.annoreg.mc.s11n.StorageOption;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;

/**
 * A wrapper for a returned object for network call.
 * @author acaly
 *
 */
public final class Future {
	
	static SimpleNetworkWrapper wrapper;
	
	public static void init() {
		wrapper = NetworkRegistry.INSTANCE.newSimpleChannel("core_ar_futuresync");
		wrapper.registerMessage(FutureSyncMessageHandler.class, FutureSyncMessage.class, 1, Side.SERVER);
        wrapper.registerMessage(FutureSyncMessageHandler.class, FutureSyncMessage.class, 2, Side.CLIENT);
	}
    
    public static interface FutureCallback<T> {
        /**
         * Called when the future gets its value
         * @param val The returned value
         */
        void onReady(T val);
    }
    
    private static final ThreadLocal<Map<Integer, Future>> futureMap = new ThreadLocal<Map<Integer, Future>>() {
        @Override
        protected Map<Integer, Future> initialValue() {
            return new HashMap();
        }
    };
    
    private static ThreadLocal<Integer> nextId = new ThreadLocal<Integer>() {@Override
        protected Integer initialValue() {
            return 0;
        }
    };
    
    private StorageOption.Option syncOption = StorageOption.Option.NULL;
    
    private NetworkTerminal target;
    private int id;
    
    private Object returned;
    private FutureCallback callback;

    /**
     * Create a new Future object.
     * This object can be directly passed to a network call as an argument,
     * or stored for later check.
     * @return
     */
    public static Future create() {
        Future ret = new Future();
        ret.target = NetworkTerminal.create();
        int id = nextId.get();
        ret.id = id;
        nextId.set(id + 1);
        futureMap.get().put(id, ret);
        return ret;
    }
    
    /**
     * Create a new Future object and bind a callback to it.
     * @param callback
     * @return
     */
    public static <T> Future create(FutureCallback<T> callback) {
        Future ret = create();
        ret.onSync(callback);
        return ret;
    }
    
    private static Future findById(int id) {
        return futureMap.get().get(id);
    }
    
    private Future() {}
    
    /**
     * Get the returned value, if any.
     * Return null if no value is received yet.
     * @return
     */
    public Object get() {
        return this.returned;
    }
    
    /**
     * Check if the value contained in this object is null.
     * Return true if no value is returned yet, or the returned value is null.
     * @return
     */
    public boolean isEmpty() {
        return this.returned == null;
    }
    
    /**
     * Set a callback to be called once a value is returned for remote.
     * @param callback
     * @return
     */
    public Future onSync(FutureCallback callback) {
        if (this.callback != null) {
            throw new RuntimeException("Can not bind multiple sync callbacks");
        }
        this.callback = callback;
        return this;
    }
    
    /**
     * Called at remote. Return an object to this Future.
     * A Future can receive ONLY ONE value from any remote.
     * Call this function more than once will cause an exception.
     * @param obj
     */
    public void setAndSync(Object obj) {
        new FutureSyncMessage(this.target, this.id, obj, this.syncOption).send();
    }
    
    private static Future createWithId(NetworkTerminal target, int id, StorageOption.Option storage) {
        Future ret = new Future();
        ret.target = target;
        ret.id = id;
        ret.syncOption = storage;
        return ret;
    }
    
    private void onDataReceived(Object obj) {
        futureMap.get().remove(this.id);
        this.returned = obj;
        if (this.callback != null) {
            this.callback.onReady(obj);
        }
    }
    
    public static class FutureSerializer implements DataSerializer<Future>, InstanceSerializer<Future> {
        
        @Override
        public Future readInstance(NBTBase nbt) throws Exception {
            NBTTagCompound comp = (NBTTagCompound) nbt;
            int id = comp.getInteger("id");
            NetworkTerminal target = NetworkTerminal.fromNBT(comp.getTag("target"));
            return Future.createWithId(target, id, StorageOption.Option.INSTANCE);
        }

        @Override
        public NBTBase writeInstance(Future obj) throws Exception {
            NBTTagCompound comp = new NBTTagCompound();
            comp.setInteger("id", obj.id);
            comp.setTag("target", obj.target.toNBT());
            return comp;
        }

        @Override
        public Future readData(NBTBase nbt, Future obj) throws Exception {
            NBTTagCompound comp = (NBTTagCompound) nbt;
            int id = comp.getInteger("id");
            NetworkTerminal target = NetworkTerminal.fromNBT(comp.getTag("target"));
            return Future.createWithId(target, id, StorageOption.Option.DATA);
        }

        @Override
        public NBTBase writeData(Future obj) throws Exception {
            NBTTagCompound comp = new NBTTagCompound();
            comp.setInteger("id", obj.id);
            comp.setTag("target", obj.target.toNBT());
            return comp;
        }
        
    }
    
    public static class FutureSyncMessage implements IMessage {
        
        public NetworkTerminal target; //only used in send
        
        public int id;
        public StorageOption.Option storage;
        public NBTBase data;

        @Override
        public void fromBytes(ByteBuf buf) {
            id = buf.readInt();
            data = ByteBufUtils.readTag(buf).getTag("data");
            storage = StorageOption.Option.values()[buf.readInt()];
        }

        @Override
        public void toBytes(ByteBuf buf) {
            buf.writeInt(id);
            NBTTagCompound write = new NBTTagCompound();
            write.setTag("data", data);
            ByteBufUtils.writeTag(buf, write);
            buf.writeInt(storage.ordinal());
        }
        
        public FutureSyncMessage() {}
        
        public FutureSyncMessage(NetworkTerminal target, int id, Object obj, StorageOption.Option storage) {
            this.target = target;
            this.id = id;
            this.data = SerializationManager.INSTANCE.serialize(obj, storage);
            this.storage = storage;
        }
        
        public void send() {
            target.send(wrapper, this);
        }
    }
    
    public static class FutureSyncMessageHandler implements IMessageHandler<FutureSyncMessage, IMessage> {

        @Override
        public IMessage onMessage(FutureSyncMessage message, MessageContext ctx) {
            Future f = Future.findById(message.id);
            if (f == null) {
                throw new RuntimeException("Can not find the Future object.");
            }
            NBTBase objNbt = message.data;
            Object obj = SerializationManager.INSTANCE.deserialize(null, objNbt, message.storage);
            f.onDataReceived(obj);
            return null;
        }
        
    }
}
