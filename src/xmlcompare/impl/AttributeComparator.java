/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package xmlcompare.impl;

import java.util.Comparator;
import org.apache.axiom.om.OMAttribute;

/**
 *
 * @author jvanek
 */
public class AttributeComparator implements Comparator<OMAttribute> {

    public AttributeComparator() {
    }

    public int compare(OMAttribute t, OMAttribute t1) {
        int i = t.getQName().toString().compareTo(t1.getQName().toString());
        if (i != 0) {
            return i;
        }
        return t.getAttributeValue().compareTo(t1.getAttributeValue());
    }

}
