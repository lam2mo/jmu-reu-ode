
package jmu.reu.ode;
/**
 * JMU REU 2022
 * 
 * Container for the data needed to represent a singular view of an ODE.
 * 
 * @author Mike Lam, Benjamin Huber
 * @version 7/9/2022
 */
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.GridLayout;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
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
    private boolean ghosting = false;
    private boolean updating = false;
    private int chartNum = 0;
    private JLabel description;
    private List<ChartPanel> charts;
    private List<ChartSettings> cSettingsList;
    private List<String> commands;
    private List<String> plotScript;
    private List<String> profileScript;
    private List<String> setScript;
    private Map<String, JTextField> fields;
    private Map<String, JSlider> sliders;
    private Map<String, Parameter> parameters;
    private Map<String, LineProfile> profiles;
    private Map<String, String> fileMap;
    private MapQueueLimited<String, double[][]> ghosts;
    private List<SeriesSettings> sSettings;
    private String title;

    public ODEView (File configFile) {
        // List of all the image filenames that appear in the file.
        description = new JLabel();
        parameters = new TreeMap<String, Parameter>();
        List<Parameter> orderedParameters = new ArrayList<Parameter>();
        commands = new ArrayList<String>();
        plotScript = new ArrayList<String>();
        setScript = new ArrayList<String>();
        sSettings = new ArrayList<>();
        profileScript = new ArrayList<String>();
        cSettingsList = new ArrayList<>();
        List<String> fileList = new ArrayList<>();
        fileMap = new HashMap<>();
        profiles = new HashMap<>();
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
                    plotScript.add(line);
                    chartNum+=1;
                } else if (line.startsWith("series")) {
                    plotScript.add(line);
                } else if (line.startsWith("set")) {
                    setScript.add(line.replaceFirst("set ", ""));
                } else if (line.startsWith("profile")) {
                    profileScript.add(line);
                } else if (line.startsWith("file")) {
                    fileList.add(line);
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

        // handle file aliasing
        for (String fileLine : fileList) {
            // List<String> matchList = new ArrayList<String>();
            // Pattern regex = Pattern.compile("([");
            // Matcher regexMatcher = regex.matcher(fileLine);
            // while (regexMatcher.find()) {
            //     if (regexMatcher.group(1) != null) {
            //         // Add double-quoted string without the quotes
            //         matchList.add(regexMatcher.group(1));
            //     } else if (regexMatcher.group(2) != null) {
            //         // Add single-quoted string without the quotes
            //         matchList.add(regexMatcher.group(2));
            //     } else {
            //         // Add unquoted string
            //         matchList.add(regexMatcher.group());
            //     }
            // }
            String[] args = fileLine.split(" +");
            fileMap.put(args[1], args[2]);
        }

        // handle profiling
        for (String profileLine : profileScript) {
            String[] args = profileLine.split(" +");
            LineProfile profile = new LineProfile();
            switch (args[2]) {
                case "hex":
                    profile.setLineColor(args[3]);
                    break;
                case "rgb":
                    String[] rgb = args[3].split(":");
                    profile.setLineColor(new Color(Integer.parseInt(rgb[0]), 
                                         Integer.parseInt(rgb[1]), Integer.parseInt(rgb[2])));
                    break;
                default:
                    throw new InvalidConfigFormatException("Invalid color format in profiling");
            }
            if (args[4] != null) {
                if (args[4].equals("weight")) {
                    if (args[5] == null) {
                        throw new InvalidConfigFormatException("No profile weight value specified");
                    }
                    float weight = Float.parseFloat(args[5]);
                    profile.setLineWeight(weight);
                }
                else {
                    throw new InvalidConfigFormatException("Invalid profile statement format");
                }
            }
            profiles.put(args[1], profile);
        }

        // image panel consists of just a single label
        JPanel imagePanel = new JPanel();
        // Get size of grid, should be refined later to a more reasonable approach
        int n = (int)(Math.sqrt((double)chartNum)+0.5);

        // set grid size
        imagePanel.setLayout(new GridLayout(n, n, 2, 2));
        charts = new ArrayList<ChartPanel>();

        // instantiate our JLabels and add them to the imagePanel
        for (int i = 0; i < chartNum; i++) {
            charts.add(new ChartPanel(null));
            charts.get(i).setVisible(true);
            imagePanel.add(charts.get(i));
        }
        imagePanel.setVisible(true);

        // Setup charts & series settings one time at the beginning
        ChartSettings chartSettings;
        int notitleCount = 0;
        for (String plotLine : plotScript) {
            // a "plot" level command
            if (plotLine.startsWith("plot ")) {
                chartSettings = new ChartSettings();
                // Regex to split on spaces when not in single or double quotes (thanks SOF)
                String[] args = plotLine.split(" +");
                // Verify correct format.
                if (!args[1].equals("xaxis") || !args[5].equals("yaxis")) {
                    throw new InvalidConfigFormatException("xaxis and yaxis not specified");
                }
                // Set axis types and titles
                switch(args[2]) {
                    case "num":
                        chartSettings.setXAxis(new NumberAxis(args[4]));
                        break;
                    case "log":
                        chartSettings.setXAxis(new LogAxis(args[4]));
                        break;
                    default:
                        throw new InvalidConfigFormatException("Invalid xaxis types specified");
                }
                switch(args[6]) {
                    case "num":
                        chartSettings.setYAxis(new NumberAxis(args[8]));
                        break;
                    case "log":
                        chartSettings.setYAxis(new LogAxis(args[8]));
                        break;
                    default:
                        throw new InvalidConfigFormatException("Invalid yaxis types specified");
                }
                cSettingsList.add(chartSettings);
                // TODO: Set Ranges if specified
            }
            // Put series settings in sSettings map (should make array later) will recode this whole
            // thing later
            else if (plotLine.startsWith("series ")) {
                String[] args = plotLine.split(" +");
                SeriesSettings settings = new SeriesSettings();
                sSettings.add(settings);
                switch (args[4]) {
                    case "notitle":
                        settings.setSeriesTitle("notitle" + notitleCount);
                        break;
                    default:
                        settings.setSeriesTitle(args[4]);
                        break;
                }
                
                settings.setLineProfile(profiles.get(args[6]));
                String[] columns = args[2].split(":");
                settings.setXColumn(Integer.parseInt(columns[0]));
                settings.setYColumn(Integer.parseInt(columns[1]));
                settings.setInputFile(fileMap.get(args[1]));
            }
        }

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

            // parse through the plot and series commands
            // necessary data for plot-level commands
            ChartSettings chartSettings;
            XYPlot plot = null;
            XYLineAndShapeRenderer defaultRenderer = null;
            int plotIndex = -1;
            int datasetIndex = -1;
            for (String line : plotScript) {
                
                if (line.startsWith("plot ")) {
                    
                    plotIndex+=1;
                    chartSettings = cSettingsList.get(plotIndex);
                    defaultRenderer = new XYLineAndShapeRenderer();
                    plot = new XYPlot(null, chartSettings.getXAxis(), chartSettings.getYAxis(), 
                                        defaultRenderer);
                    JFreeChart chart = new JFreeChart(plot);
                    charts.get(plotIndex).setChart(chart);
                }
                
                else if (line.startsWith("series ")) {
                    datasetIndex += 1;
                    // Split args
                    String[] args = line.split(" +");

                    // Load data from file
                    String filename = args[1];
                    List<String> dataLines = loadData(new File(fileMap.get(filename)));

                    // Process data into usable format
                    SeriesSettings seriesSettings = sSettings.get(datasetIndex);
                    DataAnalytics dataAnalytics = processData(dataLines, 
                                        seriesSettings.getXColumn(), seriesSettings.getYColumn());

                    // Add data to dataset
                    DefaultXYDataset data = new DefaultXYDataset();
                    data.addSeries(seriesSettings.getSeriesTitle(), dataAnalytics.getData());
                    plot.setDataset(datasetIndex, data);

                    // Setup series renderer and chart
                    LineProfile profile = seriesSettings.getLineProfile();
                    
                    defaultRenderer.setSeriesPaint(0, profile.getLineColor());
                    defaultRenderer.setSeriesStroke(0, new BasicStroke(profile.getLineWeight()));
                    if (seriesSettings.getSeriesTitle().substring(0, 7).equals("notitle")) {
                        defaultRenderer.setSeriesVisibleInLegend(0, false, true);
                    }
                }
                // Try is to prevent it from crashing on invalid ranges.
                // try {
                //     // This is to make it look nice on a LogAxis.  It still needs work at low values.
                //     if (yAxis instanceof LogAxis) {
                //         // ((LogAxis)yAxis).setSmallestValue(sInfo.min - 0.000000000001);
                //         ((LogAxis)yAxis).setRange(new Range(sInfo.min, sInfo.max + 4));
                //     }
                //     XYPlot plot = new XYPlot(dataset, xAxis, yAxis, renderer);
                    // Ghosting stuff
                    // if (ghosting) {
                    //     for (int i = 0; i < dataset.getSeriesCount(); i++) {
                    //         Color color = Color.red;
                    //         renderer.setSeriesPaint(i, color);
                    //         DefaultXYDataset ghostSet = new DefaultXYDataset();
                    //         int j = 0;
                    //         for (double[][] ghostData : ghosts.getQueue(title[i])) {
                    //             if (j == 0) {
                    //                 j++;
                    //                 continue;
                    //             }
                    //             ghostSet.addSeries(title[i] + j, ghostData);
                    //             j++;
                    //         }
                    //         plot.setDataset(i + dataset.getSeriesCount(), ghostSet);
                    //         plot.setRenderer(i + dataset.getSeriesCount(), new XYLineAndShapeRenderer());
                    //         for (int k = 0; k < j; k++) {
                    //             color = color.darker();
                    //             plot.getRendererForDataset(ghostSet).setSeriesPaint(k, color);
                    //         }
                    //     }
                    // }
                //     chart = new JFreeChart(plot);
                    
                // }
                // catch (IllegalArgumentException ex) {
                //     chart = null;
                // }
                // charts.get(count).setChart(chart);
                // count++;
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
     * 
     * @param lines your String lines
     * @param x your x column
     * @param y your y column
     * @return the data in a DataAnalytics object
     */
    private DataAnalytics processData(List<String> lines, int x, int y) {
        double[][] data = new double[2][lines.size()];
        try {
            int lineCounter = 0;
            int indexCounter = 0;
            while (lineCounter < lines.size()) {
                // Split on one or more spaces
                String[] numbers = lines.get(lineCounter).split(" +");
                // NAN and INF handling
                if (numbers[x-1].equals("nan") || numbers[y-1].equals("nan") 
                    || numbers[x-1].equals("-nan") || numbers[y-1].equals("-nan")
                    || numbers[x-1].equals("inf") || numbers[y-1].equals("inf")
                    || numbers[x-1].equals("-inf") || numbers[y-1].equals("-inf")) {
                    lineCounter+=1;
                    continue;
                }
                // Grab the values into our array
                data[0][indexCounter] = Double.parseDouble(numbers[x-1]);
                double yValue = Double.parseDouble(numbers[y-1]);
                data[1][indexCounter] = yValue;
                indexCounter+=1;
                lineCounter+=1;
            }
        }
        catch (ArrayIndexOutOfBoundsException ex) {
            // Helps to notify the user if they're trying something silly.
            System.out.println("There is no valid column " + x + " or " + y + ".");
            ex.printStackTrace();
        }
        return new DataAnalytics(data);
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
