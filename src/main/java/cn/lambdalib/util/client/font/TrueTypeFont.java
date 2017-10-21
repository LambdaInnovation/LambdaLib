package cn.lambdalib.util.client.font;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL30;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;

/**
 * Created by Paindar on 2017/10/15.
 */
public class TrueTypeFont implements IFont {

    class CachedChar{
        int ch;
        int width;
        int index;
        float u;
        float v;
        CachedChar(int ch,int w,int i,float u,float v){
            this.ch=ch;
            this.width=w;
            this.index=i;
            this.u=u;
            this.v=v;
        }
    }
    public static TrueTypeFont defaultFont = withFallback(Font.PLAIN, 32,
            "Microsoft YaHei", "Adobe Heiti Std R", "STHeiti",
            "SimHei", "微软雅黑", "黑体",
            "Consolas", "Monospace", "Arial");

    static TrueTypeFont withFallback2(int style, int size, String[] fallbackNames){
        return withFallback(style, size, fallbackNames);
    }

    static TrueTypeFont withFallback(int style, int size, String... fallbackNames){
        Font[] allfonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
        List<Font> used=new ArrayList<>();
        for (String c : fallbackNames) {
            for(Font ex:allfonts){
                if(ex.getName().equalsIgnoreCase(c))
                {
                    used.add(ex);
                    break;
                }
            }
        }
        return (used.isEmpty()) ?new TrueTypeFont(new Font(null, style, size)): new TrueTypeFont(new Font(used.get(0).getName(), style, size));
    }
    private static Color BACKGRND_COLOR = new Color(255, 255, 255, 0);


    final Font font;
    final int TEXTURE_SZ_LIMIT = Math.min(2048, GL11.glGetInteger(GL_MAX_TEXTURE_SIZE));
    final int charSize;
    private final float maxPerCol;
    private final float maxStep;
    private java.util.List<Integer> generated =new ArrayList<>();
    private BitSet dirty = new BitSet();
    private Map lookup = new HashMap<Integer, CachedChar>();
    private int step = 0;
    double texStep;


    public TrueTypeFont(Font font){
        this.font=font;
        charSize=(int)(font.getSize() * 1.4);
        maxPerCol = MathHelper.floor(1.0*TEXTURE_SZ_LIMIT / charSize);
        maxStep = maxPerCol * maxPerCol;
        texStep = 1.0 / maxPerCol;
        newTexture();
    }

    private int currentTexture(){
        return (int)generated.get(generated.size() - 1);
    }

    protected Font resolve(int codePoint){
        return font;
    }

    /**
     * Draws the string at the given position with given font option in one line. <br>
     * <p>
     * The string is assumed to not include line-seperate characters. (\n or \r). Violating this yields undefined
     * behaviour.
     *
     * @param str
     * @param px
     * @param y
     * @param option
     */
    @Override
    public void draw(String str, double px, double y, FontOption option)
    {
        double len = getTextWidth(str, option); // Which will call updateCache()
        for(int i=0;i<dirty.size();i++){
            if(dirty.get(i)) {
                glBindTexture(GL_TEXTURE_2D, generated.get(i));
                GL30.glGenerateMipmap(GL_TEXTURE_2D);
            }
        }
        dirty.clear();

        double x = px;
        Tessellator t = Tessellator.getInstance();
        double sz = option.fontSize;
        double scale = option.fontSize / charSize;

        option.color.bind();
        x = px - len * option.align.lenOffset;

        boolean preEnabled = glIsEnabled(GL_ALPHA_TEST);
        int preFunc = glGetInteger(GL_ALPHA_TEST_FUNC);
        float preRef = glGetFloat(GL_ALPHA_TEST_REF);
        glDisable(GL_ALPHA_TEST);
        // glAlphaFunc(GL_GEQUAL, 0.1f)
        glEnable(GL_TEXTURE_2D);
        // TODO group by texture to reduce draw calls?
        for(int i:codePoints(str)){
            CachedChar info = (CachedChar)lookup.get(i);
            float u = info.u;
            float v = info.v;
            glBindTexture(GL_TEXTURE_2D, generated.get(info.index));
            //t.getBuffer().startDrawingQuads();
            //TODO 
//            t.getBuffer().addVertexData(x,      y,      0, u,           v          );
//            t.getBuffer().addVertexData(x,      y + sz, 0, u,           v + texStep);
//            t.getBuffer().addVertexData(x + sz, y + sz, 0, u + texStep, v + texStep);
//            t.getBuffer().addVertexData(x + sz, y,      0, u + texStep, v          );
//            t.draw();

            x += info.width * scale;
        }
        if (preEnabled) {
            glEnable(GL_ALPHA_TEST);
        }
        glAlphaFunc(preFunc, preRef);
    }

    private List<Integer> codePoints(String str){
        List<Integer> list=new ArrayList<>();
        for(int i=0;i<str.length();i++){
            list.add(str.codePointAt(i));
        }
        return list;
    }

    /**
     * Get the width of given character when drawed with given FontOption.
     *
     * @param chr
     * @param option
     */
    @Override
    public double getCharWidth(int chr, FontOption option)
    {
        if(!lookup.containsKey(chr)) {
            writeImage(chr);
        }
        return ((CachedChar)lookup.get(chr)).width * option.fontSize / charSize;
    }

    /**
     * Get the text width that will be drawn if calls the {@link IFont#draw}.
     *
     * @param str
     * @param option
     */
    @Override
    public double getTextWidth(String str, FontOption option)
    {
        updateCache(str);
        double sum=0;
        for(int i:codePoints(str)){
            sum+= ((CachedChar)lookup.get(i)).width;
        }
        return sum * option.fontSize / charSize;
    }



    private void newTexture(){
        int texture = glGenTextures();

        glBindTexture(GL_TEXTURE_2D, texture);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, TEXTURE_SZ_LIMIT, TEXTURE_SZ_LIMIT, 0, GL_RGBA, GL_FLOAT,
                (ByteBuffer)null);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP);
        glTexParameterf(GL_TEXTURE_2D, GL14.GL_TEXTURE_LOD_BIAS, -0.65f);
        glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_MODULATE);

        glBindTexture(GL_TEXTURE_2D, 0);

        generated.add(texture);
        step = 0;
    }

    // Update the cached images to contain the given new characters.
    private void updateCache(String str){
        Set<Integer> newchars = new HashSet<Integer>();

        for(int i:codePoints(str)) {
            if(lookup.containsKey(i))
                newchars.add(i);

        }
        newchars.forEach(this::writeImage);
    }

    // Draw the image into the cached textures at current step position and increment the step by 1.
    private void writeImage(int ch){
        // Create an image holding the character
        BufferedImage image = new BufferedImage(charSize, charSize, BufferedImage.TYPE_INT_ARGB);
        int curtex = currentTexture();

        Graphics2D g = image.createGraphics();
        Font drawFont = resolve(ch);

        g.setFont(drawFont);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        FontMetrics metrics = g.getFontMetrics();
        int width = metrics.charWidth((char) ch);
        // Draw to the image
        g.setBackground(BACKGRND_COLOR);
        g.clearRect(0, 0, charSize, charSize);
        g.setColor(Color.WHITE);

        g.drawString(new java.lang.StringBuilder(2).appendCodePoint(ch).toString(), 3, 1 + metrics.getAscent());

        // Convert awt image to byte buffer
        // Original algorithm credits:
    /*
      * author James Chambers (Jimmy) <br>
      * author Jeremy Adams (elias4444) <br>
      * author Kevin Glass (kevglass) <br>
      * author Peter Korzuszek (genail) <br>
      */

        ByteBuffer byteBuffer;
        DataBuffer db = image.getData().getDataBuffer();
        Byte bpp = (byte) image.getColorModel().getPixelSize();
        if (db instanceof DataBufferInt) {
            int[] intI = ((DataBufferInt)image.getData().getDataBuffer()).getData();
            byte[] newI = new byte[intI.length * 4];
            for(int i=0;i <intI.length;i++) {
                byte[] b = intToByteArray(intI[i]);
                int newIndex = i*4;

                newI[newIndex  ]=b[1];
                newI[newIndex+1]=b[2];
                newI[newIndex+2]=b[3];
                newI[newIndex+3]=b[0];
            }

            byteBuffer = ByteBuffer.allocateDirect(
                    charSize*charSize*(bpp/8))
                    .order(ByteOrder.nativeOrder())
                    .put(newI);
        } else {
            byteBuffer = ByteBuffer.allocateDirect(
                    charSize*charSize*(bpp/8))
                    .order(ByteOrder.nativeOrder())
                    .put(((DataBufferByte)image.getData().getDataBuffer()).getData());
        }
        byteBuffer.flip();

        // write the image to texture
        int rasterX = (int)(step % maxPerCol) * charSize;
        int rasterY = (int)(step / maxPerCol) * charSize;

        glBindTexture(GL_TEXTURE_2D, curtex);
        glTexSubImage2D(GL_TEXTURE_2D, 0, rasterX, rasterY, charSize, charSize, GL_RGBA, GL_UNSIGNED_BYTE, byteBuffer);

        lookup.put(ch, new CachedChar(ch, width, generated.size() - 1, 1.0f*rasterX / TEXTURE_SZ_LIMIT,
                1.0f*rasterY / TEXTURE_SZ_LIMIT));

        step += 1;
        if (step == maxStep) {
            step = 0;
            newTexture();
        }

        dirty.set(generated.size() - 1);

        g.dispose();
    }

    private byte[] intToByteArray(int value){
        byte[] ret = new byte[4];
        ret[0]=(byte) (value >>> 24);
        ret[0]=(byte) (value >>> 16);
        ret[0]=(byte) (value >>> 8);
        ret[0]=(byte) (value      );
        return ret;
    }
}
