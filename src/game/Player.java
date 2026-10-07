package game;

import java.awt.*;
import java.awt.geom.Path2D;

public class Player extends Fighter {
    public Profile.Path path;
    public Profile.Style style;
    public Profile.DemonArt demonArt;
    public Profile.SlayerRank slayerRank;
    public float[] cds = new float[4];
    public int selAbility;
    public java.util.ArrayList<Ability> abilities = new java.util.ArrayList<>();

    private int maxJumps = 2, jumpsUsed;
    private float coyoteT, jumpBufT;
    private float dashT, dashCd, dashDir;
    private boolean wallSliding;
    public boolean burning;
    private float attackCd;
    private float trailT;
    public float spLock;
    public boolean slamPending;
    public boolean blazingSlamSlash;
    public float slamAge;
    public boolean flipActive;
    public float flipT;
    public float spRecoverDelay;
    public boolean nezukoMode, nezukoRescueUsed;
    /** > 0 while charging a form: holds the sword overhead. */
    public float chargeT;
    public float nezukoLegPoseT;
    public int nezukoLegPose;
    public float facingLockT;
    private float hopT;

    public Player(Profile p) {
        super(28, 86);
        path = p.path;
        style = p.style;
        demonArt = p.demonArt;
        slayerRank = p.slayerRank;
        team = 0;
        isDemon = path == Profile.Path.DEMON;
        name = isDemon ? "Akuma" : "Slayer";
        runSpeed = isDemon ? 360 : 345;
        maxHp = hp = !isDemon && rankedSlayer() ? 120 : 100;
        rebuildAbilities();
        resetCooldowns();
    }

    private boolean rankedSlayer() {
        return slayerRank.ordinal() >= Profile.SlayerRank.MIZUNOTO.ordinal();
    }

    @Override
    public float outgoingDamageMult() {
        return !isDemon && rankedSlayer() ? 1.2f : 1f;
    }

    /** Cheat: swap breathing style (rebuilds the ability loadout + blade color). */
    public void restyle(Profile.Style s) {
        style = s;
        rebuildAbilities();
        resetCooldowns();
        selAbility = 0;
    }

    public void setDemonArt(Profile.DemonArt a) {
        demonArt = a;
        rebuildAbilities();
        resetCooldowns();
        selAbility = 0;
    }

    public void setPath(Profile.Path p) {
        path = p;
        isDemon = path == Profile.Path.DEMON;
        name = isDemon ? "Akuma" : "Slayer";
        runSpeed = isDemon ? 360 : 345;
        rebuildAbilities();
        resetCooldowns();
        selAbility = 0;
    }

    private void rebuildAbilities() {
        abilities.clear();
        if (isDemon) {
            if (demonArt == Profile.DemonArt.FOREST_HAND) abilities.addAll(Ability.forestHand());
            else if (demonArt == Profile.DemonArt.SWAMP) abilities.addAll(Ability.swampDemonArt(true));
            else if (demonArt == Profile.DemonArt.SUSUMARU) abilities.addAll(Ability.susumaruArt());
            else if (demonArt == Profile.DemonArt.YAHABA) abilities.addAll(Ability.yahabaArt());
            else if (demonArt == Profile.DemonArt.COMBUSTIBLE_BLOOD) abilities.addAll(Ability.combustibleBlood(slayerRank));
            else abilities.addAll(Ability.crimsonHunger());
        } else if (style == Profile.Style.WATER) abilities.addAll(Ability.water(slayerRank));
        else if (style == Profile.Style.FLAME) abilities.addAll(Ability.flame(slayerRank));
        else if (style == Profile.Style.WIND) abilities.addAll(Ability.wind(slayerRank));
    }

    private void resetCooldowns() {
        cds = new float[Math.max(4, abilities.size())];
    }

    @Override
    protected boolean jumpHeld() { return Game.input.down(Input.JUMP); }

    @Override
    protected boolean isDashing() { return dashT > 0; }

    public void consume(float amt) {
        sp -= amt;
        if (amt > 0) spRecoverDelay = 2f;
        if (sp <= 0) {
            sp = 0;
            spLock = 3f;
        }
    }

    public void drainSpHalf() {
        sp = Math.max(0, sp - maxSp * 0.5f);
        spRecoverDelay = 2f;
        if (sp <= 0) spLock = 3f;
        popupSpDrain();
    }

    private void popupSpDrain() {
        World wRef = lastWorld;
        if (wRef != null) wRef.popup(x, y - h - 10, "SP DRAINED", new Color(255, 120, 120));
    }

    public boolean tryNezukoRescue(World w) {
        if (nezukoRescueUsed || nezukoMode || path != Profile.Path.SLAYER || w == null || w.slayerLevelIndex < 23) return false;
        becomeNezuko(w);
        return true;
    }

    private void becomeNezuko(World w) {
        nezukoMode = true;
        nezukoRescueUsed = true;
        path = Profile.Path.DEMON;
        isDemon = true;
        demonArt = Profile.DemonArt.COMBUSTIBLE_BLOOD;
        name = "Nezuko";
        runSpeed = 370;
        maxHp = 110;
        hp = maxHp;
        sp = maxSp;
        dead = false;
        deathT = 0;
        invulnT = 1.4f;
        stunT = staggerT = 0;
        guarding = false;
        recharging = false;
        wisteriaT = 0;
        limbFrontArm = limbBackArm = limbFrontLeg = limbBackLeg = true;
        rebuildAbilities();
        resetCooldowns();
        selAbility = 0;
        w.parts.burst(Particles.FIRE, x, y - h * 0.35f, 56, 520, 0.9f, 14, AbilityCast.NEZUKO_HI, -170, 0.92f);
        w.parts.burst(Particles.EMBER, x, y - h * 0.35f, 42, 420, 0.75f, 9, AbilityCast.NEZUKO, -120, 0.94f);
        for (Fighter e : w.enemies) {
            if (e.dead) continue;
            float dx = e.x - x;
            float dy = e.y - y;
            float d = Math.max(1, FMath.dist(0, 0, dx, dy));
            if (d > 330) continue;
            float k = 1 - d / 330f;
            e.vx += dx / d * (560 + 520 * k);
            e.vy = Math.min(e.vy, -260 - 330 * k);
            e.staggerT = Math.max(e.staggerT, 0.28f + 0.22f * k);
            e.onGround = false;
        }
        w.cam.shake(12, 0.45f);
        w.banner("Nezuko bursts forth", "Combustible Blood awakens.");
    }

    public World lastWorld;

    public void startFlip() {
        if (!legBlocked()) {
            flipActive = true;
            flipT = 0.001f;
        }
    }

    public void update(World w, float dt, InputProvider in) {
        lastWorld = w;
        timeSinceHurt();
        for (int i = 0; i < cds.length; i++) cds[i] -= dt;
        dashCd -= dt;
        attackCd -= dt;
        atkT -= dt;
        comboWindow -= dt;
        nezukoLegPoseT -= dt;
        facingLockT -= dt;
        if (nezukoLegPoseT <= 0) nezukoLegPose = 0;
        if (comboWindow <= 0 && !attacking()) comboStep = 0;
        hopT -= dt;
        if (flipActive) {
            flipT += dt / 0.42f;
            if (flipT >= 1) flipActive = false;
        }
        chargeT = Math.max(0, chargeT - dt);
        if (slamPending) {
            slamAge += dt;
            if (!onGround && vy > 150 && !attacking()) startAttack(0.45f);
            if (onGround && slamAge > 0.06f && vy >= -10) {
                AbilityCast.slam(w, this);
                slamPending = false;
            }
        }

        if (spRecoverDelay > 0) spRecoverDelay -= dt;
        if (spLock > 0) spLock -= dt;
        else if (spRecoverDelay <= 0) sp = Math.min(maxSp, sp + dt * 11 * w.fearSlowdown());
        if (isDemon) {
            demonRegen(w, dt, 3.5f);
            if (!burning && FMath.chance(dt * 5))
                w.parts.spawn(Particles.SMOKE, x + FMath.rand(-10, 10), y + FMath.rand(-30, 20),
                        FMath.rand(-15, 15), FMath.rand(-45, -15), 0.7f, 10, new Color(120, 20, 35), -20, 0.97f);
        }

        boolean wantsRecharge = !dead && in.down(Input.RECHARGE) && dashT <= 0 && atkT <= 0 && !guarding
                && !(w.level.underwater && !isDemon);
        if (wantsRecharge) setRecharging(true);
        else stopRecharge();
        tickRecharge(w, dt);

        boolean locked = dead || stunned() || immobile() || recharging;
        if (locked) guarding = false;

        float ix = 0;
        if (!locked && !guarding) {
            if (in.down(Input.LEFT)) ix -= 1;
            if (in.down(Input.RIGHT)) ix += 1;
        }
        if (facingLockT <= 0) face(ix != 0 ? ix : vx);

        if (!locked && !guarding && staggerT <= 0 && dashT <= 0) {
            float target = ix * runSpeed * legSpeedMult();
            if (w.playerMovingTowardFear(ix)) target *= w.fearSlowdown();
            float accel = onGround ? 3000 : 1900;
            if (attacking() && onGround) target = 0;
            vx = FMath.approach(vx, target, accel * dt * (ix == 0 ? 1.25f : 1));
        } else {
            vx = FMath.approach(vx, 0, (onGround ? 3200 : 700) * dt);
        }

        if (isDemon && legsMissing() == 1 && onGround && Math.abs(ix) > 0 && hopT <= 0 && !locked && !guarding) {
            vy = -235;
            hopT = 0.52f;
            w.parts.burst(Particles.CIRCLE, x, bottom(), 4, 70, 0.3f, 6, new Color(200, 195, 185), 60, 0.93f);
        }

        if (w.level.underwater && !locked && !guarding && staggerT <= 0) {
            float swim = 0;
            if (in.down(Input.JUMP)) swim -= 1;
            if (in.down(Input.DOWN)) swim += 1;
            vy = FMath.approach(vy, swim * 330, 1450 * dt);
            if (FMath.chance(dt * (Math.abs(ix) > 0 || swim != 0 ? 28 : 8)))
                w.parts.spawn(Particles.CIRCLE, x + FMath.rand(-10, 10), y + FMath.rand(-28, 28),
                        FMath.rand(-28, 28), FMath.rand(-80, -20), 0.75f, 4, new Color(130, 205, 210), -20, 0.98f);
        }

        if (!locked && !guarding && in.pressed(Input.JUMP)) jumpBufT = 0.12f;
        jumpBufT -= dt;
        coyoteT -= dt;
        if (jumpBufT > 0 && !locked && !guarding && staggerT <= 0 && dashT <= 0 && atkT <= 0) {
            if (onGround || coyoteT > 0) {
                vy = -780;
                jumpsUsed = 1;
                jumpBufT = 0;
                coyoteT = 0;
                squash = -0.4f;
                w.parts.burst(Particles.CIRCLE, x, bottom(), 5, 70, 0.3f, 6, new Color(220, 220, 210), 40, 0.92f);
            } else if (touchingWall != 0 && !legBlocked()) {
                consume(6);
                int wd = touchingWall;
                vy = -740;
                vx = -wd * 430;
                facingRight = wd < 0;
                jumpsUsed = 1;
                airDashUsed = false;
                jumpBufT = 0;
                w.parts.burst(Particles.CIRCLE, x + wd * w2(), y, 9, 150, 0.35f, 7, new Color(210, 205, 195), 60, 0.9f);
                w.cam.shake(3, 0.1f);
            } else if (jumpsUsed < maxJumps && !legBlocked()) {
                consume(6);
                vy = -700;
                jumpsUsed++;
                jumpBufT = 0;
                startFlip();
                w.parts.ring(x, y + h * 0.35f, new Color(255, 255, 255, 130));
            }
        }

        wallSliding = false;
        if (!locked && !guarding && touchingWall != 0 && !onGround && vy > 0 && ix == touchingWall && !legBlocked()) {
            wallSliding = true;
            vy = Math.min(vy, 165);
            if (FMath.chance(0.35f))
                w.parts.spawn(Particles.CIRCLE, x + touchingWall * w2(), y + FMath.rand(-20, 20),
                        -touchingWall * 30, FMath.rand(20, 70), 0.4f, 6, new Color(200, 195, 185), 30, 0.95f);
        }
        if (!locked && in.down(Input.DOWN) && !onGround) vy += 1500 * dt;
        if (!locked && in.pressed(Input.DOWN) && onGround && w.onOneWay(this)) dropThrough();

        if (!locked && !guarding && in.pressed(Input.DASH) && dashCd <= 0 && (onGround || !airDashUsed) && !legBlocked()) {
            consume(5);
            dashT = 0.16f;
            dashCd = 0.75f;
            dashDir = ix != 0 ? ix : facing();
            if (dashDir != facing()) facingRight = dashDir > 0;
            invulnT = Math.max(invulnT, 0.18f);
            if (!onGround) airDashUsed = true;
            w.parts.burst(Particles.SPARK, x, y + h * 0.05f, 12, 320, 0.25f, 7,
                    isDemon ? new Color(255, 90, 110) : new Color(180, 230, 255), 0, 0.9f);
        }
        if (dashT > 0) {
            dashT -= dt;
            vx = dashDir * 950;
            vy = 0;
            trailT -= dt;
            if (trailT <= 0) {
                trailT = 0.02f;
                w.ghosts.add(new Ghost(x, bottom(), h, path, style, facing()));
            }
        }

        guarding = !locked && in.down(Input.GUARD) && dashT <= 0 && atkT <= 0;

        if (!locked && !guarding && in.pressed(Input.ATTACK) && attackCd <= 0 && dashT <= 0 && canMelee()) {
            doAttack(w);
        }
        if (attacking() && !comboHitApplied && atkT < atkDur * 0.55f) {
            comboHitApplied = true;
            applyMelee(w);
        }

        if (!locked && !guarding) handleAbilityInput(w, in);

        boolean wasGround = onGround;
        physics(w, dt);
        if (wasGround && !onGround && vy >= 0) coyoteT = 0.09f;
        if (onGround) {
            jumpsUsed = 0;
            airDashUsed = false;
        }
        groundYForShadow = w.shadowGround(this);

        if (top() > w.cam.y + Game.VIEW_H + 140 || top() > w.level.h + 80) w.playerFell();

        if (!dead && burning && FMath.chance(dt * 34))
            w.parts.spawn(Particles.EMBER, x + FMath.rand(-this.w * 0.6f, this.w * 0.6f), y + FMath.rand(-this.h * 0.55f, this.h * 0.45f),
                    FMath.rand(-30, 30), FMath.rand(-170, -70), 0.5f, 8, new Color(255, 140, 50), -80, 0.95f);
    }

    public void nezukoLegPose(int pose, float time) {
        if (!nezukoMode) return;
        nezukoLegPose = pose;
        nezukoLegPoseT = Math.max(nezukoLegPoseT, time);
    }

    public void lockFacing(float time) {
        facingLockT = Math.max(facingLockT, time);
    }

    private float w2() { return w / 2f; }

    private boolean airDashUsed;

    private void doAttack(World w) {
        consume(4);
        noteAction();
        swingSide *= -1;
        boolean claws = isDemon;
        float[] dur = claws ? new float[]{0.2f, 0.2f, 0.34f} : new float[]{0.26f, 0.26f, 0.42f};
        float[] cdv = claws ? new float[]{0.16f, 0.16f, 0.34f} : new float[]{0.14f, 0.14f, 0.3f};
        int step = comboStep;
        startAttack(dur[step]);
        attackCd = cdv[step] * armCdMult();
        comboWindow = 0.5f;
        if (onGround) vx += facing() * (step == 2 ? 260 : 130);
        w.cam.shake(step == 2 ? 3.5f : 1.6f, 0.08f);
    }

    private void applyMelee(World w) {
        boolean claws = isDemon;
        float reach = claws ? 64 : 80;
        float[] dmg = claws ? new float[]{8, 8, 15} : new float[]{12, 12, 21};
        float slashDmg = dmg[comboStep] * (blazingSlamSlash ? 3f : 1f);
        blazingSlamSlash = false;
        float kb = comboStep == 2 ? 330 : 170;
        float up = comboStep == 2 ? 300 : 90;
        Color c = slashColor();
        double angBase = comboStep == 0 ? Math.toRadians(facingRight ? -35 : 215)
                : comboStep == 1 ? Math.toRadians(facingRight ? 10 : 170)
                : Math.toRadians(facingRight ? 300 : 240);
        double sweep = comboStep == 2 ? Math.toRadians(330) : Math.toRadians(150);
        w.meleeStrike(this, reach, slashDmg, kb, up, angBase + sweep / 2, sweep, c);
        if (comboStep == 2) comboStep = 0;
        else comboStep++;
    }

    public Color slashColor() {
        if (nezukoMode) return AbilityCast.NEZUKO_HI;
        if (isDemon) return new Color(235, 60, 85);
        return switch (style) {
            case WATER -> new Color(80, 160, 255);
            case FLAME -> new Color(255, 120, 40);
            case WIND -> new Color(80, 210, 115);
            default -> new Color(190, 190, 210);
        };
    }

    public Color bladeColor() {
        if (isDemon) return new Color(230, 225, 235);
        return switch (style) {
            case WATER -> new Color(90, 165, 255);
            case FLAME -> new Color(255, 90, 45);
            case WIND -> new Color(70, 205, 110);
            default -> new Color(240, 240, 248);
        };
    }

    private void handleAbilityInput(World w, InputProvider in) {
        int n = abilities.size();
        if (n == 0) return;
        if (in.pressed(Input.PREV)) selAbility = (selAbility + n - 1) % n;
        if (in.pressed(Input.NEXT)) selAbility = (selAbility + 1) % n;
        if (in.pressed(Input.SLOT1)) selAbility = 0;
        if (in.pressed(Input.SLOT2)) selAbility = Math.min(1, n - 1);
        if (in.pressed(Input.SLOT3)) selAbility = Math.min(2, n - 1);
        if (in.pressed(Input.SLOT4)) selAbility = Math.min(3, n - 1);
        if (in.pressed(Input.SLOT5)) selAbility = Math.min(4, n - 1);
        if (in.pressed(Input.SLOT6)) selAbility = Math.min(5, n - 1);
        if (in.pressed(Input.SLOT7)) selAbility = Math.min(6, n - 1);
        if (in.pressed(Input.CAST)) {
            Ability a = abilities.get(selAbility);
            if (cds[selAbility] <= 0 && sp >= a.cost && dashT <= 0) {
                consume(a.cost);
                cds[selAbility] = a.cooldown;
                w.castAbility(this, a);
            } else if (sp < a.cost) {
                w.popup(x, y - h, "No SP", new Color(120, 180, 255));
            }
        }
    }

    public void forceCast(World w, int idx) {
        if (idx >= abilities.size()) return;
        w.castAbility(this, abilities.get(idx));
    }

    @Override
    public void update(World world, float dt) {
    }

    public void render(Graphics2D g) {
        if (dead) return;
        if (invulnT > 0 && (int) (invulnT * 24) % 2 == 0)
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.45f));
        drawShadow(g);
        Graphics2D gg = (Graphics2D) g.create();
        poseTransform(gg);
        if (isDemon) drawDemon(gg);
        else drawSlayer(gg);
        gg.dispose();
        g.setComposite(AlphaComposite.SrcOver);
    }

    public float groundYForShadow = 99999;

    private void drawShadow(Graphics2D g) {
        float gy = groundYForShadow;
        if (gy > 9000) return;
        g.setColor(new Color(0, 0, 0, 70));
        float sw = w * (1.6f - Math.min(1, (gy - bottom()) / 400));
        g.fillOval((int) (x - sw / 2), (int) (gy - 5), (int) sw, 9);
    }

    private float squashX() { return 1 + squash * 0.22f; }

    private float squashY() { return 1 - squash * 0.28f; }

    private void poseTransform(Graphics2D g) {
        float fx = x, fy = bottom();
        g.translate(fx, fy);
        g.scale(squashX(), squashY());
        if (stunT > 0) {
            g.rotate(FMath.sin(Game.time * 13) * 0.16f);
        } else if (dashT > 0) g.rotate(facing() * 0.32f);
        else if (wallSliding) g.rotate(-facing() * 0.12f);
        else if (slamPending && !onGround && vy > 150) g.rotate(facing() * 0.45f);
        else if (!onGround && !flipActive) g.rotate(FMath.clamp(vy * 0.00012f, -0.1f, 0.12f) * facing());
        g.translate(-fx, -fy);
        if (flipActive) {
            float k = FMath.easeInOut(FMath.clamp(flipT, 0, 1));
            g.rotate(k * (float) Math.PI * 2 * facing(), x, y);
        }
    }

    private boolean sheathed() {
        return timeSinceAction > 5f && !attacking();
    }

    private void drawSlayer(Graphics2D g) {
        float speedF = FMath.clamp(Math.abs(vx) / runSpeed, 0, 1);
        float ph = animPhase * (float) Math.PI * 2;
        float bob = onGround ? Math.abs(FMath.sin(ph)) * -3f * speedF + FMath.sin(Game.time * 2.1f) * 1.1f * (1 - speedF) : 0;
        float tuck = flipActive ? FMath.sin(FMath.clamp(flipT, 0, 1) * (float) Math.PI) : 0;
        float feetF = y + h * 0.5f;
        float crouch = recharging ? 1f : 0f;
        float hipY = feetF - h * 0.44f + bob + crouch * 9f;
        float shY = feetF - h * 0.78f + bob + crouch * 5f;
        float legLen = h * 0.43f * (1 - 0.30f * tuck - 0.22f * crouch);
        float legSwing = FMath.lerp(FMath.sin(ph) * 0.75f * speedF, -2.35f, tuck);
        float airPose = onGround ? 0 : FMath.clamp(vy * 0.001f, -0.6f, 0.6f);

        Color hair = new Color(74, 43, 48);
        Color skin = new Color(242, 203, 158);
        Color cloth = new Color(36, 36, 54);
        Color haori = style == Profile.Style.FLAME ? new Color(122, 44, 36)
                : style == Profile.Style.WIND ? new Color(44, 118, 70) : new Color(46, 104, 92);

        boolean swordInBack = !sheathed() && swingSide < 0;
        boolean swordInFront = !sheathed() && swingSide > 0;

        legP(g, x - facing() * 2, hipY, -legSwing, legLen, 6, cloth.darker(), limbBackLeg);
        legP(g, x + facing() * 2, hipY, legSwing, legLen, 6, cloth, limbFrontLeg);

        if (!swordInBack && !guarding)
            arm(g, x - facing() * 7, shY + 5,
                    FMath.lerp(-FMath.sin(ph) * 0.5f * speedF - airPose * 0.4f, -2.1f, tuck),
                    27 * (1 - 0.3f * tuck), 5, haori.darker(), skin, limbBackArm);

        torso(g, shY, hipY, 9, haori, true);

        head(g, x + facing() * 1.5f, shY - 11, skin, hair, false);

        if (guarding) {
            swordBlock(g, x + facing() * 6, shY + 5, haori.darker(), bladeColor(), 1.125f, 5);
        } else if (swordInFront) {
            drawSwordArm(g, x + facing() * 6, shY + 5, false, skin, haori.darker(), tuck);
        } else {
            if (sheathed()) drawScabbard(g, hipY);
            idleArm(g, x + facing() * 7, shY + 5, ph, speedF, 5, haori, skin, tuck);
        }
        if (swordInBack && !guarding) drawSwordArm(g, x - facing() * 6, shY + 5, true, skin, haori.darker(), tuck);

        drawGuardArc(g);
        drawStunStars(g);
        if (guardAnim <= 0 && stunT <= 0 && stunnedEyes()) dizzyEyes(g, x + facing() * 2, shY - 11);
    }

    private boolean stunnedEyes() { return stunT > 0; }

    private void drawDemon(Graphics2D g) {
        float speedF = FMath.clamp(Math.abs(vx) / runSpeed, 0, 1);
        float ph = animPhase * (float) Math.PI * 2;
        float bob = onGround ? Math.abs(FMath.sin(ph)) * -3f * speedF + FMath.sin(Game.time * 2.4f) * 1.3f * (1 - speedF) : 0;
        float tuck = flipActive ? FMath.sin(FMath.clamp(flipT, 0, 1) * (float) Math.PI) : 0;
        float feetF = y + h * 0.5f;
        float crouch = recharging ? 1f : 0f;
        float hipY = feetF - h * 0.44f + bob + crouch * 9f;
        float shY = feetF - h * 0.78f + bob + crouch * 5f;
        float legLen = h * 0.43f * (1 - 0.30f * tuck - 0.22f * crouch);
        float legSwing = FMath.lerp(FMath.sin(ph) * 0.8f * speedF, -2.35f, tuck);
        float airPose = onGround ? 0 : FMath.clamp(vy * 0.001f, -0.6f, 0.6f);

        Color skin = nezukoMode ? new Color(255, 229, 200) : new Color(214, 206, 218);
        Color hairC = nezukoMode ? new Color(24, 18, 22) : new Color(238, 238, 244);
        Color pants = nezukoMode ? skin : new Color(44, 32, 54);
        Color nezukoHaori = new Color(15, 15, 18);

        boolean swordInBack = swingSide < 0;
        boolean swordInFront = swingSide > 0;

        if (nezukoMode) nezukoHair(g, x + facing() * 1.5f, shY - 11, hairC);

        if (nezukoMode && nezukoLegPose > 0) drawNezukoPoseLegs(g, hipY, legLen, legSwing, pants);
        else {
            legClawP(g, x - facing() * 2, hipY, -legSwing, legLen, 7, pants, limbBackLeg);
            legClawP(g, x + facing() * 2, hipY, legSwing, legLen, 7, pants, limbFrontLeg);
        }

        if (!swordInBack && !guarding)
            idleClawArm(g, x - facing() * 7, shY + 5, true, ph, speedF, nezukoMode ? nezukoHaori : skin.darker(), tuck);

        torso(g, shY, hipY, 10, nezukoMode ? new Color(248, 188, 207) : skin, nezukoMode);

        head(g, x + facing() * 1.5f, shY - 11, skin, hairC, true);

        if (guarding) {
            if (nezukoMode) nezukoBlockHands(g, shY, skin, nezukoHaori);
            else blockHands(g, shY, skin, 6);
        } else if (swordInFront) {
            clawArmSwing(g, x + facing() * 7, shY + 5, false, nezukoMode ? nezukoHaori : skin, tuck);
            idleClawArm(g, x - facing() * 7, shY + 5, true, ph, speedF, nezukoMode ? nezukoHaori : skin.darker(), tuck);
        } else {
            clawArmSwing(g, x + facing() * 7, shY + 5, false, nezukoMode ? nezukoHaori : skin, tuck);
        }
        if (swordInBack && !guarding) clawArmSwing(g, x - facing() * 6, shY + 5, true, nezukoMode ? nezukoHaori : skin.darker(), tuck);

        drawGuardArc(g);
        drawStunStars(g);
    }

    private void idleClawArm(Graphics2D g, float sx, float sy, boolean back, float ph, float speedF, Color c, float tuck) {
        boolean present = back ? limbBackArm : limbFrontArm;
        if (!present) {
            stubP(g, sx, sy + 7, 5, c);
            return;
        }
        float sw = FMath.lerp(FMath.sin(ph + (back ? (float) Math.PI : 0)) * 0.5f * speedF, -2.1f, tuck);
        float armLen = 26 * (1 - 0.3f * tuck);
        float ex = sx + FMath.sin(sw) * armLen;
        float ey = sy + armLen + FMath.cos(sw) * 4;
        limb(g, sx, sy, ex, ey, 5, c);
        double a = Math.atan2(ey - sy, ex - sx);
        if (nezukoMode) {
            nezukoHand(g, ex, ey, a, new Color(255, 229, 200), 0.55f);
        } else {
            g.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(c.brighter());
            g.drawLine((int) ex, (int) ey, (int) (ex + Math.cos(a) * 6), (int) (ey + Math.sin(a) * 6));
        }
    }

    private void nezukoHair(Graphics2D g, float hx, float hy, Color hair) {
        g.setColor(hair);
        Polygon back = new Polygon();
        back.addPoint((int) hx - 11, (int) hy - 8);
        back.addPoint((int) hx + 11, (int) hy - 8);
        back.addPoint((int) hx + 16, (int) hy + 48);
        back.addPoint((int) hx + 4, (int) hy + 63);
        back.addPoint((int) hx - 12, (int) hy + 44);
        g.fillPolygon(back);
        g.fillRect((int) hx - 13, (int) hy + 10, 8, 43);
        g.fillRect((int) hx + 6, (int) hy + 12, 8, 46);
        g.setColor(new Color(226, 112, 42, 210));
        g.fillRect((int) hx - 13, (int) hy + 40, 8, 15);
        g.fillRect((int) hx + 6, (int) hy + 44, 8, 16);
    }

    private void torso(Graphics2D g, float shY, float hipY, float halfW, Color c, boolean pattern) {
        Path2D.Float p = new Path2D.Float();
        p.moveTo(x - halfW + 2, shY);
        p.lineTo(x + halfW - 2, shY);
        p.quadTo(x + halfW + 1, shY + (hipY - shY) * 0.6f, x + halfW - 4, hipY + 4);
        p.lineTo(x - halfW + 4, hipY + 4);
        p.quadTo(x - halfW - 1, shY + (hipY - shY) * 0.6f, x - halfW + 2, shY);
        p.closePath();
        g.setColor(c);
        g.fill(p);
        if (pattern) {
            if (nezukoMode) {
                float beltY = shY + (hipY - shY) * 0.58f;
                g.setColor(new Color(18, 18, 22));
                g.fillRect((int) (x - halfW - 1), (int) shY + 1, 5, (int) (hipY - shY + 5));
                g.fillRect((int) (x + halfW - 4), (int) shY + 1, 5, (int) (hipY - shY + 5));
                g.setColor(new Color(178, 32, 44));
                g.fillRect((int) (x - halfW + 1), (int) beltY, (int) (halfW * 2 - 2), 7);
                g.setColor(new Color(245, 238, 230));
                for (int i = 0; i < 4; i++) {
                    int bx = (int) (x - halfW + 2 + i * 5);
                    g.fillRect(bx, (int) beltY, 3, 3);
                    g.fillRect(bx + 3, (int) beltY + 3, 3, 4);
                }
                g.setColor(new Color(255, 248, 238));
                g.fillRect((int) (x - halfW + 3), (int) (hipY + 1), (int) (halfW * 2 - 6), 3);
                g.setColor(new Color(255, 220, 235, 190));
                g.setStroke(new BasicStroke(1.2f));
                g.draw(p);
            } else {
                g.setColor(new Color(0, 0, 0, 55));
                for (int i = 0; i < 2; i++) g.fillRect((int) (x - halfW + 2 + i * 6), (int) (shY + 4 + i * 10), 4, 4);
                g.setColor(c.brighter());
                g.setStroke(new BasicStroke(1.8f));
                g.draw(p);
            }
        } else {
            g.setColor(new Color(120, 30, 45, 130));
            g.setStroke(new BasicStroke(1.3f));
            g.drawLine((int) (x - 3), (int) (shY + 7), (int) (x - 5), (int) (shY + 16));
            g.drawLine((int) (x + 4), (int) (shY + 6), (int) (x + 6), (int) (shY + 14));
        }
    }

    private void head(Graphics2D g, float hx, float hy, Color skin, Color hair, boolean demon) {
        g.setColor(skin);
        g.fillOval((int) hx - 9, (int) hy - 10, 19, 20);
        g.setColor(hair);
        Polygon spikes = new Polygon();
        spikes.addPoint((int) hx - 10, (int) hy - 1);
        spikes.addPoint((int) hx - 7, (int) hy - 12);
        spikes.addPoint((int) hx - 2, (int) hy - 8);
        spikes.addPoint((int) hx + 3, (int) hy - 13);
        spikes.addPoint((int) hx + 8, (int) hy - 8);
        spikes.addPoint((int) hx + 10, (int) hy - 2);
        spikes.addPoint((int) hx + 4, (int) hy - 6);
        spikes.addPoint((int) hx - 4, (int) hy - 5);
        g.fillPolygon(spikes);
        if (demon) {
            if (nezukoMode) {
                float eyeY = hy - 1;
                float eyeL = hx - 4.5f, eyeR = hx + 4.5f;
                Glow.blob(g, eyeL, eyeY, 5, new Color(255, 60, 170, 48));
                Glow.blob(g, eyeR, eyeY, 5, new Color(255, 60, 170, 48));
                g.setColor(new Color(255, 245, 250));
                g.fillOval((int) eyeL - 3, (int) eyeY - 3, 6, 6);
                g.fillOval((int) eyeR - 3, (int) eyeY - 3, 6, 6);
                g.setColor(new Color(215, 64, 142));
                g.fillOval((int) eyeL - 2, (int) eyeY - 2, 4, 4);
                g.fillOval((int) eyeR - 2, (int) eyeY - 2, 4, 4);
                g.setColor(new Color(75, 126, 72));
                g.fillRect((int) hx - 8, (int) hy + 4, 16, 5);
                g.setColor(new Color(105, 46, 30));
                g.fillOval((int) hx - 10, (int) hy + 5, 3, 3);
                g.fillOval((int) hx + 7, (int) hy + 5, 3, 3);
                return;
            }
            g.setColor(new Color(255, 214, 74));
            g.fillOval((int) hx + facing() * 3 - 3, (int) hy - 2, 6, 4);
            g.setColor(new Color(120, 60, 10));
            g.fillRect((int) hx + facing() * 3 - 1, (int) hy - 2, 2, 4);
            g.setColor(new Color(255, 255, 255, 220));
            g.fillPolygon(new int[]{(int) hx + facing() * 6, (int) hx + facing() * 8, (int) hx + facing() * 4},
                    new int[]{(int) hy + 6, (int) hy + 6, (int) hy + 9}, 3);
        } else {
            if (stunT > 0) {
                dizzyEyes(g, hx, hy);
            } else {
                g.setColor(new Color(60, 40, 40));
                g.fillRect((int) hx + facing() * 3 - 2, (int) hy - 2, 3, 3);
                g.fillRect((int) hx - facing() * 2 - 1, (int) hy - 2, 2, 3);
                g.setColor(new Color(196, 120, 100, 170));
                g.fillRect((int) hx + facing() * 2 - 3, (int) hy - 7, 6, 2);
            }
        }
    }

    private void dizzyEyes(Graphics2D g, float hx, float hy) {
        g.setColor(new Color(50, 45, 60));
        g.setStroke(new BasicStroke(1.4f));
        int r = facing() > 0 ? 2 : 2;
        g.drawArc((int) hx + facing() * 3 - r - 1, (int) hy - 4, r * 2 + 2, r * 2 + 2, 0, 300);
        g.drawArc((int) hx - facing() * 2 - r - 1, (int) hy - 4, r * 2, r * 2, 90, 280);
    }

    private void legP(Graphics2D g, float hx, float hy, float swing, float len, float wid, Color c, boolean present) {
        if (!present) {
            stubP(g, hx, hy + len * 0.3f, wid, c);
            return;
        }
        float fx = hx + FMath.sin(swing) * len * 0.62f;
        float fy = hy + len + FMath.cos(swing) * 5;
        limb(g, hx, hy, fx, fy - 4, wid, c);
        g.setColor(c.darker());
        g.fillRect((int) (fx - 3), (int) (fy - 3), 6, 3);
    }

    private void legClawP(Graphics2D g, float hx, float hy, float swing, float len, float wid, Color c, boolean present) {
        if (!present) {
            stubP(g, hx, hy + len * 0.3f, wid, c);
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

    private void drawNezukoPoseLegs(Graphics2D g, float hipY, float legLen, float legSwing, Color skin) {
        float backX = x - facing() * 2;
        float frontX = x + facing() * 2;
        if (nezukoLegPose == 1) {
            angledLeg(g, backX, hipY, facingRight ? Math.toRadians(160) : Math.toRadians(20), legLen * 0.78f, 7, skin, limbBackLeg);
            angledLeg(g, frontX, hipY, facingRight ? Math.toRadians(-10) : Math.toRadians(190), legLen * 0.98f, 7, skin, limbFrontLeg);
        } else if (nezukoLegPose == 2) {
            legClawP(g, backX, hipY, -legSwing * 0.35f, legLen, 7, skin, limbBackLeg);
            angledLeg(g, frontX, hipY, facingRight ? Math.toRadians(-45) : Math.toRadians(225), legLen * 0.92f, 7, skin, limbFrontLeg);
        } else if (nezukoLegPose == 3) {
            legClawP(g, backX, hipY, -0.35f, legLen * 0.82f, 7, skin, limbBackLeg);
            angledLeg(g, frontX, hipY, facingRight ? Math.toRadians(74) : Math.toRadians(106), legLen * 1.16f, 8, skin, limbFrontLeg);
        } else {
            float a = Game.time * 18f;
            angledLeg(g, backX, hipY, a + Math.PI, legLen * 0.82f, 7, skin, limbBackLeg);
            angledLeg(g, frontX, hipY, a, legLen * 0.98f, 7, skin, limbFrontLeg);
        }
    }

    private void angledLeg(Graphics2D g, float hx, float hy, double angle, float len, float wid, Color c, boolean present) {
        if (!present) {
            stubP(g, hx, hy + len * 0.3f, wid, c);
            return;
        }
        float fx = hx + (float) Math.cos(angle) * len;
        float fy = hy + (float) Math.sin(angle) * len;
        limb(g, hx, hy, fx, fy, wid, c);
        g.setStroke(new BasicStroke(1.1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(c.brighter());
        double foot = angle + (facingRight ? 0.18 : -0.18);
        for (int i = -1; i <= 1; i++)
            g.drawLine((int) fx, (int) fy, (int) (fx + Math.cos(foot + i * 0.24) * 5), (int) (fy + Math.sin(foot + i * 0.24) * 5));
    }

    private void stubP(Graphics2D g, float sx, float sy, float wid, Color c) {
        g.setColor(c);
        g.fillRoundRect((int) sx - (int) wid / 2, (int) sy - 2, (int) wid, 7, 3, 3);
        g.setColor(new Color(160, 30, 45));
        g.fillOval((int) (sx - wid / 2), (int) sy + 2, (int) wid, 3);
    }

    private void arm(Graphics2D g, float sx, float sy, float swing, float len, float wid, Color sleeve, Color skin, boolean present) {
        if (!present) {
            stubP(g, sx, sy + len * 0.4f, wid, sleeve != null ? sleeve : skin);
            return;
        }
        float ex = sx + FMath.sin(swing) * len * 0.7f;
        float ey = sy + len + FMath.cos(swing) * 4;
        if (sleeve != null) limb(g, sx, sy, ex, ey, wid, sleeve);
        else limb(g, sx, sy, ex, ey, wid, skin);
    }

    private void idleArm(Graphics2D g, float sx, float sy, float ph, float speedF, float wid, Color sleeve, Color skin, float tuck) {
        float sw = FMath.lerp(FMath.sin(ph) * 0.5f * speedF, -2.1f, tuck);
        arm(g, sx, sy, sw, 27 * (1 - 0.3f * tuck), wid, sleeve, skin, true);
    }

    private void drawSwordArm(Graphics2D g, float sx, float sy, boolean backArm, Color skin, Color sleeve, float tuck) {
        if (backArm && !limbBackArm) {
            stubP(g, sx, sy + 6, 5, sleeve);
            return;
        }
        if (!backArm && !limbFrontArm) {
            stubP(g, sx, sy + 6, 5, sleeve);
            return;
        }
        if (chargeT > 0) {
            // charging: sword held high overhead
            double armAng = Math.toRadians(facingRight ? -115 : 295);
            double wa = facingRight ? armAng : Math.PI - armAng;
            float ca = (float) Math.cos(wa), sa = (float) Math.sin(wa);
            float hx = sx + (float) Math.cos(facingRight ? armAng : Math.PI - armAng) * 25;
            float hy = sy + (float) Math.sin(facingRight ? armAng : Math.PI - armAng) * 25;
            limb(g, sx, sy, hx, hy, 5, sleeve);
            drawKatanaBlade(g, hx, hy, ca, sa, 1);
            return;
        }
        double prog = attacking() ? 1 - atkT / atkDur : -1;
        double armAng;
        if (prog >= 0) {
            double s = FMath.easeOut((float) prog);
            armAng = switch (comboStep % 3) {
                case 0 -> Math.toRadians(200 - 250 * s);
                case 1 -> Math.toRadians(-30 - 200 * s);
                default -> Math.toRadians(140 - 420 * s);
            };
        } else {
            float swing = FMath.sin(animPhase * (float) Math.PI * 2);
            armAng = Math.toRadians(58 + swing * 12 * FMath.clamp(Math.abs(vx) / runSpeed, 0, 1)) + (onGround ? 0 : -0.5f);
        }
        armAng += tuck * -2.2f;
        double twist = prog >= 0 ? FMath.easeOut((float) Math.min(1, prog * 2.6)) : 0;
        double bladeLocal = armAng - (Math.PI / 2) * (1 - twist);
        double wa = facingRight ? bladeLocal : Math.PI - bladeLocal;
        float ca = (float) Math.cos(wa), sa = (float) Math.sin(wa);
        float hx = sx + (float) Math.cos(facingRight ? armAng : Math.PI - armAng) * 25;
        float hy = sy + (float) Math.sin(facingRight ? armAng : Math.PI - armAng) * 25;
        limb(g, sx, sy, hx, hy, 5, sleeve);
        drawKatanaBlade(g, hx, hy, ca, sa, prog >= 0 ? 1 : 0.66f);
    }

    private void drawKatanaBlade(Graphics2D g, float hx, float hy, float ca, float sa, float ext) {
        float len = 60 * ext;
        float tx = hx + ca * 8, ty = hy + sa * 8;
        float bx = tx + ca * len, by = ty + sa * len;
        g.setStroke(new BasicStroke(4.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(new Color(30, 30, 36));
        g.drawLine((int) (hx - ca * 7), (int) (hy - sa * 7), (int) tx, (int) ty);
        g.setColor(new Color(200, 170, 90));
        g.fillRect((int) tx - 2, (int) ty - 2, 5, 5);
        bladeShape(g, tx, ty, bx, by, 1.125f, bladeColor(), !facingRight);
    }

    private void drawScabbard(Graphics2D g, float hipY) {
        float bx = x + facing() * 2, by = hipY + 7;
        float tx = bx - facing() * 14, ty = by + 21;
        g.setStroke(new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(new Color(14, 14, 18));
        g.drawLine((int) bx, (int) by, (int) tx, (int) ty);
        g.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(new Color(38, 38, 46));
        g.drawLine((int) (bx + facing()), (int) (by - 6), (int) bx, (int) (by - 1));
    }

    private void nezukoBlockHands(Graphics2D g, float shY, Color skin, Color sleeve) {
        float fy = shY - 8;
        limb(g, x - facing() * 8, shY + 5, x + facing() * 10, fy, 5, sleeve);
        nezukoHand(g, x + facing() * 10, fy, facingRight ? -0.7 : Math.PI + 0.7, skin, 0.5f);
        limb(g, x + facing() * 8, shY + 5, x + facing() * 16, fy + 6, 5, sleeve);
        nezukoHand(g, x + facing() * 16, fy + 6, facingRight ? 0.2 : Math.PI - 0.2, skin, 0.5f);
    }

    private void nezukoHand(Graphics2D g, float hx, float hy, double angle, Color skin, float ext) {
        g.setColor(skin);
        g.fillOval((int) hx - 3, (int) hy - 3, 6, 6);
        g.setStroke(new BasicStroke(1.1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(new Color(235, 228, 238));
        for (int i = -1; i <= 1; i++) {
            double a = angle + i * 0.32;
            g.drawLine((int) hx, (int) hy, (int) (hx + Math.cos(a) * 12 * ext), (int) (hy + Math.sin(a) * 12 * ext));
        }
    }

    private void clawArmSwing(Graphics2D g, float sx, float sy, boolean back, Color skin, float tuck) {
        boolean present = back ? limbBackArm : limbFrontArm;
        if (!present) {
            stubP(g, sx, sy + 7, 5, skin);
            return;
        }
        double prog = attacking() && ((back && swingSide < 0) || (!back && swingSide > 0)) ? 1 - atkT / atkDur
                : attacking() ? -2 : -1;
        double armAng;
        if (prog >= 0) {
            double s = FMath.easeOut((float) prog);
            armAng = switch (comboStep % 3) {
                case 0 -> Math.toRadians(210 - 260 * s);
                case 1 -> Math.toRadians(-40 - 210 * s);
                default -> Math.toRadians(150 - 430 * s);
            };
        } else {
            float base = attacking() ? 0.9f : 0;
            float swing = FMath.sin(animPhase * (float) Math.PI * 2 + (back ? (float) Math.PI : 0));
            armAng = Math.toRadians(65 + swing * 14 * FMath.clamp(Math.abs(vx) / runSpeed, 0, 1)) + (onGround ? base : -0.55f);
        }
        armAng += tuck * -2.2f;
        double wa = facingRight ? armAng : Math.PI - armAng;
        float hx = sx + (float) Math.cos(wa) * 25, hy = sy + (float) Math.sin(wa) * 25;
        limb(g, sx, sy, hx, hy, 5, skin);
        float ext = (prog >= 0 ? 1 : 0.68f) * (1 - 0.25f * tuck);
        if (nezukoMode) {
            nezukoHand(g, hx, hy, wa, new Color(255, 229, 200), ext);
        } else {
            for (int i = -1; i <= 1; i++) {
                double a = wa + i * 0.32;
                g.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.setColor(new Color(235, 228, 238));
                g.drawLine((int) hx, (int) hy, (int) (hx + Math.cos(a) * 24 * ext), (int) (hy + Math.sin(a) * 24 * ext));
            }
        }
    }

    public void renderGlow(Graphics2D g) {
        if (dead) return;
        renderRechargeGlow(g);
        if (isDemon) {
            Color eye = nezukoMode ? new Color(255, 76, 180, 65) : new Color(255, 205, 70, 190);
            if (nezukoMode) {
                float hx = x + facing() * 1.5f;
                float ey = top() + h * 0.09f;
                Glow.blob(g, hx - 4.5f, ey, 4, eye);
                Glow.blob(g, hx + 4.5f, ey, 4, eye);
            } else {
                Glow.blob(g, x + facing() * 4.5f, top() + h * 0.09f, 6, eye);
                Glow.blob(g, x - facing() * 0.5f, top() + h * 0.09f, 5, eye);
            }
        }
        if (guardAnim > 0) Glow.blob(g, x + facing() * w * 0.65f, top() + h * 0.42f, 22, new Color(255, 235, 170, 150));
    }

    @Override
    protected Color abilityColor() {
        if (isDemon) return demonArt == Profile.DemonArt.FOREST_HAND ? AbilityCast.HAND
                : demonArt == Profile.DemonArt.SWAMP ? new Color(30, 135, 122)
                : demonArt == Profile.DemonArt.SUSUMARU ? new Color(235, 190, 74)
                : demonArt == Profile.DemonArt.YAHABA ? new Color(210, 40, 55)
                : demonArt == Profile.DemonArt.COMBUSTIBLE_BLOOD ? AbilityCast.NEZUKO_HI : AbilityCast.BLOOD;
        return switch (style) {
            case WATER -> AbilityCast.WATER;
            case FLAME -> AbilityCast.FLAME;
            case WIND -> AbilityCast.WIND;
            default -> new Color(210, 220, 235);
        };
    }

    public void menuTick(float dt) {
        atkT -= dt;
        comboWindow -= dt;
        timeSinceAction += dt;
        if (!attacking()) comboStep = 0;
        onGround = true;
        vx = 130;
        animPhase += dt * 2.2f;
        invulnT = 0;
        flashT = 0;
        staggerT = 0;
        stunT = 0;
    }

    public void menuSwing() {
        if (atkT <= 0) {
            comboStep = (comboStep + 1) % 3;
            swingSide *= -1;
            startAttack(0.3f);
        }
    }

    public void noteHurt() {
    }
}
