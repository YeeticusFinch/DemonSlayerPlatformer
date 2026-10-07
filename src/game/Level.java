package game;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Level {
    public enum WinMode { GOAL, GOAL_AFTER_KILLS, BOSS }

    public enum Theme { MTN, FOREST, VILLAGE, CITY, MANSION, SHRINE, DOJO }

    public static final int GRASS = 0, ROCK = 1, WOOD = 2, ROOF = 3, STONE = 4;

    public static class Plat {
        public float x, y, w, h;
        public int style;
        public boolean oneway;
        public int slope;

        public Plat(float x, float y, float w, float h, int style) { this(x, y, w, h, style, false); }

        public Plat(float x, float y, float w, float h, int style, boolean oneway) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.style = style;
            this.oneway = oneway;
        }
    }

    public static final int TORII = 0, TREE = 1, PINE = 2, WISTERIA = 3, LANTERN = 4, HUT = 5,
            GRAVE = 6, HOUSE = 7, FENCE = 8, BANNER = 9, CAMPFIRE = 10, SIGN = 11, WINDOW = 12, STATUE = 13, TEMPLE = 14;

    public static class Deco {
        public int type;
        public float x, y, s;

        public Deco(int type, float x, float y, float s) {
            this.type = type;
            this.x = x;
            this.y = y;
            this.s = s;
        }
    }

    public static class Spawn {
        public String type;
        public float x, y;

        public Spawn(String type, float x, float y) {
            this.type = type;
            this.x = x;
            this.y = y;
        }
    }

    public String name, objective;
    public int w = 3000, h = 1500;
    public Theme theme = Theme.MTN;
    public boolean daytime = true;
    public WinMode winMode = WinMode.GOAL;
    public List<Plat> plats = new ArrayList<>();
    public List<Deco> decos = new ArrayList<>();
    public List<Spawn> spawns = new ArrayList<>();
    public String[] npcLines = new String[0];
    public float spawnX, spawnY;
    public float goalX, goalY;
    public float[] boulder;
    public String introTitle, introSub;
    public boolean dojo;
    public boolean underwater;
    public boolean protectCivilians;
    public boolean muzanEncounter;
    public boolean kyogaiRoom;
    public float kyogaiCx, kyogaiCy;

    public static class TrapSpec {
        public String type;
        public float a, b, c, d, e;

        public TrapSpec(String type, float a, float b, float c, float d, float e) {
            this.type = type;
            this.a = a;
            this.b = b;
            this.c = c;
            this.d = d;
            this.e = e;
        }
    }

    public java.util.ArrayList<TrapSpec> trapSpecs = new java.util.ArrayList<>();

    public Level spike(float x, float groundTop, float wd) {
        trapSpecs.add(new TrapSpec("spike", x, groundTop, wd, 0, 0));
        return this;
    }

    public Level crusher(float dropX, float dropY) {
        trapSpecs.add(new TrapSpec("crusher", dropX, dropY, 0, 0, 0));
        return this;
    }

    public Level swingLog(float pivotX, float pivotY, float len, float maxAngDeg, float speedRadPerSec, float phase) {
        trapSpecs.add(new TrapSpec("log", pivotX, pivotY, len, maxAngDeg, speedRadPerSec));
        return this;
    }

    public Level movingPlat(float x0, float y0, float x1, float y1, float wd, float speedRadPerSec) {
        trapSpecs.add(new TrapSpec("mplat", x0, y0, x1, y1, wd));
        return this;
    }

    public Level crumble(float x, float y, float wd) {
        trapSpecs.add(new TrapSpec("crumble", x, y, wd, 0, 0));
        return this;
    }

    public Level ground(float x, float w, float topY, int style) {
        plats.add(new Plat(x, topY, w, 900, style));
        return this;
    }

    public Level plat(float x, float y, float w, float h, int style) {
        plats.add(new Plat(x, y, w, h, style));
        return this;
    }

    public Level slope(float x, float peakY, float w, float drop, int style, int dir) {
        Plat p = new Plat(x, peakY, w, drop, style);
        p.slope = dir < 0 ? -1 : 1;
        plats.add(p);
        return this;
    }

    public Level oneWay(float x, float y, float w) {
        plats.add(new Plat(x, y, w, 16, WOOD, true));
        return this;
    }

    public Level deco(int type, float x, float y, float s) {
        decos.add(new Deco(type, x, y, s));
        return this;
    }

    public Level spawn(String type, float x, float y) {
        spawns.add(new Spawn(type, x, y));
        return this;
    }

    public Level goalAt(float x, float groundTop) {
        goalX = x;
        goalY = groundTop - 150;
        return this;
    }

    public Level spawnPlayer(float x, float groundTop) {
        spawnX = x;
        spawnY = groundTop - 4;
        return this;
    }

    public Level meta(String name, String objective, Theme t, boolean day, WinMode wm, String title, String sub) {
        this.name = name;
        this.objective = objective;
        this.theme = t;
        this.daytime = day;
        this.winMode = wm;
        this.introTitle = title;
        this.introSub = sub;
        return this;
    }

    public Level lines(String... l) {
        npcLines = l;
        return this;
    }

    public Level boulderAt(float x, float groundTop, float w, float h) {
        boulder = new float[]{x, groundTop - h, w, h};
        return this;
    }

    public Level underwater() {
        underwater = true;
        return this;
    }

    public Level protectCivilians() {
        protectCivilians = true;
        return this;
    }

    public Level muzanEncounter() {
        muzanEncounter = true;
        return this;
    }

    public Level kyogaiRoom(float cx, float cy) {
        kyogaiRoom = true;
        kyogaiCx = cx;
        kyogaiCy = cy;
        return this;
    }
}
