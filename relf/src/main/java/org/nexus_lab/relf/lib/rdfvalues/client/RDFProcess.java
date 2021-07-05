package org.nexus_lab.relf.lib.rdfvalues.client;

import org.nexus_lab.relf.lib.rdfvalues.structs.RDFProtoStruct;
import org.nexus_lab.relf.proto.Process;

import net.badata.protobuf.converter.annotation.ProtoClass;
import net.badata.protobuf.converter.annotation.ProtoField;
import net.badata.protobuf.converter.mapping.Proto2MapperImpl;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * RDF wrapper of {@link org.nexus_lab.relf.proto.Process}
 *
 * @author Ruipeng Zhang
 */
@ProtoClass(value = Process.class, mapper = Proto2MapperImpl.class)
public class RDFProcess extends RDFProtoStruct<Process> {
    @Getter
    @Setter
    @ProtoField
    private Integer pid;
    @Getter
    @Setter
    @ProtoField
    private Integer ppid;
    @Getter
    @Setter
    @ProtoField
    private String name;
    @Getter
    @Setter
    @ProtoField
    private String exe;
    @Getter
    @Setter
    @ProtoField
    private List<String> cmdline;
    @Getter
    @Setter
    @ProtoField
    private Long ctime;
    @Getter
    @Setter
    @ProtoField
    private Integer realUid;
    @Getter
    @Setter
    @ProtoField
    private Integer effectiveUid;
    @Getter
    @Setter
    @ProtoField
    private Integer savedUid;
    @Getter
    @Setter
    @ProtoField
    private Integer realGid;
    @Getter
    @Setter
    @ProtoField
    private Integer effectiveGid;
    @Getter
    @Setter
    @ProtoField
    private Integer savedGid;
    @Getter
    @Setter
    @ProtoField
    private String username;
    @Getter
    @Setter
    @ProtoField
    private String terminal;
    @Getter
    @Setter
    @ProtoField
    private String status;
    @Getter
    @Setter
    @ProtoField
    private Integer nice;
    @Getter
    @Setter
    @ProtoField
    private String cwd;
    @Getter
    @Setter
    @ProtoField
    private Integer numThreads;
    @Getter
    @Setter
    @ProtoField
    private Float userCpuTime;
    @Getter
    @Setter
    @ProtoField
    private Float systemCpuTime;
    @Getter
    @Setter
    @ProtoField
    private Float cpuPercent;
    @Getter
    @Setter
    @ProtoField(name = "RSSSize")
    private Long rssSize;
    @Getter
    @Setter
    @ProtoField(name = "VMSSize")
    private Long vmsSize;
    @Getter
    @Setter
    @ProtoField
    private Float memoryPercent;
    @Getter
    @Setter
    @ProtoField
    private List<String> openFiles;
    @Getter
    @Setter
    @ProtoField
    private List<RDFNetworkConnection> connections;

    /**
     * Add a file path to the open file list.
     *
     * @param path open file path.
     */
    public void addOpenFile(String path) {
        if (openFiles == null) {
            openFiles = new ArrayList<>();
        }
        openFiles.add(path);
    }

    /**
     * Set the real, effective, and saved UID of the process.
     *
     * @param uids real, effective, and saved UID.
     */
    public void setUids(int[] uids) {
        if (uids != null && uids.length == 3) {
            setRealUid(uids[0]);
            setEffectiveUid(uids[1]);
            setSavedUid(uids[2]);
        }
    }

    /**
     * Set the real, effective, and saved GID of the process.
     *
     * @param gids real, effective, and saved GID.
     */
    public void setGids(int[] gids) {
        if (gids != null && gids.length == 3) {
            setRealGid(gids[0]);
            setEffectiveGid(gids[1]);
            setSavedGid(gids[2]);
        }
    }

    /**
     * Add a network connection to the process connection list.
     *
     * @param connection network connection.
     */
    public void addConnection(RDFNetworkConnection connection) {
        if (connections == null) {
            connections = new ArrayList<>();
        }
        connections.add(connection);
    }

    /**
     * Add a cmdline argument to the process cmdline arguments list.
     *
     * @param cmdline cmdline argument.
     */
    public void addCmdline(String cmdline) {
        if (this.cmdline == null) {
            this.cmdline = new ArrayList<>();
        }
        this.cmdline.add(cmdline);
    }
}
