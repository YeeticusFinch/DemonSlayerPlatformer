package game;

import java.util.concurrent.ThreadLocalRandom;

public final class FMath {
    public static float clamp(float v, float a, float b) { return v < a ? a : (v > b ? b : v); }
    public static float lerp(float a, float b, float t) { return a + (b - a) * t; }
    public static float approach(float v, float target, float delta) {
        if (v < target) return Math.min(v + delta, target);
        if (v > target) return Math.max(v - delta, target);
        return target;
    }
    public static float sin(float a) { return (float) Math.sin(a); }
    public static float cos(float a) { return (float) Math.cos(a); }
    public static float rand(float min, float max) { return (float) ThreadLocalRandom.current().nextDouble(min, max); }
    public static int randInt(int min, int max) { return ThreadLocalRandom.current().nextInt(min, max + 1); }
    public static boolean chance(float p) { return ThreadLocalRandom.current().nextFloat() < p; }
    public static float dist(float x1, float y1, float x2, float y2) {
        float dx = x2 - x1, dy = y2 - y1;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }
    public static float easeOut(float t) { t = clamp(t, 0, 1); return 1 - (1 - t) * (1 - t); }
    public static float easeIn(float t) { t = clamp(t, 0, 1); return t * t; }
    public static float signum(float v) { return v > 0 ? 1 : v < 0 ? -1 : 0; }
    public static float easeInOut(float t) {
        t = clamp(t, 0, 1);
        return t < 0.5f ? 2 * t * t : 1 - (float) Math.pow(-2 * t + 2, 2) / 2;
    }
    public static float noise(float t) {
        return (sin(t) * 0.55f + sin(t * 2.17f + 1.3f) * 0.3f + sin(t * 4.7f + 2.1f) * 0.15f + 1f) * 0.5f;
    }
}
