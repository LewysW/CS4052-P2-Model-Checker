package modelChecker;

import formula.stateFormula.And;
import formula.stateFormula.StateFormula;

import java.util.*;

import model.*;
import formula.pathFormula.*;
import formula.stateFormula.*;

public class SimpleModelChecker implements ModelChecker {

    private static final int MAX_STEP_COUNT = 1000;

    private Map<String, State> states = new HashMap<>();
    private List<State> initStates = new ArrayList<>();
    private List<State> invalidStates = new ArrayList<>();
    private List <String> traceList = new ArrayList<>();
    private boolean constraintSwitch = true;
    private int numberOfSteps = 0;

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

        try {
            for (State initState : initStates) {
                if(!recursiveStateFormulaCheck(constraint, initState))
                    invalidStates.add(initState);
            }

            initStates.removeAll(invalidStates);

            for (Map.Entry<String, State> entry : states.entrySet()) {
                State value = entry.getValue();

                for (State invalid : invalidStates)
                    value.removeInvalidStateOccurance(invalid.getName());

                value.removeInvalidTransitions();
            }

            for (State invalidState : invalidStates) {
                states.remove(invalidState.getName());
            }

            constraintSwitch = false;

            // Check each path from each initial state to see if formula holds:
            for (State initState : initStates) {
                if (!recursiveStateFormulaCheck(query, initState)) {
                    traceList.add(initState.getName());
                    return false;
                }
            }

            return true;
        } catch (RuntimeException e) {
            System.out.println(e.toString());
            return true;
        }
    }

    private boolean recursiveStateFormulaCheck(StateFormula formula, State state) throws RuntimeException {
        // Evaluate State Formula:
        if (formula instanceof And) {

            StateFormula leftChild = ((And) formula).left;
            StateFormula rightChild = ((And) formula).right;

            checkStepCount();

            return recursiveStateFormulaCheck(leftChild, state) && recursiveStateFormulaCheck(rightChild, state);

        } else if (formula instanceof Or) {

            StateFormula leftChild = ((Or) formula).left;
            StateFormula rightChild = ((Or) formula).right;

            checkStepCount();

            return recursiveStateFormulaCheck(leftChild, state) || recursiveStateFormulaCheck(rightChild, state);

        } else if (formula instanceof ThereExists) {

            boolean outcome = recursivePathFormulaCheck(((ThereExists) formula).pathFormula, state, true);

            if (constraintSwitch)
                return true;
            else
                return outcome;

        } else if (formula instanceof ForAll) {

            boolean outcome = recursivePathFormulaCheck(((ForAll) formula).pathFormula, state, false);

            if (constraintSwitch)
                return true;
            else
               return outcome;

        } else if (formula instanceof Not) {

            StateFormula child = ((Not) formula).stateFormula;

            checkStepCount();

            return !(recursiveStateFormulaCheck(child, state));

        } else if (formula instanceof AtomicProp) {

            return atomicPropertyCheck((AtomicProp) formula, state);

        } else if (formula instanceof BoolProp) {

            return ((BoolProp) formula).value;

        } else {
            return false;
        }
    }

    private boolean recursivePathFormulaCheck(PathFormula formula, State state, boolean isExists) throws RuntimeException {
        if (formula instanceof Until) {
            StateFormula left = ((Until) formula).left;
            StateFormula right = ((Until) formula).right;

            if (recursiveStateFormulaCheck(right, state)) {
                return true;
            } else if (!recursiveStateFormulaCheck(left, state)) {
                return false;
            } else {
                if (isExists) {
                    boolean outcome = state.getTransitions().stream().anyMatch(n -> recursiveUntil(formula, n,  true));

                    if (constraintSwitch)
                        return true;

                    return outcome;
                } else {
                    boolean outcome = state.getTransitions().stream().allMatch(n -> recursiveUntil(formula, n,  false));

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

    private boolean recursiveUntil(PathFormula formula, Transition transition, boolean isExists) throws RuntimeException {
        State target = states.get(transition.getTarget());

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
                    checkStepCount();
                    if (isExists) {
                        return target.getTransitions().stream().anyMatch(n -> recursiveUntil(formula, n, true));
                    } else {
                        return target.getTransitions().stream().allMatch(n -> recursiveUntil(formula, n, false));
                    }
                } else if (constraintSwitch) {
                    target.addInvalidTransition(transition);
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

    private void checkStepCount() throws RuntimeException {
        if (numberOfSteps == MAX_STEP_COUNT)
            throw new RuntimeException("Maximum number of steps exceeded!");
        else
            numberOfSteps++;
    }

    @Override
    public String[] getTrace() {
        // TODO Auto-generated method stub
        return null;
    }

}
