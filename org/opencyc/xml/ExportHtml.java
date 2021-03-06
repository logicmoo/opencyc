package  org.opencyc.xml;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.StringTokenizer;
import org.apache.html.dom.HTMLAnchorElementImpl;
import org.apache.html.dom.HTMLDocumentImpl;
import org.apache.html.dom.HTMLFontElementImpl;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XHTMLSerializer;
import org.opencyc.api.CycAccess;
import org.opencyc.api.CycApiException;
import org.opencyc.api.CycConnection;
import org.opencyc.api.CycObjectFactory;
import org.opencyc.cycobject.CycConstant;
import org.opencyc.cycobject.CycFort;
import org.opencyc.cycobject.CycList;
import org.opencyc.cycobject.CycNart;
import org.opencyc.cycobject.CycObject;
import org.opencyc.cycobject.CycVariable;
import org.opencyc.cycobject.Guid;
import org.opencyc.util.Log;
import org.opencyc.util.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.html.HTMLAnchorElement;
import org.w3c.dom.html.HTMLDocument;
import org.w3c.dom.html.HTMLFontElement;

/**
 * HTML ontology export for OpenCyc/ResearchCyc.
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
public class ExportHtml extends OntologyExport {
  /**
   * Indicates whether hierarchy pages are produced.
   */
  public boolean produceHierarchyPages = false;
  /**
   * The HTML exported vocabulary path and file name.
   */
  public String exportedVocabularyOutputPath = "exported-vocabulary.html";
  /**
   * The HTML exported hierarchy path and file name.
   */
  public String exportedHierarchyOutputPath = "exported-hierarchy.html";
  /**
   * The NART note file name.
   */
  public String nartNoteOutputPath = "ontology-release-NARTs.html";
  /**
   * The html document.
   */
  protected HTMLDocument htmlDocument;
  /**
   * The HTML body element.
   */
  protected Element htmlBodyElement;
  /**
   * Indicates the presence of a comment for the current term.
   */
  protected boolean hasComment = false;
  /**
   * Indicates which terms have been previously expanded in the hierarchy page.
   */
  protected HashSet previouslyExpandedTerms;
  
  /**
   * Additional term guids not to appear in the list of direct instances even if
   * otherwise qualified to appear.
   */
  protected ArrayList filterFromDirectInstanceGuids = new ArrayList();
  
  /**
   * Additional terms not to appear in the list of direct instances even if
   * otherwise qualified to appear.
   */
  protected ArrayList filterFromDirectInstances = new ArrayList();
  
  /**
   * Export the GUID string for each term?
   */
  public boolean print_guid = false;
  
  /**
   * List of HTML vocabulary category page information items.
   */
  public ArrayList categories = new ArrayList();
  
  /**
   * Constructs a new ExportHtml object given the CycAccess object.
   *
   * @param cycAccess The CycAccess object which manages the api connection.
   */
  public ExportHtml(CycAccess cycAccess) {
    super(cycAccess);
  }
  
  /**
   * Exports the desired KB content into HTML.
   */
  public void export(int exportCommand) throws UnknownHostException, IOException, CycApiException {
    this.exportCommand = exportCommand;
    setup();
    if (verbosity > 2)
      Log.current.println("Getting terms from Cyc");
    if ((exportCommand == ExportHtml.EXPORT_KB_SUBSET) || (exportCommand == ExportHtml.EXPORT_KB_SUBSET_PLUS_UPWARD_CLOSURE)) {
      selectedCycForts = cycAccess.getAllQuotedInstances(cycKbSubsetCollection, cycAccess.inferencePSC);
    }
    else {
      // EXPORT_KB_SUBSET_BELOW_TERM
      selectedCycForts = cycAccess.getAllSpecs(rootTerm);
      selectedCycForts.add(rootTerm);
    }
    selectedCycForts.add(cycAccess.thing);
    selectedCycForts.add(cycAccess.collection);
    selectedCycForts.add(cycAccess.getKnownConstantByName("Individual"));
    selectedCycForts.add(cycAccess.getKnownConstantByName("Predicate"));
    if (verbosity > 2)
      Log.current.println("Selected " + selectedCycForts.size() + " CycFort terms");
    if (includeUpwardClosure) {
      upwardClosureCycForts = gatherUpwardClosure(selectedCycForts);
      if (verbosity > 2)
        Log.current.println("Upward closure added " + upwardClosureCycForts.size() + " CycFort terms");
      selectedCycForts.addAll(upwardClosureCycForts);
      if (verbosity > 2)
        Log.current.println("All selected " + selectedCycForts.size() + " CycFort terms");
    }
    if (verbosity > 2) {
      if (includeNonAtomicTerms)
        Log.current.println("Dropping quoted collection terms ");
      else
        Log.current.println("Dropping quoted collection terms and non-atomic terms");
    }
    CycList tempList = new CycList();
    for (int i = 0; i < selectedCycForts.size(); i++) {
      CycFort cycFort = (CycFort) selectedCycForts.get(i);
      if (cycAccess.isQuotedCollection(cycFort) || 
         (! includeNonAtomicTerms && ! (cycFort instanceof CycConstant))) {
        if (verbosity > 2)
          Log.current.println("  ommitting " + cycFort.cyclify());
      }
      else
        tempList.add(cycFort);
    }
    selectedCycForts = tempList;
    if (verbosity > 2)
      Log.current.println("Sorting " + selectedCycForts.size() + " CycFort terms");
    sortCycObjects(selectedCycForts);
    createVocabularyPage();
    if (categories.size() > 0)
      createCategorizedVocabularies();
    if (rootTerm != null &&
    produceHierarchyPages)
      createHierarchyPage(rootTerm);
    else if (includeUpwardClosure &&
    produceHierarchyPages)
      createHierarchyPage(CycAccess.thing);
    else if (verbosity > 0)
      Log.current.println("Ommiting ontology hierarchy export page");
    for (int i = 0; i < selectedCycForts.size(); i++) {
      final CycObject cycObject = (CycObject) selectedCycForts.get(i);
      if (verbosity > 2)
        Log.current.println(cycObject.cyclify());
    }
    if (verbosity > 0)
      Log.current.println("HTML export completed");
    cycAccess.close();
  }
  
  /**
   * Sets up the HTML export process.
   */
  protected void setup() throws UnknownHostException, IOException, CycApiException {
    assert cycAccess != null : "cycAccess cannot be null";
    
    if (exportCommand == OntologyExport.EXPORT_KB_SUBSET) {
      assert cycKbSubsetCollectionGuid != null : "cycKbSubsetCollectionGuid cannot be null";
      cycKbSubsetCollection = cycAccess.getKnownConstantByGuid(cycKbSubsetCollectionGuid);
      includeUpwardClosure = false;
      if (verbosity > 1)
        Log.current.println("Exporting KB subset " + cycKbSubsetCollection.cyclify());
    }
    else if (exportCommand == OntologyExport.EXPORT_KB_SUBSET_PLUS_UPWARD_CLOSURE) {
      cycKbSubsetCollection = cycAccess.getKnownConstantByGuid(cycKbSubsetCollectionGuid);
      cycKbSubsetFilter = cycAccess.getKnownConstantByGuid(cycKbSubsetFilterGuid);
      includeUpwardClosure = true;
      if (verbosity > 1)
        Log.current.println("Exporting KB subset " + cycKbSubsetCollection.cyclify() + "\n  plus upward closure to #$Thing filtered by "
        + cycKbSubsetFilter.cyclify());
    }
    else if (exportCommand == OntologyExport.EXPORT_KB_SUBSET_BELOW_TERM) {
      rootTerm = cycAccess.getKnownConstantByGuid(rootTermGuid);
      cycKbSubsetFilter = cycAccess.getKnownConstantByGuid(cycKbSubsetFilterGuid);
      cycKbSubsetCollection = cycKbSubsetFilter;
      includeUpwardClosure = false;
      if (verbosity > 1)
        Log.current.println("Exporting KB collections below root term " + rootTerm.cyclify() + "\n  filtered by " + cycKbSubsetFilter.cyclify());
    }
    else {
      System.err.println("Invalid export comand " + exportCommand);
      System.exit(1);
    }
    for (int i = 0; i < filterFromDirectInstanceGuids.size(); i++) {
      Guid guid = (Guid) filterFromDirectInstanceGuids.get(i);
      filterFromDirectInstances.add(cycAccess.getKnownConstantByGuid(guid));
    }
    for (int i = 0; i < upwardClosureKbSubsetCollectionGuids.size(); i++) {
      Guid guid = (Guid) upwardClosureKbSubsetCollectionGuids.get(i);
      upwardClosureKbSubsetCollections.add(cycAccess.getKnownConstantByGuid(guid));
    }
  }
  
  /**
   * Creates vocabulary HTML page.
   */
  protected void createVocabularyPage() throws UnknownHostException, IOException, CycApiException {
    if (verbosity > 2)
      Log.current.println("Building HTML model for vocabulary page");
    htmlDocument = new HTMLDocumentImpl();
    String title = "Cyc ontology vocabulary for " + cycKbSubsetCollection.cyclify();
    htmlDocument.setTitle(title);
    Node htmlNode = htmlDocument.getChildNodes().item(0);
    htmlBodyElement = htmlDocument.createElement("body");
    htmlNode.appendChild(htmlBodyElement);
    Element headingElement = htmlDocument.createElement("h1");
    htmlBodyElement.appendChild(headingElement);
    Node headingTextNode = htmlDocument.createTextNode(title);
    headingElement.appendChild(headingTextNode);
    for (int i = 0; i < selectedCycForts.size(); i++) {
      CycFort cycFort = (CycFort)selectedCycForts.get(i);
      if (verbosity > 2)
        Log.current.print(cycFort + "  ");
      if (cycAccess.isCollection(cycFort)) {
        if (verbosity > 2)
          Log.current.println("Collection");
      }
      else if (cycAccess.isPredicate(cycFort)) {
        if (verbosity > 2)
          Log.current.println("Predicate");
      }
      else if (cycAccess.isIndividual(cycFort)) {
        if (verbosity > 2)
          Log.current.println("Individual");
      }
      else {
        if (verbosity > 2)
          Log.current.println("other");
        continue;
      }
      if (cycFort instanceof CycConstant)
        createCycConstantNode((CycConstant) cycFort);
      else
        createCycNartNode((CycNart) cycFort);
    }
    serialize(htmlDocument, exportedVocabularyOutputPath);
  }
  
  /**
   * Creates a HTML node for a single Cyc Nart.
   * @param cycNart The CycNart from which the HTML node is created.
   */
  protected void createCycNartNode(CycNart cycNart)
  throws UnknownHostException, IOException, CycApiException {
    horizontalRule();
    HTMLFontElement htmlFontElement =
    new HTMLFontElementImpl((HTMLDocumentImpl)htmlDocument, "font");
    htmlFontElement.setSize("+1");
    htmlBodyElement.appendChild(htmlFontElement);
    HTMLAnchorElement htmlAnchorElement =
    new HTMLAnchorElementImpl((HTMLDocumentImpl)htmlDocument, "a");
    htmlAnchorElement.setName(cycNart.cyclify());
    htmlFontElement.appendChild(htmlAnchorElement);
    String generatedPhrase = cycAccess.getSingularGeneratedPhrase(cycNart);
    if (generatedPhrase.endsWith("(unclassified term)"))
      generatedPhrase = generatedPhrase.substring(0, generatedPhrase.length() - 20);
    Node collectionTextNode = htmlDocument.createTextNode(cycNart.cyclify());
    htmlAnchorElement.appendChild(collectionTextNode);
    Element italicsGeneratedPhraseElement = italics(htmlAnchorElement);
    Node generatedPhraseNode =
    htmlDocument.createTextNode("   " + generatedPhrase);
    italicsGeneratedPhraseElement.appendChild(generatedPhraseNode);
    Element blockquoteElement = htmlDocument.createElement("blockquote");
    htmlBodyElement.appendChild(blockquoteElement);
    HTMLAnchorElement natNoteAnchorElement =
    new HTMLAnchorElementImpl((HTMLDocumentImpl)htmlDocument, "a");
    natNoteAnchorElement.setHref("./" + nartNoteOutputPath);
    blockquoteElement.appendChild(natNoteAnchorElement);
    Node natNoteTextNode = htmlDocument.createTextNode("Note On Non-Atomic Terms");
    natNoteAnchorElement.appendChild(natNoteTextNode);
    createIsaNodes(cycNart, blockquoteElement);
    createGenlNodes(cycNart, blockquoteElement);
  }
  
  /**
   * Creates a HTML node for a single Cyc Constant.
   * @param cycConstant The CycConstant from which the HTML node is created.
   */
  protected void createCycConstantNode(CycConstant cycConstant)
  throws UnknownHostException, IOException, CycApiException {
    horizontalRule();
    HTMLFontElement htmlFontElement = new HTMLFontElementImpl((HTMLDocumentImpl)htmlDocument, "font");
    htmlFontElement.setSize("+1");
    htmlBodyElement.appendChild(htmlFontElement);
    HTMLAnchorElement htmlAnchorElement = new HTMLAnchorElementImpl((HTMLDocumentImpl)htmlDocument, "a");
    htmlAnchorElement.setName(cycConstant.cyclify());
    htmlFontElement.appendChild(htmlAnchorElement);
    Node collectionTextNode = htmlDocument.createTextNode(cycConstant.cyclify());
    htmlAnchorElement.appendChild(collectionTextNode);
    boolean hasRewrite = processRewriteOf(cycConstant, htmlFontElement);
    if (! hasRewrite) {
      // If no rewriteOf text, then output the generated phrase.
      String generatedPhrase;
      if (cycAccess.isCollection(cycConstant))
        generatedPhrase = cycAccess.getPluralGeneratedPhrase(cycConstant);
      else
        generatedPhrase = cycAccess.getSingularGeneratedPhrase(cycConstant);
      if (generatedPhrase.endsWith("(unclassified term)"))
        generatedPhrase = generatedPhrase.substring(0, generatedPhrase.length() - 20);
      Element italicsGeneratedPhraseElement = italics(htmlAnchorElement);
      Node generatedPhraseNode =
      htmlDocument.createTextNode("   " + generatedPhrase);
      italicsGeneratedPhraseElement.appendChild(generatedPhraseNode);
    }
    Element blockquoteElement = htmlDocument.createElement("blockquote");
    htmlBodyElement.appendChild(blockquoteElement);
    createCommentNodes(cycConstant, blockquoteElement);
    if (print_guid)
      createGuidNode(cycConstant, blockquoteElement);
    createIsaNodes(cycConstant, blockquoteElement);
    if (cycAccess.isCollection(cycConstant))
      createCollectionNode(cycConstant, blockquoteElement);
    else if (cycAccess.isPredicate(cycConstant))
      createPredicateNode(cycConstant, blockquoteElement);
    else if (cycAccess.isFunction(cycConstant))
      createFunctionNode(cycConstant, blockquoteElement);
    else if (cycAccess.isIndividual(cycConstant))
      createIndividualNode(cycConstant, blockquoteElement);
    else {
      if (verbosity > 0)
        Log.current.println("Unhandled constant: " + cycConstant.toString());
    }
  }
  
  /**
   * Processes the case where the given Cyc constant has a #$rewriteOf relationship
   * to a Cyc FORT (First Order Reified Term).  For example when the given term is
   * #$AttemptedKillingByCarAccident, then the KB contains the assertion
   * (#$rewriteOf #$AttemptedKillingByCarAccident ((KillingThroughEventTypeFn #$CarAccident))
   * and the phrase " is the atomic form of (KillingThroughEventTypeFn #$CarAccident)" is
   * output to the HTML document.
   *
   * @param cycConstant The given CycConstant for processing if a #$rewriteOf.
   * @param parentElement The parent HTML element for inserting rewriteOf text.
   * @return True iff there is in fact a rewrite term.
   */
  protected boolean processRewriteOf(CycConstant cycConstant,
  Element parentElement)
  throws UnknownHostException, IOException, CycApiException {
    CycList query = new CycList();
    query.add(cycAccess.and);
    CycList query1 = new CycList();
    query.add(query1);
    
    query1.add(cycAccess.getKnownConstantByGuid(rewriteOfGuid));
    query1.add(cycConstant);
    CycVariable fortVariable = CycObjectFactory.makeCycVariable("?FORT");
    query1.add(fortVariable);
    CycConstant inferencePSC = cycAccess.getKnownConstantByGuid(inferencePSCGuid);
    // #$rewriteOf is reflexsive so constrain arg2 to be different from arg1.
    CycList query2 = new CycList();
    query.add(query2);
    query2.add(cycAccess.not);
    CycList query3 = new CycList();
    query2.add(query3);
    query3.add(cycAccess.getKnownConstantByGuid(equalSymbolsGuid));
    query3.add(cycConstant);
    query3.add(fortVariable);
    CycList cycForts = cycAccess.queryVariable(fortVariable, query, inferencePSC, (HashMap) null);
    if (cycForts.size() == 0)
      return false;
    if (! (cycForts.get(0) instanceof CycFort)) {
      Log.current.errorPrintln("\nError, rewriteOf " + cycConstant.cyclify() +
      "\n " + cycForts.get(0) + "\nis not a CycFort\n");
      return false;
    }
    if (! (cycForts.get(0) instanceof CycNart))
      return false;
    CycNart cycNart = (CycNart) cycForts.get(0);
    
    Node rewriteOfTextNode = htmlDocument.createTextNode("  is the atomic form of ");
    parentElement.appendChild(rewriteOfTextNode);
    
    processRewrittenNart(cycNart, parentElement);
    return true;
  }
  
  /**
   * Creates nodes for rewritten NARTs.  Recursive for the NART components, putting
   * hyperlinks where possible.
   *
   * @param object The given ojbect for processing in a #$rewriteOf.
   * @param parentElement The parent HTML element for inserting rewriteOf text.
   */
  protected void processRewrittenNart(Object object, Element parentElement)
  throws UnknownHostException, IOException, CycApiException {
    if (object instanceof CycList) {
      CycList cycList = (CycList) object;
      for (int i = 0; i < cycList.size(); i++) {
        Object item = cycList.get(i);
        parentElement.appendChild(htmlDocument.createTextNode(" "));
        // recurse for each list item
        processRewrittenNart(item, parentElement);
      }
      return;
    }
    if (object instanceof CycConstant) {
      CycConstant cycConstant = (CycConstant) object;
      if (selectedCycForts.contains(object)) {
        HTMLAnchorElement cycConstantAnchorElement =
        new HTMLAnchorElementImpl((HTMLDocumentImpl)htmlDocument, "a");
        cycConstantAnchorElement.setHref("./" + exportedVocabularyOutputPath + "#" + cycConstant.cyclify());
        parentElement.appendChild(cycConstantAnchorElement);
        cycConstantAnchorElement.appendChild(htmlDocument.createTextNode(cycConstant.cyclify()));
      }
      else
        parentElement.appendChild(htmlDocument.createTextNode(cycConstant.cyclify()));
      return;
    }
    if (object instanceof CycNart) {
      CycNart cycNart = (CycNart) object;
      if (selectedCycForts.contains(cycNart)) {
        HTMLAnchorElement cycFortAnchorElement =
        new HTMLAnchorElementImpl((HTMLDocumentImpl)htmlDocument, "a");
        cycFortAnchorElement.setHref("./" + exportedVocabularyOutputPath + "#" + cycNart.cyclify());
        parentElement.appendChild(cycFortAnchorElement);
        cycFortAnchorElement.appendChild(htmlDocument.createTextNode("( "));
      }
      else
        parentElement.appendChild(htmlDocument.createTextNode("("));
      CycFort functor = cycNart.getFunctor();
      // recurse for the functor
      processRewrittenNart(functor, parentElement);
      for (int i = 0; i < cycNart.getArguments().size(); i++) {
        Object argument = cycNart.getArguments().get(i);
        parentElement.appendChild(htmlDocument.createTextNode(" "));
        // recurse for each arg
        processRewrittenNart(argument, parentElement);
      }
      Node rightParenTextNode = htmlDocument.createTextNode(")");
      parentElement.appendChild(rightParenTextNode);
      return;
    }
    parentElement.appendChild(htmlDocument.createTextNode(object.toString()));
  }
  
  
  
  /**
   * Creates a paragraph break in the HTML document.
   */
  protected void paragraphBreak() {
    Element paragraphElement = htmlDocument.createElement("p");
    htmlBodyElement.appendChild(paragraphElement);
  }
  
  /**
   * Creates an italics element in the HTML document.
   *
   * @param parentElement The parent HTML DOM element.
   * @return The italics element.
   */
  protected Element italics(Element parentElement) {
    Element italicsElement = htmlDocument.createElement("i");
    parentElement.appendChild(italicsElement);
    return  italicsElement;
  }
  
  /**
   * Creates a line break in the HTML document.
   *
   * @param parentElement The parent HTML DOM element.
   */
  protected void lineBreak(Element parentElement) {
    Element breakElement = htmlDocument.createElement("br");
    parentElement.appendChild(breakElement);
  }
  
  /**
   * Creates a horizontal rule in the HTML document.
   */
  protected void horizontalRule() {
    Element horizontalRuleElement = htmlDocument.createElement("hr");
    htmlBodyElement.appendChild(horizontalRuleElement);
  }
  
  /**
   * Creates HTML nodes for comment text containing CycConstants which are to be
   * represented as hyperlinks.
   *
   * @param cycConstant The CycConstant for which isa links are to be created.
   * @param parentElement The parent HTML DOM element.
   */
  protected void createCommentNodes(CycConstant cycConstant, Element parentElement)
  throws IOException, CycApiException {
    String comment = cycAccess.getComment(cycConstant);
    if (comment.equals("")) {
      hasComment = false;
      return;
    }
    comment = comment.replaceAll("<code>", "");
    comment = comment.replaceAll("</code>", "");
    comment = comment.replaceAll("<p>", "");
    comment = comment.replaceAll("</p>", "");
    comment = comment.replaceAll("<i>", "");
    comment = comment.replaceAll("</i>", "");
    hasComment = true;
    StringTokenizer st = new StringTokenizer(comment);
    StringBuffer stringBuffer = new StringBuffer();
    CycConstant commentConstant;
    Node commentTextNode;
    Node linkTextNode;
    HTMLAnchorElement htmlAnchorElement;
    while (st.hasMoreTokens()) {
      String word = st.nextToken();
      boolean wordHasLeadingLeftParen = false;
      if (word.startsWith("(#$")) {
        wordHasLeadingLeftParen = true;
        word = word.substring(1);
      }
      if (word.startsWith("#$")) {
        StringBuffer nonNameChars = new StringBuffer();
        while (true) {
          // Move trailing non-name characters.
          char ch = word.charAt(word.length() - 1);
          if (Character.isLetterOrDigit(ch))
            break;
          word = word.substring(0, word.length() - 1);
          nonNameChars.insert(0, ch);
          if (word.length() == 0)
            break;
        }
        commentConstant = cycAccess.getConstantByName(word);
        if (commentConstant != null && selectedCycForts.contains(commentConstant)) {
          stringBuffer.append(" ");
          commentTextNode = htmlDocument.createTextNode(stringBuffer.toString());
          parentElement.appendChild(commentTextNode);
          stringBuffer = new StringBuffer();
          htmlAnchorElement = new HTMLAnchorElementImpl((HTMLDocumentImpl)htmlDocument, "a");
          htmlAnchorElement.setHref("./" + exportedVocabularyOutputPath + "#" + commentConstant.cyclify());
          parentElement.appendChild(htmlAnchorElement);
          if (wordHasLeadingLeftParen)
            stringBuffer.append('(');
          stringBuffer.append(word);
          stringBuffer.append(nonNameChars.toString());
          linkTextNode = htmlDocument.createTextNode(stringBuffer.toString());
          htmlAnchorElement.appendChild(linkTextNode);
          stringBuffer = new StringBuffer();
        }
        else if (commentConstant == null && word.endsWith("s")) {
          commentConstant = cycAccess.getConstantByName(word.substring(0, word.length() - 1));
          if (commentConstant != null && selectedCycForts.contains(commentConstant)) {
            stringBuffer.append(" ");
            commentTextNode = htmlDocument.createTextNode(stringBuffer.toString());
            parentElement.appendChild(commentTextNode);
            stringBuffer = new StringBuffer();
            htmlAnchorElement = new HTMLAnchorElementImpl((HTMLDocumentImpl)htmlDocument, "a");
            htmlAnchorElement.setHref("./" + exportedVocabularyOutputPath + "#" + commentConstant.cyclify());
            parentElement.appendChild(htmlAnchorElement);
            if (wordHasLeadingLeftParen)
              stringBuffer.append('(');
            stringBuffer.append(word);
            stringBuffer.append(nonNameChars.toString());
            linkTextNode = htmlDocument.createTextNode(stringBuffer.toString());
            htmlAnchorElement.appendChild(linkTextNode);
            stringBuffer = new StringBuffer();
          }
          else {
            stringBuffer.append(" ");
            if (wordHasLeadingLeftParen)
              stringBuffer.append('(');
            stringBuffer.append(word);
            stringBuffer.append(nonNameChars.toString());
          }
        }
        else {
          stringBuffer.append(" ");
          if (wordHasLeadingLeftParen)
            stringBuffer.append('(');
          stringBuffer.append(word);
          stringBuffer.append(nonNameChars.toString());
        }
      }
      else {
        stringBuffer.append(" ");
        stringBuffer.append(word);
      }
    }
    if (stringBuffer.length() > 0) {
      commentTextNode = htmlDocument.createTextNode(stringBuffer.toString());
      parentElement.appendChild(commentTextNode);
    }
  }
  
  /**
   * Creates HTML node for guid.
   *
   * @param cycConstant The CycConstant for which isa links are to be created.
   * @param parentElement The parent HTML DOM element.
   */
  protected void createGuidNode(CycConstant cycConstant, Element parentElement) throws IOException, CycApiException {
    Guid guid = cycConstant.getGuid();
    if (hasComment)
      lineBreak(parentElement);
    Element bElement = htmlDocument.createElement("b");
    parentElement.appendChild(bElement);
    Node guidLabelTextNode = htmlDocument.createTextNode("guid: ");
    bElement.appendChild(guidLabelTextNode);
    Node guidTextNode = htmlDocument.createTextNode(guid.toString());
    parentElement.appendChild(guidTextNode);
  }
  
  /**
   * Creates HTML nodes for isa links.
   *
   * @param cycConstant The CycConstant for which isa links are to be created.
   * @param parentElement The parent HTML DOM element.
   */
  protected void createIsaNodes(CycFort cycFort, Element parentElement) throws IOException, CycApiException {
    //if (cycFort.toString().equals("BiochemicallyHarmfulSubstance"))
    //    verbosity = 9;
    CycList isas = cycAccess.getIsas(cycFort);
    if (verbosity > 3)
      Log.current.println("  starting isas: " + isas.cyclify());
    lineBreak(parentElement);
    Element bElement = htmlDocument.createElement("b");
    parentElement.appendChild(bElement);
    Node isasLabelTextNode = htmlDocument.createTextNode("direct instance of: ");
    bElement.appendChild(isasLabelTextNode);
    CycList createdIsas = new CycList();
    for (int i = 0; i < isas.size(); i++) {
      CycFort isa = (CycFort)isas.get(i);
      if (verbosity > 7) {
        Log.current.println("  considering " + isa.cyclify());
        Log.current.println("    selectedCycForts.contains(" + isa.cyclify() + ") " + selectedCycForts.contains(isa));
        Log.current.println("    cycAccess.isQuotedCollection(" + isa.cyclify() + ") " + cycAccess.isQuotedCollection(isa));
      }
      if (! selectedCycForts.contains(isa)) {
        isa = findSelectedGenls(isa);
        if (isa == null)
          continue;
      }
      if (createdIsas.contains(isa))
        continue;
      else if (cycAccess.isQuotedCollection(isa)) {
        if (verbosity > 2)
          Log.current.println("  omitting quoted direct-instance-of collection " + isa.cyclify());
      }
      else {
        if (verbosity > 7)
          Log.current.println("  adding direct instance of " + isa.cyclify());
        createdIsas.add(isa);
      }
    }
    createdIsas = specificCollections(createdIsas);
    // filter out any specified terms.
    createdIsas.removeAll(filterFromDirectInstances);
    for (int i = 0; i < createdIsas.size(); i++) {
      CycFort isa = (CycFort)createdIsas.get(i);
      HTMLAnchorElement isaAnchorElement = new HTMLAnchorElementImpl((HTMLDocumentImpl)htmlDocument, "a");
      isaAnchorElement.setHref("./" + exportedVocabularyOutputPath + "#" + isa.cyclify());
      parentElement.appendChild(isaAnchorElement);
      Node isaTextNode = htmlDocument.createTextNode(isa.cyclify());
      isaAnchorElement.appendChild(isaTextNode);
      Node spacesTextNode = htmlDocument.createTextNode("  ");
      parentElement.appendChild(spacesTextNode);
    }
  }
  
  /**
   * Creates HTML nodes for genl links.
   *
   * @param cycConstant The CycConstant for which genl links are to be created.
   * @param parentElement The parent HTML DOM element.
   */
  protected void createGenlNodes(CycFort cycFort, Element parentElement) throws IOException, CycApiException {
    CycList genls = cycAccess.getGenls(cycFort);
        /*
        if (cycFort.equals(cycAccess.getKnownConstantByName("CarAccident"))) {
            verbosity = 9;
        }
         */
    if (verbosity > 3)
      Log.current.println("  starting genls: " + genls.cyclify());
    genls = specificCollections(findAllowedTermsOrGenls(genls));
    lineBreak(parentElement);
    Element bElement = htmlDocument.createElement("b");
    parentElement.appendChild(bElement);
    Node genlsLabelTextNode = htmlDocument.createTextNode("direct specialization of: ");
    bElement.appendChild(genlsLabelTextNode);
    for (int i = 0; i < genls.size(); i++) {
      CycFort genl = (CycFort)genls.get(i);
      if (cycAccess.isQuotedCollection(genl)) {
        if (verbosity > 2)
          Log.current.println("  omitting quoted genl collection " + genl.cyclify());
      }
      else if (selectedCycForts.contains(genl)) {
        HTMLAnchorElement genlAnchorElement = new HTMLAnchorElementImpl((HTMLDocumentImpl)htmlDocument, "a");
        genlAnchorElement.setHref("./" + exportedVocabularyOutputPath + "#" + genl.cyclify());
        parentElement.appendChild(genlAnchorElement);
        Node genlTextNode = htmlDocument.createTextNode(genl.cyclify());
        genlAnchorElement.appendChild(genlTextNode);
        Node spacesTextNode = htmlDocument.createTextNode("  ");
        parentElement.appendChild(spacesTextNode);
      }
    }
    if ((genls.size() > 0) && produceHierarchyPages) {
      Node spacesTextNode = htmlDocument.createTextNode("  ");
      parentElement.appendChild(spacesTextNode);
      HTMLAnchorElement hierarchyAnchorElement = new HTMLAnchorElementImpl((HTMLDocumentImpl)htmlDocument, "a");
      hierarchyAnchorElement.setHref("./" + exportedHierarchyOutputPath + "./" + exportedVocabularyOutputPath + "#" + cycFort.cyclify());
      parentElement.appendChild(hierarchyAnchorElement);
      Node hierarchyTextNode = htmlDocument.createTextNode("hierarchy");
      hierarchyAnchorElement.appendChild(hierarchyTextNode);
    }
  }
  
  /**
   * Creates an HTML node for a single Cyc predicate.
   *
   * @param cycConstant The Cyc predicate from which the HTML node is created.
   * @param parentElement The parent HTML DOM element.
   */
  protected void createPredicateNode(CycConstant cycConstant, Element parentElement) throws UnknownHostException, IOException, CycApiException {
    createGenlPredsNodes(cycConstant, parentElement);
    int arity = cycAccess.getArity(cycConstant);
    if (arity > 0)
      createArg1IsaNodes(cycConstant, parentElement);
    if (arity > 1)
      createArg2IsaNodes(cycConstant, parentElement);
    if (arity > 2)
      createArg3IsaNodes(cycConstant, parentElement);
    if (arity > 3)
      createArg4IsaNodes(cycConstant, parentElement);
  }
  
  /**
   * Creates an HTML node for a single Cyc function.
   *
   * @param cycConstant The Cyc function from which the HTML node is created.
   * @param parentElement The parent HTML DOM element.
   */
  protected void createFunctionNode(CycConstant cycConstant, Element parentElement) throws UnknownHostException, IOException, CycApiException {
    int arity = cycAccess.getArity(cycConstant);
    if (arity > 0)
      createArg1IsaNodes(cycConstant, parentElement);
    if (arity > 1)
      createArg2IsaNodes(cycConstant, parentElement);
    if (arity > 2)
      createArg3IsaNodes(cycConstant, parentElement);
    if (arity > 3)
      createArg4IsaNodes(cycConstant, parentElement);
    createResultIsaNodes(cycConstant, parentElement);
  }
  
  /**
   * Creates HTML nodes for genlPreds links.
   *
   * @param cycConstant The CycConstant for which genlPreds links are to be created.
   * @param parentElement The parent HTML DOM element.
   */
  protected void createGenlPredsNodes(CycConstant cycConstant, Element parentElement) throws IOException, CycApiException {
    CycList genlPreds = filterSelectedConstants(cycAccess.getGenlPreds(cycConstant));
    if (verbosity > 4)
      Log.current.println("  genlPreds " + genlPreds);
    CycList tempList = new CycList();
    for (int i = 0; i < genlPreds.size(); i++) {
      CycConstant genlPred = (CycConstant)genlPreds.get(i);
      if (selectedCycForts.contains(genlPred))
        tempList.add(genlPred);
    }
    genlPreds = tempList;
    if (verbosity > 4)
      Log.current.println("  after filtering, genlPreds " + genlPreds);
    if (genlPreds.size() == 0)
      return;
    lineBreak(parentElement);
    Element bElement = htmlDocument.createElement("b");
    parentElement.appendChild(bElement);
    Node genlsPredsLabelTextNode = htmlDocument.createTextNode("direct specialization of: ");
    bElement.appendChild(genlsPredsLabelTextNode);
    for (int i = 0; i < genlPreds.size(); i++) {
      CycConstant genlPred = (CycConstant)genlPreds.get(i);
      HTMLAnchorElement genlPredAnchorElement =
      new HTMLAnchorElementImpl((HTMLDocumentImpl)htmlDocument, "a");
      genlPredAnchorElement.setHref("./" + exportedVocabularyOutputPath + "#" + genlPred.cyclify());
      parentElement.appendChild(genlPredAnchorElement);
      Node genlPredTextNode = htmlDocument.createTextNode(genlPred.cyclify());
      genlPredAnchorElement.appendChild(genlPredTextNode);
      Node spacesTextNode = htmlDocument.createTextNode("  ");
      parentElement.appendChild(spacesTextNode);
    }
  }
  
  /**
   * Creates HTML nodes for arg1Isa links.
   *
   * @param cycConstant The CycConstant for which arg1Isa links are to be created.
   * @param parentElement The parent HTML DOM element.
   */
  protected void createArg1IsaNodes(CycConstant cycConstant, Element parentElement) throws IOException, CycApiException {
    CycList arg1Isas = findAllowedTermsOrGenls(cycAccess.getArg1Isas(cycConstant));
    if (verbosity > 4)
      Log.current.println("  arg1Isas " + arg1Isas);
    if (arg1Isas.size() == 0)
      return;
    lineBreak(parentElement);
    Element bElement = htmlDocument.createElement("b");
    parentElement.appendChild(bElement);
    Node arg1IsasLabelTextNode = htmlDocument.createTextNode("argument one is an instance of: ");
    bElement.appendChild(arg1IsasLabelTextNode);
    for (int i = 0; i < arg1Isas.size(); i++) {
      CycConstant arg1Isa = (CycConstant)arg1Isas.get(i);
      HTMLAnchorElement arg1IsaAnchorElement =
      new HTMLAnchorElementImpl((HTMLDocumentImpl)htmlDocument, "a");
      arg1IsaAnchorElement.setHref("./" + exportedVocabularyOutputPath + "#" + arg1Isa.cyclify());
      parentElement.appendChild(arg1IsaAnchorElement);
      Node arg1IsaTextNode = htmlDocument.createTextNode(arg1Isa.cyclify());
      arg1IsaAnchorElement.appendChild(arg1IsaTextNode);
      Node spacesTextNode = htmlDocument.createTextNode("  ");
      parentElement.appendChild(spacesTextNode);
    }
  }
  
  /**
   * Creates HTML nodes for arg2Isa links.
   *
   * @param cycConstant The CycConstant for which arg2Isa links are to be created.
   * @param parentElement The parent HTML DOM element.
   */
  protected void createArg2IsaNodes(CycConstant cycConstant, Element parentElement) throws IOException, CycApiException {
    CycList arg2Isas = findAllowedTermsOrGenls(cycAccess.getArg2Isas(cycConstant));
    if (verbosity > 4)
      Log.current.println("  arg2Isas " + arg2Isas);
    if (arg2Isas.size() == 0)
      return;
    lineBreak(parentElement);
    Element bElement = htmlDocument.createElement("b");
    parentElement.appendChild(bElement);
    Node arg2IsasLabelTextNode = htmlDocument.createTextNode("argument two is an instance of: ");
    bElement.appendChild(arg2IsasLabelTextNode);
    for (int i = 0; i < arg2Isas.size(); i++) {
      CycConstant arg2Isa = (CycConstant)arg2Isas.get(i);
      HTMLAnchorElement arg2IsaAnchorElement =
      new HTMLAnchorElementImpl((HTMLDocumentImpl)htmlDocument, "a");
      arg2IsaAnchorElement.setHref("./" + exportedVocabularyOutputPath + "#" + arg2Isa.cyclify());
      parentElement.appendChild(arg2IsaAnchorElement);
      Node arg2IsaTextNode = htmlDocument.createTextNode(arg2Isa.cyclify());
      arg2IsaAnchorElement.appendChild(arg2IsaTextNode);
      Node spacesTextNode = htmlDocument.createTextNode("  ");
      parentElement.appendChild(spacesTextNode);
    }
  }
  
  /**
   * Creates HTML nodes for arg3Isa links.
   *
   * @param cycConstant The CycConstant for which arg3Isa links are to be created.
   * @param parentElement The parent HTML DOM element.
   */
  protected void createArg3IsaNodes(CycConstant cycConstant, Element parentElement) throws IOException, CycApiException {
    CycList arg3Isas = findAllowedTermsOrGenls(cycAccess.getArg3Isas(cycConstant));
    if (verbosity > 4)
      Log.current.println("  arg3Isas " + arg3Isas);
    if (arg3Isas.size() == 0)
      return;
    lineBreak(parentElement);
    Element bElement = htmlDocument.createElement("b");
    parentElement.appendChild(bElement);
    Node arg3IsasLabelTextNode = htmlDocument.createTextNode("argument three is an instance of: ");
    bElement.appendChild(arg3IsasLabelTextNode);
    for (int i = 0; i < arg3Isas.size(); i++) {
      CycConstant arg3Isa = (CycConstant)arg3Isas.get(i);
      HTMLAnchorElement arg3IsaAnchorElement =
      new HTMLAnchorElementImpl((HTMLDocumentImpl)htmlDocument, "a");
      arg3IsaAnchorElement.setHref("./" + exportedVocabularyOutputPath + "#" + arg3Isa.cyclify());
      parentElement.appendChild(arg3IsaAnchorElement);
      Node arg3IsaTextNode = htmlDocument.createTextNode(arg3Isa.cyclify());
      arg3IsaAnchorElement.appendChild(arg3IsaTextNode);
      Node spacesTextNode = htmlDocument.createTextNode("  ");
      parentElement.appendChild(spacesTextNode);
    }
  }
  
  /**
   * Creates HTML nodes for arg4Isa links.
   *
   * @param cycConstant The CycConstant for which arg4Isa links are to be created.
   * @param parentElement The parent HTML DOM element.
   */
  protected void createArg4IsaNodes(CycConstant cycConstant, Element parentElement) throws IOException, CycApiException {
    CycList arg4Isas = findAllowedTermsOrGenls(cycAccess.getArg4Isas(cycConstant));
    if (verbosity > 4)
      Log.current.println("  arg4Isas " + arg4Isas);
    if (arg4Isas.size() == 0)
      return;
    lineBreak(parentElement);
    Element bElement = htmlDocument.createElement("b");
    parentElement.appendChild(bElement);
    Node arg4IsasLabelTextNode = htmlDocument.createTextNode("argument four is an instance of: ");
    bElement.appendChild(arg4IsasLabelTextNode);
    for (int i = 0; i < arg4Isas.size(); i++) {
      CycConstant arg4Isa = (CycConstant)arg4Isas.get(i);
      HTMLAnchorElement arg4IsaAnchorElement =
      new HTMLAnchorElementImpl((HTMLDocumentImpl)htmlDocument, "a");
      arg4IsaAnchorElement.setHref("./" + exportedVocabularyOutputPath + "#" + arg4Isa.cyclify());
      parentElement.appendChild(arg4IsaAnchorElement);
      Node arg4IsaTextNode = htmlDocument.createTextNode(arg4Isa.cyclify());
      arg4IsaAnchorElement.appendChild(arg4IsaTextNode);
      Node spacesTextNode = htmlDocument.createTextNode("  ");
      parentElement.appendChild(spacesTextNode);
    }
  }
  
  /**
   * Creates HTML nodes for resultIsa links.
   *
   * @param cycConstant The CycConstant for which resultIsa links are to be created.
   * @param parentElement The parent HTML DOM element.
   */
  protected void createResultIsaNodes(CycConstant cycConstant, Element parentElement) throws IOException, CycApiException {
    CycList resultIsas = findAllowedTermsOrGenls(cycAccess.getResultIsas(cycConstant));
    if (verbosity > 4)
      Log.current.println("  resultIsas " + resultIsas);
    if (resultIsas.size() == 0)
      return;
    lineBreak(parentElement);
    Element bElement = htmlDocument.createElement("b");
    parentElement.appendChild(bElement);
    Node resultIsasLabelTextNode = htmlDocument.createTextNode("result is an instance of: ");
    bElement.appendChild(resultIsasLabelTextNode);
    for (int i = 0; i < resultIsas.size(); i++) {
      CycConstant resultIsa = (CycConstant)resultIsas.get(i);
      HTMLAnchorElement resultIsaAnchorElement =
      new HTMLAnchorElementImpl((HTMLDocumentImpl)htmlDocument, "a");
      resultIsaAnchorElement.setHref("./" + exportedVocabularyOutputPath + "#" + resultIsa.cyclify());
      parentElement.appendChild(resultIsaAnchorElement);
      Node resultIsaTextNode = htmlDocument.createTextNode(resultIsa.cyclify());
      resultIsaAnchorElement.appendChild(resultIsaTextNode);
      Node spacesTextNode = htmlDocument.createTextNode("  ");
      parentElement.appendChild(spacesTextNode);
    }
  }
  
  /**
   * Creates an HTML individual node for a single Cyc individual.
   *
   * @param cycConstant The Cyc individual from which the HTML individual node is created.
   * @param parentElement The parent HTML DOM element.
   */
  protected void createIndividualNode(CycConstant cycConstant, Element parentElement) throws UnknownHostException, IOException, CycApiException {
    HTMLAnchorElement individualAnchorElement =
    new HTMLAnchorElementImpl((HTMLDocumentImpl)htmlDocument, "a");
    individualAnchorElement.setHref("##$Individual");
    parentElement.appendChild(individualAnchorElement);
    Node individualTextNode = htmlDocument.createTextNode("#$Individual");
    individualAnchorElement.appendChild(individualTextNode);
  }
  
  /**
   * Creates an HTML node for a single Cyc collection.
   *
   * @param cycConstant The Cyc collection from which the HTML node is created.
   * @param parentElement The parent HTML DOM element.
   */
  protected void createCollectionNode(CycConstant cycConstant, Element parentElement) throws UnknownHostException, IOException, CycApiException {
    createGenlNodes(cycConstant, parentElement);
  }
  
  /**
   * Creates hierarchy HTML page.
   *
   * @param rootTerm The root term of the hierarchy tree.
   */
  protected void createHierarchyPage(CycFort rootTerm) throws UnknownHostException, IOException, CycApiException {
    if (verbosity > 2)
      Log.current.println("Building HTML model for hierarchy page");
    htmlDocument = new HTMLDocumentImpl();
    String title = "Cyc ontology hierarchy for " + cycKbSubsetCollection.cyclify();
    htmlDocument.setTitle(title);
    Node htmlNode = htmlDocument.getChildNodes().item(0);
    htmlBodyElement = htmlDocument.createElement("body");
    htmlNode.appendChild(htmlBodyElement);
    Element headingElement = htmlDocument.createElement("h1");
    htmlBodyElement.appendChild(headingElement);
    Node headingTextNode = htmlDocument.createTextNode(title);
    headingElement.appendChild(headingTextNode);
    previouslyExpandedTerms = new HashSet();
    createHierarchyNodes(rootTerm, 0);
    serialize(htmlDocument, exportedHierarchyOutputPath);
  }
  
  /**
   * Recursively creates hierarchy nodes for the given term and its spec collection terms.
   *
   * @param cycFort The given term for which hierarchy nodes will be created.
   * @param indent The current indent level.
   */
  protected void createHierarchyNodes(CycFort cycFort, int indent) throws IOException, CycApiException {
    if (indent > 0) {
      StringBuffer spaces = new StringBuffer(indent);
      StringBuffer nonBreakingSpaces = new StringBuffer(indent);
      for (int i = 0; i < indent; i++) {
        spaces.append(' ');
        nonBreakingSpaces.append("&nbsp;");
      }
      Node spacesText = htmlDocument.createTextNode(nonBreakingSpaces.toString());
      htmlBodyElement.appendChild(spacesText);
      if (verbosity > 2)
        Log.current.println(spaces.toString() + cycFort);
    }
    else {
      if (verbosity > 2)
        Log.current.println(cycFort.toString());
    }
    HTMLAnchorElement vocabularyAnchorElement = new HTMLAnchorElementImpl((HTMLDocumentImpl)htmlDocument, "a");
    vocabularyAnchorElement.setHref("./" + exportedVocabularyOutputPath + "./" + exportedVocabularyOutputPath + "#" + cycFort.cyclify());
    htmlBodyElement.appendChild(vocabularyAnchorElement);
    Node hierarchyTermTextNode = htmlDocument.createTextNode(cycFort.cyclify());
    vocabularyAnchorElement.appendChild(hierarchyTermTextNode);
    String generatedPhrase = cycAccess.getPluralGeneratedPhrase(cycFort);
    if (generatedPhrase.endsWith("(unclassified term)"))
      generatedPhrase = generatedPhrase.substring(0, generatedPhrase.length() - 20);
    Node generatedPhraseTextNode = htmlDocument.createTextNode("&nbsp;&nbsp;" + generatedPhrase + "");
    htmlBodyElement.appendChild(generatedPhraseTextNode);
    CycList specs = cycAccess.getSpecs(cycFort);
    specs = filterSelectedConstants(specs);
    if (specs.size() == 0) {
      vocabularyAnchorElement.setName(cycFort.cyclify());
      lineBreak(htmlBodyElement);
    }
    else if (previouslyExpandedTerms.contains(cycFort)) {
      Node previouslyExpandedTextNode = htmlDocument.createTextNode("&nbsp;&nbsp;... see above");
      htmlBodyElement.appendChild(previouslyExpandedTextNode);
      lineBreak(htmlBodyElement);
    }
    else {
      previouslyExpandedTerms.add(cycFort);
      vocabularyAnchorElement.setName(cycFort.cyclify());
      lineBreak(htmlBodyElement);
      for (int i = 0; i < specs.size(); i++)
        createHierarchyNodes((CycFort)specs.get(i), indent + 2);
    }
  }
  
  /**
   * Serializes the given HTML document to the given path.
   *
   * @param htmlDocument The HTML document model for serialization.
   * @param outputPath The file name of the serialized HTML document.
   */
  protected void serialize(HTMLDocument htmlDocument, String outputPath) throws IOException {
    if (verbosity > 2)
      Log.current.println("Writing HTML output to " + outputPath);
    OutputFormat outputFormat = new OutputFormat(htmlDocument, "UTF-8", true);
    BufferedWriter htmlOut = new BufferedWriter(new FileWriter(outputPath));
    XHTMLSerializer xhtmlSerializer = new XHTMLSerializer(htmlOut, outputFormat);
    xhtmlSerializer.asDOMSerializer();
    xhtmlSerializer.serialize(htmlDocument);
    htmlOut.close();
  }
  
  /**
   * Creates categorized vocabulary HTML pages.
   */
  protected void createCategorizedVocabularies()
  throws UnknownHostException, IOException, CycApiException {
    for (int i = 0; i < categories.size(); i++) {
      Category category = (Category) categories.get(i);
      createCategorizedVocabulary(category.title,
      category.queryString,
      category.mt,
      category.outputPath);
    }
  }
  
  /**
   * Creates the given categorized vocabulary HTML page.
   *
   * @param title the title of the categorized vocabulary HTML page
   * @param queryString the query string which finds the terms in the
   * category
   * @param mt the mt in which the query is asked
   * @param outputPath the name of the output HTML page
   */
  protected void createCategorizedVocabulary(String title,
  String queryString,
  CycConstant mt,
  String outputPath)
  throws UnknownHostException, IOException, CycApiException {
    CycList categoryTerms = askQueryString(queryString, mt);
    if (categoryTerms.size() == 0) {
      if (verbosity > 2)
        Log.current.println("No terms for query:\n" + queryString + "\n");
      return;
    }
    if (verbosity > 2)
      Log.current.println("Building HTML model for category: " + title);
    htmlDocument = new HTMLDocumentImpl();
    htmlDocument.setTitle(title);
    Node htmlNode = htmlDocument.getChildNodes().item(0);
    htmlBodyElement = htmlDocument.createElement("body");
    htmlNode.appendChild(htmlBodyElement);
    Element headingElement = htmlDocument.createElement("h1");
    htmlBodyElement.appendChild(headingElement);
    Node headingTextNode = htmlDocument.createTextNode(title);
    headingElement.appendChild(headingTextNode);
    for (int i = 0; i < categoryTerms.size(); i++) {
      CycFort cycFort = (CycFort) categoryTerms.get(i);
      if (verbosity > 2)
        Log.current.print(cycFort + "  ");
      if (cycAccess.isCollection(cycFort)) {
        if (verbosity > 2)
          Log.current.println("Collection");
      }
      else if (cycAccess.isPredicate(cycFort)) {
        if (verbosity > 2)
          Log.current.println("Predicate");
      }
      else if (cycAccess.isIndividual(cycFort)) {
        if (verbosity > 2)
          Log.current.println("Individual");
      }
      else {
        if (verbosity > 2)
          Log.current.println("other");
        continue;
      }
      if (cycFort instanceof CycConstant)
        createCycConstantNode((CycConstant)cycFort);
      else
        createCycNartNode((CycNart)cycFort);
    }
    serialize(htmlDocument, outputPath);
  }
  
  /**
   * Adds all categories to the HTML export.
   */
  protected void addAllCategories()
  throws CycApiException, IOException , UnknownHostException {
    addAllIETRef();
    addAllSyracuseRef();
    addAllSraRef();
    addAllVeridianRef();
    addAllSchemaMappingTypes();
    addScriptRepresentationVocabulary();
    addSemanticConstraintVocabulary();
    addAllPersonTypes();
    addAllOrganizationTypes();
    addAllEventTypes();
    addAllMaterials();
    addAllWeaponTypes();
    addAllRelations();
    addAllBinaryPredicates();
    addAllActorSlots();
    addAllPartonomicPredicates();
    addAllSpecializationsOfAffiliation();
    addAllSpecializationsOfVestedInterest();
    addAllDenotingFunctions();
    addAllTemporalRelations();
    addAllScriptedEventTypes();
    addEeldCoreOntology();
    addPublicSharedOntology();
  }
  
  /**
   * Categorizes all ontology constants directly referenced in the IET Mapping.
   */
  protected void addAllIETRef()
  throws CycApiException, IOException , UnknownHostException {
    Category category = new Category();
    categories.add(category);
    category.title = "IET Synthetic Data set constants";
    category.outputPath = "all-iet-ref.html";
    category.queryString =
    "(#$isa ?TERM #$EELDSyntheticDataConstant)\n";
    category.mt = cycAccess.getKnownConstantByName("#$EELDOntologyAlignmentSpindleCollectorMt");
  }
  
  /**
   * Categorizes all ontology constants directly referenced in the Syracuse Mapping.
   */
  protected void addAllSyracuseRef()
  throws CycApiException, IOException , UnknownHostException {
    Category category = new Category();
    categories.add(category);
    category.title = "Constants corresponding to terms in the Syracuse ontology";
    category.outputPath = "all-syracuse-ref.html";
    category.queryString =
    "(#$thereExists ?STRING\n" +
    " (#$or\n" +
    "  (#$synonymousExternalConcept ?TERM #$SyracuseOntology ?STRING)\n" +
    "  (#$overlappingExternalConcept ?TERM #$SyracuseOntology ?STRING)))\n";
    category.mt = cycAccess.getKnownConstantByName("#$EELDOntologyAlignmentSpindleCollectorMt");
  }
  
  /**
   * Categorizes all constants directly reference in the SRA NetOwl Mapping.
   */
  protected void addAllSraRef()
  throws CycApiException, IOException , UnknownHostException {
    Category category = new Category();
    categories.add(category);
    category.title = "Constants corresponding to terms in the NetOwl template extraction ontology.";
    category.outputPath = "all-sra-ref.html";
    category.queryString =
    "(#$thereExists ?STRING\n" +
    " (#$or\n" +
    "  (#$synonymousExternalConcept\n" +
    "      ?TERM #$SRATemplateExtractionOntology ?STRING)\n" +
    "  (#$overlappingExternalConcept\n" +
    "      ?TERM #$SRATemplateExtractionOntology ?STRING)))\n";
    category.mt = cycAccess.getKnownConstantByName("#$EELDOntologyAlignmentSpindleCollectorMt");
  }
  
  /**
   * Categorizes all ontology constants directly referenced in the Veridian Mapping.
   */
  protected void addAllVeridianRef()
  throws CycApiException, IOException , UnknownHostException {
    Category category = new Category();
    categories.add(category);
    category.title = "Constants corresponding to terms referenced in the Veridian schema specification.";
    category.outputPath = "all-veridian-ref.html";
    category.queryString =
    "(#$thereExists ?PROP\n" +
    "  (#$and\n" +
    "    (#$isa ?TERM #$Collection)\n" +
    "    (#$unknownSentence\n" +
    "      (#$isa ?TERM #$SKSIConstant))\n" +
    "    (#$unknownSentence\n" +
    "      (#$isa ?TERM #$IndexicalConcept))\n" +
    "    (#$different ?TERM #$meaningSentenceOfSchema)\n" +
    "    (#$ist-Asserted #$VeridianMappingMt ?PROP)\n" +
    "    (#$assertedTermSentences ?TERM ?PROP)))";
    category.mt = cycAccess.getKnownConstantByName("#$VeridianMappingMt");
  }
  
  /**
   * Categorizes all schema mapping types.
   */
  protected void addAllSchemaMappingTypes()
  throws CycApiException, IOException , UnknownHostException {
    Category category = new Category();
    categories.add(category);
    category.title = "Terms used to specify the interpretation of the Veridian schema.";
    category.outputPath = "all-schema-mapping-types.html";
    category.queryString =
    "(#$and \n" +
    "  (#$isa ?TERM #$EELDSharedOntologyConstant)\n" +
    "  (#$isa ?TERM #$SKSIConstant))\n";
    category.mt = cycAccess.getKnownConstantByName("#$SKSIMt");
  }
  
  /**
   * Categorizes script representation vocabulary.
   */
  protected void addScriptRepresentationVocabulary()
  throws CycApiException, IOException , UnknownHostException {
    Category category = new Category();
    categories.add(category);
    category.title = "Relations used in representing scripts.";
    category.outputPath = "script-representation-vocabulary.html";
    category.queryString =
    "(#$and \n" +
    "  (#$isa ?TERM #$EELDSharedOntologyConstant)\n" +
    "  (#$isa ?TERM #$ScriptRelation))\n";
    category.mt = cycAccess.getKnownConstantByName("#$EELDOntologyAlignmentSpindleCollectorMt");
  }
  
  /**
   * Categorizes semantic constraint vocabulary.
   */
  protected void addSemanticConstraintVocabulary()
  throws CycApiException, IOException , UnknownHostException {
    Category category = new Category();
    categories.add(category);
    category.title = "Relations used to specify semantic constraints.";
    category.outputPath = "semantic-constraint-vocabulary.html";
    category.queryString =
    "(#$and\n" +
    " (#$isa ?TERM #$EELDSharedOntologyConstant)\n" +
    " (#$isa ?TERM #$OpenCycDefinitionalPredicate))";
    category.mt = cycAccess.getKnownConstantByName("#$EELDOntologyAlignmentSpindleCollectorMt");
  }
  
  /**
   * Categorizes all person types.
   */
  protected void addAllPersonTypes()
  throws CycApiException, IOException , UnknownHostException {
    Category category = new Category();
    categories.add(category);
    category.title = "Classes of persons in the ontology release.";
    category.outputPath = "all-person-types.html";
    category.queryString =
    "(#$and\n" +
    " (#$isa ?TERM #$EELDSharedOntologyConstant)\n" +
    " (#$genls ?TERM #$Person))\n";
    category.mt = cycAccess.getKnownConstantByName("#$EELDOntologyAlignmentSpindleCollectorMt");
  }
  
  /**
   * Categorizes all organization types.
   */
  protected void addAllOrganizationTypes()
  throws CycApiException, IOException , UnknownHostException {
    Category category = new Category();
    categories.add(category);
    category.title = "Classes of organizations in the ontology release.";
    category.outputPath = "all-organization-types.html";
    category.queryString =
    "(#$and\n" +
    " (#$isa ?TERM #$EELDSharedOntologyConstant)\n" +
    " (#$genls ?TERM #$Organization))\n";
    category.mt = cycAccess.getKnownConstantByName("#$EELDOntologyAlignmentSpindleCollectorMt");
  }
  
  /**
   * Categorizes all event types.
   */
  protected void addAllEventTypes()
  throws CycApiException, IOException , UnknownHostException {
    Category category = new Category();
    categories.add(category);
    category.title = "Types of event.";
    category.outputPath = "all-event-types.html";
    category.queryString =
    "(#$and\n" +
    " (#$isa ?TERM #$EELDSharedOntologyConstant)\n" +
    " (#$genls ?TERM #$Event))\n";
    category.mt = cycAccess.getKnownConstantByName("#$EELDOntologyAlignmentSpindleCollectorMt");
  }
  
  /**
   * Categorizes all 'materials'.
   */
  protected void addAllMaterials()
  throws CycApiException, IOException , UnknownHostException {
    Category category = new Category();
    categories.add(category);
    category.title = "Types of material.";
    category.outputPath = "all-materials.html";
    category.queryString =
    "(#$and\n" +
    " (#$isa ?TERM #$EELDSharedOntologyConstant)\n" +
    " (#$isa ?TERM #$ExistingStuffType))\n";
    category.mt = cycAccess.getKnownConstantByName("#$EELDOntologyAlignmentSpindleCollectorMt");
  }
  
  /**
   * Categorizes all weapon types.
   */
  protected void addAllWeaponTypes()
  throws CycApiException, IOException , UnknownHostException {
    Category category = new Category();
    categories.add(category);
    category.title = "Types of weapon.";
    category.outputPath = "all-weapon-types.html";
    category.queryString =
    "(#$and\n" +
    " (#$isa ?TERM #$EELDSharedOntologyConstant)\n" +
    " (#$genls ?TERM #$Weapon))\n";
    category.mt = cycAccess.getKnownConstantByName("#$EELDOntologyAlignmentSpindleCollectorMt");
  }
  
  /**
   * Categorizes all relations.
   */
  protected void addAllRelations()
  throws CycApiException, IOException , UnknownHostException {
    Category category = new Category();
    categories.add(category);
    category.title = "All relations in the ontology.";
    category.outputPath = "all-relations.html";
    category.queryString =
    "(#$and\n" +
    " (#$isa ?TERM #$EELDSharedOntologyConstant)\n" +
    " (#$isa ?TERM #$Relation))\n";
    category.mt = cycAccess.getKnownConstantByName("#$EELDOntologyAlignmentSpindleCollectorMt");
  }
  
  /**
   * Categorizes all binary predicates.
   */
  protected void addAllBinaryPredicates()
  throws CycApiException, IOException , UnknownHostException {
    Category category = new Category();
    categories.add(category);
    category.title = "all binary predicates.";
    category.outputPath = "all-binary-predicates.html";
    category.queryString =
    "(#$and\n" +
    " (#$isa ?TERM #$EELDSharedOntologyConstant)\n" +
    " (#$isa ?TERM #$BinaryPredicate))\n";
    category.mt = cycAccess.getKnownConstantByName("#$EELDOntologyAlignmentSpindleCollectorMt");
  }
  
  /**
   * Categorizes all actorslots.
   */
  protected void addAllActorSlots()
  throws CycApiException, IOException , UnknownHostException {
    Category category = new Category();
    categories.add(category);
    category.title = "All of the binary role relations obtaining between entities and events in the ontology.";
    category.outputPath = "all-actor-slots.html";
    category.queryString =
    "(#$and\n" +
    " (#$isa ?TERM #$EELDSharedOntologyConstant)\n" +
    " (#$isa ?TERM #$ActorSlot))\n";
    category.mt = cycAccess.getKnownConstantByName("#$EELDOntologyAlignmentSpindleCollectorMt");
  }
  
  /**
   * Categorizes all temporal relations.
   */
  protected void addAllTemporalRelations()
  throws CycApiException, IOException , UnknownHostException {
    Category category = new Category();
    categories.add(category);
    category.title = "All of the temporal ordering relations in the ontology.";
    category.outputPath = "all-temporal-relations.html";
    category.queryString =
    "(#$and\n" +
    " (#$isa ?TERM #$EELDSharedOntologyConstant)\n" +
    " (#$isa ?TERM #$ComplexTemporalPredicate))\n";
    category.mt = cycAccess.getKnownConstantByName("#$EELDOntologyAlignmentSpindleCollectorMt");
  }
  
  /**
   * Categorizes all partonomic predicates.
   */
  protected void addAllPartonomicPredicates()
  throws CycApiException, IOException , UnknownHostException {
    Category category = new Category();
    categories.add(category);
    category.title = "All of the part-whole relations in the ontology.";
    category.outputPath = "all-partonomic-predicates.html";
    category.queryString =
    "(#$and\n" +
    " (#$isa ?TERM #$EELDSharedOntologyConstant)\n" +
    " (#$isa ?TERM #$PartPredicate))\n";
    category.mt = cycAccess.getKnownConstantByName("#$EELDOntologyAlignmentSpindleCollectorMt");
  }
  
  /**
   * Categorizes all specializations of affiliation.
   */
  protected void addAllSpecializationsOfAffiliation()
  throws CycApiException, IOException , UnknownHostException {
    Category category = new Category();
    categories.add(category);
    category.title = "All of the specializations of the affiliatedWith relation.";
    category.outputPath = "all-specializations-of-affiliation.html";
    category.queryString =
    "(#$and\n" +
    " (#$isa ?TERM #$EELDSharedOntologyConstant)\n" +
    " (#$genlPreds ?TERM #$affiliatedWith))\n";
    category.mt = cycAccess.getKnownConstantByName("#$EELDOntologyAlignmentSpindleCollectorMt");
  }
  
  /**
   * Categorizes all specializations of vested interest.
   */
  protected void addAllSpecializationsOfVestedInterest()
  throws CycApiException, IOException , UnknownHostException {
    Category category = new Category();
    categories.add(category);
    category.title = "All of the specializations of the vestedInterest relation.";
    category.outputPath = "all-specializations-of-vested-interest.html";
    category.queryString =
    "(#$and\n" +
    " (#$isa ?TERM #$EELDSharedOntologyConstant)\n" +
    " (#$genlPreds ?TERM #$vestedInterest))\n";
    category.mt = cycAccess.getKnownConstantByName("#$EELDOntologyAlignmentSpindleCollectorMt");
  }
  
  /**
   * Categorizes all denoting functions.
   */
  protected void addAllDenotingFunctions()
  throws CycApiException, IOException , UnknownHostException {
    Category category = new Category();
    categories.add(category);
    category.title = "All denoting functions.";
    category.outputPath = "all-denoting-functions.html";
    category.queryString =
    "(#$and\n" +
    " (#$isa ?TERM #$EELDSharedOntologyConstant)\n" +
    " (#$isa ?TERM #$Function-Denotational))\n";
    category.mt = cycAccess.getKnownConstantByName("#$EELDOntologyAlignmentSpindleCollectorMt");
  }
  
  /**
   * Categorizes all scripted event types.
   */
  protected void addAllScriptedEventTypes()
  throws CycApiException, IOException , UnknownHostException {
    Category category = new Category();
    categories.add(category);
    category.title = "All scripted event types.";
    category.outputPath = "all-scripted-event-types.html";
    category.queryString =
    "(#$and\n" +
    " (#$isa ?TERM #$EELDSharedOntologyConstant)\n" +
    " (#$isa ?TERM #$ScriptedEventType))\n";
    category.mt = cycAccess.getKnownConstantByName("#$EELDOntologyAlignmentSpindleCollectorMt");
  }
  
  
  /**
   * Categorizes the original EELD 'core' ontology.
   */
  protected void addEeldCoreOntology()
  throws CycApiException, IOException , UnknownHostException {
    Category category = new Category();
    categories.add(category);
    category.title = "EELD Core Ontology";
    category.outputPath = "all-core.html";
    category.queryString =
    "(#$isa ?TERM #$EELDSharedOntologyCoreConstant)";
    category.mt = cycAccess.getKnownConstantByName("#$EELDOntologyAlignmentSpindleCollectorMt");
  }
  
  /**
   * Categorizes all public shared ontology constants.
   */
  protected void addPublicSharedOntology()
  throws CycApiException, IOException , UnknownHostException {
    Category category = new Category();
    categories.add(category);
    category.title = "Public Shared Ontology";
    category.outputPath = "all-public-shared.html";
    category.queryString =
    "(#$and\n" +
    " (#$isa ?TERM #$EELDSharedOntologyConstant)\n" +
    " (#$isa ?TERM #$ProposedOrPublicConstant))\n";
    category.mt = cycAccess.getKnownConstantByName("#$EELDOntologyAlignmentSpindleCollectorMt");
  }
  
  /**
   * Categorizes the original EELD 'core' ontology.
   */
  protected void askEeldCoreOntology()
  throws CycApiException, IOException , UnknownHostException {
    Category category = new Category();
    categories.add(category);
    category.title = "EELD Core Ontology";
    category.outputPath = "all-core.html";
    category.queryString =
    "(#$isa ?TERM #$EELDSharedOntologyCoreConstant)";
    category.mt = cycAccess.getKnownConstantByName("#$EELDOntologyAlignmentSpindleCollectorMt");
  }
  
  /**
   * Returns the sorted terms resulting from asking the given query string.
   *
   * @param queryString the query, as a string
   * @param mt the mt in which the query is asked
   * @return the sorted terms resulting from asking the given query string
   */
  protected CycList askQueryString(String queryString, CycConstant mt)
  throws CycApiException, IOException , UnknownHostException {
    CycList query = cycAccess.makeCycList(queryString);
    CycVariable variable = CycObjectFactory.makeCycVariable("?TERM");
    CycList answer = cycAccess.queryVariable(variable, query, mt, (HashMap) null);
    if (verbosity > 2) {
      Log.current.println("query:\n" + queryString);
      Log.current.println("number of terms " + answer.size());
    }
    // Remove nauts which are returned as CycLists.
    CycList answerTemp = new CycList();
    for (int i = 0; i < answer.size(); i++) {
      Object obj = answer.get(i);
      if (obj instanceof CycFort)
        answerTemp.add(obj);
      else if (verbosity > 2)
        Log.current.println("Dropping non fort from answer\n" + obj.toString());
    }
    answer = answerTemp;
    Collections.sort(answer);
    return answer;
  }
  
  /**
   * Provides a container for term category information used to make
   * vocabulary category HTML pages.
   */
  public class Category {
    /**
     * The query string used to make this categorized vocabulary page.
     */
    public String queryString;
    
    /**
     * The mt in which the query is asked.
     */
    public CycConstant mt;
    
    /**
     * The file name used to output this categorized vocabulary page.
     */
    public String outputPath;
    
    /**
     * The title of this categorized vocabulary page.
     */
    public String title;
  }
  
  /**
   * Runs the ExportHtml application.
   *
   * @param args The command line arguments.
   */
  public static void main(String[] args) {
    final Guid counterTerrorismConstantGuid = CycObjectFactory.makeGuid("bfe31c38-9c29-11b1-9dad-c379636f7270");
    final Guid terrorismOntologyConstantGuid = CycObjectFactory.makeGuid("93cfd306-4d27-41d9-8223-b22fa483c1eb");
    CycAccess cycAccess = null;
    try {
      cycAccess = new CycAccess(CycConnection.DEFAULT_HOSTNAME,
                                3600,
                                CycConnection.DEFAULT_COMMUNICATION_MODE,
                                CycAccess.DEFAULT_CONNECTION);
    }
    catch (Exception e) {
      Log.current.errorPrintln(e.getMessage());
      System.exit(1);
    }
    ExportHtml exportHtml = new ExportHtml(cycAccess);
    exportHtml.verbosity = 3;
    exportHtml.produceHierarchyPages = false;
    exportHtml.includeUpwardClosure = true;
    String choice = "terrorism-ontology-all";
    //        if (args.length > 0)
    //            choice = args[0];
    try {
      Log.current.println("Choosing KB selection: " + choice);
      // These require the ResearchCyc or full KB to work as setup below.
      if (choice.equals("terrorism-ontology")) {
        //                exportHtml.verbosity = 9;
        exportHtml.produceHierarchyPages = false;
        exportHtml.includeUpwardClosure = false;
        exportHtml.cycKbSubsetCollectionGuid = terrorismOntologyConstantGuid;
        exportHtml.exportedVocabularyOutputPath = "terrorism-ontology.html";
        exportHtml.upwardClosureKbSubsetCollectionGuids.add(publicConstantGuid);
        exportHtml.print_guid = false;
        exportHtml.cycKbSubsetFilterGuid = researchCycConstantGuid;
        exportHtml.export(EXPORT_KB_SUBSET);
        System.exit(0);
      }
      else if (choice.equals("terrorism-ontology-all")) {
        exportHtml.produceHierarchyPages = false;
        exportHtml.includeUpwardClosure = true;
        exportHtml.cycKbSubsetCollectionGuid = terrorismOntologyConstantGuid;
        exportHtml.exportedVocabularyOutputPath = "terrorism-ontology-all.html";
        exportHtml.upwardClosureKbSubsetCollectionGuids.add(publicConstantGuid);
        exportHtml.print_guid = false;
        exportHtml.cycKbSubsetFilterGuid = researchCycConstantGuid;
        exportHtml.export(EXPORT_KB_SUBSET_PLUS_UPWARD_CLOSURE);
        System.exit(0);
      }
      else if (choice.equals("counter-terrorism")) {
        exportHtml.cycKbSubsetCollectionGuid = counterTerrorismConstantGuid;
        exportHtml.exportedVocabularyOutputPath = "counter-terrorism-vocabulary.html";
        exportHtml.exportedHierarchyOutputPath = "counter-terrorism-hierarchy.html";
      }
      else {
        System.out.println("specified choice not found - " + choice);
        System.exit(1);
      }
      exportHtml.upwardClosureKbSubsetCollectionGuids.add(publicConstantGuid);
      
      Guid quaUnterestingWeaponSystemGuid =
      CycObjectFactory.makeGuid("c1085b99-9c29-11b1-9dad-c379636f7270");
      Guid economicInterestTypeGuid =
      CycObjectFactory.makeGuid("c02a0764-9c29-11b1-9dad-c379636f7270");
      
      exportHtml.filterFromDirectInstanceGuids.add(quaUnterestingWeaponSystemGuid);
      exportHtml.filterFromDirectInstanceGuids.add(economicInterestTypeGuid);
      exportHtml.print_guid = false;
      
      exportHtml.cycKbSubsetFilterGuid = ikbConstantGuid;
      exportHtml.export(EXPORT_KB_SUBSET_PLUS_UPWARD_CLOSURE);
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
  }
  
}
