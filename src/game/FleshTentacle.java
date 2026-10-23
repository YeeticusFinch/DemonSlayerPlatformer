package game;

import java.awt.*;
import java.awt.geom.Path2D;

/** A rooted train-flesh limb that lashes at nearby fighters. */
public class FleshTentacle extends Enemy {
    private float lashCd = 0.9f, lashT, lashDir = 1, lashTargetX, lashTargetY, lashLength;

    public FleshTentacle() {
        super(54, 110);
        name = "Flesh Tentacle";
        team = 1;
        isDemon = true;
        maxHp = hp = 130;
        runSpeed = 0;
        aggroR = 1600;
        atkRange = 260;
        dmg = 8;
        atkCdBase = 1.45f;
        isBoss = false;
    }

    @Override public void update(World world, float dt) {
        super.update(world, dt);
        lashT = Math.max(0, lashT - dt);
        if (dead || sleepT > 0) return;
        lashCd -= dt;
        Fighter target = world.targetFor(this);
        if (target == null || target.dead || Math.abs(target.x - x) > 310 || lashCd > 0) return;
        lashCd = 1.45f;
        float dir = Math.signum(target.x - x);
        lashDir = dir == 0 ? facing() : dir;
        lashTargetX = target.x;
        lashTargetY = target.y - target.h * 0.3f;
        lashLength = FMath.clamp(FMath.dist(x, bottom() - 4, lashTargetX, lashTargetY), 140, 330);
        lashT = 0.54f;
        float rootY = bottom() - 4;
        Effect lash = new Effect(Effect.ENMU_WHIP, this, false).at(lashTargetX, lashTargetY).radius(27).life(0.54f)
                .damage(17, 220, 150).colors(new Color(100, 55, 110), new Color(207, 171, 214));
        lash.ex2 = 1; // keep collision, but render the tentacle itself as it swings
        lash.pts.add(new float[]{x + dir * 4, rootY});
        world.effects.add(lash);
    }

    @Override protected void think(World world, float dt, float dx, float dist) {
        vx = 0;
        if (dist <= atkRange && atkCd <= 0) beginWindup();
    }

    @Override protected void strike(World world) { /* The animated lash effect is its attack. */ }

    @Override protected void renderBody(Graphics2D g) {
        float phase = Game.time * 3f + x * 0.01f;
        g.setColor(new Color(0, 0, 0, 60));
        g.fillOval((int) x - 32, (int) bottom() - 5, 64, 10);
        float[] cx = new float[25], cy = new float[25], nx = new float[25], ny = new float[25], widths = new float[25];
        float restAngle = -(float) Math.PI * 0.5f;
        float attack = lashT > 0 ? 0.54f - lashT : 0;
        float angle = restAngle, length = 116, arc = 0;
        if (lashT > 0) {
            float targetAngle = (float) Math.atan2(lashTargetY - (bottom() - 4), lashTargetX - x);
            float delta = (float) Math.atan2(Math.sin(targetAngle - restAngle), Math.cos(targetAngle - restAngle));
            if (attack < 0.1f) {
                float wind = attack / 0.1f;
                angle = restAngle - lashDir * 0.16f * wind;
                length = 116 - 20 * wind;
            } else if (attack < 0.36f) {
                float swing = FMath.easeOut((attack - 0.1f) / 0.26f);
                angle = restAngle + delta * swing;
                length = FMath.lerp(96, lashLength, swing);
                arc = 22 * FMath.sin(swing * (float) Math.PI);
            } else if (attack < 0.43f) {
                float slam = FMath.easeOut((attack - 0.36f) / 0.07f);
                angle = targetAngle + lashDir * 0.32f * slam;
                length = lashLength;
                arc = 48 * FMath.sin(slam * (float) Math.PI * 0.5f);
            } else {
                float recoil = FMath.easeIn((attack - 0.43f) / 0.11f);
                angle = targetAngle + lashDir * 0.32f * (1 - recoil) + (restAngle - targetAngle) * recoil;
                length = FMath.lerp(lashLength, 116, recoil);
                arc = 48 * (1 - recoil);
            }
        }
        float ux = FMath.cos(angle), uy = FMath.sin(angle), perpX = -uy, perpY = ux;
        for (int i = 0; i < cx.length; i++) {
            float k = i / (float) (cx.length - 1);
            float bend = lashT > 0
                    ? FMath.sin(k * (float) Math.PI) * arc * lashDir
                    : FMath.sin(k * 6.5f + phase) * (7 + 9 * k) * k;
            cx[i] = x + ux * k * length + perpX * bend;
            cy[i] = bottom() - 4 + uy * k * length + perpY * bend;
            widths[i] = 20 * (float) Math.pow(1 - k, 0.82) + 1.8f;
        }
        for (int i = 0; i < cx.length; i++) {
            int before = Math.max(0, i - 1), after = Math.min(cx.length - 1, i + 1);
            float dx = cx[after] - cx[before], dy = cy[after] - cy[before];
            float len = Math.max(1, FMath.dist(0, 0, dx, dy));
            nx[i] = -dy / len;
            ny[i] = dx / len;
        }
        Path2D.Float body = new Path2D.Float();
        for (int i = 0; i < cx.length; i++) {
            float px = cx[i] + nx[i] * widths[i], py = cy[i] + ny[i] * widths[i];
            if (i == 0) body.moveTo(px, py); else body.lineTo(px, py);
        }
        for (int i = cx.length - 1; i >= 0; i--)
            body.lineTo(cx[i] - nx[i] * widths[i], cy[i] - ny[i] * widths[i]);
        body.closePath();
        g.setPaint(new GradientPaint(x - 18, bottom(), new Color(72, 38, 75), x + 16, y - 112,
                new Color(165, 111, 164)));
        g.fill(body);
        g.setColor(new Color(49, 27, 58, 235));
        g.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(body);
        // A pair of helical ridges makes the living limb visibly twist as it sways.
        for (int ridge = 0; ridge < 2; ridge++) {
            Path2D.Float spiral = new Path2D.Float();
            for (int i = 0; i < cx.length; i++) {
                float k = i / (float) (cx.length - 1);
                float twist = FMath.sin(k * 13f - phase * 1.8f + ridge * (float) Math.PI) * widths[i] * 0.62f;
                float px = cx[i] + nx[i] * twist, py = cy[i] + ny[i] * twist;
                if (i == 0) spiral.moveTo(px, py); else spiral.lineTo(px, py);
            }
            g.setStroke(new BasicStroke(ridge == 0 ? 4f : 2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(ridge == 0 ? new Color(207, 155, 202, 215) : new Color(76, 42, 84, 215));
            g.draw(spiral);
        }
        for (int i = 2; i < 11; i += 2) {
            int idx = cx.length - 1 - i;
            float side = FMath.sin(phase * 1.8f + i) < 0 ? -1 : 1;
            float sx = cx[idx] + nx[idx] * widths[idx] * side;
            float sy = cy[idx] + ny[idx] * widths[idx] * side;
            g.setColor(new Color(228, 206, 226, 225));
            g.fillOval((int) sx - 3, (int) sy - 3, 7, 6);
        }
    }
}
