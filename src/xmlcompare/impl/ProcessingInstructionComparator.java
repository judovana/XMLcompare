/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package xmlcompare.impl;

import java.util.Comparator;
import org.apache.axiom.om.OMProcessingInstruction;

/**
 *
 * @author jvanek
 */
class ProcessingInstructionComparator implements Comparator<OMProcessingInstruction> {

    public ProcessingInstructionComparator() {
    }

    public int compare(OMProcessingInstruction t, OMProcessingInstruction t1) {
        int i = t.getTarget().compareTo(t1.getTarget());
        if (i != 0) {
            return i;
        }

        return t.getValue().compareTo(t1.getValue());
    }

}
