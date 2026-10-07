package game;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public final class MechanicsTest {
    public static void main(String[] args) {
        int fails = 0;
        fails += t("jump", MechanicsTest::testJump);
        fails += t("dash works", MechanicsTest::testDash);
        fails += t("melee kills demon", MechanicsTest::testMeleeKill);
        fails += t("abilities damage", MechanicsTest::testAbilities);
        fails += t("shared NPC/player ability definitions", MechanicsTest::testSharedNpcAbilities);
        fails += t("sunlight burns demon (day level)", MechanicsTest::testSun);
        fails += t("sabito boulder gate", MechanicsTest::testBoulder);
        fails += t("boss death wins", MechanicsTest::testBossWin);
        fails += t("color change", MechanicsTest::testColorChange);
        fails += t("guard blocks 90%", MechanicsTest::testGuard);
        fails += t("blocking prevents stun", MechanicsTest::testBlockNoStun);
        fails += t("stun after 3 hits + lockout", MechanicsTest::testStun);
        fails += t("restunning drains SP", MechanicsTest::testStunDrain);
        fails += t("demon limb loss + regen regrow", MechanicsTest::testLimbs);
        fails += t("SP empty -> 3s lockout", MechanicsTest::testSpLock);
        fails += t("SP recovery waits 2s after spend", MechanicsTest::testSpRecoveryDelay);
        fails += t("recharge restores SP and fifth-bucket HP", MechanicsTest::testRecharge);
        fails += t("kill-gate requires all kills", MechanicsTest::testKillsGate);
        fails += t("void fall kills instantly", MechanicsTest::testVoid);
        fails += t("spikes damage", MechanicsTest::testSpikes);
        fails += t("crusher rolls on terrain", MechanicsTest::testCrusherRolls);
        fails += t("crumble cycle", MechanicsTest::testCrumble);
        fails += t("moving platform carries", MechanicsTest::testMovingCarry);
        fails += t("wisteria poisons demons", MechanicsTest::testWisteria);
        fails += t("underwater blocks recharge", MechanicsTest::testUnderwaterNoRecharge);
        fails += t("dojo dummy opponent", MechanicsTest::testDojoDummy);
        fails += t("ability log caps at 20", MechanicsTest::testAbilityLogCap);
        fails += t("rank unlocks forms", MechanicsTest::testMizunotoUnlocksForms);
        fails += t("rank stats bonus", MechanicsTest::testRankStatsBonus);
        fails += t("nezuko rescue after Asakusa", MechanicsTest::testNezukoRescue);
        fails += t("kyogai is boss-only", MechanicsTest::testKyogaiBossOnly);
        System.out.println(fails == 0 ? "MECHTEST PASS" : "MECHTEST FAIL (" + fails + ")");
        System.exit(fails == 0 ? 0 : 1);
    }

    interface T {
        boolean run() throws Exception;
    }

    static int t(String name, T fn) {
        try {
            boolean ok = fn.run();
            System.out.println((ok ? "  ok  " : " FAIL ") + name);
            return ok ? 0 : 1;
        } catch (Throwable e) {
            System.out.println(" ERR  " + name + ": " + e);
            e.printStackTrace();
            return 1;
        }
    }

    static class Scripted implements InputProvider {
        boolean l, r, u, d, dashP, atkP, castP, guardH, rechargeH;

        public boolean down(int k) {
            return switch (k) {
                case java.awt.event.KeyEvent.VK_A -> l;
                case java.awt.event.KeyEvent.VK_D -> r;
                case java.awt.event.KeyEvent.VK_S -> d;
                case java.awt.event.KeyEvent.VK_K -> guardH;
                case java.awt.event.KeyEvent.VK_L -> rechargeH;
                default -> false;
            };
        }

        public boolean pressed(int k) {
            return switch (k) {
                case java.awt.event.KeyEvent.VK_W -> u;
                case java.awt.event.KeyEvent.VK_SHIFT -> dashP;
                case java.awt.event.KeyEvent.VK_J -> atkP;
                case java.awt.event.KeyEvent.VK_F -> castP;
                default -> false;
            };
        }

        public boolean anyPressed(int... ks) {
            for (int k : ks) if (pressed(k)) return true;
            return false;
        }
    }

    static World mk(Profile p, Level lv, InputProvider in) {
        Player pl = new Player(p);
        return new World(lv, in, pl);
    }

    static void steps(World w, Scripted in, int n) {
        for (int i = 0; i < n; i++) {
            Game.time += 1 / 60f;
            w.step(1 / 60f);
            in.u = in.dashP = in.atkP = in.castP = false;
        }
    }

    static Level trapLevel() {
        Level l = new Level().meta("t", "t", Level.Theme.MTN, true, Level.WinMode.GOAL,
                "t", "t")
                .spawnPlayer(140, 700)
                .ground(0, 2400, 700, Level.GRASS)
                .goalAt(2200, 700);
        l.w = 2600;
        l.h = 1500;
        return l;
    }

    static boolean testJump() {
        Profile p = new Profile();
        Scripted in = new Scripted();
        World w = mk(p, LevelData.slayerLevels()[0], in);
        steps(w, in, 30);
        float x0 = w.player.x;
        in.r = true;
        in.u = true;
        steps(w, in, 1);
        if (!(w.player.vy < -500)) return false;
        in.u = true;
        steps(w, in, 1);
        if (!(w.player.vy < -400)) return false;
        steps(w, in, 50);
        return w.player.x > x0 + 100 && w.player.onGround;
    }

    static boolean testDash() {
        Profile p = new Profile();
        Scripted in = new Scripted();
        World w = mk(p, LevelData.slayerLevels()[6], in);
        steps(w, in, 20);
        w.player.sp = w.player.maxSp;
        in.dashP = true;
        in.r = true;
        steps(w, in, 2);
        if (Math.abs(w.player.vx) < 700) return false;
        return w.player.sp < w.player.maxSp - 2f && w.player.spLock == 0;
    }

    static boolean testMeleeKill() {
        Profile p = new Profile();
        Scripted in = new Scripted();
        World w = mk(p, LevelData.slayerLevels()[12], in);
        BasicDemon d = new BasicDemon();
        d.init(w.player.x + 60, w.player.y, d.maxHp);
        w.enemies.add(d);
        w.player.facingRight = true;
        for (int i = 0; i < 12 && !d.dead; i++) {
            in.atkP = true;
            steps(w, in, 14);
        }
        return d.dead && w.kills >= 1;
    }

    static boolean testAbilities() {
        float dmgDone = 0;
        for (Profile.Style st : new Profile.Style[]{Profile.Style.WATER, Profile.Style.FLAME, Profile.Style.WIND}) {
            Profile p = new Profile();
            p.style = st;
            Scripted in = new Scripted();
            World w = mk(p, LevelData.slayerLevels()[12], in);
            BasicDemon d = new BasicDemon();
            d.init(w.player.x + 120, w.player.y, d.maxHp);
            w.enemies.add(d);
            float before = d.hp;
            for (Ability a : new java.util.ArrayList<>(w.player.abilities))
                w.castAbility(w.player, a);
            steps(w, in, 150);
            dmgDone += before - Math.max(0, d.hp);
        }
        Profile dp = new Profile();
        dp.path = Profile.Path.DEMON;
        Scripted in2 = new Scripted();
        World w = mk(dp, LevelData.demonLevels()[0], in2);
        if (w.player.abilities.size() != 4 || w.player.abilities.get(0).kind != Ability.Kind.BLOOD_BOLT) return false;
        HumanEnemy h = new HumanEnemy(false);
        h.init(w.player.x + 110, w.player.y, h.maxHp);
        w.enemies.add(h);
        float hb = h.hp;
        for (Ability a : new java.util.ArrayList<>(w.player.abilities))
            w.castAbility(w.player, a);
        steps(w, in2, 150);
        dmgDone += hb - Math.max(0, h.hp);

        Profile fp = new Profile();
        fp.path = Profile.Path.DEMON;
        fp.demonArt = Profile.DemonArt.FOREST_HAND;
        Scripted in3 = new Scripted();
        World fw = mk(fp, LevelData.demonLevels()[0], in3);
        HumanEnemy fh = new HumanEnemy(false);
        fh.init(fw.player.x + 120, fw.player.y, fh.maxHp);
        fw.enemies.add(fh);
        int handArts = 0;
        for (Ability a : new java.util.ArrayList<>(fw.player.abilities))
            if (a.kind == Ability.Kind.HAND_SPIKES || a.kind == Ability.Kind.HAND_GRASP
                    || a.kind == Ability.Kind.HAND_SLAM || a.kind == Ability.Kind.HAND_AURA)
                handArts++;
        float fhb = fh.hp;
        for (Ability a : new java.util.ArrayList<>(fw.player.abilities))
            fw.castAbility(fw.player, a);
        steps(fw, in3, 180);
        dmgDone += fhb - Math.max(0, fh.hp);

        Profile sp = new Profile();
        sp.path = Profile.Path.DEMON;
        sp.demonArt = Profile.DemonArt.SWAMP;
        Scripted in4 = new Scripted();
        World sw = mk(sp, LevelData.demonLevels()[0], in4);
        if (sw.player.abilities.size() != 4 || sw.player.abilities.get(0).kind != Ability.Kind.SWAMP_FLURRY
                || sw.player.abilities.get(3).kind != Ability.Kind.SWAMP_STEP) return false;
        HumanEnemy sh = new HumanEnemy(false);
        sh.init(sw.player.x + 115, sw.player.y, sh.maxHp);
        sw.enemies.add(sh);
        float shb = sh.hp;
        for (Ability a : new java.util.ArrayList<>(sw.player.abilities))
            sw.castAbility(sw.player, a);
        boolean swampFx = false;
        for (Effect e : sw.effects) if (e.kind == Effect.SWAMP_CLOUD || e.kind == Effect.SWAMP_HANDS || e.kind == Effect.SWAMP_PUDDLE) swampFx = true;
        steps(sw, in4, 180);
        dmgDone += shb - Math.max(0, sh.hp);

        return handArts == 4 && swampFx && fw.player.cds.length >= fw.player.abilities.size() && dmgDone > 30;
    }

    static boolean testSharedNpcAbilities() {
        Profile dp = new Profile();
        dp.path = Profile.Path.DEMON;
        dp.demonArt = Profile.DemonArt.FOREST_HAND;
        Scripted in = new Scripted();
        World w = mk(dp, LevelData.demonLevels()[0], in);
        Ability earth = null;
        for (Ability a : w.player.abilities) if (a.kind == Ability.Kind.HAND_SLAM) earth = a;
        if (earth == null) return false;
        w.castAbility(w.player, earth);
        boolean hasCharge = false, hasImpactNow = false;
        for (Effect e : w.effects) {
            if (e.kind == Effect.HAND_CHARGE) hasCharge = true;
            if (e.kind == Effect.SHOCK_GROUND || e.kind == Effect.SLAM_RING) hasImpactNow = true;
        }
        if (!hasCharge || hasImpactNow) return false;
        steps(w, in, 45);
        boolean hasImpactLater = false;
        for (Effect e : w.effects) if (e.kind == Effect.SHOCK_GROUND || e.kind == Effect.SLAM_RING) hasImpactLater = true;
        if (!hasImpactLater) return false;

        Profile sp = new Profile();
        sp.path = Profile.Path.SLAYER;
        Scripted in2 = new Scripted();
        World fw = mk(sp, trapLevel(), in2);
        FlameBoss fb = new FlameBoss();
        fb.init(400, 700 - 47, fb.maxHp);
        Ability tiger = null;
        for (Ability a : Ability.flame()) if (a.kind == Ability.Kind.TIGER_VOLLEY) tiger = a;
        if (tiger == null) return false;
        fw.castAbility(fb, tiger);
        steps(fw, in2, 35);
        boolean tigerHead = false;
        for (Effect e : fw.effects) if (e.kind == Effect.TIGER_HEAD && !e.friendly) tigerHead = true;
        return tigerHead;
    }

    static boolean testMizunotoUnlocksForms() {
        for (Profile.Style st : new Profile.Style[]{Profile.Style.WATER, Profile.Style.FLAME, Profile.Style.WIND}) {
            Profile p = new Profile();
            p.path = Profile.Path.SLAYER;
            p.style = st;
            Player unranked = new Player(p);
            if (unranked.abilities.size() != (st == Profile.Style.FLAME ? 3 : st == Profile.Style.WIND ? 3 : 4)) return false;
            if (st == Profile.Style.WIND) {
                if (unranked.abilities.get(0).kind != Ability.Kind.WIND_DUST) return false;
                if (unranked.abilities.get(1).kind != Ability.Kind.WIND_CLAWS) return false;
                if (unranked.abilities.get(2).kind != Ability.Kind.WIND_TREE) return false;
            }

            p.slayerRank = Profile.SlayerRank.MIZUNOTO;
            Player ranked = new Player(p);
            if (st == Profile.Style.WATER) {
                if (ranked.abilities.size() != 6) return false;
                if (ranked.abilities.get(2).kind != Ability.Kind.FLOWING_DANCE) return false;
                if (ranked.abilities.get(3).kind != Ability.Kind.TIDE) return false;
                if (ranked.abilities.get(4).kind != Ability.Kind.BLESSED_RAIN) return false;
                if (ranked.abilities.get(5).kind != Ability.Kind.WHIRL) return false;
            } else if (st == Profile.Style.FLAME) {
                if (ranked.abilities.size() != 4) return false;
                if (ranked.abilities.get(3).kind != Ability.Kind.FLAME_UNDULATION) return false;

                p.slayerRank = Profile.SlayerRank.MIZUNOE;
                Player mizunoe = new Player(p);
                if (mizunoe.abilities.size() != 5) return false;
                if (mizunoe.abilities.get(4).kind != Ability.Kind.TIGER_VOLLEY) return false;
            } else {
                if (ranked.abilities.size() != 5) return false;
                if (ranked.abilities.get(3).kind != Ability.Kind.WIND_STORM) return false;
                if (ranked.abilities.get(4).kind != Ability.Kind.WIND_MOUNTAIN) return false;

                p.slayerRank = Profile.SlayerRank.MIZUNOE;
                Player mizunoe = new Player(p);
                if (mizunoe.abilities.size() != 6) return false;
                if (mizunoe.abilities.get(5).kind != Ability.Kind.WIND_MIST) return false;

                p.slayerRank = Profile.SlayerRank.KANOTO;
                Player kanoto = new Player(p);
                if (kanoto.abilities.size() != 6) return false;

                p.slayerRank = Profile.SlayerRank.KANOE;
                Player kanoe = new Player(p);
                if (kanoe.abilities.size() != 7) return false;
                if (kanoe.abilities.get(6).kind != Ability.Kind.WIND_BLACK) return false;

                p.slayerRank = Profile.SlayerRank.TSUCHINOTO;
                Player tsuchinoto = new Player(p);
                if (tsuchinoto.abilities.size() != 7) return false;

                p.slayerRank = Profile.SlayerRank.TSUCHINOE;
                Player tsuchinoe = new Player(p);
                if (tsuchinoe.abilities.size() != 8) return false;
                if (tsuchinoe.abilities.get(7).kind != Ability.Kind.WIND_EIGHT) return false;

                p.slayerRank = Profile.SlayerRank.HINOTO;
                Player hinoto = new Player(p);
                if (hinoto.abilities.size() != 8) return false;

                p.slayerRank = Profile.SlayerRank.HINOE;
                Player hinoe = new Player(p);
                if (hinoe.abilities.size() != 9) return false;
                if (hinoe.abilities.get(8).kind != Ability.Kind.WIND_NINE) return false;

                p.slayerRank = Profile.SlayerRank.KINOTO;
                Player kinoto = new Player(p);
                if (kinoto.abilities.size() != 10) return false;
                if (kinoto.abilities.get(9).kind != Ability.Kind.WIND_TEN) return false;
            }
        }
        return true;
    }

    static boolean testRankStatsBonus() {
        Profile base = new Profile();
        base.path = Profile.Path.SLAYER;
        base.style = Profile.Style.WATER;
        Player unranked = new Player(base);
        if (Math.abs(unranked.maxHp - 100) > 0.01f || Math.abs(unranked.outgoingDamageMult() - 1f) > 0.01f) return false;

        Profile rankedProfile = new Profile();
        rankedProfile.path = Profile.Path.SLAYER;
        rankedProfile.style = Profile.Style.WATER;
        rankedProfile.slayerRank = Profile.SlayerRank.MIZUNOTO;
        Player ranked = new Player(rankedProfile);
        if (Math.abs(ranked.maxHp - 120) > 0.01f || Math.abs(ranked.hp - 120) > 0.01f) return false;
        if (Math.abs(ranked.outgoingDamageMult() - 1.2f) > 0.01f) return false;

        Scripted in = new Scripted();
        World w = mk(rankedProfile, trapLevel(), in);
        BasicDemon d = new BasicDemon();
        d.init(w.player.x + 60, w.player.y, d.maxHp);
        w.enemies.add(d);
        float before = d.hp;
        w.meleeStrike(w.player, 90, 10, 0, 0, 0, Math.PI, Color.WHITE);
        if (Math.abs((before - d.hp) - 12) > 0.1f) return false;

        Ability swampHands = Ability.swampDemonArt(true).get(2);
        if (swampHands.kind != Ability.Kind.SWAMP_HANDS) return false;
        SwampDemon sw = new SwampDemon(true);
        sw.init(200, 700, sw.maxHp);
        Scripted swIn = new Scripted();
        World swWorld = mk(new Profile(), trapLevel(), swIn);
        swWorld.enemies.add(sw);
        swWorld.castAbility(sw, swampHands);
        steps(swWorld, swIn, 1);
        for (Effect e : swWorld.effects)
            if (e.kind == Effect.SWAMP_HANDS && Math.abs(e.dmg - 20.25f) < 0.01f) return true;
        return false;
    }

    static boolean testNezukoRescue() {
        Profile p = new Profile();
        p.path = Profile.Path.SLAYER;
        p.style = Profile.Style.WATER;
        p.slayerRank = Profile.SlayerRank.MIZUNOTO;
        Scripted in = new Scripted();
        World w = mk(p, trapLevel(), in);
        w.slayerLevelIndex = 23;
        w.player.hurt(w, null, 999, 1, 0, 0);
        if (w.player.dead || !w.player.nezukoMode || !w.player.isDemon) return false;
        if (w.player.demonArt != Profile.DemonArt.COMBUSTIBLE_BLOOD) return false;
        Ability.Kind[] nezukoForms = {
                Ability.Kind.NEZUKO_NAILS,
                Ability.Kind.NEZUKO_EXPLODING_BLOOD,
                Ability.Kind.NEZUKO_SCRATCHING,
                Ability.Kind.NEZUKO_HEEL_BASH,
                Ability.Kind.NEZUKO_SPIN_KICK,
                Ability.Kind.NEZUKO_FLYING_KICK
        };
        if (w.player.abilities.size() != nezukoForms.length) return false;
        for (int i = 0; i < nezukoForms.length; i++)
            if (w.player.abilities.get(i).kind != nezukoForms[i]) return false;
        if (p.path != Profile.Path.SLAYER || p.style != Profile.Style.WATER) return false;
        Player restarted = new Player(p);
        if (restarted.nezukoMode || restarted.isDemon || restarted.path != Profile.Path.SLAYER) return false;
        if (restarted.abilities.size() != 6 || restarted.abilities.get(0).kind != Ability.Kind.WAVE_PROJ) return false;

        World fallWorld = mk(p, trapLevel(), in);
        fallWorld.slayerLevelIndex = 23;
        fallWorld.playerFell();
        if (fallWorld.player.dead || !fallWorld.player.nezukoMode || !fallWorld.player.isDemon) return false;
        fallWorld.playerFell();
        if (!fallWorld.player.dead) return false;

        w.player.invulnT = 0;
        w.player.hurt(w, null, 999, 1, 0, 0);
        if (!w.player.dead) return false;
        steps(w, in, 120);
        return w.failed;
    }

    static boolean testKyogaiBossOnly() {
        for (Profile.DemonArt art : Profile.DemonArt.values())
            if (art.name().contains("KYOGAI")) return false;
        Profile p = new Profile();
        p.path = Profile.Path.DEMON;
        for (Profile.DemonArt art : Profile.DemonArt.values()) {
            p.demonArt = art;
            Player pl = new Player(p);
            for (Ability a : pl.abilities) {
                String n = a.kind.name();
                if (n.contains("KYOGAI") || n.contains("DRUM") || n.contains("ROOM")) return false;
            }
        }
        World w = mk(new Profile(), LevelData.slayerLevels()[31], new Scripted());
        return w.boss instanceof Kyogai;
    }

    static boolean testSun() {
        Profile p = new Profile();
        p.path = Profile.Path.DEMON;
        Scripted in = new Scripted();
        World w = mk(p, LevelData.demonLevels()[2], in);
        if (!w.level.daytime) return false;
        float hp0 = w.player.hp;
        for (int i = 0; i < 240 && !w.player.dead; i++) {
            Game.time += 1 / 60f;
            w.step(1 / 60f);
        }
        return w.player.hp < hp0;
    }

    static boolean testBoulder() {
        Profile p = new Profile();
        Scripted in = new Scripted();
        World w = mk(p, LevelData.slayerLevels()[10], in);
        Fighter sab = null;
        for (Fighter f : w.enemies) if (f instanceof Sabito s) sab = f;
        if (sab == null) return false;
        sab.chip(w, 9999);
        steps(w, in, 200);
        return w.boulderProgress >= 100 && w.goalUnlocked();
    }

    static boolean testBossWin() {
        Profile p = new Profile();
        Scripted in = new Scripted();
        World w = mk(p, LevelData.slayerLevels()[17], in);
        if (w.boss == null) return false;
        w.boss.chip(w, 99999);
        steps(w, in, 160);
        return w.bossDead && w.complete;
    }

    static boolean testColorChange() {
        Profile p = new Profile();
        p.path = Profile.Path.SLAYER;
        p.style = Profile.Style.NONE;
        Game.profile = p;
        Game g = new Game();
        State s = new State.ColorChange(g, 10);
        Game.change(s);
        for (int i = 0; i < 300; i++) {
            Game.time += 1 / 60f;
            s.update();
        }
        return p.colorChanged && (p.style == Profile.Style.WATER || p.style == Profile.Style.FLAME || p.style == Profile.Style.WIND);
    }

    static boolean testGuard() {
        Profile p = new Profile();
        Scripted in = new Scripted();
        World w = mk(p, trapLevel(), in);
        steps(w, in, 10);
        in.guardH = true;
        steps(w, in, 2);
        float hp0 = w.player.hp;
        w.player.invulnT = 0;
        w.player.hurt(w, null, 20, 1, 200, 100);
        float lost = hp0 - w.player.hp;
        return lost > 1.7f && lost < 2.3f;
    }

    static boolean testBlockNoStun() {
        Profile p = new Profile();
        Scripted in = new Scripted();
        World w = mk(p, trapLevel(), in);
        steps(w, in, 5);
        w.player.guarding = true;
        for (int i = 0; i < 3; i++) {
            w.player.invulnT = 0;
            w.player.hurt(w, null, 20, 1, 200, 100);
        }
        return !w.player.stunned() && w.player.hitStreak == 0;
    }

    static boolean testStun() {
        Profile p = new Profile();
        p.style = Profile.Style.WATER;
        Scripted in = new Scripted();
        World w = mk(p, LevelData.slayerLevels()[12], in);
        BasicDemon d = new BasicDemon();
        d.init(w.player.x + 70, w.player.y, d.maxHp);
        w.enemies.add(d);
        d.vx = 150;
        for (int i = 0; i < 3; i++) {
            d.invulnT = 0;
            d.guarding = false;
            d.hurt(w, w.player, 5, 1, 40, 40);
        }
        if (!d.stunned()) return false;
        steps(w, in, 10);
        return Math.abs(d.vx) < 30;
    }

    static boolean testStunDrain() {
        Profile p = new Profile();
        Scripted in = new Scripted();
        World w = mk(p, trapLevel(), in);
        steps(w, in, 5);
        HumanEnemy h = new HumanEnemy(false);
        h.init(w.player.x + 80, w.player.y, h.maxHp);
        w.enemies.add(h);
        w.player.sp = w.player.maxSp;
        h.stunT = 2f;
        for (int i = 0; i < 3; i++) {
            h.invulnT = 0;
            h.hurt(w, w.player, 4, 1, 30, 30);
        }
        return Math.abs(w.player.sp - w.player.maxSp * 0.5f) < 1f;
    }

    static boolean testLimbs() {
        Profile p = new Profile();
        p.path = Profile.Path.DEMON;
        Scripted in = new Scripted();
        World w = mk(p, trapLevel(), in);
        Fighter pl = w.player;
        int before = 4;
        for (int i = 0; i < 6; i++) pl.loseRandomLimb(w);
        int missingAfter = countMissing(pl);
        if (missingAfter != before) return false;
        pl.limbFrontArm = false;
        pl.hurtTimer = 99;
        pl.hp = pl.maxHp * 0.49f;
        pl.demonRegen(w, 0.5f, 20);
        boolean regrewAt50 = !pl.limbFrontArm || countMissing(pl) < missingAfter;
        while (countMissing(pl) > 0) {
            pl.hurtTimer = 99;
            pl.hp = pl.maxHp * 0.9f;
            pl.demonRegen(w, 0.5f, 20);
        }
        return regrewAt50 && countMissing(pl) == 0;
    }

    static int countMissing(Fighter f) {
        int n = 0;
        if (!f.limbFrontArm) n++;
        if (!f.limbBackArm) n++;
        if (!f.limbFrontLeg) n++;
        if (!f.limbBackLeg) n++;
        return n;
    }

    static boolean testSpLock() {
        Profile p = new Profile();
        Scripted in = new Scripted();
        World w = mk(p, trapLevel(), in);
        steps(w, in, 5);
        w.player.sp = 3;
        w.player.consume(10);
        if (w.player.sp != 0 || w.player.spLock <= 0) return false;
        steps(w, in, 30);
        if (w.player.sp > 0.01f) return false;
        steps(w, in, 190);
        return w.player.spLock <= 0 && w.player.sp > 5;
    }

    static boolean testSpRecoveryDelay() {
        Profile p = new Profile();
        Scripted in = new Scripted();
        World w = mk(p, trapLevel(), in);
        steps(w, in, 5);
        w.player.sp = 80;
        w.player.consume(10);
        if (Math.abs(w.player.sp - 70) > 0.01f || w.player.spRecoverDelay <= 1.9f) return false;
        steps(w, in, 110);
        if (w.player.sp > 70.01f) return false;
        steps(w, in, 35);
        return w.player.sp > 70.5f;
    }

    static boolean testRecharge() {
        Profile p = new Profile();
        p.style = Profile.Style.WATER;
        Scripted in = new Scripted();
        World w = mk(p, trapLevel(), in);
        steps(w, in, 5);
        w.player.hp = 5;
        w.player.sp = 10;
        float x0 = w.player.x;
        in.r = true;
        in.rechargeH = true;
        steps(w, in, 110);
        if (Math.abs(w.player.x - x0) > 1.5f || w.player.sp < 29f || w.player.sp > 31f || w.player.hp > 5.5f) return false;
        steps(w, in, 235);
        boolean restored = w.player.sp > 80 && w.player.hp > 19.5f && w.player.hp <= 20.5f;
        in.rechargeH = false;
        steps(w, in, 5);
        w.player.hp = 21;
        w.player.sp = 10;
        in.rechargeH = true;
        steps(w, in, 300);
        boolean nextBucket = w.player.hp > 39.5f && w.player.hp <= 40.5f;
        w.player.invulnT = 0;
        w.player.hurt(w, null, 5, 1, 10, 10);
        return restored && nextBucket && !w.player.recharging;
    }

    static boolean testKillsGate() {
        Profile p = new Profile();
        Scripted in = new Scripted();
        World w = mk(p, LevelData.slayerLevels()[12], in);
        w.player.x = w.level.goalX;
        w.player.y = w.level.goalY + 100;
        steps(w, in, 30);
        if (w.complete) return false;
        for (Fighter f : new java.util.ArrayList<>(w.enemies)) f.chip(w, 99999);
        steps(w, in, 40);
        return w.complete && w.goalUnlocked();
    }

    static boolean testVoid() {
        Profile p = new Profile();
        Scripted in = new Scripted();
        World w = mk(p, trapLevel(), in);
        steps(w, in, 5);
        w.player.y = w.cam.y + Game.VIEW_H + 400;
        w.player.vy = 500;
        steps(w, in, 8);
        if (!w.player.dead || w.player.hp > 0) return false;
        steps(w, in, 130);
        return w.failed;
    }

    static boolean testWisteria() {
        Level l = trapLevel();
        l.deco(Level.WISTERIA, 600, 700, 1.5f);
        Profile p = new Profile();
        p.path = Profile.Path.DEMON;
        Scripted in = new Scripted();
        World w = mk(p, l, in);
        w.player.x = 600;
        w.player.y = 700 - 43;
        float hp0 = w.player.hp;
        float sp0 = w.player.sp = 80;
        steps(w, in, 40);
        boolean demonHurt = w.player.hp < hp0 && w.player.sp < sp0;
        Profile p2 = new Profile();
        p2.style = Profile.Style.WATER;
        Scripted in2 = new Scripted();
        World w2 = mk(p2, trapLevel(), in2);
        w2.level.decos.add(new Level.Deco(Level.WISTERIA, 600, 700, 1.5f));
        w2.player.x = 600;
        w2.player.y = 700 - 43;
        float hp2 = w2.player.hp;
        steps(w2, in2, 40);
        boolean slayerSafe = w2.player.hp >= hp2;
        return demonHurt && slayerSafe;
    }

    static boolean testSpikes() {
        Level l = trapLevel();
        l.spike(600, 700, 120);
        Profile p = new Profile();
        Scripted in = new Scripted();
        World w = mk(p, l, in);
        w.player.x = 650;
        w.player.y = 700 - 43;
        float hp0 = w.player.hp;
        steps(w, in, 6);
        return w.player.hp < hp0;
    }

    static boolean testUnderwaterNoRecharge() {
        Profile p = new Profile();
        p.path = Profile.Path.SLAYER;
        p.style = Profile.Style.WATER;
        Scripted in = new Scripted();
        World w = mk(p, LevelData.slayerLevels()[21], in);
        if (!w.level.underwater) return false;
        w.player.sp = 20;
        in.rechargeH = true;
        steps(w, in, 180);
        return !w.player.recharging && w.player.sp < 70;
    }

    static boolean testCrusherRolls() {
        Level l = trapLevel();
        l.crusher(180, 560);
        Profile p = new Profile();
        Scripted in = new Scripted();
        World w = mk(p, l, in);
        w.player.x = 180;
        w.player.y = 700 - 43;
        steps(w, in, 220);
        Trap tr = w.traps.get(0);
        return Math.abs((tr.py + 36) - 700) < 2.5f && Math.abs(tr.px - 180) > 80;
    }

    static boolean testCrumble() {
        Level l = trapLevel();
        l.crumble(600, 600, 110);
        Profile p = new Profile();
        Scripted in = new Scripted();
        World w = mk(p, l, in);
        Trap.Crumble c = (Trap.Crumble) w.traps.get(0);
        w.player.x = 655;
        w.player.y = 600 - 44;
        w.player.vy = 50;
        steps(w, in, 10);
        int stWhileStanding = c.st[0];
        steps(w, in, 60);
        boolean fellOrGone = c.py > 640 || c.st[0] == 3;
        steps(w, in, 400);
        return stWhileStanding >= 1 && fellOrGone && c.st[0] == 0 && Math.abs(c.py - 600) < 2;
    }

    static boolean testMovingCarry() {
        Level l = trapLevel();
        l.movingPlat(600, 600, 900, 600, 120, 3f);
        Profile p = new Profile();
        Scripted in = new Scripted();
        World w = mk(p, l, in);
        Trap.MovingPlat mp = (Trap.MovingPlat) w.traps.get(0);
        w.player.x = mp.px + 60;
        w.player.y = 600 - 44;
        float x0 = w.player.x;
        steps(w, in, 90);
        return Math.abs(w.player.x - x0) > 40 && Math.abs(w.player.bottom() - mp.py) < 14;
    }

    static boolean testDojoDummy() {
        Profile p = new Profile();
        Scripted in = new Scripted();
        World w = mk(p, LevelData.dojoLevel(), in);
        if (!w.level.dojo || w.level.daytime || !w.enemies.isEmpty()) return false;
        w.spawnDojoOpponent("dummy");
        if (!(w.boss instanceof DummyEnemy) || w.enemies.size() != 1 || w.boss.maxHp != w.player.maxHp) return false;
        w.boss.hurt(w, w.player, 1, 1, 350, 80);
        steps(w, in, 4);
        return w.boss.x > w.player.x && !w.complete;
    }

    static boolean testAbilityLogCap() {
        for (int i = 0; i < 25; i++) Game.log("logcap " + i);
        java.util.List<String> lines = Game.logLines();
        return lines.size() == 20 && lines.get(0).equals("logcap 5") && lines.get(19).equals("logcap 24");
    }

    private MechanicsTest() {
    }
}
