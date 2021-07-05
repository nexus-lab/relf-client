package org.nexus_lab.relf.client.actions.android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

import org.nexus_lab.relf.client.actions.Action;
import org.nexus_lab.relf.client.actions.ActionCallback;
import org.nexus_lab.relf.lib.rdfvalues.RDFNull;
import org.nexus_lab.relf.lib.rdfvalues.android.RDFAndroidBatteryInfo;
import org.nexus_lab.relf.proto.AndroidBatteryInfo;

/**
 * @author Ruipeng Zhang
 */
public class GetAndroidBatteryInfo implements Action<RDFNull, RDFAndroidBatteryInfo> {
    private ActionCallback<RDFAndroidBatteryInfo> callback;

    private BroadcastReceiver batteryChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (callback == null) {
                return;
            }
            RDFAndroidBatteryInfo info = new RDFAndroidBatteryInfo();
            info.setLevel((float) intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0) * 100
                    / intent.getIntExtra(BatteryManager.EXTRA_SCALE, 0));
            info.setHealth(AndroidBatteryInfo.Health.forNumber(
                    intent.getIntExtra(BatteryManager.EXTRA_HEALTH, 1)));
            info.setPowerSource(AndroidBatteryInfo.PowerSource.forNumber(
                    intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0)));
            info.setIsPresent(intent.getExtras().getBoolean(BatteryManager.EXTRA_PRESENT));
            info.setStatus(AndroidBatteryInfo.ChargingStatus.forNumber(
                    intent.getIntExtra(BatteryManager.EXTRA_STATUS, 0)));
            info.setTechnology(intent.getExtras().getString(BatteryManager.EXTRA_TECHNOLOGY));
            info.setTemperature(intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0));
            info.setVoltage(intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0));

            callback.onResponse(info);
            callback.onComplete();

            context.unregisterReceiver(this);
        }
    };

    @Override
    public void execute(Context context, RDFNull request, ActionCallback<RDFAndroidBatteryInfo> callback) {
        this.callback = callback;
        context.registerReceiver(batteryChangeReceiver,
                new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }
}
