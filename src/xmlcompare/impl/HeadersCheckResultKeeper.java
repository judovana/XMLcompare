/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package xmlcompare.impl;

import java.util.List;

/**
 *
 * @author jvanek
 */
public class HeadersCheckResultKeeper {

    public enum State {

        SAME, NULL_VALUE, VALUE_NULL, DIFF, undef;

        @Override
        public String toString() {
            switch (this) {
                case SAME:
                    return "Same ";
                case NULL_VALUE:
                    return "First was not defined, second have value ";
                case VALUE_NULL:
                    return "First havbe value, second wwas not defined ";
                case DIFF:
                    return "Different vlaues";
                case undef:
                    return "udefined - mostly means no pair";
                default:
                    return "unknown";
            }

        }
    }
    public HeaderResult encoding;
    public HeaderResult version;
    public List<ProcessingInstructionsResult> pir;
}
