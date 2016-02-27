/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package xmlcompare.impl;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.apache.axiom.om.OMContainer;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;

/**
 *
 * @author jvanek
 */
public class XmlStat extends XmlComparator {

    static int orderCounter = 0;

    private String sizeOf(int size) {
        int l = 0;
        while (true) {
            if (size < 1024) {
                break;
            }
            l++;
            size = size / 1024;

        }
        String unit = "b";
        switch (l) {
            case 1:
                unit = "kb";
                break;
            case 2:
                unit = "mb";
                break;
            case 3:
                unit = "gb";
                break;
            case 4:
                unit = "tb";
                break;
            case 5:
                unit = "pb";
                break;
        }
        return size + "" + unit;

    }

    private class StatMember implements Comparable<StatMember> {

        String head;
        int size;
        int childrens;
        int order;
        int totalSize;

        @Override
        public String toString() {
            return head + " - " + order;
        }

        public StatMember(String head, int size, int childrens, int totalSize) {

            this.head = head;
            this.size = size;
            this.totalSize = totalSize;
            this.childrens = childrens;
        }

        @Override
        @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
        public boolean equals(Object obj) {
            return head.equals(obj);
        }

        @Override
        public int hashCode() {
            return head.hashCode();

        }

        public int compareTo(StatMember t) {
            return head.compareTo(t.head);
        }
    }

    private class LengthComparator implements Comparator<StatMember> {

        public int compare(StatMember t, StatMember t1) {
            return (t.head.length() - t1.head.length());
        }
    }

    private class OrderComparator implements Comparator<StatMember> {

        public int compare(StatMember t, StatMember t1) {
            return (t.order - t1.order);
        }
    }

    private class SizeComparator implements Comparator<StatMember> {

        public int compare(StatMember t, StatMember t1) {
            return (t.size - t1.size);
        }
    }

    private class TSizeComparator implements Comparator<StatMember> {

        public int compare(StatMember t, StatMember t1) {
            return (t.totalSize - t1.totalSize);
        }
    }
    Map<String, StatMember> tree = new HashMap<String, StatMember>();

    public void stat() throws IOException, XMLStreamException {

        stat(System.out);
    }

    private void stat(OutputStream out) throws IOException, XMLStreamException {
        OutputStreamWriter o = new OutputStreamWriter(out, "utf-8");
        try {
            stat(o);
        } finally {
            if (out != System.out) {
                o.close();
            }
        }
    }

    private void stat(OutputStreamWriter outputStreamWriter) throws IOException, XMLStreamException {
        BufferedWriter br = new BufferedWriter(outputStreamWriter);
        try {
            stat(br);
        } finally {
            br.close();
        }
    }

    public String format(QName qname) {
        if (super.ignoreUri) {
            return qname.getLocalPart();
        } else {
            return qname.getNamespaceURI() + ":" + qname.getLocalPart();
        }
    }

    private void stat(BufferedWriter out) throws XMLStreamException, FileNotFoundException, IOException {
        this.out = out;

        //get doc1
        output("opening parser for: " + f1.getAbsolutePath());
        this.out = out;
        XMLStreamReader parser1 = XMLInputFactory.newInstance().createXMLStreamReader(new FileInputStream(f1));
        StAXOMBuilder builder1l = new StAXOMBuilder(parser1);
        //OMDocument doc1 = builder1.getDocument();
        OMElement root = builder1l.getDocumentElement();
        output("calculaling, please wait");
        output("all elements with their real size/number of childrens/total size (sort by found first)");
        traverse(root);
        List<StatMember> els = new ArrayList(tree.size());
        Set<Entry<String, StatMember>> set = tree.entrySet();
        for (Entry<String, StatMember> entry : set) {
            els.add(entry.getValue());
        }
        Collections.sort(els, new OrderComparator());
        int totalsize = 0;
        int totalc = 0;
        int tt = 0;
        for (StatMember statMember : els) {
            output(statMember.head + " " + sizeOf(statMember.size) + "/" + statMember.childrens + "/" + sizeOf(statMember.totalSize));
            totalc += statMember.childrens;
            totalsize += statMember.size;
            tt += statMember.totalSize;
        }
        output("Total " + sizeOf(totalsize) + "/" + totalc + 1 + "/" + sizeOf(tt));
        output("xPaths with content>0, hugest first");
        List<StatMember> elsc = new ArrayList(tree.size());
        for (StatMember statMember : els) {
            if (statMember.size > 0) {
                elsc.add(statMember);
            }
        }
        Collections.sort(elsc, new SizeComparator());
        Collections.reverse(elsc);
        for (StatMember statMember : elsc) {
            output(statMember.head + " " + sizeOf(statMember.size) + "/" + statMember.childrens + "/" + sizeOf(statMember.totalSize));
        }

        output("xPaths sorted by totalSize, no root");
        List<StatMember> elscc = new ArrayList(tree.size());
        for (StatMember statMember : els) {
            if (!statMember.head.replace("/", "").equals(format(root.getQName()))) {
                elscc.add(statMember);
            }
        }
        Collections.sort(elscc, new TSizeComparator());
        Collections.reverse(elscc);
        for (StatMember statMember : elscc) {
            output(statMember.head + " " + sizeOf(statMember.size) + "/" + statMember.childrens + "/" + sizeOf(statMember.totalSize));
        }
        output("Xpath suggestion based on :");
        output("        - second biggest by total size is content (check with content sizes!) - as biggest is its parent");
        output("        - everything with root as parent found before is header)");
        output("        - everything with root as parent found after is footer)");

        String contentAdeptParent = elscc.get(0).head;
        String contentAdeptPath = elscc.get(1).head;
        String headertAdeptPath = "";
        String footerAdeptPath = "";

        for (StatMember statMember : els) {
            if (statMember.head.startsWith(contentAdeptParent)) {
                continue;
            }
            if (statMember.head.replace("/", "").equals(format(root.getQName()))) {
                continue;
            }
            if (statMember.head.substring(format(root.getQName()).length() + 2).contains("/")) {
                continue;
            }
            if (statMember.order < elscc.get(1).order) {
                headertAdeptPath += statMember.head + " | ";
            } else {
                footerAdeptPath += statMember.head + " | ";
            }
        }
        output("contentXpath=" + contentAdeptPath);
        if (headertAdeptPath.length() > 0) {
            headertAdeptPath = headertAdeptPath.substring(0, headertAdeptPath.length() - 3);
            output("headerXpath=" + headertAdeptPath);
        }
        if (footerAdeptPath.length() > 0) {
            footerAdeptPath = footerAdeptPath.substring(0, footerAdeptPath.length() - 3);
            output("footerXpath=" + footerAdeptPath);
        }

    }

    private void traverse(OMElement e) {
        List<OMElement> pi1 = listOfChildrenWithoutComments(e);
        String t = e.getText().trim();
        String ts = e.toString();
        String path = getPath(e);
        addStatMemebr(path, t.length(), pi1.size(), ts.length());
        for (OMElement oMElement : pi1) {
            traverse(oMElement);
        }

    }

    private void addStatMemebr(String path, int t, int size, int ts) {
        StatMember e = tree.get(path);
        if (e == null) {
            orderCounter++;
            e = new StatMember(path, t, size, ts);
            e.order = orderCounter;
            tree.put(path, e);
        } else {
            e.childrens += size;
            e.size += t;
        }
    }

    private String getPath(OMElement e) {
        StringBuilder sb = new StringBuilder("/" + format(e.getQName()));
        while (true) {
            OMContainer c = e.getParent();
//            if (e == null) {
//                break;
//            }
            if (c instanceof OMElement) {
                e = (OMElement) c;
                sb.insert(0, "/" + format(e.getQName()));
            } else {
                break;
            }
        }

        return sb.toString();
    }
}
