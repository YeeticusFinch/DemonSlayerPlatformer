package game;

import java.awt.*;

final class PlatformArt {

    static void plat(Graphics2D g, Level.Plat p, boolean night) {
        int x = (int) p.x, y = (int) p.y, w = (int) p.w, h = (int) p.h;
        if (p.slope != 0) {
            Polygon roof = new Polygon();
            if (p.slope > 0) {
                roof.addPoint(x, y + h);
                roof.addPoint(x + w, y);
                roof.addPoint(x + w, y + 18);
                roof.addPoint(x, y + h + 18);
            } else {
                roof.addPoint(x, y);
                roof.addPoint(x + w, y + h);
                roof.addPoint(x + w, y + h + 18);
                roof.addPoint(x, y + 18);
            }
            g.setColor(new Color(74, 96, 128));
            g.fillPolygon(roof);
            g.setColor(new Color(38, 50, 72));
            g.setStroke(new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            if (p.slope > 0) g.drawLine(x, y + h, x + w, y);
            else g.drawLine(x, y, x + w, y + h);
            g.setColor(new Color(120, 130, 148));
            for (int i = 18; i < w; i += 28) {
                float tx = x + i;
                float ty = slopeY(p, tx) + 5;
                g.fillOval((int) tx - 8, (int) ty - 5, 18, 9);
            }
            return;
        }
        if (p.oneway) {
            g.setColor(new Color(96, 68, 44));
            g.fillRect(x, y, w, 12);
            g.setColor(new Color(126, 92, 60));
            g.fillRect(x, y, w, 5);
            g.setColor(new Color(70, 50, 34));
            for (int i = 8; i < w; i += 46) g.fillRect(x + i, y + 10, 4, 6);
            return;
        }
        switch (p.style) {
            case Level.GRASS -> {
                g.setColor(night ? new Color(74, 62, 52) : new Color(122, 88, 60));
                g.fillRect(x, y + 14, w, h - 14);
                g.setColor(night ? new Color(40, 78, 52) : new Color(82, 150, 84));
                g.fillRoundRect(x - 3, y - 4, w + 6, 24, 10, 10);
                g.setColor(night ? new Color(54, 100, 66) : new Color(112, 182, 108));
                for (int i = 4; i < w; i += 17) {
                    int hh = 5 + (i * 7 % 5);
                    g.drawLine(x + i, y + 1, x + i + 2, y + 1 - hh);
                }
                g.setColor(new Color(0, 0, 0, 30));
                for (int i = 20; i < w; i += 90) g.fillOval(x + i, y + 34, 26, 8);
            }
            case Level.ROCK -> {
                g.setColor(night ? new Color(86, 90, 104) : new Color(138, 143, 152));
                g.fillRect(x, y, w, h);
                g.setColor(night ? new Color(70, 74, 88) : new Color(116, 121, 132));
                Polygon f1 = facet(x + w / 5, y + h / 3, 40);
                g.fillPolygon(f1);
                Polygon f2 = facet(x + w * 2 / 3, y + h * 2 / 3, 55);
                g.fillPolygon(f2);
                g.setColor(night ? new Color(104, 108, 124) : new Color(168, 174, 184));
                g.fillRect(x, y, w, 6);
            }
            case Level.WOOD -> {
                g.setColor(night ? new Color(94, 70, 48) : new Color(140, 104, 72));
                g.fillRect(x, y, w, h);
                g.setColor(new Color(0, 0, 0, 45));
                for (int i = 26; i < w; i += 52) g.drawLine(x + i, y + 3, x + i, y + h - 3);
                g.setColor(night ? new Color(110, 84, 58) : new Color(164, 126, 88));
                g.fillRect(x, y, w, 5);
                g.setColor(new Color(0, 0, 0, 60));
                for (int i = 13; i < w; i += 52) g.fillOval(x + i, y + 8, 3, 3);
            }
            case Level.ROOF -> {
                g.setColor(new Color(56, 74, 102));
                g.fillRect(x, y + 10, w, Math.max(6, h - 10));
                g.setColor(new Color(74, 96, 128));
                for (int i = 0; i < w; i += 22)
                    g.fillArc(x + i, y + 8, 24, 16, 0, 180);
                g.setColor(new Color(38, 50, 72));
                g.fillRect(x, y + 8, w, 5);
                g.setColor(new Color(120, 130, 148));
                g.fillRect(x - 6, y + 2, w + 12, 7);
            }
            case Level.WEB -> {
                g.setColor(new Color(92, 70, 48));
                g.fillRoundRect(x, y, w, Math.max(10, h), 9, 9);
                g.setColor(new Color(230, 236, 245, 150));
                g.setStroke(new BasicStroke(1.1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                for (int i = 8; i < w; i += 22) g.drawLine(x + i, y + h / 2, x + i - 18, y - 170);
                for (int i = 0; i < w; i += 34) g.drawArc(x + i - 28, y - 80, 56, 95, 210, 120);
                g.setColor(new Color(245, 248, 255, 95));
                g.fillRect(x, y, w, 3);
            }
            default -> {
                g.setColor(night ? new Color(104, 108, 118) : new Color(158, 163, 172));
                g.fillRect(x, y, w, h);
                g.setColor(new Color(0, 0, 0, 40));
                for (int yy = y + 18; yy < y + h; yy += 18) {
                    g.drawLine(x, yy, x + w, yy);
                    boolean off = ((yy - y) / 18) % 2 == 0;
                    for (int xx = x + (off ? 0 : 26); xx < x + w; xx += 52)
                        g.drawLine(xx, Math.max(y, yy - 18), xx, yy);
                }
            }
        }
        g.setColor(new Color(0, 0, 0, night ? 90 : 35));
        g.drawRect(x, y, w, h);
    }

    private static Polygon facet(float cx, float cy, float r) {
        Polygon p = new Polygon();
        p.addPoint((int) (cx - r), (int) cy);
        p.addPoint((int) cx, (int) (cy - r * 0.6f));
        p.addPoint((int) (cx + r), (int) cy);
        p.addPoint((int) cx, (int) (cy + r * 0.5f));
        return p;
    }

    private static float slopeY(Level.Plat p, float px) {
        float t = FMath.clamp((px - p.x) / p.w, 0, 1);
        return p.slope > 0 ? p.y + p.h * (1 - t) : p.y + p.h * t;
    }

    static void deco(Graphics2D g, Level.Deco d, boolean night) {
        float x = d.x, y = d.y, s = d.s;
        switch (d.type) {
            case Level.TORII -> {
                g.setColor(night ? new Color(140, 42, 48) : new Color(188, 52, 58));
                g.fillRect((int) (x - 46 * s), (int) (y - 150 * s), (int) (16 * s), (int) (150 * s));
                g.fillRect((int) (x + 30 * s), (int) (y - 150 * s), (int) (16 * s), (int) (150 * s));
                g.fillRect((int) (x - 62 * s), (int) (y - 160 * s), (int) (124 * s), (int) (12 * s));
                g.setColor(night ? new Color(160, 52, 56) : new Color(214, 66, 70));
                g.fillRect((int) (x - 72 * s), (int) (y - 178 * s), (int) (144 * s), (int) (14 * s));
                g.fillRect((int) (x - 54 * s), (int) (y - 136 * s), (int) (108 * s), (int) (9 * s));
            }
            case Level.TREE -> {
                g.setColor(new Color(92, 64, 42));
                g.fillRect((int) (x - 9 * s), (int) (y - 90 * s), (int) (18 * s), (int) (990 * s));
                g.setColor(night ? new Color(30, 58, 44) : new Color(64, 122, 76));
                blob(g, x, y - 120 * s, 44 * s);
                blob(g, x - 30 * s, y - 95 * s, 30 * s);
                blob(g, x + 32 * s, y - 98 * s, 32 * s);
                if (!night) {
                    g.setColor(new Color(255, 255, 255, 40));
                    blob(g, x - 10 * s, y - 130 * s, 18 * s);
                }
            }
            case Level.PINE -> {
                g.setColor(new Color(84, 58, 38));
                g.fillRect((int) (x - 7 * s), (int) (y - 40 * s), (int) (14 * s), (int) (940 * s));
                g.setColor(night ? new Color(22, 46, 40) : new Color(48, 104, 66));
                for (int i = 0; i < 3; i++) {
                    float wd = (58 - i * 14) * s;
                    float by = y - (28 + i * 34) * s;
                    g.fillPolygon(new int[]{(int) (x - wd / 2), (int) x, (int) (x + wd / 2)},
                            new int[]{(int) by, (int) (by - 46 * s), (int) by}, 3);
                }
            }
            case Level.WISTERIA -> {
                g.setColor(new Color(80, 56, 40));
                g.fillRect((int) (x - 8 * s), (int) (y - 110 * s), (int) (16 * s), (int) (110 * s));
                g.setColor(night ? new Color(46, 36, 74) : new Color(84, 62, 110));
                blob(g, x, y - 130 * s, 52 * s);
                blob(g, x - 36 * s, y - 105 * s, 30 * s);
                blob(g, x + 36 * s, y - 108 * s, 32 * s);
                for (int i = -3; i <= 3; i++) {
                    float sway = FMath.sin(Game.time * 1.3f + i) * 3f;
                    g.setColor(night ? new Color(120, 90, 190, 220) : new Color(178, 142, 220));
                    int cxp = (int) (x + i * 15 * s + sway);
                    g.fillRoundRect(cxp - 4, (int) (y - 118 * s), 8, (int) ((30 + (i % 2) * 14) * s), 4, 4);
                    g.fillOval(cxp - 6, (int) (y - (118 - 30 - (i % 2) * 14) * s - 6), 12, 10);
                }
            }
            case Level.LANTERN -> {
                g.setColor(new Color(60, 44, 34));
                g.fillRect((int) (x - 4 * s), (int) (y - 84 * s), (int) (8 * s), (int) (84 * s));
                g.setColor(new Color(240, 196, 120));
                g.fillRoundRect((int) (x - 13 * s), (int) (y - 106 * s), (int) (26 * s), (int) (30 * s), 6, 6);
                g.setColor(new Color(60, 44, 34));
                g.fillRect((int) (x - 16 * s), (int) (y - 110 * s), (int) (32 * s), (int) (5 * s));
            }
            case Level.HUT -> {
                g.setColor(new Color(146, 108, 74));
                g.fillArc((int) (x - 55 * s), (int) (y - 70 * s), (int) (110 * s), (int) (80 * s), 180, 180);
                g.setColor(night ? new Color(70, 56, 46) : new Color(96, 74, 56));
                g.fillPolygon(new int[]{(int) (x - 68 * s), (int) x, (int) (x + 68 * s)},
                        new int[]{(int) (y - 66 * s), (int) (y - 118 * s), (int) (y - 66 * s)}, 3);
                g.setColor(new Color(52, 38, 30));
                g.fillArc((int) (x - 18 * s), (int) (y - 52 * s), (int) (36 * s), (int) (52 * s), 0, 180);
                if (night) {
                    g.setColor(new Color(255, 200, 120, 200));
                    g.fillRect((int) (x + 24 * s), (int) (y - 44 * s), (int) (14 * s), (int) (14 * s));
                }
            }
            case Level.GRAVE -> {
                g.setColor(night ? new Color(110, 114, 124) : new Color(158, 162, 170));
                g.fillRoundRect((int) (x - 14 * s), (int) (y - 54 * s), (int) (28 * s), (int) (54 * s), 12, 12);
                g.setColor(new Color(0, 0, 0, 50));
                g.drawLine((int) (x - 7 * s), (int) (y - 40 * s), (int) (x + 7 * s), (int) (y - 40 * s));
                g.drawLine((int) x, (int) (y - 46 * s), (int) x, (int) (y - 33 * s));
            }
            case Level.HOUSE -> {
                g.setColor(night ? new Color(84, 70, 62) : new Color(150, 122, 98));
                g.fillRect((int) (x - 62 * s), (int) (y - 90 * s), (int) (124 * s), (int) (90 * s));
                g.setColor(night ? new Color(40, 34, 44) : new Color(74, 60, 66));
                g.fillPolygon(new int[]{(int) (x - 78 * s), (int) x, (int) (x + 78 * s)},
                        new int[]{(int) (y - 88 * s), (int) (y - 140 * s), (int) (y - 88 * s)}, 3);
                if (night) {
                    g.setColor(new Color(255, 198, 110, 230));
                    g.fillRect((int) (x - 40 * s), (int) (y - 66 * s), (int) (20 * s), (int) (22 * s));
                    g.fillRect((int) (x + 20 * s), (int) (y - 66 * s), (int) (20 * s), (int) (22 * s));
                } else {
                    g.setColor(new Color(70, 56, 52));
                    g.fillRect((int) (x - 40 * s), (int) (y - 66 * s), (int) (20 * s), (int) (22 * s));
                    g.fillRect((int) (x + 20 * s), (int) (y - 66 * s), (int) (20 * s), (int) (22 * s));
                    g.setColor(new Color(56, 42, 36));
                    g.fillRect((int) (x - 12 * s), (int) (y - 48 * s), (int) (24 * s), (int) (48 * s));
                }
            }
            case Level.FENCE -> {
                g.setColor(new Color(110, 84, 58));
                for (int i = 0; i < 4; i++)
                    g.fillRect((int) (x + i * 18 * s), (int) (y - 40 * s), (int) (6 * s), (int) (40 * s));
                g.fillRect((int) (x - 4 * s), (int) (y - 30 * s), (int) (70 * s), (int) (5 * s));
                g.fillRect((int) (x - 4 * s), (int) (y - 16 * s), (int) (70 * s), (int) (5 * s));
            }
            case Level.BANNER -> {
                g.setColor(new Color(70, 52, 40));
                g.fillRect((int) (x - 4 * s), (int) (y - 150 * s), (int) (8 * s), (int) (150 * s));
                float sway = FMath.sin(Game.time * 2.2f + x) * 6f;
                g.setColor(night ? new Color(120, 40, 50) : new Color(170, 52, 62));
                g.fillPolygon(new int[]{(int) (x + 4 * s), (int) (x + 52 * s + sway), (int) (x + 52 * s + sway), (int) (x + 4 * s)},
                        new int[]{(int) (y - 146 * s), (int) (y - 138 * s), (int) (y - 84 * s), (int) (y - 92 * s)}, 4);
                g.setColor(Color.WHITE);
                g.fillOval((int) (x + 18 * s + sway / 2), (int) (y - 124 * s), (int) (14 * s), (int) (14 * s));
            }
            case Level.CAMPFIRE -> {
                g.setColor(new Color(88, 62, 42));
                g.setStroke(new BasicStroke(6 * s, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.drawLine((int) (x - 16 * s), (int) (y - 6 * s), (int) (x + 16 * s), (int) (y - 12 * s));
                g.drawLine((int) (x - 14 * s), (int) (y - 14 * s), (int) (x + 15 * s), (int) (y - 5 * s));
            }
            case Level.SIGN -> {
                g.setColor(new Color(104, 78, 52));
                g.fillRect((int) (x - 4 * s), (int) (y - 60 * s), (int) (8 * s), (int) (60 * s));
                g.setColor(night ? new Color(120, 96, 66) : new Color(158, 126, 86));
                g.fillRoundRect((int) (x - 30 * s), (int) (y - 92 * s), (int) (60 * s), (int) (36 * s), 6, 6);
                g.setColor(new Color(0, 0, 0, 60));
                g.drawLine((int) (x - 20 * s), (int) (y - 80 * s), (int) (x + 20 * s), (int) (y - 80 * s));
                g.drawLine((int) (x - 20 * s), (int) (y - 70 * s), (int) (x + 12 * s), (int) (y - 70 * s));
            }
            case Level.WINDOW -> {
                g.setColor(new Color(70, 50, 34));
                g.fillRect((int) (x - 40 * s), (int) (y - 90 * s), (int) (80 * s), (int) (90 * s));
                g.setColor(night ? new Color(28, 34, 66) : new Color(190, 225, 245));
                g.fillRect((int) (x - 32 * s), (int) (y - 82 * s), (int) (64 * s), (int) (74 * s));
                g.setColor(new Color(70, 50, 34));
                g.drawLine((int) x, (int) (y - 82 * s), (int) x, (int) (y - 8 * s));
                if (!night) Glow.blob(g, x, y - 45 * s, 40 * s, new Color(255, 250, 210, 90));
            }
            case Level.STATUE -> {
                g.setColor(night ? new Color(96, 100, 112) : new Color(148, 152, 162));
                g.fillRect((int) (x - 22 * s), (int) (y - 26 * s), (int) (44 * s), (int) (26 * s));
                g.fillRoundRect((int) (x - 14 * s), (int) (y - 74 * s), (int) (28 * s), (int) (52 * s), 10, 10);
                g.fillOval((int) (x - 11 * s), (int) (y - 96 * s), (int) (22 * s), (int) (24 * s));
                g.fillPolygon(new int[]{(int) (x - 11 * s), (int) (x - 19 * s), (int) (x - 9 * s)},
                        new int[]{(int) (y - 86 * s), (int) (y - 92 * s), (int) (y - 78 * s)}, 3);
                g.fillPolygon(new int[]{(int) (x + 11 * s), (int) (x + 19 * s), (int) (x + 9 * s)},
                        new int[]{(int) (y - 86 * s), (int) (y - 92 * s), (int) (y - 78 * s)}, 3);
            }
            case Level.TEMPLE -> {
                g.setColor(night ? new Color(82, 58, 46) : new Color(150, 112, 82));
                g.fillRect((int) (x - 170 * s), (int) (y - 132 * s), (int) (340 * s), (int) (132 * s));
                g.setColor(night ? new Color(40, 30, 32) : new Color(76, 54, 48));
                g.fillRect((int) (x - 44 * s), (int) (y - 82 * s), (int) (88 * s), (int) (82 * s));
                g.setColor(night ? new Color(48, 38, 42) : new Color(80, 62, 58));
                g.fillRect((int) (x - 154 * s), (int) (y - 118 * s), (int) (46 * s), (int) (72 * s));
                g.fillRect((int) (x + 108 * s), (int) (y - 118 * s), (int) (46 * s), (int) (72 * s));
                Polygon roof = new Polygon();
                roof.addPoint((int) (x - 205 * s), (int) (y - 128 * s));
                roof.addPoint((int) x, (int) (y - 235 * s));
                roof.addPoint((int) (x + 205 * s), (int) (y - 128 * s));
                g.setColor(night ? new Color(48, 66, 90) : new Color(74, 96, 128));
                g.fillPolygon(roof);
                g.setColor(new Color(28, 38, 58));
                g.setStroke(new BasicStroke(5f * s, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.drawLine((int) (x - 205 * s), (int) (y - 128 * s), (int) x, (int) (y - 235 * s));
                g.drawLine((int) x, (int) (y - 235 * s), (int) (x + 205 * s), (int) (y - 128 * s));
                g.setColor(new Color(188, 52, 58));
                g.fillRect((int) (x - 190 * s), (int) (y - 132 * s), (int) (380 * s), (int) (12 * s));
            }
            case Level.WEB_HOUSE -> {
                g.setColor(new Color(236, 240, 248, 110));
                g.setStroke(new BasicStroke(1.4f * s, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                for (int i = 0; i < 12; i++) {
                    double a = i * Math.PI / 6;
                    g.drawLine((int) x, (int) (y - 210 * s), (int) (x + Math.cos(a) * 260 * s), (int) (y - 210 * s + Math.sin(a) * 190 * s));
                }
                for (int r = 70; r <= 250; r += 45) {
                    g.drawOval((int) (x - r * s), (int) (y - 210 * s - r * 0.72f * s), (int) (r * 2 * s), (int) (r * 1.44f * s));
                }
                g.setColor(new Color(72, 55, 46));
                g.fillRect((int) (x - 86 * s), (int) (y - 260 * s), (int) (172 * s), (int) (94 * s));
                g.setColor(new Color(38, 31, 34));
                g.fillPolygon(new int[]{(int) (x - 104 * s), (int) x, (int) (x + 104 * s)},
                        new int[]{(int) (y - 260 * s), (int) (y - 326 * s), (int) (y - 260 * s)}, 3);
                g.setColor(new Color(255, 218, 120, 150));
                g.fillRect((int) (x - 38 * s), (int) (y - 230 * s), (int) (24 * s), (int) (28 * s));
                g.fillRect((int) (x + 18 * s), (int) (y - 230 * s), (int) (24 * s), (int) (28 * s));
            }
        }
    }

    static void car(Graphics2D g, float x, float y, float s, boolean night) {
        Graphics2D gg = (Graphics2D) g.create();
        gg.translate(x, y);
        gg.scale(s, s);
        gg.setColor(new Color(18, 24, 30));
        gg.fillRoundRect(-62, -34, 124, 34, 10, 10);
        gg.setColor(new Color(38, 46, 54));
        gg.fillRoundRect(-36, -58, 58, 32, 10, 10);
        gg.setColor(new Color(190, 210, 220));
        gg.fillRect(-28, -52, 20, 18);
        gg.fillRect(-2, -52, 18, 18);
        gg.setColor(new Color(220, 210, 140));
        gg.fillOval(48, -24, 12, 10);
        gg.setColor(new Color(20, 20, 22));
        gg.fillOval(-45, -12, 24, 24);
        gg.fillOval(30, -12, 24, 24);
        gg.setColor(new Color(160, 160, 166));
        gg.fillOval(-38, -5, 10, 10);
        gg.fillOval(37, -5, 10, 10);
        if (night) Glow.blob(gg, 58, -19, 24, new Color(255, 225, 150, 120));
        gg.dispose();
    }

    static void foregroundTree(Graphics2D g, Level.Deco d) {
        float x = d.x, y = d.y, s = d.s;
        Composite old = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.86f));
        g.setColor(new Color(16, 14, 18, 235));
        g.fillRoundRect((int) (x - 18 * s), (int) (y - 520 * s), (int) (36 * s), (int) (1620 * s), (int) (12 * s), (int) (12 * s));
        g.setColor(new Color(28, 24, 30, 210));
        g.fillRoundRect((int) (x - 7 * s), (int) (y - 500 * s), (int) (8 * s), (int) (1600 * s), (int) (4 * s), (int) (4 * s));
        g.setColor(new Color(7, 10, 12, 225));
        blob(g, x - 22 * s, y - 485 * s, 58 * s);
        blob(g, x + 34 * s, y - 430 * s, 64 * s);
        blob(g, x - 42 * s, y - 365 * s, 72 * s);
        blob(g, x + 20 * s, y - 300 * s, 50 * s);
        g.setComposite(old);
    }

    static void campfireFlames(Graphics2D g, Level.Deco d) {
        float x = d.x, y = d.y, s = d.s;
        for (int i = 0; i < 3; i++) {
            float t = Game.time * 9 + i * 2.1f;
            float fh = (18 + FMath.sin(t) * 6 + i * 5) * s;
            float fw = (10 + FMath.cos(t * 1.3f) * 3) * s;
            g.setColor(i == 0 ? new Color(255, 120, 30) : i == 1 ? new Color(255, 180, 50) : new Color(255, 235, 160));
            g.fillPolygon(new int[]{(int) (x - fw + i * 2), (int) (x + FMath.sin(t) * 2), (int) (x + fw - i * 2)},
                    new int[]{(int) (y - 8 * s), (int) (y - 8 * s - fh), (int) (y - 8 * s)}, 3);
        }
    }

    static void boulder(Graphics2D g, World w) {
        float[] b = w.level.boulder;
        if (b == null || w.boulderProgress >= 100) return;
        float prog = w.boulderProgress / 100f;
        g.setColor(night(w) ? new Color(96, 100, 110) : new Color(140, 145, 155));
        if (prog <= 0) {
            g.fillRoundRect((int) b[0], (int) b[1], (int) b[2], (int) b[3], 30, 30);
            g.setColor(new Color(0, 0, 0, 60));
            g.drawRoundRect((int) b[0], (int) b[1], (int) b[2], (int) b[3], 30, 30);
            g.drawLine((int) (b[0] + b[2] / 2), (int) (b[1] + 8), (int) (b[0] + b[2] / 2), (int) (b[1] + b[3] - 8));
        } else {
            float off = prog * 150;
            float rot = prog * 0.25f;
            for (int side = -1; side <= 1; side += 2) {
                Graphics2D gg = (Graphics2D) g.create();
                gg.translate(b[0] + b[2] / 2 + side * off, b[1] + b[3] * 0.7f);
                gg.rotate(side * rot);
                gg.setColor(night(w) ? new Color(96, 100, 110) : new Color(140, 145, 155));
                gg.fillRoundRect((int) (-b[2] / 2 * (side == 1 ? 1 : 1)), (int) (-b[3] * 0.7f),
                        (int) (b[2] / 2 - 2), (int) (b[3] * 0.85f), 20, 20);
                gg.dispose();
            }
        }
    }

    static void goal(Graphics2D g, World w) {
        boolean locked = !w.goalUnlocked();
        Composite old = g.getComposite();
        if (locked) g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.45f));
        int gx = (int) w.level.goalX, gy = (int) w.level.goalY;
        g.setColor(new Color(188, 52, 58));
        g.fillRect(gx - 30, gy, 10, 150);
        g.fillRect(gx + 20, gy, 10, 150);
        g.fillRect(gx - 40, gy - 8, 80, 10);
        g.fillRect(gx - 46, gy - 22, 92, 12);
        g.setColor(Color.WHITE);
        g.fillOval(gx - 8, gy + 40, 16, 16);
        g.setColor(new Color(188, 52, 58));
        g.fillOval(gx - 4, gy + 44, 8, 8);
        if (!locked && FMath.chance(0.08f)) {
            w.parts.spawn(Particles.SPARK, gx + FMath.rand(-30, 30), gy + FMath.rand(0, 120), FMath.rand(-20, 20), FMath.rand(-60, -20), 0.6f, 5, new Color(255, 220, 150), 0, 0.97f);
        }
        g.setComposite(old);
    }

    static void pickup(Graphics2D g, World.Pickup p) {
        float bob = FMath.sin(p.t * 4) * 4;
        float x = p.x, y = p.y + bob;
        if (p.t > 11 && (int) (p.t * 8) % 2 == 0) return;
        if (p.type == 0) {
            g.setColor(new Color(255, 70, 95));
            heart(g, x, y, 8);
            g.setColor(new Color(255, 160, 175));
            g.fillOval((int) x - 4, (int) y - 4, 4, 4);
        } else {
            g.setColor(new Color(90, 170, 255));
            g.fillOval((int) x - 7, (int) y - 7, 14, 14);
            g.setColor(new Color(190, 225, 255));
            g.fillOval((int) x - 3, (int) y - 5, 5, 5);
        }
    }

    static void heart(Graphics2D g, float x, float y, float r) {
        g.fillOval((int) (x - r), (int) (y - r * 0.6f), (int) r, (int) r);
        g.fillOval((int) x, (int) (y - r * 0.6f), (int) r, (int) r);
        g.fillPolygon(new int[]{(int) (x - r), (int) (x + r), (int) x},
                new int[]{(int) (y - r * 0.15f), (int) (y - r * 0.15f), (int) (y + r)}, 3);
    }

    static void decoGlow(Graphics2D g, World w, Level.Deco d) {
        boolean night = !w.level.daytime;
        switch (d.type) {
            case Level.LANTERN -> Glow.blob(g, d.x, d.y - 91 * d.s, 34 * d.s, new Color(255, 200, 110, 190));
            case Level.CAMPFIRE -> Glow.blob(g, d.x, d.y - 20 * d.s, 48 * d.s, new Color(255, 150, 60, 110));
            case Level.HOUSE -> {
                if (night) {
                    Glow.blob(g, d.x - 30 * d.s, d.y - 55 * d.s, 22 * d.s, new Color(255, 198, 110, 170));
                    Glow.blob(g, d.x + 30 * d.s, d.y - 55 * d.s, 22 * d.s, new Color(255, 198, 110, 170));
                }
            }
            case Level.HUT -> {
                if (night) Glow.blob(g, d.x + 31 * d.s, d.y - 37 * d.s, 18 * d.s, new Color(255, 200, 120, 170));
            }
            case Level.TEMPLE -> {
                if (night) Glow.blob(g, d.x, d.y - 60 * d.s, 48 * d.s, new Color(255, 180, 120, 95));
            }
            case Level.TORII -> {
                if (night) Glow.blob(g, d.x, d.y - 120 * d.s, 60 * d.s, new Color(255, 90, 90, 40));
            }
            case Level.WISTERIA -> Glow.blob(g, d.x, d.y - 120 * d.s, 85 * d.s, new Color(165, 120, 235, 70));
        }
    }

    private static void blob(Graphics2D g, float x, float y, float r) {
        g.fillOval((int) (x - r), (int) (y - r), (int) (r * 2), (int) (r * 2));
    }

    private static boolean night(World w) { return !w.level.daytime; }
}
