/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package xmlcompare.impl;

import org.apache.axiom.om.OMProcessingInstruction;

/**
 *
 * @author jvanek
 */
public class ProcessingInstructionsResult {

    public enum State {

        undef, OK, DIFF, REMAINS, NONE_LEFT;

        @Override
        public String toString() {
            switch (this) {
                case undef:
                case OK:
                    return "OK";
                case DIFF:
                    return "Differs";
                case REMAINS:
                    return "Some remains uncompared";
                case NONE_LEFT:
                    return "None left but needs to be compared";
                default:
                    return "unknown";
            }
        }
    }
    public OMProcessingInstruction guilty1;
    public OMProcessingInstruction guilty2;
    public String errorMessage;
    public State state = State.undef;

    public ProcessingInstructionsResult(OMProcessingInstruction guilty1, OMProcessingInstruction guilty2, String errorMessage, State state) {
        this.guilty1 = guilty1;
        this.guilty2 = guilty2;
        this.errorMessage = errorMessage;
        this.state = state;
    }

    @Override
    public String toString() {
        if (errorMessage != null) {
            return state.toString() + " - " + errorMessage;
        } else {
            return state.toString();
        }
    }

}
