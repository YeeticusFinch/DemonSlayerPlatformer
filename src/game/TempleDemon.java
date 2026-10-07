package game;

import java.awt.*;

public class TempleDemon extends SwampDemon {
    private final java.util.ArrayList<Ability> templeArts = Ability.crimsonHunger();
    private int artIdx;
    private float templeCd = 1.4f;

    public TempleDemon() {
        super(true);
        forms = Ability.crimsonHunger();
        name = "Temple Demon";
        maxHp = hp = 190;
        runSpeed = 150;
        aggroR = 660;
        dmg = 13;
        isBoss = true;
        eliteColor = AbilityCast.BLOOD;
    }

    @Override
    protected void think(World w, float dt, float dx, float dist) {
        templeCd -= dt;
        if ((state == 0 || state == 1) && templeCd <= 0 && dist < 610 && Math.abs(w.player.y - y) < 250) {
            face(dx != 0 ? dx : vx);
            beginWindup();
            windup = 0.48f;
            templeCd = FMath.rand(2.2f, 3.5f);
            return;
        }
        if (state == 2 && stateT >= windup) {
            state = 3;
            stateT = 0;
            recover = 0.55f;
            w.castAbility(this, templeArts.get(artIdx++ % templeArts.size()));
            return;
        }
        super.think(w, dt, dx, dist);
    }

    @Override
    protected Color abilityColor() { return AbilityCast.BLOOD; }

    @Override
    protected void renderBody(Graphics2D g) {
        drawSwampBody(g, new Color(242, 242, 234), new Color(42, 84, 150), new Color(18, 86, 54), false, 1.12f);
        g.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(new Color(28, 150, 82));
        g.drawLine((int) x - 22, (int) (y - h * 0.2f), (int) x - 32, (int) (y + h * 0.1f));
        g.drawLine((int) x + 22, (int) (y - h * 0.2f), (int) x + 32, (int) (y + h * 0.1f));
    }
}
