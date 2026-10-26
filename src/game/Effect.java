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
            TEMARI = 23, ARROW = 24, BOULDER = 25, THREAD = 26, FLAME_DRAGON = 27,
            ENMU_WHISPER = 28, ENMU_WHISPER_ORB = 29, ENMU_NIGHTMARE = 30, ENMU_WHIP = 31,
            AKAZA_SHOCKWAVE = 32, AKAZA_COMPASS = 33, AKAZA_PILLAR = 34, AKAZA_CLONE = 35;

    public int kind;
    public float x, y, vx, vy, r, life, maxLife, dmg, kbX, kbY, angle, spin, pull, tick;
    public float warmup, warmupTotal, activeDuration, sleepOnHit;
    public float ex1, ex2, ex3, grav;
    public Fighter owner;
    public Fighter latched;
    public boolean friendly, followOwner, hitOnce = true, stopOnSolid, dead, demonsOnly, unbreakable, severed;
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
        if (life <= 0) {
            if (kind == FLAME_DRAGON) flameDragonImpact(w);
            else if (kind == ENMU_WHISPER_ORB) whisperOrbBurst(w);
            else dead = true;
            return;
        }
        float t = 1f - life / maxLife;

        if (kind == ENMU_NIGHTMARE) {
            if (ex2 == 5) {
                if (owner != null && !owner.dead && followOwner && !pts.isEmpty()) {
                    float[] eye = pts.get(0);
                    x = owner.x + eye[0] * owner.facing();
                    y = owner.y + eye[1];
                }
                warmup = Math.max(0, warmup - dt);
                angle = grav + (maxLife - life) * spin;
            } else if (ex2 == 0) {
                warmup = Math.max(0, warmup - dt);
                if (warmup <= 0) {
                    ex2 = 1;
                    vy = 90;
                }
            } else if (ex2 == 1) {
                vy += 1900 * dt;
                y += vy * dt;
                float groundY = w.groundYUnder(x, y + r + 80);
                if (groundY < 9000 && y + r >= groundY) {
                    y = groundY - r;
                    vy = -620;
                    ex2 = 2;
                    hitSet.clear();
                }
            } else if (ex2 == 2) {
                vy += 1900 * dt;
                y += vy * dt;
                float groundY = w.groundYUnder(x, y + r + 80);
                if (groundY < 9000 && vy >= 0 && y + r >= groundY) {
                    y = groundY - r;
                    vy = 0;
                    ex2 = 3;
                    warmup = 0.42f;
                    life = Math.min(life, 0.82f);
                    hitSet.clear();
                    nightmareShockwave(w, groundY, 158, 48);
                    w.cam.shake(8, 0.28f);
                }
            } else if (ex2 == 3) {
                warmup = Math.max(0, warmup - dt);
                if (warmup <= 0) dead = true;
            }
        }

        if (kind == AKAZA_PILLAR) warmup = Math.max(0, warmup - dt);
        if (kind == AKAZA_CLONE) {
            if (owner == null || owner.dead) { dead = true; return; }
            activeDuration = Math.max(0, activeDuration - dt);
            float orbit = grav + (maxLife - life) * spin;
            angle = orbit;
            x = owner.x + FMath.cos(orbit) * ex1 * owner.facing();
            y = owner.y + FMath.sin(orbit) * ex1 * 0.52f - owner.h * 0.2f;
            warmup -= dt;
            if (warmup <= 0) {
                AbilityCast.akazaCloneStrike(w, this);
                warmup = FMath.rand(0.28f, 0.46f);
            }
        }

        if (kind == THREAD) {
            if (ex2 == 6 || ex2 == 8) {
                warmup = Math.max(0, warmup - dt);
                if (warmup > 0) r = ex1;
                else {
                    float activeElapsed = Math.max(0, maxLife - life - warmupTotal);
                    float activeT = activeDuration > 0 ? FMath.clamp(activeElapsed / activeDuration, 0, 1) : 1;
                    r = Math.max(30, ex1 * (1 - activeT * 0.88f));
                }
                if (warmup <= 0 && FMath.chance(0.75f)) {
                    float a = FMath.rand(0, 6.28f);
                    w.parts.spawn(Particles.CIRCLE, x + FMath.cos(a) * r, y + FMath.sin(a) * r * (ex2 == 8 ? 1 : 0.9f),
                            FMath.rand(-60, 60), FMath.rand(-60, 60), 0.3f, 4, c2, 30, 0.95f);
                }
            } else if (ex2 == 7 && owner != null && pts.size() >= 1) {
                float el = maxLife - life;
                float[] a0 = pts.get(0);
                float mx = a0[0] + FMath.cos(grav) * ex1, my = a0[1] + FMath.sin(grav) * ex1;
                if (el < tick) {
                    x += vx * dt;
                    y += vy * dt;
                } else if (el < tick + 0.4f) {
                    vx = vy = 0;
                } else {
                    vx = vy = 0;
                    if (pts.size() < 2) pts.add(new float[]{x, y});
                    if (pts.size() >= 2) {
                        float k = FMath.easeIn(FMath.clamp((el - tick - 0.4f) / 0.15f, 0, 1));
                        float[] s0 = pts.get(1);
                        x = FMath.lerp(s0[0], mx, k);
                        y = FMath.lerp(s0[1], my, k);
                    }
                }
            } else if (ex2 == 1 || ex2 == 3 || ex2 == 5 || ex2 == 9 || (ex2 == 4 && latched == null)) {
                x += vx * dt;
                y += vy * dt;
            }
            if (latched != null && latched.dead) dead = true;
        }

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
        } else if (followOwner && owner != null && !owner.dead && kind != ENMU_NIGHTMARE) {
            x = owner.x + FMath.cos(angle) * ex1 * owner.facing();
            y = owner.y + FMath.sin(angle) * ex1 * 0.4f;
        }
        if (kind == FLUX && owner != null && !owner.dead) { x = owner.x; y = owner.y - 8; }

        if (pull > 0 && !(kind == THREAD && (ex2 == 6 || ex2 == 8) && warmup > 0)) {
            for (Fighter tf : targets(w)) pullOne(tf, dt);
        }

        if (kind == SHOCK_GROUND) {
            float dir = Math.signum(vx);
            float probeX = x + dir * 26;
            float gy = w.groundYUnder(probeX, y);
            if (gy > 9000 || w.solidAtPoint(probeX + dir * 12, y - 24)) { dead = true; return; }
            y = gy - 12;
            vx *= (float) Math.pow(0.5, dt);
            if (FMath.chance(0.7f))
                w.parts.spawn(ex2 == 6 ? Particles.FIRE : Particles.CIRCLE, probeX, gy, FMath.rand(-40, 40), FMath.rand(-240, -90), 0.4f, ex2 == 6 ? 12 : 9,
                        ex2 == 6 ? (FMath.chance(0.5f) ? c1 : c2) : new Color(125, 95, 70), ex2 == 6 ? -130 : 520, 0.95f);
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
                Fighter target = w.arenaMode ? w.targetFor(owner) : friendly ? nearestEnemy(w) : w.player;
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
        if (!followOwner && kind != FLUX && kind != WHIRL && kind != HEAL_AURA && kind != HAND_SPIKE && kind != ENMU_NIGHTMARE
                && kind != HAND_SWING && kind != HAND_CHARGE && kind != HAND_AURA
                && kind != SWAMP_CLOUD && kind != SWAMP_HANDS && kind != SWAMP_PUDDLE && kind != THREAD) {
            float nx = x + vx * dt, ny = y + vy * dt;
            if (stopOnSolid && w.solidAtPoint(nx, ny)) { x = nx; y = ny; impact(w); return; }
            x = nx;
            y = ny;
        }
        if (kind == TIGER) angle += spin * dt;
        if (kind != THREAD && dmg > 0 && owner != null) w.severWhiteThreadBoundaries(this);
        if ((dmg > 0 || kind == ENMU_NIGHTMARE && ex2 == 5)
                && !(kind == THREAD && (ex2 == 6 || ex2 == 8) && warmup > 0)) tryHits(w);
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
        if (w.arenaMode) return w.targetFor(owner);
        Fighter best = null;
        float bd = 999999;
        for (Fighter f : w.enemies) if (!f.dead) {
            float d = FMath.dist(x, y, f.x, f.y);
            if (d < bd) { bd = d; best = f; }
        }
        return best;
    }

    private java.util.List<Fighter> targets(World w) {
        if (kind == ENMU_NIGHTMARE && ex2 == 5) {
            java.util.ArrayList<Fighter> all = new java.util.ArrayList<>(w.enemies);
            if (w.player != null && !all.contains(w.player)) all.add(w.player);
            all.remove(owner);
            return all;
        }
        if (owner == null && !w.arenaMode)
            return friendly ? w.enemies : java.util.List.of(w.player);
        return w.attackTargets(owner);
    }

    private void tryHits(World w) {
        for (Fighter f : targets(w)) tryHitOne(w, f);
    }

    private boolean threadOverlaps(Fighter f, float dx, float dy) {
        float fw = f.w * 0.5f, fh = f.h * 0.5f;
        if (ex2 == 5 || ex2 == 9) {
            if (ex2 == 9 && Math.abs(dy) < spin + fh) return false;
            return Math.abs(dx) < 20 + fw && Math.abs(dy) < ex1 + fh;
        }
        if (ex2 == 6) {
            float adx = Math.abs(dx), ady = Math.abs(dy);
            if (Math.max(adx - fw, ady - fh) > r) return false;
            boolean hitX = adx >= r - 30 - fw && adx <= r + fw;
            boolean hitY = ady >= r - 30 - fh && ady <= r + fh;
            return (hitX && ady <= r + fh) || (hitY && adx <= r + fw);
        }
        if (ex2 == 8) {
            float d = FMath.dist(0, 0, dx, dy);
            return d >= r - 34 - fw && d <= r + 26 + fw;
        }
        if (ex2 == 4) return FMath.dist(0, 0, dx, dy) < 30 + Math.max(f.w, f.h) * 0.4f;
        if (ex2 == 7) return false;
        if (angle == 0) return Math.abs(dx) < 18 + fw && Math.abs(dy) < ex1 + fh;
        float d = FMath.dist(0, 0, dx, dy);
        return d < ex1 * 0.85f + 20 + Math.max(f.w, f.h) * 0.4f;
    }

    private void tryHitOne(World w, Fighter f) {
        if (f == null || f.dead) return;
        if (demonsOnly && !f.isDemon) return;
        if (kind == THREAD && ex2 == 4 && latched != null) return;
        if (hitOnce && hitSet.contains(f)) return;
        Float nt = nextHitT.get(f);
        if (nt != null && w.time < nt) return;
        float dx = f.x - x, dy = f.y - y;
        if (kind == ENMU_NIGHTMARE && ex2 == 5) {
            if (warmup > 0) return;
            float ux = FMath.cos(angle), uy = FMath.sin(angle);
            float depth = dx * ux + dy * uy;
            float side = -dx * uy + dy * ux;
            float bodyRadius = Math.max(f.w, f.h) * 0.28f;
            if (depth + bodyRadius <= 0 || depth - bodyRadius >= ex1
                    || Math.abs(side) > 14 + Math.max(0, depth) * 0.22f + bodyRadius * 0.45f) return;
            boolean slept = f.trySleep(sleepOnHit > 0 ? sleepOnHit : 4.5f);
            if (slept) {
                f.vx = f.vy = 0;
                f.guarding = false;
            }
            if (tick > 0) nextHitT.put(f, w.time + tick);
            if (slept) w.parts.ring(f.x, f.y - f.h * 0.5f, new Color(135, 190, 255));
            return;
        }
        if (kind == ENMU_NIGHTMARE) {
            if (ex2 == 0) {
                float depth = f.y - y;
                if (depth > 0 && depth < ex1 && Math.abs(dx) < 14 + depth * 0.22f) {
                    if (f.trySleep(4.5f)) {
                        f.vx = f.vy = 0;
                        f.guarding = false;
                    }
                    hitSet.add(f);
                    w.parts.ring(f.x, f.y - f.h * 0.5f, new Color(135, 190, 255));
                }
                return;
            }
            if (ex2 >= 1 && ex2 <= 3) {
                float reach = r + Math.max(f.w, f.h) * 0.42f;
                if (dx * dx + dy * dy > reach * reach) return;
                float dir = dx == 0 ? (owner == null ? 1 : owner.facing()) : Math.signum(dx);
                float hitDmg = dmg * (owner == null ? 1f : owner.outgoingDamageMult());
                boolean landed = f.hurt(w, owner, hitDmg, dir, kbX, kbY);
                if (landed) w.impactFeedback(f.x, f.y - f.h * 0.4f, c2);
                if (landed) {
                    hitSet.add(f);
                    ex2 = 4;
                    life = Math.min(life, 0.34f);
                    float groundY = w.groundYUnder(x, y + r + 80);
                    if (groundY > 9000) groundY = y + r;
                    nightmareShockwave(w, groundY, 220, 95);
                    w.cam.shake(11, 0.34f);
                }
                return;
            }
            return;
        }
        if (kind == ENMU_WHIP) {
            if (pts.isEmpty() || pointSegmentDistance(f.x, f.y, pts.get(0)[0], pts.get(0)[1], x, y)
                    > r + Math.max(f.w, f.h) * 0.28f) return;
        }
        if (kind == AKAZA_PILLAR) {
            if (warmup > 0 || Math.abs(dx) > r + f.w * 0.5f || f.bottom() < y - ex1 || f.top() > y) return;
        } else if (kind == WHIRL && ex2 == 3) {
            float grow = FMath.clamp((maxLife - life) / 0.5f, 0, 1);
            float bottomY = y + r * 0.08f;
            float topY = y - r * 2.25f * grow;
            if (grow <= 0.05f || Math.abs(dx) > r * 1.15f || f.bottom() < topY || f.top() > bottomY) return;
        } else if (kind == THREAD) {
            if (!threadOverlaps(f, dx, dy)) return;
        } else {
        float rr = r + Math.max(f.w, f.h) * 0.42f;
        if (dx * dx + dy * dy > rr * rr) return;
        }
        float dir = dx == 0 ? (owner != null ? owner.facing() : 1) : Math.signum(dx);
        if (kind == SHOCK_GROUND) dir = Math.signum(vx);
        float hitDmg = dmg * (owner == null ? 1f : owner.outgoingDamageMult());
        boolean landed = f.hurt(w, owner, hitDmg, dir == 0 ? 1 : dir, kbX, kbY);
        boolean deflected = false;
        if (f.compassNeedleT > 0 && owner != f && !unbreakable
                && vx * vx + vy * vy > 140 * 140) {
            deflected = true;
            owner = f;
            friendly = f.team == 0;
            vx = -vx;
            vy = -vy;
            x = f.x + Math.signum(vx) * (f.w * 0.5f + r + 5);
            y = f.y - f.h * 0.25f;
            hitSet.clear();
            hitSet.add(f);
            nextHitT.clear();
            pierceCount = Math.max(2, pierceCount);
            w.parts.ring(f.x, f.y - f.h * 0.35f, new Color(128, 207, 255));
        }
        if (landed && sleepOnHit > 0) {
            f.trySleep(sleepOnHit);
            f.vx = f.vy = 0;
            f.guarding = false;
        }
        if (landed) w.impactFeedback(f.x, f.y - f.h * 0.4f, c2);
        if (kind == AKAZA_SHOCKWAVE && ex2 == 1 && landed && !deflected) {
            if (owner != null) owner.dealtHit();
            akazaShockwaveBurst(w);
            return;
        }
        if (kind == ENMU_WHISPER_ORB && landed) {
            whisperOrbBurst(w);
            if (owner != null) owner.dealtHit();
            return;
        }
        if (kind == FLAME_DRAGON && landed) {
            flameDragonImpact(w);
            if (owner != null) owner.dealtHit();
            return;
        }
        if (kind == THREAD && ex2 == 4 && latched == null) {
            latched = f;
            vx = vy = 0;
            life = maxLife = 0.7f;
        }
        if (owner != null) owner.dealtHit();
        if (tick > 0) nextHitT.put(f, w.time + tick);
        else {
            hitSet.add(f);
            pierceCount--;
            if (pierceCount <= 0) impact(w);
        }
    }

    private void impact(World w) {
        if (kind == FLAME_DRAGON) {
            flameDragonImpact(w);
            return;
        }
        if (kind == ENMU_WHISPER_ORB) {
            whisperOrbBurst(w);
            return;
        }
        if (kind == AKAZA_SHOCKWAVE && ex2 == 1) {
            akazaShockwaveBurst(w);
            return;
        }
        if (kind == BLOOD_BOLT && ex2 == 6 && owner != null) {
            AbilityCast.combustibleVortexGround(w, owner, x, y, r * 4.9f, 1.0f, demonsOnly, ex1 == 0 ? 1.0f : ex1, 1.0f);
        }
        dead = true;
        w.parts.burst(Particles.DROP, x, y, 10, 240, 0.3f, 6, c1, 200, 0.94f);
    }

    private void akazaShockwaveBurst(World w) {
        if (ex2 == 2) return;
        ex2 = 2;
        dead = true;
        float blastRadius = ex1 > 0 ? ex1 : 92;
        float blastDamage = ex3 > 0 ? ex3 : dmg * 1.5f;
        Effect flash = new Effect(NOVA, owner, friendly).at(x, y).radius(blastRadius * 0.72f).life(0.24f)
                .colors(c1, c2);
        w.effects.add(flash);
        Effect ring = new Effect(SLAM_RING, owner, friendly).at(x, y).radius(blastRadius).life(0.48f)
                .damage(blastDamage, Math.max(kbX, 420), Math.max(kbY, 260)).colors(c1, c2);
        w.effects.add(ring);
        w.parts.burst(Particles.CIRCLE, x, y, 26, 420, 0.52f, 11, c2, 140, 0.92f);
        w.parts.burst(Particles.SPARK, x, y, 18, 360, 0.38f, 7, c1, 90, 0.91f);
        w.cam.shake(11, 0.3f);
    }

    private void flameDragonImpact(World w) {
        if (ex2 != 0) return;
        ex2 = 1;
        dead = true;
        float groundY = owner != null ? owner.bottom() : w.groundYUnder(x, y + 110);
        if (groundY > 9000) groundY = y + 8;
        Effect firestorm = new Effect(WHIRL, owner, friendly).at(x, groundY - 8).radius(286).life(1.7f)
                .damage(ex1 > 0 ? ex1 : 145, 500, 300).tickEvery(0.16f).colors(AbilityCast.FLAME, AbilityCast.FLAME_HI);
        firestorm.ex2 = 3;
        firestorm.pull = 0.3f;
        w.effects.add(firestorm);
        w.parts.burst(Particles.FIRE, x, y, 42, 430, 0.72f, 14, AbilityCast.FLAME_HI, -160, 0.92f);
        w.cam.shake(12, 0.4f);
    }

    private float pointSegmentDistance(float px, float py, float ax, float ay, float bx, float by) {
        float dx = bx - ax, dy = by - ay, len2 = dx * dx + dy * dy;
        if (len2 <= 1) return FMath.dist(px, py, ax, ay);
        float t = FMath.clamp(((px - ax) * dx + (py - ay) * dy) / len2, 0, 1);
        return FMath.dist(px, py, ax + dx * t, ay + dy * t);
    }

    private void whisperOrbBurst(World w) {
        if (ex2 == 9) return;
        ex2 = 9;
        dead = true;
        Effect pulse = new Effect(NOVA, owner, friendly).at(x, y).radius(84).life(0.28f)
                .damage(15, 110, 80).colors(c1, c2);
        w.effects.add(pulse);
        for (int i = 0; i < 8; i++) {
            float a = i * (float) Math.PI / 4f;
            Effect wisp = new Effect(ENMU_WHISPER, owner, friendly).at(x, y)
                    .vel(FMath.cos(a) * ex1, FMath.sin(a) * ex1).radius(12).life(1.15f)
                    .damage(5, 65, 45).colors(c1, c2);
            wisp.sleepOnHit = 3.7f;
            w.effects.add(wisp);
        }
        for (int i = 0; i < 4; i++) {
            float a = i * (float) Math.PI * 0.5f;
            Effect cone = new Effect(ENMU_NIGHTMARE, owner, friendly).at(x, y).radius(1).life(0.5f)
                    .colors(new Color(97, 191, 255), new Color(61, 123, 255));
            cone.ex1 = 380;
            cone.ex2 = 5;
            cone.ex3 = 1; // these burst cones have no eye at their origin
            cone.angle = cone.grav = a;
            cone.sleepOnHit = 2.6f;
            cone.tickEvery(0.18f);
            w.effects.add(cone);
        }
        w.parts.ring(x, y, c2);
    }

    private void nightmareShockwave(World w, float groundY, float radius, float damage) {
        Effect shock = new Effect(SLAM_RING, owner, friendly).at(x, groundY - 4).radius(radius).life(0.42f)
                .damage(damage, 320, 160).colors(c1, c2);
        shock.ex2 = 6;
        w.effects.add(shock);
        w.parts.burst(Particles.CIRCLE, x, groundY, 26, 300, 0.48f, 10, c2, 180, 0.93f);
        w.parts.burst(Particles.CIRCLE, x, groundY - 8, 18, 330, 0.58f, 13, new Color(95, 78, 106), 260, 0.94f);
    }

    public void render(Graphics2D g) { EffectArt.render(g, this); }
}
