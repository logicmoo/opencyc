package org.opencyc.constraintsolver;

import org.opencyc.cycobject.*;
import java.util.*;

/**
 * <tt>RuleEvaluator</tt> object evaluates constraint rules
 * for the parent <tt>ConstraintProblem</tt> object.  Although all
 * rules may be evaluated by asking the OpenCyc KB, some rules having
 * evaluatable predicates and functions may be efficiently evaluated locally<p>
 *
 * @version $Id$
 * @author Stephen L. Reed
 *
 * <p>Copyright 2001 OpenCyc.org, license is open source GNU LGPL.
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
 * @see UnitTest#testConstraintProblem
 */
public class RuleEvaluator {

    /**
     * Reference to the parent <tt>ConstraintProblem</tt> object.
     */
    protected ConstraintProblem constraintProblem;

    /**
     * Sets verbosity of the constraint solver output.  0 --> quiet ... 9 -> maximum
     * diagnostic input.
     */
    protected int verbosity = 9;

    /**
     * Cached reference to #$numericallyEqual predicate.
     */
    protected CycConstant numericallyEqual = CycConstant.makeCycConstant("numericallyEqual");

    /**
     * Cached reference to #$and logical operator.
     */
    protected CycConstant and = CycConstant.makeCycConstant("and");

    /**
     * Cached reference to #$or logical operator.
     */
    protected CycConstant or = CycConstant.makeCycConstant("or");

    /**
     * Cached reference to #$not logical operator.
     */
    protected CycConstant not = CycConstant.makeCycConstant("not");

    /**
     * Cached reference to #$different predicate.
     */
    protected CycConstant different = CycConstant.makeCycConstant("different");

    /**
     * Cached reference to #$PlusFn function.
     */
    protected CycConstant plusFn = CycConstant.makeCycConstant("PlusFn");

    /**
     * Constructs a new <tt>RuleEvaluator</tt> object for the parent
     * <tt>ConstraintProblem</tt>.
     *
     * @param constraintProblem the parent constraint problem
     */
    public RuleEvaluator(ConstraintProblem constraintProblem) {
        this.constraintProblem = constraintProblem;
    }

    /**
     * Return <tt>true</tt> iff the instantiated constraint rule is proven true,
     * otherwise return <tt>false</tt>
     *
     * @param rule the instantiated constraint rule to be evaluated
     * @return <tt>true</tt> iff the instantiated constraint rule is proven true,
     * otherwise return <tt>false</tt>
     */
    protected boolean ask(Rule rule) {
        Object predicate = rule.getPredicate();
        if (predicate.equals(and)) {
            CycList arguments = rule.getRule().rest();
            for (int i = 0; i < arguments.size(); i++) {
                Object argument = arguments.get(i);
                if (! (argument instanceof CycList))
                    throw new RuntimeException("Invalid #$or argument: " +
                                                argument);
                if (! ask(new Rule((CycList) arguments.get(i))))
                    return false;
            }
            return true;
        }
        else if (predicate.equals(or)) {
            CycList arguments = rule.getRule().rest();
            for (int i = 0; i < arguments.size(); i++) {
                Object argument = arguments.get(i);
                if (! (argument instanceof CycList))
                    throw new RuntimeException("Invalid #$or argument: " +
                                                argument);
                if (ask(new Rule((CycList) arguments.get(i))))
                    return true;
            }
            return false;
        }
        else if (predicate.equals(not)) {
            CycList expression = (CycList) rule.getRule().second();
            if (! (expression instanceof CycList) ||
                expression.size() < 2)
                throw new RuntimeException("Invalid #$not expression: " +
                                            expression);
            return ! ask(new Rule(expression));
        }
        else if (predicate.equals(numericallyEqual)) {
            //TODO if args are not numeric, ask OpenCyc.
            Object argument1 = rule.getRule().second();
            long argument1Long = 0;
            if (argument1 instanceof CycList) {
                if (((CycList) argument1).first().equals(plusFn) &&
                    ((CycList) argument1).second() instanceof Long)
                    argument1Long = 1 + ((Long) ((CycList) argument1).second()).longValue();
            }
            else if (argument1 instanceof Long)
                argument1Long = ((Long) argument1).longValue();
            else
                throw new RuntimeException("Invalid #$numericallyEqual argument1: " +
                                            argument1);
            Object argument2 = rule.getRule().third();
            long argument2Long = 0;
            if (argument2 instanceof CycList) {
                if (((CycList) argument2).first().equals(plusFn) &&
                    ((CycList) argument2).second() instanceof Long)
                    argument2Long = 1 + ((Long) ((CycList) argument2).second()).longValue();
            }
            else if (argument2 instanceof Long)
                argument2Long = ((Long) argument2).longValue();
            else
                throw new RuntimeException("Invalid #$numericallyEqual argument2: " +
                                            argument2);
            return argument1Long == argument2Long;
        }
        else if (predicate.equals(different)) {
            CycList arguments = rule.getRule().rest();
            return ! arguments.containsDuplicates();
        }
        else
            //TODO ask OpenCyc
            throw new RuntimeException("Cannot locally evaluate " + rule);

    }

    /**
     * Sets verbosity of the constraint solver output.  0 --> quiet ... 9 -> maximum
     * diagnostic input.
     *
     * @param verbosity 0 --> quiet ... 9 -> maximum diagnostic input
     */
    protected void setVerbosity(int verbosity) {
        this.verbosity = verbosity;
    }

}