package game;

import java.awt.*;

public class Ghost {
    public float x, y, h, life, maxLife;
    public int facing;
    public Profile.Path path;
    public Profile.Style style;
    public Color tint;

    public Ghost(float x, float feetY, float h, Profile.Path path, Profile.Style style, int facing) {
        this.x = x;
        this.y = feetY;
        this.h = h;
        this.path = path;
        this.style = style;
        this.facing = facing;
        this.life = this.maxLife = 0.28f;
    }

    public Ghost(float x, float feetY, float h, Color tint) {
        this(x, feetY, h, Profile.Path.SLAYER, Profile.Style.WATER, 1);
        this.tint = tint;
        this.life = this.maxLife = 0.32f;
    }

    public void update(float dt) { life -= dt; }

    public boolean dead() { return life <= 0; }

    public void render(Graphics2D g) {
        float t = life / maxLife;
        Color c = tint != null ? new Color(tint.getRed(), tint.getGreen(), tint.getBlue(), (int) (110 * t))
                : switch (path) {
            case DEMON -> new Color(255, 70, 95, (int) (110 * t));
            case SLAYER -> style == Profile.Style.FLAME
                    ? new Color(255, 130, 50, (int) (110 * t))
                    : style == Profile.Style.WIND ? new Color(90, 220, 130, (int) (110 * t))
                    : new Color(90, 170, 255, (int) (110 * t));
        };
        g.setColor(c);
        // humanoid silhouette: head, torso, arms, legs
        float feet = y;
        float headR = h * 0.105f;
        float hipY = feet - h * 0.44f;
        float shY = feet - h * 0.78f;
        float halfW = h * 0.115f;
        g.setStroke(new BasicStroke(h * 0.075f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        // legs (running pose)
        g.drawLine((int) (x - 3), (int) hipY, (int) (x - 4 - facing * h * 0.06f), (int) (feet - 2));
        g.drawLine((int) (x + 3), (int) hipY, (int) (x + 4 + facing * h * 0.10f), (int) (feet - h * 0.06f));
        // arms (swung forward/back)
        g.drawLine((int) (x - facing * halfW), (int) (shY + 4), (int) (x - facing * h * 0.16f), (int) (shY + h * 0.22f));
        g.drawLine((int) (x + facing * halfW), (int) (shY + 4), (int) (x + facing * h * 0.15f), (int) (shY + h * 0.30f));
        // torso
        g.fillRoundRect((int) (x - halfW), (int) shY, (int) (halfW * 2), (int) (hipY - shY + 5), 6, 6);
        // head
        g.fillOval((int) (x + facing * 1.5f - headR), (int) (shY - headR * 2.05f), (int) (headR * 2), (int) (headR * 2.1f));
    }
}
