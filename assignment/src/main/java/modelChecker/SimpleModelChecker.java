package modelChecker;

import formula.stateFormula.StateFormula;
import model.Model;

public class SimpleModelChecker implements ModelChecker {
    private StringBuilder trace = new StringBuilder();

    @Override
    public boolean check(Model model, StateFormula constraint, StateFormula query) {
        // TODO Auto-generated method stub

        //1) If Φ2 holds, finish else goto 2
        //if ()
        //for (State s : model.getStates()) System.out.println(s.toString());

        return false;
    }

    @Override
    public String[] getTrace() {
        // TODO Auto-generated method stub
        return null;
    }

}
