/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.util.deprecated;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.MouseInputEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.ItemCraftedEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.ItemPickupEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.ItemSmeltedEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.RenderTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent;

/**
 * In 1.12.2, FMLEventBus have been merged to {@link MinecraftForge#EVENT_BUS}.
 * 原作者语：[表情]主要是要动态注册取消handler。
 *           如果不是动态的话FML自带的Bus性能会好一些。。
 *           毕竟上的ASM
 *           别给我说你们所有的handler都直接往里面扔了。。。
 *           [表情]常驻的handler请使用FML的EventBus。。。
 *           [表情]这个主要用于动态注册取消的handler
 * @author Violet, Paindar
 *
 */
public class LIFMLGameEventDispatcher {

    public static final LIFMLGameEventDispatcher INSTANCE = new LIFMLGameEventDispatcher();
    
    private LIFMLGameEventDispatcher() {
    }
    
    public static void init() {
        MinecraftForge.EVENT_BUS.register(INSTANCE);
    }
    
    public void registerMouseInput(LIHandler handler) {
        addMouseInput.add(handler);
    }

    public void registerKeyInput(LIHandler handler) {
        addKeyInput.add(handler);
    }

    public void registerItemPickup(LIHandler handler) {
        addItemPickup.add(handler);
    }

    public void registerItemCrafted(LIHandler handler) {
        addItemCrafted.add(handler);
    }

    public void registerItemSmelted(LIHandler handler) {
        addItemSmelted.add(handler);
    }

    public void registerPlayerLoggedIn(LIHandler handler) {
        addPlayerLoggedIn.add(handler);
    }

    public void registerPlayerLoggedOut(LIHandler handler) {
        addPlayerLoggedOut.add(handler);
    }

    public void registerPlayerRespawn(LIHandler handler) {
        addPlayerRespawn.add(handler);
    }

    public void registerPlayerChangedDimension(LIHandler handler) {
        addPlayerChangedDimension.add(handler);
    }

    public void registerServerTick(LIHandler handler) {
        addServerTick.add(handler);
    }

    public void registerClientTick(LIHandler handler) {
        addClientTick.add(handler);
    }

    public void registerWorldTick(LIHandler handler) {
        addWorldTick.add(handler);
    }

    public void registerPlayerTick(LIHandler handler) {
        addPlayerTick.add(handler);
    }

    public void registerRenderTick(LIHandler handler) {
        addRenderTick.add(handler);
    }

    private final LinkedList<LIHandler> hMouseInput = new LinkedList<LIHandler>();
    private final ArrayList<LIHandler> addMouseInput = new ArrayList<LIHandler>();
    private final LinkedList<LIHandler> hKeyInput = new LinkedList<LIHandler>();
    private final ArrayList<LIHandler> addKeyInput = new ArrayList<LIHandler>();
    private final LinkedList<LIHandler> hItemPickup = new LinkedList<LIHandler>();
    private final ArrayList<LIHandler> addItemPickup = new ArrayList<LIHandler>();
    private final LinkedList<LIHandler> hItemCrafted = new LinkedList<LIHandler>();
    private final ArrayList<LIHandler> addItemCrafted = new ArrayList<LIHandler>();
    private final LinkedList<LIHandler> hItemSmelted = new LinkedList<LIHandler>();
    private final ArrayList<LIHandler> addItemSmelted = new ArrayList<LIHandler>();
    private final LinkedList<LIHandler> hPlayerLoggedIn = new LinkedList<LIHandler>();
    private final ArrayList<LIHandler> addPlayerLoggedIn = new ArrayList<LIHandler>();
    private final LinkedList<LIHandler> hPlayerLoggedOut = new LinkedList<LIHandler>();
    private final ArrayList<LIHandler> addPlayerLoggedOut = new ArrayList<LIHandler>();
    private final LinkedList<LIHandler> hPlayerRespawn = new LinkedList<LIHandler>();
    private final ArrayList<LIHandler> addPlayerRespawn = new ArrayList<LIHandler>();
    private final LinkedList<LIHandler> hPlayerChangedDimension = new LinkedList<LIHandler>();
    private final ArrayList<LIHandler> addPlayerChangedDimension = new ArrayList<LIHandler>();
    private final LinkedList<LIHandler> hServerTick = new LinkedList<LIHandler>();
    private final ArrayList<LIHandler> addServerTick = new ArrayList<LIHandler>();
    private final LinkedList<LIHandler> hClientTick = new LinkedList<LIHandler>();
    private final ArrayList<LIHandler> addClientTick = new ArrayList<LIHandler>();
    private final LinkedList<LIHandler> hWorldTick = new LinkedList<LIHandler>();
    private final ArrayList<LIHandler> addWorldTick = new ArrayList<LIHandler>();
    private final LinkedList<LIHandler> hPlayerTick = new LinkedList<LIHandler>();
    private final ArrayList<LIHandler> addPlayerTick = new ArrayList<LIHandler>();
    private final LinkedList<LIHandler> hRenderTick = new LinkedList<LIHandler>();
    private final ArrayList<LIHandler> addRenderTick = new ArrayList<LIHandler>();

    @SubscribeEvent
    public void onMouseInput(MouseInputEvent event) {
        if (!addMouseInput.isEmpty()) {
            hMouseInput.addAll(addMouseInput);
            addMouseInput.clear();
        }
        for (Iterator<LIHandler> it = hMouseInput.iterator(); it.hasNext(); ) {
            LIHandler handler = it.next();
            if (handler.isDead())
                it.remove();
            else
                handler.trigger(event);
        }
    }

    @SubscribeEvent
    public void onKeyInput(KeyInputEvent event) {
        if (!addKeyInput.isEmpty()) {
            hKeyInput.addAll(addKeyInput);
            addKeyInput.clear();
        }
        for (Iterator<LIHandler> it = hKeyInput.iterator(); it.hasNext(); ) {
            LIHandler handler = it.next();
            if (handler.isDead())
                it.remove();
            else
                handler.trigger(event);
        }
    }

    @SubscribeEvent
    public void onItemPickup(ItemPickupEvent event) {
        if (!addItemPickup.isEmpty()) {
            hItemPickup.addAll(addItemPickup);
            addItemPickup.clear();
        }
        for (Iterator<LIHandler> it = hItemPickup.iterator(); it.hasNext(); ) {
            LIHandler handler = it.next();
            if (handler.isDead())
                it.remove();
            else
                handler.trigger(event);
        }
    }

    @SubscribeEvent
    public void onItemCrafted(ItemCraftedEvent event) {
        if (!addItemCrafted.isEmpty()) {
            hItemCrafted.addAll(addItemCrafted);
            addItemCrafted.clear();
        }
        for (Iterator<LIHandler> it = hItemCrafted.iterator(); it.hasNext(); ) {
            LIHandler handler = it.next();
            if (handler.isDead())
                it.remove();
            else
                handler.trigger(event);
        }
    }

    @SubscribeEvent
    public void onItemSmelted(ItemSmeltedEvent event) {
        if (!addItemSmelted.isEmpty()) {
            hItemSmelted.addAll(addItemSmelted);
            addItemSmelted.clear();
        }
        for (Iterator<LIHandler> it = hItemSmelted.iterator(); it.hasNext(); ) {
            LIHandler handler = it.next();
            if (handler.isDead())
                it.remove();
            else
                handler.trigger(event);
        }
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerLoggedInEvent event) {
        if (!addPlayerLoggedIn.isEmpty()) {
            hPlayerLoggedIn.addAll(addPlayerLoggedIn);
            addPlayerLoggedIn.clear();
        }
        for (Iterator<LIHandler> it = hPlayerLoggedIn.iterator(); it.hasNext(); ) {
            LIHandler handler = it.next();
            if (handler.isDead())
                it.remove();
            else
                handler.trigger(event);
        }
    }

    @SubscribeEvent
    public void onPlayerLoggedOut(PlayerLoggedOutEvent event) {
        if (!addPlayerLoggedOut.isEmpty()) {
            hPlayerLoggedOut.addAll(addPlayerLoggedOut);
            addPlayerLoggedOut.clear();
        }
        for (Iterator<LIHandler> it = hPlayerLoggedOut.iterator(); it.hasNext(); ) {
            LIHandler handler = it.next();
            if (handler.isDead())
                it.remove();
            else
                handler.trigger(event);
        }
    }

    @SubscribeEvent
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        if (!addPlayerRespawn.isEmpty()) {
            hPlayerRespawn.addAll(addPlayerRespawn);
            addPlayerRespawn.clear();
        }
        for (Iterator<LIHandler> it = hPlayerRespawn.iterator(); it.hasNext(); ) {
            LIHandler handler = it.next();
            if (handler.isDead())
                it.remove();
            else
                handler.trigger(event);
        }
    }

    @SubscribeEvent
    public void onPlayerChangedDimension(PlayerChangedDimensionEvent event) {
        if (!addPlayerChangedDimension.isEmpty()) {
            hPlayerChangedDimension.addAll(addPlayerChangedDimension);
            addPlayerChangedDimension.clear();
        }
        for (Iterator<LIHandler> it = hPlayerChangedDimension.iterator(); it.hasNext(); ) {
            LIHandler handler = it.next();
            if (handler.isDead())
                it.remove();
            else
                handler.trigger(event);
        }
    }

    @SubscribeEvent
    public void onServerTick(ServerTickEvent event) {
        if (!addServerTick.isEmpty()) {
            hServerTick.addAll(addServerTick);
            addServerTick.clear();
        }
        for (Iterator<LIHandler> it = hServerTick.iterator(); it.hasNext(); ) {
            LIHandler handler = it.next();
            if (handler.isDead())
                it.remove();
            else
                handler.trigger(event);
        }
    }

    @SubscribeEvent
    public void onClientTick(ClientTickEvent event) {
        if (!addClientTick.isEmpty()) {
            hClientTick.addAll(addClientTick);
            addClientTick.clear();
        }
        for (Iterator<LIHandler> it = hClientTick.iterator(); it.hasNext(); ) {
            LIHandler handler = it.next();
            if (handler.isDead())
                it.remove();
            else
                handler.trigger(event);
        }
    }

    @SubscribeEvent
    public void onWorldTick(WorldTickEvent event) {
        if (!addWorldTick.isEmpty()) {
            hWorldTick.addAll(addWorldTick);
            addWorldTick.clear();
        }
        for (Iterator<LIHandler> it = hWorldTick.iterator(); it.hasNext(); ) {
            LIHandler handler = it.next();
            if (handler.isDead())
                it.remove();
            else
                handler.trigger(event);
        }
    }

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent event) {
        if (!addPlayerTick.isEmpty()) {
            hPlayerTick.addAll(addPlayerTick);
            addPlayerTick.clear();
        }
        for (Iterator<LIHandler> it = hPlayerTick.iterator(); it.hasNext(); ) {
            LIHandler handler = it.next();
            if (handler.isDead())
                it.remove();
            else
                handler.trigger(event);
        }
    }

    @SubscribeEvent
    public void onRenderTick(RenderTickEvent event) {
        if (!addRenderTick.isEmpty()) {
            hRenderTick.addAll(addRenderTick);
            addRenderTick.clear();
        }
        for (Iterator<LIHandler> it = hRenderTick.iterator(); it.hasNext(); ) {
            LIHandler handler = it.next();
            if (handler.isDead())
                it.remove();
            else
                handler.trigger(event);
        }
    }

    @SubscribeEvent
    public void onClientDisconnectionFromServer(ClientDisconnectionFromServerEvent event) {
        // FIXME some of these shouldn't be cleared, we might need to treat them differently
//        hMouseInput.clear();
//        addMouseInput.clear();
//        hKeyInput.clear();
//        addKeyInput.clear();
//        hItemPickup.clear();
//        addItemPickup.clear();
//        hItemCrafted.clear();
//        addItemCrafted.clear();
//        hItemSmelted.clear();
//        addItemSmelted.clear();
//        hPlayerLoggedIn.clear();
//        addPlayerLoggedIn.clear();
//        hPlayerLoggedOut.clear();
//        addPlayerLoggedOut.clear();
//        hPlayerRespawn.clear();
//        addPlayerRespawn.clear();
//        hPlayerChangedDimension.clear();
//        addPlayerChangedDimension.clear();
//        hServerTick.clear();
//        addServerTick.clear();
//        hClientTick.clear();
//        addClientTick.clear();
//        hWorldTick.clear();
//        addWorldTick.clear();
//        hPlayerTick.clear();
//        addPlayerTick.clear();
//        hRenderTick.clear();
//        addRenderTick.clear();
    }
    
}
