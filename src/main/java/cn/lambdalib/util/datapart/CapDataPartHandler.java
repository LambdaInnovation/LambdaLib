package cn.lambdalib.util.datapart;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

/**
 * Created by Paindar on 17/10/19.
 */
public class CapDataPartHandler
{
    @CapabilityInject(IDataPart.class)
    static Capability<IDataPart> DATA_PART_CAPABILITY = null;

    static IStorage<IDataPart> storage = new IStorage<IDataPart>() {
        @Override
        public NBTBase writeNBT(Capability<IDataPart> capability, IDataPart instance, EnumFacing side)
        {
            NBTTagCompound nbt=new NBTTagCompound();
            instance.writeNBT(nbt);
            return nbt;
        }

        @Override
        public void readNBT(Capability<IDataPart> capability, IDataPart instance, EnumFacing side, NBTBase base)
        {
            if (instance instanceof EntityData)
                throw new RuntimeException("IDataPart instance does not implement EntityData");
            instance.readNBT((NBTTagCompound) base);
        }
    };

    public static void register(){
        CapabilityManager.INSTANCE.register(IDataPart.class, storage,
                EntityData::new);
    }
}
