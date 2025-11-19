package com;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;

import java.util.Arrays;
import java.util.List;

public final class Box {
    private static final int DISABLE_TIME_LIMIT = -1;
    private static final int BOX_POSSIBLE_ROTATIONS = 6;
    private static final Rotation[] rotations = Rotation.values();

    private final int width;
    private final int height;
    private final int length;

    private final int max;
    private final int med;
    private final int min;

    private final long volume;

    public Box(int width, int height, int length) {
        validateDimension(width);
        validateDimension(height);
        validateDimension(length);

        this.width = width;
        this.height = height;
        this.length = length;
        this.volume = ((long) width) * height * length;

        int[] sorted = sortAsc(width, height, length);

        max = sorted[2];
        med = sorted[1];
        min = sorted[0];
    }


    @Override
    public String toString() {
        return "Box(" + width + ", " + height + ", " + length + ')';
    }

    public boolean canTheseFitIn(List<Box> boxes) {
        return _canTheseFitIn(boxes, DISABLE_TIME_LIMIT);
    }

    public boolean canTheseFitIn(List<Box> boxes, int limitMs) {
        if (limitMs <= 0) {
            throw new IllegalArgumentException("limitMs must be a positive number");
        }

        return _canTheseFitIn(boxes, limitMs);
    }

    private boolean _canTheseFitIn(List<Box> boxes, int limitMs) {
        // Quick check
        int minLengthSum = 0;
        long totalVolume = 0;

        for (var box : boxes) {
            // Check if they fit "1 in 1"
            if (max < box.max || med < box.med || min < box.min) {
                return false;
            }

            minLengthSum += box.min;
            totalVolume += box.volume;
        }

        // They fit individually, so if we stack them on the min and check
        if (minLengthSum <= min) {
            return true;
        }

        // If they have more volume we reject
        if (volume < totalVolume) {
            return false;
        }

        // Bring out the big guns
        int n = boxes.size();
        Model model = new Model("Boxes into box");

        // Coordinates (boxes should atleast be 1x1x1 so we subtract)
        IntVar[] xs = model.intVarArray("x", n, 0, width - 1);
        IntVar[] ys = model.intVarArray("y", n, 0, height - 1);
        IntVar[] zs = model.intVarArray("z", n, 0, length - 1);

        // Relative position
        BoolVar[][] ls = new BoolVar[n][];
        BoolVar[][] us = new BoolVar[n][];
        BoolVar[][] bs = new BoolVar[n][];

        for (int i = 0; i < n; i++) {
            ls[i] = new BoolVar[n];
            us[i] = new BoolVar[n];
            bs[i] = new BoolVar[n];
            for (int j = i + 1; j < n; j++) {
                ls[i][j] = model.boolVar("ls_" + i + "_" + j);
                us[i][j] = model.boolVar("us_" + i + "_" + j);
                bs[i][j] = model.boolVar("bs_" + i + "_" + j);
            }
        }

        // Chosen per box
        IntVar[] rot = new IntVar[n];
        IntVar[] cW  = new IntVar[n];
        IntVar[] cH  = new IntVar[n];
        IntVar[] cL  = new IntVar[n];

        for (int i = 0; i < n; i++) {
            int[] possibleHeights = new int[BOX_POSSIBLE_ROTATIONS];
            int[] possibleWidths  = new int[BOX_POSSIBLE_ROTATIONS];
            int[] possibleLengths = new int[BOX_POSSIBLE_ROTATIONS];

            for (int j = 0; j < BOX_POSSIBLE_ROTATIONS; j++) {
                possibleHeights[j] = boxes.get(i).height(rotations[j]);
                possibleWidths[j]  = boxes.get(i).width(rotations[j]);
                possibleLengths[j] = boxes.get(i).length(rotations[j]);
            }

            rot[i] = model.intVar("rot_" + i, 0, BOX_POSSIBLE_ROTATIONS - 1);

            cH[i] = model.intVar("chosenHeight_" + i, 0, boxes.get(i).max);
            cW[i] = model.intVar("chosenWidth_" + i, 0, boxes.get(i).max);
            cL[i] = model.intVar("chosenLength_" + i, 0, boxes.get(i).max);

            model.element(cH[i], possibleHeights, rot[i]).post();
            model.element(cW[i],  possibleWidths,  rot[i]).post();
            model.element(cL[i], possibleLengths, rot[i]).post();

            // Container Bounds
            model.arithm(xs[i], "+", cW[i], "<=", width).post();
            model.arithm(ys[i], "+", cH[i], "<=", height).post();
            model.arithm(zs[i], "+", cL[i], "<=", length).post();
        }

        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                model.ifThen(ls[i][j], model.arithm(xs[i], "+", cW[i], "<=", xs[j]));
                model.ifThen(us[i][j], model.arithm(ys[i], "+", cH[i], "<=", ys[j]));
                model.ifThen(bs[i][j], model.arithm(zs[i], "+", cL[i], "<=", zs[j]));

                model.addClausesAtMostOne(new BoolVar[]{ls[i][j], us[i][j], bs[i][j]});
                model.addClausesBoolOrArrayEqualTrue(new BoolVar[]{ls[i][j], us[i][j], bs[i][j]});
            }
        }

        Solver solver = model.getSolver();

        if (limitMs != DISABLE_TIME_LIMIT) {
            solver.limitTime(limitMs);
        }

        return solver.solve();
    }

    public int width() {
        return width;
    }

    private void validateDimension(int val) {
        if (val < 0 ) {
            throw new IllegalArgumentException("Box dimensions cannot be negative");
        }
    }

    public int height() {
        return height;
    }

    public int length() {
        return length;
    }

    private int height(Rotation r) {
        switch (r) {
            case R0, R1 -> {
                return height;
            }
            case R2, R3 -> {
                return width;
            }
            case R4, R5 -> {
                return length;
            }
            default -> throw new IllegalStateException("Unexpected value: " + r);
        }
    }

    private int length(Rotation r) {
        switch (r) {
            case R0, R3 -> {
                return length;
            }
            case R1, R5 -> {
                return width;
            }
            case R2, R4 -> {
                return height;
            }
            default -> throw new IllegalStateException("Unexpected value: " + r);
        }
    }

    private int width(Rotation r) {
        switch (r) {
            case R0, R4 -> {
                return width;
            }
            case R1, R2 -> {
                return length;
            }
            case R3, R5 -> {
                return height;
            }
            default -> throw new IllegalStateException("Unexpected value: " + r);
        }
    }

    public int[] getDimensionsSortedAsc() {
        return new int[]{min, med, max};
    }


    private static int[] sortAsc(int x, int y, int z) {
        int[] arr = {x, y ,z};
        Arrays.sort(arr);
        return arr;
    }

    private enum Rotation {
        R0,
        R1,
        R2,
        R3,
        R4,
        R5,
    }

}
