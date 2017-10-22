package cn.lambdalib.annoreg.mc;

import cn.lambdalib.annoreg.core.LoadStage;
import cn.lambdalib.core.LambdaLib;
import net.minecraftforge.fml.common.event.FMLStateEvent;
import org.apache.logging.log4j.Logger;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * This class manage all method marked by @RegCallback.
 * Initialized by LLModContainer.
 * Created by Paindar on 17/10/22.
 */
public class RegisterCallbackManager {
    public static final RegisterCallbackManager INSTANCE = new RegisterCallbackManager();
    private Set<String> registrants = new LinkedHashSet<>();
    private Map<LoadStage,List<Method>> methodMap = new HashMap<>();
    private final Logger log= LambdaLib.log;
    public void init(Set<String> newSet){
        registrants.addAll(newSet);
        for(String className:registrants){
            Class klass;

            try {
                klass=Class.forName(className);
            } catch (ClassNotFoundException e)
            {
                e.printStackTrace();
                throw new RuntimeException();
            }
            Method[] methods = klass.getMethods();
            for(Method method:methods){
                Annotation anno=method.getAnnotation(RegCallback.class);
                if(anno!=null){
                    LoadStage stg=((RegCallback)anno).stage();
                    if(!methodMap.containsKey(stg)){
                        methodMap.put(stg, new ArrayList<>());
                    }
                    methodMap.get(stg).add(method);
                }
            }
        }
        for(Map.Entry<LoadStage,List<Method>> e:methodMap.entrySet()){
            log.info(e.getKey().name+" - "+e.getValue().size());
        }
    }
    public RegisterCallbackManager(){
    }

    public void registerAll(LoadStage stage,FMLStateEvent event){
        List<Method> allMethod=methodMap.get(stage);
        if(allMethod!=null){
            for(Method method:allMethod){
                try
                {
                    method.invoke(null,event);
                } catch (IllegalAccessException e)
                {
                    log.error("Fail to execute method: "+method+", it may be private.",e);
                    e.printStackTrace();
                } catch (InvocationTargetException e)
                {
                    e.printStackTrace();
                } catch (NullPointerException e){
                    log.error("Fail to execute method: "+method+", it should be a static method.",e);
                    e.printStackTrace();
                } catch (IllegalArgumentException e){
                    log.error("Fail to execute method: "+method+" with argument : "+event+", its argument may be incorrect.",e);
                    e.printStackTrace();
                }
                if(LambdaLib.DEBUG){
                    log.info("Method: "+method+" was invoked.");
                }
            }
        }
    }
}
