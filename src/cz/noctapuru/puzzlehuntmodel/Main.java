/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.noctapuru.puzzlehuntmodel;

import cz.noctapuru.puzzlehuntmodel.controller.MainViewController;

import javax.swing.JFrame;

/**
 *
 * @author viktor
 */
public class Main {

    public Main() {
        // main frame
        JFrame mainFrame = new JFrame();

        // create main controller instance (which loads it's managed view)
        MainViewController controller = new MainViewController();
        controller.setFrame(mainFrame);

        // show the frame
        mainFrame.setVisible(true);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Main main = new Main();
    }

}
