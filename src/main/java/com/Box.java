package com;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static com.Box.Rotation.*;

public final class Box {
    // Config
    private static final int DISABLE_TIME_LIMIT = -1;

    // Array rotations
    private static final Rotation[] ROTATIONS = Rotation.values();
    private static final Rotation[] IDENTITY = {R0};
    private static final Rotation[] ROTATIONS_W_EQ_H = {R0, R1, R4};
    private static final Rotation[] ROTATIONS_W_EQ_L = {R0, R2, R3};
    private static final Rotation[] ROTATIONS_H_EQ_L = {R0, R1, R3};

    // Caches
    private final int[] dimSorted;

    private final int width;
    private final int height;
    private final int length;

    private final int max;
    private final int med;
    private final int min;

    private final long volume;
    private final int gcd;

    public Box(int width, int height, int length) {
        validateDimension(width);
        validateDimension(height);
        validateDimension(length);

        this.width = width;
        this.height = height;
        this.length = length;
        this.volume = ((long) width) * height * length;

        dimSorted = sortAsc(width, height, length);
        gcd = gcd(width, height, length);

        max = dimSorted[2];
        med = dimSorted[1];
        min = dimSorted[0];
    }


    @Override
    public String toString() {
        return "Box(" + width + ", " + height + ", " + length + ')';
    }

    public boolean canTheseFitIn(List<Box> boxes) {
        return _canTheseFitIn(boxes, DISABLE_TIME_LIMIT, false);
    }

    public boolean canTheseFitIn(List<Box> boxes, int limitMs) {
        if (limitMs <= 0) {
            throw new IllegalArgumentException("limitMs must be a positive number");
        }

        return _canTheseFitIn(boxes, limitMs, false);
    }

    private boolean _canTheseFitIn(List<Box> boxes, int limitMs, boolean alreadyScaled) {
        // Quick check
        int minLengthSum = 0;
        long totalVolume = 0;
        int boxesMaxDimension = 0;

        for (var box : boxes) {
            // Check if they fit "1 in 1"
            if (max < box.max || med < box.med || min < box.min) {
                return false;
            }

            minLengthSum += box.min;
            totalVolume += box.volume;

            if (box.max > boxesMaxDimension) {
                boxesMaxDimension = box.max;
            }
        }

        // They fit individually, so if we stack them on the min and check
        if (minLengthSum <= min) {
            return true;
        }

        // If they have more volume we reject
        if (volume < totalVolume) {
            return false;
        }

        // If we slice the box into cubes that are the max dimension and we have more cubes than items then they fit
        if ((width / boxesMaxDimension) *
            (height / boxesMaxDimension) *
            (length / boxesMaxDimension) >= boxes.size()) {
            return true;
        }

        // Try multistacking;
        {
            var perimeterBoxes = new ArrayList<>(boxes);
            perimeterBoxes.sort(Comparator.comparingLong(Box::dimSummedPartial).reversed());

            int stackH = 0;
            int stackMax = 0;
            int curMax = max;

            boolean allFits = true;

            for (var box : perimeterBoxes) {
                if (stackH + box.min > min) {
                    curMax -= stackMax;
                    stackH = 0;
                    stackMax = 0;
                }

                if (curMax < box.max) {
                    allFits = false;
                    break;
                }

                if (stackH + box.min <= min) {
                    stackH += box.min;

                    if (stackMax < box.max) {
                        stackMax = box.max;
                    }
                }
            }

            if (allFits) {
                return true;
            }
        }

        if (!alreadyScaled && gcd > 1) {
            int scale = gcd(gcd, gcd(boxes.stream().mapToInt(b -> b.gcd).toArray()));

            if (scale > 1) {
                var newBoxes = boxes.stream().map(b -> b.scaleDown(scale)).toList();
                return scaleDown(scale)._canTheseFitIn(newBoxes, limitMs, true);
            }


        }

        // Bring out the big guns
        boxes = new ArrayList<>(boxes);
        boxes.sort(Comparator.comparingLong(Box::volume).reversed());

        int n = boxes.size();
        Model model = new Model("Boxes into box");

        // Coordinates at lower left point (regardless of rotation)
        IntVar[] xs = new IntVar[n];
        IntVar[] ys = new IntVar[n];
        IntVar[] zs = new IntVar[n];

        // Relative position
        BoolVar[][] ls = new BoolVar[n][];
        BoolVar[][] us = new BoolVar[n][];
        BoolVar[][] bs = new BoolVar[n][];

        for (int i = 0; i < n; i++) {
            Box ibox = boxes.get(i);

            ls[i] = new BoolVar[n];
            us[i] = new BoolVar[n];
            bs[i] = new BoolVar[n];

            xs[i] = model.intVar("x_" + i, 0, width - ibox.min);
            ys[i] = model.intVar("y_" + i, 0, height - ibox.min);
            zs[i] = model.intVar("z_" + i, 0, length - ibox.min);

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
            Box ibox = boxes.get(i);
            int iboxRotationsCount = ibox.getDistinctRotationsCount();
            Rotation[] iboxRotations = ibox.getDistinctRotations();

            int[] possibleHeights = new int[iboxRotationsCount];
            int[] possibleWidths  = new int[iboxRotationsCount];
            int[] possibleLengths = new int[iboxRotationsCount];

            for (int j = 0; j < iboxRotationsCount; j++) {
                possibleHeights[j] = ibox.height(iboxRotations[j]);
                possibleWidths[j]  = ibox.width(iboxRotations[j]);
                possibleLengths[j] = ibox.length(iboxRotations[j]);
            }

            rot[i] = model.intVar("rot_" + i, 0, iboxRotationsCount - 1);

            cH[i] = model.intVar("chosenHeight_" + i, possibleHeights);
            cW[i] = model.intVar("chosenWidth_" + i, possibleWidths);
            cL[i] = model.intVar("chosenLength_" + i, possibleLengths);

            model.element(cH[i], possibleHeights, rot[i]).post();
            model.element(cW[i], possibleWidths,  rot[i]).post();
            model.element(cL[i], possibleLengths, rot[i]).post();

            // Container Bounds
            model.arithm(xs[i], "+", cW[i], "<=", width).post();
            model.arithm(ys[i], "+", cH[i], "<=", height).post();
            model.arithm(zs[i], "+", cL[i], "<=", length).post();
        }

        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                model.arithm(xs[i], "+", cW[i], "<=", xs[j]).reifyWith(ls[i][j]);
                model.arithm(ys[i], "+", cH[i], "<=", ys[j]).reifyWith(us[i][j]);
                model.arithm(zs[i], "+", cL[i], "<=", zs[j]).reifyWith(bs[i][j]);

                model.addClausesBoolOrArrayEqualTrue(new BoolVar[]{ls[i][j], us[i][j], bs[i][j]});
            }
        }

        // identical boxes -> enforce ordering
        for (int i = 0; i < n - 1; i++) {
            Box bi = boxes.get(i);
            Box bj = boxes.get(i + 1);

            if (Arrays.equals(bi.getDimensionsSortedAsc(), bj.getDimensionsSortedAsc())) {
                model.lexLessEq(
                    new IntVar[]{xs[i], ys[i], zs[i]},
                    new IntVar[]{xs[i+1], ys[i+1], zs[i+1]}
                ).post();
            }
        }

        Solver solver = model.getSolver();

        if (limitMs != DISABLE_TIME_LIMIT) {
            solver.limitTime(limitMs);
        }

        boolean solved = solver.solve();
//        if (solved) {
//            System.out.println("Solution found:");
//            for (int i = 0; i < n; i++) {
//                int xVal = xs[i].getValue();
//                int yVal = ys[i].getValue();
//                int zVal = zs[i].getValue();
//                int wVal = cW[i].getValue();
//                int hVal = cH[i].getValue();
//                int lVal = cL[i].getValue();
//
//                System.out.printf(
//                    "Box %d -> (x=%d, y=%d, z=%d), w=%d, h=%d, l=%d%n",
//                    i, xVal, yVal, zVal, wVal, hVal, lVal
//                );
//            }
//        }
        return solved;
    }


    public int width() {
        return width;
    }

    public long volume() {
        return volume;
    }

    private void validateDimension(int val) {
        if (val <= 0 ) {
            throw new IllegalArgumentException("Box dimensions cannot be negative");
        }
    }

    public int height() {
        return height;
    }

    public int length() {
        return length;
    }

    int height(Rotation r) {
        return switch (r) {
            case R0, R1 -> height;
            case R2, R3 -> width;
            case R4, R5 -> length;
        };
    }

    int length(Rotation r) {
        return switch (r) {
            case R0, R3 -> length;
            case R1, R5 -> width;
            case R2, R4 -> height;
        };
    }

    int width(Rotation r) {
        return switch (r) {
            case R0, R4 -> width;
            case R1, R2 -> length;
            case R3, R5 -> height;
        };
    }

    private int getDistinctRotationsCount() {
        if (width == height && height == length) {
            return 1;
        }

        if (width == height || width == length || height == length) {
            return 3;
        }

        return 6;
    }

    Rotation[] getDistinctRotations() {
        if (width == height && height == length) {
            return IDENTITY;
        }

        if (width == height) {
            return ROTATIONS_W_EQ_H;
        }

        if (width == length) {
            return ROTATIONS_W_EQ_L;
        }

        if (height == length) {
            return ROTATIONS_H_EQ_L;
        }

        return ROTATIONS;
    }

    public int[] getDimensionsSortedAsc() {
        return dimSorted;
    }

    private int dimSummedPartial() {
        return width + height + length;
    }

    private Box scaleUp(int factor) {
        return new Box(factor * width, factor * height, factor * length);
    }

    private Box scaleDown(int factor) {
        if (gcd % factor != 0) {
            throw new IllegalStateException("Cannot scale down by " + factor);
        }

        return new Box(width / factor, height / factor, length / factor);
    }

    private int gcd(int a, int b) {
        if (b == 0) {
            return a;
        }

        return gcd(b, a %b );
    }

    private int gcd(int a, int b, int c) {
        return gcd(a, gcd(b, c));
    }

    private int gcd(int[] arr) {
        int gcd = arr[0];

        for (int i = 1; i < arr.length; i++) {
            gcd = gcd(gcd, arr[i]);

            if (gcd == 1) {
                return 1;
            }
        }

        return gcd;
    }

    private static int[] sortAsc(int x, int y, int z) {
        int[] arr = {x, y ,z};
        Arrays.sort(arr);
        return arr;
    }

    enum Rotation { R0, R1, R2, R3, R4, R5,}
}
