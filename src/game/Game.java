package game;

import javax.swing.*;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferStrategy;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Game extends Canvas {
    public static final int VIEW_W = 1280, VIEW_H = 720;
    public static float time;
    public static Input input = new Input();
    public static Profile profile = new Profile();
    public static Glow glow = new Glow();
    public static boolean CHEATS_ENABLED = true;
    private static final ArrayList<String> LOG = new ArrayList<>();

    public static void log(String line) {
        while (LOG.size() >= 20) LOG.remove(0);
        LOG.add(line);
    }

    public static List<String> logLines() {
        return Collections.unmodifiableList(LOG);
    }

    private State state;
    private BufferedImage screen;
    private volatile boolean running;
    private float fade;
    private JFrame frame;

    public Game() {
        instance = this;
        setFocusable(true);
        addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) e.consume();
                input.press(e.getKeyCode());
            }

            @Override
            public void keyReleased(KeyEvent e) {
                input.release(e.getKeyCode());
            }
        });
        screen = new BufferedImage(VIEW_W, VIEW_H, BufferedImage.TYPE_INT_RGB);
    }

    public void start() {
        frame = new JFrame("Demon Slayer: Blades of the Night");
        frame.setLayout(new BorderLayout());
        frame.add(this, BorderLayout.CENTER);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(VIEW_W + frame.getInsets().left + frame.getInsets().right,
                VIEW_H + frame.getInsets().top + frame.getInsets().bottom);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        createBufferStrategy(2);
        requestFocusInWindow();
        running = true;
        change(State.title(this));
        Thread loop = new Thread(this::run, "game-loop");
        loop.start();
    }

    public static void change(State s) {
        Game g = instance;
        if (g == null) return;
        g.fade = 1;
        g.state = s;
        s.enter();
    }

    private static Game instance;

    private void run() {
        final float STEP = 1 / 60f;
        long prev = System.nanoTime();
        float acc = 0;
        while (running) {
            long now = System.nanoTime();
            acc += (now - prev) / 1e9f;
            prev = now;
            if (acc > 0.25f) acc = 0.25f;
            boolean updated = false;
            while (acc >= STEP) {
                time += STEP;
                fade = Math.max(0, fade - STEP * 3.2f);
                state.update();
                input.endFrame();
                acc -= STEP;
                updated = true;
            }
            if (updated) draw();
            try {
                Thread.sleep(2);
            } catch (InterruptedException ignored) {
            }
        }
    }

    private void draw() {
        BufferStrategy bs = getBufferStrategy();
        if (bs == null) return;
        Graphics2D g = screen.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        state.render(g);
        if (fade > 0) {
            g.setColor(new Color(4, 4, 8, (int) (255 * fade)));
            g.fillRect(0, 0, VIEW_W, VIEW_H);
        }
        g.dispose();

        do {
            do {
                Graphics2D sg = (Graphics2D) bs.getDrawGraphics();
                int cw = getWidth(), chh = getHeight();
                sg.setColor(Color.BLACK);
                sg.fillRect(0, 0, cw, chh);
                float scale = Math.min(cw / (float) VIEW_W, chh / (float) VIEW_H);
                int dw = (int) (VIEW_W * scale), dh = (int) (VIEW_H * scale);
                sg.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                sg.drawImage(screen, (cw - dw) / 2, (chh - dh) / 2, dw, dh, null);
                sg.dispose();
            } while (bs.contentsRestored());
            bs.show();
        } while (bs.contentsLost());
    }
}
