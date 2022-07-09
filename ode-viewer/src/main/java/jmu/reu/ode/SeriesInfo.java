package jmu.reu.ode;

/**
 * A class that represents the info we parse from our series in our data file.  I'm using this 
 * to keep track of data we might need for chart config, such as min/max values.
 * 
 * @author Benjamin Huber
 * @version 7/9/2022
 */
public class SeriesInfo
{
    public double min;
    public double max;
    public double[][] data;

    /**
     * Constructor for making a series from just a data array.  Min and max will be instantiated
     * to nonsensical values, so it is recommended to use the other constructor unless you will 
     * be setting them later.
     * 
     * @param data the data array
     */
    public SeriesInfo(double[][] data) {
        this.data = data;
        this.min = Double.MAX_VALUE;
        this.max = Double.MIN_VALUE;
    }

    /**
     * A more explicit constructor that sets the data array, the min, and the max values.
     * 
     * @param data the data array
     * @param min the min value
     * @param max the max value
     */
    public SeriesInfo(double[][] data, double min, double max) {
        this.data = data;
        this.min = min;
        this.max = max;
    }

    // I could add error handling for setting the min and the max, but for this program I don't 
    // see an express need to, so for now I won't.
}