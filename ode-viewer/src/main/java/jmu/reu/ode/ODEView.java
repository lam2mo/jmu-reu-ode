
package jmu.reu.ode;
/**
 * JMU REU 2022
 * 
 * Container for the data needed to represent a singular view of an ODE.
 * 
 * @author Mike Lam, Benjamin Huber
 * @version 7/9/2022
 */
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.Paint;
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
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.Range;
import org.jfree.data.xy.DefaultXYDataset;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;


public class ODEView extends JPanel implements ChangeListener, DocumentListener {
    private final double PARAM_FACTOR = 100.0;
    private boolean ghosting = false;
    private boolean updating = false;
    private int size = 0;
    private JLabel description;
    private List<ChartPanel> charts;
    private List<String> commands;
    private List<String> plotScript;
    private Map<String, JTextField> fields;
    private Map<String, JSlider> sliders;
    private Map<String, Parameter> parameters;
    private MapQueueLimited<String, double[][]> ghosts;
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
                                Double.parseDouble(args[4]),
                                Double.parseDouble(args[5]));
                    parameters.put(p.label, p);
                    orderedParameters.add(p);
                } else if (line.startsWith("run")) {
                    commands.add(line.replaceFirst("run ", ""));
                } else if (line.startsWith("plot")) {
                    plotScript.add(line.replaceFirst("plot ", ""));
                    size+=1;
                } else if (line.startsWith("set")) {
                    plotScript.add(line);
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
            JSnapSlider slider = new JSnapSlider(
                    (int)(p.min * PARAM_FACTOR),
                    (int)(p.max * PARAM_FACTOR),
                    (int)(p.def * PARAM_FACTOR),
                    (int)(p.snap * PARAM_FACTOR));
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

            // build plot using JFreeChart
            int count = 0;
            ValueAxis xAxis = new NumberAxis();
            ValueAxis yAxis = new NumberAxis();
            for (String line : plotScript) {
                SeriesInfo sInfo = null;
                if (line.substring(0, 4).equals("set ")) {
                    String[] args = line.trim().split(" +");
                    // Get the title of the axis for applicable set commands
                    String title = "";
                    if (args.length == 4) {
                        title = args[3];
                    }
                    switch (args[1]) {
                        case "log":
                            switch (args[2]) {
                                case "x":
                                    xAxis = new LogAxis(title);
                                    break;
                                case "y":
                                    yAxis = new LogAxis(title);
                                    break;
                            }
                            break;
                        case "numeric":
                            switch (args[2]) {
                                case "x":
                                    xAxis = new NumberAxis(title);
                                    break;
                                case "y":
                                    yAxis = new NumberAxis(title);
                                    break;
                            }
                            break;
                        case "ghosting":
                            if (ghosting) {
                                break;
                            }
                            ghosting = true;
                            ghosts = new MapQueueLimited<>(Integer.parseInt(args[2]));
                            break;
                        default:
                            break;
                    }
                // Handles dataset loading.
                } else {
                    // Splits the individual "series" into an array for processing
                    String[] series = line.trim().split(", ");
                    DefaultXYDataset dataset = new DefaultXYDataset();
                    String[] title = new String[series.length];
                    // Loops over the series, splitting it and dealing with the arguments one at a time
                    for (int i = 0; i < series.length; i++) {
                        String[] arguments = series[i].trim().split("\\s+(?![^\\[]*\\])");
                        File datafile = new File(arguments[0]);
                        ArrayList<String> fileLines = loadData(datafile);
                        String[] numbers = arguments[1].split(":");
                        int x = Integer.parseInt(numbers[0]);
                        int y = Integer.parseInt(numbers[1]);
                        sInfo = processData(fileLines, x, y);
                        title[i] = arguments[2].substring(1, arguments[2].length()-1);
                        dataset.addSeries(title[i], sInfo.data);
                        if (ghosting) {
                            ghosts.put(title[i], sInfo.data);
                        }
                    }
                    // Draws the chart
                    XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true, true);
                    JFreeChart chart;
                    // Try is to prevent it from crashing on invalid ranges.
                    try {
                        // This is to make it look nice on a LogAxis.  It still needs work at low values.
                        if (yAxis instanceof LogAxis) {
                            // ((LogAxis)yAxis).setSmallestValue(sInfo.min - 0.000000000001);
                            ((LogAxis)yAxis).setRange(new Range(sInfo.min, sInfo.max + 4));
                        }
                        XYPlot plot = new XYPlot(dataset, xAxis, yAxis, renderer);
                        // Ghosting stuff
                        if (ghosting) {
                            for (int i = 0; i < dataset.getSeriesCount(); i++) {
                                Color color = Color.red;
                                renderer.setSeriesPaint(i, color);
                                DefaultXYDataset ghostSet = new DefaultXYDataset();
                                int j = 0;
                                for (double[][] ghostData : ghosts.getQueue(title[i])) {
                                    if (j == 0) {
                                        j++;
                                        continue;
                                    }
                                    ghostSet.addSeries(title[i] + j, ghostData);
                                    j++;
                                }
                                plot.setDataset(i + dataset.getSeriesCount(), ghostSet);
                                plot.setRenderer(i + dataset.getSeriesCount(), new XYLineAndShapeRenderer());
                                for (int k = 0; k < j; k++) {
                                    color = color.darker();
                                    plot.getRendererForDataset(ghostSet).setSeriesPaint(k, color);
                                }
                            }
                        }
                        chart = new JFreeChart(plot);
                        
                    }
                    catch (IllegalArgumentException ex) {
                        chart = null;
                    }
                    charts.get(count).setChart(chart);
                    count++;
                }
            }

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
    private SeriesInfo processData(List<String> lines, int x, int y) {
        double[][] data = new double[2][lines.size()];
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        try {
            int lineCounter = 0;
            int indexCounter = 0;
            while (lineCounter < lines.size()) {
                String[] numbers = lines.get(lineCounter).split(" +");
                if (numbers[x-1].equals("nan") || numbers[y-1].equals("nan") 
                    || numbers[x-1].equals("-nan") || numbers[y-1].equals("-nan")
                    || numbers[x-1].equals("inf") || numbers[y-1].equals("inf")
                    || numbers[x-1].equals("-inf") || numbers[y-1].equals("-inf")) {
                    lineCounter+=1;
                    continue;
                }
                data[0][indexCounter] = Double.parseDouble(numbers[x-1]);
                double yValue = Double.parseDouble(numbers[y-1]);
                if (yValue < min && yValue > 0) {
                    min = yValue;
                }
                if (yValue > max) {
                    max = yValue;
                }
                data[1][indexCounter] = yValue;
                indexCounter+=1;
                lineCounter+=1;
            }
        }
        catch (ArrayIndexOutOfBoundsException ex) {
            System.out.println("There is no valid column " + x + " or " + y + ".");
            ex.printStackTrace();
        }
        return new SeriesInfo(data, min, max);
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
