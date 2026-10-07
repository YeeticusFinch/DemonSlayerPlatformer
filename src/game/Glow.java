package game;

import java.awt.*;
import java.awt.image.*;

public final class Glow {
    public static final Composite ADD = new AdditiveComposite();

    private static final float SCALE = 0.5f;

    private BufferedImage cap, tmp;
    private ConvolveOp hBlur, vBlur;
    private Graphics2D capG;

    public Glow() {
        float[] k = kernel(9, 2.2f);
        hBlur = new ConvolveOp(new Kernel(9, 1, k), ConvolveOp.EDGE_NO_OP, null);
        vBlur = new ConvolveOp(new Kernel(1, 9, k), ConvolveOp.EDGE_NO_OP, null);
    }

    private static float[] kernel(int size, float sigma) {
        float[] f = new float[size];
        float mid = (size - 1) / 2f, sum = 0;
        for (int i = 0; i < size; i++) {
            float d = i - mid;
            f[i] = (float) Math.exp(-(d * d) / (2 * sigma * sigma));
            sum += f[i];
        }
        for (int i = 0; i < size; i++) f[i] /= sum;
        return f;
    }

    /** Per-frame capture buffer: cleared every call, camera-transformed, so the
     *  blurred glare stays pixel-locked to its sources (no persistence, no trails). */
    public Graphics2D begin(float camX, float camY, int vw, int vh) {
        int cw = Math.max(8, (int) (vw * SCALE)), ch = Math.max(8, (int) (vh * SCALE));
        if (cap == null || cap.getWidth() != cw || cap.getHeight() != ch) {
            cap = new BufferedImage(cw, ch, BufferedImage.TYPE_INT_ARGB);
            tmp = new BufferedImage(cw, ch, BufferedImage.TYPE_INT_ARGB);
        }
        if (capG != null) capG.dispose();
        capG = cap.createGraphics();
        Graphics2D g = capG;
        g.setComposite(AlphaComposite.Clear);
        g.fillRect(0, 0, cw, ch);
        g.setComposite(AlphaComposite.SrcOver);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g.scale(SCALE, SCALE);
        g.translate(-camX, -camY);
        return g;
    }

    public void end(Graphics2D target, int vw, int vh) {
        if (capG != null) { capG.dispose(); capG = null; }
        hBlur.filter(cap, tmp);
        vBlur.filter(tmp, cap);
        Graphics2D tg = (Graphics2D) target.create();
        tg.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        tg.setComposite(ADD);
        tg.drawImage(cap, 0, 0, vw, vh, null);
        tg.drawImage(cap, 0, 0, vw, vh, null);
        tg.dispose();
    }

    public static void blob(Graphics2D g, float x, float y, float r, Color c) {
        float[] dists = {0f, 0.42f, 1f};
        g.setPaint(new RadialGradientPaint(x, y, r, dists,
                new Color[]{new Color(255, 255, 255, Math.min(170, c.getAlpha())), c,
                        new Color(c.getRed(), c.getGreen(), c.getBlue(), 0)}));
        g.fillOval((int) (x - r), (int) (y - r), (int) (r * 2), (int) (r * 2));
    }

    public static void line(Graphics2D g, float x1, float y1, float x2, float y2, float wid, Color c) {
        Stroke old = g.getStroke();
        g.setStroke(new BasicStroke(wid * 3.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), Math.min(90, c.getAlpha())));
        g.drawLine((int) x1, (int) y1, (int) x2, (int) y2);
        g.setStroke(new BasicStroke(wid * 1.55f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(c);
        g.drawLine((int) x1, (int) y1, (int) x2, (int) y2);
        g.setStroke(new BasicStroke(Math.max(1.2f, wid * 0.45f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(new Color(255, 255, 255, Math.min(180, c.getAlpha())));
        g.drawLine((int) x1, (int) y1, (int) x2, (int) y2);
        g.setStroke(old);
    }

    private static final class AdditiveComposite implements Composite {
        @Override
        public CompositeContext createContext(ColorModel srcCM, ColorModel dstCM, RenderingHints hints) {
            return new Ctx();
        }
    }

    private static final class Ctx implements CompositeContext {
        private int[] srcPix = new int[4096], dstPix = new int[4096];

        @Override
        public void compose(Raster src, Raster dstIn, WritableRaster dstOut) {
            int w = Math.min(Math.min(src.getWidth(), dstIn.getWidth()), dstOut.getWidth());
            int h = Math.min(Math.min(src.getHeight(), dstIn.getHeight()), dstOut.getHeight());
            int[] s = w > srcPix.length ? new int[w] : srcPix;
            int[] d = w > dstPix.length ? new int[w] : dstPix;
            for (int y = 0; y < h; y++) {
                src.getDataElements(0, y, w, 1, s);
                dstIn.getDataElements(0, y, w, 1, d);
                for (int x = 0; x < w; x++) {
                    int sp = s[x];
                    int sa = sp >>> 24;
                    if (sa == 0) continue;
                    int dp = d[x], da = dp >>> 24;
                    int sr = sp >> 16 & 0xFF, sg = sp >> 8 & 0xFF, sb = sp & 0xFF;
                    int dr = dp >> 16 & 0xFF, dg = dp >> 8 & 0xFF, db = dp & 0xFF;
                    dr = Math.min(255, dr + (sr * sa >> 8));
                    dg = Math.min(255, dg + (sg * sa >> 8));
                    db = Math.min(255, db + (sb * sa >> 8));
                    d[x] = (Math.min(255, da + sa) << 24) | (dr << 16) | (dg << 8) | db;
                }
                dstOut.setDataElements(0, y, w, 1, d);
            }
        }

        @Override
        public void dispose() {
        }
    }
}
