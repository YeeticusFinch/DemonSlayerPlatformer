package game;

import java.awt.*;

public class SlayerEnemy extends Enemy {
    public final Profile.Style style;
    private java.util.ArrayList<Ability> forms;
    private float castCd = 1.6f;
    private int comboSub;
    private int formIdx;
    private boolean castingForm;

    public SlayerEnemy(Profile.Style style) {
        super(28, 82);
        this.style = style;
        team = 1;
        isDemon = false;
        name = style == Profile.Style.FLAME ? "Flame Bearer" : style == Profile.Style.WIND ? "Wind Bearer" : "Water Bearer";
        maxHp = hp = 48;
        runSpeed = 235;
        aggroR = 700;
        atkRange = 86;
        windup = 0.3f;
        recover = 0.42f;
        atkCdBase = 1.05f;
        dmg = 9;
        elite = true;
        eliteColor = styleColor();
        forms = switch (style) {
            case FLAME -> Ability.flame();
            case WIND -> Ability.wind();
            default -> Ability.water();
        };
    }

    @Override
    protected void think(World w, float dt, float dx, float dist) {
        castCd -= dt;
        Player pl = w.player;
        if (state == 0 || state == 1) {
            face(dx != 0 ? dx : vx);
            if (dist > 260) {
                eliteMove(w, dt, dx, dist);
                if (eliteDashT <= 0)
                    vx = FMath.approach(vx, Math.signum(dx) * runSpeed, 2300 * dt);
                state = 1;
                if (touchingWall != 0 && onGround) vy = -700;
            } else if (dist < 110) {
                vx = FMath.approach(vx, -Math.signum(dx) * runSpeed * 0.7f, 2300 * dt);
            } else {
                vx = FMath.approach(vx, FMath.sin(aiTime * 2.2f) * runSpeed * 0.5f, 1800 * dt);
            }
            boolean wantCast = castCd <= 0 && !pl.dead && Math.abs(pl.y - y) < 150;
            if (wantCast) {
                castCd = style == Profile.Style.FLAME ? 2.6f : 2.3f;
                windup = style == Profile.Style.FLAME ? 0.48f : 0.34f;
                castingForm = true;
                beginWindup();
            } else if (dist < atkRange && atkCd <= 0) {
                windup = 0.28f;
                castingForm = false;
                beginWindup();
            }
            return;
        }
        if (state == 2 && stateT >= windup) {
            state = 3;
            stateT = 0;
            recover = 0.45f;
            execute(w, pl, Math.abs(pl.x - x));
            atkCd = atkCdBase;
        }
        if (state == 3 && stateT >= recover) state = 0;
        stateT += dt;
        if (state == 2) vx = FMath.approach(vx, 0, 2400 * dt);
    }

    private void execute(World w, Player pl, float dist) {
        noteAction();
        swingSide *= -1;
        if (castingForm && !forms.isEmpty()) {
            w.castAbility(this, forms.get(formIdx++ % forms.size()));
            castingForm = false;
        } else {
            comboSub = (comboSub + 1) % 2;
            basicStrike(w, 78, 240, 120, comboSub == 0 ? -20 : 16, styleColor());
        }
    }

    protected Color styleColor() {
        return style == Profile.Style.FLAME ? AbilityCast.FLAME : style == Profile.Style.WIND ? AbilityCast.WIND : AbilityCast.WATER;
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
        float ph = animPhase * 6.28f;
        float speedF = FMath.clamp(Math.abs(vx) / runSpeed, 0, 1);
        float bob = onGround ? Math.abs(FMath.sin(ph)) * -2.6f * speedF : 0;
        float crouch = recharging ? 1f : 0f;
        float hipY = bottom() - 25 + bob + crouch * 8f, shY = bottom() - 51 + bob + crouch * 5f;
        float legSwing = FMath.sin(ph) * 0.8f * speedF;
        Color uniform = new Color(38, 42, 66);
        Color haori = style == Profile.Style.FLAME ? new Color(140, 46, 34)
                : style == Profile.Style.WIND ? new Color(48, 126, 76) : new Color(40, 92, 128);

        leg(g, x - 3, hipY, -legSwing, 24, 6, uniform.darker(), true);
        leg(g, x + 3, hipY, legSwing, 24, 6, uniform, true);

        boolean swordDrawn = guarding || timeSinceAction < 5f || attacking() || state == 2 || state == 3;

        g.setColor(haori);
        g.fillRoundRect((int) (x - 9), (int) shY, 18, (int) (hipY - shY + 7), 9, 9);
        g.setColor(haori.brighter());
        g.setStroke(new BasicStroke(1.6f));
        g.drawRoundRect((int) (x - 9), (int) shY, 18, (int) (hipY - shY + 7), 9, 9);

        g.setColor(uniform.darker());
        if (!guarding) limb(g, x - facing() * 7, shY + 4, x - facing() * 21, shY + 26, 5, uniform.darker());
        g.setColor(new Color(238, 205, 168));
        g.fillOval((int) x - 8, (int) shY - 17, 17, 17);
        g.setColor(new Color(30, 28, 30));
        g.fillArc((int) x - 9, (int) shY - 19, 19, 13, 0, 180);
        g.fillRect((int) x + facing() * 2 - 1, (int) shY - 11, 2, 3);
        if (state == 2) {
            g.setColor(style == Profile.Style.FLAME ? new Color(255, 160, 60, 130)
                    : style == Profile.Style.WIND ? new Color(120, 255, 160, 130) : new Color(140, 200, 255, 130));
            g.fillOval((int) (x + facing() * 12 - 6), (int) (shY - 2), 12, 12);
        }

        float sx = x + facing() * 6, sy = shY + 5;
        double prog = attacking() || state == 3 ? Math.min(1, stateT / 0.2f) : -1;
        if (guarding) {
            swordBlock(g, sx, sy, uniform.darker(), styleColor(), 1.05f, 5);
        } else if (swordDrawn) {
            double armAng;
            if (prog >= 0) {
                armAng = switch (comboSub % 2) {
                    case 0 -> Math.toRadians(facingRight ? 190 - 220 * prog : 350 - 220 * prog);
                    default -> Math.toRadians(facingRight ? -20 - 200 * prog : 200 - 200 * prog);
                };
            } else {
                armAng = Math.toRadians(facingRight ? 55 : 125) + FMath.sin(ph) * 0.2f;
            }
            double twist = prog >= 0 ? FMath.easeOut((float) prog * 1.2f) : 0;
            double bladeLocal = armAng - (Math.PI / 2) * (1 - twist);
            double wa = facingRight ? bladeLocal : Math.PI - bladeLocal;
            float aa = (float) (facingRight ? armAng : Math.PI - armAng);
            float hx = sx + FMath.cos(aa) * 22, hy = sy + FMath.sin(aa) * 22;
            limb(g, sx, sy, hx, hy, 5, uniform.darker());
            float len = prog >= 0 ? 58 : 40;
            float bx = hx + FMath.cos((float) wa) * len, by = hy + FMath.sin((float) wa) * len;
            bladeShape(g, hx, hy, bx, by, 1.05f, styleColor(), !facingRight);
        } else {
            drawScabbardNpc(g, hipY, haori.darker());
            limb(g, sx, sy, sx + facing() * 18, sy + 24, 5, uniform.darker());
        }
        drawLimbFlash(g, limbFlashFA, x + facing() * 7, shY + 8, 6);
        drawLimbFlash(g, limbFlashBA, x - facing() * 7, shY + 8, 6);
        g.setComposite(old);
    }

    private void drawScabbardNpc(Graphics2D g, float hipY, Color c) {
        float bx = x + facing() * 2, by = hipY + 6;
        float tx = bx - facing() * 12, ty = by + 18;
        g.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(new Color(14, 14, 18));
        g.drawLine((int) bx, (int) by, (int) tx, (int) ty);
    }

    @Override
    public void renderGlow(Graphics2D g) {
        renderRechargeGlow(g);
    }

    @Override
    protected Color abilityColor() { return styleColor(); }
}
