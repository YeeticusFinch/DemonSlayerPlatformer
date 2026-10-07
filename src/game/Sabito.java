package game;

import java.awt.*;

public class Sabito extends Enemy {
    private static final String[] TAUNTS = {"Too slow!", "Is that all?", "Focus.", "Again!", "Hmph."};
    private float teleCd = 3.2f;
    private float waveCd = 2.2f;
    private final java.util.ArrayList<Ability> forms = Ability.water();
    private int formIdx;

    public Sabito() {
        super(28, 84);
        team = 1;
        name = "Sabito";
        isBoss = true;
        maxHp = hp = 130;
        runSpeed = 305;
        aggroR = 900;
        atkRange = 100;
        atkCdBase = 1.05f;
        dmg = 8;
        elite = true;
        eliteColor = new Color(190, 230, 255);
    }

    @Override
    protected void think(World w, float dt, float dx, float dist) {
        Player pl = w.player;
        teleCd -= dt;
        waveCd -= dt;
        face(dx != 0 ? dx : vx);
        switch (state) {
            case 0 -> {
                eliteMove(w, dt, dx, dist);
                if (eliteDashT <= 0)
                    vx = FMath.approach(vx, Math.signum(dx == 0 ? 1 : dx) * runSpeed, 2600 * dt);
                if (FMath.chance(dt * 14))
                    w.ghosts.add(new Ghost(x, bottom(), h, new Color(220, 228, 255)));
                if (dist < atkRange && atkCd <= 0) {
                    state = 2;
                    stateT = 0;
                } else if (waveCd <= 0 && dist > 180 && dist < 640 && Math.abs(pl.y - y) < 120) {
                    waveCd = FMath.rand(2.4f, 3.6f);
                    state = 6;
                    stateT = 0;
                } else if (teleCd <= 0) {
                    teleCd = FMath.rand(3.4f, 4.6f);
                    state = 5;
                    stateT = 0;
                    invulnT = 0.55f;
                    w.popup(x, top() - 14, TAUNTS[FMath.randInt(0, TAUNTS.length - 1)], new Color(225, 235, 255));
                } else if (touchingWall != 0 && onGround) vy = -720;
            }
            case 2 -> {
                vx = FMath.approach(vx, 0, 3000 * dt);
                if (stateT >= 0.24f) {
                    state = 3;
                    stateT = 0;
                    recover = 0.55f;
                    vx += facing() * 340;
                    noteAction();
                    swingSide *= -1;
                    for (int i = 0; i < 3; i++) {
                        final int idx = i;
                        w.schedule(i * 0.11f, () -> {
                            w.meleeStrike(this, 96, 8, 200, 110,
                                    Math.toRadians(facingRight ? -26 + idx * 24 : 206 - idx * 24),
                                    Math.toRadians(150), idx % 2 == 0 ? new Color(188, 220, 255) : new Color(240, 248, 255));
                            w.parts.burst(Particles.DROP, x + facing() * 50, y - 10, 5, 150, 0.25f, 4,
                                    new Color(170, 215, 255), 280, 0.95f);
                        });
                    }
                    atkCd = atkCdBase;
                }
            }
            case 3 -> {
                vx = FMath.approach(vx, Math.signum(dx) * runSpeed * 0.4f, 1400 * dt);
                if (stateT >= recover) state = 0;
            }
            case 5 -> {
                vx = 0;
                if (FMath.chance(dt * 40)) w.parts.spawn(Particles.CIRCLE, x + FMath.rand(-14, 14), y + FMath.rand(-40, 10), FMath.rand(-20, 20), FMath.rand(-60, -20), 0.5f, 7, new Color(210, 225, 255), -40, 0.96f);
                if (stateT >= 0.4f) {
                    float nx = pl.x - pl.facing() * 74;
                    if (!w.solidAtPoint(nx, y)) x = nx;
                    facingRight = pl.x < x;
                    invulnT = 0.3f;
                    w.parts.ring(x, y, new Color(220, 230, 255));
                    state = 0;
                    if (FMath.chance(0.55f) && Math.abs(pl.x - x) < 130) {
                        state = 2;
                        stateT = 0.1f;
                    }
                }
            }
            case 6 -> {
                vx = FMath.approach(vx, 0, 2600 * dt);
                if (stateT >= 0.34f) {
                    w.castAbility(this, forms.get(formIdx++ % forms.size()));
                    state = 0;
                }
            }
        }
        stateT += dt;
    }

    @Override
    protected void strike(World w) {
    }

    @Override
    public boolean hurt(World world, Fighter src, float dmgAmt, float dir, float kbX, float kbY) {
        kbX *= 0.35f;
        kbY *= 0.6f;
        return super.hurt(world, src, dmgAmt, dir, kbX, kbY);
    }

    @Override
    protected void renderBody(Graphics2D g) {
        g.setColor(new Color(0, 0, 0, 45));
        g.fillOval((int) (x - w * 0.8f), (int) (bottom() - 5), (int) (w * 1.6f), 8);
        Composite old = g.getComposite();
        float alpha = state == 5 ? Math.max(0, 0.92f * (1 - stateT / 0.4f)) : 0.94f;
        if (flashT > 0) g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.min(alpha + 0.08f, 1)));
        else g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

        float ph = animPhase * 6.28f;
        float speedF = FMath.clamp(Math.abs(vx) / runSpeed, 0, 1);
        float bob = onGround ? Math.abs(FMath.sin(ph)) * -2.6f * speedF : 0;
        float crouch = recharging ? 1f : 0f;
        float hipY = bottom() - 26 + bob + crouch * 8f, shY = bottom() - 53 + bob + crouch * 5f;
        float legSwing = FMath.sin(ph) * 0.85f * speedF;
        Color gi = new Color(52, 56, 66);
        Color skin = new Color(236, 208, 178);

        leg(g, x - 3, hipY, -legSwing, 25, 6, gi.darker(), true);
        leg(g, x + 3, hipY, legSwing, 25, 6, gi, true);

        boolean swordDrawn = guarding || timeSinceAction < 5f || attacking() || state == 2 || state == 3 || state == 6;

        g.setColor(gi);
        g.fillRoundRect((int) (x - 9), (int) shY, 18, (int) (hipY - shY + 8), 9, 9);
        if (!guarding) limb(g, x - facing() * 7, shY + 4, x - facing() * 21, shY + 27, 5, gi.darker());
        g.setColor(skin);
        g.fillOval((int) x - 8, (int) shY - 17, 17, 17);

        Polygon hairSpike = new Polygon();
        hairSpike.addPoint((int) x - 10, (int) shY - 8);
        hairSpike.addPoint((int) x - 6, (int) shY - 25);
        hairSpike.addPoint((int) x + 1, (int) shY - 20);
        hairSpike.addPoint((int) x + 7, (int) shY - 26);
        hairSpike.addPoint((int) x + 10, (int) shY - 8);
        g.setColor(new Color(238, 240, 246));
        g.fillPolygon(hairSpike);

        g.setColor(new Color(245, 245, 250));
        g.fillOval((int) x + facing() * 2 - 8, (int) shY - 16, 15, 16);
        g.setColor(new Color(200, 40, 50));
        g.setStroke(new BasicStroke(1.8f));
        int mx = (int) (x + facing() * 2);
        g.drawPolyline(new int[]{mx - 6, mx - 3, mx}, new int[]{(int) (shY - 12), (int) (shY - 10), (int) (shY - 12)}, 3);
        g.drawPolyline(new int[]{mx, mx + 3, mx + 6}, new int[]{(int) (shY - 12), (int) (shY - 10), (int) (shY - 12)}, 3);
        g.setColor(new Color(30, 30, 36));
        g.drawLine(mx - 4, (int) (shY - 7), mx - 1, (int) (shY - 7));
        g.drawLine(mx + 1, (int) (shY - 7), mx + 4, (int) (shY - 7));
        g.setColor(new Color(190, 60, 65));
        g.drawLine(mx - 7, (int) (shY - 4), mx - 5, (int) (shY - 1));
        g.drawLine(mx + 7, (int) (shY - 4), mx + 5, (int) (shY - 1));

        float sx = x + facing() * 6, sy = shY + 5;
        double prog = attacking() || state == 3 ? Math.min(1, stateT / 0.22f) : -1;
        if (guarding) {
            swordBlock(g, sx, sy, gi.darker(), AbilityCast.WATER, 1.05f, 5);
        } else if (swordDrawn) {
            double armAng;
            if (prog >= 0) armAng = Math.toRadians(facingRight ? 190 - 230 * prog : 350 - 230 * prog);
            else armAng = Math.toRadians(facingRight ? 52 : 128) + FMath.sin(ph) * 0.18f;
            double twist = prog >= 0 ? FMath.easeOut((float) prog * 1.3f) : 0;
            double bladeLocal = armAng - (Math.PI / 2) * (1 - twist);
            double wa = facingRight ? bladeLocal : Math.PI - bladeLocal;
            float aa = (float) (facingRight ? armAng : Math.PI - armAng);
            float hx = sx + FMath.cos(aa) * 22, hy = sy + FMath.sin(aa) * 22;
            limb(g, sx, sy, hx, hy, 5, gi.darker());
            float len = prog >= 0 ? 55 : 38;
            float bx = hx + FMath.cos((float) wa) * len, by = hy + FMath.sin((float) wa) * len;
            bladeShape(g, hx, hy, bx, by, 1.05f, new Color(224, 196, 148), !facingRight);
        } else {
            g.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(new Color(14, 14, 18));
            g.drawLine((int) (x + facing() * 2), (int) (hipY + 6), (int) (x - facing() * 11), (int) (hipY + 24));
        }
        drawLimbFlash(g, limbFlashFA, x + facing() * 7, shY + 8, 6);
        drawLimbFlash(g, limbFlashBA, x - facing() * 7, shY + 8, 6);
        g.setComposite(old);
    }

    @Override
    public void renderGlow(Graphics2D g) {
        if (dead || state == 5) return;
        renderRechargeGlow(g);
        Glow.blob(g, x + facing() * 4, top() + h * 0.22f, 9, new Color(160, 220, 255, 130));
    }

    @Override
    protected Color abilityColor() { return AbilityCast.WATER; }
}
