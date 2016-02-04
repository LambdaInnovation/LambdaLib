/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.s11n.network;

import cn.lambdalib.annoreg.core.Registrant;
import cn.lambdalib.annoreg.mc.RegMessageHandler;
import cn.lambdalib.core.LambdaLib;
import cn.lambdalib.s11n.network.NetworkS11n.ContextException;
import cn.lambdalib.util.generic.ReflectionUtils;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * {@link NetworkMessage} is used for fast messaging of objects (that are usually) across network. <br>
 *
 * Use <code>NetworkMessage.sendXXX</code> to send a message to some object.
 * The object is then retrieved using deserialization in receiver side, and all event
 * listeners (methods decorated with {@link Listener} within the object
 * is invoked with given parameters supplied. This could be really useful
 * in small synchronizations of objects that can be retrieved in both sides,
 * e.g. TileEntities, Entities and many else. <br>
 *
 * @author WeAthFolD
 */
@Registrant
public class NetworkMessage {

    /**
     * Annotates on network message listener methods. The method can contain arbitary parameters. The serialized parameters should
     * be able to be passed into the method, or it results in an exception.
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Listener {
        /**
         * @return The channel that this method listens
         */
        String channel();

        /**
         * @return The side(s) that this listener receives event
         */
        Side[] side();
    }

    /**
     * Indicate that this parameter can be transformed & deserialized as null.
     */
    @Target(ElementType.PARAMETER)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface NullablePar {}

    public interface INetworkListener {
        void invoke(Object instance, Object... args) throws Exception;

        /**
         * Get the parameter limit of this listener. If return -1 there is no limit.
         */
        default int getParameterCount() {
            return -1;
        }
    }

    /**
     * Send the message to the object itself on the fly.
     */
    public static void sendToSelf(Object instance, String channel, Object ...params) {
        processMessage(instance, channel, params);
    }

    public static void sendToServer(Object instance, String channel, Object ...params) {
        network.sendToServer(new Message(instance, channel, params));
    }

    public static void sendTo(EntityPlayer player, Object instance, String channel, Object ...params) {
        network.sendTo(new Message(instance, channel, params), (EntityPlayerMP) player);
    }

    public static void sendToPlayers(EntityPlayerMP[] players, Object instance, String channel, Object ...params) {
        Message msg = new Message(instance, channel, params);
        for (EntityPlayerMP player : players) {
            network.sendTo(msg, player);
        }
    }

    public static void sendToAll(Object instance, String channel, Object ...params) {
        network.sendToAll(new Message(instance, channel, params));
    }

    public static void sendToAllAround(TargetPoint trg, Object instance, String channel, Object ...params) {
        network.sendToAllAround(new Message(instance, channel, params), trg);
    }

    public static void sendToDimension(int dimensionId, Object instance, String channel, Object ...params) {
        network.sendToDimension(new Message(instance, channel, params), dimensionId);
    }

    public static void registerExtListener(Class type, String channel, Side side, INetworkListener listener) {
        ChannelID cid = id(type, channel, side);
        List<INetworkListener> list = extListeners.get(cid);
        if (list == null) {
            list = new ArrayList<>();
            extListeners.put(cid, list);
        }
        list.add(listener);
    }

    public static <T> void registerExtension(Class<T> type, Function<T, Object> extType) {
        List<Function> list = regExtensions.get(type);
        if (list == null) {
            list = new ArrayList<>();
            regExtensions.put(type, list);
        }
        list.add(extType);
    }

    // ---

    private static class ChannelID {
        public final Class c;
        public final String channel;
        public final Side side;

        public ChannelID(Class _c, String _channel, Side _side) {
            c = _c;
            channel = _channel;
            side = _side;
        }

        @Override
        public int hashCode() {
            return c.hashCode() ^ channel.hashCode() ^ side.hashCode();
        }

        @Override
        public boolean equals(Object other) {
            if (other instanceof ChannelID) {
                ChannelID chn = ((ChannelID) other);
                return chn.c == c && channel.equals(chn.channel) && side == chn.side;
            }
            return false;
        }
    }

    private static ChannelID id(Class c, String channel, Side side) {
        return new ChannelID(c, channel, side);
    }

    static final SimpleNetworkWrapper network = LambdaLib.channel;

    private static Map<ChannelID, List<INetworkListener>> extListeners = new HashMap<>();
    private static Map<ChannelID, List<INetworkListener>> cachedListeners = new HashMap<>();

    private static Map<Class, List<Function>> regExtensions = new HashMap<>();
    private static Map<Class, List<Function>> cachedExtensions = new HashMap<>();

    private static Map<Object, List<Object>> aliveExtensions = new WeakHashMap<>();

    /**
     * Invoked at callee side. Send the message to the instance.
     */
    private static void processMessage(Object instance, String channel, Object... params) {
        Side side = FMLCommonHandler.instance().getEffectiveSide();
        List<INetworkListener> listeners = getListeners(instance, channel, side);
        if (listeners.isEmpty()) {
            // LambdaLib.log.warn("Orphant event " + eventSignature(instance, channel));
        } else {
            for (INetworkListener m : listeners) {
                // Check parameter size
                final int paramc = m.getParameterCount();
                if (paramc > params.length) {
                    throw new RuntimeException("Too few arguments in event " + eventSignature(instance, channel)
                     + " for event listener [" + m + "]. Expected at least " + params.length + " arguments");
                } else {
                    Object[] paramsArg;
                    if (paramc == params.length || paramc == -1) {
                        paramsArg = params;
                    } else {
                        paramsArg = Arrays.copyOf(params, paramc);
                    }

                    try {
                        m.invoke(instance, paramsArg);
                    } catch (IllegalArgumentException e) {
                        LambdaLib.log.error("Illegal argument for event listener " + m, e);
                    } catch (Exception e) {
                        LambdaLib.log.fatal("Error during network message.", e);
                    }
                }
            }
        }

        if (!aliveExtensions.containsKey(instance)) {
            List<Function> suppliers = getExtSuppliers(instance.getClass());
            if (!suppliers.isEmpty()) {
                aliveExtensions.put(instance, suppliers.stream().map(x -> x.apply(instance)).collect(Collectors.toList()));
            } else {
                aliveExtensions.put(instance, Collections.emptyList());
            }
        }

        aliveExtensions.get(instance).forEach(x -> processMessage(x, channel, params));
    }

    private static String eventSignature(Object instance, String channel) {
        return instance.getClass().getName() + "#" + channel;
    }

    private static List<INetworkListener> getListeners(Object instance, String channel, Side side) {
        final Class type = instance.getClass();
        final ChannelID cid = id(type, channel, side);

        List<INetworkListener> result = cachedListeners.get(cid);
        if (result == null) {
            result = new ArrayList<>();
            buildCache(type, channel, side, result);

            cachedListeners.put(cid, result);
        }
        return result;
    }

    private static List<Function> getExtSuppliers(Class type) {
        if (cachedExtensions.containsKey(type)) {
            return cachedExtensions.get(type);
        } else {
            List<Function> ret = new ArrayList<>();
            cachedExtensions.put(type, ret);
            buildExtCache(type, ret);
            return ret;
        }
    }

    private static void buildExtCache(Class type, List<Function> out) {
        Class cur = type;
        while (cur != null) {
            List<Function> s = regExtensions.get(cur);
            if (s != null) {
                out.addAll(s);
            }
            cur = cur.getSuperclass();
        }
    }

    private static void buildCache(Class type, String channel, Side side, List<INetworkListener> out) {
        out.addAll(ReflectionUtils.getAllAccessibleMethods(type)
                .stream()
                .filter(m -> {
                    Listener anno = m.getAnnotation(Listener.class);

                    if (anno == null || !anno.channel().equals(channel)) {
                        return false;
                    } else {
                        for (Side s : anno.side()) {
                            if (s == side) {
                                return true;
                            }
                        }
                        return false;
                    }
                })
                .map(NetworkMessage::methodListener)
                .collect(Collectors.toList()));

        Class cur = type;
        while (cur != null) {
            ChannelID cid = id(cur, channel, side);
            List<INetworkListener> exts = extListeners.get(cid);
            if (exts != null) {
                out.addAll(exts);
            }

            cur = cur.getSuperclass();
        }
    }

    private static INetworkListener methodListener(Method m) {
        return new INetworkListener() {

            BitSet nullUnchecked = new BitSet();
            {
                for (int i = 0; i < m.getParameterCount(); ++i) {
                    Parameter[] pars = m.getParameters();
                    if (pars[i].isAnnotationPresent(NullablePar.class)) {
                        nullUnchecked.set(i);
                    }
                }
            }

            @Override
            public void invoke(Object instance, Object... args) throws Exception {
                for (int i = 0; i < args.length; ++i) { // Null check
                    if (!nullUnchecked.get(i)) {
                        if (args[i] == null) {
                            return;
                        }
                    }
                }
                m.invoke(instance, args);
            }

            @Override
            public int getParameterCount() {
                return m.getParameterCount();
            }

            @Override
            public String toString() {
                return m.toString();
            }
        };
    }

    public static class Message implements IMessage {

        boolean valid;
        Object instance;
        String channel;
        Object[] params;

        Message(Object _instance, String _channel, Object ..._params) {
            instance = _instance;
            channel = _channel;
            params = _params;
        }

        public Message() {}

        @Override
        public void toBytes(ByteBuf buf) {
            NetworkS11n.serialize(buf, instance, false);
            ByteBufUtils.writeUTF8String(buf, channel);
            buf.writeByte(params.length);
            for (Object o : params) {
                NetworkS11n.serialize(buf, o, true);
            }
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            try {
                instance = NetworkS11n.deserialize(buf);
                channel = ByteBufUtils.readUTF8String(buf);
                params = new Object[buf.readByte()];
                for (int i = 0; i < params.length; ++i) {
                    params[i] = NetworkS11n.deserialize(buf);
                }
                valid = true;
            } catch (ContextException e) {
                valid = false;
            }
        }

    }

    public static class Handler implements IMessageHandler<Message, IMessage> {

        @Override
        public IMessage onMessage(Message message, MessageContext ctx) {
            if (message.valid) {
                // LambdaLib.log.info("Received message " + message.channel + " on " + message.instance);
                processMessage(message.instance, message.channel, message.params);
            } else {
                LambdaLib.log.info("Ignored some network message");
            }
            return null;
        }
    }

}
