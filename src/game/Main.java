package game;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public final class Main {
    public static void main(String[] args) {
        for (String a : args) if (a.equals("--selftest")) { System.exit(SelfTest.run()); return; }
        new Game().start();
    }
}

final class SelfTest {
    static int run() {
        try {
            Profile p = new Profile();
            p.path = Profile.Path.SLAYER;
            p.style = Profile.Style.WATER;
            simPath(p, LevelData.slayerLevels(), "slayer/water");
            p.style = Profile.Style.FLAME;
            simPath(p, LevelData.slayerLevels(), "slayer/flame");
            p.style = Profile.Style.WIND;
            simPath(p, LevelData.slayerLevels(), "slayer/wind");
            p.style = Profile.Style.NONE;
            simPath(p, LevelData.slayerLevels(), "slayer/black-blade");
            p.path = Profile.Path.DEMON;
            p.style = Profile.Style.NONE;
            simPath(p, LevelData.demonLevels(), "demon");
            System.out.println("SELFTEST PASS");
            return 0;
        } catch (Throwable t) {
            System.out.println("SELFTEST FAIL");
            t.printStackTrace();
            return 1;
        }
    }

    private static void simPath(Profile prof, Level[] levels, String tag) {
        FakeInput in = new FakeInput();
        Glow glow = new Glow();
        BufferedImage img = new BufferedImage(Game.VIEW_W, Game.VIEW_H, BufferedImage.TYPE_INT_RGB);
        long seed = 12345;
        for (int li = 0; li < levels.length; li++) {
            Player pl = new Player(prof);
            World w = new World(levels[li], in, pl);
            float dt = 1 / 60f;
            for (int tick = 0; tick < 900; tick++) {
                Game.time += dt;
                seed = seed * 6364136223846793005L + 1442695040888963407L;
                int r = (int) (seed >>> 33);
                in.left = (r & 3) == 0;
                in.right = (r & 3) == 1;
                in.jump = (r & 7) == 2;
                in.dash = (r & 15) == 3;
                in.attack = (r & 7) == 4;
                in.cast = (r & 31) == 5;
                if (tick % 90 == 0 && !pl.abilities.isEmpty()) {
                    int slot = (tick / 90) % pl.abilities.size();
                    w.castAbility(pl, pl.abilities.get(slot));
                }
                w.step(dt);
                if (tick % 300 == 150 && !w.player.dead) {
                    Graphics2D g = img.createGraphics();
                    g.setColor(Color.BLACK);
                    g.fillRect(0, 0, Game.VIEW_W, Game.VIEW_H);
                    WorldView.render(w, g, glow, Game.VIEW_W, Game.VIEW_H);
                    g.dispose();
                }
            }
            System.out.println("[" + tag + "] level " + li + " ok - kills=" + w.kills + "/" + w.totalEnemies
                    + (w.level.winMode == Level.WinMode.BOSS ? " bossDead=" + w.bossDead : ""));
        }
    }

    static class FakeInput implements InputProvider {
        boolean left, right, jump, dash, attack, cast;

        public boolean down(int kc) {
            return switch (kc) {
                case java.awt.event.KeyEvent.VK_A -> left;
                case java.awt.event.KeyEvent.VK_D -> right;
                case java.awt.event.KeyEvent.VK_SHIFT -> dash;
                default -> false;
            };
        }

        public boolean pressed(int kc) {
            return switch (kc) {
                case java.awt.event.KeyEvent.VK_W -> jump;
                case java.awt.event.KeyEvent.VK_J -> attack;
                case java.awt.event.KeyEvent.VK_F -> cast;
                default -> false;
            };
        }

        public boolean anyPressed(int... kcs) {
            for (int k : kcs) if (pressed(k)) return true;
            return false;
        }
    }
}
