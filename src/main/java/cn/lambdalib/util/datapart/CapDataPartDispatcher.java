package cn.lambdalib.util.datapart;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;


import net.minecraftforge.common.capabilities.ICapabilitySerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static cn.lambdalib.util.datapart.CapDataPartHandler.DATA_PART_CAPABILITY;

/**
 * Created by Paindar on 17/10/20.
 */
public class CapDataPartDispatcher implements ICapabilitySerializable<NBTTagCompound>
{
    private IDataPart dataPart = new EntityData<>();
    private IStorage<IDataPart> storage = DATA_PART_CAPABILITY.getStorage();
    private final String TAG_ID = "LL_EntityData";
    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing)
    {
        return capability.equals(DATA_PART_CAPABILITY);
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing)
    {
        if (DATA_PART_CAPABILITY.equals(capability))
        {
            @SuppressWarnings("unchecked")
            T result = (T) dataPart;
            return result;
        }
        return null;
    }

    @Override
    public NBTTagCompound serializeNBT()
    {
        NBTTagCompound nbt=new NBTTagCompound();
        nbt.setTag(TAG_ID,storage.writeNBT(DATA_PART_CAPABILITY,dataPart,null));
        return null;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt)
    {
        NBTTagCompound tag= (NBTTagCompound) nbt.getTag(TAG_ID);
        storage.readNBT(DATA_PART_CAPABILITY,dataPart,null,tag);
    }
}
