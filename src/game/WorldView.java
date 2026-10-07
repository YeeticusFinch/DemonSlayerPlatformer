package game;

import java.awt.*;
import java.awt.geom.Path2D;

final class WorldView {

    static void render(World w, Graphics2D g, Glow glow, int vw, int vh) {
        float fearN = FMath.clamp(w.fearLevel / 10f, 0, 1);
        boolean inverted = fearInvertActive(fearN);
        boolean upsideDown = inverted && w.fearLevel >= 8f && ((int) Math.floor(Game.time * fearPulseRate(fearN))) % 3 == 0;
        if (upsideDown) {
            Graphics2D ug = (Graphics2D) g.create();
            ug.translate(vw, vh);
            ug.rotate(Math.PI);
            renderScene(w, ug, glow, vw, vh, inverted);
            ug.dispose();
        } else {
            renderScene(w, g, glow, vw, vh, inverted);
        }
    }

    private static void renderScene(World w, Graphics2D g, Glow glow, int vw, int vh, boolean inverted) {
        float fearN = FMath.clamp(w.fearLevel / 10f, 0, 1);
        float fearShake = fearN * 12f;
        if (inverted) fearShake += fearN * 20f;
        float camX = w.cam.x + w.cam.offX() + FMath.sin(Game.time * 37f) * fearShake;
        float camY = w.cam.y + w.cam.offY() + FMath.sin(Game.time * 43f + 1.7f) * fearShake * 0.7f;
        boolean night = !w.level.daytime;

        BackgroundRenderer.render(g, w, camX, camY, vw, vh);
        renderFearBackgroundTint(g, w, vw, vh);
        Graphics2D ag = (Graphics2D) g.create();
        ag.translate(-camX, -camY);
        renderMuzanAuras(ag, w);
        ag.dispose();
        renderFearBackgroundFigure(g, w, vw, vh);

        Graphics2D wg = (Graphics2D) g.create();
        wg.translate(-camX, -camY);

        for (Level.Deco d : w.level.decos)
            if (d.x > w.viewL - 200 && d.x < w.viewR + 200) PlatformArt.deco(wg, d, night);
        if (w.level.boulder != null) PlatformArt.boulder(wg, w);
        for (Level.Plat p : w.level.plats)
            if (p.x + p.w > w.viewL && p.x < w.viewR && p.y + p.h > w.viewT && p.y < w.viewB)
                PlatformArt.plat(wg, p, night);
        for (Trap t : w.traps)
            if (t.inView(w, 120)) t.render(wg, night);
        if (w.level.theme == Level.Theme.CITY) renderCars(wg, w, night);
        for (Level.Deco d : w.level.decos)
            if (d.type == Level.CAMPFIRE && d.x > w.viewL && d.x < w.viewR) PlatformArt.campfireFlames(wg, d);
        if (!w.level.dojo) PlatformArt.goal(wg, w);

        for (World.Pickup p : w.pickups) PlatformArt.pickup(wg, p);
        for (Ghost gh : w.ghosts) gh.render(wg);
        for (Fighter e : w.enemies)
            if (e.x > w.viewL - 160 && e.x < w.viewR + 160 && e.y > w.viewT - 200 && e.y < w.viewB + 300) e.render(wg);
        w.player.render(wg);
        for (Effect e : w.effects)
            if (e.x > w.viewL - 220 && e.x < w.viewR + 220 && e.y > w.viewT - 240 && e.y < w.viewB + 320) e.render(wg);
        w.parts.render(wg);
        for (Debris d : w.debris) d.render(wg);

        Font oldF = wg.getFont();
        wg.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        for (World.Popup p : w.popups) {
            float a = 1 - Math.max(0, p.t - 0.6f) / 0.4f;
            wg.setColor(new Color(10, 10, 14, (int) (170 * a)));
            wg.drawString(p.text, p.x - p.text.length() * 4.2f + 1.5f, p.y + 1.5f);
            wg.setColor(new Color(p.color.getRed(), p.color.getGreen(), p.color.getBlue(), (int) (255 * a)));
            wg.drawString(p.text, p.x - p.text.length() * 4.2f, p.y);
        }
        wg.setFont(oldF);
        wg.dispose();

        if (night) {
            g.setColor(new Color(18, 22, 52, 42));
            g.fillRect(0, 0, vw, vh);
        }
        if (w.level.underwater) {
            g.setPaint(new GradientPaint(0, 0, new Color(8, 46, 58, 120), 0, vh, new Color(0, 14, 20, 190)));
            g.fillRect(0, 0, vw, vh);
            g.setColor(new Color(170, 230, 220, 32));
            for (int i = 0; i < 24; i++) {
                float x = ((i * 173 + Game.time * (12 + i % 5)) % (vw + 120)) - 60;
                float y = (i * 47 + FMath.sin(Game.time * 0.9f + i) * 16) % vh;
                g.fillOval((int) x, (int) y, 5 + i % 4, 5 + i % 4);
            }
        }
        vignette(g, vw, vh, w);

        Graphics2D eg = glow.begin(camX, camY, vw, vh);
        for (Effect e : w.effects) {
            if (e.kind == Effect.HAND_SPIKE || e.kind == Effect.HAND_SWING || e.kind == Effect.HAND_CHARGE || e.kind == Effect.HAND_AURA
                    || e.kind == Effect.SWAMP_HANDS || e.kind == Effect.TEMARI || e.kind == Effect.BOULDER
                    || e.x <= w.viewL - 220 || e.x >= w.viewR + 220 || e.y <= w.viewT - 240 || e.y >= w.viewB + 320)
                continue;
            int passes = flameGlowEffect(e) ? 4 : 2;
            for (int pass = 0; pass < passes; pass++) e.render(eg);
        }
        w.parts.render(eg);
        for (Ghost gh : w.ghosts) gh.render(eg);
        glow.end(g, vw, vh);

        Graphics2D gg = (Graphics2D) g.create();
        gg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        gg.setComposite(Glow.ADD);
        gg.translate(-camX, -camY);
        for (Level.Deco d : w.level.decos) if (d.x > w.viewL - 200 && d.x < w.viewR + 200) PlatformArt.decoGlow(gg, w, d);
        for (Fighter e : w.enemies)
            if (e instanceof Enemy en && !e.dead) en.renderGlow(gg);
        w.player.renderGlow(gg);
        gg.dispose();

        BackgroundRenderer.screenCelestialGlow(g, w, camX, camY, vw, vh);

        float wyStart = w.level.h - 330;
        float syF = wyStart - camY;
        if (syF < vh) {
            float sy0 = Math.max(0f, syF);
            g.setPaint(new GradientPaint(0, sy0, new Color(0, 0, 0, 0), 0, syF + 380, new Color(0, 0, 0, 255)));
            g.fillRect(0, (int) sy0, vw, vh - (int) sy0 + 2);
        }
        renderFearScreen(g, w, vw, vh, inverted);
    }

    private static void renderMuzanAuras(Graphics2D g, World w) {
        for (Fighter e : w.enemies) {
            if (!(e instanceof Muzan) || e.dead) continue;
            float r = World.MUZAN_FEAR_RADIUS;
            for (int i = 0; i < 5; i++) {
                float k = i / 4f;
                float rr = FMath.lerp(r, r * 0.42f, k);
                int a = (int) FMath.lerp(34, 82, k);
                g.setColor(new Color(0, 0, 0, a));
                g.fillOval((int) (e.x - rr), (int) (e.y - rr), (int) (rr * 2), (int) (rr * 2));
            }
        }
    }

    private static void renderFearScreen(Graphics2D g, World w, int vw, int vh, boolean inverted) {
        float n = FMath.clamp(w.fearLevel / 10f, 0, 1);
        if (n <= 0.001f) return;
        int lines = (int) (12 + n * 78);
        for (int i = 0; i < lines; i++) {
            float seed = i * 19.137f;
            int x = (int) ((fract(seed * 0.23f + Game.time * (0.07f + n * 0.08f)) * (vw + 80)) - 40);
            int y = (int) (FMath.sin(seed + Game.time * 6f) * 18f * n);
            int h = (int) (vh * FMath.lerp(0.24f, 0.9f, n) * (0.55f + 0.45f * fract(seed * 1.7f)));
            int wid = 1 + (int) (n * 4 + fract(seed) * n * 5);
            int a = (int) (24 + 126 * n * fract(seed * 3.1f));
            g.setColor(new Color(6, 5, 4, a));
            g.fillRect(x, y, wid, h);
        }
        if (inverted) {
            g.setXORMode(Color.WHITE);
            g.fillRect(0, 0, vw, vh);
            g.setPaintMode();
        }
    }

    private static float fearPulseRate(float n) {
        return 0.95f + n * 0.8f;
    }

    private static boolean fearInvertActive(float n) {
        if (n <= 0.001f) return false;
        float shortPulse = fract(Game.time * fearPulseRate(n));
        if (shortPulse < 0.045f + 0.095f * n) return true;
        float longRate = 0.28f + n * 0.18f;
        float longPulse = fract(Game.time * longRate + 0.37f);
        return longPulse < 0.75f * longRate * FMath.clamp((n - 0.35f) / 0.65f, 0, 1);
    }

    private static void renderFearBackgroundTint(Graphics2D g, World w, int vw, int vh) {
        float n = FMath.clamp(w.fearLevel / 10f, 0, 1);
        if (n <= 0.001f) return;
        int tintA = (int) (204 * n);
        if (tintA <= 0) return;
        g.setColor(new Color(70, 61, 50, tintA));
        g.fillRect(0, 0, vw, vh);
    }

    private static void renderFearBackgroundFigure(Graphics2D g, World w, int vw, int vh) {
        if (w.fearSource == null || w.fearLevel < 5f) return;
        float a = FMath.clamp(0.5f + (w.fearLevel - 5f) / 10f, 0.5f, 1f);
        Composite old = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, a));
        Graphics2D bg = (Graphics2D) g.create();
        bg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        bg.translate(vw * 0.5f, vh * 1.5f);
        bg.scale(10.0, 10.0);
        bg.translate(-w.fearSource.x, -w.fearSource.bottom());
        w.fearSource.render(bg);
        if (w.fearSource instanceof Enemy en) {
            Composite bgOld = bg.getComposite();
            bg.setComposite(Glow.ADD);
            en.renderGlow(bg);
            bg.setComposite(bgOld);
        }
        bg.dispose();
        g.setComposite(old);
    }

    private static float fract(float v) {
        return v - (float) Math.floor(v);
    }

    private static void vignette(Graphics2D g, int vw, int vh, World w) {
        Player pl = w.player;
        float danger = 0;
        if (!pl.dead && pl.hp < 35) danger = 1 - pl.hp / 35f;
        if (pl.burning) danger = Math.max(danger, 0.55f + 0.2f * FMath.sin(Game.time * 9));
        float dmgFlash = w.playerDmgFlash;
        float pois = pl.wisteriaT > 0 ? 0.45f + 0.15f * FMath.sin(Game.time * 8) : 0;
        if (danger <= 0.01f && dmgFlash <= 0.01f && pois <= 0.01f) return;
        float m = 90;
        if (pois > 0.01f) {
            g.setPaint(new GradientPaint(0, 0, new Color(150, 70, 220, (int) (95 * pois)), vw / 2f, vh / 2f,
                    new Color(150, 70, 220, (int) (30 * pois)), true));
            g.setStroke(new BasicStroke(m * 2f));
            g.draw(new Rectangle((int) -m, (int) -m, (int) (vw + m * 2), (int) (vh + m * 2)));
        }
        float edgeA = (int) (Math.min(1, danger * 110 + dmgFlash * 150));
        g.setPaint(new GradientPaint(0, 0, new Color(190, 15, 30, (int) (edgeA * 0.55f)), vw / 2f, vh / 2f,
                new Color(190, 15, 30, (int) (edgeA * 1.1f)), true));
        g.setStroke(new BasicStroke(m * 2f));
        g.draw(new Rectangle((int)-m, (int)-m, (int)(vw + m * 2), (int)(vh + m * 2)));
        if (dmgFlash > 0.02f) {
            g.setColor(new Color(210, 20, 35, (int) (70 * dmgFlash)));
            g.fillRect(0, 0, vw, vh);
        }
    }

    private static void renderCars(Graphics2D g, World w, boolean night) {
        float roadY = 718;
        for (int i = 0; i < 5; i++) {
            float speed = 95 + i * 18;
            float span = w.level.w + 520;
            float x = (Game.time * speed + i * 640) % span - 260;
            PlatformArt.car(g, x, roadY - (i % 2) * 34, 0.78f, night);
        }
    }

    private static boolean flameGlowEffect(Effect e) {
        return e.kind == Effect.FLAME_RIBBON || e.kind == Effect.TIGER || e.kind == Effect.TIGER_HEAD
                || e.kind == Effect.SLAM_RING || e.c1.getRed() > 220 && e.c1.getGreen() < 170 && e.c1.getBlue() < 100;
    }
}
