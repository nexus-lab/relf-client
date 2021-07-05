package org.nexus_lab.relf.client;

import android.util.Log;

import org.nexus_lab.relf.lib.communicator.Communicator;
import org.nexus_lab.relf.lib.config.ConfigLib;
import org.nexus_lab.relf.lib.exceptions.CertificateException;
import org.nexus_lab.relf.lib.exceptions.DecodeException;
import org.nexus_lab.relf.lib.exceptions.VerificationException;
import org.nexus_lab.relf.lib.rdfvalues.RDFURN;
import org.nexus_lab.relf.lib.rdfvalues.client.ClientURN;
import org.nexus_lab.relf.lib.rdfvalues.crypto.RDFX509Cert;
import org.nexus_lab.relf.lib.rdfvalues.crypto.RSAPrivateKey;
import org.nexus_lab.relf.lib.rdfvalues.crypto.RSAPublicKey;


/**
 * Client side implementation of {@link Communicator}
 *
 * @author Ruipeng Zhang
 */
public class ClientCommunicator extends Communicator {
    private static final String TAG = ClientCommunicator.class.getSimpleName();

    /**
     * @param clientName client name in URN
     * @param serverName server name in URN
     * @param privateKey client local private key
     * @param publicKey  server public key
     */
    public ClientCommunicator(RDFURN clientName, RDFURN serverName, RSAPrivateKey privateKey,
            RSAPublicKey publicKey) {
        super(clientName, serverName, privateKey, publicKey);
    }

    private static void checkCertificateSerialNumber(RDFX509Cert certificate)
            throws CertificateException {
        int serialNumber = certificate.getSerialNumber().intValue();
        int localSerialNumber = ConfigLib.get("Client.server_serial_number", 0);
        if (serialNumber < localSerialNumber) {
            throw new CertificateException("Server certificate is too old.");
        } else if (serialNumber > localSerialNumber) {
            Log.i(TAG, "Server certificate serial number updated to " + serialNumber + ".");
            ConfigLib.set("Client.server_serial_number", serialNumber);
            ConfigLib.write();
        }
    }

    /**
     * Create {@link ClientCommunicator} from certificates and local private key
     *
     * @param caCertificate     server CA certificate
     * @param serverCertificate server certificate
     * @param privateKey        client local private key
     * @return client communicator
     * @throws CertificateException  failed to parse certificate, certificate is expired or
     *                               certificate is invalid
     * @throws VerificationException error verifying certificate using CA certificate
     * @throws DecodeException       cannot create client URN from private key
     */
    public static ClientCommunicator from(RDFX509Cert caCertificate, RDFX509Cert serverCertificate,
            RSAPrivateKey privateKey)
            throws CertificateException, VerificationException, DecodeException {
        if (!serverCertificate.verify(caCertificate.getPublicKey())) {
            throw new CertificateException("Server certificate is invalid.");
        }
        checkCertificateSerialNumber(serverCertificate);

        RDFURN clientName = ClientURN.fromPrivateKey(privateKey);
        RDFURN serverName = new RDFURN(serverCertificate.getCN());
        RSAPublicKey publicKey = serverCertificate.getPublicKey();
        return new ClientCommunicator(clientName, serverName, privateKey, publicKey);
    }

    /**
     * @return client name in URN
     */
    public RDFURN getClientName() {
        return getLocalName();
    }

    /**
     * @return server name in URN
     */
    public RDFURN getServerName() {
        return getRemoteName();
    }
}
