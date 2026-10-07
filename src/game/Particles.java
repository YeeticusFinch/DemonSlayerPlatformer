package game;

import java.awt.*;

public class Particles {
    public static final int CIRCLE = 0, SPARK = 1, EMBER = 2, SMOKE = 3, DROP = 4, LEAF = 5, BLOOD = 6, PETAL = 7, RING = 8, FIRE = 9;

    private static class P {
        float x, y, vx, vy, life, maxLife, size, grav, drag;
        Color c;
        int kind;
        float rot, spin;
    }

    private final java.util.ArrayList<P> list = new java.util.ArrayList<>();

    public void clear() { list.clear(); }

    public int size() { return list.size(); }

    public void removeOldest() { if (!list.isEmpty()) list.remove(0); }

    public void ring(float x, float y, Color c) {
        spawn(RING, x, y, 0, 0, 0.35f, 14, c, 0, 1);
    }

    public void spawn(int kind, float x, float y, float vx, float vy, float life, float size, Color c, float grav, float drag) {
        P p = new P();
        p.kind = kind;
        p.x = x;
        p.y = y;
        p.vx = vx;
        p.vy = vy;
        p.life = p.maxLife = life;
        p.size = size;
        p.c = c;
        p.grav = grav;
        p.drag = drag;
        p.rot = FMath.rand(0, 6.28f);
        p.spin = FMath.rand(-4, 4);
        list.add(p);
    }

    public void burst(int kind, float x, float y, int n, float speed, float life, float size, Color c, float grav, float drag) {
        for (int i = 0; i < n; i++) {
            float a = FMath.rand(0, (float) Math.PI * 2);
            float s = FMath.rand(speed * 0.3f, speed);
            spawn(kind, x, y, FMath.cos(a) * s, FMath.sin(a) * s - speed * 0.2f, FMath.rand(life * 0.5f, life), size * FMath.rand(0.6f, 1.3f), c, grav, drag);
        }
    }

    public void update(float dt) {
        for (int i = list.size() - 1; i >= 0; i--) {
            P p = list.get(i);
            p.life -= dt;
            if (p.life <= 0) { list.remove(i); continue; }
            p.vy += p.grav * dt;
            float d = (float) Math.pow(p.drag, dt * 60f);
            p.vx *= d;
            p.vy *= d;
            p.x += p.vx * dt;
            p.y += p.vy * dt;
            p.rot += p.spin * dt;
        }
    }

    public void render(Graphics2D g) {
        for (P p : list) {
            float t = p.life / p.maxLife;
            int alpha = (int) (255 * Math.min(1, t * 1.6f));
            g.setColor(new Color(p.c.getRed(), p.c.getGreen(), p.c.getBlue(), alpha));
            switch (p.kind) {
                case SPARK -> {
                    float len = p.size * (1.5f + t);
                    g.setStroke(new BasicStroke(Math.max(1, p.size * 0.35f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    float dx = p.vx, dy = p.vy;
                    float m = FMath.dist(0, 0, dx, dy);
                    if (m < 1) { dx = 1; dy = 0; m = 1; }
                    g.drawLine((int) p.x, (int) p.y, (int) (p.x - dx / m * len), (int) (p.y - dy / m * len));
                }
                case EMBER, FIRE -> {
                    int s = (int) (p.size * (p.kind == FIRE ? 1f : t));
                    g.fillOval((int) (p.x - s / 2f), (int) (p.y - s / 2f), s, s);
                    g.setColor(new Color(255, 240, 180, alpha / 2));
                    g.fillOval((int) (p.x - s / 4f), (int) (p.y - s / 4f), Math.max(1, s / 2), Math.max(1, s / 2));
                }
                case SMOKE -> {
                    int s = (int) (p.size * (2f - t));
                    g.setColor(new Color(p.c.getRed(), p.c.getGreen(), p.c.getBlue(), alpha / 3));
                    g.fillOval((int) (p.x - s / 2f), (int) (p.y - s / 2f), s, s);
                }
                case RING -> {
                    float r = p.size * (1f - t) * 3f;
                    g.setStroke(new BasicStroke(Math.max(1, 3 * t), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g.drawOval((int) (p.x - r), (int) (p.y - r), (int) (r * 2), (int) (r * 2));
                }
                case LEAF, PETAL -> {
                    g.translate(p.x, p.y);
                    g.rotate(p.rot);
                    g.fillOval((int) (-p.size), (int) (-p.size / 2), (int) (p.size * 2), (int) p.size);
                    g.rotate(-p.rot);
                    g.translate(-p.x, -p.y);
                }
                case DROP -> {
                    g.setStroke(new BasicStroke(Math.max(1, p.size * 0.5f)));
                    g.drawLine((int) p.x, (int) p.y, (int) (p.x - p.vx * 0.02f), (int) (p.y - p.vy * 0.02f));
                }
                default -> {
                    int s = (int) Math.max(1, p.size * t);
                    g.fillOval((int) (p.x - s / 2f), (int) (p.y - s / 2f), s, s);
                }
            }
        }
    }
}
