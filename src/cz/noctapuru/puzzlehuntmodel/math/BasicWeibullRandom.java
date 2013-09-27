/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.noctapuru.puzzlehuntmodel.math;

import java.util.Random;

/**
 *
 * @author viktor
 */

public class BasicWeibullRandom extends Random implements WeibullRandom {

    /**
     * return a Weibull distributed random number with parameters alpha and beta
     * using a inverse of the cumulative distribution function
     */
    synchronized public double nextWeibull(double alpha, double beta) {
        return beta * Math.pow(- Math.log(nextDouble()), 1.0 / alpha);
    }

}
