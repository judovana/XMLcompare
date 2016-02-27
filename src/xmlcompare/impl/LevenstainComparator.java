/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package xmlcompare.impl;

import java.util.Comparator;
import org.apache.axiom.om.OMElement;

/**
 *
 * @author jvanek
 */
class LevenstainComparator implements Comparator<OMElement> {

    private final OMElement pattern;

    public LevenstainComparator(OMElement pi1) {
        this.pattern = pi1;
    }

    public int compare(OMElement t1, OMElement t2) {
        long l1 = XmlComparator.normalizeLevenstainValueOnElement(pattern, t1);
        long l2 = XmlComparator.normalizeLevenstainValueOnElement(pattern, t2);
        return (int) (l1 - l2);
    }

}
