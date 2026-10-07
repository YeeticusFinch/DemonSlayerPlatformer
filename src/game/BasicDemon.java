package game;

import java.awt.*;

public class BasicDemon extends Enemy {
    protected final Color skin, skinDark;
    protected final int variant;

    public BasicDemon() {
        this(34, 78, 34, FMath.rand(140, 175), 9);
    }

    public BasicDemon(float w, float h, float hp, float speed, float dmg) {
        super(w, h);
        isDemon = true;
        team = 1;
        name = "Demon";
        maxHp = this.hp = hp;
        runSpeed = speed;
        aggroR = 520;
        atkRange = 60;
        windup = 0.42f;
        recover = 0.55f;
        atkCdBase = 1.5f;
        this.dmg = dmg;
        variant = FMath.randInt(0, 2);
        skin = switch (variant) {
            case 0 -> new Color(122, 138, 108);
            case 1 -> new Color(128, 106, 132);
            default -> new Color(104, 118, 134);
        };
        skinDark = skin.darker().darker();
    }

    @Override
    protected float contactDamage() { return 5; }

    @Override
    protected float chaseSpeed() { return runSpeed * 1.35f; }

    @Override
    protected void strike(World w) {
        basicStrike(w, 64, 210, 100, -18, new Color(255, 70, 95));
    }

    @Override
    public void update(World w, float dt) {
        super.update(w, dt);
        demonRegen(w, dt, 2.6f);
    }

    @Override
    protected void renderBody(Graphics2D g) {
        shadow(g);
        Composite old = g.getComposite();
        if (flashT > 0) g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.85f));
        float cx = x, feet = bottom();
        float ph = animPhase * 6.28f;
        float bob = onGround ? Math.abs(FMath.sin(ph)) * -2.4f : 0;
        float crouch = recharging ? 1f : 0f;
        float hipY = feet - 24 + bob + crouch * 8f, shY = feet - 50 + bob + crouch * 5f;
        float legSwing = FMath.sin(ph) * 0.7f * FMath.clamp(Math.abs(vx) / (runSpeed * 1.35f), 0, 1);

        leg(g, cx - 3, hipY, -legSwing, 24, 7, skinDark, limbBackLeg);
        leg(g, cx + 3, hipY, legSwing, 24, 7, skinDark, limbFrontLeg);

        if (swingSide < 0 && limbBackArm && !guarding) demonArm(g, cx - facing() * 8, shY + 4, true, skinDark);

        g.setColor(skin);
        g.fillRoundRect((int) (cx - 10), (int) shY, 20, (int) (hipY - shY + 8), 11, 11);
        spineSpikes(g, cx, shY);

        head(g, cx + facing() * 2.5f, shY - 11);

        if (guarding) blockHands(g, shY, skin, 7);
        else demonArm(g, cx + facing() * 8, shY + 4, false, skin);
        g.setComposite(old);
        drawLimbFlash(g, limbFlashBA, cx - facing() * 8, shY + 8, 7);
        drawLimbFlash(g, limbFlashFA, cx + facing() * 8, shY + 8, 7);
        drawLimbFlash(g, limbFlashBL, cx - 3, hipY + 12, 6);
        drawLimbFlash(g, limbFlashFL, cx + 3, hipY + 12, 6);
    }

    private void spineSpikes(Graphics2D g, float cx, float shY) {
        g.setColor(skinDark);
        for (int i = 0; i < 3; i++)
            g.fillPolygon(new int[]{(int) (cx - facing() * 9), (int) (cx - facing() * 13), (int) (cx - facing() * 7)},
                    new int[]{(int) (shY + 4 + i * 8), (int) (shY + 9 + i * 8), (int) (shY + 8 + i * 8)}, 3);
    }

    protected void head(Graphics2D g, float hx, float hy) {
        g.setColor(skin);
        g.fillOval((int) hx - 10, (int) hy - 10, 21, 21);
        g.setColor(skinDark);
        g.fillPolygon(new int[]{(int) hx - 8, (int) hx - 12, (int) hx - 4}, new int[]{(int) hy - 8, (int) hy - 16, (int) hy - 9}, 3);
        g.fillPolygon(new int[]{(int) hx + 7, (int) hx + 11, (int) hx + 3}, new int[]{(int) hy - 9, (int) hy - 17, (int) hy - 10}, 3);
        g.setColor(new Color(255, 210, 80));
        g.fillOval((int) hx + facing() * 3 - 3, (int) hy - 3, 6, 4);
        g.fillOval((int) hx - facing() * 4 - 2, (int) hy - 3, 5, 4);
        if (stunT > 0) g.setColor(new Color(90, 80, 40));
        else g.setColor(new Color(40, 10, 15));
        g.fillRect((int) hx - 4, (int) hy + 5, 9, 3);
        g.setColor(Color.WHITE);
        for (int i = 0; i < 3; i++) g.fillRect((int) hx - 3 + i * 3, (int) hy + 4, 2, 2);
    }

    private void demonArm(Graphics2D g, float sx, float sy, boolean back, Color c) {
        boolean active = attacking() && ((back && swingSide < 0) || (!back && swingSide > 0));
        double ang;
        if (active) {
            double s = FMath.easeOut(1 - atkT / atkDur);
            ang = Math.toRadians(facingRight ? 210 - 270 * s : -30 + 270 * s - 180);
        } else {
            float sw = FMath.sin(animPhase * 6.28f + (back ? (float) Math.PI : 0)) * 0.45f;
            ang = Math.toRadians(facingRight ? 50 + sw * 20 : 130 - sw * 20);
        }
        float ex = sx + FMath.cos((float) ang) * 25, ey = sy + FMath.sin((float) ang) * 25;
        limb(g, sx, sy, ex, ey, 6, c);
        g.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(c.brighter());
        for (int i = -1; i <= 1; i++)
            g.drawLine((int) ex, (int) ey, (int) (ex + Math.cos(ang + i * 0.45) * 12), (int) (ey + Math.sin(ang + i * 0.45) * 12));
    }

    @Override
    public void renderGlow(Graphics2D g) {
        if (dead) return;
        renderRechargeGlow(g);
        float hx = x + facing() * 2.5f;
        Glow.blob(g, hx + facing() * 3, top() + h * 0.22f, 7, new Color(255, 195, 60, 190));
        Glow.blob(g, hx - facing() * 4, top() + h * 0.23f, 5, new Color(255, 195, 60, 150));
    }

    private void shadow(Graphics2D g) {
        g.setColor(new Color(0, 0, 0, 60));
        g.fillOval((int) (x - w * 0.8f), (int) (bottom() - 5), (int) (w * 1.6f), 8);
    }
}
