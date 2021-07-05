package org.nexus_lab.relf.client.actions.android;

import static android.Manifest.permission.READ_PHONE_STATE;
import static android.os.Build.VERSION_CODES.LOLLIPOP_MR1;
import static android.telephony.PhoneStateListener.LISTEN_NONE;
import static android.telephony.PhoneStateListener.LISTEN_SERVICE_STATE;
import static android.telephony.PhoneStateListener.LISTEN_SIGNAL_STRENGTHS;

import static org.nexus_lab.relf.utils.ReflectionUtils.Parameter.from;
import static org.nexus_lab.relf.utils.ReflectionUtils.callConstructor;
import static org.nexus_lab.relf.utils.ReflectionUtils.callInstanceMethod;
import static org.nexus_lab.relf.utils.ReflectionUtils.setFieldValue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.SystemClock;
import android.telephony.CellIdentity;
import android.telephony.CellIdentityCdma;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellSignalStrength;
import android.telephony.CellSignalStrengthCdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;

import androidx.annotation.RequiresApi;
import androidx.test.core.app.ApplicationProvider;

import com.google.protobuf.Internal;
import org.nexus_lab.relf.client.actions.ActionCallback;
import org.nexus_lab.relf.client.exceptions.TestSuccessException;
import org.nexus_lab.relf.lib.rdfvalues.android.RDFAndroidCellInfo;
import org.nexus_lab.relf.lib.rdfvalues.android.RDFAndroidSubscriptionInfo;
import org.nexus_lab.relf.lib.rdfvalues.android.RDFAndroidTelephonyInfo;
import org.nexus_lab.relf.proto.AndroidSubscriptionInfo;
import org.nexus_lab.relf.robolectric.ShadowTelephonyManager;
import org.nexus_lab.relf.utils.RandomUtils;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowSubscriptionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author Ruipeng Zhang
 */
@RunWith(RobolectricTestRunner.class)
@Config(shadows = {ShadowTelephonyManager.class})
public class GetAndroidTelephonyInfoTest {
    private final List<SubscriptionInfo> subscriptions = new ArrayList<>();
    private Context context;
    private TelephonyManager manager;
    private final ActionCallback<RDFAndroidTelephonyInfo> callback = telephonyInfo -> {
        try {
            assertEquals(manager.getAllCellInfo().size(), telephonyInfo.getCells().size());
            for (int i = 0; i < manager.getAllCellInfo().size(); i++) {
                assertCellEquals(manager.getAllCellInfo().get(i),
                        telephonyInfo.getCells().get(i));
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                assertEquals(subscriptions.size(), telephonyInfo.getSubscriptions().size());
                for (int i = 0; i < subscriptions.size(); i++) {
                    assertSubscriptionEquals(manager, subscriptions.get(i),
                            telephonyInfo.getSubscriptions().get(i));
                }
            } else {
                assertEquals(1, telephonyInfo.getSubscriptions().size());
                assertSubscriptionEquals(manager, telephonyInfo.getSubscriptions().get(0));
            }
            assertEquals(manager.getCallState(), telephonyInfo.getCallState().getNumber());
            assertEquals(manager.getDataState(), telephonyInfo.getDataState().getNumber());
            assertEquals(manager.getDataActivity(),
                    telephonyInfo.getDataActivity().getNumber());
            assertEquals(manager.isSmsCapable(), telephonyInfo.getIsSmsCapable());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                assertEquals(manager.isVoiceCapable(), telephonyInfo.getIsVoiceCapable());
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                assertEquals(manager.getPhoneCount(), telephonyInfo.getPhoneCount().intValue());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        throw new TestSuccessException();
    };

    private void setSubscriptionId(PhoneStateListener listener, int subscriptionId)
            throws Exception {
        if (subscriptionId == -1) {
            return;
        }
        setFieldValue(PhoneStateListener.class, listener, "mSubId", subscriptionId);
    }

    private void assertReturnEquals(TelephonyManager telephony, String telephonyMethod,
            RDFAndroidSubscriptionInfo rdf, String rdfMethod, int sId) throws Exception {
        Object expected = callInstanceMethod(telephony, telephonyMethod,
                from(int.class, sId));
        Object actual = callInstanceMethod(rdf, rdfMethod);
        if (actual instanceof Internal.EnumLite) {
            actual = ((Internal.EnumLite) actual).getNumber();
        }
        assertEquals(expected, actual);
    }

    private void assertCellEquals(CellInfo cellInfo, RDFAndroidCellInfo rdf) throws Exception {
        long bootTime = (System.currentTimeMillis() - SystemClock.elapsedRealtime()) * 1000;
        assertEquals(cellInfo.getTimeStamp() / 1000.0 + bootTime, rdf.getTimestamp().getValue(),
                1000 * 1000);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            assertEquals(cellInfo.getCellConnectionStatus(), rdf.getStatus().getNumber());
        }
        if (cellInfo instanceof CellInfoGsm) {
            assertEquals(1, rdf.getType().getNumber());
        } else if (cellInfo instanceof CellInfoCdma) {
            assertEquals(2, rdf.getType().getNumber());
        } else if (cellInfo instanceof CellInfoLte) {
            assertEquals(3, rdf.getType().getNumber());
        } else if (cellInfo instanceof CellInfoWcdma) {
            assertEquals(4, rdf.getType().getNumber());
        }
        Object identity = callInstanceMethod(cellInfo, "getCellIdentity");
        assertEquals(identity.toString(), rdf.getIdentity());
        CellSignalStrength signalStrength = callInstanceMethod(cellInfo, "getCellSignalStrength");
        assertEquals(signalStrength.getLevel(), rdf.getLevel().intValue());
        assertEquals(signalStrength.toString(), rdf.getSignalStrength());
    }

    private void assertServiceStateEquals(TelephonyManager manager,
            RDFAndroidSubscriptionInfo info, int subscriptionId) throws Exception {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ServiceState serviceState = manager.getServiceState();
            assertNotNull(serviceState);
            assertEquals(serviceState.getState(), info.getServiceState().getNumber());
            assertEquals(serviceState.toString(), info.getServiceInfo());
        } else {
            assertNotNull(info.getServiceInfo());
            PhoneStateListener listener = new PhoneStateListener() {
                @Override
                public void onServiceStateChanged(ServiceState serviceState) {
                    assertNotNull(serviceState);
                    assertEquals(serviceState.getState(), info.getServiceState().getNumber());
                    assertEquals(serviceState.toString(), info.getServiceInfo());
                }
            };
            setSubscriptionId(listener, subscriptionId);
            manager.listen(listener, LISTEN_SERVICE_STATE);
            manager.listen(listener, LISTEN_NONE);
        }
    }

    private void assertSignalStrengthEquals(TelephonyManager manager,
            RDFAndroidSubscriptionInfo info, int subscriptionId) throws Exception {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            SignalStrength signalStrength = manager.getSignalStrength();
            assertNotNull(signalStrength);
            assertEquals(signalStrength.getLevel(), info.getLevel().intValue());
            assertEquals(signalStrength.toString(), info.getSignalStrength());
        } else {
            assertNotNull(info.getSignalStrength());
            PhoneStateListener listener = new PhoneStateListener() {
                @Override
                public void onSignalStrengthsChanged(SignalStrength signalStrength) {
                    assertNotNull(signalStrength);
                    try {
                        assertEquals((int) callInstanceMethod(signalStrength, "getLevel"),
                                info.getLevel().intValue());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    assertEquals(signalStrength.toString(), info.getSignalStrength());
                }
            };
            setSubscriptionId(listener, subscriptionId);
            manager.listen(listener, LISTEN_SIGNAL_STRENGTHS);
            manager.listen(listener, LISTEN_NONE);
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private void assertSubscriptionEquals(TelephonyManager telephony,
            RDFAndroidSubscriptionInfo rdf) throws Exception {
        assertServiceStateEquals(telephony, rdf, -1);
        assertSignalStrengthEquals(telephony, rdf, -1);
        assertEquals(0, rdf.getSimSlot().intValue());
        assertEquals(telephony.getLine1Number(), rdf.getPhoneNumber());
        String networkOperator = telephony.getNetworkOperator();
        assertEquals(Integer.parseInt(networkOperator.substring(0, 3)), rdf.getMcc().intValue());
        assertEquals(Integer.parseInt(networkOperator.substring(3)), rdf.getMnc().intValue());
        assertEquals(telephony.getSimOperatorName(), rdf.getCarrierName());
        assertEquals(telephony.getDeviceId(), rdf.getDeviceId());
        assertEquals(telephony.getSimSerialNumber(), rdf.getSimSerialNumber());
        assertEquals(telephony.getSimState(), rdf.getSimState().getNumber());
        assertEquals(telephony.getVoiceMailNumber(), rdf.getVoicemailNumber());
        assertEquals(telephony.getSubscriberId(), rdf.getSubscriberId());
        assertEquals(telephony.getPhoneType(), rdf.getRadioType().getNumber());
        assertEquals(telephony.isNetworkRoaming(), rdf.getIsRoaming());
        assertEquals(telephony.getNetworkType(), rdf.getDataNetworkType().getNumber());
        assertEquals(callInstanceMethod(telephony, "getDataEnabled"), rdf.getIsDataEnabled());
    }

    @SuppressWarnings("deprecation")
    @RequiresApi(LOLLIPOP_MR1)
    private void assertSubscriptionEquals(TelephonyManager telephony, SubscriptionInfo subscription,
            RDFAndroidSubscriptionInfo rdf) throws Exception {
        TelephonyManager manager = telephony;
        int simSlotIndex = subscription.getSimSlotIndex();
        int subscriptionId = subscription.getSubscriptionId();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            manager = manager.createForSubscriptionId(subscriptionId);
        }
        assertServiceStateEquals(manager, rdf, subscriptionId);
        assertSignalStrengthEquals(manager, rdf, subscriptionId);
        assertEquals(simSlotIndex, rdf.getSimSlot().intValue());
        assertEquals(subscription.getNumber(), rdf.getPhoneNumber());
        assertEquals(subscription.getMcc(), rdf.getMcc().intValue());
        assertEquals(subscription.getMnc(), rdf.getMnc().intValue());
        assertEquals(subscription.getDisplayName(), rdf.getSubscriptionName());
        assertEquals(subscription.getCarrierName(), rdf.getCarrierName());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            assertEquals(manager.getImei(), rdf.getDeviceId());
            assertEquals(manager.getMeid(), rdf.getDeviceId());
            assertEquals(manager.getDeviceId(), rdf.getDeviceId());
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            assertEquals(manager.getDeviceId(simSlotIndex), rdf.getDeviceId());
        } else {
            assertReturnEquals(manager, "getDeviceId", rdf, "getDeviceId", simSlotIndex);
        }
        assertReturnEquals(manager, "getSimState", rdf, "getSimState", simSlotIndex);
        assertReturnEquals(manager, "getSimSerialNumber", rdf, "getSimSerialNumber",
                subscriptionId);
        assertReturnEquals(manager, "getVoiceMailNumber", rdf, "getVoicemailNumber",
                subscriptionId);
        assertReturnEquals(manager, "getSubscriberId", rdf, "getSubscriberId", subscriptionId);
        assertReturnEquals(manager, "getCurrentPhoneType", rdf, "getRadioType", subscriptionId);
        assertReturnEquals(manager, "getDataNetworkType", rdf, "getDataNetworkType",
                subscriptionId);
        assertReturnEquals(manager, "isNetworkRoaming", rdf, "getIsRoaming", subscriptionId);
        assertReturnEquals(manager, "getDataEnabled", rdf, "getIsDataEnabled", subscriptionId);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            assertEquals(manager.isConcurrentVoiceAndDataSupported(),
                    rdf.getIsConcurrentVoiceAndDataSupported());
        }
    }

    private Object createCellIdentity(Class<? extends CellIdentity> clazz) throws Exception {
        Random random = new Random();
        if (clazz.equals(CellIdentityCdma.class)) {
            return callConstructor(clazz,
                    from(int.class, random.nextInt(65536)), // nid
                    from(int.class, random.nextInt(32768)), // sid
                    from(int.class, random.nextInt(65535)), // bid
                    from(int.class, random.nextInt(2592000 * 2 + 1) - 2592000), // lon
                    from(int.class, random.nextInt(1296000 * 2 + 1) - 1296000) // lat
            );
        } else if (clazz.equals(CellIdentityGsm.class)) {
            return callConstructor(clazz,
                    from(int.class, random.nextInt(1000)), // mcc
                    from(int.class, random.nextInt(1000)), // mnc
                    from(int.class, random.nextInt(65536)), // lac
                    from(int.class, random.nextInt(65536)) // cid
            );
        } else if (clazz.equals(CellIdentityLte.class)) {
            return callConstructor(clazz,
                    from(int.class, random.nextInt(1000)), // mcc
                    from(int.class, random.nextInt(1000)), // mnc
                    from(int.class, random.nextInt(268435456)), // ci
                    from(int.class, random.nextInt(504)), // pci
                    from(int.class, random.nextInt(65535)) // tac
            );
        } else if (clazz.equals(CellIdentityWcdma.class)) {
            return callConstructor(clazz,
                    from(int.class, random.nextInt(1000)), // mcc
                    from(int.class, random.nextInt(1000)), // mnc
                    from(int.class, random.nextInt(65536)), // lac
                    from(int.class, random.nextInt(268435456)), // cid
                    from(int.class, random.nextInt(512)) // psc
            );
        }
        throw new RuntimeException(
                "Identity class " + clazz.getSimpleName() + " not implemented in the test.");
    }

    private CellSignalStrength createCellSignalStrength(Class<? extends CellSignalStrength> clazz)
            throws Exception {
        Random random = new Random();
        if (clazz.equals(CellSignalStrengthCdma.class)) {
            return callConstructor(clazz,
                    from(int.class, random.nextInt(1000) - 1), // cdmaDbm
                    from(int.class, random.nextInt(1000) - 1), // cdmaEcio
                    from(int.class, random.nextInt(1000) - 1), // evdoDbm
                    from(int.class, random.nextInt(1000) - 1), // evdoEcio
                    from(int.class, random.nextInt(10) - 1) // evdoSnr
            );
        } else if (clazz.equals(CellSignalStrengthGsm.class)) {
            return callConstructor(clazz,
                    from(int.class, random.nextBoolean() ? random.nextInt(32) : 99), // ss
                    from(int.class, random.nextBoolean() ? random.nextInt(8) : 99) // ber
            );
        } else if (clazz.equals(CellSignalStrengthLte.class)) {
            return callConstructor(clazz,
                    from(int.class, random.nextInt()), // signalStrength
                    from(int.class, -random.nextInt(200)), // rsrp
                    from(int.class, random.nextInt()), // rsrq
                    from(int.class, random.nextInt(100) - 40), // rssnr
                    from(int.class, random.nextInt()), // cqi
                    from(int.class, random.nextInt()) // timingAdvance
            );
        } else if (clazz.equals(CellSignalStrengthWcdma.class)) {
            return callConstructor(clazz,
                    from(int.class, random.nextBoolean() ? random.nextInt(32) : 99), // ss
                    from(int.class, random.nextBoolean() ? random.nextInt(8) : 99) // ber
            );
        }
        throw new RuntimeException(
                "Signal strength class " + clazz.getSimpleName() + " not implemented in the test.");
    }

    @SuppressWarnings({"JavaReflectionMemberAccess", "unchecked"})
    private CellInfo createCellInfo() throws Exception {
        Class[] infoClasses = {
                CellInfoCdma.class,
                CellInfoWcdma.class,
                CellInfoLte.class,
                CellInfoGsm.class
        };
        Class[] identityClasses = {
                CellIdentityCdma.class,
                CellIdentityWcdma.class,
                CellIdentityLte.class,
                CellIdentityGsm.class
        };
        Class[] signalStrengthClasses = {
                CellSignalStrengthCdma.class,
                CellSignalStrengthWcdma.class,
                CellSignalStrengthLte.class,
                CellSignalStrengthGsm.class
        };
        Random random = new Random();
        int type = random.nextInt(infoClasses.length);
        CellInfo cellInfo = (CellInfo) callConstructor(infoClasses[type]);
        callInstanceMethod(cellInfo, "setRegistered",
                from(boolean.class, random.nextBoolean()));
        callInstanceMethod(cellInfo, "setCellIdentity",
                from(identityClasses[type], createCellIdentity(identityClasses[type])));
        callInstanceMethod(cellInfo, "setCellSignalStrength",
                from(signalStrengthClasses[type],
                        createCellSignalStrength(signalStrengthClasses[type])));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            callInstanceMethod(cellInfo, "setCellConnectionStatus",
                    from(int.class, RandomUtils.oneOf(
                            CellInfo.CONNECTION_NONE,
                            CellInfo.CONNECTION_PRIMARY_SERVING,
                            CellInfo.CONNECTION_SECONDARY_SERVING,
                            CellInfo.CONNECTION_UNKNOWN)));
        }
        return cellInfo;
    }

    private SubscriptionInfo createSubscriptionInfo(int id) throws Exception {
        Random random = new Random();
        SubscriptionInfo subscriptionInfo =
                ShadowSubscriptionManager.SubscriptionInfoBuilder.newBuilder()
                        .setId(id)
                        .setDisplayName(RandomStringUtils.randomAlphabetic(8))
                        .setCarrierName(RandomStringUtils.randomAlphabetic(6))
                        .setCountryIso(RandomStringUtils.randomAlphabetic(2))
                        .setDataRoaming(random.nextInt(2))
                        .setNumber(RandomStringUtils.randomNumeric(10))
                        .setSimSlotIndex(id)
                        .buildSubscriptionInfo();
        setFieldValue(subscriptionInfo, "mMcc", random.nextInt(1000));
        setFieldValue(subscriptionInfo, "mMnc", random.nextInt(1000));
        return subscriptionInfo;
    }

    private ServiceState createServiceState() {
        Random random = new Random();
        ServiceState state = new ServiceState();
        state.setState(RandomUtils.oneOf(
                AndroidSubscriptionInfo.ServiceState.SRV_IN_SERVICE_VALUE,
                AndroidSubscriptionInfo.ServiceState.SRV_OUT_OF_SERVICE_VALUE,
                AndroidSubscriptionInfo.ServiceState.SRV_EMERGENCY_ONLY_VALUE,
                AndroidSubscriptionInfo.ServiceState.SRV_POWER_OFF_VALUE
        ));
        state.setIsManualSelection(random.nextBoolean());
        state.setRoaming(random.nextBoolean());
        state.setOperatorName(RandomStringUtils.randomAlphabetic(10),
                RandomStringUtils.randomAlphabetic(6), RandomStringUtils.randomNumeric(10));
        return state;
    }

    private SignalStrength createSignalStrength() throws Exception {
        return callConstructor(SignalStrength.class);
    }

    private void setTelephonyManager(ShadowTelephonyManager manager, int subId) throws Exception {
        Random random = new Random();
        manager.setLine1Number(RandomStringUtils.randomNumeric(10));
        manager.setNetworkOperator(RandomStringUtils.randomNumeric(6));
        manager.setSimOperatorName(RandomStringUtils.randomNumeric(6));
        manager.setSimSerialNumber(subId, RandomStringUtils.randomAlphanumeric(16));
        manager.setSimState(subId, RandomUtils.oneOf(
                TelephonyManager.SIM_STATE_UNKNOWN,
                TelephonyManager.SIM_STATE_ABSENT,
                TelephonyManager.SIM_STATE_PIN_REQUIRED,
                TelephonyManager.SIM_STATE_PUK_REQUIRED,
                TelephonyManager.SIM_STATE_NETWORK_LOCKED,
                TelephonyManager.SIM_STATE_READY,
                TelephonyManager.SIM_STATE_NOT_READY,
                TelephonyManager.SIM_STATE_PERM_DISABLED,
                TelephonyManager.SIM_STATE_CARD_IO_ERROR,
                TelephonyManager.SIM_STATE_CARD_RESTRICTED
        ));
        manager.setVoiceMailNumber(subId, RandomStringUtils.randomAlphanumeric(10));
        manager.setSubscriberId(subId, RandomStringUtils.randomNumeric(12));
        int radioType = RandomUtils.oneOf(
                AndroidSubscriptionInfo.RadioType.RT_NONE_VALUE,
                AndroidSubscriptionInfo.RadioType.RT_GSM_VALUE,
                AndroidSubscriptionInfo.RadioType.RT_CDMA_VALUE,
                AndroidSubscriptionInfo.RadioType.RT_SIP_VALUE,
                AndroidSubscriptionInfo.RadioType.RT_THIRD_PARTY_VALUE,
                AndroidSubscriptionInfo.RadioType.RT_IMS_VALUE,
                AndroidSubscriptionInfo.RadioType.RT_CDMA_LTE_VALUE
        );
        manager.setPhoneType(radioType);
        manager.setCurrentPhoneType(subId, radioType);
        int networkType = RandomUtils.oneOf(
                AndroidSubscriptionInfo.DataNetworkType.DNT_UNKNOWN_VALUE,
                AndroidSubscriptionInfo.DataNetworkType.DNT_GPRS_VALUE,
                AndroidSubscriptionInfo.DataNetworkType.DNT_EDGE_VALUE,
                AndroidSubscriptionInfo.DataNetworkType.DNT_UMTS_VALUE,
                AndroidSubscriptionInfo.DataNetworkType.DNT_CDMA_VALUE,
                AndroidSubscriptionInfo.DataNetworkType.DNT_EVDO_0_VALUE,
                AndroidSubscriptionInfo.DataNetworkType.DNT_EVDO_A_VALUE,
                AndroidSubscriptionInfo.DataNetworkType.DNT_1XRTT_VALUE,
                AndroidSubscriptionInfo.DataNetworkType.DNT_HSDPA_VALUE,
                AndroidSubscriptionInfo.DataNetworkType.DNT_HSUPA_VALUE,
                AndroidSubscriptionInfo.DataNetworkType.DNT_HSPA_VALUE,
                AndroidSubscriptionInfo.DataNetworkType.DNT_IDEN_VALUE,
                AndroidSubscriptionInfo.DataNetworkType.DNT_EVDO_B_VALUE,
                AndroidSubscriptionInfo.DataNetworkType.DNT_LTE_VALUE,
                AndroidSubscriptionInfo.DataNetworkType.DNT_EHRPD_VALUE,
                AndroidSubscriptionInfo.DataNetworkType.DNT_HSPAP_VALUE,
                AndroidSubscriptionInfo.DataNetworkType.DNT_GSM_VALUE,
                AndroidSubscriptionInfo.DataNetworkType.DNT_TD_SCDMA_VALUE,
                AndroidSubscriptionInfo.DataNetworkType.DNT_IWLAN_VALUE,
                AndroidSubscriptionInfo.DataNetworkType.DNT_LTE_CA_VALUE
        );
        manager.setNetworkType(networkType);
        manager.setDataNetworkType(subId, networkType);
        manager.setIsNetworkRoaming(subId, random.nextBoolean());
        manager.setDataEnabled(subId, random.nextBoolean());
        manager.setServiceState(subId, createServiceState());
        manager.setSignalStrength(subId, createSignalStrength());
        String deviceId = RandomStringUtils.randomAlphanumeric(16);
        manager.setDeviceId(subId, deviceId);
        manager.setImei(subId, deviceId);
        manager.setMeid(subId, deviceId);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.setConcurrentVoiceAndDataSupported(random.nextBoolean());
        }
    }

    @Test(expected = TestSuccessException.class)
    public void execute() throws Exception {
        context = ApplicationProvider.getApplicationContext();
        Shadows.shadowOf((Application) context).grantPermissions(READ_PHONE_STATE);
        manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        Random random = new Random();
        ShadowTelephonyManager shadowManager = (ShadowTelephonyManager) Shadows.shadowOf(manager);
        shadowManager.setCallState(RandomUtils.oneOf(
                TelephonyManager.CALL_STATE_IDLE,
                TelephonyManager.CALL_STATE_RINGING,
                TelephonyManager.CALL_STATE_OFFHOOK
        ));
        shadowManager.setDataState(RandomUtils.oneOf(
                TelephonyManager.DATA_DISCONNECTED,
                TelephonyManager.DATA_CONNECTING,
                TelephonyManager.DATA_CONNECTED,
                TelephonyManager.DATA_SUSPENDED
        ));
        shadowManager.setDataActivity(RandomUtils.oneOf(
                TelephonyManager.DATA_ACTIVITY_NONE,
                TelephonyManager.DATA_ACTIVITY_IN,
                TelephonyManager.DATA_ACTIVITY_OUT,
                TelephonyManager.DATA_ACTIVITY_INOUT,
                TelephonyManager.DATA_ACTIVITY_DORMANT
        ));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            shadowManager.setVoiceCapable(random.nextBoolean());
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            shadowManager.setPhoneCount(random.nextInt(4));
        }
        List<CellInfo> cells = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            cells.add(createCellInfo());
        }
        shadowManager.setAllCellInfo(cells);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            for (int i = 0; i < 10; i++) {
                subscriptions.add(createSubscriptionInfo(i));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    TelephonyManager telephony = callConstructor(
                            TelephonyManager.class, from(Context.class, context));
                    setTelephonyManager((ShadowTelephonyManager) Shadows.shadowOf(telephony), i);
                    shadowManager.setTelephonyManagerForSubscriptionId(i, telephony);
                } else {
                    setTelephonyManager(shadowManager, i);
                }
            }
            ShadowSubscriptionManager shadowSubscriptionManager = Shadows.shadowOf(
                    (SubscriptionManager) context.getSystemService(
                            Context.TELEPHONY_SUBSCRIPTION_SERVICE));
            shadowSubscriptionManager.setActiveSubscriptionInfoList(subscriptions);
        }
        setTelephonyManager(shadowManager, 0);
        new GetAndroidTelephonyInfo().execute(context, null, callback);
    }
}