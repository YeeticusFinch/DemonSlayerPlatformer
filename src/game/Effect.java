package game;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Effect {
    public static final int ARC = 0, WAVE = 1, WHEEL = 2, WHIRL = 3, FLUX = 4,
            TIGER = 5, BLOOD_BOLT = 6, CLAW_WAVE = 7, NOVA = 8, SHOCK_GROUND = 9,
            HEAL_AURA = 10, SLAM_RING = 11, FLAME_RIBBON = 12, HAND_SPIKE = 13, WATER_RIBBON = 14,
            TIGER_HEAD = 15, WIND_RIBBON = 16, HAND_SWING = 17, HAND_CHARGE = 18, HAND_AURA = 19,
            SWAMP_CLOUD = 20, SWAMP_HANDS = 21, SWAMP_PUDDLE = 22,
            TEMARI = 23, ARROW = 24, BOULDER = 25;

    public int kind;
    public float x, y, vx, vy, r, life, maxLife, dmg, kbX, kbY, angle, spin, pull, tick;
    public float ex1, ex2, grav;
    public Fighter owner;
    public boolean friendly, followOwner, hitOnce = true, stopOnSolid, dead, demonsOnly;
    public Color c1 = Color.WHITE, c2 = Color.BLUE;

    private int pierceCount = 3;
    private final HashSet<Fighter> hitSet = new HashSet<>();
    private final HashMap<Fighter, Float> nextHitT = new HashMap<>();
    public final ArrayList<float[]> pts = new ArrayList<>();

    public Effect(int kind, Fighter owner, boolean friendly) {
        this.kind = kind;
        this.owner = owner;
        this.friendly = friendly;
    }

    public Effect life(float l) { life = maxLife = l; return this; }

    public Effect at(float x, float y) { this.x = x; this.y = y; return this; }

    public Effect vel(float vx, float vy) { this.vx = vx; this.vy = vy; return this; }

    public Effect radius(float r) { this.r = r; return this; }

    public Effect damage(float dmg, float kbX, float kbY) { this.dmg = dmg; this.kbX = kbX; this.kbY = kbY; return this; }

    public Effect colors(Color c1, Color c2) { this.c1 = c1; this.c2 = c2; return this; }

    public Effect pierce(int n) { this.pierceCount = n + 1; return this; }

    public Effect tickEvery(float t) { this.tick = t; hitOnce = false; return this; }

    public Effect demonsOnly() { demonsOnly = true; return this; }

    public Effect followAt(float ang, float dist) { followOwner = true; angle = ang; ex1 = dist; return this; }

    public void update(World w, float dt) {
        life -= dt;
        if (life <= 0) { dead = true; return; }
        float t = 1f - life / maxLife;

        if (kind == FLAME_RIBBON) {
            if (owner != null && !owner.dead && pts.size() < 90)
                pts.add(new float[]{owner.x + owner.facing() * 16, owner.y - owner.h * 0.22f});
            if (FMath.chance(0.6f))
                w.parts.spawn(Particles.EMBER, x + FMath.rand(-14, 14), y + FMath.rand(-20, 20),
                        FMath.rand(-40, 40), FMath.rand(-80, -10), 0.4f, 7, c2, -60, 0.95f);
            if (!pts.isEmpty()) {
                float[] lastP = pts.get(pts.size() - 1);
                x = lastP[0];
                y = lastP[1];
            }
            return;
        }

        if (kind == WATER_RIBBON) {
            if (owner != null && !owner.dead && pts.size() < 90)
                pts.add(new float[]{owner.x, owner.y - 6});
            if (FMath.chance(0.85f))
                w.parts.spawn(Particles.DROP, x + FMath.rand(-8, 8), y + FMath.rand(-12, 12),
                        (owner != null ? owner.facing() : 1) * FMath.rand(180, 420), FMath.rand(-60, 60),
                        0.35f, 5, c2, 300, 0.97f);
            if (!pts.isEmpty()) {
                float[] lastP = pts.get(pts.size() - 1);
                x = lastP[0];
                y = lastP[1];
            }
            return;
        }

        if (kind == WIND_RIBBON) {
            if (owner != null && !owner.dead && pts.size() < 90)
                pts.add(new float[]{owner.x + owner.facing() * 12, owner.y - owner.h * 0.28f});
            float dir = owner != null ? owner.facing() : (vx < 0 ? -1 : 1);
            if (FMath.chance(0.85f))
                w.parts.spawn(Particles.CIRCLE, x + FMath.rand(-10, 10), y + FMath.rand(-14, 14),
                        -dir * FMath.rand(80, 180), FMath.rand(-90, 70),
                        0.32f, 5, c2, 80, 0.97f);
            if (!pts.isEmpty()) {
                float[] lastP = pts.get(pts.size() - 1);
                x = lastP[0];
                y = lastP[1];
            }
            return;
        }

        if (kind == WHIRL && ex2 == 3 && owner != null && !owner.dead) {
            x = owner.x;
            y = ex1 == 1 ? owner.bottom() : owner.y - owner.h * 0.1f;
        } else if (kind == WHIRL && ex2 == 5) {
            float nx = x + vx * dt, ny = y + vy * dt + FMath.sin((maxLife - life) * 16f) * 18f * dt;
            if (stopOnSolid && w.solidAtPoint(nx, ny)) { x = nx; y = ny; impact(w); return; }
            x = nx;
            y = ny;
        } else if (followOwner && owner != null && !owner.dead) {
            x = owner.x + FMath.cos(angle) * ex1 * owner.facing();
            y = owner.y + FMath.sin(angle) * ex1 * 0.4f;
        }
        if (kind == FLUX && owner != null && !owner.dead) { x = owner.x; y = owner.y - 8; }

        if (pull > 0) {
            if (friendly) {
                for (Fighter tf : w.enemies) pullOne(tf, dt);
            } else if (!w.player.dead) pullOne(w.player, dt);
        }

        if (kind == SHOCK_GROUND) {
            float dir = Math.signum(vx);
            float probeX = x + dir * 26;
            float gy = w.groundYUnder(probeX, y);
            if (gy > 9000 || w.solidAtPoint(probeX + dir * 12, y - 24)) { dead = true; return; }
            y = gy - 12;
            vx *= (float) Math.pow(0.5, dt);
            if (FMath.chance(0.7f))
                w.parts.spawn(Particles.CIRCLE, probeX, gy, FMath.rand(-40, 40), FMath.rand(-240, -90), 0.4f, 9,
                        new Color(125, 95, 70), 520, 0.95f);
        }
        if (kind == HAND_SPIKE) {
            float p = 1f - life / maxLife;
            if (p < 0.18f && FMath.chance(0.8f))
                w.parts.burst(Particles.CIRCLE, x, y, 2, 90, 0.35f, 7, new Color(110, 150, 80), 260, 0.94f);
            if (p > 0.75f && FMath.chance(0.5f))
                w.parts.spawn(Particles.CIRCLE, x + FMath.rand(-8, 8), y - ex1 * 0.4f, FMath.rand(-30, 30), FMath.rand(-60, -10), 0.35f, 6, new Color(140, 180, 100), 200, 0.95f);
        }
        if (kind == HAND_SWING && owner != null && !owner.dead) {
            float dir = vx == 0 ? owner.facing() : Math.signum(vx);
            float p = FMath.easeOut(Math.min(1, t * 1.15f));
            float baseY = owner.y - owner.h * 0.35f;
            x = owner.x + dir * (34 + FMath.sin(p * (float) Math.PI * 0.78f) * ex1);
            y = baseY - 28 + FMath.sin((p - 0.22f) * (float) Math.PI) * 54;
            if (FMath.chance(0.7f))
                w.parts.spawn(Particles.CIRCLE, x + FMath.rand(-10, 10), y + FMath.rand(-8, 8),
                        -dir * FMath.rand(40, 110), FMath.rand(-60, 30), 0.34f, 6, c2, 70, 0.95f);
        }
        if (kind == HAND_AURA && owner != null && !owner.dead) {
            x = owner.x;
            y = owner.y - owner.h * 0.22f;
        }
        if (kind == SWAMP_HANDS) {
            float p = 1f - life / maxLife;
            if (p < 0.25f && FMath.chance(0.7f))
                w.parts.burst(Particles.CIRCLE, x, y, 2, 80, 0.32f, 7, new Color(12, 18, 16), 80, 0.94f);
        }
        if (kind == SWAMP_CLOUD && FMath.chance(0.95f)) {
            w.parts.spawn(Particles.SMOKE, x + FMath.rand(-r, r), y + FMath.rand(-r * 0.55f, r * 0.55f),
                    FMath.rand(-35, 35), FMath.rand(-35, 18), 0.7f, 18, new Color(8, 24, 22), 10, 0.96f);
        }
        if (kind == TEMARI) {
            vy += (grav == 0 ? 880 : grav) * dt;
            for (Level.Plat p : w.solids) {
                float nx = x + vx * dt, ny = y + vy * dt;
                if (nx + r > p.x && nx - r < p.x + p.w && ny + r > p.y && ny - r < p.y + p.h) {
                    if (y + r <= p.y + 8 && vy > 0) { y = p.y - r; vy = -Math.abs(vy) * 0.78f; }
                    else vx = -vx * 0.85f;
                }
            }
            if (FMath.chance(0.45f)) w.parts.spawn(Particles.SPARK, x, y, -vx * 0.05f, -vy * 0.05f, 0.2f, 4, c2, 20, 0.9f);
        }
        if (kind == ARROW) {
            if (ex2 == 7 && (pts.isEmpty() || FMath.dist(pts.get(pts.size() - 1)[0], pts.get(pts.size() - 1)[1], x, y) > 10)) {
                pts.add(new float[]{x, y});
                while (pts.size() > 18) pts.remove(0);
            }
            if (pull > 0 && owner != null) {
                Fighter target = friendly ? nearestEnemy(w) : w.player;
                if (target != null && !target.dead) {
                    float dx = target.x - x, dy = target.y - y;
                    float d = Math.max(1, FMath.dist(0, 0, dx, dy));
                    vx = FMath.approach(vx, dx / d * ex1, 900 * dt);
                    vy = FMath.approach(vy, dy / d * ex1, 900 * dt);
                }
            }
            angle = (float) Math.atan2(vy, vx);
        }
        if (kind == WHIRL && FMath.chance(0.85f)) {
            float a = FMath.rand(0, 6.28f), d = FMath.rand(r * 0.35f, r);
            int type = c1.getGreen() > c1.getBlue() ? Particles.CIRCLE : Particles.DROP;
            float lift = ex2 == 1 || ex2 == 3 ? -80 : 80;
            w.parts.spawn(type, x + FMath.cos(a) * d, y + FMath.sin(a) * d,
                    -FMath.cos(a) * (ex2 == 1 || ex2 == 3 ? 260 : 190), -FMath.sin(a) * 210 + lift, 0.38f, ex2 == 1 || ex2 == 3 ? 6 : 5, c2, 0, 0.96f);
            if (ex2 == 1 && FMath.chance(0.45f))
                w.parts.spawn(Particles.DROP, x + FMath.rand(-r * 0.6f, r * 0.6f), y + r * 0.55f,
                        FMath.rand(-75, 75), -FMath.rand(150, 310), 0.48f, 5, new Color(235, 250, 255), 280, 0.97f);
            if (ex2 == 5 && FMath.chance(0.75f))
                w.parts.spawn(Particles.CIRCLE, x + FMath.rand(-r * 0.9f, r * 0.9f), y + FMath.rand(-r * 0.7f, r * 0.7f),
                        -vx * 0.18f + FMath.rand(-120, 120), FMath.rand(-180, 120), 0.36f, 7,
                        FMath.chance(0.5f) ? new Color(8, 10, 10) : new Color(210, 28, 42), 100, 0.93f);
        }
        if ((kind == TIGER || kind == SLAM_RING) && FMath.chance(0.9f))
            w.parts.spawn(Particles.EMBER, x + FMath.rand(-9, 9), y + FMath.rand(-9, 9), FMath.rand(-40, 40),
                    FMath.rand(-70, 10), 0.45f, 8, new Color(255, 150, 50), -60, 0.95f);
        if (kind == TIGER_HEAD && FMath.chance(0.95f))
            w.parts.spawn(Particles.FIRE, x + FMath.rand(-10, 10), y + FMath.rand(-10, 10),
                    -vx * 0.12f, FMath.rand(-50, 30), 0.45f, 9, new Color(255, 140, 40), -30, 0.95f);
        if (kind == WAVE) {
            if (FMath.chance(0.9f))
                w.parts.spawn(Particles.DROP, x, y + FMath.rand(-16, 16), FMath.rand(-70, 70), FMath.rand(-60, 60), 0.32f, 5, new Color(160, 215, 255), 320, 0.98f);
            if (FMath.chance(0.4f))
                w.parts.spawn(Particles.DROP, x, y + FMath.rand(-12, 12), -vx * 0.15f + FMath.rand(-40, 40), FMath.rand(-140, -40), 0.4f, 4, new Color(210, 240, 255), 380, 0.98f);
        }
        if (kind == WHEEL && FMath.chance(0.95f)) {
            float a = FMath.rand(0, 6.28f);
            w.parts.spawn(Particles.DROP, x + FMath.cos(a) * r, y + FMath.sin(a) * r,
                    FMath.cos(a) * 120 + vx * 0.2f, FMath.sin(a) * 120, 0.3f, 4, new Color(170, 220, 255), 260, 0.97f);
        }
        if (kind == HEAL_AURA && FMath.chance(0.6f)) {
            Color pc = ex2 == 6 ? (FMath.chance(0.5f) ? c1 : c2) : new Color(120, 255, 160);
            w.parts.spawn(Particles.CIRCLE, owner.x + FMath.rand(-16, 16), owner.y + FMath.rand(-10, 30),
                    FMath.rand(-15, 15), FMath.rand(-120, -50), 0.6f, 6, pc, ex2 == 6 ? -120 : -40, 0.98f);
        }

        if (grav != 0 && kind != TEMARI) vy += grav * dt;
        if (!followOwner && kind != FLUX && kind != WHIRL && kind != HEAL_AURA && kind != HAND_SPIKE
                && kind != HAND_SWING && kind != HAND_CHARGE && kind != HAND_AURA
                && kind != SWAMP_CLOUD && kind != SWAMP_HANDS && kind != SWAMP_PUDDLE) {
            float nx = x + vx * dt, ny = y + vy * dt;
            if (stopOnSolid && w.solidAtPoint(nx, ny)) { x = nx; y = ny; impact(w); return; }
            x = nx;
            y = ny;
        }
        if (kind == TIGER) angle += spin * dt;
        if (dmg > 0) tryHits(w);
    }

    private void pullOne(Fighter tf, float dt) {
        if (tf.dead) return;
        if (ex2 == 3) {
            tornadoOne(tf, dt);
            return;
        }
        float tx = x;
        float ty = y;
        float dx = tx - tf.x, dy = ty - tf.y;
        float d = FMath.dist(0, 0, dx, dy);
        if (d < r * 1.3f && d > 14) {
            float strength = ex2 == 1 ? 1500f : 1000f;
            float mult = ex2 == 1 ? pull : 1f;
            tf.vx += dx / d * strength * mult * dt;
            tf.vy += dy / d * (ex2 == 1 ? 760f : 480f) * mult * dt;
        }
    }

    private void tornadoOne(Fighter tf, float dt) {
        float dx = tf.x - x;
        float grow = FMath.clamp((maxLife - life) / 0.5f, 0, 1);
        float bottomY = y + r * 0.08f;
        float topY = y - r * 2.25f * grow;
        if (grow <= 0.05f || Math.abs(dx) > r * 1.15f || tf.bottom() < topY || tf.top() > bottomY) return;
        float vertical = FMath.clamp((tf.y - topY) / Math.max(1, bottomY - topY), 0, 1);
        float targetY = y - r * 1.22f + FMath.sin(life * 11f + tf.x * 0.03f) * r * 0.15f;
        float side = dx >= 0 ? 1f : -1f;
        float swirl = 640f + (1f - vertical) * 260f;
        tf.vx += (-dx * 5.2f + side * swirl) * dt;
        tf.vy += ((targetY - tf.y) * 5.6f - 360f) * dt;
        tf.onGround = false;
    }

    private Fighter nearestEnemy(World w) {
        Fighter best = null;
        float bd = 999999;
        for (Fighter f : w.enemies) if (!f.dead) {
            float d = FMath.dist(x, y, f.x, f.y);
            if (d < bd) { bd = d; best = f; }
        }
        return best;
    }

    private void tryHits(World w) {
        if (friendly) {
            for (Fighter f : w.enemies) tryHitOne(w, f);
        } else {
            tryHitOne(w, w.player);
        }
    }

    private void tryHitOne(World w, Fighter f) {
        if (f == null || f.dead) return;
        if (demonsOnly && !f.isDemon) return;
        if (hitOnce && hitSet.contains(f)) return;
        Float nt = nextHitT.get(f);
        if (nt != null && w.time < nt) return;
        float dx = f.x - x, dy = f.y - y;
        if (kind == WHIRL && ex2 == 3) {
            float grow = FMath.clamp((maxLife - life) / 0.5f, 0, 1);
            float bottomY = y + r * 0.08f;
            float topY = y - r * 2.25f * grow;
            if (grow <= 0.05f || Math.abs(dx) > r * 1.15f || f.bottom() < topY || f.top() > bottomY) return;
        } else {
        float rr = r + Math.max(f.w, f.h) * 0.42f;
        if (dx * dx + dy * dy > rr * rr) return;
        }
        float dir = dx == 0 ? (owner != null ? owner.facing() : 1) : Math.signum(dx);
        if (kind == SHOCK_GROUND) dir = Math.signum(vx);
        float hitDmg = dmg * (owner == null ? 1f : owner.outgoingDamageMult());
        f.hurt(w, owner, hitDmg, dir == 0 ? 1 : dir, kbX, kbY);
        w.impactFeedback(f.x, f.y - f.h * 0.4f, c2);
        if (owner != null) owner.dealtHit();
        if (tick > 0) nextHitT.put(f, w.time + tick);
        else {
            hitSet.add(f);
            pierceCount--;
            if (pierceCount <= 0) impact(w);
        }
    }

    private void impact(World w) {
        if (kind == BLOOD_BOLT && ex2 == 6 && owner != null) {
            AbilityCast.combustibleVortexGround(w, owner, x, y, r * 4.9f, 1.0f, demonsOnly);
        }
        dead = true;
        w.parts.burst(Particles.DROP, x, y, 10, 240, 0.3f, 6, c1, 200, 0.94f);
    }

    public void render(Graphics2D g) { EffectArt.render(g, this); }
}
