/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package xmlcompare.impl;

import java.util.Comparator;
import javax.xml.namespace.QName;
import org.apache.axiom.om.OMElement;

/**
 *
 * @author jvanek
 */
public class AlphaAndChildCountComparator implements Comparator<OMElement> {

    private String prefixLast(QName qName) {
        return qName.getLocalPart() + qName.getNamespaceURI();
    }

    public static enum ContentComapre {

        NONE, JUST_CHILD_LESS, WHOLE
    };
    ContentComapre contentCompare;

    public AlphaAndChildCountComparator(ContentComapre contentCompare) {
        this.contentCompare = contentCompare;
    }

    public int compare(OMElement t, OMElement t2) {
        int a = prefixLast(t.getQName()).compareTo(prefixLast(t2.getQName()));
        if (a == 0) {
            int s = XmlComparator.listOfChildrenWithoutComments(t, null).size();
            int s2 = XmlComparator.listOfChildrenWithoutComments(t2, null).size();
            if (s == s2 && contentCompare == ContentComapre.JUST_CHILD_LESS && s == 0) {
                return t.toString().replaceAll("\\s*", "").compareTo(t2.toString().replaceAll("\\s*", ""));
            }
            if (s == s2 && contentCompare == ContentComapre.WHOLE) {
                return t.toString().replaceAll("\\s*", "").compareTo(t2.toString().replaceAll("\\s*", ""));
            }
            return s - s2;
        } else {
            return a;
        }
    }
}
