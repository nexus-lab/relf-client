package org.nexus_lab.relf.client.actions.android;

import static android.hardware.Sensor.REPORTING_MODE_ONE_SHOT;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.TriggerEvent;
import android.hardware.TriggerEventListener;
import android.os.Handler;

import org.nexus_lab.relf.client.actions.Action;
import org.nexus_lab.relf.client.actions.ActionCallback;
import org.nexus_lab.relf.lib.rdfvalues.RDFDatetime;
import org.nexus_lab.relf.lib.rdfvalues.android.RDFAndroidSensorData;
import org.nexus_lab.relf.lib.rdfvalues.android.RDFAndroidSensorDataRequest;
import org.nexus_lab.relf.lib.rdfvalues.android.RDFAndroidSensorDataResponse;
import org.nexus_lab.relf.proto.AndroidSensorData;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ruipeng Zhang
 */
public class GetAndroidSensorData implements
        Action<RDFAndroidSensorDataRequest, RDFAndroidSensorDataResponse>, SensorEventListener {
    private final Handler handler = new Handler();
    private Sensor sensor;
    private List<RDFAndroidSensorData> sensorData = new ArrayList<>();
    private TriggerEventListener triggerListener = new TriggerEventListener() {
        @Override
        public void onTrigger(TriggerEvent event) {
            RDFAndroidSensorData data = new RDFAndroidSensorData();
            data.setTimestamp(event.timestamp);
            if (event.values != null) {
                ArrayList<Float> values = new ArrayList<>();
                for (float value : event.values) {
                    values.add(value);
                }
                data.setValues(values);
            }
            sensorData.add(data);
        }
    };

    @Override
    public void onSensorChanged(SensorEvent event) {
        RDFAndroidSensorData data = new RDFAndroidSensorData();
        data.setAccuracy(AndroidSensorData.Accuracy.forNumber(event.accuracy));
        data.setTimestamp(event.timestamp);
        if (event.values != null) {
            ArrayList<Float> values = new ArrayList<>();
            for (float value : event.values) {
                values.add(value);
            }
            data.setValues(values);
        }
        sensorData.add(data);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void execute(Context context, RDFAndroidSensorDataRequest request,
            ActionCallback<RDFAndroidSensorDataResponse> callback) {
        SensorManager manager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if (request == null) {
            callback.onError(new Exception("Need arguments for dumping sensor data."));
            return;
        }
        int type = request.getType().getNumber();
        int rate = request.getSamplingRate();
        int duration = request.getDuration();
        RDFAndroidSensorDataResponse response = new RDFAndroidSensorDataResponse();
        response.setType(request.getType());
        response.setStartTime(RDFDatetime.now());
        sensor = manager.getDefaultSensor(type);
        if (sensor == null) {
            List<Sensor> sensors = manager.getSensorList(type);
            if (sensors.size() > 0) {
                sensor = sensors.get(0);
            }
        }
        if (sensor == null) {
            callback.onError(new Exception(
                    "The device is not equipped with " + request.getType().name() + " sensor."));
        } else {
            if (sensor.getReportingMode() == REPORTING_MODE_ONE_SHOT) {
                manager.requestTriggerSensor(triggerListener, sensor);
            } else {
                manager.registerListener(this, sensor, rate * 1000);
            }
            handler.postDelayed(() -> {
                if (sensor.getReportingMode() == REPORTING_MODE_ONE_SHOT) {
                    manager.cancelTriggerSensor(triggerListener, sensor);
                } else {
                    manager.unregisterListener(GetAndroidSensorData.this);
                }
                response.setData(sensorData);
                callback.onResponse(response);
                callback.onComplete();
            }, duration * 1000);
        }
    }
}
