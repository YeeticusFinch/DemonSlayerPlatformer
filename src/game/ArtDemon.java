package game;

import java.awt.*;

public class ArtDemon extends BasicDemon {
    private float castCd = 2f;
    private final java.util.ArrayList<Ability> forms = Ability.crimsonHunger();
    private int formIdx;

    public ArtDemon() {
        super(38, 86, 62, 155, 11);
        name = "Blood Art Demon";
        atkRange = 70;
        elite = true;
        eliteColor = AbilityCast.BLOOD;
    }

    @Override
    protected void think(World w, float dt, float dx, float dist) {
        castCd -= dt;
        Player pl = w.player;
        if ((state == 0 || state == 1) && castCd <= 0 && !pl.dead && dist < 620 && Math.abs(pl.y - y) < 260) {
            face(dx != 0 ? dx : vx);
            beginWindup();
            windup = 0.5f;
            castCd = FMath.rand(2.6f, 3.8f);
            return;
        }
        if (state == 2 && stateT >= windup) {
            state = 3;
            stateT = 0;
            recover = 0.5f;
            castArt(w, pl, dist);
            atkCd = atkCdBase * armCdMult();
            return;
        }
        super.think(w, dt, dx, dist);
    }

    private void castArt(World w, Player pl, float dist) {
        noteAction();
        w.castAbility(this, forms.get(formIdx++ % forms.size()));
    }

    @Override
    protected void renderBody(Graphics2D g) {
        super.renderBody(g);
        Composite old = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f + 0.25f * FMath.sin(Game.time * 5)));
        g.setColor(new Color(190, 20, 45));
        g.fillRect((int) (x - 6), (int) (top() + h * 0.34f), 12, 2);
        g.fillRect((int) (x - 3), (int) (top() + h * 0.42f), 6, 2);
        g.setComposite(old);
    }
}
