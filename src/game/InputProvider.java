package game;

public interface InputProvider {
    boolean down(int keyCode);

    boolean pressed(int keyCode);

    boolean anyPressed(int... keyCodes);
}
