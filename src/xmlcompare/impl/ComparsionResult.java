/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package xmlcompare.impl;

import java.util.ArrayList;
import java.util.List;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;

/**
 *
 * @author jvanek
 */
public class ComparsionResult {

    public enum Reasons {

        OK, EL_NAME, EL_VALUE, AT_MATCH, AT_NAME, AT_VALUE, EL_CHILD_COUNT, AT_COUNT, Child_Fails, NULL, undef;

        @Override
        public String toString() {
            switch (this) {
                case OK:
                    return "OK";
                case EL_NAME:
                    return "Element name differs";
                case EL_CHILD_COUNT:
                    return "Number of children differs";
                case EL_VALUE:
                    return "Element value differs";
                case AT_MATCH:
                    return "Attribute mish-match ";
                case AT_NAME:
                    return "Attribute name differs";
                case AT_VALUE:
                    return "Attribute value differs";
                case AT_COUNT:
                    return "Attributes count differs";
                case Child_Fails:
                    return "No suitable children path found";
                case NULL:
                    return "Some input was null";
                case undef:
                    return " undef - that mostly means that there left nothing to be cmpared with";
                default:
                    return "UNKNOWN";
            }
        }
    }
    public Reasons reason = Reasons.undef;
    public String reasonValue1;
    public String reasonValue2;
//    public Integer line1;
//    public Integer line2;
    //tracking

    public OMElement guilty1;
    public OMElement guilty2;
    public List<OMAttribute> guiltyAtts1 = new ArrayList<OMAttribute>(0);
    public List<OMAttribute> guiltyAtts2 = new ArrayList<OMAttribute>(0);

    public ComparsionResult() {
    }

    public ComparsionResult(Reasons reason, String reasonValue1, String reasonValue2, OMElement guilty1, OMElement guilty2) {
        this.reason = reason;
        this.reasonValue1 = reasonValue1;
        this.reasonValue2 = reasonValue2;
        this.guilty1 = guilty1;
        this.guilty2 = guilty2;
    }

    public Reasons getReason() {
        return reason;
    }

    @Override
    public String toString() {
        if (reason.equals(Reasons.OK)) {
            return reason.toString();
        } else {
            StringBuilder sb = new StringBuilder(reason.toString());
            sb.append(getMessage());
            return sb.toString();
        }
    }

    public String getMessage() {
        StringBuilder sb = new StringBuilder();
        if (reasonValue1 != null) {
            sb.append("; value1: ").append(reasonValue1);
        } else {
            sb.append("; value1: null");
        }
        if (reasonValue2 != null) {
            sb.append("; value2: ").append(reasonValue2);
        } else {
            sb.append("; value2: null");
        }
        if (guilty1 != null) {
            sb.append("; line1: ").append(guilty1.getLineNumber());
        } else {
            sb.append("; line1: null");
        }
        if (guilty2 != null) {
            sb.append("; line2: ").append(guilty2.getLineNumber());
        } else {
            sb.append("; line2: null");
        }

        return sb.toString();
    }

}
