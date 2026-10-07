package game;

import java.awt.*;

public abstract class Entity {
    public float x, y, w, h, vx, vy;
    public boolean onGround, dead, facingRight = true, isDemon;
    public int team;
    public float flashT;

    public Entity(float w, float h) {
        this.w = w;
        this.h = h;
    }

    public float left() { return x - w / 2f; }
    public float right() { return x + w / 2f; }
    public float top() { return y - h / 2f; }
    public float bottom() { return y + h / 2f; }

    public boolean overlaps(Entity o) {
        return left() < o.right() && right() > o.left() && top() < o.bottom() && bottom() > o.top();
    }

    public boolean overlapsRect(float rx, float ry, float rw, float rh) {
        return left() < rx + rw && right() > rx && top() < ry + rh && bottom() > ry;
    }

    public boolean containsPoint(float px, float py) {
        return px >= left() && px <= right() && py >= top() && py <= bottom();
    }

    public abstract void update(World world, float dt);

    public abstract void render(Graphics2D g);
}
