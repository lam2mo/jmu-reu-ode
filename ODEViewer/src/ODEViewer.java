/**
 * JMU REU 2021
 *
 * GUI view for ODE solver
 *
 * @author Mike Lam
 */

import java.awt.*;
import java.io.*;
import javax.swing.*;

class ODEViewer extends JFrame
{
    // parameter info
    private File file;

    public ODEViewer() {
        file = selectFile();
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Container cp = this.getContentPane();
        cp.add(new ODEView(file));
        this.setVisible(true);
        this.setSize(700, 700);
    }

    /**
     * Selects the file, returns null if the file is not found.
     * 
     * @return the file selected, otherwise null.
     */
    private File selectFile() {
        File f;
        JFileChooser dialog = new JFileChooser();
        dialog.setCurrentDirectory(new File(System.getProperty("user.dir")));
        dialog.setFileFilter(new javax.swing.filechooser.FileFilter() {
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().endsWith(".cfg");
            }
            public String getDescription() {
                return "Configurations (*.cfg)";
            }
        });
        int rval = dialog.showOpenDialog(null);
        if (rval == JFileChooser.APPROVE_OPTION) {
            f = dialog.getSelectedFile();
        } else {
            return null;
        }
        return f;
    }

    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new ODEViewer();
            }
        });
    }
}
