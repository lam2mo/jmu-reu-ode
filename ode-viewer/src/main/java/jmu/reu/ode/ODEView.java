
package jmu.reu.ode;
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
import java.awt.GridLayout;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.BoxLayout;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.LogAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.DefaultXYDataset;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;


public class ODEView extends JPanel implements ChangeListener, DocumentListener {
    private final double PARAM_FACTOR = 100.0;
    private boolean updating = false;
    private int size = 0;
    private JLabel description;
    private List<ChartPanel> charts;
    private List<String> commands;
    private List<String> plotScript;
    private Map<String, JTextField> fields;
    private Map<String, JSlider> sliders;
    private Map<String, Parameter> parameters;
    private String title;

    public ODEView (File configFile) {
        // List of all the image filenames that appear in the file.
        description = new JLabel();
        parameters = new TreeMap<String, Parameter>();
        List<Parameter> orderedParameters = new ArrayList<Parameter>();
        commands = new ArrayList<String>();
        plotScript = new ArrayList<String>();
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
                } else if (line.startsWith("plot")) {
                    plotScript.add(line.replaceFirst("plot ", ""));
                    size+=1;
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
        // Get size of grid, should be refined later to a more reasonable approach
        int n = (int)(Math.sqrt((double)size)+0.5);

        // set grid size
        imagePanel.setLayout(new GridLayout(n, n, 2, 2));
        charts = new ArrayList<ChartPanel>();

        // instantiate our JLabels and add them to the imagePanel
        for (int i = 0; i < size; i++) {
            charts.add(new ChartPanel(null));
            charts.get(i).setVisible(true);
            imagePanel.add(charts.get(i));
        }
        imagePanel.setVisible(true);

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
            // Process process = Runtime.getRuntime().exec("gnuplot");
            // PrintWriter gnuplot = new PrintWriter(process.getOutputStream());
            // for (String line : gnuplotScript) {
            //     gnuplot.println(line);
            // }
            // gnuplot.close();
            // BufferedReader reader = new BufferedReader(
            //         new InputStreamReader(process.getInputStream()));
            // String line;
            // while ((line = reader.readLine()) != null) {
            //     System.out.println(line);
            // }
            // reader.close();
            int count = 0;
            for (String line : plotScript) {
                String[] series = line.trim().split(", ");
                DefaultXYDataset dataset = new DefaultXYDataset();
                for (int i = 0; i < series.length; i++) {
                    String[] arguments = series[i].trim().split("\\s+(?![^\\[]*\\])");
                    File datafile = new File(arguments[0]);
                    ArrayList<String> fileLines = loadData(datafile);
                    String[] numbers = arguments[1].split(":");
                    int x = Integer.parseInt(numbers[0]);
                    int y = Integer.parseInt(numbers[1]);
                    double[][] data = processData(fileLines, x, y);
                    dataset.addSeries(arguments[2].substring(1, arguments[2].length()-1), data);
                }
                XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true, true);
                NumberAxis xAxis = new NumberAxis();
                LogAxis yAxis = new LogAxis();
                XYPlot plot = new XYPlot(dataset, xAxis, yAxis, renderer);
                JFreeChart chart = new JFreeChart(plot);
                charts.get(count).setChart(chart);
                count++;
            }

            // read and display images
            // for (int i = 0; i < imageFilenames.size(); i++) {
            //     try {
            //         BufferedImage img = ImageIO.read(new File(imageFilenames.get(i)));
            //         images.get(i).setIcon(new ImageIcon(img));
            //     }
            //     catch (NullPointerException ex) {
            //         images.get(i).setIcon(null);
            //     }
            // }

        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (NullPointerException ex) {
            ex.printStackTrace();
            // for (int i = 0; i < images.size(); i++) {
            //     images.get(i).setIcon(null);
            // }
        }
    }

    /**
     * A helper method that takes a list of Strings and 2 integers, each 
     * representing what columns of data will represent x and y, and puts them
     * into a 2D array for use with the DefaultXYDataset.
     * @param lines your String lines
     * @param x your x column
     * @param y your y column
     * @return
     */
    private double[][] processData(List<String> lines, int x, int y) {
        double[][] data = new double[2][lines.size()];
        try {
            for (int i = 0; i < lines.size(); i++) {
                String[] numbers = lines.get(i).split(" +");
                data[0][i] = Double.parseDouble(numbers[x-1]);
                data[1][i] = Double.parseDouble(numbers[y-1]);
            }
        }
        catch (ArrayIndexOutOfBoundsException ex) {
            System.out.println("There is no valid column " + x + " or " + y + ".");
            ex.printStackTrace();
        }
        return data;
    }

    /**
     * Helper method to load data from a file.
     * @param file the file to load data from
     * @return the lines of the file as an arrayList
     */
    private ArrayList<String> loadData(File file) {
        ArrayList<String> lines = new ArrayList<String>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line.trim());
            }
            reader.close();
        } catch (FileNotFoundException ex) {
            System.out.println("No such file exists.");
            ex.printStackTrace();
        } catch (IOException ex) {
            System.out.println("Error reading in file.");
            ex.printStackTrace();
        }
        return lines;
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
