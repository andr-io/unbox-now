package com;

import java.util.List;

public class Main {
    private static final String ANSI_RESET  = "\u001B[0m";
    private static final String ANSI_GREEN  = "\u001B[32m";
    private static final String ANSI_RED    = "\u001B[31m";

    public static void main(String[] args) {
        run("Two slim boxes fit in 10x10x10",
            new Box(10, 10, 10),
            List.of(new Box(9, 1, 1), new Box(9, 1, 1)),
            true);

        run("Too much volume, should fail",
            new Box(3, 3, 3), // volume 27
            List.of(new Box(3, 3, 3), new Box(1, 1, 1)), // total volume 28 > 27
            false);

        run("Individually too big, should fail early",
            new Box(10, 5, 5),
            List.of(new Box(9, 9, 1)), // sorted (1,9,9) vs container (5,5,10) -> 9 > 5
            false);

        run("Three cubes 3x3x3 in 5x5x5, should NOT fit",
            new Box(5, 5, 5),
            List.of(new Box(3, 3, 3), new Box(3, 3, 3), new Box(3, 3, 3)),
            false);

        run("Two 5x5x5 in 10x5x5, should fit",
            new Box(10, 5, 5),
            List.of(new Box(5, 5, 5), new Box(5, 5, 5)),
            true);

        run("Tight fit with rotations required",
            new Box(6, 4, 4),
            List.of(new Box(4, 4, 2), new Box(4, 2, 4)),
            true);

        run("Does not fit due to dimensions even though volume is fine",
            new Box(6, 4, 4),
            List.of(new Box(5, 4, 4), new Box(2, 4, 4)),
            false);

        run("Early min-stack accept: many thin slices",
            new Box(10, 5, 5),
            List.of(new Box(5, 5, 1), new Box(5, 5, 1), new Box(5, 5, 1), new Box(5, 5, 1)),
            true);

        run("Multiple small boxes in a small container",
            new Box(4, 4, 4),
            List.of(new Box(2, 2, 2), new Box(2, 2, 2), new Box(2, 2, 2), new Box(2, 2, 2)),
            true);

        run("Exact fit",
            new Box(3, 3, 3),
            List.of(new Box(3, 1, 1), new Box(1, 1, 1), new Box(1, 1, 1), new Box(1, 1, 1),
                new Box(1, 1, 1), new Box(1, 1, 1), new Box(1, 1, 3), new Box(1, 1, 1), new Box(1, 2, 1),
                new Box(1, 1, 2), new Box(2, 1, 1), new Box(1, 3, 1), new Box(1, 2, 1), new Box(1, 2, 1)),
            true);

        run("No fit",
            new Box(3, 3, 3),
            List.of(new Box(3, 1, 1), new Box(1, 1, 1), new Box(1, 1, 1), new Box(1, 1, 1),
                new Box(1, 1, 1), new Box(1, 1, 1), new Box(1, 1, 3), new Box(1, 1, 1), new Box(1, 2, 1),
                new Box(2, 1, 2), new Box(2, 1, 1), new Box(1, 3, 1), new Box(1, 2, 1), new Box(1, 2, 1)),
            false);

        // --- Stress tests with 20 boxes ---

        run("20 small cubes in 10x10x10, should fit",
            new Box(10, 10, 10),
            List.of(
                new Box(2, 2, 2), new Box(2, 2, 2), new Box(2, 2, 2), new Box(2, 2, 2),
                new Box(2, 2, 2), new Box(2, 2, 2), new Box(2, 2, 2), new Box(2, 2, 2),
                new Box(2, 2, 2), new Box(2, 2, 2), new Box(2, 2, 2), new Box(2, 2, 2),
                new Box(2, 2, 2), new Box(2, 2, 2), new Box(2, 2, 2), new Box(2, 2, 2),
                new Box(2, 2, 2), new Box(2, 2, 2), new Box(2, 2, 2), new Box(2, 2, 2)
            ),
            true);

        run("20 medium cubes in 10x10x10, should fit",
            new Box(10, 10, 10),
            List.of(
                new Box(3, 3, 3), new Box(3, 3, 3), new Box(3, 3, 3), new Box(3, 3, 3),
                new Box(3, 3, 3), new Box(3, 3, 3), new Box(3, 3, 3), new Box(3, 3, 3),
                new Box(3, 3, 3), new Box(3, 3, 3), new Box(3, 3, 3), new Box(3, 3, 3),
                new Box(3, 3, 3), new Box(3, 3, 3), new Box(3, 3, 3), new Box(3, 3, 3),
                new Box(3, 3, 3), new Box(3, 3, 3), new Box(3, 3, 3), new Box(3, 3, 3)
            ),
            true);

        run("20 thin slices in 20x5x5, should fit by stacking",
            new Box(20, 5, 5),
            List.of(
                new Box(5, 5, 1), new Box(5, 5, 1), new Box(5, 5, 1), new Box(5, 5, 1),
                new Box(5, 5, 1), new Box(5, 5, 1), new Box(5, 5, 1), new Box(5, 5, 1),
                new Box(5, 5, 1), new Box(5, 5, 1), new Box(5, 5, 1), new Box(5, 5, 1),
                new Box(5, 5, 1), new Box(5, 5, 1), new Box(5, 5, 1), new Box(5, 5, 1),
                new Box(5, 5, 1), new Box(5, 5, 1), new Box(5, 5, 1), new Box(5, 5, 1)
            ),
            true);

        run("20 large boxes in 50x50x50, should fit easily",
            new Box(50, 50, 50),
            List.of(
                new Box(10, 10, 10), new Box(10, 10, 10), new Box(10, 10, 10), new Box(10, 10, 10),
                new Box(10, 10, 10), new Box(10, 10, 10), new Box(10, 10, 10), new Box(10, 10, 10),
                new Box(10, 10, 10), new Box(10, 10, 10), new Box(10, 10, 10), new Box(10, 10, 10),
                new Box(10, 10, 10), new Box(10, 10, 10), new Box(10, 10, 10), new Box(10, 10, 10),
                new Box(10, 10, 10), new Box(10, 10, 10), new Box(10, 10, 10), new Box(10, 10, 10)
            ),
            true);

        run("20  boxes in 20x20x20, should NOT fit",
            new Box(20, 20, 20),
            List.of(
                new Box(7, 7, 7), new Box(7, 7, 7), new Box(7, 7, 7), new Box(7, 7, 7),
                new Box(7, 7, 7), new Box(7, 7, 7), new Box(7, 7, 7), new Box(7, 7, 7),
                new Box(7, 7, 7), new Box(7, 7, 7), new Box(7, 7, 7), new Box(7, 7, 7),
                new Box(7, 7, 7), new Box(7, 7, 7), new Box(7, 7, 7), new Box(7, 7, 7),
                new Box(7, 7, 7), new Box(7, 7, 7), new Box(7, 7, 7), new Box(7, 7, 7)
            ),
            false);

    }

    private static void run(String name, Box container, List<Box> items, boolean expected) {
        long start = System.nanoTime();

        boolean result = container.canTheseFitIn(items);

        long end = System.nanoTime();
        long elapsedMs = (end - start) / 1_000_000; // convert to milliseconds

        boolean ok = (result == expected);
        String verdict = ok ? ANSI_GREEN + "OK" + ANSI_RESET
            : ANSI_RED   + "Mismatch" + ANSI_RESET;

        System.out.printf("[%s] -> result=%s, expected=%s => %s (time=%d ms)%n",
            name, result, expected, verdict, elapsedMs);
    }

}
