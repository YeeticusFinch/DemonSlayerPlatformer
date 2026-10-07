package game;

import java.awt.*;

public abstract class Enemy extends Fighter {
    protected float aggroR = 560, atkRange = 64, windup = 0.45f, recover = 0.5f, atkCdBase = 1.4f;
    protected float atkCd, stateT, dmg = 8;
    protected int state;
    protected float patrolDir = 1, patrolT, aiTime, hopT;
    protected float showHpT;
    protected float guardCd, guardHoldT;
    protected float rechargeCd;
    /** Elites (breathing-form slayers, blood-art demons) can dash, jump and double jump. */
    protected boolean elite;
    protected Color eliteColor = Color.WHITE;
    private float eliteDashCd, eliteJumpCd, eliteAirJumps;
    /** > 0 while an elite dash is in progress ( subclasses check this to skip normal movement ). */
    protected float eliteDashT;

    protected Enemy(float w, float h) {
        super(w, h);
        this.isBoss = false;
    }

    @Override
    public void update(World w, float dt) {
        atkCd -= dt;
        showHpT -= dt;
        guardCd -= dt;
        rechargeCd -= dt;
        aiTime += dt;
        if (guardHoldT > 0) {
            guardHoldT -= dt;
            if (guardHoldT <= 0) guarding = false;
        }
        if (dead) {
            deathT += dt;
            return;
        }
        Player pl = w.player;
        float dx = pl.x - x, dist = Math.abs(dx);
        if (recharging) {
            if (dead || stunned() || dist < 190 || rechargeSatisfied()) {
                stopRecharge();
                rechargeCd = 2f;
            } else {
                setRecharging(true);
                tickRecharge(w, dt);
                physics(w, dt);
                return;
            }
        } else if (!dead && !stunned() && rechargeCd <= 0 && dist > 360 && (hp < rechargeHpCap() - 0.5f || sp < maxSp * 0.65f)
                && FMath.chance(dt * 0.45f)) {
            setRecharging(true);
            tickRecharge(w, dt);
            physics(w, dt);
            return;
        }
        if (!stunned()) tryGuard(w, pl);
        if (guarding) {
            vx = FMath.approach(vx, 0, 2600 * dt);
            physics(w, dt);
            return;
        }
        if (seekShadeIfNeeded(w, dt)) return;
        if (stunned()) {
            guarding = false;
            vx = FMath.approach(vx, 0, 2200 * dt);
        } else if (!pl.dead && staggerT <= 0) {
            think(w, dt, dx, dist);
        } else {
            vx = FMath.approach(vx, 0, 2200 * dt);
        }
        if (isDemon && legsMissing() == 1 && onGround && Math.abs(vx) > 30 && hopT <= 0) {
            vy = -225;
            hopT = 0.55f;
        }
        physics(w, dt);
        if (!isDemon || dead || pl.dead || !canMelee()) return;
        contactCd -= dt;
        if (contactDamage() > 0 && overlaps(pl) && contactCd <= 0 && pl.invulnT <= 0) {
            contactCd = 0.9f * armCdMult();
            pl.hurt(w, this, contactDamage(), Math.signum(pl.x - x) == 0 ? facing() : Math.signum(pl.x - x), 190, 140);
            dealtHit();
        }
    }

    @Override
    public boolean hurt(World world, Fighter src, float dmgAmt, float dir, float kbX, float kbY) {
        boolean r = super.hurt(world, src, dmgAmt, dir, kbX, kbY);
        if (r) {
            showHpT = 3.5f;
            if (!isBoss && (state == 2 || state == 3)) {
                state = 0;
                atkCd = Math.max(atkCd, 0.4f);
            }
        }
        return r;
    }

    protected float contactDamage() { return 0; }

    protected void think(World w, float dt, float dx, float dist) {
        face(dx != 0 ? dx : vx);
        switch (state) {
            case 0 -> {
                vx = FMath.approach(vx, 0, 1600 * dt);
                if (dist < atkRange && atkCd <= 0 && canMelee()) beginWindup();
                else if (dist < aggroR) state = 1;
                else patrol(w, dt);
            }
            case 1 -> {
                float dir = Math.signum(dx == 0 ? 1 : dx);
                if (elite) eliteMove(w, dt, dx, dist);
                if (!elite || eliteDashT <= 0) {
                    if (pathBlocked(w, dir)) vx = FMath.approach(vx, 0, 2100 * dt);
                    else vx = FMath.approach(vx, dir * chaseSpeed(), 2100 * dt);
                }
                if (touchingWall != 0 && onGround && !legBlocked()) vy = -690;
                if (dist <= atkRange && atkCd <= 0 && canMelee()) beginWindup();
                else if (dist > aggroR * 1.25f) state = 0;
            }
            case 2 -> {
                vx = FMath.approach(vx, 0, 2600 * dt);
                if (stateT >= windup) {
                    state = 3;
                    stateT = 0;
                    swingSide *= -1;
                    strike(w);
                }
            }
            case 3 -> {
                if (stateT >= recover) {
                    state = 0;
                    atkCd = atkCdBase * armCdMult();
                }
            }
        }
        stateT += dt;
    }

    protected void beginWindup() { state = 2; stateT = 0; }

    protected float chaseSpeed() { return runSpeed; }

    private void patrol(World wo, float dt) {
        patrolT -= dt;
        if (patrolT <= 0 || pathBlocked(wo, patrolDir)) {
            patrolDir *= -1;
            patrolT = FMath.rand(1.4f, 3f);
        }
        face(patrolDir);
        vx = FMath.approach(vx, patrolDir * runSpeed * 0.32f, 900 * dt);
    }

    /** True if stepping in `dir` would walk off a ledge or deeper into wisteria. */
    private boolean pathBlocked(World wo, float dir) {
        if (dir == 0) return false;
        float probeX = x + dir * (w / 2f + 14);
        float gy = wo.groundYUnder(probeX, bottom() + 2);
        if (gy > 9000 || gy - bottom() > 170) return true;
        if (isDemon && wo.level.daytime && wo.skyCovered(x, top()) && !wo.skyCovered(probeX, top())) return true;
        if (isDemon && !isBoss) {
            float nx = x + dir * 26;
            for (Level.Deco d : wo.level.decos) {
                if (d.type != Level.WISTERIA) continue;
                if (bottom() <= d.y - 230 * d.s || top() >= d.y + 25 * d.s) continue;
                float halfW = 130 * d.s;
                boolean inNow = Math.abs(x - d.x) < halfW;
                boolean inNext = Math.abs(nx - d.x) < halfW;
                if (inNext && (!inNow || Math.abs(nx - d.x) < Math.abs(x - d.x))) return true;
            }
        }
        return false;
    }

    private boolean seekShadeIfNeeded(World wo, float dt) {
        if (!isDemon || !wo.level.daytime || wo.skyCovered(x, top())) return false;
        float dir = shadeDir(wo);
        face(dir);
        vx = FMath.approach(vx, dir * runSpeed * 1.1f, 2200 * dt);
        if (dir == 0) vx = FMath.approach(vx, 0, 2200 * dt);
        physics(wo, dt);
        return true;
    }

    private float shadeDir(World wo) {
        float best = 99999, bestDir = 0;
        for (int i = 1; i <= 14; i++) {
            float step = i * 55f;
            bestDir = shadeProbe(wo, x - step, -1, step, best, bestDir);
            if (bestDir != 0) best = Math.min(best, step);
            bestDir = shadeProbe(wo, x + step, 1, step, best, bestDir);
            if (bestDir != 0 && step <= best) best = step;
        }
        return bestDir;
    }

    private float shadeProbe(World wo, float px, float dir, float dist, float best, float bestDir) {
        if (dist >= best || px < w / 2f || px > wo.level.w - w / 2f) return bestDir;
        float gy = wo.groundYUnder(px, bottom() + 2);
        if (gy > 9000 || Math.abs(gy - bottom()) > 190) return bestDir;
        return wo.skyCovered(px, top()) ? dir : bestDir;
    }

    /** Elite movement: gap-closing dash with afterimages, plus jump/double-jump
     *  toward a player above. Call from chase logic each tick. */
    protected void eliteMove(World w, float dt, float dx, float dist) {
        Player pl = w.player;
        float dir = Math.signum(dx == 0 ? 1 : dx);
        eliteDashCd -= dt;
        eliteJumpCd -= dt;
        if (eliteDashT > 0) {
            eliteDashT -= dt;
            vx = dir * 840;
            if (FMath.chance(0.45f)) w.ghosts.add(new Ghost(x, bottom(), h, eliteColor));
            return;
        }
        if (onGround) eliteAirJumps = 1;
        if (eliteJumpCd <= 0 && pl.y < y - 50 && Math.abs(dx) < 320 && !legBlocked()) {
            if (onGround) {
                vy = -760;
                eliteJumpCd = 0.55f;
            } else if (eliteAirJumps > 0 && vy > -150) {
                vy = -700;
                eliteAirJumps--;
                eliteJumpCd = 0.55f;
                w.parts.ring(x, y + h * 0.35f, new Color(255, 255, 255, 110));
            }
        }
        if (eliteDashCd <= 0 && dist > 170 && dist < 540 && Math.abs(pl.y - y) < 130
                && !pathBlocked(w, dir)) {
            eliteDashT = 0.2f;
            eliteDashCd = FMath.rand(2.2f, 3.4f);
            vx = dir * 840;
        }
    }

    protected void tryGuard(World w, Player pl) {
        if (guardCd > 0 || guarding || pl == null || pl.dead || isBoss && FMath.chance(0.999f)) {
            if (isBoss && guardCd <= 0 && pl != null && pl.attacking() && Math.abs(pl.x - x) < 170 && hp < maxHp * 0.7f) {
                guarding = true;
                guardHoldT = 0.55f;
                guardCd = 2.6f;
            }
            return;
        }
        if (pl.attacking() && Math.abs(pl.x - x) < 150 && hp < maxHp * 0.85f) {
            guarding = true;
            guardHoldT = 0.6f;
            guardCd = 2.8f;
        }
    }

    protected abstract void strike(World w);

    protected void basicStrike(World w, float reach, float kb, float up, double angDeg, Color c) {
        vx += facing() * 130;
        w.meleeStrike(this, reach, dmg, kb, up, Math.toRadians(facingRight ? angDeg : 180 - angDeg), Math.toRadians(150), c);
    }

    public void renderGlow(Graphics2D g) {
        renderRechargeGlow(g);
    }

    public final void render(Graphics2D g) {
        if (dead) return;
        renderBody(g);
        drawGuardArc(g);
        drawStunStars(g);
        miniBar(g);
    }

    protected abstract void renderBody(Graphics2D g);

    protected void miniBar(Graphics2D g) {
        if (showHpT <= 0 || dead || isBoss) return;
        float bw = 44, bx = x - bw / 2, by = top() - 12;
        g.setColor(new Color(10, 10, 14, 160));
        g.fillRoundRect((int) bx - 1, (int) by - 1, (int) bw + 2, 6, 3, 3);
        g.setColor(new Color(225, 60, 70));
        g.fillRect((int) bx, (int) by, (int) (bw * FMath.clamp(hp / maxHp, 0, 1)), 4);
    }
}
