package cn.lambdalib.test;

import cn.lambdalib.s11n.SerializeType;
import cn.lambdalib.s11n.nbt.NBTS11n;
import com.google.common.base.Objects;
import net.minecraft.nbt.NBTTagCompound;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.lambdalib.util.generic.RandUtils.*;

public class NBTS11nTest {

    public enum TestEnum {
        FAT, THIN, DIRTY
    }

    @SerializeType
    static class TestInner {
        public int a, b, c;
        public String msg;
        public TestEnum charc;

        public TestInner() {
            a = nextInt(100);
            b = nextInt(100);
            c = nextInt(100);
            msg = String.valueOf(nextDouble());
            charc = TestEnum.values()[nextInt(TestEnum.values().length)];
        }

        public String toString() {
            return Objects.toStringHelper(this)
                    .add("msg", msg)
                    .add("a", a)
                    .add("b", b)
                    .add("c", c)
                    .add("charc", charc)
                    .toString();
        }
    }

    static class TestOuter {
        public float x, y, z;
        // public TestInner inner = new TestInner();
        public Map map = new HashMap<>();
        public int[] array = new int[] { 1, 2, 3 };
        public List<Object> objects = new ArrayList<>();

        public TestOuter() {
            x = nextFloat();
            y = nextFloat();
            z = nextFloat();

            map.put(nextFloat(), 233);
            map.put("aaa", nextFloat());
            map.put("bbb", nextDouble());

            objects.add("aaa");
            objects.add("bbb");

            objects.add(nextFloat());
            objects.add(nextFloat());
        }

        public String toString() {
            return Objects.toStringHelper(this)
                    // .add("inner", inner)
                    .add("x", x)
                    .add("y", y)
                    .add("z", z)
                    .add("map", map)
                    .add("objects", objects)
                    .toString();
        }
    }

    public static void main(String[] args) {
        TestOuter data = new TestOuter();
        NBTTagCompound tag = new NBTTagCompound();

        System.out.println("Input: " + data);

        NBTS11n.write(tag, data);

        TestOuter readed = new TestOuter();
        NBTS11n.read(tag, readed);

<<<<<<< HEAD
        System.out.println("Tag: " + tag);

        System.out.println("Output: " + readed);
=======
        System.out.println("Input: " + data);
        System.out.println("Output: " + data);
>>>>>>> parent of dc3e82b... oops
    }

}
