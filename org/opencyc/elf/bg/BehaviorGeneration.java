package org.opencyc.elf.bg;

import java.util.ArrayList;

import org.opencyc.elf.NodeComponent;
import org.opencyc.elf.goal.Goal;


/**
 * Provides Behavior Generation for the Elementary Loop Functioning (ELF).<br>
 * 
 * @version $Id: BehaviorGeneration.java,v 1.3 2002/11/19 02:42:53 stephenreed
 *          Exp $
 * @author Stephen L. Reed  
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
public class BehaviorGeneration extends NodeComponent {
  /** the commanded goal for generated behavior to achieve */
  protected Goal commandedGoal;

  /**
   * the parent node's BehaviorGeneration object.  The topmost
   * BehaviorGeneration object has a value null here.
   */
  protected BehaviorGeneration parentBehaviorGeneration;

  /**
   * the child nodes' BehaviorGeneration objects.  The lowest level
   * BehavoriGeneration object has a value null here.
   */
  protected ArrayList childrenBehaviorGeneration;

  /**
   * Constructs a new BehaviorGeneration object.
   */
  public BehaviorGeneration() {
  }

  /**
   * Returns a string representation of this object.
   * 
   * @return a string representation of this object
   */
  public String toString() {
    return "BehaviorGeneration for " + node.getName();
  }

  /**
   * Gets the goal for generated behavior to achieve
   * 
   * @return the goal for generated behavior to achieve
   */
  public Goal getCommandedGoal() {
    return commandedGoal;
  }

  /**
   * Sets the goal for generated behavior to achieve
   * 
   * @param commandedGoal the goal for generated behavior to achieve
   */
  public void setCommandedGoal(Goal commandedGoal) {
    this.commandedGoal = commandedGoal;
  }

  /**
   * Gets the parent node's BehaviorGeneration object
   * 
   * @return the parent node's BehaviorGeneration object
   */
  public BehaviorGeneration getParentBehaviorGeneration() {
    return parentBehaviorGeneration;
  }

  /**
   * Sets the parent node's BehaviorGeneration object
   * 
   * @param parentBehaviorGeneration the parent node's BehaviorGeneration
   *        object
   */
  public void setParentBehaviorGeneration(BehaviorGeneration parentBehaviorGeneration) {
    this.parentBehaviorGeneration = parentBehaviorGeneration;
  }

  /**
   * Gets the child nodes' BehaviorGeneration objects
   * 
   * @return the child nodes' BehaviorGeneration objects
   */
  public ArrayList getChildrenBehaviorGeneration() {
    return childrenBehaviorGeneration;
  }

  /**
   * Sets the child nodes' BehaviorGeneration objects
   * 
   * @param childrenBehaviorGeneration the child nodes' BehaviorGeneration
   *        objects
   */
  public void setChildrenBehaviorGeneration(ArrayList childrenBehaviorGeneration) {
    this.childrenBehaviorGeneration = childrenBehaviorGeneration;
  }
}