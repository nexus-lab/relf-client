package org.nexus_lab.relf.client.actions.network;

import android.content.Context;
import android.util.Pair;

import org.nexus_lab.relf.client.actions.Action;
import org.nexus_lab.relf.client.actions.ActionCallback;
import org.nexus_lab.relf.lib.rdfvalues.RDFNull;
import org.nexus_lab.relf.lib.rdfvalues.client.RDFNetworkConnection;
import org.nexus_lab.relf.proto.NetworkConnection;
import org.nexus_lab.relf.proto.NetworkEndpoint;
import org.nexus_lab.relf.utils.os.Net;
import org.nexus_lab.relf.utils.os.Process;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Ruipeng Zhang
 */
public class Netstat implements Action<RDFNull, RDFNetworkConnection> {
    private static final Map<Integer, NetworkConnection.State> TCP_STATES = new HashMap<>();

    static {
        TCP_STATES.put(-1, NetworkConnection.State.NONE);
        TCP_STATES.put(1, NetworkConnection.State.ESTABLISHED);
        TCP_STATES.put(2, NetworkConnection.State.SYN_SENT);
        TCP_STATES.put(3, NetworkConnection.State.SYN_RECV);
        TCP_STATES.put(4, NetworkConnection.State.FIN_WAIT1);
        TCP_STATES.put(5, NetworkConnection.State.FIN_WAIT2);
        TCP_STATES.put(6, NetworkConnection.State.TIME_WAIT);
        TCP_STATES.put(7, NetworkConnection.State.CLOSE);
        TCP_STATES.put(8, NetworkConnection.State.CLOSE_WAIT);
        TCP_STATES.put(9, NetworkConnection.State.LAST_ACK);
        TCP_STATES.put(10, NetworkConnection.State.LISTEN);
        TCP_STATES.put(11, NetworkConnection.State.CLOSING);
    }

    /**
     * Get all internet connections of a given process.
     *
     * @param process the process to be checked.
     * @return all internet connections.
     */
    public static List<RDFNetworkConnection> getInetConnections(Process process) {
        Set<Long> inodes = new HashSet<>();
        ArrayList<RDFNetworkConnection> result = new ArrayList<>();
        for (Pair<Integer, Long> socket : process.socket()) {
            inodes.add(socket.second);
        }
        for (Net.InetSocket socket : Net.inet()) {
            if (inodes.contains(socket.getInode())) {
                inodes.remove(socket.getInode());
                RDFNetworkConnection conn = new RDFNetworkConnection();
                conn.setFamily(NetworkConnection.Family.forNumber(socket.getFamily()));
                conn.setType(NetworkConnection.Type.forNumber(socket.getType()));
                conn.setPid(process.pid());
                conn.setState(TCP_STATES.get(socket.getState()));
                NetworkEndpoint local = NetworkEndpoint.newBuilder()
                        .setIp(socket.getLocalAddress().getHostAddress())
                        .setPort(socket.getLocalPort())
                        .build();
                conn.setLocalAddress(local);
                NetworkEndpoint remote = NetworkEndpoint.newBuilder()
                        .setIp(socket.getRemoteAddress().getHostAddress())
                        .setPort(socket.getRemotePort())
                        .build();
                conn.setRemoteAddress(remote);
                result.add(conn);
            }
        }
        return result;
    }

    @Override
    public void execute(Context context, RDFNull request, ActionCallback<RDFNetworkConnection> callback) {
        for (Process process : Process.allProcesses()) {
            for (RDFNetworkConnection connection : getInetConnections(process)) {
                connection.setPid(process.pid());
                connection.setProcessName(process.name());
                callback.onResponse(connection);
            }
        }
        callback.onComplete();
    }
}
