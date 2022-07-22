package jmu.reu.ode;

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

    /**
     * Default constructor.
     */
    public ChartSettings() {
        xAxis = null;
        yAxis = null;
        xRange = null;
        yRange = null;
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
}
