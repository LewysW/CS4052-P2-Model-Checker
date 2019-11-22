package modelChecker;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

import formula.FormulaParser;
import formula.stateFormula.StateFormula;
import modelChecker.ModelChecker;
import modelChecker.SimpleModelChecker;
import model.Model;
import org.junit.rules.ExpectedException;

public class ModelCheckerTest {
    private final ExpectedException exception = ExpectedException.none();


    /*
     * An example of how to set up and call the model building methods and make
     * a call to the model checker itself. The contents of model.json,
     * constraint1.json and ctl.json are just examples, you need to add new
     * models and formulas for the mutual exclusion task.
     */
    @Test
    public void constraintTest() {
        try {
            Model model = Model.parseModel("src/test/resources/givenTests/model1.json");
            StateFormula fairnessConstraint = new FormulaParser("src/test/resources/myTests/pORq.json").parse();
            StateFormula query = new FormulaParser("src/test/resources/myTests/existsPuntilR.json").parse();

            ModelChecker mc = new SimpleModelChecker();

            assertFalse(mc.check(model, fairnessConstraint, query));
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    @Test
    public void mutualExclusionTest1() {
        try {
            Model model = Model.parseModel("src/test/resources/myTests/MutualModel.json");
            StateFormula fairnessConstraint = new FormulaParser("src/test/resources/myTests/TrueConstraint.json").parse();
            StateFormula query = new FormulaParser("src/test/resources/myTests/MutualCTL1.json").parse();

            ModelChecker mc = new SimpleModelChecker();

            assertTrue(mc.check(model, fairnessConstraint, query));
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    @Test
    public void mutualExclusionTest2() {
        try {
            Model model = Model.parseModel("src/test/resources/myTests/MutualModel.json");
            StateFormula fairnessConstraint = new FormulaParser("src/test/resources/myTests/TrueConstraint.json").parse();
            StateFormula query = new FormulaParser("src/test/resources/myTests/MutualCTL2.json").parse();

            ModelChecker mc = new SimpleModelChecker();

            assertTrue(mc.check(model, fairnessConstraint, query));
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    @Test
    public void mutualExclusionTest3() {
        try {
            Model model = Model.parseModel("src/test/resources/myTests/MutualModel.json");
            StateFormula fairnessConstraint = new FormulaParser("src/test/resources/myTests/TrueConstraint.json").parse();
            StateFormula query = new FormulaParser("src/test/resources/myTests/MutualCTL3.json").parse();

            ModelChecker mc = new SimpleModelChecker();

            assertTrue(mc.check(model, fairnessConstraint, query));
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    @Test
    public void mutualExclusionTest4() {
        try {
            Model model = Model.parseModel("src/test/resources/myTests/MutualModel.json");
            StateFormula fairnessConstraint = new FormulaParser("src/test/resources/myTests/TrueConstraint.json").parse();
            StateFormula query = new FormulaParser("src/test/resources/myTests/MutualCTL4.json").parse();

            ModelChecker mc = new SimpleModelChecker();

            assertTrue(mc.check(model, fairnessConstraint, query));
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    @Test
    public void mutualExclusionTest5() {
        try {
            Model model = Model.parseModel("src/test/resources/myTests/MutualModel.json");
            StateFormula fairnessConstraint = new FormulaParser("src/test/resources/myTests/TrueConstraint.json").parse();
            StateFormula query = new FormulaParser("src/test/resources/myTests/MutualCTL5.json").parse();

            ModelChecker mc = new SimpleModelChecker();

            assertTrue(mc.check(model, fairnessConstraint, query));
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    @Test
    public void mutualExclusionTest6() {
        try {
            Model model = Model.parseModel("src/test/resources/myTests/MutualModel.json");
            StateFormula fairnessConstraint = new FormulaParser("src/test/resources/myTests/TrueConstraint.json").parse();
            StateFormula query = new FormulaParser("src/test/resources/myTests/MutualCTL6.json").parse();

            ModelChecker mc = new SimpleModelChecker();

            assertTrue(mc.check(model, fairnessConstraint, query));
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    @Test
    public void mutualExclusionTest7() {
        try {
            Model model = Model.parseModel("src/test/resources/myTests/MutualModel.json");
            StateFormula fairnessConstraint = new FormulaParser("src/test/resources/myTests/TrueConstraint.json").parse();
            StateFormula query = new FormulaParser("src/test/resources/myTests/MutualCTL7.json").parse();

            ModelChecker mc = new SimpleModelChecker();

            exception.expect(RuntimeException.class);
            mc.check(model, fairnessConstraint, query);
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    @Test
    public void mutualExclusionTest8() {
        try {
            Model model = Model.parseModel("src/test/resources/myTests/MutualModel.json");
            StateFormula fairnessConstraint = new FormulaParser("src/test/resources/myTests/TrueConstraint.json").parse();
            StateFormula query = new FormulaParser("src/test/resources/myTests/MutualCTL8.json").parse();

            ModelChecker mc = new SimpleModelChecker();

            exception.expect(RuntimeException.class);
            mc.check(model, fairnessConstraint, query);
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    @Test
    public void mutualExclusionTest9() {
        try {
            Model model = Model.parseModel("src/test/resources/myTests/MutualModel.json");
            StateFormula fairnessConstraint = new FormulaParser("src/test/resources/myTests/TrueConstraint.json").parse();
            StateFormula query = new FormulaParser("src/test/resources/myTests/MutualCTL9.json").parse();

            ModelChecker mc = new SimpleModelChecker();

            assertTrue(mc.check(model, fairnessConstraint, query));
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    @Test
    public void mutualExclusionTest10() {
        try {
            Model model = Model.parseModel("src/test/resources/myTests/MutualModel.json");
            StateFormula fairnessConstraint = new FormulaParser("src/test/resources/myTests/TrueConstraint.json").parse();
            StateFormula query = new FormulaParser("src/test/resources/myTests/MutualCTL10.json").parse();

            ModelChecker mc = new SimpleModelChecker();

            exception.expect(RuntimeException.class);
            mc.check(model, fairnessConstraint, query);
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    @Test
    public void forAll1() {
        try {
            Model model = Model.parseModel("src/test/resources/myTests/ForAllModel.json");
            StateFormula fairnessConstraint = new FormulaParser("src/test/resources/myTests/TrueConstraint.json").parse();
            StateFormula query = new FormulaParser("src/test/resources/myTests/ForAll1.json").parse();

            ModelChecker mc = new SimpleModelChecker();

            assertTrue(mc.check(model, fairnessConstraint, query));
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    @Test
    public void forAll2() {
        try {
            Model model = Model.parseModel("src/test/resources/myTests/ForAllModel.json");
            StateFormula fairnessConstraint = new FormulaParser("src/test/resources/myTests/TrueConstraint.json").parse();
            StateFormula query = new FormulaParser("src/test/resources/myTests/ForAll2.json").parse();

            ModelChecker mc = new SimpleModelChecker();

            exception.expect(RuntimeException.class);
            mc.check(model, fairnessConstraint, query);
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }


}
