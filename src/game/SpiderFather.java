package game;

import java.awt.*;

public class SpiderFather extends Enemy {
    private final java.util.ArrayList<Ability> arts = Ability.spiderFatherArt();
    private float artCd = 1.2f;
    private int artIdx;
    private boolean molted;

    public SpiderFather() {
        super(96, 168);
        team = 1;
        isDemon = true;
        isBoss = true;
        name = "Spider Demon Father";
        maxHp = hp = 520;
        maxSp = sp = 130;
        runSpeed = 150;
        aggroR = 1400;
        atkRange = 150;
        windup = 0.36f;
        recover = 0.48f;
        atkCdBase = 1.25f;
        dmg = 16.2f;
    }

    @Override
    protected void think(World w, float dt, float dx, float dist) {
        if (!molted && hp <= maxHp * 0.5f) molt(w);
        sp = Math.min(maxSp, sp + (molted ? 22.4f : 14f) * dt);
        artCd -= dt;
        if (artCd <= 0 && sp >= 26 && dist < 760 && (state == 0 || state == 1)) {
            Ability a = arts.get(artIdx++ % arts.size());
            if (sp >= a.cost) {
                sp -= a.cost;
                artCd = molted ? 1.8f : 2.6f;
                w.castAbility(this, a);
                return;
            }
        }
        super.think(w, dt, dx, dist);
    }

    private void molt(World w) {
        molted = true;
        this.w = Math.max(this.w, 144);
        h = 252;
        y -= 42;
        runSpeed *= 1.35f;
        dmg *= 1.5f;
        w.banner("MOLTED", "The father's shell hardens.");
    }

    @Override
    public boolean hurt(World world, Fighter src, float dmgAmt, float dir, float kbX, float kbY) {
        return super.hurt(world, src, molted ? dmgAmt * 0.5f : dmgAmt, dir, kbX, kbY);
    }

    @Override
    protected void strike(World w) {
        basicStrike(w, molted ? 178 : 136, molted ? 640 : 420, 180, -12, new Color(122, 81, 140));
    }

    @Override
    protected void renderBody(Graphics2D g) {
        float feet = bottom();
        float ph = animPhase * 6.28f;
        float scale = molted ? 1.18f : 1f;
        Color skin = molted ? new Color(0x40, 0x44, 0x57) : new Color(0x94, 0x8a, 0x94);
        Color head = molted ? new Color(0x40, 0x44, 0x57) : new Color(0x7a, 0x51, 0x8c);
        Color pants = new Color(0x2b, 0x2f, 0x37);
        Color belt = new Color(0x38, 0x3c, 0x68);
        Color spike = new Color(0x1d, 0x16, 0x16);
        Composite old = g.getComposite();
        if (flashT > 0) g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.82f));
        g.setColor(new Color(0, 0, 0, 70));
        g.fillOval((int) (x - 62 * scale), (int) feet - 7, (int) (124 * scale), 13);
        float hipY = feet - 48 * scale, shY = feet - 122 * scale;
        float swing = FMath.sin(ph) * 0.5f;
        limb(g, x - 22 * scale, hipY, x - 34 * scale + swing * 16, feet - 6, 18 * scale, pants);
        limb(g, x + 22 * scale, hipY, x + 34 * scale - swing * 16, feet - 6, 18 * scale, pants);
        g.setColor(skin);
        g.fillRoundRect((int) (x - 37 * scale), (int) shY, (int) (74 * scale), (int) (78 * scale), (int) (24 * scale), (int) (24 * scale));
        g.setColor(belt);
        g.fillRect((int) (x - 36 * scale), (int) (hipY - 12 * scale), (int) (72 * scale), (int) (11 * scale));
        drawArm(g, x + facing() * 34 * scale, shY + 20 * scale, facingRight ? -0.55 : Math.PI + 0.55, skin, spike, scale);
        drawArm(g, x - facing() * 32 * scale, shY + 26 * scale, facingRight ? -2.35 : Math.PI + 2.35, skin.darker(), spike, scale);
        g.setColor(head);
        g.fillOval((int) (x - 26 * scale), (int) (shY - 45 * scale), (int) (52 * scale), (int) (48 * scale));
        g.setColor(Color.WHITE);
        g.fillRoundRect((int) (x - 22 * scale), (int) (shY - 61 * scale), (int) (44 * scale), (int) (34 * scale), (int) (16 * scale), (int) (16 * scale));
        if (molted) {
            g.setColor(spike);
            g.fillPolygon(new int[]{(int) (x - 18 * scale), (int) (x - 35 * scale), (int) (x - 10 * scale)}, new int[]{(int) (shY - 48 * scale), (int) (shY - 78 * scale), (int) (shY - 56 * scale)}, 3);
            g.fillPolygon(new int[]{(int) (x + 18 * scale), (int) (x + 35 * scale), (int) (x + 10 * scale)}, new int[]{(int) (shY - 48 * scale), (int) (shY - 78 * scale), (int) (shY - 56 * scale)}, 3);
            for (int side = -1; side <= 1; side += 2) {
                g.fillPolygon(new int[]{(int) (x + side * 37 * scale), (int) (x + side * 62 * scale), (int) (x + side * 32 * scale)}, new int[]{(int) (shY + 8 * scale), (int) (shY - 4 * scale), (int) (shY + 26 * scale)}, 3);
            }
        }
        g.setColor(new Color(110, 180, 220));
        for (int i = 0; i < 8; i++) {
            int col = i % 4, row = i / 4;
            g.fillOval((int) (x - 14 * scale + col * 9 * scale), (int) (shY - 31 * scale + row * 8 * scale), (int) (4 * scale), (int) (4 * scale));
        }
        g.setColor(new Color(20, 8, 10));
        g.fillRoundRect((int) (x - 16 * scale), (int) (shY - 13 * scale), (int) (32 * scale), (int) (14 * scale), (int) (8 * scale), (int) (8 * scale));
        g.setColor(Color.WHITE);
        for (int i = 0; i < 6; i++) g.fillPolygon(new int[]{(int) (x - 13 * scale + i * 5 * scale), (int) (x - 9 * scale + i * 5 * scale), (int) (x - 11 * scale + i * 5 * scale)}, new int[]{(int) (shY - 12 * scale), (int) (shY - 12 * scale), (int) (shY - 4 * scale)}, 3);
        g.setColor(spike);
        g.setStroke(new BasicStroke(3.5f * scale, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine((int) (x - 18 * scale), (int) (shY - 8 * scale), (int) (x - 34 * scale), (int) (shY + 1 * scale));
        g.drawLine((int) (x + 18 * scale), (int) (shY - 8 * scale), (int) (x + 34 * scale), (int) (shY + 1 * scale));
        g.setComposite(old);
    }

    private void drawArm(Graphics2D g, float sx, float sy, double ang, Color c, Color spike, float scale) {
        float ex = sx + FMath.cos((float) ang) * 48 * scale, ey = sy + FMath.sin((float) ang) * 48 * scale;
        float hx = ex + FMath.cos((float) ang) * 35 * scale, hy = ey + FMath.sin((float) ang) * 35 * scale;
        limb(g, sx, sy, ex, ey, 18 * scale, c);
        limb(g, ex, ey, hx, hy, 15 * scale, c.darker());
        if (molted) {
            g.setColor(spike);
            for (int i = 0; i < 3; i++) {
                float k = i / 2f;
                float px = FMath.lerp(sx, hx, k), py = FMath.lerp(sy, hy, k);
                g.fillPolygon(new int[]{(int) px, (int) (px + facing() * 15 * scale), (int) (px - facing() * 3 * scale)}, new int[]{(int) py, (int) (py - 9 * scale), (int) (py + 7 * scale)}, 3);
            }
        }
    }
}
