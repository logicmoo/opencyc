package  org.opencyc.api;

import  java.net.*;
import  java.io.*;
import  ViolinStrings.*;
import  org.opencyc.util.*;
import  org.opencyc.cycobject.*;

/**
 * Provides a binary connection and an ascii connection to the OpenCyc server.  The ascii connection is
 * legacy and its use is deprecated.<p>
 *
 * Collaborates with the <tt>CycAccess</tt> class which wraps the api functions.  CycAccess may be
 * specified as null in the CycConnection constructors when the binary api is used.
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
public class CycConnection implements CycConnectionInterface {

    /**
     * Default host name for the OpenCyc server.
     */
    public static final String DEFAULT_HOSTNAME = "localhost";

    /**
     * Default base tcp port for the OpenCyc server.
     */
    public static final int DEFAULT_BASE_PORT = 3600;

    /**
     * HTTP port offset for the OpenCyc server.
     */
    public static final int HTTP_PORT_OFFSET = 0;

    /**
     * ASCII port offset for the OpenCyc server.
     */
    public static final int ASCII_PORT_OFFSET = 1;

    /**
     * CFASL (binary) port offset for the OpenCyc server.
     */
    public static final int CFASL_PORT_OFFSET = 14;

    /**
     * No api trace.
     */
    public static final int API_TRACE_NONE = 0;

    /**
     * Message-level api trace.
     */
    public static final int API_TRACE_MESSAGES = 1;

    /**
     * Detailed api trace.
     */
    public static final int API_TRACE_DETAILED = 2;

    /**
     * Parameter that, when true, causes a trace of the messages to and from the server.
     */
    protected int trace = API_TRACE_NONE;

    /**
     * Ascii mode connnection to the OpenCyc server.
     */

    public static final int ASCII_MODE = 1;
    /**
     * CFASL (binary) mode connnection to the OpenCyc server.
     */
    public static final int BINARY_MODE = 2;

    /**
     * Default communication mode connnection to the OpenCyc server.
     */
    public static final int DEFAULT_COMMUNICATION_MODE = BINARY_MODE;

    /**
     * Indicator for whether to use the binary or acsii connection with OpenCyc.
     */
    protected int communicationMode = 0;

    /**
     * The ascii interface input stream.
     */
    protected BufferedReader in;

    /**
     * The ascii interface output stream.
     */
    protected BufferedWriter out;

    /**
     * The binary interface input stream</tt>.
     */
    protected CfaslInputStream cfaslInputStream;

    /**
     * The binary interface output stream</tt>.
     */
    protected CfaslOutputStream cfaslOutputStream;

    /**
     * The name of the computer hosting the OpenCyc server.
     */
    protected String hostName;

    /**
     * The tcp port from which the asciiPort and cfaslPorts are derived.
     */
    protected int basePort;

    /**
     * The tcp port assigned to the ascii connection to the OpenCyc server.
     */
    protected int asciiPort;

    /**
     * The tcp port assigned to the binary connection to the OpenCyc server.
     */
    protected int cfaslPort;

    /**
     * The tcp socket assigned to the ascii connection to the OpenCyc server.
     */
    protected Socket asciiSocket;

    /**
     * The tcp socket assigned to the binary connection to the OpenCyc server.
     *
     */
    protected Socket cfaslSocket;

    /**
     * The timer which optionally monitors the duration of requests to the OpenCyc server.
     */
    protected static final Timer notimeout = new Timer();

    /**
     * Indicates if the response from the OpenCyc server is a symbolic expression (enclosed in
     * parentheses).
     */
    protected boolean isSymbolicExpression = false;

    /**
     * A reference to the parent CycAccess object for dereferencing constants in ascii symbolic expressions.
     */
    protected CycAccess cycAccess;

    /**
     * An indicator for ascii communications mode that strings should retain their quote delimiters.
     */
    protected boolean quotedStrings;

    /**
     * Constructs a new CycConnection using the given socket obtained from the parent AgentManager
     * listener.
     *
     * @param cfaslSocket tcp socket which forms the binary connection to the OpenCyc server
     */
    public CycConnection (Socket cfaslSocket) throws IOException {
        this.cfaslSocket = cfaslSocket;
        hostName = cfaslSocket.getInetAddress().getHostName();
        basePort = cfaslSocket.getPort() - CFASL_PORT_OFFSET;
        asciiPort = basePort + ASCII_PORT_OFFSET;
        communicationMode = BINARY_MODE;
        cycAccess = null;
        cfaslInputStream = new CfaslInputStream(cfaslSocket.getInputStream());
        cfaslInputStream.trace = trace;
        cfaslOutputStream = new CfaslOutputStream(cfaslSocket.getOutputStream());
        cfaslOutputStream.trace = trace;
    }

    /**
     * Constructs a new CycConnection object using the default host name, default base port number and
     * binary communication mode.  When CycAccess is null, diagnostic output is reduced.
     */
    public CycConnection ()
    throws IOException, UnknownHostException, CycApiException {
        this(DEFAULT_HOSTNAME, DEFAULT_BASE_PORT, DEFAULT_COMMUNICATION_MODE, null);
    }

    /**
     * Constructs a new CycConnection object using the default host name, default base port number and
     * binary communication mode.
     */
    public CycConnection (CycAccess cycAccess)
    throws IOException, UnknownHostException, CycApiException {
        this(DEFAULT_HOSTNAME, DEFAULT_BASE_PORT, DEFAULT_COMMUNICATION_MODE, cycAccess);
    }

    /**
     * Constructs a new CycConnection object using a given host name, the given base port number, and
     * the given communication mode.
     *
     * @param host the name of the computer hosting the OpenCyc server.
     * @param basePort the base tcp port on which the OpenCyc server is listening for connections.
     * @param communicationMode either ASCII_MODE or BINARY_MODE
     */
    public CycConnection (String hostName, int basePort, int communicationMode, CycAccess cycAccess)
        throws IOException, UnknownHostException, CycApiException {
        this.hostName = hostName;
        this.basePort = basePort;
        asciiPort = basePort + ASCII_PORT_OFFSET;
        cfaslPort = basePort + CFASL_PORT_OFFSET;
        this.communicationMode = communicationMode;
        this.cycAccess = cycAccess;
        if ((communicationMode != ASCII_MODE) && (communicationMode != BINARY_MODE))
            throw  new CycApiException("Invalid communication mode " + communicationMode);
        initializeApiConnections();
        //if (trace >= API_TRACE_NONE) {
        if (trace > API_TRACE_NONE) {
            if (communicationMode == ASCII_MODE)
                System.out.println("Ascii connection " + asciiSocket);
            else
                System.out.println("Binary connection " + cfaslSocket);
        }
    }

    /**
     * Initializes the OpenCyc ascii socket and the OpenCyc binary socket connections.
     */
    private void initializeApiConnections () throws IOException, UnknownHostException {
        if (communicationMode == ASCII_MODE) {
            asciiSocket = new Socket(hostName, asciiPort);
            in = new BufferedReader(new InputStreamReader(asciiSocket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(asciiSocket.getOutputStream()));
        }
        else {
            cfaslSocket = new Socket(hostName, cfaslPort);
            cfaslInputStream = new CfaslInputStream(cfaslSocket.getInputStream());
            cfaslInputStream.trace = trace;
            cfaslOutputStream = new CfaslOutputStream(cfaslSocket.getOutputStream());
            cfaslOutputStream.trace = trace;
        }
    }

    /**
     * Ensures that the api socket connections are closed when this object is garbage collected.
     */
    protected void finalize() {
        close();
    }

    /**
     * Close the api sockets and streams.
     */
    public void close () {
        if (asciiSocket != null) {
            if (out != null) {
                try {
                    out.write("(API-QUIT)\n");
                }
                catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("Error finalizing the api connection " + e.getMessage());
                }
            }
            if (in != null) {
                try {
                    in.close();
                }
                catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("Error finalizing the api connection " + e.getMessage());
                }
            }
            if (asciiSocket != null) {
                try {
                    asciiSocket.close();
                }
                catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("Error finalizing the api connection " + e.getMessage());
                }
            }
        }
        if (cfaslSocket != null) {
            if (cfaslOutputStream != null) {
                CycList command = new CycList();
                command.add(CycObjectFactory.makeCycSymbol("API-QUIT"));
                try {
                    cfaslOutputStream.writeObject(command);
                }
                catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("Error finalizing the api connection " + e.getMessage());
                }
            }
            if (cfaslInputStream != null) {
                try {
                    cfaslInputStream.close();
                }
                catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("Error finalizing the api connection " + e.getMessage());
                }
            }
            if (cfaslSocket != null) {
                try {
                    cfaslSocket.close();
                }
                catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("Error finalizing the api connection " + e.getMessage());
                }
            }
        }
    }

  /**
   * Return the name of the host to which the CycConnection is established.
   * @return the name of the Host to which this <tt>CycConnection</tt> is
   * connected.
   */
  public String getHostName() {return this.hostName;}

  /**
   * Return the base port to which the CycConnection is established.
   * @return the port to which this <tt>CycConnection</tt> is
   * connected.
   */
  public int getBasePort() {return this.basePort;}

  /**
   * Return the ASCII port to which the CycConnection is established.
   * @return the ASCII to which this <tt>CycConnection</tt> is
   * connected.
   */
  public int getAsciiPort() {return this.asciiPort;}

  /**
   * Return the CFASL port to which the CycConnection is established.
   * @return the CFASL port to which this <tt>CycConnection</tt> is
   * connected.
   */
  public int getCfaslPort() {return this.cfaslPort;}

    /**
     * Send a message to Cyc and return the <tt>Boolean</tt> true as the first
     * element of an object array, and the cyc response Symbolic Expression as
     * the second element.  If an error occurs the first element is <tt>Boolean</tt>
     * false and the second element is the error message string.
     *
     * @param message the api command
     * @return an array of two objects, the first is an response status object
     * either a Boolean (binary mode) or Integer (ascii mode), and the second is the
     * response object or error string.
     */
    public synchronized Object[] converse (Object message) throws IOException, CycApiException {
        return  converse(message, notimeout);
    }

    /**
     * Send a message to Cyc and return the response code as the first
     * element of an object array, and the cyc response Symbolic Expression as
     * the second element, spending no less time than the specified timer allows
     * but throwing a <code>TimeOutException</code> at the first opportunity
     * where that time limit is exceeded.
     * If an error occurs the second element is the error message string.
     *
     * @param message the api command which must be a String or a CycList
     * @param timeout a <tt>Timer</tt> object giving the time limit for the api call
     * @return an array of two objects, the first is an response status object
     * either a Boolean (binary mode) or Integer (ascii mode), and the second is the
     * response object or error string.
     */
    public synchronized Object[] converse (Object message, Timer timeout)
        throws IOException, TimeOutException, CycApiException {
        if (communicationMode == CycConnection.ASCII_MODE) {
            String messageString;
            if (message instanceof String)
                messageString = (String) message;
            else if (message instanceof CycList)
                messageString = ((CycList) message).cyclify();
            else
                throw new CycApiException("Invalid class for message " + message);
            return  converseAscii(messageString, timeout);
        }
        else {
            CycList messageCycList;
            if (message instanceof CycList)
                messageCycList = (CycList) message;
            else if (message instanceof String) {
                if (cycAccess == null)
                    throw new RuntimeException("CycAccess is required to process commands in string form");
                messageCycList = cycAccess.makeCycList((String) message);
            }
            else
                throw new CycApiException("Invalid class for message " + message);
            messageCycList = substituteForBackquote(messageCycList, timeout);
            return  converseBinary(messageCycList, timeout);
        }
    }

    /**
     * Substitute a READ-FROM-STRING expression for expressions directly containing a
     * backquote symbol.  This transformation is only required for the binary api,
     * which does not parse the backquoted expression.
     *
     * @param messageCyclist the input expression to be checked for directly containing
     * a backquote symbol.
     * @param timeout a <tt>Timer</tt> object giving the time limit for the api call
     * @return the expression with a READ-FROM-STRING expression substituted for
     * expressions directly containing a backquote symbol
     */
    protected CycList substituteForBackquote(CycList messageCycList, Timer timeout)
        throws IOException, CycApiException {
        if (messageCycList.treeContains(CycObjectFactory.backquote)) {
            CycList substituteCycList = new CycList();
            substituteCycList.add(CycObjectFactory.makeCycSymbol("read-from-string"));
            substituteCycList.add(messageCycList.cyclify());
            Object[] response = converseBinary(substituteCycList, timeout);
            if ((response[0].equals(Boolean.TRUE)) &&
                (response[1] instanceof CycList)) {
                CycList backquoteExpression = (CycList) response[1];
                return backquoteExpression.subst(CycObjectFactory.makeCycSymbol("api-bq-list"),
                                                 CycObjectFactory.makeCycSymbol("bq-list"));
            }
            throw new CycApiException("Invalid backquote substitution in " + messageCycList +
                                      "\nstatus" + response[0] + "\nmessage " + response[1]);

        }
        return messageCycList;
    }

    /**
     * Send a message to Cyc and return the response code as the first
     * element of an object array, and the cyc response Symbolic Expression as
     * the second element, spending no less time than the specified timer allows
     * but throwing a <code>TimeOutException</code> at the first opportunity
     * where that time limit is exceeded.
     * If an error occurs the second element is the error message string.
     *
     * @param message the api command
     * @param timeout a <tt>Timer</tt> object giving the time limit for the api call
     * @return an array of two objects, the first is an Integer response code, and the second is the
     * response object or error string.
     */
    synchronized protected Object[] converseBinary (Object message, Timer timeout)
        throws IOException, TimeOutException, CycApiException {
        sendBinary(message);
        return  receiveBinary();
    }

    /**
     * Sends an object to the CYC server.  If the connection is not already open, it is
     * opened.  The object must be a valid CFASL-translatable: an Integer, Float, Double,
     * Boolean, String, or cyc object.
     *
     * @param message the api command
     */
    public void sendBinary (Object message) throws IOException {
        if (trace > API_TRACE_NONE) {
            if (message instanceof CycList)
                System.out.println(((CycList) message).safeToString() + " --> cyc");
            else if (message instanceof CycFort)
                System.out.println(((CycFort) message).safeToString() + " --> cyc");
            else
                System.out.println(message + " --> cyc");
        }
        cfaslOutputStream.writeObject(message);
        cfaslOutputStream.flush();
    }

    /**
     * Receives an object from the CYC server.
     *
     * @return an array of two objects, the first is a Boolean response, and the second is the
     * response object or error string.
     */
    public Object[] receiveBinary () throws IOException, CycApiException {
        Object[] answer =  {
            null, null
        };
        Object status = cfaslInputStream.readObject();
        Object response = cfaslInputStream.readObject();
        if (status == null ||
            status.equals(CycObjectFactory.nil)) {
            answer[0] = Boolean.FALSE;
            answer[1] = response;
            if (trace > API_TRACE_NONE) {
                String responseString = null;
                if (response instanceof CycList)
                    responseString = ((CycList) response).safeToString();
                else if (response instanceof CycFort)
                    responseString = ((CycFort) response).safeToString();
                else
                    responseString = response.toString();
                System.out.println("received error = (" + status + ") " + responseString);
            }
            return answer;
        }
        answer[0] = Boolean.TRUE;
        if (cycAccess == null)
            answer[1] = response;
        else if (cycAccess.deferObjectCompletion)
            answer[1] = response;
        else
            answer[1] = cycAccess.completeObject(response);
        if (trace > API_TRACE_NONE) {
            String responseString = null;
            if (response instanceof CycList)
                responseString = ((CycList) response).safeToString();
            else if (response instanceof CycFort)
                responseString = ((CycFort) response).safeToString();
            else
                responseString = response.toString();
            System.out.println("cyc --> (" + answer[0] + ") " + responseString);
        }
        return  answer;
    }

    /**
     * Receives a binary (cfasl) api request from a cyc server.  Unlike the api response handled
     * by the receiveBinary method, this method does not expect an input status object.
     *
     * @return the api request expression.
     */
    public CycList receiveBinaryApiRequest () throws IOException, CycApiException {
        CycList apiRequest = (CycList) cfaslInputStream.readObject();
        if (trace > API_TRACE_NONE) {
            System.out.println("cyc --> (api-request) " + apiRequest.safeToString());
        }
        return apiRequest;
    }

    /**
     * Sends a binary (cfasl) api response to a cyc server.  This method prepends a status
     * object (the symbol T) to the message.
     *
     * @param the api response object
     */
    public void sendBinaryApiResponse (Object message) throws IOException, CycApiException {
        if (trace > API_TRACE_NONE) {
            String messageString = null;
            if (message instanceof CycList)
                messageString = ((CycList) message).safeToString();
            else if (message instanceof CycFort)
                messageString = ((CycFort) message).safeToString();
            else
                messageString = message.toString();
            System.out.println("(" + CycObjectFactory.t + ") " + messageString + " --> cyc");
        }
        CycList apiResponse = new CycList();
        apiResponse.add(CycObjectFactory.t);
        apiResponse.add(message);
        cfaslOutputStream.writeObject(apiResponse);
    }

    /**
     * Send a message to Cyc and return the Boolean response as the first
     * element of an object array, and the cyc response Symbolic Expression as
     * the second element, spending no less time than the specified timer allows
     * but throwing a <code>TimeOutException</code> at the first opportunity
     * where that time limit is exceeded.
     * If an error occurs the first element is Boolean.FALSE and the second element
     * is the error message string.
     *
     * @param message the api command
     * @param timeout a <tt>Timer</tt> object giving the time limit for the api call
     * @return an array of two objects, the first is an Integer response code, and the second is the
     * response object or error string.
     */
    synchronized protected Object[] converseAscii (String message, Timer timeout)
    throws IOException, TimeOutException, CycApiException {
        isSymbolicExpression = false;
        Object[] response = converseUsingAsciiStrings(message, timeout);
        if (response[0].equals(Boolean.TRUE)) {
            String answer = ((String)response[1]).trim();
            if (StringUtils.isDelimitedString(answer)) {
                response[1] = StringUtils.removeDelimiters(answer);
                // Return the string.
                return response;
            }
            if (isSymbolicExpression) {
                // Recurse to complete contained CycConstant, CycNart objects.
                if (cycAccess == null)
                    throw new RuntimeException("CycAccess is required to process commands in string form");
                response[1] = CycAccess.current().makeCycList(answer);
                // Return the CycList object.
                return response;
            }
            if (answer.equals("NIL")) {
                response[1] = CycObjectFactory.nil;
                // Return the symbol nil.
                return response;
            }
            if (answer.startsWith("#$")) {
                if (cycAccess == null)
                    throw new RuntimeException("CycAccess is required to process commands in string form");
                response[1] = CycAccess.current().makeCycConstant(answer);
                // Return the constant.
                return response;
            }
            if (answer.startsWith("?")) {
                response[1] = CycObjectFactory.makeCycVariable(answer);
                // Return the variable.
                return response;
            }
            if (StringUtils.isNumeric(answer)) {
                response[1] = new Integer(answer);
                // Return the number.
                return response;
            }
            if (CycSymbol.isValidSymbolName(answer)) {
                response[1] = CycObjectFactory.makeCycSymbol(answer);
                // Return the symbol.
                return response;
            }

            try {
                double doubleAnswer = Double.parseDouble(answer);
                response[1] = new Double(doubleAnswer);
                // Return the double.
                return response;
            }
            catch (NumberFormatException e) {
            }
            if (answer.endsWith("d0") &&
                (ViolinStrings.Strings.indexOfAnyOf(answer.substring(0, answer.length() - 2), "0123456789") > -1)) {
                String floatPart = answer.substring(0, answer.length() - 2);
                response[1] = new Double(floatPart);
                // Return the double.
                return response;
            }
            throw new CycApiException("Ascii api response not understood " + answer);
        }
        else
            return response;
    }

    /**
     * Send a message to Cyc and return the response code as the first
     * element of a object array, and the Cyc response string as the second
     * element.
     */
    protected Object[] converseUsingAsciiStrings (String message, Timer timeout) throws IOException, TimeOutException {
        if (trace > API_TRACE_NONE)
            System.out.println(message + " --> cyc");
        out.write(message);
        if (!message.endsWith("\n"))
            out.newLine();
        out.flush();
        if (trace > API_TRACE_NONE)
            System.out.print("cyc --> ");
        Object[] answer = readAsciiCycResponse(timeout);
        if (trace > API_TRACE_NONE)
            System.out.println();
        return  answer;
    }

    /**
     * Read the cyc response.
     */
    private Object[] readAsciiCycResponse (Timer timeout) throws IOException, TimeOutException {
        Object[] answer =  {
            null, null
        };
        // Parse the response code digits.
        StringBuffer responseCodeDigits = new StringBuffer();
        while (true) {
            timeout.checkForTimeOut();
            int ch = in.read();
            if (trace > API_TRACE_NONE)
                System.out.print((char)ch);
            if (ch == ' ')
                break;
            responseCodeDigits.append((char)ch);
        }
        int responseCode = 0;
        try {
            responseCode = (new Integer(responseCodeDigits.toString().trim())).intValue();
        }
        catch (NumberFormatException e) {
            throw new RuntimeException("Invalid response code digits " + responseCodeDigits);
        }
        if (responseCode == 200)
            answer[0] = Boolean.TRUE;
        else
            answer[0] = Boolean.FALSE;
        in.mark(1);
        int ch = in.read();
        in.reset();
        if (ch == '(') {
            isSymbolicExpression = true;
            answer[1] = readSymbolicExpression();
        }
        else if (ch == '"')
            answer[1] = readQuotedString();
        else
            answer[1] = readAtom();
        // Read the terminating newline.
        ch = in.read();
        if (trace > API_TRACE_NONE)
            System.out.print((char)ch);
        return  answer;
    }

    /**
     * Reads a complete symbolic expression as an ascii string.
     *
     * @return a complete symbolic expression as an ascii string
     */
    private String readSymbolicExpression () throws IOException {
        int parenLevel = 0;
        boolean isQuotedString = false;
        StringBuffer result = new StringBuffer();
        int ch = in.read();
        if (trace > API_TRACE_NONE)
            System.out.print((char)ch);
        parenLevel++;
        result.append((char)ch);
        while (parenLevel != 0) {
            ch = in.read();
            if (trace > API_TRACE_NONE)
                System.out.print((char)ch);
            if (ch == '"')
                if (isQuotedString)
                    isQuotedString = false;
                else
                    isQuotedString = true;
            if (! isQuotedString) {
                if (ch == '(')
                    parenLevel++;
                if (ch == ')')
                    parenLevel--;
            }
            result.append((char) ch);
        }
        return  result.toString();
    }

    /**
     * Reads a quoted string
     *
     * @return the quoted string read
     */
    private String readQuotedString () throws IOException {
        StringBuffer result = new StringBuffer();
        int ch = in.read();
        if (trace > API_TRACE_NONE)
            System.out.print((char)ch);
        boolean escapedChar = false;
        while (true) {
            ch = in.read();
            if (trace > API_TRACE_NONE)
                System.out.print((char)ch);
            if ((ch == '"')  && (! escapedChar))
                return  "\"" + result.toString() + "\"";
            if (escapedChar)
                escapedChar = false;
            else if (ch == '\\')
                escapedChar = true;
            result.append((char)ch);
        }
    }

    /**
     * Reads an atom.
     *
     * @return the atom read as an ascii string
     */
    private String readAtom () throws IOException {
        StringBuffer result = new StringBuffer();
        while (true) {
            in.mark(1);
            int ch = in.read();
            if (trace > API_TRACE_NONE)
                System.out.print((char)ch);
            if (ch == '\r')
                break;
            if (ch == '\n')
                break;
            result.append((char)ch);
        }
        in.reset();
        return  result.toString();
    }

    /**
     * Returns the trace value.
     */
    public int getTrace() {
        return trace;
    }

    /**
     * Sets the trace value.
     * @param trace the trace value
     */
    public void setTrace(int trace) {
        this.trace = trace;
    }

    /**
     * Turns on the diagnostic trace of socket messages.
     */
    public void traceOn() {
        trace = API_TRACE_MESSAGES;
        if (communicationMode == BINARY_MODE) {
            cfaslInputStream.trace = trace;
            cfaslOutputStream.trace = trace;
        }
    }

    /**
     * Turns on the detailed diagnostic trace of socket messages.
     */
    public void traceOnDetailed() {
        trace = API_TRACE_DETAILED;
        if (communicationMode == BINARY_MODE) {
            cfaslInputStream.trace = trace;
            cfaslOutputStream.trace = trace;
        }
    }

    /**
     * Turns off the diagnostic trace of socket messages.
     */
    public void traceOff() {
        trace = API_TRACE_NONE;
        if (communicationMode == BINARY_MODE) {
            cfaslInputStream.trace = trace;
            cfaslOutputStream.trace = trace;
        }
    }

    /**
     * Returns connection information, suitable for diagnostics.
     */
    public String connectionInfo () {
        return "host " + hostName +
               ", asciiPort " + asciiPort +
               ", cfaslPort " + cfaslPort;
    }

}







