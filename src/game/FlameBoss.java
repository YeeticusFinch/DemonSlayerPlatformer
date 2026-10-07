package game;

import java.awt.*;

public class FlameBoss extends Enemy {
    private float specialCd = 2.2f;
    private boolean phase2;
    private boolean comboFlip;
    private final java.util.ArrayList<Ability> forms = Ability.flame();
    private int formIdx;
    private boolean castingForm;

    public FlameBoss() {
        super(32, 94);
        team = 1;
        name = "Kurenai, Blazing Captain";
        isBoss = true;
        maxHp = hp = 330;
        runSpeed = 235;
        aggroR = 1100;
        atkRange = 96;
        dmg = 11;
        elite = true;
        eliteColor = AbilityCast.FLAME;
    }

    @Override
    protected void think(World w, float dt, float dx, float dist) {
        if (!phase2 && hp < maxHp * 0.55f) {
            phase2 = true;
            runSpeed = 275;
            w.banner("The captain ignites!", "");
            w.cam.shake(6, 0.3f);
            for (int i = 0; i < 14; i++)
                w.parts.spawn(Particles.EMBER, x + FMath.rand(-20, 20), y + FMath.rand(-80, 10), FMath.rand(-60, 60), FMath.rand(-220, -60), 0.8f, 9, AbilityCast.FLAME_HI, -70, 0.95f);
        }
        float scale = phase2 ? 0.7f : 1f;
        specialCd -= dt;
        face(dx != 0 ? dx : vx);
        switch (state) {
            case 0 -> {
                eliteMove(w, dt, dx, dist);
                if (eliteDashT <= 0)
                    vx = FMath.approach(vx, Math.signum(dx == 0 ? 1 : dx) * runSpeed * (dist > 300 ? 1 : 0.6f), 2400 * dt);
                if (FMath.chance(dt * 10))
                    w.parts.spawn(Particles.EMBER, x + facing() * 14, y + FMath.rand(-50, 4), FMath.rand(-20, 20), FMath.rand(-70, -20), 0.5f, 7, AbilityCast.FLAME_HI, -50, 0.95f);
                if (specialCd <= 0) {
                    state = 2;
                    stateT = 0;
                    windup = (dist > 200 ? 0.42f : 0.3f) * scale;
                    castingForm = true;
                } else if (dist < atkRange && atkCd <= 0) {
                    state = 2;
                    stateT = 0;
                    windup = 0.26f;
                    castingForm = false;
                }
            }
            case 2 -> {
                vx = FMath.approach(vx, 0, 2800 * dt);
                if (stateT >= windup) {
                    state = 3;
                    stateT = 0;
                    execute(w, dist);
                }
            }
            case 3 -> {
                if (stateT >= recover) {
                    state = 0;
                    atkCd = 0.85f * scale;
                    specialCd = (phase2 ? 1.7f : 2.5f) + FMath.rand(0, 0.7f);
                }
            }
        }
        stateT += dt;
    }

    private void execute(World w, float dist) {
        Player pl = w.player;
        noteAction();
        swingSide *= -1;
        recover = 0.55f;
        if (castingForm && !forms.isEmpty()) {
            w.castAbility(this, forms.get(formIdx++ % forms.size()));
            castingForm = false;
            return;
        }
        basicStrike(w, 84, 260, 140, comboFlip ? -24 : 16, AbilityCast.FLAME);
        comboFlip = !comboFlip;
    }

    @Override
    protected void strike(World w) {
    }

    @Override
    protected void renderBody(Graphics2D g) {
        g.setColor(new Color(0, 0, 0, 60));
        g.fillOval((int) (x - w), (int) (bottom() - 6), (int) (w * 2), 9);
        Composite old = g.getComposite();
        if (flashT > 0) g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.82f));
        float ph = animPhase * 6.28f;
        float speedF = FMath.clamp(Math.abs(vx) / runSpeed, 0, 1);
        float bob = onGround ? Math.abs(FMath.sin(ph)) * -3f * speedF : 0;
        float crouch = recharging ? 1f : 0f;
        float hipY = bottom() - 30 + bob + crouch * 9f, shY = bottom() - 61 + bob + crouch * 5f;
        float legSwing = FMath.sin(ph) * 0.8f * speedF;
        Color uniform = new Color(44, 38, 52);
        Color haori = phase2 ? new Color(178, 48, 28) : new Color(150, 52, 32);

        leg(g, x - 4, hipY, -legSwing, 29, 7, uniform.darker(), true);
        leg(g, x + 4, hipY, legSwing, 29, 7, uniform, true);

        g.setColor(haori);
        g.fillRoundRect((int) (x - 10), (int) shY, 20, (int) (hipY - shY + 8), 10, 10);
        GradientPaint cape = new GradientPaint(x - 11, shY, haori.brighter(), x + 11, hipY, new Color(255, 140, 40));
        g.setPaint(cape);
        g.fillPolygon(new int[]{(int) (x - facing() * 10), (int) (x - facing() * 19), (int) (x - facing() * 15)},
                new int[]{(int) (shY + 4), (int) (shY + 30), (int) (hipY)}, 3);
        g.setColor(new Color(238, 205, 168));
        g.fillOval((int) x - 8, (int) shY - 18, 17, 17);
        g.setColor(phase2 ? new Color(255, 190, 60) : new Color(40, 32, 30));
        g.fillArc((int) x - 9, (int) shY - 20, 19, 13, 0, 180);
        Polygon brow = new Polygon();
        brow.addPoint((int) (x + facing()), (int) (shY - 11));
        brow.addPoint((int) (x + facing() * 8), (int) (shY - 13));
        brow.addPoint((int) (x + facing() * 8), (int) (shY - 10));
        g.setColor(new Color(120, 30, 25));
        g.fillPolygon(brow);
        g.setColor(new Color(30, 26, 28));
        g.fillRect((int) (x + facing() * 3 - 1), (int) (shY - 12), 2, 3);

        float sx = x + facing() * 7, sy = shY + 5;
        if (guarding) {
            swordBlock(g, sx, sy, uniform.darker(), new Color(255, 150, 50), 1.24f, 6);
        } else {
            double prog = attacking() || state == 3 ? Math.min(1, stateT / 0.22f) : -1;
            double armAng = state == 2 ? Math.toRadians(facingRight ? -130 : 310)
                    : Math.toRadians(facingRight ? 56 + Math.sin(ph) * 10 : 124 - Math.sin(ph) * 10);
            double twist = prog >= 0 ? FMath.easeOut((float) prog * 1.3f) : 0;
            double bladeLocal = armAng + (Math.PI / 2) * (1 - twist);
            double wa = facingRight ? bladeLocal : Math.PI - bladeLocal;
            float aa = (float) (facingRight ? armAng : Math.PI - armAng);
            float hx = sx + FMath.cos(aa) * 23, hy = sy + FMath.sin(aa) * 23;
            limb(g, sx, sy, hx, hy, 6, uniform.darker());
            float len = prog >= 0 || state == 2 ? 62 : 42;
            float bx = hx + FMath.cos((float) wa) * len, by = hy + FMath.sin((float) wa) * len;
            bladeShape(g, hx, hy, bx, by, 1.24f, new Color(255, 150, 50), !facingRight);
        }
        drawLimbFlash(g, limbFlashFA, x + facing() * 7, shY + 8, 6);
        drawLimbFlash(g, limbFlashBA, x - facing() * 7, shY + 8, 6);
        g.setComposite(old);
    }

    @Override
    public void renderGlow(Graphics2D g) {
        if (dead) return;
        renderRechargeGlow(g);
        if (phase2) Glow.blob(g, x, top() + 10, 16, new Color(255, 140, 50, 110));
    }

    @Override
    protected Color abilityColor() { return AbilityCast.FLAME; }
}
