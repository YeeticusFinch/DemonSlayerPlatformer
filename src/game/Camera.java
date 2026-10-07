package game;

public class Camera {
    public float x, y;
    private float shakeT, shakeMag;

    public void follow(float tx, float ty, float worldW, float worldH, float viewW, float viewH, float dt) {
        float nx = FMath.lerp(x, tx - viewW / 2f, 1 - (float) Math.pow(0.0008, dt));
        float ny = FMath.lerp(y, ty - viewH / 2f - 60, 1 - (float) Math.pow(0.002, dt));
        x = FMath.clamp(nx, 0, Math.max(0, worldW - viewW));
        y = FMath.clamp(ny, 0, Math.max(0, worldH - viewH));
    }

    public void snap(float cx, float cy, float worldW, float worldH, float viewW, float viewH) {
        x = FMath.clamp(cx - viewW / 2f, 0, Math.max(0, worldW - viewW));
        y = FMath.clamp(cy - viewH / 2f - 60, 0, Math.max(0, worldH - viewH));
    }

    public void shake(float mag, float time) {
        shakeMag = Math.max(shakeMag, mag);
        shakeT = Math.max(shakeT, time);
    }

    public void update(float dt) {
        if (shakeT > 0) {
            shakeT -= dt;
            if (shakeT <= 0) shakeMag = 0;
            else shakeMag *= (float) Math.pow(0.02, dt);
        }
    }

    public float offX() { return shakeT > 0 ? FMath.rand(-shakeMag, shakeMag) : 0; }
    public float offY() { return shakeT > 0 ? FMath.rand(-shakeMag, shakeMag) : 0; }
}
