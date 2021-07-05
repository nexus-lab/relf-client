package org.nexus_lab.relf.lib.rdfvalues.paths;

import org.nexus_lab.relf.lib.rdfvalues.ByteSize;
import org.nexus_lab.relf.lib.rdfvalues.RDFURN;
import org.nexus_lab.relf.lib.rdfvalues.client.ClientURN;
import org.nexus_lab.relf.lib.rdfvalues.structs.RDFProtoStruct;
import org.nexus_lab.relf.proto.PathSpec;
import org.nexus_lab.relf.utils.PathUtils;

import net.badata.protobuf.converter.annotation.ProtoClass;
import net.badata.protobuf.converter.annotation.ProtoField;
import net.badata.protobuf.converter.inspection.DefaultValue;
import net.badata.protobuf.converter.mapping.Proto2MapperImpl;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import lombok.Getter;
import lombok.Setter;

/**
 * RDF wrapper of {@link PathSpec}
 *
 * @author Ruipeng Zhang
 */
@ProtoClass(value = PathSpec.class, mapper = Proto2MapperImpl.class)
public class RDFPathSpec extends RDFProtoStruct<PathSpec> implements Iterable<RDFPathSpec> {
    private static final String[] AFF4_PREFIXES = new String[]{
            "/fs/os",
            "/fs/tsk",
            "/registry",
            "/devices/memory",
            "/temp",
    };

    @Getter
    @Setter
    @ProtoField(defaultValue = PathTypeDefaultValueImpl.class)
    private PathSpec.PathType pathtype = PathSpec.PathType.UNSET;
    @Getter
    @Setter
    @ProtoField
    private String path;
    @Getter
    @Setter
    @ProtoField
    private String mountPoint;
    @Getter
    @Setter
    @ProtoField
    private String streamName;
    @Getter
    @Setter
    @ProtoField
    private RDFPathSpec nestedPath;
    @Getter
    @Setter
    @ProtoField
    private Long offset;
    @Getter
    @Setter
    @ProtoField
    private PathSpec.Options pathOptions;
    @Getter
    @Setter
    @ProtoField
    private Long recursionDepth;
    @Getter
    @Setter
    @ProtoField(converter = ByteSize.Converter.class)
    private ByteSize fileSizeOverride;
    @Getter
    @Setter
    @ProtoField(defaultValue = VirtualRootDefaultValueImpl.class)
    private Boolean isVirtualroot = false;

    public RDFPathSpec() {
        super();
    }

    /**
     * @return path nesting depth when child path is not of unknown {@link PathSpec.PathType}
     */
    public int length() {
        int size = 0;
        for (RDFPathSpec ignored : this) {
            size++;
        }
        return size;
    }

    /**
     * Get child path of the given depth
     *
     * @param index child path depth
     * @return child path
     */
    public RDFPathSpec get(int index) {
        int i = 0;
        for (RDFPathSpec path : this) {
            if (i == index) {
                return path;
            }
            i++;
        }
        throw new IndexOutOfBoundsException(
                String.format("PathSpec index (%s) is out of range.", index));
    }

    @Override
    public Iterator<RDFPathSpec> iterator() {
        return new Iterator<RDFPathSpec>() {
            private RDFPathSpec last = RDFPathSpec.this;

            @Override
            public boolean hasNext() {
                return last != null && last.getPathtype() != null;
            }

            @Override
            public RDFPathSpec next() {
                RDFPathSpec result = last;
                if (last.getNestedPath() != null) {
                    last = last.getNestedPath();
                } else {
                    last = null;
                }
                return result;
            }
        };
    }

    /**
     * @return the deepest child path
     */
    public RDFPathSpec last() {
        int length = length();
        if (length >= 1 && getPathtype() != null && getPathtype() != PathSpec.PathType.UNSET) {
            return get(length - 1);
        }
        return this;
    }

    /**
     * Insert a path segment to a certain depth
     *
     * @param index child path depth where the new segment is inserted
     * @param path  path segment to be inserted
     */
    public void insert(int index, RDFPathSpec path) {
        if (index == 0) {
            RDFPathSpec nested = (RDFPathSpec) this.duplicate();
            this.copyFrom(path);
            this.last().setNestedPath(nested);
        } else {
            RDFPathSpec previous = get(index - 1);
            RDFPathSpec newpath = (RDFPathSpec) path.duplicate();
            newpath.last().setNestedPath(previous.getNestedPath());
            previous.setNestedPath(newpath);
        }
    }

    /**
     * Insert a path segment as the child path of the deepest child path.
     *
     * @param component path segment to be inserted
     */
    public void append(RDFPathSpec component) {
        if (getPathtype() != null) {
            this.last().setNestedPath(component);
        } else {
            this.copyFrom(component);
        }
    }

    /**
     * Concat all the path segments to a path string
     *
     * @return concated path string
     */
    public String collapse() {
        RDFPathSpec path = this;
        List<String> paths = new ArrayList<>();
        if (path.getPath() != null) {
            paths.add(path.getPath());
        }
        while (path.getNestedPath() != null) {
            path = path.getNestedPath();
            paths.add(path.getPath());
        }
        return PathUtils.normalize(PathUtils.concat(paths.toArray(new String[0])));
    }

    /**
     * Remove a child path segment of the given depth (will create a new instance).
     *
     * @param index child path segment depth
     * @return a copy of edited {@link RDFPathSpec}
     */
    public RDFPathSpec remove(int index) {
        RDFPathSpec result;
        if (index == 0) {
            result = (RDFPathSpec) this.duplicate();
            RDFPathSpec nestedPath = getNestedPath();
            if (nestedPath != null) {
                this.copyFrom(nestedPath);
                this.setAge(nestedPath.getAge());
            } else {
                this.copyFrom(new RDFPathSpec());
                this.setPathtype(null);
            }
        } else {
            RDFPathSpec previous = get(index - 1);
            result = previous.getNestedPath();
            previous.setNestedPath(result.getNestedPath());
        }
        result.setNestedPath(null);
        return result;
    }

    /**
     * Get the directory {@link RDFPathSpec} which is the current {@link RDFPathSpec} but without
     * deepest child path
     *
     * @return a copy of edited {@link RDFPathSpec}
     */
    public RDFPathSpec directory() {
        RDFPathSpec result = (RDFPathSpec) this.duplicate();
        String lastDirectory;
        while (true) {
            lastDirectory = PathUtils.getPath(result.last().getPath());
            if (!lastDirectory.equals("/") || result.length() <= 1) {
                result.last().setPath(lastDirectory);
                break;
            }
            result.remove(result.length() - 1);
        }
        return result;
    }

    /**
     * @return the base name of the whole path
     */
    public String basename() {
        Stack<RDFPathSpec> paths = new Stack<>();
        for (RDFPathSpec path : this) {
            paths.add(path);
        }
        while (!paths.empty()) {
            RDFPathSpec path = paths.pop();
            if (path.getPath() != null) {
                return PathUtils.getName(path.getPath());
            }
        }
        return "";
    }

    /**
     * @return true if the current {@link PathSpec.PathType} is set
     */
    public boolean validate() {
        return getPathtype() != null && getPathtype() != PathSpec.PathType.UNSET;
    }

    /**
     * Append current {@link PathSpec} to a {@link ClientURN}
     *
     * @param clientURN client URN to be appended
     * @return result {@link RDFURN}
     */
    public RDFURN aff4Path(ClientURN clientURN) {
        if (getPathtype() == null) {
            throw new RuntimeException("Can't determine AFF4 path without a valid pathtype.");
        }
        RDFPathSpec first = this;
        String dev = first.getPath();
        if (first.getOffset() != null) {
            dev += ":" + String.valueOf(first.getOffset() / 512);
        }
        int start;
        List<String> result = new ArrayList<>();
        if (length() > 1 && first.getPathtype() == PathSpec.PathType.OS
                && get(1).getPathtype() == PathSpec.PathType.TSK) {
            result.add(AFF4_PREFIXES[PathSpec.PathType.TSK_VALUE]);
            result.add(dev);
            start = 1;
        } else {
            result.add(AFF4_PREFIXES[first.getPathtype().getNumber()]);
            start = 0;
        }

        for (int i = start; i < length(); i++) {
            RDFPathSpec path = get(i);
            String component = path.getPath();
            if (path.getOffset() != null) {
                component += ":" + String.valueOf(path.getOffset() / 512);
            }
            if (path.getStreamName() != null) {
                component += ":" + path.getStreamName();
            }
            result.add(component);
        }
        return clientURN.add(StringUtils.join(result, "/"));
    }

    /**
     * Default value for {@link RDFPathSpec#pathtype}.
     */
    public static class PathTypeDefaultValueImpl implements DefaultValue {

        @Override
        public Object generateValue(Class<?> type) {
            return PathSpec.PathType.UNSET;
        }
    }

    /**
     * Default value for {@link RDFPathSpec#isVirtualroot}.
     */
    public static class VirtualRootDefaultValueImpl implements DefaultValue {

        @Override
        public Object generateValue(Class<?> type) {
            return false;
        }
    }
}
