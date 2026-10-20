package game;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Svg {
    public static final class Group {
        public final String name;
        public final BufferedImage img;
        public final float pivotX, pivotY;

        Group(String name, BufferedImage img, float pivotX, float pivotY) {
            this.name = name;
            this.img = img;
            this.pivotX = pivotX;
            this.pivotY = pivotY;
        }
    }

    public float w = 64, h = 96;
    private final ArrayList<Group> groups = new ArrayList<>();

    private static final Pattern TAG = Pattern.compile("</?([a-zA-Z][-a-zA-Z]*)([^>]*)>");
    private static final Pattern ATTR = Pattern.compile("([a-zA-Z:-]+)\\s*=\\s*\"([^\"]*)\"");
    private static final Pattern TOKEN = Pattern.compile("[MmLlHhVvCcSsQqTtZzAa]|-?\\d+(?:\\.\\d+)?(?:[eE][-+]?\\d+)?");
    private static final Pattern FUNC = Pattern.compile("(\\w+)\\s*\\(([^)]*)\\)");
    private static final Pattern NUM = Pattern.compile("-?\\d+(?:\\.\\d+)?(?:[eE][-+]?\\d+)?");
    private static final Pattern STYLE = Pattern.compile("([a-zA-Z-]+)\\s*:\\s*([^;]+)");
    private static final Pattern STYLE_BANG = Pattern.compile("stroke:none", Pattern.CASE_INSENSITIVE);


    private static class Item {
        Shape shape;
        Color fill, stroke;
        float sw = 1, alpha = 1;
    }

    private static class Ctx {
        String name;
        AffineTransform at = new AffineTransform();
        ArrayList<Item> items = new ArrayList<>();
        float centerX = Float.NaN, centerY = Float.NaN;
    }

    public static Svg load(String path) {
        try {
            return new Svg(new String(Files.readAllBytes(Paths.get(path)), java.nio.charset.StandardCharsets.UTF_8));
        } catch (Exception e) {
            return null;
        }
    }

    private Svg(String text) {
        parse(text);
    }

    public Group group(String name) {
        for (Group g : groups) if (g.name.equals(name)) return g;
        return null;
    }

    private void parse(String text) {
        ArrayList<Ctx> stack = new ArrayList<>();
        Ctx root = new Ctx();
        root.name = "base";
        stack.add(root);
        ArrayList<Ctx> done = new ArrayList<>();
        Matcher m = TAG.matcher(text);
        while (m.find()) {
            boolean closing = text.charAt(m.start() + 1) == '/';
            String tag = m.group(1);
            if (closing) {
                if (stack.size() > 1) done.add(stack.remove(stack.size() - 1));
                continue;
            }
            java.util.HashMap<String, String> a = new java.util.HashMap<>();
            Matcher am = ATTR.matcher(m.group(2));
            while (am.find()) a.put(am.group(1), am.group(2));
            String style = a.get("style");
            if (style != null) {
                Matcher sm = STYLE.matcher(style);
                while (sm.find()) a.put(sm.group(1), sm.group(2).trim());
            }
            if (tag.equalsIgnoreCase("svg")) {
                String vb = a.get("viewBox");
                if (vb != null) {
                    float[] n = nums(vb);
                    if (n.length >= 4) {
                        w = n[2];
                        h = n[3];
                    }
                }
                continue;
            }
            if (tag.equalsIgnoreCase("g")) {
                Ctx c = new Ctx();
                c.name = a.getOrDefault("inkscape:label", a.getOrDefault("id", "group"));
                if (a.containsKey("inkscape:transform-center-x"))
                    c.centerX = Float.parseFloat(a.get("inkscape:transform-center-x"));
                if (a.containsKey("inkscape:transform-center-y"))
                    c.centerY = Float.parseFloat(a.get("inkscape:transform-center-y"));
                c.at = stack.get(stack.size() - 1).at;
                String tr = a.get("transform");
                if (tr != null) {
                    AffineTransform t = transform(tr);
                    t.preConcatenate(c.at);
                    c.at = t;
                }
                stack.add(c);
                continue;
            }
            Shape s = shape(tag, a);
            if (s == null) continue;
            Item it = new Item();
            Path2D.Float p = new Path2D.Float(s);
            String elementTransform = a.get("transform");
            if (elementTransform != null) p.transform(transform(elementTransform));
            p.transform(stack.get(stack.size() - 1).at);
            it.fill = color(a.get("fill"));
            it.stroke = color(a.get("stroke"));
            it.alpha = op(a.get("opacity")) * op(a.get("fill-opacity"));
            String sws = a.get("stroke-width");
            if (sws != null) it.sw = Float.parseFloat(sws);
            for (int i = 1; i < stack.size(); i++) {
                Item part = new Item();
                part.shape = p;
                part.fill = it.fill;
                part.stroke = it.stroke;
                part.sw = it.sw;
                part.alpha = it.alpha;
                stack.get(i).items.add(part);
            }
        }
        while (stack.size() > 1) done.add(stack.remove(stack.size() - 1));
        done.add(root);
        for (Ctx c : done) if (!c.items.isEmpty()) groups.add(rasterize(c));
    }

    private Group rasterize(Ctx c) {
        float s = 2f;
        BufferedImage img = new BufferedImage(Math.max(1, Math.round(w * s)), Math.max(1, Math.round(h * s)),
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g.scale(s, s);
        Rectangle bounds = null;
        for (Item it : c.items) {
            Rectangle b = it.shape.getBounds();
            if (bounds == null) bounds = new Rectangle(b);
            else bounds.add(b);
            if (it.fill != null) {
                g.setColor(alpha(it.fill, it.alpha));
                g.fill(it.shape);
            }
            if (it.stroke != null) {
                g.setStroke(new BasicStroke(it.sw, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.setColor(alpha(it.stroke, it.alpha));
                g.draw(it.shape);
            }
        }
        g.dispose();
        float pivotX = bounds == null ? w / 2f : (float) bounds.getCenterX();
        float pivotY = bounds == null ? h / 2f : (float) bounds.getCenterY();
        if (!Float.isNaN(c.centerX)) pivotX += c.centerX;
        if (!Float.isNaN(c.centerY)) pivotY -= c.centerY;
        return new Group(c.name, img, pivotX, pivotY);
    }

    private static Shape shape(String tag, java.util.HashMap<String, String> a) {
        switch (tag) {
            case "rect": {
                float x = f(a.get("x")), y = f(a.get("y")), wd = f(a.get("width")), ht = f(a.get("height"));
                String rx = a.get("rx");
                if (rx != null) {
                    float r = Float.parseFloat(rx);
                    return new RoundRectangle2D.Float(x, y, wd, ht, r * 2, r * 2);
                }
                Path2D.Float p = new Path2D.Float();
                p.moveTo(x, y);
                p.lineTo(x + wd, y);
                p.lineTo(x + wd, y + ht);
                p.lineTo(x, y + ht);
                p.closePath();
                return p;
            }
            case "circle": {
                float cx = f(a.get("cx")), cy = f(a.get("cy")), r = f(a.get("r"));
                return new Ellipse2D.Float(cx - r, cy - r, r * 2, r * 2);
            }
            case "ellipse": {
                float cx = f(a.get("cx")), cy = f(a.get("cy")), rx = f(a.get("rx")), ry = f(a.get("ry"));
                return new Ellipse2D.Float(cx - rx, cy - ry, rx * 2, ry * 2);
            }
            case "polygon": {
                float[] n = nums(a.get("points"));
                Path2D.Float p = new Path2D.Float();
                for (int i = 0; i + 1 < n.length; i += 2) {
                    if (i == 0) p.moveTo(n[0], n[1]);
                    else p.lineTo(n[i], n[i + 1]);
                }
                p.closePath();
                return p;
            }
            case "path": {
                String d = a.get("d");
                return d == null ? null : parsePath(d);
            }
            default:
                return null;
        }
    }

    private static float f(String v) {
        return v == null ? 0 : Float.parseFloat(v);
    }

    private static float op(String v) {
        if (v == null) return 1;
        try {
            return Float.parseFloat(v);
        } catch (Exception e) {
            return 1;
        }
    }

    private static Color alpha(Color c, float a) {
        return a >= 0.999f ? c : new Color(c.getRed(), c.getGreen(), c.getBlue(), Math.round(c.getAlpha() * Math.max(0, Math.min(1, a))));
    }

    private static float[] nums(String v) {
        ArrayList<Float> l = new ArrayList<>();
        Matcher m = NUM.matcher(v);
        while (m.find()) l.add(Float.parseFloat(m.group()));
        float[] out = new float[l.size()];
        for (int i = 0; i < out.length; i++) out[i] = l.get(i);
        return out;
    }

    private static Color color(String v) {
        if (v == null || v.equals("none")) return null;
        v = v.trim();
        try {
            if (v.startsWith("#")) {
                if (v.length() == 4) {
                    int r = Integer.parseInt(v.substring(1, 2), 16) * 17;
                    int g = Integer.parseInt(v.substring(2, 3), 16) * 17;
                    int b = Integer.parseInt(v.substring(3, 4), 16) * 17;
                    return new Color(r, g, b);
                }
                return new Color(Integer.parseInt(v.substring(1), 16));
            }
            return switch (v) {
                case "white" -> Color.WHITE;
                case "black" -> Color.BLACK;
                case "red" -> Color.RED;
                default -> null;
            };
        } catch (Exception e) {
            return null;
        }
    }

    private static AffineTransform transform(String v) {
        AffineTransform at = new AffineTransform();
        Matcher m = FUNC.matcher(v);
        while (m.find()) {
            float[] n = nums(m.group(2));
            AffineTransform t = new AffineTransform();
            switch (m.group(1)) {
                case "translate" -> t.translate(n[0], n.length > 1 ? n[1] : 0);
                case "scale" -> t.scale(n[0], n.length > 1 ? n[1] : n[0]);
                case "rotate" -> t.rotate(Math.toRadians(n[0]));
                case "matrix" -> t.setTransform(n[0], n[1], n[2], n[3], n[4], n[5]);
            }
            at.concatenate(t);
        }
        return at;
    }

    private static Path2D.Float parsePath(String d) {
        Path2D.Float p = new Path2D.Float();
        Matcher m = TOKEN.matcher(d);
        char cmd = 0;
        ArrayList<Float> a = new ArrayList<>();
        float[] st = new float[8];
        while (m.find()) {
            String t = m.group();
            if (t.length() == 1 && "MmLlHhVvCcSsQqTtZzAa".indexOf(t.charAt(0)) >= 0) {
                seg(p, cmd, a, st);
                cmd = t.charAt(0);
                a.clear();
            } else if (cmd != 0) {
                a.add(Float.parseFloat(t));
            }
        }
        seg(p, cmd, a, st);
        return p;
    }

    private static void seg(Path2D.Float p, char cmd, ArrayList<Float> a, float[] st) {
        int i = 0;
        char c = cmd;
        while (i < a.size() || c == 'Z' || c == 'z') {
            float x = st[0], y = st[1];
            boolean rel = Character.isLowerCase(c);
            switch (Character.toUpperCase(c)) {
                case 'M': {
                    float nx = rel ? x + a.get(i) : a.get(i);
                    float ny = rel ? y + a.get(i + 1) : a.get(i + 1);
                    i += 2;
                    p.moveTo(nx, ny);
                    st[2] = st[0] = nx;
                    st[3] = st[1] = ny;
                    c = rel ? 'l' : 'L';
                    break;
                }
                case 'L': {
                    float nx = rel ? x + a.get(i) : a.get(i);
                    float ny = rel ? y + a.get(i + 1) : a.get(i + 1);
                    i += 2;
                    p.lineTo(nx, ny);
                    st[0] = nx;
                    st[1] = ny;
                    break;
                }
                case 'H': {
                    float nx = rel ? x + a.get(i) : a.get(i);
                    i++;
                    p.lineTo(nx, y);
                    st[0] = nx;
                    break;
                }
                case 'V': {
                    float ny = rel ? y + a.get(i) : a.get(i);
                    i++;
                    p.lineTo(x, ny);
                    st[1] = ny;
                    break;
                }
                case 'C': {
                    float x1 = rel ? x + a.get(i) : a.get(i);
                    float y1 = rel ? y + a.get(i + 1) : a.get(i + 1);
                    float x2 = rel ? x + a.get(i + 2) : a.get(i + 2);
                    float y2 = rel ? y + a.get(i + 3) : a.get(i + 3);
                    float x3 = rel ? x + a.get(i + 4) : a.get(i + 4);
                    float y3 = rel ? y + a.get(i + 5) : a.get(i + 5);
                    i += 6;
                    p.curveTo(x1, y1, x2, y2, x3, y3);
                    st[6] = x2;
                    st[7] = y2;
                    st[0] = x3;
                    st[1] = y3;
                    break;
                }
                case 'S': {
                    float x1 = 2 * x - st[6], y1 = 2 * y - st[7];
                    float x2 = rel ? x + a.get(i) : a.get(i);
                    float y2 = rel ? y + a.get(i + 1) : a.get(i + 1);
                    float x3 = rel ? x + a.get(i + 2) : a.get(i + 2);
                    float y3 = rel ? y + a.get(i + 3) : a.get(i + 3);
                    i += 4;
                    p.curveTo(x1, y1, x2, y2, x3, y3);
                    st[6] = x2;
                    st[7] = y2;
                    st[0] = x3;
                    st[1] = y3;
                    break;
                }
                case 'Q': {
                    float x1 = rel ? x + a.get(i) : a.get(i);
                    float y1 = rel ? y + a.get(i + 1) : a.get(i + 1);
                    float x2 = rel ? x + a.get(i + 2) : a.get(i + 2);
                    float y2 = rel ? y + a.get(i + 3) : a.get(i + 3);
                    i += 4;
                    p.quadTo(x1, y1, x2, y2);
                    st[4] = x1;
                    st[5] = y1;
                    st[0] = x2;
                    st[1] = y2;
                    break;
                }
                case 'T': {
                    float x1 = 2 * x - st[4], y1 = 2 * y - st[5];
                    float x2 = rel ? x + a.get(i) : a.get(i);
                    float y2 = rel ? y + a.get(i + 1) : a.get(i + 1);
                    i += 2;
                    p.quadTo(x1, y1, x2, y2);
                    st[4] = x1;
                    st[5] = y1;
                    st[0] = x2;
                    st[1] = y2;
                    break;
                }
                case 'A': {
                    float rx = Math.abs(a.get(i));
                    float ry = Math.abs(a.get(i + 1));
                    float rotation = a.get(i + 2);
                    boolean large = a.get(i + 3) != 0;
                    boolean sweep = a.get(i + 4) != 0;
                    float x2 = rel ? x + a.get(i + 5) : a.get(i + 5);
                    float y2 = rel ? y + a.get(i + 6) : a.get(i + 6);
                    i += 7;
                    arcTo(p, x, y, rx, ry, rotation, large, sweep, x2, y2);
                    st[0] = x2;
                    st[1] = y2;
                    break;
                }
                case 'Z': {
                    p.closePath();
                    st[0] = st[2];
                    st[1] = st[3];
                    return;
                }
                default:
                    return;
            }
            if (c == 'Z' || c == 'z') break;
        }
    }

    private static void arcTo(Path2D.Float p, double x1, double y1, double rx, double ry,
                              double rotation, boolean large, boolean sweep, double x2, double y2) {
        if (rx == 0 || ry == 0 || (x1 == x2 && y1 == y2)) {
            p.lineTo((float) x2, (float) y2);
            return;
        }
        double phi = Math.toRadians(rotation % 360);
        double cos = Math.cos(phi), sin = Math.sin(phi);
        double dx = (x1 - x2) / 2, dy = (y1 - y2) / 2;
        double xp = cos * dx + sin * dy;
        double yp = -sin * dx + cos * dy;
        double scale = xp * xp / (rx * rx) + yp * yp / (ry * ry);
        if (scale > 1) {
            double factor = Math.sqrt(scale);
            rx *= factor;
            ry *= factor;
        }
        double numerator = Math.max(0, rx * rx * ry * ry - rx * rx * yp * yp - ry * ry * xp * xp);
        double denominator = rx * rx * yp * yp + ry * ry * xp * xp;
        double sign = large == sweep ? -1 : 1;
        double coef = denominator == 0 ? 0 : sign * Math.sqrt(numerator / denominator);
        double cxp = coef * rx * yp / ry;
        double cyp = -coef * ry * xp / rx;
        double cx = cos * cxp - sin * cyp + (x1 + x2) / 2;
        double cy = sin * cxp + cos * cyp + (y1 + y2) / 2;
        double ux = (xp - cxp) / rx, uy = (yp - cyp) / ry;
        double vx = (-xp - cxp) / rx, vy = (-yp - cyp) / ry;
        double start = Math.atan2(uy, ux);
        double delta = Math.atan2(ux * vy - uy * vx, ux * vx + uy * vy);
        if (!sweep && delta > 0) delta -= Math.PI * 2;
        if (sweep && delta < 0) delta += Math.PI * 2;
        int segments = Math.max(1, (int) Math.ceil(Math.abs(delta) / (Math.PI / 2)));
        double step = delta / segments;
        for (int i = 0; i < segments; i++) {
            double a0 = start + i * step;
            double a1 = a0 + step;
            double k = 4.0 / 3.0 * Math.tan((a1 - a0) / 4.0);
            double cos0 = Math.cos(a0), sin0 = Math.sin(a0);
            double cos1 = Math.cos(a1), sin1 = Math.sin(a1);
            double p1x = rx * (cos0 - k * sin0), p1y = ry * (sin0 + k * cos0);
            double p2x = rx * (cos1 + k * sin1), p2y = ry * (sin1 - k * cos1);
            double p3x = rx * cos1, p3y = ry * sin1;
            p.curveTo(
                    (float) (cx + cos * p1x - sin * p1y), (float) (cy + sin * p1x + cos * p1y),
                    (float) (cx + cos * p2x - sin * p2y), (float) (cy + sin * p2x + cos * p2y),
                    (float) (cx + cos * p3x - sin * p3y), (float) (cy + sin * p3x + cos * p3y));
        }
    }
}
