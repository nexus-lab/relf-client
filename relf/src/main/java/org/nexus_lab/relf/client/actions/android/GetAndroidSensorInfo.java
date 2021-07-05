package org.nexus_lab.relf.client.actions.android;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;

import org.nexus_lab.relf.client.actions.Action;
import org.nexus_lab.relf.client.actions.ActionCallback;
import org.nexus_lab.relf.lib.rdfvalues.RDFNull;
import org.nexus_lab.relf.lib.rdfvalues.android.RDFAndroidSensorInfo;
import org.nexus_lab.relf.proto.AndroidSensorInfo.ReportingMode;
import org.nexus_lab.relf.proto.AndroidSensorInfo.ReportingRateLevel;
import org.nexus_lab.relf.proto.AndroidSensorType;

import java.util.List;

/**
 * @author Ruipeng Zhang
 */
public class GetAndroidSensorInfo implements Action<RDFNull, RDFAndroidSensorInfo> {
    @Override
    public void execute(Context context, RDFNull request, ActionCallback<RDFAndroidSensorInfo> callback) {
        SensorManager manager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> sensors = manager.getSensorList(Sensor.TYPE_ALL);
        for (Sensor sensor : sensors) {
            RDFAndroidSensorInfo info = new RDFAndroidSensorInfo();
            info.setName(sensor.getName());
            info.setType(AndroidSensorType.forNumber(sensor.getType()));
            info.setVendor(sensor.getVendor());
            info.setVersion(sensor.getVersion());

            info.setPower(sensor.getPower());
            info.setResolution(sensor.getResolution());
            info.setMinDelay(sensor.getMinDelay());
            info.setMaxDelay(sensor.getMaxDelay());
            info.setMaxRange(sensor.getMaximumRange());

            info.setReportingMode(ReportingMode.forNumber(sensor.getReportingMode()));
            info.setFifoMaxEventCount(sensor.getFifoMaxEventCount());
            info.setFifoReservedEventCount(sensor.getFifoReservedEventCount());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                info.setHighestDirectReportRateLevel(
                        ReportingRateLevel.forNumber(sensor.getHighestDirectReportRateLevel()));
            }
            callback.onResponse(info);
        }
        callback.onComplete();
    }
}
