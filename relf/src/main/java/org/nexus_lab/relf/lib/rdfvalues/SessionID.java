package org.nexus_lab.relf.lib.rdfvalues;

import net.badata.protobuf.converter.type.TypeConverter;

import java.util.Random;
import java.util.regex.Pattern;

import lombok.NoArgsConstructor;

/**
 * @author Ruipeng Zhang
 */
@NoArgsConstructor
public class SessionID extends RDFURN {
    private static final Pattern SESSION_ID_PATTERN = Pattern.compile(
            "^[-0-9a-zA-Z]+:[0-9a-zA-Z]+(:[0-9a-zA-Z]+)?$");
    private static final RDFURN DEFAULT_FLOW_QUEUE = new RDFURN("F");

    public SessionID(String value) {
        super(value);
    }

    public SessionID(RDFURN value) {
        parse(value.getValue());
        setAge(value.getAge());
    }

    /**
     * Create {@link SessionID} from ReLF flow name
     *
     * @param flowName ReLF flow name
     * @return new instance of {@link SessionID}
     */
    public static SessionID fromFlow(String flowName) {
        return fromFlow(DEFAULT_FLOW_QUEUE, flowName);
    }

    /**
     * Create {@link SessionID} from queue name and ReLF flow name
     *
     * @param queue    session queue name
     * @param flowName ReLF flow name
     * @return new instance of {@link SessionID}
     */
    public static SessionID fromFlow(RDFURN queue, String flowName) {
        return fromFlow("aff4:/flows", queue, flowName);
    }

    /**
     * Create {@link SessionID} from queue name and ReLF flow name
     *
     * @param base     base {@link RDFURN} path string
     * @param queue    session queue name
     * @param flowName ReLF flow name
     * @return new instance of {@link SessionID}
     */
    public static SessionID fromFlow(String base, RDFURN queue, String flowName) {
        String newFlowName = flowName == null ? Integer.toHexString(new Random().nextInt())
                : flowName;
        RDFURN value = new RDFURN(base).join(
                String.format("%s:%s", queue.getBasename(), newFlowName));
        return new SessionID(value);
    }

    @Override
    public RDFURN parse(String value) {
        super.parse(value);
        if (!SESSION_ID_PATTERN.matcher(getBasename()).find()) {
            throw new IllegalArgumentException(
                    String.format("Invalid SessionID: %s", getBasename()));
        }
        return this;
    }

    /**
     * @return session queue name
     */
    public RDFURN getQueue() {
        return new RDFURN(getBasename().split(":")[0]);
    }

    /**
     * @return ReLF flow name
     */
    public String getFlowName() {
        return getBasename().split(":")[1];
    }

    @Override
    public RDFURN join(String path) {
        RDFURN result = super.join(path);
        result.setAge(getAge());
        return result;
    }

    /**
     * {@link TypeConverter} for inter-conversion between {@link String} and {@link SessionID}
     * during protobuf conversion.
     */
    public static class Converter implements TypeConverter<SessionID, String> {
        @Override
        public SessionID toDomainValue(Object instance) {
            if (instance instanceof String) {
                return new SessionID((String) instance);
            }
            throw new RuntimeException("Failed to convert protobuf values to rdf values.");
        }

        @Override
        public String toProtobufValue(Object instance) {
            if (instance instanceof SessionID) {
                return ((SessionID) instance).stringify();
            }
            throw new RuntimeException("Failed to convert rdf values to protobuf values.");
        }
    }
}
