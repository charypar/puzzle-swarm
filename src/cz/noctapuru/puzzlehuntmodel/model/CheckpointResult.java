/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.noctapuru.puzzlehuntmodel.model;

import cz.noctapuru.puzzlehuntmodel.math.MonteCarloResult;

/**
 * Simple result container. The only point of this is encapsulation.
 * 
 * @author viktor
 */
public class CheckpointResult {
    private MonteCarloResult openingTime;
    private MonteCarloResult closingTime;
    private MonteCarloResult reachedByTeams;

    public CheckpointResult(int size) {
        this.openingTime = new MonteCarloResult(size);
        this.closingTime = new MonteCarloResult(size);
        this.reachedByTeams = new MonteCarloResult(size);
    }

    public MonteCarloResult getClosingTime() {
        return closingTime;
    }

    public MonteCarloResult getOpeningTime() {
        return openingTime;
    }

    public MonteCarloResult getReachedByTeams() {
        return reachedByTeams;
    }
}
