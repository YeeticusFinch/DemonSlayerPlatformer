package game;

import java.awt.*;

final class BackgroundRenderer {

    static void render(Graphics2D g, World w, float camX, float camY, int vw, int vh) {
        Level l = w.level;
        if (l.underwater) {
            underwater(g, w, camX, camY, vw, vh);
            return;
        }
        boolean day = l.daytime;
        int seed = l.name.hashCode();

        Color skyTop = switch (l.theme) {
            case MANSION -> day ? new Color(150, 190, 225) : new Color(20, 24, 48);
            case CITY -> day ? new Color(150, 185, 210) : new Color(14, 16, 34);
            case DOJO -> new Color(42, 28, 18);
            default -> day ? new Color(110, 180, 220) : new Color(13, 16, 38);
        };
        Color skyBot = switch (l.theme) {
            case VILLAGE -> day ? new Color(240, 224, 190) : new Color(64, 44, 78);
            case MANSION -> new Color(60, 50, 42);
            case CITY -> day ? new Color(220, 205, 185) : new Color(48, 42, 68);
            case DOJO -> new Color(92, 62, 36);
            case SHRINE -> new Color(96, 34, 52);
            default -> day ? new Color(205, 228, 235) : new Color(52, 42, 88);
        };
        GradientPaint sky = new GradientPaint(0, 0, skyTop, 0, vh, skyBot);
        g.setPaint(sky);
        g.fillRect(0, 0, vw, vh);

        if (l.theme == Level.Theme.MANSION) {
            interior(g, w, camX, camY, vw, vh, day);
            return;
        }
        if (l.theme == Level.Theme.DOJO) {
            dojoInterior(g, camX, vw, vh);
            return;
        }

        if (!day) stars(g, camX, camY, vw, vh, seed);
        if (day) sun(g, w, camX, vw, vh);
        else moon(g, l, camX, vw, vh);

        clouds(g, w, camX, vw, vh, day, seed);

        float horizon = vh * 0.62f - camY * 0.1f;
        ridge(g, w, camX, horizon - 130, 90, seed + 11, 0.15f, vw,
                day ? new Color(126, 168, 178) : new Color(26, 33, 66));
        ridge(g, w, camX, horizon - 55, 70, seed + 29, 0.28f, vw,
                day ? new Color(92, 134, 146) : new Color(19, 25, 52));
        if (l.theme == Level.Theme.CITY) cityRow(g, w, camX, horizon + 80, vw, day, seed);
        else if (l.theme == Level.Theme.VILLAGE) villageRow(g, w, camX, horizon + 40, vw, day, seed);
        else treeLine(g, w, camX, horizon + 30, vw, day, seed);

        g.setPaint(new GradientPaint(0, vh - 160, new Color(255, 255, 255, 0), 0, vh,
                day ? new Color(230, 240, 245, 70) : new Color(30, 40, 80, 80)));
        g.fillRect(0, vh - 160, vw, 160);

        if (!day && (l.theme == Level.Theme.FOREST || l.theme == Level.Theme.SHRINE)) fireflies(g, w, camX, camY, vw, vh, seed);
    }

    static void worldDecoGlows(Graphics2D gg, World w) {
    }

    static void screenCelestialGlow(Graphics2D g, World w, float camX, float camY, int vw, int vh) {
        Level l = w.level;
        Composite old = g.getComposite();
        g.setComposite(Glow.ADD);
        if (l.theme == Level.Theme.MANSION || l.theme == Level.Theme.DOJO) {
            float p = 0.3f;
            int start = (int) Math.floor((camX * p - 200) / 260f), end = (int) Math.ceil((camX * p + vw + 200) / 260f);
            for (int i = start; i <= end; i++)
                Glow.blob(g, i * 260f - camX * p + 190, 87, 26, new Color(255, 210, 130, 200));
        } else if (l.daytime) {
            Glow.blob(g, 1180 - camX * 0.05f, 90 - camY * 0.04f, 110, new Color(255, 240, 170, 140));
        } else {
            float mx = 1060 - camX * 0.05f, my = 110 - camY * 0.04f;
            Glow.blob(g, mx, my, l.theme == Level.Theme.SHRINE ? 170 : 90,
                    l.theme == Level.Theme.SHRINE ? new Color(255, 90, 90, 150) : new Color(210, 220, 255, 140));
        }
        g.setComposite(old);
    }

    private static void stars(Graphics2D g, float camX, float camY, int vw, int vh, int seed) {
        for (int i = 0; i < 90; i++) {
            float h1 = frac(seed * 0.017f + i * 0.613f), h2 = frac(i * 0.379f + seed * 0.003f);
            float x = ((h1 * (vw + 600)) - camX * 0.06f % (vw + 600) + vw + 600) % (vw + 600) - 300;
            float y = h2 * vh * 0.65f;
            float tw = 0.5f + 0.5f * FMath.sin(Game.time * (2 + frac(i * 7.7f) * 3) + i);
            int a = (int) (90 + 140 * tw);
            g.setColor(new Color(220, 228, 255, a));
            int s = frac(i * 3.1f) > 0.85f ? 2 : 1;
            g.fillRect((int) x, (int) y, s, s);
        }
    }

    private static void sun(Graphics2D g, World w, float camX, int vw, int vh) {
        float sx = 1180 - camX * 0.05f, sy = 90;
        g.setColor(new Color(255, 246, 200));
        g.fillOval((int) sx - 36, (int) sy - 36, 72, 72);
        g.setColor(new Color(255, 250, 220, 90));
        g.fillOval((int) sx - 52, (int) sy - 52, 104, 104);
    }

    private static void moon(Graphics2D g, Level l, float camX, int vw, int vh) {
        float mx = 1060 - camX * 0.05f, my = 110;
        boolean red = l.theme == Level.Theme.SHRINE;
        g.setColor(red ? new Color(255, 120, 110, 60) : new Color(200, 214, 255, 45));
        g.fillOval((int) mx - 74, (int) my - 74, 148, 148);
        g.setColor(red ? new Color(255, 128, 116) : new Color(228, 234, 252));
        g.fillOval((int) mx - 42, (int) my - 42, 84, 84);
        g.setColor(red ? new Color(232, 100, 92) : new Color(198, 206, 236));
        g.fillOval((int) mx - 26, (int) my - 18, 22, 18);
        g.fillOval((int) mx + 8, (int) my + 6, 14, 12);
        g.fillOval((int) mx - 8, (int) my + 16, 10, 8);
    }

    private static void clouds(Graphics2D g, World w, float camX, int vw, int vh, boolean day, int seed) {
        for (int i = 0; i < 7; i++) {
            float speed = 12 + frac(i * 5.3f) * 10;
            float span = vw + 700;
            float x = (float) (((frac(i * 0.71f + seed) * span) + Game.time * speed - camX * 0.08) % span + span) % span - 350;
            float y = 40 + frac(i * 2.9f) * vh * 0.35f;
            int alpha = day ? 150 : 40;
            g.setColor(day ? new Color(255, 255, 255, alpha) : new Color(90, 100, 160, alpha));
            cloudBlob(g, x, y, 46 + frac(i * 3.7f) * 50);
        }
    }

    private static void cloudBlob(Graphics2D g, float x, float y, float r) {
        g.fillOval((int) (x - r), (int) (y - r * 0.4f), (int) (r * 2), (int) (r * 0.8f));
        g.fillOval((int) (x - r * 0.55f), (int) (y - r * 0.75f), (int) (r * 1.2f), (int) (r * 0.9f));
        g.fillOval((int) (x - r * 0.1f), (int) (y - r * 0.55f), (int) (r * 1.1f), (int) (r * 0.75f));
    }

    private static void ridge(Graphics2D g, World w, float camX, float baseY, float amp, int seed, float parallax, int vw, Color c) {
        g.setColor(c);
        Polygon poly = new Polygon();
        int step = 24;
        for (int sxp = -step; sxp <= vw + step; sxp += step) {
            float wx = sxp + camX * parallax;
            float hgt = noise(wx * 0.004f + seed) * amp + noise(wx * 0.013f + seed * 2) * amp * 0.4f;
            poly.addPoint(sxp, (int) (baseY - hgt));
        }
        poly.addPoint(vw + step, (int) baseY + 400);
        poly.addPoint(-step, (int) baseY + 400);
        g.fillPolygon(poly);
    }

    private static void treeLine(Graphics2D g, World w, float camX, float baseY, int vw, boolean day, int seed) {
        g.setColor(day ? new Color(56, 92, 70) : new Color(14, 22, 34));
        float p = 0.45f;
        int start = (int) Math.floor((camX * p - 100) / 90f), end = (int) Math.ceil((camX * p + vw + 100) / 90f);
        for (int i = start; i <= end; i++) {
            float x = i * 90f - camX * p;
            float hgt = 70 + frac(i * 12.9898f + seed) * 80;
            float wd = 40 + frac(i * 7.13f) * 26;
            pine(g, x, baseY + 60, wd, hgt);
        }
        g.setColor(day ? new Color(44, 76, 58) : new Color(10, 16, 27));
        g.fillRect(0, (int) baseY + 52, vw, 400);
    }

    private static void villageRow(Graphics2D g, World w, float camX, float baseY, int vw, boolean day, int seed) {
        float p = 0.45f;
        int start = (int) Math.floor((camX * p - 160) / 170f), end = (int) Math.ceil((camX * p + vw + 160) / 170f);
        for (int i = start; i <= end; i++) {
            float x = i * 170f - camX * p;
            float hh = 60 + frac(i * 3.31f + seed) * 46;
            g.setColor(day ? new Color(96, 88, 82) : new Color(22, 20, 34));
            g.fillRect((int) (x), (int) (baseY - hh), 130, (int) hh + 300);
            Polygon roof = new Polygon();
            roof.addPoint((int) (x - 12), (int) (baseY - hh));
            roof.addPoint((int) (x + 65), (int) (baseY - hh - 34));
            roof.addPoint((int) (x + 142), (int) (baseY - hh));
            g.setColor(day ? new Color(70, 62, 66) : new Color(15, 13, 24));
            g.fillPolygon(roof);
            if (!day) {
                for (int wi = 0; wi < 2; wi++)
                    if (frac(i * 5.77f + wi) > 0.3f) {
                        g.setColor(new Color(255, 196, 110, 220));
                        g.fillRect((int) (x + 22 + wi * 54), (int) (baseY - hh + 22), 18, 22);
                    }
            }
        }
    }

    private static void fireflies(Graphics2D g, World w, float camX, float camY, int vw, int vh, int seed) {
        for (int i = 0; i < 26; i++) {
            float px = frac(i * 0.753f) * (vw + 200) - 100;
            float py = frac(i * 0.417f) * vh;
            float fx = px + FMath.sin(Game.time * 0.7f + i * 2.1f) * 26;
            float fy = py + FMath.cos(Game.time * 0.5f + i) * 20;
            float twil = 0.4f + 0.6f * FMath.sin(Game.time * 3 + i * 1.7f);
            g.setColor(new Color(200, 255, 130, (int) (150 * Math.max(0, twil))));
            g.fillOval((int) fx, (int) fy, 3, 3);
        }
    }

    private static void interior(Graphics2D g, World w, float camX, float camY, int vw, int vh, boolean day) {
        g.setColor(new Color(122, 88, 60));
        g.fillRect(0, 0, vw, vh);
        float p = 0.3f;
        int start = (int) Math.floor((camX * p - 200) / 260f), end = (int) Math.ceil((camX * p + vw + 200) / 260f);
        for (int i = start; i <= end; i++) {
            float x = i * 260f - camX * p;
            g.setColor(new Color(104, 74, 50));
            g.fillRect((int) x, 0, 12, vh);
            boolean window = frac(i * 4.37f) > 0.55f;
            if (window) {
                g.setColor(day ? new Color(185, 220, 240) : new Color(30, 36, 70));
                g.fillRect((int) (x + 60), 60, 120, 200);
                g.setColor(new Color(70, 50, 34));
                g.setStroke(new BasicStroke(8f));
                g.drawRect((int) (x + 60), 60, 120, 200);
                g.drawLine((int) (x + 120), 60, (int) (x + 120), 260);
                if (day) {
                    g.setColor(new Color(255, 250, 220, 60));
                    g.fillRect((int) (x + 60), 60, 120, 200);
                }
            }
            float lampX = x + 190;
            g.setColor(new Color(60, 42, 28));
            g.drawLine((int) lampX, 0, (int) lampX, 70);
            g.setColor(new Color(240, 200, 130));
            g.fillRoundRect((int) lampX - 14, 70, 28, 34, 8, 8);
        }
        g.setPaint(new GradientPaint(0, 0, new Color(0, 0, 0, 60), 0, vh, new Color(0, 0, 0, 130)));
        g.fillRect(0, 0, vw, vh);
    }

    private static void dojoInterior(Graphics2D g, float camX, int vw, int vh) {
        g.setColor(new Color(92, 62, 36));
        g.fillRect(0, 0, vw, vh);
        g.setPaint(new GradientPaint(0, 0, new Color(28, 20, 16), 0, vh, new Color(126, 88, 54)));
        g.fillRect(0, 0, vw, vh);
        float p = 0.28f;
        int start = (int) Math.floor((camX * p - 220) / 220f), end = (int) Math.ceil((camX * p + vw + 220) / 220f);
        for (int i = start; i <= end; i++) {
            float x = i * 220f - camX * p;
            g.setColor(new Color(64, 38, 24));
            g.fillRect((int) x, 0, 14, vh);
            g.setColor(new Color(206, 182, 132));
            g.fillRect((int) (x + 38), 115, 132, 230);
            g.setColor(new Color(72, 48, 32));
            g.setStroke(new BasicStroke(6f));
            g.drawRect((int) (x + 38), 115, 132, 230);
            g.drawLine((int) (x + 104), 115, (int) (x + 104), 345);
            g.drawLine((int) (x + 38), 230, (int) (x + 170), 230);
            g.setColor(new Color(170, 42, 44));
            g.fillRect((int) (x + 76), 380, 58, 82);
            g.setColor(new Color(250, 245, 230));
            g.fillOval((int) (x + 92), 402, 26, 26);
            float lampX = x + 190;
            g.setColor(new Color(36, 24, 18));
            g.drawLine((int) lampX, 0, (int) lampX, 62);
            g.setColor(new Color(244, 196, 112));
            g.fillRoundRect((int) lampX - 14, 62, 28, 34, 9, 9);
        }
        g.setColor(new Color(72, 46, 28));
        for (int y = 70; y < vh; y += 92) g.fillRect(0, y, vw, 7);
        g.setPaint(new GradientPaint(0, 0, new Color(0, 0, 0, 105), 0, vh, new Color(0, 0, 0, 25)));
        g.fillRect(0, 0, vw, vh);
    }

    private static void underwater(Graphics2D g, World w, float camX, float camY, int vw, int vh) {
        g.setPaint(new GradientPaint(0, 0, new Color(8, 58, 70), 0, vh, new Color(1, 12, 20)));
        g.fillRect(0, 0, vw, vh);
        float p = 0.22f;
        int start = (int) Math.floor((camX * p - 120) / 180f), end = (int) Math.ceil((camX * p + vw + 120) / 180f);
        for (int i = start; i <= end; i++) {
            float x = i * 180f - camX * p;
            float base = vh * 0.74f - camY * 0.05f + FMath.sin(i * 1.9f) * 24;
            g.setColor(new Color(5, 28, 26, 150));
            g.fillPolygon(new int[]{(int) x - 80, (int) x, (int) x + 80}, new int[]{vh, (int) base, vh}, 3);
            g.setColor(new Color(18, 78, 62, 95));
            for (int j = 0; j < 4; j++)
                g.drawArc((int) x - 30 + j * 15, (int) base - 20 - j * 8, 36, 80, 80, 70);
        }
        g.setColor(new Color(160, 230, 220, 34));
        for (int i = 0; i < 16; i++) {
            float x = ((frac(i * 0.37f) * (vw + 200)) - camX * 0.04f + Game.time * (6 + i % 4)) % (vw + 200) - 100;
            float y = frac(i * 0.61f) * vh;
            g.drawLine((int) x, (int) y, (int) x + 40 + i % 5 * 14, (int) y - 16);
        }
    }

    private static void cityRow(Graphics2D g, World w, float camX, float baseY, int vw, boolean day, int seed) {
        float p = 0.42f;
        int start = (int) Math.floor((camX * p - 180) / 150f), end = (int) Math.ceil((camX * p + vw + 180) / 150f);
        for (int i = start; i <= end; i++) {
            float x = i * 150f - camX * p;
            float hh = 150 + frac(i * 3.31f + seed) * 130;
            g.setColor(day ? new Color(130, 122, 114) : new Color(24, 24, 40));
            g.fillRect((int) x, (int) (baseY - hh), 124, (int) hh + 300);
            g.setColor(day ? new Color(98, 88, 82) : new Color(14, 14, 28));
            g.fillRect((int) x - 6, (int) (baseY - hh - 8), 136, 10);
            for (int row = 0; row < 4; row++) for (int col = 0; col < 3; col++) {
                if (frac(i * 5.77f + row * 1.3f + col) > (day ? 0.7f : 0.28f)) {
                    g.setColor(day ? new Color(60, 70, 82) : new Color(255, 205, 116, 210));
                    g.fillRect((int) x + 18 + col * 34, (int) (baseY - hh + 28 + row * 35), 18, 20);
                }
            }
        }
        g.setColor(day ? new Color(70, 70, 72) : new Color(18, 18, 24));
        g.fillRect(0, (int) baseY + 48, vw, 260);
        g.setColor(new Color(220, 220, 190, day ? 90 : 55));
        for (int x = -80; x < vw + 80; x += 140) g.fillRect(x, (int) baseY + 102, 70, 5);
    }

    private static void pine(Graphics2D g, float x, float baseY, float wd, float hgt) {
        Polygon tri = new Polygon();
        tri.addPoint((int) (x - wd / 2), (int) baseY);
        tri.addPoint((int) x, (int) (baseY - hgt));
        tri.addPoint((int) (x + wd / 2), (int) baseY);
        g.fillPolygon(tri);
        Polygon tri2 = new Polygon();
        tri2.addPoint((int) (x - wd / 2.6f), (int) (baseY - hgt * 0.45f));
        tri2.addPoint((int) x, (int) (baseY - hgt * 1.15f));
        tri2.addPoint((int) (x + wd / 2.6f), (int) (baseY - hgt * 0.45f));
        g.fillPolygon(tri2);
    }

    private static float noise(float t) {
        return (FMath.sin(t) * 0.55f + FMath.sin(t * 2.17f + 1.3f) * 0.3f + FMath.sin(t * 4.7f + 2.1f) * 0.15f + 1f) * 0.5f;
    }

    private static float frac(float v) {
        return v - (float) Math.floor(v);
    }
}
