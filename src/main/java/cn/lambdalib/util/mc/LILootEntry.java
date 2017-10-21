package cn.lambdalib.util.mc;

import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootEntry;
import net.minecraft.world.storage.loot.conditions.LootCondition;

import java.util.Collection;
import java.util.Random;

/**
 * .
 * Created by Paindar on 17/10/21.
 */
public class LILootEntry extends LootEntry
{
    private static final LootCondition LILootConditionEmpty = (rand, context) -> true;
    private Item item;
    private int num;
    public LILootEntry(Item item,int num, int weightIn, String entryName)
    {
        super(weightIn, 0, new LootCondition[]{LILootConditionEmpty}, entryName);
        this.item=item;
        this.num=num;
    }

    @Override
    public void addLoot(Collection<ItemStack> stacks, Random rand, LootContext context)
    {
        if(rand.nextInt(100)<=weight)
            stacks.add(new ItemStack(item,num));
    }

    @Override
    protected void serialize(JsonObject json, JsonSerializationContext context)
    {
        json.addProperty("item", item.getUnlocalizedName());
        json.addProperty("num",num);
    }
}
