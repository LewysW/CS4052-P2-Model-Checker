package model;
import java.util.HashMap;

/**
 * 
 * */
public class State {
    private boolean init;
    private String name;
    private String [] label;

    // Added to store possible state transitions:
    private HashMap<String, State> transitions = new HashMap<>();
	
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

    // Add a transition:
    public void addTransition(String action, State state) {
        transitions.put(action, state);
    }

    // Get all transitions:
    public HashMap<String, State> getTransitions() {
        return  transitions;
    }

}
