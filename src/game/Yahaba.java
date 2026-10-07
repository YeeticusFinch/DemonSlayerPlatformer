package game;

import java.awt.*;

public class Yahaba extends Enemy {
    private final java.util.ArrayList<Ability> forms = Ability.yahabaArt();
    private int formIdx;
    private float castCd = 1.4f;

    public Yahaba() {
        super(32, 84);
        team = 1;
        isDemon = true;
        isBoss = true;
        name = "Yahaba";
        maxHp = hp = 130;
        runSpeed = 165;
        aggroR = 820;
        atkRange = 70;
        dmg = 10;
        elite = true;
        eliteColor = new Color(210, 40, 55);
    }

    @Override
    protected void think(World w, float dt, float dx, float dist) {
        castCd -= dt;
        if ((state == 0 || state == 1) && castCd <= 0 && dist < 780 && Math.abs(w.player.y - y) < 360) {
            face(dx != 0 ? dx : vx);
            beginWindup();
            windup = 0.44f;
            castCd = FMath.rand(1.4f, 2.4f);
            return;
        }
        if (state == 2 && stateT >= windup) {
            state = 3;
            stateT = 0;
            recover = 0.46f;
            w.castAbility(this, forms.get(formIdx++ % forms.size()));
            return;
        }
        super.think(w, dt, dx, dist);
    }

    @Override
    protected void strike(World w) { basicStrike(w, 70, 190, 120, -8, new Color(210, 40, 55)); }

    @Override
    protected Color abilityColor() { return new Color(210, 40, 55); }

    @Override
    public void update(World w, float dt) {
        super.update(w, dt);
        demonRegen(w, dt, 2.4f);
    }

    @Override
    protected void renderBody(Graphics2D g) {
        g.setColor(new Color(0, 0, 0, 58));
        g.fillOval((int) x - 26, (int) bottom() - 5, 52, 9);
        float feet = bottom(), sh = feet - 56, hip = feet - 25;
        Color skin = new Color(230, 218, 205), robe = new Color(84, 78, 92);
        limb(g, x - 4, hip, x - 7, feet - 4, 7, robe.darker());
        limb(g, x + 4, hip, x + 7, feet - 4, 7, robe);
        g.setColor(robe);
        g.fillRoundRect((int) x - 13, (int) sh, 26, (int) (hip - sh + 10), 12, 12);
        limb(g, x - facing() * 8, sh + 8, x - facing() * 25, sh + 28, 5, skin.darker());
        limb(g, x + facing() * 8, sh + 8, x + facing() * 25, sh + 18, 5, skin);
        g.setColor(skin);
        g.fillOval((int) x - 11, (int) sh - 22, 22, 22);
        g.setColor(new Color(24, 22, 28));
        g.fillArc((int) x - 12, (int) sh - 25, 24, 16, 0, 180);
        g.setColor(new Color(210, 40, 55));
        g.fillPolygon(new int[]{(int) x - 7, (int) x - 1, (int) x - 7}, new int[]{(int) sh - 12, (int) sh - 9, (int) sh - 6}, 3);
        g.fillPolygon(new int[]{(int) x + 7, (int) x + 1, (int) x + 7}, new int[]{(int) sh - 12, (int) sh - 9, (int) sh - 6}, 3);
    }
}
