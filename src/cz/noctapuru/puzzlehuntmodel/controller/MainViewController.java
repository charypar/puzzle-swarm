/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.noctapuru.puzzlehuntmodel.controller;

import cz.noctapuru.puzzlehuntmodel.math.BasicWeibullRandom;
import cz.noctapuru.puzzlehuntmodel.math.WeibullRandom;

import cz.noctapuru.puzzlehuntmodel.model.GameModel;
import cz.noctapuru.puzzlehuntmodel.model.Checkpoint;
import cz.noctapuru.puzzlehuntmodel.model.CheckpointResult;

import cz.noctapuru.puzzlehuntmodel.view.MainView;
import java.awt.BorderLayout;
import java.awt.Point;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JDialog;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;
import org.jfree.data.statistics.BoxAndWhiskerCategoryDataset;
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;

/**
 *
 * @author viktor
 */
public class MainViewController extends AbstractViewController {

    private GameModel gameModel;
    private MainView mainView;
    
    // checkpoints window
    private JDialog checkpointsFrame = null;
    private CheckpointsViewController checkpointsController;

    public MainViewController() {
        WeibullRandom generator = new BasicWeibullRandom();
        this.gameModel = new GameModel(10000, 50, 60.0*4, generator);

        // example checkpoints to start with
        ArrayList checkpoints = new ArrayList<Checkpoint>(3);
        checkpoints.add(new Checkpoint(4.2, 38));
        checkpoints.add(new Checkpoint(2.2, 109));
        checkpoints.add(new Checkpoint(3.4, 95));
        gameModel.setCheckpoints(checkpoints);

        System.out.println("MainViewController started on thread "+
                Thread.currentThread().getName());
    }

    // Accessors

    public JDialog getCheckpointsFrame() {
        if(checkpointsFrame == null) {

            checkpointsFrame = new JDialog(this.frame, "Checkpoints");
            checkpointsFrame.getContentPane().add(getCheckpointsController().getView(), BorderLayout.CENTER);
            checkpointsFrame.getRootPane().putClientProperty("Window.style", "small");
            checkpointsFrame.pack();

            // move frame to the right of the main window
            Point l = this.frame.getLocation();
            l.x += this.frame.getWidth() / 3;
            l.y += 100;
            checkpointsFrame.setLocation(l);
        }

        return checkpointsFrame;
    }

    public CheckpointsViewController getCheckpointsController() {
        if(checkpointsController == null) {
            checkpointsController = new CheckpointsViewController();
            checkpointsController.setParentViewController(this);
        }

        return checkpointsController;
    }

    public GameModel getGameModel() {
        return gameModel;
    }

    // the controller mediates between the model and the view
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String propName = evt.getPropertyName();
        if(evt.getSource() == this.gameModel) {
            if(propName.equals(GameModel.PROP_STATUS)) {
                this.mainView.setProgress((Double) evt.getNewValue());
            }

            if(propName.equals(GameModel.PROP_RESULTS)) {
                this.mainView.setResults(this.gameModel.getResults());
                this.mainView.setCureentTimeUIEnabled(true);

                CheckpointResult finishResult = this.gameModel.getResults().get(this.gameModel.getCheckpoints().size());
                System.out.println("Estimated teams finishing the game: " + finishResult.getReachedByTeams());
            }

            if(propName.equals(GameModel.PROP_TEAMS_AT_CHECKPOINTS)) {
                (new TeamsChartDatasetJob((List<List<Integer>>) evt.getNewValue())).execute();
            }

            if(propName.equals(GameModel.PROP_CURRENT_TIME)) {
                Date newCurrentDate = this.datePlusMinutes(this.mainView.getStartDate(), Math.round((Double) evt.getNewValue()));
                
                this.mainView.setCurrentDate(newCurrentDate);
            }

            if(propName.equals(GameModel.PROP_TIME_LIMIT)) {
                Date newEndDate = this.datePlusMinutes(this.mainView.getStartDate(), Math.round((Double) evt.getNewValue()));

                this.mainView.setEndDate(newEndDate);
            }

        } else {
            if(propName.equals(MainView.PROP_SIMULATIONS)) {
                this.gameModel.setSize((Integer) evt.getNewValue());
            }

            if(propName.equals(MainView.PROP_TEAMS)) {
                this.gameModel.setGameSize((Integer) evt.getNewValue());
            }

            if(propName.equals(MainView.PROP_START_DATE)) {
                this.startDateChanged((Date) evt.getNewValue());
            }

            if(propName.equals(MainView.PROP_END_DATE)) {
                this.endDateChanged((Date) evt.getNewValue());
            }

            if(propName.equals(MainView.PROP_CURRENT_DATE)) {
                this.currentDateChanged((Date) evt.getNewValue());
            }
        }
    }

    private void startDateChanged(Date startDate) {
        Date newEndDate = this.datePlusMinutes(startDate, Math.round(this.gameModel.getTimeLimit()));
        Date newCurrentDate = this.datePlusMinutes(startDate, Math.round(this.gameModel.getCurrentTime()));

        this.mainView.setEndDate(newEndDate);
        this.mainView.setCurrentDate(newCurrentDate);
    }


    private void endDateChanged(Date endDate) {
        double newTimeLimit = this.minutesBetweenDates(this.mainView.getStartDate(), endDate);

        if(newTimeLimit == this.gameModel.getTimeLimit())
            return;

        if(endDate.before(this.mainView.getCurrentDate()))
            this.mainView.setCurrentDate(endDate);

        this.gameModel.setTimeLimit(newTimeLimit);
        this.mainView.setCureentTimeUIEnabled(false);
    }

    private void currentDateChanged(Date currentDate) {
        double newCurrentTime = this.minutesBetweenDates(this.mainView.getStartDate(), currentDate);

        if(newCurrentTime == this.gameModel.getCurrentTime())
            return;

        if(newCurrentTime < 0)
            newCurrentTime = 0;

        double timeLimit = this.gameModel.getTimeLimit();
        if(newCurrentTime > timeLimit)
            newCurrentTime = timeLimit;

        this.gameModel.setCurrentTime(newCurrentTime);
        
        if(this.gameModel.isSimulationDone())
            this.gameModel.recomputeTeamsAtCheckpoints();
    }

    private void datasetReady(TeamsChartDatasetJob job) {
        try {
            this.mainView.setTeamsChartData(job.get());
        } catch (InterruptedException ex) {
            Logger.getLogger(MainViewController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ExecutionException ex) {
            Logger.getLogger(MainViewController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // View lifecycle

    @Override
    protected MainView loadView() {
        this.mainView = new MainView(this);
        return this.mainView;
    }

    @Override
    protected void viewLoaded() {
        // set up observers
        this.gameModel.addPropertyChangeListener(this);

        this.mainView.setSimulations(this.gameModel.getSize());
        this.mainView.setTeams(this.gameModel.getGameSize());
        this.mainView.setEndDate(this.datePlusMinutes(this.mainView.getStartDate(), Math.round(this.gameModel.getTimeLimit())));
        this.mainView.setCurrentDate(this.datePlusMinutes(this.mainView.getStartDate(), Math.round(this.gameModel.getCurrentTime())));
    }

    @Override
    protected void frameSet() {
        this.frame.setTitle("Puzzle hunt model");
        this.frame.getContentPane().add(this.getView(), BorderLayout.CENTER);
        this.frame.setMinimumSize(this.getView().getMinimumSize());
        this.frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.frame.setLocation(150, 100); // TODO center the window...
        this.frame.pack();
    }

    // view actions
    
    public void runButtonClicked() {
        this.gameModel.runSimulation();
    }

    public void checkpointsButtonClicked() {
        if(this.getCheckpointsFrame().isVisible()) {
            this.getCheckpointsFrame().setVisible(false);
        } else {
            this.getCheckpointsFrame().setVisible(true);
        }
    }

    // private helper methods for model/view time conversions

    private Date datePlusMinutes(Date date, long minutes) {
        return new Date(date.getTime() + minutes * 60000);
    }

    private long minutesBetweenDates(Date startDate, Date endDate) {
        return  (endDate.getTime() - startDate.getTime()) / 60000;
    }

    private class TeamsChartDatasetJob extends SwingWorker<BoxAndWhiskerCategoryDataset, Integer> {

        List<List<Integer>> teamsAtCheckpoints;

        public TeamsChartDatasetJob(List<List<Integer>> teamsAtCheckpoints) {
            this.teamsAtCheckpoints = teamsAtCheckpoints;
        }

        @Override
        protected BoxAndWhiskerCategoryDataset doInBackground() throws Exception {
            DefaultBoxAndWhiskerCategoryDataset dataset = new DefaultBoxAndWhiskerCategoryDataset();

            for(int i = 0; i < this.teamsAtCheckpoints.size(); i++) {
                dataset.add(teamsAtCheckpoints.get(i), 1, i);
            }
            
            return dataset;
        }

        @Override
        protected void done() {
            datasetReady(this);
        }
    }
}
