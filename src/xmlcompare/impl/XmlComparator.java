/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package xmlcompare.impl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JFrame;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMComment;
import org.apache.axiom.om.OMContainer;
import org.apache.axiom.om.OMDocument;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.OMProcessingInstruction;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import xmlcompare.DiffVisualisation;
import xmlcompare.impl.gui.ViewWithControls;
import xmlcompare.impl.gui.XmlViewerComponent;

/**
 *
 * @author jvanek
 */
public class XmlComparator {

    public static final String VERSION = "3.1";
    protected File f1;
    protected File f2;
    private File schema = null;
    protected BufferedWriter out;
    private boolean ignoreContent = false;
    private int filesCount = 0;
    private String indent = "";
    private static final String OK = "OK     : ";
    private static final String I = "INFO   : ";
    private static final String W = "WARNING: ";
    private static final String E = "ERROR  : ";
    private int errors = 0;
    private boolean failFast = false;
    private boolean silent = false;
    private int ignoreOrder = 0;
    private static final int KEEP_ORDER = 0;
    private static final int SORT_ORDER = 1;
    private static final int MATCH_ORDER = 2;
    private Double levenstain;
    protected boolean ignoreUri = false;
    private boolean forceTrim = false;
    private boolean ignoreNameCase = false;
    private boolean ignoreValueCase = false;
    private boolean removeSpaces = false;
    private boolean ignoreHeaders = false;
    private boolean visualise = false;
    StAXOMBuilder builder1;
    StAXOMBuilder builder2;
    private boolean dual = false;
    private boolean orphanSeek = false;
    private File diff = null;

    public void setVisualise(boolean visualise) {
        this.visualise = visualise;
    }

    public void setLevenstain(Double levenstain) {
        this.levenstain = levenstain;
    }

    public void setIgnoreNameCase(boolean ignoreNameCase) {
        this.ignoreNameCase = ignoreNameCase;
    }

    public void setIgnoreUri(boolean ignoreUri) {
        this.ignoreUri = ignoreUri;
    }

    public void setForceTrim(boolean forceTrim) {
        this.forceTrim = forceTrim;
    }

    public void setIgnoreValueCase(boolean ignoreValueCase) {
        this.ignoreValueCase = ignoreValueCase;
    }

    public void setRemoveSpaces(boolean removeSapces) {
        this.removeSpaces = removeSapces;
    }

    public void deindentize() {
        if (indent.length() > 4) {
            indent = indent.substring(4);
        }
    }

    public void indentize() {
        indent += "    ";
    }

    public void setSchmea(File schmea) {
        this.schema = schmea;
    }

    public File getSchmea() {
        return schema;
    }

    public File getF1() {
        return f1;
    }

    public File getF2() {
        return f2;
    }

    public XmlComparator() {
    }

    public boolean compare() throws IOException, XMLStreamException {

        return comapre(System.out);
    }

    public BruteForceResult recompare() throws InterruptedException, FileNotFoundException, IllegalStateException, XMLStreamException {
        errors = 0;
        output("-_-_-_recomparing_-_-_-");
        BruteForceResult br = compareStaxomBuilders(builder1, builder2);
        // riseViewDialog(br);
        return br;
    }

    private BruteForceResult compareStaxomBuilders(StAXOMBuilder builder1, StAXOMBuilder builder2) {
        BruteForceResult result = new BruteForceResult();
        if (!ignoreHeaders) {
            result.h1 = checkHeaders(builder1.getDocument(), builder2.getDocument());
        }
        result.br1 = compareRoots(builder1.getDocumentElement(), builder2.getDocumentElement());

        if (dual) {
            output("++++ reverse order comparsion ++++");
            if (!ignoreHeaders) {
                result.h2 = checkHeaders(builder2.getDocument(), builder1.getDocument());
            }
            result.br2 = compareRoots(builder2.getDocumentElement(), builder1.getDocumentElement());
        }
        return result;

    }

    private BruteForceResultKeeper compareRoots(OMElement root1, OMElement root2) {
        output("Entering root");
        final BruteForceResultKeeper currentNode = new BruteForceResultKeeper(root1);
        if (ignoreOrder == MATCH_ORDER) {

            boolean q = mohamadMatch(root1, root2, currentNode);
            //                if (!q) {
            //                    pe();
            //                }
            indent = "";
            output("-------bruteforce summary--------");
            traverseBruteResults(currentNode);
            return currentNode;
        } else {
            checkElements(root1, root2, currentNode);
            return currentNode;
        }
    }

    private boolean comapre(OutputStream out) throws IOException, XMLStreamException {
        OutputStreamWriter o = new OutputStreamWriter(out, "utf-8");
        try {
            return compare(o);
        } finally {
            if (out != System.out) {
                out.flush();
                //  o.close();
            }
        }
    }

    private boolean compare(OutputStreamWriter outputStreamWriter) throws IOException, XMLStreamException {
        BufferedWriter br = new BufferedWriter(outputStreamWriter);
        try {
            return comapre(br);
        } finally {
            // br.close();
            out.flush();
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        out.close();
    }

    private boolean comapre(BufferedWriter out) throws XMLStreamException, FileNotFoundException, IOException {
        this.out = out;
        //try {

        if (schema != null) {
            validateAndcompareResults(f1, f2);
        }

        //get doc1
        output("opening parser for: " + f1.getAbsolutePath());
        this.out = out;
        XMLStreamReader parser1 = XMLInputFactory.newInstance().createXMLStreamReader(new FileInputStream(f1));
        builder1 = new StAXOMBuilder(parser1);

        output("opening parser for: " + f2.getAbsolutePath());
        //get doc2
        XMLStreamReader parser2 = XMLInputFactory.newInstance().createXMLStreamReader(new FileInputStream(f2));
        builder2 = new StAXOMBuilder(parser2);
        final BruteForceResult br = compareStaxomBuilders(builder1, builder2);
        if (diff != null) {
            saveDiff(br, diff, builder1, builder2);
        }
        if (visualise) {
            try {
                riseViewDialog(br);
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        }
        //} finally {
        if (silent) {
            if (getErrors() == 0) {
                out.write("XMLs are same");
                return true;
            } else {
                out.write("Total errors: " + getErrors());
                return false;
            }
        } else {
            if (getErrors() == 0) {
                output("XMLs are same");
                return true;
            } else {
                output("Total errors: " + getErrors());
                return false;
            }
        }
        //}
    }

    public static boolean haveTrueOne(BruteForceResultKeeper b, int i, BruteForceResultKeeper currentNode) {
        boolean haveTrueOne = false;
        if (!b.result.reason.equals(ComparsionResult.Reasons.OK)) {
            for (int ii = i + 1; ii < currentNode.childs.size(); ii++) {
                BruteForceResultKeeper bb = currentNode.childs.get(ii);
                if (bb.node.equals(b.node) && bb.result.reason.equals(ComparsionResult.Reasons.OK)) {
                    haveTrueOne = true;
                    break;
                }
            }
        }
        return haveTrueOne;
    }

    private void riseViewDialog(final BruteForceResult br) throws InterruptedException, FileNotFoundException, IllegalStateException, XMLStreamException {
        final XmlViewerComponent xv1 = new XmlViewerComponent(builder1);
        final XmlViewerComponent xv2 = new XmlViewerComponent(builder2);

        final DiffVisualisation df = new DiffVisualisation();
        if (f1 != null && f2 == null) {
            df.setTitle(f1.getName() + " view");
        } else if (f2 != null && f1 == null) {
            df.setTitle(f2.getName() + " view");
        } else {
            if (dual) {
                df.setTitle(f1.getName() + " x " + f2.getName() + " comparsion");
            } else {
                df.setTitle(f1.getName() + " wiewed with diffs against " + f2.getName());
            }
        }
        final ViewWithControls vwc1 = new ViewWithControls(0);
        final ViewWithControls vwc2 = new ViewWithControls(1);

        vwc1.setXmlComparator(this);
        vwc2.setXmlComparator(this);

        df.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                try {
                    vwc1.setXmlViewerComponent(xv1);
                    df.setLeftComponent(vwc1);
                    if (br.br2 != null) {
                        vwc2.setXmlViewerComponent(xv2);
                        df.setRightComponent(vwc2);
                        vwc1.setSister(vwc2);
                        vwc2.setSister(vwc1);
                    }
                    df.setVisible(true);
                } catch (Exception ex) {
                    throw new IllegalStateException(ex);
                }
            }
        });
        Thread.sleep(500);
        vwc1.prepareSpinner();
        xv1.applyResults(br, 0);
        if (br.br2 != null) {
            vwc2.prepareSpinner();
            xv2.applyResults(br, 1);
        }
        df.repaint();
    }

    private Boolean validateAndcompareResults(File file1, File file2) {
        Boolean result = null;
        output("validating f1 - " + file1.getAbsolutePath() + " against " + schema.getAbsolutePath());
        ChoosingValidator v = new ChoosingValidator(file1, schema);
        boolean passed1 = v.validate();
        if (passed1) {
            output("...valid");
        } else {
            output("...invalid");
        }
        output("validating f2 - " + file2.getAbsolutePath() + " against " + schema.getAbsolutePath());
        v = new ChoosingValidator(file2, schema);
        boolean passed2 = v.validate();
        if (passed2) {
            output("...valid");
        } else {
            output("...invalid");
        }
        if (passed1 != passed2) {
            output(E + "one is valid, one invalid");
            result = false;
            pe();
        } else {
            if (passed1) {
                output(OK + "both are valid");
                result = true;
            } else {
                output(W + "both are invalid");
                result = null;
            }
        }
        return result;
    }

    protected void output(String string) {
        try {
            if (!silent) {
                out.write(string);
                out.newLine();
                out.flush();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private HeadersCheckResultKeeper checkHeaders(OMDocument d1, OMDocument d2) {
        HeadersCheckResultKeeper hr = new HeadersCheckResultKeeper();
        output("Checking version: " + d1.getXMLVersion() + " against " + d2.getXMLVersion());
        if (d1.getXMLVersion() == null && d2.getXMLVersion() == null) {
            String s = OK + " both xml versions are null";
            hr.encoding = new HeaderResult(HeadersCheckResultKeeper.State.SAME, s);
            output(s);
        } else if (d1.getXMLVersion() == null && d2.getXMLVersion() != null) {
            String s = W + " xml versions for f1 is null, wheather for f2 is " + d2.getXMLVersion();
            hr.encoding = new HeaderResult(HeadersCheckResultKeeper.State.NULL_VALUE, s);
            output(s);
        } else if (d1.getXMLVersion() != null && d2.getXMLVersion() == null) {
            String s = W + " xml versions for f2 is null, wheather for f2 is " + d1.getXMLVersion();
            hr.encoding = new HeaderResult(HeadersCheckResultKeeper.State.VALUE_NULL, s);
            output(s);
        } else if (compareSValues(d1.getXMLVersion(), d2.getXMLVersion())) {
            String s = OK + " xml versions  are same " + d1.getXMLVersion();
            hr.encoding = new HeaderResult(HeadersCheckResultKeeper.State.SAME, s);
            output(s);
        } else {
            String s = W + " wersions differs (" + d1.getXMLVersion() + " x " + d2.getXMLVersion();
            hr.encoding = new HeaderResult(HeadersCheckResultKeeper.State.DIFF, s);
            output(s);
        }
        output("Checking encoding: " + d1.getCharsetEncoding() + " against " + d2.getCharsetEncoding());
        if (compareSValues(d1.getCharsetEncoding(), d2.getCharsetEncoding())) {
            String s = OK + " encodings equals - " + d1.getCharsetEncoding();
            hr.version = new HeaderResult(HeadersCheckResultKeeper.State.SAME, s);
            output(s);
        } else {
            String s = W + " encodings differs - " + d1.getCharsetEncoding() + " x " + d2.getCharsetEncoding();
            hr.version = new HeaderResult(HeadersCheckResultKeeper.State.DIFF, s);
            output(s);
        }
        output("Checking processing instructions");
        List<OMProcessingInstruction> pi1 = listOfProcessingInstructions(d1);
        List<OMProcessingInstruction> pi2 = listOfProcessingInstructions(d2);
        if (pi1.size() == pi2.size()) {
            output(OK + " number of processing instructions equals " + pi1.size());
        } else {
            output(E + " number of processing instructions differs " + pi1.size() + " x " + pi2.size());
            pe();
        }

        hr.pir = comapreListsOfProcessingInstructions(pi1, pi2);
        return hr;

    }

    private void checkElements(OMElement e1, OMElement e2, BruteForceResultKeeper bf) {
        indentize();
        output(indent + "*Checking element: " + e1.getQName().toString() + " against " + e2.getQName().toString());
        bf.setResult(new ComparsionResult(ComparsionResult.Reasons.OK, e1.getQName().toString(), e2.getQName().toString(), e1, e2));
        if (compareElementsNames(e1.getQName(), e2.getQName())) {
            output(indent + OK + "names equals (" + e1.getQName().toString() + ")");
        } else {
            output(indent + E + "names differs: f1 line " + e1.getLineNumber() + ", f2 line " + e2.getLineNumber());
            bf.setResult(new ComparsionResult(ComparsionResult.Reasons.EL_NAME, e1.getQName().toString(), e2.getQName().toString(), e1, e2));
            pe();
        }
        output(indent + "Checking  attributes");
        List<OMAttribute> as1 = listOfAttributes(e1);
        List<OMAttribute> as2 = listOfAttributes(e2);
        if (as1.size() == as2.size()) {
            output(indent + OK + " number of attributes equals " + as1.size());

        } else {
            output(indent + E + " number of attributes differs " + as1.size() + " x " + as2.size());
            bf.setResult(new ComparsionResult(ComparsionResult.Reasons.AT_COUNT, String.valueOf(as1.size()), String.valueOf(as2.size()), e1, e2));
            pe();
        }
        Collections.sort(as1, new AttributeComparator());
        Collections.sort(as2, new AttributeComparator());
        comapreListsOfSortedAttrributes(as1, as2, bf);

        output(indent + "Checking  childrens");
        List<OMElement> pi1 = listOfChildrenWithoutComments(e1);
        List<OMElement> pi2 = listOfChildrenWithoutComments(e2);
        if (pi1.size() == pi2.size()) {
            output(indent + OK + " number of childrens  equals " + pi1.size());
        } else {
            output(indent + E + " number of childrens  differs " + pi1.size() + " x " + pi2.size());
            bf.setResult(new ComparsionResult(ComparsionResult.Reasons.EL_CHILD_COUNT, String.valueOf(pi1.size()), String.valueOf(pi2.size()), e1, e2));
            pe();
        }

        if (!ignoreContent && pi1.size() == 0 && pi2.size() == 0) {
            output(indent + "Checking  content");
            if (compareElementValues(e1.getText(), e2.getText())) {
                output(indent + OK + "content equals - lines f1: " + e1.getLineNumber() + ", f2: " + e2.getLineNumber() + " : " + e1.getText());
            } else {
                output(indent + E + "content differs - lines f1: " + e1.getLineNumber() + " : " + e1.getText() + ", f2: " + e2.getLineNumber() + " : " + e2.getText());
                bf.setResult(new ComparsionResult(ComparsionResult.Reasons.EL_VALUE, e1.getText(), e2.getText(), e1, e2));
                pe();
            }
        }

        if (ignoreOrder == SORT_ORDER) {
            comapreListsOfSortedChildren(pi1, pi2, bf);
        } else if (ignoreOrder == KEEP_ORDER) {
            comapreListsOfUnsortedChildren(pi1, pi2, bf);
        } else if (ignoreOrder == MATCH_ORDER) {
            // no op here, different access by mohamadMatch(pi1,pi2);
        } else {
            output("unknown order state - " + ignoreOrder);
        }

        deindentize();
        output(indent + "leaving element: " + e1.getQName().toString() + " and " + e2.getQName().toString());

    }

    public static List<OMProcessingInstruction> listOfProcessingInstructions(OMDocument d1) {
        Iterator children = d1.getChildren();
        List<OMProcessingInstruction> r = new LinkedList();
        while (children.hasNext()) {
            OMNode node = (OMNode) children.next();

            if (node instanceof OMProcessingInstruction) {
                r.add((OMProcessingInstruction) node);
            }
        }

        return r;
    }

    private List<ProcessingInstructionsResult> comapreListsOfSortedProcessingInstructions(List<OMProcessingInstruction> pi1, List<OMProcessingInstruction> pi2) {
        List<ProcessingInstructionsResult> r = new ArrayList(pi1.size());
        Collections.sort(pi1, new ProcessingInstructionComparator());
        Collections.sort(pi2, new ProcessingInstructionComparator());
        for (int i = 0; i < Math.min(pi1.size(), pi2.size()); i++) {
            OMProcessingInstruction p1 = pi1.get(i);
            OMProcessingInstruction p2 = pi2.get(i);
            if (compareSValues(p1.getTarget(), p2.getTarget()) && compareSValues(p1.getValue(), p2.getValue())) {
                output(indent + OK + "instructions " + p1.getTarget() + "=" + p1.getValue() + " are similar");
            } else {
                output(indent + E + "instructions " + p1.getTarget() + "=" + p1.getValue() + " x " + p2.getTarget() + "=" + p2.getValue() + " differs. ");
                pe();

            }

        }
        return r;
    }

    private List<ProcessingInstructionsResult> comapreListsOfProcessingInstructions(List<OMProcessingInstruction> pi1, List<OMProcessingInstruction> pi2) {
        List<ProcessingInstructionsResult> r = new ArrayList(pi1.size());
        for (int i = 0; i < pi1.size(); i++) {
            for (int j = 0; j < pi2.size(); j++) {

                OMProcessingInstruction p1 = pi1.get(i);
                OMProcessingInstruction p2 = pi2.get(j);
                if (compareSValues(p1.getTarget(), p2.getTarget()) && compareSValues(p1.getValue(), p2.getValue())) {
                    String s = OK + "instructions " + p1.getTarget() + " " + p1.getValue() + " have match";
                    output(indent + s);
                    r.add(new ProcessingInstructionsResult(p1, p2, s, ProcessingInstructionsResult.State.OK));

                    pi1.remove(i);
                    pi2.remove(j);
                    i--;
                    break;

                } else {
                    if (j == pi2.size() - 1) {
                        String s = E + "instructions " + p1.getTarget() + " " + p1.getValue() + " have no match";
                        r.add(new ProcessingInstructionsResult(p1, null, s, ProcessingInstructionsResult.State.DIFF));
                        output(indent + s);
                        pe();
                    }
                }
            }
        }
        if (pi1.size() == 0 && pi2.size() == 0) {

            String s = OK + " all processing instructins match";
            output(indent + s);
            //return true;
        } else if (pi1.size() > 0) {
            // some elemets have no match!
            String s = " some processing instructions have no match(" + pi1.size() + "), eg: " + pi1.get(0).getTarget() + " " + pi1.get(0).getValue();
            output(indent + E + s);
            r.add(new ProcessingInstructionsResult(pi1.get(0), null, s, ProcessingInstructionsResult.State.REMAINS));
            pe();
            //return false;
        } else {
            //else there are redundant elements in second file
            String s = " some processing instructions are missing(" + pi2.size() + "), eg:" + pi2.get(0).getTarget() + " " + pi2.get(0).getValue();
            output(indent + E + s);
            r.add(new ProcessingInstructionsResult(null, pi2.get(0), s, ProcessingInstructionsResult.State.NONE_LEFT));
            pe();
            //return false;
        }

        return r;
    }

    public void setIgnoreContent(boolean b) {
        this.ignoreContent = b;
    }

    public void setIgnoreOrder(int b) {
        this.ignoreOrder = b;
    }

    public void addInput(File f) throws FileNotFoundException {
        filesCount++;
        if (filesCount == 1) {
            if (!f.exists()) {
                throw new FileNotFoundException(f.getAbsolutePath() + " dont exists");
            }
            this.f1 = f;
        } else if (filesCount == 2) {

            if (!f.exists()) {
                throw new FileNotFoundException(f.getAbsolutePath() + " dont exists");
            }

            this.f2 = f;
        } else {
            throw new IllegalArgumentException("exactly two of files expected!");
        }
    }

    public void sumarize() {
        System.out.println("files count: " + filesCount);
        if (f1 != null) {
            System.out.println("f1: " + f1.getAbsolutePath());
        }
        if (f2 != null) {
            System.out.println("f2: " + f2.getAbsolutePath());
        }

        if (diff != null) {
            System.out.println("compressed diff will be written into: " + diff.getAbsolutePath());
        }
        if (ignoreOrder == KEEP_ORDER) {
            System.out.println("order is important");
        } else if (ignoreOrder == SORT_ORDER) {
            System.out.println("order is ignored - sorting");
        } else if (ignoreOrder == MATCH_ORDER) {
            System.out.println("order is ignored - matching");
        } else {
            System.out.println("order is ignoring is set WRONGLY. You will fail");
        }
        System.out.println("ignoreContent: " + ignoreContent);
        System.out.println("failFast: " + failFast);
        System.out.println("silent: " + silent);
        System.out.println("ignoreUri: " + ignoreUri);
        System.out.println("forceTrim: " + forceTrim);
        System.out.println("ignoreValueCase: " + ignoreValueCase);
        System.out.println("ignoreNameCase: " + ignoreNameCase);
        System.out.println("removeSpaces: " + removeSpaces);
        System.out.println("ignoreHeaders: " + ignoreHeaders);
        System.out.println("dual: " + dual);
        System.out.println("orphanSeek: " + orphanSeek);
        System.out.println("visualise: " + visualise);
        if (levenstain == null) {
            System.out.println("levenstain is off");
        } else {
            System.out.println("levenstain is " + levenstain.toString());
        }

        if (schema == null) {
            System.out.println("validation is off");
        } else {
            System.out.println("validation against " + schema.getAbsolutePath());
            if (schema.exists()) {
                System.out.println("...exists");
            } else {
                throw new IllegalStateException("..dont exists");
            }
            if (!schema.isDirectory()) {
                System.out.println("...is file");
            } else {
                throw new IllegalStateException("..is directory");
            }
        }

    }

    private void comapreListsOfSortedAttrributes(List<OMAttribute> as1, List<OMAttribute> as2, BruteForceResultKeeper bf) {

        for (int i = 0; i < Math.min(as1.size(), as2.size()); i++) {
            OMAttribute p1 = as1.get(i);
            OMAttribute p2 = as2.get(i);
            if (ignoreContent) {
                if (compareAttributeNames(p1.getQName(), p2.getQName())) {
                    output(indent + OK + "attribute " + p1.getQName().toString() + " - accoures in both");
                } else {
                    output(indent + E + "attribute " + p1.getQName().toString() + " mishmash");
                    bf.setResult(new ComparsionResult(ComparsionResult.Reasons.AT_NAME, p1.getQName().toString(), p2.getQName().toString(), p1.getOwner(), p2.getOwner()));
                    pe();

                }
            } else {
                if (compareAttributeNames(p1.getQName(), p2.getQName()) && compareAttributeValues(p1.getAttributeValue(), p2.getAttributeValue())) {
                    output(indent + OK + "attribute " + p1.getQName().toString() + " - " + p1.getAttributeValue() + " are similar");
                } else {
                    output(indent + E + "attribute " + p1.getQName().toString() + " - " + p1.getAttributeValue() + " x " + p2.getQName().toString() + " - " + p2.getAttributeValue() + " differs");
                    bf.setResult(new ComparsionResult(ComparsionResult.Reasons.AT_VALUE, p1.getQName().toString() + "=" + p1.getAttributeValue(), p2.getQName().toString() + "=" + p2.getAttributeValue(), p1.getOwner(), p2.getOwner()));
                    pe();

                }
            }

        }
    }

    public static List<OMAttribute> listOfAttributes(OMElement e1) {
        Iterator children = e1.getAllAttributes();
        List<OMAttribute> r = new LinkedList();
        while (children.hasNext()) {
            OMAttribute node = (OMAttribute) children.next();

            if (node instanceof OMAttribute) {
                r.add((OMAttribute) node);
            }
        }

        return r;
    }

    protected List<OMElement> listOfChildrenWithoutComments(OMElement e1) {
        return listOfChildrenWithoutComments(e1, this);
    }

    public static List<OMElement> listOfChildrenWithoutComments(OMElement e1, XmlComparator c) {
        Iterator children = e1.getChildElements();
        List<OMElement> r = new LinkedList();
        while (children.hasNext()) {
            OMElement node = (OMElement) children.next();
            if (node instanceof OMComment) {
                if (c != null) {
                    c.output(c.indent + I + " ignored comment on line " + node.getLineNumber() + " of " + node.getText());
                }
            } else if (node instanceof OMElement) {
                r.add((OMElement) node);
            }
        }
        return r;
    }

    private void comapreListsOfUnsortedChildren(List<OMElement> pi1, List<OMElement> pi2, BruteForceResultKeeper bf) {
        for (int i = 0; i < Math.min(pi1.size(), pi2.size()); i++) {
            OMElement e1 = pi1.get(i);
            OMElement e2 = pi2.get(i);
            BruteForceResultKeeper current = new BruteForceResultKeeper(e1);
            bf.addChild(current);
            checkElements(e1, e2, current);

        }
    }

    private void comapreListsOfSortedChildren(List<OMElement> pi1, List<OMElement> pi2, BruteForceResultKeeper bf) {
        AlphaAndChildCountComparator aac = null;
        //if (ignoreContent) aac=new AlphaAndChildCountComparator(AlphaAndChildCountComparator.ContentComapre.NONE);
        //else aac=new AlphaAndChildCountComparator(AlphaAndChildCountComparator.ContentComapre.JUST_CHILD_LESS);
        aac = new AlphaAndChildCountComparator(AlphaAndChildCountComparator.ContentComapre.WHOLE);
        Collections.sort(pi1, aac);
        Collections.sort(pi2, aac);
        for (int i = 0; i < Math.min(pi1.size(), pi2.size()); i++) {
            OMElement e1 = pi1.get(i);
            OMElement e2 = pi2.get(i);
            BruteForceResultKeeper current = new BruteForceResultKeeper(e1);
            bf.addChild(current);
            checkElements(e1, e2, current);

        }
    }

    private void pe() {
        errors++;
        if (failFast) {
            throw new Error("xmls are not same. End");
        }
    }

    public int getErrors() {
        return errors;
    }

    public void setFailFast(boolean failFast) {
        this.failFast = failFast;
    }

    public void setSilent(boolean b) {
        this.silent = b;
    }

    private String buildXpathLocation(OMElement p1) {
        StringBuilder sb = new StringBuilder(p1.getQName().toString());
        while (p1.getParent() != null) {
            if (p1.getParent() instanceof OMElement) {
                p1 = (OMElement) p1.getParent();
                sb.insert(0, p1.getQName() + "/");
            } else {
                break;
            }
        }
        return sb.toString();

    }

    private boolean mohamadMatch(final OMElement pi1, final OMElement pi2, final BruteForceResultKeeper currentNode) {
        try {
            indentize();

            if (pi1 != null && pi2 != null) {
                // output(indent + " checking " + pi1.getQName().toString() + " against " + pi2.getQName().toString());

                List<OMElement> p1Childs = listOfChildrenWithoutComments(pi1);
                List<OMElement> p2Childs = listOfChildrenWithoutComments(pi2);

                // Compare only if both Nodes have the same name, else return false
                if (compareElementsNames(pi1.getQName(), pi2.getQName())) {

                    //we wan to know more if (p1Childs.size() == p2Childs.size()) { // Compare children
                    for (int i = 0; i < p1Childs.size(); i++) {
                        BruteForceResultKeeper chb = new BruteForceResultKeeper(p1Childs.get(i));
                        currentNode.addChild(chb);
                        for (int j = 0; j < p2Childs.size(); j++) {

                            boolean isSame = mohamadMatch(p1Childs.get(i), p2Childs.get(j), chb);

                            if (!isSame/* && j == childLength - 1*/) {
//                                    currentNode.setResult(new ComparsionResult(ComparsionResult.Reasons.Child_Fails, pi1.getQName().toString(), pi2.getQName().toString(), pi1, pi2));
//                                    return false;
                            } else if (isSame) { // If both child matches, remove them from the list of children
                                p1Childs.remove(i);
                                p2Childs.remove(j);
                                i--;
                                break;
                            }
                        }
                    }

                    if (p1Childs.size() == 0 && p2Childs.size() == 0) {
                        {
                            boolean mat = matchAttributes(pi1, pi2, currentNode);
                            if (mat) {
                                if (ignoreContent) {
                                    currentNode.setResult(new ComparsionResult(ComparsionResult.Reasons.OK, "name and descs and atts based match", null, pi1, pi2));
                                    return true;
                                }
                                boolean cev = compareElementValues(pi1.getText(), pi2.getText());
                                if (!cev) {
                                    currentNode.setResult(new ComparsionResult(ComparsionResult.Reasons.EL_VALUE, pi1.getQName() + ":" + pi1.getText(), pi2.getQName() + ":" + pi2.getText(), pi1, pi2));
                                    return false;
                                } else {
                                    currentNode.setResult(new ComparsionResult(ComparsionResult.Reasons.OK, "name, descs, value ats based match", null, pi1, pi2));
                                    return true;
                                }
                            } else {
                                //error should be set from atts
                                return false;
                            }

                        }

                    } else if (p1Childs.size() > 0) {
                        // some elemets have no match!
                        currentNode.setResult(new ComparsionResult(ComparsionResult.Reasons.Child_Fails, "eg: " + p1Childs.get(0).getLocalName(), "", pi1, null));
                        return false;
                    } else {
                        //else there are redundant elements in second file
                        currentNode.setResult(new ComparsionResult(ComparsionResult.Reasons.Child_Fails, "unmatched elements left", "", pi1, null));
                        return false;
                    }

                } else {
                    if (orphanSeek) {
                        OMContainer par = pi2.getParent();
                        if (par instanceof OMElement) {
                            List<OMElement> nextDocSiblinks = listOfChildrenWithoutComments((OMElement) par, this);
                            if (nextDocSiblinks.size() < 2) {
                                currentNode.setResult(new ComparsionResult(ComparsionResult.Reasons.EL_NAME, pi1.getQName().toString(), pi2.getQName().toString(), pi1, pi2));
                                return false;
                            }
                            Collections.sort(nextDocSiblinks, new LevenstainComparator(pi1));
                            OMElement winner = nextDocSiblinks.get(0);
                            long match = normalizeLevenstainValueOnElement(pi1, winner);
                            long match2 = normalizeLevenstainValueOnElement(pi1, pi2);
                            if (match2 < match) {
                                winner = pi2;
                            }
                            ComparsionResult.Reasons res = ComparsionResult.Reasons.EL_NAME;
                            if (pi1.getQName().getLocalPart().equals(winner.getQName().getLocalPart())) {
                                res = ComparsionResult.Reasons.EL_VALUE;
                            }
                            currentNode.setResult(new ComparsionResult(res, pi1.getQName().toString(), winner.getQName().toString() + " matching by " + match, pi1, winner));
                            return false;
                        } else {
                            currentNode.setResult(new ComparsionResult(ComparsionResult.Reasons.EL_NAME, pi1.getQName().toString(), pi2.getQName().toString(), pi1, pi2));
                            return false;
                        }
                    } else {
                        currentNode.setResult(new ComparsionResult(ComparsionResult.Reasons.EL_NAME, pi1.getQName().toString(), pi2.getQName().toString(), pi1, pi2));
                        return false;
                    }
                }
            } else {
                if (pi1 != null) {
                    currentNode.setResult(new ComparsionResult(ComparsionResult.Reasons.NULL, pi1.getQName().toString(), null, pi1, null)); //should not occure
                } else if (pi2 != null) {
                    currentNode.setResult(new ComparsionResult(ComparsionResult.Reasons.NULL, null, pi2.getQName().toString(), null, pi2)); //should not occure
                } else {
                    currentNode.setResult(new ComparsionResult(ComparsionResult.Reasons.NULL, null, null, null, null)); //should not occure
                }
            }
            return false;
        } finally {
            deindentize();
        }
    }

    private boolean matchAttributesSort(OMElement e1, OMElement e2, BruteForceResultKeeper b) {

        if (e1 != null && e2 != null) {

            List<OMAttribute> appTemplateAttributes = listOfAttributes(e1);
            List<OMAttribute> launchJNLPAttributes = listOfAttributes(e2);

            if (appTemplateAttributes.size() == launchJNLPAttributes.size()) { //we would like to see which one is missing, but it can cause failures

                Collections.sort(appTemplateAttributes, new AttributeComparator());
                Collections.sort(launchJNLPAttributes, new AttributeComparator());

                int size = Math.min(launchJNLPAttributes.size(), appTemplateAttributes.size()); //Number of attributes

                for (int i = 0; i < size; i++) {
                    OMAttribute a1 = appTemplateAttributes.get(i);
                    OMAttribute a2 = launchJNLPAttributes.get(i);

                    if (compareAttributeNames(a1.getQName(), a2.getQName())) { // If both Node's attribute name are the same then compare the values

                        // Check if the Attribute values match
                        boolean isSame = true;
                        if (!ignoreContent) {
                            isSame = compareAttributeValues(
                                    a1.getAttributeValue(), a2.getAttributeValue());
                        }
                        if (!isSame) {
                            b.setResult(new ComparsionResult(ComparsionResult.Reasons.AT_VALUE, e1.getQName().toString() + ":" + a1.getQName().toString() + ":" + a1.getAttributeValue(), e2.getQName().toString() + ":" + a2.getQName().toString() + ":" + a2.getAttributeValue(), e1, e2));
                            return false;
                        }
                        //return isSame;

                    } else // If attributes names do not match, return false
                    {
                        b.setResult(new ComparsionResult(ComparsionResult.Reasons.AT_NAME, e1.getQName().toString() + ":" + a1.getQName().toString(), e2.getQName().toString() + ":" + a2.getQName().toString(), e1, e2));
                        return false;
                    }
                }
                return true;
            } else {
                b.setResult(new ComparsionResult(ComparsionResult.Reasons.AT_COUNT, e1.getQName() + ":" + appTemplateAttributes.size(), e2.getQName() + ":" + launchJNLPAttributes.size(), e1, e2));
            }
        }
        return false;
    }

    /**
     * Compares attributes of two Nodes regardless of order
     *
     * @param appTemplateNode signed application or template's Node with
     * attributes
     * @param launchJNLPNode launching JNLP file's Node with attributes
     *
     * @return true if both Nodes have 'matched' attributes, otherwise false
     */
    private boolean matchAttributes(OMElement e1, OMElement e2, BruteForceResultKeeper b) {

        if (e1 != null && e2 != null) {

            List<OMAttribute> e1a = listOfAttributes(e1);
            List<OMAttribute> e2a = listOfAttributes(e2);

            // if (e1a.size() == e2a.size()) { we want to find bad ones
            for (int i = 0; i < e2a.size(); i++) {
                for (int j = 0; j < e1a.size(); j++) {

                    OMAttribute a2 = e2a.get(i);
                    OMAttribute a1 = e1a.get(j);

                    if (compareAttributeNames(a1.getQName(), a2.getQName())) { // If both Node's attribute name are the same then compare the values

                        boolean isSame = true;
                        if (!ignoreContent) {
                            isSame = compareAttributeValues(a1.getAttributeValue(), a2.getAttributeValue());
                        }

                        if (isSame) {
                            e2a.remove(i);
                            e1a.remove(j);
                            i--;
                            break; //Break for-loop
                        }

                    }
                }

            }
            //analyze result
            if (e1a.size() == 0 && e2a.size() == 0) {
                return true;
            } else if (e1a.size() > 0) {
                // some elemets have no match!
                b.setResult(new ComparsionResult(ComparsionResult.Reasons.AT_MATCH, "eg: " + e1a.get(0).getLocalName() + "=" + e1a.get(0).getAttributeValue(), "", e1, e2));
                b.result.guiltyAtts1 = e1a;
                b.result.guiltyAtts2 = e2a;
                return false;
            } else {
                //else there are redundant elements in second file
                b.setResult(new ComparsionResult(ComparsionResult.Reasons.AT_MATCH, "unmatched elements left", "", e1, e2));
                b.result.guiltyAtts1 = e1a;
                b.result.guiltyAtts2 = e2a;
                return false;
            }

            //  }else {
            //  b.setResult(new ComparsionResult(ComparsionResult.Reasons.AT_COUNT, e1.getQName() + ":" + appTemplateAttributes.size(), e2.getQName() + ":" + launchJNLPAttributes.size(), e1, e2));
            //}
        } else {
            if (e1 != null) {
                b.setResult(new ComparsionResult(ComparsionResult.Reasons.NULL, e1.getQName().toString(), null, e1, null)); //should not occure
            } else if (e2 != null) {
                b.setResult(new ComparsionResult(ComparsionResult.Reasons.NULL, null, e2.getQName().toString(), null, e2)); //should not occure
            } else {
                b.setResult(new ComparsionResult(ComparsionResult.Reasons.NULL, null, null, null, null)); //should not occure
            }
            return false;
        }

    }

    private boolean compareAttributeNames(QName a1, QName a2) {
        return compareQnames(a1, a2);
    }

    private boolean compareElementsNames(QName e1, QName e2) {
        return compareQnames(e1, e2);
    }

    private boolean compareAttributeValues(String a1, String a2) {
        return compareSValues(a1, a2);
    }

    private boolean compareElementValues(String e1, String e2) {
        return compareSValues(e1, e2);
    }

    private boolean compareQnames(QName a1, QName a2) {
        String s1 = a1.toString();
        String s2 = a2.toString();
        if (ignoreUri) {
            s1 = a1.getLocalPart();
            s2 = a2.getLocalPart();
        }
        if (ignoreNameCase) {
            s1 = s1.toLowerCase();
            s2 = s2.toLowerCase();
        }
        return eqOrLeven(s1, s2);
    }

    private boolean compareSValues(String s1, String s2) {

        if (ignoreValueCase) {
            s1 = s1.toLowerCase();
            s2 = s2.toLowerCase();
        }

        if (forceTrim) {
            s1 = s1.trim();
            s2 = s2.trim();
        }

        if (removeSpaces) {
            s1 = s1.replaceAll("\\s*", "");
            s2 = s2.replaceAll("\\s*", "");
        }

        return eqOrLeven(s1, s2);
    }

    private boolean eqOrLeven(String s1, String s2) {
        if (levenstain == null) {
            return s1.equals(s2);
        } else {
            return normalizeLevenstain(s1, s2);
        }
    }

    static long normalizeLevenstainValueOnElement(OMElement s1, OMElement s2) {
        return Math.round(
                100000 * normalizeLevenstainValue(s1.getQName().getLocalPart(), s2.getQName().getLocalPart())
                + 100 * normalizeLevenstainValue(s1.getText(), s2.getText()));

    }

    static double normalizeLevenstainValue(String s1, String s2) {
        int max = Math.max(s1.length(), s2.length());
        /*
         * Should be shorter one filled to max length?
         * consider "a" "bbbbbbb"?
         * NO!
         * distance will be "bbbb.."length
         */
        if (max == 0) {
            //return true; //nothig to compare, same;
            return 0;
        }
        int ch = getLevenshteinDistance(s1, s2);
        /*
         * 0 changes => 0/n => 0 == same
         * n/2 changes => (n/2)/n => 1/2 == half-same
         * n changes n/n => 1 => different/
         *
         */
        double norm = (double) ch / (double) max;
        //-levenstain=FLOAT.NUMBER conte...mber is 0-1 where 0 is for completely same, and 1 for completely different stings so your level is maximal  possible difference (dist<FLOAT.NUMBER)
        return norm;
    }

    private boolean normalizeLevenstain(String s1, String s2) {
        double norm = normalizeLevenstainValue(s1, s2);
        return norm <= levenstain;
    }

    public void setIgnoreHeaders(boolean b) {
        ignoreHeaders = b;
    }

    private void traverseBruteResults(BruteForceResultKeeper currentNode) {
        indentize();
        String verdikt = "";
        if (currentNode.result.reason.equals(ComparsionResult.Reasons.OK)) {
            verdikt = "OK: ";
        } else {
            pe();
            verdikt = "ERROR(" + errors + "): ";
        }
        output(indent + verdikt + currentNode.toString());
        for (int i = 0; i < currentNode.childs.size(); i++) {
            BruteForceResultKeeper b = currentNode.childs.get(i);
            boolean haveTrueOne = haveTrueOne(b, i, currentNode);
            if (!haveTrueOne) {
                traverseBruteResults(b);
            }
        }
        deindentize();
    }

    public void setDual(boolean b) {
        dual = b;
    }

    public void setOrphanSeek(boolean b) {
        orphanSeek = true;
    }

    public void setDiff(File file) {
        this.diff = file;
    }

    private void saveDiff(BruteForceResult br, File df, StAXOMBuilder b1, StAXOMBuilder b2) throws IOException {
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(df)));
        try {
            bw.write("<!--");
            bw.write("diff is alpha! Havy development now! best to be run with:");
            bw.write("-ignoreOrder=2 -orphanSeek -dual");
            bw.write("-->");
            bw.newLine();
            if (br.h1.encoding.state == HeadersCheckResultKeeper.State.SAME) {
            } else {
                bw.write("- " + b1.getDocument().getCharsetEncoding());
                bw.newLine();
                bw.write("+ " + b2.getDocument().getCharsetEncoding());
                bw.newLine();
            }
            if (br.h1.version.state == HeadersCheckResultKeeper.State.SAME) {
            } else {
                bw.write("- " + b1.getDocument().getXMLVersion());
                bw.newLine();
                bw.write("+ " + b2.getDocument().getXMLVersion());
                bw.newLine();
            }
            if (br.h1 != null) {
                for (ProcessingInstructionsResult pi : br.h1.pir) {
                    if (pi.state != ProcessingInstructionsResult.State.OK) {
                        bw.write("- " + pi.guilty1.getTarget() + " " + pi.guilty1.getValue());
                        bw.newLine();
                    }
                }
            }
            if (br.h2 != null) {
                for (ProcessingInstructionsResult pi : br.h2.pir) {
                    if (pi.state != ProcessingInstructionsResult.State.OK) {
                        bw.write("+ " + pi.guilty1.getTarget() + " " + pi.guilty1.getValue());
                        bw.newLine();
                    }
                }
            }

            List<DiffResult> l = new ArrayList();
            if (br.br1 != null) {
                traverseBruteToDiff("- ", br.br1, l, br.br1);
            }
            if (br.br1 != null) {
                traverseBruteToDiff("+ ", br.br2, l, br.br2);
            }
            Collections.sort(l);
            for (DiffResult diffResult : l) {
                if (diffResult.trace.result.reason == ComparsionResult.Reasons.OK) {

                } else {
                    bw.write(diffResult.l1);
                    bw.newLine();
                    bw.write(diffResult.l2);
                    bw.newLine();
                }
            }
        } finally {
            bw.close();
        }
    }

    private boolean areChildrenInErrorChildrens(List<OMElement> children, List<BruteForceResultKeeper> childs) {
        //we need to be sure, taht all childrens and ?their childrens-no just childrens!?  have their error message.
        //If some children have no message, the return false
        for (OMElement es : children) {
            boolean found = findElementInErrors(es, childs);
            if (!found) {
                return false;
            }
            //boolean found2=areChildrenInErrorChildrens(listOfChildrenWithoutComments(es),childs);
            //if (!found2) return false;
        }
        return true;

    }

    private boolean findElementInErrors(OMElement es, List<BruteForceResultKeeper> childs) {
        for (BruteForceResultKeeper child : childs) {
            if (child.node == es) {
                return true;
            }
            boolean q = findElementInErrors(es, child.childs);
            if (q) {
                return true;
            }
        }
        return false;
    }

    private static class DiffResult implements Comparable<DiffResult> {

        private String l1;
        private int ln;
        private String l2;
        private BruteForceResultKeeper trace;

        public DiffResult() {
        }

        public int compareTo(DiffResult o) {
            return ln - o.ln;
        }
    }

    private void traverseBruteToDiff(String pfix, BruteForceResultKeeper bf, List<DiffResult> l, BruteForceResultKeeper root) throws IOException {
        {
            DiffResult dr = new DiffResult();
            dr.trace = bf;
            dr.l1 = (pfix + bf.getNode().getLineNumber() + " " + extractPath(bf.node));
            dr.ln = bf.node.getLineNumber();
            boolean justSimple = true;
            justSimple = areChildrenInErrorChildrens(listOfChildrenWithoutComments(bf.node), root.childs);
            if (justSimple) {
                dr.l2 = (pfix + writeSimpleNode(bf.node));
            } else {
                dr.l2 = bf.node.toString();
            }
            boolean contains = false;
            if (bf.result.reason == ComparsionResult.Reasons.OK) {
                for (int i = 0; i < l.size(); i++) {
                    DiffResult diffResult = l.get(i);
                    if (diffResult.trace.node == dr.trace.node) {
                        l.remove(i);
                        i--;
                    }

                }

            } else {
                for (DiffResult diffResult : l) {
                    if (diffResult.trace.node == dr.trace.node) {
                        contains = true;
                        break;
                    }
                }
            }
            if (!contains) {
                l.add(dr);
            }
        }
        for (int i = 0; i < bf.getChilds().size(); i++) {
            BruteForceResultKeeper b = bf.getChilds().get(i);
//            boolean haveTrueOne = XmlComparator.haveTrueOne(b, i, bf);
//            if (!haveTrueOne) {
            traverseBruteToDiff(pfix, b, l, root);
            //           }
        }

    }

    private String extractPath(OMElement node) {
        String s = "/";
        OMContainer n = node.getParent();
        while (true) {
            if (n == null) {
                break;
            }
            if (!(n instanceof OMElement)) {
                break;
            }
            s = "/" + ((OMElement) n).getLocalName() + s;
            n = ((OMElement) n).getParent();
        }
        return s;
    }

    private String writeSimpleNode(OMElement node) {
        String uri = "";
        if (node.getQName().getNamespaceURI() != null && node.getQName().getNamespaceURI().trim().length() > 0) {
            uri = node.getQName().getNamespaceURI() + ":";
        }
        String s = "<" + uri + node.getLocalName();
        List<OMAttribute> as = listOfAttributes(node);
        for (OMAttribute a : as) {
            s = s + " " + a.getLocalName() + "='" + a.getAttributeValue() + "' ";
        }
        s = s + ">";
        s = s + node.getText().trim().replace('\n', ' ');
        s = s + "</" + node.getLocalName() + ">";

        return s;
    }

    /**
     * Taken from
     * https://commons.apache.org/proper/commons-lang/apidocs/src-html/org/apache/commons/lang3/StringUtils.html
     * *
     */
    public static int getLevenshteinDistance(CharSequence s, CharSequence t) {
        if (s == null || t == null) {
            throw new IllegalArgumentException("Strings must not be null");
        }

        /*
         The difference between this impl. and the previous is that, rather
         than creating and retaining a matrix of size s.length() + 1 by t.length() + 1,
         we maintain two single-dimensional arrays of length s.length() + 1.  The first, d,
         is the 'current working' distance array that maintains the newest distance cost
         counts as we iterate through the characters of String s.  Each time we increment
         the index of String t we are comparing, d is copied to p, the second int[].  Doing so
         allows us to retain the previous cost counts as required by the algorithm (taking
         the minimum of the cost count to the left, up one, and diagonally up and to the left
         of the current cost count being calculated).  (Note that the arrays aren't really
         copied anymore, just switched...this is clearly much better than cloning an array
         or doing a System.arraycopy() each time  through the outer loop.)
 
         Effectively, the difference between the two implementations is this one does not
         cause an out of memory condition when calculating the LD over two very large strings.
         */
        int n = s.length(); // length of s
        int m = t.length(); // length of t

        if (n == 0) {
            return m;
        } else if (m == 0) {
            return n;
        }

        if (n > m) {
            // swap the input strings to consume less memory
            final CharSequence tmp = s;
            s = t;
            t = tmp;
            n = m;
            m = t.length();
        }

        int p[] = new int[n + 1]; //'previous' cost array, horizontally
        int d[] = new int[n + 1]; // cost array, horizontally
        int _d[]; //placeholder to assist in swapping p and d

        // indexes into strings s and t
        int i; // iterates through s
        int j; // iterates through t

        char t_j; // jth character of t

        int cost; // cost

        for (i = 0; i <= n; i++) {
            p[i] = i;
        }

        for (j = 1; j <= m; j++) {
            t_j = t.charAt(j - 1);
            d[0] = j;

            for (i = 1; i <= n; i++) {
                cost = s.charAt(i - 1) == t_j ? 0 : 1;
                // minimum of cell to the left+1, to the top+1, diagonally left and up +cost
                d[i] = Math.min(Math.min(d[i - 1] + 1, p[i] + 1), p[i - 1] + cost);
            }

            // copy current distance counts to 'previous row' distance counts
            _d = p;
            p = d;
            d = _d;
        }

        // our last action in the above loop was to switch d and p, so p now
        // actually has the most recent cost counts
        return p[n];
    }
}
