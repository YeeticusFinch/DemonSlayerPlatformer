package game;

import java.awt.*;

public class Muzan extends Enemy {
    public boolean escaping;
    private float wanderT, wanderDir;

    public Muzan() {
        super(32, 92);
        name = "Muzan";
        team = 1;
        isDemon = true;
        maxHp = hp = 9999;
        runSpeed = 0;
        aggroR = 0;
        dmg = 0;
    }

    @Override
    public boolean hurt(World world, Fighter src, float dmgAmt, float dir, float kbX, float kbY) {
        flashT = 0.05f;
        return false;
    }

    @Override
    protected void think(World w, float dt, float dx, float dist) {
        if (escaping) {
            vx = 520;
            facingRight = true;
        } else {
            wanderT -= dt;
            if (wanderT <= 0) {
                wanderT = FMath.rand(1.3f, 3.2f);
                wanderDir = FMath.rand(-1f, 1f);
                if (Math.abs(wanderDir) < 0.4f) wanderDir = 0;
            }
            face(wanderDir);
            vx = FMath.approach(vx, wanderDir * 54, 520 * dt);
        }
    }

    @Override
    protected void strike(World w) {}

    @Override
    protected void renderBody(Graphics2D g) {
        if (escaping) PlatformArt.car(g, x - 10, bottom() + 4, 0.72f, true);
        float feet = bottom();
        float hip = feet - 31;
        float sh = feet - 67;
        float ph = animPhase * 6.28f * 2.2f;
        float walkAmt = FMath.clamp(Math.abs(vx) / 180f, 0, 1);
        float legSwing = FMath.sin(ph) * 10f * walkAmt;
        g.setColor(new Color(0, 0, 0, 60));
        g.fillOval((int) x - 24, (int) feet - 5, 48, 8);

        Color suit = new Color(14, 14, 20);
        Color suitHi = new Color(30, 30, 38);
        Color shirt = new Color(236, 236, 230);
        Color skin = new Color(246, 226, 210);

        limb(g, x - 5, hip, x - 8 - legSwing, feet - 4, 7, suit.darker());
        limb(g, x + 5, hip, x + 8 + legSwing, feet - 4, 7, suit);

        limb(g, x - facing() * 10, sh + 9, x - facing() * 24, sh + 36, 6, suit.darker());
        limb(g, x + facing() * 10, sh + 9, x + facing() * 24, sh + 33, 6, suitHi);
        g.setColor(skin);
        g.fillOval((int) (x - facing() * 24) - 4, (int) (sh + 36) - 4, 8, 8);
        g.fillOval((int) (x + facing() * 24) - 4, (int) (sh + 33) - 4, 8, 8);

        g.setColor(suit);
        g.fillRoundRect((int) x - 14, (int) sh, 28, (int) (hip - sh + 9), 8, 8);
        Polygon leftLap = new Polygon();
        leftLap.addPoint((int) x - 14, (int) sh + 2);
        leftLap.addPoint((int) x - 1, (int) sh + 26);
        leftLap.addPoint((int) x - 13, (int) hip + 7);
        g.setColor(new Color(4, 4, 8));
        g.fillPolygon(leftLap);
        Polygon rightLap = new Polygon();
        rightLap.addPoint((int) x + 14, (int) sh + 2);
        rightLap.addPoint((int) x + 1, (int) sh + 28);
        rightLap.addPoint((int) x + 13, (int) hip + 7);
        g.setColor(suitHi);
        g.fillPolygon(rightLap);
        g.setColor(shirt);
        g.fillPolygon(new int[]{(int) x - 6, (int) x + 6, (int) x}, new int[]{(int) sh + 4, (int) sh + 4, (int) sh + 34}, 3);
        g.setColor(new Color(160, 20, 34));
        g.fillPolygon(new int[]{(int) x - 4, (int) x + 4, (int) x}, new int[]{(int) sh + 10, (int) sh + 10, (int) sh + 22}, 3);

        g.setColor(skin);
        g.fillOval((int) x - 10, (int) sh - 23, 20, 22);
        g.setColor(new Color(28, 22, 24));
        g.fillArc((int) x - 11, (int) sh - 24, 22, 13, 0, 180);
        g.fillRoundRect((int) x - 10, (int) sh - 19, 5, 11, 4, 4);
        g.fillRoundRect((int) x + 5, (int) sh - 19, 5, 11, 4, 4);

        g.setColor(new Color(245, 245, 238));
        g.fillOval((int) x - 15, (int) sh - 20, 30, 8);
        g.fillRoundRect((int) x - 9, (int) sh - 28, 18, 11, 4, 4);
        g.setColor(new Color(24, 24, 28));
        g.fillRect((int) x - 11, (int) sh - 19, 22, 3);

        g.setColor(new Color(245, 236, 230));
        g.fillArc((int) x - 7, (int) sh - 14, 7, 6, 180, 180);
        g.fillArc((int) x + 1, (int) sh - 14, 7, 6, 180, 180);
        g.setColor(new Color(180, 20, 30));
        g.fillArc((int) x - 6, (int) sh - 13, 5, 4, 180, 180);
        g.fillArc((int) x + 2, (int) sh - 13, 5, 4, 180, 180);
    }

    @Override
    public void renderGlow(Graphics2D g) {
        float sh = bottom() - 67;
        g.setColor(new Color(255, 20, 35, 82));
        g.fillArc((int) x - 8, (int) sh - 14, 9, 7, 180, 180);
        g.fillArc((int) x, (int) sh - 14, 9, 7, 180, 180);
    }
}
