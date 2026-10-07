package game;

import java.awt.*;
import java.util.ArrayList;

final class HUD {
    private float hpGhost = 100;

    void render(Graphics2D g, World w, Profile prof, int vw, int vh, boolean showHelp) {
        Player p = w.player;
        hpGhost = Math.max(p.hp, FMath.approach(hpGhost, p.hp, 60 * 0.016f));

        portrait(g, p);
        bar(g, 92, 26, 250, 18, p.hp / p.maxHp, hpGhost / p.maxHp,
                new Color(232, 62, 74), new Color(120, 30, 40), "HP");
        bar(g, 92, 50, 250, 11, p.sp / p.maxSp, p.sp / p.maxSp,
                new Color(70, 160, 255), new Color(30, 70, 130), "SP");
        if (p.spLock > 0 && (int) (Game.time * 9) % 2 == 0) {
            g.setColor(new Color(255, 60, 60, 230));
            g.setStroke(new BasicStroke(2.4f));
            g.drawRoundRect(90, 48, 254, 15, 6, 6);
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
            g.setColor(new Color(255, 90, 90));
            g.drawString("EXHAUSTED", 348, 60);
        }

        abilityBar(g, w, p);

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        g.setColor(Color.WHITE);
        String lv = w.level.name;
        g.drawString(lv, vw - g.getFontMetrics().stringWidth(lv) - 20, 28);
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        g.setColor(new Color(220, 225, 235));
        g.drawString(w.level.objective, vw - g.getFontMetrics().stringWidth(w.level.objective) - 20, 46);
        if (w.level.winMode == Level.WinMode.GOAL_AFTER_KILLS) {
            String k = "Foes slain: " + w.kills + " / " + w.totalEnemies;
            g.setColor(new Color(255, 200, 120));
            g.drawString(k, vw - g.getFontMetrics().stringWidth(k) - 20, 64);
        }

        bossBars(g, w, vw, vh);

        banners(g, w, vw);

        if (!p.dead && !w.complete)
            hint(g, "A/D move   W jump x2   Shift dash   J attack   K guard   L recharge   Q/E + F form   H help", vw - 20, vh - 16, false);

        if (showHelp) helpPanel(g, p, vw, vh);

        if (p.dead) {
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 44));
            centerShadow(g, "YOU DIED", vw / 2f, vh / 2f - 10, new Color(220, 60, 70));
            g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 15));
            centerShadow(g, "Press R to rise again", vw / 2f, vh / 2f + 24, new Color(230, 230, 240));
        }
        if (w.complete) {
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 40));
            centerShadow(g, "LEVEL CLEAR", vw / 2f, vh / 3f, new Color(255, 215, 120));
            g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 15));
            centerShadow(g, "Press ENTER to continue", vw / 2f, vh / 3f + 30, new Color(240, 240, 245));
        }
    }

    private void portrait(Graphics2D g, Player p) {
        g.setColor(new Color(12, 14, 20, 200));
        g.fillOval(22, 18, 58, 58);
        g.setColor(swatch(p).brighter());
        g.setStroke(new BasicStroke(2.5f));
        g.drawOval(22, 18, 58, 58);
        if (p.nezukoMode) {
            Color skin = new Color(255, 229, 200);
            Color hair = new Color(24, 18, 22);
            g.setColor(hair);
            g.fillRect(35, 33, 8, 28);
            g.fillRect(58, 33, 8, 28);
            g.setColor(new Color(226, 112, 42, 210));
            g.fillRect(35, 52, 8, 10);
            g.fillRect(58, 53, 8, 10);
            g.setColor(skin);
            g.fillOval(36, 36, 30, 30);
            g.setColor(hair);
            Polygon sp = new Polygon();
            sp.addPoint(35, 42);
            sp.addPoint(39, 27);
            sp.addPoint(45, 33);
            sp.addPoint(51, 26);
            sp.addPoint(57, 33);
            sp.addPoint(65, 42);
            sp.addPoint(58, 38);
            sp.addPoint(45, 39);
            g.fillPolygon(sp);
            g.setColor(new Color(255, 245, 250));
            g.fillOval(42, 45, 8, 8);
            g.fillOval(54, 45, 8, 8);
            g.setColor(new Color(215, 64, 142));
            g.fillOval(44, 47, 4, 4);
            g.fillOval(56, 47, 4, 4);
            g.setColor(new Color(75, 126, 72));
            g.fillRect(41, 55, 20, 6);
            g.setColor(new Color(105, 46, 30));
            g.fillOval(39, 56, 4, 4);
            g.fillOval(60, 56, 4, 4);
            return;
        }
        Color skin = p.isDemon ? new Color(214, 206, 218) : new Color(242, 203, 158);
        Color hair = p.nezukoMode ? new Color(24, 18, 22) : p.isDemon ? new Color(238, 238, 244) : new Color(74, 43, 48);
        g.setColor(hair);
        g.fillArc(32, 26, 38, 34, 0, 180);
        g.setColor(skin);
        g.fillOval(36, 36, 30, 30);
        g.setColor(hair);
        Polygon sp = new Polygon();
        sp.addPoint(35, 42);
        sp.addPoint(39, 27);
        sp.addPoint(45, 33);
        sp.addPoint(51, 26);
        sp.addPoint(57, 33);
        sp.addPoint(65, 42);
        g.fillPolygon(sp);
        if (p.isDemon) {
            g.setColor(p.nezukoMode ? new Color(255, 86, 170) : new Color(255, 214, 74));
            g.fillOval(43, 46, 7, 5);
            g.fillOval(53, 46, 7, 5);
        } else {
            g.setColor(new Color(60, 40, 40));
            g.fillRect(44, 47, 4, 4);
            g.fillRect(54, 47, 4, 4);
            g.setColor(new Color(196, 120, 100, 170));
            g.fillRect(45, 39, 12, 3);
        }
    }

    private Color swatch(Player p) {
        if (p.isDemon) return p.demonArt == Profile.DemonArt.FOREST_HAND ? new Color(95, 160, 75)
                : p.demonArt == Profile.DemonArt.SWAMP ? new Color(30, 135, 122)
                : p.demonArt == Profile.DemonArt.SUSUMARU ? new Color(235, 190, 74)
                : p.demonArt == Profile.DemonArt.YAHABA ? new Color(210, 40, 55)
                : p.demonArt == Profile.DemonArt.COMBUSTIBLE_BLOOD ? AbilityCast.NEZUKO_HI : new Color(205, 25, 55);
        return switch (p.style) {
            case WATER -> new Color(70, 150, 255);
            case FLAME -> new Color(255, 95, 45);
            case WIND -> new Color(70, 205, 110);
            default -> new Color(120, 118, 132);
        };
    }

    private void bar(Graphics2D g, float x, float y, float wd, float ht, float v, float ghost, Color c, Color dark, String label) {
        g.setColor(new Color(10, 12, 18, 210));
        g.fillRoundRect((int) x - 2, (int) y - 2, (int) wd + 4, (int) ht + 4, 8, 8);
        g.setColor(dark);
        g.fillRoundRect((int) x, (int) y, (int) wd, (int) ht, 6, 6);
        g.setColor(new Color(255, 255, 255, 70));
        g.fillRoundRect((int) x, (int) y, (int) (wd * FMath.clamp(ghost, 0, 1)), (int) ht, 6, 6);
        g.setColor(c);
        g.fillRoundRect((int) x, (int) y, (int) (wd * FMath.clamp(v, 0, 1)), (int) ht, 6, 6);
        g.setPaint(new GradientPaint(x, y, new Color(255, 255, 255, 90), x, y + ht, new Color(255, 255, 255, 0)));
        g.fillRoundRect((int) x, (int) y, (int) (wd * FMath.clamp(v, 0, 1)), (int) ht, 6, 6);
        g.setColor(new Color(255, 255, 255, 60));
        for (int i = 1; i < 5; i++)
            g.drawLine((int) (x + wd * i / 5f), (int) y + 1, (int) (x + wd * i / 5f), (int) (y + ht - 1));
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        g.setColor(new Color(235, 238, 245));
        g.drawString(label, x - 2, y - 6);
    }

    private void abilityBar(Graphics2D g, World w, Player p) {
        int n = p.abilities.size();
        if (n == 0) {
            g.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 12));
            g.setColor(new Color(200, 205, 215));
            g.drawString("No forms yet - your blade awaits its color...", 96, 84);
            return;
        }
        for (int i = 0; i < n; i++) {
            Ability a = p.abilities.get(i);
            float bx = 92 + i * 54, by = 68, bs = 46;
            boolean sel = i == p.selAbility;
            boolean ready = p.cds[i] <= 0 && p.sp >= a.cost;
            g.setColor(sel ? new Color(30, 34, 46) : new Color(16, 18, 26, 190));
            g.fillRoundRect((int) bx, (int) by, (int) bs, (int) bs, 9, 9);
            g.setColor(sel ? swatch(p).brighter() : new Color(80, 86, 100));
            g.setStroke(new BasicStroke(sel ? 2.6f : 1.4f));
            g.drawRoundRect((int) bx, (int) by, (int) bs, (int) bs, 9, 9);
            Composite old = g.getComposite();
            if (!ready) g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.35f));
            glyph(g, a.kind, bx + bs / 2, by + bs / 2 + 1, swatch(p));
            g.setComposite(old);
            if (p.cds[i] > 0) {
                double frac = FMath.clamp(p.cds[i] / Math.max(0.01f, a.cooldown), 0, 1);
                g.setColor(new Color(10, 12, 18, 170));
                g.fillArc((int) bx + 3, (int) by + 3, (int) bs - 6, (int) bs - 6, 90, -(int) (360 * frac));
            }
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
            g.setColor(ready ? new Color(235, 238, 245) : new Color(130, 135, 148));
            g.drawString(String.valueOf(i + 1), bx + 4, by + 13);
            if (sel) {
                g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
                String nm = a.form;
                if (nm.length() > 42) nm = nm.substring(0, 41) + ".";
                g.setColor(new Color(225, 228, 238));
                g.drawString(nm, 96, 132);
                g.setColor(new Color(160, 166, 182));
                g.drawString("F to use - " + a.cost + " SP", 96, 146);
            }
        }
    }

    private void glyph(Graphics2D g, Ability.Kind k, float cx, float cy, Color c) {
        g.setStroke(new BasicStroke(2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(c);
        switch (k) {
            case WAVE_PROJ -> g.drawArc((int) cx - 13, (int) cy - 13, 26, 26, -60, 140);
            case WHEEL -> {
                g.drawOval((int) cx - 12, (int) cy - 12, 24, 24);
                g.drawLine((int) cx, (int) cy, (int) cx + 9, (int) cy - 9);
                g.drawLine((int) cx, (int) cy, (int) cx - 9, (int) cy - 9);
                g.drawLine((int) cx, (int) cy, (int) cx, (int) cy + 11);
            }
            case FLOWING_DANCE -> {
                for (int i = -1; i <= 1; i++)
                    g.drawArc((int) cx - 14 + i * 8, (int) cy - 12, 14, 24, -50, 110);
            }
            case TIDE -> {
                for (int i = 0; i < 2; i++)
                    g.drawOval((int) cx - 13 + i * 12, (int) cy - 9, 18, 18);
            }
            case BLESSED_RAIN -> {
                g.drawArc((int) cx - 12, (int) cy - 16, 24, 30, 210, 125);
                g.drawLine((int) cx + 7, (int) cy - 8, (int) cx - 8, (int) cy + 12);
            }
            case WHIRL -> g.drawArc((int) cx - 12, (int) cy - 12, 24, 24, 30, 270);
            case FIRE_DASH -> {
                g.drawLine((int) cx - 12, (int) cy, (int) cx + 10, (int) cy);
                g.drawLine((int) cx + 10, (int) cy, (int) cx + 3, (int) cy - 7);
                g.drawLine((int) cx + 10, (int) cy, (int) cx + 3, (int) cy + 7);
            }
            case RISING_SUN -> {
                g.drawArc((int) cx - 12, (int) cy - 12, 24, 24, 0, 180);
                g.drawLine((int) cx, (int) cy + 2, (int) cx, (int) cy - 12);
            }
            case BLAZING_SLAM -> {
                g.drawLine((int) cx, (int) cy - 12, (int) cx, (int) cy + 6);
                g.fillPolygon(new int[]{(int) cx - 7, (int) cx + 7, (int) cx}, new int[]{(int) cy + 4, (int) cy + 4, (int) cy + 13}, 3);
            }
            case TIGER_VOLLEY -> {
                for (int i = -1; i <= 1; i++)
                    g.fillPolygon(new int[]{(int) cx + i * 9 - 4, (int) cx + i * 9 + 4, (int) cx + i * 9},
                            new int[]{(int) cy - 5, (int) cy - 5, (int) cy + 8}, 3);
            }
            case FLAME_UNDULATION -> {
                g.drawArc((int) cx - 14, (int) cy - 14, 28, 28, 35, 285);
                g.drawLine((int) cx - 8, (int) cy - 11, (int) cx + 9, (int) cy + 10);
                g.drawLine((int) cx - 9, (int) cy + 10, (int) cx + 8, (int) cy - 11);
            }
            case WIND_DUST -> {
                g.drawArc((int) cx - 14, (int) cy - 12, 28, 24, 20, 300);
                g.drawLine((int) cx - 7, (int) cy + 5, (int) cx + 11, (int) cy - 6);
            }
            case WIND_CLAWS -> {
                for (int i = 0; i < 4; i++)
                    g.drawLine((int) cx - 12 + i * 8, (int) cy + 11, (int) cx - 4 + i * 8, (int) cy - 10);
            }
            case WIND_STORM -> {
                g.drawArc((int) cx - 13, (int) cy - 15, 26, 30, 30, 270);
                g.drawArc((int) cx - 8, (int) cy - 11, 16, 22, 210, 230);
            }
            case WIND_MOUNTAIN -> {
                g.drawArc((int) cx - 14, (int) cy - 9, 28, 18, 8, 160);
                g.drawArc((int) cx - 9, (int) cy - 15, 24, 24, 190, 130);
            }
            case WIND_TREE -> {
                g.drawLine((int) cx, (int) cy + 13, (int) cx, (int) cy - 13);
                for (int i = -1; i <= 1; i++)
                    g.drawLine((int) cx, (int) cy - i * 3, (int) cx + i * 11, (int) cy - 10 + Math.abs(i) * 6);
            }
            case WIND_MIST -> {
                g.drawLine((int) cx, (int) cy + 12, (int) cx, (int) cy - 13);
                g.drawArc((int) cx - 10, (int) cy - 14, 20, 26, 240, 230);
            }
            case WIND_BLACK -> {
                g.drawLine((int) cx - 13, (int) cy - 9, (int) cx + 12, (int) cy + 9);
                g.drawLine((int) cx - 12, (int) cy + 9, (int) cx + 13, (int) cy - 9);
                g.drawArc((int) cx - 12, (int) cy - 12, 24, 24, 35, 110);
            }
            case WIND_EIGHT, WIND_NINE, WIND_TEN -> {
                g.drawOval((int) cx - 10, (int) cy - 10, 20, 20);
                g.drawLine((int) cx - 6, (int) cy + 6, (int) cx + 6, (int) cy - 6);
            }
            case BLOOD_BOLT -> g.fillPolygon(new int[]{(int) cx - 11, (int) cx, (int) cx + 11, (int) cx},
                    new int[]{(int) cy, (int) cy - 8, (int) cy, (int) cy + 8}, 4);
            case NEZUKO_NAILS -> {
                g.drawLine((int) cx - 10, (int) cy + 10, (int) cx, (int) cy - 12);
                g.drawLine((int) cx, (int) cy - 12, (int) cx + 8, (int) cy - 4);
            }
            case NEZUKO_EXPLODING_BLOOD -> {
                g.drawOval((int) cx - 8, (int) cy - 13, 16, 26);
                g.drawLine((int) cx - 13, (int) cy, (int) cx + 13, (int) cy);
            }
            case NEZUKO_SCRATCHING -> {
                for (int i = 0; i < 3; i++) g.drawLine((int) cx - 10 + i * 8, (int) cy + 10, (int) cx - 2 + i * 8, (int) cy - 10);
                g.drawLine((int) cx - 11, (int) cy - 9, (int) cx + 11, (int) cy + 9);
            }
            case NEZUKO_HEEL_BASH -> {
                g.drawArc((int) cx - 12, (int) cy - 15, 24, 22, 200, 190);
                g.drawLine((int) cx, (int) cy - 4, (int) cx + 8, (int) cy + 12);
            }
            case NEZUKO_SPIN_KICK -> {
                g.drawArc((int) cx - 13, (int) cy - 13, 26, 26, 30, 300);
                g.drawLine((int) cx + 2, (int) cy + 2, (int) cx + 13, (int) cy + 8);
            }
            case NEZUKO_FLYING_KICK -> {
                g.drawLine((int) cx - 12, (int) cy + 5, (int) cx + 11, (int) cy - 6);
                g.drawLine((int) cx + 11, (int) cy - 6, (int) cx + 5, (int) cy - 11);
            }
            case CLAW_WAVES -> {
                for (int i = -1; i <= 1; i++)
                    g.drawArc((int) cx - 12 + i * 6, (int) cy - 12, 24, 24, -40 + i * 20, 80);
            }
            case NOVA -> {
                for (int i = 0; i < 8; i++) {
                    double an = i * Math.PI / 4;
                    g.drawLine((int) (cx + FMath.cos((float) an) * 4), (int) (cy + FMath.sin((float) an) * 4),
                            (int) (cx + FMath.cos((float) an) * 12), (int) (cy + FMath.sin((float) an) * 12));
                }
            }
            case DEVOUR -> PlatformArt.heart(g, cx, cy - 2, 9);
            case SWAMP_FLURRY -> {
                for (int i = 0; i < 4; i++)
                    g.drawLine((int) cx - 12 + i * 8, (int) cy + 8, (int) cx - 5 + i * 8, (int) cy - 9);
            }
            case AQUATIC_DASH -> {
                g.drawLine((int) cx - 13, (int) cy, (int) cx + 11, (int) cy);
                g.drawArc((int) cx - 12, (int) cy - 11, 22, 22, 210, 250);
            }
            case SWAMP_HANDS -> {
                g.fillOval((int) cx - 12, (int) cy + 6, 24, 8);
                g.drawLine((int) cx, (int) cy + 6, (int) cx, (int) cy - 12);
                for (int i = -2; i <= 2; i++) g.drawLine((int) cx, (int) cy - 12, (int) cx + i * 4, (int) cy - 17);
            }
            case SWAMP_STEP -> {
                g.fillOval((int) cx - 13, (int) cy + 4, 26, 9);
                g.drawArc((int) cx - 9, (int) cy - 12, 18, 22, 40, 250);
            }
            case BALL_KICK, SPINNING_THROW, SIXFOLD_TEMARI, SPIRALING_SHOT -> {
                g.drawOval((int) cx - 11, (int) cy - 11, 22, 22);
                g.drawLine((int) cx - 11, (int) cy, (int) cx + 11, (int) cy);
                g.drawLine((int) cx, (int) cy - 11, (int) cx, (int) cy + 11);
            }
            case PIERCING_KICK -> {
                g.drawLine((int) cx - 12, (int) cy + 8, (int) cx + 12, (int) cy - 8);
                g.drawLine((int) cx + 12, (int) cy - 8, (int) cx + 4, (int) cy - 11);
            }
            case BOULDER_TOSS, SMACK_DOWN, CHASER_ARROW, ERUPTION, TORRENTIAL_ARROWS -> {
                g.drawLine((int) cx - 13, (int) cy, (int) cx + 8, (int) cy);
                g.fillPolygon(new int[]{(int) cx + 8, (int) cx + 15, (int) cx + 8}, new int[]{(int) cy - 7, (int) cy, (int) cy + 7}, 3);
            }
            case HAND_SPIKES -> {
                for (int i = -1; i <= 1; i++)
                    g.drawLine((int) cx + i * 7, (int) cy + 10, (int) cx + i * 4, (int) cy - 10);
            }
            case HAND_GRASP -> {
                g.drawArc((int) cx - 17, (int) cy - 8, 22, 18, -20, 210);
                g.drawArc((int) cx - 5, (int) cy - 8, 22, 18, -190, 210);
                g.fillOval((int) cx - 4, (int) cy - 4, 8, 8);
            }
            case HAND_SLAM -> {
                g.drawLine((int) cx, (int) cy - 13, (int) cx, (int) cy + 6);
                g.drawLine((int) cx - 13, (int) cy + 9, (int) cx - 4, (int) cy + 3);
                g.drawLine((int) cx + 4, (int) cy + 3, (int) cx + 13, (int) cy + 9);
            }
            case HAND_AURA -> {
                for (int i = 0; i < 6; i++) {
                    double a = i * Math.PI / 3 + 0.4;
                    g.drawLine((int) (cx + Math.cos(a) * 4), (int) (cy + Math.sin(a) * 3),
                            (int) (cx + Math.cos(a) * 14), (int) (cy + Math.sin(a) * 11));
                }
            }
        }
    }

    private void bossBars(Graphics2D g, World w, int vw, int vh) {
        ArrayList<Fighter> bosses = new ArrayList<>();
        for (Fighter e : w.enemies) if (e.isBoss && !e.dead) bosses.add(e);
        if (w.boss != null && !w.boss.dead && !bosses.contains(w.boss)) bosses.add(w.boss);
        for (int i = 0; i < bosses.size(); i++) bossBar(g, bosses.get(i), vw, vh, i, bosses.size());
    }

    private void bossBar(Graphics2D g, Fighter boss, int vw, int vh, int index, int total) {
        int bw = 520, bx = (vw - bw) / 2, by = vh - 62;
        by -= (total - 1 - index) * 50;
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        g.setColor(Color.WHITE);
        g.drawString(boss.name.toUpperCase(), bx, by - 8);
        g.setColor(new Color(10, 12, 18, 210));
        g.fillRoundRect(bx - 3, by - 3, bw + 6, 22, 10, 10);
        g.setColor(new Color(70, 24, 32));
        g.fillRoundRect(bx, by, bw, 16, 8, 8);
        float v = FMath.clamp(boss.hp / boss.maxHp, 0, 1);
        g.setPaint(new GradientPaint(bx, by, new Color(255, 110, 90), bx, by + 16, new Color(160, 20, 40)));
        g.fillRoundRect(bx, by, (int) (bw * v), 16, 8, 8);
        g.setColor(new Color(255, 220, 200, 80));
        g.fillRoundRect(bx, by, (int) (bw * v), 6, 8, 8);
        int spy = by + 23;
        g.setColor(new Color(10, 12, 18, 210));
        g.fillRoundRect(bx - 3, spy - 2, bw + 6, 14, 7, 7);
        g.setColor(new Color(22, 46, 82));
        g.fillRoundRect(bx, spy, bw, 9, 5, 5);
        float sv = FMath.clamp(boss.sp / boss.maxSp, 0, 1);
        g.setColor(new Color(80, 165, 255));
        g.fillRoundRect(bx, spy, (int) (bw * sv), 9, 5, 5);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 9));
        g.setColor(new Color(205, 225, 255));
        g.drawString("SP", bx - 22, spy + 8);
    }

    private void banners(Graphics2D g, World w, int vw) {
        if (w.banners.isEmpty()) return;
        String[] b = w.banners.get(0);
        float t = w.bannerT;
        float alpha = FMath.clamp(Math.min(t, 3.4f - t) * 2.4f, 0, 1);
        Composite old = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        Font big = new Font(Font.SANS_SERIF, Font.BOLD, b.length > 1 && !b[1].isEmpty() ? 34 : 22);
        g.setFont(big);
        centerShadow(g, b[0], vw / 2f, 128, b[0].equals("VICTORY") ? new Color(255, 220, 120) : Color.WHITE);
        if (b.length > 1 && !b[1].isEmpty()) {
            g.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 16));
            centerShadow(g, b[1], vw / 2f, 158, new Color(225, 225, 235));
        }
        g.setComposite(old);
    }

    private void helpPanel(Graphics2D g, Player p, int vw, int vh) {
        ArrayList<String> lines = new ArrayList<>();
        lines.add("CONTROLS");
        lines.add("A / D - move        W - jump / double jump / wall jump");
        lines.add("S - fast fall / drop through platforms");
        lines.add("SHIFT - dash (i-frames)     K (hold) - GUARD: blocks 90%, no stun");
        lines.add("L (hold) - recharge: crouch 2s, then rapid SP and limited HP regen");
        lines.add("J - attack (3-hit combo)");
        lines.add("Q / E or 1-6 - select form     F - cast form");
        lines.add("H - toggle this panel    ESC - pause    R - retry");
        lines.add("Attacks, dash, extra jumps cost SP. Hit zero: 3s lockout!");
        if (p.isDemon) {
            lines.add("Demons burn under open sky in daylight. Stay covered!");
            lines.add("Wisteria trees POISON demons nearby - purple haze drains HP+SP.");
            lines.add("Big hits can sever demon limbs - regeneration restores them.");
        } else {
            lines.add("Melee hits restore SP. Forms spend it.");
            lines.add("Sword sheaths after 5s of peace - attack to redraw.");
        }
        int bw2 = 480, bh2 = lines.size() * 24 + 30;
        int bx = (vw - bw2) / 2, by = (vh - bh2) / 2;
        g.setColor(new Color(8, 10, 16, 225));
        g.fillRoundRect(bx, by, bw2, bh2, 16, 16);
        g.setColor(new Color(120, 126, 145));
        g.drawRoundRect(bx, by, bw2, bh2, 16, 16);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 17));
        g.setColor(Color.WHITE);
        g.drawString(lines.get(0), bx + 24, by + 34);
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        for (int i = 1; i < lines.size(); i++) {
            g.setColor(i == lines.size() - 1 ? new Color(255, 200, 120) : new Color(210, 214, 226));
            g.drawString(lines.get(i), bx + 24, by + 34 + i * 24);
        }
    }

    private void hint(Graphics2D g, String s, float x, float y, boolean right) {
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        g.setColor(new Color(200, 205, 218, 160));
        g.drawString(s, x - g.getFontMetrics().stringWidth(s), y);
    }

    private void centerShadow(Graphics2D g, String s, float cx, float y, Color c) {
        int wdt = g.getFontMetrics().stringWidth(s);
        g.setColor(new Color(8, 8, 12, 200));
        g.drawString(s, cx - wdt / 2f + 2, y + 2);
        g.setColor(c);
        g.drawString(s, cx - wdt / 2f, y);
    }
}
