package jmu.reu.ode.data;

/**
 * A class that keeps track of data and important info about it such as min and max so that we can
 * better format the graphs.
 * 
 * @author Benjamin Huber
 * @version 7/14/2022
 */
public class DataAnalytics {
    private double maxY = Double.MIN_VALUE;
    private double minY = Double.MAX_VALUE; 
    private double[][] data; // data is always an array of length 2, X

    /**
     * Constructor that builds a dataAnalytics object from a data array. 
     * 
     * @param data the data array. Should always be length 2, X
     */
    public DataAnalytics(double[][] data) {
        this.setData(data);
    }

    /**
     * Accessor method for MaxY variable.
     * 
     * @return the MaxY value
     */
    public double getMaxY() {
        return maxY;
    }

    /**
     * Accessor method for MinY variable.
     * 
     * @return the MinY value
     */
    public double getMiny() {
        return minY;
    }

    /**
     * Accessor method for data variable.
     * 
     * @return the data array
     */
    public double[][] getData() {
        return data;
    }

    /**
     * Mutator method for data variable.  Also sets the min and the max appropriately.
     *
     * @param data the new data array, to which the min and the max will be set appropriately.
     */
    public void setData(double[][] newData) {
        if (newData.length != 2 || newData[0].length == 0 || newData[1].length == 0 
            || newData[0].length != newData[1].length) {
            throw new IllegalArgumentException();
        }
        this.data = newData;
        maxY = Double.MAX_VALUE;
        minY = Double.MAX_VALUE;
        // Assign Min and Max values for the data
        for (int j = 0; j < data[0].length; j++) {
            if (data[1][j] < minY) {
                minY = data[1][j];
            }
            if (data[1][j] > maxY) {
                maxY = data[1][j];
            }
        }
    }

}