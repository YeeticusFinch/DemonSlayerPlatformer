package game;

import java.awt.*;
import java.util.ArrayList;
import java.util.IdentityHashMap;

public class World {
    public static final float MUZAN_FEAR_RADIUS = 645f;

    public Level level;
    public Player player;
    public InputProvider in;
    public ArrayList<Fighter> enemies = new ArrayList<>();
    public ArrayList<Effect> effects = new ArrayList<>();
    public ArrayList<Ghost> ghosts = new ArrayList<>();
    public ArrayList<Debris> debris = new ArrayList<>();
    public ArrayList<Pickup> pickups = new ArrayList<>();
    public ArrayList<Trap> traps = new ArrayList<>();
    public Particles parts = new Particles();
    public Camera cam = new Camera();
    public ArrayList<Popup> popups = new ArrayList<>();
    public ArrayList<String[]> banners = new ArrayList<>();
    public float bannerT;

    public final ArrayList<Level.Plat> solids = new ArrayList<>();
    public final ArrayList<Level.Plat> oneWays = new ArrayList<>();

    public float viewL, viewR, viewT, viewB;
    public float cameraZoom = 1f;
    public float ruiTitleAlpha;
    public Fighter introFocus;
    private boolean ruiIntroActive;
    private float ruiIntroElapsed;
    public float frameDt = 1 / 60f;
    public float playerDmgFlash;
    public float fearLevel, fearSourceX, fearSourceY;
    public Fighter fearSource;

    public float time;
    public float hitstop, timeScale = 1;
    public int kills, totalEnemies;
    public boolean complete, failed;
    public float completeT, failT;
    public String failReason = "Level failed";
    public boolean bossDead;
    private float bossEndT = -1;
    public Fighter boss;
    public boolean akazaFinisherActive, akazaFinisherDone;
    public float akazaFinisherT;
    public Rengoku finisherRengoku;
    public Akaza finisherAkaza;
    private boolean akazaFinisherImpact;
    public boolean arenaMode, arenaEnded;
    public boolean lakePrompt;
    public Fighter arenaFighterA, arenaFighterB, arenaWinner;
    public int slayerLevelIndex = -1;
    public int kyogaiRoomRot;
    private final ArrayList<Level.Plat> kyogaiRoomBaseSolids = new ArrayList<>();
    private final IdentityHashMap<Level.Plat, float[]> crashPlatBases = new IdentityHashMap<>();
    private boolean muzanTriggered;

    public int boulderProgress;
    private boolean boulderStarted;
    private float boulderTimer;
    private Level.Plat boulderPlat;

    private final ArrayList<Task> tasks = new ArrayList<>();

    private static class Task {
        float t;
        java.util.function.BooleanSupplier cond;
        Runnable run;
    }

    public static class Pickup {
        public float x, y, t;
        public int type;

        public Pickup(float x, float y, int type) {
            this.x = x;
            this.y = y;
            this.type = type;
        }
    }

    public static class Popup {
        public float x, y, t;
        public String text;
        public Color color;

        public Popup(float x, float y, String text, Color c) {
            this.x = x;
            this.y = y;
            this.text = text;
            this.color = c;
        }
    }

    public World(Level l, InputProvider in, Player pl) {
        this.level = l;
        this.in = in;
        this.player = pl;
        pl.init(l.spawnX, l.spawnY, 100);
        for (Level.Plat p : l.plats) {
            if (p.oneway) oneWays.add(p);
            else solids.add(p);
            if (l.chaoticTrainCrash && p.style == Level.TRAIN_ROOF)
                crashPlatBases.put(p, new float[]{p.x, p.y, FMath.rand(0f, 6.28f)});
        }
        if (l.kyogaiRoom) for (Level.Plat p : solids) kyogaiRoomBaseSolids.add(new Level.Plat(p.x, p.y, p.w, p.h, p.style));
        if (l.boulder != null) {
            boulderPlat = new Level.Plat(l.boulder[0], l.boulder[1], l.boulder[2], l.boulder[3], Level.ROCK);
            solids.add(boulderPlat);
        }
        for (Level.Spawn s : l.spawns) {
            Fighter e = makeEnemy(s.type);
            e.init(s.x, s.y, e.maxHp);
            enemies.add(e);
            if (l.winMode == Level.WinMode.BOSS && (s.type.equals("hand") || s.type.equals("flameboss")
                    || s.type.equals("sabito") || s.type.equals("temple") || s.type.startsWith("swamp")
                    || s.type.equals("kyogai") || s.type.equals("headless_puppet") || s.type.equals("spider_mother")
                    || s.type.equals("spider_brother") || s.type.equals("spider_father") || s.type.equals("rui")
                    || s.type.equals("enmu") || s.type.equals("enmucore"))) boss = e;
            if (l.rengokuIntro && s.type.equals("rengoku")) introFocus = e;
            if (l.akazaRengokuDuel) {
                if (s.type.equals("rengoku")) {
                    boss = e;
                    e.isBoss = true;
                } else if (s.type.equals("akaza")) {
                    introFocus = e;
                    e.isBoss = true;
                }
            }
        }
        totalEnemies = 0;
        for (Fighter e : enemies) if (countsForKills(e)) totalEnemies++;
        for (Level.TrapSpec ts : l.trapSpecs) {
            switch (ts.type) {
                case "spike" -> traps.add(new Trap.Spikes(ts.a, ts.b, ts.c));
                case "crusher" -> traps.add(new Trap.Crusher(ts.a, ts.b));
                case "log" -> traps.add(new Trap.SwingLog(ts.a, ts.b, ts.c, ts.d, ts.e, FMath.rand(0, 3f)));
                case "mplat" -> traps.add(new Trap.MovingPlat(ts.a, ts.b, ts.c, ts.d, ts.e, 2.2f));
                case "crumble" -> traps.add(new Trap.Crumble(ts.a, ts.b, ts.c));
            }
        }
        if (introFocus == null && (boss instanceof Rui || boss instanceof Enmu)) introFocus = boss;
        if (!l.dojo && introFocus != null) {
            ruiIntroActive = true;
            cam.x = FMath.clamp(introFocus.x - Game.VIEW_W / 2f, 0, Math.max(0, l.w - Game.VIEW_W));
            cam.y = FMath.clamp(introFocus.y - Game.VIEW_H / 2f, 0, Math.max(0, l.h - Game.VIEW_H));
        } else {
            cam.snap(pl.x, pl.y, l.w, l.h, Game.VIEW_W, Game.VIEW_H);
        }
        updateViewBounds();
        if (l.npcLines.length > 0) {
            for (String lineText : l.npcLines) banners.add(new String[]{lineText});
            bannerT = 3.4f;
        }
    }

    public Fighter makeEnemy(String type) {
        return switch (type) {
            case "dummy" -> new DummyEnemy();
            case "demon" -> new BasicDemon();
            case "artdemon" -> new ArtDemon();
            case "human" -> new HumanEnemy(false);
            case "civilian" -> new Civilian();
            case "tamayo" -> new Tamayo();
            case "yushiro" -> new Yushiro();
            case "rengoku" -> new Rengoku(level.dojo || level.akazaRengokuDuel);
            case "akaza" -> new Akaza();
            case "hunter" -> new HumanEnemy(true);
            case "muzan" -> new Muzan();
            case "slayer_water" -> new SlayerEnemy(Profile.Style.WATER);
            case "slayer_flame" -> new SlayerEnemy(Profile.Style.FLAME);
            case "slayer_wind" -> new SlayerEnemy(Profile.Style.WIND);
            case "sabito" -> new Sabito();
            case "hand" -> new HandDemon();
            case "temple" -> new TempleDemon();
            case "swamp" -> new SwampDemon(false);
            case "swamp_strong" -> new SwampDemon(true);
            case "crawler" -> new CrawlingDemon();
            case "puppet_slayer" -> new PuppetSlayer();
            case "headless_puppet" -> new HeadlessPuppetDemon();
            case "spider_mother" -> new SpiderMother();
            case "spider_brother" -> new SpiderBrother();
            case "spider_father" -> new SpiderFather();
            case "rui" -> new Rui();
            case "enmu" -> new Enmu();
            case "enmucore" -> new EnmuCore();
            case "fleshmonster" -> new FleshMonster();
            case "fleshtentacle" -> new FleshTentacle();
            case "susumaru" -> new Susumaru();
            case "yahaba" -> new Yahaba();
            case "kyogai" -> new Kyogai();
            case "flameboss" -> new FlameBoss();
            default -> new BasicDemon();
        };
    }

    public void beginArena(String typeA, String typeB) {
        arenaMode = true;
        arenaFighterA = addArenaFighter(typeA, 720, 0);
        arenaFighterB = addArenaFighter(typeB, 1880, 1);
        totalEnemies = 2;
        cam.snap(arenaFighterA.x, arenaFighterA.y, level.w, level.h, Game.VIEW_W, Game.VIEW_H);
        updateViewBounds();
    }

    public void suicidePlayer() { suicidePlayer(false); }

    public void suicidePlayerToWake() { suicidePlayer(true); }

    private void suicidePlayer(boolean wake) {
        if (player == null || player.dead) return;
        player.suicideSlash(this);
        player.hp = 0;
        player.dead = true;
        player.deathT = 0;
        if (Game.profile != null) Game.profile.deaths++;
        cam.shake(7, 0.3f);
        if (wake) {
            complete = true;
            completeT = 0;
            deathDelay = 0;
        } else {
            deathDelay = 1.4f;
        }
    }

    public boolean beginAkazaFinisher(Rengoku rengoku) {
        if (akazaFinisherActive || akazaFinisherDone || rengoku == null) return false;
        Akaza akaza = null;
        for (Fighter f : enemies) if (f instanceof Akaza a && !a.dead) { akaza = a; break; }
        if (akaza == null) return false;
        finisherRengoku = rengoku;
        finisherAkaza = akaza;
        akazaFinisherActive = true;
        akazaFinisherT = 0;
        akazaFinisherImpact = false;
        timeScale = 0.12f;
        player.invulnT = Math.max(player.invulnT, 10f);
        rengoku.hp = 1;
        rengoku.invulnT = 10f;
        rengoku.staggerT = rengoku.stunT = 0;
        rengoku.guarding = false;
        rengoku.state = 0;
        rengoku.stateT = 0;
        akaza.x = FMath.clamp(rengoku.x + 70, 80, level.w - 80);
        akaza.y = rengoku.y;
        akaza.vx = akaza.vy = 0;
        akaza.guarding = false;
        akaza.staggerT = akaza.stunT = 0;
        akaza.face(rengoku.x - akaza.x);
        rengoku.face(akaza.x - rengoku.x);
        akaza.finisherPoseT = rengoku.finisherPoseT = 0;
        cam.shake(4, 0.3f);
        return true;
    }

    private Fighter addArenaFighter(String type, float x, int team) {
        Fighter fighter = makeEnemy(type);
        fighter.team = team;
        fighter.init(x, 700, fighter.maxHp);
        if (fighter instanceof DummyEnemy dummy) {
            dummy.maxHp = dummy.hp = 180;
            dummy.runSpeed = 190;
            dummy.aggroR = 900;
            dummy.atkRange = 80;
            dummy.dmg = 9;
        } else if (fighter instanceof Muzan muzan) {
            muzan.maxHp = muzan.hp = 500;
            muzan.runSpeed = 220;
            muzan.aggroR = 900;
            muzan.atkRange = 78;
            muzan.dmg = 12;
        } else if (fighter instanceof Civilian civilian) {
            civilian.maxHp = civilian.hp = 100;
            civilian.runSpeed = 185;
            civilian.aggroR = 700;
            civilian.atkRange = 66;
            civilian.dmg = 7;
        }
        enemies.add(fighter);
        return fighter;
    }

    public boolean isArenaFighter(Fighter f) {
        return arenaMode && (f == arenaFighterA || f == arenaFighterB);
    }

    public Fighter targetFor(Fighter fighter) {
        if (!arenaMode && level.akazaRengokuDuel) {
            Akaza akaza = null;
            Rengoku rengoku = null;
            for (Fighter e : enemies) {
                if (e instanceof Akaza a && !a.dead) akaza = a;
                else if (e instanceof Rengoku r && !r.dead) rengoku = r;
            }
            if (fighter instanceof Rengoku) return akaza != null ? akaza : fighter;
            if (fighter instanceof Akaza) {
                if (rengoku == null) return player;
                if (player.dead) return rengoku;
                return FMath.dist(fighter.x, fighter.y, player.x, player.y)
                        <= FMath.dist(fighter.x, fighter.y, rengoku.x, rengoku.y) ? player : rengoku;
            }
            return player;
        }
        if (!arenaMode) return player;
        if (fighter == null) return arenaFighterA;
        if (fighter == arenaFighterA || fighter != arenaFighterB && fighter.team == arenaFighterA.team)
            return arenaFighterB;
        return arenaFighterA;
    }

    public java.util.List<Fighter> attackTargets(Fighter source) {
        if (!arenaMode && level.akazaRengokuDuel) {
            Akaza akaza = null;
            Rengoku rengoku = null;
            for (Fighter e : enemies) {
                if (e instanceof Akaza a && !a.dead) akaza = a;
                else if (e instanceof Rengoku r && !r.dead) rengoku = r;
            }
            if (source instanceof Akaza) {
                ArrayList<Fighter> targets = new ArrayList<>(2);
                if (player != null && !player.dead) targets.add(player);
                if (rengoku != null) targets.add(rengoku);
                return targets;
            }
            if (source instanceof Rengoku || source == player)
                return akaza == null ? java.util.List.of() : java.util.List.of(akaza);
        }
        if (arenaMode) return java.util.List.of(targetFor(source));
        return source != null && source.team == 0 ? enemies : java.util.List.of(player);
    }

    public boolean compassTeleport(Fighter source) {
        if (source == null || source.compassNeedleT <= 0) return false;
        Fighter target = null;
        float nearest = Float.POSITIVE_INFINITY;
        for (Fighter f : attackTargets(source)) {
            if (f == null || f.dead || f instanceof Civilian) continue;
            float d = FMath.dist(source.x, source.y, f.x, f.y);
            if (d < nearest) { nearest = d; target = f; }
        }
        if (target == null || nearest > 920) return false;
        float dir = Math.signum(target.x - source.x);
        if (dir == 0) dir = source.facing();
        float wantedX = FMath.clamp(target.x - dir * (target.w * 0.5f + source.w * 0.5f + 14),
                source.w * 0.5f, level.w - source.w * 0.5f);
        float destX = Float.NaN, destY = Float.NaN, destGround = 99999;
        for (int step = 0; step <= 6; step++) {
            float cx = step == 0 ? wantedX : FMath.clamp(wantedX - dir * step * 38f,
                    source.w * 0.5f, level.w - source.w * 0.5f);
            float ground = groundYUnder(cx, target.bottom() + 60);
            float cy = target.y;
            if (target.onGround && ground < 9000) cy = ground - source.h * 0.5f;
            else if (ground < 9000 && cy + source.h * 0.5f > ground - 2)
                cy = ground - source.h * 0.5f;
            cy = FMath.clamp(cy, source.h * 0.5f, level.h - source.h * 0.5f);
            if (!overlapsSolidAt(source, cx, cy)) {
                destX = cx;
                destY = cy;
                destGround = ground;
                break;
            }
        }
        if (Float.isNaN(destX)) return false;
        source.x = destX;
        source.y = destY;
        source.vx = source.vy = 0;
        source.onGround = destGround < 9000 && Math.abs(source.bottom() - destGround) < 4;
        source.face(dir);
        parts.ring(target.x, target.y - target.h * 0.4f, new Color(152, 218, 255));
        return true;
    }

    private boolean overlapsSolidAt(Fighter fighter, float cx, float cy) {
        float left = cx - fighter.w * 0.5f, right = cx + fighter.w * 0.5f;
        float top = cy - fighter.h * 0.5f, bottom = cy + fighter.h * 0.5f;
        for (Level.Plat p : solids)
            if (right > p.x && left < p.x + p.w && bottom > p.y + 1 && top < p.y + p.h) return true;
        for (Level.Plat p : oneWays)
            if (right > p.x && left < p.x + p.w && bottom > p.y + 1 && top < p.y + p.h) return true;
        for (Trap trap : traps)
            if (trap.solidNow() && right > trap.px && left < trap.px + trap.pw
                    && bottom > trap.py + 1 && top < trap.py + trap.ph) return true;
        return false;
    }

    public void rotateKyogaiRoom(Kyogai kyogai, int dir) {
        if (!level.kyogaiRoom) return;
        kyogaiRoomRot = Math.floorMod(kyogaiRoomRot + (dir > 0 ? 1 : -1), 4);
        applyKyogaiRoomRotation();
        rotateFighter(kyogai, dir);
        kyogai.roomRot = kyogaiRoomRot;
        kyogai.face(player.x - kyogai.x);
        cam.shake(14, 0.5f);
        hitstop = Math.max(hitstop, 0.08f);
        if (!player.dead && player.onGround) {
            float away = Math.signum(player.x - kyogai.x);
            if (away == 0) away = -kyogai.facing();
            player.consume(player.maxSp * 0.3f);
            player.hurt(this, kyogai, 16, away, 1050, 620);
            player.flashT = Math.max(player.flashT, 0.24f);
            parts.ring(player.x, player.y - player.h * 0.3f, new Color(255, 255, 255, 210));
        }
    }

    private void applyKyogaiRoomRotation() {
        for (int i = 0; i < solids.size() && i < kyogaiRoomBaseSolids.size(); i++) {
            Level.Plat dst = solids.get(i);
            Level.Plat src = kyogaiRoomBaseSolids.get(i);
            setRotatedPlat(dst, src, kyogaiRoomRot);
        }
    }

    private void setRotatedPlat(Level.Plat dst, Level.Plat src, int rot) {
        float cx = src.x + src.w / 2f, cy = src.y + src.h / 2f;
        float dx = cx - level.kyogaiCx, dy = cy - level.kyogaiCy;
        float nx = cx, ny = cy, nw = src.w, nh = src.h;
        for (int i = 0; i < rot; i++) {
            float ndx = ny - level.kyogaiCy;
            float ndy = -(nx - level.kyogaiCx);
            nx = level.kyogaiCx + ndx;
            ny = level.kyogaiCy + ndy;
            float t = nw;
            nw = nh;
            nh = t;
        }
        dst.x = nx - nw / 2f;
        dst.y = ny - nh / 2f;
        dst.w = nw;
        dst.h = nh;
    }

    private void rotateFighter(Fighter f, int dir) {
        float dx = f.x - level.kyogaiCx, dy = f.y - level.kyogaiCy;
        f.x = dir > 0 ? level.kyogaiCx + dy : level.kyogaiCx - dy;
        f.y = dir > 0 ? level.kyogaiCy - dx : level.kyogaiCy + dx;
        f.vx = f.vy = 0;
    }

    public void spawnDojoOpponent(String type) {
        enemies.clear();
        effects.clear();
        ghosts.clear();
        popups.clear();
        bossDead = false;
        bossEndT = -1;
        kills = 0;
        totalEnemies = 1;
        Fighter e = makeEnemy(type);
        e.init(Math.min(level.w - 480, player.x + 620), level.spawnY + 4, e.maxHp);
        e.face(player.x - e.x);
        enemies.add(e);
        boss = e;
        banner(e.name, "Opponent selected");
    }

    private boolean countsForKills(Fighter f) {
        return !(f instanceof Civilian) && !(f instanceof Muzan)
                && (!(f instanceof Rengoku r) || r.aggressive);
    }

    public void step(float rdt) {
        if (arenaMode && arenaEnded) {
            updateCamera(rdt);
            return;
        }
        frameDt = rdt;
        time += rdt;
        cam.update(rdt);
        playerDmgFlash = Math.max(0, playerDmgFlash - rdt * 2.4f);
        if (bannerT > 0) {
            bannerT -= rdt;
            if (bannerT <= 0 && !banners.isEmpty()) banners.remove(0);
            if (!banners.isEmpty()) bannerT = 3.4f;
        }
        if (hitstop > 0) {
            hitstop -= rdt;
            parts.update(rdt * 0.2f);
            return;
        }
        if (failed) {
            failT += rdt;
            updateCamera(rdt);
            return;
        }
        float dt = rdt * timeScale;

        updateAkazaFinisher(rdt);

        updateCrashTrainPlatforms();
        if (level.chaoticTrainCrash && FMath.chance(dt * 1.8f))
            cam.shake(FMath.rand(2.2f, 5.2f), FMath.rand(0.1f, 0.23f));

        if (!complete) for (Trap t : traps) t.update(this, dt);

        if (boulderStarted && boulderProgress < 100) {
            boulderTimer += dt;
            boulderProgress = (int) FMath.clamp(boulderTimer / 1.1f * 100, 0, 100);
            if (boulderProgress % 8 == 0 && FMath.chance(0.6f)) {
                float[] b = level.boulder;
                parts.burst(Particles.CIRCLE, b[0] + FMath.rand(0, b[2]), b[1] + FMath.rand(0, b[3]), 4,
                        120f, 0.6f, 9f, new Color(150, 140, 130), 300f, 0.95f);
                cam.shake(4, 0.12f);
            }
        }

        updateFear();

        if (!akazaFinisherActive && !arenaMode && !player.dead && !complete && !failed) player.update(this, dt, in);
        else if (player.dead) {
            player.deathT += dt;
            player.suicidePoseT = Math.max(0, player.suicidePoseT - dt);
            player.atkT = Math.max(0, player.atkT - dt);
        }
        if (level.dojo) recoverDojoBoundary(player);
        lakePrompt = level.frozenLake && player.onGround && player.bottom() >= level.frozenLakeY - 4
                && player.x >= level.frozenLakeX && player.x <= level.frozenLakeX + level.frozenLakeW;

        boolean frozen = complete;
        // Enemy updates may summon reinforcements. Iterate a snapshot so those additions
        // become active next frame instead of invalidating this frame's traversal.
        for (Fighter e : new ArrayList<>(enemies)) {
            if (!enemies.contains(e)) continue;
            if (akazaFinisherActive) {
                e.vx = e.vy = 0;
                continue;
            }
            boolean onScreen = e.x > viewL - 240 && e.x < viewR + 240 && e.y > viewT - 260 && e.y < viewB + 420;
            if (!frozen && (arenaMode || e.isBoss || onScreen)) e.update(this, dt);
            else if (e.dead && (e.deathT += dt) > 0.1f) { /* corpse timer */ }
            if (level.dojo && !e.dead) recoverDojoBoundary(e);
            if (e.dead && e.deathT > 1.1f) enemies.remove(e);
        }
        updateFear();
        for (int i = effects.size() - 1; i >= 0; i--) {
            Effect ef = effects.get(i);
            ef.update(this, dt);
            if (ef.dead) effects.remove(i);
        }
        for (int i = ghosts.size() - 1; i >= 0; i--) {
            ghosts.get(i).update(dt);
            if (ghosts.get(i).dead()) ghosts.remove(i);
        }
        for (Task t : new ArrayList<>(tasks)) {
            if (t.cond != null && !t.cond.getAsBoolean()) continue;
            t.t -= dt;
            if (t.t <= 0) {
                t.run.run();
                tasks.remove(t);
            }
        }
        parts.update(dt);
        while (parts.size() > 1000) parts.removeOldest();

        for (int i = popups.size() - 1; i >= 0; i--) {
            Popup p = popups.get(i);
            p.t += dt;
            p.y -= dt * 46;
            if (p.t > 1) popups.remove(i);
        }
        for (int i = pickups.size() - 1; i >= 0; i--) {
            Pickup p = pickups.get(i);
            p.t += dt;
            if (p.t > 14) { pickups.remove(i); continue; }
            if (!player.dead && FMath.dist(p.x, p.y, player.x, player.y) < 40) {
                if (p.type == 0) {
                    player.heal(this, 22);
                    popup(p.x, p.y - 10, "+22", new Color(120, 255, 160));
                } else {
                    player.sp = Math.min(player.maxSp, player.sp + 35);
                    popup(p.x, p.y - 10, "+SP", new Color(110, 190, 255));
                }
                parts.ring(p.x, p.y, p.type == 0 ? new Color(120, 255, 160) : new Color(110, 190, 255));
                pickups.remove(i);
            }
        }
        for (int i = debris.size() - 1; i >= 0; i--) {
            Debris d = debris.get(i);
            d.update(this, dt);
            if (d.dead()) debris.remove(i);
        }

        if (!complete) {
            checkSun(dt);
            checkWisteria(dt);
            checkMuzanEncounter();
        }
        if (arenaMode && !arenaEnded && (arenaFighterA.dead || arenaFighterB.dead)) {
            arenaEnded = true;
            arenaWinner = arenaFighterA.dead ? arenaFighterB : arenaFighterA;
        }
        updateCamera(rdt);

        if (!level.dojo && level.winMode != Level.WinMode.BOSS && !complete && goalUnlocked()
                && player.overlapsRect(level.goalX, level.goalY, 90, 150)) win();
        if (!level.dojo && !goalUnlocked() && level.winMode == Level.WinMode.GOAL_AFTER_KILLS
                && player.overlapsRect(level.goalX - 60, level.goalY - 60, 210, 270)) {
            if (FMath.chance(rdt * 0.8f))
                popup(player.x, player.y - player.h - 16, (totalEnemies - kills) + " foes remain", new Color(255, 200, 120));
        }

        if (!akazaFinisherActive && !level.akazaRengokuDuel && !level.dojo && boss != null && boss.dead && bossEndT < 0 && !bossDead) {
            bossDead = true;
            bossEndT = 0;
            timeScale = 0.22f;
            cam.shake(10, 0.5f);
            hitstop = 0.18f;
            banner("VICTORY", "");
        }
        if (bossEndT >= 0) {
            bossEndT += rdt;
            timeScale = FMath.approach(timeScale, 1, rdt * 0.7f);
            if (bossEndT > 1.7f) win();
        }
        if (player.dead && deathDelay > 0) {
            deathDelay -= rdt;
            if (deathDelay <= 0) failed = true;
        }
    }

    private void updateFear() {
        fearLevel = 0;
        fearSource = null;
        if (arenaMode || player.dead) return;
        for (Fighter e : enemies) {
            boolean muzan = e instanceof Muzan;
            boolean akaza = e instanceof Akaza;
            if ((!muzan && !akaza) || e.dead) continue;
            float radius = muzan ? MUZAN_FEAR_RADIUS : MUZAN_FEAR_RADIUS * 0.84f;
            float maxFear = muzan ? 10f : 6.2f;
            float d = FMath.dist(player.x, player.y, e.x, e.y);
            if (d >= radius) continue;
            float level = d <= radius * 0.4f
                    ? maxFear
                    : Math.max(maxFear * 0.1f, (1f - d / radius) / 0.6f * maxFear);
            if (level > fearLevel) {
                fearLevel = level;
                fearSourceX = e.x;
                fearSourceY = e.y;
                fearSource = e;
            }
        }
    }

    private void updateAkazaFinisher(float rdt) {
        if (!akazaFinisherActive && !akazaFinisherDone) return;
        if (akazaFinisherT < 5.8f) akazaFinisherT += rdt;
        if (finisherRengoku != null) finisherRengoku.finisherPoseT = akazaFinisherT;
        if (finisherAkaza != null) finisherAkaza.finisherPoseT = akazaFinisherT;
        if (akazaFinisherActive) {
            timeScale = 0.12f;
            if (!akazaFinisherImpact && akazaFinisherT >= 1.55f) {
                akazaFinisherImpact = true;
                float ix = finisherRengoku.x, iy = finisherRengoku.y - finisherRengoku.h * 0.28f;
                parts.burst(Particles.DROP, ix, iy, 26, 260, 0.58f, 10, new Color(173, 26, 48), 80, 0.92f);
                parts.burst(Particles.CIRCLE, ix, iy, 20, 220, 0.42f, 7, new Color(215, 58, 76), 40, 0.94f);
                cam.shake(9, 0.42f);
            }
            if (akazaFinisherT >= 4.85f && finisherRengoku != null && !finisherRengoku.dead) {
                finisherRengoku.hp = 0;
                finisherRengoku.die(this);
                bossDead = true;
                complete = true;
                completeT = 0;
                akazaFinisherActive = false;
                akazaFinisherDone = true;
                banner("VICTORY", "");
            }
        }
    }

    private void updateCrashTrainPlatforms() {
        if (!level.chaoticTrainCrash) return;
        for (var entry : crashPlatBases.entrySet()) {
            Level.Plat p = entry.getKey();
            float[] base = entry.getValue();
            float phase = base[2];
            p.x = base[0] + FMath.sin(time * 1.7f + phase) * 11f;
            p.y = base[1] + FMath.sin(time * 3.1f + phase) * 28f
                    + FMath.sin(time * 6.8f + phase * 1.7f) * 12f;
        }
    }

    public float fearSlowdown() {
        return FMath.clamp(1f - 0.09f * fearLevel, 0.1f, 1f);
    }

    public boolean playerMovingTowardFear(float inputDir) {
        if (fearLevel <= 0 || inputDir == 0) return false;
        return Math.signum(fearSourceX - player.x) == Math.signum(inputDir);
    }

    private float deathDelay;

    private void win() {
        if (complete) return;
        complete = true;
        completeT = 0;
        player.vx = 0;
        player.guarding = false;
    }

    public boolean goalUnlocked() {
        if (level.boulder != null && boulderProgress < 100) return false;
        if (level.winMode == Level.WinMode.GOAL_AFTER_KILLS && kills < totalEnemies) return false;
        return true;
    }

    private void checkSun(float dt) {
        if (!level.daytime) return;
        if (player.isDemon && !player.dead) {
            player.burning = !skyCovered(player.x, player.top());
            if (player.burning) {
                player.chip(this, 22 * dt);
                spawnBurnFlames(player, dt * 34);
            }
        } else player.burning = false;
        for (Fighter e : enemies) {
            if (!e.isDemon || e.dead) continue;
            if (!skyCovered(e.x, e.top())) {
                e.chip(this, 34 * dt);
                spawnBurnFlames(e, dt * 26);
            }
        }
    }

    private void spawnBurnFlames(Fighter f, float rate) {
        if (!FMath.chance(rate / 30f)) return;
        parts.spawn(Particles.EMBER,
                f.x + FMath.rand(-f.w * 0.55f, f.w * 0.55f),
                f.top() + FMath.rand(0, f.h),
                FMath.rand(-35, 35), FMath.rand(-180, -70), 0.55f, 9, new Color(255, 140, 50), -90, 0.95f);
        if (FMath.chance(0.4f))
            parts.spawn(Particles.FIRE,
                    f.x + FMath.rand(-10, 10), f.top() + FMath.rand(6, f.h - 8),
                    FMath.rand(-20, 20), FMath.rand(-90, -30), 0.45f, 13, new Color(255, 90, 30), -50, 0.96f);
    }

    private void checkWisteria(float dt) {
        for (Level.Deco d : level.decos) {
            if (d.type != Level.WISTERIA) continue;
            if (d.x < viewL - 200 || d.x > viewR + 200 || d.y < viewT - 300 || d.y > viewB + 100) continue;
            ambientPetal(d, dt);
            float auraHalfW = 130 * d.s;
            float top = d.y - 230 * d.s;
            float bot = d.y + 25 * d.s;
            for (Fighter f : enemies) {
                if (!f.isDemon || f.dead || f.isBoss) continue;
                if (inAura(f, d.x, top, bot, auraHalfW)) poison(f, dt, 9 * dt);
            }
            if (player.isDemon && !player.dead && inAura(player, d.x, top, bot, auraHalfW))
                poison(player, dt, 7.5f * dt);
        }
    }

    private boolean inAura(Fighter f, float cx, float topY, float botY, float halfW) {
        return Math.abs(f.x - cx) < halfW && f.bottom() > topY && f.top() < botY;
    }

    private void poison(Fighter f, float dt, float dmg) {
        f.chip(this, dmg);
        f.wisteriaT = 0.3f;
        if (f == player) player.consume(15 * dt);
        if (FMath.chance(dt * 26)) {
            parts.spawn(Particles.CIRCLE,
                    f.x + FMath.rand(-f.w * 0.6f, f.w * 0.6f),
                    f.top() + FMath.rand(0, f.h),
                    FMath.rand(-25, 25), FMath.rand(-120, -40), 0.6f, 7,
                    new Color(178, 110, 235), -60, 0.97f);
        }
        if (FMath.chance(dt * 14)) {
            parts.spawn(Particles.PETAL,
                    f.x + FMath.rand(-24, 24), f.top() + FMath.rand(-8, f.h * 0.5f),
                    FMath.rand(-50, 50), FMath.rand(-90, -20), 0.7f, 6,
                    new Color(205, 150, 245), 130, 0.98f);
        }
    }

    private void ambientPetal(Level.Deco d, float dt) {
        if (!FMath.chance(dt * 2.2f)) return;
        parts.spawn(Particles.PETAL,
                d.x + FMath.rand(-80 * d.s, 80 * d.s),
                d.y - FMath.rand(90 * d.s, 160 * d.s),
                FMath.rand(-18, 18), FMath.rand(22, 55), FMath.rand(1.4f, 2.4f), 5,
                new Color(196, 140, 238), 26, 1f);
    }

    public boolean skyCovered(float x, float headY) {
        for (Level.Plat p : solids) {
            float py = p.slope != 0 ? slopeY(p, x) : p.y;
            if (x > p.x - 6 && x < p.x + p.w + 6 && py < headY) return true;
        }
        if (level.boulder != null && boulderProgress < 100 && x > level.boulder[0] && x < level.boulder[0] + level.boulder[2]) return true;
        return false;
    }

    private void updateCamera(float rdt) {
        if (arenaMode && arenaFighterA != null) {
            cameraZoom = 1f;
            cam.follow(arenaFighterA.x, arenaFighterA.y - 40, level.w, level.h, Game.VIEW_W, Game.VIEW_H, rdt);
            updateViewBounds();
            return;
        }
        if (akazaFinisherActive || akazaFinisherDone) {
            float focusX = finisherRengoku == null ? player.x : finisherRengoku.x;
            float focusY = finisherRengoku == null ? player.y : finisherRengoku.y;
            if (akazaFinisherT >= 1.25f && finisherAkaza != null && finisherRengoku != null)
                focusX = (finisherAkaza.x + finisherRengoku.x) * 0.5f;
            cameraZoom = FMath.lerp(1f, 2.05f, FMath.easeOut(FMath.clamp(akazaFinisherT / 0.8f, 0, 1)));
            cam.follow(focusX, focusY + 46, level.w, level.h, Game.VIEW_W, Game.VIEW_H, rdt);
            updateViewBounds();
            return;
        }
        if (ruiIntroActive && introFocus != null && !introFocus.dead) {
            ruiIntroElapsed += rdt;
            float focusX = introFocus.x, focusY = introFocus.y;
            if (ruiIntroElapsed < 1f) ruiTitleAlpha = 0;
            else if (ruiIntroElapsed < 2f) ruiTitleAlpha = FMath.easeOut(ruiIntroElapsed - 1f);
            else if (ruiIntroElapsed < 4f) ruiTitleAlpha = 1f;
            else if (ruiIntroElapsed < 5f) ruiTitleAlpha = 1f - FMath.easeIn(ruiIntroElapsed - 4f);
            else ruiTitleAlpha = 0;
            if (ruiIntroElapsed < 0.5f) {
                float zoomIn = FMath.easeOut(FMath.clamp(ruiIntroElapsed / 0.5f, 0, 1));
                cameraZoom = FMath.lerp(1f, 2.2f, zoomIn);
            } else if (ruiIntroElapsed < 3f) {
                cameraZoom = 2.2f;
            } else if (ruiIntroElapsed < 3.6f) {
                float returnT = FMath.easeInOut(FMath.clamp((ruiIntroElapsed - 3f) / 0.6f, 0, 1));
                focusX = FMath.lerp(introFocus.x, player.x, returnT);
                focusY = FMath.lerp(introFocus.y, player.y - 40, returnT);
                cameraZoom = FMath.lerp(2.2f, 1f, returnT);
            } else if (ruiIntroElapsed < 5f) {
                cameraZoom = 1f;
                float tx = player.x, ty = player.y - 40;
                cam.follow(tx, ty, level.w, level.h, Game.VIEW_W, Game.VIEW_H, rdt);
                updateViewBounds();
                return;
            } else {
                ruiIntroActive = false;
                introFocus = null;
                cameraZoom = 1f;
                ruiTitleAlpha = 0;
            }
            if (ruiIntroActive) {
                cam.follow(focusX, focusY + 60, level.w, level.h, Game.VIEW_W, Game.VIEW_H, rdt);
                updateViewBounds();
                return;
            }
        }
        cameraZoom = 1f;
        float tx = player.x, ty = player.y - 40;
        if (boss != null && !boss.dead) {
            float midX = (player.x + boss.x) / 2f;
            if (FMath.dist(player.x, player.y, boss.x, boss.y) < 700) tx = FMath.lerp(tx, midX, 0.45f);
        }
        cam.follow(tx, ty, level.w, level.h, Game.VIEW_W, Game.VIEW_H, rdt);
        updateViewBounds();
    }

    private void updateViewBounds() {
        viewL = cam.x - 420;
        viewR = cam.x + Game.VIEW_W + 420;
        viewT = cam.y - 300;
        viewB = cam.y + Game.VIEW_H + 340;
    }

    public void playerFell() {
        if (player.dead) return;
        if (player.tryNezukoRescue(this)) {
            player.x = level.spawnX;
            player.y = level.spawnY - player.h / 2f;
            player.vx = player.vy = 0;
            cam.snap(player.x, player.y, level.w, level.h, Game.VIEW_W, Game.VIEW_H);
            updateViewBounds();
            return;
        }
        player.hp = 0;
        player.die(this);
        if (Game.profile != null) Game.profile.deaths++;
        deathDelay = 0.9f;
    }

    public void onHurt(Fighter f, float dmg) {
        if (f == player) {
            player.noteHurt();
            playerDmgFlash = Math.min(1, playerDmgFlash + 0.35f + dmg / 32f);
            cam.shake(Math.min(7, 2 + dmg * 0.25f), 0.18f);
        }
    }

    public void impactFeedback(float x, float y, Color c) {
        parts.burst(Particles.SPARK, x, y, 10, 300, 0.24f, 7, c, 120, 0.9f);
        parts.ring(x, y, new Color(255, 255, 255, 170));
    }

    public void onFighterDeath(Fighter f) {
        spawnRagdoll(f);
        if (f == player) {
            deathDelay = 1.4f;
            cam.shake(8, 0.4f);
            hitstop = 0.15f;
            if (Game.profile != null) Game.profile.deaths++;
            return;
        }
        if (f instanceof Civilian civ && level.protectCivilians && !civ.converted) {
            failed = true;
            failT = 0;
            failReason = "Level failed, a civilian died";
            banner("Level failed", "A civilian died.");
            return;
        }
        kills++;
        if (level.dojo) return;
        Profile pr = Game.profile;
        if (pr != null) pr.totalKills++;
        hitstop = Math.max(hitstop, 0.06f);
        cam.shake(3, 0.15f);
        if (Game.profile != null && Game.profile.path == Profile.Path.DEMON && FMath.chance(0.38f))
            pickups.add(new Pickup(f.x, f.y - 20, 0));
        else if (Game.profile != null && Game.profile.path == Profile.Path.SLAYER && FMath.chance(0.33f))
            pickups.add(new Pickup(f.x, f.y - 20, 1));
        if (level.boulder != null && !boulderStarted && f instanceof Sabito) {
            schedule(0.9f, () -> {
                boulderStarted = true;
                boulderTimer = 0;
                if (boulderPlat != null) solids.remove(boulderPlat);
                banner("The boulder...", "...is cut in two.");
            });
        }
    }

    public void spawnRagdoll(Fighter f) {
        Color skin = f.isDemon ? new Color(214, 206, 218) : new Color(238, 205, 168);
        Color cloth = bodyClothColor(f);
        float cx = f.x, cy = f.y;
        addPiece(Debris.HEAD, cx, cy - f.h * 0.42f, skin, 7);
        addPiece(Debris.TORSO, cx, cy - f.h * 0.1f, cloth, 8);
        addPiece(Debris.LIMB, cx - 6, cy + f.h * 0.1f, cloth.darker(), 5);
        addPiece(Debris.LIMB, cx + 6, cy + f.h * 0.1f, cloth.darker(), 5);
        addPiece(Debris.LIMB, cx - 8, cy - f.h * 0.25f, skin.darker(), 4.5f);
        addPiece(Debris.LIMB, cx + 8, cy - f.h * 0.25f, skin.darker(), 4.5f);
        for (int i = 0; i < 3; i++)
            addPiece(Debris.CHUNK, cx + FMath.rand(-10, 10), cy + FMath.rand(-16, 10),
                    new Color(150, 25, 45), 4 + FMath.rand(0, 3));
        parts.burst(Particles.SPARK, cx, cy, 14, 320, 0.35f, 7, new Color(255, 230, 230), 200, 0.92f);
        parts.burst(Particles.CIRCLE, cx, cy, 10, 220, 0.5f, 9, new Color(140, 20, 40), 260, 0.93f);
    }

    private Color bodyClothColor(Fighter f) {
        if (f.isDemon) return new Color(44, 32, 54);
        if (f instanceof Sabito) return new Color(52, 56, 66);
        if (f instanceof HandDemon) return new Color(94, 112, 88);
        if (f instanceof TempleDemon) return new Color(42, 84, 150);
        if (f instanceof SwampDemon) return new Color(22, 104, 104);
        if (f instanceof Susumaru) return new Color(206, 84, 110);
        if (f instanceof Yahaba) return new Color(84, 78, 92);
        if (f instanceof FlameBoss) return new Color(44, 38, 52);
        if (f instanceof SlayerEnemy se)
            return se.style == Profile.Style.FLAME ? new Color(140, 46, 34)
                    : se.style == Profile.Style.WIND ? new Color(48, 126, 76) : new Color(40, 92, 128);
        return new Color(122, 88, 58);
    }

    private void addPiece(int kind, float px, float py, Color c, float size) {
        float a = FMath.rand(0, 6.28f);
        float s = FMath.rand(120, 330);
        debris.add(new Debris(kind, px, py, FMath.cos(a) * s, -FMath.rand(120, 330), size, FMath.rand(1.1f, 1.7f), c));
    }

    public void limbSevered(float ax, float ay, boolean demon) {
        parts.burst(Particles.CIRCLE, ax, ay, 14, 260, 0.5f, 8, new Color(170, 20, 40), 320, 0.92f);
        parts.ring(ax, ay, new Color(255, 90, 110));
        for (int i = 0; i < 2; i++)
            debris.add(new Debris(Debris.LIMB, ax, ay, FMath.rand(-160, 160), -FMath.rand(120, 300),
                    FMath.rand(3.5f, 5.5f), 1.2f, demon ? new Color(214, 206, 218) : new Color(150, 130, 110)));
    }

    public void schedule(float delay, Runnable r) {
        Task t = new Task();
        t.t = delay;
        t.run = r;
        tasks.add(t);
    }

    public void scheduleWhen(java.util.function.BooleanSupplier cond, Runnable r) {
        Task t = new Task();
        t.t = 3;
        t.cond = cond;
        t.run = r;
        tasks.add(t);
    }

    public void meleeStrike(Fighter src, float reach, float dmg, float kb, float up, double angCenter, double sweep, Color c) {
        compassTeleport(src);
        dmg *= src.outgoingDamageMult();
        float cx = src.x + src.facing() * reach * 0.52f;
        float cy = src.y - 8;
        float hw = reach * 0.66f, hh = 46 + (up > 150 ? 26 : 0);
        boolean anyHit = false;
        for (Fighter f : attackTargets(src)) {
            if (f.dead || !f.overlapsRect(cx - hw, cy - hh, hw * 2, hh * 2)) continue;
            float dir = Math.signum(f.x - src.x);
            if (dir == 0) dir = src.team == 0 ? src.facing() : -src.facing();
            if (f.hurt(this, src, dmg, dir, kb, up)) {
                anyHit = true;
                impactFeedback(f.x, cy, c);
                f.vx *= 0.55f;
            }
        }
        for (int i = effects.size() - 1; i >= 0; i--) {
            Effect e = effects.get(i);
            if (e.dead || e.kind != Effect.THREAD || e.unbreakable || e.owner == null || e.owner.team == src.team) continue;
            boolean sever;
            if (e.ex2 == 6 || e.ex2 == 8) {
                if (e.warmup > 0) continue;
                float d = FMath.dist(cx, cy, e.x, e.y);
                sever = Math.abs(d - e.r) <= FMath.dist(0, 0, hw, hh) + 16;
            } else {
                float eh = e.ex1;
                sever = e.x > cx - hw - 36 && e.x < cx + hw + 36 && e.y + eh > cy - hh - 20 && e.y - eh < cy + hh + 20;
            }
            if (sever) {
                e.dead = true;
                e.severed = true;
                parts.burst(Particles.CIRCLE, e.x, e.y, 9, 200, 0.32f, 5, e.c1, 160, 0.95f);
                parts.burst(Particles.SPARK, e.x, e.y, 6, 160, 0.25f, 4, e.c2, 120, 0.95f);
            }
        }
        if (anyHit) {
            src.dealtHit();
            if (src.team == 0 && !arenaMode) player.sp = Math.min(player.maxSp, player.sp + 4);
            hitstop = Math.max(hitstop, 0.05f);
            cam.shake(2.6f, 0.11f);
        }
        Effect arc = new Effect(Effect.ARC, src, src.team == 0)
                .at(src.x + src.facing() * 16, cy)
                .radius(reach * 1.08f)
                .life(0.19f);
        arc.angle = (float) angCenter;
        arc.ex1 = (float) sweep;
        arc.colors(c, Color.WHITE);
        effects.add(arc);
    }

    private void recoverDojoBoundary(Fighter fighter) {
        if (fighter == null || fighter.dead) return;
        float halfW = fighter.w * 0.5f, halfH = fighter.h * 0.5f;
        float minX = 42 + halfW, maxX = level.w - 42 - halfW;
        float minY = 222 + halfH, maxY = 700 - halfH;
        float safeX = FMath.clamp(fighter.x, minX, maxX);
        float safeY = FMath.clamp(fighter.y, minY, maxY);
        if (safeX == fighter.x && safeY == fighter.y) return;
        fighter.x = safeX;
        fighter.y = safeY;
        fighter.vx = fighter.vy = 0;
        fighter.onGround = Math.abs(safeY - maxY) < 1f;
        fighter.touchingWall = 0;
    }

    public void severWhiteThreadBoundaries(Effect attack) {
        if (attack == null || attack.owner == null || attack.kind == Effect.THREAD || attack.dmg <= 0) return;
        float attackRadius = Math.max(1, attack.r);
        for (Effect thread : effects) {
            if (thread.dead || thread.kind != Effect.THREAD || thread.ex2 != 8 || thread.warmup > 0 || thread.unbreakable
                    || thread.owner == null || thread.owner.team == attack.owner.team) continue;
            float distance = FMath.dist(attack.x, attack.y, thread.x, thread.y);
            float borderRadius = thread.r;
            float halfWidth = 16f;
            if (Math.abs(distance - borderRadius) <= attackRadius + halfWidth) {
                thread.dead = true;
                thread.severed = true;
                parts.burst(Particles.CIRCLE, thread.x, thread.y, 12, 230, 0.34f, 6, thread.c1, 180, 0.94f);
                parts.burst(Particles.SPARK, attack.x, attack.y, 8, 200, 0.28f, 5, thread.c2, 150, 0.94f);
            }
        }
    }

    public void castAbility(Player p, Ability a) {
        Game.log("Player used " + a.form);
        AbilityCast.cast(this, p, a);
    }

    public void castAbility(Fighter f, Ability a) {
        Game.log((f.name == null || f.name.isEmpty() ? "NPC" : f.name) + " used " + a.form);
        AbilityCast.cast(this, f, a);
    }

    public void castAbility(Fighter f, Ability a, boolean isHashira) {
        Game.log((f.name == null || f.name.isEmpty() ? "NPC" : f.name) + " used " + a.form);
        AbilityCast.cast(this, f, a, isHashira);
    }

    public void popup(float x, float y, String text, Color c) {
        popups.add(new Popup(x, y, text, c));
    }

    public void banner(String text, String sub) {
        banners.clear();
        banners.add(new String[]{text, sub});
        bannerT = 2.6f;
    }

    public boolean solidAtPoint(float x, float y) {
        for (Level.Plat p : solids) {
            if (p.slope != 0) {
                float sy = slopeY(p, x);
                if (x >= p.x && x <= p.x + p.w && y >= sy && y <= sy + 24) return true;
                continue;
            }
            if (x >= p.x && x <= p.x + p.w && y >= p.y && y <= p.y + p.h) return true;
        }
        return false;
    }

    public float groundYUnder(float x, float belowY) {
        float best = 99999;
        for (Level.Plat p : level.plats) {
            float py = p.slope != 0 ? slopeY(p, x) : p.y;
            if (x >= p.x && x <= p.x + p.w && py >= belowY - 60 && py < best) best = py;
        }
        for (Trap t : traps)
            if (t.solidNow() && x >= t.px && x <= t.px + t.pw && t.py >= belowY - 60 && t.py < best) best = t.py;
        return best;
    }

    public boolean onOneWay(Fighter f) {
        for (Level.Plat p : oneWays)
            if (Math.abs(f.bottom() - p.y) < 6 && f.right() > p.x && f.left() < p.x + p.w) return true;
        return false;
    }

    private void checkMuzanEncounter() {
        if (!level.muzanEncounter || muzanTriggered) return;
        Muzan muzan = null;
        for (Fighter e : enemies) if (e instanceof Muzan m && !m.dead) { muzan = m; break; }
        if (muzan == null || Math.abs(player.x - muzan.x) > 260) return;
        muzanTriggered = true;
        Civilian victim = null;
        for (Fighter e : enemies) if (e instanceof Civilian c && !c.dead) { victim = c; break; }
        float sx = muzan.x, sy = muzan.bottom();
        if (victim != null) {
            victim.converted = true;
            enemies.remove(victim);
        }
        BasicDemon demon = new BasicDemon(34, 78, 52, 165, 10);
        demon.init(sx, sy, demon.maxHp);
        demon.face(player.x - demon.x);
        enemies.add(demon);
        kills = Math.max(0, kills);
        totalEnemies++;
        muzan.escaping = true;
        banner("Muzan vanishes", "A civilian twists into a demon!");
    }

    public float shadowGround(Fighter f) {
        float best = 99999;
        for (Level.Plat p : level.plats) {
            float py = p.slope != 0 ? slopeY(p, f.x) : p.y;
            if (f.x > p.x && f.x < p.x + p.w && py >= f.bottom() - 6 && py < best) best = py;
        }
        return best;
    }

    private float slopeY(Level.Plat p, float px) {
        float t = FMath.clamp((px - p.x) / p.w, 0, 1);
        return p.slope > 0 ? p.y + p.h * (1 - t) : p.y + p.h * t;
    }
}
