package org.nexus_lab.relf.client.actions.android;

import static org.nexus_lab.relf.utils.ReflectionUtils.setFieldValue;

import static org.junit.Assert.assertEquals;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;

import androidx.test.core.app.ApplicationProvider;

import org.nexus_lab.relf.client.actions.ActionCallback;
import org.nexus_lab.relf.client.exceptions.TestSuccessException;
import org.nexus_lab.relf.lib.rdfvalues.android.RDFAndroidSensorInfo;
import org.nexus_lab.relf.robolectric.ShadowSensorManager;
import org.nexus_lab.relf.utils.RandomUtils;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowSensor;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author Ruipeng Zhang
 */
@RunWith(RobolectricTestRunner.class)
@Config(shadows = {ShadowSensorManager.class})
public class GetAndroidSensorInfoTest {
    private Sensor createSensor() throws ReflectiveOperationException {
        Random random = new Random();
        int type = RandomUtils.oneOf(
                Sensor.TYPE_ACCELEROMETER,
                Sensor.TYPE_MAGNETIC_FIELD,
                Sensor.TYPE_GYROSCOPE,
                Sensor.TYPE_LIGHT,
                Sensor.TYPE_PRESSURE,
                Sensor.TYPE_PROXIMITY,
                Sensor.TYPE_GRAVITY,
                Sensor.TYPE_LINEAR_ACCELERATION,
                Sensor.TYPE_ROTATION_VECTOR,
                Sensor.TYPE_RELATIVE_HUMIDITY,
                Sensor.TYPE_AMBIENT_TEMPERATURE,
                Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED,
                Sensor.TYPE_GAME_ROTATION_VECTOR,
                Sensor.TYPE_GYROSCOPE_UNCALIBRATED,
                Sensor.TYPE_SIGNIFICANT_MOTION,
                Sensor.TYPE_STEP_DETECTOR,
                Sensor.TYPE_STEP_COUNTER,
                Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR,
                Sensor.TYPE_HEART_RATE,
                Sensor.TYPE_POSE_6DOF,
                Sensor.TYPE_STATIONARY_DETECT,
                Sensor.TYPE_MOTION_DETECT,
                Sensor.TYPE_HEART_BEAT,
                Sensor.TYPE_LOW_LATENCY_OFFBODY_DETECT,
                Sensor.TYPE_ACCELEROMETER_UNCALIBRATED
        );
        Sensor sensor = ShadowSensor.newInstance(type);
        setFieldValue(sensor, "mVersion", random.nextInt());
        setFieldValue(sensor, "mName", RandomStringUtils.randomAlphanumeric(6));
        setFieldValue(sensor, "mVendor", RandomStringUtils.randomAlphanumeric(6));
        setFieldValue(sensor, "mMaxRange", random.nextFloat());
        setFieldValue(sensor, "mResolution", random.nextFloat());
        setFieldValue(sensor, "mPower", random.nextFloat());
        setFieldValue(sensor, "mMinDelay", random.nextInt());
        setFieldValue(sensor, "mMaxDelay", random.nextInt());
        setFieldValue(sensor, "mFifoReservedEventCount", random.nextInt());
        setFieldValue(sensor, "mFifoMaxEventCount", random.nextInt());
        return sensor;
    }

    @Test(expected = TestSuccessException.class)
    public void execute() throws Exception {
        Context context = ApplicationProvider.getApplicationContext();
        SensorManager manager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        ShadowSensorManager shadowManager = (ShadowSensorManager) Shadows.shadowOf(manager);
        List<Sensor> sensors = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            Sensor sensor = createSensor();
            sensors.add(sensor);
            shadowManager.addSensor(sensor);
        }
        new GetAndroidSensorInfo().execute(context, null,
                new ActionCallback<RDFAndroidSensorInfo>() {
                    private int i;

                    @Override
                    public void onResponse(RDFAndroidSensorInfo actual) {
                        Sensor expected = sensors.get(i);
                        assertEquals(expected.getName(), actual.getName());
                        assertEquals(expected.getType(), actual.getType().getNumber());
                        assertEquals(expected.getVendor(), actual.getVendor());
                        assertEquals(expected.getVersion(), actual.getVersion().intValue());
                        assertEquals(expected.getPower(), actual.getPower(), 0);
                        assertEquals(expected.getResolution(), actual.getResolution(), 0);
                        assertEquals(expected.getMinDelay(), actual.getMinDelay().intValue());
                        assertEquals(expected.getMaxDelay(), actual.getMaxDelay().intValue());
                        assertEquals(expected.getMaximumRange(), actual.getMaxRange(), 0);
                        assertEquals(expected.getReportingMode(),
                                actual.getReportingMode().getNumber());
                        assertEquals(expected.getFifoMaxEventCount(),
                                actual.getFifoMaxEventCount().intValue());
                        assertEquals(expected.getFifoReservedEventCount(),
                                actual.getFifoReservedEventCount().intValue());
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            assertEquals(expected.getHighestDirectReportRateLevel(),
                                    actual.getHighestDirectReportRateLevel().getNumber());
                        }
                        i++;
                    }

                    @Override
                    public void onComplete() {
                        assertEquals(sensors.size(), i);
                        throw new TestSuccessException();
                    }
                });
    }
}