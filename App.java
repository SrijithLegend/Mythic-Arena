public class App {
    public static void main(String[] args) {
        stats starter = new stats(45, 49, 49, 45, 65);
        System.out.println("Created stats: " + starter);

        level lv = new level(5);
        System.out.println("Level " + lv.getLevel() + " total: " + lv.getTotalStats());

        assert starter.getTotalStats() == 253 : "stat total";
        assert new level().getLevel() == 1 : "default level";
        assert new level(1).getTotalStats() == 100 : "level 1 total";
        assert lv.getTotalStats() == 140 : "level 5 total";
        assertThrows(() -> new stats(0, 49, 49, 45, 65), "zero stat");
        assertThrows(() -> new level(0), "level below min");
        assertThrows(() -> new level(101), "level above max");
        System.out.println("checks passed"); 
    }

    private static void assertThrows(Runnable r, String what) {
        try {
            r.run();
            throw new AssertionError("expected IllegalArgumentException: " + what);
        } catch (IllegalArgumentException expected) {
            // ok
        }
    }
}
