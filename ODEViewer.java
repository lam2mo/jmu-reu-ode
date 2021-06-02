/**
 * JMU REU 2021
 *
 * GUI view for ODE solver
 *
 * @author Mike Lam
 */

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.imageio.ImageIO;

class ODEViewer extends JFrame implements ChangeListener
{
    // parameter info (currently hard-coded)
    private final double PARAM_FACTOR = 100.0;
    private final int PARAM_COUNT = 6;
    private final String PARAM_LABEL[] =   {  "a",  "b",  "c", "x0", "tn", "dt" };
    private final double PARAM_MIN[] =     { -2.0, -2.0, -2.0, -2.0,  0.0, 0.01 };
    private final double PARAM_MAX[] =     {  2.0,  2.0,  2.0,  2.0,  5.0, 0.10 };
    private final double PARAM_DEFAULT[] = {  1.0,  1.0,  1.0,  1.0,  1.5, 0.05 };

    // dynamice components
    private JLabel image;
    private JLabel labels[];
    private JSlider sliders[];

    public ODEViewer()
    {
        // image panel consists of just a single label
        JPanel imagePanel = new JPanel();
        image = new JLabel();
        imagePanel.add(image);

        // slider panel consists of a stack of labels and sliders
        JPanel sliderPanel = new JPanel();
        sliderPanel.setLayout(new BoxLayout(sliderPanel, BoxLayout.PAGE_AXIS));
        labels = new JLabel[PARAM_COUNT];
        sliders = new JSlider[PARAM_COUNT];
        for (int i=0; i<PARAM_COUNT; i++) {
            JPanel tmpPanel = new JPanel();
            tmpPanel.setLayout(new BoxLayout(tmpPanel, BoxLayout.LINE_AXIS));
            labels[i] = new JLabel("   " + PARAM_LABEL[i]);
            sliders[i] = new JSlider(
                    (int)(PARAM_MIN[i]     * PARAM_FACTOR),
                    (int)(PARAM_MAX[i]     * PARAM_FACTOR),
                    (int)(PARAM_DEFAULT[i] * PARAM_FACTOR));
            sliders[i].addChangeListener(this);
            tmpPanel.add(labels[i]);
            tmpPanel.add(sliders[i]);
            sliderPanel.add(tmpPanel);
        }

        // main window consists of image panel and slider panel
        Container cp = getContentPane();
        cp.setLayout(new BorderLayout());
        cp.add(imagePanel, BorderLayout.CENTER);
        cp.add(sliderPanel, BorderLayout.SOUTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("ODE Viewer");
        setSize(700, 700);
        setVisible(true);

        stateChanged(null);
    }

    public void stateChanged(ChangeEvent e)
    {
        // gather parameters for call to plotting script (also update labels)
        StringBuilder cmd = new StringBuilder("bash plot.sh");
        for (int i=0; i<PARAM_COUNT; i++) {
            double value = (double)(sliders[i].getValue()) / PARAM_FACTOR;
            cmd.append(" ");
            cmd.append(value);
            labels[i].setText("   " + PARAM_LABEL[i] + " = " + value);
        }

        // run script and wait for it to finish, then load the new image
        try {
            Process process = Runtime.getRuntime().exec(cmd.toString());
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            reader.close();
            BufferedImage img = ImageIO.read(new File("./out.png"));
            image.setIcon(new ImageIcon(img));
        } catch (IOException ex) {
            System.out.println(ex.getStackTrace());
        }
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
