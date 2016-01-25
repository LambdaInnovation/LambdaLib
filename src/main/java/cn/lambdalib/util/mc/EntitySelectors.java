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
package cn.lambdalib.util.mc;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    public static IEntitySelector living = or(
        new SelectorOfType(EntityLivingBase.class),
        new SelectorOfType(EntityDragonPart.class) // Workaround to let it be applicable for dragon
    );
    
    public static IEntitySelector player = new SelectorOfType(EntityPlayer.class);
    
    public static IEntitySelector survivalPlayer = new IEntitySelector() {

        @Override
        public boolean isEntityApplicable(Entity e) {
            return (e instanceof EntityPlayer && !((EntityPlayer)e).capabilities.isCreativeMode);
        }
        
    };
    
    public static IEntitySelector nothing = new IEntitySelector() {

        @Override
        public boolean isEntityApplicable(Entity p_82704_1_) {
            return false;
        }
        
    };
    
    public static IEntitySelector everything = new IEntitySelector() {

        @Override
        public boolean isEntityApplicable(Entity p_82704_1_) {
            return true;
        }
        
    };
    
    public static class SelectorOfType implements IEntitySelector {
        
        final Class<? extends Entity> klass;
        
        public SelectorOfType(Class<? extends Entity> _klass) {
            klass = _klass;
        }

        @Override
        public boolean isEntityApplicable(Entity entity) {
            return klass.isInstance(entity);
        }
        
    }
    
    public static class ExcludeType implements IEntitySelector {
        
        final Class<? extends Entity> klass;

        public ExcludeType(Class<? extends Entity> _klass) {
            klass = _klass;
        }
        
        @Override
        public boolean isEntityApplicable(Entity entity) {
            return !klass.isInstance(entity);
        }
        
    }
    
    public static class RestrictRange implements IEntitySelector {
        
        final double x, y, z;
        final double rangeSq;
        
        public RestrictRange(Entity e, double range) {
            this(e.posX, e.posY, e.posZ, range);
        }
        
        public RestrictRange(double _x, double _y, double _z, double _range) {
            x = _x;
            y = _y;
            z = _z;
            rangeSq = _range * _range;
        }

        @Override
        public boolean isEntityApplicable(Entity entity) {
            double dx = entity.posX - x,
                    dy = entity.posY - y,
                    dz = entity.posZ - z;
            return dx * dx + dy * dy + dz * dz <= rangeSq;
        }
        
    }
    
    public static class Exclusion implements IEntitySelector {
        
        final Set<Entity> exclusions = new HashSet<>();
        
        public Exclusion(Entity ...excls) {
            for(Entity e : excls)
                exclusions.add(e);
        }
        
        public Exclusion add(Entity e) {
            exclusions.add(e);
            return this;
        }

        @Override
        public boolean isEntityApplicable(Entity entity) {
            return !exclusions.contains(entity);
        }
        
    }
    
    public static class SelectorList implements IEntitySelector {
        
        List<IEntitySelector> list = new ArrayList<>();
        
        public SelectorList(IEntitySelector ...sels) {
            for(IEntitySelector i : sels)
                list.add(i);
        }
        
        public SelectorList append(IEntitySelector selector) {
            list.add(selector);
            return this;
        }

        @Override
        public boolean isEntityApplicable(Entity entity) {
            
            for(IEntitySelector i : list)
                if(!i.isEntityApplicable(entity))
                    return false;
            return true;
        }
        
    }
    
    /**
     * Combine a set of EntitySelectors (logical AND) to create a new EntitySelector.
     */
    public static IEntitySelector and(IEntitySelector ...sels) {
        return new SelectorList(sels);
    }
    
    /**
     * Combine a set of EntitySelectors (logical OR) to create a new EntitySelector.
     */
    public static IEntitySelector or(IEntitySelector ...sels) {
        return new IEntitySelector() {
            @Override
            public boolean isEntityApplicable(Entity entity) {
                if(sels.length == 0)
                    return true;
                for(IEntitySelector s : sels)
                    if(s.isEntityApplicable(entity))
                        return true;
                return false;
            }
            
        };
    }
    
    /**
     * Create an EntitySelector that excludes the passed in entities.
     */
    public static IEntitySelector excludeOf(Entity ...ents) {
        return new Exclusion(ents);
    }
    
    public static IEntitySelector excludeType(Class<? extends Entity> klass) {
        return new ExcludeType(klass);
    }
    
}
