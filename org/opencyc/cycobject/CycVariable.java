package org.opencyc.cycobject;

import org.apache.oro.util.*;

/**
 * Provides the behavior and attributes of an OpenCyc variable, typically used
 * in rule and query expressions.
 *
 * @version $0.1$
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
public class CycVariable implements Comparable {

    /**
     * Least Recently Used Cache of CycVariables, so that a reference to an existing <tt>CycVariable</tt>
     * is returned instead of constructing a duplicate.
     */
    protected static Cache cache = new CacheLRU(500);

    /**
     * The variable represented as a <tt>String</tt>.
     */
    public String name;

    /**
     * The ID of the <tt>CycVariable<tt> object which is an integer unique within an OpenCyc
     * KB but not necessarily unique globally.
     */
    public Integer id;

    /**
     * A variable name suffix used to make unique names.
     */
    protected static int suffix = 1;

    /**
     * Constructs a new <tt>CycVariable</tt> object using the variable name.
     *
     * @param name a <tt>String</tt> name.
     */
    public static CycVariable makeCycVariable(String name) {
        if (name.startsWith("?"))
            name = name.substring(1);
        CycVariable cycVariable = (CycVariable) cache.getElement(name);
        if (cycVariable == null) {
            cycVariable = new CycVariable(name);
            cache.addElement(name, cycVariable);
        }
        return cycVariable;
    }

    /**
     * Constructs a new <tt>CycVariable</tt> object by suffixing the given variable.
     *
     * @param modelCycVariable a <tt>CycVariable</tt> to suffix
     */
    public static CycVariable makeUniqueCycVariable(CycVariable modelCycVariable) {
        String name = modelCycVariable.name + "_" + suffix++;
        CycVariable cycVariable = (CycVariable) cache.getElement(name);
        if (cycVariable == null) {
            cycVariable = new CycVariable(name);
            cache.addElement(name, cycVariable);
        }
        return cycVariable;
    }

    /**
     * Constructs a new empty <tt>CycVariable</tt> object.
     */
    public CycVariable() {
    }

    /**
     * Constructs a new <tt>CycVariable</tt> object.  Non-public to enforce
     * use of the object cache.
     *
     * @param name the <tt>String</tt> name of the <tt>CycVariable</tt>.
     */
    protected CycVariable(String name) {
        if (name.startsWith("?"))
            this.name = name.substring(1);
        else
            this.name = name;
    }

    /**
     * Returns the string representation of the <tt>CycVariable</tt>
     *
     * @return the representation of the <tt>CycVariable</tt> as a <tt>String</tt>
     */
    public String toString() {
        return cyclify();
        //return name;
    }

    /**
     * Returns the OpenCyc representation of the <tt>CycVariable</tt>
     *
     * @return the OpenCyc representation of the <tt>CycVariable</tt> as a
     * <tt>String</tt> prefixed by "?"
     */
    public String cyclify() {
        return "?" + name;
    }

    /**
     * Returns this object in a form suitable for use as an <tt>String</tt> api expression value.
     *
     * @return this object in a form suitable for use as an <tt>String</tt> api expression value
     */
    public String stringApiValue() {
        return cyclify();
    }

    /**
     * Returns this object in a form suitable for use as an <tt>CycList</tt> api expression value.
     *
     * @return this object in a form suitable for use as an <tt>CycList</tt> api expression value
     */
    public Object cycListApiValue() {
        return this;
    }

    /**
     * Returns <tt>true</tt> some object equals this <tt>CycVariable</tt>
     *
     * @param object the <tt>Object</tt> for equality comparison
     * @return equals <tt>boolean</tt> value indicating equality or non-equality.
     */
    public boolean equals(Object object) {
        if (! (object instanceof CycVariable))
            return false;
        return ((CycVariable) object).name.equals(name);
    }

    /**
     * Compares this object with the specified object for order.
     * Returns a negative integer, zero, or a positive integer as this
     * object is less than, equal to, or greater than the specified object.
     *
     * @param object the reference object with which to compare.
     * @return a negative integer, zero, or a positive integer as this
     * object is less than, equal to, or greater than the specified object
     */
     public int compareTo (Object object) {
        if (! (object instanceof CycVariable))
            throw new ClassCastException("Must be a CycVariable object");
        return this.name.compareTo(((CycVariable) object).name);
     }

    /**
     * Resets the <tt>CycVariable</tt> cache.
     */
    public static void resetCache() {
        cache = new CacheLRU(500);
    }

    /**
     * Adds the <tt>CycVariable<tt> to the cache.
     */
    public static void addCache(CycVariable cycVariable) {
        if (cycVariable.name == null)
            throw new RuntimeException("Invalid variable for caching " + cycVariable);
        cache.addElement(cycVariable.name, cycVariable);
    }

    /**
     * Retrieves the <tt>CycVariable</tt> with <tt>name</tt>,
     * returning null if not found in the cache.
     *
     * @return a <tt>CycVariable</tt> if found in the cache, otherwise
     * <tt>null</tt>
     */
    public static CycVariable getCache(String name) {
        return (CycVariable) cache.getElement(name);
    }

    /**
     * Removes the <tt>CycVariable</tt> from the cache if it is contained within.
     */
    public static void removeCache(CycVariable cycVariable) {
        Object element = cache.getElement(cycVariable.name);
        if (element != null)
            cache.addElement(cycVariable.name, null);
    }

    /**
     * Returns the size of the <tt>CycVariable</tt> object cache.
     *
     * @return an <tt>int</tt> indicating the number of <tt>CycVariable</tt> objects in the cache
     */
    public static int getCacheSize() {
        return cache.size();
    }
}