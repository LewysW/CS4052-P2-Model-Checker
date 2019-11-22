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

        // Try to model check within the defined limited number of recursive steps:
        try {
            /*
            Loop through all initial states testing the constraint, this will evaluate states (and their paths if
            necessary). In the case of for all the paths that do not satisfy the constraint are removed. For any other
            constraint such as there exists or holds p the initial state is evaluated and if the constraint does not
            hold then it is removed.
             */
            for (State initState : initStates) {
                if(!recursiveStateFormulaCheck(constraint, initState))
                    invalidStates.add(initState);
            }

            initStates.removeAll(invalidStates);

            /*
            Loop through every state and their transitions. If any transitions contain removed states they must be
            removed as those states are no longer accessible. This is done through use of the state class functions:
             */
            for (Map.Entry<String, State> entry : states.entrySet()) {
                State value = entry.getValue();

                for (State invalid : invalidStates)
                    value.removeInvalidStateOccurrence(invalid.getName());

                value.removeInvalidTransitions();
            }

            // Remove all invalid states from the states list:
            for (State invalidState : invalidStates) {
                states.remove(invalidState.getName());
            }

            /*
             Evaluating the model against the constraint has finished so we may switch functionality to evaluating the
             query:
             */
            constraintSwitch = false;

            // Evaluate the query against the initial states and their paths (if necessary):
            for (State initState : initStates) {
                if (!recursiveStateFormulaCheck(query, initState))
                    return false;
            }

            return true;
        } catch (RuntimeException e) {
            /*
            If max number of steps have been reached and the model is still being checked assume that the query/
            constraint holds and return true:
             */
            System.out.println(e.toString());
            return true;
        }
    }

    private boolean recursiveStateFormulaCheck(StateFormula formula, State state) throws RuntimeException {
        /*
         Evaluate State Formula, the cases of recursion the step count is incremented and checked for being max:
         */
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

            return recursivePathConversion(((ThereExists) formula).pathFormula, state, true);

        } else if (formula instanceof ForAll) {

            boolean outcome = recursivePathConversion(((ForAll) formula).pathFormula, state, false);

            /*
             If we are currently evaluating the constraint in the case of for all then this call should return true
             as we do not wish to remove the initial states, only the invalid paths:
             */

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

    private boolean recursivePathConversion(PathFormula formula, State state, boolean isExists) throws RuntimeException {
        if (formula instanceof Until) {

            //Evaluates until:

            List<String> loopStates = new ArrayList<>();
            List<String> visitedStates = new ArrayList<>();
            StateFormula left = ((Until) formula).left;
            StateFormula right = ((Until) formula).right;

            if (recursiveStateFormulaCheck(right, state)) {

                /*
                 If the right formula (PhiTwo) already holds for the state then there is no need for further query
                 return true:
                 */

                return true;
            } else if (!recursiveStateFormulaCheck(left, state)) {

                 /*
                 If the left formula (PhiOne) does not hold for the state then there is no way to continue, return
                 false:
                 */

                 return false;
            } else {

                if (isExists) {

                    /*
                    If the left formula (PhiOne) does hold and the path formula is the case of exists then check the next
                    states (via the transitions) to see whether or not the formula holds for one of the paths (any match):
                    */

                    return state.getTransitions().stream().anyMatch(n -> recursiveUntil(formula, n, visitedStates, loopStates, true));

                } else {

                    /*
                    If the left formula (PhiOne) does hold and the path formula is the case of for all then check the next
                    states (via the transitions) to see whether or not the formula holds for all of the paths (all match):
                    */

                    boolean outcome = state.getTransitions().stream().allMatch(n -> recursiveUntil(formula, n, visitedStates, loopStates, false));

                    /*
                     If we are checking the constraint then return true in order to prevent the initial state from being
                     removed. This is because any paths voiding the for all claim will be removed from the super set of
                     paths leaving a subset which will be evaluated by the query. Please see accompanying report for a
                     more thorougher description:
                     */

                    if (constraintSwitch)
                        return true;
                    else if (!outcome)  // If the outcome was false and not in constraint mode then add to the fail trace.
                        traceList.add(state.getName());

                    return outcome;
                }
            }

        } else if (formula instanceof Always) {

            // Converts always in terms of until and recalls function:

            BoolProp left = new BoolProp(true);
            Not right = new Not(((Always) formula).stateFormula);
            Set<String> leftActions = new HashSet<>();
            Set<String> rightActions = ((Always) formula).getActions();

            Until always = new Until(left, right, leftActions, rightActions);

            return !recursivePathConversion(always, state, isExists);

        } else if (formula instanceof Eventually) {

            // Converts eventually in terms of until and recalls function:

            BoolProp left = new BoolProp(true);
            StateFormula right = ((Eventually) formula).stateFormula;

            // Left actions is set to the empty set as does not apply (see report):
            Set<String> leftActions = ((Eventually) formula).getLeftActions();
            Set<String> rightActions = ((Eventually) formula).getRightActions();

            Until eventually = new Until(left, right, leftActions, rightActions);
            return recursivePathConversion(eventually, state, isExists);

        } else if (formula instanceof Next) {

            // Converts next in terms of until:

            /*
             Here left formula (PhiOne) is set to false to prevent any states after the next ones in the
             path being checked:
             */

            List<String> loopStates = new ArrayList<>();
            List<String> visitedStates = new ArrayList<>();
            BoolProp left = new BoolProp(false);
            StateFormula right = ((Next) formula).stateFormula;

            // Left actions is set to the empty set as does not apply (see report):
            Set<String> leftActions = new HashSet<>();
            Set<String> rightActions = ((Next) formula).getActions();

            Until next = new Until(left, right, leftActions, rightActions);

            if (isExists) {

                /*
                If exists then check the next states (via the transitions) to see whether or not the formula holds
                for one path (any match):
                */

                return state.getTransitions().stream().anyMatch(n -> recursiveUntil(next, n, visitedStates, loopStates,  true));

            } else {

                boolean outcome = state.getTransitions().stream().allMatch(n -> recursiveUntil(next, n, visitedStates, loopStates, false));

                 /*
                 If we are checking the constraint then return true in order to prevent the initial state from being
                 removed. This is because any paths voiding the for all claim will be removed from the super set of
                 paths leaving a subset which will be evaluated by the query. Please see accompanying report for a
                 more detailed description:
                 */

                if (constraintSwitch)
                    return true;
                else if (!outcome)
                    traceList.add(state.getName()); // If the outcome was false and not in constraint mode then add to the fail trace.

                return outcome;
            }

        } else {

            return false;

        }
    }

    private boolean recursiveUntil(PathFormula formula, Transition transition, List<String> visitedStates, List<String> loopStates, Boolean isExists) throws RuntimeException {
        // Get the state needed from the transition:
        State target = states.get(transition.getTarget());

        //Add the current state to the list of visited to prevent it being checked again.
        visitedStates.add(target.getName());

        StateFormula left = ((Until) formula).left;
        StateFormula right = ((Until) formula).right;

        if (recursiveStateFormulaCheck(right, target)) {

            /*
            If the right formula (PhiTwo) already holds for this path then we must evaluate the action of the
            transition that got us here to check whether or not the path is valid:
            */

            boolean outcome = checkActions(((Until) formula).getRightActions(), transition.getActions());

            if (constraintSwitch && !outcome)   // If the outcome is false and we are testing the constraint then this transition is invalid.
                target.addInvalidTransition(transition);
            else if (!isExists && !outcome) // If the outcome is false and we are testing the query and for all then add the state to the fail trace. 
                traceList.add(target.getName());

            return outcome;

        } else if (recursiveStateFormulaCheck(left, target)) {

            /*
            If the left formula (PhiOne) holds for this path then we must evaluate the action of the
            transition that got us here to check whether or not the path is valid. Furthermore we prevent
            */

            if (checkActions(((Until) formula).getLeftActions(), transition.getActions())) {

                checkStepCount();

                /*
                 The following code is implemented as an extension in order to reduce the number of recursions and
                 improve efficiency. For more detail please refer to the report.
                 */

                ArrayList<Transition> transitionsToCheck = new ArrayList<>();

                // For all transitions of this state get the ones which lead to states that we've not seen:
                for (Transition t : target.getTransitions()) {
                    // Ensures that loop states get checked once:
                    if (target.getName().equals(t.getTarget()) && !loopStates.contains(target.getName())) {
                        transitionsToCheck.add(t);
                        loopStates.add(target.getName());
                    } else {
                        if (!visitedStates.contains(t.getTarget()))
                            transitionsToCheck.add(t);
                    }
                }

                if (transitionsToCheck.size() == 0)
                    return false;

                if (isExists) {

                    /*
                    If exists then check the next states (via the transitions) to see whether or not the formula holds
                    for one path (any match):
                    */

                    return transitionsToCheck.stream().anyMatch(n -> recursiveUntil(formula, n, visitedStates, loopStates, true));

                } else {
                    if (!transitionsToCheck.stream().allMatch(n -> recursiveUntil(formula, n, visitedStates, loopStates, false))) {

                        /*
                        If one paths leading from this state shows false then return false.
                         */

                        if (!constraintSwitch)  // If we are evaluating the query then add state to fail trace:
                            traceList.add(target.getName());

                        return false;
                    } else {
                        return true;
                    }
                }
            }

        }

        /*
         If neither formula holds (phiOne or phiTwo) return false and carry out correct functionality depending on
         whether evaluating the constraint or the query:
         */

        if (constraintSwitch)
            target.addInvalidTransition(transition);
        else if (!isExists)
            traceList.add(target.getName());

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
        /*
        If there is no set of actions to check (for example with a normal until) return true. Else loop through the set
        checking if there is an action from the transition actions which corresponds to the set.
         */

        if (allowedActions.isEmpty())
            return true;

        for (String action : actions) {
            if (allowedActions.contains(action))
                return true;
        }

        return false;
    }

    private void checkStepCount() throws RuntimeException {
        /*
        Checks if the maximum number of reclusive steps has occurred (in order to avoid a stack overflow). If throw
        RuntimeException:
         */
        if (numberOfSteps == MAX_STEP_COUNT)
            throw new RuntimeException("Maximum number of steps exceeded!");
        else
            numberOfSteps++;
    }

    @Override
    public String[] getTrace() {
        /*
        If the trace size is greater than 0 it means that a for all error has occurred and a trace is needed
        otherwise an empty trace should be returned:
         */

        if (traceList.size() > 0) {

            //Calculate the length of the traces with the inclusion of '->'s:
            int traceLength = (traceList.size() * 2) - 1;
            String[] trace = new String[traceLength];

            /*
            Build trace backwards (as it was populated back to front due to recursion) also include '->' between states:
            */
            int i = 0;
            for (int j = traceList.size() - 1; j >= 0; j--) {
                trace[i] = traceList.get(j);
                System.out.println(trace[i]);
                i++;
                if (j != 0) {
                    trace[i] = " -> ";
                    System.out.println(trace[i]);
                    i++;
                }
            }

            return trace;
        } else {
            return new String[0];
        }
    }

}
