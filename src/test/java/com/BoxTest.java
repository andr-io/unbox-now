package com;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BoxTest {

    @Test
    void testSimpleFit() {
        Box container = new Box(5, 5, 5);

        List<Box> items = List.of(
            new Box(1, 1, 1), new Box(1, 1, 1), new Box(1, 1, 1),
            new Box(1, 1, 1), new Box(1, 1, 1), new Box(1, 1, 1),
            new Box(1, 1, 1), new Box(1, 1, 1), new Box(1, 1, 3)
        );

        assertTrue(container.canTheseFitIn(items));
    }

    @Test
    void testTwoSlimBoxesFit() {
        Box container = new Box(10, 10, 10);

        List<Box> items = List.of(new Box(9, 1, 1), new Box(9, 1, 1));

        assertTrue(container.canTheseFitIn(items));
    }

    @Test
    void testTooMuchVolumeFails() {
        Box container = new Box(3, 3, 3); // volume 27

        List<Box> items = List.of(new Box(3, 3, 3), new Box(1, 1, 1)); // total volume 28

        assertFalse(container.canTheseFitIn(items));
    }

    @Test
    void testIndividuallyTooBigFails() {
        Box container = new Box(10, 5, 5);

        List<Box> items = List.of(new Box(9, 9, 1));

        assertFalse(container.canTheseFitIn(items));
    }

    @Test
    void testThreeCubesDontFit() {
        Box container = new Box(5, 5, 5);

        List<Box> items = List.of(new Box(3, 3, 3), new Box(3, 3, 3), new Box(3, 3, 3));

        assertFalse(container.canTheseFitIn(items));
    }

    @Test
    void testTwoCubesFit() {
        Box container = new Box(10, 5, 5);

        List<Box> items = List.of(new Box(5, 5, 5), new Box(5, 5, 5));

        assertTrue(container.canTheseFitIn(items));
    }

    @Test
    void testTightFitWithRotation() {
        Box container = new Box(6, 4, 4);

        List<Box> items = List.of(new Box(4, 4, 2), new Box(4, 2, 4));

        assertTrue(container.canTheseFitIn(items));
    }

    @Test
    void testDimensionMismatchFails() {
        Box container = new Box(6, 4, 4);

        List<Box> items = List.of(new Box(5, 4, 4), new Box(2, 4, 4));

        assertFalse(container.canTheseFitIn(items));
    }

    @Test
    void test20SmallCubesFit() {
        Box container = new Box(10, 10, 10);

        List<Box> items = List.of(
            new Box(2, 2, 2), new Box(2, 2, 2), new Box(2, 2, 2), new Box(2, 2, 2),
            new Box(2, 2, 2), new Box(2, 2, 2), new Box(2, 2, 2), new Box(2, 2, 2),
            new Box(2, 2, 2), new Box(2, 2, 2), new Box(2, 2, 2), new Box(2, 2, 2),
            new Box(2, 2, 2), new Box(2, 2, 2), new Box(2, 2, 2), new Box(2, 2, 2),
            new Box(2, 2, 2), new Box(2, 2, 2), new Box(2, 2, 2), new Box(2, 2, 2)
        );

        assertTrue(container.canTheseFitIn(items));
    }

    @Test
    void test20MediumCubesFit() {
        Box container = new Box(10, 10, 10);

        List<Box> items = List.of(
            new Box(3, 3, 3), new Box(3, 3, 3), new Box(3, 3, 3), new Box(3, 3, 3),
            new Box(3, 3, 3), new Box(3, 3, 3), new Box(3, 3, 3), new Box(3, 3, 3),
            new Box(3, 3, 3), new Box(3, 3, 3), new Box(3, 3, 3), new Box(3, 3, 3),
            new Box(3, 3, 3), new Box(3, 3, 3), new Box(3, 3, 3), new Box(3, 3, 3),
            new Box(3, 3, 3), new Box(3, 3, 3), new Box(3, 3, 3), new Box(3, 3, 3)
        );

        assertTrue(container.canTheseFitIn(items));
    }

    @Test
    void test20ThinSlicesFit() {
        Box container = new Box(20, 5, 5);

        List<Box> items = List.of(
            new Box(5, 5, 1), new Box(5, 5, 1), new Box(5, 5, 1), new Box(5, 5, 1),
            new Box(5, 5, 1), new Box(5, 5, 1), new Box(5, 5, 1), new Box(5, 5, 1),
            new Box(5, 5, 1), new Box(5, 5, 1), new Box(5, 5, 1), new Box(5, 5, 1),
            new Box(5, 5, 1), new Box(5, 5, 1), new Box(5, 5, 1), new Box(5, 5, 1),
            new Box(5, 5, 1), new Box(5, 5, 1), new Box(5, 5, 1), new Box(5, 5, 1)
        );

        assertTrue(container.canTheseFitIn(items));
    }

    @Test
    void test20LargeBoxesFit() {
        Box container = new Box(50, 50, 50);

        List<Box> items = List.of(
            new Box(10, 10, 10), new Box(10, 10, 10), new Box(10, 10, 10), new Box(10, 10, 10),
            new Box(10, 10, 10), new Box(10, 10, 10), new Box(10, 10, 10), new Box(10, 10, 10),
            new Box(10, 10, 10), new Box(10, 10, 10), new Box(10, 10, 10), new Box(10, 10, 10),
            new Box(10, 10, 10), new Box(10, 10, 10), new Box(10, 10, 10), new Box(10, 10, 10),
            new Box(10, 10, 10), new Box(10, 10, 10), new Box(10, 10, 10), new Box(10, 10, 10)
        );

        assertTrue(container.canTheseFitIn(items));
    }

    @Test
    void test20BoxesDontFitIn20x20x20() {
        Box container = new Box(20, 20, 20);

        List<Box> items = List.of(
            new Box(7, 7, 7), new Box(7, 7, 7), new Box(7, 7, 7), new Box(7, 7, 7),
            new Box(7, 7, 7), new Box(7, 7, 7), new Box(7, 7, 7), new Box(7, 7, 7),
            new Box(7, 7, 7), new Box(7, 7, 7), new Box(7, 7, 7), new Box(7, 7, 7),
            new Box(7, 7, 7), new Box(7, 7, 7), new Box(7, 7, 7), new Box(7, 7, 7),
            new Box(7, 7, 7), new Box(7, 7, 7), new Box(7, 7, 7), new Box(7, 7, 7)
        );

        assertFalse(container.canTheseFitIn(items));
    }

    @Test
    void test20VariantBoxesFitIn20x20x20() {
        Box container = new Box(20, 20, 20);

        List<Box> items = List.of(
            new Box(7, 7, 6), new Box(7, 6, 7), new Box(6, 7, 7), new Box(6, 6, 6),
            new Box(5, 7, 7), new Box(7, 5, 7), new Box(7, 5, 7), new Box(7, 7, 5),
            new Box(5, 4, 7), new Box(6, 7, 5), new Box(6, 5, 7), new Box(7, 5, 5),
            new Box(6, 6, 7), new Box(7, 7, 7), new Box(7, 7, 7), new Box(7, 7, 7),
            new Box(7, 7, 7), new Box(7, 7, 7), new Box(7, 7, 7), new Box(7, 7, 7)
        );

        assertTrue(container.canTheseFitIn(items));
    }

    @Test
    void test20BoxesDontFitIn40x40x40() {
        Box container = new Box(40, 40, 40);

        List<Box> items = List.of(
            new Box(14, 14, 14), new Box(14, 14, 14), new Box(14, 14, 14), new Box(14, 14, 14),
            new Box(14, 14, 14), new Box(14, 14, 14), new Box(14, 14, 14), new Box(14, 14, 14),
            new Box(14, 14, 14), new Box(14, 14, 14), new Box(14, 14, 14), new Box(14, 14, 14),
            new Box(14, 14, 14), new Box(14, 14, 14), new Box(14, 14, 14), new Box(14, 14, 14),
            new Box(14, 14, 14), new Box(14, 14, 14), new Box(14, 14, 14), new Box(14, 14, 14)
        );

        assertFalse(container.canTheseFitIn(items));
    }

    @Test
    void testEarlyMinStackAccept() {
        Box container = new Box(10, 5, 5);

        List<Box> items = List.of(
            new Box(5, 5, 1), new Box(5, 5, 1),
            new Box(5, 5, 1), new Box(5, 5, 1)
        );

        assertTrue(container.canTheseFitIn(items));
    }

    @Test
    void testMultipleSmallBoxesFit() {
        Box container = new Box(4, 4, 4);

        List<Box> items = List.of(
            new Box(2, 2, 2), new Box(2, 2, 2),
            new Box(2, 2, 2), new Box(2, 2, 2)
        );

        assertTrue(container.canTheseFitIn(items));
    }

    @Test
    void testExactFit() {
        Box container = new Box(3, 3, 3);

        List<Box> items = List.of(
            new Box(3, 1, 1), new Box(1, 1, 1), new Box(1, 1, 1), new Box(1, 1, 1),
            new Box(1, 1, 1), new Box(1, 1, 1), new Box(1, 1, 3), new Box(1, 1, 1),
            new Box(1, 2, 1), new Box(1, 1, 2), new Box(2, 1, 1), new Box(1, 3, 1),
            new Box(1, 2, 1), new Box(1, 2, 1)
        );

        assertTrue(container.canTheseFitIn(items));
    }

    @Test
    void testNoFit() {
        Box container = new Box(3, 3, 3);

        List<Box> items = List.of(
            new Box(3, 1, 1), new Box(1, 1, 1), new Box(1, 1, 1), new Box(1, 1, 1),
            new Box(1, 1, 1), new Box(1, 1, 1), new Box(1, 1, 3), new Box(1, 1, 1),
            new Box(1, 2, 1), new Box(2, 1, 2), new Box(2, 1, 1), new Box(1, 3, 1),
            new Box(1, 2, 1), new Box(1, 2, 1)
        );

        assertFalse(container.canTheseFitIn(items));
    }

}