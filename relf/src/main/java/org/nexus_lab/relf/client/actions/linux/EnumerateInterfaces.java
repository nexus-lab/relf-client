package org.nexus_lab.relf.client.actions.linux;

import android.content.Context;

import org.nexus_lab.relf.client.actions.Action;
import org.nexus_lab.relf.client.actions.ActionCallback;
import org.nexus_lab.relf.lib.rdfvalues.RDFBytes;
import org.nexus_lab.relf.lib.rdfvalues.RDFNull;
import org.nexus_lab.relf.lib.rdfvalues.client.MacAddress;
import org.nexus_lab.relf.lib.rdfvalues.client.RDFInterface;
import org.nexus_lab.relf.lib.rdfvalues.client.RDFNetworkAddress;
import org.nexus_lab.relf.proto.NetworkAddress;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * @author Ruipeng Zhang
 */
public class EnumerateInterfaces implements Action<RDFNull, RDFInterface> {
    @Override
    public void execute(Context context, RDFNull request, ActionCallback<RDFInterface> callback) {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                RDFInterface ifaceValue = new RDFInterface();
                ifaceValue.setIfname(iface.getName());
                if (iface.getHardwareAddress() != null) {
                    ifaceValue.setMacAddress(new MacAddress(iface.getHardwareAddress()));
                }

                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    RDFNetworkAddress addressValue = new RDFNetworkAddress();
                    if (address instanceof Inet6Address) {
                        addressValue.setAddressType(NetworkAddress.Family.INET6);
                    } else {
                        addressValue.setAddressType(NetworkAddress.Family.INET);
                    }
                    addressValue.setPackedBytes(new RDFBytes(address.getAddress()));
                    ifaceValue.addAddress(addressValue);
                }

                callback.onResponse(ifaceValue);
            }
            callback.onComplete();
        } catch (SocketException e) {
            callback.onError(e);
        }
    }
}
