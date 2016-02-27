/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package xmlcompare.impl;

import java.util.LinkedList;
import java.util.List;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;

/**
 *
 * @author jvanek
 */
public class BruteForceResultKeeper {

    OMElement node;
    ComparsionResult result = new ComparsionResult();
    List<BruteForceResultKeeper> childs = new LinkedList();

    public BruteForceResultKeeper(OMElement node) {
        this.node = node;
    }

    public void setResult(ComparsionResult result) {
        //if (this.result!=null && this.result.reason==ComparsionResult.Reasons.OK) return;
        this.result = result;
    }

    public void addChild(BruteForceResultKeeper b) {
        childs.add(b);
    }

    public List<BruteForceResultKeeper> getChilds() {
        return childs;
    }

    public OMElement getNode() {
        return node;
    }

    public ComparsionResult getResult() {
        return result;
    }

    @Override
    public int hashCode() {
        return node.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final BruteForceResultKeeper other = (BruteForceResultKeeper) obj;
        return !(this.node != other.node && (this.node == null || !this.node.equals(other.node)));
    }

    @Override
    public String toString() {

        return node.getQName() + ": " + result.toString();

    }

    BruteForceResultKeeper getOkOne() {
        for (BruteForceResultKeeper bfr : childs) {
            if (bfr.result.reason.equals(ComparsionResult.Reasons.OK)) {
                return bfr;
            }
        }
        return null;
    }

    public String fullInfo() {
        StringBuilder s = new StringBuilder();
        s.append(node.getQName()).append(": ").append(result.reason.toString()).append(" ").append(result.getMessage()).append("\n");
        s.append("childrens ").append(childs.size()).append("\n");
        s.append(node.getClass().getName()).append('@').append(Integer.toHexString(node.hashCode())).append(": \n");
        if (result.guilty1 != null) {
            s.append(result.guilty1.getClass().getName()).append('@').append(Integer.toHexString(result.guilty1.hashCode()));
        }
        s.append(" x ");
        if (result.guilty2 != null) {
            s.append(result.guilty2.getClass().getName()).append('@').append(Integer.toHexString(result.guilty2.hashCode()));
        }
        s.append(": \n");
        s.append("guilty atts1: ");
        List<OMAttribute> l1 = result.guiltyAtts1;
        for (OMAttribute oMAttribute : l1) {
            s.append(oMAttribute.getLocalName()).append(" ");
        }
        s.append(" \n");
        s.append("guilty atts2: ");
        List<OMAttribute> l2 = result.guiltyAtts2;
        for (OMAttribute oMAttribute : l2) {
            s.append(oMAttribute.getLocalName()).append(" ");
        }
        s.append(" \n");
        s.append(node.toString());
        return s.toString();
    }
}
