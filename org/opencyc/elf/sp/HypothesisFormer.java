package org.opencyc.elf.sp;

import org.opencyc.elf.NodeComponent;


/**
 * Provides the HypothesisFormer for ELF SensoryPerception.<br>
 * 
 * @version $Id: HypothesisFormer.java,v 1.1 2002/11/19 02:42:53 stephenreed
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
public class HypothesisFormer extends NodeComponent {
  /** the sensory perception instance which owns this hypothesis former */
  protected SensoryPerception sensoryPerception;

  /**
   * Constructs a new HypothesisFormer object.
   */
  public HypothesisFormer() {
  }

  /**
   * Gets the sensory perception instance which owns this hypothesis former
   * 
   * @return the sensory perception instance which owns this hypothesis former
   */
  public SensoryPerception getSensoryPerception() {
    return sensoryPerception;
  }

  /**
   * Sets the sensory perception instance which owns this hypothesis former
   * 
   * @param sensoryPerception the sensory perception instance which owns this
   *        hypothesis former
   */
  public void setSensoryPerception(SensoryPerception sensoryPerception) {
    this.sensoryPerception = sensoryPerception;
  }
}