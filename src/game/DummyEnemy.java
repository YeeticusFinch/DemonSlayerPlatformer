package game;

import java.awt.*;

public class DummyEnemy extends Enemy {
    public DummyEnemy() {
        super(30, 86);
        team = 1;
        name = "Training Dummy";
        maxHp = hp = 100;
        sp = maxSp = 100;
        runSpeed = 0;
        aggroR = 0;
        atkRange = 0;
        dmg = 0;
    }

    @Override
    public void update(World w, float dt) {
        showHpT -= dt;
        timeSinceHurt();
        if (dead) {
            deathT += dt;
            return;
        }
        guarding = false;
        recharging = false;
        atkT -= dt;
        stunT -= dt;
        staggerT -= dt;
        flashT -= dt;
        invulnT -= dt;
        vx = FMath.approach(vx, 0, (onGround ? 700 : 120) * dt);
        physics(w, dt);
    }

    @Override
    protected void strike(World w) {
    }

    @Override
    protected void renderBody(Graphics2D g) {
        g.setColor(new Color(0, 0, 0, 55));
        g.fillOval((int) (x - w * 0.85f), (int) (bottom() - 5), (int) (w * 1.7f), 8);
        Composite old = g.getComposite();
        if (flashT > 0) g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.85f));
        float feet = bottom();
        float sway = FMath.sin(Game.time * 1.8f) * 1.5f + FMath.clamp(vx * 0.01f, -6, 6);
        g.setStroke(new BasicStroke(7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(new Color(84, 54, 32));
        g.drawLine((int) x, (int) (feet - 6), (int) (x + sway), (int) (feet - 76));
        g.setStroke(new BasicStroke(10f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(new Color(154, 104, 56));
        g.drawLine((int) (x - 20), (int) (feet - 48), (int) (x + 20), (int) (feet - 48));
        g.drawLine((int) (x - 14), (int) (feet - 18), (int) (x + 14), (int) (feet - 18));
        g.setColor(new Color(178, 126, 68));
        g.fillRoundRect((int) (x - 15 + sway), (int) (feet - 72), 30, 48, 13, 13);
        g.setColor(new Color(118, 72, 38));
        g.fillOval((int) (x - 13 + sway), (int) (feet - 94), 26, 26);
        g.setStroke(new BasicStroke(2f));
        g.setColor(new Color(72, 42, 26));
        g.drawLine((int) (x - 12 + sway), (int) (feet - 51), (int) (x + 12 + sway), (int) (feet - 51));
        g.drawLine((int) (x - 9 + sway), (int) (feet - 37), (int) (x + 9 + sway), (int) (feet - 37));
        g.setComposite(old);
    }
}
