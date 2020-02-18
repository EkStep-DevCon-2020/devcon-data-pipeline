package org.ekstep.ep.samza.util;

public class VisitorElement {

    private Visitor Visitor;

    public VisitorElement(Visitor visitor) {
        this.Visitor = visitor;
    }

    public org.ekstep.ep.samza.util.Visitor getVisitor() {
        return Visitor;
    }
}
