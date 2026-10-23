package game;

import java.awt.*;
import java.util.ArrayList;

/** Upper Rank Three, a powerful close-range demon with a compass-like battle sense. */
public class Akaza extends Enemy {
    private static final String SVG_PATH = "assets/akaza.svg";
    private static Svg svg;
    private static Svg.Group gHead, gEyeGlow, gArmFront, gArmBack, gTorso, gLegFront, gLegBack;
    private static boolean svgTried;
    private float dashCooldown = 0.65f, dashT, dashDir = 1, strikeGlowT;
    private float groundJumpCooldown, airJumpCooldown, extraAirJumps;
    private boolean flipActive;
    private float flipT;
    public float finisherPoseT = -1;
    private float artCooldown = 0.55f;
    private int artIndex;
    private final ArrayList<Ability> arts = Ability.destructiveDeath();

    static boolean isDestructiveDeathUser(Fighter fighter) {
        return fighter instanceof Akaza || fighter instanceof Player player
                && player.isDemon && player.demonArt == Profile.DemonArt.AKAZA;
    }

    public Akaza() {
        super(48, 106);
        name = "Akaza, Upper Rank Three";
        team = 1;
        isDemon = true;
        maxHp = hp = 1750;
        maxSp = sp = 450;
        runSpeed = 425;
        aggroR = 1800;
        atkRange = 104;
        atkCdBase = 0.3f;
        windup = 0.17f;
        recover = 0.28f;
        dmg = 24;
        elite = true;
        eliteColor = new Color(80, 164, 225);
        loadSvg();
    }

    private static void loadSvg() {
        if (svgTried) return;
        svgTried = true;
        svg = Svg.load(SVG_PATH);
        if (svg != null) {
            gHead = svg.group("head");
            gEyeGlow = svg.group("eye-glow");
            gArmFront = svg.group("arm-front");
            gArmBack = svg.group("arm-back");
            gTorso = svg.group("torso");
            gLegFront = svg.group("leg-front");
            gLegBack = svg.group("leg-back");
        }
        if (svg == null || gHead == null || gEyeGlow == null || gArmFront == null || gArmBack == null
                || gTorso == null || gLegFront == null || gLegBack == null) svg = null;
    }

    @Override public void update(World world, float dt) {
        recoverToMapCenter(world);
        boolean wasGrounded = onGround;
        float oldVy = vy;
        dashCooldown = Math.max(0, dashCooldown - dt);
        dashT = Math.max(0, dashT - dt);
        strikeGlowT = Math.max(0, strikeGlowT - dt);
        super.update(world, dt);
        recoverToMapCenter(world);
        if ((wasGrounded && !onGround && vy < -160) || (oldVy > -160 && vy < -500)) beginFrontflip();
        groundJumpCooldown = Math.max(0, groundJumpCooldown - dt);
        airJumpCooldown = Math.max(0, airJumpCooldown - dt);
        if (flipActive) {
            flipT += dt / 0.5f;
            if (flipT >= 1) flipActive = false;
        }
        if (!dead) sp = Math.min(maxSp, sp + 20 * dt);
        demonRegen(world, dt, 45f);
    }

    private void recoverToMapCenter(World world) {
        if (dead || (x >= w * 0.5f && x <= world.level.w - w * 0.5f
                && top() >= -h && bottom() <= world.level.h + h)) return;
        x = world.level.w * 0.5f;
        float ground = world.groundYUnder(x, world.level.h);
        if (ground > 9000) ground = 700;
        y = ground - h * 0.5f;
        vx = vy = 0;
        onGround = true;
        dashT = 0;
        abilityRootT = 0;
        state = 0;
        stateT = 0;
    }

    @Override public boolean hurt(World world, Fighter src, float dmgAmt, float dir, float kbX, float kbY) {
        if (world.level.akazaRengokuDuel && src == world.player && hp - dmgAmt <= 0)
            dmgAmt = Math.max(0, hp - 1);
        return super.hurt(world, src, dmgAmt, dir, kbX, kbY);
    }

    @Override protected void think(World world, float dt, float dx, float dist) {
        Fighter target = world.targetFor(this);
        if (target == null || target.dead) {
            vx = 0;
            return;
        }
        if (akazaKneelT > 0) {
            vx = 0;
            return;
        }
        if (dashT > 0) {
            vx = dashDir * 1480;
            face(dashDir);
            if (FMath.chance(0.8f)) world.ghosts.add(new Ghost(x, bottom(), h, new Color(80, 164, 225)));
            return;
        }
        artCooldown -= dt;
        if ((state == 0 || state == 1) && artCooldown <= 0 && dist < 1050
                && Math.abs(target.y - y) < 520) {
            for (int i = 0; i < arts.size(); i++) {
                Ability art = arts.get(artIndex++ % arts.size());
                if (sp < art.cost) continue;
                if (art.kind == Ability.Kind.AKAZA_FLYING_PLANET && target.y >= y - 45) continue;
                artCooldown = Math.max(0.55f, Math.min(4.5f, art.cooldown * 0.42f));
                if (art.kind == Ability.Kind.AKAZA_BLUE_SILVER_AFTERGLOW) sp = 0;
                else sp -= art.cost;
                world.castAbility(this, art);
                return;
            }
            artCooldown = 0.5f;
        }
        if ((state == 0 || state == 1) && dashCooldown <= 0 && dist > 170 && dist < 680) {
            dashDir = Math.signum(dx == 0 ? target.x - x : dx);
            if (dashDir == 0) dashDir = facing();
            dashT = 0.25f;
            dashCooldown = FMath.rand(0.5f, 0.86f);
            vx = dashDir * 1480;
            if (onGround) {
                vy = -235;
                beginFrontflip();
            }
            face(dashDir);
            world.cam.shake(2.8f, 0.12f);
            return;
        }
        super.think(world, dt, dx, dist);
    }

    @Override protected void eliteMove(World world, float dt, float dx, float dist) {
        Fighter target = world.targetFor(this);
        if (target == null || target.dead) return;
        if (onGround) extraAirJumps = 1;
        float dir = Math.signum(dx == 0 ? target.x - x : dx);
        if (dir == 0) dir = facing();
        if (onGround && groundJumpCooldown <= 0 && dist < 520
                && (target.y < y - 38 || FMath.chance(dt * 0.62f)) && !legBlocked()) {
            vy = -760;
            onGround = false;
            groundJumpCooldown = FMath.rand(0.38f, 0.62f);
            extraAirJumps = 1;
            beginFrontflip();
        } else if (!onGround && extraAirJumps > 0 && airJumpCooldown <= 0 && vy > -160
                && dist < 620 && (target.y < y - 28 || FMath.chance(dt * 1.6f)) && !legBlocked()) {
            vy = -710;
            onGround = false;
            extraAirJumps--;
            airJumpCooldown = FMath.rand(0.18f, 0.34f);
            beginFrontflip();
            world.parts.ring(x, y + h * 0.34f, new Color(126, 204, 255, 180));
        }
    }

    private void beginFrontflip() {
        flipActive = true;
        flipT = 0.001f;
    }

    @Override protected void strike(World world) {
        strikeGlowT = 0.3f;
        basicStrike(world, 112, 325, 215, -16, new Color(87, 169, 231));
        world.parts.burst(Particles.CIRCLE, x + facing() * 58, y - 22, 10, 230, 0.34f, 8,
                new Color(98, 181, 239), 65, 0.94f);
    }

    @Override protected void renderBody(Graphics2D g) {
        float feet = bottom();
        g.setColor(new Color(0, 0, 0, 65));
        g.fillOval((int) x - 31, (int) feet - 5, 62, 9);
        if (svg == null) {
            g.setColor(new Color(224, 169, 145));
            g.fillRoundRect((int) x - 16, (int) feet - 74, 32, 66, 12, 12);
            g.setColor(new Color(244, 162, 171));
            g.fillOval((int) x - 13, (int) feet - 105, 26, 29);
            g.setColor(new Color(57, 112, 157));
            g.drawLine((int) x - 10, (int) feet - 79, (int) x + 11, (int) feet - 46);
            return;
        }
        Composite old = g.getComposite();
        if (flashT > 0) g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.84f));
        Graphics2D bg = createSvgTransform(g);
        float move = FMath.clamp(Math.abs(vx) / 240f, 0, 1);
        float phase = animPhase * 6.28f;
        float legSwing = FMath.sin(phase) * 0.78f * move;
        float armSwing = FMath.sin(phase) * 0.58f * move;
        float frontArm = armSwing, backArm = -armSwing * 0.7f;
        if (state == 2) {
            float wind = FMath.easeOut(FMath.clamp(stateT / Math.max(0.01f, windup), 0, 1));
            if (swingSide > 0) frontArm -= wind * 1.25f;
            else backArm -= wind * 1.25f;
        } else if (state == 3) {
            float swing = 1 - FMath.easeOut(FMath.clamp(stateT / Math.max(0.01f, recover), 0, 1));
            if (swingSide > 0) frontArm = FMath.lerp(0.95f, -1.2f, swing);
            else backArm = FMath.lerp(0.95f, -1.2f, swing);
        }
        if (attacking()) frontArm -= FMath.sin((1 - atkT / Math.max(0.01f, atkDur)) * (float) Math.PI) * 1.3f;
        if (finisherPoseT >= 0) {
            if (finisherPoseT < 1.1f) frontArm = 0.55f;
            else if (finisherPoseT < 1.85f)
                frontArm = FMath.lerp(0.55f, -1.62f, FMath.easeOut((finisherPoseT - 1.1f) / 0.75f));
            else frontArm = -1.62f;
            backArm = -0.18f;
        }
        float headTurn = FMath.sin(Game.time * 2.4f) * 0.035f;
        if (abilityRootT > 0) {
            // Lower Akaza's weight, brace one leg, and plant his front hand on the floor.
            drawPart(bg, gLegBack, 0.02f, -1, 1);
            drawPart(bg, gLegFront, 0.78f, 7, 2);
            drawPart(bg, gArmBack, -0.48f, -1, 8);
            drawPart(bg, gTorso, 0.25f, 2, 11);
            drawPart(bg, gArmFront, 1.28f, 5, 17);
            drawPart(bg, gHead, 0.12f, 5, 12);
        } else {
            drawPart(bg, gLegBack, -legSwing);
            drawPart(bg, gArmBack, backArm);
            drawPart(bg, gTorso, 0);
            drawPart(bg, gLegFront, legSwing);
            drawPart(bg, gArmFront, frontArm);
            drawPart(bg, gHead, headTurn);
        }
        bg.dispose();
        g.setComposite(old);
    }

    @Override public void renderGlow(Graphics2D g) {
        super.renderGlow(g);
        if (svg != null && gEyeGlow != null) {
            Graphics2D bg = createSvgTransform(g);
            drawPart(bg, gEyeGlow, FMath.sin(Game.time * 2.4f) * 0.025f,
                    gHead.pivotX, gHead.pivotY, 0, 0);
            bg.dispose();
        } else {
            float eyeY = bottom() - h * 0.67f;
            float pulse = 0.72f + 0.28f * FMath.sin(Game.time * 5.2f);
            Glow.blob(g, x + facing() * 5, eyeY, 17 * pulse, new Color(94, 177, 255, 185));
        }
        if (strikeGlowT > 0) Glow.blob(g, x + facing() * 42, y - 24, 72 * strikeGlowT / 0.3f,
                new Color(84, 171, 245, 170));
    }

    private Graphics2D createSvgTransform(Graphics2D g) {
        Graphics2D bg = (Graphics2D) g.create();
        float scale = h / svg.h;
        bg.translate(x, bottom());
        bg.scale(facing() * scale, scale);
        bg.translate(-svg.w / 2f, -svg.h);
        if (flipActive)
            bg.rotate(facing() * FMath.easeInOut(FMath.clamp(flipT, 0, 1)) * (float) Math.PI * 2,
                    svg.w * 0.5f, svg.h * 0.55f);
        return bg;
    }

    private void drawPart(Graphics2D bg, Svg.Group group, float rotation) {
        if (group == null) return;
        drawPart(bg, group, rotation, group.pivotX, group.pivotY, 0, 0);
    }

    private void drawPart(Graphics2D bg, Svg.Group group, float rotation, float offsetX, float offsetY) {
        if (group == null) return;
        drawPart(bg, group, rotation, group.pivotX, group.pivotY, offsetX, offsetY);
    }

    private void drawPart(Graphics2D bg, Svg.Group group, float rotation,
                          float pivotX, float pivotY, float offsetX, float offsetY) {
        if (group == null) return;
        Graphics2D part = (Graphics2D) bg.create();
        part.translate(offsetX, offsetY);
        if (rotation != 0) {
            part.translate(pivotX, pivotY);
            part.rotate(rotation);
            part.drawImage(group.img, Math.round(-pivotX), Math.round(-pivotY),
                    Math.round(svg.w), Math.round(svg.h), null);
        } else {
            part.drawImage(group.img, 0, 0, Math.round(svg.w), Math.round(svg.h), null);
        }
        part.dispose();
    }
}
