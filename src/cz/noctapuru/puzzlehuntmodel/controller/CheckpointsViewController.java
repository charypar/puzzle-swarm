/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.noctapuru.puzzlehuntmodel.controller;

import cz.noctapuru.puzzlehuntmodel.model.Checkpoint;
import cz.noctapuru.puzzlehuntmodel.model.GameModel;
import cz.noctapuru.puzzlehuntmodel.view.View;
import cz.noctapuru.puzzlehuntmodel.view.CheckpointsView;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author viktor
 */
public class CheckpointsViewController extends AbstractViewController {
    private CheckpointsView checkpointsView;

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if(evt.getSource().equals(this.view)) {
            if(evt.getPropertyName().equals(CheckpointsView.PROP_CHECKPOINTS)) {
                List<Checkpoint> copy = new ArrayList<Checkpoint>((List<Checkpoint>) evt.getNewValue());
                ((MainViewController) this.parentViewController).getGameModel().setCheckpoints(copy);
            }
        } else { // model is the source
            if(evt.getPropertyName().equals(GameModel.PROP_CHECKPOINTS)) {
                List<Checkpoint> copy = new ArrayList<Checkpoint>((List<Checkpoint>) evt.getNewValue());
                ((CheckpointsView) this.view).setCheckpoints(copy);
            }
        }
    }

    @Override
    protected View loadView() {
        this.checkpointsView = new CheckpointsView(this);
        return this.checkpointsView;
    }

    @Override
    protected void viewLoaded() {
        GameModel model = ((MainViewController) this.getParentViewController()).getGameModel();
        model.addPropertyChangeListener(this);
        this.checkpointsView.setCheckpoints(model.getCheckpoints());
    }



}
