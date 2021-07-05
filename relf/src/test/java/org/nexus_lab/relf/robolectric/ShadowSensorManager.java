package org.nexus_lab.relf.robolectric;

import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import java.util.ArrayList;
import java.util.List;

import edu.emory.mathcs.backport.java.util.Collections;

/**
 * Robolectric shadow {@link android.hardware.SensorManager} class for testing.
 *
 * @author Ruipeng Zhang
 */
@Implements(value = SensorManager.class, minSdk = Build.VERSION_CODES.LOLLIPOP)
public class ShadowSensorManager extends org.robolectric.shadows.ShadowSensorManager {
    private List<Sensor> sensorList = new ArrayList<>();

    @Override
    public void addSensor(Sensor sensor) {
        super.addSensor(sensor);
        sensorList.add(sensor);
    }

    /**
     * Use this method to get the list of available sensors of a certain type.
     *
     * @param type of sensors requested
     * @return a list of sensors matching the asked type.
     */
    @SuppressWarnings("unchecked")
    @Implementation
    public List<Sensor> getSensorList(int type) {
        if (type == Sensor.TYPE_ALL) {
            return Collections.unmodifiableList(sensorList);
        }
        List<Sensor> sensors = new ArrayList<>();
        for (Sensor sensor : sensorList) {
            if (sensor.getType() == type) {
                sensors.add(sensor);
            }
        }
        return Collections.unmodifiableList(sensors);
    }
}
