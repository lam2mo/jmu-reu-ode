package jmu.reu.ode.data;

/**
 * An abstract class used to identify what it means to be a parameter.  This class should do 
 * anything that any parameter can do.
 * @author Benjamin Huber
 * @version 10/7/2022
 */
public abstract class Parameter {
    private String label;
    public Parameter(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
    
}
