package game;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.RoundRectangle2D;

interface State {
    void update();

    void render(Graphics2D g);

    default void enter() {
    }

    static State title(Game game) {
        return new Title(game);
    }

    class Title implements State {
        private final Game game;

        Title(Game game) { this.game = game; }

        public void update() {
            if (Game.input.anyPressed(Input.CONFIRM, KeyEvent.VK_SPACE)) Game.change(new CharSelect(game));
        }

        public void render(Graphics2D g) {
            int vw = Game.VIEW_W, vh = Game.VIEW_H;
            g.setPaint(new GradientPaint(0, 0, new Color(12, 15, 36), 0, vh, new Color(44, 30, 66)));
            g.fillRect(0, 0, vw, vh);
            for (int i = 0; i < 70; i++) {
                float x = frac(i * 0.613f) * vw, y = frac(i * 0.379f) * vh * 0.6f;
                int tw = (int) (90 + 140 * (0.5 + 0.5 * FMath.sin(Game.time * (2 + frac(i * 7.7f) * 3) + i)));
                g.setColor(new Color(215, 224, 255, tw));
                g.fillRect((int) x, (int) y, frac(i * 3.1f) > 0.85f ? 2 : 1, frac(i * 3.1f) > 0.85f ? 2 : 1);
            }
            g.setColor(new Color(222, 230, 252));
            g.fillOval(940, 90, 130, 130);
            g.setColor(new Color(196, 206, 240));
            g.fillOval(975, 120, 26, 20);
            g.fillOval(1010, 160, 16, 14);
            g.setColor(new Color(10, 13, 30));
            ridge(g, vh * 0.72f, 110, 3);
            g.setColor(new Color(6, 8, 20));
            ridge(g, vh * 0.82f, 80, 7);

            for (int i = 0; i < 24; i++) {
                float px = frac(i * 0.771f) * vw + FMath.sin(Game.time * 0.8f + i) * 30;
                float py = ((frac(i * 0.431f) * vh) + Game.time * (26 + frac(i) * 30)) % (vh + 40) - 20;
                g.setColor(new Color(190, 150, 235, 170));
                g.fillOval((int) px, (int) py, 7, 4);
            }

            String t = "DEMON  SLAYER";
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 84));
            shadow(g, t, vw / 2f, 250, new Color(232, 236, 248));
            g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 19));
            shadow(g, "Kimetsu no Yaiba  -  fan platformer", vw / 2f, 292, new Color(180, 186, 210));
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
            float bl = 0.55f + 0.45f * FMath.sin(Game.time * 4);
            shadow(g, "PRESS  ENTER", vw / 2f, 500, new Color(255, 220, 140, (int) (255 * bl)));
            g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
            shadow(g, "Choose the path of a Demon Slayer... or become the demon.", vw / 2f, 540, new Color(150, 156, 180));
        }

        private void ridge(Graphics2D g, float baseY, float amp, int seed) {
            Polygon poly = new Polygon();
            for (int x = 0; x <= Game.VIEW_W; x += 20)
                poly.addPoint(x, (int) (baseY - (FMath.noise(x * 0.004f + seed) * amp)));
            poly.addPoint(Game.VIEW_W, Game.VIEW_H);
            poly.addPoint(0, Game.VIEW_H);
            g.fillPolygon(poly);
        }
    }

    class CharSelect implements State {
        private final Game game;
        private int sel;
        private Player slayDemo, demDemo;
        private float swing1, swing2;

        CharSelect(Game game) { this.game = game; }

        public void enter() {
            Profile ps = new Profile();
            ps.path = Profile.Path.SLAYER;
            ps.style = Profile.Style.WATER;
            slayDemo = new Player(ps);
            slayDemo.init(0, 0, 100);
            slayDemo.groundYForShadow = 0;
            slayDemo.facingRight = true;
            Profile pd = new Profile();
            pd.path = Profile.Path.DEMON;
            demDemo = new Player(pd);
            demDemo.init(0, 0, 100);
            demDemo.groundYForShadow = 0;
            demDemo.facingRight = false;
        }

        public void update() {
            if (Game.input.pressed(Input.LEFT)) sel = 0;
            if (Game.input.pressed(Input.RIGHT)) sel = 1;
            if (Game.input.pressed(Input.PAUSE)) Game.change(new Title(game));
            if (Game.input.pressed(Input.CONFIRM)) {
                Game.profile.path = sel == 0 ? Profile.Path.SLAYER : Profile.Path.DEMON;
                Game.change(new Intro(game));
            }
            float dt = 1 / 60f;
            swing1 -= dt;
            swing2 -= dt;
            if (swing1 <= 0) { swing1 = FMath.rand(1.4f, 2.6f); slayDemo.menuSwing(); }
            if (swing2 <= 0) { swing2 = FMath.rand(1.4f, 2.6f); demDemo.menuSwing(); }
            slayDemo.menuTick(dt);
            demDemo.menuTick(dt);
        }

        public void render(Graphics2D g) {
            int vw = Game.VIEW_W, vh = Game.VIEW_H;
            g.setPaint(new GradientPaint(0, 0, new Color(14, 14, 26), 0, vh, new Color(30, 24, 44)));
            g.fillRect(0, 0, vw, vh);
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 38));
            shadow(g, "CHOOSE YOUR PATH", vw / 2f, 92, Color.WHITE);

            drawCard(g, 250, 150, 340, 420, sel == 0, "DEMON SLAYER", new Color(70, 150, 255),
                    "Human. Hunts demons", "with breathing arts.", "Train under Urokodaki,", "survive Final Selection.", slayDemo);
            drawCard(g, 690, 150, 340, 420, sel == 1, "DEMON", new Color(205, 25, 55),
                    "Night walker. Regenerates,", "wields Blood Arts...", "but sunlight is death.", "", demDemo);

            g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 15));
            shadow(g, "A / D  to choose      ENTER  to commit", vw / 2f, 640, new Color(200, 205, 225));
        }

        private void drawCard(Graphics2D g, int x, int y, int w, int h, boolean sel, String title, Color c,
                              String l1, String l2, String l3, String l4, Player demo) {
            Graphics2D gg = (Graphics2D) g.create();
            if (sel) {
                Glow.blob(gg, x + w / 2f, y + h / 2f, 300, new Color(c.getRed(), c.getGreen(), c.getBlue(), 60));
            }
            gg.setColor(new Color(10, 11, 18, sel ? 220 : 150));
            gg.fillRoundRect(x, y, w, h, 22, 22);
            if (sel) {
                gg.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 40));
                gg.fillRoundRect(x, y, w, h, 22, 22);
            }
            gg.setColor(sel ? c : new Color(50, 52, 68));
            gg.setStroke(new BasicStroke(sel ? 4f : 2f));
            gg.drawRoundRect(x, y, w, h, 22, 22);
            Graphics2D fg = (Graphics2D) gg.create();
            fg.setClip(new RoundRectangle2D.Float(x + 10, y + 12, w - 20, 282, 18, 18));
            float sc = 2.25f;
            float groundX = x + w / 2f, groundY = y + 278;
            fg.translate(groundX, groundY);
            fg.scale(sc, sc);
            fg.translate(-demo.x, -(demo.y + demo.h / 2f));
            demo.render(fg);
            fg.setComposite(Glow.ADD);
            demo.renderGlow(fg);
            fg.dispose();
            gg.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 27));
            shadow(gg, title, x + w / 2f, y + 312, sel ? c.brighter() : new Color(120, 124, 142));
            gg.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
            String[] ls = {l1, l2, l3, l4};
            for (int i = 0; i < 4; i++)
                if (!ls[i].isEmpty()) {
                    gg.setColor(new Color(195, 200, 218));
                    shadow(gg, ls[i], x + w / 2f, y + 342 + i * 21, new Color(195, 200, 218));
                }
            gg.dispose();
        }
    }

    class Intro implements State {
        private final Game game;
        private static final String[] SLAY = {
                "Tanjiro's family was slaughtered by a demon.",
                "His sister Nezuko was turned - but she resisted her hunger.",
                "",
                "You are a trainee under Urokodaki Sakonji,",
                "the masked former Water Hashira of Mt. Sagiri.",
                "",
                "Ten trials await on the mountain.",
                "Then... Final Selection.",
        };
        private static final String[] DEMN = {
                "You died in the mud outside your village.",
                "And woke in the dark - changed.",
                "",
                "Flesh knits. Strength floods. Hunger whispers.",
                "The sun is your enemy; the night, your kingdom.",
                "",
                "The slayers will come for you.",
                "Let them.",
        };
        private float t;

        Intro(Game game) { this.game = game; }

        public void update() {
            t += 1 / 60f;
            if (Game.input.pressed(Input.CONFIRM) && t > 1) Game.change(new LevelSelect(game));
        }

        public void render(Graphics2D g) {
            g.setColor(new Color(8, 9, 16));
            g.fillRect(0, 0, Game.VIEW_W, Game.VIEW_H);
            String[] ls = Game.profile.path == Profile.Path.SLAYER ? SLAY : DEMN;
            int shown = (int) (t / 1.1f);
            g.setFont(new Font(Font.SERIF, Font.PLAIN, 21));
            for (int i = 0; i < Math.min(shown, ls.length); i++) {
                float a = FMath.clamp((t - i * 1.1f) / 0.9f, 0, 1);
                if (ls[i].isEmpty()) continue;
                g.setColor(new Color(214, 218, 232, (int) (255 * a)));
                int wd = g.getFontMetrics().stringWidth(ls[i]);
                g.drawString(ls[i], Game.VIEW_W / 2f - wd / 2f, 150 + i * 52);
            }
            if (shown >= ls.length) {
                g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
                shadow(g, "ENTER", Game.VIEW_W / 2f, 620, new Color(255, 220, 140));
            }
        }
    }

    class LevelSelect implements State {
        private final Game game;
        private int sel;

        LevelSelect(Game game) { this.game = game; }

        public void update() {
            Level[] all = levels();
            int unlocked = unlockedCount();
            int dojoSel = all.length;
            if (sel > unlocked && sel != dojoSel) sel = dojoSel;
            if (Game.input.pressed(Input.RIGHT) || Game.input.pressed(Input.NEXT))
                sel = sel == dojoSel ? 0 : sel < unlocked ? sel + 1 : dojoSel;
            if (Game.input.pressed(Input.LEFT) || Game.input.pressed(Input.PREV))
                sel = sel == dojoSel ? unlocked : Math.max(0, sel - 1);
            if (Game.input.pressed(Input.DOWN)) {
                if (sel != dojoSel) {
                    int next = sel + 6;
                    sel = next <= unlocked ? next : dojoSel;
                }
            }
            if (Game.input.pressed(Input.JUMP))
                sel = sel == dojoSel ? Math.max(0, unlocked - unlocked % 6) : Math.max(0, sel - 6);
            if (Game.input.pressed(Input.PAUSE)) Game.change(new CharSelect(game));
            if (Game.input.pressed(Input.CONFIRM)) Game.change(new Play(game, sel == dojoSel ? -1 : sel));
        }

        private Level[] levels() {
            return Game.profile.path == Profile.Path.SLAYER ? LevelData.slayerLevels() : LevelData.demonLevels();
        }

        private int unlockedCount() {
            return Game.profile.path == Profile.Path.SLAYER ? Game.profile.unlockedSlayer : Game.profile.unlockedDemon;
        }

        public void render(Graphics2D g) {
            int vw = Game.VIEW_W, vh = Game.VIEW_H;
            g.setPaint(new GradientPaint(0, 0, new Color(13, 15, 28), 0, vh, new Color(26, 22, 40)));
            g.fillRect(0, 0, vw, vh);
            Level[] all = levels();
            int unlocked = unlockedCount();
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 32));
            String hd = Game.profile.path == Profile.Path.SLAYER ? "THE SLAYER'S ROAD" : "THE DEMON'S ROAD";
            shadow(g, hd, vw / 2f, 64, Game.profile.path == Profile.Path.SLAYER ? new Color(120, 180, 255) : new Color(255, 90, 110));
            g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
            String st = Game.profile.path == Profile.Path.SLAYER
                    ? (Game.profile.style == Profile.Style.NONE ? "Blade color: unchanging..." :
                    "Blade: " + styleName(Game.profile.style)) + rankSuffix(Game.profile.slayerRank)
                    : "Blood Demon Art awakened";
            shadow(g, st, vw / 2f, 88, new Color(170, 176, 198));

            int cols = 6, cw = 172, chh = 108, gx = (vw - cols * (cw + 14) + 14) / 2;
            for (int i = 0; i < all.length; i++) {
                int r = i / cols, c = i % cols;
                int x = gx + c * (cw + 14), y = 130 + r * (chh + 16);
                boolean open = i <= unlocked;
                boolean cur = i == sel;
                g.setColor(cur ? new Color(40, 46, 66) : new Color(14, 16, 26, 200));
                g.fillRoundRect(x, y, cw, chh, 14, 14);
                g.setColor(cur ? new Color(255, 210, 130) : new Color(60, 64, 84));
                g.setStroke(new BasicStroke(cur ? 3f : 1.5f));
                g.drawRoundRect(x, y, cw, chh, 14, 14);
                if (!open) {
                    g.setColor(new Color(60, 62, 76, 140));
                    g.fillRoundRect(x, y, cw, chh, 14, 14);
                    g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
                    g.setColor(new Color(110, 114, 132));
                    shadow(g, "X", x + cw / 2f, y + chh / 2f + 8, new Color(110, 114, 132));
                    continue;
                }
                g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
                g.setColor(new Color(235, 238, 246));
                g.drawString((i + 1) + ".", x + 10, y + 22);
                g.setColor(all[i].daytime ? new Color(255, 210, 100) : new Color(150, 165, 255));
                g.fillOval(x + cw - 24, y + 10, 13, 13);
                g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
                g.setColor(new Color(208, 212, 226));
                wrap(g, all[i].name, x + 10, y + 44, cw - 20);
                if (all[i].winMode == Level.WinMode.BOSS) {
                    g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
                    g.setColor(new Color(255, 120, 110));
                    g.drawString("BOSS", x + 10, y + chh - 10);
                }
                if (i == unlocked) {
                    g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
                    g.setColor(new Color(140, 255, 170));
                    g.drawString("NEXT", x + cw - 42, y + chh - 10);
                }
            }
            int dbw = 300, dbh = 58, dbx = (vw - dbw) / 2, dby = vh - 112;
            boolean dojoCur = sel == all.length;
            g.setColor(dojoCur ? new Color(42, 34, 28) : new Color(16, 14, 18, 220));
            g.fillRoundRect(dbx, dby, dbw, dbh, 16, 16);
            g.setColor(dojoCur ? new Color(255, 210, 130) : new Color(110, 86, 58));
            g.setStroke(new BasicStroke(dojoCur ? 3f : 1.5f));
            g.drawRoundRect(dbx, dby, dbw, dbh, 16, 16);
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
            shadow(g, "Enter Dojo", vw / 2f, dby + 37, dojoCur ? new Color(255, 220, 150) : new Color(200, 182, 152));
            g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
            shadow(g, "Arrows navigate    ENTER begin    ESC back", vw / 2f, vh - 26, new Color(185, 190, 210));
        }

        private void wrap(Graphics2D g, String s, float x, float y, int maxw) {
            var fm = g.getFontMetrics();
            String[] words = s.split(" ");
            StringBuilder line = new StringBuilder();
            int yy = 0;
            for (String wordText : words) {
                if (fm.stringWidth(line + " " + wordText) > maxw) {
                    g.drawString(line.toString(), x, y + yy);
                    yy += 15;
                    line = new StringBuilder();
                }
                if (line.length() > 0) line.append(" ");
                line.append(wordText);
            }
            g.drawString(line.toString(), x, y + yy);
        }
    }

    class Play implements State {
        private final Game game;
        public final int index;
        private World world;
        private final HUD hud = new HUD();
        private boolean paused, showHelp;
        private int pauseSel;
        private boolean viewingLog, dojoChoosing;
        private int dojoSel, dojoScroll;
        private static final String[] DOJO_TYPES = {
                "dummy", "demon", "artdemon", "swamp", "swamp_strong", "temple", "civilian", "muzan", "susumaru", "yahaba", "human", "hunter", "slayer_water", "slayer_flame", "slayer_wind", "sabito", "hand", "flameboss"
        };
        private static final String[] DOJO_NAMES = {
                "Dummy", "Demon", "Blood Art Demon", "Swamp Demon", "Strong Swamp Demon", "Temple Demon", "Civilian", "Muzan", "Susumaru", "Yahaba", "Bandit", "Hunter", "Water Bearer", "Flame Bearer", "Wind Bearer", "Sabito", "Hand Demon", "Kurenai"
        };

        public Play(Game game, int index) {
            this.game = game;
            this.index = index;
            enter();
        }

        public void enter() {
            restart();
        }

        private void restart() {
            Player pl = new Player(Game.profile);
            Level lv = dojo() ? LevelData.dojoLevel()
                    : Game.profile.path == Profile.Path.SLAYER ? LevelData.slayerLevels()[index] : LevelData.demonLevels()[index];
            world = new World(lv, Game.input, pl);
            world.slayerLevelIndex = !dojo() && Game.profile.path == Profile.Path.SLAYER ? index : -1;
            paused = false;
            viewingLog = false;
            dojoChoosing = dojo();
        }

        private boolean dojo() { return index < 0; }

        public void update() {
            if (Game.CHEATS_ENABLED && Game.input.pressed(KeyEvent.VK_CLOSE_BRACKET)) { skip(1); return; }
            if (Game.CHEATS_ENABLED && Game.input.pressed(KeyEvent.VK_OPEN_BRACKET)) { skip(-1); return; }
            if (Game.CHEATS_ENABLED && Game.input.pressed(KeyEvent.VK_BACK_SLASH)) switchStyle();
            if (Game.CHEATS_ENABLED && Game.input.pressed(KeyEvent.VK_EQUALS)) switchPath();
            if (Game.input.pressed(Input.HELP)) showHelp = !showHelp;
            if (dojo()) {
                updateDojo();
                if (dojoChoosing) return;
            }
            if (world.complete) {
                if (Game.input.pressed(Input.CONFIRM)) advance();
                world.step(1 / 60f);
                return;
            }
            if (!paused) {
                if (Game.input.pressed(Input.PAUSE)) paused = true;
                else {
                    world.step(1 / 60f);
                    if (world.failed && Game.input.pressed(Input.RETRY)) restart();
                    if (world.failed && Game.input.pressed(Input.CONFIRM)) restart();
                }
            } else {
                if (viewingLog) {
                    if (Game.input.pressed(Input.PAUSE) || Game.input.pressed(Input.CONFIRM)) viewingLog = false;
                    return;
                }
                if (Game.input.pressed(Input.JUMP) || Game.input.pressed(Input.DOWN))
                    pauseSel = (pauseSel + (Game.input.pressed(Input.DOWN) ? 1 : 3)) % 4;
                if (Game.input.pressed(Input.PAUSE)) paused = false;
                if (Game.input.pressed(Input.RETRY)) { restart(); return; }
                if (Game.input.pressed(Input.CONFIRM)) {
                    if (pauseSel == 0) paused = false;
                    else if (pauseSel == 1) restart();
                    else if (pauseSel == 2) viewingLog = true;
                    else Game.change(new Title(game));
                }
            }
        }

        private void updateDojo() {
            if (!dojoChoosing && world.player.dead && world.player.deathT > 1.0f) {
                restart();
                return;
            }
            if (!dojoChoosing && world.boss != null && world.boss.dead && world.boss.deathT > 0.9f) {
                restart();
                return;
            }
            if (!dojoChoosing) return;
            if (Game.input.pressed(Input.DOWN)) dojoSel = (dojoSel + 1) % (DOJO_NAMES.length + 1);
            if (Game.input.pressed(Input.JUMP)) dojoSel = (dojoSel + DOJO_NAMES.length) % (DOJO_NAMES.length + 1);
            int visibleRows = 14;
            if (dojoSel < dojoScroll) dojoScroll = dojoSel;
            if (dojoSel >= dojoScroll + visibleRows) dojoScroll = dojoSel - visibleRows + 1;
            if (Game.input.pressed(Input.PAUSE)) Game.change(new LevelSelect(game));
            if (Game.input.pressed(Input.CONFIRM)) {
                if (dojoSel >= DOJO_NAMES.length) Game.change(new LevelSelect(game));
                else {
                    Player pl = world.player;
                    pl.init(world.level.spawnX, world.level.spawnY, pl.maxHp);
                    pl.hp = pl.maxHp;
                    pl.sp = pl.maxSp;
                    pl.dead = false;
                    pl.deathT = 0;
                    world.spawnDojoOpponent(DOJO_TYPES[dojoSel]);
                    dojoChoosing = false;
                }
            }
        }

        /** Cheat: [ and ] jump between levels without touching progression. */
        private void skip(int dir) {
            if (dojo()) return;
            int count = Game.profile.path == Profile.Path.SLAYER ? LevelData.SLAYER_COUNT : LevelData.DEMON_COUNT;
            int next = index + dir;
            if (next < 0) return;
            if (next >= count) { Game.change(new Ending(game)); return; }
            Game.change(new Play(game, next));
        }

        /** Cheat: backslash cycles breathing styles or demon Blood Demon Art groups. */
        private void switchStyle() {
            if (Game.profile.path != Profile.Path.SLAYER) {
                Game.profile.demonArt = switch (Game.profile.demonArt) {
                    case CRIMSON_HUNGER -> Profile.DemonArt.FOREST_HAND;
                    case FOREST_HAND -> Profile.DemonArt.SWAMP;
                    case SWAMP -> Profile.DemonArt.SUSUMARU;
                    case SUSUMARU -> Profile.DemonArt.YAHABA;
                    case YAHABA -> Profile.DemonArt.COMBUSTIBLE_BLOOD;
                    case COMBUSTIBLE_BLOOD -> Profile.DemonArt.CRIMSON_HUNGER;
                };
                world.player.setDemonArt(Game.profile.demonArt);
                String artName = switch (Game.profile.demonArt) {
                    case FOREST_HAND -> "Grasp of the Dead";
                    case SWAMP -> "Swamp Demon Art";
                    case SUSUMARU -> "Temari Demon Art";
                    case YAHABA -> "Arrow Demon Art";
                    case COMBUSTIBLE_BLOOD -> "Combustible Blood";
                    default -> "Crimson Hunger";
                };
                Color artColor = Game.profile.demonArt == Profile.DemonArt.FOREST_HAND ? new Color(150, 220, 110)
                        : Game.profile.demonArt == Profile.DemonArt.SWAMP ? new Color(70, 190, 175)
                        : Game.profile.demonArt == Profile.DemonArt.SUSUMARU ? new Color(235, 190, 74)
                        : Game.profile.demonArt == Profile.DemonArt.YAHABA ? new Color(255, 80, 92)
                        : Game.profile.demonArt == Profile.DemonArt.COMBUSTIBLE_BLOOD ? AbilityCast.NEZUKO_HI : new Color(255, 120, 130);
                world.popup(world.player.x, world.player.y - world.player.h - 12,
                        artName, artColor);
                return;
            }
            Game.profile.style = switch (Game.profile.style) {
                case NONE -> Profile.Style.WATER;
                case WATER -> Profile.Style.FLAME;
                case FLAME -> Profile.Style.WIND;
                case WIND -> Profile.Style.NONE;
            };
            world.player.restyle(Game.profile.style);
            world.popup(world.player.x, world.player.y - world.player.h - 12,
                    Game.profile.style == Profile.Style.NONE ? "Black Blade" : styleName(Game.profile.style),
                    new Color(140, 220, 255));
        }

        /** Cheat: equals toggles between slayer breathing and demon blood art loadouts. */
        private void switchPath() {
            if (Game.profile.path == Profile.Path.SLAYER) {
                Game.profile.path = Profile.Path.DEMON;
                world.player.demonArt = Game.profile.demonArt;
                world.player.setPath(Profile.Path.DEMON);
                world.popup(world.player.x, world.player.y - world.player.h - 12, "Demon: Blood Demon Art", new Color(255, 120, 130));
            } else {
                Game.profile.path = Profile.Path.SLAYER;
                if (Game.profile.style == Profile.Style.NONE) Game.profile.style = Profile.Style.WATER;
                world.player.style = Game.profile.style;
                world.player.setPath(Profile.Path.SLAYER);
                world.popup(world.player.x, world.player.y - world.player.h - 12, styleName(Game.profile.style), new Color(140, 220, 255));
            }
        }

        private void advance() {
            if (dojo()) { Game.change(new LevelSelect(game)); return; }
            Profile pr = Game.profile;
            boolean slayer = pr.path == Profile.Path.SLAYER;
            int next = index + 1;
            if (slayer) {
                pr.unlockedSlayer = Math.max(pr.unlockedSlayer, next);
                if (index == LevelData.TRAINING_COUNT - 1 && !pr.colorChanged) {
                    Game.change(new ColorChange(game, next));
                    return;
                }
                if (index == 17 && pr.slayerRank == Profile.SlayerRank.NONE) {
                    pr.slayerRank = Profile.SlayerRank.MIZUNOTO;
                    Game.change(new RankAward(pr.slayerRank, new Play(game, next)));
                    return;
                }
                if (next >= LevelData.SLAYER_COUNT) {
                    pr.finishedSlayer = true;
                    Game.change(new Ending(game));
                } else Game.change(new Play(game, next));
            } else {
                pr.unlockedDemon = Math.max(pr.unlockedDemon, next);
                if (next >= LevelData.DEMON_COUNT) {
                    pr.finishedDemon = true;
                    Game.change(new Ending(game));
                } else Game.change(new Play(game, next));
            }
        }

        public void render(Graphics2D g) {
            WorldView.render(world, g, Game.glow, Game.VIEW_W, Game.VIEW_H);
            hud.render(g, world, Game.profile, Game.VIEW_W, Game.VIEW_H, showHelp);
            if (dojoChoosing) renderDojoChooser(g);
            if (world.failed) renderFailure(g);
            if (paused) {
                g.setColor(new Color(6, 7, 12, 170));
                g.fillRect(0, 0, Game.VIEW_W, Game.VIEW_H);
                g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 40));
                shadow(g, "PAUSED", Game.VIEW_W / 2f, 260, Color.WHITE);
                if (viewingLog) {
                    renderLog(g);
                    return;
                }
                String[] opts = {"Resume", "Restart level", "View log", "Quit to title"};
                g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 21));
                for (int i = 0; i < opts.length; i++) {
                    boolean cur = i == pauseSel;
                    shadow(g, (cur ? "> " : "") + opts[i], Game.VIEW_W / 2f, 340 + i * 42,
                            cur ? new Color(255, 215, 130) : new Color(175, 180, 200));
                }
                g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
                shadow(g, "W/S select   ENTER confirm   R restart   ESC resume", Game.VIEW_W / 2f, 520, new Color(150, 155, 175));
            }
        }

        private void renderFailure(Graphics2D g) {
            g.setColor(new Color(5, 6, 10, 178));
            g.fillRect(0, 0, Game.VIEW_W, Game.VIEW_H);
            int bw = 560, bh = 190, bx = (Game.VIEW_W - bw) / 2, by = 248;
            g.setColor(new Color(18, 12, 14, 238));
            g.fillRoundRect(bx, by, bw, bh, 18, 18);
            g.setColor(new Color(160, 52, 58));
            g.setStroke(new BasicStroke(2f));
            g.drawRoundRect(bx, by, bw, bh, 18, 18);
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 34));
            shadow(g, "LEVEL FAILED", Game.VIEW_W / 2f, by + 58, new Color(255, 210, 210));
            g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 20));
            shadow(g, world.failReason, Game.VIEW_W / 2f, by + 104, new Color(235, 220, 210));
            g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
            shadow(g, "Press ENTER or R to retry", Game.VIEW_W / 2f, by + 148, new Color(170, 176, 196));
        }

        private void renderDojoChooser(Graphics2D g) {
            g.setColor(new Color(6, 7, 12, 185));
            g.fillRect(0, 0, Game.VIEW_W, Game.VIEW_H);
            int bw = 430, rowH = 31, visibleRows = 14, totalRows = DOJO_NAMES.length + 1;
            int bx = (Game.VIEW_W - bw) / 2, by = 92;
            int bh = 112 + visibleRows * rowH;
            g.setColor(new Color(18, 14, 12, 235));
            g.fillRoundRect(bx, by, bw, bh, 18, 18);
            g.setColor(new Color(148, 108, 64));
            g.setStroke(new BasicStroke(2f));
            g.drawRoundRect(bx, by, bw, bh, 18, 18);
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 28));
            shadow(g, "DOJO", Game.VIEW_W / 2f, by + 42, new Color(255, 220, 150));
            g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
            shadow(g, "Choose an opponent", Game.VIEW_W / 2f, by + 66, new Color(205, 190, 170));
            for (int row = 0; row < visibleRows; row++) {
                int i = dojoScroll + row;
                if (i >= totalRows) break;
                boolean cur = i == dojoSel;
                String label = i == DOJO_NAMES.length ? "Leave Dojo" : DOJO_NAMES[i];
                int y = by + 100 + row * rowH;
                if (cur) {
                    g.setColor(new Color(70, 48, 30));
                    g.fillRoundRect(bx + 42, y - 21, bw - 84, 27, 10, 10);
                }
                g.setFont(new Font(Font.SANS_SERIF, cur ? Font.BOLD : Font.PLAIN, 18));
                shadow(g, (cur ? "> " : "") + label, Game.VIEW_W / 2f, y,
                        cur ? new Color(255, 218, 132) : new Color(214, 208, 196));
            }
            if (totalRows > visibleRows) {
                int trackX = bx + bw - 28, trackY = by + 94, trackH = visibleRows * rowH - 8;
                g.setColor(new Color(70, 58, 46, 190));
                g.fillRoundRect(trackX, trackY, 7, trackH, 6, 6);
                float frac = dojoScroll / (float) (totalRows - visibleRows);
                int thumbH = Math.max(36, (int) (trackH * visibleRows / (float) totalRows));
                int thumbY = trackY + (int) ((trackH - thumbH) * FMath.clamp(frac, 0, 1));
                g.setColor(new Color(255, 218, 132));
                g.fillRoundRect(trackX - 1, thumbY, 9, thumbH, 7, 7);
            }
            g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
            shadow(g, "W/S select   ENTER confirm   ESC leave", Game.VIEW_W / 2f, by + bh - 18, new Color(150, 155, 175));
        }

        private void renderLog(Graphics2D g) {
            int bw = 660, bh = 460, bx = (Game.VIEW_W - bw) / 2, by = 150;
            g.setColor(new Color(8, 10, 16, 238));
            g.fillRoundRect(bx, by, bw, bh, 16, 16);
            g.setColor(new Color(120, 126, 145));
            g.drawRoundRect(bx, by, bw, bh, 16, 16);
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
            shadow(g, "ABILITY LOG", Game.VIEW_W / 2f, by + 42, Color.WHITE);
            java.util.List<String> lines = Game.logLines();
            g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 15));
            if (lines.isEmpty()) {
                shadow(g, "No abilities used yet.", Game.VIEW_W / 2f, by + 100, new Color(170, 176, 196));
            } else {
                for (int i = 0; i < lines.size(); i++) {
                    g.setColor(new Color(214, 218, 232));
                    g.drawString(lines.get(i), bx + 34, by + 88 + i * 18);
                }
            }
            g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
            shadow(g, "ENTER or ESC to return", Game.VIEW_W / 2f, by + bh - 24, new Color(150, 155, 175));
        }
    }

    class RankAward implements State {
        private final Profile.SlayerRank rank;
        private final State next;
        private float t;

        RankAward(Profile.SlayerRank rank, State next) {
            this.rank = rank;
            this.next = next;
        }

        public void update() {
            t += 1 / 60f;
            if (t >= 5f) Game.change(next);
        }

        public void render(Graphics2D g) {
            int vw = Game.VIEW_W, vh = Game.VIEW_H;
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, vw, vh);
            float alpha = FMath.clamp(Math.min(t / 1.35f, (5f - t) / 1.35f), 0, 1);
            Composite old = g.getComposite();
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g.setColor(Color.WHITE);
            g.setFont(new Font(Font.SERIF, Font.BOLD, 132));
            shadow(g, rankSymbol(rank), vw / 2f, vh / 2f - 12, Color.WHITE);
            g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 27));
            shadow(g, rankName(rank), vw / 2f, vh / 2f + 48, Color.WHITE);
            g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 15));
            shadow(g, "Demon Slayer Corps Rank Granted", vw / 2f, vh / 2f + 92, new Color(235, 235, 235));
            g.setComposite(old);
        }
    }

    class ColorChange implements State {
        private final Game game;
        private final int nextIndex;
        private float t;
        private Profile.Style result;
        private boolean decided;

        ColorChange(Game game, int nextIndex) {
            this.game = game;
            this.nextIndex = nextIndex;
        }

        public void update() {
            t += 1 / 60f;
            if (t > 1.1f && !decided) {
                decided = true;
                int pick = (int) (Math.random() * 3);
                result = pick == 0 ? Profile.Style.WATER : pick == 1 ? Profile.Style.FLAME : Profile.Style.WIND;
                Game.profile.style = result;
                Game.profile.colorChanged = true;
            }
            if ((t > 5.2f || Game.input.pressed(Input.CONFIRM) && t > 3.4f)) Game.change(new Play(game, nextIndex));
        }

        public void render(Graphics2D g) {
            int vw = Game.VIEW_W, vh = Game.VIEW_H;
            g.setColor(new Color(6, 6, 10));
            g.fillRect(0, 0, vw, vh);

            float cx = vw / 2f, cy = vh / 2f - 30;
            Color bladeC;
            if (!decided) {
                int pick = (int) (t * 9) % 4;
                bladeC = switch (pick) {
                    case 0 -> new Color(70, 150, 255);
                    case 1 -> new Color(255, 85, 40);
                    case 2 -> new Color(70, 205, 110);
                    default -> new Color(255, 255, 255);
                };
            } else {
                bladeC = result == Profile.Style.WATER ? new Color(70, 150, 255)
                        : result == Profile.Style.FLAME ? new Color(255, 85, 40) : new Color(70, 205, 110);
                Glow.blob(g, cx, cy, 190 + FMath.sin(t * 6) * 12,
                        new Color(bladeC.getRed(), bladeC.getGreen(), bladeC.getBlue(), 110));
            }

            double ang = Math.toRadians(-58);
            float hx = cx - (float) Math.cos(ang) * 150, hy = cy - (float) Math.sin(ang) * 150;
            float bx = cx + (float) Math.cos(ang) * 210, by = cy + (float) Math.sin(ang) * 210;
            g.setStroke(new BasicStroke(13, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(new Color(34, 34, 40));
            g.drawLine((int) (hx - Math.cos(ang) * 40), (int) (hy - Math.sin(ang) * 40), (int) hx, (int) hy);
            g.setColor(new Color(190, 160, 90));
            g.fillRect((int) hx - 8, (int) hy - 8, 16, 16);
            g.setStroke(new BasicStroke(9, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(bladeC.darker());
            g.drawLine((int) hx, (int) hy, (int) bx, (int) by);
            g.setStroke(new BasicStroke(4.5f));
            g.setColor(bladeC);
            g.drawLine((int) hx, (int) hy, (int) bx, (int) by);
            g.setStroke(new BasicStroke(1.6f));
            g.setColor(Color.WHITE);
            g.drawLine((int) hx, (int) hy, (int) bx, (int) by);

            if (decided) {
                for (int i = 0; i < 14; i++) {
                    double a = i * 0.449f + t * 1.4f;
                    float d = 60 + ((t * 90 + i * 37) % 190);
                    g.setColor(new Color(255, 245, 200, (int) (200 - (d - 60))));
                    g.fillOval((int) (cx + Math.cos(a) * d), (int) (cy + Math.sin(a) * d), 4, 4);
                }
            }

            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 30));
            String txt = !decided ? "The blade trembles..." :
                    result == Profile.Style.WATER ? "Your blade turned BLUE - Water Breathing!" :
                            result == Profile.Style.FLAME ? "Your blade turned RED - Flame Breathing!" :
                                    "Your blade turned GREEN - Wind Breathing!";
            shadow(g, txt, vw / 2f, vh - 120, decided ? bladeC.brighter() : new Color(210, 212, 224));
            if (decided) {
                g.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 15));
                shadow(g, result == Profile.Style.WATER
                        ? "Flow like water. Forms: Surface Slash, Water Wheel, Flowing Dance, Striking Tide."
                        : result == Profile.Style.FLAME
                        ? "Burn like wildfire. Forms: Unknowing Fire, Rising Scorching Sun, Blazing Universe."
                        : "Cut like wind. Forms: Dust Whirlwind, Claws-Purifying Wind, Rising Dust Storm, Cold Mountain Wind.",
                        vw / 2f, vh - 86, new Color(200, 204, 220));
            }
        }
    }

    class Ending implements State {
        private final Game game;

        Ending(Game game) { this.game = game; }

        public void update() {
            if (Game.input.pressed(Input.CONFIRM)) Game.change(new Title(game));
        }

        public void render(Graphics2D g) {
            int vw = Game.VIEW_W, vh = Game.VIEW_H;
            boolean slayer = Game.profile.path == Profile.Path.SLAYER;
            g.setPaint(new GradientPaint(0, 0, slayer ? new Color(16, 22, 48) : new Color(30, 8, 14),
                    0, vh, slayer ? new Color(56, 44, 84) : new Color(60, 18, 30)));
            g.fillRect(0, 0, vw, vh);
            for (int i = 0; i < 40; i++) {
                float px = frac(i * 0.613f) * vw, py = ((frac(i * 0.379f) * vh) + Game.time * 20) % vh;
                g.setColor(slayer ? new Color(190, 150, 235, 140) : new Color(255, 90, 90, 90));
                g.fillOval((int) px, (int) py, 5, 3);
            }
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 44));
            shadow(g, slayer ? "YOU ARE A DEMON SLAYER" : "THE NIGHT IS YOURS",
                    vw / 2f, 180, slayer ? new Color(140, 195, 255) : new Color(255, 100, 115));
            g.setFont(new Font(Font.SERIF, Font.PLAIN, 19));
            String[] lines = slayer ? new String[]{
                    "You descended Fujikasane alive, Nichirin blade in hand.",
                    "Somewhere out there, the demon who took everything still waits.",
                    "",
                    "This is only the beginning."} : new String[]{
                    "The captain fell. The rumors will spread - and stronger slayers with them.",
                    "But tonight, you feast beneath the red moon.",
                    "",
                    "You chose this hunger. Wear it well."};
            for (int i = 0; i < lines.length; i++) {
                g.setColor(new Color(216, 220, 234));
                shadow(g, lines[i], vw / 2f, 270 + i * 40, new Color(216, 220, 234));
            }
            Profile pr = Game.profile;
            g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
            shadow(g, "Demons slain: " + pr.totalKills + "     Deaths: " + pr.deaths,
                    vw / 2f, 480, new Color(255, 215, 140));
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
            shadow(g, "Press ENTER for title", vw / 2f, 550, new Color(230, 230, 240));
        }
    }

    private static float frac(float v) { return v - (float) Math.floor(v); }

    private static String styleName(Profile.Style style) {
        return switch (style) {
            case WATER -> "Water Breathing";
            case FLAME -> "Flame Breathing";
            case WIND -> "Wind Breathing";
            default -> "Black Blade";
        };
    }

    private static String rankSuffix(Profile.SlayerRank rank) {
        return rank == Profile.SlayerRank.NONE ? "" : "     Rank: " + rankSymbol(rank) + " (" + rankName(rank) + ")";
    }

    private static String rankSymbol(Profile.SlayerRank rank) {
        return switch (rank) {
            case MIZUNOTO -> "癸";
            case MIZUNOE -> "壬";
            case KANOTO -> "辛";
            case KANOE -> "庚";
            case TSUCHINOTO -> "己";
            case TSUCHINOE -> "戊";
            case HINOTO -> "丁";
            case HINOE -> "丙";
            case KINOTO -> "乙";
            case KINOE -> "甲";
            default -> "";
        };
    }

    private static String rankName(Profile.SlayerRank rank) {
        return switch (rank) {
            case MIZUNOTO -> "Mizunoto";
            case MIZUNOE -> "Mizunoe";
            case KANOTO -> "Kanoto";
            case KANOE -> "Kanoe";
            case TSUCHINOTO -> "Tsuchinoto";
            case TSUCHINOE -> "Tsuchinoe";
            case HINOTO -> "Hinoto";
            case HINOE -> "Hinoe";
            case KINOTO -> "Kinoto";
            case KINOE -> "Kinoe";
            default -> "Unranked";
        };
    }

    private static void shadow(Graphics2D g, String s, float cx, float y, Color c) {
        int wdt = g.getFontMetrics().stringWidth(s);
        g.setColor(new Color(5, 5, 10, 210));
        g.drawString(s, cx - wdt / 2f + 2, y + 2);
        g.setColor(c);
        g.drawString(s, cx - wdt / 2f, y);
    }
}
