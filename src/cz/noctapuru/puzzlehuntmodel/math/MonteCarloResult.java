/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.noctapuru.puzzlehuntmodel.math;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates a result obtained by Monte Carlo computation. Values MUST be
 * i.i.d. samples from a distribution with finite 1st and 2nd moment. Also
 * the number of samples must be big enough.
 *
 * @author viktor
 */
public class MonteCarloResult {
    private List<Double> values;
    private double N = 0;
    private double mean = 0;
    private double error = 0;
    private static DecimalFormat format = new DecimalFormat("####.##");

    public MonteCarloResult(int size) {
        this.values = new ArrayList<Double>(size);
    }

    public double getError() {
        if(N != values.size())
            recompute();

        return error;
    }

    public double getMean() {
        if(N != values.size())
            recompute();
        
        return mean;
    }

    public void addValue(double value) {
        values.add(value);
    }

    public void recompute() {
        this.N = values.size();
        if(N == 0)  
            throw new RuntimeException("No values set");

        double sum = 0;

        // compute mean
        for(double v : values)
            sum += v;
        this.mean = sum / N;

        // estimate variance
        double var = 0;
        for(double v : values)
            var += (v - this.mean)*(v-this.mean);
        var = var / (N-1);

        // estimate error with a 95% probability
        this.error = 2*var / Math.sqrt(N);
    }

    @Override
    public String toString() {

        return format.format(this.getMean()) + " ± " + format.format(this.getError());
    }
}
