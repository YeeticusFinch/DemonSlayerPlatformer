package game;

import java.awt.*;

public class HandDemon extends Enemy {
    private float grabCd, swipeCd, slamCd = 1.6f, artCd = 3.5f;
    private final java.util.ArrayList<Ability> forestArts = Ability.forestHand();
    private int forestIdx;
    private Ability pendingArt;
    private boolean grabbing;
    public boolean phase2;

    public HandDemon() {
        super(108, 178);
        team = 1;
        isDemon = true;
        name = "The Hand Demon";
        isBoss = true;
        maxHp = hp = 215;
        runSpeed = 88;
        aggroR = 1200;
        atkRange = 200;
        dmg = 14;
    }

    @Override
    protected void think(World w, float dt, float dx, float dist) {
        if (!phase2 && hp < maxHp * 0.5f) {
            phase2 = true;
            runSpeed = 122;
            w.banner("It's furious...", "");
            w.cam.shake(8, 0.4f);
        }
        float scale = phase2 ? 0.68f : 1f;
        grabCd -= dt;
        swipeCd -= dt;
        slamCd -= dt;
        artCd -= dt;
        face(dx != 0 ? dx : vx);

        if (state == 0 || state == 3 || state == 4) {
            vx = FMath.approach(vx, Math.signum(dx == 0 ? 1 : dx) * runSpeed, 1400 * dt);
            if (touchingWall != 0 && onGround) vy = -760;
            if (state != 4) {
                if (artCd <= 0 && dist > 220 && dist < 760) {
                    state = 4;
                    stateT = 0;
                    windup = 0.55f * scale;
                    pendingArt = nextRangedForestArt();
                    grabbing = false;
                } else if (dist < 128 && grabCd <= 0) {
                    state = 2;
                    stateT = 0;
                    windup = 0.4f * scale;
                    grabbing = true;
                    grabCd = phase2 ? 2.9f : 4.2f;
                } else if (dist < 205 && swipeCd <= 0) {
                    state = 2;
                    stateT = 0;
                    windup = 0.36f * scale;
                    dmg = 14;
                    grabbing = false;
                    vx += facing() * 60;
                } else if (slamCd <= 0 && dist < 520 && dist > 210) {
                    state = 4;
                    stateT = 0;
                    windup = 0.58f * scale;
                    pendingArt = forestArt(Ability.Kind.HAND_SLAM);
                    grabbing = false;
                    slamCd = phase2 ? 1.9f : 2.9f;
                }
            }
        }
        if (state == 2 && stateT >= windup) {
            state = 3;
            stateT = 0;
            recover = 0.5f * scale;
            execute(w);
        }
        if (state == 4) {
            if (stateT >= windup) {
                state = 3;
                stateT = 0;
                recover = 0.6f * scale;
                if (pendingArt != null) w.castAbility(this, pendingArt);
                pendingArt = null;
                artCd = phase2 ? 4.5f : 6.5f;
            }
        }
        if (state == 3 && stateT >= recover) state = 0;
        stateT += dt;
    }

    private void execute(World w) {
        if (grabbing) {
            Player pl = w.player;
            if (!pl.dead && pl.overlapsRect(x + facing() * 40 - 55, y - 70, 130, 100)) {
                pl.hurt(w, this, 20, facing(), 240, 0);
                pl.vx = facing() * 660;
                pl.vy = -430;
                dealtHit();
                w.popup(pl.x, pl.top() - 10, "SEIZED!", new Color(255, 90, 110));
                w.cam.shake(7, 0.3f);
            }
            w.meleeStrike(this, 96, 0, 0, 0, Math.toRadians(facingRight ? -12 : 192), Math.toRadians(90), new Color(120, 30, 45));
            grabbing = false;
            return;
        }
        swipeCd = phase2 ? 1.35f : 2f;
        w.meleeStrike(this, 152, 14, 520, 280, Math.toRadians(facingRight ? -16 : 196), Math.toRadians(170), new Color(110, 140, 90));
        w.cam.shake(5, 0.18f);
    }

    private Ability forestArt(Ability.Kind kind) {
        for (Ability a : forestArts) if (a.kind == kind) return a;
        return forestArts.get(0);
    }

    private Ability nextRangedForestArt() {
        Ability a;
        do a = forestArts.get(forestIdx++ % forestArts.size());
        while (a.kind == Ability.Kind.HAND_GRASP);
        return a;
    }

    @Override
    protected void strike(World w) {
    }

    @Override
    public void update(World w, float dt) {
        super.update(w, dt);
        demonRegen(w, dt, 1.1f);
    }

    @Override
    protected float rechargeHpRate() { return 12f; }

    @Override
    protected void renderBody(Graphics2D g) {
        g.setColor(new Color(0, 0, 0, 70));
        g.fillOval((int) (x - 78), (int) (bottom() - 8), 156, 14);
        Composite old = g.getComposite();
        if (flashT > 0) g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.82f));
        float feet = bottom();
        float ph = animPhase * 5f;
        float bob = onGround ? Math.abs(FMath.sin(ph)) * -3.5f : 0;
        if (recharging) bob += 10f;
        Color hide = phase2 ? new Color(108, 118, 84) : new Color(94, 112, 88);
        Color dark = hide.darker().darker();

        limb(g, x - 28, feet - 46, x - 30, feet - 4, 24, dark);
        limb(g, x + 28, feet - 46, x + 32, feet - 4, 24, dark);
        limb(g, x - facing() * 48, feet - 116 + bob, x - facing() * 68, feet - 64 + bob, 19, dark);
        limbClawBig(g, x - facing() * 68, feet - 64 + bob, 15, dark);
        limb(g, x + facing() * 46, feet - 114 + bob, x + facing() * 66, feet - 62 + bob, 18, dark);
        limbClawBig(g, x + facing() * 66, feet - 62 + bob, 14, dark);

        g.setColor(hide);
        g.fillRoundRect((int) (x - 42), (int) (feet - 146 + bob), 84, 104, 30, 30);
        g.setColor(dark);
        for (int i = 0; i < 3; i++)
            g.drawArc((int) (x - 31), (int) (feet - 136 + bob + i * 25), 62, 20, 0, 180);

        double armAng = switch (state) {
            case 2 -> Math.toRadians(-125 + FMath.sin(stateT * 20) * 6);
            case 4 -> Math.toRadians(-135);
            case 3 -> Math.toRadians(30);
            default -> Math.toRadians(58 + Math.sin(ph) * 7);
        };
        if (guarding) {
            drawMainArm(g, x + facing() * 30, feet - 132 + bob, facingRight ? -2.45 : Math.PI + 2.45, hide);
            drawMainArm(g, x - facing() * 27, feet - 128 + bob, facingRight ? -2.12 : Math.PI + 2.12, dark);
        } else {
            drawMainArm(g, x + facing() * 30, feet - 132 + bob, facingRight ? -armAng : Math.PI + armAng, hide);
            drawMainArm(g, x - facing() * 27, feet - 128 + bob, facingRight ? -armAng - 0.5 : Math.PI + armAng + 0.5, dark);
        }

        float hy = feet - 156 + bob;
        g.setColor(hide);
        g.fillOval((int) (x - 24), (int) (hy - 21), 48, 40);
        g.setColor(new Color(28, 14, 18));
        g.fillRoundRect((int) (x - 18), (int) (hy - 4), 36, 15, 8, 8);
        g.setColor(Color.WHITE);
        for (int i = 0; i < 6; i++)
            g.fillPolygon(new int[]{(int) (x - 15 + i * 6), (int) (x - 11 + i * 6), (int) (x - 13 + i * 6)},
                    new int[]{(int) (hy - 3), (int) (hy - 3), (int) (hy + 5)}, 3);
        g.setColor(phase2 ? new Color(255, 80, 70) : new Color(230, 190, 70));
        g.fillOval((int) (x + facing() * 8 - 5), (int) (hy - 12), 10, 6);
        g.fillOval((int) (x - facing() * 8 - 4), (int) (hy - 12), 9, 6);
        g.setColor(dark);
        g.fillPolygon(new int[]{(int) (x - 20), (int) (x - 28), (int) (x - 15)}, new int[]{(int) (hy - 19), (int) (hy - 29), (int) (hy - 20)}, 3);
        g.fillPolygon(new int[]{(int) (x + 18), (int) (x + 26), (int) (x + 13)}, new int[]{(int) (hy - 20), (int) (hy - 30), (int) (hy - 19)}, 3);
        g.setComposite(old);
        if (state == 4) {
            String s = pendingArt != null ? pendingArt.form : "Grasp of the Dead";
            Font oldF = g.getFont();
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
            float wd = g.getFontMetrics().stringWidth(s);
            float tx = x - wd / 2f, ty = top() - 30;
            g.setColor(new Color(8, 12, 8, 190));
            g.drawString(s, tx + 1.5f, ty + 1.5f);
            g.setColor(new Color(150, 255, 130));
            g.drawString(s, tx, ty);
            g.setFont(oldF);
        }
    }

    private void drawMainArm(Graphics2D g, float sx, float sy, double ang, Color c) {
        float ex = sx + FMath.cos((float) ang) * 50, ey = sy + FMath.sin((float) ang) * 50;
        float hx = ex + FMath.cos((float) ang) * 32, hy = ey + FMath.sin((float) ang) * 32;
        limb(g, sx, sy, ex, ey, 22, c);
        limb(g, ex, ey, hx, hy, 17, c.brighter());
        g.setColor(c.darker().darker());
        g.fillOval((int) hx - 14, (int) hy - 12, 28, 25);
        g.setColor(new Color(235, 225, 210));
        g.fillOval((int) hx - 7, (int) hy - 6, 15, 11);
        g.setColor(state >= 2 ? new Color(255, 70, 60) : new Color(160, 60, 50));
        g.fillOval((int) hx - 3, (int) hy - 3, 6, 6);
    }

    private void limbClawBig(Graphics2D g, float cx, float cy, float len, Color c) {
        g.setStroke(new BasicStroke(7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(c);
        for (int i = -2; i <= 2; i++) {
            double a = Math.PI / 2 + i * 0.32;
            g.drawLine((int) cx, (int) cy, (int) (cx + Math.cos(a) * len), (int) (cy + Math.sin(a) * len));
        }
    }

    @Override
    public void renderGlow(Graphics2D g) {
        if (dead) return;
        renderRechargeGlow(g);
        float feet = bottom(), bob = onGround ? Math.abs(FMath.sin(animPhase * 5f)) * -3.5f : 0;
        Color eye = phase2 ? new Color(255, 70, 55) : new Color(255, 200, 70);
        Glow.blob(g, x + facing() * 8, feet - 165 + bob, 5, eye);
        Glow.blob(g, x - facing() * 8, feet - 165 + bob, 4.5f, eye);
        if (phase2) Glow.blob(g, x, top() + 12, 14, new Color(255, 90, 40, 90));
    }
}
