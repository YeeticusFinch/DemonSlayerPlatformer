package game;

import java.awt.*;
import java.util.ArrayList;

abstract class Trap {
    public float px, py, pw, ph;

    public abstract void update(World w, float dt);

    public abstract void render(Graphics2D g, boolean night);

    public boolean solidNow() { return false; }

    public boolean inView(World w, float margin) {
        return px + pw > w.viewL - margin && px < w.viewR + margin && py + ph > w.viewT - margin && py < w.viewB + margin;
    }

    public static class Spikes extends Trap {
        private float bloodT;

        public Spikes(float x, float y, float wd) {
            px = x;
            py = y - 22;
            pw = wd;
            ph = 22;
        }

        @Override
        public void update(World w, float dt) {
            bloodT = Math.max(0, bloodT - dt);
            java.util.ArrayList<Fighter> all = new ArrayList<>();
            all.add(w.player);
            all.addAll(w.enemies);
            for (Fighter f : all) {
                if (f.dead || f.invulnT > 0 || !f.overlapsRect(px + 4, py + 6, pw - 8, ph - 6)) continue;
                float dir = FMath.signum(f.x - (px + pw / 2));
                f.hurt(w, null, 12, dir == 0 ? 1 : dir, 140, 330);
                bloodT = 0.6f;
            }
        }

        @Override
        public void render(Graphics2D g, boolean night) {
            g.setColor(new Color(70, 66, 62));
            g.fillRect((int) px, (int) (py + ph - 5), (int) pw, 5);
            int n = Math.max(2, (int) (pw / 18));
            float sw = pw / n;
            for (int i = 0; i < n; i++) {
                float bx = px + i * sw;
                g.setColor(night ? new Color(128, 130, 142) : new Color(168, 172, 182));
                g.fillPolygon(new int[]{(int) bx + 2, (int) (bx + sw / 2), (int) (bx + sw - 2)},
                        new int[]{(int) (py + ph - 4), (int) py, (int) (py + ph - 4)}, 3);
                g.setColor(new Color(255, 255, 255, 60));
                g.drawLine((int) (bx + sw / 2), (int) (py + 3), (int) (bx + sw / 2), (int) (py + ph - 6));
                if (bloodT > 0) {
                    g.setColor(new Color(160, 20, 35, (int) (200 * bloodT / 0.6f)));
                    g.fillPolygon(new int[]{(int) (bx + sw * 0.3f), (int) (bx + sw / 2), (int) (bx + sw * 0.7f)},
                            new int[]{(int) (py + ph - 5), (int) (py + 8), (int) (py + ph - 5)}, 3);
                }
            }
        }
    }

    public static class Crusher extends Trap {
        private final float homeX, homeY, radius = 36;
        private int state;
        private float vx, vy, rollAng, timer, respawnT, telegraphT;
        private float hitCd;

        public static final int ARMED = 0, TELEGRAPH = 1, FALLING = 2, ROLLING = 3, DEAD = 4;

        public Crusher(float x, float y) {
            homeX = px = x;
            homeY = py = y;
            pw = ph = radius * 2;
            state = ARMED;
        }

        @Override
        public void update(World w, float dt) {
            hitCd -= dt;
            Player pl = w.player;
            switch (state) {
                case ARMED -> {
                    py = homeY + FMath.sin(Game.time * 2) * 3;
                    if (!pl.dead && Math.abs(pl.x - homeX) < 90 && pl.bottom() > homeY) {
                        state = TELEGRAPH;
                        telegraphT = 0.38f;
                    }
                }
                case TELEGRAPH -> {
                    telegraphT -= dt;
                    px = homeX + FMath.rand(-3, 3);
                    if (FMath.chance(0.5f))
                        w.parts.spawn(Particles.CIRCLE, px + FMath.rand(-24, 24), py + radius, FMath.rand(-20, 20),
                                FMath.rand(20, 70), 0.4f, 6, new Color(150, 140, 125), 100, 0.95f);
                    if (telegraphT <= 0) { px = homeX; state = FALLING; vy = 60; }
                }
                case FALLING -> {
                    vy += 2100 * dt;
                    py += vy * dt;
                    float gy = w.groundYUnder(px, py + radius - 10);
                    if (gy < 9000 && py + radius >= gy) {
                        py = gy - radius;
                        state = ROLLING;
                        float dir = pl.x < px ? -1 : 1;
                        vx = dir * 240;
                        w.cam.shake(9, 0.35f);
                        w.parts.burst(Particles.CIRCLE, px, gy, 18, 280, 0.6f, 10, new Color(145, 130, 112), 400, 0.93f);
                        hitFighters(w, 22, 430, 340);
                    }
                }
                case ROLLING -> {
                    vx = FMath.approach(vx, Math.signum(vx) * 560, 700 * dt);
                    float nx = px + vx * dt;
                    for (Level.Plat p : w.solids) {
                        if (py + radius <= p.y + 8 || py - radius >= p.y + p.h) continue;
                        if (nx + radius > p.x && nx - radius < p.x + p.w) {
                            if (vx > 0 && px + radius <= p.x + 2) nx = p.x - radius;
                            else if (vx < 0 && px - radius >= p.x + p.w - 2) nx = p.x + p.w + radius;
                            else continue;
                            vx *= -0.45f;
                            break;
                        }
                    }
                    px = FMath.clamp(nx, radius, w.level.w - radius);
                    rollAng += vx / radius * dt;
                    float gy = rollingGround(w);
                    if (gy < 9000 && py + radius >= gy - 54) {
                        float targetY = gy - radius;
                        if (py < targetY - 1) {
                            vy = Math.min(vy + 2100 * dt, 900);
                            py = Math.min(targetY, py + vy * dt);
                        } else {
                            py = targetY;
                            vy = 0;
                        }
                    } else {
                        vy = Math.min(vy + 2100 * dt, 1400);
                        py += vy * dt;
                    }
                    if (FMath.chance(0.4f))
                        w.parts.spawn(Particles.CIRCLE, px - Math.signum(vx) * radius * 0.6f, py + radius - 4,
                                -vx * 0.15f, FMath.rand(-140, -40), 0.4f, 7, new Color(150, 138, 120), 350, 0.94f);
                    hitFighters(w, 18, 400 * Math.signum(vx), 300);
                    timer += dt;
                    if (timer > 6 || px < 40 || px > w.level.w - 40) { state = DEAD; respawnT = 4; }
                }
                case DEAD -> {
                    respawnT -= dt;
                    if (respawnT <= 0) {
                        px = homeX;
                        py = homeY;
                        vx = vy = 0;
                        timer = 0;
                        state = ARMED;
                    }
                }
            }
        }

        private float rollingGround(World w) {
            float best = 99999;
            float lead = Math.signum(vx == 0 ? 1 : vx) * radius * 0.65f;
            best = Math.min(best, w.groundYUnder(px, py + radius + 4));
            best = Math.min(best, w.groundYUnder(px + lead, py + radius + 4));
            best = Math.min(best, w.groundYUnder(px - lead * 0.65f, py + radius + 4));
            return best;
        }

        private void hitFighters(World w, float dmg, float kbx, float kby) {
            java.util.ArrayList<Fighter> all = new ArrayList<>();
            all.add(w.player);
            all.addAll(w.enemies);
            for (Fighter f : all) {
                if (f.dead || hitCd > 0) continue;
                float cx2 = FMath.clamp(px, f.left(), f.right());
                float cy2 = FMath.clamp(py, f.top(), f.bottom());
                if (FMath.dist(px, py, cx2, cy2) < radius * 0.92f) {
                    if (f.hurt(w, null, dmg, Math.signum(f.x - px) == 0 ? Math.signum(vx == 0 ? 1 : vx) : Math.signum(f.x - px), kbx, kby))
                        hitCd = 0.5f;
                }
            }
        }

        @Override
        public void render(Graphics2D g, boolean night) {
            Composite old = g.getComposite();
            if (state == DEAD) g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0));
            g.setColor(new Color(0, 0, 0, 55));
            float gy = py + radius;
            g.fillOval((int) (px - radius * 0.9f), (int) (gy - 6), (int) (radius * 1.8f), 9);
            g.setColor(night ? new Color(96, 99, 110) : new Color(136, 139, 148));
            g.fillOval((int) (px - radius), (int) (py - radius), (int) (radius * 2), (int) (radius * 2));
            Graphics2D gg = (Graphics2D) g.create();
            gg.rotate(rollAng, px, py);
            gg.setColor(night ? new Color(76, 79, 90) : new Color(110, 113, 122));
            for (int i = 0; i < 3; i++) {
                double a = i * 2.09;
                gg.fillPolygon(new int[]{(int) (px + Math.cos(a) * radius * 0.55f), (int) (px + Math.cos(a + 1) * radius * 0.75f), (int) (px + Math.cos(a + 2) * radius * 0.4f)},
                        new int[]{(int) (py + Math.sin(a) * radius * 0.55f), (int) (py + Math.sin(a + 1) * radius * 0.75f), (int) (py + Math.sin(a + 2) * radius * 0.4f)}, 3);
            }
            gg.setColor(new Color(255, 255, 255, 50));
            gg.fillOval((int) (px - radius * 0.5f), (int) (py - radius * 0.55f), (int) (radius * 0.5f), (int) (radius * 0.4f));
            gg.dispose();
            g.setComposite(old);
        }
    }

    public static class SwingLog extends Trap {
        private final float pivotX, pivotY, len, maxAng, speed, phase;
        private final float rad = 13;
        private float curAng, angVel;

        public SwingLog(float pxx, float pyy, float ln, float maxAngDeg, float spd, float ph) {
            pivotX = pxx;
            pivotY = pyy;
            len = ln;
            maxAng = (float) Math.toRadians(maxAngDeg);
            speed = spd;
            phase = ph;
            pw = rad * 2;
            ph = rad * 2;
        }

        @Override
        public void update(World w, float dt) {
            float prevAng = curAng;
            curAng = maxAng * FMath.sin(Game.time * speed + phase);
            angVel = (curAng - prevAng) / Math.max(dt, 1e-4f);
            float dxs = FMath.sin(curAng), dys = FMath.cos(curAng);
            float ax = pivotX + dxs * len, ay = pivotY + dys * len;
            float endX = pivotX + dxs * (len * 2), endY = pivotY + dys * (len * 2);
            px = Math.min(pivotX, endX) - rad;
            py = Math.min(pivotY, endY) - rad;
            pw = Math.abs(endX - pivotX) + rad * 2;
            ph = Math.abs(endY - pivotY) + rad * 2;

            java.util.ArrayList<Fighter> all = new ArrayList<>();
            all.add(w.player);
            all.addAll(w.enemies);
            float ex = dxs * len, ey = dys * len;
            float segLenSq = ex * ex + ey * ey;
            for (Fighter f : all) {
                if (f.dead) continue;
                // closest point on the log's axis to the fighter centre
                float t = FMath.clamp(((f.x - ax) * ex + (f.y - ay) * ey) / segLenSq, 0, 1);
                float sxp = ax + ex * t, syp = ay + ey * t;
                // closest point on the fighter's box to that axis point
                float bx = FMath.clamp(sxp, f.left(), f.right());
                float by = FMath.clamp(syp, f.top(), f.bottom());
                float ddx = bx - sxp, ddy = by - syp;
                float d = FMath.dist(0, 0, ddx, ddy);
                if (d >= rad + 3) continue;
                // push normal: from the log surface toward the fighter
                float nx, ny;
                if (d > 0.01f) { nx = ddx / d; ny = ddy / d; }
                else {
                    float side = Math.signum((f.x - sxp) * dys + (f.y - syp) * -dxs);
                    if (side == 0) side = 1;
                    nx = dys * side;
                    ny = -dxs * side;
                }
                // solid: shove the fighter out of the log, cancel velocity into it
                float push = rad + 3 - d;
                f.x += nx * push;
                f.y += ny * push;
                float vn = f.vx * nx + f.vy * ny;
                if (vn < 0) { f.vx -= vn * nx; f.vy -= vn * ny; }
                // damage on contact, flung along the log's motion
                if (f.invulnT <= 0) {
                    float tangent = angVel * dys * len * 1.5f;
                    float dir = Math.abs(tangent) < 20 ? Math.signum(f.x - ax) : Math.signum(tangent);
                    if (dir == 0) dir = 1;
                    float force = FMath.clamp(Math.abs(tangent) * 0.32f, 340, 560);
                    f.hurt(w, null, 14, dir, force, 300);
                }
            }
        }

        @Override
        public void render(Graphics2D g, boolean night) {
            float dxs = FMath.sin(curAng), dys = FMath.cos(curAng);
            float ax = pivotX + dxs * len, ay = pivotY + dys * len;
            float perpX = dys, perpY = -dxs;
            g.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(night ? new Color(90, 78, 60) : new Color(120, 102, 74));
            g.drawLine((int) (pivotX + perpX * 7), (int) (pivotY + perpY * 7), (int) (ax + perpX * 5), (int) (ay + perpY * 5));
            g.drawLine((int) (pivotX - perpX * 7), (int) (pivotY - perpY * 7), (int) (ax - perpX * 5), (int) (ay - perpY * 5));
            g.setColor(new Color(0, 0, 0, 45));
            g.drawOval((int) (pivotX - 10), (int) (pivotY - 5), 20, 8);
            Graphics2D gg = (Graphics2D) g.create();
            gg.translate(ax, ay);
            gg.rotate(-curAng);
            gg.setColor(night ? new Color(84, 62, 42) : new Color(118, 88, 56));
            gg.fillRoundRect((int) (-rad), (int) (-rad), (int) (rad * 2), (int) (len), (int) (rad * 2), (int) (rad * 2));
            gg.setColor(night ? new Color(104, 80, 54) : new Color(146, 112, 72));
            gg.fillRoundRect((int) (-rad * 0.55f), (int) (-rad), (int) (rad * 1.1f), (int) len, (int) rad, (int) rad);
            for (float k = 0.18f; k < 1f; k += 0.24f) {
                gg.setColor(new Color(0, 0, 0, 35));
                gg.drawLine((int) (-rad * 0.8f), (int) (len * k), (int) (rad * 0.8f), (int) (len * k));
            }
            gg.dispose();
        }
    }

    public static class MovingPlat extends Trap {
        private final float x0, y0, x1, y1, speed;
        private float phase;

        public MovingPlat(float xa, float ya, float xb, float yb, float wd, float spd) {
            x0 = xa;
            y0 = ya;
            x1 = xb;
            y1 = yb;
            px = xa;
            py = ya;
            pw = wd;
            ph = 16;
            speed = spd;
        }

        @Override
        public void update(World w, float dt) {
            phase += dt * speed;
            float k = 0.5f - 0.5f * FMath.cos(phase);
            float nx2 = FMath.lerp(x0, x1, k), ny2 = FMath.lerp(y0, y1, k);
            float dx = nx2 - px, dy = ny2 - py;
            px = nx2;
            py = ny2;
            carry(w.player, dx, dy);
            for (Fighter f : w.enemies) carry(f, dx, dy);
        }

        private void carry(Fighter f, float dx, float dy) {
            if (f.dead || dx == 0 && dy == 0) return;
            if (Math.abs(f.bottom() - py) < 10 && f.right() > px && f.left() < px + pw && f.vy >= -10) {
                f.x += dx;
                f.y += dy;
                f.onGround = true;
            }
        }

        @Override
        public boolean solidNow() { return true; }

        @Override
        public void render(Graphics2D g, boolean night) {
            g.setColor(new Color(0, 0, 0, 40));
            g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(new Color(120, 126, 138, 90));
            g.drawLine((int) (x0 + pw / 2), (int) (y0 + 4), (int) (x1 + pw / 2), (int) (y1 + 4));
            g.setColor(night ? new Color(96, 72, 50) : new Color(134, 100, 68));
            g.fillRect((int) px, (int) py, (int) pw, (int) ph);
            g.setColor(night ? new Color(116, 88, 62) : new Color(162, 124, 86));
            g.fillRect((int) px, (int) py, (int) pw, 5);
            g.setColor(new Color(0, 0, 0, 50));
            for (int i = 10; i < pw; i += 34) g.fillRect((int) (px + i), (int) (py + 5), 3, (int) (ph - 5));
            g.setColor(new Color(150, 156, 168));
            g.fillRect((int) (px + pw / 2 - 5), (int) (py - 6), 10, 6);
        }
    }

    public static class Crumble extends Trap {
        private final float ox, oy;
        private final int IDLE = 0, SHAKE = 1, FALL = 2, GONE = 3, RISE = 4;
        final int[] st = {IDLE};
        private float t, vy, shakeSeed;

        public Crumble(float x, float y, float wd) {
            ox = px = x;
            oy = py = y;
            pw = wd;
            ph = 14;
        }

        @Override
        public void update(World w, float dt) {
            t += dt;
            switch (st[0]) {
                case IDLE -> {
                    if (stoodOnBy(w)) { st[0] = SHAKE; t = 0; shakeSeed = FMath.rand(0, 9); }
                }
                case SHAKE -> {
                    py = oy + 6 * (t / 0.45f);
                    if (t > 0.45f) { st[0] = FALL; t = 0; vy = 40; }
                }
                case FALL -> {
                    vy += 1900 * dt;
                    py += vy * dt;
                    if (t > 2.2f || py > oy + 700) { st[0] = GONE; t = 0; }
                }
                case GONE -> {
                    if (t > 0.8f) { st[0] = RISE; t = 0; }
                }
                case RISE -> {
                    float k = FMath.easeInOut(Math.min(1, t / 1.6f));
                    py = FMath.lerp(py, oy, 1 - (float) Math.pow(0.0001, dt * k));
                    if (Math.abs(py - oy) < 1.5f) { py = oy; st[0] = IDLE; t = 0; }
                }
            }
        }

        private boolean stoodOnBy(World w) {
            if (Math.abs(w.player.bottom() - py) < 8 && w.player.right() > px && w.player.left() < px + pw && w.player.vy >= 0 && !w.player.dead)
                return true;
            for (Fighter f : w.enemies)
                if (!f.dead && Math.abs(f.bottom() - py) < 8 && f.right() > px && f.left() < px + pw && f.vy >= 0) return true;
            return false;
        }

        @Override
        public boolean solidNow() { return st[0] <= SHAKE || st[0] == RISE && py - oy < 30; }

        @Override
        public void render(Graphics2D g, boolean night) {
            Composite old = g.getComposite();
            if (st[0] == GONE) g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.15f));
            float jx = st[0] == SHAKE ? FMath.rand(-2.4f, 2.4f) : 0;
            float jy = st[0] == SHAKE ? FMath.rand(-1.6f, 1.6f) : 0;
            float x = px + jx, y = py + jy;
            g.setColor(night ? new Color(98, 74, 52) : new Color(138, 104, 72));
            g.fillRect((int) x, (int) y, (int) pw, (int) ph);
            g.setColor(night ? new Color(116, 88, 62) : new Color(160, 124, 86));
            g.fillRect((int) x, (int) y, (int) pw, 4);
            int cracks = st[0] == IDLE ? 1 : st[0] == SHAKE ? 3 : 2;
            g.setColor(new Color(0, 0, 0, 90));
            for (int i = 0; i < cracks; i++) {
                float cxp = x + pw * (0.25f + 0.25f * i) + ((i * 37 + shakeSeed) % 7) - 3;
                g.drawLine((int) cxp, (int) y, (int) (cxp - 4), (int) (y + ph));
            }
            g.setComposite(old);
        }
    }
}
