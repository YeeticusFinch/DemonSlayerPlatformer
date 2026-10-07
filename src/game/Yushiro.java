package game;

import java.awt.*;

public class Yushiro extends Civilian {
    public Yushiro() {
        name = "Yushiro";
        maxHp = hp = 90;
    }

    @Override
    public boolean hurt(World world, Fighter src, float dmgAmt, float dir, float kbX, float kbY) { return false; }

    @Override
    protected void renderBody(Graphics2D g) {
        float feet = bottom(), sh = feet - 52, hip = feet - 24;
        g.setColor(new Color(0, 0, 0, 45));
        g.fillOval((int) x - 21, (int) feet - 5, 42, 8);
        Color robe = new Color(92, 114, 88);
        g.setColor(robe);
        g.fillRoundRect((int) x - 13, (int) sh, 26, (int) (hip - sh + 26), 12, 12);
        limb(g, x - 10, sh + 8, x - 24, sh + 30, 5, robe.darker());
        limb(g, x + 10, sh + 8, x + 24, sh + 30, 5, robe);
        g.setColor(new Color(238, 210, 174));
        g.fillOval((int) x - 10, (int) sh - 21, 20, 21);
        g.setColor(new Color(210, 220, 178));
        g.fillArc((int) x - 12, (int) sh - 27, 24, 19, 0, 180);
        g.setColor(new Color(70, 80, 56));
        g.drawLine((int) x - 7, (int) sh - 13, (int) x + 7, (int) sh - 13);
    }
}
