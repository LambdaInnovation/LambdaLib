package cn.lambdalib.util.mc;

import cn.lambdalib.annoreg.core.Registrant;
import cn.lambdalib.annoreg.mc.RegEventHandler;
import net.minecraft.world.storage.loot.LootEntry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Paindar on 17/10/21.
 */
@Registrant
public class LILootManager {

    @RegEventHandler(RegEventHandler.Bus.Forge)
    private static LILootManager instance=new LILootManager();
    private Map<String,List<LootEntry>> typeMap = new HashMap<>();

    @SubscribeEvent
    public void onLootLoad(LootTableLoadEvent evt) {

    }

    public static LILootManager getInstance()
    {
        return instance;
    }

    public void add(String type, LootEntry entry){
        if(!typeMap.containsKey(type)) {
            typeMap.put(type,new ArrayList<>());
        }
        typeMap.get(type).add(entry);
    }

}
