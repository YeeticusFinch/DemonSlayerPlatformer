package game;

import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Input implements InputProvider {
    public static final int LEFT = KeyEvent.VK_A;
    public static final int RIGHT = KeyEvent.VK_D;
    public static final int JUMP = KeyEvent.VK_W;
    public static final int DOWN = KeyEvent.VK_S;
    public static final int DASH = KeyEvent.VK_SHIFT;
    public static final int ATTACK = KeyEvent.VK_J;
    public static final int CAST = KeyEvent.VK_F;
    public static final int GUARD = KeyEvent.VK_K;
    public static final int RECHARGE = KeyEvent.VK_L;
    public static final int NEXT = KeyEvent.VK_E;
    public static final int PREV = KeyEvent.VK_Q;
    public static final int HELP = KeyEvent.VK_H;
    public static final int PAUSE = KeyEvent.VK_ESCAPE;
    public static final int CONFIRM = KeyEvent.VK_ENTER;
    public static final int RETRY = KeyEvent.VK_R;
    public static final int SLOT1 = KeyEvent.VK_1;
    public static final int SLOT2 = KeyEvent.VK_2;
    public static final int SLOT3 = KeyEvent.VK_3;
    public static final int SLOT4 = KeyEvent.VK_4;
    public static final int SLOT5 = KeyEvent.VK_5;
    public static final int SLOT6 = KeyEvent.VK_6;
    public static final int SLOT7 = KeyEvent.VK_7;

    private final Map<Integer, Boolean> down = new HashMap<>();
    private final Set<Integer> pressed = new HashSet<>();

    public void press(int kc) {
        if (!Boolean.TRUE.equals(down.get(kc))) pressed.add(kc);
        down.put(kc, true);
    }

    public void release(int kc) { down.put(kc, false); }

    public void endFrame() { pressed.clear(); }

    @Override
    public boolean down(int kc) { return Boolean.TRUE.equals(down.get(kc)); }

    @Override
    public boolean pressed(int kc) { return pressed.contains(kc); }

    @Override
    public boolean anyPressed(int... kcs) {
        for (int k : kcs) if (pressed.contains(k)) return true;
        return false;
    }
}
