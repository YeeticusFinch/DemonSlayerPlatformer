package game;

import java.awt.*;

public class Civilian extends HumanEnemy {
    public boolean converted;
    private float wanderT, wanderDir;

    public Civilian() {
        super(false);
        name = "Civilian";
        maxHp = hp = 22;
        runSpeed = 120;
        aggroR = 0;
        dmg = 0;
    }

    @Override
    protected void think(World w, float dt, float dx, float dist) {
        Fighter nearest = null;
        float nd = 99999;
        for (Fighter e : w.enemies) if (e.isDemon && !e.dead && !(e instanceof Muzan)) {
            float d = FMath.dist(x, y, e.x, e.y);
            if (d < nd) { nd = d; nearest = e; }
        }
        if (nearest != null && nd < 420) {
            face(x - nearest.x);
            vx = FMath.approach(vx, Math.signum(x - nearest.x) * runSpeed, 1200 * dt);
        } else {
            wanderT -= dt;
            if (wanderT <= 0) {
                wanderT = FMath.rand(1.0f, 2.8f);
                wanderDir = FMath.rand(-1f, 1f);
                if (Math.abs(wanderDir) < 0.35f) wanderDir = 0;
            }
            face(wanderDir);
            vx = FMath.approach(vx, wanderDir * runSpeed * 0.36f, 700 * dt);
        }
    }

    @Override
    protected float contactDamage() { return 0; }

    @Override
    protected void strike(World w) {}

    @Override
    protected void renderBody(Graphics2D g) {
        g.setColor(new Color(0, 0, 0, 45));
        g.fillOval((int) (x - w * 0.7f), (int) (bottom() - 5), (int) (w * 1.4f), 8);
        float bob = onGround ? Math.abs(FMath.sin(animPhase * 6.28f)) * -1.6f : 0;
        float feet = bottom(), hip = feet - 23 + bob, sh = feet - 48 + bob;
        Color kimono = new Color(116, 92, 136);
        limb(g, x - 3, hip, x - 6, feet - 4, 6, new Color(54, 48, 62));
        limb(g, x + 3, hip, x + 6, feet - 4, 6, new Color(68, 58, 72));
        g.setColor(kimono);
        g.fillRoundRect((int) x - 11, (int) sh, 22, (int) (hip - sh + 10), 10, 10);
        limb(g, x - 9, sh + 5, x - 20, sh + 25, 5, kimono.darker());
        limb(g, x + 9, sh + 5, x + 20, sh + 25, 5, kimono);
        g.setColor(new Color(238, 205, 165));
        g.fillOval((int) x - 9, (int) sh - 19, 19, 19);
        g.setColor(new Color(42, 30, 28));
        g.fillArc((int) x - 11, (int) sh - 22, 23, 16, 0, 180);
        g.fillOval((int) x - 5, (int) sh - 29, 11, 11);
        g.setColor(new Color(30, 24, 24));
        g.fillRect((int) x + facing() * 2 - 1, (int) sh - 12, 2, 3);
    }
}
