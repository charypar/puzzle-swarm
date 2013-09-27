/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.noctapuru.puzzlehuntmodel.model;

import cz.noctapuru.puzzlehuntmodel.math.WeibullRandom;
import java.util.List;

/**
 * Holds times for one team, i.e. one realization of the game
 * @author viktor
 */
public class Team {
    private GameModel game;
    private double[] times; // times when puzzles were solved, counted from the start of the game [minutes]

    public Team(GameModel game) {
        this.game = game;
    }

    public void generateTimes() {
        List<Checkpoint> checkpoints = this.game.getCheckpoints();
        this.times = new double[checkpoints.size()];
        double time = 0;

        WeibullRandom generator = game.getGenerator();

        for(int i = 0; i < this.times.length; i++) {
            time += generator.nextWeibull(checkpoints.get(i).getAlpha(), checkpoints.get(i).getBeta());
            this.times[i] = time;
        }
    }

    public double[] getTimes() {
        if(this.times == null) {
            generateTimes();
        }

        return times;
    }

    int getCheckpoint(double currentTime) {
        double time = 0;
        int i = 0;
        
        while(i < this.times.length && this.times[i] < currentTime)
            i++;

        return i; // team has finished the game
    }
}
