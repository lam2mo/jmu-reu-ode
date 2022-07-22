package jmu.reu.ode;

import java.awt.Color;

/**
 * A class that encapsulates settings for lines in our charts.  It contains data such as color, 
 * lineweight, shapes if the line were to use them, etc.
 * 
 * @author Benjamin Huber
 * @version 7/14/2022
 */
public class LineProfile {
    private Color lineColor; // RED
    private float lineWeight; // 1

    /**
     * Default constructor for LineProfile, initiating the values to their defaults.
     */
    public LineProfile() {
        lineColor = Color.RED;
        lineWeight = 1;
    }

    /**
     * Explicit constructor for LineProfile.
     * 
     * @param lineColor the lineColor of this profile
     * @param lineWeight the lineWeight of this profile
     */
    public LineProfile(Color lineColor, float lineWeight) {
        this.lineColor = lineColor;
        this.lineWeight = lineWeight;
    }

    /**
     * Accessor method for lineColor.
     * 
     * @return the lineColor
     */
    public Color getLineColor() {
        return lineColor;
    }

    /**
     * Accessor method for lineWeight.
     * 
     * @return the lineWeight
     */
    public float getLineWeight() {
        return lineWeight;
    }

    /**
     * Mutator method for lineColor.
     * 
     * @param newColor the new Color value for the lineColor attribute
     */
    public void setLineColor(Color newColor) {
        this.lineColor = newColor;
    }

    /**
     * Mutator method that changes the lineColor field.  Set using a hex string formatted as 
     * #XXXXXX where X is a digit or any character A-F.
     * @param hexColor
     */
    public void setLineColor(String hexColor) {
        if (hexColor.charAt(0) != '#' || hexColor.length() != 7) {
            throw new IllegalArgumentException();
        }
        this.lineColor = new Color(Integer.parseInt(hexColor.substring(1), 16));
    }

    /**
     * Mutator method for lineWeight.
     * 
     * @param newWeight the new weight value for the lineWeight attribute
     */
    public void setLineWeight(float newWeight) {
        this.lineWeight = newWeight;
    }
}
