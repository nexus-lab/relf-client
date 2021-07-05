package org.nexus_lab.relf.client.actions.android;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Looper;

import androidx.test.core.app.ApplicationProvider;

import org.nexus_lab.relf.client.exceptions.TestSuccessException;
import org.nexus_lab.relf.lib.rdfvalues.android.RDFAndroidSensorData;
import org.nexus_lab.relf.lib.rdfvalues.android.RDFAndroidSensorDataRequest;
import org.nexus_lab.relf.lib.rdfvalues.android.RDFAndroidSensorDataResponse;
import org.nexus_lab.relf.proto.AndroidSensorType;
import org.nexus_lab.relf.utils.RandomUtils;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.shadows.ShadowLooper;
import org.robolectric.shadows.ShadowSensor;
import org.robolectric.shadows.ShadowSensorManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

/**
 * @author Ruipeng Zhang
 */
@RunWith(RobolectricTestRunner.class)
public class GetAndroidSensorDataTest {
    private long startTime;
    private Sensor sensor;
    private Context context;
    private ShadowSensorManager shadowManager;
    private RDFAndroidSensorDataRequest request;
    private RDFAndroidSensorDataResponse response;
    private final Thread actionThread = new Thread() {
        private boolean quit = false;

        @Override
        public void run() {
            Looper.prepare();
            startTime = System.currentTimeMillis();
            new GetAndroidSensorData().execute(context, request, response -> {
                GetAndroidSensorDataTest.this.response = response;
                quit = true;
            });
            ShadowLooper shadowLooper = Shadows.shadowOf(Looper.myLooper());
            while (!quit) {
                try {
                    Thread.sleep(100);
                    shadowLooper.getScheduler().advanceBy(100, TimeUnit.MILLISECONDS);
                } catch (InterruptedException ignored) {
                }
            }
        }
    };
    private List<SensorEvent> events = new ArrayList<>();
    private final Thread sensorThread = new Thread() {
        @Override
        public void run() {
            Random random = new Random();
            int milliseconds = request.getSamplingRate() / 1000 / 1000;
            int microseconds = request.getSamplingRate() - milliseconds * 1000 * 1000;
            try {
                Thread.sleep(1000);
                while (actionThread.isAlive()) {
                    SensorEvent event = ShadowSensorManager.createSensorEvent(3);
                    event.sensor = sensor;
                    event.timestamp = System.nanoTime();
                    event.accuracy = RandomUtils.oneOf(
                            SensorManager.SENSOR_STATUS_ACCURACY_HIGH,
                            SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM,
                            SensorManager.SENSOR_STATUS_ACCURACY_LOW,
                            SensorManager.SENSOR_STATUS_NO_CONTACT,
                            SensorManager.SENSOR_STATUS_UNRELIABLE
                    );
                    event.values[0] = random.nextFloat();
                    event.values[1] = random.nextFloat();
                    event.values[2] = random.nextFloat();
                    for (SensorEventListener listener : shadowManager.getListeners()) {
                        listener.onSensorChanged(event);
                    }
                    events.add(event);
                    Thread.sleep(milliseconds, microseconds);
                }
            } catch (InterruptedException ignored) {
            }
        }
    };

    @Before
    public void setup() {
        context = ApplicationProvider.getApplicationContext();
        SensorManager manager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        shadowManager = Shadows.shadowOf(manager);
        sensor = ShadowSensor.newInstance(Sensor.TYPE_ACCELEROMETER);
        shadowManager.addSensor(sensor);
        request = new RDFAndroidSensorDataRequest();
        request.setType(AndroidSensorType.ACCELEROMETER);
        request.setSamplingRate(50);
        request.setDuration(2);
    }

    @Test(expected = TestSuccessException.class)
    public void execute() {
        actionThread.start();
        sensorThread.start();
        try {
            actionThread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        assertEquals(sensor.getType(), response.getType().getNumber());
        assertEquals(startTime,
                response.getStartTime().asMicrosecondsFromEpoch() / 1000.0, 100);
        // allow response to miss some data at the end
        assertEquals(events.size(), response.getData().size(), 5);
        for (int i = 0; i < response.getData().size(); i++) {
            SensorEvent expected = events.get(i);
            RDFAndroidSensorData actual = response.getData().get(i);
            assertEquals(expected.accuracy, actual.getAccuracy().getNumber());
            assertEquals(expected.timestamp, actual.getTimestamp().longValue());
            assertEquals(expected.values.length, actual.getValues().size());
            for (int j = 0; j < expected.values.length; j++) {
                assertEquals(expected.values[j], actual.getValues().get(j), 0);
            }
        }
        throw new TestSuccessException();
    }
}