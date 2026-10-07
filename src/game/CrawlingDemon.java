package game;

import java.awt.*;

public class CrawlingDemon extends Enemy {
    private final Color skin = new Color(112, 96, 128);
    private final Color dark = new Color(48, 34, 62);

    public CrawlingDemon() {
        super(44, 42);
        team = 1;
        isDemon = true;
        name = "Crawling Demon";
        maxHp = hp = 46;
        runSpeed = 230;
        aggroR = 620;
        atkRange = 78;
        windup = 0.36f;
        recover = 0.5f;
        atkCdBase = 1.25f;
        dmg = 10;
    }

    @Override
    protected float contactDamage() { return 4; }

    @Override
    protected float chaseSpeed() { return runSpeed * 1.45f; }

    @Override
    protected void strike(World w) {
        basicStrike(w, 86, 230, 90, -8, new Color(215, 60, 105));
    }

    @Override
    public void update(World w, float dt) {
        super.update(w, dt);
        demonRegen(w, dt, 2.1f);
    }

    @Override
    protected void renderBody(Graphics2D g) {
        g.setColor(new Color(0, 0, 0, 60));
        g.fillOval((int) x - 30, (int) bottom() - 5, 60, 9);
        Composite old = g.getComposite();
        if (flashT > 0) g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.86f));
        float ph = animPhase * 6.28f;
        float cy = bottom() - 22;
        g.setColor(skin);
        g.fillOval((int) x - 22, (int) cy - 14, 44, 24);
        g.setColor(dark);
        g.fillOval((int) (x + facing() * 11) - 12, (int) cy - 22, 24, 22);
        for (int i = -1; i <= 1; i += 2) {
            float sx = x + i * 12;
            float sw = FMath.sin(ph + i) * 8;
            limb(g, sx, cy + 2, sx + i * (18 + sw), bottom() - 4, 4, dark);
            limb(g, sx, cy - 2, sx + i * (16 - sw), bottom() - 13, 4, skin.darker());
        }
        g.setColor(new Color(255, 220, 70));
        g.fillOval((int) (x + facing() * 14) - 3, (int) cy - 17, 6, 4);
        g.setStroke(new BasicStroke(2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(new Color(210, 45, 80));
        float tx = x + facing() * (attacking() ? 38 : 22);
        g.drawLine((int) (x + facing() * 18), (int) cy - 7, (int) tx, (int) cy - 4);
        g.setComposite(old);
    }
}
