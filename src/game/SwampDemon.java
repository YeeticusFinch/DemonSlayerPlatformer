package game;

import java.awt.*;

public class SwampDemon extends Enemy {
    protected java.util.ArrayList<Ability> forms;
    protected int formIdx;
    protected float castCd = 1.6f;
    private final boolean strong;

    public SwampDemon(boolean strong) {
        super(strong ? 42 : 36, strong ? 88 : 82);
        this.strong = strong;
        team = 1;
        isDemon = true;
        name = strong ? "Swamp Demon" : "Lurking Swamp Demon";
        maxHp = hp = strong ? 96 : 58;
        runSpeed = strong ? 205 : 176;
        aggroR = strong ? 760 : 620;
        atkRange = 64;
        dmg = strong ? 12 : 9;
        elite = true;
        eliteColor = new Color(30, 135, 122);
        forms = Ability.swampDemonArt(strong);
    }

    @Override
    protected float contactDamage() { return strong ? 6 : 4; }

    @Override
    protected float chaseSpeed() { return runSpeed * (strong ? 1.34f : 1.22f); }

    @Override
    protected void think(World w, float dt, float dx, float dist) {
        castCd -= dt;
        if ((state == 0 || state == 1) && castCd <= 0 && dist < (strong ? 640 : 520) && Math.abs(w.player.y - y) < 260) {
            face(dx != 0 ? dx : vx);
            beginWindup();
            windup = strong ? 0.36f : 0.44f;
            castCd = FMath.rand(strong ? 1.5f : 2.1f, strong ? 2.5f : 3.2f);
            return;
        }
        if (state == 2 && stateT >= windup) {
            state = 3;
            stateT = 0;
            recover = 0.42f;
            w.castAbility(this, forms.get(formIdx++ % forms.size()));
            atkCd = atkCdBase * armCdMult();
            return;
        }
        super.think(w, dt, dx, dist);
    }

    @Override
    protected void strike(World w) {
        basicStrike(w, 66, 220, 120, -18, new Color(38, 132, 122));
    }

    @Override
    public void update(World w, float dt) {
        super.update(w, dt);
        demonRegen(w, dt, strong ? 3.2f : 2.3f);
    }

    @Override
    protected Color abilityColor() { return new Color(30, 135, 122); }

    @Override
    protected void renderBody(Graphics2D g) {
        drawSwampBody(g, new Color(238, 240, 235), new Color(22, 104, 104), new Color(20, 118, 154), true, strong ? 1.1f : 1f);
    }

    protected void drawSwampBody(Graphics2D g, Color skin, Color uniform, Color hair, boolean horns, float scale) {
        g.setColor(new Color(0, 0, 0, 68));
        g.fillOval((int) (x - w * 0.8f), (int) (bottom() - 5), (int) (w * 1.6f), 9);
        Composite old = g.getComposite();
        if (flashT > 0) g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.84f));
        float feet = bottom();
        float ph = animPhase * 6.28f;
        float bob = onGround ? Math.abs(FMath.sin(ph)) * -2.6f : 0;
        float hipY = feet - 25 * scale + bob;
        float shY = feet - 55 * scale + bob;
        float legSwing = FMath.sin(ph) * 0.72f * FMath.clamp(Math.abs(vx) / Math.max(1, runSpeed), 0, 1);

        limb(g, x - 4, hipY, x - 8 + FMath.sin(-legSwing) * 12, feet - 5, 7, uniform.darker());
        limb(g, x + 4, hipY, x + 8 + FMath.sin(legSwing) * 12, feet - 5, 7, uniform);
        g.setColor(uniform);
        g.fillRoundRect((int) (x - 12 * scale), (int) shY, (int) (24 * scale), (int) (hipY - shY + 9), 10, 10);
        g.setColor(uniform.brighter());
        g.fillRect((int) (x - 8 * scale), (int) (shY + 6), (int) (16 * scale), 3);
        limb(g, x - facing() * 8, shY + 6, x - facing() * 24, shY + 28, 6, skin.darker());
        limb(g, x + facing() * 8, shY + 6, x + facing() * 27, shY + (attacking() ? -4 : 24), 6, skin);

        float hx = x + facing() * 2, hy = shY - 14;
        g.setColor(hair);
        for (int i = -3; i <= 3; i++) {
            float len = 32 + (i % 2 == 0 ? 10 : 0);
            g.fillPolygon(new int[]{(int) hx + i * 5, (int) hx + i * 8, (int) hx + i * 2},
                    new int[]{(int) hy - 4, (int) (hy + len), (int) hy + 6}, 3);
        }
        g.setColor(skin);
        g.fillOval((int) hx - 11, (int) hy - 10, 23, 23);
        g.setColor(new Color(118, 60, 150));
        g.fillRect((int) hx - 11, (int) hy - 8, 22, 4);
        g.setColor(hair.darker());
        for (int i = -2; i <= 2; i++)
            g.fillPolygon(new int[]{(int) hx + i * 5, (int) hx + i * 8, (int) hx + i * 2},
                    new int[]{(int) hy - 9, (int) hy - 20 - Math.abs(i) * 4, (int) hy - 8}, 3);
        if (horns) {
            g.setColor(new Color(205, 210, 212));
            g.fillPolygon(new int[]{(int) hx - 9, (int) hx - 17, (int) hx - 7}, new int[]{(int) hy - 7, (int) hy - 20, (int) hy - 10}, 3);
            g.fillPolygon(new int[]{(int) hx + 9, (int) hx + 17, (int) hx + 7}, new int[]{(int) hy - 7, (int) hy - 20, (int) hy - 10}, 3);
        }
        g.setColor(new Color(225, 34, 46));
        g.fillOval((int) hx + facing() * 3 - 3, (int) hy - 2, 6, 4);
        g.fillOval((int) hx - facing() * 4 - 2, (int) hy - 2, 5, 4);
        g.setColor(new Color(30, 12, 18));
        g.fillRect((int) hx - 5, (int) hy + 7, 10, 3);
        g.setComposite(old);
    }

    @Override
    public void renderGlow(Graphics2D g) {
        if (dead) return;
        renderRechargeGlow(g);
        Glow.blob(g, x + facing() * 4, top() + h * 0.2f, 7, new Color(255, 40, 50, 160));
    }
}
