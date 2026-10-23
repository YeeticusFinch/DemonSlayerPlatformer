package game;

import java.awt.*;

public class SpiderBrother extends Enemy {
    private static final Color POISON = new Color(18, 82, 78);
    private static final Color POISON_HI = new Color(76, 142, 132);
    private float crawlerCd = 0.75f, poisonCd = 1.2f, stringCd = 1.8f, globCd = 3.15f, barrageCd = 3.9f;
    private float stringT, stringX, stringY, stringVx, stringVy;

    public SpiderBrother() {
        super(96, 118);
        team = 1;
        isDemon = true;
        isBoss = true;
        name = "Spider Demon Brother";
        maxHp = hp = 216;
        maxSp = sp = 210;
        runSpeed = 0;
        dmg = 16.2f;
    }

    @Override
    public void update(World w, float dt) {
        invulnT -= dt;
        flashT -= dt;
        stunT = Math.max(0, stunT - dt);
        staggerT = Math.max(0, staggerT - dt);
        timeSinceAction += dt;
        hurtTimer += dt;
        stringT = Math.max(0, stringT - dt);
        if (dead) { deathT += dt; return; }
        vx = vy = 0;
        onGround = false;
        face(w.targetFor(this).x - x);
        sp = Math.min(maxSp, sp + 11.25f * dt);
        crawlerCd -= dt;
        poisonCd -= dt;
        stringCd -= dt;
        globCd -= dt;
        barrageCd -= dt;
        if (stunned()) return;
        if (barrageCd <= 0 && sp >= 45) { sp -= 45; barrageCd = 5.85f; poisonBarrage(w); }
        else if (globCd <= 0 && sp >= 36) { sp -= 36; globCd = 4.8f; poisonGlob(w); }
        else if (stringCd <= 0 && sp >= 24) { sp -= 24; stringCd = 2.55f; stringShot(w); }
        else if (poisonCd <= 0 && sp >= 18) { sp -= 18; poisonCd = 1.575f; spitPoison(w); }
        else if (crawlerCd <= 0 && sp >= 28) { sp -= 28; crawlerCd = 5.4f; spawnCrawlers(w); }
    }

    private void spawnCrawlers(World w) {
        for (int i = 0; i < 2; i++) {
            CrawlingDemon c = new CrawlingDemon(0.5f, "Spider Crawling Demon");
            c.team = team;
            float sx = FMath.clamp(w.targetFor(this).x + (i == 0 ? -150 : 170), 80, w.level.w - 80);
            float gy = w.groundYUnder(sx, w.viewT);
            if (gy > 9000) gy = w.targetFor(this).bottom();
            c.init(sx, gy, c.maxHp);
            c.face(w.targetFor(this).x - sx);
            w.schedule(0.01f, () -> { if (!dead) w.enemies.add(c); });
        }
    }

    private void spitPoison(World w) {
        float dx = w.targetFor(this).x - x, dy = w.targetFor(this).y - y;
        float d = Math.max(1, FMath.dist(0, 0, dx, dy));
        Effect e = new Effect(Effect.ARROW, this, false).at(x, y + 35).vel(dx / d * 560, dy / d * 560)
                .radius(32).life(1.7f).damage(16.2f, 220, 140).colors(POISON, POISON_HI);
        w.effects.add(e);
    }

    private void stringShot(World w) {
        float sx = x, sy = y + 35;
        float dx = w.targetFor(this).x - sx, dy = (w.targetFor(this).y - w.targetFor(this).h * 0.35f) - sy;
        float d = Math.max(1, FMath.dist(0, 0, dx, dy));
        stringX = sx;
        stringY = sy;
        stringVx = dx / d;
        stringVy = dy / d;
        stringT = 0.45f;
        Effect e = new Effect(Effect.ARROW, this, false).at(sx, sy).vel(dx / d * 760, dy / d * 760)
                .radius(18).life(0.95f).damage(18, 920, 250).colors(new Color(245, 248, 255), new Color(255, 255, 255));
        w.effects.add(e);
    }

    private void poisonGlob(World w) {
        float tx = w.targetFor(this).x, ty = w.targetFor(this).y - 20;
        Effect glob = new Effect(Effect.BOULDER, this, false).at(x, y + 25).vel((tx - x) * 0.75f, (ty - y) * 0.75f)
                .radius(38).life(1.25f).damage(14.4f, 160, 120).colors(new Color(58, 120, 46), new Color(190, 255, 120));
        w.effects.add(glob);
        for (int i = 1; i <= 3; i++) {
            w.schedule(i * 0.27f, () -> {
                if (!glob.dead) w.effects.add(new Effect(Effect.SLAM_RING, this, false).at(glob.x, glob.y).radius(70).life(0.18f)
                        .colors(new Color(100, 210, 70), new Color(225, 255, 150)));
            });
        }
        w.schedule(1.05f, () -> poisonExplosion(w, glob.x, glob.y));
    }

    private void poisonExplosion(World w, float gx, float gy) {
        if (dead) return;
        w.effects.add(new Effect(Effect.SLAM_RING, this, false).at(gx, gy).radius(64).life(0.45f)
                .damage(18, 260, 180).colors(new Color(78, 180, 64), new Color(210, 255, 130)));
        for (int i = 0; i < 10; i++) {
            float a = i * 0.628f;
            Effect e = new Effect(Effect.ARROW, this, false).at(gx, gy).vel(FMath.cos(a) * 620, FMath.sin(a) * 620)
                    .radius(18).life(0.42f).colors(POISON, POISON_HI);
            w.effects.add(e);
        }
        w.cam.shake(7, 0.22f);
    }

    private void poisonBarrage(World w) {
        for (int i = 0; i < 18; i++) {
            final int idx = i;
            w.schedule(idx * 0.045f, () -> {
                if (dead) return;
                float a = idx * 0.9f + Game.time * 3.5f;
                Effect e = new Effect(Effect.ARROW, this, false).at(x + FMath.cos(a) * 42, y + 35 + FMath.sin(a) * 30)
                        .vel(FMath.cos(a) * 580, FMath.sin(a) * 580).radius(18).life(0.8f)
                        .damage(9, 170, 100).colors(POISON, POISON_HI);
                w.effects.add(e);
            });
        }
    }

    private static float pointSegmentDist(float px, float py, float ax, float ay, float bx, float by) {
        float dx = bx - ax, dy = by - ay, len2 = dx * dx + dy * dy;
        if (len2 <= 1) return FMath.dist(px, py, ax, ay);
        float t = FMath.clamp(((px - ax) * dx + (py - ay) * dy) / len2, 0, 1);
        return FMath.dist(px, py, ax + dx * t, ay + dy * t);
    }

    @Override protected void think(World w, float dt, float dx, float dist) {}
    @Override protected void strike(World w) {}

    @Override
    protected void renderBody(Graphics2D g) {
        Stroke oldS = g.getStroke();
        g.setStroke(new BasicStroke(1.2f));
        g.setColor(new Color(245, 248, 255, 150));
        g.drawLine((int) x, (int) (y - 520), (int) x, (int) (y - 18));
        if (stringT > 0) {
            float elapsed = 0.45f - stringT;
            float tipX = stringX + stringVx * elapsed * 760f;
            float tipY = stringY + stringVy * elapsed * 760f;
            float tailX = stringX + stringVx * Math.max(0, elapsed * 760f - 180f);
            float tailY = stringY + stringVy * Math.max(0, elapsed * 760f - 180f);
            g.setStroke(new BasicStroke(2.1f));
            g.setColor(new Color(245, 248, 255, (int) (180 * stringT / 0.32f)));
            g.drawLine((int) tailX, (int) tailY, (int) tipX, (int) tipY);
        }
        g.setStroke(oldS);
        Composite old = g.getComposite();
        if (flashT > 0) g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.84f));
        Graphics2D gg = (Graphics2D) g.create();
        gg.translate(x, y + 28);
        gg.setColor(new Color(8, 9, 10));
        gg.fillOval(-42, -26, 84, 52);
        gg.setColor(new Color(220, 96, 38));
        for (int i = -2; i <= 2; i++) gg.fillRoundRect(i * 14 - 4, -22, 8, 44, 5, 5);
        gg.setStroke(new BasicStroke(8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        gg.setColor(new Color(8, 9, 10));
        for (int side = -1; side <= 1; side += 2) for (int i = 0; i < 4; i++) {
            float sy = -18 + i * 12;
            gg.drawLine(side * 30, (int) sy, side * (62 + i * 8), (int) (sy - 26 + i * 10));
        }
        gg.setColor(new Color(236, 232, 222));
        gg.fillOval(-24, 16, 48, 38);
        gg.setColor(new Color(24, 18, 20));
        gg.fillOval(-8, 30, 5, 5);
        gg.fillOval(3, 30, 5, 5);
        gg.dispose();
        g.setComposite(old);
    }
}
