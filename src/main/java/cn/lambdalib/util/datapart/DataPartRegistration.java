package cn.lambdalib.util.datapart;

import cn.lambdalib.annoreg.core.AnnotationData;
import cn.lambdalib.annoreg.core.LoadStage;
import cn.lambdalib.annoreg.core.RegistryType;
import cn.lambdalib.annoreg.core.RegistryTypeDecl;
import net.minecraft.entity.EntityLivingBase;

import java.util.Arrays;
import java.util.EnumSet;

@RegistryTypeDecl
public class DataPartRegistration extends RegistryType {

    public DataPartRegistration() {
        super(RegDataPart.class, "RegDataPart");
        setLoadStage(LoadStage.INIT);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean registerClass(AnnotationData data) throws Exception {
        RegDataPart anno = (RegDataPart) data.anno;
        Class regType = anno.value();
        EntityData.register(
                (Class<? extends DataPart<EntityLivingBase>>) data.getTheClass(),
                EnumSet.copyOf(Arrays.asList(anno.side())),
                regType::isAssignableFrom,
                anno.lazy());

        return true;
    }

}
