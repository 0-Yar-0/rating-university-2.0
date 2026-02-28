package ru.ystu.rating.university.util;

public final class Normalizer {
    private Normalizer() {
    }

    public static double clamp01(double x, double vmin, double vmax) {
        if (Double.isNaN(x) || Double.isInfinite(x)) return 0.0;
        if (x <= vmin) return 0.0;
        if (x >= vmax) return 1.0;
        return (x - vmin) / (vmax - vmin);
    }

    public static double safeDiv(double a, double b) {
        return Math.abs(b) < 1e-9 ? 0.0 : a / b;
    }
}

