package ru.ystu.rating.university.util;

public enum BMetric {
    B11(40.0, 90.0, 23.0),
    B12(80.0, 100.0, 3.0),
    B13(0.0, 0.5, 4.0),
    B21(0.0, 1.0, 2.0);

    public final double vmin;
    public final double vmax;
    public final double weight;

    BMetric(double vmin, double vmax, double weight) {
        this.vmin = vmin;
        this.vmax = vmax;
        this.weight = weight;
    }
}
