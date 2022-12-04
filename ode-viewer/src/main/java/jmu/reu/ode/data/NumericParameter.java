package jmu.reu.ode.data;

/**
 * JMU REU 2022
 * 
 * A class that describes parameters for our ODE.
 * 
 * @author Mike Lam, Benjamin Huber
 * @version 7/9/2022
 */

public class NumericParameter extends Parameter
    {
        public double min;
        public double def;  // default
        public double max;
        public double value;
        public double snap;

        public NumericParameter(String label, double min, double def, double max, double snap)
        {
            super(label);
            this.min = min;
            this.def = def;
            this.max = max;
            this.value = def;
            this.snap = snap;
        }
    }