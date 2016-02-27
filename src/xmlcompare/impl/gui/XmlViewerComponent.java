/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package xmlcompare.impl.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMProcessingInstruction;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.impl.llom.OMDocumentImpl;
import xmlcompare.impl.AttributeComparator;
import xmlcompare.impl.BruteForceResult;
import xmlcompare.impl.BruteForceResultKeeper;
import xmlcompare.impl.ComparsionResult;
import xmlcompare.impl.HeaderResult;
import xmlcompare.impl.HeadersCheckResultKeeper;
import xmlcompare.impl.ProcessingInstructionsResult;
import xmlcompare.impl.XmlComparator;

/**
 *
 * @author jvanek
 */
public class XmlViewerComponent extends JComponent {

    private StAXOMBuilder document;
    private File file;
    private int LINE_INDENT = 4;
    private int LEVEL_INDENT = 10;
    private Map<OMElement, OMElementExtensions> nodesSettings = new HashMap<OMElement, OMElementExtensions>();
    private ExtensionBase encodingAndVersion;
    private Map<OMProcessingInstruction, OMProcessingInstructionExtension> instructionsSettings = new HashMap();
    private int SCROLLBAR_SIZE = 16;
    private final XmlViewerComponent self;
    private boolean focused = false;
    ScrollBars scrollbars;
    boolean rectangles = true;
    private Integer fontSize;
    private BruteForceResultKeeper resultRoot;
    private HeadersCheckResultKeeper hr;
    private XmlViewerComponent sister;
    private int position;
    private boolean redLines = true;
    private boolean greenLines = true;
    private boolean lastError = true;

    public boolean isLastError() {
        return lastError;
    }

    public void setLastError(boolean lastError) {
        this.lastError = lastError;
    }

    @Override
    public boolean isFocusable() {
        return true;
    }

    public XmlViewerComponent() {
        self = this;
        scrollbars = new ScrollBars(self);
        addFocusListener(new FocusListener() {

            public void focusGained(FocusEvent fe) {
                self.focused = true;
                repaint();
            }

            public void focusLost(FocusEvent fe) {
                self.focused = false;
                repaint();
            }
        });
        addMouseWheelListener(new MouseWheelListener() {

            public void mouseWheelMoved(MouseWheelEvent mwe) {
                scrollbars.scrollBy(0, -mwe.getWheelRotation() * 10);

            }
        });

        addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case (KeyEvent.VK_LEFT):
                        scrollbars.scrollBy(10, 0);

                        break;
                    case (KeyEvent.VK_RIGHT):
                        scrollbars.scrollBy(-10, 0);
                        repaint();
                        break;
                    case (KeyEvent.VK_DOWN):
                        scrollbars.scrollBy(0, -1);
                        break;
                    case (KeyEvent.VK_UP):
                        scrollbars.scrollBy(0, +1);
                        break;
                    case (KeyEvent.VK_PAGE_DOWN):
                        scrollbars.scrollBy(0, -2 * getWidth() / 3);
                        break;
                    case (KeyEvent.VK_PAGE_UP):
                        scrollbars.scrollBy(0, +2 * getWidth() / 3);
                        break;
                }
            }
        });

        addMouseListener(new MouseListener() {

            public void mouseClicked(MouseEvent me) {
                //printOutMatches(me.getPoint());
                MouseObject mo = getMoused(me.getPoint());
                if (mo == null) {
                    return;
                }
                if (mo instanceof CollapseAtts) {
                    ((CollapseButton) mo).conf.atts = !((CollapseButton) mo).conf.atts;
                    repaint();
                }
                if (mo instanceof CollapseChilds) {
                    ((CollapseButton) mo).conf.childs = !((CollapseButton) mo).conf.childs;
                    repaint();
                }
                if (mo instanceof CollapseText) {
                    ((CollapseButton) mo).conf.text = !((CollapseButton) mo).conf.text;
                    repaint();
                }
                if (mo instanceof CollapseNamespace) {
                    ((CollapseButton) mo).conf.namespace = !((CollapseButton) mo).conf.namespace;
                    repaint();
                }
                if (me.getButton() == MouseEvent.BUTTON1) {
                    if (mo instanceof NodeName) {
                        String s = JOptionPane.showInputDialog(self, "Enter new name", ((NodePart) mo).source.getQName().getLocalPart());
                        if (s != null) {
                            ((NodePart) mo).source.setLocalName(s);
                        }
                        repaint();
                    }
                    if (mo instanceof NodeText) {
                        String s = JOptionPane.showInputDialog(self, "Enter new value", ((NodePart) mo).source.getText());
                        if (s != null) {
                            ((NodePart) mo).source.setText(s);
                        }
                        repaint();
                    }
                    if (mo instanceof AttName) {
                        String s = JOptionPane.showInputDialog(self, "Enter new name", ((NodeAttPart) mo).att.getLocalName());
                        if (s != null) {
                            ((NodeAttPart) mo).att.setLocalName(s);
                        }
                        repaint();
                    }
                    if (mo instanceof AttValue) {
                        String s = JOptionPane.showInputDialog(self, "Enter new value", ((NodeAttPart) mo).att.getAttributeValue());
                        if (s != null) {
                            ((NodeAttPart) mo).att.setAttributeValue(s);
                        }
                        repaint();
                    }
                } else if (me.getButton() == MouseEvent.BUTTON3) {
                    if (mo instanceof NameAndVersion) {
                        NameAndVersion nmo = (NameAndVersion) mo;
                        JOptionPane.showMessageDialog(self, "encoding: " + nmo.encoding.toString() + " version: " + nmo.version.toString());
                    }
                    if (mo instanceof ProcessingInstruction) {
                        ProcessingInstruction nmo = (ProcessingInstruction) mo;
                        JOptionPane.showMessageDialog(self, nmo.conf.error.toString());
                    }
                    if (mo instanceof NodePart && resultRoot != null) {
                        OMElement e = ((NodePart) mo).source;
                        BruteForceResultKeeper b = rfindError(e, resultRoot);
                        if (b != null) {
                            JOptionPane.showMessageDialog(self, b.toString());
                        }
                    }
                } else {
                    if (mo instanceof NodePart && resultRoot != null) {
                        OMElement e = ((NodePart) mo).source;
                        BruteForceResultKeeper b = rfindError(e, resultRoot);
                        if (b != null) {
                            FullTraceDialog ft = new FullTraceDialog(null, false);
                            ft.getComonText().setText(b.fullInfo());
                            if (b.getResult().guilty1 != null) {
                                ft.getLeftText().setText(b.getResult().guilty1.toString());
                            }
                            if (b.getResult().guilty2 != null) {
                                ft.getRightText().setText(b.getResult().guilty2.toString());
                            }
                            ft.setVisible(true);
                        }
                    }
                }

            }

            public void mousePressed(MouseEvent me) {
                requestFocus();
                MouseObject mo = getMoused(me.getPoint());
                if (mo == null) {
                    return;
                }
                if (mo instanceof ScrollHorizontalRider) {
                    //System.out.println("Horizont rider!");
                }
                if (mo instanceof ScrollVerticalRider) {
                    //System.out.println("Vertical rider!");
                }
                if (mo instanceof ScrollSmallUpClick) {
                    scrollbars.scrollBy(0, 5);
                }
                if (mo instanceof ScrollSmallDownClick) {
                    scrollbars.scrollBy(0, -5);
                }
                if (mo instanceof ScrollSmallRightClick) {
                    scrollbars.scrollBy(-5, 0);
                }
                if (mo instanceof ScrollSmallLeftClick) {
                    scrollbars.scrollBy(5, 0);
                }
                if (mo instanceof ScrollBigUpClick) {
                    scrollbars.scrollBy(0, (2 * getHeight()) / 3);
                }
                if (mo instanceof ScrollBigDownClick) {
                    scrollbars.scrollBy(0, -(2 * getHeight()) / 3);
                }
                if (mo instanceof ScrollBigRightClick) {
                    scrollbars.scrollBy(-(2 * getWidth()) / 3, 0);
                }
                if (mo instanceof ScrollBigLeftClick) {
                    scrollbars.scrollBy((2 * getWidth()) / 3, 0);
                }
            }

            public void mouseReleased(MouseEvent me) {
            }

            public void mouseEntered(MouseEvent me) {
            }

            public void mouseExited(MouseEvent me) {
            }

            private BruteForceResultKeeper rfindError(OMElement e, BruteForceResultKeeper r) {
                if (r.getNode() == e) {
                    return r;
                }
                List<BruteForceResultKeeper> l = r.getChilds();
                for (BruteForceResultKeeper bruteForceResultKeeper : l) {
                    BruteForceResultKeeper q = rfindError(e, bruteForceResultKeeper);
                    if (q != null) {
                        return q;
                    }

                }
                return null;
            }
        });

    }

    public void applyResults(BruteForceResult br, int i) {
        if (i == 0) {
            this.resultRoot = br.br1;
            this.hr = br.h1;
        } else {
            this.resultRoot = br.br2;
            this.hr = br.h2;
        }
        if (hr != null) {
            encodingAndVersion = new ExtensionBase();
            encodingAndVersion.rectangle = Color.red;
            if (hr.encoding.state == HeadersCheckResultKeeper.State.SAME
                    && hr.version.state == HeadersCheckResultKeeper.State.SAME) {
                encodingAndVersion.rectangle = Color.green;
            }
            List<OMProcessingInstruction> pil = XmlComparator.listOfProcessingInstructions(document.getDocument());
            Collections.reverse(hr.pir);
            for (OMProcessingInstruction pi : pil) {

                for (ProcessingInstructionsResult pir : hr.pir) {
                    OMProcessingInstruction piii = null;
                    if (pir.guilty1 == pi) {
                        piii = pi;
                    } else if (pir.guilty2 == pi) {
                        piii = pi;
                    }
                    if (piii != null) {
                        OMProcessingInstructionExtension conf = instructionsSettings.get(piii);
                        if (conf == null) {
                            conf = new OMProcessingInstructionExtension();
                            instructionsSettings.put(piii, conf);
                        }
                        conf.error = pir;
                        conf.keeper = piii;
                        if (pir.state == ProcessingInstructionsResult.State.OK) {
                            conf.rectangle = Color.green;
                        } else {
                            conf.rectangle = Color.red;
                        }
                    }
                }
            }
            Collections.reverse(hr.pir);

        }
        Set<Entry<OMElement, OMElementExtensions>> es = nodesSettings.entrySet();
        for (Entry<OMElement, OMElementExtensions> entry : es) {
            entry.getValue().rectangle = null;
        }
        traverseBR(resultRoot);
    }

    private boolean isGreen(Color c) {
        if (c == null) {
            return false;
        }
        return c.equals(Color.green);
    }

    private void traverseBR(BruteForceResultKeeper br) {
        OMElementExtensions ex = nodesSettings.get(br.getNode());
        if (ex != null) {
            if (br.getResult().getReason() == ComparsionResult.Reasons.OK) {
                ex.rectangle = Color.green;
                ex.matcher = br.getResult().guilty2;
            } else {
                if (lastError) {
                    if (!isGreen(ex.rectangle)) {
                        //                      List<OMAttribute> as = XmlComparator.listOfAttributes(br.getNode());
//                if(as.get(0).getAttributeValue().equals("kukykuk2"))
//                    System.out.println("reding");
                        ex.rectangle = Color.red;
                        ex.matcher = br.getResult().guilty2;
                    }
                } else {
                    if (ex.rectangle == null) {
                        //                      List<OMAttribute> as = XmlComparator.listOfAttributes(br.getNode());
                        //               if(as.get(0).getAttributeValue().equals("kukykuk2"))
                        //                   System.out.println("reding");
                        ex.rectangle = Color.red;
                        ex.matcher = br.getResult().guilty2;
                    }
                }
            }

        }
        for (int i = 0; i < br.getChilds().size(); i++) {
            BruteForceResultKeeper b = br.getChilds().get(i);
            boolean haveTrueOne = XmlComparator.haveTrueOne(b, i, br);
            if (!haveTrueOne) {
                traverseBR(b);
            }
        }
    }

    public void writeTo(File file) throws XMLStreamException, UnsupportedEncodingException, FileNotFoundException {
        ((OMDocumentImpl) document.getDocument()).serialize(new OutputStreamWriter(new FileOutputStream(file), "utf-8"));
    }

    void setSister(XmlViewerComponent xv) {
        sister = xv;
    }

    void setPosition(int resultIndex) {
        this.position = resultIndex;
    }

    void setRedLines(boolean lines) {
        this.redLines = lines;
        repaint();
    }

    void setGreenLines(boolean lines) {
        this.greenLines = lines;
        repaint();
    }

    private class ScrollBars {

        int max_width;
        int max_height;
        JComponent owener;
        private final Point scroll = new Point(0, 0);

        private void tryMaxWidth(int i) {
            if (i - scroll.x > max_width) {
                max_width = i - scroll.x;
            }
        }

        public ScrollBars(JComponent owener) {
            this.owener = owener;
        }

        public void scrollto(int x, int y) {
            scroll.x = x;
            scroll.y = y;
            if (scroll.x < -max_width + getWidth()) {
                scroll.x = -max_width + getWidth();
            }
            if (scroll.y < -max_height + getHeight()) {
                scroll.y = -max_height + getHeight();
            }
            if (scroll.x > 0) {
                scroll.x = 0;
            }
            if (scroll.y > 0) {
                scroll.y = 0;
            }

            repaint();
        }

        public void scrollBy(int x, int y) {
            scrollto(scroll.x + x, scroll.y + y);

        }

        Integer getHorizontalCaretLeft() {
            if ((max_width - owener.getWidth()) != 0) {
                int possibleWidth = owener.getWidth() - 4 * SCROLLBAR_SIZE;
                int xposs = ((possibleWidth * -(scroll.x)) / (max_width - owener.getWidth()));
                return xposs;
            }
            return null;
        }

        Integer getVerticalCaretTop() {
            if ((max_height - owener.getHeight()) != 0) {
                int possibleHeight = getHeight() - 4 * SCROLLBAR_SIZE;
                int yposs = ((possibleHeight * -(scroll.y)) / (max_height - owener.getHeight()));
                return yposs;
            }
            return null;
        }

        private void drawScrollBar(Graphics g, char c) {

            switch (c) {
                case '|':
                    g.drawRect(0, 0, SCROLLBAR_SIZE, owener.getHeight() - SCROLLBAR_SIZE);
                    drawSymbol(g, '^', 0, 0, SCROLLBAR_SIZE, SCROLLBAR_SIZE, null);
                    drawSymbol(g, 'ˇ', 0, owener.getHeight() - 2 * SCROLLBAR_SIZE, SCROLLBAR_SIZE, SCROLLBAR_SIZE, null);
                    Integer yposs = getVerticalCaretTop();
                    if (yposs != null) {
                        drawSymbol(g, 'o', 0, SCROLLBAR_SIZE + yposs, SCROLLBAR_SIZE, SCROLLBAR_SIZE, null);
                    }
                    break;
                case '-':
                    g.drawRect(SCROLLBAR_SIZE, owener.getHeight() - SCROLLBAR_SIZE - 1, owener.getWidth() - 2, SCROLLBAR_SIZE);
                    drawSymbol(g, '<', SCROLLBAR_SIZE, owener.getHeight() - SCROLLBAR_SIZE - 1, SCROLLBAR_SIZE, SCROLLBAR_SIZE, null);
                    drawSymbol(g, '>', owener.getWidth() - 2 - SCROLLBAR_SIZE, owener.getHeight() - SCROLLBAR_SIZE - 1, SCROLLBAR_SIZE, SCROLLBAR_SIZE, null);
                    Integer xposs = getHorizontalCaretLeft();
                    if (xposs != null) {
                        drawSymbol(g, 'o', 2 * SCROLLBAR_SIZE + xposs, owener.getHeight() - SCROLLBAR_SIZE - 1, SCROLLBAR_SIZE, SCROLLBAR_SIZE, null);
                    }
                    break;

            }
        }

        private ScrollBarClick resolveClick(Point p) {

            if (isPinR(p, new Rectangle(0, 0, SCROLLBAR_SIZE, SCROLLBAR_SIZE))) {
                return new ScrollSmallUpClick();
            }
            if (isPinR(p, new Rectangle(0, owener.getHeight() - 2 * SCROLLBAR_SIZE, SCROLLBAR_SIZE, SCROLLBAR_SIZE))) {
                return new ScrollSmallDownClick();
            }
            if (isPinR(p, new Rectangle(SCROLLBAR_SIZE, owener.getHeight() - SCROLLBAR_SIZE, SCROLLBAR_SIZE, SCROLLBAR_SIZE))) {
                return new ScrollSmallLeftClick();
            }
            if (isPinR(p, new Rectangle(owener.getWidth() - SCROLLBAR_SIZE, owener.getHeight() - SCROLLBAR_SIZE, SCROLLBAR_SIZE, SCROLLBAR_SIZE))) {
                return new ScrollSmallRightClick();
            }

            if (isPinR(p, new Rectangle(0, SCROLLBAR_SIZE, SCROLLBAR_SIZE, getVerticalCaretTop()))) {
                return new ScrollBigUpClick();
            }
            if (isPinR(p, new Rectangle(0, getVerticalCaretTop() + 2 * SCROLLBAR_SIZE, SCROLLBAR_SIZE, getHeight() - 2 * SCROLLBAR_SIZE - getVerticalCaretTop()))) {
                return new ScrollBigDownClick();
            }

            if (isPinR(p, new Rectangle(0, getVerticalCaretTop() + SCROLLBAR_SIZE, SCROLLBAR_SIZE, SCROLLBAR_SIZE))) {
                return new ScrollVerticalRider();
            }
            if (isPinR(p, new Rectangle(getHorizontalCaretLeft() + 2 * SCROLLBAR_SIZE, owener.getHeight() - SCROLLBAR_SIZE, SCROLLBAR_SIZE, SCROLLBAR_SIZE))) {
                return new ScrollHorizontalRider();
            }

            if (isPinR(p, new Rectangle(2 * SCROLLBAR_SIZE, owener.getHeight() - SCROLLBAR_SIZE, getHorizontalCaretLeft(), SCROLLBAR_SIZE))) {
                return new ScrollBigLeftClick();
            }
            if (isPinR(p, new Rectangle(getHorizontalCaretLeft() + 3 * SCROLLBAR_SIZE, owener.getHeight() - SCROLLBAR_SIZE, owener.getWidth() - 4 * SCROLLBAR_SIZE, SCROLLBAR_SIZE))) {
                return new ScrollBigRightClick();
            }

            return null;
        }
    }

    public XmlViewerComponent(File f) throws FileNotFoundException, XMLStreamException {
        this();
        loadFromfile(f);
    }

    public XmlViewerComponent(InputStream is) throws XMLStreamException {
        this();
        loadFromStream(is);
    }

    public XmlViewerComponent(StAXOMBuilder builder) {
        this();
        loadFromDocument(builder);
    }

    public XmlViewerComponent(XMLStreamReader builder) {
        this();
        loadFromParser(builder);
    }

    private void loadFromfile(File f) throws FileNotFoundException, XMLStreamException {
        loadFromStream(new FileInputStream(f));
        file = f;
    }

    private void loadFromDocument(StAXOMBuilder builder) {
        document = builder;

        file = null;
    }

    private void loadFromParser(XMLStreamReader parser) {
        loadFromDocument(new StAXOMBuilder(parser));
    }

    private void loadFromStream(InputStream is) throws XMLStreamException {

        XMLStreamReader parser = XMLInputFactory.newInstance().createXMLStreamReader(is);
        loadFromParser(parser);
    }

    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
        repaint();
    }

    @Override
    public void paint(Graphics g) {
        if (fontSize == null) {
            fontSize = getGraphics().getFont().getSize();
        }
        g.setFont(g.getFont().deriveFont(fontSize.floatValue()));
        OMElement root = document.getDocumentElement();
        scrollbars.max_width = 0;
        int[] coords = {scrollbars.scroll.x + LEVEL_INDENT + SCROLLBAR_SIZE, scrollbars.scroll.y + g.getFontMetrics().getHeight() + LINE_INDENT};
        if (document.getDocument().getXMLVersion() != null || document.getDocument().getCharsetEncoding() != null) {
            String versionString = "<?xml ";
            if (document.getDocument().getXMLVersion() != null) {
                versionString += " version='" + document.getDocument().getXMLVersion() + "'";
            }
            if (document.getDocument().getCharsetEncoding() != null) {
                versionString += " encoding='" + document.getDocument().getCharsetEncoding() + "'";
            }
            versionString += "?>";
            g.setColor(Color.black);
            g.drawString(versionString, coords[0], coords[1]);
            if (encodingAndVersion == null) {
                encodingAndVersion = new ExtensionBase();
            }
            encodingAndVersion.pos = new Point(coords[0], coords[1]);
            encodingAndVersion.nameBounds = new Rectangle(coords[0], coords[1] - g.getFontMetrics().getHeight(), csw(g.getFontMetrics(), versionString), g.getFontMetrics().getHeight());
            if (rectangles && encodingAndVersion.rectangle != null) {
                g.setColor(encodingAndVersion.rectangle);
                g.drawRect(encodingAndVersion.nameBounds.x, encodingAndVersion.nameBounds.y, encodingAndVersion.nameBounds.width, encodingAndVersion.nameBounds.height);
            }
            coords[1] += g.getFontMetrics().getHeight() + LINE_INDENT;
        }
        List<OMProcessingInstruction> pil = XmlComparator.listOfProcessingInstructions(document.getDocument());
        for (OMProcessingInstruction pi : pil) {
            g.setColor(Color.black);
            String versionString = "<?xml " + pi.getTarget() + " " + pi.getValue() + " ?>";
            g.drawString(versionString, coords[0], coords[1]);
            OMProcessingInstructionExtension pircConf = instructionsSettings.get(pi);

            if (pircConf == null) {
                pircConf = new OMProcessingInstructionExtension();
                instructionsSettings.put(pi, pircConf);
            }
            pircConf.pos = new Point(coords[0], coords[1]);
            pircConf.nameBounds = new Rectangle(coords[0], coords[1] - g.getFontMetrics().getHeight(), csw(g.getFontMetrics(), versionString), g.getFontMetrics().getHeight());
            if (rectangles && pircConf.rectangle != null) {
                g.setColor(pircConf.rectangle);
                g.drawRect(pircConf.nameBounds.x, pircConf.nameBounds.y, pircConf.nameBounds.width, pircConf.nameBounds.height);
            }

            coords[1] += g.getFontMetrics().getHeight() + LINE_INDENT;
        }
        traverse(coords, root, (Graphics2D) g);
        scrollbars.max_height = coords[1] - scrollbars.scroll.y + g.getFontMetrics().getHeight() + LINE_INDENT;
        g.setColor(Color.black);
        scrollbars.drawScrollBar(g, '|');
        scrollbars.drawScrollBar(g, '-');
        if (focused) {
            drawSymbol(g, 'o', 0, getHeight() - SCROLLBAR_SIZE, SCROLLBAR_SIZE, SCROLLBAR_SIZE, null);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        paint(g);
    }

    private void traverse(int[] i, OMElement e, Graphics2D g) {
        int fh = g.getFontMetrics().getHeight();
        OMElementExtensions conf = nodesSettings.get(e);
        if (conf == null) {
            conf = new OMElementExtensions();
            nodesSettings.put(e, conf);
        }

        conf.pos = new Point(i[0], i[1]);
        if (sister != null && conf.rectangle != null
                && ((redLines && conf.rectangle.equals(Color.RED))
                || (greenLines && conf.rectangle.equals(Color.GREEN)))) {

            Stroke s = g.getStroke();
            Stroke ss = new BasicStroke(5);
            try {
                g.setStroke(ss);
                OMElementExtensions m = sister.nodesSettings.get(conf.matcher);
                g.setColor(new Color(0, 0, 200));
                if (m != null && m.pos != null) {
                    if (position == 0) {
                        g.drawLine(conf.pos.x, conf.pos.y, m.pos.x + this.getWidth(), m.pos.y);
                    } else {
                        g.drawLine(conf.pos.x, conf.pos.y, m.pos.x - sister.getWidth(), m.pos.y);
                    }
                }
                ss = new BasicStroke(2f);
                g.setStroke(ss);
                if (conf.rectangle != null) {
                    g.setColor(conf.rectangle);
                } else {
                    g.setColor(Color.orange);
                }
                if (m != null && m.pos != null) {
                    if (position == 0) {
                        g.drawLine(conf.pos.x, conf.pos.y, m.pos.x + this.getWidth(), m.pos.y);
                    } else {
                        g.drawLine(conf.pos.x, conf.pos.y, m.pos.x - sister.getWidth(), m.pos.y);
                    }
                }
            } finally {
                g.setStroke(s);
            }

        }

        String name = e.getQName().getLocalPart();
        String uri = e.getQName().getNamespaceURI();

        String lineStart = "< ";
        g.setColor(Color.yellow);
        g.drawString(lineStart, i[0], i[1]);
        int ls = csw(g.getFontMetrics(), lineStart);
        if (uri != null && uri.trim().length() > 0) {
            g.setColor(Color.PINK);
            if (conf.namespace) {
                drawSymbol(g, '-', i[0] + ls, i[1] - fh, 5, fh, null);
            } else {
                drawSymbol(g, '+', i[0] + ls, i[1] - fh, 5, fh, null);
            }
            conf.nameSpaceButton = new Rectangle(i[0] + ls, i[1] - fh, 5, fh);
            ls += 5;

            if (conf.namespace) {

                g.setColor(Color.CYAN);
                g.drawString(uri, i[0] + ls, i[1]);
                ls += csw(g.getFontMetrics(), uri);
                g.setColor(Color.yellow);
                g.drawString(":", i[0] + ls, i[1]);
                ls += csw(g.getFontMetrics(), ":");

            }
        } else {
            conf.nameSpaceButton = null;
        }
        g.setColor(Color.blue);
        g.drawString(name, i[0] + ls, i[1]);
        int nameL = csw(g.getFontMetrics(), name);
        conf.nameBounds = new Rectangle(i[0] + ls, i[1] - fh, nameL, fh);
        ls += nameL;
        ls += 5; //just indentation

        List<OMAttribute> as = XmlComparator.listOfAttributes(e);

        conf.attsBounds = new ArrayList<Rectangle>(as.size());
        conf.attsValuesBounds = new ArrayList<Rectangle>(as.size());
        if (as.size() > 0) {
            conf.attsButton = new Rectangle(i[0] + ls, i[1] - fh, 5, fh);
            if (conf.atts) {
                g.setColor(Color.PINK);
                drawSymbol(g, '-', i[0] + ls, i[1] - fh, 5, fh, null);
                ls += 5; //just indentation
                Collections.sort(as, new AttributeComparator());
                for (OMAttribute att : as) {
                    ls += 5;
                    g.setColor(Color.white);
                    g.drawString(att.getLocalName(), i[0] + ls, i[1]);
                    int laNameL = csw(g.getFontMetrics(), att.getLocalName());
                    conf.attsBounds.add(0, new Rectangle(i[0] + ls, i[1] - fh, laNameL, fh));
                    ls += laNameL;
                    g.setColor(Color.yellow);
                    g.drawString("=\"", i[0] + ls, i[1]);
                    ls += csw(g.getFontMetrics(), "=\"");
                    g.setColor(Color.magenta);
                    g.drawString(att.getAttributeValue(), i[0] + ls, i[1]);
                    int attValueL = csw(g.getFontMetrics(), att.getAttributeValue());
                    conf.attsValuesBounds.add(0, new Rectangle(i[0] + ls, i[1] - fh, attValueL, fh));
                    ls += attValueL;
                    g.setColor(Color.yellow);
                    g.drawString("\"", i[0] + ls, i[1]);
                    ls += csw(g.getFontMetrics(), "\"");
                }
            } else {
                g.setColor(Color.PINK);
                drawSymbol(g, '+', i[0] + ls, i[1] - fh, 5, fh, null);
                ls += 5; //just indentation
            }
        } else {
            conf.attsButton = null;
        }
        List<OMElement> l = XmlComparator.listOfChildrenWithoutComments(e, null);
        g.setColor(Color.yellow);
        boolean isText = e.getText().trim().length() > 0;
        boolean isContent = isText || l.size() > 0;
        if (isContent) {
            g.drawString("  >", i[0] + ls, i[1]);
            ls += csw(g.getFontMetrics(), "  >");
            if (isText) {
                if (conf.text) {
                    g.setColor(Color.PINK);
                    drawSymbol(g, '-', i[0] + ls, i[1] - fh, 5, fh, null);
                } else {
                    g.setColor(Color.PINK);
                    drawSymbol(g, '+', i[0] + ls, i[1] - fh, 5, fh, null);
                }
                conf.textButton = new Rectangle(i[0] + ls, i[1] - fh, 5, fh);
                ls += 5; //moving for ^^
            } else {
                conf.textButton = null;
            }
            if (l.size() > 0) {
                if (conf.childs) {
                    g.setColor(Color.PINK);
                    drawSymbol(g, '-', i[0] + ls, i[1] - fh, 5, fh, null);
                } else {
                    g.setColor(Color.PINK);
                    drawSymbol(g, '+', i[0] + ls, i[1] - fh, 5, fh, null);
                }
                conf.childsButton = new Rectangle(i[0] + ls, i[1] - fh, 5, fh);
            } else {
                conf.childsButton = null;
            }

        } else {
            g.drawString("  />", i[0] + ls, i[1]);
            ls += csw(g.getFontMetrics(), " />");
        }
        scrollbars.tryMaxWidth(i[0] + ls);
        if (isText && conf.text) {
            g.setColor(Color.black);
            i[1] += fh + LINE_INDENT;
            g.drawString(e.getText(), i[0] + LEVEL_INDENT, i[1]);
            int tw = csw(g.getFontMetrics(), e.getText());
            scrollbars.tryMaxWidth(i[0] + LEVEL_INDENT + tw);
            conf.textBounds = new Rectangle(i[0] + LEVEL_INDENT, i[1] - fh, tw, fh);
        } else {
            conf.textBounds = null;
        }

        int enteringTop = i[1];
        i[0] += LEVEL_INDENT;
        i[1] += fh + LINE_INDENT;
        try {
            if (conf.childs) {
                for (OMElement ee : l) {
                    traverse(i, ee, g);
                }
            } else {
                for (OMElement ee : l) {
                    clear(ee);
                }
            }
        } finally {

            i[0] -= LEVEL_INDENT;
            if (isContent) {
                String lineEnd = "</";
                g.setColor(Color.yellow);
                g.drawString(lineEnd, i[0], i[1]);
                int ls2 = csw(g.getFontMetrics(), lineEnd);
                g.setColor(Color.blue);
                g.drawString(name, i[0] + ls2, i[1]);
                ls2 += csw(g.getFontMetrics(), name);
                g.setColor(Color.yellow);
                g.drawString(">", i[0] + ls2, i[1]);
                i[1] += fh + LINE_INDENT;

            }
            if (conf.rectangle != null && rectangles) {
                g.setColor(conf.rectangle);
                int reduct = fh;
                int aduct = 0;
                if (isText && conf.text) {
                    reduct += fh + LINE_INDENT;
                    aduct += fh + LINE_INDENT;
                }
                if (!isContent) {
                    g.drawRect(i[0], enteringTop - reduct, ls - i[0] + LEVEL_INDENT + scrollbars.scroll.x, fh + LINE_INDENT);
                } else {
                    int ww = Math.max(ls - i[0], csw(g.getFontMetrics(), e.getText()));
                    g.drawRect(i[0], enteringTop - reduct, ww + LEVEL_INDENT + scrollbars.scroll.x, i[1] - enteringTop + aduct);
                }
            }
        }
    }

    private void drawSymbol(Graphics g, char ch, int x, int y, int w, int h, Color fillQuad) {

        if (fillQuad != null) {
            Color c = g.getColor();
            g.setColor(fillQuad);
            g.fillRect(x, y, w, h);
            g.setColor(c);
        }

        g.drawRect(x, y, w, h);

        switch (ch) {
            case '>':
                Polygon p = new Polygon();
                p.addPoint(x + w / 4, y + h / 4);
                p.addPoint(x + 3 * w / 4, y + h / 2);
                p.addPoint(x + w / 4, y + 3 * h / 4);
                g.fillPolygon(p);
                break;
            case '<':
                p = new Polygon();
                p.addPoint(x + 3 * w / 4, y + h / 4);
                p.addPoint(x + w / 4, y + h / 2);
                p.addPoint(x + 3 * w / 4, y + 3 * h / 4);
                g.fillPolygon(p);
                break;
            case '^':
                p = new Polygon();
                p.addPoint(x + w / 4, y + 3 * h / 4);
                p.addPoint(x + w / 2, y + h / 4);
                p.addPoint(x + 3 * w / 4, y + 3 * h / 4);
                g.fillPolygon(p);
                break;
            case 'ˇ':
                p = new Polygon();
                p.addPoint(x + w / 4, y + h / 4);
                p.addPoint(x + w / 2, y + 3 * h / 4);
                p.addPoint(x + 3 * w / 4, y + h / 4);
                g.fillPolygon(p);
                break;
            case '+':
                g.drawLine(x + w / 2, y + h / 4, x + w / 2, y + 3 * h / 4);
                g.drawLine(x + w / 4, y + h / 2, x + 3 * w / 4, y + h / 2);
                break;
            case '-':
                g.drawLine(x + w / 4, y + h / 2, x + 3 * w / 4, y + h / 2);
                break;
            case 'o':
                g.fillOval(x + w / 4, y + h / 4, w / 2, h / 2);
                break;
            default:
                break;
        }

    }

    public void setAllNamespaces(boolean namespaces) {
        Set<Entry<OMElement, OMElementExtensions>> all = nodesSettings.entrySet();

        for (Entry<OMElement, OMElementExtensions> entry : all) {
            entry.getValue().namespace = namespaces;

        }
        repaint();
    }

    public void setAllAtts(boolean namespaces) {
        Set<Entry<OMElement, OMElementExtensions>> all = nodesSettings.entrySet();

        for (Entry<OMElement, OMElementExtensions> entry : all) {
            entry.getValue().atts = namespaces;

        }
        repaint();
    }

    public void setHideShowAll(boolean namespaces) {
        Set<Entry<OMElement, OMElementExtensions>> all = nodesSettings.entrySet();

        for (Entry<OMElement, OMElementExtensions> entry : all) {
            entry.getValue().childs = namespaces;

        }
        repaint();
    }

    public void setAllTexts(boolean namespaces) {
        Set<Entry<OMElement, OMElementExtensions>> all = nodesSettings.entrySet();

        for (Entry<OMElement, OMElementExtensions> entry : all) {
            entry.getValue().text = namespaces;

        }
        repaint();
    }

    private int csw(FontMetrics fontMetrics, String lineStart) {
        return SwingUtilities.computeStringWidth(fontMetrics, lineStart);
    }

    private boolean isPinR(Point p, Rectangle r) {
        if (p == null || r == null) {
            return false;
        }
        return (p.x > r.x && p.x < r.x + r.width
                && p.y > r.y && p.y < r.y + r.height);
    }

    private void clear(OMElement e) {
        OMElementExtensions conf = nodesSettings.get(e);
        if (conf != null) {
            conf.attsButton = null;
            conf.childsButton = null;
            conf.nameBounds = null;
            conf.nameSpaceButton = null;
            conf.textBounds = null;
            conf.textButton = null;
            conf.attsBounds = new ArrayList<Rectangle>(0);
            conf.attsValuesBounds = new ArrayList<Rectangle>(0);
            conf.pos = null;
        }
        List<OMElement> l = XmlComparator.listOfChildrenWithoutComments(e, null);
        for (OMElement oMElement : l) {
            clear(oMElement);
        }
    }

    private class ExtensionBase {

        public Color rectangle = null;
        public Rectangle nameBounds;
        public Point pos;
    }

    private class OMProcessingInstructionExtension extends ExtensionBase {

        OMProcessingInstruction keeper;
        ProcessingInstructionsResult error;
    }

    private class OMElementExtensions extends ExtensionBase {

        public boolean namespace = true;
        public boolean text = true;
        public boolean childs = true;
        public boolean atts = true;
        public Rectangle nameSpaceButton;
        public Rectangle attsButton;
        public Rectangle childsButton;
        public Rectangle textButton;
        public List<Rectangle> attsBounds;
        public List<Rectangle> attsValuesBounds;
        public Rectangle textBounds;
        private OMElement matcher;
    }

    private MouseObject getMoused(int x, int y) {
        return getMoused(new Point(x, y));
    }

    private MouseObject getMoused(Point p) {
        ScrollBarClick sb = scrollbars.resolveClick(p);
        if (sb != null) {
            return sb;
        }
        if (isPinR(p, encodingAndVersion.nameBounds)) {
            NameAndVersion r = new NameAndVersion();
            r.encoding = hr.encoding;
            r.version = hr.version;
            return r;
        }
        Set<Entry<OMProcessingInstruction, OMProcessingInstructionExtension>> aii = instructionsSettings.entrySet();
        for (Entry<OMProcessingInstruction, OMProcessingInstructionExtension> conf : aii) {

            if (isPinR(p, conf.getValue().nameBounds)) {
                ProcessingInstruction r = new ProcessingInstruction();
                r.conf = conf.getValue();
                r.keyInstruction = conf.getKey();
                return r;
            }
        }

        Set<Entry<OMElement, OMElementExtensions>> all = nodesSettings.entrySet();
        for (Entry<OMElement, OMElementExtensions> entry : all) {
            OMElementExtensions conf = entry.getValue();
            if (isPinR(p, conf.attsButton)) {
                CollapseAtts r = new CollapseAtts();
                r.conf = conf;
                return r;
            }
            if (isPinR(p, conf.childsButton)) {
                CollapseChilds r = new CollapseChilds();
                r.conf = conf;
                return r;
            }
            if (isPinR(p, conf.nameBounds)) {
                NodeName r = new NodeName();
                r.conf = conf;
                r.source = entry.getKey();
                return r;
            }
            if (isPinR(p, conf.nameSpaceButton)) {
                CollapseNamespace r = new CollapseNamespace();
                r.conf = conf;
                return r;
            }
            if (isPinR(p, conf.textBounds)) {
                NodeText r = new NodeText();
                r.conf = conf;
                r.source = entry.getKey();
                return r;
            }
            if (isPinR(p, conf.textButton)) {
                CollapseText r = new CollapseText();
                r.conf = conf;
                return r;
            }
            {
                List<Rectangle> l1 = conf.attsBounds;
                for (int i = 0; i < l1.size(); i++) {
                    Rectangle rectangle = l1.get(i);
                    if (isPinR(p, rectangle)) {
                        List<OMAttribute> l11 = XmlComparator.listOfAttributes(entry.getKey());
                        AttName r = new AttName();
                        r.conf = conf;
                        r.source = entry.getKey();
                        r.att = l11.get(l11.size() - 1 - i);
                        return r;
                    }

                }
            }
            {
                List<Rectangle> l1 = conf.attsValuesBounds;
                for (int i = 0; i < l1.size(); i++) {
                    Rectangle rectangle = l1.get(i);
                    if (isPinR(p, rectangle)) {
                        List<OMAttribute> l11 = XmlComparator.listOfAttributes(entry.getKey());
                        AttValue r = new AttValue();
                        r.conf = conf;
                        r.source = entry.getKey();
                        r.att = l11.get(l11.size() - 1 - i);
                        return r;
                    }

                }
            }

        }
        return null;
    }

    private void printOutMatches(int x, int y) {
        printOutMatches(new Point(x, y));
    }

    private void printOutMatches(Point p) {

        Set<Entry<OMElement, OMElementExtensions>> all = nodesSettings.entrySet();
        for (Entry<OMElement, OMElementExtensions> entry : all) {
            OMElementExtensions conf = entry.getValue();
            if (isPinR(p, conf.attsButton)) {
                System.out.println("AttsButton for " + entry.getKey().getQName().toString());
            }
            if (isPinR(p, conf.childsButton)) {
                System.out.println("childsButton for " + entry.getKey().getQName().toString());
            }
            if (isPinR(p, conf.nameBounds)) {
                System.out.println("name for " + entry.getKey().getQName().toString());
            }
            if (isPinR(p, conf.nameSpaceButton)) {
                System.out.println("namespacebutton for " + entry.getKey().getQName().toString());
            }
            if (isPinR(p, conf.textBounds)) {
                System.out.println("text for " + entry.getKey().getQName().toString());
            }
            if (isPinR(p, conf.textButton)) {
                System.out.println("textButton for " + entry.getKey().getQName().toString());
            }
            {
                List<Rectangle> l1 = conf.attsBounds;
                for (int i = 0; i < l1.size(); i++) {
                    Rectangle rectangle = l1.get(i);
                    if (isPinR(p, rectangle)) {
                        List<OMAttribute> l11 = XmlComparator.listOfAttributes(entry.getKey());
                        System.out.println("attName for " + l11.get(i).getLocalName() + " for " + entry.getKey().getQName().toString());
                    }

                }
            }
            {
                List<Rectangle> l1 = conf.attsValuesBounds;
                for (int i = 0; i < l1.size(); i++) {
                    Rectangle rectangle = l1.get(i);
                    if (isPinR(p, rectangle)) {
                        List<OMAttribute> l11 = XmlComparator.listOfAttributes(entry.getKey());
                        System.out.println("attValue for " + l11.get(i).getAttributeValue() + " for " + entry.getKey().getQName().toString());
                    }

                }
            }

        }

    }

    public void setRectangles(boolean rectangles) {
        this.rectangles = rectangles;
        repaint();
    }

    public boolean isRectangles() {
        return rectangles;
    }

    private abstract class MouseObject {
    }

    private abstract class ScrollBarClick extends MouseObject {
    }

    private class NameAndVersion extends MouseObject {

        HeaderResult encoding;
        HeaderResult version;
    }

    private class ProcessingInstruction extends MouseObject {

        OMProcessingInstructionExtension conf;
        OMProcessingInstruction keyInstruction;
    }

    private class ScrollVerticalRider extends ScrollBarClick {
    }

    private class ScrollHorizontalRider extends ScrollBarClick {
    }

    private class ScrollSmallUpClick extends ScrollBarClick {
    }

    private class ScrollSmallDownClick extends ScrollBarClick {
    }

    private class ScrollSmallLeftClick extends ScrollBarClick {
    }

    private class ScrollSmallRightClick extends ScrollBarClick {
    }

    private class ScrollBigUpClick extends ScrollBarClick {
    }

    private class ScrollBigDownClick extends ScrollBarClick {
    }

    private class ScrollBigLeftClick extends ScrollBarClick {
    }

    private class ScrollBigRightClick extends ScrollBarClick {
    }

    private abstract class ElementExtensionsObject extends MouseObject {

        OMElementExtensions conf;
    }

    private abstract class CollapseButton extends ElementExtensionsObject {
    }

    private class CollapseAtts extends CollapseButton {
    }

    private class CollapseNamespace extends CollapseButton {
    }

    private class CollapseText extends CollapseButton {
    }

    private class CollapseChilds extends CollapseButton {
    }

    private abstract class NodePart extends ElementExtensionsObject {

        OMElement source;
    }

    private abstract class NodeAttPart extends NodePart {

        OMAttribute att;
    }

    private abstract class NodeEllPart extends NodePart {
    }

    private class NodeName extends NodeEllPart {
    }

    private class NodeText extends NodeEllPart {
    }

    private class AttName extends NodeAttPart {
    }

    private class AttValue extends NodeAttPart {
    }
}
