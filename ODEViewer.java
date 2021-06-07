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
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.imageio.ImageIO;

class ODEViewer extends JFrame implements ChangeListener, DocumentListener
{
    // parameter info
    private final double PARAM_FACTOR = 100.0;
    private class Parameter
    {
        public String label;
        public double min;
        public double def;  // default
        public double max;
        public double value;

        public Parameter(String label, double min, double def, double max)
        {
            this.label = label;
            this.min = min;
            this.def = def;
            this.max = max;
            this.value = def;
        }
    }

    // dynamic components
    private JLabel description;
    private Map<String, Parameter> parameters;
    private java.util.List<String> commands;
    private java.util.List<String> gnuplotScript;
    private String imageFilename;
    private JLabel image;
    private Map<String, JTextField> fields;
    private Map<String, JSlider> sliders;

    public ODEViewer(File configFile)
    {
        // parse configuration file
        description = new JLabel();
        parameters = new TreeMap<String, Parameter>();
        java.util.List<Parameter> orderedParameters = new ArrayList<Parameter>();
        commands = new ArrayList<String>();
        gnuplotScript = new ArrayList<String>();
        BufferedReader file;
        String line = "";
        try {
            file = new BufferedReader(new FileReader(configFile));
            line = file.readLine();
            while (line != null) {
                if (line.startsWith("title")) {
                    setTitle(line.replaceFirst("title ", ""));
                } else if (line.startsWith("description")) {
                    description.setText(line.replaceFirst("description ", ""));
                } else if (line.startsWith("parameter")) {
                    String[] args = line.split("\\s+");
                    Parameter p = new Parameter(args[1],
                                Double.parseDouble(args[2]),
                                Double.parseDouble(args[3]),
                                Double.parseDouble(args[4]));
                    parameters.put(p.label, p);
                    orderedParameters.add(p);
                } else if (line.startsWith("run")) {
                    commands.add(line.replaceFirst("run ", ""));
                } else if (line.startsWith("gnuplot")) {
                    gnuplotScript.add(line.replaceFirst("gnuplot ", ""));
                } else if (line.startsWith("image")) {
                    imageFilename = line.replaceFirst("image ", "");
                }
                line = file.readLine();
            }
            file.close();
        } catch (IOException ex) {
            System.out.println("Could not read config file '" + configFile + "'");
            System.out.println(ex.getStackTrace());
        } catch (NumberFormatException ex) {
            System.out.println("Invalid parameter: " + line);
        }

        // image panel consists of just a single label
        JPanel imagePanel = new JPanel();
        image = new JLabel();
        imagePanel.add(image);

        // slider panel consists of a stack of labels and sliders
        JPanel sliderPanel = new JPanel();
        sliderPanel.setLayout(new BoxLayout(sliderPanel, BoxLayout.PAGE_AXIS));
        sliderPanel.add(description);
        fields = new TreeMap<String, JTextField>();
        sliders = new TreeMap<String, JSlider>();
        int i = 0;
        for (Parameter p : orderedParameters) {
            JPanel lblPanel = new JPanel();
            lblPanel.setLayout(new BoxLayout(lblPanel, BoxLayout.LINE_AXIS));
            JLabel lbl = new JLabel("   " + p.label + " = ");
            JTextField field = new JTextField("" + p.def, 5);
            field.getDocument().addDocumentListener(this);
            fields.put(p.label, field);
            lblPanel.add(lbl);
            lblPanel.add(field);
            JPanel tmpPanel = new JPanel();
            tmpPanel.setLayout(new BorderLayout());
            JSlider slider = new JSlider(
                    (int)(p.min * PARAM_FACTOR),
                    (int)(p.max * PARAM_FACTOR),
                    (int)(p.def * PARAM_FACTOR));
            slider.addChangeListener(this);
            sliders.put(p.label, slider);
            tmpPanel.add(lblPanel, BorderLayout.WEST);
            tmpPanel.add(slider, BorderLayout.CENTER);
            sliderPanel.add(tmpPanel);
            i++;
        }

        // main window consists of image panel and slider panel
        Container cp = getContentPane();
        cp.setLayout(new BorderLayout());
        cp.add(imagePanel, BorderLayout.CENTER);
        cp.add(sliderPanel, BorderLayout.SOUTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 700);
        setVisible(true);

        updatePlot();
    }

    public void updatePlot()
    {
        try {

            // run all commands
            for (String cmd : commands) {
                for (Parameter p : parameters.values()) {
                    cmd = cmd.replaceAll("\\$"+p.label, ""+p.value);
                }
                Process process = Runtime.getRuntime().exec(cmd.toString());
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
                reader.close();
            }

            // run Gnuplot
            Process process = Runtime.getRuntime().exec("gnuplot");
            PrintWriter gnuplot = new PrintWriter(process.getOutputStream());
            for (String line : gnuplotScript) {
                gnuplot.println(line);
            }
            gnuplot.close();
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            reader.close();

            // read and display image
            BufferedImage img = ImageIO.read(new File(imageFilename));
            image.setIcon(new ImageIcon(img));

        } catch (IOException ex) {
            System.out.println(ex.getStackTrace());
        } catch (NullPointerException ex) {
            image.setIcon(null);
        }
    }

    private boolean updating = false;

    public void stateChanged(ChangeEvent e)
    {
        if (updating) return;
        updating = true;
        for (Parameter p : parameters.values()) {
            double value = (double)(sliders.get(p.label).getValue()) / PARAM_FACTOR;
            p.value = value;
            fields.get(p.label).setText("" + value);
        }
        updatePlot();
        updating = false;
    }

    public void insertUpdate(DocumentEvent e)
    {
        if (updating) return;
        updating = true;
        try {
            for (Parameter p : parameters.values()) {
                double value = Double.parseDouble(fields.get(p.label).getText());
                p.value = value;
                sliders.get(p.label).setValue((int)(value * PARAM_FACTOR));
            }
            updatePlot();
        } catch (NumberFormatException ex) {
            image.setIcon(null);
        }
        updating = false;
    }
    public void removeUpdate(DocumentEvent e)
    {
        insertUpdate(e);
    }
    public void changedUpdate(DocumentEvent e) { /* don't need to handle style updates */ }

    public static void main(String[] args)
    {
        File f;
        if (args.length == 1) {
            f = new File(args[0]);
        } else {
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
                return;
            }
        }
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new ODEViewer(f);
            }
        });
    }
}
