package io.github.bric3.fireplace.charts;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A dataset containing zero, one or many (x, y) data items.
 */
public class XYDataset {

    private final List<XY<Long, Double>> items;

    private final Range<Long> rangeOfX;

    private final Range<Double> rangeOfY;
    private String label;

    /**
     * Creates a new dataset.
     *
     * @param sourceItems the source items ({@code null} not permitted).
     */
    public XYDataset(List<XY<Long, Double>> sourceItems, String label) {
        this.label = label;
        // verify that none of the x-values is null or NaN ot INF
        // and while doing that record the mins and maxes
        Objects.requireNonNull(sourceItems);
        this.items = new ArrayList<>();
        long minX = Long.MAX_VALUE;
        long maxX = Long.MIN_VALUE;
        double minY = Double.POSITIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        for (var sourceItem : sourceItems) {
            if (sourceItem != null) {
                minX = Math.min(minX, sourceItem.x);
                maxX = Math.max(maxX, sourceItem.x);

                minY = Math.min(minY, sourceItem.y);
                maxY = Math.max(maxY, sourceItem.y);
                items.add(sourceItem);
            }
        }
        this.rangeOfX = new Range<>(minX, maxX);
        this.rangeOfY = new Range<>(minY, maxY);
    }

    public Object getProperty(String key) {
        return null;
    }

    public int getItemCount() {
        return items.size();
    }

    public Range<Long> getRangeOfX() {
        return this.rangeOfX;
    }

    public Range<Double> getRangeOfY() {
        return this.rangeOfY;
    }

    public Long getX(int index) {
        return this.items.get(index).x;
    }

    public Double getY(int index) {
        return this.items.get(index).y;
    }

    /**
     * Compare with another instance.
     * Note doesn't compare the data points, only the ranges and the label.
     *
     * @param o the object to compare with this instance.
     * @return whether two data set are assumed to be equal.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        XYDataset xyDataset = (XYDataset) o;
        return rangeOfX.equals(xyDataset.rangeOfX) && rangeOfY.equals(xyDataset.rangeOfY) && label.equals(xyDataset.label);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rangeOfX, rangeOfY, label);
    }

    /**
     * A pair of objects.
     *
     * @param <X> the type of the first object.
     * @param <Y> the type of the second object.
     */
    public static class XY<X extends Number, Y extends Number> {
        public final X x;
        public final Y y;

        /**
         * Creates a new pair.
         *
         * @param x the first object.
         * @param y the second object.
         */
        public XY(X x, Y y) {
            this.x = x;
            this.y = y;
        }
    }
}
