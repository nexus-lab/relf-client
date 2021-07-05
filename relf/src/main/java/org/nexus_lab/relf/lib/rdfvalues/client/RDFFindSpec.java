package org.nexus_lab.relf.lib.rdfvalues.client;

import org.nexus_lab.relf.lib.rdfvalues.RDFDatetime;
import org.nexus_lab.relf.lib.rdfvalues.paths.GlobExpression;
import org.nexus_lab.relf.lib.rdfvalues.paths.RDFPathSpec;
import org.nexus_lab.relf.lib.rdfvalues.standard.RegularExpression;
import org.nexus_lab.relf.lib.rdfvalues.structs.RDFProtoStruct;
import org.nexus_lab.relf.proto.FindSpec;

import net.badata.protobuf.converter.annotation.ProtoClass;
import net.badata.protobuf.converter.annotation.ProtoField;
import net.badata.protobuf.converter.inspection.DefaultValue;
import net.badata.protobuf.converter.mapping.Proto2MapperImpl;

import lombok.Getter;
import lombok.Setter;

/**
 * RDF wrapper of {@link FindSpec}
 *
 * @author Ruipeng Zhang
 */
@ProtoClass(value = FindSpec.class, mapper = Proto2MapperImpl.class)
public class RDFFindSpec extends RDFProtoStruct<FindSpec> {
    @Getter
    @Setter
    @ProtoField
    private RDFIterator iterator;
    @Getter
    @Setter
    @ProtoField
    private RDFPathSpec pathspec;
    @Getter
    @Setter
    @ProtoField(converter = GlobExpression.Converter.class)
    private GlobExpression pathGlob;
    @Getter
    @Setter
    @ProtoField(converter = RegularExpression.Converter.class)
    private RegularExpression pathRegex;
    @Getter
    @Setter
    @ProtoField(converter = RegularExpression.Converter.class)
    private RegularExpression dataRegex;
    @Getter
    @Setter
    @ProtoField(converter = RDFDatetime.Converter.class, defaultValue = StartTimeDefaultValueImpl.class)
    private RDFDatetime startTime;
    @Getter
    @Setter
    @ProtoField(converter = RDFDatetime.Converter.class, defaultValue = EndTimeDefaultValueImpl.class)
    private RDFDatetime endTime;
    @Getter
    @Setter
    @ProtoField(defaultValue = CrossDevDefaultValueImpl.class)
    private boolean crossDevs;
    @Getter
    @Setter
    @ProtoField(defaultValue = MaxDepthDefaultValueImpl.class)
    private Integer maxDepth = 15;
    @Getter
    @Setter
    @ProtoField
    private RDFStatEntry hit;
    @Getter
    @Setter
    @ProtoField(defaultValue = MaxDataDefaultValueImpl.class)
    private Long maxData = 1024000L;
    @Getter
    @Setter
    @ProtoField(defaultValue = MinFileSizeDefaultValueImpl.class)
    private Long minFileSize = 0L;
    @Getter
    @Setter
    @ProtoField(defaultValue = MaxFileSizeDefaultValueImpl.class)
    private Long maxFileSize = Long.MAX_VALUE;
    @Getter
    @Setter
    @ProtoField(defaultValue = PermMaskDefaultValueImpl.class)
    private Long permMask = 07777L;
    @Getter
    @Setter
    @ProtoField
    private Long permMode;
    @Getter
    @Setter
    @ProtoField
    private Long uid;
    @Getter
    @Setter
    @ProtoField
    private Long gid;

    /**
     * Default value for {@link RDFFindSpec#startTime}.
     */
    public static class StartTimeDefaultValueImpl implements DefaultValue {

        @Override
        public Object generateValue(Class<?> type) {
            return new RDFDatetime(0);
        }
    }

    /**
     * Default value for {@link RDFFindSpec#endTime}.
     */
    public static class EndTimeDefaultValueImpl implements DefaultValue {

        @Override
        public Object generateValue(Class<?> type) {
            return new RDFDatetime(Long.MAX_VALUE);
        }
    }

    /**
     * Default value for {@link RDFFindSpec#crossDevs}.
     */
    public static class CrossDevDefaultValueImpl implements DefaultValue {

        @Override
        public Object generateValue(Class<?> type) {
            return false;
        }
    }

    /**
     * Default value for {@link RDFFindSpec#maxDepth}.
     */
    public static class MaxDepthDefaultValueImpl implements DefaultValue {

        @Override
        public Object generateValue(Class<?> type) {
            return 15;
        }
    }

    /**
     * Default value for {@link RDFFindSpec#maxData}.
     */
    public static class MaxDataDefaultValueImpl implements DefaultValue {

        @Override
        public Object generateValue(Class<?> type) {
            return 1024000L;
        }
    }

    /**
     * Default value for {@link RDFFindSpec#minFileSize}.
     */
    public static class MinFileSizeDefaultValueImpl implements DefaultValue {

        @Override
        public Object generateValue(Class<?> type) {
            return 0L;
        }
    }

    /**
     * Default value for {@link RDFFindSpec#maxFileSize}.
     */
    public static class MaxFileSizeDefaultValueImpl implements DefaultValue {

        @Override
        public Object generateValue(Class<?> type) {
            return Long.MAX_VALUE;
        }
    }

    /**
     * Default value for {@link RDFFindSpec#permMask}.
     */
    public static class PermMaskDefaultValueImpl implements DefaultValue {

        @Override
        public Object generateValue(Class<?> type) {
            return 07777L;
        }
    }
}
