package modelChecker;

import formula.stateFormula.StateFormula;
import model.Model;

import java.util.Stack;

public class SimpleModelChecker implements ModelChecker {
    private Map<String, State> states = new HashMap<>();
    private List<State> initStates = new ArrayList<>();
    private Stack<State> currentPaths = new Stack<State>();

    @Override
    public boolean check(Model model, StateFormula constraint, StateFormula query) {
        // & the query and the constraint in order to test both at the same time:
        StateFormula formula = new And(query, constraint);

        // Get all the states (represeting them with a hashmap) get all the initial states to construct valid paths:
        for (State state : model.getStates()) {
            states.put(state.getName(), state);
            if (state.isInit())
                startStates.add(s);
        }

        // Populate state transition tables:
        for (Transition transition : model.getTransition()) {
            String source = transition.getSource();
            State target = states.get(transition.getTarget());
            for (String action : transition.getActions())
                states.get(source).addTransition(action, target);
        }

        // Check each path from each initial state to see if formula holds:
        for (State initState : initStates) {
            if (!recursiveStateFormulaCheck(formula, initState))
                return false;
        }

        return true;
    }

    private boolean recursiveStateCheck(StateFormula formula, State state) {
        boolean leftInstance = false;

        // Evaluate State Formula:
        if (leftInstance = (formula instanceof And) || formula instanceof Or) {

            StateFormula leftChild = (And) formula).left;
            StateFormula rightChild = (And) formula).right;

            return (leftInstance ? recursiveStateCheck(leftChild, state) && recursiveStateCheck(rightChild, state) :
                    recursiveStateCheck(leftChild, state) || recursiveStateCheck(rightChild, state));

        } else if (leftInstance = (formula instanceof ThereExists) || formula instanceof ForAll) {

            return (leftInstance ? thereExistsCheck(formula, state) : !thereExistsCheck(formula, state));

        } else if (formula instanceof Not) {

            StateFormula child = ((Not) formula).stateFormula;
            return !(recursiveStateCheck(child, state));

        } else if (formula instanceof AtomicProp) {

            StateFormula child = (AtomicProp) formula;
            return atomicPropertyCheck(child, state);

        } else if (formula instanceof BoolProp) {

            return ((BoolProp) formula).value;

        } else {
            return false;
        }
    }

    private boolean pathCheck(PathFormula formula) {
        //TODO implement functionality
    }

    private boolean atomicPropertyCheck(AtomicProp formula, State state) {
        String[] stateLabeles = state.getLabel();

        // Check to see if atomic property passed in is held by current state:
        for (String label : stateLabeles) {
            if (formula.label.equals(label))
                return true;
        }

        return false;
    }

    private boolean thereExistsCheck(StateFormula formula, State state) {
        PathFormula pathFormula = formula instanceof ThereExists ? (ThereExists) formula).pathFormula:
        (ForAll) formula).pathFormula;

        currentPaths.push(state);

        //TODO implement functionality
    }

    @Override
    public String[] getTrace() {
        // TODO Auto-generated method stub
        return null;
    }

}
