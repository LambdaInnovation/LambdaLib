package cn.lambdalib.test;

import cn.lambdalib.annoreg.core.Registrant;
import cn.lambdalib.annoreg.mc.RegCommand;
import cn.lambdalib.annoreg.mc.RegInitCallback;
import cn.lambdalib.s11n.network.Future;
import cn.lambdalib.s11n.network.NetworkMessage;
import cn.lambdalib.s11n.network.NetworkMessage.Listener;
import cn.lambdalib.s11n.network.NetworkS11n.NetworkS11nType;
import cn.lambdalib.template.command.LICommandBase;
import cn.lambdalib.util.generic.RandUtils;
import cn.lambdalib.util.key.KeyHandler;
import cn.lambdalib.util.key.KeyManager;
import cn.lambdalib.util.mc.PlayerUtils;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.command.ICommandSender;
import org.lwjgl.input.Keyboard;

// @Registrant
@NetworkS11nType
public class FutureTest {
    static final String MSG_QUERY = "query";

    @RegCommand
    public static class Command extends LICommandBase {

        @Override
        public String getCommandName() {
            return "testfut";
        }

        @Override
        public String getCommandUsage(ICommandSender ics) {
            return "nepnepne";
        }

        @Override
        public void processCommand(ICommandSender ics, String[] args) {
            NetworkMessage.sendTo(getCommandSenderAsPlayer(ics),
                    NetworkMessage.staticCaller(FutureTest.class),
                    MSG_QUERY,
                    Future.create((Integer input) -> {
                        sendChat(ics, "Get the value in server! " + input);
                    }));
        }
    }

    @RegInitCallback
    public static void init() {
        KeyManager.dynamic.addKeyHandler("future_test", Keyboard.KEY_P, new KeyHandler() {
            @Override
            public void onKeyDown() {
                NetworkMessage.sendToServer(
                        NetworkMessage.staticCaller(FutureTest.class),
                        MSG_QUERY,
                        Future.create((Integer input) -> {
                            PlayerUtils.sendChat(getPlayer(), "Get the value in client! " + input);
                        }));
            }
        });
    }

    @Listener(channel=MSG_QUERY, side={Side.CLIENT, Side.SERVER})
    static void onQuery(Future<Integer> future) {
        int result = RandUtils.rangei(232, 236);
        future.sendResult(result);
    }
}
