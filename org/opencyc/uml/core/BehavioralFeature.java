package org.opencyc.uml.core;

import java.util.*;

/**
 * BehavioralFeature from the UML Core package.
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

public class BehavioralFeature extends Feature {

    /**
     * indicates if this is a query
     */
    protected boolean isQuery;

    /**
     * the parameters for this behavioral feature
     */
    protected ArrayList parameter;

    /**
     * Constructs a new BehavioralFeature object.
     */
    public BehavioralFeature() {
    }

    /**
     * Indicates if this is a query.
     *
     * @return if this is a query
     */
    public boolean isQuery () {
        return isQuery;
    }

    /**
     * Sets whether this is a query.
     *
     * @param isQuery  whether this is a query
     */
    public void setIsQuery (boolean isQuery) {
        this.isQuery = isQuery;
    }

    /**
     * Gets the parameters for this behavioral feature.
     *
     * @return the parameters for this behavioral feature
     */
    public ArrayList getX () {
        return parameter;
    }

    /**
     * Sets the parameters for this behavioral feature.
     *
     * @param parameter the parameters for this behavioral feature
     */
    public void setX (ArrayList parameter) {
        this.parameter = parameter;
    }
}