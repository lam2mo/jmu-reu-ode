package jmu.reu.ode.data;

/**
 * A class that represents all the info we need to chart a single series. This includes things such
 * as lineColors, data, and data analytics needed for formatting.  You should not be calling 
 * getDataAnlytics() before calling the appropriate setter.
 * 
 * @author Benjamin Huber
 * @version 7/9/2022
 */
public class SeriesSettings {
    private LineProfile lineProfile;
    private DataAnalytics data;
    private int xColumn;
    private int yColumn;
    private String dataInput;
    private String seriesTitle;

    public SeriesSettings() {
        setDefaults();
        data = null;
    }

    /**
     * A constructor that builds a SeriesSettings object from a DataAnalytics object. Instantiating 
     * everything else to their default values.
     * 
     * @param data the DataAnalyitcs Object.
     */
    public SeriesSettings(DataAnalytics data) {
        this.data = data;
        setDefaults();
    }

    /**
     * Accessor method for lineProfile field.
     * 
     * @return the lineProfile field.
     */
    public LineProfile getLineProfile() {
        return lineProfile;
    }

    /**
     * Accessor method for dataAnalytics object.
     * 
     * @return the dataAnalyitcs object.
     */
    public DataAnalytics getData() {
        return data;
    }

    /**
     * Mutator method that changes the lineProfile field.
     * 
     * @param newColor the new profile for the lines of this series.
     */
    public void setLineProfile(LineProfile newProfile) {
        this.lineProfile = newProfile;
    }

    /**
     * Mutator method for the DataAnalytics field.
     * 
     * @param data the new DataAnalytics object
     */
    public void setDataAnalytics(DataAnalytics data) {
        this.data = data;
    }

    /**
     * Sets all values to their defaults.  The data field does not have a default, and will not be 
     * affected.
     */
    @Deprecated
    private void setDefaults() {
        this.lineProfile = new LineProfile();
    }

    /**
     * Mutator method for setting the xColumn attribute.
     * 
     * @param newXColumn the new XColumn attribute
     */
    public void setXColumn(int newXColumn) {
        this.xColumn = newXColumn;
    }

    /**
     * Mutator method for setting the yColumn attribute.
     * 
     * @param newYColumn the new YColumn attribute
     */
    public void setYColumn(int newYColumn) {
        this.yColumn = newYColumn;
    }

    /**
     * Accessor method for xColumn field.
     * 
     * @return xColumn
     */
    public int getXColumn() {
        return xColumn;
    }

    /**
     * Accessor method for yColumn field.
     * 
     * @return yColumn
     */
    public int getYColumn() {
        return yColumn;
    }

    /**
     * Mutator method for dataFile field.
     * 
     * @param newInputFile the new dataFile
     */
    public void setInputFile(String newInputFile) {
        this.dataInput = newInputFile;
    }

    /**
     * Accessor method for dataFile field.
     * 
     * @return dataInput field
     */
    public String getInputFile() {
        return dataInput;
    }

    /**
     * Mutator method for seriesTitle field.
     * 
     * @param newSeriesTitle the new series title value
     */
    public void setSeriesTitle(String newSeriesTitle) {
        this.seriesTitle = newSeriesTitle;
    }

    /**
     * Accessor method for seriesTitle field.
     * 
     * @return the seriesTitle field
     */
    public String getSeriesTitle() {
        return seriesTitle;
    }
}