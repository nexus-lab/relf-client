package org.nexus_lab.relf.client.actions.android;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;

import androidx.test.core.app.ApplicationProvider;

import org.nexus_lab.relf.client.actions.ActionCallback;
import org.nexus_lab.relf.client.exceptions.TestSuccessException;
import org.nexus_lab.relf.lib.rdfvalues.android.RDFAndroidBatteryInfo;
import org.nexus_lab.relf.proto.AndroidBatteryInfo.ChargingStatus;
import org.nexus_lab.relf.proto.AndroidBatteryInfo.Health;
import org.nexus_lab.relf.proto.AndroidBatteryInfo.PowerSource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

/**
 * @author Ruipeng Zhang
 */
@RunWith(RobolectricTestRunner.class)
public class GetAndroidBatteryInfoTest {
    private Intent intent;
    private final ActionCallback<RDFAndroidBatteryInfo> callback = info -> {
        float level = (float) intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0) * 100
                / intent.getIntExtra(BatteryManager.EXTRA_SCALE, 0);
        assertTrue(Math.abs(info.getLevel() - level) <= 2);
        assertEquals(Health.forNumber(intent.getIntExtra(BatteryManager.EXTRA_HEALTH, 1)),
                info.getHealth());
        assertEquals(PowerSource.forNumber(intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0)),
                info.getPowerSource());
        assertEquals(
                ChargingStatus.forNumber(intent.getIntExtra(BatteryManager.EXTRA_STATUS, 0)),
                info.getStatus());
        assertEquals(intent.getExtras().getBoolean(BatteryManager.EXTRA_PRESENT),
                info.getIsPresent());
        assertEquals(intent.getExtras().getString(BatteryManager.EXTRA_TECHNOLOGY),
                info.getTechnology());
        assertEquals(intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0),
                info.getTemperature().intValue());
        assertEquals(intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0),
                info.getVoltage().intValue());
        throw new TestSuccessException();
    };
    private Context context;

    @Before
    public void setup() {
        context = ApplicationProvider.getApplicationContext();
        intent = new Intent(Intent.ACTION_BATTERY_CHANGED);
        Intent extras = new Intent();
        intent.putExtra(BatteryManager.EXTRA_LEVEL, 1);
        intent.putExtra(BatteryManager.EXTRA_SCALE, 2);
        intent.putExtra(BatteryManager.EXTRA_HEALTH, Health.DEAD.getNumber());
        intent.putExtra(BatteryManager.EXTRA_PLUGGED, PowerSource.WIRELESS.getNumber());
        intent.putExtra(BatteryManager.EXTRA_STATUS, ChargingStatus.DISCHARGING.getNumber());
        extras.putExtra(BatteryManager.EXTRA_PRESENT, true);
        extras.putExtra(BatteryManager.EXTRA_TECHNOLOGY, "Black Magic");
        intent.putExtras(extras);
        intent.putExtra(BatteryManager.EXTRA_TEMPERATURE, 100);
        intent.putExtra(BatteryManager.EXTRA_VOLTAGE, 36);
    }

    @Test(expected = TestSuccessException.class)
    public void execute() {
        new GetAndroidBatteryInfo().execute(context, null, callback);
        context.sendBroadcast(intent);
    }

}