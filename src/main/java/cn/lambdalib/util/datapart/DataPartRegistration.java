/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.util.datapart;

import cn.lambdalib.annoreg.core.AnnotationData;
import cn.lambdalib.annoreg.core.LoadStage;
import cn.lambdalib.annoreg.core.RegistryType;
import cn.lambdalib.annoreg.core.RegistryTypeDecl;

/**
 * @author WeAthFolD
 *
 */
@RegistryTypeDecl
public class DataPartRegistration extends RegistryType {

    public DataPartRegistration() {
        super(RegDataPart.class, "LL_DataPart");
        setLoadStage(LoadStage.INIT);
    }

    @Override
    public boolean registerClass(AnnotationData data) throws Exception {
        Class c = data.getTheClass();
        RegDataPart anno = data.getAnnotation();
        EntityData.register(anno.value(), c, anno.type());
        return true;
    }

}
