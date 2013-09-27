/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.noctapuru.puzzlehuntmodel.controller;

import cz.noctapuru.puzzlehuntmodel.view.View;
import java.beans.*;
import javax.swing.JFrame;

/**
 *
 * @author viktor
 */
public abstract class AbstractViewController implements PropertyChangeListener {

    protected View view = null; // controlled view
    protected AbstractViewController parentViewController = null;
    protected JFrame frame = null; // controlled frame, if any

    public void setParentViewController(AbstractViewController parentViewController) {
        this.parentViewController = parentViewController;
    }

    public AbstractViewController getParentViewController() {
        return parentViewController;
    }

    public View getView() {
        if(this.view == null) {
            this.view = this.loadView();

            // listen to the view's changes
            this.view.addPropertyChangeListener(this);

            this.viewLoaded();
        }

        return this.view;
    }

    public JFrame getFrame() {
        return frame;
    }

    public void setFrame(JFrame frame) {
        this.frame = frame;
        this.frameSet();
    }

    public abstract void propertyChange(PropertyChangeEvent evt);

    // used by subclass to load it's view
    protected abstract View loadView();

    // used by subclass to perform initialization after loading the view
    protected void viewLoaded() {};

    // used by subclass to perform initialization after getting a frame
    protected void frameSet() {}
}
