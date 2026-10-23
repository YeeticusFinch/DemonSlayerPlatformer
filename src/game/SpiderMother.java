package game;

import java.awt.*;

public class SpiderMother extends Enemy {
    private float puppetCd = 1.2f, crawlerCd = 3.2f, whipCd = 2.0f;
    private int action;
    private float actionT, whipFlashT, whipTargetX, whipTargetY, lastWhipX, lastWhipY;
    private boolean actionDone;

    public SpiderMother() {
        super(70, 124);
        team = 1;
        isDemon = true;
        isBoss = true;
        name = "Spider Demon Mother";
        maxHp = hp = 340;
        maxSp = sp = 120;
        runSpeed = 0;
        aggroR = 1500;
        atkRange = 0;
        dmg = 22;
    }

    @Override
    public void update(World w, float dt) {
        invulnT -= dt;
        flashT -= dt;
        stunT = Math.max(0, stunT - dt);
        staggerT = Math.max(0, staggerT - dt);
        showHpT -= dt;
        timeSinceAction += dt;
        hurtTimer += dt;
        whipFlashT = Math.max(0, whipFlashT - dt);
        if (dead) { deathT += dt; return; }
        vx = vy = 0;
        onGround = true;
        face(w.targetFor(this).x - x);
        if (stunned()) return;

        sp = Math.min(maxSp, sp + 12f * dt);
        puppetCd -= dt;
        crawlerCd -= dt;
        whipCd -= dt;

        if (action == 0) chooseAction(w);
        if (action == 0) return;
        actionT += dt;
        if (action == 1 && actionT >= 0.52f && !actionDone) {
            actionDone = true;
            spawnPuppets(w);
        } else if (action == 2 && actionT >= 0.42f && !actionDone) {
            actionDone = true;
            snapWhip(w);
        } else if (action == 3 && actionT >= 0.5f && !actionDone) {
            actionDone = true;
            spawnCrawlers(w);
        }
        if (actionT >= 0.9f) action = 0;
    }

    private void chooseAction(World w) {
        if (whipCd <= 0 && sp >= 28 && FMath.dist(x, y, w.targetFor(this).x, w.targetFor(this).y) < 900) {
            beginAction(2);
            sp -= 28;
            whipCd = 2.8f;
            whipTargetX = w.targetFor(this).x;
            whipTargetY = w.targetFor(this).y - w.targetFor(this).h * 0.35f;
            lastWhipX = whipTargetX;
            lastWhipY = whipTargetY;
        } else if (puppetCd <= 0 && sp >= 42) {
            beginAction(1);
            sp -= 42;
            puppetCd = 6.8f;
        } else if (crawlerCd <= 0 && sp >= 34) {
            beginAction(3);
            sp -= 34;
            crawlerCd = 7.8f;
        }
    }

    private void beginAction(int a) {
        action = a;
        actionT = 0;
        actionDone = false;
        noteAction();
    }

    private void spawnPuppets(World w) {
        for (int i = 0; i < 2; i++) {
            PuppetSlayer p = new PuppetSlayer();
            p.team = team;
            float sx = FMath.clamp(w.targetFor(this).x + (i == 0 ? -1 : 1) * FMath.rand(90, 240), 80, w.level.w - 80);
            float dropBottom = Math.max(80, w.viewT - FMath.rand(80, 170));
            p.init(sx, dropBottom, p.maxHp);
            p.vy = FMath.rand(220, 340);
            p.face(w.targetFor(this).x - p.x);
            w.schedule(0.01f, () -> { if (!dead) w.enemies.add(p); });
        }
        w.popup(x, top() - 12, "Threads descend", new Color(230, 235, 255));
    }

    private void spawnCrawlers(World w) {
        for (int i = 0; i < 2; i++) {
            CrawlingDemon c = new CrawlingDemon();
            c.team = team;
            float sx = FMath.clamp(w.targetFor(this).x + (i == 0 ? -1 : 1) * FMath.rand(140, 290), 80, w.level.w - 80);
            float gy = w.groundYUnder(sx, w.targetFor(this).y);
            if (gy > 9000) gy = w.groundYUnder(x, y);
            if (gy > 9000) gy = bottom();
            c.init(sx, gy, c.maxHp);
            c.face(w.targetFor(this).x - c.x);
            w.schedule(0.01f, () -> { if (!dead) w.enemies.add(c); });
        }
        w.popup(x, top() - 12, "Crawl for me", new Color(240, 210, 220));
    }

    private void snapWhip(World w) {
        lastWhipX = whipTargetX;
        lastWhipY = whipTargetY;
        whipFlashT = 0.32f;
        float hx = handX(), hy = handY();
            Fighter pl = w.targetFor(this);
        if (!pl.dead && pointSegmentDist(pl.x, pl.y - pl.h * 0.35f, hx, hy, whipTargetX, whipTargetY) < 72) {
            float dir = Math.signum(pl.x - x);
            if (dir == 0) dir = facing();
            pl.hurt(w, this, dmg, dir, 980, 260);
            w.impactFeedback(pl.x, pl.y - pl.h * 0.35f, new Color(240, 245, 255));
            w.cam.shake(6, 0.2f);
            dealtHit();
        }
    }

    private static float pointSegmentDist(float px, float py, float ax, float ay, float bx, float by) {
        float dx = bx - ax, dy = by - ay;
        float len2 = dx * dx + dy * dy;
        if (len2 <= 1) return FMath.dist(px, py, ax, ay);
        float t = FMath.clamp(((px - ax) * dx + (py - ay) * dy) / len2, 0, 1);
        return FMath.dist(px, py, ax + dx * t, ay + dy * t);
    }

    @Override
    protected void think(World w, float dt, float dx, float dist) {
    }

    @Override
    protected void strike(World w) {
    }

    @Override
    protected void renderBody(Graphics2D g) {
        Composite old = g.getComposite();
        drawBoulder(g);
        if (flashT > 0) g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.84f));
        drawStringFan(g);
        drawMother(g);
        drawWhip(g);
        g.setComposite(old);
    }

    private void drawBoulder(Graphics2D g) {
        float feet = bottom();
        g.setColor(new Color(78, 80, 88));
        g.fillRoundRect((int) x - 72, (int) feet - 44, 144, 62, 34, 28);
        g.setColor(new Color(108, 110, 120));
        g.fillOval((int) x - 42, (int) feet - 39, 44, 16);
        g.fillOval((int) x + 8, (int) feet - 28, 38, 13);
        g.setColor(new Color(0, 0, 0, 70));
        g.drawRoundRect((int) x - 72, (int) feet - 44, 144, 62, 34, 28);
    }

    private void drawMother(Graphics2D g) {
        float feet = bottom(), seat = feet - 42;
        float shY = seat - 64;
        Color skin = new Color(236, 232, 226);
        Color hair = new Color(238, 238, 232);
        Color kimono = new Color(235, 235, 230);
        Color red = new Color(154, 24, 36);

        g.setColor(kimono);
        g.fillRoundRect((int) x - 31, (int) shY, 62, 77, 26, 24);
        g.setColor(red);
        g.fillRect((int) x - 31, (int) (shY + 69), 62, 7);
        g.fillRect((int) x - 31, (int) shY + 7, 5, 62);
        g.fillRect((int) x + 26, (int) shY + 7, 5, 62);
        for (int i = -2; i <= 2; i++) {
            g.fillOval((int) (x + i * 11 - 3), (int) (shY + 71 + Math.abs(i % 2) * 2), 6, 4);
        }

        limb(g, x - 22, shY + 26, x - 63, shY + 18, 9, kimono);
        limb(g, x + 22, shY + 26, x + 63, shY + 18, 9, kimono);
        g.setColor(red);
        g.setStroke(new BasicStroke(2.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine((int) x - 48, (int) shY + 21, (int) x - 65, (int) shY + 18);
        g.drawLine((int) x + 48, (int) shY + 21, (int) x + 65, (int) shY + 18);
        g.setColor(skin);
        g.fillOval((int) x - 69, (int) shY + 13, 12, 12);
        g.fillOval((int) x + 57, (int) shY + 13, 12, 12);

        g.setColor(hair.darker());
        g.fillOval((int) x - 26, (int) shY - 43, 52, 56);
        g.fillRoundRect((int) x - 28, (int) shY - 28, 18, 94, 10, 18);
        g.fillRoundRect((int) x + 10, (int) shY - 28, 18, 94, 10, 18);
        g.setColor(skin);
        g.fillOval((int) x - 18, (int) shY - 35, 36, 38);
        g.setColor(hair);
        g.fillArc((int) x - 24, (int) shY - 45, 48, 38, 0, 180);
        g.fillRoundRect((int) x - 25, (int) shY - 28, 12, 92, 8, 16);
        g.fillRoundRect((int) x + 13, (int) shY - 28, 12, 92, 8, 16);
        g.setColor(new Color(88, 18, 28));
        g.fillOval((int) x - 8, (int) shY - 20, 5, 4);
        g.fillOval((int) x + 3, (int) shY - 20, 5, 4);
        g.setColor(new Color(120, 44, 56));
        g.drawLine((int) x - 5, (int) shY - 8, (int) x + 5, (int) shY - 8);
    }

    private void drawStringFan(Graphics2D g) {
        Stroke oldS = g.getStroke();
        g.setStroke(new BasicStroke(0.85f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int side = -1; side <= 1; side += 2) {
            float hx = x + side * 63, hy = handY();
            for (int i = 0; i < 7; i++) {
                float seed = i * 1.73f + side * 4.2f;
                float tx = hx + side * (240 + i * 52) + FMath.sin(seed) * 170;
                float ty = hy - 440 - i * 38 + FMath.cos(seed * 1.7f) * 180;
                g.setPaint(new GradientPaint(hx, hy, new Color(255, 255, 255, 125), tx, ty, new Color(255, 255, 255, 0)));
                g.drawLine((int) hx, (int) hy, (int) tx, (int) ty);
            }
        }
        g.setStroke(oldS);
    }

    private void drawWhip(Graphics2D g) {
        if (action != 2 && whipFlashT <= 0) return;
        float a = action == 2 ? FMath.clamp(actionT / 0.42f, 0, 1) : FMath.clamp(whipFlashT / 0.32f, 0, 1);
        float hx = handX(), hy = handY();
        float tx = action == 2 ? whipTargetX : lastWhipX;
        float ty = action == 2 ? whipTargetY : lastWhipY;
        Stroke oldS = g.getStroke();
        g.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(new Color(245, 248, 255, (int) (190 * Math.max(0.18f, a))));
        int steps = 12;
        int px = (int) hx, py = (int) hy;
        for (int i = 1; i <= steps; i++) {
            float k = i / (float) steps;
            float wobble = FMath.sin(k * 18f + Game.time * 18f) * 18f * (1 - Math.abs(k - 0.5f));
            float nx = FMath.lerp(hx, tx, k);
            float ny = FMath.lerp(hy, ty, k) + wobble;
            g.drawLine(px, py, (int) nx, (int) ny);
            px = (int) nx;
            py = (int) ny;
        }
        g.setStroke(oldS);
    }

    private float handX() { return x + facing() * 63; }

    private float handY() { return bottom() - 88; }

    @Override
    public void renderGlow(Graphics2D g) {
        if (dead) return;
        Glow.blob(g, x, top() + 20, 28, new Color(255, 245, 245, 50));
    }

    @Override
    protected Color abilityColor() { return new Color(240, 245, 255); }
}
