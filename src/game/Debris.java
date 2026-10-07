package game;

import java.awt.*;

public class Debris {
    public static final int HEAD = 0, TORSO = 1, LIMB = 2, CHUNK = 3;

    public float x, y, vx, vy, rot, vr, size, life;
    public Color c;
    public int kind;

    public Debris(int kind, float x, float y, float vx, float vy, float size, float life, Color c) {
        this.kind = kind;
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.size = size;
        this.life = life;
        this.c = c;
        this.rot = FMath.rand(0, 6.28f);
        this.vr = FMath.rand(-9, 9);
    }

    public void update(World w, float dt) {
        vy += 1750 * dt;
        x += vx * dt;
        y += vy * dt;
        rot += vr * dt;
        float gy = w.groundYUnder(x, y - 6);
        if (gy < 9000 && y > gy - 3 && vy > 0) {
            y = gy - 3;
            vy *= -0.36f;
            vx *= 0.6f;
            vr *= 0.5f;
            if (Math.abs(vy) < 40) vy = 0;
        }
        life -= dt;
    }

    public boolean dead() { return life <= 0; }

    public void render(Graphics2D g) {
        float a = Math.min(1, life * 1.5f);
        Composite old = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, a));
        g.translate(x, y);
        g.rotate(rot);
        g.setColor(c);
        switch (kind) {
            case HEAD -> {
                g.fillOval((int) -size, (int) -size, (int) (size * 2), (int) (size * 2));
                g.setColor(c.darker());
                g.fillRect((int) -size * 2 / 3, (int) (-size / 2), (int) (size * 4 / 3), 2);
            }
            case TORSO -> g.fillRoundRect((int) -size, (int) (-size * 1.4f), (int) (size * 2), (int) (size * 2.8f), 6, 6);
            case LIMB -> g.fillRoundRect((int) -size * 2 / 5, (int) -size, (int) (size * 4 / 5), (int) (size * 2), (int) size, (int) size);
            default -> {
                g.fillOval((int) -size, (int) -size * 2 / 3, (int) (size * 2), (int) (size * 4 / 3));
                g.setColor(c.brighter());
                g.fillOval((int) -size / 2, (int) -size / 3, (int) size, (int) (size * 2 / 3));
            }
        }
        g.rotate(-rot);
        g.translate(-x, -y);
        g.setComposite(old);
    }
}
