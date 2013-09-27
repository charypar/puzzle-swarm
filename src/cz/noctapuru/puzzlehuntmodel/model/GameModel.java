/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.noctapuru.puzzlehuntmodel.model;

import cz.noctapuru.puzzlehuntmodel.math.WeibullRandom;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.beans.*;
import java.io.Serializable;

import javax.swing.SwingWorker;

/**
 * Main simulation model. Holds the game parameters, schedules simulation and status
 * computation tasks for running in a thread pool, collects the results and acts
 * as a model for the graph view.
 *
 * @author viktor
 */
public class GameModel implements Serializable {
    // JavaBeans
    public static final String PROP_SIZE = "size";
    public static final String PROP_GAME_SIZE = "gameSize";
    public static final String PROP_CHECKPOINTS = "checkpoints";
    public static final String PROP_CURRENT_TIME = "currentTime";
    public static final String PROP_TIME_LIMIT = "timeLimit";
    public static final String PROP_TEAMS_AT_CHECKPOINTS = "teamsAtCheckpoints";
    public static final String PROP_STATUS = "status";
    public static final String PROP_RESULTS = "results";

    private PropertyChangeSupport propertySupport;

    // GameModel attributes
  
    // settings
    private int size; // number of simulations to run
    private int gameSize; // number of teams to simulate for each game
    private List<Checkpoint> checkpoints = new ArrayList<Checkpoint>(); // game parameters

    private double currentTime = 0;
    private double timeLimit;

    // simulations
    private List<Game> games; // game realizations
    private List<List<Integer>> teamsAtCheckpoints; // at currentTime

    // results
    private List<CheckpointResult> results;

    // support
    private WeibullRandom generator;
    private double status = 0; // [0..1] observe for computation status monitoring

    // constructors

    public GameModel(int size, int gameSize, double timeLimit, WeibullRandom generator) {
        this();

        this.size = size;
        this.gameSize = gameSize;
        this.generator = generator;
        this.timeLimit = timeLimit;
    }

    public GameModel() {
        this.propertySupport = new PropertyChangeSupport(this);
        this.results = null;
    }

    // accessors

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        int oldSize = this.size;
        this.size = size;
        propertySupport.firePropertyChange(PROP_SIZE, oldSize, size);
    }

    public int getGameSize() {
        return gameSize;
    }

    public void setGameSize(int gameSize) {
        int oldGameSize = this.gameSize;
        this.gameSize = gameSize;
        propertySupport.firePropertyChange(PROP_GAME_SIZE, oldGameSize, gameSize);
    }

    public List<Checkpoint> getCheckpoints() {
        return checkpoints;
    }

    public void setCheckpoints(List<Checkpoint> checkpoints) {
        List<Checkpoint> oldCheckpoints = this.checkpoints;
        this.checkpoints = checkpoints;

        propertySupport.firePropertyChange(PROP_CHECKPOINTS, oldCheckpoints, checkpoints);
    }

    public double getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(double currentTime) {
        double oldCurrentTime = this.currentTime;
        this.currentTime = currentTime;
        
        propertySupport.firePropertyChange(PROP_CURRENT_TIME, oldCurrentTime, currentTime);
    }

    public double getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(double timeLimit) {
        double oldTimeLimit = this.timeLimit;
        this.timeLimit = timeLimit;

        propertySupport.firePropertyChange(PROP_TIME_LIMIT, oldTimeLimit, timeLimit);
    }

    public List<List<Integer>> getTeamsAtCheckpoints() {
        return teamsAtCheckpoints;
    }
    
    public WeibullRandom getGenerator() {
        return generator;
    }

    public List<CheckpointResult> getResults() {
        return results;
    }

    public boolean isSimulationDone() {
        return this.games != null && this.games.size() == this.size;
    }

    public void clear() {
        this.games = new ArrayList<Game>(this.size);
        this.results = new ArrayList<CheckpointResult>(this.checkpoints.size() + 1);
        for(int i = 0; i <= this.checkpoints.size(); i++)
            this.results.add(new CheckpointResult(this.size));
    }

    // background jobs scheduling

    public void runSimulation() {
        if(this.size < 1)
            throw new RuntimeException("Size cannot be zero");

        if(this.checkpoints.isEmpty())
            throw new RuntimeException("No checkpoint configuration provided");

        // reset the simulation
        clear();

        for(int i = 0; i < this.size; i++) {
            (new GameSimulationJob(this)).execute();
        }
    }

    public void recomputeTeamsAtCheckpoints() {
        if(this.games.isEmpty())
            throw new RuntimeException("Teams at checkpoints requested before a simulation ended");

        // re-initialize results
        int chpSize = this.checkpoints.size() + 1; // +1 for finished teams
        
        this.teamsAtCheckpoints = new ArrayList<List<Integer>>(chpSize);
        for(int i = 0; i < chpSize; i++) {
            this.teamsAtCheckpoints.add(new ArrayList<Integer>(this.size));
        }

        for(Game game : this.games) {
            (new TeamsAtCheckpointsJob(this, game, this.currentTime)).execute();
        }
    }

    private void computeResults() {
        (new ResultJob(this)).execute();
    }

    // Background job notifications (called on ED thread)

    private void simulationDone(GameSimulationJob job) {
        Game g;
        try {
            g = job.get();
        } catch (InterruptedException ex) {
            Logger.getLogger(GameModel.class.getName()).log(Level.SEVERE, null, ex);
            return;
        } catch (ExecutionException ex) {
            Logger.getLogger(GameModel.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }

        this.games.add(g); // no locking, got synchronization from Swing for free
        // no locking, got synchronization from Swing for free
        double[] ot = g.getOpeningTimes();
        double[] ct = g.getClosingTimes();
        int[] rbt = g.getReachedByTeams();

        for(int i = 0; i <= this.checkpoints.size(); i++) {
            CheckpointResult result = this.results.get(i);

            result.getOpeningTime().addValue(ot[i]);
            result.getReachedByTeams().addValue((double) rbt[i]);
            if(i < this.checkpoints.size())
                result.getClosingTime().addValue(ct[i]);

        }

        // won't be true untill the very end of the simulation,
        // so it shouldn't need any synchronization
        int gamesDone = this.games.size();
        if (gamesDone == this.size) {
            this.recomputeTeamsAtCheckpoints();
            this.computeResults();
        }

        if(gamesDone % (this.size / 100) == 0) {
            double oldStatus = this.status;
            this.status = (double) gamesDone / (double) this.size;
            propertySupport.firePropertyChange(PROP_STATUS, oldStatus, this.status);
        }
    }

    private void teamsAtCheckpointsDone(TeamsAtCheckpointsJob job) {
        int[] t;
        
        try {
            t = job.get();
        } catch (InterruptedException ex) {
            Logger.getLogger(GameModel.class.getName()).log(Level.SEVERE, null, ex);
            return;
        } catch (ExecutionException ex) {
            Logger.getLogger(GameModel.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        
        for(int i = 0; i < t.length; i++) {
            List<Integer> teams = this.teamsAtCheckpoints.get(i);

            teams.add(t[i]); // no locking, got synchronization from Swing for free
        }

        int gamesDone = this.teamsAtCheckpoints.get(0).size();
        if (gamesDone == this.size) {
            propertySupport.firePropertyChange(PROP_TEAMS_AT_CHECKPOINTS, null, this.teamsAtCheckpoints);
        }

        if(gamesDone % (this.size / 100) == 0) {
            double oldStatus = this.status;
            this.status = (double) gamesDone / (double) this.size;
            propertySupport.firePropertyChange(PROP_STATUS, oldStatus, this.status);
        }
    }

    private void resultDone(ResultJob job) {
        propertySupport.firePropertyChange(PROP_RESULTS, null, this.results);
    }

    // property change support

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.removePropertyChangeListener(listener);
    }

    // coputation jobs scheduled on the swing worker thread pool (yay!)

    class GameSimulationJob extends SwingWorker<Game, Integer> {
        private Game game;
        private GameModel model;

        public GameSimulationJob() {
        }

        public GameSimulationJob(GameModel gameModel) {
            this.model = gameModel;
        }

        @Override
        public Game doInBackground() throws Exception {
            this.game = new Game(model);
            this.game.generateTeams();

            return this.game;
        }

        @Override
        protected void done() {
            model.simulationDone(this);
        }
    }

    class TeamsAtCheckpointsJob extends SwingWorker<int[], Integer> {
        private double currentTime;
        private GameModel model;
        private Game game;

        public TeamsAtCheckpointsJob(GameModel model, Game game, double currentTime) {
            this.model = model;
            this.game = game;
            this.currentTime = currentTime;
        }

        @Override
        public int[] doInBackground() throws Exception {
            return game.getTeamsAtCheckpoints(this.currentTime);
        }

        @Override
        protected void done() {
            model.teamsAtCheckpointsDone(this);
        }
    }

    class ResultJob extends SwingWorker<Boolean, Integer> {
        private GameModel model;

        public ResultJob(GameModel model) {
            this.model = model;
        }

        @Override
        protected Boolean doInBackground() throws Exception {
            for(CheckpointResult r : this.model.results) {
                r.getOpeningTime().recompute();
                r.getClosingTime().recompute();
                r.getReachedByTeams().recompute();
            }

            return true;
        }

        @Override
        protected void done() {
            this.model.resultDone(this);
        }
    }
}
