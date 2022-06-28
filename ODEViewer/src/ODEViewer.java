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
import javax.swing.*;

class ODEViewer extends JFrame
{
    // parameter info
    private JTabbedPane mainPanel;

    public ODEViewer() {
        super();

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Container cp = this.getContentPane();
        cp.setLayout(new BorderLayout());

        mainPanel = new JTabbedPane();
        cp.add(mainPanel, BorderLayout.CENTER);

        setupMenu();
        this.setVisible(true);
        this.setSize(700,700);

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
        fileMenu.add(newMenu);
        menuBar.add(fileMenu);
    }

    /**
     * Adds a new tab to our Viewer.
     * 
     * @param panel the panel to add
     */
    public void addNewTab(ODEView panel) {
        mainPanel.add(panel.getTitle(), panel);
    }

    public static void main(String[] args)
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
