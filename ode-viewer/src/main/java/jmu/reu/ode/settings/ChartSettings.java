package jmu.reu.ode.settings;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.data.Range;

/**
 * A class that encapsulates all of the "chart level settings" that we need to keep around.
 * 
 * @author Benjamin Huber
 * @version 7/20/2022
 */
public class ChartSettings {
    private ValueAxis xAxis;
    private ValueAxis yAxis;
    private Range xRange;
    private Range yRange;
    private boolean autoXRange;
    private boolean autoYRange;

    /**
     * Default constructor.
     */
    public ChartSettings() {
        xAxis = null;
        yAxis = null;
        xRange = null;
        yRange = null;
        autoXRange = true;
        autoYRange = true;
    }

    /**
     * Accessor method for xAxis attribute.
     * 
     * @return xAxis attribute
     */
    public ValueAxis getXAxis() {
        return xAxis;
    }

    /**
     * Accessor method for yAxis attribute.
     * 
     * @return yAxis attribute
     */
    public ValueAxis getYAxis() {
        return yAxis;
    }

    /**
     * Accessor method for xRange attribute.
     * 
     * @return xRange attribute
     */
    public Range getXRange() {
        return xRange;
    }

    /**
     * Accessor method for yRange attribute.
     * 
     * @return yRange attribute
     */
    public Range getYRange() {
        return yRange;
    }

    /**
     * Accessor method for autoXRange attribute.
     * 
     * @return autoXRange attribute
     */
    public boolean getAutoXRange() {
        return autoXRange;
    }

    /**
     * Accessor method for autoYRange attibute.
     * 
     * @return autoYRange attribute
     */
    public boolean getAutoYRange() {
        return autoYRange;
    }

    /**
     * Mutator method for xAxis attribute.
     * 
     * @param newAxis the new xAxis
     */
    public void setXAxis(ValueAxis newAxis) {
        this.xAxis = newAxis;
    }

    /**
     * Mutator method for yAxis attribute.
     * 
     * @param newAxis the new yAxis
     */
    public void setYAxis(ValueAxis newAxis) {
        this.yAxis = newAxis;
    }

    /**
     * Mutator method for xRange attribute.
     * 
     * @param newRange the new xRange
     */
    public void setXRange(Range newRange) {
        this.xRange = newRange;
    }

    /**
     * Mutator method for yRange attribute.
     * 
     * @param newRange the new yRange
     */
    public void setYRange(Range newRange) {
        this.yRange = newRange;
    }

    /**
     * Mutator method for autoXRange attribute.
     * 
     * @param auto whether the x Axis should be autoRanged or not.
     */
    public void setAutoXRange(boolean auto) {
        this.autoXRange = auto;
    }

    /**
     * Mutator method for autoYRange attribute
     * 
     * @param auto whether the y Axis should be autoRanged or not.
     */
    public void setAutoYRange(boolean auto) {
        this.autoYRange = auto;
    }
}
