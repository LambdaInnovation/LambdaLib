/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.core;

import cn.lambdalib.annoreg.core.RegistrationManager;
import cn.lambdalib.annoreg.mc.RegisterCallbackManager;
import com.google.common.base.Throwables;
import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import net.minecraftforge.fml.common.discovery.asm.ModAnnotation;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class LLModContainer extends DummyModContainer
{

    public static Logger log = LogManager.getLogger("LambdaLib|Core");
    public static final String MODID = "LambdaLib|Core";
    private static Set<String> removedClasses = new HashSet<>();

    private static ModMetadata getModMetadata() {
        ModMetadata metadata = new ModMetadata();
        metadata.modId = MODID;
        metadata.name = "LambdaLib|Core";
        metadata.version = LambdaLib.VERSION;

        return metadata;
    }

    private final Field ehFieldValue;

    public LLModContainer() {
        super(getModMetadata());

        try {
            ehFieldValue = ModAnnotation.EnumHolder.class.getDeclaredField("value");
            ehFieldValue.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw Throwables.propagate(e);
        }
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
        ASMDataTable data = event.getASMHarvestedData();

        Set<String> removedClasses = Sets.newHashSet();
        { // Get removed classes
            String startupSide = FMLCommonHandler.instance().getSide().toString();
            Set<ASMDataTable.ASMData> sideData = data.getAll("cpw.mods.fml.relauncher.SideOnly");
            for (ASMDataTable.ASMData ad : sideData) if (ad.getClassName().equals(ad.getObjectName())) { // If is a class
                ModAnnotation.EnumHolder enumHolder = (ModAnnotation.EnumHolder) ad.getAnnotationInfo().get("value");
                try {
                    String value = (String) ehFieldValue.get(enumHolder);
                    if (!value.equals(startupSide)) {
                        removedClasses.add(ad.getClassName());
                    }
                } catch (IllegalAccessException ex) {
                    throw Throwables.propagate(ex);
                }
            }
        }

        LLModContainer.removedClasses.addAll(removedClasses);

        Set<String> registrants = mapToClass(data.getAll("cn.lambdalib.annoreg.core.Registrant"));
        registrants.removeAll(removedClasses);
        RegistrationManager.INSTANCE.annotationList(registrants);
        RegisterCallbackManager.INSTANCE.init(registrants);

        Set<String> registryTypes = mapToClass(data.getAll("cn.lambdalib.annoreg.core.RegistryTypeDecl"));
        registryTypes.removeAll(removedClasses);
        RegistrationManager.INSTANCE.addRegistryTypes(registryTypes);

        Set<String> registryMods = mapToClass(data.getAll("cn.lambdalib.annoreg.core.RegistrationMod"));
        registryMods.removeAll(removedClasses);
        RegistrationManager.INSTANCE.addAnnotationMod(registryMods);
    }

    @Subscribe
    public void loadComplete(FMLLoadCompleteEvent event) {
        log.info("AnnotationRegistry is loaded. Checking states.");
        if (LambdaLib.DEBUG) {
            log.info(RegistrationManager.INSTANCE.profiler.toString());
        }

        RegistrationManager.INSTANCE.checkLoadState();
    }

    private Set<String> mapToClass(Set<ASMDataTable.ASMData> adset) {
        return adset.stream().map(ASMDataTable.ASMData::getClassName).collect(Collectors.toSet());
    }

    public static boolean isClassRemoved(String className) {
        return removedClasses.contains(className);
    }

}
