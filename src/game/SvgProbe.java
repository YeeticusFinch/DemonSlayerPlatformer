package game;

public class SvgProbe {
    public static void main(String[] args) {
        Svg s = Svg.load("assets/rui.svg");
        System.out.println("loaded=" + (s != null));
        if (s == null) return;
        for (String n : new String[]{"head", "torso", "arm-back", "arm-front", "leg-back", "leg-front", "eye-glow"}) {
            Svg.Group g = s.group(n);
            System.out.println(n + " -> " + (g != null ? g.img.getWidth() + "x" + g.img.getHeight() : "MISSING"));
        }
    }
}
