package model;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * */
public class State {
    private boolean init;
    private String name;
    private String [] label;
    private List<Transition> transitions = new ArrayList<>();
    private List<Transition> invalidTransitions = new ArrayList<>();

    /**
     * Is state an initial state
     * @return boolean init 
     * */
    public boolean isInit() {
	return init;
    }
	
    /**
     * Returns the name of the state
     * @return String name 
     * */
    public String getName() {
	return name;
    }
	
    /**
     * Returns the labels of the state
     * @return Array of string labels
     * */
    public String[] getLabel() {
	return label;
    }

    public void addTransition(Transition transition) {
        transitions.add(transition);
    }

    public void addInvalidTransition(Transition transition) {
        invalidTransitions.add(transition);
    }

    public void removeInvalidTransitions() {
        transitions.removeAll(invalidTransitions);
    }

    public List<Transition> getTransitions() { return transitions; }

}
