package org.nexus_lab.relf.client.actions.android;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.ACCESS_NETWORK_STATE;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.LOLLIPOP_MR1;

import static org.nexus_lab.relf.utils.ReflectionUtils.Parameter.from;
import static org.nexus_lab.relf.utils.ReflectionUtils.boxType;
import static org.nexus_lab.relf.utils.ReflectionUtils.callInstanceMethod;
import static org.nexus_lab.relf.utils.ReflectionUtils.setFieldValue;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.SystemClock;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellSignalStrength;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.RequiresApi;

import org.nexus_lab.relf.client.actions.Action;
import org.nexus_lab.relf.client.actions.ActionCallback;
import org.nexus_lab.relf.client.actions.UsesPermission;
import org.nexus_lab.relf.lib.rdfvalues.RDFDatetime;
import org.nexus_lab.relf.lib.rdfvalues.RDFNull;
import org.nexus_lab.relf.lib.rdfvalues.android.RDFAndroidCellInfo;
import org.nexus_lab.relf.lib.rdfvalues.android.RDFAndroidSubscriptionInfo;
import org.nexus_lab.relf.lib.rdfvalues.android.RDFAndroidTelephonyInfo;
import org.nexus_lab.relf.proto.AndroidCellInfo.CellType;
import org.nexus_lab.relf.proto.AndroidCellInfo.ConnectionStatus;
import org.nexus_lab.relf.proto.AndroidSubscriptionInfo.DataNetworkType;
import org.nexus_lab.relf.proto.AndroidSubscriptionInfo.RadioType;
import org.nexus_lab.relf.proto.AndroidSubscriptionInfo.ServiceState;
import org.nexus_lab.relf.proto.AndroidSubscriptionInfo.SimState;
import org.nexus_lab.relf.proto.AndroidTelephonyInfo.CallState;
import org.nexus_lab.relf.proto.AndroidTelephonyInfo.DataActivity;
import org.nexus_lab.relf.proto.AndroidTelephonyInfo.DataState;

/**
 * @author Ruipeng Zhang
 */
@UsesPermission(
        allOf = {READ_PHONE_STATE, ACCESS_NETWORK_STATE},
        anyOf = {ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION}
)
public class GetAndroidTelephonyInfo implements Action<RDFNull, RDFAndroidTelephonyInfo> {
    private static final String TAG = GetAndroidTelephonyInfo.class.getSimpleName();

    private void set(TelephonyManager manager, String getter, RDFAndroidSubscriptionInfo info,
            String setter, int sId) {
        try {
            Object value = callInstanceMethod(manager, getter, from(int.class, sId));
            callInstanceMethod(info, setter, from(boxType(value.getClass()), value));
        } catch (Exception e) {
            Log.w(TAG, e);
        }
    }

    private void setSubscriptionId(PhoneStateListener listener, int subscriptionId) {
        if (subscriptionId == -1) {
            return;
        }
        try {
            setFieldValue(PhoneStateListener.class, listener, "mSubId", subscriptionId);
        } catch (Exception e) {
            Log.w(TAG, e);
        }
    }

    @SuppressLint({"MissingPermission", "HardwareIds"})
    private void setDeviceId(TelephonyManager manager, RDFAndroidSubscriptionInfo info,
            int simSlotId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (manager.getImei() != null) {
                info.setDeviceId(manager.getImei());
            } else if (manager.getMeid() != null) {
                info.setDeviceId(manager.getMeid());
            } else {
                //noinspection deprecation
                info.setDeviceId(manager.getDeviceId());
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            info.setDeviceId(manager.getDeviceId(simSlotId));
        } else {
            set(manager, "getDeviceId", info, "setDeviceId", simSlotId);
        }
    }

    @SuppressWarnings({"JavaReflectionMemberAccess", "MissingPermission"})
    private void setServiceState(TelephonyManager manager, RDFAndroidSubscriptionInfo info,
            int subscriptionId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            android.telephony.ServiceState serviceState = manager.getServiceState();
            if (serviceState != null) {
                info.setServiceState(ServiceState.forNumber(serviceState.getState()));
                info.setServiceInfo(serviceState.toString());
            }
        } else {
            PhoneStateListener listener = new PhoneStateListener() {
                @Override
                public void onServiceStateChanged(android.telephony.ServiceState serviceState) {
                    info.setServiceState(ServiceState.forNumber(serviceState.getState()));
                    info.setServiceInfo(serviceState.toString());
                }
            };
            setSubscriptionId(listener, subscriptionId);
            // listener should be triggered instantly
            manager.listen(listener, PhoneStateListener.LISTEN_SERVICE_STATE);
            // unregister listener as soon as the listener is registered
            manager.listen(listener, PhoneStateListener.LISTEN_NONE);
        }
    }

    private void setSignalStrength(TelephonyManager manager, RDFAndroidSubscriptionInfo info,
            int subscriptionId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            SignalStrength signalStrength = manager.getSignalStrength();
            if (signalStrength != null) {
                info.setLevel(signalStrength.getLevel());
                info.setSignalStrength(signalStrength.toString());
            }
        } else {
            PhoneStateListener listener = new PhoneStateListener() {
                @Override
                public void onSignalStrengthsChanged(SignalStrength signalStrength) {
                    try {
                        info.setLevel(callInstanceMethod(signalStrength, "getLevel"));
                    } catch (Exception e) {
                        Log.w(TAG, e);
                    }
                    info.setSignalStrength(signalStrength.toString());
                }
            };
            setSubscriptionId(listener, subscriptionId);
            // listener should be triggered instantly
            manager.listen(listener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
            // unregister listener as soon as the listener is registered
            manager.listen(listener, PhoneStateListener.LISTEN_NONE);
        }
    }

    private RDFAndroidCellInfo createCellInfo(CellInfo cellInfo) {
        RDFAndroidCellInfo info = new RDFAndroidCellInfo();
        if (cellInfo instanceof CellInfoCdma) {
            info.setType(CellType.CDMA);
        } else if (cellInfo instanceof CellInfoGsm) {
            info.setType(CellType.GSM);
        } else if (cellInfo instanceof CellInfoLte) {
            info.setType(CellType.LTE);
        } else if (cellInfo instanceof CellInfoWcdma) {
            info.setType(CellType.WCDMA);
        } else {
            info.setType(CellType.CELL_UNKNOWN);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            info.setStatus(ConnectionStatus.forNumber(cellInfo.getCellConnectionStatus()));
        }
        // timestamp plus system boot time
        info.setTimestamp(new RDFDatetime(cellInfo.getTimeStamp() / 1000
                + (System.currentTimeMillis() - SystemClock.elapsedRealtime()) * 1000));
        info.setIsRegistered(cellInfo.isRegistered());
        if (info.getType() != CellType.CELL_UNKNOWN) {
            Class<? extends CellInfo> clazz = cellInfo.getClass();
            try {
                Object identity = callInstanceMethod(cellInfo, "getCellIdentity");
                CellSignalStrength signalStrength = callInstanceMethod(cellInfo,
                        "getCellSignalStrength");
                info.setLevel(signalStrength.getLevel());
                info.setIdentity(identity.toString());
                info.setSignalStrength(signalStrength.toString());
            } catch (Exception e) {
                Log.w(TAG, e);
            }
        }
        return info;
    }

    @RequiresApi(LOLLIPOP)
    @SuppressLint({"MissingPermission", "HardwareIds"})
    private RDFAndroidSubscriptionInfo createSubscription(TelephonyManager manager) {
        RDFAndroidSubscriptionInfo info = new RDFAndroidSubscriptionInfo();
        setServiceState(manager, info, -1);
        setSignalStrength(manager, info, -1);
        info.setSimSlot(0);
        info.setPhoneNumber(manager.getLine1Number());
        String networkOperator = manager.getNetworkOperator();
        if (!TextUtils.isEmpty(networkOperator)) {
            int mcc = Integer.parseInt(networkOperator.substring(0, 3));
            int mnc = Integer.parseInt(networkOperator.substring(3));
            info.setMcc(mcc);
            info.setMnc(mnc);
        }
        info.setCarrierName(manager.getSimOperatorName());
        info.setDeviceId(manager.getDeviceId());
        info.setSimSerialNumber(manager.getSimSerialNumber());
        info.setSimState(SimState.forNumber(manager.getSimState()));
        info.setVoicemailNumber(manager.getVoiceMailNumber());
        info.setSubscriberId(manager.getSubscriberId());
        info.setRadioType(RadioType.forNumber(manager.getPhoneType()));
        info.setIsRoaming(manager.isNetworkRoaming());
        info.setDataNetworkType(DataNetworkType.forNumber(manager.getNetworkType()));
        try {
            info.setIsDataEnabled(callInstanceMethod(manager, "getDataEnabled"));
        } catch (Exception e) {
            Log.w(TAG, e);
        }
        return info;
    }

    @RequiresApi(LOLLIPOP_MR1)
    @SuppressLint({"MissingPermission", "HardwareIds"})
    private RDFAndroidSubscriptionInfo createSubscription(TelephonyManager telephony,
            SubscriptionInfo subscription) {
        TelephonyManager manager = telephony;
        RDFAndroidSubscriptionInfo info = new RDFAndroidSubscriptionInfo();
        int simSlotIndex = subscription.getSimSlotIndex();
        int subscriptionId = subscription.getSubscriptionId();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            manager = manager.createForSubscriptionId(subscriptionId);
        }
        setServiceState(manager, info, subscriptionId);
        setSignalStrength(manager, info, subscriptionId);
        info.setSimSlot(simSlotIndex);
        info.setPhoneNumber(subscription.getNumber());
        info.setMcc(subscription.getMcc());
        info.setMnc(subscription.getMnc());
        info.setSubscriptionName((String) subscription.getDisplayName());
        info.setCarrierName((String) subscription.getCarrierName());
        setDeviceId(manager, info, simSlotIndex);
        set(manager, "getSimState", info, "setSimState", simSlotIndex);
        set(manager, "getSimSerialNumber", info, "setSimSerialNumber", subscriptionId);
        set(manager, "getVoiceMailNumber", info, "setVoicemailNumber", subscriptionId);
        set(manager, "getSubscriberId", info, "setSubscriberId", subscriptionId);
        set(manager, "getCurrentPhoneType", info, "setRadioType", subscriptionId);
        set(manager, "getDataNetworkType", info, "setDataNetworkType", subscriptionId);
        set(manager, "isNetworkRoaming", info, "setIsRoaming", subscriptionId);
        set(manager, "getDataEnabled", info, "setIsDataEnabled", subscriptionId);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            info.setIsConcurrentVoiceAndDataSupported(manager.isConcurrentVoiceAndDataSupported());
        }
        return info;
    }

    @Override
    @SuppressLint("MissingPermission")
    public void execute(Context context, RDFNull request, ActionCallback<RDFAndroidTelephonyInfo> callback) {
        RDFAndroidTelephonyInfo info = new RDFAndroidTelephonyInfo();
        TelephonyManager telephony = (TelephonyManager) context.getSystemService(
                Context.TELEPHONY_SERVICE);
        if (telephony == null) {
            throw new NullPointerException("Cannot access Android WiFi Service");
        }
        for (CellInfo cellInfo : telephony.getAllCellInfo()) {
            info.addCell(createCellInfo(cellInfo));
        }
        info.setCallState(CallState.forNumber(telephony.getCallState()));
        info.setDataState(DataState.forNumber(telephony.getDataState()));
        info.setDataActivity(DataActivity.forNumber(telephony.getDataActivity()));
        info.setIsSmsCapable(telephony.isSmsCapable());
        if (Build.VERSION.SDK_INT >= LOLLIPOP_MR1) {
            info.setIsVoiceCapable(telephony.isVoiceCapable());
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            info.setPhoneCount(telephony.getPhoneCount());
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            SubscriptionManager manager = (SubscriptionManager) context.getSystemService(
                    Context.TELEPHONY_SUBSCRIPTION_SERVICE);
            if (manager != null && manager.getActiveSubscriptionInfoList() != null) {
                for (SubscriptionInfo subscription : manager.getActiveSubscriptionInfoList()) {
                    info.addSubscription(createSubscription(telephony, subscription));
                }
            }
        } else {
            info.addSubscription(createSubscription(telephony));
        }
        callback.onResponse(info);
        callback.onComplete();
    }
}
