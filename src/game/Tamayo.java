package game;

import java.awt.*;

public class Tamayo extends Civilian {
    public Tamayo() {
        name = "Tamayo";
        maxHp = hp = 120;
    }

    @Override
    public boolean hurt(World world, Fighter src, float dmgAmt, float dir, float kbX, float kbY) { return false; }

    @Override
    protected void renderBody(Graphics2D g) {
        float feet = bottom(), sh = feet - 54, hip = feet - 24;
        g.setColor(new Color(0, 0, 0, 45));
        g.fillOval((int) x - 22, (int) feet - 5, 44, 8);
        Color kimono = new Color(64, 62, 126);
        g.setColor(kimono);
        g.fillRoundRect((int) x - 15, (int) sh, 30, (int) (hip - sh + 28), 12, 12);
        g.setColor(new Color(235, 210, 230));
        for (int i = 0; i < 7; i++) {
            float px = x - 11 + (i % 3) * 10;
            float py = sh + 8 + i * 8;
            g.fillOval((int) px - 3, (int) py - 3, 6, 6);
            g.setColor(new Color(250, 225, 170));
            g.fillOval((int) px - 1, (int) py - 1, 2, 2);
            g.setColor(new Color(235, 210, 230));
        }
        limb(g, x - 12, sh + 8, x - 24, sh + 34, 5, kimono.darker());
        limb(g, x + 12, sh + 8, x + 24, sh + 34, 5, kimono);
        g.setColor(new Color(236, 204, 170));
        g.fillOval((int) x - 10, (int) sh - 21, 20, 21);
        g.setColor(new Color(32, 26, 38));
        g.fillArc((int) x - 12, (int) sh - 25, 24, 16, 0, 180);
        g.fillOval((int) x - 8, (int) sh - 34, 16, 14);
        g.setStroke(new BasicStroke(2f));
        g.setColor(new Color(220, 190, 120));
        g.drawLine((int) x - 16, (int) sh - 30, (int) x + 16, (int) sh - 38);
        g.setColor(new Color(120, 20, 40));
        g.fillOval((int) x - 4, (int) sh - 12, 3, 3);
        g.fillOval((int) x + 3, (int) sh - 12, 3, 3);
    }
}
