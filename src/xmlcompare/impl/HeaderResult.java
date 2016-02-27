/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package xmlcompare.impl;

import xmlcompare.impl.HeadersCheckResultKeeper.State;

/**
 *
 * @author jvanek
 */
public class HeaderResult {

    public HeadersCheckResultKeeper.State state = HeadersCheckResultKeeper.State.undef;
    public String reason;

    public HeaderResult(State state, String reason) {
        this.state = state;
        this.reason = reason;
    }

    @Override
    public String toString() {
        if (reason == null) {
            return state.toString();
        } else {
            return state.toString() + " - " + reason;
        }
    }

}
