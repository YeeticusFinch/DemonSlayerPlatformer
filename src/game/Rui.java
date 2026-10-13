package game;

import java.awt.*;
import java.awt.geom.Path2D;

public class Rui extends Enemy {
    private final java.util.ArrayList<Ability> arts = Ability.ruiArt();
    private int artIdx;
    private float stubCd = 1.6f;

    public Rui() {
        super(30, 80);
        team = 1;
        isDemon = true;
        isBoss = true;
        name = "Rui";
        maxHp = hp = 430;
        maxSp = sp = 180;
        runSpeed = 180;
        aggroR = 980;
        atkRange = 88;
        dmg = 14;
        elite = true;
        eliteColor = new Color(245, 245, 255);
    }

    @Override
    protected void think(World w, float dt, float dx, float dist) {
        sp = Math.min(maxSp, sp + 12f * dt);
        stubCd -= dt;
        if ((state == 0 || state == 1) && stubCd <= 0 && dist < 760 && sp >= 18) {
            Ability a = arts.get(artIdx++ % arts.size());
            sp -= Math.min(sp, a.cost);
            stubCd = a.cooldown;
            w.popup(x, top() - 14, a.form, new Color(245, 245, 255));
            noteAction();
        }
        super.think(w, dt, dx, dist);
    }

    @Override
    protected void strike(World w) {
        basicStrike(w, 96, 260, 120, -8, new Color(235, 235, 248));
    }

    @Override
    protected Color abilityColor() { return new Color(245, 245, 255); }

    @Override
    public void update(World w, float dt) {
        super.update(w, dt);
        demonRegen(w, dt, 2.6f);
    }

    @Override
    protected void renderBody(Graphics2D g) {
        Composite old = g.getComposite();
        if (flashT > 0) g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.84f));
        float feet = bottom();
        float ph = animPhase * 6.28f;
        float hip = feet - 27;
        float sh = feet - 56;
        Color skin = new Color(248, 248, 246);
        Color robe = new Color(246, 246, 241);
        Color robeShadow = new Color(214, 214, 216);
        Color web = new Color(164, 164, 172);
        Color red = new Color(154, 20, 34);
        Color hair = new Color(232, 229, 242);

        g.setColor(new Color(0, 0, 0, 58));
        g.fillOval((int) x - 24, (int) feet - 5, 48, 9);
        limb(g, x - 5, hip, x - 9 + FMath.sin(ph) * 3, feet - 4, 7, robeShadow);
        limb(g, x + 5, hip, x + 9 - FMath.sin(ph) * 3, feet - 4, 7, robe);

        g.setColor(robe);
        g.fillRoundRect((int) x - 17, (int) sh, 34, (int) (hip - sh + 14), 13, 13);
        g.setColor(robeShadow);
        g.drawRoundRect((int) x - 17, (int) sh, 34, (int) (hip - sh + 14), 13, 13);
        g.setColor(red);
        g.fillRect((int) x - 17, (int) hip - 6, 34, 7);
        for (int i = 0; i < 8; i++) {
            int col = i % 4;
            int row = i / 4;
            g.fillOval((int) x - 12 + col * 8, (int) sh + 12 + row * 19, 4, 4);
        }

        g.setColor(web);
        g.setStroke(new BasicStroke(1.1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = 0; i < 3; i++) {
            int yy = (int) sh + 10 + i * 14;
            g.drawLine((int) x - 14, yy, (int) x + 12, yy + 9);
            g.drawLine((int) x + 14, yy, (int) x - 12, yy + 9);
        }

        limb(g, x - facing() * 12, sh + 13, x - facing() * 32, sh + 32, 7, robeShadow);
        limb(g, x + facing() * 12, sh + 13, x + facing() * (attacking() ? 44 : 31), sh + (attacking() ? 22 : 25), 7, robe);
        g.setColor(skin);
        g.fillOval((int) (x + facing() * (attacking() ? 45 : 32)) - 4, (int) sh + (attacking() ? 18 : 21), 8, 8);

        g.setColor(skin);
        g.fillOval((int) x - 12, (int) sh - 26, 24, 25);
        drawHair(g, sh, hair);
        g.setColor(new Color(170, 18, 36));
        g.fillOval((int) x - 6, (int) sh - 16, 4, 3);
        g.fillOval((int) x + 2, (int) sh - 16, 4, 3);
        g.setColor(new Color(106, 24, 38));
        g.drawLine((int) x - 4, (int) sh - 6, (int) x + 4, (int) sh - 6);
        g.setComposite(old);
    }

    @Override
    public void renderGlow(Graphics2D g) {
        if (dead) return;
        renderRechargeGlow(g);
        Glow.blob(g, x - 4, top() + h * 0.22f, 5, new Color(210, 20, 44, 150));
        Glow.blob(g, x + 4, top() + h * 0.22f, 5, new Color(210, 20, 44, 150));
    }

    private void drawHair(Graphics2D g, float sh) {
        drawHair(g, sh, new Color(232, 229, 242));
    }

    private void drawHair(Graphics2D g, float sh, Color hair) {
        g.setColor(hair.darker());
        g.fillArc((int) x - 15, (int) sh - 35, 30, 20, 0, 180);
        g.setColor(hair);
        g.fillArc((int) x - 14, (int) sh - 37, 28, 24, 0, 180);
        g.setStroke(new BasicStroke(5.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int side = -1; side <= 1; side += 2) {
            hairStrand(g, x + side * 3, sh - 28, x + side * 17, sh - 18, x + side * 14, sh - 2);
            hairStrand(g, x + side * 8, sh - 30, x + side * 25, sh - 19, x + side * 22, sh + 7);
            hairStrand(g, x + side * 12, sh - 26, x + side * 31, sh - 12, x + side * 28, sh + 15);
        }
        hairStrand(g, x, sh - 31, x + facing() * 11, sh - 18, x + facing() * 6, sh - 2);
    }

    private void hairStrand(Graphics2D g, float sx, float sy, float cx, float cy, float ex, float ey) {
        Path2D.Float p = new Path2D.Float();
        p.moveTo(sx, sy);
        p.quadTo(cx, cy, ex, ey);
        g.draw(p);
    }
}
