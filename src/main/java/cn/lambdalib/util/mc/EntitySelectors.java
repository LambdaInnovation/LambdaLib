/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.util.mc;

import java.util.*;
import com.google.common.base.Predicate;

import net.minecraft.command.IEntitySelector;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityDragonPart;
import net.minecraft.entity.player.EntityPlayer;


/**
 * Some commonly used entity selectors.
 * @author WeAthFolD
 */
public class EntitySelectors {

    public static Predicate of(Class<? extends Entity> type) {
        return entity -> type.isInstance(type);
    }

    public static Predicate everything = IEntitySelector.selectAnything;

    public static Predicate nothing = input -> false;

    public static Predicate survivalPlayer = entity -> entity instanceof EntityPlayer && !((EntityPlayer) entity).capabilities.isCreativeMode;

    public static Predicate exclude(Entity ...exclusions) {
        Set<Entity> set = new HashSet<>();
        Collections.addAll(set, exclusions);
        return entity -> !set.contains(entity);
    }

    public static Predicate within(Entity entity, double range) {
        double sq = range * range;
        return e -> e instanceof Entity && ((Entity)e).getDistanceSqToEntity(entity) <= sq;
    }

    public static Predicate within(double x, double y, double z, double range) {
        double sq = range * range;
        return e->e instanceof Entity && ((Entity)e).getDistanceSq(x, y, z) <= sq;
    }

    public static Predicate player() {
        return entity -> entity instanceof EntityPlayer;
    }

    /**
     * @return The selector that returns living things. WARNING: Doesn't select only EntityLivingBase. Some special
     *  living types such as boss parts are taken into consideration.
     */
    public static Predicate living() {
        return entity -> entity instanceof EntityLivingBase || entity instanceof EntityDragonPart;
    }

    /*
    public static IEntitySelector toEntitySelector(Predicate<Entity> pred) {
        if (pred == null) return TRUE;
        else              return new IEntitySelector() {
            @Override
            public boolean isEntityApplicable(Entity entity) {
                return pred.test(entity);
            }
        };
    }

    private static IEntitySelector TRUE = new IEntitySelector() {
        @Override
        public boolean isEntityApplicable(Entity p_82704_1_) {
            return true;
        }
    };*/
    
}
