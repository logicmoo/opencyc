package org.opencyc.constraintsolver;

import java.util.*;
import java.io.*;
import junit.framework.*;
import org.opencyc.cycobject.*;
import org.opencyc.api.*;

/**
 * Provides a suite of JUnit test cases for the <tt>org.opencyc.constraintsolver</tt> package.<p>
 *
 * @version $Id$
 * @author Stephen L. Reed
 *
 * <p>Copyright 2001 Cycorp, Inc., license is open source GNU LGPL.
 * <p><a href="http://www.opencyc.org/license.txt">the license</a>
 * <p><a href="http://www.opencyc.org">www.opencyc.org</a>
 * <p><a href="http://www.sourceforge.net/projects/opencyc">OpenCyc at SourceForge</a>
 * <p>
 * THIS SOFTWARE AND KNOWLEDGE BASE CONTENT ARE PROVIDED ``AS IS'' AND
 * ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE OPENCYC
 * ORGANIZATION OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE AND KNOWLEDGE
 * BASE CONTENT, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
public class UnitTest extends TestCase {

    /**
     * Constructs a new UnitTest object.
     * @param name the test case name.
     */
    public UnitTest(String name) {
        super(name);
    }

    /**
     * Main method in case tracing is prefered over running JUnit GUI.
     */
    public static void main(String[] args) {
        boolean allTests = false;
        //boolean allTests = true;
        runTests(allTests);
    }

    /**
     * Runs the unit tests
     */
    public static void runTests(boolean allTests) {
        TestSuite testSuite;
        if (allTests)
            testSuite = new TestSuite(UnitTest.class);
        else {
            testSuite = new TestSuite();
            //testSuite.addTest(new UnitTest("testHelloWorld"));
            //testSuite.addTest(new UnitTest("testRule"));
            //testSuite.addTest(new UnitTest("testHornClause"));
            //testSuite.addTest(new UnitTest("testBinding"));
            //testSuite.addTest(new UnitTest("testSolution"));
            //testSuite.addTest(new UnitTest("testRuleEvaluator"));
            //testSuite.addTest(new UnitTest("testArgumentTypeConstrainer"));
            //testSuite.addTest(new UnitTest("testProblemParser"));
            testSuite.addTest(new UnitTest("testConstraintProblem1"));
            //testSuite.addTest(new UnitTest("testConstraintProblem2"));
        }
        TestResult testResult = new TestResult();
        testSuite.run(testResult);
    }

    /**
     * Tests the test harness itself.
     */
    public void testHelloWorld() {
        System.out.println("** testHelloWorld **");
        Assert.assertTrue(true);
        System.out.println("** testHelloWorld OK **");
    }

    /**
     * Tests the <tt>ProblemParser</tt> class.
     */
    public void testProblemParser() {
        System.out.println("** testProblemParser **");

        ConstraintProblem constraintProblem = new ConstraintProblem();
        String problemString1 =
            "(#$and " +
            "  (#$isa ?country #$WesternEuropeanCountry) " +
            "  (#$isa ?cathedral #$Cathedral) " +
            "  (#$countryOfCity ?country ?city) " +
            "  (#$objectFoundInLocation ?cathedral ?city)) ";
        Rule rule1 = null;
        Rule rule2 = null;
        Rule rule3 = null;
        Rule rule4 = null;
        Rule rule5 = null;
        Rule rule6 = null;
        try {
            CycList problem1 = CycAccess.current().makeCycList(problemString1);
            constraintProblem.problem = problem1;
            constraintProblem.simplifiedRules = Rule.simplifyRuleExpression(problem1);
            ProblemParser problemParser = constraintProblem.problemParser;
            problemParser.extractRulesAndDomains();
        }
        catch (Exception e) {
            Assert.fail(e.getMessage());
        }
        Assert.assertNotNull(constraintProblem.constraintRules);
        Assert.assertNotNull(constraintProblem.domainPopulationRules);
        try {
            rule1 = new Rule("(#$isa ?country #$WesternEuropeanCountry)");
            rule2 = new Rule("(#$isa ?cathedral #$Cathedral)");
            rule3 = new Rule("(#$countryOfCity ?country ?city)");
            rule4 = new Rule("(#$objectFoundInLocation ?cathedral ?city)");
            rule5 = new Rule("(#$isa ?city #$City)");
            rule6 = new Rule("(#$isa ?country #$Country)");
        }
        catch (Exception e) {
            Assert.fail(e.getMessage());
        }
        System.out.println("domainPopulationRules\n" + constraintProblem.domainPopulationRules);
        Assert.assertEquals(3, constraintProblem.domainPopulationRules.size());
        Assert.assertTrue(constraintProblem.domainPopulationRules.contains(rule1));
        Assert.assertTrue(constraintProblem.domainPopulationRules.contains(rule2));
        Assert.assertTrue(constraintProblem.domainPopulationRules.contains(rule5));
        System.out.println("constraintRules\n" + constraintProblem.constraintRules);
        Assert.assertEquals(2, constraintProblem.constraintRules.size());
        Assert.assertTrue(constraintProblem.constraintRules.contains(rule3));
        Assert.assertTrue(constraintProblem.constraintRules.contains(rule4));

        System.out.println("** testProblemParser OK **");
    }

    /**
     * Tests the <tt>RuleEvaluator</tt> class.
     */
    public void testRuleEvaluator() {
        System.out.println("** testRuleEvaluator **");

        ConstraintProblem constraintProblem = new ConstraintProblem();
        RuleEvaluator ruleEvaluator = constraintProblem.ruleEvaluator;
        try {
            Assert.assertTrue(ruleEvaluator.ask(new Rule("(#$numericallyEqual 1 1)")));
            Assert.assertTrue(! ruleEvaluator.ask(new Rule("(#$numericallyEqual 2 1)")));
            Assert.assertTrue(ruleEvaluator.ask(new Rule("(#$different 2 1)")));
            Assert.assertTrue(! ruleEvaluator.ask(new Rule("(#$different 2 2)")));
            Assert.assertTrue(ruleEvaluator.ask(new Rule("(#$different \"a\" \"b\")")));
            Assert.assertTrue(! ruleEvaluator.ask(new Rule("(#$different \"a\" \"a\")")));
            Assert.assertTrue(ruleEvaluator.ask(new Rule("(#$not (#$different 1 1))")));
            Assert.assertTrue(! ruleEvaluator.ask(new Rule("(#$not (#$not (#$different 1 1)))")));
            Assert.assertTrue(ruleEvaluator.ask(new Rule("(#$and (#$numericallyEqual 1 1) (#$numericallyEqual 3 3))")));
            Assert.assertTrue(! ruleEvaluator.ask(new Rule("(#$and (#$numericallyEqual 1 2) (#$numericallyEqual 3 3))")));
            Assert.assertTrue(! ruleEvaluator.ask(new Rule("(#$and (#$numericallyEqual 1 1) (#$numericallyEqual 3 4))")));
            Assert.assertTrue(ruleEvaluator.ask(new Rule("(#$and (#$numericallyEqual 1 1) (#$numericallyEqual 3 3) (#$numericallyEqual 4 4))")));
            Assert.assertTrue(ruleEvaluator.ask(new Rule("(#$or (#$numericallyEqual 1 2) (#$numericallyEqual 3 3))")));
            Assert.assertTrue(! ruleEvaluator.ask(new Rule("(#$or (#$numericallyEqual 1 2) (#$numericallyEqual 3 4))")));
            Assert.assertTrue(ruleEvaluator.ask(new Rule("(#$numericallyEqual 2 (#$PlusFn 1))")));
            Assert.assertTrue(ruleEvaluator.ask(new Rule("(#$numericallyEqual (#$PlusFn 1) 2)")));
            Assert.assertTrue(! ruleEvaluator.ask(new Rule("(#$numericallyEqual (#$PlusFn 1) 5)")));
        }
        catch (Exception e) {
            Assert.fail(e.getMessage());
        }

        System.out.println("** testRuleEvaluator OK **");
    }

    /**
     * Tests the <tt>Binding</tt> class.
     */
    public void testBinding() {
        System.out.println("** testBinding **");

        Binding binding1 = new Binding(CycVariable.makeCycVariable("?x"), "abc");
        Assert.assertNotNull(binding1);
        Assert.assertEquals(CycVariable.makeCycVariable("?x"), binding1.getCycVariable());
        Assert.assertEquals("abc", binding1.getValue());
        Assert.assertEquals("?x = \"abc\"", binding1.toString());

        System.out.println("** testBinding OK **");
    }

    /**
     * Tests the <tt>Rule</tt> class.
     */
    public void testRule() {
        System.out.println("** testRule **");

        CycAccess cycAccess = null;
        try {
            cycAccess = new CycAccess();
        }
        catch (Exception e) {
            Assert.fail(e.getMessage());
        }

        // Construction
        String ruleAsString = null;
        Rule rule1 = null;
        try {
            ruleAsString = "(#$isa ?x #$Cathedral)";
            rule1 = new Rule (cycAccess.makeCycList(ruleAsString));
            Assert.assertNotNull(rule1);
            Assert.assertNotNull(rule1.getRule());
            CycList cycList = rule1.getRule();
            Assert.assertEquals(ruleAsString, cycList.cyclify());
            Assert.assertEquals(ruleAsString, rule1.cyclify());
        }
        catch (Exception e) {
            Assert.fail(e.getMessage());
        }

        // Equality
        try {
            Rule rule2 = new Rule (cycAccess.makeCycList(ruleAsString));
            Assert.assertEquals(rule1.toString(), rule2.toString());
            Assert.assertEquals(rule1.cyclify(), rule2.cyclify());
            Assert.assertEquals(rule1, rule2);
        }
        catch (Exception e) {
            Assert.fail(e.getMessage());
        }

        // gatherVariables and arity.
        Assert.assertEquals(1, rule1.getArity());
        Assert.assertTrue(rule1.getVariables().contains(CycVariable.makeCycVariable("?x")));

        // simplifyRuleExpression
        try {
            CycList ruleExpression = cycAccess.makeCycList("(isa ?x Cathedral)");
            ArrayList rules = Rule.simplifyRuleExpression(ruleExpression);
            Assert.assertNotNull(rules);
            Assert.assertEquals(1, rules.size());
            Assert.assertTrue(rules.get(0) instanceof Rule);
            Rule rule3 = (Rule) rules.get(0);
            Assert.assertEquals(ruleExpression.cyclify(), rule3.cyclify());
        }
        catch (Exception e) {
            Assert.fail(e.getMessage());
        }

        // instantiate
        Rule rule5 = null;
        try {
            //cycAccess.traceOn();
            Rule rule4 = new Rule("(#$isa ?x #$Cathedral)");
            rule5 = rule4.instantiate(CycVariable.makeCycVariable("?x"),
                                      cycAccess.makeCycConstant("NotreDame"));
        }
        catch (Exception e) {
            Assert.fail(e.getMessage());
        }
        Assert.assertEquals("(#$isa #$NotreDame #$Cathedral)", rule5.cyclify());


        // isDifferent
        try {
            Rule rule6 = new Rule("(#$isa ?x #$Cathedral)");
            Assert.assertTrue(! rule6.isAllDifferent());
            Rule rule7 = new Rule("(#$different ?x ?y)");
            Assert.assertTrue(rule7.isAllDifferent());
        }
        catch (IOException e) {
            Assert.fail(e.getMessage());
        }

        // isEvaluatable
        try {
            Rule rule8 = new Rule("(#$isa ?x #$Cathedral)");
            Assert.assertTrue(! rule8.isEvaluatable());
            Rule rule9 = new Rule("(#$numericallyEqual ?x 1)");
            Assert.assertTrue(rule9.isEvaluatable());
            Rule rule10 = new Rule("(#$and (#$isa ?x #$Cathedral) (#$numericallyEqual ?x 2))");
            Assert.assertTrue(! rule10.isEvaluatable());
            Rule rule11 = new Rule("(#$and (#$numericallyEqual 1 (#$PlusFn ?x)) (#$numericallyEqual ?x 2))");
            Assert.assertTrue(rule11.isEvaluatable());
            Rule rule12 = new Rule("(#$or (#$numericallyEqual 1 (#$PlusFn ?x)) (#$numericallyEqual ?x 2))");
            Assert.assertTrue(rule11.isEvaluatable());
        }
        catch (IOException e) {
            Assert.fail(e.getMessage());
        }

        // evaluateConstraintRule
        try {
            CycList cycList13 = cycAccess.makeCycList("(#$numericallyEqual 0 0)");
            Assert.assertTrue(Rule.evaluateConstraintRule(cycList13));
            CycList cycList14 = cycAccess.makeCycList("(#$numericallyEqual 1 0)");
            Assert.assertTrue(! Rule.evaluateConstraintRule(cycList14));
            CycList cycList15 = cycAccess.makeCycList("(#$numericallyEqual 0 1)");
            Assert.assertTrue(! Rule.evaluateConstraintRule(cycList15));
            CycList cycList16 = cycAccess.makeCycList("(#$numericallyEqual (#$PlusFn 0) 1)");
            Assert.assertTrue(Rule.evaluateConstraintRule(cycList16));
            CycList cycList17 = cycAccess.makeCycList("(#$numericallyEqual (#$PlusFn 3) 1)");
            Assert.assertTrue(! Rule.evaluateConstraintRule(cycList17));
            CycList cycList18 = cycAccess.makeCycList("(#$or (#$numericallyEqual (#$PlusFn 3) 1) " +
                                                      "      (#$numericallyEqual 4 (#$PlusFn 3)))");
            Assert.assertTrue(Rule.evaluateConstraintRule(cycList18));
            CycList cycList19 = cycAccess.makeCycList("(#$or (#$numericallyEqual (#$PlusFn 3) 1) " +
                                                      "      (#$numericallyEqual 4 (#$PlusFn 7)))");
            Assert.assertTrue(! Rule.evaluateConstraintRule(cycList19));
            CycList cycList20 = cycAccess.makeCycList("(#$and (#$numericallyEqual (#$PlusFn 3) 4) " +
                                                      "       (#$numericallyEqual 4 (#$PlusFn 3)))");
            Assert.assertTrue(Rule.evaluateConstraintRule(cycList20));
            CycList cycList21 = cycAccess.makeCycList("(#$and (#$numericallyEqual (#$PlusFn 3) 1) " +
                                                      "       (#$numericallyEqual 4 (#$PlusFn 7)))");
            Assert.assertTrue(! Rule.evaluateConstraintRule(cycList21));
        }
        catch (Exception e) {
            Assert.fail(e.getMessage());
        }

        // substituteVariable
        Rule rule22 = new Rule("(#$isa ?x #$Cathedral)");
        rule22.substituteVariable(CycVariable.makeCycVariable("?x"),
                                  CycVariable.makeCycVariable("?cathedral"));
        Assert.assertEquals("(#$isa ?cathedral #$Cathedral)", rule22.cyclify());
        Rule rule23 = new Rule("(#$isa ?x #$Cathedral)");
        try {
            rule23.substituteVariable(CycVariable.makeCycVariable("?x"),
                                      cycAccess.makeCycConstant("NotreDameCathedral"));
        }
        catch (Exception e) {
            Assert.fail(e.getMessage());
        }
        Assert.assertEquals("(#$isa #$NotreDameCathedral #$Cathedral)", rule23.cyclify());

        //Zebra Puzzle rules
        String zebraPuzzleString =
            "(#$and " +
            "  (#$or " +
            "    (#$numericallyEqual ?norwegian (#$PlusFn ?blue 1)) " +
            "    (#$numericallyEqual ?blue (#$PlusFn ?norwegian 1))) " +
            "  (#$numericallyEqual ?japanese ?volkswagen) " +
            "  (#$numericallyEqual ?mercedes-benz ?orange-juice) " +
            "  (#$or " +
            "    (#$numericallyEqual ?ford (#$PlusFn ?horse 1)) " +
            "    (#$numericallyEqual ?horse (#$PlusFn ?ford 1))) " +
            "  (#$or " +
            "    (#$numericallyEqual ?chevrolet (#$PlusFn ?fox 1)) " +
            "    (#$numericallyEqual ?fox (#$PlusFn ?chevrolet 1))) " +
            "  (#$numericallyEqual ?norwegian 1) " +
            "  (#$numericallyEqual ?milk 3) " +
            "  (#$numericallyEqual ?ford ?yellow) " +
            "  (#$numericallyEqual ?oldsmobile ?snails) " +
            "  (#$numericallyEqual ?green (#$PlusFn ?ivory 1)) " +
            "  (#$numericallyEqual ?ukranian ?eggnog) " +
            "  (#$numericallyEqual ?cocoa ?green) " +
            "  (#$numericallyEqual ?spaniard ?dog) " +
            "  (#$numericallyEqual ?english ?red) " +
            "  (#$different ?ford ?chevrolet ?oldsmobile ?mercedes-benz ?volkswagen) " +
            "  (#$different ?orange-juice ?cocoa ?eggnog ?milk ?water) " +
            "  (#$different ?dog ?snails ?horse ?fox ?zebra) " +
            "  (#$different ?english ?spaniard ?norwegian ?japanese ?ukranian) " +
            "  (#$different ?blue ?red ?green ?yellow ?ivory) " +
            "  (#$elementOf ?blue (#$TheSet 1 2 3 4 5)) " +
            "  (#$elementOf ?chevrolet (#$TheSet 1 2 3 4 5)) " +
            "  (#$elementOf ?cocoa (#$TheSet 1 2 3 4 5)) " +
            "  (#$elementOf ?dog (#$TheSet 1 2 3 4 5)) " +
            "  (#$elementOf ?eggnog (#$TheSet 1 2 3 4 5)) " +
            "  (#$elementOf ?english (#$TheSet 1 2 3 4 5)) " +
            "  (#$elementOf ?ford (#$TheSet 1 2 3 4 5)) " +
            "  (#$elementOf ?fox (#$TheSet 1 2 3 4 5)) " +
            "  (#$elementOf ?green (#$TheSet 1 2 3 4 5)) " +
            "  (#$elementOf ?horse (#$TheSet 1 2 3 4 5)) " +
            "  (#$elementOf ?ivory (#$TheSet 1 2 3 4 5)) " +
            "  (#$elementOf ?japanese (#$TheSet 1 2 3 4 5)) " +
            "  (#$elementOf ?mercedes-benz (#$TheSet 1 2 3 4 5)) " +
            "  (#$elementOf ?milk (#$TheSet 1 2 3 4 5)) " +
            "  (#$elementOf ?norwegian (#$TheSet 1 2 3 4 5)) " +
            "  (#$elementOf ?oldsmobile (#$TheSet 1 2 3 4 5)) " +
            "  (#$elementOf ?orange-juice (#$TheSet 1 2 3 4 5)) " +
            "  (#$elementOf ?red (#$TheSet 1 2 3 4 5)) " +
            "  (#$elementOf ?snails (#$TheSet 1 2 3 4 5)) " +
            "  (#$elementOf ?spaniard (#$TheSet 1 2 3 4 5)) " +
            "  (#$elementOf ?ukranian (#$TheSet 1 2 3 4 5)) " +
            "  (#$elementOf ?volkswagen (#$TheSet 1 2 3 4 5)) " +
            "  (#$elementOf ?water (#$TheSet 1 2 3 4 5)) " +
            "  (#$elementOf ?yellow (#$TheSet 1 2 3 4 5)) " +
            "  (#$elementOf ?zebra (#$TheSet 1 2 3 4 5))) ";
        CycList zebraPuzzleCycList = null;
        try {
            zebraPuzzleCycList = cycAccess.makeCycList(zebraPuzzleString);
        }
        catch (Exception e) {
            Assert.fail(e.getMessage());
        }
        ArrayList zebraPuzzleRules = null;
        try {
            zebraPuzzleRules = Rule.simplifyRuleExpression(zebraPuzzleCycList);
        }
        catch (IOException e) {
            Assert.fail(e.getMessage());
        }
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter("unit-test-output.txt");
        }
        catch (IOException e) {
            Assert.fail(e.getMessage());
        }
        PrintWriter printWriter = new PrintWriter(fileWriter);
        for (int i = 0; i < zebraPuzzleRules.size(); i++) {
            //System.out.println(((Rule) zebraPuzzleRules.get(i)).cyclify());
            printWriter.println(((Rule) zebraPuzzleRules.get(i)).cyclify());
        }
        printWriter.close();

        // subsumes
        Rule rule31 = null;
        Rule rule32 = null;
        Rule rule33 = null;
        Rule rule34 = null;
        Rule rule35 = null;
        Rule rule36 = null;
        try {
            rule31 = new Rule("(#$isa ?country #$WesternEuropeanCountry)");
            rule32 = new Rule("(#$isa ?cathedral #$Cathedral)");
            rule33 = new Rule("(#$countryOfCity ?country ?city)");
            rule34 = new Rule("(#$objectFoundInLocation ?cathedral ?city)");
            rule35 = new Rule("(#$isa ?city #$City)");
            rule36 = new Rule("(#$isa ?country #$Country)");
        }
        catch (Exception e) {
            Assert.fail(e.getMessage());
        }

        try {
            Assert.assertEquals(Rule.SUBSUMES, rule31.determineSubsumption(rule31));
            Assert.assertTrue(rule31.subsumes(rule31));
            Assert.assertTrue(rule31.isSubsumedBy(rule31));
            Assert.assertEquals(Rule.NO_SUBSUMPTION, rule31.determineSubsumption(rule32));
            Assert.assertTrue(! rule31.subsumes(rule32));
            Assert.assertTrue(! rule31.isSubsumedBy(rule32));
            Assert.assertEquals(Rule.NO_SUBSUMPTION, rule31.determineSubsumption(rule33));
            Assert.assertEquals(Rule.NO_SUBSUMPTION, rule31.determineSubsumption(rule34));
            Assert.assertEquals(Rule.NO_SUBSUMPTION, rule31.determineSubsumption(rule35));
            Assert.assertEquals(Rule.SUBSUMED_BY, rule31.determineSubsumption(rule36));
            Assert.assertTrue(rule31.isSubsumedBy(rule36));
            Assert.assertTrue(! (rule31.subsumes(rule36)));
            Assert.assertEquals(Rule.SUBSUMES, rule36.determineSubsumption(rule31));
            Assert.assertTrue(rule36.subsumes(rule31));
            Assert.assertTrue(! (rule36.isSubsumedBy(rule31)));
        }
        catch (Exception e) {
            Assert.fail(e.getMessage());
        }

        try {
            cycAccess.close();
        }
        catch (Exception e) {
            Assert.fail(e.getMessage());
        }

        System.out.println("** Rule OK **");
    }

    /**
     * Tests the <tt>HornClause</tt> class.
     */
    public void testHornClause() {
        System.out.println("** testHornClause **");

        CycAccess cycAccess = null;
        try {
            cycAccess = new CycAccess();
        }
        catch (Exception e) {
            Assert.fail(e.getMessage());
        }
        // constructor
        HornClause hornClause1 = null;
        try {
            //cycAccess.traceOn();
            String hornClauseString =
                "(#$implies " +
                " (#$and " +
                "  (#$isa ?BOAT #$Watercraft-Surface) " +
                "  (#$isa ?WATER #$BodyOfWater) " +
                "  (#$objectFoundInLocation ?BOAT ?WATER)) " +
                " (#$in-Floating ?BOAT ?WATER))";
            hornClause1 = new HornClause(hornClauseString);
            Assert.assertEquals("(#$in-Floating ?BOAT ?WATER)",
                                hornClause1.consequent.cyclify());
            Assert.assertEquals(3, hornClause1.getAntecedantConjuncts().size());
            Assert.assertEquals(2, hornClause1.getVariables().size());
            Assert.assertTrue(
                hornClause1.getVariables().contains(CycVariable.makeCycVariable("?BOAT")));
            Assert.assertTrue(
                hornClause1.getVariables().contains(CycVariable.makeCycVariable("?WATER")));
            Assert.assertTrue(
                hornClause1.getAntecedantConjuncts().contains(
                    new Rule("(#$isa ?BOAT #$Watercraft-Surface)")));
            Assert.assertTrue(
                hornClause1.getAntecedantConjuncts().contains(
                    new Rule("(#$isa ?WATER #$BodyOfWater)")));
            Assert.assertTrue(
                hornClause1.getAntecedantConjuncts().contains(
                    new Rule("(#$objectFoundInLocation ?BOAT ?WATER)")));
        }
        catch (Exception e) {
            Assert.fail(e.getMessage());
        }

        // clone()
        HornClause hornClause2 = (HornClause) hornClause1.clone();
        Assert.assertEquals(hornClause1.toString(), hornClause2.toString());
        Assert.assertEquals(hornClause1.cyclify(), hornClause2.cyclify());
        Assert.assertEquals(hornClause1, hornClause2);
        Assert.assertTrue(hornClause1 != hornClause2);

        // substituteVariable
        HornClause hornClause3 = (HornClause) hornClause1.clone();
        hornClause3.substituteVariable(
            CycVariable.makeCycVariable("?BOAT"),
            CycVariable.makeCycVariable("?waterCraft"));
        Assert.assertTrue(
            ! (hornClause3.getVariables().contains(CycVariable.makeCycVariable("?BOAT"))));
        Assert.assertTrue(
            hornClause3.getVariables().contains(CycVariable.makeCycVariable("?waterCraft")));
        Assert.assertEquals(3, hornClause3.getAntecedantConjuncts().size());
        Assert.assertEquals(2, hornClause3.getVariables().size());
        Assert.assertTrue(
            hornClause3.getVariables().contains(CycVariable.makeCycVariable("?WATER")));
        try {
            Assert.assertTrue(
                hornClause3.getAntecedantConjuncts().contains(
                    new Rule("(#$isa ?waterCraft #$Watercraft-Surface)")));
            Assert.assertTrue(
                hornClause3.getAntecedantConjuncts().contains(
                    new Rule("(#$isa ?WATER #$BodyOfWater)")));
            Assert.assertTrue(
                hornClause3.getAntecedantConjuncts().contains(
                    new Rule("(#$objectFoundInLocation ?waterCraft ?WATER)")));

            HornClause hornClause4 = (HornClause) hornClause1.clone();
            hornClause4.substituteVariable(
                CycVariable.makeCycVariable("?BOAT"),
                cycAccess.makeCycConstant("#$Motorboat"));
            Assert.assertTrue(
                ! (hornClause4.getVariables().contains(CycVariable.makeCycVariable("?BOAT"))));
            Assert.assertEquals(3, hornClause4.getAntecedantConjuncts().size());
            Assert.assertEquals(1, hornClause4.getVariables().size());
            Assert.assertTrue(
                hornClause4.getVariables().contains(CycVariable.makeCycVariable("?WATER")));
            Assert.assertTrue(
                hornClause4.getAntecedantConjuncts().contains(
                    new Rule("(#$isa #$Motorboat #$Watercraft-Surface)")));
            Assert.assertTrue(
                hornClause4.getAntecedantConjuncts().contains(
                    new Rule("(#$isa ?WATER #$BodyOfWater)")));
            Assert.assertTrue(
                hornClause4.getAntecedantConjuncts().contains(
                    new Rule("(#$objectFoundInLocation #$Motorboat ?WATER)")));
        }
        catch (Exception e) {
            Assert.fail(e.getMessage());
        }

        // renameVariables
        HornClause hornClause5 = (HornClause) hornClause1.clone();
        ArrayList otherVariables = new ArrayList();
        Assert.assertTrue(hornClause5.equals(hornClause1));
        hornClause5.renameVariables(otherVariables, 9);
        Assert.assertTrue(hornClause5.equals(hornClause1));

        otherVariables.add(CycVariable.makeCycVariable("?animal"));
        hornClause5.renameVariables(otherVariables, 9);
        Assert.assertTrue(hornClause5.equals(hornClause1));

        otherVariables.add(CycVariable.makeCycVariable("?BOAT"));
        hornClause5.renameVariables(otherVariables, 9);
        Assert.assertEquals("(#$in-Floating ?BOAT_1 ?WATER)",
                            hornClause5.consequent.cyclify());
        Assert.assertEquals(3, hornClause5.getAntecedantConjuncts().size());
        Assert.assertEquals(2, hornClause5.getVariables().size());
        Assert.assertTrue(
            ! (hornClause5.getVariables().contains(CycVariable.makeCycVariable("?BOAT"))));
        Assert.assertTrue(
            hornClause5.getVariables().contains(CycVariable.makeCycVariable("?WATER")));
        try {
            Assert.assertTrue(
                ! (hornClause5.getAntecedantConjuncts().contains(
                    new Rule("(#$isa ?BOAT #$Watercraft-Surface)"))));
            Assert.assertTrue(
                hornClause5.getAntecedantConjuncts().contains(
                    new Rule("(#$isa ?WATER #$BodyOfWater)")));
            Assert.assertTrue(
                ! (hornClause5.getAntecedantConjuncts().contains(
                    new Rule("(#$objectFoundInLocation ?BOAT ?WATER)"))));
        }
        catch (Exception e) {
            Assert.fail(e.getMessage());
        }

        try {
            cycAccess.close();
        }
        catch (Exception e) {
            Assert.fail(e.getMessage());
        }

        System.out.println("** testHornClause OK **");
    }

    /**
     * Tests the <tt>ArgumentTypeConstrainer</tt> class.
     */
    public void testArgumentTypeConstrainer() {
        System.out.println("** testArgumentTypeConstrainer **");

        ConstraintProblem constraintProblem = null;
        try {
            constraintProblem = new ConstraintProblem();
        }
        catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }

        try{
            Rule rule1 = new Rule("(#$countryOfCity ?country ?city)");
            ArrayList argConstraints =
                constraintProblem.argumentTypeConstrainer.retrieveArgumentTypeConstraintRules(rule1);
            Rule rule2 = new Rule ("(#$isa ?country #$Country)");
            Rule rule3 = new Rule ("(#$isa ?city #$City)");
            Assert.assertNotNull(argConstraints);
            Assert.assertEquals(2, argConstraints.size());
            Assert.assertTrue(argConstraints.contains(rule2));
            Assert.assertTrue(argConstraints.contains(rule3));
        }
        catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }

        try {
            CycAccess.current().close();
        }
        catch (Exception e) {
            Assert.fail(e.getMessage());
        }

        System.out.println("** testArgumentTypeConstrainer OK **");
    }

    /**
     * Tests the <tt>Unifier</tt> class.
     */
    public void testUnifier() {
        System.out.println("** testUnifier **");

        ConstraintProblem constraintProblem = null;
        try {
            constraintProblem = new ConstraintProblem();
        }
        catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
        CycAccess cycAccess = constraintProblem.cycAccess;
        Unifier unifier = constraintProblem.backchainer.unifier;

        // unify
        try {
            Rule rule1 = new Rule("(#$objectFoundInLocation #$CityOfAustinTX ?where)");
            String hornClauseString =
                "(#$implies " +
                " (#$and " +
                "  (#$isa ?WATER #$BodyOfWater) " +
                "  (#$in-Floating ?OBJ ?WATER)) " +
                " (#$objectFoundInLocation ?OBJ ?WATER))";
            HornClause hornClause1 = new HornClause(hornClauseString);
            ArrayList unifiedConjuncts = unifier.unify(rule1, hornClause1);
            Assert.assertEquals(2, unifiedConjuncts.size());
            System.out.println("unified conjuncts: " + unifiedConjuncts);
            Assert.assertTrue(unifiedConjuncts.contains(new Rule("(#$in-Floating #$CityOfAustinTX ?where)")));
            Assert.assertTrue(unifiedConjuncts.contains(new Rule("(#$isa ?where #$BodyOfWater)")));

            Rule rule2 = new Rule("(#$doneBy #$CityOfAustinTX ?what)");
            String hornClauseString2 =
                "(#$implies " +
                " (#$and " +
                "  (#$isa ?WATER #$BodyOfWater) " +
                "  (#$in-Floating ?OBJ ?WATER)) " +
                " (#$objectFoundInLocation ?OBJ ?WATER))";
            HornClause hornClause2 = new HornClause(hornClauseString2);
            ArrayList unifiedConjuncts2 = unifier.unify(rule2, hornClause2);
            Assert.assertNull(unifiedConjuncts2);

            Rule rule3 = new Rule("(#$objectFoundInLocation #$CityOfAustinTX ?where)");
            String hornClauseString3 =
                "(#$implies " +
                " (#$and " +
                "  (#$isa ?WATER #$BodyOfWater) " +
                "  (#$in-Floating ?OBJ ?WATER)) " +
                " (#$objectFoundInLocation #$CityOfHoustonTX ?WATER))";
            HornClause hornClause3 = new HornClause(hornClauseString3);
            ArrayList unifiedConjuncts3 = unifier.unify(rule3, hornClause3);
            Assert.assertNull(unifiedConjuncts2);
        }
        catch (Exception e) {
            Assert.fail(e.getMessage());
        }

        System.out.println("** testUnifier OK **");
    }

    /**
     * Tests the <tt>Solution</tt> class.
     */
    public void testSolution() {
        System.out.println("** testSolution **");

        // constructor
        ConstraintProblem constraintProblem = null;
        try {
            constraintProblem = new ConstraintProblem();
        }
        catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
        Solution solution = new Solution(constraintProblem);

        // getCurrentSolution
        Assert.assertTrue(solution.getCurrentSolution().size() == 0);

        // getSolutions
        Assert.assertTrue(solution.getSolutions().size() == 1);
        Binding binding1 = new Binding(CycVariable.makeCycVariable("?x"), new Long(1));

        // addBindingToCurrentSolution
        solution.addBindingToCurrentSolution(binding1);
        Assert.assertTrue(solution.getCurrentSolution().size() == 1);
        Assert.assertTrue(solution.getCurrentSolution().contains(binding1));

        // removeBindingFromCurrentSolution
        solution.removeBindingFromCurrentSolution(binding1);
        Assert.assertTrue(solution.getCurrentSolution().size() == 0);

        // addBindingToCurrentSolution
        Binding binding2 = new Binding(CycVariable.makeCycVariable("?y"), new Long(2));
        Binding binding3 = new Binding(CycVariable.makeCycVariable("?z"), new Long(3));
        solution.addBindingToCurrentSolution(binding2);
        solution.addBindingToCurrentSolution(binding3);
        Assert.assertTrue(solution.getCurrentSolution().size() == 2);

        // addSolution
        solution.addSolution(new ArrayList());
        Assert.assertTrue(solution.getSolutions().size() == 2);
        Assert.assertTrue(solution.getCurrentSolution().size() == 0);
        solution.addBindingToCurrentSolution(binding1);
        Assert.assertTrue(solution.getCurrentSolution().size() == 1);
        Assert.assertTrue(solution.getCurrentSolution().contains(binding1));

        // recordNewSolution
        solution.addBindingToCurrentSolution(binding2);
        solution.addBindingToCurrentSolution(binding3);
        Assert.assertTrue(solution.getCurrentSolution().size() == 3);
        Assert.assertTrue(solution.getCurrentSolution().contains(binding2));
        solution.recordNewSolution(binding2);
        Assert.assertTrue(solution.getSolutions().size() == 3);
        Assert.assertTrue(solution.getCurrentSolution().size() == 2);
        Assert.assertTrue(! solution.getCurrentSolution().contains(binding2));

        // finalizeAllSolutions
        solution.addSolution(new ArrayList());
        Assert.assertTrue(solution.getSolutions().size() == 4);
        Assert.assertTrue(solution.getCurrentSolution().size() == 0);
        constraintProblem.nbrSolutionsRequested = null;
        solution.nbrSolutionsFound = 3;
        solution.finalizeAllSolutions();
        Assert.assertTrue(solution.getSolutions().size() == 3);
        Assert.assertTrue(solution.getCurrentSolution().size() == 2);

        System.out.println("** testSolution OK **");
    }

    /**
     * Tests the <tt>ConstraintProblem</tt> class.
     */
    public void testConstraintProblem1() {
        System.out.println("** testConstraintProblem1 **");

        //Zebra Puzzle
        String zebraPuzzleString =
            "(#$and " +
            "  (#$or " +
            "    (#$numericallyEqual ?norwegian (#$PlusFn ?blue 1)) " +
            "    (#$numericallyEqual ?blue (#$PlusFn ?norwegian 1))) " +
            "  (#$numericallyEqual ?japanese ?volkswagen) " +
            "  (#$numericallyEqual ?mercedes-benz ?orange-juice) " +
            "  (#$or " +
            "    (#$numericallyEqual ?ford (#$PlusFn ?horse 1)) " +
            "    (#$numericallyEqual ?horse (#$PlusFn ?ford 1))) " +
            "  (#$or " +
            "    (#$numericallyEqual ?chevrolet (#$PlusFn ?fox 1)) " +
            "    (#$numericallyEqual ?fox (#$PlusFn ?chevrolet 1))) " +
            "  (#$numericallyEqual ?norwegian 1) " +
            "  (#$numericallyEqual ?milk 3) " +
            "  (#$numericallyEqual ?ford ?yellow) " +
            "  (#$numericallyEqual ?oldsmobile ?snails) " +
            "  (#$numericallyEqual ?green (#$PlusFn ?ivory 1)) " +
            "  (#$numericallyEqual ?ukranian ?eggnog) " +
            "  (#$numericallyEqual ?cocoa ?green) " +
            "  (#$numericallyEqual ?spaniard ?dog) " +
            "  (#$numericallyEqual ?english ?red) " +
            "  (#$different ?ford ?chevrolet ?oldsmobile ?mercedes-benz ?volkswagen) " +
            "  (#$different ?orange-juice ?cocoa ?eggnog ?milk ?water) " +
            "  (#$different ?dog ?snails ?horse ?fox ?zebra) " +
            "  (#$different ?english ?spaniard ?norwegian ?japanese ?ukranian) " +
            "  (#$different ?blue ?red ?green ?yellow ?ivory) " +
            "  (#$elementOf ?blue (#$TheSet 1 2 3 4 5)) " +
            "  (#$elementOf ?chevrolet (#$TheSet 1 2 3 4 5)) " +
            "  (#$elementOf ?cocoa (#$TheSet 1 2 3 4 5)) " +
            "  (#$elementOf ?dog (#$TheSet 1 2 3 4 5)) " +
            "  (#$elementOf ?eggnog (#$TheSet 1 2 3 4 5)) " +
            "  (#$elementOf ?english (#$TheSet 1 2 3 4 5)) " +
            "  (#$elementOf ?ford (#$TheSet 1 2 3 4 5)) " +
            "  (#$elementOf ?fox (#$TheSet 1 2 3 4 5)) " +
            "  (#$elementOf ?green (#$TheSet 1 2 3 4 5)) " +
            "  (#$elementOf ?horse (#$TheSet 1 2 3 4 5)) " +
            "  (#$elementOf ?ivory (#$TheSet 1 2 3 4 5)) " +
            "  (#$elementOf ?japanese (#$TheSet 1 2 3 4 5)) " +
            "  (#$elementOf ?mercedes-benz (#$TheSet 1 2 3 4 5)) " +
            "  (#$elementOf ?milk (#$TheSet 1 2 3 4 5)) " +
            "  (#$elementOf ?norwegian (#$TheSet 1 2 3 4 5)) " +
            "  (#$elementOf ?oldsmobile (#$TheSet 1 2 3 4 5)) " +
            "  (#$elementOf ?orange-juice (#$TheSet 1 2 3 4 5)) " +
            "  (#$elementOf ?red (#$TheSet 1 2 3 4 5)) " +
            "  (#$elementOf ?snails (#$TheSet 1 2 3 4 5)) " +
            "  (#$elementOf ?spaniard (#$TheSet 1 2 3 4 5)) " +
            "  (#$elementOf ?ukranian (#$TheSet 1 2 3 4 5)) " +
            "  (#$elementOf ?volkswagen (#$TheSet 1 2 3 4 5)) " +
            "  (#$elementOf ?water (#$TheSet 1 2 3 4 5)) " +
            "  (#$elementOf ?yellow (#$TheSet 1 2 3 4 5)) " +
            "  (#$elementOf ?zebra (#$TheSet 1 2 3 4 5))) ";
        ConstraintProblem zebraProblem = new ConstraintProblem();
        CycAccess cycAccess = zebraProblem.cycAccess;
        CycList zebraPuzzleCycList = cycAccess.makeCycList(zebraPuzzleString);
        try {
            ArrayList zebraPuzzleRules = Rule.simplifyRuleExpression(zebraPuzzleCycList);
        }
        catch (IOException e) {
            Assert.fail(e.getMessage());
        }

        zebraProblem.setVerbosity(1);
        ArrayList solutions = zebraProblem.solve(zebraPuzzleCycList);
        Assert.assertNotNull(solutions);

        // test extractRulesAndDomains()
        zebraProblem.displayConstraintRules();
        Assert.assertEquals(17, zebraProblem.getNbrConstraintRules());
        Assert.assertEquals(25, zebraProblem.getNbrDomainPopulationRules());

        // test gatherVariables()
        Assert.assertEquals(25, zebraProblem.getNbrVariables());

        // test ValueDomains.initializeDomains()
        Assert.assertEquals(25, zebraProblem.valueDomains.domains.size());
        Assert.assertEquals(25, zebraProblem.valueDomains.varsDictionary.size());
        CycVariable blue = CycVariable.makeCycVariable("?blue");
        Assert.assertNotNull(zebraProblem.valueDomains.varsDictionary.get(blue));
        Assert.assertTrue(zebraProblem.valueDomains.varsDictionary.get(blue) instanceof ArrayList);
        ArrayList domainValues = (ArrayList) zebraProblem.valueDomains.varsDictionary.get(blue);
        Assert.assertEquals(5, domainValues.size());
        Assert.assertTrue(domainValues.contains(new Long(1)));
        Assert.assertTrue(domainValues.contains(new Long(2)));
        Assert.assertTrue(domainValues.contains(new Long(3)));
        Assert.assertTrue(domainValues.contains(new Long(4)));
        Assert.assertTrue(domainValues.contains(new Long(5)));

        // test ValueDomains.domainHasValue(CycVariable cycVariable, Object value)
        Assert.assertTrue(zebraProblem.valueDomains.domainHasValue(blue, new Long(1)));
        Assert.assertTrue(! (zebraProblem.valueDomains.domainHasValue(blue, new Long(6))));

        // test ValueDomains.getDomainValues(CycVariable cycVariable)
        ArrayList domainValues2 = zebraProblem.valueDomains.getDomainValues(blue);
        Assert.assertEquals(domainValues, domainValues2);

        // test ValueDomains.initializeDomainValueMarking()
        Assert.assertNotNull(zebraProblem.valueDomains.domains.get(blue));
        Assert.assertTrue((zebraProblem.valueDomains.domains.get(blue)) instanceof HashMap);
        HashMap domainValueMarks = (HashMap) zebraProblem.valueDomains.domains.get(blue);
        Assert.assertTrue(domainValueMarks.containsKey(new Long(1)));
        Assert.assertNotNull(domainValueMarks.get(new Long(1)));

        // test HighCardinalityDomains
        Assert.assertTrue(zebraProblem.highCardinalityDomains.highCardinalityDomains.size() == 0);

        // test NodeConsistencyAchiever.applyUnaryRulesAndPropagate()
        Assert.assertEquals(27, zebraProblem.nodeConsistencyAchiever.unaryConstraintRules.size());
        Assert.assertTrue(zebraProblem.nodeConsistencyAchiever.affectedVariables.contains(CycVariable.makeCycVariable("?milk")));
        Assert.assertTrue(zebraProblem.nodeConsistencyAchiever.affectedVariables.contains(CycVariable.makeCycVariable("?norwegian")));
        Assert.assertEquals(5, zebraProblem.nodeConsistencyAchiever.allDifferentRules.size());
        Assert.assertTrue(zebraProblem.nodeConsistencyAchiever.singletons.contains(CycVariable.makeCycVariable("milk")));
        Assert.assertTrue(zebraProblem.nodeConsistencyAchiever.singletons.contains(CycVariable.makeCycVariable("norwegian")));

        System.out.println("** testConstraintProblem1 OK **");
    }
    /**
     * Tests the <tt>ConstraintProblem</tt> class.
     */
    public void testConstraintProblem2() {
        System.out.println("** testConstraintProblem2 **");

        // European Cathedrals with arg type discovery
        String europeanCathedralsString2 =
            "(#$and " +
            "  (#$isa ?country #$WesternEuropeanCountry) " +
            "  (#$isa ?cathedral #$Cathedral) " +
            "  (#$countryOfCity ?country ?city) " +
            "  (#$objectFoundInLocation ?cathedral ?city)) ";
        System.out.println(europeanCathedralsString2);
        ConstraintProblem europeanCathedralsProblem2 = new ConstraintProblem();
        europeanCathedralsProblem2.setVerbosity(8);
        // Request one solution.
        europeanCathedralsProblem2.nbrSolutionsRequested = new Integer(1);
        // Request all solutions.
        //europeanCathedralsProblem2.nbrSolutionsRequested = null;
        try {
            europeanCathedralsProblem2.mt =
                CycAccess.current().getConstantByName("TourAndVacationPackageItinerariesMt");
            ArrayList solutions = europeanCathedralsProblem2.solve(CycAccess.current().makeCycList(europeanCathedralsString2));
        Assert.assertNotNull(solutions);
        }
        catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
            Assert.fail(e.getMessage());
        }

        // European Cathedrals
        String europeanCathedralsString =
            "(#$and " +
            "  (#$isa ?country #$WesternEuropeanCountry) " +
            "  (#$isa ?city #$City) " +
            "  (#$isa ?cathedral #$Cathedral) " +
            "  (#$countryOfCity ?country ?city) " +
            "  (#$objectFoundInLocation ?cathedral ?city)) ";
        System.out.println(europeanCathedralsString);
        ConstraintProblem europeanCathedralsProblem = new ConstraintProblem();
        europeanCathedralsProblem.setVerbosity(1);
        // Request two solutions.
        // europeanCathedralsProblem.nbrSolutionsRequested = new Integer(2);
        // Request all solutions.
        europeanCathedralsProblem.nbrSolutionsRequested = null;
        try {
            europeanCathedralsProblem.mt =
                CycAccess.current().getConstantByName("TourAndVacationPackageItinerariesMt");
        }
        catch (IOException e) {
            Assert.fail(e.getMessage());
        }
        ArrayList solutions = europeanCathedralsProblem.solve(CycAccess.current().makeCycList(europeanCathedralsString));
        Assert.assertNotNull(solutions);


        System.out.println("** testConstraintProblem2 OK **");
    }

}