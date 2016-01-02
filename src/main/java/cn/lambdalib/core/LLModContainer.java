/**
 * Copyright (c) Lambda Innovation, 2013-2015
 * 本作品版权由Lambda Innovation所有。
 * http://www.li-dev.cn/
 *
 * This project is open-source, and it is distributed under
 * the terms of GNU General Public License. You can modify
 * and distribute freely as long as you follow the license.
 * 本项目是一个开源项目，且遵循GNU通用公共授权协议。
 * 在遵照该协议的情况下，您可以自由传播和修改。
 * http://www.gnu.org/licenses/gpl.html
 */
package cn.lambdalib.core;

import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import cn.lambdalib.annoreg.core.RegistrationManager;
import cn.lambdalib.networkcall.Future;
import cpw.mods.fml.common.DummyModContainer;
import cpw.mods.fml.common.LoadController;
import cpw.mods.fml.common.ModMetadata;
import cpw.mods.fml.common.discovery.ASMDataTable;
import cpw.mods.fml.common.event.FMLConstructionEvent;
import cpw.mods.fml.common.event.FMLLoadCompleteEvent;

public class LLModContainer extends DummyModContainer {

    public static Logger log = LogManager.getLogger("LambdaLib|Core");
    public static final String MODID = "LambdaLib|Core";

    private static ModMetadata getModMetadata() {
        ModMetadata metadata = new ModMetadata();
        metadata.modId = MODID;
        metadata.name = "LambdaLib|Core";
        metadata.version = LambdaLib.VERSION;

        return metadata;
    }

    public LLModContainer() {
        super(getModMetadata());
    }

    @Override
    public boolean registerBus(EventBus bus, LoadController controller) {
        bus.register(this);
        return true;
    }

    @Subscribe
    public void constructMod(FMLConstructionEvent event) {
        log.info("LambdaLib|Core is loading.");

        // Get annotation information from forge asm data table.
        // This must be done before PreInit stage.
        ASMDataTable dt = event.getASMHarvestedData();
        RegistrationManager.INSTANCE.addRegistryTypes(dt.getAll("cn.lambdalib.annoreg.core.RegistryTypeDecl"));
        RegistrationManager.INSTANCE.annotationList(dt.getAll("cn.lambdalib.annoreg.core.Registrant"));
        RegistrationManager.INSTANCE.addAnnotationMod(dt.getAll("cn.lambdalib.annoreg.core.RegistrationMod"));

        // Misc initialization
        Future.init();
    }

    @Subscribe
    public void loadComplete(FMLLoadCompleteEvent event) {
        log.info("AnnotationRegistry is loaded. Checking states.");
        RegistrationManager.INSTANCE.checkLoadState();
    }

}
