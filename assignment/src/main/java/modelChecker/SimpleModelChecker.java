package modelChecker;

import formula.stateFormula.And;
import formula.stateFormula.StateFormula;
import java.util.*;
import model.*;
import formula.pathFormula.*;
import formula.stateFormula.*;

public class SimpleModelChecker implements ModelChecker {
    private Map<String, State> states = new HashMap<>();
    private List<State> initStates = new ArrayList<>();
    private Stack<State> currentPaths = new Stack<>();

    @Override
    public boolean check(Model model, StateFormula constraint, StateFormula query) {
        // & the query and the constraint in order to test both at the same time:
        StateFormula formula = new And(query, constraint);

        // Get all the states (representing them with a hashmap) get all the initial states to construct valid paths:
        for (State state : model.getStates()) {
            states.put(state.getName(), state);
            if (state.isInit())
                initStates.add(state);
        }

        // Populate state transition tables:
        for (Transition transition : model.getTransitions()) {
            String source = transition.getSource();
            State target = states.get(transition.getTarget());
            for (String action : transition.getActions())
                states.get(source).addTransition(action, target);
        }

        // Check each path from each initial state to see if formula holds:
        for (State initState : initStates) {
            if (!recursiveStateCheck(formula, initState))
                return false;
        }

        return true;
    }

    private boolean recursiveStateCheck(StateFormula formula, State state) {
        // Evaluate State Formula:
        if (formula instanceof And) {

            StateFormula leftChild = ((And) formula).left;
            StateFormula rightChild = ((And) formula).right;

            return recursiveStateCheck(leftChild, state) && recursiveStateCheck(rightChild, state);

        } else if (formula instanceof Or) {

            StateFormula leftChild = ((Or) formula).left;
            StateFormula rightChild = ((Or) formula).right;

            return recursiveStateCheck(leftChild, state) || recursiveStateCheck(rightChild, state);

        } else if (formula instanceof ThereExists) {

            return thereExistsCheck(formula, state);

        } else if (formula instanceof ForAll) {

            return !thereExistsCheck(formula, state);

        } else if (formula instanceof  Not) {

            StateFormula child = ((Not) formula).stateFormula;
            return !(recursiveStateCheck(child, state));

        } else if (formula instanceof AtomicProp) {

            return atomicPropertyCheck((AtomicProp) formula, state);

        } else if (formula instanceof BoolProp) {

            return ((BoolProp) formula).value;

        } else {
            return false;
        }
    }

    private boolean pathCheck(PathFormula formula) {
        //TODO implement functionality

        return false;
    }

    private boolean atomicPropertyCheck(AtomicProp formula, State state) {
        String[] stateLabels = state.getLabel();

        // Check to see if atomic property passed in is held by current state:
        for (String label : stateLabels) {
            if (formula.label.equals(label))
                return true;
        }

        return false;
    }

    private boolean thereExistsCheck(StateFormula formula, State state) {

        PathFormula pathFormula;

        if (formula instanceof ThereExists) {
            pathFormula = ((ThereExists) formula).pathFormula;
        } else if (formula instanceof ForAll) {
            pathFormula = ((ForAll) formula).pathFormula;
        }

        currentPaths.push(state);

        //TODO implement functionality
        return false;
    }

    @Override
    public String[] getTrace() {
        // TODO Auto-generated method stub
        return null;
    }

}
