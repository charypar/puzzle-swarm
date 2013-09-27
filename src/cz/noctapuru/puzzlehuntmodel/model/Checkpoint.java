/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.noctapuru.puzzlehuntmodel.model;

/**
 *
 * @author viktor
 */
public class Checkpoint {
    private double alpha; // shape - approx. puzzle dificulty (~ 0.8 - 15.0)
    private double beta; // scale - approx. puzzle time requirements (in minutes)

    public Checkpoint(double alpha, double beta) {
        this.alpha = alpha;
        this.beta = beta;
    }
    
    public double getAlpha() {
        return alpha;
    }

    public double getBeta() {
        return beta;
    }

    public boolean equals(Checkpoint other) {
        return this.alpha == other.getAlpha() && this.beta == other.getBeta();
    }
}
