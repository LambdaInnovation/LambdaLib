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
package cn.lambdalib.networkcall;

import io.netty.buffer.ByteBuf;

import java.util.HashMap;
import java.util.Map;

import cn.lambdalib.annoreg.mc.SideHelper;
import cn.lambdalib.core.LLModContainer;
import cn.lambdalib.networkcall.s11n.NullInstanceException;
import cn.lambdalib.networkcall.s11n.SerializationManager;
import cn.lambdalib.networkcall.s11n.StorageOption;
import cn.lambdalib.networkcall.s11n.StorageOption.Target.RangeOption;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;

public class NetworkCallManager {

	private interface Caller {
		void invoke(Object[] args);
	}

	public static class NetworkCallMessage implements IMessage {

		String delegateName;
		NBTTagList params;

		public NetworkCallMessage() {
		}

		public NetworkCallMessage(String delegateName, NBTTagList params) {
			this.delegateName = delegateName;
			this.params = params;
		}

		@Override
		public void fromBytes(ByteBuf buf) {
			delegateName = ByteBufUtils.readUTF8String(buf);
			params = (NBTTagList) ByteBufUtils.readTag(buf).getTag("params");
		}

		@Override
		public void toBytes(ByteBuf buf) {
			ByteBufUtils.writeUTF8String(buf, delegateName);
			NBTTagCompound tag = new NBTTagCompound();
			tag.setTag("params", params);
			ByteBufUtils.writeTag(buf, tag);
		}

	}

	public static class MessageHandler implements IMessageHandler<NetworkCallMessage, IMessage> {

		@Override
		public IMessage onMessage(NetworkCallMessage message, MessageContext ctx) {
			NetworkCallDelegate delegate = delegateMap.get(message.delegateName);
			if (delegate == null) {
				LLModContainer.log.fatal("Unknown network call. Delegate name: \"{}\".", message.delegateName);
				throw new RuntimeException();
			}
			// Get objects from message
			NBTTagList nbtList = message.params;
			Object[] params = new Object[nbtList.tagCount()];
			try {
				for (int i = 0; i < params.length; ++i) {
					// TODO removeTag is the only way to get a tag at the given
					// index?
					params[i] = SerializationManager.INSTANCE.deserialize(null, nbtList.removeTag(0),
							StorageOption.Option.AUTO);
				}
			} catch (NullInstanceException e) {
				// null check failed, cancel.
				return null;
			}
			delegate.invoke(params);
			return null;
		}

	}

	private static Map<String, NetworkCallDelegate> delegateMap = new HashMap();
	private static Map<String, Caller> callerMap = new HashMap();

	private static SimpleNetworkWrapper netHandler = new SimpleNetworkWrapper("core_ar_networkcall");

	static {
		netHandler.registerMessage(MessageHandler.class, NetworkCallMessage.class, 1, Side.SERVER);
		netHandler.registerMessage(MessageHandler.class, NetworkCallMessage.class, 2, Side.CLIENT);
	}

	// WARNING: this method is called by the dynamically generated code. Don't
	// change name.
	public static void onNetworkCall(String delegateName, Object[] args) {
		Caller caller = callerMap.get(delegateName);
		if (caller == null) {
			LLModContainer.log.fatal("Unknown network call. Delegate name: \"{}\".", delegateName);
			throw new RuntimeException();
		}
		caller.invoke(args);
	}

	public static void registerClientDelegateClass(final String delegateName, NetworkCallDelegate delegate,
			final StorageOption.Option[] options, final int targetIndex, final RangeOption range,
			final double sendRange) {
		callerMap.put(delegateName, new Caller() {
			@Override
			public void invoke(Object[] args) {
				if (args.length != options.length) {
					// This should not happen.
					LLModContainer.log.fatal("Network call: incorrect parameter number.");
					throw new RuntimeException();
				}
				NBTTagList params = new NBTTagList();
				for (int i = 0; i < args.length; ++i) {
					NBTTagCompound tag = (NBTTagCompound) SerializationManager.INSTANCE.serialize(args[i], options[i]);
					if (tag == null) {
						LLModContainer.log.fatal("Serialization error while processing arg " + i + " (" + args[i] + ","
								+ options[i] + ")");
						throw new RuntimeException();
					}
					params.appendTag(SerializationManager.INSTANCE.serialize(args[i], options[i]));
				}
				if (targetIndex == -1) {
					netHandler.sendToAll(new NetworkCallMessage(delegateName, params));
				} else if (range == null) {
					TargetPoint point = TargetPointHelper.convert(args[targetIndex], sendRange);
					if (point != null) {
						netHandler.sendToAllAround(new NetworkCallMessage(delegateName, params), point);
					} else {
						LLModContainer.log.error("NULL TargetPoint par: Sending failed. " + delegateName);
					}
				} else if (range == RangeOption.SINGLE) {
					EntityPlayerMP playerTarget = (EntityPlayerMP) args[targetIndex];
					if (playerTarget != null) {
						netHandler.sendTo(new NetworkCallMessage(delegateName, params), playerTarget);
					} else {
						LLModContainer.log.error("NULL EntityPlayerMP par: Sending failed.");
					}
				} else {
					EntityPlayerMP playerTarget = (EntityPlayerMP) args[targetIndex];
					for (Object p : SideHelper.getPlayerList()) {
						if (p != null && p instanceof EntityPlayerMP && p != playerTarget) {
							netHandler.sendTo(new NetworkCallMessage(delegateName, params), (EntityPlayerMP) p);
						}
					}
				}
			}
		});
		delegateMap.put(delegateName, delegate);
	}

	public static void registerServerDelegateClass(final String delegateName, NetworkCallDelegate delegate,
			final StorageOption.Option[] options) {
		callerMap.put(delegateName, new Caller() {
			@Override
			public void invoke(Object[] args) {
				if (args.length != options.length) {
					// This should not happen.
					LLModContainer.log.fatal("Network call: incorrect parameter number.");
					throw new RuntimeException();
				}
				NBTTagList params = new NBTTagList();
				for (int i = 0; i < args.length; ++i) {
					params.appendTag(SerializationManager.INSTANCE.serialize(args[i], options[i]));
				}

				netHandler.sendToServer(new NetworkCallMessage(delegateName, params));
			}
		});
		delegateMap.put(delegateName, delegate);
	}
}
