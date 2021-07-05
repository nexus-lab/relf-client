package org.nexus_lab.relf.lib.rdfvalues;

import lombok.NoArgsConstructor;

/**
 * @author Ruipeng Zhang
 */
@NoArgsConstructor
public class FlowSessionID extends SessionID {
    public FlowSessionID(String value) {
        super(value);
    }

    public FlowSessionID(RDFURN value) {
        super(value);
    }

    @Override
    public FlowSessionID parse(String string) {
        super.parse(!string.startsWith("aff4") ? "aff4:/flows/" + string : string);
        return this;
    }
}
