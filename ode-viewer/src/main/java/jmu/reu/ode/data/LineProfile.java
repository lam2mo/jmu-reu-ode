package jmu.reu.ode.data;

import java.awt.Color;
import java.awt.BasicStroke;

/**
 * A class that encapsulates settings for lines in our charts.  It contains data such as color, 
 * lineweight, shapes if the line were to use them, etc.
 * 
 * @author Benjamin Huber
 * @version 7/14/2022
 */
public class LineProfile {
    private Color lineColor; // RED
    private BasicStroke stroke;

    /**
     * Default constructor for LineProfile, initiating the values to their defaults.
     */
    public LineProfile() {
        stroke = new BasicStroke(1);
        lineColor = Color.RED;
    }

    /**
     * Explicit constructor for LineProfile.
     * 
     * @param lineColor the lineColor of this profile
     * @param lineWeight the lineWeight of this profile
     */
    public LineProfile(Color lineColor, BasicStroke stroke) {
        this.lineColor = lineColor;
        this.stroke = stroke;
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
     * Accessor method for stroke
     * 
     * @return stroke
     */
    public BasicStroke getStroke() {
        return stroke;
    }

    /**
     * Mutator method for stroke
     * 
     * @param stroke the new stroke value
     */
    public void setStroke(BasicStroke stroke) {
        this.stroke = stroke;
    }
}
