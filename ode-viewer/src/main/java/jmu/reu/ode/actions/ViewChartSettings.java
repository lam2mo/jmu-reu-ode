package jmu.reu.ode.actions;

import java.util.List;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JFrame;

import jmu.reu.ode.app.ODEViewer;
import jmu.reu.ode.settings.ChartSettings;
import jmu.reu.ode.settings.ChartSettingsViewer;

/**
 * A class that encapsulates the action of opening a ChartSettingsPanel.
 * 
 * @author Benjamin Huber
 * @version 10/20/2022
 */
public class ViewChartSettings extends AbstractAction {
    private ODEViewer parent;

    public ViewChartSettings(ODEViewer parent) {
        this.parent = parent;
        putValue(AbstractAction.NAME, "Chart Settings");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        List<ChartSettings> cSettingsList = parent.getView().getChartSettings();
        
        JFrame settingsViewer = new JFrame();

        settingsViewer.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        settingsViewer.add(new ChartSettingsViewer(parent.getView(), cSettingsList));

        settingsViewer.setSize(500, 500);

        settingsViewer.setVisible(true);
    }
}
