/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.test;

import cn.lambdalib.annoreg.core.Registrant;
import cn.lambdalib.annoreg.mc.RegInitCallback;
import cn.lambdalib.core.LambdaLib;
import cn.lambdalib.s11n.network.NetworkMessage;
import cn.lambdalib.s11n.network.NetworkMessage.INetworkListener;
import cn.lambdalib.s11n.network.NetworkMessage.Listener;
import cn.lambdalib.s11n.network.NetworkS11n;
import cn.lambdalib.s11n.network.NetworkS11n.NetS11nAdaptor;
import cn.lambdalib.util.key.KeyHandler;
import cn.lambdalib.util.key.KeyManager;
import com.google.common.base.Joiner;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import org.lwjgl.input.Keyboard;

// @Registrant
public class NetMessageTest {

    static class BaseTestEnvironment {

        @Listener(channel = "alpha", side={Side.CLIENT, Side.SERVER})
        private void alpha() {
            debug("alpha BASE");
        }

    }

    static class TestEnvironment extends BaseTestEnvironment {

        @Listener(channel = "alpha", side={Side.CLIENT, Side.SERVER})
        public void alpha1() {
            debug("alpha1");
        }

        @Listener(channel ="alpha", side = Side.SERVER)
        void alpha2(int par1, double par2) {
            debug("alpha2 " + par1 + " " + par2);
        }

        @Listener(channel = "alpha", side={Side.CLIENT, Side.SERVER})
        private void alpha3(int par1) {
            debug("alpha3 " + par1);
        }

        @Listener(channel = "beta", side={Side.CLIENT, Side.SERVER})
        public void beta(String what) {
            debug("beta " + what);
        }

    }

    static class TestExtension {

        public TestExtension(TestEnvironment env) {
            System.out.println("testExt constructed: " + env);
        }

        @Listener(channel = "beta", side={Side.CLIENT, Side.SERVER})
        public void beta() {
            debug("Hey I've hacked in!");
        }

    }

    static final TestEnvironment env = new TestEnvironment();

    @SideOnly(Side.CLIENT)
    @RegInitCallback
    public static void initClient() {
        // Test beta channel
        KeyManager.dynamic.addKeyHandler("TNE1", Keyboard.KEY_H, new KeyHandler() {
            @Override
            public void onKeyDown() {
                debug("BetaDown");
                NBTTagCompound tag = new NBTTagCompound();
                NetworkMessage.sendToServer(env, "beta", "Nothing is true, everything is permitted.");
            }
        });

        // Test alpha channel
        KeyManager.dynamic.addKeyHandler("TNE2", Keyboard.KEY_J, new KeyHandler() {
            @Override
            public void onKeyDown() {
                debug("AlphaDown");
                NetworkMessage.sendToServer(env, "alpha", 1, 2.0);
            }
        });
    }

    @RegInitCallback
    public static void init() {
        NetworkMessage.registerExtListener(TestEnvironment.class, "alpha", Side.SERVER, new INetworkListener() {
            @Override
            public void invoke(Object instance, Object... args) throws Exception {
                debug("Invoked ext listener! args: " + Joiner.on(',').join(args));
            }
        });

        NetworkMessage.registerExtension(TestEnvironment.class, env -> new TestExtension(env));

        NetworkS11n.addDirect(TestEnvironment.class, new NetS11nAdaptor<TestEnvironment>() {
            @Override
            public void write(ByteBuf buf, TestEnvironment obj) {}
            @Override
            public TestEnvironment read(ByteBuf buf) {
                return env;
            }
        });
    }

    private static void debug(Object msg) {
        LambdaLib.log.info("[TestEnv]" + msg);
    }

}
