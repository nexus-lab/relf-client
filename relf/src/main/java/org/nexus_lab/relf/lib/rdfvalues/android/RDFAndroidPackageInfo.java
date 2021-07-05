package org.nexus_lab.relf.lib.rdfvalues.android;

import com.google.protobuf.ByteString;
import org.nexus_lab.relf.lib.rdfvalues.RDFDatetime;
import org.nexus_lab.relf.lib.rdfvalues.crypto.RDFX509Cert;
import org.nexus_lab.relf.lib.rdfvalues.structs.RDFProtoStruct;
import org.nexus_lab.relf.proto.AndroidPackageInfo;

import net.badata.protobuf.converter.annotation.ProtoClass;
import net.badata.protobuf.converter.annotation.ProtoField;
import net.badata.protobuf.converter.mapping.Proto2MapperImpl;
import net.badata.protobuf.converter.type.TypeConverter;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Ruipeng Zhang
 */
@ProtoClass(value = AndroidPackageInfo.class, mapper = Proto2MapperImpl.class)
public class RDFAndroidPackageInfo extends RDFProtoStruct<AndroidPackageInfo> {
    @Getter
    @Setter
    @ProtoField
    private String name;
    @Getter
    @Setter
    @ProtoField
    private String packageName;
    @Getter
    @Setter
    @ProtoField
    private String versionName;
    @Getter
    @Setter
    @ProtoField
    private Long versionCode;
    @Getter
    @Setter
    @ProtoField
    private Integer minSdkVersion;
    @Getter
    @Setter
    @ProtoField
    private Integer targetSdkVersion;
    @Getter
    @Setter
    @ProtoField
    private AndroidPackageInfo.Category category;
    @Getter
    @Setter
    @ProtoField
    private Boolean isEnabled;
    @Getter
    @Setter
    @ProtoField
    private Integer uid;
    @Getter
    @Setter
    @ProtoField
    private List<Integer> gids;
    @Getter
    @Setter
    @ProtoField
    private String sharedUserId;
    @Getter
    @Setter
    @ProtoField(converter = RDFDatetime.Converter.class)
    private RDFDatetime firstInstallTime;
    @Getter
    @Setter
    @ProtoField(converter = RDFDatetime.Converter.class)
    private RDFDatetime lastUpdateTime;
    @Getter
    @Setter
    @ProtoField
    private AndroidPackageInfo.InstallLocation installLocation;
    @Getter
    @Setter
    @ProtoField
    private String sourceDir;
    @Getter
    @Setter
    @ProtoField
    private String dataDir;
    @Getter
    @Setter
    @ProtoField
    private List<String> requestedPermissions;
    @Getter
    @Setter
    @ProtoField(converter = SignaturesConverter.class)
    private List<RDFX509Cert> signatures;

    /**
     * Add a group ID to the package's group ID list.
     *
     * @param gid group ID to be added.
     */
    public void addGid(int gid) {
        if (gids == null) {
            gids = new ArrayList<>();
        }
        gids.add(gid);
    }

    /**
     * Add a package signature to the package's signature list.
     *
     * @param signature signature to be added.
     */
    public void addSignature(RDFX509Cert signature) {
        if (signatures == null) {
            signatures = new ArrayList<>();
        }
        signatures.add(signature);
    }

    /**
     * {@link TypeConverter} for inter-conversion between {@link List<RDFX509Cert>} and {@link
     * List<ByteString>} during protobuf conversion.
     */
    public static class SignaturesConverter implements
            TypeConverter<List<RDFX509Cert>, List<ByteString>> {
        private RDFX509Cert.Converter converter = new RDFX509Cert.Converter();

        @Override
        public List<RDFX509Cert> toDomainValue(Object instance) {
            List<RDFX509Cert> signatures = new ArrayList<>();
            for (ByteString protobuf : ((List<ByteString>) instance)) {
                signatures.add(converter.toDomainValue(protobuf));
            }
            return signatures;
        }

        @Override
        public List<ByteString> toProtobufValue(Object instance) {
            List<ByteString> protobufs = new ArrayList<>();
            for (RDFX509Cert signature : ((List<RDFX509Cert>) instance)) {
                protobufs.add(converter.toProtobufValue(signature));
            }
            return protobufs;
        }
    }
}
