package game;

import java.awt.*;
import java.util.ArrayList;

/** A peaceful Hashira guest on the Mugen Train; his full Flame forms remain available to him. */
public class Rengoku extends SlayerEnemy {
    private static final String SVG_PATH = "assets/rengoku.svg";
    private static final float SVG_CX = 64, SVG_FEET = 92;
    private static final float HASHIRA_RANK_BONUS = 1f + 12 * 0.10f;
    private static final float SP_REGEN = 45f, DASH_SP_COST = 8f;
    private static final float SP_RETREAT_START = 0.30f, SP_RETREAT_END = 0.70f;
    private static Svg svg;
    private static Svg.Group gHeadBack, gHead, gCape, gTorso, gArmB, gArmF, gLegB, gLegF, gSword, gSheath;
    private static boolean svgTried;
    private boolean retreatingForSP;
    private float defensiveDashT, defensiveDashDir, defensiveDashCooldown, retreatJumpCooldown;
    private float blockRetreatT, blockRetreatDir;
    private Ability.Kind activeFlameForm;
    private float flameFormT, flameFormDuration;
    private boolean flipActive;
    private float flipT;
    public float finisherPoseT = -1;

    private static final class IncomingAttack {
        final float sourceX;
        final boolean dashable;
        final boolean melee;

        IncomingAttack(float sourceX, boolean dashable, boolean melee) {
            this.sourceX = sourceX;
            this.dashable = dashable;
            this.melee = melee;
        }
    }

    public final Profile.SlayerRank rank = Profile.SlayerRank.KINOE;
    public final ArrayList<Ability> flameForms = Ability.flame(rank);
    public final boolean aggressive;

    public Rengoku() {
        this(false);
    }

    public Rengoku(boolean aggressive) {
        super(Profile.Style.FLAME);
        this.aggressive = aggressive;
        name = "Kyojuro Rengoku";
        w = 42;
        h = 98;
        maxHp = hp = 420 * HASHIRA_RANK_BONUS;
        runSpeed = aggressive ? 352.5f : 0;
        maxSp = sp = 300 * HASHIRA_RANK_BONUS;
        aggroR = aggressive ? 850 : 0;
        atkRange = 110;
        atkCdBase = 0.34f;
        windup = 0.14f;
        forms = new ArrayList<>(flameForms);
        loadSvg();
    }

    @Override
    public void update(World world, float dt) {
        defensiveDashCooldown = Math.max(0, defensiveDashCooldown - dt);
        retreatJumpCooldown = Math.max(0, retreatJumpCooldown - dt);
        if (aggressive) {
            if (sp <= maxSp * SP_RETREAT_START) retreatingForSP = true;
            else if (retreatingForSP && sp >= maxSp * SP_RETREAT_END) retreatingForSP = false;
            if (recharging) stopRecharge();
            rechargeCd = 100000;
        }
        super.update(world, dt);
        if (activeFlameForm != null) {
            flameFormT += dt;
            if (flameFormT >= flameFormDuration) activeFlameForm = null;
        }
        if (flipActive) {
            flipT += dt / 0.42f;
            if (flipT >= 1) flipActive = false;
        }
        if (guarding && blockRetreatT > 0) {
            vx = blockRetreatDir * runSpeed * 0.32f;
            blockRetreatT = Math.max(0, blockRetreatT - dt);
        } else if (!guarding) {
            blockRetreatT = 0;
        }
        if (!dead) sp = Math.min(maxSp, sp + SP_REGEN * dt);
    }

    private static void loadSvg() {
        if (svgTried) return;
        svgTried = true;
        svg = Svg.load(SVG_PATH);
        if (svg == null) return;
        gHeadBack = svg.group("head_backside");
        gHead = svg.group("head");
        gCape = svg.group("cape");
        gTorso = svg.group("torso");
        gArmB = svg.group("arm-back");
        gArmF = svg.group("arm-front");
        gLegF = svg.group("leg-front");
        gLegB = svg.group("leg-back");
        gSword = svg.group("sword");
        gSheath = svg.group("sheath");
        if (gHeadBack == null || gHead == null || gCape == null || gTorso == null || gArmB == null || gArmF == null
                || gLegF == null || gLegB == null || gSword == null || gSheath == null)
            svg = null;
    }

    @Override
    protected void think(World world, float dt, float dx, float dist) {
        if (world.level.akazaRengokuDuel && world.targetFor(this) == this) {
            vx = 0;
            return;
        }
        if (aggressive) {
            if (defensiveDashT > 0) {
                defensiveDashT = Math.max(0, defensiveDashT - dt);
                vx = defensiveDashDir * 950;
                return;
            }
            if (retreatingForSP) {
                if (state != 0) {
                    state = 0;
                    cancelPendingForm();
                }
                float away = -Math.signum(dx == 0 ? facing() : dx);
                if (away == 0) away = -facing();
                face(away);
                vx = FMath.approach(vx, away * runSpeed * 1.15f, 1900 * dt);
                if (defensiveDashCooldown <= 0 && sp >= DASH_SP_COST) {
                    startDefensiveDash(away);
                    if (onGround) {
                        vy = -250;
                        retreatJumpCooldown = 0.72f;
                    }
                    return;
                }
                if (onGround && retreatJumpCooldown <= 0) {
                    vy = -670;
                    retreatJumpCooldown = 0.72f;
                }
                return;
            }
            Fighter target = world.targetFor(this);
            float risingCost = 20;
            boolean risingReady = castCd <= 0 && sp >= risingCost && target.y < y - 24 && dist < 220;
            if ((state == 0 || state == 1) && target.y < y - 24 && dist <= atkRange + 18 && !risingReady)
                eliteMove(world, dt, dx, dist);
            super.think(world, dt, dx, dist);
            return;
        }
        vx = 0;
        face(dx);
    }

    @Override
    protected boolean aggressivePursuit() { return aggressive; }

    @Override
    protected float eliteJumpHeightThreshold() { return 24; }

    @Override
    protected boolean canCastFormOn(Fighter target, float dist) {
        return Math.abs(target.y - y) < 150 || dist <= atkRange + 12;
    }

    @Override
    protected boolean shouldCastForm(Fighter target, float dist) {
        for (int i = 0; i < forms.size(); i++) {
            Ability form = peekNextForm();
            if (form.kind == Ability.Kind.RISING_SUN && target.y >= y - 24) {
                skipNextForm();
                continue;
            }
            if ((form.kind == Ability.Kind.FLAME_UNDULATION || form.kind == Ability.Kind.TIGER_VOLLEY) && dist > 240)
                return false;
            return true;
        }
        return false;
    }

    @Override
    public boolean hurt(World world, Fighter src, float dmg, float dir, float kbX, float kbY) {
        if (aggressive && !guarding && invulnT <= 0 && stunT <= 0 && state != 2 && state != 3
                && activeFlameForm == null) {
            guarding = true;
            guardHoldT = 0.5f;
            guardCd = 0.2f;
            guardAnim = 0.3f;
            blockRetreatDir = src == null ? -facing() : Math.signum(x - src.x);
            if (blockRetreatDir == 0) blockRetreatDir = -facing();
            blockRetreatT = guardHoldT;
            vx = blockRetreatDir * runSpeed * 0.32f;
        }
        if (aggressive && world.level.akazaRengokuDuel && world.boss == this && src instanceof Akaza
                && invulnT <= 0) {
            float damageAfterGuard = guarding ? dmg * (src.compassNeedleT > 0 ? 0.5f : 0.10f) : dmg;
            if (hp - damageAfterGuard <= 0 && world.beginAkazaFinisher(this)) return true;
        }
        return aggressive && super.hurt(world, src, dmg, dir, kbX, kbY);
    }

    @Override
    protected float contactDamage() { return aggressive ? super.contactDamage() : 0; }

    @Override
    public float outgoingDamageMult() { return HASHIRA_RANK_BONUS; }

    @Override
    protected Ability nextForm(Fighter target) {
        for (int i = 0; i < forms.size(); i++) {
            Ability form = super.nextForm(target);
            if (form.kind != Ability.Kind.RISING_SUN || target.y < y - 24) return form;
        }
        return flameForms.get(0);
    }

    @Override
    protected float formCooldown(Ability form) { return form.cooldown * 0.5f; }

    @Override
    protected float formWindup() { return 0.24f; }

    @Override
    protected float meleeWindup() { return 0.14f; }

    @Override
    protected float attackRecover() { return 0.225f; }

    @Override
    protected boolean castForm(World world, Ability form) {
        if (sp < form.cost) return false;
        sp -= form.cost;
        activeFlameForm = form.kind;
        flameFormT = 0;
        flameFormDuration = switch (form.kind) {
            case FIRE_DASH, TIGER_VOLLEY -> 1.2f;
            case RISING_SUN -> 1.1f;
            case BLAZING_SLAM, FLAME_PURGATORY -> 1.5f;
            case FLAME_UNDULATION, FLAME_SEARING_RINGS -> 2.1f;
            case FLAME_EMBER_BARRAGE -> 1.4f;
            case FLAME_RETAINED_HEAT -> 4.15f;
            default -> 0.8f;
        };
        if (form.kind == Ability.Kind.RISING_SUN || form.kind == Ability.Kind.BLAZING_SLAM) {
            flipActive = true;
            flipT = 0;
        }
        world.castAbility(this, form, true);
        return true;
    }

    @Override
    protected void tryGuard(World world, Fighter player) {
        if (!aggressive || player == null || player.dead) {
            super.tryGuard(world, player);
            return;
        }
        if (guarding) return;
        IncomingAttack threat = incomingAttack(world, player);
        if (threat == null) return;
        face(threat.sourceX - x);
        boolean canDash = threat.dashable && sp >= DASH_SP_COST && defensiveDashCooldown <= 0;
        boolean dodge = canDash && !threat.melee && (retreatingForSP || guardCd > 0);
        state = 0;
        cancelPendingForm();
        if (dodge) {
            float away = Math.signum(x - threat.sourceX);
            if (away == 0) away = -facing();
            startDefensiveDash(away);
            if (onGround) {
                vy = -250;
                retreatJumpCooldown = 0.72f;
            }
            guarding = false;
        } else {
            guarding = true;
            guardHoldT = 0.5f;
            guardCd = 0.2f;
            guardAnim = 0.3f;
            blockRetreatDir = Math.signum(x - threat.sourceX);
            if (blockRetreatDir == 0) blockRetreatDir = -facing();
            blockRetreatT = guardHoldT;
            vx = blockRetreatDir * runSpeed * 0.32f;
        }
    }

    private void startDefensiveDash(float dir) {
        sp = Math.max(0, sp - DASH_SP_COST);
        defensiveDashT = 0.18f;
        defensiveDashDir = Math.signum(dir);
        if (defensiveDashDir == 0) defensiveDashDir = -facing();
        defensiveDashCooldown = 0.88f;
        invulnT = Math.max(invulnT, 0.18f);
        vx = defensiveDashDir * 950;
    }

    private IncomingAttack incomingAttack(World world, Fighter player) {
        float dx = x - player.x, dy = y - player.y;
        if (player instanceof Player human && human.attacking()
                && human.facing() * dx > 0 && Math.abs(dx) < 108 && Math.abs(dy) < 125)
            return new IncomingAttack(player.x, true, true);
        IncomingAttack closest = null;
        float closestT = Float.POSITIVE_INFINITY;
        for (Effect e : world.effects) {
            if (e.dead || !e.friendly || e.owner == null || e.owner == this || e.owner.team == team || e.dmg <= 0) continue;
            float rx = x - e.x, ry = y - e.y;
            float speed2 = e.vx * e.vx + e.vy * e.vy;
            float approach = rx * e.vx + ry * e.vy;
            float currentReach = e.r + Math.max(w, h) * 0.42f + 18;
            if (speed2 >= 1 && approach < 0 && rx * rx + ry * ry > currentReach * currentReach) continue;
            float t = speed2 < 1 ? 0 : FMath.clamp(approach / speed2, 0, 0.35f);
            float cx = e.x + e.vx * t, cy = e.y + e.vy * t;
            float ox = x - cx, oy = y - cy;
            float reach = e.r + Math.max(w, h) * 0.42f + 18;
            boolean threat;
            if (e.kind == Effect.THREAD && (e.ex2 == 1 || e.ex2 == 2 || e.ex2 == 3 || e.ex2 == 5 || e.ex2 == 9)) {
                threat = Math.abs(ox) < 42 + e.r && Math.abs(oy) < Math.max(55, e.ex1) + h * 0.5f;
            } else if (e.kind == Effect.THREAD) {
                threat = FMath.dist(0, 0, ox, oy) < Math.max(reach, e.r + 55);
            } else {
                threat = ox * ox + oy * oy < reach * reach;
            }
            if (!threat || t >= closestT) continue;
            closestT = t;
            closest = new IncomingAttack(e.owner.x, e.kind != Effect.THREAD && !e.unbreakable, false);
        }
        return closest;
    }

    @Override
    protected void renderBody(Graphics2D g) {
        float feet = bottom();
        g.setColor(new Color(0, 0, 0, 50));
        g.fillOval((int) x - 22, (int) feet - 5, 44, 8);
        if (svg == null) {
            g.setColor(new Color(176, 54, 32));
            g.fillRoundRect((int) x - 15, (int) feet - 66, 30, 60, 12, 12);
            g.setColor(new Color(250, 214, 174));
            g.fillOval((int) x - 10, (int) feet - 92, 20, 23);
            g.setColor(new Color(255, 153, 43));
            g.fillPolygon(new int[]{(int) x - 13, (int) x, (int) x + 13}, new int[]{(int) feet - 81, (int) feet - 102, (int) feet - 81}, 3);
            return;
        }
        Composite old = g.getComposite();
        float finishFade = finisherPoseT < 3.65f ? 1f : 1f - FMath.clamp((finisherPoseT - 3.65f) / 1.2f, 0, 1);
        if (flashT > 0 || finishFade < 1)
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.min(finishFade, flashT > 0 ? 0.84f : 1f)));
        float s = h / SVG_FEET;
        float ph = animPhase * 6.28f;
        float speedF = runSpeed > 0 ? FMath.clamp(Math.abs(vx) / runSpeed, 0, 1) : 0;
        float legSwing = FMath.sin(ph) * 0.68f * speedF;
        float armSwing = FMath.sin(ph) * 0.34f * speedF;
        boolean swordDrawn = finisherPoseT < 0 && aggressive
                && (guarding || attacking() || state == 2 || state == 3 || timeSinceAction < 5f);
        float frontArmRot = armSwing;
        if (activeFlameForm != null) {
            frontArmRot = flameSwordArmAngle();
        } else if (state == 2) {
            frontArmRot = -0.85f;
        } else if (state == 3) {
            float t = FMath.easeOut(FMath.clamp(stateT / Math.max(0.01f, recover), 0, 1));
            frontArmRot = swingSide < 0 ? FMath.lerp(-0.9f, 1.15f, t) : FMath.lerp(0.9f, -1.15f, t);
        }
        if (finisherPoseT >= 0) {
            float impact = FMath.clamp((finisherPoseT - 1.1f) / 0.75f, 0, 1);
            frontArmRot = finisherPoseT < 1.1f ? 0.15f : FMath.lerp(0.15f, 1.15f, FMath.easeOut(impact));
        }
        Graphics2D bg = (Graphics2D) g.create();
        bg.translate(x, feet);
        bg.scale(facing(), 1);
        bg.scale(s, s);
        bg.translate(-SVG_CX, -SVG_FEET);
        if (finisherPoseT >= 0 && finisherPoseT > 2.05f) {
            float fall = FMath.easeInOut(FMath.clamp((finisherPoseT - 2.05f) / 2f, 0, 1));
            bg.rotate(-facing() * fall * 1.14f, SVG_CX, SVG_FEET - 30);
        }
        if (flipActive) bg.rotate(facing() * FMath.easeInOut(FMath.clamp(flipT, 0, 1)) * (float) Math.PI * 2,
                SVG_CX, SVG_FEET - 46);
        float localMotion = vx * facing();
        float motionAmount = FMath.clamp(localMotion / 280f, -1, 1);
        float capeSway = Math.abs(vx) > 3
                ? -motionAmount * 0.22f + FMath.sin(animPhase * 2f) * Math.abs(motionAmount) * 0.04f
                : FMath.sin(Game.time * 2.5f) * 0.045f;
        drawPart(bg, gHeadBack, 0);
        drawPart(bg, gCape, capeSway);
        drawPart(bg, gSheath, 0);
        drawPart(bg, gLegB, -legSwing);
        drawPart(bg, gLegF, legSwing);
        bg.translate(0, Math.abs(FMath.sin(ph)) * -0.7f);
        drawPart(bg, gTorso, 0);
        drawPart(bg, gArmB, -armSwing);
        drawPart(bg, gArmF, frontArmRot);
        if (swordDrawn) drawPart(bg, gSword, frontArmRot, gArmF.pivotX, gArmF.pivotY);
        drawPart(bg, gHead, 0);
        bg.dispose();
        g.setComposite(old);
    }

    private float flameSwordArmAngle() {
        float hashiraCharge = 0.5f * 0.5f;
        return switch (activeFlameForm) {
            case FIRE_DASH -> flameFormT < 0.21f ? -2.25f
                    : FMath.lerp(-2.25f, 1.5f, FMath.easeOut(FMath.clamp((flameFormT - 0.21f) / 0.22f, 0, 1)));
            case RISING_SUN -> FMath.sin(flameFormT * 48f) * 1.42f;
            case BLAZING_SLAM -> flameFormT < 0.525f ? -2.35f
                    : FMath.lerp(-2.35f, 1.8f, FMath.easeOut(FMath.clamp((flameFormT - 0.525f) / 0.12f, 0, 1)));
            case FLAME_UNDULATION -> FMath.sin(flameFormT * 34f) * (0.45f + FMath.sin(flameFormT * 12f) * 0.95f);
            case TIGER_VOLLEY -> flameFormT < hashiraCharge ? -2.35f
                    : FMath.lerp(-2.35f, 1.85f, FMath.easeOut(FMath.clamp((flameFormT - hashiraCharge) / 0.22f, 0, 1)));
            case FLAME_SEARING_RINGS -> flameFormT < 0.17f ? -2.3f : FMath.sin(flameFormT * 48f) * 1.4f;
            case FLAME_EMBER_BARRAGE -> FMath.sin(flameFormT * 68f) * 1.55f;
            case FLAME_RETAINED_HEAT -> -0.55f;
            case FLAME_PURGATORY -> flameFormT < 0.5f ? -2.4f
                    : FMath.lerp(-2.4f, 1.95f, FMath.easeOut(FMath.clamp((flameFormT - 0.5f) / 0.12f, 0, 1)));
            default -> 0;
        };
    }

    private void drawPart(Graphics2D bg, Svg.Group grp, float rot) {
        if (grp == null) return;
        drawPart(bg, grp, rot, grp.pivotX, grp.pivotY);
    }

    private void drawPart(Graphics2D bg, Svg.Group grp, float rot, float pivotX, float pivotY) {
        if (grp == null) return;
        Graphics2D gg = (Graphics2D) bg.create();
        if (rot != 0) {
            gg.translate(pivotX, pivotY);
            gg.rotate(rot);
            gg.drawImage(grp.img, Math.round(-pivotX), Math.round(-pivotY), Math.round(svg.w), Math.round(svg.h), null);
        } else {
            gg.drawImage(grp.img, 0, 0, Math.round(svg.w), Math.round(svg.h), null);
        }
        gg.dispose();
    }
}
