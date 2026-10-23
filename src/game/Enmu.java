package game;

import java.awt.*;
import java.util.ArrayList;

public class Enmu extends Enemy {
    private static final String SVG_PATH = "assets/enmu.svg";
    private static final float SVG_CX = 32, SVG_FEET = 92;
    private static Svg svg;
    private static Svg.Group gHead, gCape, gCapeFront, gTorso, gArmB, gArmF, gLegB, gLegF;
    private static boolean svgTried;

    private final ArrayList<Ability> arts = Ability.enmuArt();
    private int artIdx;
    private float artCd = 1.1f, reflexT, reflexTime, reflexDuration, reflexRadiusScale, reflexCenterX, reflexCenterY, reflexHitCd;
    private float dodgeT, dodgeDir, dodgeCooldown;

    public Enmu() {
        super(30, 90);
        name = "Enmu, Lower One";
        team = 1;
        isDemon = true;
        isBoss = true;
        maxHp = hp = 504;
        maxSp = sp = 280;
        runSpeed = 205;
        aggroR = 1500;
        atkRange = 94;
        dmg = 12;
        elite = true;
        eliteColor = new Color(110, 150, 255);
        loadSvg();
    }

    private static void loadSvg() {
        if (svgTried) return;
        svgTried = true;
        svg = Svg.load(SVG_PATH);
        if (svg == null) return;
        gHead = svg.group("head");
        gCape = svg.group("cape");
        gCapeFront = svg.group("cape-front");
        gTorso = svg.group("torso");
        gArmB = svg.group("arm-back");
        gArmF = svg.group("arm-front");
        gLegB = svg.group("leg-back");
        gLegF = svg.group("leg-front");
        if (gHead == null || gCape == null || gCapeFront == null || gTorso == null || gArmB == null || gArmF == null
                || gLegB == null || gLegF == null)
            svg = null;
    }

    @Override
    public void update(World world, float dt) {
        reflexHitCd = Math.max(0, reflexHitCd - dt);
        dodgeCooldown = Math.max(0, dodgeCooldown - dt);
        dodgeT = Math.max(0, dodgeT - dt);
        recoverToTrainRoofCenter(world);
        super.update(world, dt);
        sp = Math.min(maxSp, sp + 30 * dt);
        recoverToTrainRoofCenter(world);
    }

    private void recoverToTrainRoofCenter(World world) {
        if (world.level.theme == Level.Theme.TRAIN_ROOF && !dead
                && (x < 0 || x > world.level.w || top() < -h || bottom() > world.level.h + h)) {
            x = world.level.w / 2f;
            float gy = world.groundYUnder(x, world.level.h);
            if (gy > 9000) gy = 650;
            y = gy - h / 2f;
            vx = vy = 0;
            onGround = true;
            state = 0;
            stateT = 0;
        }
    }

    public void beginReflex() {
        reflexT = 1f;
        reflexDuration = 1f;
        reflexRadiusScale = 36f;
        reflexTime = 0;
        reflexCenterX = x - facing() * reflexRadiusScale * 5f;
        reflexCenterY = y;
        reflexHitCd = 0;
        state = 0;
        stateT = 0;
    }

    @Override
    protected void tryGuard(World world, Fighter target) {
        if (reflexT > 0 || dodgeT > 0 || target == null || target.dead) return;
        if (dodgeCooldown <= 0 && incomingThreat(world, target)) {
            beginDodge(target);
            return;
        }
        super.tryGuard(world, target);
    }

    private boolean incomingThreat(World world, Fighter target) {
        float dx = x - target.x, dy = y - target.y;
        if (target.attacking() && target.facing() * dx > 0 && Math.abs(dx) < 118 && Math.abs(dy) < 135) return true;
        if (target instanceof Enemy enemy && enemy.state == 2
                && enemy.stateT >= Math.max(0, enemy.windup - 0.08f)
                && target.facing() * dx > 0 && Math.abs(dx) < 145 && Math.abs(dy) < 145) return true;
        for (Effect e : world.effects) {
            if (e.dead || !e.friendly || e.owner == null || e.owner.team == team || e.dmg <= 0) continue;
            float rx = x - e.x, ry = y - e.y, speed2 = e.vx * e.vx + e.vy * e.vy;
            float dot = rx * e.vx + ry * e.vy;
            float radius = e.r + Math.max(w, h) * 0.42f + 14;
            if (speed2 > 1 && dot < 0 && rx * rx + ry * ry > radius * radius) continue;
            float t = speed2 > 1 ? FMath.clamp(dot / speed2, 0, 0.32f) : 0;
            float px = x - e.x - e.vx * t, py = y - e.y - e.vy * t;
            if (px * px + py * py <= radius * radius) return true;
        }
        return false;
    }

    private void beginDodge(Fighter target) {
        dodgeDir = Math.signum(x - target.x);
        if (dodgeDir == 0) dodgeDir = -facing();
        dodgeT = 0.28f;
        dodgeCooldown = 0.82f;
        invulnT = Math.max(invulnT, 0.28f);
        vx = dodgeDir * 820;
        vy = -620;
        onGround = false;
        face(-dodgeDir);
    }

    @Override
    public boolean hurt(World world, Fighter src, float dmgAmt, float dir, float kbX, float kbY) {
        if (dodgeT > 0) return false;
        if (reflexT > 0) {
            invulnT = 0;
            boolean damaged = super.hurt(world, src, dmgAmt * 0.5f, dir, kbX, kbY);
            // Reflex halves each incoming hit rather than granting the usual post-hit invulnerability.
            if (reflexT > 0) invulnT = 0;
            return damaged;
        }
        boolean damaged = super.hurt(world, src, dmgAmt, dir, kbX, kbY);
        if (damaged && sleepT <= 0 && dodgeCooldown <= 0 && src != null) beginDodge(src);
        return damaged;
    }

    @Override
    protected void think(World world, float dt, float dx, float dist) {
        Fighter target = world.targetFor(this);
        if (dodgeT > 0) {
            vx = dodgeDir * 820;
            if (onGround) vy = -620;
            onGround = false;
            return;
        }
        if (reflexT > 0) {
            reflexT = Math.max(0, reflexT - dt);
            reflexTime += dt;
            float theta = reflexTime * (float) (Math.PI * 2 / Math.max(0.01f, reflexDuration));
            float r = reflexRadiusScale * (3 + 2 * FMath.cos(theta * 3f));
            float nx = reflexCenterX + facing() * r * FMath.cos(theta);
            float ny = reflexCenterY - r * FMath.sin(theta) * 0.72f;
            vx = (nx - x) / Math.max(dt, 0.001f);
            vy = (ny - y) / Math.max(dt, 0.001f) - Fighter.GRAV * dt;
            onGround = false;
            face(target.x - x);
            if (reflexHitCd <= 0 && FMath.dist(x, y, target.x, target.y) < 150) {
                reflexHitCd = 0.38f;
                noteAction();
                swingSide *= -1;
                world.meleeStrike(this, 92, 16, 210, 130,
                        Math.toRadians(facingRight ? -22 : 202), Math.toRadians(150), new Color(120, 160, 255));
            }
            return;
        }
        artCd -= dt;
        if ((state == 0 || state == 1) && artCd <= 0 && dist < 1050 && Math.abs(target.y - y) < 340) {
            Ability whisper = arts.get(0);
            if (sp >= whisper.cost && FMath.chance(0.45f)) {
                sp -= whisper.cost;
                artCd = whisper.cooldown;
                world.castAbility(this, whisper);
                return;
            }
            for (int i = 0; i < arts.size(); i++) {
                Ability a = arts.get(artIdx++ % arts.size());
                if (sp < a.cost) continue;
                sp -= a.cost;
                artCd = a.cooldown;
                world.castAbility(this, a);
                return;
            }
            artCd = 0.6f;
        }
        super.think(world, dt, dx, dist);
    }

    @Override
    protected void strike(World world) {
        basicStrike(world, 86, 210, 110, -10, new Color(100, 145, 255));
    }

    @Override
    protected void renderBody(Graphics2D g) {
        float feet = bottom();
        float ph = animPhase * 6.28f;
        float speedF = FMath.clamp(Math.abs(vx) / Math.max(1, runSpeed), 0, 1);
        float swing = FMath.sin(ph) * 0.55f * speedF;
        g.setColor(new Color(0, 0, 0, 60));
        g.fillOval((int) (x - 22), (int) feet - 5, 44, 8);
        if (svg == null) {
            g.setColor(new Color(55, 49, 70));
            g.fillRoundRect((int) x - 14, (int) feet - 66, 28, 60, 12, 12);
            g.setColor(new Color(233, 207, 198));
            g.fillOval((int) x - 10, (int) feet - 92, 20, 22);
            return;
        }
        float scale = h / SVG_FEET;
        Graphics2D bg = (Graphics2D) g.create();
        bg.translate(x, feet);
        bg.scale(facing(), 1);
        bg.scale(scale, scale);
        bg.translate(-SVG_CX, -SVG_FEET);
        float localMotion = vx * facing();
        float motionAmount = FMath.clamp(localMotion / 240f, -1, 1);
        float capeSway = Math.abs(vx) > 3
                ? -motionAmount * 0.22f + FMath.sin(animPhase * 2f) * Math.abs(motionAmount) * 0.04f
                : FMath.sin(Game.time * 2.5f) * 0.045f;
        if (reflexT > 0) {
            float turn = (float) (reflexTime / Math.max(0.01f, reflexDuration) * Math.PI * 2);
            bg.rotate(facing() * turn, SVG_CX, SVG_FEET - 45);
        } else if (dodgeT > 0) {
            bg.rotate(facing() * FMath.easeOut(1f - dodgeT / 0.28f) * (float) Math.PI, SVG_CX, SVG_FEET - 45);
        }
        drawPart(bg, gCape, capeSway);
        drawPart(bg, gLegB, -swing);
        drawPart(bg, gLegF, swing);
        drawPart(bg, gCapeFront, capeSway);
        drawPart(bg, gTorso, 0);
        drawPart(bg, gArmB, -swing * 0.45f);
        drawPart(bg, gArmF, reflexT > 0 ? FMath.sin(reflexTime * 34f) * 0.9f : swing * 0.4f);
        drawPart(bg, gHead, 0);
        bg.dispose();
    }

    private void drawPart(Graphics2D bg, Svg.Group part, float rotation) {
        if (part == null) return;
        Graphics2D gg = (Graphics2D) bg.create();
        if (rotation != 0) {
            gg.translate(part.pivotX, part.pivotY);
            gg.rotate(rotation);
            gg.drawImage(part.img, Math.round(-part.pivotX), Math.round(-part.pivotY),
                    Math.round(svg.w), Math.round(svg.h), null);
        } else gg.drawImage(part.img, 0, 0, Math.round(svg.w), Math.round(svg.h), null);
        gg.dispose();
    }
}
