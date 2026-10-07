package game;

public class LevelData {
    public static final int TRAINING_COUNT = 10;
    public static final int SLAYER_COUNT = 32;
    public static final int DEMON_COUNT = 6;

    private static Level[] slayer, demon;
    private static Level dojo;

    public static Level[] slayerLevels() {
        if (slayer == null) buildSlayer();
        return slayer;
    }

    public static Level[] demonLevels() {
        if (demon == null) buildDemon();
        return demon;
    }

    public static Level dojoLevel() {
        if (dojo == null) buildDojo();
        return dojo;
    }

    private static void buildDojo() {
        dojo = new Level()
                .meta("Dojo", "Choose an opponent and train under lantern light", Level.Theme.DOJO, false, Level.WinMode.GOAL,
                        "Training Dojo", "No sunlight reaches this floor. Demons are safe here.")
                .spawnPlayer(360, 700)
                .ground(0, 2600, 700, Level.WOOD)
                .plat(0, 180, 2600, 42, Level.WOOD)
                .plat(0, 220, 42, 520, Level.WOOD)
                .plat(2558, 220, 42, 520, Level.WOOD)
                .oneWay(950, 545, 700)
                .deco(Level.LANTERN, 280, 700, 1.15f)
                .deco(Level.LANTERN, 2320, 700, 1.15f)
                .deco(Level.BANNER, 650, 700, 1f)
                .deco(Level.BANNER, 1950, 700, 1f);
        dojo.w = 2600;
        dojo.h = 1500;
        dojo.dojo = true;
    }

    private static void buildSlayer() {
        slayer = new Level[SLAYER_COUNT];
        slayer[0] = new Level()
                .meta("Footsteps of Dawn", "Reach the torii gate", Level.Theme.MTN, true, Level.WinMode.GOAL,
                        "Mt. Sagiri", "Train under Urokodaki. Run and jump with A/D and W.")
                .spawnPlayer(140, 700)
                .ground(0, 900, 700, Level.GRASS)
                .plat(1000, 660, 260, 40, Level.GRASS)
                .plat(1380, 600, 240, 40, Level.GRASS)
                .ground(1720, 1200, 620, Level.GRASS)
                .plat(2380, 520, 200, 36, Level.GRASS)
                .plat(2650, 470, 220, 36, Level.GRASS)
                .ground(2960, 700, 540, Level.GRASS)
                .deco(Level.HUT, 300, 700, 1.2f)
                .deco(Level.SIGN, 620, 700, 1f)
                .deco(Level.TREE, 1150, 700, 1.1f)
                .deco(Level.PINE, 1900, 620, 1.3f)
                .deco(Level.TREE, 2550, 520, 1f)
                .deco(Level.TORII, 3300, 540, 1.1f)
                .goalAt(3390, 540);
        slayer[0].w = 3700;
        slayer[0].h = 1500;

        slayer[1] = new Level()
                .meta("Valley Leaps", "Cross the ravines", Level.Theme.MTN, true, Level.WinMode.GOAL,
                        "Breath of the Mountain", "Press W twice for a double jump.")
                .spawnPlayer(120, 760)
                .ground(0, 500, 760, Level.GRASS)
                .plat(650, 700, 180, 36, Level.GRASS)
                .plat(950, 620, 170, 36, Level.GRASS)
                .plat(1250, 540, 170, 36, Level.GRASS)
                .plat(1560, 480, 200, 36, Level.GRASS)
                .ground(1900, 420, 640, Level.ROCK)
                .plat(2440, 580, 160, 36, Level.GRASS)
                .plat(2720, 500, 160, 36, Level.GRASS)
                .plat(3010, 430, 170, 36, Level.GRASS)
                .ground(3300, 900, 560, Level.GRASS)
                .deco(Level.TREE, 200, 760, 1.2f)
                .deco(Level.PINE, 1980, 640, 1.4f)
                .deco(Level.SIGN, 3400, 560, 1f)
                .deco(Level.TORII, 4000, 560, 1.1f)
                .goalAt(4100, 560);
        slayer[1].w = 4400;
        slayer[1].h = 1600;

        slayer[2] = new Level()
                .meta("The Cliff Face", "Scale the sheer walls", Level.Theme.MTN, true, Level.WinMode.GOAL,
                        "The Cliff Face", "Slide on walls and press W to wall jump!")
                .spawnPlayer(130, 800)
                .ground(0, 700, 800, Level.GRASS)
                .plat(700, 640, 30, 500, Level.ROCK)
                .plat(880, 480, 30, 660, Level.ROCK)
                .plat(700, 320, 210, 30, Level.ROCK)
                .plat(1060, 200, 240, 36, Level.GRASS)
                .plat(1420, 300, 30, 420, Level.ROCK)
                .plat(1240, 140, 200, 30, Level.ROCK)
                .plat(1560, 90, 260, 36, Level.GRASS)
                .plat(1900, 190, 30, 380, Level.ROCK)
                .plat(1960, 240, 420, 36, Level.GRASS)
                .ground(2380, 1100, 380, Level.GRASS)
                .deco(Level.TREE, 2500, 380, 1.3f)
                .deco(Level.PINE, 2900, 380, 1.2f)
                .deco(Level.TORII, 3330, 380, 1.1f)
                .goalAt(3420, 380);
        slayer[2].w = 3700;
        slayer[2].h = 1500;

        slayer[3] = new Level()
                .meta("Broken Steps", "Mind your footing", Level.Theme.MTN, true, Level.WinMode.GOAL,
                        "Broken Steps", "Small ledges. Precision beats speed.")
                .spawnPlayer(120, 720)
                .ground(0, 460, 720, Level.GRASS)
                .plat(560, 680, 110, 30, Level.ROCK)
                .plat(790, 620, 100, 30, Level.ROCK)
                .plat(1010, 560, 100, 30, Level.ROCK)
                .plat(1240, 620, 100, 30, Level.ROCK)
                .plat(1460, 560, 110, 30, Level.ROCK)
                .plat(1690, 500, 100, 30, Level.ROCK)
                .ground(1900, 500, 560, Level.GRASS)
                .plat(2540, 480, 96, 28, Level.ROCK)
                .plat(2750, 410, 96, 28, Level.ROCK)
                .plat(2960, 350, 110, 28, Level.ROCK)
                .ground(3180, 1000, 420, Level.GRASS)
                .deco(Level.TREE, 2050, 560, 1.2f)
                .deco(Level.CAMPFIRE, 3550, 420, 1f)
                .deco(Level.TORII, 4020, 420, 1.1f)
                .goalAt(4110, 420);
        slayer[3].w = 4400;
        slayer[3].h = 1500;

        slayer[4] = new Level()
                .meta("Cedar Crossing", "Branch to branch", Level.Theme.FOREST, true, Level.WinMode.GOAL,
                        "Cedar Crossing", "Thin branches hold you - drop through with S if you need to fall.")
                .spawnPlayer(120, 700)
                .ground(0, 420, 700, Level.GRASS)
                .oneWay(560, 640, 150)
                .oneWay(820, 560, 140)
                .oneWay(1080, 490, 150)
                .oneWay(1360, 560, 130)
                .oneWay(1620, 630, 150)
                .oneWay(1900, 550, 140)
                .ground(2150, 500, 620, Level.GRASS)
                .oneWay(2800, 540, 140)
                .oneWay(3060, 450, 140)
                .oneWay(3330, 380, 150)
                .ground(3600, 900, 480, Level.GRASS)
                .deco(Level.TREE, 150, 700, 1.4f)
                .deco(Level.TREE, 2250, 620, 1.5f)
                .deco(Level.TREE, 3050, 620, 1.4f)
                .deco(Level.CAMPFIRE, 3900, 480, 1f)
                .deco(Level.TORII, 4320, 480, 1.1f)
                .goalAt(4410, 480);
        slayer[4].w = 4700;
        slayer[4].h = 1500;

        slayer[5] = new Level()
                .meta("Stone Chimneys", "Up we go", Level.Theme.MTN, true, Level.WinMode.GOAL,
                        "Stone Chimneys", "Chimney walls: jump between facing walls to climb.")
                .spawnPlayer(130, 860)
                .ground(0, 640, 860, Level.ROCK)
                .plat(640, 700, 30, 700, Level.ROCK)
                .plat(820, 560, 30, 840, Level.ROCK)
                .plat(640, 420, 210, 30, Level.ROCK)
                .plat(1000, 340, 30, 620, Level.ROCK)
                .plat(1180, 220, 30, 740, Level.ROCK)
                .plat(1000, 100, 210, 30, Level.ROCK)
                .plat(1360, 160, 240, 34, Level.GRASS)
                .plat(1720, 260, 30, 500, Level.ROCK)
                .plat(1880, 320, 500, 36, Level.GRASS)
                .ground(2450, 1200, 420, Level.GRASS)
                .spike(2600, 420, 200)
                .swingLog(2900, -20, 400, 80, 1.3f, 0f)
                .deco(Level.PINE, 2700, 420, 1.3f)
                .deco(Level.PINE, 3100, 420, 1.2f)
                .deco(Level.TORII, 3480, 420, 1.1f)
                .goalAt(3570, 420);
        slayer[5].w = 3800;
        slayer[5].h = 1500;

        slayer[6] = new Level()
                .meta("Wind over Sagiri", "Dash across the gaps", Level.Theme.MTN, true, Level.WinMode.GOAL,
                        "Wind over Sagiri", "Press SHIFT to dash. Chain dash + double jump for distance.")
                .spawnPlayer(120, 780)
                .ground(0, 420, 780, Level.GRASS)
                .plat(700, 720, 140, 32, Level.ROCK)
                .plat(1120, 660, 130, 32, Level.ROCK)
                .plat(1540, 610, 130, 32, Level.ROCK)
                .plat(1960, 560, 140, 32, Level.ROCK)
                .ground(2380, 380, 620, Level.GRASS)
                .crusher(2570, 250)
                .spike(2660, 620, 90)
                .movingPlat(2980, 540, 2980, 320, 120, 2.4f)
                .plat(3040, 560, 120, 30, Level.ROCK)
                .plat(3420, 500, 120, 30, Level.ROCK)
                .plat(3810, 440, 130, 30, Level.ROCK)
                .ground(4200, 1000, 480, Level.GRASS)
                .crusher(4700, 110)
                .spike(4980, 480, 120)
                .deco(Level.BANNER, 2500, 620, 1f)
                .deco(Level.TREE, 4500, 480, 1.3f)
                .deco(Level.TORII, 5020, 480, 1.1f)
                .goalAt(5110, 480);
        slayer[6].w = 5400;
        slayer[6].h = 1500;

        slayer[7] = new Level()
                .meta("The Old Shrine", "Rooftop pilgrimage", Level.Theme.FOREST, true, Level.WinMode.GOAL,
                        "The Old Shrine", "Hop across the shrine roofs.")
                .spawnPlayer(120, 720)
                .ground(0, 500, 720, Level.GRASS)
                .plat(640, 640, 220, 46, Level.ROOF)
                .plat(960, 540, 220, 46, Level.ROOF)
                .plat(1280, 450, 200, 46, Level.ROOF)
                .spike(1320, 450, 70)
                .plat(1580, 540, 220, 46, Level.ROOF)
                .plat(1900, 640, 220, 46, Level.ROOF)
                .movingPlat(1500, 430, 1780, 430, 110, 2.6f)
                .crumble(1960, 600, 110)
                .ground(2230, 420, 700, Level.GRASS)
                .plat(2760, 600, 240, 46, Level.ROOF)
                .plat(3100, 500, 240, 46, Level.ROOF)
                .plat(3440, 400, 240, 46, Level.ROOF)
                .plat(3780, 500, 240, 46, Level.ROOF)
                .ground(4130, 900, 560, Level.GRASS)
                .deco(Level.STATUE, 2350, 700, 1.1f)
                .deco(Level.LANTERN, 2900, 554, 1f)
                .deco(Level.LANTERN, 3560, 354, 1f)
                .deco(Level.WISTERIA, 4450, 560, 1.3f)
                .deco(Level.TORII, 4850, 560, 1.1f)
                .goalAt(4940, 560);
        slayer[7].w = 5200;
        slayer[7].h = 1500;

        slayer[8] = new Level()
                .meta("Swordsmith's Trail", "A knife's edge path", Level.Theme.MTN, true, Level.WinMode.GOAL,
                        "Swordsmith's Trail", "Tiny platforms ahead. Patience.")
                .spawnPlayer(120, 740)
                .ground(0, 400, 740, Level.GRASS)
                .plat(540, 700, 80, 26, Level.ROCK)
                .crumble(740, 640, 76)
                .plat(930, 700, 80, 26, Level.ROCK)
                .crumble(1130, 620, 76)
                .crumble(1320, 560, 84)
                .plat(1160, 760, 220, 60, Level.ROCK)
                .spike(1180, 760, 180)
                .crumble(1530, 500, 76)
                .plat(1730, 560, 84, 26, Level.ROCK)
                .ground(1940, 360, 640, Level.GRASS)
                .plat(2420, 580, 78, 26, Level.ROCK)
                .crumble(2620, 500, 74)
                .plat(2820, 430, 82, 26, Level.ROCK)
                .crumble(3030, 370, 74)
                .swingLog(2700, -60, 420, 80, 1.45f, 1.4f)
                .ground(3220, 1100, 440, Level.GRASS)
                .deco(Level.HUT, 3500, 440, 1f)
                .deco(Level.SIGN, 3300, 440, 1f)
                .deco(Level.TORII, 4150, 440, 1.1f)
                .goalAt(4240, 440);
        slayer[8].w = 4500;
        slayer[8].h = 1500;

        slayer[9] = new Level()
                .meta("Urokodaki's Summit", "Final trial of the mountain", Level.Theme.MTN, true, Level.WinMode.GOAL,
                        "Urokodaki's Summit", "Everything you have learned. One climb.")
                .lines("Urokodaki: The summit tests all who seek my blade.", "Fail, and repeat it a thousand times.")
                .spawnPlayer(120, 820)
                .ground(0, 380, 820, Level.GRASS)
                .plat(520, 760, 110, 28, Level.ROCK)
                .plat(760, 680, 100, 28, Level.ROCK)
                .plat(640, 560, 30, 400, Level.ROCK)
                .plat(820, 460, 30, 500, Level.ROCK)
                .plat(640, 340, 210, 28, Level.ROCK)
                .oneWay(960, 300, 130)
                .oneWay(1200, 240, 130)
                .plat(1440, 300, 30, 460, Level.ROCK)
                .plat(1520, 340, 420, 34, Level.GRASS)
                .plat(2060, 240, 30, 460, Level.ROCK)
                .plat(2120, 280, 300, 34, Level.GRASS)
                .oneWay(2560, 200, 130)
                .crumble(2790, 140, 130)
                .ground(3020, 1300, 220, Level.GRASS)
                .crusher(3400, -180)
                .spike(3560, 220, 170)
                .movingPlat(3060, 160, 3060, 420, 120, 2.8f)
                .swingLog(3950, -140, 320, 80, 1.5f, 2.2f)
                .deco(Level.HUT, 3250, 220, 1.2f)
                .deco(Level.CAMPFIRE, 3550, 220, 1f)
                .deco(Level.STATUE, 3850, 220, 1.2f)
                .deco(Level.TORII, 4120, 220, 1.2f)
                .goalAt(4220, 220);
        slayer[9].w = 4500;
        slayer[9].h = 1500;

        slayer[10] = new Level()
                .meta("Sabito's Boulder", "Defeat Sabito and split the boulder", Level.Theme.FOREST, true, Level.WinMode.BOSS,
                        "Half a Year Later", "Sabito waits beyond the boulder. Cut what cannot be cut.")
                .lines("Makomo: Sabito is strong... but he is kind.", "Win, and the boulder will fall.")
                .spawnPlayer(140, 700)
                .ground(0, 4400, 700, Level.GRASS)
                .boulderAt(1250, 700, 150, 190)
                .plat(2300, 560, 240, 36, Level.GRASS)
                .plat(2900, 480, 220, 36, Level.GRASS)
                .deco(Level.TREE, 400, 700, 1.4f)
                .deco(Level.TREE, 1800, 700, 1.3f)
                .deco(Level.WISTERIA, 2600, 560, 1.3f)
                .deco(Level.GRAVE, 3300, 700, 1f)
                .deco(Level.GRAVE, 3420, 700, 1f)
                .deco(Level.TORII, 4150, 700, 1.1f)
                .spawn("sabito", 2900, 480)
                .goalAt(4240, 700);
        slayer[10].w = 4600;
        slayer[10].h = 1500;

        String fsTitle = "Final Selection";
        slayer[11] = new Level()
                .meta("Wisteria Gate", "Survive the descent - reach the shrine", Level.Theme.MTN, false, Level.WinMode.GOAL,
                        fsTitle, "Seven nights on Mt. Fujikasane. Demons roam. Go.")
                .lines("Gatekeeper: Survive all seven nights...", "...and you will be a Demon Slayer.")
                .spawnPlayer(140, 760)
                .ground(0, 700, 760, Level.GRASS)
                .plat(850, 680, 220, 36, Level.GRASS)
                .ground(1220, 800, 700, Level.GRASS)
                .plat(2170, 640, 200, 36, Level.GRASS)
                .ground(2520, 900, 560, Level.GRASS)
                .plat(3570, 480, 200, 36, Level.GRASS)
                .ground(3920, 900, 420, Level.GRASS)
                .deco(Level.WISTERIA, 300, 760, 1.5f)
                .deco(Level.WISTERIA, 500, 760, 1.3f)
                .deco(Level.TORII, 120, 760, 1.4f)
                .deco(Level.PINE, 1500, 700, 1.3f)
                .deco(Level.LANTERN, 2700, 560, 1f)
                .deco(Level.TORII, 4620, 420, 1.1f)
                .spawn("demon", 1500, 700)
                .spawn("demon", 2900, 560)
                .goalAt(4720, 420);
        slayer[11].w = 5000;
        slayer[11].h = 1500;

        slayer[12] = new Level()
                .meta("Forest of Demons", "Slay every demon, then move on", Level.Theme.FOREST, false, Level.WinMode.GOAL_AFTER_KILLS,
                        fsTitle + " II", "Clear the grove before proceeding.")
                .spawnPlayer(140, 720)
                .ground(0, 4600, 720, Level.GRASS)
                .plat(1200, 560, 220, 36, Level.GRASS)
                .plat(2400, 500, 220, 36, Level.GRASS)
                .plat(3500, 560, 220, 36, Level.GRASS)
                .deco(Level.TREE, 500, 720, 1.4f)
                .deco(Level.TREE, 1700, 720, 1.3f)
                .deco(Level.TREE, 2800, 720, 1.5f)
                .deco(Level.TREE, 3900, 720, 1.3f)
                .deco(Level.CAMPFIRE, 2100, 720, 1f)
                .deco(Level.WISTERIA, 2500, 720, 1.5f)
                .spawn("demon", 1300, 720)
                .spawn("demon", 2000, 720)
                .spawn("demon", 2700, 720)
                .spawn("demon", 3400, 720)
                .spawn("demon", 4100, 720)
                .goalAt(4400, 720);
        slayer[12].w = 4800;
        slayer[12].h = 1500;

        slayer[13] = new Level()
                .meta("Moonlit Cliffs", "Climb through the hunting grounds", Level.Theme.MTN, false, Level.WinMode.GOAL,
                        fsTitle + " III", "Demons stalk the cliffs at night.")
                .spawnPlayer(130, 820)
                .ground(0, 640, 820, Level.GRASS)
                .plat(640, 740, 30, 600, Level.ROCK)
                .plat(830, 600, 30, 740, Level.ROCK)
                .plat(640, 470, 220, 30, Level.ROCK)
                .plat(1010, 380, 260, 34, Level.GRASS)
                .plat(1390, 300, 30, 500, Level.ROCK)
                .plat(1470, 340, 380, 34, Level.GRASS)
                .plat(1970, 220, 30, 480, Level.ROCK)
                .plat(2030, 260, 420, 34, Level.GRASS)
                .ground(2560, 1300, 380, Level.GRASS)
                .deco(Level.LANTERN, 2700, 380, 1f)
                .deco(Level.WISTERIA, 2950, 380, 1.4f)
                .deco(Level.LANTERN, 3300, 380, 1f)
                .deco(Level.TORII, 3660, 380, 1.1f)
                .spawn("demon", 1550, 340)
                .spawn("demon", 2250, 260)
                .spawn("demon", 2900, 380)
                .goalAt(3760, 380);
        slayer[13].w = 4000;
        slayer[13].h = 1500;

        slayer[14] = new Level()
                .meta("Ruined Path", "Purge the ruins", Level.Theme.FOREST, false, Level.WinMode.GOAL_AFTER_KILLS,
                        fsTitle + " IV", "Nothing survives here by accident.")
                .spawnPlayer(140, 700)
                .ground(0, 4800, 700, Level.ROCK)
                .plat(900, 580, 180, 34, Level.STONE)
                .plat(1500, 500, 180, 34, Level.STONE)
                .plat(2200, 560, 180, 34, Level.STONE)
                .plat(3000, 480, 180, 34, Level.STONE)
                .deco(Level.GRAVE, 600, 700, 1f)
                .deco(Level.GRAVE, 700, 700, 1f)
                .deco(Level.STATUE, 1900, 700, 1.2f)
                .deco(Level.GRAVE, 3300, 700, 1f)
                .deco(Level.LANTERN, 2600, 700, 1f)
                .spawn("demon", 1100, 700)
                .spawn("artdemon", 1800, 700)
                .spawn("demon", 2500, 700)
                .spawn("demon", 3200, 700)
                .spawn("demon", 3900, 700)
                .spawn("hunter", 4400, 700)
                .goalAt(4600, 700);
        slayer[14].w = 5000;
        slayer[14].h = 1500;

        slayer[15] = new Level()
                .meta("Ascent", "The steepest night", Level.Theme.MTN, false, Level.WinMode.GOAL,
                        fsTitle + " V", "Wall, dash, pray, repeat.")
                .spawnPlayer(130, 860)
                .ground(0, 560, 860, Level.ROCK)
                .plat(560, 780, 30, 640, Level.ROCK)
                .plat(740, 640, 30, 780, Level.ROCK)
                .plat(560, 500, 210, 30, Level.ROCK)
                .plat(920, 420, 30, 620, Level.ROCK)
                .plat(1080, 460, 340, 32, Level.GRASS)
                .plat(1540, 340, 30, 560, Level.ROCK)
                .plat(1620, 380, 300, 32, Level.GRASS)
                .plat(2040, 260, 30, 520, Level.ROCK)
                .oneWay(2100, 300, 140)
                .plat(2360, 200, 130, 28, Level.ROCK)
                .ground(2600, 1400, 300, Level.GRASS)
                .deco(Level.WISTERIA, 3350, 300, 1.5f)
                .deco(Level.CAMPFIRE, 2800, 300, 1f)
                .deco(Level.TORII, 3800, 300, 1.1f)
                .spawn("demon", 1200, 460)
                .spawn("demon", 1750, 380)
                .spawn("demon", 3000, 300)
                .goalAt(3900, 300);
        slayer[15].w = 4200;
        slayer[15].h = 1500;

        slayer[16] = new Level()
                .meta("Grove of Graves", "The last hunt before the last night", Level.Theme.FOREST, false, Level.WinMode.GOAL_AFTER_KILLS,
                        fsTitle + " VI", "They smell your blade. End them.")
                .spawnPlayer(140, 720)
                .ground(0, 5000, 720, Level.GRASS)
                .oneWay(900, 560, 150)
                .oneWay(1500, 480, 150)
                .oneWay(2200, 560, 150)
                .plat(2900, 460, 200, 34, Level.GRASS)
                .deco(Level.GRAVE, 400, 720, 1f)
                .deco(Level.GRAVE, 480, 720, 1f)
                .deco(Level.GRAVE, 560, 720, 1f)
                .deco(Level.TREE, 1200, 720, 1.4f)
                .deco(Level.WISTERIA, 2500, 720, 1.5f)
                .deco(Level.GRAVE, 3400, 720, 1f)
                .deco(Level.LANTERN, 3100, 720, 1f)
                .spawn("demon", 1000, 720)
                .spawn("demon", 1600, 720)
                .spawn("demon", 2200, 720)
                .spawn("demon", 2800, 720)
                .spawn("demon", 3500, 720)
                .spawn("artdemon", 4100, 720)
                .goalAt(4800, 720);
        slayer[16].w = 5200;
        slayer[16].h = 1500;

        slayer[17] = new Level()
                .meta("The Hand Demon", "It remembers you. End this night.", Level.Theme.SHRINE, false, Level.WinMode.BOSS,
                        fsTitle + " - Final Night", "The demon with too many hands has waited years for revenge.")
                .lines("Hand Demon: I remember you... Urokodaki's brats all die here!", "Face it. Cut it down.")
                .spawnPlayer(160, 700)
                .ground(0, 2600, 700, Level.STONE)
                .plat(300, 480, 26, 220, Level.STONE)
                .plat(2280, 480, 26, 220, Level.STONE)
                .deco(Level.LANTERN, 400, 700, 1.1f)
                .deco(Level.LANTERN, 2200, 700, 1.1f)
                .deco(Level.STATUE, 1300, 700, 1.4f)
                .deco(Level.TORII, 2450, 700, 1.2f)
                .spawn("hand", 1900, 700)
                .goalAt(2520, 700);
        slayer[17].w = 2700;
        slayer[17].h = 1500;

        String bogTitle = "Kidnapper's Bog";
        slayer[18] = new Level()
                .meta("Swamp Road", "Cross the daytime swamp", Level.Theme.FOREST, true, Level.WinMode.GOAL,
                        bogTitle, "A demon hides where the sun cannot reach.")
                .lines("Villager: Children vanish near the reeds...", "A temple stands ahead. Something waits inside.")
                .spawnPlayer(140, 720)
                .ground(0, 5600, 720, Level.GRASS)
                .plat(780, 650, 220, 22, Level.WOOD)
                .plat(1260, 610, 200, 22, Level.WOOD)
                .plat(1750, 650, 230, 22, Level.WOOD)
                .slope(3045, 473, 215, 113, Level.ROOF, 1)
                .slope(3260, 473, 215, 113, Level.ROOF, -1)
                .deco(Level.TREE, 360, 720, 1.35f)
                .deco(Level.TREE, 1060, 720, 1.2f)
                .deco(Level.TEMPLE, 3260, 720, 1.05f)
                .deco(Level.TREE, 4300, 720, 1.4f)
                .deco(Level.TORII, 5280, 720, 1.1f)
                .spawn("temple", 3260, 610)
                .goalAt(5380, 720);
        slayer[18].w = 5700;
        slayer[18].h = 1500;

        slayer[19] = new Level()
                .meta("Swamp Village", "Enter the silent village", Level.Theme.VILLAGE, true, Level.WinMode.GOAL,
                        bogTitle + " II", "No demons. Only empty streets and frightened homes.")
                .lines("No one answers the doors.", "The bog goes quiet before nightfall.")
                .spawnPlayer(140, 720)
                .ground(0, 4300, 720, Level.WOOD)
                .plat(840, 600, 260, 34, Level.ROOF)
                .plat(1660, 560, 260, 34, Level.ROOF)
                .plat(2520, 610, 260, 34, Level.ROOF)
                .deco(Level.HOUSE, 560, 720, 1.2f)
                .deco(Level.HOUSE, 1380, 720, 1.15f)
                .deco(Level.HOUSE, 2240, 720, 1.25f)
                .deco(Level.LANTERN, 3060, 720, 1f)
                .deco(Level.HOUSE, 3440, 720, 1.1f)
                .goalAt(4050, 720);
        slayer[19].w = 4400;
        slayer[19].h = 1500;

        slayer[20] = new Level()
                .meta("Night in the Bog", "Defeat the swamp demon", Level.Theme.VILLAGE, false, Level.WinMode.BOSS,
                        bogTitle + " III", "The first body rises from the mud.")
                .lines("Swamp Demon: The bog keeps what it takes.", "Do not let it sink away.")
                .spawnPlayer(140, 720)
                .ground(0, 3600, 720, Level.WOOD)
                .plat(760, 610, 260, 34, Level.ROOF)
                .plat(2260, 580, 260, 34, Level.ROOF)
                .deco(Level.HOUSE, 520, 720, 1.15f)
                .deco(Level.LANTERN, 1200, 720, 1f)
                .deco(Level.HOUSE, 1940, 720, 1.2f)
                .deco(Level.LANTERN, 2840, 720, 1f)
                .spawn("swamp", 2300, 720)
                .goalAt(3360, 720);
        slayer[20].w = 3700;
        slayer[20].h = 1500;

        slayer[21] = new Level()
                .meta("Swamp Domain", "Swim through the underwater dimension", Level.Theme.FOREST, false, Level.WinMode.GOAL_AFTER_KILLS,
                        bogTitle + " IV", "The bog opens below the world. Swim with W/S; recharge fails underwater.")
                .lines("The water is black, but the demons move easily.", "Cut through the domain and surface alive.")
                .underwater()
                .spawnPlayer(140, 560)
                .ground(0, 5200, 940, Level.ROCK)
                .plat(700, 760, 260, 30, Level.ROCK)
                .plat(1320, 620, 220, 30, Level.ROCK)
                .plat(1980, 720, 260, 30, Level.ROCK)
                .plat(2700, 560, 250, 30, Level.ROCK)
                .plat(3500, 700, 280, 30, Level.ROCK)
                .spawn("swamp", 860, 760)
                .spawn("swamp", 1520, 620)
                .spawn("swamp", 2240, 720)
                .spawn("swamp", 3040, 560)
                .spawn("swamp", 3820, 700)
                .goalAt(5000, 940);
        slayer[21].w = 5400;
        slayer[21].h = 1600;

        slayer[22] = new Level()
                .meta("Three in the Village", "Defeat the three stronger swamp demons", Level.Theme.VILLAGE, false, Level.WinMode.GOAL_AFTER_KILLS,
                        bogTitle + " V", "The split demons reunite above the mud.")
                .lines("Three voices: Sink. Drown. Disappear.", "End the kidnappings tonight.")
                .spawnPlayer(140, 720)
                .ground(0, 4700, 720, Level.WOOD)
                .plat(760, 600, 260, 34, Level.ROOF)
                .plat(1560, 540, 260, 34, Level.ROOF)
                .plat(2460, 600, 260, 34, Level.ROOF)
                .plat(3320, 560, 260, 34, Level.ROOF)
                .deco(Level.HOUSE, 520, 720, 1.15f)
                .deco(Level.HOUSE, 1420, 720, 1.2f)
                .deco(Level.HOUSE, 2320, 720, 1.15f)
                .deco(Level.LANTERN, 3060, 720, 1f)
                .deco(Level.HOUSE, 3820, 720, 1.25f)
                .spawn("swamp_strong", 1200, 720)
                .spawn("swamp_strong", 2400, 720)
                .spawn("swamp_strong", 3600, 720)
                .goalAt(4480, 720);
        slayer[22].w = 4800;
        slayer[22].h = 1500;

        String asakusa = "Asakusa";
        slayer[23] = new Level()
                .meta("Asakusa Streets", "Walk through the night city", Level.Theme.CITY, false, Level.WinMode.GOAL,
                        asakusa, "Electric lights. Motorcars. Too many people to protect.")
                .protectCivilians()
                .lines("Crow: Asakusa. Muzan's scent is close.", "Do not draw your blade near civilians.")
                .spawnPlayer(140, 720)
                .ground(0, 5200, 720, Level.STONE)
                .plat(940, 610, 260, 34, Level.ROOF)
                .plat(2160, 580, 260, 34, Level.ROOF)
                .plat(3420, 610, 260, 34, Level.ROOF)
                .deco(Level.HOUSE, 620, 720, 1.25f)
                .deco(Level.HOUSE, 1540, 720, 1.25f)
                .deco(Level.LANTERN, 2380, 720, 1.1f)
                .deco(Level.HOUSE, 2920, 720, 1.3f)
                .deco(Level.HOUSE, 4180, 720, 1.2f)
                .spawn("civilian", 820, 720)
                .spawn("civilian", 1780, 720)
                .spawn("civilian", 3100, 720)
                .spawn("civilian", 3900, 720)
                .goalAt(5000, 720);
        slayer[23].w = 5400;
        slayer[23].h = 1500;

        slayer[24] = new Level()
                .meta("Man in the White Hat", "Protect civilians and slay the newly made demon", Level.Theme.CITY, false, Level.WinMode.GOAL_AFTER_KILLS,
                        asakusa + " II", "Muzan walks among the crowd.")
                .protectCivilians()
                .muzanEncounter()
                .lines("There. The scent from that night.", "Approach carefully. Civilians are everywhere.")
                .spawnPlayer(140, 720)
                .ground(0, 5200, 720, Level.STONE)
                .plat(850, 610, 260, 34, Level.ROOF)
                .plat(2400, 590, 260, 34, Level.ROOF)
                .deco(Level.HOUSE, 540, 720, 1.25f)
                .deco(Level.LANTERN, 1320, 720, 1.1f)
                .deco(Level.HOUSE, 2080, 720, 1.35f)
                .deco(Level.HOUSE, 3600, 720, 1.25f)
                .spawn("civilian", 1560, 720)
                .spawn("civilian", 2860, 720)
                .spawn("civilian", 4100, 720)
                .spawn("muzan", 2700, 720)
                .goalAt(5000, 720);
        slayer[24].w = 5400;
        slayer[24].h = 1500;

        slayer[25] = new Level()
                .meta("Tamayo's House", "Meet Tamayo and Yushiro", Level.Theme.MANSION, false, Level.WinMode.GOAL,
                        asakusa + " III", "Hidden behind the city lights, two demons offer help.")
                .lines("Tamayo: I want to defeat Muzan as well.", "Yushiro: Mind your manners in Lady Tamayo's home.")
                .spawnPlayer(160, 700)
                .ground(0, 2600, 700, Level.WOOD)
                .plat(0, 160, 2600, 42, Level.WOOD)
                .plat(0, 220, 42, 520, Level.WOOD)
                .plat(2558, 220, 42, 520, Level.WOOD)
                .deco(Level.WINDOW, 650, 700, 1.1f)
                .deco(Level.LANTERN, 1180, 700, 1.2f)
                .deco(Level.WINDOW, 1780, 700, 1.1f)
                .spawn("tamayo", 1240, 700)
                .spawn("yushiro", 1440, 700)
                .goalAt(2380, 700);
        slayer[25].w = 2700;
        slayer[25].h = 1500;

        slayer[26] = new Level()
                .meta("Temari and Arrows", "Defeat Susumaru and Yahaba", Level.Theme.MANSION, false, Level.WinMode.GOAL_AFTER_KILLS,
                        asakusa + " IV", "Tamayo's front garden erupts with temari and invisible arrows.")
                .lines("Susumaru: Let's play temari!", "Yahaba: The arrows have already chosen your path.")
                .spawnPlayer(140, 720)
                .ground(0, 4800, 720, Level.GRASS)
                .plat(820, 610, 260, 34, Level.WOOD)
                .plat(1680, 540, 240, 34, Level.WOOD)
                .plat(2780, 610, 260, 34, Level.WOOD)
                .plat(3680, 570, 260, 34, Level.WOOD)
                .deco(Level.HOUSE, 620, 720, 1.45f)
                .deco(Level.FENCE, 1180, 720, 1.7f)
                .deco(Level.FENCE, 1860, 720, 1.7f)
                .deco(Level.FENCE, 2540, 720, 1.7f)
                .deco(Level.FENCE, 3220, 720, 1.7f)
                .deco(Level.TREE, 1460, 720, 1.2f)
                .deco(Level.PINE, 2360, 720, 1.15f)
                .deco(Level.TREE, 3980, 720, 1.25f)
                .deco(Level.LANTERN, 840, 720, 1.05f)
                .deco(Level.LANTERN, 3460, 720, 1.05f)
                .spawn("susumaru", 1820, 720)
                .spawn("yahaba", 3080, 720)
                .goalAt(4560, 720);
        slayer[26].w = 5000;
        slayer[26].h = 1500;

        String tsuzumi = "Tsuzumi Mansion";
        slayer[27] = new Level()
                .meta("Meadow Before the Mansion", "Enter the abandoned mansion", Level.Theme.FOREST, true, Level.WinMode.GOAL,
                        tsuzumi + " I", "Daylight over the meadow. An old house waits beyond the trees.")
                .lines("Crow: Tsuzumi Mansion lies ahead.", "The torii marks the entrance. Once inside, trust nothing.")
                .spawnPlayer(140, 720)
                .ground(0, 4300, 720, Level.GRASS)
                .oneWay(720, 620, 180)
                .oneWay(1180, 560, 170)
                .oneWay(1660, 620, 190)
                .deco(Level.TREE, 260, 720, 1.45f)
                .deco(Level.PINE, 760, 720, 1.35f)
                .deco(Level.TREE, 1420, 720, 1.35f)
                .deco(Level.PINE, 2140, 720, 1.3f)
                .deco(Level.HOUSE, 3500, 720, 2.1f)
                .deco(Level.TORII, 3920, 720, 1.25f)
                .goalAt(4020, 720);
        slayer[27].w = 4500;
        slayer[27].h = 1500;

        slayer[28] = kyogaiMansion0(tsuzumi);

        slayer[29] = kyogaiMansion1(tsuzumi);

        slayer[30] = kyogaiMansion2(tsuzumi);

        slayer[31] = new Level()
                .meta("Drum House", "Defeat Kyogai", Level.Theme.MANSION, false, Level.WinMode.BOSS,
                        tsuzumi + " V", "The room obeys the drums. Jump when the house turns.")
                .lines("Kyogai: This room is mine.", "When his hand rises, leave the floor.")
                .kyogaiRoom(1600, 700)
                .spawnPlayer(1230, 1230)
                .plat(1050, 130, 1100, 40, Level.WOOD)
                .plat(1050, 1230, 1100, 40, Level.WOOD)
                .plat(1030, 150, 40, 1100, Level.WOOD)
                .plat(2130, 150, 40, 1100, Level.WOOD)
                .deco(Level.WINDOW, 1240, 1230, 1.05f)
                .deco(Level.LANTERN, 1600, 1230, 1.2f)
                .deco(Level.WINDOW, 1960, 1230, 1.05f)
                .spawn("kyogai", 1990, 1230);
        slayer[31].w = 3200;
        slayer[31].h = 1500;
    }

    private static Level kyogaiMansion0(String tsuzumi) {
        String[] rows = {
                "  |_____________________________________|",
                "  |                                     |",
                "  |                                     |            |_____|",
                "  |    ____________________________|    |            |     |",
                "  |                                |    |            |     |",
                "  |                                |     ____________      |",
                "   ________________|    |_____|    |                       |",
                "                   |    |     |    |                       |",
                "                   |    |     |    |    |__________________",
                "                   |    |     |    |    |",
                "                   |    |     |    |    |",
                "                   |    |     |    |    |     |_______________|",
                "                   |    |     |    |    |     |               |",
                "                   |    |     |    |    |     |               |",
                "|__________________     |     |    |     _____                |",
                "|                       |     |    |                          |",
                "|s                      |     |    |                        x |",
                " __________________|    |     |    |    |_______|    |________",
                "                   |    |     |    |    |       |    |",
                "                   |    |     |    |    |       |    |",
                "                   |    |     |    |    |       |    |",
                "                   |     _____     |    |       |    |",
                "                   |               |    |       |    |"
        };
        int cw = 54, rh = 118, ox = 120, oy = 80;
        Level l = new Level().meta("Mansion Halls", "Climb through layered corridors", Level.Theme.MANSION, false, Level.WinMode.GOAL,
                tsuzumi + " II", "Hallways stack over hallways. The house turns upward.")
                .lines("The hallway folds back on itself.", "Find the opening upward, then cross to the far corridor.");
        int max = 0;
        for (String row : rows) max = Math.max(max, row.length());
        for (int r = 0; r < rows.length; r++) {
            String row = rows[r];
            int c = 0;
            while (c < row.length()) {
                char ch = row.charAt(c);
                if (ch == 's') l.spawnPlayer(ox + c * cw + 20, oy + (r + 1) * rh);
                if (ch == 'x') l.goalAt(ox + c * cw + 20, oy + (r + 1) * rh);
                if (ch != '_') { c++; continue; }
                int start = c;
                while (c < row.length() && row.charAt(c) == '_') c++;
                l.plat(ox + start * cw, oy + r * rh, (c - start) * cw, 28, Level.WOOD);
            }
        }
        for (int c = 0; c < max; c++) {
            int r = 0;
            while (r < rows.length) {
                char ch = c < rows[r].length() ? rows[r].charAt(c) : ' ';
                if (ch != '|') { r++; continue; }
                int start = r;
                while (r < rows.length && c < rows[r].length() && rows[r].charAt(c) == '|') r++;
                l.plat(ox + c * cw, oy + start * rh, 32, Math.max(r - start, 1) * rh, Level.WOOD);
            }
        }
        l.deco(Level.LANTERN, ox + 12 * cw, oy + 18 * rh, 1f)
                .deco(Level.WINDOW, ox + 35 * cw, oy + 7 * rh, 1f)
                .deco(Level.LANTERN, ox + 54 * cw, oy + 17 * rh, 1f);
        l.w = ox + max * cw + 260;
        l.h = oy + rows.length * rh + 320;
        return l;
    }

    private static Level kyogaiMansion1(String tsuzumi) {
        String[] rows = {
                "     |    |  |    |  |    |        |",
                "     |     __     |  |    |        |",
                "     |            |  |    |        |",
                "     |            |  |    |        |",
                "     |    |__|    |  |    |        |",
                "     |    |  |    |  |     ________",
                "     |    |  |    |  |    ",
                "     |    |  |    |  |    ",
                "     |    |  |    |  |    |________|",
                "     |    |  |    |  |    |        |",
                "     |    |  |    |  |    |        |",
                "|____     |  |    |  |    |        |",
                "|         |  |    |  |    |        |",
                "|s        |  |    |  |    |        |",
                " ____|    |  |    |  |    |        |",
                "     |    |  |    |  |    |        |",
                "     |    |  |     __     |        |                        x",
                "     |    |  |            |        |          |______________|",
                "     |    |  |            |        |          |              |",
                "     |    |  |    |__|    |        |          |              |",
                "     |    |  |    |  |    |        |          |              |",
                "     |    |  |    |  |    |        |          |              |",
                "     |    |  |    |  |    |        |          |              |",
                "     |    |  |    |  |    |        |          |              |",
                "     |    |  |    |  |    |        |          |              |",
                "     |    |  |    |  |    |        |          |              |",
                "     |    |  |    |  |    |        |          |              |",
                "     |    |  |    |  |    |        |          |              |"
        };
        Level l = mansionAsciiLevel(rows, "Rotten Corridors", "Wall-jump up the vertical halls",
                tsuzumi + " III", "Some hallways stand on their side like shafts.");
        l.lines("The second hall rises higher than the first.", "Use the vertical shafts to climb and cross.")
                .deco(Level.LANTERN, 720, 1760, 1f)
                .deco(Level.WINDOW, 1780, 860, 1f)
                .deco(Level.LANTERN, 3300, 2120, 1f);
        return l;
    }

    private static Level kyogaiMansion2(String tsuzumi) {
        String[] rows = {
                " |_______________________________________________________|",
                " |                                                       |",
                " |                                                       |",
                " |                                     d                 |",
                " |      _______________________________________|         |",
                " |                                             |         |",
                " |                    d                        |         |",
                "  _______________________________________|     |         |",
                "|________________________________________      |         |",
                "|                                              |         |",
                "|s                              d              |         |",
                "|______________________________________________          |",
                "|                                                        |",
                "|x              d                                        |",
                " ________________________________________________________"
        };
        Level l = mansionAsciiLevel(rows, "Tongues in the Walls", "Survive the crawling demons",
                tsuzumi + " IV", "Low shapes crawl through the stacked corridors.", Level.WinMode.GOAL_AFTER_KILLS, true);
        l.lines("Tongues scrape the floorboards.", "Cut down the crawling demons before leaving.")
                .deco(Level.WINDOW, 620, 920, 1f)
                .deco(Level.LANTERN, 1920, 1380, 1f)
                .deco(Level.WINDOW, 3120, 560, 1f);
        return l;
    }

    private static Level mansionAsciiLevel(String[] rows, String name, String objective, String title, String sub) {
        return mansionAsciiLevel(rows, name, objective, title, sub, Level.WinMode.GOAL, false);
    }

    private static Level mansionAsciiLevel(String[] rows, String name, String objective, String title, String sub, Level.WinMode winMode, boolean demonMarkers) {
        int cw = 54, rh = 118, ox = 120, oy = 80;
        Level l = new Level().meta(name, objective, Level.Theme.MANSION, false, winMode, title, sub);
        int max = 0;
        for (String row : rows) max = Math.max(max, row.length());
        for (int r = 0; r < rows.length; r++) {
            String row = rows[r];
            int c = 0;
            while (c < row.length()) {
                char ch = row.charAt(c);
                if (ch == 's') l.spawnPlayer(ox + c * cw + 20, oy + (r + 1) * rh);
                if (ch == 'x') l.goalAt(ox + c * cw + 20, oy + (r + 1) * rh);
                if (demonMarkers && ch == 'd') l.spawn("crawler", ox + c * cw + 20, oy + (r + 1) * rh);
                if (ch != '_') { c++; continue; }
                int start = c;
                while (c < row.length() && row.charAt(c) == '_') c++;
                l.plat(ox + start * cw, oy + r * rh, (c - start) * cw, 28, Level.WOOD);
            }
        }
        for (int c = 0; c < max; c++) {
            int r = 0;
            while (r < rows.length) {
                char ch = c < rows[r].length() ? rows[r].charAt(c) : ' ';
                if (ch != '|') { r++; continue; }
                int start = r;
                while (r < rows.length && c < rows[r].length() && rows[r].charAt(c) == '|') r++;
                l.plat(ox + c * cw, oy + start * rh, 32, Math.max(r - start, 1) * rh, Level.WOOD);
            }
        }
        l.w = ox + max * cw + 260;
        l.h = oy + rows.length * rh + 320;
        return l;
    }

    private static void buildDemon() {
        demon = new Level[DEMON_COUNT];
        demon[0] = new Level()
                .meta("First Hunger", "Feed. Leave no witnesses.", Level.Theme.VILLAGE, false, Level.WinMode.GOAL_AFTER_KILLS,
                        "Nightfall Rebirth", "You woke as something else. Hunt with J. Unleash Blood Arts with F (cycle Q/E).")
                .lines("Voice: Flesh mends. Hunger remains.", "Go. The village sleeps.")
                .spawnPlayer(140, 720)
                .ground(0, 4200, 720, Level.WOOD)
                .plat(1000, 580, 200, 40, Level.ROOF)
                .plat(1900, 520, 200, 40, Level.ROOF)
                .plat(2900, 580, 200, 40, Level.ROOF)
                .deco(Level.HOUSE, 500, 720, 1.2f)
                .deco(Level.HOUSE, 1500, 720, 1.1f)
                .deco(Level.WISTERIA, 2050, 720, 1.5f)
                .deco(Level.LANTERN, 800, 720, 1f)
                .deco(Level.LANTERN, 2200, 720, 1f)
                .deco(Level.HOUSE, 3200, 720, 1.2f)
                .spawn("human", 1200, 720)
                .spawn("human", 1800, 720)
                .spawn("human", 2500, 720)
                .spawn("human", 3100, 720)
                .goalAt(3950, 720);
        demon[0].w = 4300;
        demon[0].h = 1500;

        demon[1] = new Level()
                .meta("Sleepless Streets", "The town watch is ready. Good.", Level.Theme.VILLAGE, false, Level.WinMode.GOAL_AFTER_KILLS,
                        "Blood in the Lanes", "Villagers flee when wounded. Hunters throw knives.")
                .spawnPlayer(140, 700)
                .ground(0, 4600, 700, Level.WOOD)
                .plat(900, 560, 200, 42, Level.ROOF)
                .plat(1700, 500, 220, 42, Level.ROOF)
                .plat(2600, 560, 200, 42, Level.ROOF)
                .deco(Level.HOUSE, 600, 700, 1.3f)
                .deco(Level.HOUSE, 1400, 700, 1.2f)
                .deco(Level.LANTERN, 1100, 700, 1f)
                .deco(Level.BANNER, 2000, 700, 1f)
                .deco(Level.WISTERIA, 2450, 700, 1.4f)
                .deco(Level.HOUSE, 3000, 700, 1.3f)
                .deco(Level.LANTERN, 3500, 700, 1f)
                .spawn("human", 900, 700)
                .spawn("human", 1500, 700)
                .spawn("hunter", 2100, 700)
                .spawn("human", 2800, 700)
                .spawn("hunter", 3500, 700)
                .goalAt(4350, 700);
        demon[1].w = 4700;
        demon[1].h = 1500;

        demon[2] = new Level()
                .meta("The Sunlit Road", "Cross by daylight - stay under cover or burn", Level.Theme.FOREST, true, Level.WinMode.GOAL_AFTER_KILLS,
                        "First Sunlight", "The open sky is death now. Hop between awnings and ledges; cross gaps fast.")
                .lines("Voice: The sun watches. Use the shade.")
                .spawnPlayer(140, 720)
                .ground(0, 4600, 720, Level.GRASS)
                .plat(600, 540, 420, 42, Level.ROOF)
                .plat(1250, 520, 380, 42, Level.ROOF)
                .plat(1860, 540, 420, 42, Level.ROOF)
                .plat(2480, 500, 360, 42, Level.ROOF)
                .plat(3080, 540, 420, 42, Level.ROOF)
                .plat(3760, 520, 400, 42, Level.ROOF)
                .plat(900, 640, 160, 30, Level.ROCK)
                .plat(1680, 650, 150, 30, Level.ROCK)
                .plat(2300, 645, 150, 30, Level.ROCK)
                .plat(2950, 640, 150, 30, Level.ROCK)
                .deco(Level.TREE, 450, 720, 1.5f)
                .deco(Level.TREE, 1150, 720, 1.4f)
                .deco(Level.TREE, 1750, 720, 1.5f)
                .deco(Level.TREE, 2400, 720, 1.4f)
                .deco(Level.TREE, 3000, 720, 1.5f)
                .deco(Level.CAMPFIRE, 1420, 720, 1f)
                .deco(Level.WISTERIA, 2620, 720, 1.6f)
                .deco(Level.HUT, 3650, 720, 1.1f)
                .spawn("human", 700, 720)
                .spawn("human", 1450, 720)
                .spawn("slayer_water", 2050, 720)
                .spawn("human", 2700, 720)
                .spawn("artdemon", 3350, 720)
                .goalAt(4350, 720);
        demon[2].w = 4700;
        demon[2].h = 1500;

        demon[3] = new Level()
                .meta("Crowns of Smoke", "A patrol hunts YOU tonight", Level.Theme.VILLAGE, false, Level.WinMode.GOAL_AFTER_KILLS,
                        "The Patrol", "Water bearers keep their distance. Close it fast or burn them out.")
                .spawnPlayer(140, 700)
                .ground(0, 5000, 700, Level.WOOD)
                .plat(800, 560, 200, 42, Level.ROOF)
                .plat(1600, 480, 220, 42, Level.ROOF)
                .plat(2500, 560, 200, 42, Level.ROOF)
                .plat(3400, 480, 220, 42, Level.ROOF)
                .deco(Level.HOUSE, 500, 700, 1.3f)
                .deco(Level.LANTERN, 1200, 700, 1f)
                .deco(Level.BANNER, 2100, 700, 1f)
                .deco(Level.LANTERN, 3000, 700, 1f)
                .deco(Level.HOUSE, 3900, 700, 1.2f)
                .spawn("slayer_water", 1100, 700)
                .spawn("human", 1600, 700)
                .spawn("artdemon", 2050, 700)
                .spawn("slayer_water", 2300, 700)
                .spawn("slayer_flame", 3000, 700)
                .spawn("slayer_water", 3900, 700)
                .goalAt(4750, 700);
        demon[3].w = 5100;
        demon[3].h = 1500;

        demon[4] = new Level()
                .meta("Mansion of Paper Walls", "Raid the estate under cover of dark", Level.Theme.MANSION, false, Level.WinMode.GOAL_AFTER_KILLS,
                        "House of Lanterns", "Lamplit halls full of guards. No sun here - but no mercy either.")
                .lines("Voice: Paper walls. Thin doors. Warm blood.", "")
                .spawnPlayer(140, 700)
                .ground(0, 5200, 700, Level.WOOD)
                .plat(700, 560, 500, 40, Level.ROOF)
                .plat(1500, 480, 600, 40, Level.ROOF)
                .plat(2400, 560, 500, 40, Level.ROOF)
                .plat(3200, 480, 600, 40, Level.ROOF)
                .plat(4100, 560, 500, 40, Level.ROOF)
                .deco(Level.WINDOW, 900, 560, 1.2f)
                .deco(Level.WINDOW, 2700, 560, 1.2f)
                .deco(Level.LANTERN, 1350, 700, 1f)
                .deco(Level.LANTERN, 3000, 700, 1f)
                .deco(Level.BANNER, 3900, 700, 1f)
                .spawn("slayer_water", 1000, 700)
                .spawn("artdemon", 1500, 700)
                .spawn("slayer_flame", 1900, 700)
                .spawn("slayer_water", 2800, 700)
                .spawn("slayer_flame", 3700, 700)
                .spawn("human", 4400, 700)
                .goalAt(4950, 700);
        demon[4].w = 5400;
        demon[4].h = 1500;

        demon[5] = new Level()
                .meta("Duel Beneath the Red Moon", "The captain comes alone", Level.Theme.SHRINE, false, Level.WinMode.BOSS,
                        "Red Moon Duel", "Kurenai, Blazing Captain of the slayer corps, answers the rumors. Devour him.")
                .lines("Kurenai: For everyone you ate - answer to my flame.", "")
                .spawnPlayer(160, 700)
                .ground(0, 2600, 700, Level.STONE)
                .plat(300, 470, 26, 230, Level.STONE)
                .plat(2270, 470, 26, 230, Level.STONE)
                .deco(Level.TORII, 150, 700, 1.4f)
                .deco(Level.LANTERN, 500, 700, 1.1f)
                .deco(Level.STATUE, 1300, 700, 1.4f)
                .deco(Level.LANTERN, 2100, 700, 1.1f)
                .spawn("flameboss", 1850, 700)
                .goalAt(2480, 700);
        demon[5].w = 2700;
        demon[5].h = 1500;
    }
}
