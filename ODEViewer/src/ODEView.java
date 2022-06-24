/**
 * JMU REU 2022
 * 
 * Container for the data needed to represent a singular view of an ODE.
 * 
 * @author Mike Lam, Benjamin Huber
 * @version 6/23/2022
 */
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;


public class ODEView extends JPanel implements ChangeListener, DocumentListener {
    private final double PARAM_FACTOR = 100.0;
    private boolean updating = false;
    private JLabel description;
    private JLabel image;
    private List<String> commands;
    private List<String> gnuplotScript;
    private Map<String, JTextField> fields;
    private Map<String, JSlider> sliders;
    private Map<String, Parameter> parameters;
    // private String configFileLocation;
    private String imageFilename;
    private String title;

    public ODEView (File configFile) {
        // configFileLocation = configFile.getParentFile().getAbsolutePath();
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
            System.err.println("Could not read config file '" + configFile + "'");
            ex.printStackTrace();
        } catch (NumberFormatException ex) {
            System.err.println("Invalid parameter: " + line);
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
        }

        // main window consists of image panel and slider panel
        Container cp = this;
        cp.setLayout(new BorderLayout());
        cp.add(imagePanel, BorderLayout.CENTER);
        cp.add(sliderPanel, BorderLayout.SOUTH);
        // setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 700);
        setVisible(true);

        updatePlot();
    }

    public void updatePlot()
    {
        try {

            // run all commands
            for (String cmd : commands) {
                // cmd = cmd.replaceAll("./", configFileLocation + "/");
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
            ex.printStackTrace();
        } catch (NullPointerException ex) {
            image.setIcon(null);
        }
    }

    /**
     * Private mutator method for title.  Really shouldn't be necessary, and can probaly be 
     * removed later, but I wanted to keep it in for easier editability.
     * 
     * @param title the new title of the panel
     */
    private void setTitle(String title) {
        this.title = title;
    }

    /**
     * Accessor method for title.  This is used so ODEViewer can name its TabbedPanes when I 
     * get to implementing that.
     * 
     * @return
     */
    public String getTitle() {
        return title;
    }

    @Override
    public void changedUpdate(DocumentEvent e) { /* don't need to handle style updates */ }

    @Override
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

    @Override
    public void removeUpdate(DocumentEvent e) {
        insertUpdate(e);
    }

    @Override
    public void stateChanged(ChangeEvent e) {
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
}