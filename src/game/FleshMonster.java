package game;

import java.awt.*;

/** A mobile mass of the train's flesh, with an eye that sweeps the carriage. */
public class FleshMonster extends Enemy {
    private float eyeCooldown = FMath.rand(3f, 8f), eyeWarnT, eyeOnT, eyeFireCd, eyeAngle, eyeSpin;
    private boolean eyeWarning, eyeActive;

    public FleshMonster() {
        super(68, 76);
        name = "Flesh Monster";
        team = 1;
        isDemon = true;
        maxHp = hp = 180;
        runSpeed = 105;
        aggroR = 1200;
        atkRange = 80;
        dmg = 13;
        elite = true;
        eliteColor = new Color(91, 154, 255);
    }

    @Override public void update(World world, float dt) {
        super.update(world, dt);
        if (dead || sleepT > 0) return;
        if (!eyeWarning && !eyeActive) {
            eyeCooldown -= dt;
            if (eyeCooldown <= 0) {
                eyeWarning = true;
                eyeWarnT = 1f;
                eyeAngle = FMath.rand(0, (float) (Math.PI * 2));
                eyeSpin = FMath.chance(0.5f) ? FMath.rand(1f, 3f) : -FMath.rand(1f, 3f);
            }
            return;
        }
        if (eyeWarning) {
            eyeWarnT -= dt;
            if (eyeWarnT <= 0) {
                eyeWarning = false;
                eyeActive = true;
                eyeOnT = FMath.rand(1f, 3f);
                eyeFireCd = 0;
            }
            return;
        }
        eyeOnT -= dt;
        eyeAngle += eyeSpin * dt;
        eyeFireCd -= dt;
        if (eyeOnT <= 0) {
            eyeActive = false;
            eyeCooldown = FMath.rand(3f, 8f);
            return;
        }
        if (eyeFireCd <= 0) {
            eyeFireCd = 0.08f;
            Effect eye = new Effect(Effect.ENMU_NIGHTMARE, this, false).at(x, y - 8).radius(1).life(0.14f);
            eye.ex1 = 440; eye.ex2 = 5; eye.grav = eyeAngle; eye.spin = eyeSpin;
            eye.followOwner = true;
            eye.sleepOnHit = 1.8f; eye.tickEvery(0.2f); eye.pts.add(new float[]{0, -8});
            world.effects.add(eye);
        }
    }

    @Override protected void strike(World world) {
        basicStrike(world, 74, 200, 115, -14, new Color(160, 72, 140));
    }

    @Override protected void renderBody(Graphics2D g) {
        float pulse = 1 + FMath.sin(Game.time * 5 + x) * 0.035f;
        g.setColor(new Color(0, 0, 0, 65));
        g.fillOval((int) (x - 42), (int) bottom() - 5, 84, 12);
        g.setColor(new Color(61, 33, 57));
        g.fillOval((int) (x - 34 * pulse), (int) (y - 27 * pulse), (int) (68 * pulse), (int) (58 * pulse));
        g.setColor(new Color(130, 75, 119));
        g.fillOval((int) (x - 24), (int) (y - 29), 48, 43);
        g.setColor(new Color(241, 239, 250));
        g.fillOval((int) x - 15, (int) y - 25, 30, 20);
        g.setColor(new Color(58, 154, 255));
        g.fillOval((int) x - 7, (int) y - 25, 15, 17);
        g.setColor(new Color(14, 39, 105));
        g.fillOval((int) x - 2, (int) y - 24, 5, 15);
        if (eyeWarning) {
            float warningPulse = 0.5f + 0.5f * FMath.sin(Game.time * 14f);
            EffectArt.drawEnmuEyeWarning(g, x, y - 16, eyeAngle, 440, warningPulse);
        }
        g.setColor(new Color(83, 42, 73));
        for (int i = 0; i < 4; i++) {
            float side = (i % 2 == 0 ? -1 : 1);
            int sx = (int) (x + side * (22 + i * 4));
            int sy = (int) (y + 3 + i * 5);
            g.setStroke(new BasicStroke(7 - i, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawLine(sx, sy, sx + (int) (side * 20), sy + 14);
        }
    }

    @Override public void renderGlow(Graphics2D g) {
        super.renderGlow(g);
        if (eyeWarning) {
            float pulse = 0.5f + 0.5f * FMath.sin(Game.time * 14f);
            Glow.blob(g, x, y - 16, 23 + pulse * 14, new Color(73, 166, 255, (int) (125 + pulse * 100)));
        }
    }
}
