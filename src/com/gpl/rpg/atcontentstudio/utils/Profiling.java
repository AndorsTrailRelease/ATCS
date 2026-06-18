package com.gpl.rpg.atcontentstudio.utils;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;

public final class Profiling {

    public static final boolean LOAD = Boolean.getBoolean("atcs.profile.load");
    public static final boolean LINK = Boolean.getBoolean("atcs.profile.link");
    public static final boolean VERBOSE = Boolean.getBoolean("atcs.profile.verbose");
    private static final ThreadLocal<Integer> INDENT_LEVEL = ThreadLocal.withInitial(() -> 0);

    private Profiling() {
    }

    /**
     * Runs the given action and, when enabled, prints the elapsed time to stdout.
     *
     * @param label a short human-readable description of the work being timed
     * @param enabled whether timing output should be emitted
     * @param action the work to execute
     */
    public static void run(String label, boolean enabled, Runnable action) {
        long start = enabled ? System.nanoTime() : 0L;
        increaseIndent();
        action.run();
        if (enabled) {
            printf("%s took %d ms", label, elapsedMillis(start));
        }
        decreaseIndent();
    }


    public static void increaseIndent() {
        INDENT_LEVEL.set(INDENT_LEVEL.get() + 1);
    }

    public static void decreaseIndent() {
        INDENT_LEVEL.set(INDENT_LEVEL.get() - 1);
        if(INDENT_LEVEL.get() < 0) INDENT_LEVEL.set(0);
    }

    /**
     * Prints a profiling line using the current indentation level.
     *
     * @param format printf-style format string
     * @param args format arguments
     */
    public static void printf(String format, Object... args) {
        System.out.printf(indent() + format + "%n", args);
    }

    /**
     * Runs the given action for each item in the collection and prints timing output.
     * When verbose profiling is enabled, each item is timed individually.
     *
     * @param groupLabel label used for the summary line and optional per-item lines
     * @param items items to process
     * @param enabled whether the summary timing output should be emitted
     * @param itemLabel function producing a readable label for each item
     * @param action work to execute for each item
     * @param <T> item type
     * @return number of processed items
     */
    public static <T> int runEach(String groupLabel, Collection<T> items, boolean enabled, Function<T, String> itemLabel, Consumer<T> action) {
        long start = enabled ? System.nanoTime() : 0L;
        int count = 0;
        for (T item : items) {
            if (VERBOSE) {
                long itemStart = System.nanoTime();
                action.accept(item);
                printf("%s %s in %d ms", groupLabel, itemLabel.apply(item), elapsedMillis(itemStart));
            } else {
                action.accept(item);
            }
            count++;
        }
        if (enabled) {
            printf("%s: %d items in %d ms", groupLabel, count, elapsedMillis(start));
        }
        return count;
    }

    /**
     * Runs the given action for each item in the collection in parallel and prints timing output.
     * Use this only when the supplied action is thread-safe.
     *
     * @param groupLabel label used for the summary line and optional per-item lines
     * @param items items to process
     * @param enabled whether the summary timing output should be emitted
     * @param itemLabel function producing a readable label for each item
     * @param action work to execute for each item
     * @param <T> item type
     * @return number of processed items
     */
    public static <T> int runEachParallel(String groupLabel, Collection<T> items, boolean enabled, Function<T, String> itemLabel, Consumer<T> action) {
        long start = enabled ? System.nanoTime() : 0L;
        String prefix = indent();
        items.parallelStream().forEach(item -> {
            if (VERBOSE) {
                long itemStart = System.nanoTime();
                action.accept(item);
                System.out.printf(prefix + "%s %s in %d ms%n", groupLabel, itemLabel.apply(item), elapsedMillis(itemStart));
            } else {
                action.accept(item);
            }
        });
        if (enabled) {
            System.out.printf(prefix + "%s: %d items in %d ms%n", groupLabel, items.size(), elapsedMillis(start));
        }
        return items.size();
    }

    /**
     * Converts a start time captured from {@link System#nanoTime()} into elapsed milliseconds.
     *
     * @param startNanos start timestamp in nanoseconds
     * @return elapsed time in milliseconds
     */
    public static long elapsedMillis(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000L;
    }

    private static String indent() {
        int level = INDENT_LEVEL.get();
        if (level <= 0) return "";
        return "  ".repeat(level);
    }
}


