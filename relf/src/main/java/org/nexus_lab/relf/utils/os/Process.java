package org.nexus_lab.relf.utils.os;

import android.annotation.SuppressLint;
import android.system.Os;
import android.system.StructStat;
import android.util.Log;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nexus_lab.relf.service.RelfService;
import org.nexus_lab.relf.service.struct.StructPasswd;
import org.nexus_lab.relf.utils.PathUtils;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Ruipeng Zhang
 * @see <a href="http://man7.org/linux/man-pages/man5/proc.5.html">proc(5)</a>
 */
@SuppressWarnings({"checkstyle:methodname"})
public final class Process {
    private static final String TAG = Process.class.getSimpleName();

    private final int pid;

    /**
     * @param pid Process ID.
     */
    public Process(int pid) {
        this.pid = pid;
    }

    /**
     * @return {@link Process} of current application.
     */
    public static Process currentProcess() {
        return new Process(Os.getpid());
    }

    /**
     * @return all running processes.
     */
    public static Process[] allProcesses() {
        List<Process> processes = new ArrayList<>();
        String[] files = RelfService.listdir("/proc");
        if (files != null) {
            for (String file : files) {
                if (RelfService.isdir("/proc/" + file) && StringUtils.isNumeric(file)) {
                    processes.add(new Process(Integer.parseInt(file)));
                }
            }
        }
        return processes.toArray(new Process[0]);
    }

    /**
     * @return PID of the process.
     */
    public int pid() {
        return pid;
    }

    /**
     * @return The process name.
     * @see Stat#name()
     */
    public String name() {
        String name = stat().name();
        return name == null ? String.valueOf(pid) : name;
    }

    /**
     * @return the username of process's real user.
     */
    public String username() {
        int[] uids = status().uids();
        if (uids != null) {
            int real = uids[0];
            try {
                StructPasswd passwd = RelfService.getpwuid(real);
                if (passwd != null) {
                    return passwd.pw_name;
                }
            } catch (Exception ignored) {
            }
        }
        return String.valueOf(pid);
    }

    /**
     * @return the terminal device that the process is using.
     */
    public String terminal() {
        int tty = stat().tty_nr();
        Map<Long, String> terminalMap = getTerminalMap();
        return terminalMap.get((long) tty);
    }

    /**
     * @return The actual pathname of the executed command.
     */
    @Nullable
    public String exe() {
        String path = String.format(Locale.ENGLISH, "/proc/%d/exe", pid);
        try {
            return RelfService.readlink(path);
        } catch (IOException ignored) {
        }
        return null;
    }

    /**
     * @return The actual path to the current working directory of the process.
     */
    @Nullable
    public String cwd() {
        String path = String.format(Locale.ENGLISH, "/proc/%d/cwd", pid);
        try {
            return RelfService.readlink(path);
        } catch (IOException ignored) {
        }
        return null;
    }

    /**
     * @return Status information about the process.
     */
    public Stat stat() {
        return new Stat();
    }

    /**
     * @return Information about memory usage, measured in pages.
     */
    public Statm statm() {
        return new Statm();
    }

    /**
     * @return Information in /proc/[pid]/stat and /proc/[pid]/statm in a format that's easier for
     * humans to parse.
     */
    public Status status() {
        return new Status();
    }

    /**
     * @return I/O statistics for the process.
     */
    public IO io() {
        return new IO();
    }

    /**
     * @return This read-only file holds the complete command line for the process, unless the
     * process is a zombie. In the latter case, there is nothing in this file: that is, a read on
     * this file will return 0 characters. The command-line arguments appear in this file as a set
     * of strings separated by null bytes ('\0'), with a further null byte after the last string.
     */
    public String[] cmdline() {
        List<String> cmdline = new ArrayList<>();
        String path = String.format(Locale.ENGLISH, "/proc/%d/cmdline", pid);
        try (RelfService.LineReader reader = new RelfService.LineReader(path)) {
            String line = reader.readLine();
            if (line != null && !line.isEmpty()) {
                int i = 0;
                int j = 0;
                while (j < line.length()) {
                    if (line.charAt(j) == 0) {
                        if (i < j) {
                            cmdline.add(line.substring(i, j));
                        }
                        i = j + 1;
                        while (i < line.length() && line.charAt(i) == 0) {
                            i++;
                        }
                        j = i + 1;
                    } else {
                        j++;
                    }
                }
            }
        } catch (IOException ignored) {
        }
        return cmdline.toArray(new String[0]);
    }

    /**
     * @return files which the process has open, named by its file descriptor, and which is a
     * symbolic link to the actual file.  Thus, 0 is standard input, 1 standard output, 2 standard
     * error, and so on.
     */
    public Pair<Integer, String>[] fd() {
        List<Pair<Integer, String>> result = new ArrayList<>();
        String path = String.format(Locale.ENGLISH, "/proc/%d/fd", pid);
        String[] fds = RelfService.listdir(path);
        if (fds != null) {
            for (String fd : fds) {
                try {
                    String file = RelfService.readlink(path + "/" + fd);
                    if (file != null) {
                        result.add(new Pair<>(Integer.parseInt(fd), file));
                    }
                } catch (IOException ignored) {
                }
            }
        }
        return result.toArray(new Pair[0]);
    }

    /**
     * @return all sockets file descriptor and inode numbers opened by this process.
     */
    public Pair<Integer, Long>[] socket() {
        Pair<Integer, String>[] fd = fd();
        List<Pair<Integer, Long>> sockets = new ArrayList<>();
        for (Pair<Integer, String> file : fd) {
            String path = file.second;
            if (path != null && path.startsWith("socket:[")) {
                long inode = Long.parseLong(path.substring(8, path.length() - 1));
                sockets.add(new Pair<>(file.first, inode));
            }
        }
        return sockets.toArray(new Pair[0]);
    }

    @NonNull
    @Override
    public String toString() {
        return "Process(" + pid + ")";
    }

    /**
     * @return terminal device ID to file path mappings.
     */
    private Map<Long, String> getTerminalMap() {
        @SuppressLint("UseSparseArrays") Map<Long, String> map = new HashMap<>();
        List<String> devices = new ArrayList<>();
        String[] directories = RelfService.listdir("/dev");
        if (directories != null) {
            for (String device : directories) {
                if (device.startsWith("tty")) {
                    devices.add("/dev/" + device);
                }
            }
        }
        directories = RelfService.listdir("/dev/pts");
        if (directories != null) {
            for (String device : directories) {
                devices.add("/dev/pts/" + device);
            }
        }
        for (String device : devices) {
            try {
                StructStat stat = RelfService.stat(device);
                long minor = ((stat.st_rdev >> 20) << 8) | (stat.st_rdev & 0xff);
                long major = (stat.st_rdev >> 8) & 0xff;
                long ttyNr = major << 20 | minor;
                map.put(ttyNr, device);
            } catch (IOException ignored) {
            }
        }
        return map;
    }

    /**
     * Status information about the process.
     *
     * @see <a href="http://man7.org/linux/man-pages/man5/proc.5.html">proc(5)</a>
     */
    public class Stat {
        private MatchResult match;

        Stat() {
            parse("/proc/" + pid + "/stat");
        }

        private void parse(String path) {
            StringBuilder builder = new StringBuilder("(\\d+)\\s+\\((.+)\\)\\s+(\\w)");
            for (int i = 0; i < 20; i++) {
                builder.append("\\s+(-?\\d+)");
            }
            builder.append(".*");
            String regex = builder.toString();
            try (RelfService.LineReader reader = new RelfService.LineReader(path)) {
                String statLine = reader.readLine();
                if (statLine != null) {
                    Matcher matcher = Pattern.compile(regex).matcher(statLine);
                    if (matcher.matches()) {
                        match = matcher.toMatchResult();
                    } else {
                        Log.w(TAG, "\"" + statLine + "\" does not match \"" + regex + "\"");
                    }
                }
            } catch (IOException ignored) {
            }
        }

        private String matchGroup(int group) {
            return (match != null && match.groupCount() >= group) ? match.group(group + 1) : null;
        }

        /**
         * @return One of the following characters, indicating process state:
         * R  Running
         * S  Sleeping in an interruptible wait
         * D  Waiting in uninterruptible disk sleep
         * Z  Zombie
         * T  Stopped (on a signal) or (before Linux 2.6.33) trace stopped
         * t  Tracing stop (Linux 2.6.33 onward)
         * W  Paging (only before Linux 2.6.0)
         * X  Dead (from Linux 2.6.0 onward)
         * x  Dead (Linux 2.6.33 to 3.13 only)
         * K  Wakekill (Linux 2.6.33 to 3.13 only)
         * W  Waking (Linux 2.6.33 to 3.13 only)
         * P  Parked (Linux 3.9 to 3.13 only)
         */
        public String status() {
            return matchGroup(2);
        }

        /**
         * @return The filename of the executable, in parentheses. This is visible whether or not
         * the executable is swapped out.
         */
        @Nullable
        public String name() {
            String name = matchGroup(1);
            if (name != null) {
                // On UNIX the name gets truncated to the first 15 characters.
                // If it matches the first part of the cmdline we return that
                // one instead because it's usually more explicative.
                // Examples are "gnome-keyring-d" vs. "gnome-keyring-daemon".
                if (name.length() >= 15) {
                    String[] cmdline = cmdline();
                    if (cmdline.length > 0) {
                        String extendedName = PathUtils.getName(cmdline[0]);
                        if (extendedName.startsWith(name)) {
                            return extendedName;
                        }
                    }
                }
                return name;
            }
            return null;
        }

        /**
         * @return The PID of the parent of this process.
         */
        public int ppid() {
            String m = matchGroup(3);
            return m != null ? Integer.parseInt(m) : -1;
        }

        /**
         * @return The session ID of the process.
         */
        public int session() {
            String m = matchGroup(5);
            return m != null ? Integer.parseInt(m) : -1;
        }

        /**
         * @return The controlling terminal of the process. (The minor device number is contained in
         * the combination of bits 31 to 20 and 7 to 0; the major device number is in bits 15 to 8.)
         */
        public int tty_nr() {
            String m = matchGroup(6);
            return m != null ? Integer.parseInt(m) : -1;
        }

        /**
         * @return Amount of time that this process has been scheduled in user mode, measured in
         * clock ticks (divide by {@link android.system.OsConstants#_SC_CLK_TCK}).  This includes
         * guest time, guest_time (time spent running a virtual CPU, see below), so that
         * applications that are not aware of the guest time field do not lose that time from their
         * calculations.
         */
        public long utime() {
            String m = matchGroup(13);
            return m != null ? Long.parseLong(m) : -1;
        }

        /**
         * @return Amount of time that this process has been scheduled in kernel mode, measured in
         * clock ticks (divide by {@link android.system.OsConstants#_SC_CLK_TCK}).
         */
        public long stime() {
            String m = matchGroup(14);
            return m != null ? Long.parseLong(m) : -1;
        }

        /**
         * @return Amount of time that this process's waited-for children have been scheduled in
         * user mode, measured in clock ticks (divide by
         * {@link android.system.OsConstants#_SC_CLK_TCK}). (See also times(2).)  This includes
         * guest time, cguest_time (time spent running a virtual CPU, see below).
         */
        public long cutime() {
            String m = matchGroup(15);
            return m != null ? Long.parseLong(m) : -1;
        }

        /**
         * @return Amount of time that this process's waited-for children have been scheduled in
         * kernel mode, measured in clock ticks (divide by
         * {@link android.system.OsConstants#_SC_CLK_TCK}).
         */
        public long cstime() {
            String m = matchGroup(16);
            return m != null ? Long.parseLong(m) : -1;
        }

        /**
         * @return The nice value (see setpriority(2)), a value in the range 19 (low priority) to
         * -20 (high priority).
         */
        public int nice() {
            String m = matchGroup(18);
            return m != null ? Integer.parseInt(m) : Integer.MIN_VALUE;
        }

        /**
         * @return Number of threads in this process (since Linux 2.6). Before kernel 2.6, this
         * field was hard coded to 0 as a placeholder for an earlier removed field.
         */
        public int num_threads() {
            String m = matchGroup(19);
            return m != null ? Integer.parseInt(m) : -1;
        }

        /**
         * @return The time the process started after system boot. In kernels before Linux 2.6,
         * this value was expressed in jiffies.  Since Linux 2.6, the value is expressed in clock
         * ticks (divide by {@link android.system.OsConstants#_SC_CLK_TCK}).
         *
         * The format for this field was %lu before Linux 2.6.
         */
        public long starttime() {
            String m = matchGroup(21);
            return m != null ? Long.parseLong(m) : -1;
        }
    }

    /**
     * Information about memory usage, measured in pages.
     *
     * @see <a href="http://man7.org/linux/man-pages/man5/proc.5.html">proc(5)</a>
     */
    public class Statm {
        private String[] fields;

        Statm() {
            String path = String.format(Locale.ENGLISH, "/proc/%d/statm", pid);
            try (RelfService.LineReader reader = new RelfService.LineReader(path)) {
                String statmLine = reader.readLine();
                if (statmLine != null) {
                    fields = statmLine.split("\\s+");
                }
            } catch (IOException ignored) {
            }
        }

        /**
         * @return number of total program pages.
         */
        public long size() {
            return fields != null ? Long.parseLong(fields[0]) : -1;
        }

        /**
         * @return number of resident set pages.
         */
        public long resident() {
            return fields != null ? Long.parseLong(fields[1]) : -1;
        }

        /**
         * @return number of resident shared pages (i.e., backed by a file).
         */
        public long shared() {
            return fields != null ? Long.parseLong(fields[2]) : -1;
        }

        /**
         * @return text (code) pages.
         */
        public long text() {
            return fields != null ? Long.parseLong(fields[3]) : -1;
        }

        /**
         * @return library (unused since Linux 2.6; always 0) pages.
         */
        public long lib() {
            return fields != null ? Long.parseLong(fields[4]) : -1;
        }

        /**
         * @return data + stack pages.
         */
        public long data() {
            return fields != null ? Long.parseLong(fields[5]) : -1;
        }

        /**
         * @return dirty pages (unused since Linux 2.6; always 0).
         */
        public long dt() {
            return fields != null ? Long.parseLong(fields[6]) : -1;
        }
    }

    /**
     * Provides much of the information in /proc/[pid]/stat and /proc/[pid]/statm in a format that's
     * easier for humans to parse.
     *
     * @see <a href="http://man7.org/linux/man-pages/man5/proc.5.html">proc(5)</a>
     */
    public class Status {
        private Map<String, String> fields = new HashMap<>();

        Status() {
            String path = String.format(Locale.ENGLISH, "/proc/%d/status", pid);
            try (RelfService.LineReader reader = new RelfService.LineReader(path)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] segments = line.split(":");
                    if (segments.length > 1) {
                        String key = segments[0].trim();
                        String value = segments[1].trim();
                        fields.put(key, value);
                    }
                }
            } catch (IOException ignored) {
            }
        }

        /**
         * @return the real, effective, and saved UID of the process.
         */
        @Nullable
        public int[] uids() {
            String uid = fields.get("Uid");
            if (uid != null) {
                int[] uids = new int[3];
                String[] segments = uid.split("\\s+");
                for (int i = 0; i < 3; i++) {
                    uids[i] = Integer.parseInt(segments[i].trim());
                }
                return uids;
            }
            return null;
        }

        /**
         * @return the real, effective, and saved GID of the process.
         */
        @Nullable
        public int[] gids() {
            String gid = fields.get("Gid");
            if (gid != null) {
                int[] gids = new int[3];
                String[] segments = gid.split("\\s+");
                for (int i = 0; i < 3; i++) {
                    gids[i] = Integer.parseInt(segments[i].trim());
                }
                return gids;
            }
            return null;
        }
    }


    /**
     * I/O statistics for the process.
     *
     * @see <a href="http://man7.org/linux/man-pages/man5/proc.5.html">proc(5)</a>
     */
    public class IO {
        private Map<String, String> fields = new HashMap<>();

        IO() {
            String path = String.format(Locale.ENGLISH, "/proc/%d/io", pid);
            try (RelfService.LineReader reader = new RelfService.LineReader(path)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] segments = line.split(":");
                    if (segments.length > 1) {
                        String key = segments[0].trim();
                        String value = segments[1].trim();
                        fields.put(key, value);
                    }
                }
            } catch (IOException ignored) {
            }
        }

        /**
         * @return The number of bytes which this task has caused to be read from storage.
         */
        public long rchar() {
            String value = fields.get("rchar");
            return value != null ? Long.parseLong(value) : -1;
        }

        /**
         * @return The number of bytes which this task has caused, or shall cause to be written to disk.
         */
        public long wchar() {
            String value = fields.get("wchar");
            return value != null ? Long.parseLong(value) : -1;
        }

        /**
         * @return Attempt to count the number of read I/O operations (read syscalls).
         */
        public long syscr() {
            String value = fields.get("syscr");
            return value != null ? Long.parseLong(value) : -1;
        }

        /**
         * @return Attempt to count the number of write I/O operations (write syscalls).
         */
        public long syscw() {
            String value = fields.get("syscw");
            return value != null ? Long.parseLong(value) : -1;
        }

        /**
         * @return Attempt to count the number of bytes which this process really did cause to be
         * fetched from the storage layer.
         */
        public long read_bytes() {
            String value = fields.get("read_bytes");
            return value != null ? Long.parseLong(value) : -1;
        }

        /**
         * @return Attempt to count the number of bytes which this process caused to be sent to the storage layer.
         */
        public long write_bytes() {
            String value = fields.get("write_bytes");
            return value != null ? Long.parseLong(value) : -1;
        }

        /**
         * @return Represents the number of bytes which this process caused to not happen, by truncating page‐cache.
         */
        public long cancelled_write_bytes() {
            String value = fields.get("cancelled_write_bytes");
            return value != null ? Long.parseLong(value) : -1;
        }
    }
}
