package jmu.reu.ode;

public class ChoiceParameter extends Parameter {
    public String[] options;
    public String selected;
    public ChoiceParameter(String label, String... choices) {
        super(label);
        options = new String[choices.length];
        int i = 0;
        for (String choice : choices) {
            options[i] = choices[i];
            i++;
        }
        selected = options[0];
    }
}
