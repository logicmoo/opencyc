package org.opencyc.xml;

import java.io.*;
import java.util.*;
import com.hp.hpl.jena.daml.*;
import com.hp.hpl.jena.daml.common.DAMLModelImpl;
import com.hp.hpl.mesa.rdf.jena.model.*;
import org.opencyc.util.*;

/**
 * Imports DAML xml content.<p>
 * <p>
 * The Another RDF Parser (ARP) is used to parse the input DAML document.
 * This class implements statement callbacks from ARP. Each triple in the
 * input file causes a call on one of the statement methods.
 * The same triple may occur more than once in a file, causing repeat calls
 * to the method.
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
public class ImportDaml {

    /**
     * The default verbosity of this application.  0 --> quiet ... 9 -> maximum
     * diagnostic input.
     */
    public static final int DEFAULT_VERBOSITY = 3;

    /**
     * Sets verbosity of this application.  0 --> quiet ... 9 -> maximum
     * diagnostic input.
     */
    protected int verbosity = DEFAULT_VERBOSITY;

    /**
     * Constructs a new ImportDaml object.
     */
    public ImportDaml() {
    }

    /**
     * Provides the main method for the ImportDaml application.
     *
     * @param args optionally provide the path to the DAML document.
     */
    public static void main(String[] argv) {
        Log.makeLog();
        String damlPath = "file:///opencyc/xml/military-elements-ont-1.daml";
        //if (args.length > 0)
        //   umlModelPath = args[0];
        ImportDaml importDaml = new ImportDaml();
        importDaml.initialize();
        importDaml.importDaml(damlPath);
    }

    /**
     * Initializes the ImportDaml object.
     */
    protected void initialize () {
    }

    /**
     * Imports the DAML document.
     */
    protected void importDaml (String damlPath) {
        DAMLModel damlModel = new DAMLModelImpl();
        System.out.println("Loading " + damlPath);
        try {
            damlModel.read(damlPath);
        }
        catch (RDFException e) {
            e.printStackTrace();
        }
        System.out.println(damlModel.toString());
        System.out.println("\nProperties\n");

        Iterator iter = damlModel.listDAMLProperties();
        while (iter.hasNext()) {
            DAMLProperty c = (DAMLProperty)iter.next();
            System.out.println(c.toString());
        }


        System.out.println("\nClasses and Properties\n");
        iter = damlModel.listDAMLClasses();
        while (iter.hasNext()) {
            DAMLClass damlClass = (DAMLClass)iter.next();
            System.out.println(damlClass.toString());
            Iterator iterProperties = damlClass.getDefinedProperties();
            while (iterProperties.hasNext()) {
                System.out.println("    "+iterProperties.next().toString());
            }
        }
    }

    /**
     * Sets verbosity of the constraint solver output.  0 --> quiet ... 9 -> maximum
     * diagnostic input.
     *
     * @param verbosity 0 --> quiet ... 9 -> maximum diagnostic input
     */
    public void setVerbosity(int verbosity) {
        this.verbosity = verbosity;
    }



}