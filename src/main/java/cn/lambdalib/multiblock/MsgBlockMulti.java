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
package cn.lambdalib.multiblock;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import cn.lambdalib.annoreg.core.Registrant;
import cn.lambdalib.annoreg.mc.RegMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * @author WeathFolD
 *
 */
@Registrant
public class MsgBlockMulti implements IMessage {

	int x, y, z;
	ForgeDirection dir;
	int s; // subID

	public MsgBlockMulti(InfoBlockMulti i) {
		TileEntity te = i.te;
		x = te.xCoord;
		y = te.yCoord;
		z = te.zCoord;
		dir = i.dir;
		s = i.subID;
	}

	public MsgBlockMulti() {
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		x = buf.readInt();
		y = buf.readInt();
		z = buf.readInt();
		dir = ForgeDirection.values()[buf.readByte()];
		s = buf.readByte();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(x).writeInt(y).writeInt(z).writeByte(dir.ordinal()).writeByte(s);
	}

	public static class Req implements IMessage {

		int x, y, z;

		public Req(InfoBlockMulti ibm) {
			TileEntity te = ibm.te;
			x = te.xCoord;
			y = te.yCoord;
			z = te.zCoord;
		}

		public Req() {
		}

		@Override
		public void fromBytes(ByteBuf buf) {
			x = buf.readInt();
			y = buf.readInt();
			z = buf.readInt();
		}

		@Override
		public void toBytes(ByteBuf buf) {
			buf.writeInt(x).writeInt(y).writeInt(z);
		}

	}

	@RegMessageHandler(msg = Req.class, side = RegMessageHandler.Side.SERVER)
	public static class ReqHandler implements IMessageHandler<Req, MsgBlockMulti> {

		@Override
		public MsgBlockMulti onMessage(Req msg, MessageContext ctx) {
			TileEntity te = ctx.getServerHandler().playerEntity.worldObj.getTileEntity(msg.x, msg.y, msg.z);
			if (te instanceof IMultiTile) {
				InfoBlockMulti i = ((IMultiTile) te).getBlockInfo();
				if (i != null) {
					return new MsgBlockMulti(i);
				}
			}
			return null;
		}

	}

	@RegMessageHandler(msg = MsgBlockMulti.class, side = RegMessageHandler.Side.CLIENT)
	public static class Handler implements IMessageHandler<MsgBlockMulti, IMessage> {

		@Override
		@SideOnly(Side.CLIENT)
		public IMessage onMessage(MsgBlockMulti msg, MessageContext ctx) {
			World world = Minecraft.getMinecraft().theWorld;
			TileEntity te = world.getTileEntity(msg.x, msg.y, msg.z);
			if (!(te instanceof IMultiTile))
				return null;
			InfoBlockMulti info = ((IMultiTile) te).getBlockInfo();
			// If TE is there and info is present, do the sync.
			if (info != null) {
				info.dir = msg.dir;
				info.subID = msg.s;
				info.setLoaded();
			}
			return null;
		}

	}

}
