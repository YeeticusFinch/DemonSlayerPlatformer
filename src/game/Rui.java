package game;

import java.awt.*;
import java.awt.geom.Path2D;

public class Rui extends Enemy {
    private final java.util.ArrayList<Ability> arts = Ability.ruiArt();
    private int artIdx;
    private float stubCd = 1.6f;
    private float traversalCooldown, traversalT, traversalDuration, traversalProgress;
    private int traversalState;
    private float traversalStartX, traversalStartY, traversalEndX, traversalEndY, traversalDirX, traversalDirY;
    private boolean headlessPhaseUsed, headless, reattaching;
    private float headlessTimer, headRecoveryT;
    private float detachedHeadX, detachedHeadY, detachedHeadVx, detachedHeadVy, detachedHeadRoll, detachedHeadSpin;
    private float headRecoveryStartX, headRecoveryStartY, headRecoveryStartRoll;
    private static final String SVG_PATH = "assets/rui.svg";
    private static final float SVG_CX = 32, SVG_FEET = 92;
    private static Svg svg;
    private static Svg.Group gHead, gTorso, gArmB, gArmF, gLegB, gLegF, gEyeGlow;
    private static boolean svgTried;

    public Rui() {
        super(22, 80);
        team = 1;
        isDemon = true;
        isBoss = true;
        name = "Rui";
        maxHp = hp = 430;
        maxSp = sp = 180;
        runSpeed = 120;
        aggroR = 1500;
        atkRange = 0;
        dmg = 14;
        elite = true;
        eliteColor = new Color(245, 245, 255);
        loadSvg();
    }

    private static void loadSvg() {
        if (svgTried) return;
        svgTried = true;
        svg = Svg.load(SVG_PATH);
        if (svg == null) return;
        gHead = svg.group("head");
        gTorso = svg.group("torso");
        gArmB = svg.group("arm-back");
        gArmF = svg.group("arm-front");
        gLegB = svg.group("leg-back");
        gLegF = svg.group("leg-front");
        gEyeGlow = svg.group("eye-glow");
        if (gTorso == null || gHead == null) svg = null;
    }

    /** Ranged fighter: keeps a mid-distance band, retreats if the player closes in. */
    @Override
    protected void think(World w, float dt, float dx, float dist) {
        if (traversalCooldown > 0) traversalCooldown -= dt;
        if (traversalState == 1) {
            traversalT += dt;
            vx = vy = 0;
            if (traversalT >= 0.1f) {
                traversalState = 2;
                traversalT = 0;
                traversalDuration = FMath.dist(x, y, traversalEndX, traversalEndY) / (5200f * 0.75f);
            }
            return;
        }
        if (traversalState == 2) {
            float remaining = FMath.dist(x, y, traversalEndX, traversalEndY);
            if (remaining <= 24 || traversalT >= traversalDuration) {
                traversalState = 0;
                traversalCooldown = 2.5f;
                vx = vy = 0;
            } else {
                traversalT += dt;
                vx = traversalDirX * 5200f * 0.75f;
                vy = traversalDirY * 5200f * 0.75f;
                onGround = false;
            }
            return;
        }
        if (reattaching) {
            vx = FMath.approach(vx, 0, 2200 * dt);
            return;
        }

        sp = Math.min(maxSp, sp + 12f * dt);
        stubCd -= dt;
        face(dx != 0 ? dx : vx);
        if (!headless && traversalCooldown <= 0 && shouldWebTraverse(w)) {
            beginWebTraverse(w, dx);
            return;
        }
        float dir = dx == 0 ? 1 : Math.signum(dx);
        if (headless) {
            vx = FMath.approach(vx, (dist < 300 ? -dir : dir) * runSpeed * 0.55f, 900 * dt);
            return;
        }
        if (dist > 640) vx = FMath.approach(vx, dir * runSpeed, 1600 * dt);
        else if (dist < 300) vx = FMath.approach(vx, -dir * runSpeed, 1900 * dt);
        else vx = FMath.approach(vx, dir * runSpeed * 0.12f + FMath.sin(aiTime * 1.4f) * 18, 1400 * dt);
        if (touchingWall != 0 && onGround) vx = FMath.approach(vx, 0, 2200 * dt);
        if (touchingWall != 0 && onGround && dist < 300) vy = -560;
        if ((state == 0 || state == 1) && stubCd <= 0 && dist < 1000 && Math.abs(w.targetFor(this).y - y) < 380) {
            Ability a = arts.get(artIdx++ % arts.size());
            if (sp >= a.cost) {
                sp -= a.cost;
                stubCd = Math.max(0.4f, a.cooldown * 0.75f);
                w.castAbility(this, a);
                return;
            }
        }
    }

    @Override
    protected void strike(World w) {}

    @Override
    public float outgoingDamageMult() { return 1.4f; }

    @Override
    public boolean hurt(World world, Fighter src, float dmgAmt, float dir, float kbX, float kbY) {
        if (headless || reattaching) return false;
        return super.hurt(world, src, dmgAmt, dir, kbX, kbY);
    }

    @Override
    public void chip(World world, float dmgAmt) {
        if (headless || reattaching) return;
        super.chip(world, dmgAmt);
    }

    @Override
    protected void die(World world) {
        if (!headlessPhaseUsed) {
            headlessPhaseUsed = true;
            headless = true;
            headlessTimer = 5f;
            hp = 0;
            dead = false;
            deathT = 0;
            invulnT = 0;
            stunT = staggerT = 0;
            traversalState = 0;
            float s = h / SVG_FEET;
            detachedHeadX = x + facing() * (gHead == null ? 0 : (gHead.pivotX - SVG_CX) * s);
            detachedHeadY = bottom() + ((gHead == null ? 27 : gHead.pivotY) - SVG_FEET) * s;
            detachedHeadVx = FMath.rand(-190, 190);
            detachedHeadVy = FMath.rand(-340, -220);
            detachedHeadRoll = 0;
            detachedHeadSpin = FMath.rand(-8f, 8f);
            world.cam.shake(12, 0.5f);
            world.banner("BEHEADED", "Rui's body keeps moving.");
            return;
        }
        super.die(world);
    }

    @Override
    protected Color abilityColor() { return new Color(245, 245, 255); }

    @Override
    public void update(World w, float dt) {
        super.update(w, dt);
        if (dead) return;
        if (headless && !reattaching) {
            headlessTimer -= dt;
            updateDetachedHead(w, dt);
            if (headlessTimer <= 0) {
                reattaching = true;
                headRecoveryT = 0;
                headRecoveryStartX = detachedHeadX;
                headRecoveryStartY = detachedHeadY;
                headRecoveryStartRoll = detachedHeadRoll;
                vx = 0;
            }
            return;
        }
        if (reattaching) {
            headRecoveryT += dt;
            float t = FMath.easeInOut(FMath.clamp(headRecoveryT / 0.65f, 0, 1));
            detachedHeadX = FMath.lerp(headRecoveryStartX, attachedHeadX(), t);
            detachedHeadY = FMath.lerp(headRecoveryStartY, attachedHeadY(), t);
            detachedHeadRoll = FMath.lerp(headRecoveryStartRoll, 0, t);
            if (headRecoveryT >= 0.65f) {
                reattaching = false;
                headless = false;
                hp = maxHp * 0.5f;
                invulnT = 0;
                hurtTimer = 0;
                vx = vy = 0;
                w.cam.shake(8, 0.35f);
                w.banner("REASSEMBLED", "The threads pull Rui's head back into place.");
            }
            return;
        }
        demonRegen(w, dt, 0.78f);
    }

    @Override
    protected float rechargeHpRate() {
        return headless || reattaching ? 0 : super.rechargeHpRate() * 0.3f;
    }

    private void updateDetachedHead(World w, float dt) {
        float radius = h * 0.18f;
        float previousBottom = detachedHeadY + radius;
        detachedHeadVy += Fighter.GRAV * dt;
        float nextX = FMath.clamp(detachedHeadX + detachedHeadVx * dt, radius, w.level.w - radius);
        float nextY = detachedHeadY + detachedHeadVy * dt;
        detachedHeadRoll += detachedHeadSpin * dt;
        float floor = Math.min(w.groundYUnder(nextX, previousBottom + 60),
                Math.min(w.groundYUnder(nextX - radius * 0.7f, previousBottom + 60),
                        w.groundYUnder(nextX + radius * 0.7f, previousBottom + 60)));
        detachedHeadX = nextX;
        detachedHeadY = nextY;
        if (floor < 9000 && detachedHeadY + radius >= floor) {
            detachedHeadY = floor - radius;
            if (detachedHeadVy > 70) detachedHeadVy *= -0.24f;
            else detachedHeadVy = 0;
            detachedHeadVx *= (float) Math.pow(0.48f, dt);
            detachedHeadSpin *= (float) Math.pow(0.55f, dt);
        }
    }

    private boolean shouldWebTraverse(World w) {
        if (reattaching || headless || dead) return false;
        if (x < 520 || x > w.level.w - 520) return true;
        Fighter target = w.targetFor(this);
        float playerDist = FMath.dist(x, y, target.x, target.y);
        if (playerDist < 250) return true;
        for (Effect e : w.effects) {
            if (e.dead || !e.friendly || e.owner == null || e.owner.team == team || e.dmg <= 0) continue;
            float speed2 = e.vx * e.vx + e.vy * e.vy;
            float t = speed2 < 1 ? 0 : FMath.clamp(((x - e.x) * e.vx + (y - e.y) * e.vy) / speed2, 0, 0.25f);
            float ex = e.x + e.vx * t, ey = e.y + e.vy * t;
            if (FMath.dist(x, y, ex, ey) < 95) return true;
        }
        return false;
    }

    private void beginWebTraverse(World w, float dx) {
        float away = Math.signum(dx == 0 ? -facing() : -dx);
        float dirX = away, dirY = -0.58f;
        boolean nearLeftEdge = x < 520;
        boolean nearRightEdge = x > w.level.w - 520;
        float nearestThreat = 210f;
        for (Effect e : w.effects) {
            if (e.dead || !e.friendly || e.owner == null || e.owner.team == team || e.dmg <= 0) continue;
            float d = FMath.dist(x, y, e.x, e.y);
            if (d < nearestThreat) {
                nearestThreat = d;
                dirX = x - e.x;
                dirY = y - e.y - 120;
            }
        }
        if (nearLeftEdge || nearRightEdge) {
            dirX = Math.signum(w.level.w * 0.5f - x);
            dirY = -0.12f;
        }
        float mag = Math.max(1, FMath.dist(0, 0, dirX, dirY));
        traversalDirX = dirX / mag;
        traversalDirY = dirY / mag;
        traversalStartX = x + facing() * 12;
        traversalStartY = y - h * 0.36f;
        traversalEndX = FMath.clamp(x + traversalDirX * 520, this.w / 2f, w.level.w - this.w / 2f);
        traversalEndY = FMath.clamp(y + traversalDirY * 520, h / 2f + 8, w.level.h - h / 2f - 8);
        float vxToEnd = traversalEndX - x, vyToEnd = traversalEndY - y;
        mag = Math.max(1, FMath.dist(0, 0, vxToEnd, vyToEnd));
        traversalDirX = vxToEnd / mag;
        traversalDirY = vyToEnd / mag;
        traversalState = 1;
        traversalT = 0;
        traversalProgress = 0;
        vy = 0;
        noteAction();
    }

    private float attachedHeadX() {
        float s = h / SVG_FEET;
        return x + facing() * ((gHead == null ? SVG_CX : gHead.pivotX) - SVG_CX) * s;
    }

    private float attachedHeadY() {
        float s = h / SVG_FEET;
        return bottom() + ((gHead == null ? 27 : gHead.pivotY) - SVG_FEET) * s;
    }

    @Override
    protected void renderBody(Graphics2D g) {
        Composite old = g.getComposite();
        if (flashT > 0) g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.84f));
        if (svg != null) renderSvgBody(g);
        else renderVectorBody(g);
        if (headless || reattaching) drawDetachedHead(g);
        if (reattaching) drawHeadTether(g);
        if (traversalState != 0) drawTraversalThread(g);
        g.setComposite(old);
    }

    private void renderSvgBody(Graphics2D g) {
        float feet = bottom();
        float s = h / SVG_FEET;
        float ph = animPhase * 6.28f;
        float swing = FMath.sin(ph) * 0.3f;
        Graphics2D bg = (Graphics2D) g.create();
        bg.translate(x, feet);
        bg.scale(facing(), 1);
        bg.scale(s, s);
        bg.translate(-SVG_CX, -SVG_FEET);
        drawPart(bg, gLegB, -swing * 0.9f);
        drawPart(bg, gLegF, swing * 0.9f);
        bg.translate(0, Math.abs(FMath.sin(ph)) * -1.5f);
        drawPart(bg, gTorso, 0);
        drawPart(bg, gArmB, -swing * 0.6f);
        drawPart(bg, gArmF, attacking() ? -1.15f : swing * 0.7f);
        if (!headless && !reattaching) drawPart(bg, gHead, 0);
        bg.dispose();
    }

    private void drawPart(Graphics2D bg, Svg.Group grp, float rot) {
        if (grp == null) return;
        Graphics2D gg = (Graphics2D) bg.create();
        if (rot != 0) {
            gg.translate(grp.pivotX, grp.pivotY);
            gg.rotate(rot);
            gg.drawImage(grp.img, Math.round(-grp.pivotX), Math.round(-grp.pivotY), Math.round(svg.w), Math.round(svg.h), null);
        } else {
            gg.drawImage(grp.img, 0, 0, Math.round(svg.w), Math.round(svg.h), null);
        }
        gg.dispose();
    }

    private void drawDetachedHead(Graphics2D g) {
        if (gHead == null || svg == null) return;
        float s = h / SVG_FEET;
        Graphics2D hg = (Graphics2D) g.create();
        hg.translate(detachedHeadX, detachedHeadY);
        hg.rotate(detachedHeadRoll);
        hg.scale(facing() * s, s);
        hg.translate(-gHead.pivotX, -gHead.pivotY);
        hg.drawImage(gHead.img, 0, 0, Math.round(svg.w), Math.round(svg.h), null);
        hg.dispose();
    }

    private void drawHeadTether(Graphics2D g) {
        float s = h / SVG_FEET;
        float neckX = x + facing() * 2;
        float neckY = bottom() + (36 - SVG_FEET) * s;
        float mx = (neckX + detachedHeadX) * 0.5f;
        float my = Math.min(neckY, detachedHeadY) - 32;
        Color thread = hp < maxHp * 0.5f ? new Color(205, 24, 44) : new Color(244, 247, 255);
        Graphics2D tg = (Graphics2D) g.create();
        tg.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        tg.setColor(new Color(thread.getRed(), thread.getGreen(), thread.getBlue(), 100));
        Path2D.Float path = new Path2D.Float();
        path.moveTo(neckX, neckY);
        path.quadTo(mx, my, detachedHeadX, detachedHeadY);
        tg.draw(path);
        tg.setStroke(new BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        tg.setColor(thread);
        tg.draw(path);
        tg.dispose();
    }

    private void drawTraversalThread(Graphics2D g) {
        float progress = traversalState == 1 ? FMath.clamp(traversalT / 0.1f, 0, 1) : 1f;
        float tx = FMath.lerp(traversalStartX, traversalEndX, progress);
        float ty = FMath.lerp(traversalStartY, traversalEndY, progress);
        float hx = x + facing() * 12, hy = y - h * 0.36f;
        float cx = (hx + tx) * 0.5f - traversalDirX * 22;
        float cy = (hy + ty) * 0.5f - 28;
        Color thread = hp < maxHp * 0.5f ? new Color(205, 24, 44) : new Color(244, 247, 255);
        Graphics2D tg = (Graphics2D) g.create();
        Path2D.Float path = new Path2D.Float();
        path.moveTo(hx, hy);
        path.quadTo(cx, cy, tx, ty);
        tg.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        tg.setColor(new Color(thread.getRed(), thread.getGreen(), thread.getBlue(), 95));
        tg.draw(path);
        tg.setStroke(new BasicStroke(1.25f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        tg.setColor(thread);
        tg.draw(path);
        tg.dispose();
    }

    private void renderVectorBody(Graphics2D g) {
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
        Composite old = g.getComposite();
        if (flashT > 0) g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.84f));

        g.setColor(new Color(0, 0, 0, 58));
        g.fillOval((int) x - 15, (int) feet - 5, 30, 8);
        limb(g, x - 4, hip, x - 7 + FMath.sin(ph) * 3, feet - 4, 5, robeShadow);
        limb(g, x + 4, hip, x + 7 - FMath.sin(ph) * 3, feet - 4, 5, robe);

        g.setColor(robe);
        g.fillRoundRect((int) x - 11, (int) sh, 22, (int) (hip - sh + 14), 9, 9);
        g.setColor(robeShadow);
        g.drawRoundRect((int) x - 11, (int) sh, 22, (int) (hip - sh + 14), 9, 9);
        g.setColor(red);
        g.fillRect((int) x - 11, (int) hip - 6, 22, 6);
        for (int i = 0; i < 6; i++) {
            int col = i % 3;
            int row = i / 3;
            g.fillOval((int) x - 8 + col * 7, (int) sh + 12 + row * 19, 3, 3);
        }

        g.setColor(web);
        g.setStroke(new BasicStroke(0.9f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = 0; i < 3; i++) {
            int yy = (int) sh + 10 + i * 14;
            g.drawLine((int) x - 9, yy, (int) x + 8, yy + 9);
            g.drawLine((int) x + 9, yy, (int) x - 8, yy + 9);
        }

        limb(g, x - facing() * 8, sh + 13, x - facing() * 22, sh + 32, 5, robeShadow);
        limb(g, x + facing() * 8, sh + 13, x + facing() * (attacking() ? 34 : 22), sh + (attacking() ? 22 : 25), 5, robe);
        g.setColor(skin);
        g.fillOval((int) (x + facing() * (attacking() ? 35 : 23)) - 3, (int) sh + (attacking() ? 18 : 21), 6, 6);

        if (!headless && !reattaching) {
            g.setColor(skin);
            g.fillOval((int) x - 8, (int) sh - 26, 16, 25);
            drawHair(g, sh, hair);
            g.setColor(new Color(170, 18, 36));
            g.fillOval((int) x - 5, (int) sh - 16, 3, 3);
            g.fillOval((int) x + 2, (int) sh - 16, 3, 3);
        }
        g.setComposite(old);
    }

    @Override
    public void renderGlow(Graphics2D g) {
        if (dead) return;
        renderRechargeGlow(g);
        if (gEyeGlow != null && !headless && !reattaching) {
            float feet = bottom();
            float s = h / SVG_FEET;
            float bob = Math.abs(FMath.sin(animPhase * 6.28f)) * -1.5f;
            Graphics2D gg = (Graphics2D) g.create();
            gg.setComposite(Glow.ADD);
            gg.translate(x, feet);
            gg.scale(facing(), 1);
            gg.scale(s, s);
            gg.translate(-SVG_CX, -SVG_FEET);
            gg.translate(0, bob);
            gg.drawImage(gEyeGlow.img, 0, 0, Math.round(svg.w), Math.round(svg.h), null);
            gg.dispose();
        } else if (gEyeGlow != null && (headless || reattaching)) {
            float s = h / SVG_FEET;
            Graphics2D gg = (Graphics2D) g.create();
            gg.setComposite(Glow.ADD);
            gg.translate(detachedHeadX, detachedHeadY);
            gg.rotate(detachedHeadRoll);
            gg.scale(facing() * s, s);
            gg.translate(-gHead.pivotX, -gHead.pivotY);
            gg.drawImage(gEyeGlow.img, 0, 0, Math.round(svg.w), Math.round(svg.h), null);
            gg.dispose();
        } else {
            float eyeY = top() + h * 0.22f;
            Glow.blob(g, x - 3, eyeY, 5, new Color(255, 60, 80, 170));
            Glow.blob(g, x + 3, eyeY, 5, new Color(255, 60, 80, 170));
        }
    }

    private void drawHair(Graphics2D g, float sh) {
        drawHair(g, sh, new Color(232, 229, 242));
    }

    private void drawHair(Graphics2D g, float sh, Color hair) {
        g.setColor(hair.darker());
        g.fillArc((int) x - 10, (int) sh - 35, 20, 20, 0, 180);
        g.setColor(hair);
        g.fillArc((int) x - 9, (int) sh - 37, 18, 24, 0, 180);
        g.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int side = -1; side <= 1; side += 2) {
            hairStrand(g, x + side * 2, sh - 28, x + side * 12, sh - 18, x + side * 9, sh - 6);
            hairStrand(g, x + side * 5, sh - 30, x + side * 18, sh - 19, x + side * 13, sh - 1);
            hairStrand(g, x + side * 8, sh - 26, x + side * 21, sh - 12, x + side * 12, sh + 4);
        }
        hairStrand(g, x + facing() * 2, sh - 31, x + facing() * 6, sh - 20, x - facing() * 4, sh - 9);
        hairStrand(g, x + facing() * 6, sh - 27, x + facing() * 12, sh - 14, x + facing() * 1, sh - 3);
    }

    private void hairStrand(Graphics2D g, float sx, float sy, float cx, float cy, float ex, float ey) {
        Path2D.Float p = new Path2D.Float();
        p.moveTo(sx, sy);
        p.quadTo(cx, cy, ex, ey);
        float tipX = ex - (ex - cx) * 0.35f;
        float tipY = ey + (ey - sy) * 0.14f;
        p.quadTo(ex + (tipX - ex) * 0.5f, ey + (tipY - ey) * 0.5f, tipX, tipY);
        g.draw(p);
    }
}
