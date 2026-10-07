package game;

import java.awt.*;

public class HumanEnemy extends Enemy {
    private final boolean hunter;
    private float throwCd;

    public HumanEnemy(boolean hunter) {
        super(28, 74);
        this.hunter = hunter;
        team = 1;
        name = hunter ? "Hunter" : "Bandit";
        maxHp = hp = 18;
        runSpeed = 185;
        atkRange = hunter ? 60 : 66;
        windup = 0.36f;
        recover = 0.42f;
        atkCdBase = hunter ? 1.9f : 1.15f;
        dmg = hunter ? 6 : 7;
        aggroR = 470;
    }

    @Override
    protected void think(World w, float dt, float dx, float dist) {
        throwCd -= dt;
        Player pl = w.player;
        boolean flee = hp < 7 && !hunter;
        if (flee) {
            state = 1;
            vx = FMath.approach(vx, -Math.signum(dx == 0 ? 1 : dx) * runSpeed, 2000 * dt);
            if (touchingWall != 0 && onGround) vy = -640;
            return;
        }
        if (hunter && dist < 430 && dist > 120 && throwCd <= 0 && Math.abs(pl.y - y) < 90) {
            throwCd = 2.1f;
            face(dx);
            Effect k = new Effect(Effect.BLOOD_BOLT, this, false).at(x + facing() * 20, y - 30)
                    .vel(facing() * 640, -30).radius(14).life(0.8f).damage(7, 160, 80)
                    .colors(new Color(150, 150, 160), new Color(220, 220, 230));
            k.stopOnSolid = true;
            w.effects.add(k);
            return;
        }
        super.think(w, dt, dx, dist);
    }

    @Override
    protected void strike(World w) {
        if (hunter) basicStrike(w, 64, 190, 90, -10, new Color(210, 210, 225));
        else basicStrike(w, 72, 200, 95, -14, new Color(255, 240, 250));
    }

    @Override
    protected void renderBody(Graphics2D g) {
        g.setColor(new Color(0, 0, 0, 55));
        g.fillOval((int) (x - w * 0.75f), (int) (bottom() - 5), (int) (w * 1.5f), 8);
        Composite old = g.getComposite();
        if (flashT > 0) g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.85f));
        float ph = animPhase * 6.28f;
        float bob = onGround ? Math.abs(FMath.sin(ph)) * -2.2f : 0;
        float crouch = recharging ? 1f : 0f;
        float hipY = bottom() - 24 + bob + crouch * 8f, shY = bottom() - 49 + bob + crouch * 5f;
        float legSwing = FMath.sin(ph) * 0.75f * FMath.clamp(Math.abs(vx) / runSpeed, 0, 1);
        Color shirt = hunter ? new Color(86, 96, 78) : new Color(122, 88, 58);
        Color pantsC = new Color(70, 62, 54);

        limb(g, x - 3, hipY, x - 3 + FMath.sin(-legSwing) * 11, hipY + 23, 7, pantsC.darker());
        limb(g, x + 3, hipY, x + 3 + FMath.sin(legSwing) * 11, hipY + 23, 7, pantsC);
        if (!guarding) limb(g, x - facing() * 9, shY + 4, x - facing() * 22, shY + 28, 6, shirt.darker());
        g.setColor(shirt);
        g.fillRoundRect((int) (x - 11), (int) shY, 22, (int) (hipY - shY + 8), 10, 10);
        g.setColor(new Color(238, 205, 165));
        g.fillOval((int) x - 9, (int) shY - 19, 19, 19);
        g.setColor(hunter ? new Color(50, 44, 38) : new Color(38, 32, 30));
        g.fillArc((int) x - 10, (int) shY - 21, 21, 14, 0, 180);
        if (!hunter) {
            g.setColor(new Color(170, 40, 45));
            g.fillRect((int) x - 9, (int) shY - 13, 18, 3);
        }
        g.setColor(new Color(25, 25, 28));
        g.fillRect((int) x + facing() * 2 - 1, (int) shY - 12, 2, 3);

        if (guarding) {
            blockHands(g, shY, new Color(238, 205, 165), 6);
        } else {
            double prog = attacking() ? 1 - atkT / atkDur : -1;
            double ang;
            if (prog >= 0) {
                double s = FMath.easeOut((float) prog);
                ang = Math.toRadians(facingRight ? 220 - 280 * s : -40 + 280 * s - 180);
            } else ang = Math.toRadians(facingRight ? 35 : 145);
            float sx = x + facing() * 8, sy = shY + 5;
            float ex = sx + FMath.cos((float) ang) * 31, ey = sy + FMath.sin((float) ang) * 31;
            limb(g, sx, sy, ex, ey, 5, new Color(96, 68, 40));
            g.setStroke(new BasicStroke(2.4f));
            g.setColor(new Color(180, 185, 195));
            if (hunter) {
                for (int i = -1; i <= 1; i++)
                    g.drawLine((int) ex, (int) ey,
                            (int) (ex + FMath.cos((float) ang + i * 0.3f) * 13), (int) (ey + FMath.sin((float) ang + i * 0.3f) * 13));
            } else {
                g.drawLine((int) ex, (int) ey, (int) (ex + FMath.cos((float) ang) * 16), (int) (ey + FMath.sin((float) ang) * 16));
            }
        }
        g.setComposite(old);
    }
}
