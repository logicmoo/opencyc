package org.opencyc.constraintsolver;

import java.util.*;
import org.opencyc.cycobject.*;
import org.opencyc.api.*;

/**
 * <tt>Rule</tt> object to model the attributes and behavior of a constraint rule.<p>
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
 *
 * @see UnitTest#testRule
 */
public class Rule {

    /**
     * The constraint rule formula as an OpenCyc query.
     */
    protected CycList rule;

    /**
     * The collection of <tt>CycVariables</tt> used in the rule.  There should
     * be at least one, because if there are no variables, then the rule is
     * either always true or always false and has no effect on the
     * constraint problem's solution.
     */
    protected ArrayList variables;

    /**
     * The depth of backchaining when this rule was introduced.  For rules that originate
     * from the input constraint problem, this value is 0.  When this value equals the
     * maximum depth of backchain limit, then this rule cannot be the subject of a further
     * backchain inference step.
     */
    protected int backchainDepth = 0;

    /**
     * Constructs a new <tt>Rule</tt> object from a <tt>CycList</tt> <tt>String</tt>
     * representation.<p>
     *
     * @param ruleString the rule's formula <tt>String</tt>, which must be a well formed OpenCyc
     * query represented by a <tt>CycList</tt>.
     * @param cycAccess the OpenCyc api connection
     */
    public Rule (String ruleString, CycAccess cycAccess) {
        rule = cycAccess.makeCycList(ruleString);
        gatherVariables();
    }

    /**
     * Constructs a new <tt>Rule</tt> object from a <tt>CycList</tt>.<p>
     *
     * <pre>
     *  String ruleAsString = "(#$isa ?x #$Cathedral)";
     *  Rule rule1 = new Rule (cycAccess.makeCycList(ruleAsString));
     * </pre>
     *
     * @param rule the rule's formula, which must be a well formed OpenCyc
     * query represented by a <tt>CycList</tt>.
     */
    public Rule(CycList rule) {
        this.rule = rule;
        gatherVariables();
    }

    /**
     * Constructs a new <tt>Rule</tt> object from a <tt>CycList</tt> at the given
     * backchain depth.<p>
     *
     * <pre>
     *  String ruleAsString = "(#$isa ?x #$Cathedral)";
     *  Rule rule1 = new Rule (new CycList(ruleAsString), 2);
     * </pre>
     *
     * @param rule the rule's formula, which must be a well formed OpenCyc
     * query represented by a <tt>CycList</tt>.
     * @param backchainDepth the depth of backchaining when this rule is introduced
     */
    public Rule(CycList rule, int backchainDepth) {
        this.rule = rule;
        this.backchainDepth = backchainDepth;
        gatherVariables();
    }

    /**
     * Simplifies a rule expression.<p>
     * (#$and (<rule1> <rule2> ... <ruleN>) becomes <rule1> <rule2> ... <ruleN>
     *
     * @param cycList the rule expression that is simplified
     * @return an <tt>ArrayList</tt> of <tt>Rule</tt> objects.
     * @see UnitTest#testRule
     */
    public static ArrayList simplifyRuleExpression(CycList cycList) {
        ArrayList rules = new ArrayList();
        if (cycList.size() < 2)
            throw new RuntimeException("Invalid rule: " + cycList);
        //TODO - use static value from CycConstant class.
        if (cycList.first().toString().equals("and"))
            for (int i = 1; i < cycList.size(); i++)
                rules.add(new Rule((CycList) cycList.get(i)));
        else
            rules.add(new Rule(cycList));
        return rules;
    }

    /**
     * Gathers the unique variables from the rule's formula.
     */
    protected void gatherVariables() {
        HashSet uniqueVariables = new HashSet();
        Enumeration e = rule.cycListVisitor();
        while (true) {
            if (! e.hasMoreElements())
                break;
            Object element = e.nextElement();
            if (element instanceof CycVariable)
                uniqueVariables.add(element);
        }
        variables = new ArrayList(uniqueVariables);
    }


    /**
     * Gets the rule's formula.
     *
     * @return a <tt>CycList</tt> which is the rule's formula.
     */
    public CycList getRule() {
        return rule;
    }

    /**
     * Returns the rule's variables.
     *
     * @return the <tt>ArrayList</tt> which lists the unique <tt>CycVariables</tt> that are
     * used in the rule's formula.
     */
    public ArrayList getVariables() {
        return variables;
    }

    /**
     * Returns the rule's arity which is defined to be the number of variables, not
     * necessarily equalling the arity of the rule's first predicate.
     *
     * @return rule's arity which is defined to be the number of variables, not
     * necessarily equalling the arity of the rule's first predicate
     */
    public int getArity() {
        return variables.size();
    }

    /**
     * Returns the backchain depth when this rule was introduced.
     *
     * @return the backchain depth when this rule was introduced
     */
    public int getBackchainDepth() {
        return this.backchainDepth;
    }

    /**
     * Returns <tt>true</tt> if the object equals this object.
     *
     * @param object the object for comparison
     * @return <tt>boolean</tt> indicating equality of an object with this object.
     */
    public boolean equals(Object object) {
        if (! (object instanceof Rule))
            return false;
        Rule thatRule = (Rule) object;
        return this.rule.equals(thatRule.getRule());
    }

    /**
     * Creates and returns a copy of this <tt>Rule</tt>.
     *
     * @return a clone of this instance
     */
    public Object clone() {
        return new Rule((CycList) this.rule.clone());
    }

    /**
     * Returns the predicate of this <tt>Rule</tt> object.
     *
     * @return the predicate <tt>CycConstant</tt> or <tt>CycSymbol</tt>
     * of this <tt>Rule</tt> object
     */
    public CycConstant getPredicate() {
        return (CycConstant) rule.first();
    }

    /**
     * Returns the arguments of this <tt>Rule</tt> object.
     *
     * @return the arguments of this <tt>Rule</tt> object
     */
    public CycList getArguments() {
        return (CycList) rule.rest();
    }

    /**
     * Substitutes an object for a variable.
     *
     * @param oldVariable the variable to replaced
     * @parma newObject the <tt>Object</tt> to be substituted for the variable
     */
    public void substituteVariable(CycVariable variable, Object newObject) {
        if (! (variables.contains(variable)))
            throw new RuntimeException(variable + " is not a variable of " + this);
        variables.remove(variable);
        if (newObject instanceof CycVariable)
            variables.add(newObject);
        rule = rule.subst(newObject, variable);
    }

    /**
     * Returns <tt>true</tt> if this is a variable domain populating <tt>Rule</tt>.
     *
     * @return <tt>boolean</tt> indicating if this is a variable domain populating
     * <tt>Rule</tt>.
     */
    public boolean isVariableDomainPopulatingRule() {
        return isIntensionalVariableDomainPopulatingRule() ||
               isExtensionalVariableDomainPopulatingRule();
    }

    /**
     * Returns <tt>true</tt> if this <tt>Rule</tt> is a #$different constraint rule.
     *
     * @return <tt>boolean</tt> indicating if this <tt>Rule</tt> is a #$different
     * constraint rule
     */
    public boolean isAllDifferent() {
        if (this.getArity() < 2)
            return false;
        //TODO make right
        if (this.getPredicate().toString().equals("different"))
            return true;
        else
            return false;
    }

    /**
     * Returns <tt>true</tt> if this <tt>Rule</tt> is a simple evaluatable constraint rule,
     * which can be answered without KB lookup.  Typically an evaluatable constraint
     * rule is a relational operator applied to a primitive data type.
     *
     *
     * @return <tt>true</tt> if this <tt>Rule</tt> is a simple evaluatable constraint rule,
     * which can be answered without KB lookup
     */
    public boolean isEvaluatable() {
        if (this.getArguments().size() < 2)
            return false;
        if (this.getPredicate().toString().equals("numericallyEqual"))
            return hasEvaluatableNumericalArgs();
        else if (this.getPredicate().toString().equals("or") ||
                 this.getPredicate().toString().equals("and")) {
            for (int i = 0; i < this.getArguments().size(); i++) {
                Rule orArgument = new Rule((CycList) this.getArguments().get(i));
                if (! orArgument.isEvaluatable())
                    return false;
            }
            return true;
        }
        else
            return false;
    }

    /**
     * Returns <tt>true</tt> if this <tt>Rule</tt> has simple evaluatable numerical arguments.
     * Numbers and variables return <tt>true</tt> and functional expressions return
     * <tt>true</tt> iff their arguments are simple numerical expressions.
     *
     *
     * @return <tt>true</tt> if this <tt>Rule</tt> has simple evaluatable numerical arguments
     */
    public boolean hasEvaluatableNumericalArgs() {
        CycList args = this.getRule().rest();
        for (int i = 0; i < args.size(); i++) {
            Object arg = args.get(i);
            if (arg instanceof CycVariable)
                continue;
            else if (arg instanceof Long)
                continue;
            else if (arg instanceof CycNart) {
                CycNart cycNart = (CycNart) arg;
                if (cycNart.getFunctor().toString().equals("PlusFn")) {
                    Object plusFnArg = cycNart.getArguments().get(0);
                    if (plusFnArg instanceof CycVariable)
                        continue;
                    if (plusFnArg instanceof Long)
                        continue;
                }
            }
            else if (arg instanceof CycList) {
                CycList cycList = (CycList) arg;
                if (cycList.first().toString().equals("PlusFn")) {
                    Object plusFnArg = cycList.second();
                    if (plusFnArg instanceof CycVariable)
                        continue;
                    if (plusFnArg instanceof Long)
                        continue;
                }
            }
            else
                return false;
        }
        return true;
    }

    /**
     * Evaluates the instantiated constraint rule locally without asking OpenCyc.
     *
     * @param instantiatedRule the fully instantiated constraint rule whose predicates
     * can be evaluated locally without asking OpenCyc.
     * @return the truth value of the fully instantiated constraint rule
     */
    public static boolean evaluateConstraintRule(CycList instantiatedRule) {
        CycConstant predicate = (CycConstant) instantiatedRule.first();
        if (predicate.toString().equals("numericallyEqual")) {
            long value = numericallyEvaluateExpression(instantiatedRule.second());
            for (int i = 2; i < instantiatedRule.size(); i++) {
                if (numericallyEvaluateExpression(instantiatedRule.get(i)) != value)
                    return false;
            }
            return true;
        }
        else if (predicate.toString().equals("or")) {
            CycList args = instantiatedRule.rest();
            for (int i = 0; i < args.size(); i++) {
                CycList arg = (CycList) args.get(i);
                if (evaluateConstraintRule(arg))
                    return true;
            }
            return false;
        }
        else if (predicate.toString().equals("and")) {
            CycList args = instantiatedRule.rest();
            for (int i = 0; i < args.size(); i++) {
                CycList arg = (CycList) args.get(i);
                if (! evaluateConstraintRule(arg))
                    return false;
            }
            return true;
        }
        else
            throw new RuntimeException(instantiatedRule + "Cannot be evaluated");
    }

    /**
     * Returns the numerical value of the expression.
     *
     * @param expression the expression to be evaluated which can be a <tt>Long</tt> value,
     * or a <tt>CycList</tt>
     * @return the numerical value of the expression
     */
    public static long numericallyEvaluateExpression(Object expression) {
        if (expression instanceof Long)
            return ((Long) expression).longValue();
        else if (expression instanceof CycNart) {
            CycNart cycNart = (CycNart) expression;
            CycFort functor = cycNart.getFunctor();
            Object arg = cycNart.getArguments().get(0);
            if (functor.toString().equals("PlusFn")) {
                return numericallyEvaluateExpression(arg) + 1;
            }
        }
        else if (expression instanceof CycList) {
            CycList cycList = (CycList) expression;
            CycConstant functor = (CycConstant) cycList.first();
            Object arg = cycList.get(1);
            if (functor.toString().equals("PlusFn")) {
                return numericallyEvaluateExpression(arg) + 1;
            }
        }
        throw new RuntimeException(expression + "Cannot be evaluated");
    }


    /**
     * Returns <tt>true</tt> if this is an intensional variable domain populating <tt>Rule</tt>.
     * An extensional rule is one in which values are queried from the OpenCyc KB.
     *
     * @return <tt>boolean</tt> indicating if this is an intensional variable domain populating
     * <tt>Rule</tt>.
     */
    public boolean isIntensionalVariableDomainPopulatingRule() {
        if (this.getArity() != 1)
            // Only unary rules can populate a domain.
            return false;
        //TODO make right
        if (this.getPredicate().toString().equals("isa"))
            return true;
        else
            return false;
    }

    /**
     * Returns <tt>true</tt> if this is an extensional variable domain populating <tt>Rule</tt>.
     * An extensional rule is one in which all the values are listed.
     *
     * @return <tt>boolean</tt> indicating if this is an extensional variable domain populating
     * <tt>Rule</tt>.
     */
    public boolean isExtensionalVariableDomainPopulatingRule() {
        if (this.getArity() != 1)
            // Only unary rules can populate a domain.
            return false;
        //TODO put elementOf in the right place
        //if (this.predicate().equals(CycConstant.elementOf))
        if (this.getPredicate().toString().equals("elementOf"))
            return true;
        else
            return false;
    }

    /**
     * Returns a string representation of the <tt>Rule</tt>.
     *
     * @return the rule's formula formated as a <tt>String</tt>.
     */
    public String toString() {
        return rule.toString();
    }

    /**
     * Returns a cyclified string representation of the rule's formula.
     * Embedded constants are prefixed with ""#$".
     *
     * @return a cyclified <tt>String</tt>.
     */
    public String cyclify() {
        return rule.cyclify();
    }

    /**
     * Returns a new <tt>Rule</tt> which is the result of substituting the given
     * <tt>Object</tt> value for the given <tt>CycVariable</tt>.
     *
     * @param cycVariable the variable for substitution
     * @param value the value which is substituted for each occurrance of the variable
     * @return a new <tt>Rule</tt> which is the result of substituting the given
     * <tt>Object</tt> value for the given <tt>CycVariable</tt>
     */
    public Rule instantiate(CycVariable cycVariable, Object value) {
        if (! variables.contains(cycVariable))
            throw new RuntimeException("Cannot instantiate " + cycVariable +
                                       " in rule " + this);
        CycList newRule = rule.subst(value, cycVariable);
        return new Rule(newRule);
    }
}