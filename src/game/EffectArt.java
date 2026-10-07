package game;

import java.awt.*;
import java.awt.geom.Path2D;

final class EffectArt {
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
                g.setColor(arm % 2 == 0 ? new Color(5, 8, 6, 190) : new Color(38, 210, 86, 170));
                g.draw(jag);
                g.setStroke(new BasicStroke(4f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
                g.setColor(new Color(190, 255, 200, 150));
                if (arm % 2 != 0) g.draw(jag);
            }
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fade * 0.45f));
            g.setColor(new Color(42, 220, 90, 80));
            g.fillOval((int) (e.x - e.r * 0.95f), (int) (e.y - height * grow), (int) (e.r * 1.9f), (int) (height * grow));
            return;
        }
        if (e.ex2 == 4) {
            float fade = Math.max(0, 0.86f * (1 - t * 0.25f));
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fade));
            float spin = e.life * 13f;
            for (int arm = 0; arm < 8; arm++) {
                float a = spin + arm * 0.78f;
                float inner = e.r * 0.25f;
                float outer = e.r * (0.9f + FMath.sin(spin + arm) * 0.16f);
                int x1 = (int) (e.x + FMath.cos(a) * inner);
                int y1 = (int) (e.y + FMath.sin(a) * inner * 0.75f);
                int x2 = (int) (e.x + FMath.cos(a + 0.7f) * outer);
                int y2 = (int) (e.y + FMath.sin(a + 0.7f) * outer * 0.75f);
                g.setStroke(new BasicStroke(arm % 2 == 0 ? 7f : 4f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
                g.setColor(arm % 2 == 0 ? new Color(8, 12, 10, 200) : new Color(62, 225, 102, 185));
                g.drawLine(x1, y1, x2, y2);
            }
            g.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(new Color(210, 255, 215, 150));
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
        float sc = 1 + t * 0.5f;
        g.scale(sc, sc);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0, 0.9f * (1 - t))));
        g.rotate(Math.atan2(e.vy, e.vx));
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

    private static void slamRing(Graphics2D g, Effect e, float t) {
        float r = e.r * FMath.easeOut(t);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0, 0.85f * (1 - t))));
        g.setStroke(new BasicStroke(10 * (1 - t) + 2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(new Color(e.c1.getRed(), e.c1.getGreen(), e.c1.getBlue(), 220));
        g.drawOval((int) (e.x - r), (int) (e.y - r * 0.4f), (int) (r * 2), (int) (r * 0.8f));
        g.setColor(new Color(e.c2.getRed(), e.c2.getGreen(), e.c2.getBlue(), 230));
        g.setStroke(new BasicStroke(4 * (1 - t) + 1));
        float r2 = r * 0.66f;
        g.drawOval((int) (e.x - r2), (int) (e.y - r2 * 0.4f), (int) (r2 * 2), (int) (r2 * 0.8f));
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
        gg.setColor(new Color(190, 24, 38, 210));
        gg.setStroke(new BasicStroke(6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        gg.drawLine(-24, 0, 18, 0);
        gg.fillPolygon(new int[]{18, 34, 18}, new int[]{-12, 0, 12}, 3);
        gg.setColor(new Color(255, 110, 120, 180));
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
