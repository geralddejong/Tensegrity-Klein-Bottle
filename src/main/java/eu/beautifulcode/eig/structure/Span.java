/*
 * Copyright (C)2008 Gerald de Jong - GNU General Public License
 * please see the LICENSE.TXT in this distribution for more details.
 */

package eu.beautifulcode.eig.structure;

/**
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class Span {
    private static final double MINIMUM_SPAN = 0.001;

    double actual;
    double ideal;
    double stress;
    Future future;

    public Span(double actual, double ideal, double stress) {
        this.actual = actual;
        this.ideal = ideal;
        this.stress = stress;
    }

    public double getActual() {
        return actual;
    }

    public double getCurrentIdeal() {
        return ideal;
    }

    public double getStress() {
        return stress;
    }

    public double getStress(StressRange range) {
        double value = (stress - range.minimum()) / (range.maximum() - range.minimum());
        if (value >= 1) {
            value = 1 - 1e-12;
        }
        if (value < 0) {
            value = 0;
        }
        return value;
    }

    public void perturbIdeal(int howLong, double... perturbations) {
        int step = howLong / (perturbations.length+1);
        double ultimateIdeal = getUltimateIdeal();
        for (double perturbation : perturbations) {
            setIdeal(ultimateIdeal * perturbation, step);
        }
        setIdeal(ultimateIdeal, step);
    }

    public void setIdeal(double idealValue, int howLong) {
        if (howLong == 0) {
            ideal = idealValue;
        }
        else {
            Future newFuture = new Future(idealValue, howLong);
            if (future == null) {
                future = newFuture;
            }
            else {
                Future walk = future;
                while (walk.nextFuture != null) {
                    walk = walk.nextFuture;
                }
                walk.nextFuture = newFuture;
            }
        }
    }

    public double getUltimateIdeal() {
        if (future == null) {
            return ideal;
        }
        Future walk = future;
        while (walk.nextFuture != null) {
            walk = walk.nextFuture;
        }
        return future.value;
    }

    public void adjustIdeal(double factor) {
        if (future == null) {
            ideal *= factor;
        }
        else {
            Future walk = future;
            while (walk.nextFuture != null) {
                walk = walk.nextFuture;
            }
            future.value *= factor;
        }
    }

    public boolean isActive() {
        return future != null;
    }

    public boolean experienceTime(long time) {
        if (isActive()) {
            if (future.when == 0) {
                future.when = time + future.howLong;
                future.initial = ideal;
            }
            int timeLeft = (int)(future.when - time);
            if (timeLeft < 0) {
                ideal = future.value;
                future = future.nextFuture;
            }
            else {
                ideal = future.getCurrent(timeLeft);
            }
        }
        return isActive();
    }

    public String toString() {
        StringBuffer out = new StringBuffer("Span(ideal="+ ideal +")");
        Future walk = future;
        while (walk != null) {
            out.append(" => ").append(walk.value);
            walk = walk.nextFuture;
        }
        return out.toString();
    }

    int getChainSize() {
        int count = 0;
        Future walk = future;
        while (walk != null) {
            walk = walk.nextFuture;
            count++;
        }
        return count;
    }

    public boolean isSignificant() {
        return actual > MINIMUM_SPAN;
    }

    public interface StressRange {
        double minimum();
        double maximum();
    }

    public static class Future {

        private Future(double value, int howLong) {
            this.value = value;
            this.howLong = howLong;
        }

        public Future() {
        }

        Future nextFuture;
        double initial;
        double value;
        int howLong;
        long when;

        double getCurrent(int timeLeft) {
            double remaining = (double)timeLeft/howLong;
            return initial * remaining + value * (1-remaining);
        }
    }
}