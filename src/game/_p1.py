s = open('Player.java').read()

# ---- 1. flip pivot at body center ----
old = """        if (flipActive) {
            float k = FMath.easeInOut(FMath.clamp(flipT, 0, 1));
            g.rotate(k * (float) Math.PI * 2 * facing(), x, y - h * 0.45f);
        }"""
new = """        if (flipActive) {
            float k = FMath.easeInOut(FMath.clamp(flipT, 0, 1));
            g.rotate(k * (float) Math.PI * 2 * facing(), x, y);
        }"""
assert old in s; s = s.replace(old, new)

# ---- 2+3+4. drawSlayer: proportions anchored to feet, tuck, single-color arms ----
old = """    private void drawSlayer(Graphics2D g) {
        float speedF = FMath.clamp(Math.abs(vx) / runSpeed, 0, 1);
        float ph = animPhase * (float) Math.PI * 2;
        float bob = onGround ? Math.abs(FMath.sin(ph)) * -3f * speedF + FMath.sin(Game.time * 2.1f) * 1.1f * (1 - speedF) : 0;
        float hipY = y + h * 0.54f + bob;
        float shY = y + h * 0.26f + bob;
        float legSwing = FMath.sin(ph) * 0.75f * speedF;
        float airPose = onGround ? 0 : FMath.clamp(vy * 0.001f, -0.6f, 0.6f);

        Color hair = new Color(74, 43, 48);
        Color skin = new Color(242, 203, 158);
        Color cloth = new Color(36, 36, 54);
        Color haori = style == Profile.Style.FLAME ? new Color(122, 44, 36) : new Color(46, 104, 92);

        legP(g, x - facing() * 2, hipY, -legSwing, 27, 6, cloth.darker(), limbBackLeg);
        legP(g, x + facing() * 2, hipY, legSwing, 27, 6, cloth, limbFrontLeg);

        if (!sheathed() && swingSide < 0) drawSwordArm(g, x - facing() * 6, shY + 5, true, skin, haori.darker());

        torso(g, shY, hipY, 9, haori, true);

        head(g, x + facing() * 1.5f, shY - 11, skin, hair, false);

        arm(g, x - facing() * 7, shY + 5, -FMath.sin(ph) * 0.5f * speedF - airPose * 0.4f, 12, 5,
                sheathed() || swingSide > 0 ? haori.darker() : null, skin, limbBackArm);

        if (!sheathed() && swingSide > 0) drawSwordArm(g, x + facing() * 6, shY + 5, false, skin, haori.darker());
        else if (sheathed()) {
            drawScabbard(g, hipY, cloth.darker().darker());
            idleArm(g, x + facing() * 7, shY + 5, ph, speedF, 5, haori, skin);
        } else {
            idleArm(g, x + facing() * 7, shY + 5, ph, speedF, 5, haori, skin);
        }
        drawGuardArc(g);
        drawStunStars(g);
        if (guardAnim <= 0 && stunT <= 0 && stunnedEyes()) dizzyEyes(g, x + facing() * 2, shY - 11);
    }"""
new = """    private void drawSlayer(Graphics2D g) {
        float speedF = FMath.clamp(Math.abs(vx) / runSpeed, 0, 1);
        float ph = animPhase * (float) Math.PI * 2;
        float bob = onGround ? Math.abs(FMath.sin(ph)) * -3f * speedF + FMath.sin(Game.time * 2.1f) * 1.1f * (1 - speedF) : 0;
        float tuck = flipActive ? FMath.sin(FMath.clamp(flipT, 0, 1) * (float) Math.PI) : 0;
        float feetF = y + h * 0.5f;
        float hipY = feetF - h * 0.36f + bob;
        float shY = feetF - h * 0.74f + bob;
        float legLen = h * 0.36f * (1 - 0.32f * tuck);
        float legSwing = FMath.lerp(FMath.sin(ph) * 0.75f * speedF, -2.35f, tuck);
        float airPose = onGround ? 0 : FMath.clamp(vy * 0.001f, -0.6f, 0.6f);

        Color hair = new Color(74, 43, 48);
        Color skin = new Color(242, 203, 158);
        Color cloth = new Color(36, 36, 54);
        Color haori = style == Profile.Style.FLAME ? new Color(122, 44, 36) : new Color(46, 104, 92);

        boolean swordInBack = !sheathed() && swingSide < 0;
        boolean swordInFront = !sheathed() && swingSide > 0;

        legP(g, x - facing() * 2, hipY, -legSwing, legLen, 6, cloth.darker(), limbBackLeg);
        legP(g, x + facing() * 2, hipY, legSwing, legLen, 6, cloth, limbFrontLeg);

        if (!swordInBack)
            arm(g, x - facing() * 7, shY + 5,
                    FMath.lerp(-FMath.sin(ph) * 0.5f * speedF - airPose * 0.4f, -2.1f, tuck),
                    12 * (1 - 0.3f * tuck), 5, haori.darker(), skin, limbBackArm);

        torso(g, shY, hipY, 9, haori, true);

        head(g, x + facing() * 1.5f, shY - 11, skin, hair, false);

        if (swordInFront) {
            drawSwordArm(g, x + facing() * 6, shY + 5, false, skin, haori.darker(), tuck);
        } else {
            if (sheathed()) drawScabbard(g, hipY);
            idleArm(g, x + facing() * 7, shY + 5, ph, speedF, 5, haori, skin, tuck);
        }
        if (swordInBack) drawSwordArm(g, x - facing() * 6, shY + 5, true, skin, haori.darker(), tuck);

        drawGuardArc(g);
        drawStunStars(g);
        if (guardAnim <= 0 && stunT <= 0 && stunnedEyes()) dizzyEyes(g, x + facing() * 2, shY - 11);
    }"""
assert old in s; s = s.replace(old, new)

# ---- drawDemon same treatment ----
old = """    private void drawDemon(Graphics2D g) {
        float speedF = FMath.clamp(Math.abs(vx) / runSpeed, 0, 1);
        float ph = animPhase * (float) Math.PI * 2;
        float bob = onGround ? Math.abs(FMath.sin(ph)) * -3f * speedF + FMath.sin(Game.time * 2.4f) * 1.3f * (1 - speedF) : 0;
        float hipY = y + h * 0.52f + bob;
        float shY = y + h * 0.24f + bob;
        float legSwing = FMath.sin(ph) * 0.8f * speedF;
        float airPose = onGround ? 0 : FMath.clamp(vy * 0.001f, -0.6f, 0.6f);

        Color skin = new Color(214, 206, 218);
        Color hairC = new Color(238, 238, 244);
        Color pants = new Color(44, 32, 54);

        legClawP(g, x - facing() * 2, hipY, -legSwing, 28, 7, pants.darker(), limbBackLeg);
        legClawP(g, x + facing() * 2, hipY, legSwing, 28, 7, pants, limbFrontLeg);

        if (swingSide < 0) clawArmSwing(g, x - facing() * 6, shY + 5, true, skin.darker());

        torso(g, shY, hipY, 10, skin, false);

        head(g, x + facing() * 1.5f, shY - 11, skin, hairC, true);

        if (swingSide < 0 && limbBackArm) {
            if (attacking()) clawArmSwing(g, x - facing() * 6, shY + 5, true, skin.darker());
            else idleArm(g, x - facing() * 7, shY + 5, ph, speedF, 5, skin.darker(), skin.darker());
        }

        clawArmSwing(g, x + facing() * 7, shY + 5, false, skin);
        drawGuardArc(g);
        drawStunStars(g);
    }"""
new = """    private void drawDemon(Graphics2D g) {
        float speedF = FMath.clamp(Math.abs(vx) / runSpeed, 0, 1);
        float ph = animPhase * (float) Math.PI * 2;
        float bob = onGround ? Math.abs(FMath.sin(ph)) * -3f * speedF + FMath.sin(Game.time * 2.4f) * 1.3f * (1 - speedF) : 0;
        float tuck = flipActive ? FMath.sin(FMath.clamp(flipT, 0, 1) * (float) Math.PI) : 0;
        float feetF = y + h * 0.5f;
        float hipY = feetF - h * 0.36f + bob;
        float shY = feetF - h * 0.74f + bob;
        float legLen = h * 0.37f * (1 - 0.32f * tuck);
        float legSwing = FMath.lerp(FMath.sin(ph) * 0.8f * speedF, -2.35f, tuck);
        float airPose = onGround ? 0 : FMath.clamp(vy * 0.001f, -0.6f, 0.6f);

        Color skin = new Color(214, 206, 218);
        Color hairC = new Color(238, 238, 244);
        Color pants = new Color(44, 32, 54);

        boolean swordInBack = swingSide < 0;
        boolean swordInFront = swingSide > 0;

        legClawP(g, x - facing() * 2, hipY, -legSwing, legLen, 7, pants.darker(), limbBackLeg);
        legClawP(g, x + facing() * 2, hipY, legSwing, legLen, 7, pants, limbFrontLeg);

        if (!swordInBack)
            idleClawArm(g, x - facing() * 7, shY + 5, true, ph, speedF, skin.darker(), tuck);

        torso(g, shY, hipY, 10, skin, false);

        head(g, x + facing() * 1.5f, shY - 11, skin, hairC, true);

        if (swordInFront) {
            clawArmSwing(g, x + facing() * 7, shY + 5, false, skin, tuck);
            idleClawArm(g, x - facing() * 7, shY + 5, true, ph, speedF, skin.darker(), tuck);
        } else {
            clawArmSwing(g, x + facing() * 7, shY + 5, false, skin, tuck);
        }
        if (swordInBack) clawArmSwing(g, x - facing() * 6, shY + 5, true, skin.darker(), tuck);

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
        float ex = sx + FMath.sin(sw) * 12 * (1 - 0.3f * tuck);
        float ey = sy + 12 * (1 - 0.3f * tuck) + FMath.cos(sw) * 4;
        limb(g, sx, sy, ex, ey, 5, c);
        g.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(c.brighter());
        double a = Math.atan2(ey - sy, ex - sx);
        g.drawLine((int) ex, (int) ey, (int) (ex + Math.cos(a) * 6), (int) (ey + Math.sin(a) * 6));
    }"""
assert old in s; s = s.replace(old, new)

# ---- 5. perpendicular grip flipped (player + glow) ----
old = """        double twist = prog >= 0 ? FMath.easeOut((float) Math.min(1, prog * 2.6)) : 0;
        double bladeLocal = armAng + (Math.PI / 2) * (1 - twist);"""
new = """        double twist = prog >= 0 ? FMath.easeOut((float) Math.min(1, prog * 2.6)) : 0;
        double bladeLocal = armAng - (Math.PI / 2) * (1 - twist);"""
assert old in s; s = s.replace(old, new)
old = """            double bladeLocal = armAng + (Math.PI / 2) * (1 - twist);"""
assert old in s; s = s.replace(old, """            double bladeLocal = armAng - (Math.PI / 2) * (1 - twist);""")

# ---- drawSwordArm gains tuck param ----
old = "    private void drawSwordArm(Graphics2D g, float sx, float sy, boolean backArm, Color skin, Color sleeve) {"
new = "    private void drawSwordArm(Graphics2D g, float sx, float sy, boolean backArm, Color skin, Color sleeve, float tuck) {"
assert old in s; s = s.replace(old, new)
old = """        double twist = prog >= 0 ? FMath.easeOut((float) Math.min(1, prog * 2.6)) : 0;
        double bladeLocal = armAng - (Math.PI / 2) * (1 - twist);"""
new = """        armAng += tuck * -2.2f;
        double twist = prog >= 0 ? FMath.easeOut((float) Math.min(1, prog * 2.6)) : 0;
        double bladeLocal = armAng - (Math.PI / 2) * (1 - twist);"""
assert old in s; s = s.replace(old, new)

# ---- clawArmSwing gains tuck param ----
old = "    private void clawArmSwing(Graphics2D g, float sx, float sy, boolean back, Color skin) {"
new = "    private void clawArmSwing(Graphics2D g, float sx, float sy, boolean back, Color skin, float tuck) {"
assert old in s; s = s.replace(old, new)
old = """        double wa = facingRight ? armAng : Math.PI - armAng;
        float hx = sx + (float) Math.cos(wa) * 13, hy = sy + (float) Math.sin(wa) * 13;
        limb(g, sx, sy, hx, hy, 5, skin);
        float ext = prog >= 0 ? 1 : 0.68f;"""
new = """        armAng += tuck * -2.2f;
        double wa = facingRight ? armAng : Math.PI - armAng;
        float hx = sx + (float) Math.cos(wa) * 13, hy = sy + (float) Math.sin(wa) * 13;
        limb(g, sx, sy, hx, hy, 5, skin);
        float ext = (prog >= 0 ? 1 : 0.68f) * (1 - 0.25f * tuck);"""
assert old in s; s = s.replace(old, new)

# ---- idleArm gains tuck ----
old = """    private void idleArm(Graphics2D g, float sx, float sy, float ph, float speedF, float wid, Color sleeve, Color skin) {
        float sw = FMath.sin(ph) * 0.5f * speedF;
        arm(g, sx, sy, sw, 12, wid, sleeve, skin, true);
    }"""
new = """    private void idleArm(Graphics2D g, float sx, float sy, float ph, float speedF, float wid, Color sleeve, Color skin, float tuck) {
        float sw = FMath.lerp(FMath.sin(ph) * 0.5f * speedF, -2.1f, tuck);
        arm(g, sx, sy, sw, 12 * (1 - 0.3f * tuck), wid, sleeve, skin, true);
    }"""
assert old in s; s = s.replace(old, new)

# ---- 6. scabbard: black + low on the hip ----
old = """    private void drawScabbard(Graphics2D g, float hipY, Color c) {
        float bx = x - facing() * 5, by = hipY + 2;
        float tx = bx + facing() * 14, ty = by - 22;
        g.setStroke(new BasicStroke(4.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(c);
        g.drawLine((int) bx, (int) by, (int) tx, (int) ty);
        g.setStroke(new BasicStroke(2.5f));
        g.setColor(new Color(40, 40, 48));
        g.drawLine((int) (tx + facing() * 2), (int) (ty - 4), (int) (tx + facing() * 6), (int) (ty - 8));
    }"""
new = """    private void drawScabbard(Graphics2D g, float hipY) {
        float bx = x + facing() * 2, by = hipY + 7;
        float tx = bx - facing() * 14, ty = by + 21;
        g.setStroke(new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(new Color(14, 14, 18));
        g.drawLine((int) bx, (int) by, (int) tx, (int) ty);
        g.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(new Color(38, 38, 46));
        g.drawLine((int) (bx + facing()), (int) (by - 6), (int) bx, (int) (by - 1));
    }"""
assert old in s; s = s.replace(old, new)

open('Player.java', 'w').write(s)
print("player patched")
