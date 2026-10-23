package game;

import java.awt.*;
import java.awt.geom.Path2D;

/** Enmu's exposed engine-room heart, retaining his Blood Demon Art. */
public class EnmuCore extends Enmu {
    private float summonCd = 2.4f;
    private float anchorX = Float.NaN, anchorY;
    private final float[] eyeTimers = new float[6], eyeAngles = new float[6], eyeSpins = new float[6], eyeFireTimers = new float[6];
    private final boolean[] eyeActive = new boolean[6], eyeWarning = new boolean[6];

    public EnmuCore() {
        super();
        name = "Enmu's Core";
        maxHp = hp = 720;
        maxSp = sp = 400;
        runSpeed = 150;
        aggroR = 1700;
        eliteColor = new Color(115, 55, 160);
        // Open the boss intro with a full eye-cone telegraph, then each eye enters
        // its normal randomized warning/attack/cooldown cycle.
        for (int i = 0; i < eyeTimers.length; i++) {
            eyeWarning[i] = true;
            eyeTimers[i] = 1f;
            eyeAngles[i] = FMath.rand(0, (float) (Math.PI * 2));
            eyeSpins[i] = FMath.chance(0.5f) ? FMath.rand(1f, 3f) : -FMath.rand(1f, 3f);
        }
    }

    @Override public void update(World world, float dt) {
        if (Float.isNaN(anchorX)) {
            anchorX = x;
            anchorY = y;
        }
        super.update(world, dt);
        // Keep Enmu's attack selection and casting, but pin the core in the engine.
        x = anchorX;
        y = anchorY;
        vx = vy = 0;
        onGround = true;
        if (dead) return;
        summonCd -= dt;
        if (summonCd <= 0) {
            summonCd = 5.4f;
            spawnFlesh(world, "fleshmonster", x + FMath.rand(-260, 260), "Flesh Monster");
            spawnFlesh(world, "fleshtentacle", x + FMath.rand(-320, 320), "Flesh Tentacle");
        }
        if (sleepT > 0) return;
        // Each eye warns for one second, sweeps while active, then rests for 3-8 seconds.
        for (int eye = 0; eye < eyeTimers.length; eye++) {
            eyeTimers[eye] -= dt;
            if (eyeWarning[eye]) {
                if (eyeTimers[eye] <= 0) {
                    eyeWarning[eye] = false;
                    eyeActive[eye] = true;
                    eyeTimers[eye] = FMath.rand(1f, 3f);
                    eyeFireTimers[eye] = 0;
                }
                continue;
            }
            if (eyeActive[eye]) {
                eyeAngles[eye] += eyeSpins[eye] * dt;
                if (eyeTimers[eye] <= 0) {
                    eyeActive[eye] = false;
                    eyeTimers[eye] = FMath.rand(3f, 8f);
                    continue;
                }
                eyeFireTimers[eye] -= dt;
                if (eyeFireTimers[eye] > 0) continue;
                eyeFireTimers[eye] = FMath.rand(0.07f, 0.12f);
            } else {
                if (eyeTimers[eye] > 0) continue;
                eyeWarning[eye] = true;
                eyeTimers[eye] = 1f;
                eyeAngles[eye] = FMath.rand(0, (float) (Math.PI * 2));
                eyeSpins[eye] = FMath.chance(0.5f) ? FMath.rand(1f, 3f) : -FMath.rand(1f, 3f);
                continue;
            }
            float offsetX = (eye - 2.5f) * 24f;
            float offsetY = -18f + (eye % 2) * 27f;
            float angle = eyeAngles[eye];
            Effect cone = new Effect(Effect.ENMU_NIGHTMARE, this, false)
                    .at(x + offsetX * facing(), y + offsetY).radius(1).life(0.16f);
            cone.ex1 = 760; cone.ex2 = 5; cone.grav = angle; cone.spin = eyeSpins[eye];
            cone.followOwner = true; cone.sleepOnHit = 2.2f; cone.tickEvery(0.2f);
            cone.pts.add(new float[]{offsetX, offsetY});
            world.effects.add(cone);
        }
    }

    @Override protected float rechargeHpRate() { return 0; }

    @Override protected float rechargeHpCap() { return hp; }

    @Override protected boolean rechargeSatisfied() { return sp >= maxSp - 0.5f; }

    private void spawnFlesh(World world, String type, float atX, String label) {
        Fighter add = world.makeEnemy(type);
        float sx = FMath.clamp(atX, 90, world.level.w - 90);
        float gy = world.groundYUnder(sx, world.level.h);
        if (gy > 9000) gy = world.level.h - 90;
        add.init(sx, gy, add.maxHp);
        add.team = team;
        world.enemies.add(add);
        world.popups.add(new World.Popup(sx, gy - 70, label, new Color(189, 130, 199)));
    }

    @Override protected void renderBody(Graphics2D g) {
        float pulse = 0.9f + FMath.sin(Game.time * 4.7f) * 0.08f;
        g.setColor(new Color(0, 0, 0, 90));
        g.fillOval((int) (x - 118), (int) (bottom() - 10), 236, 24);
        for (int i = 0; i < 7; i++) {
            float side = i % 2 == 0 ? -1 : 1;
            float rootX = x + side * (48 + (i % 3) * 18);
            float rootY = y + 28 + (i % 2) * 10;
            Path2D.Float vein = new Path2D.Float();
            vein.moveTo(rootX, rootY);
            vein.curveTo(rootX + side * 40, rootY + 30, rootX - side * 15, rootY + 48,
                    rootX + side * (24 + (i % 3) * 8), bottom() + 8);
            g.setStroke(new BasicStroke(13 - i % 3 * 2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(i % 2 == 0 ? new Color(62, 25, 66) : new Color(155, 52, 111));
            g.draw(vein);
        }
        g.setColor(new Color(32, 16, 42, 215));
        g.fillOval((int) (x - 92 * pulse), (int) (y - 58 * pulse), (int) (184 * pulse), (int) (116 * pulse));
        g.setColor(new Color(129, 51, 127, 230));
        g.fillOval((int) (x - 74 * pulse), (int) (y - 46 * pulse), (int) (148 * pulse), (int) (92 * pulse));
        for (int i = 0; i < 6; i++) {
            float ex = x + (i - 2.5f) * 24;
            float ey = y - 18 + (i % 2) * 27;
            g.setColor(new Color(245, 240, 250));
            g.fillOval((int) ex - 11, (int) ey - 7, 22, 14);
            g.setColor(new Color(55, 151, 255));
            g.fillOval((int) ex - 5, (int) ey - 6, 12, 12);
            g.setColor(new Color(12, 43, 110));
            g.fillOval((int) ex - 2, (int) ey - 5, 4, 10);
            if (eyeWarning[i]) {
                float warningPulse = 0.5f + 0.5f * FMath.sin(Game.time * 14f + i * 1.7f);
                EffectArt.drawEnmuEyeWarning(g, ex, ey, eyeAngles[i], 760, warningPulse);
            }
        }
    }

    @Override public void renderGlow(Graphics2D g) {
        super.renderGlow(g);
        for (int i = 0; i < eyeWarning.length; i++) {
            if (!eyeWarning[i]) continue;
            float pulse = 0.5f + 0.5f * FMath.sin(Game.time * 14f + i * 1.7f);
            float ex = x + (i - 2.5f) * 24;
            float ey = y - 18 + (i % 2) * 27f;
            Glow.blob(g, ex, ey, 20 + pulse * 12, new Color(73, 166, 255, (int) (120 + pulse * 105)));
        }
    }
}
