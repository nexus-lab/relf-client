package org.nexus_lab.relf.robolectric;

import static android.os.Build.VERSION_CODES.LOLLIPOP_MR1;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.P;
import static android.telephony.PhoneStateListener.LISTEN_SERVICE_STATE;
import static android.telephony.PhoneStateListener.LISTEN_SIGNAL_STRENGTHS;
import static android.telephony.TelephonyManager.NETWORK_TYPE_UNKNOWN;

import static org.nexus_lab.relf.utils.ReflectionUtils.getFieldValue;

import android.os.Build;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;

import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import java.util.HashMap;
import java.util.Map;

import lombok.Setter;

/**
 * Robolectric shadow {@link TelephonyManager} class for testing.
 *
 * @author Ruipeng Zhang
 */
@Implements(value = TelephonyManager.class, minSdk = Build.VERSION_CODES.LOLLIPOP)
public class ShadowTelephonyManager extends org.robolectric.shadows.ShadowTelephonyManager {
    @Setter
    private int dataState = TelephonyManager.DATA_DISCONNECTED;
    @Setter
    private int dataActivity = TelephonyManager.DATA_ACTIVITY_DORMANT;
    @Setter
    private boolean isVoiceCapable;
    @Setter
    private int phoneCount;
    @Setter
    private boolean isConcurrentVoiceAndDataSupported;
    private Map<Integer, String> simSerialNumber = new HashMap<>();
    private Map<Integer, String> voiceMailNumber = new HashMap<>();
    private Map<Integer, String> subscriberId = new HashMap<>();
    private Map<Integer, Integer> dataNetworkType = new HashMap<>();
    private Map<Integer, Boolean> isNetworkRoaming = new HashMap<>();
    private Map<Integer, Boolean> isDataEnabled = new HashMap<>();
    private Map<Integer, String> IMEI = new HashMap<>();
    private Map<Integer, String> MEID = new HashMap<>();
    private Map<Integer, ServiceState> serviceState = new HashMap<>();
    private Map<Integer, SignalStrength> signalStrength = new HashMap<>();

    /**
     * @return a constant indicating the current data connection state (cellular).
     * @see TelephonyManager#DATA_DISCONNECTED
     * @see TelephonyManager#DATA_CONNECTING
     * @see TelephonyManager#DATA_CONNECTED
     * @see TelephonyManager#DATA_SUSPENDED
     */
    @Implementation
    protected int getDataState() {
        return dataState;
    }

    /**
     * @return a constant indicating the type of activity on a data connection (cellular).
     * @see TelephonyManager#DATA_ACTIVITY_NONE
     * @see TelephonyManager#DATA_ACTIVITY_IN
     * @see TelephonyManager#DATA_ACTIVITY_OUT
     * @see TelephonyManager#DATA_ACTIVITY_INOUT
     * @see TelephonyManager#DATA_ACTIVITY_DORMANT
     */
    @Implementation
    protected int getDataActivity() {
        return dataActivity;
    }

    /**
     * @return true if the current device is "voice capable".
     * @see TelephonyManager#isVoiceCapable()
     */
    @Implementation(minSdk = LOLLIPOP_MR1)
    protected boolean isVoiceCapable() {
        return isVoiceCapable;
    }

    /**
     * @return the number of phones available.
     * @see TelephonyManager#getPhoneCount()
     */
    @Implementation(minSdk = M)
    protected int getPhoneCount() {
        return phoneCount;
    }

    @Override
    protected String getDeviceId() {
        return getDeviceId(0);
    }

    @Override
    @Implementation(minSdk = LOLLIPOP_MR1)
    protected String getDeviceId(int slot) {
        return super.getDeviceId(slot);
    }

    /**
     * @param slotIndex of which IMEI is returned
     * @return the IMEI (International Mobile Equipment Identity). Return null if IMEI is not
     * available.
     */
    @Implementation(minSdk = O)
    protected String getImei(int slotIndex) {
        return IMEI.get(slotIndex);
    }

    @Override
    protected String getImei() {
        return getImei(0);
    }

    /**
     * Set IMEI value of the assigned slot which will be returned by {@link #getImei(int)}.
     *
     * @param slotIndex Slot index.
     * @param imei      IMEI value.
     */
    public void setImei(int slotIndex, String imei) {
        IMEI.put(slotIndex, imei);
    }

    /**
     * @param slotIndex of which MEID is returned
     * @return the MEID (Mobile Equipment Identifier). Return null if MEID is not available.
     */
    @Implementation(minSdk = O)
    protected String getMeid(int slotIndex) {
        return MEID.get(slotIndex);
    }

    /**
     * Set MEID value of the assigned slot which will be returned by {@link #getMeid(int)}.
     *
     * @param slotIndex Slot index.
     * @param meid      MEID value.
     */
    public void setMeid(int slotIndex, String meid) {
        MEID.put(slotIndex, meid);
    }

    @Override
    protected String getMeid() {
        return getMeid(0);
    }

    @Override
    protected String getSimSerialNumber() {
        return getSimSerialNumber(0);
    }

    /**
     * @param subId for which Sim Serial number is returned.
     * @return the serial number for the given subscription, if applicable. Return null if it is
     * unavailable.
     */
    @HiddenApi
    @Implementation(minSdk = LOLLIPOP_MR1)
    protected String getSimSerialNumber(int subId) {
        return simSerialNumber.get(subId);
    }

    /**
     * Set serial number of a SIM card with the subscription ID which will be returned by {@link
     * #getSimSerialNumber(int)}.
     *
     * @param subId        Subscription ID.
     * @param serialNumber SIM card serial number.
     */
    public void setSimSerialNumber(int subId, String serialNumber) {
        simSerialNumber.put(subId, serialNumber);
    }

    @Override
    @Implementation(minSdk = LOLLIPOP_MR1)
    protected int getSimState(int slotIndex) {
        return super.getSimState(slotIndex);
    }

    @Override
    protected String getVoiceMailNumber() {
        return getVoiceMailNumber(0);
    }

    /**
     * @param subId for which Sim Serial number is returned.
     * @return the voice mail number. Return null if it is unavailable.
     */
    @HiddenApi
    @Implementation(minSdk = LOLLIPOP_MR1)
    protected String getVoiceMailNumber(int subId) {
        return voiceMailNumber.get(subId);
    }

    /**
     * Set voice mail number of a subscription which will be returned by {@link
     * #getVoiceMailNumber(int)}.
     *
     * @param subId  Subscription ID.
     * @param number Voice mail phone number.
     */
    public void setVoiceMailNumber(int subId, String number) {
        voiceMailNumber.put(subId, number);
    }

    @Override
    protected String getSubscriberId() {
        return getSubscriberId(0);
    }

    /**
     * @param subId subscription ID.
     * @return the unique subscriber ID, for example, the IMSI for a GSM phone for a subscription.
     */
    @HiddenApi
    @Implementation(minSdk = LOLLIPOP_MR1)
    protected String getSubscriberId(int subId) {
        return subscriberId.get(subId);
    }

    /**
     * Set subscriber ID of a subscription which will be returned by {@link #getSubscriberId(int)}.
     *
     * @param subId Subscription ID.
     * @param id    Subscriber ID.
     */
    public void setSubscriberId(int subId, String id) {
        subscriberId.put(subId, id);
    }

    /**
     * @param subId Subscription ID.
     * @return a constant indicating the device phone type for a subscription.
     * @see TelephonyManager#PHONE_TYPE_NONE
     * @see TelephonyManager#PHONE_TYPE_GSM
     * @see TelephonyManager#PHONE_TYPE_CDMA
     * @see TelephonyManager#PHONE_TYPE_SIP
     */
    @HiddenApi
    @Implementation(minSdk = LOLLIPOP_MR1)
    protected int getCurrentPhoneType(int subId) {
        return super.getCurrentPhoneType(subId);
    }

    /**
     * @param subId Subscription ID.
     * @return true if the device is considered roaming on the current network for a subscription.
     */
    @HiddenApi
    @Implementation(minSdk = LOLLIPOP_MR1)
    @SuppressWarnings("ConstantConditions")
    protected boolean isNetworkRoaming(int subId) {
        return isNetworkRoaming.getOrDefault(subId, false);
    }

    /**
     * Set if the network of a subscription is roaming which will be returned by {@link
     * #isNetworkRoaming(int)}.
     *
     * @param subId     Subscription ID.
     * @param isRoaming True if the network is roaming.
     */
    public void setIsNetworkRoaming(int subId, boolean isRoaming) {
        isNetworkRoaming.put(subId, isRoaming);
    }

    /**
     * Returns a constant indicating the radio technology (network type)
     * currently in use on the device for data transmission.
     *
     * @param subId Subscription ID.
     * @return The network type.
     * @see TelephonyManager#NETWORK_TYPE_UNKNOWN
     * @see TelephonyManager#NETWORK_TYPE_GPRS
     * @see TelephonyManager#NETWORK_TYPE_EDGE
     * @see TelephonyManager#NETWORK_TYPE_UMTS
     * @see TelephonyManager#NETWORK_TYPE_HSDPA
     * @see TelephonyManager#NETWORK_TYPE_HSUPA
     * @see TelephonyManager#NETWORK_TYPE_HSPA
     * @see TelephonyManager#NETWORK_TYPE_CDMA
     * @see TelephonyManager#NETWORK_TYPE_EVDO_0
     * @see TelephonyManager#NETWORK_TYPE_EVDO_A
     * @see TelephonyManager#NETWORK_TYPE_EVDO_B
     * @see TelephonyManager#NETWORK_TYPE_1xRTT
     * @see TelephonyManager#NETWORK_TYPE_IDEN
     * @see TelephonyManager#NETWORK_TYPE_LTE
     * @see TelephonyManager#NETWORK_TYPE_EHRPD
     * @see TelephonyManager#NETWORK_TYPE_HSPAP
     */
    @HiddenApi
    @Implementation(minSdk = LOLLIPOP_MR1)
    @SuppressWarnings("ConstantConditions")
    protected int getDataNetworkType(int subId) {
        return dataNetworkType.getOrDefault(subId, NETWORK_TYPE_UNKNOWN);
    }

    /**
     * Set type of the network of a subscription which will be returned by {@link
     * #getDataNetworkType(int)}.
     *
     * @param subId Subscription ID.
     * @param type  Data network type.
     * @see TelephonyManager#NETWORK_TYPE_UNKNOWN
     * @see TelephonyManager#NETWORK_TYPE_GPRS
     * @see TelephonyManager#NETWORK_TYPE_EDGE
     * @see TelephonyManager#NETWORK_TYPE_UMTS
     * @see TelephonyManager#NETWORK_TYPE_HSDPA
     * @see TelephonyManager#NETWORK_TYPE_HSUPA
     * @see TelephonyManager#NETWORK_TYPE_HSPA
     * @see TelephonyManager#NETWORK_TYPE_CDMA
     * @see TelephonyManager#NETWORK_TYPE_EVDO_0
     * @see TelephonyManager#NETWORK_TYPE_EVDO_A
     * @see TelephonyManager#NETWORK_TYPE_EVDO_B
     * @see TelephonyManager#NETWORK_TYPE_1xRTT
     * @see TelephonyManager#NETWORK_TYPE_IDEN
     * @see TelephonyManager#NETWORK_TYPE_LTE
     * @see TelephonyManager#NETWORK_TYPE_EHRPD
     * @see TelephonyManager#NETWORK_TYPE_HSPAP
     */
    public void setDataNetworkType(int subId, int type) {
        dataNetworkType.put(subId, type);
    }

    /**
     * Returns whether mobile data is enabled or not per user setting.
     *
     * @return true if mobile data is enabled.
     */
    @HiddenApi
    @Implementation
    protected boolean getDataEnabled() {
        return getDataEnabled(0);
    }

    /**
     * @param subId Subscription ID.
     * @return whether mobile data is enabled or not per user setting.
     */
    @HiddenApi
    @Implementation(minSdk = LOLLIPOP_MR1)
    @SuppressWarnings("ConstantConditions")
    protected boolean getDataEnabled(int subId) {
        return isDataEnabled.getOrDefault(subId, false);
    }

    /**
     * Set if the data service of a subscription is enabled which will be returned by {@link
     * #getDataEnabled(int)}.
     *
     * @param subId     Subscription ID.
     * @param isEnabled True if the data service is enabled.
     */
    public void setDataEnabled(int subId, boolean isEnabled) {
        isDataEnabled.put(subId, isEnabled);
    }

    @Override
    protected ServiceState getServiceState() {
        if (serviceState.size() > 0) {
            return serviceState.values().iterator().next();
        }
        return null;
    }

    /**
     * Set the service state of a subscription.
     *
     * @param subId Subscription ID.
     * @param state Service state of the subscription.
     */
    public void setServiceState(int subId, ServiceState state) {
        serviceState.put(subId, state);
    }

    /**
     * @return The service state of a subscription.
     */
    @Implementation(minSdk = P)
    protected SignalStrength getSignalStrength() {
        if (signalStrength.size() > 0) {
            return signalStrength.values().iterator().next();
        }
        return null;
    }

    /**
     * Set the signal strength of a subscription.
     *
     * @param subId    Subscription ID.
     * @param strength Signal strength of the subscription.
     */
    public void setSignalStrength(int subId, SignalStrength strength) {
        signalStrength.put(subId, strength);
    }

    /**
     * Whether the device is currently on a technology (e.g. UMTS or LTE) which can support
     * voice and data simultaneously. This can change based on location or network condition.
     *
     * @return {@code true} if simultaneous voice and data supported, and {@code false} otherwise.
     */
    @Implementation(minSdk = O)
    protected boolean isConcurrentVoiceAndDataSupported() {
        return isConcurrentVoiceAndDataSupported;
    }

    @Override
    protected void listen(PhoneStateListener listener, int flags) {
        super.listen(listener, flags);
        int subId = 0;
        if (Build.VERSION.SDK_INT >= LOLLIPOP_MR1) {
            try {
                subId = getFieldValue(PhoneStateListener.class, listener, "mSubId");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        if ((flags & LISTEN_SERVICE_STATE) != 0) {
            listener.onServiceStateChanged(serviceState.get(subId));
        }
        if ((flags & LISTEN_SIGNAL_STRENGTHS) != 0) {
            listener.onSignalStrengthsChanged(signalStrength.get(subId));
        }
    }
}
