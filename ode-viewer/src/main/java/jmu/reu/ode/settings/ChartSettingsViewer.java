package jmu.reu.ode.settings;

import java.util.List;
import java.awt.GridLayout;

import javax.swing.JPanel;

import jmu.reu.ode.view.ODEView;

public class ChartSettingsViewer extends JPanel {
    public ChartSettingsViewer(ODEView parent, List<ChartSettings> cSettingsList) {
        super();

        int n = (int)(Math.sqrt((double)cSettingsList.size())+0.5);

        this.setLayout(new GridLayout(n, n, 2, 2));
        
        for (ChartSettings settings : cSettingsList) {
            ChartSettingsPanel panel;
            if (settings.getXAxis() == null) {
                panel = new ChartSettingsPanel(parent, null);
            } else {
                panel = new ChartSettingsPanel(parent, settings);
            }
            this.add(panel);
        }
    }
}
