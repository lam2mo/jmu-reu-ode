package jmu.reu.ode;
/**
 * JMU REU 2021
 *
 * GUI view for ODE solver
 *
 * @author Mike Lam, Benjamin Huber
 * @version 6/26/2022
 */

import java.io.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import javax.swing.*;

class ODEViewer extends JFrame
{
    // parameter info
    private JTabbedPane mainPanel;
    private CloseTab closeAction;

    public ODEViewer() {
        super();

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Container cp = this.getContentPane();
        cp.setLayout(new BorderLayout());

        mainPanel = new JTabbedPane();
        cp.add(mainPanel, BorderLayout.CENTER);

        setupMenu();
        this.setVisible(true);
        this.setSize(840,950);

        mainPanel.setVisible(true);
    }

    private void setupMenu() {

        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        // File Menu
        JMenu fileMenu = new JMenu("File");
        // New Sub Menu
        JMenu newMenu = new JMenu("New");
        NewODEView newODEViewAction = new NewODEView(this);
        newMenu.add(newODEViewAction);

        // Close action
        fileMenu.add(newMenu);
        closeAction = new CloseTab();
        fileMenu.add(closeAction);
        menuBar.add(fileMenu);
    }

    public class CloseTab extends AbstractAction {

        public CloseTab () {
            putValue(AbstractAction.NAME, "Close");
        }
        @Override
        public void actionPerformed(ActionEvent arg0) {
            
            Component selected = mainPanel.getSelectedComponent();
            if (selected != null) {
                mainPanel.remove(selected);
            }          
        }
        
    }

    /**
     * Adds a new tab to our Viewer.
     * 
     * @param panel the panel to add
     */
    public void addNewTab(ODEView panel) {
        mainPanel.add(panel.getTitle(), panel);
    }

    public static void main(final String[] args)
    {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                ODEViewer v = new ODEViewer();
                for (int i = 0; i < args.length; i++) {
                    v.addNewTab(new ODEView(new File(args[i])));
                }
            }
        });
    }
}
