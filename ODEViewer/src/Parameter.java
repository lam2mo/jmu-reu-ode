/**
 * JMU REU 2022
 * 
 * A class that describes parameters for our ODE.
 * 
 * @author Mike Lam, Benjamin Huber
 * @version 6/23/2022 
 */

public class Parameter
    {
        public String label;
        public double min;
        public double def;  // default
        public double max;
        public double value;

        public Parameter(String label, double min, double def, double max)
        {
            this.label = label;
            this.min = min;
            this.def = def;
            this.max = max;
            this.value = def;
        }
    }