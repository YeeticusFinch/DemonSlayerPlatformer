package game;

import java.awt.*;

public class PuppetSlayer extends Enemy {
    private final Color uniform = new Color(35, 38, 58);
    private final Color haori = new Color(78, 84, 94);
    private final float anchorDrift;

    public PuppetSlayer() {
        super(28, 82);
        team = 1;
        isDemon = false;
        name = "Puppet Demon Slayer";
        maxHp = hp = 54;
        maxSp = sp = 0;
        runSpeed = 210;
        aggroR = 760;
        atkRange = 82;
        windup = 0.34f;
        recover = 0.48f;
        atkCdBase = 1.08f;
        dmg = 10;
        anchorDrift = FMath.rand(0, 6.28f);
    }

    @Override
    protected void think(World w, float dt, float dx, float dist) {
        face(dx != 0 ? dx : vx);
        Player pl = w.player;
        switch (state) {
            case 0 -> {
                vx = FMath.approach(vx, 0, 1000 * dt);
                vy = FMath.approach(vy, FMath.sin(Game.time * 1.8f + anchorDrift) * 34f, 900 * dt);
                if (dist < atkRange && atkCd <= 0 && Math.abs(pl.y - y) < 120) beginWindup();
                else if (dist < aggroR) state = 1;
            }
            case 1 -> {
                float dir = Math.signum(dx == 0 ? 1 : dx);
                vx = FMath.approach(vx, dir * runSpeed, 1350 * dt);
                float targetY = pl.y - 18 + FMath.sin(Game.time * 2.3f + anchorDrift) * 42f;
                vy = FMath.approach(vy, FMath.clamp((targetY - y) * 2.5f, -210, 210), 1100 * dt);
                if (dist <= atkRange && atkCd <= 0 && Math.abs(pl.y - y) < 135) beginWindup();
                else if (dist > aggroR * 1.25f) state = 0;
            }
            case 2 -> {
                vx = FMath.approach(vx, 0, 1800 * dt);
                vy = FMath.approach(vy, 0, 1600 * dt);
                if (stateT >= windup) {
                    state = 3;
                    stateT = 0;
                    swingSide *= -1;
                    strike(w);
                }
            }
            case 3 -> {
                vx += facing() * 18f;
                if (stateT >= recover) {
                    state = 0;
                    atkCd = atkCdBase;
                }
            }
        }
        stateT += dt;
    }

    @Override
    protected void strike(World w) {
        basicStrike(w, 84, 250, 120, swingSide > 0 ? -22 : 18, new Color(210, 220, 235));
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
        limbFlashFA = Math.max(0, limbFlashFA - dt);
        limbFlashBA = Math.max(0, limbFlashBA - dt);
        limbFlashFL = Math.max(0, limbFlashFL - dt);
        limbFlashBL = Math.max(0, limbFlashBL - dt);
        x = FMath.clamp(x + vx * dt, w / 2f, world.level.w - w / 2f);
        y = FMath.clamp(y + vy * dt, 130, world.level.h - 180);
        onGround = false;
        touchingWall = 0;
        animPhase += dt * (1.5f + Math.abs(vx) * 0.003f);
        squash = Math.max(0, squash - dt * 5f);
    }

    @Override
    protected void renderBody(Graphics2D g) {
        drawStrings(g, 1f);
        Composite old = g.getComposite();
        if (flashT > 0) g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.86f));
        float ph = animPhase * 6.28f;
        float sway = FMath.sin(Game.time * 2.1f + anchorDrift) * 5f;
        float hipY = bottom() - 25 + sway * 0.35f, shY = bottom() - 51 + sway;
        float legSwing = FMath.sin(ph) * 0.55f;

        leg(g, x - 3, hipY, -legSwing, 24, 6, uniform.darker(), true);
        leg(g, x + 3, hipY, legSwing, 24, 6, uniform, true);
        g.setColor(haori);
        g.fillRoundRect((int) (x - 9), (int) shY, 18, (int) (hipY - shY + 7), 9, 9);
        g.setColor(uniform.darker());
        limb(g, x - facing() * 7, shY + 4, x - facing() * 21, shY + 26, 5, uniform.darker());
        g.setColor(new Color(218, 186, 152));
        g.fillOval((int) x - 8, (int) shY - 17, 17, 17);
        g.setColor(new Color(25, 23, 25));
        g.fillArc((int) x - 9, (int) shY - 19, 19, 13, 0, 180);

        float sx = x + facing() * 6, sy = shY + 5;
        double prog = state == 3 ? Math.min(1, stateT / 0.2f) : -1;
        double armAng = prog >= 0
                ? Math.toRadians(facingRight ? 190 - 220 * prog : 350 - 220 * prog)
                : Math.toRadians(facingRight ? 55 : 125) + FMath.sin(ph) * 0.2f;
        float aa = (float) (facingRight ? armAng : Math.PI - armAng);
        float hx = sx + FMath.cos(aa) * 22, hy = sy + FMath.sin(aa) * 22;
        limb(g, sx, sy, hx, hy, 5, uniform.darker());
        float bladeAng = (float) (facingRight ? armAng - Math.PI / 2 : Math.PI - armAng + Math.PI / 2);
        float bx = hx + FMath.cos(bladeAng) * 50, by = hy + FMath.sin(bladeAng) * 50;
        g.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(new Color(210, 216, 225));
        g.drawLine((int) hx, (int) hy, (int) bx, (int) by);
        g.setComposite(old);
        drawLimbFlash(g, limbFlashFA, x + facing() * 7, shY + 8, 6);
        drawLimbFlash(g, limbFlashBA, x - facing() * 7, shY + 8, 6);
    }

    protected void drawStrings(Graphics2D g, float scale) {
        Composite old = g.getComposite();
        Stroke oldS = g.getStroke();
        g.setStroke(new BasicStroke(Math.max(0.6f, scale), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        float top = y - 920 * scale;
        float[] xs = {x - w * 0.35f, x, x + w * 0.35f, x + facing() * 16 * scale};
        for (int i = 0; i < xs.length; i++) {
            float sway = FMath.sin(Game.time * 1.1f + anchorDrift + i) * 18f * scale;
            g.setPaint(new GradientPaint(xs[i] + sway, top, new Color(255, 255, 255, 0), xs[i], y - h * 0.35f,
                    new Color(255, 255, 255, i == 1 ? 150 : 105)));
            g.drawLine((int) (xs[i] + sway), (int) top, (int) xs[i], (int) (y - h * (0.35f + i % 2 * 0.18f)));
        }
        g.setStroke(oldS);
        g.setComposite(old);
    }
}
