package org.opencyc.elf.bg.predicate;

//// Internal Imports
import org.opencyc.cycobject.CycList;

import org.opencyc.elf.bg.expression.Operator;

import org.opencyc.elf.wm.state.State;

//// External Imports
import java.util.List;

/** NotNull is a predicate of arity one that returns true if its argument
 * is not null.
 *
 * @version $Id$
 * @author  reed
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
public class NotNull extends Operator implements Predicate {
  
  //// Constructors
  
  /** Creates a new instance of NotNull */
  public NotNull() {
    super();
  }
  
  //// Public Area
    
  /** Evaluates the given arguments and returns true if the first is not null.
   *
   * @param arguments the given arguments to evaluate
   * @param state the given state
   * @return true if the first argument is not null
   */
   public Object evaluate(List arguments, State state) {
    return new Boolean(evaluateArgument(arguments.get(0), state) != null);
  }
  
  /** Returns a string representation of this predicate given
   * the arguments.
   *
   * @param arguments the given arguments to evaluate
   * @return a string representation of this object
   */
  public String toString(List arguments) {
    StringBuffer stringBuffer = new StringBuffer();
    stringBuffer.append("(not-null ");
    Object obj = arguments.get(0);
    if (obj instanceof String) {
      stringBuffer.append('"');
      stringBuffer.append(obj);
      stringBuffer.append('"');
    }
    else if (obj instanceof CycList)
      stringBuffer.append(((CycList) obj).cyclify());
    else
      stringBuffer.append(obj.toString());
    stringBuffer.append(")");
    return stringBuffer.toString();
  }
    
  //// Protected Area
  
  //// Private Area
  
  //// Internal Rep
  
}
