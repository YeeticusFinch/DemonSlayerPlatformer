package game;

import java.awt.*;

public class Susumaru extends Enemy {
    private final java.util.ArrayList<Ability> forms = Ability.susumaruArt();
    private int formIdx;
    private float castCd = 1.2f;
    private boolean sixArms;

    public Susumaru() {
        super(34, 82);
        team = 1;
        isDemon = true;
        isBoss = true;
        name = "Susumaru";
        maxHp = hp = 217.5f;
        runSpeed = 205;
        aggroR = 760;
        atkRange = 74;
        dmg = 12;
        elite = true;
        eliteColor = new Color(235, 190, 74);
    }

    @Override
    protected void think(World w, float dt, float dx, float dist) {
        if (!sixArms && hp < maxHp * 0.5f) {
            sixArms = true;
            w.banner("Susumaru laughs", "Six arms whirl with temari.");
        }
        castCd -= dt;
        if ((state == 0 || state == 1) && castCd <= 0 && dist < 720 && Math.abs(w.player.y - y) < 320) {
            face(dx != 0 ? dx : vx);
            beginWindup();
            windup = sixArms ? 0.24f : 0.42f;
            castCd = FMath.rand(sixArms ? 0.85f : 1.6f, sixArms ? 1.55f : 2.6f);
            return;
        }
        if (state == 2 && stateT >= windup) {
            state = 3;
            stateT = 0;
            recover = sixArms ? 0.28f : 0.45f;
            w.castAbility(this, forms.get(formIdx++ % forms.size()));
            return;
        }
        super.think(w, dt, dx, dist);
    }

    @Override
    public float outgoingDamageMult() { return 0.85f; }

    @Override
    public boolean hurt(World world, Fighter src, float dmgAmt, float dir, float kbX, float kbY) {
        if (sixArms) dmgAmt *= 0.75f;
        return super.hurt(world, src, dmgAmt, dir, kbX, kbY);
    }

    @Override
    protected void strike(World w) { basicStrike(w, 72, 220, 120, -12, new Color(235, 190, 74)); }

    @Override
    protected Color abilityColor() { return new Color(235, 190, 74); }

    @Override
    public void update(World w, float dt) {
        super.update(w, dt);
        demonRegen(w, dt, 2.7f);
    }

    @Override
    protected void renderBody(Graphics2D g) {
        g.setColor(new Color(0, 0, 0, 58));
        g.fillOval((int) x - 28, (int) bottom() - 5, 56, 9);
        float feet = bottom(), sh = feet - 54, hip = feet - 25;
        Color skin = new Color(242, 236, 224);
        Color orange = new Color(218, 98, 32);
        Color black = new Color(24, 18, 20);
        limb(g, x - 4, hip, x - 8, feet - 4, 7, sixArms ? orange.darker() : black.darker());
        limb(g, x + 4, hip, x + 8, feet - 4, 7, sixArms ? orange : black);
        if (sixArms) {
            g.setColor(skin);
            g.fillRoundRect((int) x - 13, (int) sh, 26, 25, 12, 12);
            g.setColor(black);
            g.fillRoundRect((int) x - 11, (int) sh + 8, 22, 9, 5, 5);
            g.setColor(orange);
            g.fillRoundRect((int) x - 13, (int) sh + 24, 26, (int) (hip - sh - 10), 10, 10);
            for (int i = 0; i < 6; i++) {
                int side = i < 3 ? facing() : -facing();
                int row = i % 3;
                float sy = sh + 8 + row * 11;
                float ey = sy + (row - 1) * 8;
                float reach = side == facing() ? 35 + row * 4 : 25 + row * 3;
                limb(g, x + side * 6, sy, x + side * reach, ey, 5, side == facing() ? skin : skin.darker());
                g.setColor(new Color(235, 190, 74));
                g.fillOval((int) (x + side * reach) - 6, (int) ey - 6, 12, 12);
            }
        } else {
            g.setColor(orange);
            g.fillRoundRect((int) x - 12, (int) sh + 4, 24, (int) (hip - sh + 8), 10, 10);
            g.setColor(black);
            g.fillRoundRect((int) x - 15, (int) sh, 30, 22, 8, 8);
            g.fillPolygon(new int[]{(int) x - 15, (int) x - 2, (int) x - 12}, new int[]{(int) sh + 3, (int) sh + 18, (int) hip + 2}, 3);
            g.fillPolygon(new int[]{(int) x + 15, (int) x + 2, (int) x + 12}, new int[]{(int) sh + 3, (int) sh + 18, (int) hip + 2}, 3);
            limb(g, x - facing() * 10, sh + 10, x - facing() * 25, sh + 33, 6, black.darker());
            limb(g, x + facing() * 10, sh + 10, x + facing() * 32, sh + 22, 6, black);
            g.setColor(new Color(235, 190, 74));
            g.fillOval((int) (x + facing() * 34) - 6, (int) sh + 16, 12, 12);
        }
        g.setColor(skin);
        g.fillOval((int) x - 11, (int) sh - 22, 22, 22);
        drawBobHair(g, sh);
        g.setColor(new Color(255, 230, 68));
        g.fillOval((int) x - 5, (int) sh - 13, 4, 3);
        g.fillOval((int) x + 3, (int) sh - 13, 4, 3);
    }

    @Override
    public void renderGlow(Graphics2D g) {
        if (dead) return;
        renderRechargeGlow(g);
        Glow.blob(g, x - 4, top() + h * 0.24f, 6, new Color(255, 230, 68, 170));
        Glow.blob(g, x + 4, top() + h * 0.24f, 6, new Color(255, 230, 68, 170));
    }

    private void drawBobHair(Graphics2D g, float sh) {
        float hy = sh - 16;
        g.setColor(new Color(24, 18, 22));
        g.fillArc((int) x - 14, (int) hy - 13, 28, 24, 0, 180);
        g.fillRoundRect((int) x - 14, (int) hy - 2, 8, 20, 6, 6);
        g.fillRoundRect((int) x + 6, (int) hy - 2, 8, 20, 6, 6);
        g.setColor(new Color(214, 72, 26));
        g.fillRoundRect((int) x - 14, (int) hy + 11, 8, 7, 5, 5);
        g.fillRoundRect((int) x + 6, (int) hy + 11, 8, 7, 5, 5);
        g.fillArc((int) x - 13, (int) hy - 11, 26, 22, 205, 130);
    }
}
