package org.nexus_lab.relf.utils.os;

import static android.system.OsConstants.AF_INET;
import static android.system.OsConstants.AF_INET6;
import static android.system.OsConstants.AF_UNIX;
import static android.system.OsConstants.SOCK_DGRAM;
import static android.system.OsConstants.SOCK_STREAM;

import android.util.Log;

import org.nexus_lab.relf.service.RelfService;
import org.nexus_lab.relf.utils.ByteUtils;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.Getter;

/**
 * @author Ruipeng Zhang
 * @see <a href="http://man7.org/linux/man-pages/man5/proc.5.html">proc(5)</a>
 */
public class Net {
    private static final String TAG = Net.class.getSimpleName();

    private static final String TCP_PATTERN =
            "^(?:\\d+:)\\s+([0-9a-fA-F]+):([0-9a-fA-F]+)\\s+([0-9a-fA-F]+):([0-9a-fA-F]+)\\s+"
                    + "([0-9a-fA-F]+)\\s+([0-9a-fA-F]+):([0-9a-fA-F]+)\\s+([0-9a-fA-F]+):"
                    + "([0-9a-fA-F]+)\\s+([0-9a-fA-F]+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)"
                    + "\\s+([0-9a-fA-F]+)(?:\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s*(-?\\d+))"
                    + "*$";
    private static final String UDP_PATTERN =
            "^(?:\\d+:)\\s+([0-9a-fA-F]+):([0-9a-fA-F]+)\\s+([0-9a-fA-F]+):([0-9a-fA-F]+)\\s+"
                    + "([0-9a-fA-F]+)\\s+([0-9a-fA-F]+):([0-9a-fA-F]+)\\s+([0-9a-fA-F]+):"
                    + "([0-9a-fA-F]+)\\s+([0-9a-fA-F]+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)"
                    + "\\s+([0-9a-fA-F]+)\\s+(\\d+)$";
    private static final String UNIX_PATTERN =
            "^(?:[0-9a-fA-F]+):\\s+([0-9a-fA-F]+)\\s+([0-9a-fA-F]+)\\s+([0-9a-fA-F]+)\\s+"
                    + "([0-9a-fA-F]+)\\s+([0-9a-fA-F]+)\\s+(\\d+)(?:\\s+(.*))?$";

    private final int family;
    private final int type;
    private final String regex;
    private final String path;

    /**
     * @param family IP protocol version.
     * @param type   socket type.
     * @param path   /proc/net/* file path;
     */
    public Net(int family, int type, String path) {
        this.type = type;
        this.path = path;
        this.family = family;
        if (type == SOCK_STREAM) {
            this.regex = TCP_PATTERN;
        } else if (type == SOCK_DGRAM) {
            this.regex = UDP_PATTERN;
        } else {
            this.regex = UNIX_PATTERN;
        }
    }

    /**
     * @return all IPv4 TCP sockets.
     */
    public static InetSocket[] tcp4() {
        return Arrays.asList(new Net(AF_INET, SOCK_STREAM, "/proc/net/tcp").parseSockets())
                .toArray(new InetSocket[0]);
    }

    /**
     * @return all IPv6 TCP sockets.
     */
    public static InetSocket[] tcp6() {
        return Arrays.asList(new Net(AF_INET6, SOCK_STREAM, "/proc/net/tcp6").parseSockets())
                .toArray(new InetSocket[0]);
    }

    /**
     * @return all IPv4 UDP sockets.
     */
    public static InetSocket[] udp4() {
        return Arrays.asList(new Net(AF_INET, SOCK_DGRAM, "/proc/net/udp").parseSockets())
                .toArray(new InetSocket[0]);
    }

    /**
     * @return all IPv6 UDP sockets.
     */
    public static InetSocket[] udp6() {
        return Arrays.asList(new Net(AF_INET6, SOCK_DGRAM, "/proc/net/udp6").parseSockets())
                .toArray(new InetSocket[0]);
    }

    /**
     * @return all IP sockets.
     */
    public static InetSocket[] inet() {
        InetSocket[] tcp = tcp4();
        InetSocket[] udp = udp4();
        InetSocket[] tcp6 = tcp6();
        InetSocket[] udp6 = udp6();
        InetSocket[] sockets = new InetSocket[tcp.length + udp.length + tcp6.length + udp6.length];
        System.arraycopy(tcp, 0, sockets, 0, tcp.length);
        System.arraycopy(udp, 0, sockets, tcp.length, udp.length);
        System.arraycopy(tcp6, 0, sockets, tcp.length + udp.length, tcp6.length);
        System.arraycopy(udp6, 0, sockets, tcp.length + udp.length + tcp6.length, udp6.length);
        return sockets;
    }

    /**
     * @return all Unix socket sockets.
     */
    public static UnixSocket[] unix() {
        return Arrays.asList(new Net(AF_UNIX, 0, "/proc/net/unix").parseSockets())
                .toArray(new UnixSocket[0]);
    }

    private MatchResult matchLine(String line) throws IOException {
        Matcher matcher = Pattern.compile(regex).matcher(line.trim());
        if (!matcher.matches()) {
            throw new IOException(
                    "Socket \"" + line.trim() + "\" does not match \"" + regex + "\"");
        }
        return matcher.toMatchResult();
    }

    /**
     * Process /proc/net/[tcp|tcp6|udp|udp6].
     */
    private InetSocket parseInetSocket(MatchResult match) throws IOException {
        long inode = Long.parseLong(match.group(13));
        InetSocket connection = new InetSocket(inode, family, type);
        if (type == SOCK_STREAM) {
            connection.state = Integer.parseInt(match.group(5), 16);
        } else {
            connection.state = -1;
        }
        connection.localAddress = ByteUtils.decodeAddress(match.group(1));
        connection.localPort = Integer.parseInt(match.group(2), 16);
        connection.remoteAddress = ByteUtils.decodeAddress(match.group(3));
        connection.remotePort = Integer.parseInt(match.group(4), 16);
        return connection;
    }

    /**
     * Process /proc/net/unix.
     */
    private UnixSocket parseUnixSocket(MatchResult match) {
        long inode = Long.parseLong(match.group(6));
        UnixSocket connection = new UnixSocket(inode, family, type);
        connection.path = match.group(7);
        return connection;
    }

    /**
     * @return all sockets parsed from the given /proc/net/* file.
     */
    private Socket[] parseSockets() {
        List<Socket> sockets = new ArrayList<>();
        try (RelfService.LineReader reader = new RelfService.LineReader(path)) {
            reader.readLine(); // skip first line
            String line;
            while ((line = reader.readLine()) != null) {
                MatchResult match = matchLine(line);
                if (family == AF_INET || family == AF_INET6) {
                    sockets.add(parseInetSocket(match));
                } else {
                    sockets.add(parseUnixSocket(match));
                }
            }
        } catch (IOException e) {
            Log.w(TAG, e);
        }
        return sockets.toArray(new Socket[0]);
    }

    /**
     * Object which represents a socket in /proc/net/[tcp|tcp6|udp|udp6|unix].
     * This class is read-only.
     */
    public static class Socket {
        @Getter
        private final long inode;
        @Getter
        private final int family;
        @Getter
        private final int type;

        /**
         * @param inode  socket inode number.
         * @param family socket address family.
         * @param type   socket type.
         */
        Socket(long inode, int family, int type) {
            this.inode = inode;
            this.family = family;
            this.type = type;
        }
    }

    /**
     * Object which represents a socket in /proc/net/[tcp|tcp6|udp|udp6].
     * This class is read-only.
     */
    public static class InetSocket extends Socket {
        @Getter
        protected int state;
        @Getter
        private InetAddress localAddress;
        @Getter
        private int localPort;
        @Getter
        private InetAddress remoteAddress;
        @Getter
        private int remotePort;

        /**
         * @inheritDoc
         */
        InetSocket(long inode, int family, int type) {
            super(inode, family, type);
        }
    }

    /**
     * A Unix socket connection.
     */
    public class UnixSocket extends Socket {
        @Getter
        private String path;

        /**
         * @inheritDoc
         */
        UnixSocket(long inode, int family, int type) {
            super(inode, family, type);
        }
    }
}
