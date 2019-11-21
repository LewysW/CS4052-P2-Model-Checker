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
    private List<State> invalidStates = new ArrayList<>();
    private boolean constraintSwitch = true;

    @Override
    public boolean check(Model model, StateFormula constraint, StateFormula query) {

        // Get all the states (representing them with a hashmap) get all the initial states to construct valid paths:
        for (State state : model.getStates()) {
            states.put(state.getName(), state);
            if (state.isInit())
                initStates.add(state);
        }

        // Populate state transition tables:
        for (Transition transition : model.getTransitions()) {
            String source = transition.getSource();
            states.get(source).addTransition(transition);
        }

        for (State initState : initStates) {
            if(!recursiveStateFormulaCheck(query, initState))
                invalidStates.add(initState);
        }

        for (State invalidState : invalidStates) {
            states.remove(invalidState.getName());
        }

        initStates.removeAll(invalidStates);

        for (Map.Entry<String, State> entry : states.entrySet()) {
            entry.getValue().removeInvalidTransitions();
        }

        constraintSwitch = false;

        // Check each path from each initial state to see if formula holds:
        for (State initState : initStates) {
            if (!recursiveStateFormulaCheck(query, initState))
                return false;
        }

        System.out.println("Passed");
        return true;

    }

    private boolean recursiveStateFormulaCheck(StateFormula formula, State state) {
        // Evaluate State Formula:
        if (formula instanceof And) {

            StateFormula leftChild = ((And) formula).left;
            StateFormula rightChild = ((And) formula).right;

            return recursiveStateFormulaCheck(leftChild, state) && recursiveStateFormulaCheck(rightChild, state);

        } else if (formula instanceof Or) {

            StateFormula leftChild = ((Or) formula).left;
            StateFormula rightChild = ((Or) formula).right;

            return recursiveStateFormulaCheck(leftChild, state) || recursiveStateFormulaCheck(rightChild, state);

        } else if (formula instanceof ThereExists) {

            boolean outcome = recursivePathFormulaCheck(((ThereExists) formula).pathFormula, state, true);

            if (constraintSwitch)
                return true;
            else
                return outcome;

        } else if (formula instanceof ForAll) {

            boolean outcome = recursivePathFormulaCheck(((ForAll) formula).pathFormula, state, true);

            if (constraintSwitch)
                return true;
            else
                return outcome;

        } else if (formula instanceof Not) {

            StateFormula child = ((Not) formula).stateFormula;
            return !(recursiveStateFormulaCheck(child, state));

        } else if (formula instanceof AtomicProp) {

            return atomicPropertyCheck((AtomicProp) formula, state);

        } else if (formula instanceof BoolProp) {

            return ((BoolProp) formula).value;

        } else {
            return false;
        }
    }

    private boolean recursivePathFormulaCheck(PathFormula formula, State state, boolean isExists) {
        if (formula instanceof Until) {

            HashSet<State> visitedStates = new HashSet<>();
            StateFormula left = ((Until) formula).left;
            StateFormula right = ((Until) formula).right;

            if (recursiveStateFormulaCheck(right, state)) {
                return true;
            } else if (!recursiveStateFormulaCheck(left, state)) {
                return false;
            } else {
                if (isExists) {
                    boolean outcome = state.getTransitions().stream().anyMatch(n -> recursiveUntil(formula, n, visitedStates, true));

                    if (constraintSwitch)
                        return true;

                    return outcome;
                } else {
                    boolean outcome = state.getTransitions().stream().allMatch(n -> recursiveUntil(formula, n, visitedStates, false));

                    if (constraintSwitch)
                        return true;

                    return outcome;
                }
            }

        } else if (formula instanceof Always) {

            BoolProp left = new BoolProp(true);
            Not right = new Not(((Always) formula).stateFormula);
            Set<String> rightActions = ((Always) formula).getActions();

            Until always = new Until(left, right, null, rightActions);
            return !recursivePathFormulaCheck(always, state, isExists);

        } else if (formula instanceof Eventually) {

            BoolProp left = new BoolProp(true);
            StateFormula right = ((Eventually) formula).stateFormula;
            Set<String> leftActions = ((Eventually) formula).getLeftActions();
            Set<String> rightActions = ((Eventually) formula).getRightActions();

            Until eventually = new Until(left, right, leftActions, rightActions);
            return recursivePathFormulaCheck(eventually, state, isExists);

        } else if (formula instanceof Next) {

            BoolProp left = new BoolProp(true);
            StateFormula right = ((Next) formula).stateFormula;
            Set<String> leftActions = new HashSet<>();
            Set<String> rightActions = ((Next) formula).getActions();

            Until next = new Until(left, right, leftActions, rightActions);
            return recursivePathFormulaCheck(next, state, isExists);

        } else {

            return false;

        }
    }

    private boolean recursiveUntil(PathFormula formula, Transition transition, HashSet<State> visitedStates, boolean isExists) {
        State target = states.get(transition.getTarget());

        if (!visitedStates.contains(target)) {
            visitedStates.add(target);

            if (!transition.getTarget().equals(transition.getSource())) {
                StateFormula left = ((Until) formula).left;
                StateFormula right = ((Until) formula).right;

                if (recursiveStateFormulaCheck(right, target)) {

                    boolean outcome = checkActions(((Until) formula).getRightActions(), transition.getActions());

                    if (constraintSwitch && !outcome)
                        target.addInvalidTransition(transition);

                    return outcome;

                } else if (recursiveStateFormulaCheck(left, target)) {

                    if (checkActions(((Until) formula).getLeftActions(), transition.getActions())) {
                        if (isExists)
                            return target.getTransitions().stream().anyMatch(n -> recursiveUntil(formula, n, visitedStates, true));
                        else
                            return target.getTransitions().stream().allMatch(n -> recursiveUntil(formula, n, visitedStates, false));
                    } else if (constraintSwitch) {
                        target.addInvalidTransition(transition);
                    }

                }
            }
        }

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

    private boolean checkActions(Set<String> allowedActions, String[] actions) {
        if (allowedActions == null || allowedActions.isEmpty())
            return true;

        for (String action : actions) {
            if (allowedActions.contains(action))
                return true;
        }

        return false;
    }

    @Override
    public String[] getTrace() {
        // TODO Auto-generated method stub
        return null;
    }

}
