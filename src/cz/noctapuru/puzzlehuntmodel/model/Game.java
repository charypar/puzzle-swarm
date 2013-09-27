/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.noctapuru.puzzlehuntmodel.model;

/**
 * One game realization with a number of teams given in game model
 * 
 * @author viktor
 */
class Game {
    private GameModel model;
    private Team[] teams; // times for each team
    private double[] openingTimes; // for each checkpoint
    private double[] closingTimes; // ditto, except the last one
    private int[] reachedByTeams; // teams finishing before game time limit

    public Game(GameModel model) {
        this.model = model;
    }

    public void generateTeams() {
        int gameSize = this.model.getGameSize();
        this.teams = new Team[gameSize];

        int checkpoints = this.model.getCheckpoints().size();

        double times[] = new double[checkpoints];
        this.openingTimes = new double[checkpoints + 1];
        this.closingTimes = new double[checkpoints + 1];
        this.reachedByTeams = new int[checkpoints + 1];

        this.reachedByTeams[0] = gameSize;
        this.openingTimes[0] = 0;
        
        for(int i = 0; i < gameSize; i++) {
            this.teams[i] = new Team(model);
            this.teams[i].generateTimes();
            times = this.teams[i].getTimes();

            // find min and max times for open/close estimate
            for(int j = 0; j < checkpoints; j++) {
                if(times[j] > this.closingTimes[j])
                    this.closingTimes[j] = times[j];
                if(times[j] < this.openingTimes[j + 1] || i == 0)
                    this.openingTimes[j + 1] = times[j];
                if(times[j] <= this.model.getTimeLimit())
                    this.reachedByTeams[j+1]++;
            }
        }
    }

    public Team[] getTeams() {
        if(this.teams == null)
            this.generateTeams();

        return teams;
    }

    public double[] getOpeningTimes() {
        return this.openingTimes;
    }

    public double [] getClosingTimes() {
        return this.closingTimes;
    }

    public int[] getReachedByTeams() {
        return reachedByTeams;
    }

    /**
     * Returns number of teams which are at given checkpoints.
     *
     * @param time Current time in minutes
     * @return
     */
    public int[] getTeamsAtCheckpoints(double currentTime) {
        int[] teamNumbers = new int[this.model.getCheckpoints().size() + 1];
        int teamChp = 0;

        // count teams
        for(Team team : this.teams) {
            teamNumbers[team.getCheckpoint(currentTime)]++;
        }

        return teamNumbers;
    }
}
