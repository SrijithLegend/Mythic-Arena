public class level {

    private static final int MIN_LEVEL = 1;
    private static final int MAX_LEVEL = 100;

    private int level;
    private int total_stats;

    public level() {
        this(MIN_LEVEL);
    }

    public level(int level) {
        if (level < MIN_LEVEL || level > MAX_LEVEL) {
            throw new IllegalArgumentException(
                "Level must be between " + MIN_LEVEL + " and " + MAX_LEVEL + ".");
        }

        this.level = level;
        this.total_stats = 100 + 10 * (level - MIN_LEVEL);
    }

    public int getLevel() {
        return level;
    }

    public int getTotalStats() {
        return total_stats;
    }
}
