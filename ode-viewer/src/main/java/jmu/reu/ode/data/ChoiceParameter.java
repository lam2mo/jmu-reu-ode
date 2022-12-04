package jmu.reu.ode.data;

public class ChoiceParameter extends Parameter {
    public String[] options;
    public String selected;
    public ChoiceParameter(String label, String... choices) {
        super(label);
        options = new String[choices.length];
        for (int i = 0; i < choices.length; i++) {
            options[i] = choices[i];
            i++;
        }
        selected = options[0];
    }
}
