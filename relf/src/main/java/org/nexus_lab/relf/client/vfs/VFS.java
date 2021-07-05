package org.nexus_lab.relf.client.vfs;

import org.nexus_lab.relf.lib.rdfvalues.paths.RDFPathSpec;
import org.nexus_lab.relf.proto.PathSpec;
import org.nexus_lab.relf.utils.PathUtils;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Virtual File System helper class
 * TODO: refactor
 *
 * @author Ruipeng Zhang
 */
public final class VFS {
    private static final Map<PathSpec.PathType, Class<? extends VFSFile>> HANDLERS =
            new HashMap<>();
    private static final Map<PathSpec.PathType, RDFPathSpec> VIRTUALROOTS = new HashMap<>();

    static {
        HANDLERS.put(PathSpec.PathType.OS, OSFile.class);
        HANDLERS.put(PathSpec.PathType.UNSET, OSFile.class);
        RDFPathSpec osRoot = new RDFPathSpec();
        osRoot.setPath("/");
        osRoot.setPathtype(PathSpec.PathType.OS);
        osRoot.setIsVirtualroot(true);
        VIRTUALROOTS.put(PathSpec.PathType.OS, osRoot);
        VIRTUALROOTS.put(PathSpec.PathType.UNSET, osRoot);
    }

    private VFS() {
    }

    /**
     * Returns the name of the component which matches best our children file names
     *
     * @param file      file for matching
     * @param component a component name which should be present in this directory.
     * @return the best component name
     */
    public static RDFPathSpec matchBestComponentName(VFSFile file, String component) {
        if (!file.isDirectory()) {
            // currently do not support container objects like image or zip
            return null;
        }
        String newComponent = component;
        List<String> filenames = Arrays.asList(file.list());
        if (!filenames.contains(newComponent)) {
            String lowerComponent = newComponent.toLowerCase();
            for (String filename : filenames) {
                if (filename.toLowerCase().equals(lowerComponent)) {
                    newComponent = filename;
                    break;
                }
            }
        }
        RDFPathSpec result;
        if (file.getSupportedPathType() != file.getPathspec().getPathtype()) {
            result = new RDFPathSpec();
            result.setPathtype(file.getSupportedPathType());
        } else {
            result = (RDFPathSpec) file.getPathspec().last().duplicate();
        }
        result.setPath(newComponent);
        return result;
    }

    /**
     * Create a new {@link VFSFile} object from parameters
     *
     * @param type     file path type
     * @param file     parent directory {@link VFSFile} object
     * @param child    child file path relative to the parent
     * @param callback VFS open operation processing callback
     * @return {@link VFSFile} result
     * @throws IOException throw when there's {@link ReflectiveOperationException} in the
     *                     instantiation
     */
    private static VFSFile newFile(PathSpec.PathType type, VFSFile file,
            RDFPathSpec child, VFSFile.ProgressCallback callback) throws IOException {
        Class<? extends VFSFile> handler = HANDLERS.get(type);
        if (handler == null) {
            throw new IOException(String.format("VFS type %s not supported", type.name()));
        }
        try {
            return handler.getConstructor(VFSFile.class, RDFPathSpec.class,
                    VFSFile.ProgressCallback.class)
                    .newInstance(file, child, callback);
        } catch (ReflectiveOperationException e) {
            throw new IOException(
                    String.format("Cannot instantiate file handler %s", handler.getName()));
        }
    }

    /**
     * Try to open a component, if failed, try to correct the casing until we can not open the path
     * any more.
     *
     * @param file     the parent/upper directory file will use
     * @param child    the component file to be open
     * @param pathspec the rest of pathspec object
     * @param callback a callback to indicate that the open call is still working but needs more
     *                 time
     * @return a {@link VFSFile} object
     */
    private static VFSFile openFile(VFSFile file, RDFPathSpec child, RDFPathSpec pathspec,
            VFSFile.ProgressCallback callback) throws IOException {
        if (child.getPathOptions() == PathSpec.Options.CASE_LITERAL) {
            return newFile(child.getPathtype(), file, child, callback);
        }
        VFSFile newFile = file;
        String[] segments = PathUtils.normalize(
                child.getPath() == null ? "" : child.getPath()).split("/");
        List<String> components = new ArrayList<>(Arrays.asList(segments));
        components.add(0, "/");
        CollectionUtils.filter(components, StringUtils::isNotEmpty);
        for (int i = 0; i < components.size(); i++) {
            String pathComponent = components.get(i);
            RDFPathSpec newPathspec;
            if (newFile != null) {
                newPathspec = matchBestComponentName(newFile, pathComponent);
                if (newPathspec == null) {
                    continue;
                }
            } else {
                newPathspec = child;
                newPathspec.setPath(pathComponent);
            }
            try {
                newFile = newFile(newPathspec.getPathtype(), newFile, newPathspec, callback);
            } catch (IOException e) {
                // Can not open the first component, we must raise here.
                if (i <= 1) {
                    throw new FileNotFoundException();
                }
                // Insert the remaining path at the front of the pathspec.
                RDFPathSpec inserted = new RDFPathSpec();
                inserted.setPath(PathUtils.concat(components.subList(i, components.size())));
                inserted.setPathtype(PathSpec.PathType.TSK);
                pathspec.insert(0, inserted);
                break;
            }
        }
        return newFile;
    }

    /**
     * Expands pathspec to return an expanded Path.
     * <p>
     * A pathspec is a specification of how to access the file by recursively opening
     * each part of the path by different drivers.
     *
     * @param pathspec a Path() protobuf to normalize
     * @param callback a callback to indicate that the open call is still working but needs more
     *                 time
     * @return a {@link VFSFile} object. This will contain the expanded Path() protobuf as the
     * member fd.pathspec
     * @throws IOException unable to open the give path
     */
    public static VFSFile open(RDFPathSpec pathspec, VFSFile.ProgressCallback callback)
            throws IOException {
        RDFPathSpec workingPath;
        RDFPathSpec vroot = VIRTUALROOTS.get(pathspec.getPathtype());
        if (vroot == null || pathspec.getIsVirtualroot() || pathspec.collapse().startsWith(
                vroot.collapse())) {
            workingPath = (RDFPathSpec) pathspec.duplicate();
        } else {
            workingPath = (RDFPathSpec) vroot.duplicate();
            workingPath.last().setNestedPath((RDFPathSpec) pathspec.duplicate());
        }
        VFSFile file = null;
        while (workingPath.length() > 0) {
            RDFPathSpec component = workingPath.remove(0);
            file = openFile(file, component, workingPath, callback);
        }
        return file;
    }
}
