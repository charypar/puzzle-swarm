/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.noctapuru.puzzlehuntmodel.view;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.JPanel;

/**
 *
 * @author viktor
 */
public class View extends JPanel {
    protected PropertyChangeSupport propertySupport;

    public View() {
        this.propertySupport = new PropertyChangeSupport(this);
    }

    // property change support

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.addPropertyChangeListener(listener);
        super.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.removePropertyChangeListener(listener);
        super.removePropertyChangeListener(listener);
    }

}
