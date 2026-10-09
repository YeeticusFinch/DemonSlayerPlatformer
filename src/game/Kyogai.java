package game;

import java.awt.*;
import java.awt.geom.Path2D;

public class Kyogai extends Enemy {
    private float castCd = 1.0f;
    private float actionT;
    private int action;
    public int roomRot;

    public Kyogai() {
        super(46, 92);
        team = 1;
        isDemon = true;
        isBoss = true;
        name = "Kyogai";
        maxHp = hp = 490;
        runSpeed = 0;
        aggroR = 1200;
        atkRange = 0;
        dmg = 0;
    }

    @Override
    public void update(World w, float dt) {
        invulnT -= dt;
        flashT -= dt;
        stunT = Math.max(0, stunT - dt);
        staggerT = Math.max(0, staggerT - dt);
        showHpT -= dt;
        if (dead) { deathT += dt; return; }
        demonRegen(w, dt, 1.4f);
        vx = vy = 0;
        onGround = true;
        face(w.player.x - x);
        if (stunned()) return;
        castCd -= dt;
        if (action > 0) {
            actionT += dt;
            return;
        }
        if (castCd <= 0 && !w.player.dead) {
            float d = FMath.dist(x, y, w.player.x, w.player.y);
            if (d < 1150) chooseCast(w);
        }
    }

    private void chooseCast(World w) {
        int pick = FMath.randInt(0, 3);
        if (pick < 2) clawVolley(w);
        else beginRotate(w, pick == 2 ? 1 : -1);
    }

    private void clawVolley(World w) {
        action = 1;
        actionT = 0;
        castCd = FMath.rand(1.5f, 2.2f);
        w.schedule(0.34f, () -> {
            if (dead) return;
            float dx = w.player.x - x;
            float dy = (w.player.y - w.player.h * 0.3f) - (y - h * 0.28f);
            double base = Math.atan2(dy, dx);
            for (int i = -1; i <= 1; i++) launchClaw(w, base + Math.toRadians(i * 10));
            action = 0;
        });
    }

    private void beginRotate(World w, int dir) {
        action = dir > 0 ? 2 : 3;
        actionT = 0;
        castCd = FMath.rand(4.5f, 6.5f);
        w.schedule(0.75f, () -> {
            if (dead) return;
            Effect shock = new Effect(Effect.SLAM_RING, this, false).at(x, y + h * 0.08f).radius(2200).life(0.5f)
                    .colors(new Color(95, 70, 54), new Color(235, 225, 180));
            w.effects.add(shock);
            w.parts.ring(x, y + h * 0.08f, new Color(235, 225, 180));
            w.cam.shake(7, 0.22f);
        });
        w.schedule(1.25f, () -> {
            if (dead) return;
            w.rotateKyogaiRoom(this, dir);
            action = 0;
        });
    }

    private void launchClaw(World w, double a) {
        float spd = 520;
        Effect e = new Effect(Effect.ARROW, this, false).at(x + (float) Math.cos(a) * 34, y - h * 0.28f + (float) Math.sin(a) * 18)
                .vel((float) Math.cos(a) * spd, (float) Math.sin(a) * spd)
                .radius(26).life(1.6f).damage(15, 320, 160).pierce(0).colors(new Color(8, 8, 10), new Color(185, 170, 150));
        e.angle = (float) a;
        e.ex2 = 7;
        e.stopOnSolid = true;
        w.effects.add(e);
    }

    @Override
    protected void strike(World w) { }

    @Override
    protected Color abilityColor() { return new Color(120, 90, 60); }

    @Override
    protected void renderBody(Graphics2D g) {
        Graphics2D gg = (Graphics2D) g.create();
        double bodyRot = roomRot * Math.PI / 2.0;
        if (roomRot % 2 != 0) bodyRot += Math.PI;
        gg.rotate(bodyRot, x, y);
        if (flashT > 0) gg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.86f));
        gg.setColor(new Color(0, 0, 0, 70));
        gg.fillOval((int) x - 34, (int) bottom() - 5, 68, 10);
        float feet = bottom(), sh = feet - 64, hip = feet - 31;
        Color skin = new Color(224, 196, 166);
        Color skinDark = new Color(172, 126, 98);
        Color belt = new Color(92, 18, 26);
        Color purple = new Color(68, 44, 92);
        Color black = new Color(20, 16, 24);
        Color drum = new Color(232, 166, 88);
        Color drumRed = new Color(155, 28, 34);
        limb(gg, x - 8, hip + 22, x - 14, feet - 3, 9, purple.darker());
        limb(gg, x + 8, hip + 22, x + 14, feet - 3, 9, black);

        Path2D.Float skirt = new Path2D.Float();
        skirt.moveTo(x - 20, hip + 4);
        skirt.lineTo(x + 20, hip + 4);
        skirt.lineTo(x + 28, feet - 1);
        skirt.lineTo(x - 28, feet - 1);
        skirt.closePath();
        gg.setClip(skirt);
        for (int i = -4; i <= 4; i++) {
            gg.setColor(i % 2 == 0 ? purple : black);
            gg.fillRect((int) x + i * 8 - 4, (int) hip + 4, 9, (int) (feet - hip));
        }
        gg.setClip(null);
        gg.setColor(new Color(42, 24, 48, 210));
        gg.setStroke(new BasicStroke(1.4f));
        gg.draw(skirt);

        gg.setColor(skin);
        gg.fillOval((int) x - 26, (int) sh - 1, 52, 28);
        gg.fillRoundRect((int) x - 21, (int) sh + 5, 42, (int) (hip - sh + 8), 18, 18);
        gg.setColor(skinDark);
        gg.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        gg.drawLine((int) x, (int) sh + 12, (int) x, (int) hip - 4);
        gg.drawArc((int) x - 17, (int) sh + 16, 14, 10, 10, 160);
        gg.drawArc((int) x + 3, (int) sh + 16, 14, 10, 10, 160);
        gg.setColor(belt);
        gg.fillRoundRect((int) x - 23, (int) hip - 1, 46, 9, 5, 5);

        drum(gg, x - 22, sh + 6, 12, drum, drumRed);
        drum(gg, x + 22, sh + 6, 12, drum, drumRed);
        drum(gg, x, hip - 15, 14, drum, drumRed);
        boolean handUp = action == 2 || action == 3;
        limb(gg, x - facing() * 18, sh + 12, x - facing() * 35, sh + 34, 8, skinDark);
        limb(gg, x + facing() * 18, sh + 12, x + facing() * 32, handUp ? sh - 20 : sh + 30, 8, skin);
        gg.setColor(skin);
        gg.fillOval((int) x - 13, (int) sh - 28, 26, 26);
        messyHair(gg, sh);
        gg.setColor(new Color(205, 18, 28));
        gg.fillArc((int) x - 8, (int) sh - 18, 8, 7, 0, 180);
        gg.fillArc((int) x + 2, (int) sh - 18, 8, 7, 0, 180);
        gg.setColor(new Color(70, 32, 28));
        gg.fillRect((int) x - 5, (int) sh - 9, 11, 3);
        gg.dispose();
    }

    private void drum(Graphics2D g, float cx, float cy, int r, Color fill, Color border) {
        g.setColor(fill);
        g.fillOval((int) cx - r, (int) cy - r, r * 2, r * 2);
        g.setColor(border);
        g.setStroke(new BasicStroke(3f));
        g.drawOval((int) cx - r, (int) cy - r, r * 2, r * 2);
    }

    private void messyHair(Graphics2D g, float sh) {
        float hy = sh - 20;
        g.setColor(new Color(16, 14, 16));
        g.fillArc((int) x - 17, (int) hy - 14, 34, 26, 0, 180);
        g.fillRoundRect((int) x - 17, (int) hy - 2, 9, 23, 6, 6);
        g.fillRoundRect((int) x + 8, (int) hy - 3, 9, 24, 6, 6);
        Polygon bangs = new Polygon();
        bangs.addPoint((int) x - 15, (int) hy - 3);
        bangs.addPoint((int) x - 8, (int) hy + 9);
        bangs.addPoint((int) x - 2, (int) hy - 2);
        bangs.addPoint((int) x + 4, (int) hy + 10);
        bangs.addPoint((int) x + 10, (int) hy - 1);
        bangs.addPoint((int) x + 16, (int) hy + 8);
        bangs.addPoint((int) x + 14, (int) hy - 9);
        bangs.addPoint((int) x - 15, (int) hy - 9);
        g.fillPolygon(bangs);
    }

    @Override
    public void renderGlow(Graphics2D g) {
        if (dead) return;
        renderRechargeGlow(g);
        Glow.blob(g, x - 4, top() + h * 0.24f, 6, new Color(220, 18, 28, 140));
        Glow.blob(g, x + 4, top() + h * 0.24f, 6, new Color(220, 18, 28, 140));
    }
}
