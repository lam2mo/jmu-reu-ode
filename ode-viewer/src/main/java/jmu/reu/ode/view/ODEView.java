
package jmu.reu.ode.view;
/**
 * JMU REU 2022
 * 
 * Container for the data needed to represent a singular view of an ODE.
 * 
 * @author Mike Lam, Benjamin Huber
 * @version 10/20/2022
 */
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.JCheckBox;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.LogAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.ui.Layer;
import org.jfree.data.Range;
import org.jfree.data.xy.DefaultXYDataset;

import jmu.reu.ode.data.ChoiceParameter;
import jmu.reu.ode.data.DataAnalytics;
import jmu.reu.ode.data.FileSettings;
import jmu.reu.ode.data.LineProfile;

import jmu.reu.ode.data.MarkSettings;
import jmu.reu.ode.data.NumericParameter;
import jmu.reu.ode.data.Parameter;
import jmu.reu.ode.data.SeriesSettings;
import jmu.reu.ode.error.InvalidConfigFormatException;
import jmu.reu.ode.gui.JSnapSlider;
import jmu.reu.ode.settings.ChartSettings;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JTextField;


public class ODEView extends JPanel implements ChangeListener, DocumentListener, ActionListener {
    private final double PARAM_FACTOR = 100.0;
    private boolean updating = false;
    private int chartNum = 0;
    private JLabel description;
    private List<ChartPanel> charts;
    private List<ChartSettings> cSettingsList;
    private List<String> commands;
    private List<String> plotScript;
    private List<String> profileScript;
    private List<String> setScript;
    private Map<String, Object> fields;
    private Map<String, JSlider> sliders;
    private Map<String, Parameter> parameters;
    private Map<String, LineProfile> profiles;
    private Map<String, FileSettings> fileMap;
    private List<SeriesSettings> sSettings;
    private List<MarkSettings> mSettings;
    private String title;
    private String divider = " +";

    private boolean linespoints = false;

    public ODEView (File configFile) {
        // List of all the image filenames that appear in the file.
        description = new JLabel();
        parameters = new TreeMap<String, Parameter>();
        List<Parameter> orderedParameters = new ArrayList<Parameter>();
        commands = new ArrayList<String>();
        plotScript = new ArrayList<String>();
        setScript = new ArrayList<String>();
        sSettings = new ArrayList<>();
        mSettings = new ArrayList<>();
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
                    NumericParameter p = new NumericParameter(args[1],
                                Double.parseDouble(args[2]),
                                Double.parseDouble(args[3]),
                                Double.parseDouble(args[4]),
                                Double.parseDouble(args[5]));
                    parameters.put(p.getLabel(), p);
                    orderedParameters.add(p);
                } else if (line.startsWith("choice")) {
                    String[] args = line.split("\\s+");
                    // copy everything after index 0 into choices array
                    String[] choices = new String[args.length - 2];
                    for (int i = 0; i < choices.length; i++) {
                        choices[i] = args[i+2];
                    }
                    ChoiceParameter p = new ChoiceParameter(args[1], choices);
                    parameters.put(p.getLabel(), p);
                    orderedParameters.add(p);
                } else if (line.startsWith("run")) {
                    commands.add(line.replaceFirst("run ", ""));
                } else if (line.startsWith("plot")) {
                    plotScript.add(line);
                    chartNum+=1;
                } else if (line.startsWith("error")) {
                    plotScript.add(line);
                    chartNum+=1;
                } else if (line.startsWith("static series")) {
                    plotScript.add(line);
                } else if (line.startsWith("series")) {
                    plotScript.add(line);
                } else if (line.startsWith("mark")) {
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

        for (String setLine : setScript) {
            String[] args = setLine.split(" +");
            switch (args[0]) {
                case "filetype":
                    switch (args[1]) {
                        case "csv":
                            divider = ",";
                            break;
                        case "dat":
                            divider = " +";
                            break;
                    }
                    break;
                default:
                    throw new InvalidConfigFormatException("Invalid set command format");
            }
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
            FileSettings fileSettings = new FileSettings(args[2]);
            if (args.length == 5) {
                if (!args[3].equals("skip")) {
                    throw new InvalidConfigFormatException("Invalid file line format");
                }
                fileSettings.setFileSkip(Integer.parseInt(args[4]));
            }
            fileMap.put(args[1], fileSettings);
        }

        // handle profiling
        for (String profileLine : profileScript) {
            String[] args = profileLine.split(" +");
            LineProfile profile = new LineProfile();
            
            // handle color
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
            
            // handle stroke
            float weight;
            if (args[4].equals("weight")) {
                if (args[5] == null) {
                    throw new InvalidConfigFormatException("No profile weight value specified");
                }
                weight = Float.parseFloat(args[5]);
            }
            else {
                throw new InvalidConfigFormatException("Invalid profile statement format");
            }
            switch (args[6]) {
                case "solid":
                    profile.setStroke(new BasicStroke(weight));
                    break;
                case "dashed":
                    profile.setStroke(new BasicStroke(weight, BasicStroke.CAP_SQUARE, 
                                        BasicStroke.JOIN_BEVEL, (float)0, new float[]{0.5f, 10}, 0));
                    break;
                default:
                    throw new InvalidConfigFormatException("Invalid linetype specified");
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
            
            if (plotLine.startsWith("error ")) {
                cSettingsList.add(new ChartSettings());
            }
            else if (plotLine.startsWith("plot ")) {
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
                        if (!args[3].equals("[::]")) {
                            String rangeValues = args[3].substring(1, args[3].length() - 1);
                            String[] rangeValuesList = rangeValues.split(":");
                            double lowerBound = Double.parseDouble(rangeValuesList[0]);
                            double upperBound = Double.parseDouble(rangeValuesList[2]);
                            chartSettings.setXRange(new Range(lowerBound, upperBound));
                            chartSettings.setAutoXRange(false);
                        }
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
                        if (!args[7].equals("[::]")) {
                            String rangeValues = args[7].substring(1, args[7].length() - 1);
                            String[] rangeValuesList = rangeValues.split(":");
                            double lowerBound = Double.parseDouble(rangeValuesList[0]);
                            double upperBound = Double.parseDouble(rangeValuesList[2]);
                            chartSettings.setYRange(new Range(lowerBound, upperBound));
                            chartSettings.setAutoYRange(false);
                        }
                        break;
                    case "log":
                        chartSettings.setYAxis(new LogAxis(args[8]));
                        break;
                    default:
                        throw new InvalidConfigFormatException("Invalid yaxis types specified");
                }
                
                cSettingsList.add(chartSettings);
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
                        notitleCount+=1;
                        break;
                    default:
                        settings.setSeriesTitle(args[4]);
                        break;
                }
                
                settings.setLineProfile(profiles.get(args[6]));
                String[] columns = args[2].split(":");
                settings.setXColumn(Integer.parseInt(columns[0]));
                settings.setYColumn(Integer.parseInt(columns[1]));
                settings.setInputFile(fileMap.get(args[1]).getFileName());
            }

            // static series parsing
            else if (plotLine.startsWith("static series ")) {
                String args[] = plotLine.split(" +");
                
                // Load in dataset
                FileSettings fileSettings = fileMap.get(args[2]);
                List<String> lines = loadData(new File(fileSettings.getFileName()),
                                              fileSettings.getFileSkip());
                String numbers[] = args[3].split(":");
                DataAnalytics data = processData(lines, Integer.parseInt(numbers[0]), 
                                                                    Integer.parseInt(numbers[1]));

                // Load in settings
                SeriesSettings seriesSettings = new SeriesSettings(data);
                seriesSettings.setLineProfile(profiles.get(args[7]));
                seriesSettings.setSeriesTitle(args[5]);

                // Add series settings to list
                sSettings.add(seriesSettings);
            }

            // mark parsing
            else if (plotLine.startsWith("mark ")) {
                String args[] = plotLine.split(" +");
                MarkSettings mOptions = new MarkSettings();

                switch(args[1]) {
                    case "x":
                        mOptions.setDomain(true);
                        break;
                    case "y":
                        mOptions.setDomain(false);
                        break;
                    default:
                        throw new InvalidConfigFormatException("Invalid mark x/y option");
                }

                mOptions.setValue(Double.parseDouble(args[2]));

                switch (args[3]) {
                    case "foreground":
                        mOptions.setForeground(true);
                        break;
                    case "background":
                        mOptions.setForeground(false);
                        break;
                    default:
                        throw new InvalidConfigFormatException("Invalid mark foreground/background option");
                }

                mOptions.setProfile(profiles.get(args[5]));
                mSettings.add(mOptions);
            }
        }

        // slider panel consists of a stack of labels and sliders
        JPanel sliderPanel = new JPanel();
        sliderPanel.setLayout(new BoxLayout(sliderPanel, BoxLayout.PAGE_AXIS));
        sliderPanel.add(description);
        JCheckBox pointsCheckBox = new JCheckBox("Points?");
        pointsCheckBox.addActionListener(this);
        sliderPanel.add(pointsCheckBox);
        fields = new TreeMap<String, Object>();
        sliders = new TreeMap<String, JSlider>();
        for (Parameter p : orderedParameters) {
            JPanel lblPanel = new JPanel();
            lblPanel.setLayout(new BoxLayout(lblPanel, BoxLayout.LINE_AXIS));
            JLabel lbl = new JLabel("   " + p.getLabel() + " = ");
            if (p instanceof NumericParameter) {
                NumericParameter numParam = (NumericParameter) p;
                JTextField field = new JTextField("" + numParam.def, 5);
                field.getDocument().addDocumentListener(this);
                fields.put(p.getLabel(), field);
                lblPanel.add(lbl);
                lblPanel.add(field);
                JPanel tmpPanel = new JPanel();
                tmpPanel.setLayout(new BorderLayout());
                JSnapSlider slider = new JSnapSlider(
                        (int)(numParam.min * PARAM_FACTOR),
                        (int)(numParam.max * PARAM_FACTOR),
                        (int)(numParam.def * PARAM_FACTOR),
                        (int)(numParam.snap * PARAM_FACTOR));
                slider.addChangeListener(this);
                sliders.put(p.getLabel(), slider);
                tmpPanel.add(lblPanel, BorderLayout.WEST);
                tmpPanel.add(slider, BorderLayout.CENTER);
                sliderPanel.add(tmpPanel);
            }
            if (p instanceof ChoiceParameter) {
                ChoiceParameter cParam = (ChoiceParameter) p;
                JLabel selected = new JLabel(cParam.options[0]);
                lblPanel.add(lbl);
                lblPanel.add(selected);
                JPanel tmpPanel = new JPanel();
                tmpPanel.setLayout(new BorderLayout());
                ButtonGroup group = new ButtonGroup();
                JPanel choicesPanel = new JPanel();
                choicesPanel.setLayout(new GridLayout(1, cParam.options.length));
                int i = 0;
                for (String choice : cParam.options) {
                    JRadioButton button = new JRadioButton(choice);
                    button.setActionCommand(cParam.options[i]);
                    if (i == 0) { // set initial option to selected
                        button.setSelected(true);
                    }
                    button.addChangeListener(this);
                    group.add(button);
                    choicesPanel.add(button);
                    i++;
                }
                fields.put(cParam.getLabel(), group);
                tmpPanel.add(lblPanel, BorderLayout.WEST);
                tmpPanel.add(choicesPanel, BorderLayout.CENTER);
                sliderPanel.add(tmpPanel);
            }
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

    /* 
     * Accessor method for the chartSettingsList attribute.
     * 
     * @return the chartSettingsList attribute.
     */
    public List<ChartSettings> getChartSettings() {
        return cSettingsList;
    }

    public void updatePlot()
    {
        try {
            // run all commands
            for (String cmd : commands) {
                // cmd = cmd.replaceAll("./", configFileLocation + "/");
                for (Parameter p : parameters.values()) {
                    if (p instanceof NumericParameter) {
                        NumericParameter numParam = (NumericParameter) p;
                        cmd = cmd.replaceAll("\\$"+ numParam.getLabel(), "" + numParam.value);
                    } else if (p instanceof ChoiceParameter) {
                        ChoiceParameter cParam = (ChoiceParameter) p;
                        cmd = cmd.replaceAll("\\$" + cParam.getLabel(), "" + cParam.selected);
                    }
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
            DefaultXYDataset data = null;
            int plotIndex = -1;
            int listIndex = -1;
            int datasetIndex = -1;
            int markIndex = -1;
            for (String line : plotScript) {
                
                if (line.startsWith("error ")) {
                    plotIndex+=1;
                    datasetIndex = -1;
                    defaultRenderer = new XYLineAndShapeRenderer(true, linespoints);
                    plot = new XYPlot(null, new NumberAxis("Aligned Value"), 
                                        new NumberAxis("Errors"), defaultRenderer);
                    data = new DefaultXYDataset();
                    plot.setDataset(data);
                    JFreeChart chart = new JFreeChart(plot);
                    charts.get(plotIndex).setChart(chart);

                    String[] args = line.split(" +");

                    String file1 = args[2];
                    String file2 = args[5];

                    data = new DefaultXYDataset();
                    plot.setDataset(data);

                    File bigFile1 = new File(fileMap.get(file1).getFileName());
                    File bigFile2 = new File(fileMap.get(file2).getFileName());

                    String[] cols1 = args[3].split(":");
                    String[] cols2 = args[6].split(":");

                    int code;

                    switch (args[1]) {
                        case "diff":
                            code = 0;
                            break;
                        case "abs":
                            code = 1;
                            break;
                        case "rel":
                            code = 2;
                            break;
                        default:
                            code = 0;
                            break;
                    }

                    data.addSeries(args[1], loadError(bigFile1, Integer.parseInt(cols1[0]),
                                                                Integer.parseInt(cols1[1]),
                                                      bigFile2, Integer.parseInt(cols2[0]),
                                                                Integer.parseInt(cols2[1]),
                                                      0, 0, code));
                    
                    defaultRenderer.setSeriesPaint(0, profiles.get(args[8]).getLineColor());
                    defaultRenderer.setSeriesStroke(0, profiles.get(args[8]).getStroke());
                }
                else if (line.startsWith("plot ")) {
                    
                    plotIndex+=1;
                    // System.out.println("Plotting plot: " + plotIndex);
                    datasetIndex = -1;
                    chartSettings = cSettingsList.get(plotIndex);
                    defaultRenderer = new XYLineAndShapeRenderer(true, linespoints);
                    ValueAxis xAxis = chartSettings.getXAxis();
                    ValueAxis yAxis = chartSettings.getYAxis();
                    if (!chartSettings.getAutoXRange()) {
                        xAxis.setRange(chartSettings.getXRange());
                    }
                    if (!chartSettings.getAutoYRange()) {
                        yAxis.setRange(chartSettings.getYRange());
                    }
                    plot = new XYPlot(null, xAxis, yAxis, defaultRenderer);
                    data = new DefaultXYDataset();
                    plot.setDataset(data);
                    JFreeChart chart = new JFreeChart(plot);
                    charts.get(plotIndex).setChart(chart);
                }
                
                else if (line.startsWith("series ")) {
                    listIndex += 1;
                    datasetIndex += 1;
                    // Split args
                    
                    String[] args = line.split(" +");

                    // Load data from file
                    String filename = args[1];
                    FileSettings fileSettings = fileMap.get(filename);
                    List<String> dataLines = loadData(new File(fileSettings.getFileName()),
                                                      fileSettings.getFileSkip());

                    // Process data into usable format
                    SeriesSettings seriesSettings = sSettings.get(listIndex);
                    // System.out.println("Plotting series " + datasetIndex + " titled " + seriesSettings.getSeriesTitle() + " on plot " + plotIndex);
                    DataAnalytics dataAnalytics = processData(dataLines, 
                                        seriesSettings.getXColumn(), seriesSettings.getYColumn());

                    // Add data to dataset
                    data.addSeries(seriesSettings.getSeriesTitle(), dataAnalytics.getData());

                    // Setup series renderer and chart
                    LineProfile profile = seriesSettings.getLineProfile();
                    
                    defaultRenderer.setSeriesPaint(datasetIndex, profile.getLineColor());
                    defaultRenderer.setSeriesStroke(datasetIndex, profile.getStroke());
                    if (seriesSettings.getSeriesTitle().length() > 7 && 
                                seriesSettings.getSeriesTitle().substring(0, 7).equals("notitle")) {
                        defaultRenderer.setSeriesVisibleInLegend(datasetIndex, false, true);
                    }
                }

                else if (line.startsWith("static series ")) {
                    datasetIndex+=1;
                    listIndex+=1;

                    // Split args
                    SeriesSettings seriesSettings = sSettings.get(listIndex);
                    // System.out.println("Plotting static series " + datasetIndex + " titled " + 
                                        // seriesSettings.getSeriesTitle() + " on plot " + plotIndex);

                    // Make dataset
                    data.addSeries(seriesSettings.getSeriesTitle(), 
                                                                seriesSettings.getData().getData());

                    // set profile
                    LineProfile profile = seriesSettings.getLineProfile();
                    defaultRenderer.setSeriesPaint(datasetIndex, profile.getLineColor());
                    defaultRenderer.setSeriesStroke(datasetIndex, profile.getStroke());
                    if (seriesSettings.getSeriesTitle().length() > 7 && 
                                seriesSettings.getSeriesTitle().substring(0, 7).equals("notitle")) {
                        defaultRenderer.setSeriesVisibleInLegend(datasetIndex, false, true);
                    }
                }
                
                else if (line.startsWith("mark ")) {
                    markIndex += 1;
                    MarkSettings mOptions = mSettings.get(markIndex);

                    LineProfile profile = mOptions.getProfile();
                    Layer layer = null;
                    if (mOptions.getForeground()) {
                        layer = Layer.FOREGROUND;
                    }
                    else {
                        layer = Layer.BACKGROUND;
                    }
                    if (mOptions.getDomain()) {
                        plot.addDomainMarker(new ValueMarker(mOptions.getValue(), 
                            profile.getLineColor(), profile.getStroke()), layer);
                    }
                    else {
                        plot.addRangeMarker(new ValueMarker(mOptions.getValue(), 
                            profile.getLineColor(), profile.getStroke()), layer);
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
        int lineCounter = 0;    
        int indexCounter = 0;
        double[][] shortenedData = null;
        try {
            
            while (lineCounter < lines.size()) {
                // Split on one or more spaces
                String[] numbers = lines.get(lineCounter).split(divider);

                if (numbers.length <= 1) {
                    lineCounter+=1;
                    continue;
                }
                // NAN and INF handling
                if (numbers[x-1].equals("nan") || numbers[y-1].equals("nan") 
                    || numbers[x-1].equals("-nan") || numbers[y-1].equals("-nan")
                    || numbers[x-1].equals("inf") || numbers[y-1].equals("inf")
                    || numbers[x-1].equals("-inf") || numbers[y-1].equals("-inf")) {
                    lineCounter+=1;
                    continue;
                }
                try {
                    // Grab the values into our array
                    double yValue = Double.parseDouble(numbers[y-1]);
                    double xValue = Double.parseDouble(numbers[x-1]);
                    data[0][indexCounter] = xValue;
                    
                    data[1][indexCounter] = yValue;
                    indexCounter+=1;
                } catch (NumberFormatException ex) { /* probably a header line */ }
                lineCounter+=1;
            }
            shortenedData = new double[2][indexCounter];

            for (int i = 0; i < indexCounter; i++) {
                shortenedData[0][i] = data[0][i];
            }
            for (int i = 0; i < indexCounter; i++) {
                shortenedData[1][i] = data[1][i];
            }
        }
        catch (ArrayIndexOutOfBoundsException ex) {
            // Helps to notify the user if they're trying something silly.
            System.out.println("There is no valid column " + x + " or " + y + ".");
            ex.printStackTrace();
        }
        
        return new DataAnalytics(shortenedData);
    }

    private double[][] loadError(File file1, int xCol1, int yCol1, 
                                                File file2, int xCol2, int yCol2, int a1Align, int a2Align, int code) {
        ArrayList<String> file1Lines = loadData(file1, 1);
        ArrayList<String> file2Lines = loadData(file2, 1);

        DataAnalytics data1 = processData(file1Lines, xCol1, yCol1);
        DataAnalytics data2 = processData(file2Lines, xCol2, yCol2);
        
        return matchCull(data1, data2, a1Align, a2Align, code);
    }

    private double[][] matchCull(DataAnalytics data1, DataAnalytics data2, int a1Align, int a2Align,
                                    int code) {
        double[][] array1 = data1.getData();
        double[][] array2 = data2.getData();

        double[][] output = new double[2][Math.max(array1[0].length, array2[0].length)];

        int count = 0;
        for (int i = 0; i < array1[0].length; i++) {
            for (int j = 0; j < array2[0].length; j++) {
                if (array1[a1Align][i] == array2[a2Align][j]) {
                    output[0][count] = array1[a1Align][i];
                    double dataPoint;
                    switch(code) {
                        case 0:
                            dataPoint = array1[1-a1Align][i] - array2[1-a2Align][j];
                            break;
                        case 1:
                            dataPoint = Math.abs(array1[1-a1Align][i] - array2[1-a2Align][j]);
                            break;
                        case 2:
                            if (array2[1-a2Align][j] == 0) {
                                continue;
                            }
                            dataPoint = Math.abs(array1[1-a1Align][i] - array2[1-a2Align][j])/array2[1-a2Align][j];
                            break;
                        default:
                            dataPoint = 80.0; // shouldn't happen
                    }
                    output[1][count] = dataPoint;
                    count++;
                    continue;
                }
            }
        }

        double[][] newOutput = new double[2][count];
        for (int i = 0; i < count; i++) {
            newOutput[0][i] = output[0][i];
            newOutput[1][i] = output[1][i];
        }

        return newOutput;
    }

    /**
     * Helper method to load data from a file.
     * @param file the file to load data from
     * @return the lines of the file as an arrayList
     */
    private ArrayList<String> loadData(File file, int fileSkip) {
        ArrayList<String> lines = new ArrayList<String>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            int counter = fileSkip;
            while ((line = reader.readLine()) != null) {
                if (counter == fileSkip) {
                    lines.add(line.trim());
                    counter = 0;
                }
                counter += 1;
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
                if (p instanceof NumericParameter) {
                    NumericParameter numParam = (NumericParameter) p;
                    double value = Double.parseDouble(((JTextField) fields.get(numParam.getLabel())).getText());
                    numParam.value = value;
                    sliders.get(numParam.getLabel()).setValue((int)(value * PARAM_FACTOR));
                }
                else if (p instanceof ChoiceParameter) {
                    ChoiceParameter cParam = (ChoiceParameter) p;
                    ButtonModel selected = ((ButtonGroup) fields.get(cParam.getLabel())).getSelection();
                    String text = selected.getActionCommand();
                    cParam.selected = text;
                }
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
            if (p instanceof NumericParameter) {
                NumericParameter numParam = (NumericParameter) p;
                double value = (double)(sliders.get(numParam.getLabel()).getValue()) / PARAM_FACTOR;
                numParam.value = value;

                ((JTextField) fields.get(numParam.getLabel())).setText("" + value);
            }
            else if (p instanceof ChoiceParameter) {
                ChoiceParameter cParam = (ChoiceParameter) p;
                ButtonModel selected = ((ButtonGroup) fields.get(cParam.getLabel())).getSelection();
                String text = selected.getActionCommand();
                cParam.selected = text;
            }
        }
        updatePlot();
        updating = false;
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        if (updating) return;
        String source = arg0.getActionCommand();
        updating = true;
        if (source.equals("Points?")) {
            linespoints = !linespoints;
            
        }
        updatePlot();
        updating = false;
        
    }
}
