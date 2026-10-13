package game;

import java.awt.*;

public class HeadlessPuppetDemon extends Enemy {
    private final Color hide = new Color(86, 88, 76);
    private final Color dark = new Color(44, 42, 46);
    private float slamCd = 1.4f;
    private final float anchorDrift = FMath.rand(0, 6.28f);

    public HeadlessPuppetDemon() {
        super(96, 168);
        team = 1;
        isDemon = true;
        isBoss = true;
        name = "Headless Demon Puppet";
        maxHp = hp = 460;
        maxSp = sp = 0;
        runSpeed = 116;
        aggroR = 1400;
        atkRange = 178;
        windup = 0.48f;
        recover = 0.58f;
        atkCdBase = 1.22f;
        dmg = 32;
    }

    @Override
    protected void think(World w, float dt, float dx, float dist) {
        slamCd -= dt;
        face(dx != 0 ? dx : vx);
        switch (state) {
            case 0, 1 -> {
                state = 1;
                vx = FMath.approach(vx, Math.signum(dx == 0 ? 1 : dx) * runSpeed, 1200 * dt);
                float targetY = w.player.y - 26 + FMath.sin(Game.time * 1.6f + anchorDrift) * 34f;
                vy = FMath.approach(vy, FMath.clamp((targetY - y) * 1.9f, -170, 170), 950 * dt);
                if (dist < atkRange && atkCd <= 0) beginWindup();
                else if (slamCd <= 0 && dist < 390) {
                    state = 4;
                    stateT = 0;
                    windup = 0.62f;
                }
            }
            case 2 -> {
                vx = FMath.approach(vx, 0, 1500 * dt);
                if (stateT >= windup) {
                    state = 3;
                    stateT = 0;
                    swingSide *= -1;
                    w.meleeStrike(this, 176, dmg, 520, 220, Math.toRadians(facingRight ? -10 : 190), Math.toRadians(165), new Color(170, 170, 165));
                    w.cam.shake(4, 0.16f);
                }
            }
            case 3 -> {
                if (stateT >= recover) {
                    state = 1;
                    atkCd = atkCdBase;
                }
            }
            case 4 -> {
                vx = FMath.approach(vx, 0, 1600 * dt);
                vy = FMath.approach(vy, -45, 700 * dt);
                if (stateT >= windup) {
                    state = 3;
                    stateT = 0;
                    recover = 0.72f;
                    slamCd = 3.4f;
                    w.meleeStrike(this, 220, 38, 360, 520, Math.toRadians(facingRight ? -90 : 270), Math.toRadians(210), new Color(150, 150, 145));
                    w.cam.shake(8, 0.28f);
                    w.parts.ring(x, bottom() - 25, new Color(230, 230, 230, 150));
                }
            }
        }
        stateT += dt;
    }

    @Override
    protected void strike(World w) {
    }

    @Override
    protected void physics(World world, float dt) {
        staggerT -= dt;
        invulnT -= dt;
        flashT -= dt;
        contactCd -= dt;
        guardAnim = Math.max(0, guardAnim - dt);
        timeSinceAction += dt;
        hurtTimer += dt;
        wisteriaT = Math.max(0, wisteriaT - dt);
        stunT = Math.max(0, stunT - dt);
        x = FMath.clamp(x + vx * dt, w / 2f, world.level.w - w / 2f);
        y = FMath.clamp(y + vy * dt, 120, world.level.h - 230);
        onGround = false;
        touchingWall = 0;
        animPhase += dt * (1.2f + Math.abs(vx) * 0.0025f);
        squash = Math.max(0, squash - dt * 5f);
    }

    @Override
    protected void renderBody(Graphics2D g) {
        drawStrings(g);
        g.setColor(new Color(0, 0, 0, 65));
        g.fillOval((int) x - 72, (int) bottom() - 7, 144, 13);
        Composite old = g.getComposite();
        if (flashT > 0) g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.82f));
        float feet = bottom();
        float ph = animPhase * 5.2f;
        float sway = FMath.sin(Game.time * 1.7f + anchorDrift) * 7f;
        float hipY = feet - 48 + sway * 0.4f, shY = feet - 126 + sway;
        float legSwing = FMath.sin(ph) * 0.42f;

        limb(g, x - 24, hipY, x - 34 + legSwing * 16, feet - 7, 20, dark);
        limb(g, x + 24, hipY, x + 34 - legSwing * 16, feet - 7, 20, dark);
        g.setColor(hide);
        g.fillRoundRect((int) (x - 43), (int) shY, 86, (int) (hipY - shY + 20), 28, 28);
        g.setColor(dark);
        g.fillOval((int) x - 20, (int) shY - 10, 40, 20);
        g.setColor(new Color(24, 20, 22));
        g.fillOval((int) x - 15, (int) shY - 19, 30, 22);

        double arm = state == 2 || state == 4 ? -2.55 + FMath.sin(stateT * 18) * 0.12 : 0.8 + FMath.sin(ph) * 0.12;
        drawArm(g, x + facing() * 34, shY + 18, facingRight ? -arm : Math.PI + arm, hide.brighter());
        drawArm(g, x - facing() * 32, shY + 21, facingRight ? -arm - 0.35 : Math.PI + arm + 0.35, dark);
        g.setComposite(old);
    }

    private void drawArm(Graphics2D g, float sx, float sy, double ang, Color c) {
        float ex = sx + FMath.cos((float) ang) * 53, ey = sy + FMath.sin((float) ang) * 53;
        float hx = ex + FMath.cos((float) ang) * 34, hy = ey + FMath.sin((float) ang) * 34;
        limb(g, sx, sy, ex, ey, 18, c);
        limb(g, ex, ey, hx, hy, 14, c.darker());
        g.setColor(c.darker().darker());
        g.fillOval((int) hx - 12, (int) hy - 11, 24, 22);
    }

    private void drawStrings(Graphics2D g) {
        Stroke oldS = g.getStroke();
        g.setStroke(new BasicStroke(1.1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        float top = y - 1100;
        for (int i = -2; i <= 2; i++) {
            float baseX = x + i * 22f;
            float sway = FMath.sin(Game.time * 0.9f + anchorDrift + i) * 34f;
            float attachY = y + h * (0.06f + Math.abs(i) * 0.04f);
            g.setPaint(new GradientPaint(baseX + sway, top, new Color(255, 255, 255, 0), baseX, attachY,
                    new Color(255, 255, 255, i == 0 ? 160 : 105)));
            g.drawLine((int) (baseX + sway), (int) top, (int) baseX, (int) attachY);
        }
        g.setStroke(oldS);
    }

    @Override
    protected Color abilityColor() { return new Color(210, 210, 205); }
}
