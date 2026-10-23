package game;

import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;

final class EffectArt {
    private static Svg flameDragonSvg;
    private static Svg.Group flameDragonGroup;
    private static boolean flameDragonTried;
    private static Svg akazaCompassSvg;
    private static Svg.Group akazaCompassGroup, akazaCompassGlowGroup;
    private static boolean akazaCompassTried;
    private static Svg akazaCloneSvg;
    private static Svg.Group akazaCloneHead, akazaCloneArmFront, akazaCloneArmBack,
            akazaCloneTorso, akazaCloneLegFront, akazaCloneLegBack;
    private static boolean akazaCloneTried;

    static void render(Graphics2D g, Effect e) {
        float t = 1f - e.life / e.maxLife;
        Composite old = g.getComposite();
        switch (e.kind) {
            case Effect.ARC -> arc(g, e, t);
            case Effect.WAVE -> wave(g, e, t);
            case Effect.WHEEL -> wheel(g, e, t);
            case Effect.WHIRL -> whirl(g, e, t);
            case Effect.FLUX -> flux(g, e, t);
            case Effect.TIGER -> tiger(g, e, t);
            case Effect.BLOOD_BOLT -> bolt(g, e, t);
            case Effect.CLAW_WAVE -> claw(g, e, t);
            case Effect.NOVA -> nova(g, e, t);
            case Effect.SHOCK_GROUND -> shock(g, e, t);
            case Effect.HEAL_AURA -> heal(g, e, t);
            case Effect.SLAM_RING -> slamRing(g, e, t);
            case Effect.FLAME_RIBBON -> ribbon(g, e, t);
            case Effect.HAND_SPIKE -> handSpike(g, e, t);
            case Effect.WATER_RIBBON -> waterRibbon(g, e, t);
            case Effect.TIGER_HEAD -> tigerHead(g, e, t);
            case Effect.WIND_RIBBON -> windRibbon(g, e, t);
            case Effect.HAND_SWING -> handSwing(g, e, t);
            case Effect.HAND_CHARGE -> handCharge(g, e, t);
            case Effect.HAND_AURA -> handAura(g, e, t);
            case Effect.SWAMP_CLOUD -> swampCloud(g, e, t);
            case Effect.SWAMP_HANDS -> swampHands(g, e, t);
            case Effect.SWAMP_PUDDLE -> swampPuddle(g, e, t);
            case Effect.TEMARI -> temari(g, e, t);
            case Effect.ARROW -> arrow(g, e, t);
            case Effect.BOULDER -> boulder(g, e, t);
            case Effect.THREAD -> thread(g, e, t);
            case Effect.FLAME_DRAGON -> flameDragon(g, e, t);
            case Effect.ENMU_WHISPER -> enmuWhisper(g, e, t);
            case Effect.ENMU_WHISPER_ORB -> enmuWhisperOrb(g, e, t);
            case Effect.ENMU_NIGHTMARE -> enmuNightmare(g, e, t);
            case Effect.ENMU_WHIP -> enmuWhip(g, e, t);
            case Effect.AKAZA_SHOCKWAVE -> akazaShockwave(g, e, t);
            case Effect.AKAZA_COMPASS -> akazaCompass(g, e, t);
            case Effect.AKAZA_PILLAR -> akazaPillar(g, e, t);
            case Effect.AKAZA_CLONE -> akazaClone(g, e, t);
            default -> {}
        }
        g.setComposite(old);
    }

    private static void arc(Graphics2D g, Effect e, float t) {
        float prog = FMath.easeOut(Math.min(1, t * 1.7f));
        float a0 = e.angle - e.ex1 / 2f;
        float sweep = e.ex1 * prog;
        float thick = (e.ex2 > 0 ? e.ex2 : e.r * 0.3f) * (1 - t * 0.35f);
        int alpha = (int) (200 * (1 - t));
        fillCrescent(g, e.x, e.y, e.r, a0, sweep, thick, new Color(e.c1.getRed(), e.c1.getGreen(), e.c1.getBlue(), Math.max(0, alpha)));
        fillCrescent(g, e.x, e.y, e.r - thick * 0.28f, a0 + sweep * 0.08f, sweep * 0.84f, thick * 0.38f,
                new Color(255, 255, 255, Math.max(0, (int) (170 * (1 - t)))));
        if (isFlame(e)) {
            float mid = a0 + sweep * 0.68f;
            flameGlare(g, e.x + FMath.cos(mid) * e.r, e.y + FMath.sin(mid) * e.r, thick * 1.7f, 0.8f * (1 - t));
        }
    }

    private static void fillCrescent(Graphics2D g, float cx, float cy, float r, float a0, float sweep, float thick, Color c) {
        if (sweep <= 0.01f || thick <= 0.5f) return;
        int n = 18;
        Path2D.Float p = new Path2D.Float();
        for (int i = 0; i <= n; i++) {
            float k = i / (float) n;
            float a = a0 + sweep * k;
            float rr = r + FMath.sin(k * (float) Math.PI) * thick * 0.18f;
            float px = cx + FMath.cos(a) * rr, py = cy + FMath.sin(a) * rr;
            if (i == 0) p.moveTo(px, py);
            else p.lineTo(px, py);
        }
        for (int i = n; i >= 0; i--) {
            float k = i / (float) n;
            float a = a0 + sweep * k;
            float th = thick * (float) Math.pow(Math.max(0, FMath.sin(k * (float) Math.PI)), 0.65f);
            float rr = r - th;
            p.lineTo(cx + FMath.cos(a) * rr, cy + FMath.sin(a) * rr);
        }
        p.closePath();
        g.setColor(c);
        g.fill(p);
    }

    private static void wave(Graphics2D g, Effect e, float t) {
        g.translate(e.x, e.y);
        g.rotate(Math.atan2(e.vy, e.vx));
        float s = e.r * (0.85f + t * 0.35f);
        for (int i = 2; i >= 0; i--) {
            int alpha = Math.max(0, ((int) (160 * (1 - t))) >> i);
            g.setColor(new Color(e.c1.getRed(), e.c1.getGreen(), e.c1.getBlue(), alpha));
            g.setStroke(new BasicStroke(13 - i * 3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawArc((int) (-s * 0.55), (int) (-s), (int) (s * 1.15), (int) (s * 2), -75, 150);
        }
        g.setColor(new Color(255, 255, 255, (int) (190 * (1 - t))));
        g.setStroke(new BasicStroke(3f));
        g.drawArc((int) (-s * 0.44), (int) (-s * 0.88), (int) (s * 0.94), (int) (s * 1.76), -70, 140);
        g.rotate(-Math.atan2(e.vy, e.vx));
        g.translate(-e.x, -e.y);
    }

    private static void wheel(Graphics2D g, Effect e, float t) {
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0, 0.92f * (1 - t))));
        g.translate(e.x, e.y);
        float rot = e.life * 14;
        g.rotate(rot);
        // jagged rim: teeth sticking out of the wheel's edge
        int teeth = 12;
        Polygon rim = new Polygon();
        for (int i = 0; i < teeth * 2; i++) {
            double a = i * Math.PI / teeth;
            float rr = i % 2 == 0 ? e.r * 1.2f : e.r * 0.9f;
            rim.addPoint((int) (Math.cos(a) * rr), (int) (Math.sin(a) * rr));
        }
        g.setColor(new Color(e.c1.getRed(), e.c1.getGreen(), e.c1.getBlue(), 200));
        g.setStroke(new BasicStroke(9f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawPolygon(rim);
        g.setColor(new Color(e.c1.getRed(), e.c1.getGreen(), e.c1.getBlue(), 170));
        g.setStroke(new BasicStroke(5.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawOval((int) -e.r, (int) -e.r, (int) (e.r * 2), (int) (e.r * 2));
        g.setColor(new Color(e.c2.getRed(), e.c2.getGreen(), e.c2.getBlue(), 220));
        g.setStroke(new BasicStroke(3.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawPolygon(rim);
        g.drawOval((int) -e.r, (int) -e.r, (int) (e.r * 2), (int) (e.r * 2));
        g.setStroke(new BasicStroke(6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(new Color(e.c2.getRed(), e.c2.getGreen(), e.c2.getBlue(), 210));
        for (int i = 0; i < 3; i++) {
            float a = (float) (i * 2.0944f);
            g.drawLine((int) (Math.cos(a) * e.r * 0.15f), (int) (Math.sin(a) * e.r * 0.15f),
                    (int) (Math.cos(a) * e.r * 0.88f), (int) (Math.sin(a) * e.r * 0.88f));
        }
        g.rotate(-rot);
        g.translate(-e.x, -e.y);
    }

    private static void whirl(Graphics2D g, Effect e, float t) {
        if (e.ex2 == 3) {
            float elapsed = e.maxLife - e.life;
            float grow = FMath.clamp(elapsed / 0.5f, 0, 1);
            float maxTu = 17f * grow;
            if (maxTu <= 0.05f) return;
            float fade = Math.max(0, 0.92f * FMath.clamp(e.life / 0.25f, 0, 1));
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fade));
            float height = e.r * 2.25f;
            float yScale = height / 34f;
            float xScale = e.r * 0.1f;
            float aBase = -elapsed * 8.5f;
            boolean flame = e.c1.getRed() > e.c1.getGreen() * 1.25f;
            for (int arm = 0; arm < 7; arm++) {
                Path2D.Float jag = new Path2D.Float();
                float a = aBase - arm * 0.85f;
                int steps = Math.max(3, (int) (34 * grow));
                for (int i = 0; i <= steps; i++) {
                    float tu = i / (float) steps * maxTu;
                    float x = 0.5f * FMath.sin(0.4f * tu - a)
                            + 0.2f * (7f + (0.5f * tu - 2f) * (0.5f * tu - 2f)) * FMath.sin(6f * (a + tu));
                    float y = 2f * tu;
                    float px = e.x + x * xScale + FMath.sin(tu * 2.7f + arm) * 4f;
                    float py = e.y - y * yScale;
                    if (i == 0) jag.moveTo(px, py);
                    else jag.lineTo(px, py);
                }
                g.setStroke(new BasicStroke(arm % 2 == 0 ? 18f : 12f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
                g.setColor(arm % 2 == 0
                        ? (flame ? new Color(105, 24, 10, 210) : new Color(5, 8, 6, 190))
                        : (flame ? new Color(255, 88, 20, 190) : new Color(38, 210, 86, 170)));
                g.draw(jag);
                g.setStroke(new BasicStroke(4f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
                g.setColor(flame ? new Color(255, 228, 148, 180) : new Color(190, 255, 200, 150));
                if (arm % 2 != 0) g.draw(jag);
            }
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fade * 0.45f));
            g.setColor(flame ? new Color(255, 64, 12, 100) : new Color(42, 220, 90, 80));
            g.fillOval((int) (e.x - e.r * 0.95f), (int) (e.y - height * grow), (int) (e.r * 1.9f), (int) (height * grow));
            return;
        }
        if (e.ex2 == 4) {
            float fade = Math.max(0, 0.86f * (1 - t * 0.25f));
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fade));
            float spin = e.life * 13f;
            boolean flame = e.c1.getRed() > e.c1.getGreen() * 1.25f;
            for (int arm = 0; arm < 8; arm++) {
                float a = spin + arm * 0.78f;
                float inner = e.r * 0.25f;
                float outer = e.r * (0.9f + FMath.sin(spin + arm) * 0.16f);
                int x1 = (int) (e.x + FMath.cos(a) * inner);
                int y1 = (int) (e.y + FMath.sin(a) * inner * 0.75f);
                int x2 = (int) (e.x + FMath.cos(a + 0.7f) * outer);
                int y2 = (int) (e.y + FMath.sin(a + 0.7f) * outer * 0.75f);
                g.setStroke(new BasicStroke(arm % 2 == 0 ? 7f : 4f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
                g.setColor(arm % 2 == 0
                        ? (flame ? new Color(120, 25, 8, 210) : new Color(8, 12, 10, 200))
                        : (flame ? new Color(255, 90, 18, 200) : new Color(62, 225, 102, 185)));
                g.drawLine(x1, y1, x2, y2);
            }
            g.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(flame ? new Color(255, 225, 145, 180) : new Color(210, 255, 215, 150));
            g.drawOval((int) (e.x - e.r * 0.58f), (int) (e.y - e.r * 0.42f), (int) (e.r * 1.16f), (int) (e.r * 0.84f));
            return;
        }
        if (e.ex2 == 5) {
            float fade = Math.max(0, 0.94f * (1 - t * 0.38f));
            float dir = e.ex1 == 0 ? 1 : Math.signum(e.ex1);
            Graphics2D gg = (Graphics2D) g.create();
            gg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fade));
            gg.translate(e.x, e.y);
            gg.scale(dir, 1);
            for (int arm = 0; arm < 8; arm++) {
                Path2D.Float p = new Path2D.Float();
                float phase = e.life * 13f + arm * 0.78f;
                for (int i = 0; i <= 26; i++) {
                    float k = i / 26f;
                    float px = -e.r * 0.75f + k * e.r * 2.35f;
                    float amp = e.r * (0.18f + FMath.sin(k * (float) Math.PI) * 0.72f);
                    float py = FMath.sin(k * 15f + phase) * amp + FMath.sin(k * 47f + phase) * 9f;
                    if (i % 2 == 1) py += FMath.sin(phase + i) * 14f;
                    if (i == 0) p.moveTo(px, py);
                    else p.lineTo(px, py);
                }
                gg.setStroke(new BasicStroke(arm % 2 == 0 ? 12f : 8f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
                gg.setColor(arm % 2 == 0 ? new Color(4, 6, 6, 215) : new Color(172, 18, 32, 200));
                gg.draw(p);
                gg.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
                gg.setColor(new Color(235, 245, 230, 130));
                if (arm % 3 == 1) gg.draw(p);
            }
            gg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fade * 0.35f));
            gg.setColor(new Color(180, 12, 28, 90));
            gg.fillOval((int) (-e.r * 0.55f), (int) (-e.r * 0.82f), (int) (e.r * 2.1f), (int) (e.r * 1.64f));
            gg.dispose();
            return;
        }
        if (e.ex2 == 6) {
            float elapsed = e.maxLife - e.life;
            float grow = FMath.clamp(elapsed / 0.5f, 0, 1);
            if (grow <= 0.01f) return;
            float fade = 0.92f * FMath.clamp(elapsed / 0.12f, 0, 1) * FMath.clamp(e.life / 0.12f, 0, 1);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fade));
            float spin = (e.maxLife - e.life) * 12f;
            for (int arm = 0; arm < 6; arm++) {
                Path2D.Float flame = new Path2D.Float();
                float phase = spin + arm * 1.05f;
                int steps = Math.max(2, (int) (27 * grow));
                for (int i = 0; i <= steps; i++) {
                    float k = i / 27f;
                    float amp = e.r * (0.55f - k * 0.28f) * (0.45f + 0.55f * grow);
                    float px = e.x + FMath.sin(phase + k * 10f) * amp;
                    float py = e.y + e.r * 0.55f - k * e.r * 2.15f;
                    if (i == 0) flame.moveTo(px, py);
                    else flame.lineTo(px, py);
                }
                g.setStroke(new BasicStroke(arm % 2 == 0 ? 15f : 10f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.setColor(arm % 2 == 0 ? new Color(210, 18, 72, 180) : new Color(255, 76, 178, 205));
                g.draw(flame);
                g.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.setColor(new Color(255, 205, 232, 170));
                if (arm % 2 != 0) g.draw(flame);
            }
            g.setColor(new Color(255, 48, 142, 55));
            float h = e.r * 2.1f * grow;
            g.fillOval((int) (e.x - e.r * 0.72f), (int) (e.y + e.r * 0.55f - h), (int) (e.r * 1.44f), (int) h);
            return;
        }
        if (e.ex2 == 2) {
            float fade = Math.max(0, 0.95f * (1 - t * 0.12f));
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fade));
            float spin = (e.maxLife - e.life) * 9.5f;
            for (int arm = 0; arm < 2; arm++) {
                Path2D.Float flame = new Path2D.Float();
                float base = spin + arm * (float) Math.PI;
                for (int i = 0; i < 46; i++) {
                    float k = i / 45f;
                    float rr = e.r * (0.08f + k * 1.24f);
                    float und = FMath.sin(e.life * 15f + k * 18f) * e.r * 0.045f * (1 - k * 0.35f);
                    float a = base + k * 6.2f + FMath.sin(e.life * 6f + k * 8f) * 0.22f;
                    float px = e.x + FMath.cos(a) * (rr + und);
                    float py = e.y + FMath.sin(a) * (rr * 0.68f + und * 0.35f);
                    if (i == 0) flame.moveTo(px, py);
                    else flame.lineTo(px, py);
                }
                g.setStroke(new BasicStroke(28f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.setColor(new Color(180, 35, 8, 125));
                g.draw(flame);
                g.setStroke(new BasicStroke(17f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.setColor(new Color(e.c1.getRed(), e.c1.getGreen(), e.c1.getBlue(), 220));
                g.draw(flame);
                g.setStroke(new BasicStroke(7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.setColor(new Color(e.c2.getRed(), e.c2.getGreen(), e.c2.getBlue(), 235));
                g.draw(flame);
                for (int p = 0; p < 7; p++) {
                    float pulse = fract((e.maxLife - e.life) * 2.8f + p / 7f);
                    float rr = e.r * (0.18f + pulse * 1.16f);
                    float a = base + pulse * 6.2f + FMath.sin(e.life * 6f + pulse * 8f) * 0.22f;
                    float px = e.x + FMath.cos(a) * rr;
                    float py = e.y + FMath.sin(a) * rr * 0.68f;
                    float nx = FMath.cos(a + 0.9f), ny = FMath.sin(a + 0.9f) * 0.68f;
                    g.setStroke(new BasicStroke(10f - p * 0.65f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g.setColor(p % 3 == 0 ? new Color(255, 224, 68, 210) : p % 3 == 1 ? new Color(255, 116, 24, 205) : new Color(210, 42, 8, 200));
                    g.drawLine((int) px, (int) py, (int) (px + nx * 54), (int) (py + ny * 54));
                    flameGlare(g, px, py, 38f * (1 - pulse * 0.25f), 0.65f * (1 - t * 0.35f));
                }
            }
            for (int i = 0; i < 18; i++) {
                float a = spin * 1.25f + i * 0.72f;
                float rr = e.r * (0.16f + (i % 9) * 0.095f);
                float px = e.x + FMath.cos(a) * rr;
                float py = e.y + FMath.sin(a) * rr * 0.72f;
                float s = 12 + (i % 4) * 4;
                g.setColor(i % 3 == 0 ? new Color(255, 220, 58, 190) : i % 3 == 1 ? new Color(255, 120, 28, 190) : new Color(210, 42, 8, 180));
                g.fillOval((int) (px - s * 0.5f), (int) (py - s * 0.5f), (int) s, (int) s);
            }
            g.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(new Color(255, 245, 180, 190));
            g.drawOval((int) (e.x - e.r * 0.18f), (int) (e.y - e.r * 0.12f), (int) (e.r * 0.36f), (int) (e.r * 0.24f));
            return;
        }
        if (e.ex2 == 1) {
            float fade = Math.max(0, 0.78f * (1 - t * 0.55f));
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fade));
            g.setColor(new Color(e.c1.getRed(), e.c1.getGreen(), e.c1.getBlue(), 42));
            g.fillOval((int) (e.x - e.r * 0.85f), (int) (e.y - e.r * 0.55f), (int) (e.r * 1.7f), (int) (e.r * 1.1f));
            for (int line = 0; line < 4; line++) {
                Path2D.Float p = new Path2D.Float();
                for (int i = 0; i < 30; i++) {
                    float k = i / 29f;
                    float amp = e.r * (0.95f - k * 0.62f);
                    float ph = e.life * 9f + line * 1.55f + k * 9.5f;
                    float px = e.x + FMath.sin(ph) * amp;
                    float py = e.y + (k - 0.48f) * e.r * 1.85f;
                    if (i == 0) p.moveTo(px, py);
                    else p.lineTo(px, py);
                }
                g.setStroke(new BasicStroke(7f - line, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.setColor(new Color(e.c1.getRed(), e.c1.getGreen(), e.c1.getBlue(), 135 - line * 18));
                g.draw(p);
                g.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.setColor(new Color(240, 252, 255, 150 - line * 20));
                g.draw(p);
            }
            g.setStroke(new BasicStroke(2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(new Color(235, 250, 255, 155));
            for (int i = 0; i < 9; i++) {
                float a = e.life * 8f + i * 0.7f;
                float sx = e.x + FMath.cos(a) * e.r * (0.35f + i * 0.055f);
                float sy = e.y + FMath.sin(a * 1.3f) * e.r * 0.55f;
                g.drawLine((int) sx, (int) sy, (int) (sx + FMath.cos(a) * 8), (int) (sy - 13 - i % 3 * 4));
            }
            return;
        }
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0, 0.5f * (1 - t * 0.4f))));
        g.setColor(new Color(e.c1.getRed(), e.c1.getGreen(), e.c1.getBlue(), 60));
        g.fillOval((int) (e.x - e.r), (int) (e.y - e.r * 1.15f), (int) (e.r * 2), (int) (e.r * 2.3f));
        g.setStroke(new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        float ph = e.life * 9;
        for (int i = 0; i < 4; i++) {
            g.setColor(new Color(e.c2.getRed(), e.c2.getGreen(), e.c2.getBlue(), 155 - i * 25));
            float rr = e.r * (0.32f + i * 0.21f);
            g.drawArc((int) (e.x - rr), (int) (e.y - rr * 1.1f), (int) (rr * 2), (int) (rr * 2.2f),
                    (int) ((ph + i * 87) % 360), 150);
        }
    }

    private static void flux(Graphics2D g, Effect e, float t) {
        float ph = e.life * 10;
        for (int i = 0; i < 13; i++) {
            float k = i / 13f;
            float a = ph + k * 9f;
            float rad = 62 + FMath.sin(ph * 1.4f + i) * 16;
            float px = e.x + FMath.cos(a) * rad;
            float py = e.y + FMath.sin(a) * rad * 0.65f - 20 + FMath.sin(i * 1.7f + ph) * 8;
            int s = (int) (17 - i * 0.7f + FMath.sin(ph * 2 + i) * 3);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0, 0.75f - k * 0.55f)));
            g.setColor(new Color(e.c1.getRed(), e.c1.getGreen(), e.c1.getBlue()));
            g.fillOval((int) (px - s / 2f), (int) (py - s / 2f), s, s);
            if (isFlame(e)) flameGlare(g, px, py, s * 1.8f, 0.35f * (1 - k));
            g.setColor(new Color(e.c2.getRed(), e.c2.getGreen(), e.c2.getBlue(), 200));
            g.fillOval((int) (px - s / 5f), (int) (py - s / 5f), Math.max(1, s / 3), Math.max(1, s / 3));
        }
    }

    private static void tiger(Graphics2D g, Effect e, float t) {
        g.translate(e.x, e.y);
        g.rotate(Math.atan2(e.vy, e.vx) + FMath.sin(t * 22) * 0.08f);
        float s = e.r * (1f - t * 0.25f);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0, 0.95f * (1 - t * 0.6f))));
        g.setColor(new Color(255, 90, 20, 220));
        g.fillPolygon(new int[]{(int) (-s), (int) (s * 0.2f), (int) (s * 0.05f), (int) (s)},
                new int[]{(int) (-s * 0.55f), (int) (-s * 0.18f), (int) (s * 0.18f), (int) (s * 0.55f)}, 4);
        g.setColor(new Color(255, 190, 40));
        g.fillPolygon(new int[]{(int) (-s * 0.7f), (int) (s * 0.15f), (int) (s * 0.7f)},
                new int[]{(int) (-s * 0.28f), 0, (int) (s * 0.28f)}, 3);
        g.setColor(new Color(255, 245, 180));
        g.fillOval((int) (s * 0.25f), -4, 9, 8);
        flameGlare(g, s * 0.28f, 0, s * 1.2f, 0.85f * (1 - t));
        g.rotate(-Math.atan2(e.vy, e.vx) - FMath.sin(t * 22) * 0.08f);
        g.translate(-e.x, -e.y);
    }

    private static void bolt(Graphics2D g, Effect e, float t) {
        float a = (float) Math.atan2(e.vy, e.vx);
        g.translate(e.x, e.y);
        g.rotate(a);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0, 1 - t * 0.4f)));
        g.setColor(new Color(140, 10, 25));
        g.fillPolygon(new int[]{-22, -8, 16, -8}, new int[]{-7, -3, 0, 3}, 4);
        g.fillPolygon(new int[]{-22, -8, 16, -8}, new int[]{7, 3, 0, -3}, 4);
        g.setColor(new Color(255, 60, 80));
        g.fillOval(8, -4, 10, 8);
        g.setColor(new Color(255, 200, 210, 180));
        g.fillOval(10, -2, 5, 4);
        g.rotate(-a);
        g.translate(-e.x, -e.y);
    }

    private static void claw(Graphics2D g, Effect e, float t) {
        int r = (int) e.r;
        g.translate(e.x, e.y);
        float sc = (e.ex2 == 6 ? 1.18f : 1f) + t * (e.ex2 == 6 ? 0.72f : 0.5f);
        g.scale(sc, sc);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0, 0.9f * (1 - t))));
        g.rotate(Math.atan2(e.vy, e.vx));
        if (e.ex2 == 6) {
            g.setColor(new Color(255, 34, 112, (int) (72 * (1 - t))));
            g.fillOval((int) (-r * 1.05f), (int) (-r * 0.88f), (int) (r * 2.3f), (int) (r * 1.76f));
            for (int i = -2; i <= 2; i++) {
                g.setColor(new Color(e.c1.getRed(), e.c1.getGreen(), e.c1.getBlue(), 185));
                g.setStroke(new BasicStroke(9f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.drawArc((int) (-r * 1.04f), (int) (-r * 1.02f), (int) (r * 2.08f), (int) (r * 2.04f), -52 + i * 25, 92);
                g.setColor(new Color(255, 214, 236, 175));
                g.setStroke(new BasicStroke(3.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.drawArc((int) (-r * 0.9f), (int) (-r * 0.89f), (int) (r * 1.8f), (int) (r * 1.78f), -44 + i * 25, 70);
            }
            for (int i = 0; i < 5; i++) {
                Path2D.Float curl = new Path2D.Float();
                float off = -r * 0.56f + i * r * 0.28f;
                for (int j = 0; j <= 18; j++) {
                    float k = j / 18f;
                    float px = -r * 0.45f + k * r * 1.55f;
                    float py = off + FMath.sin(k * 8.2f + i * 1.1f + e.life * 16f) * r * (0.11f + k * 0.08f);
                    if (j == 0) curl.moveTo(px, py);
                    else curl.lineTo(px, py);
                }
                g.setStroke(new BasicStroke(8f - i * 0.7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.setColor(i % 2 == 0 ? new Color(210, 18, 72, 150) : new Color(255, 86, 190, 170));
                g.draw(curl);
                g.setStroke(new BasicStroke(2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.setColor(new Color(255, 232, 242, 145));
                if (i % 2 == 1) g.draw(curl);
            }
            flameGlare(g, r * 0.3f, 0, r * 0.78f, 0.6f * (1 - t));
            g.rotate(-Math.atan2(e.vy, e.vx));
            g.scale(1 / sc, 1 / sc);
            g.translate(-e.x, -e.y);
            return;
        }
        g.setStroke(new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = -1; i <= 1; i++) {
            g.setColor(new Color(e.c1.getRed(), e.c1.getGreen(), e.c1.getBlue(), 190));
            g.drawArc(-r, -r, r * 2, r * 2, -40 + i * 34, 80);
        }
        g.setColor(new Color(e.c2.getRed(), e.c2.getGreen(), e.c2.getBlue(), 210));
        g.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawArc(-r + 5, -r + 5, r * 2 - 10, r * 2 - 10, -28, 64);
        g.rotate(-Math.atan2(e.vy, e.vx));
        g.scale(1 / sc, 1 / sc);
        g.translate(-e.x, -e.y);
    }

    private static void nova(Graphics2D g, Effect e, float t) {
        float r = e.r * FMath.easeOut(t);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0, 0.9f * (1 - t))));
        g.setStroke(new BasicStroke(14 * (1 - t) + 3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(new Color(170, 15, 40));
        g.drawOval((int) (e.x - r), (int) (e.y - r), (int) (r * 2), (int) (r * 2));
        r *= 0.72f;
        g.setStroke(new BasicStroke(7 * (1 - t) + 2));
        g.setColor(new Color(255, 80, 110));
        g.drawOval((int) (e.x - r), (int) (e.y - r), (int) (r * 2), (int) (r * 2));
        g.setColor(new Color(255, 220, 230, (int) (200 * (1 - t))));
        r *= 0.5f;
        g.fillOval((int) (e.x - r), (int) (e.y - r), (int) (r * 2), (int) (r * 2));
    }

    private static void shock(Graphics2D g, Effect e, float t) {
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0, 0.95f * (1 - t))));
        if (e.ex2 == 6) {
            float dir = Math.signum(e.vx == 0 ? 1 : e.vx);
            for (int line = 0; line < 4; line++) {
                Path2D.Float curl = new Path2D.Float();
                float baseY = e.y - 6 - line * 6;
                for (int i = 0; i <= 22; i++) {
                    float k = i / 22f;
                    float px = -dir * i * 11f;
                    float py = baseY - FMath.sin(k * 9.5f + line + e.life * 12f) * (12 + line * 4) - k * 18f;
                    if (i == 0) curl.moveTo(e.x + px, py);
                    else curl.lineTo(e.x + px, py);
                }
                g.setStroke(new BasicStroke(9f - line * 1.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.setColor(line % 2 == 0 ? new Color(e.c1.getRed(), e.c1.getGreen(), e.c1.getBlue(), 170) : new Color(e.c2.getRed(), e.c2.getGreen(), e.c2.getBlue(), 185));
                g.draw(curl);
                g.setStroke(new BasicStroke(2.3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.setColor(new Color(255, 232, 242, 135));
                if (line % 2 == 1) g.draw(curl);
            }
        }
        int n = 4;
        for (int i = 0; i < n; i++) {
            float off = -i * 26 * Math.signum(e.vx) + FMath.sin(e.life * 30 + i * 2) * 3;
            float hgt = (26 - i * 5) * (1 - t * 0.4f);
            int bx = (int) (e.x + off);
            g.setColor(new Color(96, 70, 52));
            g.fillPolygon(new int[]{bx - 12, bx, bx + 12}, new int[]{(int) (e.y + 12), (int) (e.y - hgt), (int) (e.y + 12)}, 3);
            g.setColor(new Color(150, 115, 88));
            g.fillPolygon(new int[]{bx - 6, bx, bx + 6}, new int[]{(int) (e.y + 12), (int) (e.y - hgt * 0.7f), (int) (e.y + 12)}, 3);
        }
    }

    private static void heal(Graphics2D g, Effect e, float t) {
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0, 0.5f * (1 - t))));
        g.setStroke(new BasicStroke(3f));
        g.setColor(new Color(e.c1.getRed(), e.c1.getGreen(), e.c1.getBlue(), 130));
        float r = e.r * (0.8f + FMath.sin(e.life * 8) * 0.1f);
        g.drawOval((int) (e.x - r), (int) (e.y - r), (int) (r * 2), (int) (r * 2));
        g.setStroke(new BasicStroke(1.5f));
        g.setColor(new Color(e.c2.getRed(), e.c2.getGreen(), e.c2.getBlue(), 105));
        g.drawOval((int) (e.x - r * 0.62f), (int) (e.y - r * 0.62f), (int) (r * 1.24f), (int) (r * 1.24f));
    }

    private static void flameDragon(Graphics2D g, Effect e, float t) {
        if (!flameDragonTried) {
            flameDragonTried = true;
            flameDragonSvg = Svg.load("assets/flame_dragon.svg");
            if (flameDragonSvg != null) flameDragonGroup = flameDragonSvg.group("dragon");
        }
        if (flameDragonSvg == null || flameDragonGroup == null) return;
        Graphics2D gg = (Graphics2D) g.create();
        float fade = Math.min(1f, Math.min((1 - t) * 8f, e.life * 8f));
        gg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0, fade)));
        float dir = e.vx < 0 ? -1 : 1;
        float scale = 0.82f;
        gg.translate(e.x, e.y + FMath.sin(t * 18f) * 5f);
        gg.scale(dir * scale, scale);
        gg.drawImage(flameDragonGroup.img, Math.round(-flameDragonSvg.w / 2f), Math.round(-flameDragonSvg.h / 2f),
                Math.round(flameDragonSvg.w), Math.round(flameDragonSvg.h), null);
        gg.dispose();
    }

    private static void enmuWhisper(Graphics2D g, Effect e, float t) {
        float fade = Math.max(0, 0.95f * (1 - t * 0.35f));
        Graphics2D gg = (Graphics2D) g.create();
        gg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fade));
        float ang = (float) Math.atan2(e.vy, e.vx);
        gg.translate(e.x, e.y);
        gg.rotate(ang);
        gg.setPaint(new GradientPaint(-22, 0, new Color(89, 83, 223, 0), 8, 0, new Color(119, 201, 255, 190)));
        gg.fillOval(-30, -8, 35, 16);
        gg.setColor(new Color(151, 125, 255, 210));
        gg.fillOval(-8, -10, 20, 20);
        gg.setColor(new Color(116, 209, 255, 235));
        gg.fillOval(-4, -6, 12, 12);
        gg.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        gg.setColor(new Color(220, 241, 255, 210));
        gg.drawArc(-8, -9, 18, 18, 35, 220);
        gg.dispose();
        Glow.blob(g, e.x, e.y, 20, new Color(107, 147, 255, 145));
    }

    private static void enmuWhisperOrb(Graphics2D g, Effect e, float t) {
        float pulse = 0.82f + 0.18f * FMath.sin((e.maxLife - e.life) * 22f);
        float r = e.r * pulse;
        Graphics2D gg = (Graphics2D) g.create();
        gg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.82f));
        gg.setColor(new Color(88, 81, 220, 45));
        gg.fillOval((int) (e.x - r * 1.9f), (int) (e.y - r * 1.9f), (int) (r * 3.8f), (int) (r * 3.8f));
        gg.setColor(new Color(138, 109, 250, 175));
        gg.fillOval((int) (e.x - r), (int) (e.y - r), (int) (r * 2), (int) (r * 2));
        gg.setColor(new Color(87, 189, 255, 215));
        gg.fillOval((int) (e.x - r * 0.56f), (int) (e.y - r * 0.56f), (int) (r * 1.12f), (int) (r * 1.12f));
        gg.setColor(new Color(232, 244, 255, 230));
        gg.fillOval((int) (e.x - r * 0.22f), (int) (e.y - r * 0.28f), (int) (r * 0.38f), (int) (r * 0.38f));
        gg.dispose();
        Glow.blob(g, e.x, e.y, r * 2.2f, new Color(99, 132, 255, 180));
    }

    private static void enmuNightmare(Graphics2D g, Effect e, float t) {
        float fade = Math.max(0, Math.min(1, e.life * 5f));
        Graphics2D gg = (Graphics2D) g.create();
        gg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fade));
        if (e.ex2 == 5) {
            float ux = FMath.cos(e.angle), uy = FMath.sin(e.angle), px = -uy, py = ux;
            float sx = e.x + ux * 16, sy = e.y + uy * 16;
            float ex = e.x + ux * e.ex1, ey = e.y + uy * e.ex1;
            float farWidth = Math.min(180, e.ex1 * 0.22f);
            Polygon beam = new Polygon(
                    new int[]{(int) (sx + px * 12), (int) (sx - px * 12), (int) (ex - px * farWidth), (int) (ex + px * farWidth)},
                    new int[]{(int) (sy + py * 12), (int) (sy - py * 12), (int) (ey - py * farWidth), (int) (ey + py * farWidth)}, 4);
            gg.setPaint(new GradientPaint(sx, sy, new Color(97, 191, 255, 178), ex, ey,
                    new Color(61, 123, 255, 8)));
            gg.fillPolygon(beam);
            gg.setColor(new Color(129, 199, 255, 80));
            gg.setStroke(new BasicStroke(3f));
            gg.drawLine((int) sx, (int) sy, (int) (ex - px * farWidth), (int) (ey - py * farWidth));
            gg.drawLine((int) sx, (int) sy, (int) (ex + px * farWidth), (int) (ey + py * farWidth));
            if (e.ex3 == 0) {
                gg.setColor(new Color(246, 243, 255, 255));
                gg.fillOval((int) e.x - 17, (int) e.y - 8, 34, 16);
                gg.setColor(new Color(48, 147, 255, 245));
                gg.fillOval((int) e.x - 7, (int) e.y - 7, 14, 14);
                gg.setColor(new Color(12, 54, 150, 255));
                gg.fillOval((int) e.x - 2, (int) e.y - 6, 5, 12);
            }
            gg.dispose();
            return;
        }
        if (e.ex2 <= 1) {
            Polygon beam = new Polygon(
                    new int[]{(int) (e.x - 12), (int) (e.x + 12), (int) (e.x + 86), (int) (e.x - 86)},
                    new int[]{(int) (e.y + 20), (int) (e.y + 20), (int) (e.y + e.ex1), (int) (e.y + e.ex1)}, 4);
            gg.setPaint(new GradientPaint(0, e.y + 18, new Color(97, 191, 255, 178), 0, e.y + e.ex1,
                    new Color(61, 123, 255, 8)));
            gg.fillPolygon(beam);
            gg.setColor(new Color(129, 199, 255, 70));
            gg.setStroke(new BasicStroke(3f));
            gg.drawLine((int) e.x, (int) (e.y + 14), (int) (e.x - 86), (int) (e.y + e.ex1));
            gg.drawLine((int) e.x, (int) (e.y + 14), (int) (e.x + 86), (int) (e.y + e.ex1));
        }
        float r = e.r;
        Path2D.Float flesh = new Path2D.Float();
        flesh.moveTo(e.x - r, e.y + r * 0.3f);
        flesh.curveTo(e.x - r * 1.2f, e.y - r * 0.7f, e.x - r * 0.4f, e.y - r * 1.1f, e.x, e.y - r * 0.7f);
        flesh.curveTo(e.x + r * 0.28f, e.y - r * 1.35f, e.x + r * 1.1f, e.y - r * 0.75f, e.x + r, e.y + r * 0.25f);
        flesh.curveTo(e.x + r * 0.7f, e.y + r * 0.85f, e.x - r * 0.8f, e.y + r * 0.9f, e.x - r, e.y + r * 0.3f);
        flesh.closePath();
        gg.setColor(new Color(101, 67, 119, 230));
        gg.fill(flesh);
        gg.setStroke(new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        gg.setColor(new Color(59, 43, 78, 235));
        gg.draw(flesh);
        for (int i = 0; i < 6; i++) {
            float side = i % 2 == 0 ? -1 : 1;
            float sy = e.y - r * 0.2f + (i / 2) * r * 0.28f;
            Path2D.Float tentacle = new Path2D.Float();
            tentacle.moveTo(e.x + side * r * 0.62f, sy);
            tentacle.quadTo(e.x + side * r * 1.2f, sy + side * r * 0.25f, e.x + side * r * (1.35f + 0.12f * (i % 3)), sy - r * 0.1f);
            gg.setStroke(new BasicStroke(8f - (i % 3), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            gg.setColor(i % 2 == 0 ? new Color(171, 151, 176, 220) : new Color(100, 70, 126, 240));
            gg.draw(tentacle);
            gg.setColor(new Color(203, 187, 210, 220));
            gg.fillOval((int) (e.x + side * r * (1.25f + 0.12f * (i % 3)) - 5), (int) sy - 7, 12, 10);
        }
        // The pale almond eye and its luminous blue iris remain visible as the flesh descends.
        Path2D.Float eye = new Path2D.Float();
        eye.moveTo(e.x - 36, e.y - 9);
        eye.quadTo(e.x, e.y - 40, e.x + 36, e.y - 9);
        eye.quadTo(e.x, e.y + 24, e.x - 36, e.y - 9);
        eye.closePath();
        gg.setColor(new Color(246, 243, 255, 255));
        gg.fill(eye);
        gg.setColor(new Color(48, 147, 255, 245));
        gg.fillOval((int) e.x - 12, (int) e.y - 16, 25, 25);
        gg.setColor(new Color(12, 54, 150, 255));
        gg.fillOval((int) e.x - 5, (int) e.y - 11, 11, 17);
        gg.setColor(new Color(255, 255, 255, 245));
        gg.fillOval((int) e.x - 5, (int) e.y - 12, 5, 5);
        if (e.ex2 >= 3) {
            gg.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            gg.setColor(new Color(55, 35, 73, 245));
            for (int i = 0; i < 5; i++) {
                float side = i % 2 == 0 ? -1 : 1;
                float sx = e.x + side * (15 + i * 8);
                float sy = e.y + 4 + (i % 3) * 8;
                gg.drawLine((int) sx, (int) sy, (int) (sx + side * 12), (int) (sy + 8));
                gg.drawLine((int) (sx + side * 12), (int) (sy + 8), (int) (sx + side * 8), (int) (sy + 20));
            }
            if (e.ex2 >= 4) {
                gg.setColor(new Color(126, 99, 145, 220));
                for (int i = 0; i < 7; i++) {
                    float a = i * 0.897f + Game.time * 5f;
                    float sx = e.x + FMath.cos(a) * (e.r + 12 + i * 3);
                    float sy = e.y + FMath.sin(a) * (e.r * 0.45f + 10);
                    gg.fillOval((int) sx - 5, (int) sy - 4, 10, 8);
                }
            }
        }
        gg.dispose();
    }

    static void enmuNightmareEyeGlow(Graphics2D g, Effect e) {
        if (e.dead || e.ex3 != 0 || (e.ex2 >= 4 && e.ex2 != 5)) return;
        Glow.blob(g, e.x, e.y - 3, e.ex2 == 5 ? 22 : 30, new Color(58, 153, 255, 215));
    }

    static void drawEnmuEyeWarning(Graphics2D g, float x, float y, float angle, float length, float pulse) {
        float ux = FMath.cos(angle), uy = FMath.sin(angle), px = -uy, py = ux;
        float sx = x + ux * 18, sy = y + uy * 18;
        float ex = x + ux * length, ey = y + uy * length;
        float width = Math.min(180, length * 0.22f);
        Graphics2D gg = (Graphics2D) g.create();
        int alpha = (int) (42 + 42 * pulse);
        gg.setColor(new Color(117, 202, 255, alpha));
        gg.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10f,
                new float[]{8f, 7f}, 0));
        gg.drawLine((int) sx, (int) sy, (int) (ex + px * width), (int) (ey + py * width));
        gg.drawLine((int) (ex + px * width), (int) (ey + py * width), (int) (ex - px * width), (int) (ey - py * width));
        gg.drawLine((int) (ex - px * width), (int) (ey - py * width), (int) sx, (int) sy);
        gg.dispose();
    }

    private static void enmuWhip(Graphics2D g, Effect e, float t) {
        if (e.ex2 == 1) return; // collision-only lash; the tentacle itself animates the strike
        if (e.pts.isEmpty()) return;
        float[] root = e.pts.get(0);
        float fade = Math.max(0, Math.min(1, e.life * 8f));
        Graphics2D gg = (Graphics2D) g.create();
        gg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fade));
        float dx = e.x - root[0], dy = e.y - root[1];
        float len = Math.max(1, FMath.dist(0, 0, dx, dy));
        float nx = -dy / len, ny = dx / len;
        float[] px = new float[25], py = new float[25], widths = new float[25];
        float lashPhase = (e.maxLife - e.life) * 28f;
        for (int i = 0; i < px.length; i++) {
            float k = i / (float) (px.length - 1);
            float flick = FMath.sin(k * 11f - lashPhase) * 17f * k * k;
            float bend = -FMath.sin(k * (float) Math.PI) * 24f + flick;
            px[i] = root[0] + dx * k + nx * bend;
            py[i] = root[1] + dy * k + ny * bend;
            widths[i] = e.r * (float) Math.pow(1 - k, 0.88) + 1.2f;
        }
        Path2D.Float lash = new Path2D.Float();
        for (int i = 0; i < px.length; i++) {
            int before = Math.max(0, i - 1), after = Math.min(px.length - 1, i + 1);
            float tx = px[after] - px[before], ty = py[after] - py[before];
            float tl = Math.max(1, FMath.dist(0, 0, tx, ty));
            float ox = -ty / tl * widths[i], oy = tx / tl * widths[i];
            if (i == 0) lash.moveTo(px[i] + ox, py[i] + oy); else lash.lineTo(px[i] + ox, py[i] + oy);
        }
        for (int i = px.length - 1; i >= 0; i--) {
            int before = Math.max(0, i - 1), after = Math.min(px.length - 1, i + 1);
            float tx = px[after] - px[before], ty = py[after] - py[before];
            float tl = Math.max(1, FMath.dist(0, 0, tx, ty));
            lash.lineTo(px[i] + ty / tl * widths[i], py[i] - tx / tl * widths[i]);
        }
        lash.closePath();
        gg.setColor(new Color(54, 29, 66, 235));
        gg.fill(lash);
        gg.setColor(new Color(153, 105, 164, 245));
        gg.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        gg.draw(lash);
        Path2D.Float ridge = new Path2D.Float();
        for (int i = 0; i < px.length; i++) {
            float k = i / (float) (px.length - 1);
            float offset = widths[i] * FMath.sin(k * 12f - lashPhase) * 0.48f;
            float sx = px[i] + nx * offset, sy = py[i] + ny * offset;
            if (i == 0) ridge.moveTo(sx, sy); else ridge.lineTo(sx, sy);
        }
        gg.setStroke(new BasicStroke(3.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        gg.setColor(new Color(223, 185, 226, 220));
        gg.draw(ridge);
        gg.setColor(new Color(238, 220, 239, 240));
        gg.fillOval((int) px[px.length - 1] - 3, (int) py[py.length - 1] - 3, 6, 6);
        gg.dispose();
    }

    private static void akazaShockwave(Graphics2D g, Effect e, float t) {
        Graphics2D gg = (Graphics2D) g.create();
        gg.translate(e.x, e.y);
        float angle = (float) Math.atan2(e.vy, e.vx);
        gg.rotate(angle);
        float pulse = 0.86f + 0.14f * FMath.sin((e.maxLife - e.life) * 28f);
        float size = e.r * pulse;
        gg.setColor(new Color(41, 117, 204, 55));
        gg.fillOval((int) (-size * 0.75f), (int) (-size * 0.62f), (int) (size * 1.6f), (int) (size * 1.24f));
        gg.setColor(new Color(61, 151, 238, 200));
        gg.setStroke(new BasicStroke(Math.max(2f, size * 0.22f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        gg.drawArc((int) (-size * 0.72f), (int) (-size * 0.65f), (int) (size * 1.45f), (int) (size * 1.3f), -65, 130);
        gg.setColor(new Color(179, 222, 255, 245));
        gg.setStroke(new BasicStroke(Math.max(1.5f, size * 0.09f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        gg.drawArc((int) (-size * 0.5f), (int) (-size * 0.43f), (int) size, (int) (size * 0.86f), -61, 122);
        gg.setColor(new Color(112, 188, 255, 225));
        gg.fillOval((int) (-size * 0.55f), (int) (-size * 0.12f), (int) (size * 0.72f), (int) (size * 0.24f));
        gg.setColor(new Color(233, 247, 255, 245));
        gg.fillOval((int) (-size * 0.18f), (int) (-size * 0.08f), (int) (size * 0.32f), (int) (size * 0.16f));
        gg.dispose();
        Glow.blob(g, e.x, e.y, size * 1.25f, new Color(66, 154, 255, 145));
    }

    private static void akazaCompass(Graphics2D g, Effect e, float t) {
        if (!akazaCompassTried) {
            akazaCompassTried = true;
            akazaCompassSvg = Svg.load("assets/akaza_compass.svg");
            if (akazaCompassSvg != null) {
                akazaCompassGroup = akazaCompassSvg.group("compass");
                akazaCompassGlowGroup = akazaCompassSvg.group("glow");
            }
        }
        if (akazaCompassSvg == null || akazaCompassGroup == null) return;
        float pulse = 0.9f + 0.1f * FMath.sin((e.maxLife - e.life) * 8f);
        float size = e.r * 2f * 3f * pulse;
        float fade = e.ex2 == 1 ? 0.22f + 0.22f * (0.5f + 0.5f * FMath.sin((e.maxLife - e.life) * 22f))
                : Math.min(1, e.life * 3.2f);
        Graphics2D gg = (Graphics2D) g.create();
        gg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fade));
        gg.drawImage(akazaCompassGroup.img, Math.round(e.x - size * 0.5f), Math.round(e.y - size * 0.5f),
                Math.round(size), Math.round(size), null);
        gg.dispose();
    }

    static void akazaCompassGlow(Graphics2D g, Effect e) {
        if (!akazaCompassTried) {
            akazaCompassTried = true;
            akazaCompassSvg = Svg.load("assets/akaza_compass.svg");
            if (akazaCompassSvg != null) {
                akazaCompassGroup = akazaCompassSvg.group("compass");
                akazaCompassGlowGroup = akazaCompassSvg.group("glow");
            }
        }
        if (e.dead || akazaCompassSvg == null || akazaCompassGlowGroup == null) return;
        float pulse = 0.72f + 0.28f * FMath.sin((e.maxLife - e.life) * (e.ex2 == 1 ? 19f : 8f));
        float size = e.r * 2f * 3f * pulse;
        float fade = Math.min(1, e.life * 3.2f) * (e.ex2 == 1 ? 0.4f : 0.5f);
        Glow.blob(g, e.x, e.y, size * 1.2f, new Color(76, 163, 255, (int) (75 * fade)));
        Graphics2D gg = (Graphics2D) g.create();
        gg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fade));
        gg.drawImage(akazaCompassGlowGroup.img, Math.round(e.x - size * 0.5f), Math.round(e.y - size * 0.5f),
                Math.round(size), Math.round(size), null);
        gg.dispose();
    }

    private static void akazaPillar(Graphics2D g, Effect e, float t) {
        float elapsed = e.maxLife - e.life - e.warmupTotal;
        float grow = FMath.clamp(elapsed / 0.48f, 0, 1);
        if (grow <= 0) return;
        float height = e.ex1 * FMath.easeOut(grow);
        float fade = Math.min(1, grow * 2.8f) * FMath.clamp(e.life * 2.5f, 0, 1);
        Graphics2D gg = (Graphics2D) g.create();
        gg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fade));
        gg.setColor(new Color(7, 8, 15, 190));
        gg.fillOval((int) (e.x - e.r * 0.85f), (int) (e.y - 12), (int) (e.r * 1.7f), 24);
        for (int strand = 0; strand < 8; strand++) {
            Path2D.Float flame = new Path2D.Float();
            float phase = strand * 0.79f + Game.time * (strand % 2 == 0 ? 7f : -6f);
            for (int i = 0; i <= 20; i++) {
                float k = i / 20f;
                float sway = FMath.sin(k * 8f + phase) * (10 + 14 * k);
                float x = e.x + FMath.cos(phase + k * 4.5f) * (8 + k * 18) + sway;
                float y = e.y - k * height;
                if (i == 0) flame.moveTo(x, y); else flame.lineTo(x, y);
            }
            gg.setStroke(new BasicStroke(strand % 2 == 0 ? 15f : 9f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            gg.setColor(strand % 2 == 0 ? new Color(7, 8, 13, 235) : new Color(191, 31, 53, 225));
            gg.draw(flame);
            if (strand % 2 == 1) {
                gg.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                gg.setColor(new Color(255, 102, 119, 190));
                gg.draw(flame);
            }
        }
        gg.dispose();
        Glow.blob(g, e.x, e.y - height * 0.48f, e.r * 1.4f, new Color(176, 33, 61, (int) (85 * fade)));
    }

    private static void akazaClone(Graphics2D g, Effect e, float t) {
        if (!akazaCloneTried) {
            akazaCloneTried = true;
            akazaCloneSvg = Svg.load("assets/akaza.svg");
            if (akazaCloneSvg != null) {
                Color tint = new Color(255, 42, 75);
                akazaCloneHead = tintedGroup(akazaCloneSvg.group("head"), tint, 0.48f);
                akazaCloneArmFront = tintedGroup(akazaCloneSvg.group("arm-front"), tint, 0.5f);
                akazaCloneArmBack = tintedGroup(akazaCloneSvg.group("arm-back"), tint, 0.5f);
                akazaCloneTorso = tintedGroup(akazaCloneSvg.group("torso"), tint, 0.48f);
                akazaCloneLegFront = tintedGroup(akazaCloneSvg.group("leg-front"), tint, 0.5f);
                akazaCloneLegBack = tintedGroup(akazaCloneSvg.group("leg-back"), tint, 0.5f);
            }
        }
        if (akazaCloneSvg == null || akazaCloneHead == null || akazaCloneTorso == null) return;
        float fade = FMath.clamp(Math.min(e.life * 2.2f, 0.64f), 0, 1);
        float height = e.r * 1.75f;
        float scale = height / akazaCloneSvg.h;
        float facing = e.ex3 < 0 ? -1 : 1;
        float phase = Game.time * 22f + e.ex2 * 1.71f;
        float strike = e.activeDuration > 0 ? FMath.sin((0.18f - e.activeDuration) / 0.18f * (float) Math.PI) : 0;
        float armAction = FMath.sin(phase) * 0.18f + strike * 1.25f;
        float legAction = FMath.sin(phase + (float) Math.PI) * 0.12f - strike * 0.32f;
        Graphics2D gg = (Graphics2D) g.create();
        gg.translate(e.x, e.y + e.r * 0.68f);
        gg.scale(facing * scale * 0.72f, scale);
        gg.translate(-akazaCloneSvg.w / 2f, -akazaCloneSvg.h);
        gg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fade));
        drawClonePart(gg, akazaCloneLegBack, -legAction);
        drawClonePart(gg, akazaCloneArmBack, -armAction * 0.55f);
        drawClonePart(gg, akazaCloneTorso, 0);
        drawClonePart(gg, akazaCloneLegFront, legAction);
        drawClonePart(gg, akazaCloneArmFront, -armAction);
        drawClonePart(gg, akazaCloneHead, FMath.sin(phase * 0.35f) * 0.035f);
        gg.dispose();
        Glow.blob(g, e.x, e.y - e.r * 0.35f, e.r * 1.25f, new Color(230, 34, 70, (int) (130 * fade)));
        if (strike > 0.05f) {
            g.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(new Color(255, 75, 100, (int) (150 * strike * fade)));
            g.drawLine((int) (e.x - facing * 15), (int) (e.y - 24), (int) (e.x + facing * (32 + strike * 24)), (int) (e.y - 40));
        }
    }

    private static Svg.Group tintedGroup(Svg.Group source, Color tint, float amount) {
        if (source == null) return null;
        BufferedImage src = source.img;
        BufferedImage img = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < src.getHeight(); y++) for (int x = 0; x < src.getWidth(); x++) {
            int pixel = src.getRGB(x, y), alpha = pixel >>> 24;
            if (alpha == 0) continue;
            int red = (pixel >> 16) & 255, green = (pixel >> 8) & 255, blue = pixel & 255;
            red = Math.round(red * (1 - amount) + tint.getRed() * amount);
            green = Math.round(green * (1 - amount) + tint.getGreen() * amount);
            blue = Math.round(blue * (1 - amount) + tint.getBlue() * amount);
            img.setRGB(x, y, alpha << 24 | red << 16 | green << 8 | blue);
        }
        return new Svg.Group(source.name, img, source.pivotX, source.pivotY);
    }

    private static void drawClonePart(Graphics2D g, Svg.Group group, float rotation) {
        if (group == null) return;
        Graphics2D part = (Graphics2D) g.create();
        if (rotation != 0) {
            part.translate(group.pivotX, group.pivotY);
            part.rotate(rotation);
            part.drawImage(group.img, Math.round(-group.pivotX), Math.round(-group.pivotY),
                    Math.round(akazaCloneSvg.w), Math.round(akazaCloneSvg.h), null);
        } else part.drawImage(group.img, 0, 0, Math.round(akazaCloneSvg.w), Math.round(akazaCloneSvg.h), null);
        part.dispose();
    }

    private static void slamRing(Graphics2D g, Effect e, float t) {
        float r = e.r * FMath.easeOut(t);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0, 0.85f * (1 - t))));
        g.setStroke(new BasicStroke((e.ex2 == 6 ? 16 : 10) * (1 - t) + 2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(new Color(e.c1.getRed(), e.c1.getGreen(), e.c1.getBlue(), 220));
        g.drawOval((int) (e.x - r), (int) (e.y - r * 0.4f), (int) (r * 2), (int) (r * 0.8f));
        g.setColor(new Color(e.c2.getRed(), e.c2.getGreen(), e.c2.getBlue(), 230));
        g.setStroke(new BasicStroke(4 * (1 - t) + 1));
        float r2 = r * 0.66f;
        g.drawOval((int) (e.x - r2), (int) (e.y - r2 * 0.4f), (int) (r2 * 2), (int) (r2 * 0.8f));
        if (e.ex2 == 6) {
            for (int line = 0; line < 7; line++) {
                Path2D.Float curl = new Path2D.Float();
                float start = line * 0.9f + e.life * 5f;
                for (int i = 0; i <= 24; i++) {
                    float k = i / 24f;
                    float a = start + k * 1.65f;
                    float rr = r * (0.58f + k * 0.52f);
                    float px = e.x + FMath.cos(a) * rr;
                    float py = e.y + FMath.sin(a) * rr * 0.38f - FMath.sin(k * (float) Math.PI) * r * 0.18f;
                    if (i == 0) curl.moveTo(px, py);
                    else curl.lineTo(px, py);
                }
                g.setStroke(new BasicStroke(8.5f - line * 0.55f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.setColor(line % 2 == 0 ? new Color(e.c1.getRed(), e.c1.getGreen(), e.c1.getBlue(), 155) : new Color(e.c2.getRed(), e.c2.getGreen(), e.c2.getBlue(), 175));
                g.draw(curl);
            }
            if (r > 1) Glow.blob(g, e.x, e.y, r * 1.05f, new Color(255, 34, 112, (int) (95 * (1 - t))));
        }
        if (isFlame(e)) flameGlare(g, e.x, e.y, r * 0.8f, 0.8f * (1 - t));
    }

    private static void ribbon(Graphics2D g, Effect e, float t) {
        int n = e.pts.size();
        if (n < 2) return;
        float overall = Math.max(0, 1 - t * 0.7f);
        // two sinusoidal trails 180 degrees out of phase, tapering + fading toward the far end
        for (int line = 0; line < 2; line++) {
            float sgn = line == 0 ? 1f : -1f;
            for (int i = 1; i < n; i++) {
                float[] a = e.pts.get(i - 1);
                float[] b = e.pts.get(i);
                float k = i / (float) n;
                float dx = b[0] - a[0], dy = b[1] - a[1];
                float len = FMath.dist(0, 0, dx, dy);
                float nxp = len > 0.01f ? -dy / len : 0, nyp = len > 0.01f ? dx / len : 1;
                float off = sgn * (float) Math.sin(k * Math.PI) * 13f * FMath.sin(k * 52f + e.life * 24f);
                float ax = a[0] + nxp * off, ay = a[1] + nyp * off;
                float bx = b[0] + nxp * off, by = b[1] + nyp * off;
                float wdt = 2f + 14f * k;
                g.setStroke(new BasicStroke(wdt, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.setColor(new Color(255, 110, 30, (int) (235 * k * overall)));
                g.drawLine((int) ax, (int) ay, (int) bx, (int) by);
                g.setStroke(new BasicStroke(Math.max(0.6f, wdt * 0.45f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.setColor(new Color(255, 220, 120, (int) (225 * k * overall)));
                g.drawLine((int) ax, (int) ay, (int) bx, (int) by);
                if (line == 0 && i % 5 == 0) flameGlare(g, bx, by, wdt * 2.3f, 0.42f * k * overall);
            }
        }
    }

    private static void handSpike(Graphics2D g, Effect e, float t) {        float rise = t < 0.3 ? FMath.easeOut(t / 0.3f) : t > 0.72 ? 1 - FMath.easeIn((t - 0.72f) / 0.28f) : 1;
        if (rise <= 0.01f) return;
        float hgt = e.ex1 * rise;
        float sway = FMath.sin(t * 9 + e.x) * 4;
        Graphics2D gg = (Graphics2D) g.create();
        gg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.min(1, (1 - t) * 2.2f)));
        gg.setColor(new Color(74, 112, 58));
        gg.setStroke(new BasicStroke(17f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        gg.drawLine((int) e.x, (int) (e.y + 6), (int) (e.x + sway), (int) (e.y - hgt));
        gg.setColor(new Color(108, 152, 82));
        gg.setStroke(new BasicStroke(10f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        gg.drawLine((int) e.x, (int) (e.y + 6), (int) (e.x + sway), (int) (e.y - hgt));
        float hx = e.x + sway, hy = e.y - hgt;
        gg.setColor(new Color(96, 138, 72));
        gg.fillOval((int) hx - 14, (int) hy - 13, 28, 24);
        gg.setColor(new Color(128, 168, 96));
        gg.fillOval((int) hx - 10, (int) hy - 10, 20, 17);
        gg.setColor(new Color(60, 92, 46));
        for (int i = -2; i <= 2; i++) {
            double a = -Math.PI / 2 + i * 0.5;
            gg.setStroke(new BasicStroke(4.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            gg.drawLine((int) (hx + i * 5), (int) (hy - 4),
                    (int) (hx + i * 5 + Math.cos(a) * 11), (int) (hy - 4 + Math.sin(a) * 11));
        }
        gg.setColor(new Color(255, 240, 200, 190));
        gg.fillOval((int) hx - 3, (int) hy - 6, 6, 5);
        gg.dispose();
    }

    private static void handSwing(Graphics2D g, Effect e, float t) {
        if (e.owner == null) return;
        float dir = e.vx == 0 ? e.owner.facing() : Math.signum(e.vx);
        float ox = e.owner.x + dir * 14, oy = e.owner.y - e.owner.h * 0.35f;
        float midX = (ox + e.x) * 0.5f + dir * 18;
        float midY = Math.min(oy, e.y) - 36 * FMath.sin(t * (float) Math.PI);
        float fade = Math.min(1, (1 - t) * 2.4f);
        Graphics2D gg = (Graphics2D) g.create();
        gg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fade));
        Path2D.Float arm = new Path2D.Float();
        arm.moveTo(ox, oy);
        arm.quadTo(midX, midY, e.x, e.y);
        gg.setStroke(new BasicStroke(18f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        gg.setColor(new Color(74, 112, 58));
        gg.draw(arm);
        gg.setStroke(new BasicStroke(10f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        gg.setColor(new Color(108, 152, 82));
        gg.draw(arm);
        drawHandPalm(gg, e.x, e.y, dir > 0 ? -0.15f : (float) Math.PI + 0.15f, 1.15f, 230);
        gg.dispose();
    }

    private static void handCharge(Graphics2D g, Effect e, float t) {
        float dir = e.vx == 0 ? 1 : Math.signum(e.vx);
        float bx = e.owner != null ? e.owner.x + dir * 16 : e.x;
        float by = e.owner != null ? e.owner.y - e.owner.h * 0.34f : e.y;
        float rise = FMath.easeOut(Math.min(1, t / 0.34f));
        float turn = FMath.easeIn(Math.max(0, (t - 0.22f) / 0.78f));
        float ang = (float) (-Math.PI / 2 + dir * Math.toRadians(100) * turn);
        float len = 154 * rise;
        float hx = bx + FMath.cos(ang) * len;
        float hy = by + FMath.sin(ang) * len;
        Graphics2D gg = (Graphics2D) g.create();
        gg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.min(1, 0.25f + t * 1.2f)));
        gg.setStroke(new BasicStroke(26f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        gg.setColor(new Color(64, 100, 52, 230));
        gg.drawLine((int) bx, (int) by, (int) hx, (int) hy);
        gg.setStroke(new BasicStroke(15f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        gg.setColor(new Color(108, 152, 82, 240));
        gg.drawLine((int) bx, (int) by, (int) hx, (int) hy);
        drawHandPalm(gg, hx, hy, ang, 1.65f, 245);
        if (e.ex2 > 0) {
            gg.setColor(new Color(150, 200, 110, (int) (130 * t)));
            gg.setStroke(new BasicStroke(3f));
            float rr = 32 + 18 * FMath.sin(t * (float) Math.PI);
            gg.drawOval((int) (e.ex1 - rr), (int) (e.ex2 - rr * 0.35f), (int) (rr * 2), (int) (rr * 0.7f));
        }
        gg.dispose();
    }

    private static void handAura(Graphics2D g, Effect e, float t) {
        float fade = Math.max(0, 1 - t * 0.4f);
        float maxR = e.r * FMath.easeOut(t);
        Graphics2D gg = (Graphics2D) g.create();
        gg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.min(1, fade)));
        gg.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = 0; i < 14; i++) {
            float k = i / 13f;
            float rr = maxR * (0.18f + k * 0.9f);
            float a = t * 7.5f + i * 0.9f;
            float hx = e.x + FMath.cos(a) * rr;
            float hy = e.y + FMath.sin(a) * rr * 0.62f;
            float px = e.x + FMath.cos(a - 0.28f) * Math.max(8, rr - 34);
            float py = e.y + FMath.sin(a - 0.28f) * Math.max(8, rr - 34) * 0.62f;
            gg.setColor(new Color(74, 112, 58, (int) (170 * fade)));
            gg.drawLine((int) px, (int) py, (int) hx, (int) hy);
            drawHandPalm(gg, hx, hy, a + (float) Math.PI * 0.5f, 0.65f + k * 0.38f, (int) (210 * fade));
        }
        gg.dispose();
    }

    private static void swampCloud(Graphics2D g, Effect e, float t) {
        Graphics2D gg = (Graphics2D) g.create();
        float fade = Math.max(0, 0.88f * (1 - t * 0.65f));
        gg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fade));
        for (int i = 0; i < 13; i++) {
            float a = i * 1.7f + e.life * 1.9f;
            float rr = e.r * (0.18f + (i % 5) * 0.16f);
            float sx = e.x + FMath.cos(a) * rr;
            float sy = e.y + FMath.sin(a) * rr * 0.55f;
            int sz = (int) (e.r * (0.36f + (i % 4) * 0.09f));
            gg.setColor(new Color(4, 18, 16, 185));
            gg.fillOval((int) sx - sz, (int) sy - sz / 2, sz * 2, sz);
            gg.setColor(new Color(18, 64, 58, 95));
            gg.drawOval((int) sx - sz, (int) sy - sz / 2, sz * 2, sz);
        }
        gg.dispose();
    }

    private static void swampHands(Graphics2D g, Effect e, float t) {
        swampPuddle(g, e, t);
        float rise = t < 0.24f ? FMath.easeOut(t / 0.24f) : t > 0.74f ? 1 - FMath.easeIn((t - 0.74f) / 0.26f) : 1;
        if (rise <= 0.01f) return;
        Graphics2D gg = (Graphics2D) g.create();
        gg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.min(1, (1 - t) * 2.5f)));
        float hgt = e.ex1 * rise;
        gg.setColor(new Color(235, 238, 232));
        gg.setStroke(new BasicStroke(11f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        gg.drawLine((int) e.x, (int) (e.y - 2), (int) e.x, (int) (e.y - hgt));
        gg.setColor(new Color(250, 252, 246));
        gg.fillOval((int) e.x - 12, (int) (e.y - hgt) - 11, 24, 20);
        gg.setColor(new Color(190, 198, 192));
        gg.setStroke(new BasicStroke(3.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = -2; i <= 2; i++)
            gg.drawLine((int) e.x + i * 4, (int) (e.y - hgt - 5), (int) e.x + i * 5, (int) (e.y - hgt - 20 + Math.abs(i) * 3));
        gg.dispose();
    }

    private static void swampPuddle(Graphics2D g, Effect e, float t) {
        float fade = Math.max(0, 1 - t * 0.7f);
        float r = e.r * (0.65f + FMath.sin(Math.min(1, t) * (float) Math.PI) * 0.35f);
        Graphics2D gg = (Graphics2D) g.create();
        gg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fade));
        gg.setColor(new Color(1, 6, 7, 220));
        gg.fillOval((int) (e.x - r), (int) (e.y - r * 0.22f), (int) (r * 2), (int) (r * 0.44f));
        gg.setColor(new Color(22, 72, 66, 130));
        gg.setStroke(new BasicStroke(3f));
        gg.drawOval((int) (e.x - r), (int) (e.y - r * 0.22f), (int) (r * 2), (int) (r * 0.44f));
        gg.dispose();
    }

    private static void temari(Graphics2D g, Effect e, float t) {
        Graphics2D gg = (Graphics2D) g.create();
        gg.translate(e.x, e.y);
        gg.rotate(e.life * 10);
        int r = (int) e.r;
        gg.setColor(new Color(42, 104, 196));
        gg.fillOval(-r, -r, r * 2, r * 2);
        gg.setClip(new java.awt.geom.Ellipse2D.Float(-r, -r, r * 2, r * 2));
        int teeth = 12;
        for (int i = 0; i < teeth; i++) {
            int x0 = -r + i * r * 2 / teeth;
            int x1 = -r + (i + 1) * r * 2 / teeth;
            int xm = (x0 + x1) / 2;
            gg.setColor(Color.WHITE);
            gg.fillPolygon(new int[]{x0, x1, xm}, new int[]{-3, -3, 8}, 3);
            gg.fillPolygon(new int[]{x0, x1, xm}, new int[]{3, 3, -8}, 3);
            gg.setColor(new Color(245, 204, 58));
            gg.fillPolygon(new int[]{x0, x1, xm}, new int[]{-r / 2, -r / 2, -5}, 3);
            gg.fillPolygon(new int[]{x0, x1, xm}, new int[]{r / 2, r / 2, 5}, 3);
            gg.setColor(new Color(188, 40, 44));
            gg.fillPolygon(new int[]{x0 + 3, x1 - 3, xm}, new int[]{-r / 2 + 4, -r / 2 + 4, -10}, 3);
            gg.fillPolygon(new int[]{x0 + 3, x1 - 3, xm}, new int[]{r / 2 - 4, r / 2 - 4, 10}, 3);
            gg.setColor(new Color(232, 116, 36));
            gg.fillPolygon(new int[]{x0, x1, xm}, new int[]{-r, -r, -r / 2}, 3);
            gg.fillPolygon(new int[]{x0, x1, xm}, new int[]{r, r, r / 2}, 3);
        }
        gg.setClip(null);
        gg.setColor(new Color(22, 44, 94));
        gg.setStroke(new BasicStroke(2.4f));
        gg.drawOval(-r, -r, r * 2, r * 2);
        gg.dispose();
    }

    private static void arrow(Graphics2D g, Effect e, float t) {
        if (e.ex2 == 7) {
            Graphics2D trail = (Graphics2D) g.create();
            trail.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0, 0.88f * (1 - t * 0.2f))));
            trail.setColor(new Color(0, 0, 0, 220));
            trail.setStroke(new BasicStroke(5.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            if (!e.pts.isEmpty()) {
                float[] start = e.pts.get(0);
                trail.drawLine((int) start[0], (int) start[1], (int) e.x, (int) e.y);
            }
            trail.dispose();
            float pulse = 0.78f + 0.22f * FMath.sin((e.maxLife - e.life) * 58f);
            Glow.blob(g, e.x, e.y, e.r * (0.85f + 0.35f * pulse), new Color(150, 150, 160, 155));
            g.setColor(new Color(118, 120, 128));
            int rr = (int) (9 + pulse * 3);
            g.fillOval((int) e.x - rr, (int) e.y - rr, rr * 2, rr * 2);
            g.setColor(new Color(220, 220, 225, 150));
            g.fillOval((int) e.x - rr / 2, (int) e.y - rr / 2, rr, rr);
            return;
        }
        Graphics2D gg = (Graphics2D) g.create();
        gg.translate(e.x, e.y);
        gg.rotate(e.angle);
        gg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0, 0.92f * (1 - t * 0.25f))));
        gg.setColor(new Color(e.c1.getRed(), e.c1.getGreen(), e.c1.getBlue(), 210));
        gg.setStroke(new BasicStroke(6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        gg.drawLine(-24, 0, 18, 0);
        gg.fillPolygon(new int[]{18, 34, 18}, new int[]{-12, 0, 12}, 3);
        gg.setColor(new Color(e.c2.getRed(), e.c2.getGreen(), e.c2.getBlue(), 180));
        gg.setStroke(new BasicStroke(2f));
        gg.drawLine(-22, 0, 26, 0);
        gg.dispose();
    }

    private static void boulder(Graphics2D g, Effect e, float t) {
        Graphics2D gg = (Graphics2D) g.create();
        gg.translate(e.x, e.y);
        gg.rotate(e.life * 4);
        int r = (int) e.r;
        gg.setColor(new Color(98, 92, 86));
        gg.fillOval(-r, -r, r * 2, r * 2);
        gg.setColor(new Color(145, 138, 126));
        gg.fillOval(-r / 2, -r / 2, r, r / 2);
        gg.setColor(new Color(55, 50, 48));
        gg.setStroke(new BasicStroke(3f));
        gg.drawOval(-r, -r, r * 2, r * 2);
        gg.dispose();
    }

    private static void thread(Graphics2D g, Effect e, float t) {
        if (e.ex2 == 6 || e.ex2 == 8) {
            threadCage(g, e, t);
            return;
        }
        if (e.ex2 == 5 || e.ex2 == 9) {
            threadWall(g, e, t);
            return;
        }
        if (e.ex2 == 7) {
            contortingThread(g, e, t);
            return;
        }
        if (e.ex2 == 4) {
            snatchThread(g, e, t);
            return;
        }
        float fade = Math.min(1, (1 - t) * 7f);
        float sa = FMath.sin(e.angle), ca = FMath.cos(e.angle);
        float half = e.ex1;
        float topX = e.x + sa * half, topY = e.y - ca * half;
        float botX = e.x - sa * half, botY = e.y + ca * half;
        float hx, hy;
        if (e.owner != null && !e.owner.dead) {
            hx = e.owner.x + e.owner.facing() * 14;
            hy = e.owner.y - e.owner.h * 0.35f;
        } else {
            hx = e.x - Math.signum(e.vx == 0 ? 1 : e.vx) * 60;
            hy = e.y;
        }
        boolean connectTop = topY <= hy || e.angle != 0 && sa < 0;
        float endX = connectTop ? topX : botX, endY = connectTop ? topY : botY;
        float cpX = hx + (endX - hx) * 0.18f - Math.signum(e.vx == 0 ? 1 : e.vx) * 22;
        float cpY = connectTop ? hy - 52 : hy + 52;
        int alpha = (int) (235 * fade);
        Graphics2D gg = (Graphics2D) g.create();
        gg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fade));
        gg.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        gg.setColor(new Color(e.c2.getRed(), e.c2.getGreen(), e.c2.getBlue(), alpha / 2));
        quad(gg, hx, hy, cpX, cpY, endX, endY);
        gg.setColor(new Color(e.c1.getRed(), e.c1.getGreen(), e.c1.getBlue(), alpha));
        gg.setStroke(new BasicStroke(1.3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        quad(gg, hx, hy, cpX, cpY, endX, endY);
        gg.drawLine((int) topX, (int) topY, (int) botX, (int) botY);
        if (e.ex2 == 2) {
            gg.setStroke(new BasicStroke(0.7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            gg.setColor(new Color(255, 255, 255, alpha / 2));
            quad(gg, hx, hy, cpX, cpY, endX, endY);
        }
        if (e.unbreakable) {
            gg.setStroke(new BasicStroke(0.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            gg.setColor(new Color(255, 190, 200, alpha / 2));
            quad(gg, hx, hy, cpX, cpY, endX, endY);
            gg.drawLine((int) topX, (int) topY, (int) botX, (int) botY);
        }
        gg.dispose();
    }

    private static void contortingThread(Graphics2D g, Effect e, float t) {
        if (e.owner == null) return;
        float fade = Math.min(1, e.life / 0.3f);
        float hx = e.owner.x + e.owner.facing() * 14, hy = e.owner.y - e.owner.h * 0.35f;
        float cpX = (hx + e.x) / 2, cpY = (hy + e.y) / 2 - 34;
        int alpha = (int) (225 * fade);
        Graphics2D gg = (Graphics2D) g.create();
        gg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fade));
        gg.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        gg.setColor(new Color(e.c2.getRed(), e.c2.getGreen(), e.c2.getBlue(), alpha / 2));
        quad(gg, hx, hy, cpX, cpY, e.x, e.y);
        gg.setColor(new Color(e.c1.getRed(), e.c1.getGreen(), e.c1.getBlue(), alpha));
        gg.setStroke(new BasicStroke(1.1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        quad(gg, hx, hy, cpX, cpY, e.x, e.y);
        if (e.vx == 0 && e.vy == 0 && e.pts.size() < 2) {
            Glow.blob(g, e.x, e.y, 7, new Color(e.c2.getRed(), e.c2.getGreen(), e.c2.getBlue(), alpha / 2));
        }
        gg.dispose();
    }

    private static void snatchThread(Graphics2D g, Effect e, float t) {
        if (e.owner == null) return;
        float fade = Math.min(1, (1 - t) * 7f);
        float hx = e.owner.x + e.owner.facing() * 14, hy = e.owner.y - e.owner.h * 0.35f;
        int alpha = (int) (230 * fade);
        float tx, ty, cpX, cpY;
        if (e.latched != null && !e.latched.dead) {
            tx = e.latched.x + e.ex1;
            ty = e.latched.y - e.latched.h * 0.35f;
            cpX = (hx + tx) / 2;
            cpY = Math.min(hy, ty) - 96;
        } else {
            tx = e.x;
            ty = e.y;
            cpX = (hx + tx) / 2;
            cpY = (hy + ty) / 2 + 16;
        }
        Graphics2D gg = (Graphics2D) g.create();
        gg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fade));
        gg.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        gg.setColor(new Color(e.c2.getRed(), e.c2.getGreen(), e.c2.getBlue(), alpha / 2));
        quad(gg, hx, hy, cpX, cpY, tx, ty);
        gg.setColor(new Color(e.c1.getRed(), e.c1.getGreen(), e.c1.getBlue(), alpha));
        gg.setStroke(new BasicStroke(1.1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        quad(gg, hx, hy, cpX, cpY, tx, ty);
        gg.dispose();
    }

    private static void quad(Graphics2D g, float x0, float y0, float cx, float cy, float x1, float y1) {
        Path2D.Float p = new Path2D.Float();
        p.moveTo(x0, y0);
        p.quadTo(cx, cy, x1, y1);
        g.draw(p);
    }

    private static void threadWall(Graphics2D g, Effect e, float t) {
        float fade = Math.min(1, e.life / 0.15f);
        float half = e.ex1;
        boolean hole = e.ex2 == 9;
        float holeHalf = e.spin;
        Graphics2D gg = (Graphics2D) g.create();
        gg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fade));
        gg.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int alpha = (int) (215 * fade);
        gg.setColor(new Color(e.c1.getRed(), e.c1.getGreen(), e.c1.getBlue(), alpha));
        float back = Math.abs(e.vx) > 0 ? -Math.signum(e.vx) * 13 : 0;
        for (int i = -3; i <= 3; i++) {
            float cx = e.x + i * 24;
            float bend = FMath.sin(e.life * 9 + i * 1.7f) * 6;
            if (hole) {
                quad(gg, cx, e.y - half, cx + back + bend, e.y - holeHalf, cx, e.y - holeHalf);
                quad(gg, cx, e.y + half, cx + back + bend, e.y + holeHalf, cx, e.y + holeHalf);
            } else {
                quad(gg, cx, e.y - half, cx + back + bend, e.y, cx, e.y + half);
            }
        }
        gg.setStroke(new BasicStroke(0.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        gg.setColor(new Color(e.c2.getRed(), e.c2.getGreen(), e.c2.getBlue(), alpha / 2));
        for (float yy = e.y - half + 30; yy < e.y + half; yy += 56) {
            if (hole && Math.abs(yy - e.y) < holeHalf + 4) continue;
            gg.drawLine((int) (e.x - 74), (int) yy, (int) (e.x + 74), (int) yy);
        }
        if (hole) {
            float ph = (e.maxLife - e.life) * 11f;
            for (int i = 0; i < 6; i++) {
                float k = (ph + i * 0.52f) % 3.1f;
                float yy = e.y - half + k / 3.1f * (holeHalf - 8);
                float xx = e.x + FMath.sin(ph * 2.3f + i * 2.1f) * 40;
                razorSlash(gg, xx, yy, e.vx > 0 ? 1 : -1, 56, e.c1, e.c2, alpha);
                float yb = e.y + half - k / 3.1f * (holeHalf - 8);
                razorSlash(gg, xx + 20, yb, e.vx > 0 ? 1 : -1, 56, e.c1, e.c2, alpha);
            }
        }
        gg.dispose();
    }

    /** A large curved razor-web slash: hooked crescent blade with a white-hot edge. */
    private static void razorSlash(Graphics2D g, float x, float y, float dir, float len,
                                   Color c1, Color c2, int alpha) {
        Graphics2D gg = (Graphics2D) g.create();
        gg.translate(x, y);
        gg.scale(dir, 1);
        razorSlashLocal(gg, len, c1, c2, alpha);
        gg.dispose();
    }

    private static void razorSlashLocal(Graphics2D g, float len, Color c1, Color c2, int alpha) {
        Path2D.Float hook = new Path2D.Float();
        hook.moveTo(-len * 0.5f, -len * 0.26f);
        hook.quadTo(0, -len * 0.42f, len * 0.42f, -len * 0.08f);
        hook.quadTo(len * 0.62f, len * 0.06f, len * 0.2f, len * 0.34f);
        hook.quadTo(len * 0.52f, len * 0.3f, len * 0.62f, len * 0.05f);
        hook.quadTo(len * 0.6f, len * 0.4f, len * 0.05f, len * 0.42f);
        hook.quadTo(-len * 0.4f, len * 0.42f, -len * 0.5f, -len * 0.26f);
        g.setColor(new Color(c2.getRed(), c2.getGreen(), c2.getBlue(), alpha / 2));
        g.setStroke(new BasicStroke(7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(hook);
        g.setColor(new Color(c1.getRed(), c1.getGreen(), c1.getBlue(), alpha));
        g.setStroke(new BasicStroke(3.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(hook);
        g.setColor(new Color(255, 255, 255, Math.min(255, alpha + 20)));
        g.setStroke(new BasicStroke(1.1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(hook);
        for (int i = 0; i < 4; i++) {
            float k = 0.2f + i * 0.2f;
            float px = FMath.lerp(-len * 0.5f, len * 0.62f, k);
            float py = -len * 0.3f + k * len * 0.55f;
            g.setColor(new Color(c1.getRed(), c1.getGreen(), c1.getBlue(), alpha));
            g.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawLine((int) px, (int) py, (int) (px + 14), (int) (py - 16));
        }
    }

    private static void threadCage(Graphics2D g, Effect e, float t) {
        float fade = Math.min(1, e.life / 0.12f);
        float r = e.r;
        float drawProgress = e.warmupTotal > 0 && e.warmup > 0
                ? FMath.clamp(1 - e.warmup / e.warmupTotal, 0, 1) : 1f;
        Graphics2D gg = (Graphics2D) g.create();
        gg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fade));
        int alpha = (int) (225 * fade);
        gg.setColor(new Color(e.c1.getRed(), e.c1.getGreen(), e.c1.getBlue(), alpha));
        if (e.ex2 == 8) {
            float band = 30;
            gg.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int extent = Math.round(-360 * drawProgress);
            gg.drawArc((int) (e.x - r), (int) (e.y - r), (int) (r * 2), (int) (r * 2), 90, extent);
            float spin = (e.maxLife - e.life) * 9f;
            for (int i = 0; i < (drawProgress >= 1 ? 7 : 0); i++) {
                float a = spin * 2.4f + i * 0.9f;
                float ax = e.x + FMath.cos(a) * r, ay = e.y + FMath.sin(a) * r;
                float tang = a + (float) Math.PI * 0.5f;
                Graphics2D sg = (Graphics2D) gg.create();
                sg.translate(ax, ay);
                sg.rotate(tang + FMath.sin(spin * 3f + i * 1.7f) * 0.3f);
                razorSlashLocal(sg, r * 0.34f, e.c1, e.c2, alpha);
                sg.dispose();
            }
            float ir = Math.max(8, r - band);
            gg.setStroke(new BasicStroke(0.9f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            gg.setColor(new Color(e.c2.getRed(), e.c2.getGreen(), e.c2.getBlue(), alpha / 2));
            gg.drawArc((int) (e.x - ir), (int) (e.y - ir), (int) (ir * 2), (int) (ir * 2), 90, extent);
        } else {
            float band = 30;
            gg.setStroke(new BasicStroke(1.3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            drawPartialRect(gg, e.x - r, e.y - r, r * 2, r * 2, drawProgress);
            float ir = Math.max(8, r - band);
            gg.setStroke(new BasicStroke(0.9f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            drawPartialRect(gg, e.x - ir, e.y - ir, ir * 2, ir * 2, drawProgress);
            gg.setStroke(new BasicStroke(0.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int gridAlpha = (int) (alpha / 2f * drawProgress);
            gg.setColor(new Color(e.c2.getRed(), e.c2.getGreen(), e.c2.getBlue(), gridAlpha));
            for (float xx = e.x - ir + 20; xx < e.x + ir; xx += 20) {
                float wob = FMath.sin(e.life * 10 + xx) * 3;
                quad(gg, xx, e.y - r + wob, xx + wob, e.y - ir, xx, e.y - ir);
                quad(gg, xx, e.y + ir + wob, xx + wob, e.y + r, xx, e.y + r);
            }
            for (float yy = e.y - ir + 20; yy < e.y + ir; yy += 20) {
                float wob = FMath.sin(e.life * 10 + yy) * 3;
                quad(gg, e.x - r + wob, yy, e.x - ir, yy + wob, e.x - ir, yy);
                quad(gg, e.x + ir + wob, yy, e.x + r, yy + wob, e.x + r, yy);
            }
        }
        gg.dispose();
    }

    private static void drawHandPalm(Graphics2D g, float hx, float hy, float angle, float scale, int alpha) {
        Graphics2D gg = (Graphics2D) g.create();
        gg.translate(hx, hy);
        gg.rotate(angle + Math.PI / 2);
        gg.scale(scale, scale);
        gg.setColor(new Color(96, 138, 72, alpha));
        gg.fillOval(-13, -12, 26, 24);
        gg.setColor(new Color(128, 168, 96, alpha));
        gg.fillOval(-9, -9, 18, 16);
        gg.setColor(new Color(60, 92, 46, alpha));
        gg.setStroke(new BasicStroke(4.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = -2; i <= 2; i++)
            gg.drawLine(i * 5, -4, i * 6, -18 - (2 - Math.abs(i)) * 3);
        gg.setColor(new Color(255, 240, 200, Math.min(210, alpha)));
        gg.fillOval(-3, -5, 6, 5);
        gg.dispose();
    }

    private static void drawPartialRect(Graphics2D g, float x, float y, float w, float h, float progress) {
        float total = 2 * (w + h), remain = total * FMath.clamp(progress, 0, 1);
        float[][] segs = {{x, y, x + w, y}, {x + w, y, x + w, y + h},
                {x + w, y + h, x, y + h}, {x, y + h, x, y}};
        for (float[] s : segs) {
            float len = FMath.dist(s[0], s[1], s[2], s[3]);
            float take = Math.min(len, remain);
            if (take <= 0) break;
            float k = take / len;
            g.drawLine(Math.round(s[0]), Math.round(s[1]),
                    Math.round(FMath.lerp(s[0], s[2], k)), Math.round(FMath.lerp(s[1], s[3], k)));
            remain -= take;
        }
    }

    private static void waterRibbon(Graphics2D g, Effect e, float t) {        int n = e.pts.size();
        if (n < 2) return;
        float overall = Math.max(0, 1 - t * 0.55f);
        // fade + taper from the back of the trail: thin & transparent at the
        // oldest point, thick & bright near the dasher
        for (int i = 1; i < n; i++) {
            float[] a = e.pts.get(i - 1);
            float[] b = e.pts.get(i);
            float k = i / (float) n;
            float wdt = 0.8f + 8.2f * k;
            g.setStroke(new BasicStroke(wdt + 3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(new Color(e.c1.getRed(), e.c1.getGreen(), e.c1.getBlue(), (int) (160 * k * overall)));
            g.drawLine((int) a[0], (int) a[1], (int) b[0], (int) b[1]);
            g.setStroke(new BasicStroke(Math.max(0.6f, wdt * 0.45f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(new Color(e.c2.getRed(), e.c2.getGreen(), e.c2.getBlue(), (int) (230 * k * overall)));
            g.drawLine((int) a[0], (int) a[1], (int) b[0], (int) b[1]);
        }
    }

    private static void windRibbon(Graphics2D g, Effect e, float t) {
        int n = e.pts.size();
        if (n < 2) return;
        float overall = Math.max(0, 1 - t * 0.6f);
        for (int i = 1; i < n; i++) {
            float[] a = e.pts.get(i - 1);
            float[] b = e.pts.get(i);
            float k = i / (float) n;
            float dx = b[0] - a[0], dy = b[1] - a[1];
            float len = FMath.dist(0, 0, dx, dy);
            float nxp = len > 0.01f ? -dy / len : 0, nyp = len > 0.01f ? dx / len : 1;
            float off = (float) Math.sin(k * Math.PI) * 18f * FMath.sin(k * 36f + e.life * 18f);
            float ax = a[0] + nxp * off, ay = a[1] + nyp * off;
            float bx = b[0] + nxp * off, by = b[1] + nyp * off;
            float wdt = 1.2f + 8.5f * k;
            g.setStroke(new BasicStroke(wdt + 3.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(new Color(e.c1.getRed(), e.c1.getGreen(), e.c1.getBlue(), (int) (145 * k * overall)));
            g.drawLine((int) ax, (int) ay, (int) bx, (int) by);
            g.setStroke(new BasicStroke(Math.max(0.8f, wdt * 0.38f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(new Color(e.c2.getRed(), e.c2.getGreen(), e.c2.getBlue(), (int) (235 * k * overall)));
            g.drawLine((int) ax, (int) ay, (int) bx, (int) by);
        }
    }

    /** Flaming tiger head in side view, jaws wide open, flying forward. */
    private static void tigerHead(Graphics2D g, Effect e, float t) {
        Graphics2D gg = (Graphics2D) g.create();
        gg.translate(e.x, e.y);
        // mirror across the vertical axis when flying left (no upside-down rotation)
        float dir = e.vx >= 0 ? 1f : -1f;
        gg.scale(dir, 1);
        gg.rotate((float) Math.atan2(e.vy, Math.abs(e.vx)) * dir);
        float s = e.r * (1f - t * 0.1f);
        gg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0, 0.95f * (1 - t * 0.5f))));
        // upper head: cranium + snout profile
        gg.setColor(new Color(240, 100, 25));
        gg.fillPolygon(new int[]{(int) (-s * 0.95f), (int) (-s * 0.9f), (int) (-s * 0.6f), (int) (-s * 0.15f),
                        (int) (s * 0.3f), (int) (s * 0.68f), (int) (s * 1.05f), (int) (s * 1.0f),
                        (int) (s * 0.55f), (int) (s * 0.15f), (int) (-s * 0.35f)},
                new int[]{(int) (s * 0.05f), (int) (-s * 0.5f), (int) (-s * 0.9f), (int) (-s * 1.0f),
                        (int) (-s * 0.82f), (int) (-s * 0.55f), (int) (-s * 0.3f), (int) (-s * 0.1f),
                        (int) (-s * 0.06f), (int) (s * 0.02f), (int) (-s * 0.02f)}, 11);
        // ear
        gg.setColor(new Color(205, 72, 15));
        gg.fillPolygon(new int[]{(int) (-s * 0.5f), (int) (-s * 0.3f), (int) (-s * 0.08f)},
                new int[]{(int) (-s * 0.88f), (int) (-s * 1.32f), (int) (-s * 0.95f)}, 3);
        // lower jaw, dropped wide open
        gg.setColor(new Color(215, 82, 18));
        gg.fillPolygon(new int[]{(int) (s * 0.1f), (int) (s * 0.6f), (int) (s * 0.85f),
                        (int) (s * 0.35f), (int) (-s * 0.15f)},
                new int[]{(int) (s * 0.05f), (int) (s * 0.45f), (int) (s * 0.95f),
                        (int) (s * 0.75f), (int) (s * 0.3f)}, 5);
        // dark gaping maw between the jaws
        gg.setColor(new Color(110, 18, 6, 235));
        gg.fillPolygon(new int[]{(int) (s * 0.14f), (int) (s * 0.98f), (int) (s * 0.85f), (int) (s * 0.4f)},
                new int[]{(int) (s * 0.0f), (int) (-s * 0.06f), (int) (s * 0.9f), (int) (s * 0.55f)}, 4);
        // hot glow in the throat
        gg.setColor(new Color(255, 230, 140, 220));
        gg.fillOval((int) (s * 0.25f), (int) (s * 0.18f), (int) (s * 0.22f), (int) (s * 0.16f));
        flameGlare(gg, s * 0.36f, s * 0.25f, s * 0.7f, 0.95f * (1 - t));
        // sharp fangs: upper row hanging down, lower row jutting up
        gg.setColor(new Color(255, 246, 222));
        for (int i = 0; i < 3; i++) {
            float fx = s * (0.32f + i * 0.26f);
            gg.fillPolygon(new int[]{(int) fx, (int) (fx + s * 0.14f), (int) (fx + s * 0.07f)},
                    new int[]{(int) (-s * 0.04f), (int) (-s * 0.04f), (int) (s * 0.34f)}, 3);
        }
        for (int i = 0; i < 2; i++) {
            float fx = s * (0.46f + i * 0.24f);
            float fy = s * (0.4f + i * 0.2f);
            gg.fillPolygon(new int[]{(int) fx, (int) (fx + s * 0.12f), (int) (fx + s * 0.06f)},
                    new int[]{(int) fy, (int) fy, (int) (s * 0.06f)}, 3);
        }
        // nose
        gg.setColor(new Color(120, 25, 8));
        gg.fillOval((int) (s * 0.9f), (int) (-s * 0.26f), (int) (s * 0.14f), (int) (s * 0.1f));
        // glaring eye + brow
        gg.setColor(new Color(255, 240, 170));
        gg.fillOval((int) (s * 0.05f), (int) (-s * 0.62f), (int) (s * 0.3f), (int) (s * 0.13f));
        flameGlare(gg, s * 0.2f, -s * 0.55f, s * 0.35f, 0.45f * (1 - t));
        gg.setColor(new Color(140, 35, 8));
        gg.setStroke(new BasicStroke(Math.max(2f, s * 0.055f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        gg.drawLine((int) (-s * 0.02f), (int) (-s * 0.74f), (int) (s * 0.42f), (int) (-s * 0.58f));
        // tiger stripes on the cranium
        gg.setColor(new Color(165, 42, 10, 225));
        gg.drawLine((int) (-s * 0.55f), (int) (-s * 0.85f), (int) (-s * 0.44f), (int) (-s * 0.42f));
        gg.drawLine((int) (-s * 0.28f), (int) (-s * 0.98f), (int) (-s * 0.2f), (int) (-s * 0.52f));
        gg.drawLine((int) (-s * 0.04f), (int) (-s * 1.0f), (int) (s * 0.04f), (int) (-s * 0.58f));
        gg.drawLine((int) (s * 0.3f), (int) (-s * 0.8f), (int) (s * 0.42f), (int) (-s * 0.5f));
        gg.dispose();
    }

    private static boolean isFlame(Effect e) {
        return e.c1.getRed() > 220 && e.c1.getGreen() < 170 && e.c1.getBlue() < 100;
    }

    private static float fract(float v) {
        return v - (float) Math.floor(v);
    }

    private static void flameGlare(Graphics2D g, float x, float y, float r, float alpha) {
        int a = (int) (FMath.clamp(alpha, 0, 1) * 150);
        if (a <= 0 || r <= 1) return;
        Glow.blob(g, x, y, r, new Color(255, 150, 38, a));
        Glow.blob(g, x, y, r * 0.35f, new Color(255, 245, 180, Math.min(180, a + 45)));
    }
}
