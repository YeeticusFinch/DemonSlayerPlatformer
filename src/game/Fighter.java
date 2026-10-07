package game;

import java.awt.*;
import java.awt.geom.Path2D;
import java.util.ArrayList;

public abstract class Fighter extends Entity {
    public static final float GRAV = 2400f, MAX_FALL = 1500f;

    public String name = "";
    public float maxHp, hp;
    public float sp = 100, maxSp = 100;
    public float runSpeed = 340;
    public float invulnT, staggerT;
    public float animPhase, squash, lean;
    public int touchingWall;
    public float deathT;
    public float contactCd;

    public float atkT, atkDur, comboWindow;
    public int comboStep;
    protected boolean comboHitApplied;

    public boolean guarding;
    public float guardAnim;
    public boolean recharging;
    public float rechargeT;
    private float rechargePartT;
    private float rechargeHpTarget;
    public int hitStreak;
    private float lastChainHitT = -99f;
    public float stunT;
    public boolean isBoss;

    public boolean limbFrontArm = true, limbBackArm = true, limbFrontLeg = true, limbBackLeg = true;
    public float limbFlashFA, limbFlashBA, limbFlashFL, limbFlashBL;
    public boolean lost30;
    public int swingSide = 1;
    public float timeSinceAction = 99;

    protected Fighter(float w, float h) {
        super(w, h);
    }

    public void init(float x, float y, float hp) {
        this.x = x;
        this.y = y - h / 2f;
        this.maxHp = this.hp = hp;
    }

    public void noteAction() { timeSinceAction = 0; }

    public boolean stunned() { return stunT > 0; }

    public int armsPresent() {
        return (limbFrontArm ? 1 : 0) + (limbBackArm ? 1 : 0);
    }

    public int legsMissing() {
        return (limbFrontLeg ? 0 : 1) + (limbBackLeg ? 0 : 1);
    }

    public boolean canMelee() { return !isDemon || armsPresent() > 0; }

    public float armCdMult() { return isDemon && armsPresent() == 1 ? 2f : 1f; }

    public float legSpeedMult() { return isDemon && legsMissing() == 1 ? 0.5f : 1f; }

    public boolean legBlocked() { return isDemon && legsMissing() > 0; }

    public boolean immobile() { return isDemon && legsMissing() >= 2; }

    public boolean hurt(World world, Fighter src, float dmg, float dir, float kbX, float kbY) {
        if (dead || invulnT > 0) return false;
        hurtTimer = 0;
        boolean guarded = guarding;
        if (guarded) {
            dmg *= 0.10f;
            kbX *= 0.10f;
            kbY *= 0.2f;
            guardAnim = 0.25f;
            world.parts.burst(Particles.SPARK, x + facing() * w * 0.7f, y - h * 0.45f, 8, 220, 0.22f, 6,
                    new Color(255, 240, 190), 60, 0.9f);
        }
        hp -= dmg;
        flashT = guarded ? 0.08f : 0.16f;
        invulnT = 0.4f;
        if (!guarded) {
            stopRecharge();
            staggerT = 0.16f;
            vx += dir * kbX;
            vy = -kbY;
            onGround = false;
            world.onHurt(this, dmg);
            tookHit(world, src);
            if (isDemon) demonLimbHitCheck(world, dmg);
        }
        if (hp <= 0) {
            if (this instanceof Player pl && pl.tryNezukoRescue(world)) return true;
            hp = 0;
            die(world);
        }
        return true;
    }

    private void tookHit(World world, Fighter src) {
        if (world.time - lastChainHitT > 3f) hitStreak = 0;
        lastChainHitT = world.time;
        hitStreak++;
        if (hitStreak >= 3) {
            hitStreak = 0;
            if (stunT > 0 && src instanceof Player plSrc) {
                plSrc.drainSpHalf();
            } else if (stunT <= 0) {
                stunT = 2f;
                vx *= 0.2f;
                world.parts.ring(x, y - h * 0.5f, new Color(255, 230, 120));
            }
        }
    }

    public void dealtHit() {
        hitStreak = 0;
        noteAction();
    }

    public float outgoingDamageMult() { return 1f; }

    public void demonLimbHitCheck(World w, float dmg) {
        if (!isDemon || isBoss) return;
        if (dmg > maxHp * 0.10f && FMath.chance(0.35f)) loseRandomLimb(w);
        if (hp < maxHp * 0.3f && !lost30) {
            lost30 = true;
            loseRandomLimb(w);
        }
    }

    public void loseRandomLimb(World w) {
        ArrayList<Integer> pool = new ArrayList<>();
        if (limbFrontArm) pool.add(0);
        if (limbBackArm) pool.add(1);
        if (limbFrontLeg) pool.add(2);
        if (limbBackLeg) pool.add(3);
        if (pool.isEmpty()) return;
        int pick = pool.get(FMath.randInt(0, pool.size() - 1));
        float ax = x, ay = y - h * 0.45f;
        switch (pick) {
            case 0 -> { limbFrontArm = false; ax = x + facing() * 8; ay = y - h * 0.38f; }
            case 1 -> { limbBackArm = false; ax = x - facing() * 8; ay = y - h * 0.38f; }
            case 2 -> { limbFrontLeg = false; ax = x + facing() * 4; ay = y - h * 0.05f; }
            case 3 -> { limbBackLeg = false; ax = x - facing() * 4; ay = y - h * 0.05f; }
        }
        w.limbSevered(ax, ay, isDemon);
        if (this == w.player) w.cam.shake(6, 0.25f);
    }

    public void regrowOne(World w) {
        ArrayList<Integer> pool = new ArrayList<>();
        if (!limbFrontArm) pool.add(0);
        if (!limbBackArm) pool.add(1);
        if (!limbFrontLeg) pool.add(2);
        if (!limbBackLeg) pool.add(3);
        if (pool.isEmpty()) return;
        int pick = pool.get(FMath.randInt(0, pool.size() - 1));
        float ax = x, ay = y - h * 0.45f;
        switch (pick) {
            case 0 -> { limbFrontArm = true; limbFlashFA = 0.5f; ax = x + facing() * 8; ay = y - h * 0.38f; }
            case 1 -> { limbBackArm = true; limbFlashBA = 0.5f; ax = x - facing() * 8; ay = y - h * 0.38f; }
            case 2 -> { limbFrontLeg = true; limbFlashFL = 0.5f; ax = x + facing() * 4; ay = y - h * 0.05f; }
            case 3 -> { limbBackLeg = true; limbFlashBL = 0.5f; ax = x - facing() * 4; ay = y - h * 0.05f; }
        }
        w.parts.burst(Particles.SPARK, ax, ay, 14, 220, 0.4f, 6, new Color(255, 120, 140), 40, 0.92f);
        w.parts.ring(ax, ay, new Color(255, 170, 190));
    }

    public void regrowAll(World w) {
        while (!limbFrontArm || !limbBackArm || !limbFrontLeg || !limbBackLeg) regrowOne(w);
    }

    public void demonRegen(World w, float dt, float rate) {
        if (!isDemon || dead || timeSinceHurt() < 5f) return;
        float prev = hp;
        hp = Math.min(maxHp, hp + rate * dt);
        if (prev < maxHp * 0.5f && hp >= maxHp * 0.5f) regrowOne(w);
        if (prev >= maxHp * 0.3f || prev < maxHp * 0.3f) {
            if (hp < maxHp * 0.3f) lost30 = false;
        }
        if (hp >= maxHp - 0.01f) regrowAll(w);
    }

    public float timeSinceHurt() { return hurtTimer; }

    public float hurtTimer = 99;
    public float wisteriaT;

    public void chip(World w, float dmg) {
        if (dead || hp <= 0) return;
        hp -= dmg;
        hurtTimer = 0;
        stopRecharge();
        flashT = Math.max(flashT, 0.07f);
        if (hp <= 0) {
            if (this instanceof Player pl && pl.tryNezukoRescue(w)) return;
            hp = 0;
            die(w);
        }
    }

    protected void die(World world) {
        dead = true;
        deathT = 0;
        world.onFighterDeath(this);
    }

    public boolean attacking() { return atkT > 0; }

    public void setRecharging(boolean active) {
        if (!active || dead || stunned()) {
            stopRecharge();
            return;
        }
        if (!recharging) {
            rechargeT = 0;
            rechargeHpTarget = currentRechargeHpCap();
        }
        recharging = true;
        guarding = false;
        guardAnim = 0;
        noteAction();
    }

    public void stopRecharge() {
        recharging = false;
        rechargeT = 0;
        rechargePartT = 0;
        rechargeHpTarget = 0;
    }

    protected void tickRecharge(World world, float dt) {
        if (!recharging) return;
        guarding = false;
        vx = 0;
        float prev = rechargeT;
        rechargeT += dt;
        Color c = abilityColor();
        if (prev < 2f && rechargeT >= 2f) world.parts.ring(x, y - h * 0.35f, c.brighter());
        if (rechargeT < 2f) return;
        float spRate = 110f * (this == world.player ? world.fearSlowdown() : 1f);
        sp = Math.min(maxSp, sp + dt * spRate);
        if (isDemon) hp = Math.min(maxHp, hp + dt * rechargeHpRate());
        else hp = Math.min(rechargeHpCap(), hp + dt * rechargeHpRate());
        rechargePartT -= dt;
        if (rechargePartT <= 0) {
            rechargePartT = 0.035f;
            for (int i = 0; i < 2; i++) {
                float a = Game.time * 8f + i * (float) Math.PI + rechargeT * 1.7f;
                float r = w * (0.75f + i * 0.2f);
                world.parts.spawn(Particles.CIRCLE, x + FMath.cos(a) * r, bottom() - 6,
                        FMath.sin(a) * 65, -FMath.rand(135, 230), 0.52f, 6, c.brighter(), -90, 0.96f);
            }
        }
    }

    protected Color abilityColor() {
        return isDemon ? AbilityCast.BLOOD : new Color(210, 220, 235);
    }

    protected float rechargeHpRate() {
        return isDemon ? 24f : 7f;
    }

    protected float rechargeHpCap() {
        return recharging && rechargeHpTarget > 0 ? rechargeHpTarget : currentRechargeHpCap();
    }

    protected boolean rechargeSatisfied() {
        return sp >= maxSp - 0.5f && hp >= rechargeHpCap() - 0.5f;
    }

    private float currentRechargeHpCap() {
        if (isDemon || hp >= maxHp - 0.01f) return maxHp;
        float q = (float) Math.ceil((hp / maxHp) * 5f - 0.0001f) / 5f;
        return FMath.clamp(q * maxHp, 0, maxHp);
    }

    public void renderRechargeGlow(Graphics2D g) {
        if (!recharging) return;
        Color c = abilityColor();
        float warm = FMath.clamp(rechargeT / 2f, 0, 1);
        float pulse = 0.75f + 0.25f * FMath.sin(Game.time * 8f);
        Glow.blob(g, x, y - h * 0.32f, (34 + 18 * warm) * pulse,
                new Color(c.getRed(), c.getGreen(), c.getBlue(), (int) (70 + 100 * warm)));
        if (rechargeT >= 2f) Glow.blob(g, x, y - h * 0.42f, 16 * pulse, new Color(255, 255, 255, 150));
    }

    public float atkDurStored;

    protected void startAttack(float dur) {
        atkT = dur;
        atkDur = dur;
        atkDurStored = dur;
        comboHitApplied = false;
        noteAction();
    }

    protected void physics(World world, float dt) {
        if (staggerT > 0) staggerT -= dt;
        invulnT -= dt;
        flashT -= dt;
        contactCd -= dt;
        guardAnim = Math.max(0, guardAnim - dt);
        timeSinceAction += dt;
        hurtTimer += dt;
        wisteriaT = Math.max(0, wisteriaT - dt);
        stunT = Math.max(0, stunT - dt);
        limbFlashFA = Math.max(0, limbFlashFA - dt);
        limbFlashBA = Math.max(0, limbFlashBA - dt);
        limbFlashFL = Math.max(0, limbFlashFL - dt);
        limbFlashBL = Math.max(0, limbFlashBL - dt);
        boolean dashing = isDashing();
        if (!dashing) {
            float grav = world.level.underwater ? GRAV * 0.16f : GRAV;
            float maxFall = world.level.underwater ? 360f : MAX_FALL;
            vy += grav * dt * ((vy < 0 && jumpHeld()) ? 0.82f : 1f);
            if (vy > maxFall) vy = maxFall;
        }
        float prevBottom = bottom();
        float nx = x + vx * dt;
        for (Level.Plat p : world.solids) {
            if (p.slope != 0) continue;
            if (nx + w / 2f > p.x && nx - w / 2f < p.x + p.w && y + h / 2f > p.y + 2 && y - h / 2f < p.y + p.h) {
                if (vx > 0) nx = p.x - w / 2f;
                else if (vx < 0) nx = p.x + p.w + w / 2f;
                vx = 0;
            }
        }
        x = FMath.clamp(nx, w / 2f, world.level.w - w / 2f);
        float ny = y + vy * dt;
        boolean wasGround = onGround;
        onGround = false;
        for (Level.Plat p : world.solids) {
            if (p.slope != 0) {
                float sx = FMath.clamp(x, p.x, p.x + p.w);
                float sy = p.slope > 0 ? p.y + p.h * (1 - (sx - p.x) / p.w) : p.y + p.h * ((sx - p.x) / p.w);
                if (x + w / 2f > p.x && x - w / 2f < p.x + p.w && vy >= 0
                        && prevBottom <= sy + Math.max(10, vy * dt + 4) && ny + h / 2f >= sy - 2) {
                    ny = sy - h / 2f;
                    land(world);
                    vy = 0;
                }
                continue;
            }
            if (x + w / 2f > p.x && x - w / 2f < p.x + p.w && ny + h / 2f > p.y && ny - h / 2f < p.y + p.h) {
                if (vy > 0 && prevBottom <= p.y + Math.max(6, vy * dt + 2)) {
                    ny = p.y - h / 2f;
                    land(world);
                    vy = 0;
                } else if (vy < 0) {
                    ny = p.y + p.h + h / 2f;
                    vy = Math.max(vy, 0);
                }
            }
        }
        for (Level.Plat p : world.oneWays) {
            if (dropThroughT > 0) break;
            if (x + w / 2f > p.x && x - w / 2f < p.x + p.w && vy >= 0 && prevBottom <= p.y + 8 && ny + h / 2f >= p.y && ny - h / 2f < p.y + p.h) {
                ny = p.y - h / 2f;
                land(world);
                vy = 0;
            }
        }
        for (Trap tr : world.traps) {
            if (!tr.solidNow()) continue;
            if (x + w / 2f > tr.px && x - w / 2f < tr.px + tr.pw && vy >= 0 && prevBottom <= tr.py + 10 && ny + h / 2f >= tr.py) {
                ny = tr.py - h / 2f;
                land(world);
                vy = 0;
            }
        }
        y = ny;
        if (!wasGround && onGround) {
            for (Level.Plat p : world.solids) {
                if (p.slope != 0) continue;
                if (x + w / 2f > p.x && x - w / 2f < p.x + p.w && Math.abs(bottom() - p.y) < 2) {
                    y = p.y - h / 2f;
                    break;
                }
            }
        }
        touchingWall = probeWall(world);
        if (onGround) dropThroughT = 0;
        dropThroughT -= dt;
        float spd = Math.abs(vx);
        // ~2.2 stride cycles/s at full run: smooth sinusoidal limbs, no jitter
        animPhase += dt * (onGround ? spd * 0.0065f : 1.8f);
        squash = Math.max(0, squash - dt * 5f);
    }

    private void land(World world) {
        if (!onGround && vy > 500) {
            squash = Math.min(1, vy / 1400f);
            world.parts.burst(Particles.CIRCLE, x, bottom(), 6, 90, 0.35f, 7, new Color(200, 200, 190), 60, 0.9f);
            world.cam.shake(Math.min(4, vy / 400f), 0.1f);
        }
        onGround = true;
    }

    protected float dropThroughT;

    public void dropThrough() { dropThroughT = 0.22f; }

    private int probeWall(World world) {
        float pad = 3;
        int dir = 0;
        for (Level.Plat p : world.solids) {
            if (right() + pad > p.x && right() < p.x + p.w && top() < p.y + p.h - 4 && bottom() > p.y + 6 && !onGround)
                dir = 1;
            if (left() - pad < p.x + p.w && left() > p.x && top() < p.y + p.h - 4 && bottom() > p.y + 6 && !onGround)
                dir = -1;
        }
        return dir;
    }

    protected boolean jumpHeld() { return true; }

    protected boolean isDashing() { return false; }

    public void face(float dx) {
        if (dx > 0.01f) facingRight = true;
        else if (dx < -0.01f) facingRight = false;
    }

    public int facing() { return facingRight ? 1 : -1; }

    protected void limb(Graphics2D g, float x1, float y1, float x2, float y2, float wid, Color c) {
        g.setStroke(new BasicStroke(wid, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(c);
        g.drawLine((int) x1, (int) y1, (int) x2, (int) y2);
    }

    /** Katana blade: uniform-thickness black body, spine running straight the
     *  full length while the cutting edge angles into a sharp triangular tip,
     *  with a thin colored line along that cutting edge. `mirror` flips the
     *  edge to the opposite side (for left-facing wielders). */
    protected static void bladeShape(Graphics2D g, float bx, float by, float tx, float ty, float w, Color edge, boolean mirror) {
        float dx = tx - bx, dy = ty - by;
        float m = FMath.dist(0, 0, dx, dy);
        if (m < 2) return;
        float ca = dx / m, sa = dy / m;
        float px = mirror ? sa : -sa;
        float py = mirror ? -ca : ca;
        // body: spine (-perp) straight base->tip; edge (+perp) straight to 82%,
        // then the diagonal into the tip forms the triangular kissaki
        Path2D.Float p = new Path2D.Float();
        p.moveTo(bx + px * w, by + py * w);
        p.lineTo(bx + ca * m * 0.82f + px * w, by + sa * m * 0.82f + py * w);
        p.lineTo(bx + ca * m - px * w, by + sa * m - py * w);
        p.lineTo(bx - px * w, by - py * w);
        p.closePath();
        g.setColor(new Color(14, 14, 18));
        g.fill(p);
        // thin cutting-edge line (style colored) along the edge + tip diagonal
        g.setStroke(new BasicStroke(1.3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(edge);
        g.drawLine((int) (bx + px * w), (int) (by + py * w),
                (int) (bx + ca * m * 0.82f + px * w), (int) (by + sa * m * 0.82f + py * w));
        g.drawLine((int) (bx + ca * m * 0.82f + px * w), (int) (by + sa * m * 0.82f + py * w),
                (int) (bx + ca * m - px * w), (int) (by + sa * m - py * w));
    }

    protected void leg(Graphics2D g, float hx, float hy, float swing, float len, float wid, Color c, boolean present) {
        if (!present) {
            stub(g, hx, hy + len * 0.3f, wid, c);
            return;
        }
        float fx = hx + FMath.sin(swing) * len * 0.62f;
        float fy = hy + len + FMath.cos(swing) * 5;
        limb(g, hx, hy, fx, fy - 4, wid, c);
        g.setColor(c.darker());
        g.fillRect((int) (fx - 3), (int) (fy - 3), 6, 3);
    }

    protected void legClaw(Graphics2D g, float hx, float hy, float swing, float len, float wid, Color c, boolean present) {
        if (!present) {
            stub(g, hx, hy + len * 0.3f, wid, c);
            return;
        }
        float fx = hx + FMath.sin(swing) * len * 0.62f;
        float fy = hy + len + FMath.cos(swing) * 5;
        limb(g, hx, hy, fx, fy - 4, wid, c);
        g.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(c.brighter());
        for (int i = -1; i <= 1; i++)
            g.drawLine((int) fx, (int) (fy - 3), (int) (fx + i * 2), (int) (fy + 1));
    }

    protected void stub(Graphics2D g, float sx, float sy, float wid, Color c) {
        g.setColor(c);
        g.fillRoundRect((int) (sx - wid / 2), (int) sy - 2, (int) wid, 7, 3, 3);
        g.setColor(new Color(160, 30, 45));
        g.fillOval((int) (sx - wid / 2), (int) sy + 2, (int) wid, 3);
    }

    protected void drawGuardArc(Graphics2D g) {
        if (guardAnim <= 0) return;
        float a = Math.min(1, guardAnim / 0.25f);
        Composite old = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, a));
        g.setStroke(new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(new Color(255, 235, 170));
        int cx = (int) (x + facing() * w * 0.65f), cy = (int) (y - h * 0.45f);
        g.drawArc(cx - 26, cy - 26, 52, 52, facingRight ? -70 : 110, 140);
        g.setColor(new Color(255, 255, 255, 160));
        g.setStroke(new BasicStroke(2f));
        g.drawArc(cx - 21, cy - 21, 42, 42, facingRight ? -70 : 110, 140);
        g.setComposite(old);
    }

    protected void swordBlock(Graphics2D g, float sx, float sy, Color sleeve, Color edge, float bladeW, float armW) {
        float hx = sx + facing() * 15, hy = sy - 18;
        limb(g, sx, sy, hx, hy, armW, sleeve);
        double ba = facingRight ? -Math.PI * 0.75 : -Math.PI * 0.25;
        float ca = (float) Math.cos(ba), sa = (float) Math.sin(ba);
        float tx = hx + ca * 6, ty = hy + sa * 6;
        float bx = tx + ca * 58, by = ty + sa * 58;
        g.setStroke(new BasicStroke(4.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(new Color(30, 30, 36));
        g.drawLine((int) (hx - ca * 6), (int) (hy - sa * 6), (int) tx, (int) ty);
        g.setColor(new Color(200, 170, 90));
        g.fillRect((int) tx - 2, (int) ty - 2, 5, 5);
        bladeShape(g, tx, ty, bx, by, bladeW, edge, !facingRight);
    }

    protected void blockHands(Graphics2D g, float shY, Color c, float wid) {
        float fy = shY - 8;
        limb(g, x - facing() * 8, shY + 5, x + facing() * 10, fy, wid, c.darker());
        limb(g, x + facing() * 8, shY + 5, x + facing() * 16, fy + 6, wid, c);
    }

    protected void drawStunStars(Graphics2D g) {
        if (stunT <= 0) return;
        float cy = top() - 12;
        for (int i = 0; i < 3; i++) {
            double a = Game.time * 6 + i * 2.09;
            float sx = x + (float) Math.cos(a) * 14, sy = cy + (float) Math.sin(a) * 5;
            g.setColor(new Color(255, 225, 110));
            star(g, sx, sy, 4);
        }
    }

    static void star(Graphics2D g, float cx, float cy, float r) {
        Polygon p = new Polygon();
        for (int i = 0; i < 10; i++) {
            double an = i * Math.PI / 5 - Math.PI / 2;
            float rr = i % 2 == 0 ? r : r * 0.45f;
            p.addPoint((int) (cx + Math.cos(an) * rr), (int) (cy + Math.sin(an) * rr));
        }
        g.fillPolygon(p);
    }

    protected void drawLimbFlash(Graphics2D g, float flash, float ax, float ay, float r) {
        if (flash <= 0) return;
        Composite old = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.min(1, flash * 2)));
        g.setColor(new Color(255, 190, 205, 180));
        g.fillOval((int) (ax - r), (int) (ay - r), (int) (r * 2), (int) (r * 2));
        g.setColor(Color.WHITE);
        g.fillOval((int) (ax - r * 0.4f), (int) (ay - r * 0.4f), (int) (r * 0.8f), (int) (r * 0.8f));
        g.setComposite(old);
    }
}
