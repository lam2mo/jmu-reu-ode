package jmu.reu.ode.data;

/**
 * A class that defines Settings for drawing Marks on a chart.
 * 
 * @author Benjamin Huber
 * @version 8/14/2022
 */
public class MarkSettings {
    private LineProfile profile;
    private double value;
    private boolean domain;
    private boolean foreground;

    /**
     * Default constructor initializes all values to their default values.
     * Realistically value should be changed using the mutator method provided when constructing 
     * this way.
     */
    public MarkSettings() {
        profile = new LineProfile();
        value = 0.0;
        domain = false;
        foreground = false;
    }

    /**
     * Accessor method for LineProfile.
     * 
     * @return profile
     */
    public LineProfile getProfile() {
        return profile;
    }

    /** 
     * Mutator method for LineProfile.
     * 
     * @param profile the new lineProfile
     */
    public void setProfile(LineProfile profile) {
        this.profile = profile;
    }

    /**
     * Accessor method for value attribute.
     * 
     * @return value
     */
    public double getValue() {
        return value;
    }

    /**
     * Mutator method for value attribute.
     * 
     * @param value the new value attribute
     */
    public void setValue(double value) {
        this.value = value;
    }

    /**
     * Accessor method for domain attribute.
     * 
     * @return domain
     */
    public boolean getDomain() {
        return domain;
    }

    /**
     * Mutator method for domain attribute.
     * 
     * @param domain the new domain attribute
     */
    public void setDomain(boolean domain) {
        this.domain = domain;
    }

    /**
     * Accessor method for foreground attribute.
     * 
     * @return foreground
     */
    public boolean getForeground() {
        return foreground;
    }

    /**
     * Mutator method for foreground attribute.
     * 
     * @param foreground the new foreground attribute
     */
    public void setForeground(boolean foreground) {
        this.foreground = foreground;
    }

}
