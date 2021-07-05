package org.nexus_lab.relf.service.struct;

/**
 * A Java object for <code>struct passwd</code>.
 * @see <a href="http://man7.org/linux/man-pages/man3/getpwnam.3.html">getpwnam(3)</a>
 */
@SuppressWarnings({"checkstyle:membername", "checkstyle:visibilitymodifier"})
public class StructPasswd {
    /** Username. */
    public String pw_name;
    /** Password. */
    public String pw_passwd;
    /** User ID. */
    public int pw_uid;
    /** Group ID. */
    public int pw_gid;
    /** Real name. */
    public String pw_gecos;
    /** Home directory. */
    public String pw_dir;
    /** Shell program. */
    public String pw_shell;
}
