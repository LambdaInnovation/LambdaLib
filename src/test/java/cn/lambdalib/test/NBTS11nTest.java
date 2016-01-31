package cn.lambdalib.test;

import cn.lambdalib.s11n.SerializeType;
import cn.lambdalib.s11n.nbt.NBTS11n;
import com.google.common.base.Objects;
import net.minecraft.nbt.NBTTagCompound;

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
        public TestInner inner = new TestInner();

        public TestOuter() {
            x = nextFloat();
            y = nextFloat();
            z = nextFloat();
        }

        public String toString() {
            return Objects.toStringHelper(this)
                    .add("inner", inner)
                    .add("x", x)
                    .add("y", y)
                    .add("z", z)
                    .toString();
        }
    }

    public static void main(String[] args) {
        TestOuter data = new TestOuter();
        NBTTagCompound tag = new NBTTagCompound();
        NBTS11n.write(tag, data);

        TestOuter readed = new TestOuter();
        NBTS11n.read(tag, readed);

        System.out.println("Input: " + data);
        System.out.println("Output: " + data);
    }

}
