package game;

import java.awt.*;
import java.util.ArrayList;

/** A peaceful Hashira guest on the Mugen Train; his full Flame forms remain available to him. */
public class Rengoku extends SlayerEnemy {
    private static final String SVG_PATH = "assets/rengoku.svg";
    private static final float SVG_CX = 48, SVG_FEET = 92;
    private static final float HASHIRA_RANK_BONUS = 1f + 12 * 0.10f;
    private static Svg svg;
    private static Svg.Group gHeadBack, gHead, gCape, gTorso, gArmB, gArmF, gLegB, gLegF, gSword, gSheath;
    private static boolean svgTried;

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
        runSpeed = aggressive ? 255 : 0;
        maxSp = sp = 300;
        aggroR = aggressive ? 850 : 0;
        loadSvg();
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
        if (aggressive) {
            super.think(world, dt, dx, dist);
            return;
        }
        vx = 0;
        face(dx);
    }

    @Override
    public boolean hurt(World world, Fighter src, float dmg, float dir, float kbX, float kbY) {
        return aggressive && super.hurt(world, src, dmg, dir, kbX, kbY);
    }

    @Override
    protected float contactDamage() { return aggressive ? super.contactDamage() : 0; }

    @Override
    public float outgoingDamageMult() { return HASHIRA_RANK_BONUS; }

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
        if (flashT > 0) g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.84f));
        float s = h / SVG_FEET;
        float ph = animPhase * 6.28f;
        float speedF = runSpeed > 0 ? FMath.clamp(Math.abs(vx) / runSpeed, 0, 1) : 0;
        float legSwing = FMath.sin(ph) * 0.68f * speedF;
        float armSwing = FMath.sin(ph) * 0.34f * speedF;
        boolean swordDrawn = aggressive && (guarding || attacking() || state == 2 || state == 3 || timeSinceAction < 5f);
        float frontArmRot = armSwing;
        if (state == 2) {
            frontArmRot = -0.85f;
        } else if (state == 3) {
            float t = FMath.easeOut(FMath.clamp(stateT / Math.max(0.01f, recover), 0, 1));
            frontArmRot = swingSide < 0 ? FMath.lerp(-0.9f, 1.15f, t) : FMath.lerp(0.9f, -1.15f, t);
        }
        Graphics2D bg = (Graphics2D) g.create();
        bg.translate(x, feet);
        bg.scale(facing(), 1);
        bg.scale(s, s);
        bg.translate(-SVG_CX, -SVG_FEET);
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
