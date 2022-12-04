package jmu.reu.ode.settings;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jfree.chart.axis.LogAxis;
import org.jfree.chart.axis.NumberAxis;

import jmu.reu.ode.view.ODEView;

/**
 * A JPanel that contains options for editing the chartSettings of a 
 * chart on the viewer.
 * 
 * @author Benjamin Huber
 * @version 10/20/2022
 */
public class ChartSettingsPanel extends JPanel implements ChangeListener {
    private ChartSettings settings;
    private ODEView parent;
    private ButtonGroup xAxisTypeGroup;
    private ButtonGroup yAxisTypeGroup;

    public ChartSettingsPanel(ODEView parent, ChartSettings chartSettings) {
        super();

        this.settings = chartSettings;
        this.parent = parent;

        if (chartSettings == null) {
            this.add(new JLabel("Settings not available for this chart"));
            return;
        }

        JLabel graphName = new JLabel(chartSettings.getXAxis().getLabel() + " vs. " + 
                                      chartSettings.getYAxis().getLabel());
        
        JLabel xAxisLabel = new JLabel("xAxis Type");
        JRadioButton xAxisLog = new JRadioButton("Log");
        xAxisLog.setActionCommand("x_axis_log_toggle");
        JRadioButton xAxisNum = new JRadioButton("Numeric");
        xAxisNum.setActionCommand("x_axis_num_toggle");
        xAxisTypeGroup = new ButtonGroup();
        xAxisTypeGroup.add(xAxisLog);
        xAxisTypeGroup.add(xAxisNum);
        
        JLabel yAxisLabel = new JLabel("yAxis Type");
        JRadioButton yAxisLog = new JRadioButton("Log");
        yAxisLog.setActionCommand("y_axis_log_toggle");
        JRadioButton yAxisNum = new JRadioButton("Numeric");
        yAxisNum.setActionCommand("y_axis_num_toggle");
        yAxisTypeGroup = new ButtonGroup();
        yAxisTypeGroup.add(yAxisLog);
        yAxisTypeGroup.add(yAxisNum);

        if (settings.getXAxis() instanceof NumberAxis) {
            xAxisNum.setSelected(true);
        } else {
            xAxisLog.setSelected(true);
        }

        if (settings.getYAxis() instanceof NumberAxis) {
            yAxisNum.setSelected(true);
        } else {
            yAxisLog.setSelected(true);
        }

        this.setLayout(new GridLayout(0, 1));

        JPanel xChoicePanel = new JPanel();
        xChoicePanel.setLayout(new GridLayout(1, 0));
        xChoicePanel.add(xAxisLabel);
        xChoicePanel.add(xAxisNum);
        xChoicePanel.add(xAxisLog);

        JPanel yChoicePanel = new JPanel();
        yChoicePanel.setLayout(new GridLayout(1, 0));
        yChoicePanel.add(yAxisLabel);
        yChoicePanel.add(yAxisNum);
        yChoicePanel.add(yAxisLog);

        this.add(graphName);
        this.add(xChoicePanel);
        this.add(yChoicePanel);

        xAxisNum.addChangeListener(this);
        xAxisLog.addChangeListener(this);

        yAxisNum.addChangeListener(this);
        yAxisLog.addChangeListener(this);
        
        this.setBorder(BorderFactory.createLineBorder(Color.gray));
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        String xActionCommand = xAxisTypeGroup.getSelection().getActionCommand();

        String yActionCommand = yAxisTypeGroup.getSelection().getActionCommand();

        switch (xActionCommand) {
            case "x_axis_log_toggle":
                settings.setXAxis(new LogAxis(settings.getXAxis().getLabel()));
                break;
            case "x_axis_num_toggle":
                settings.setXAxis(new NumberAxis(settings.getXAxis().getLabel()));
                break;
        }

        switch (yActionCommand) {
            case "y_axis_log_toggle":
                settings.setYAxis(new LogAxis(settings.getYAxis().getLabel()));
                break;
            case "y_axis_num_toggle":
                settings.setYAxis(new NumberAxis(settings.getYAxis().getLabel()));
                break;
        }

        // Spaghetti
        parent.actionPerformed(new ActionEvent(this, 1, "spaghet"));
    }
}
