package cn.lambdalib.util.mc;

import cn.lambdalib.annoreg.core.LoadStage;
import cn.lambdalib.annoreg.core.Registrant;
import cn.lambdalib.annoreg.mc.RegCallback;
import net.minecraft.world.storage.loot.LootEntry;
import net.minecraft.world.storage.loot.LootPool;
import net.minecraft.world.storage.loot.RandomValueRange;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
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

    private static LILootManager instance=new LILootManager();
    private Map<String,List<LootEntry>> typeMap = new HashMap<>();

    @RegCallback(stage= LoadStage.PRE_INIT)
    public static void preInit(FMLPreInitializationEvent evt){
        MinecraftForge.EVENT_BUS.register(instance);
    }

    @SubscribeEvent
    public void onLootLoad(LootTableLoadEvent evt) {
        register(evt);
    }

    public static LILootManager getInstance()
    {
        return instance;
    }

    public void register(LootTableLoadEvent evt){
        for(Map.Entry<String,List<LootEntry>>entry:typeMap.entrySet()){
            if (evt.getName().toString().contains(entry.getKey())) {
                LootEntry[] ets=new LootEntry[entry.getValue().size()];
                entry.getValue().toArray(ets);
                LootPool pool = new LootPool(ets, new LootCondition[]{(loot, ctx)->true},
                        new RandomValueRange(1), new RandomValueRange(0),"LLib_LootPool"); // Other params set as you wish.
                evt.getTable().addPool(pool);
                break;
            }
        }

    }

    public void add(String type, LootEntry entry){
        if(!typeMap.containsKey(type)) {
            typeMap.put(type,new ArrayList<>());
        }
        typeMap.get(type).add(entry);
    }

}
