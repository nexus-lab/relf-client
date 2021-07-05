package org.nexus_lab.relf.client.actions.android;

import android.app.Application;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Build;
import android.os.Looper;

import androidx.test.core.app.ApplicationProvider;

import org.nexus_lab.relf.client.actions.ActionCallback;
import org.nexus_lab.relf.client.exceptions.TestSuccessException;
import org.nexus_lab.relf.lib.rdfvalues.android.RDFAndroidLocation;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.shadows.ShadowLocationManager;
import org.robolectric.shadows.ShadowLooper;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.content.Context.LOCATION_SERVICE;
import static org.junit.Assert.assertTrue;

/**
 * @author Ruipeng Zhang
 */
@RunWith(RobolectricTestRunner.class)
public class GetAndroidLocationTest {
    private Context context;
    private ShadowLocationManager shadowManager;
    private ShadowApplication shadowApplication;

    @Before
    public void setup() {
        context = ApplicationProvider.getApplicationContext();
        shadowApplication = Shadows.shadowOf((Application) context);
        LocationManager manager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
        shadowManager = Shadows.shadowOf(manager);
        shadowApplication.grantPermissions(ACCESS_COARSE_LOCATION);
        shadowApplication.grantPermissions(ACCESS_FINE_LOCATION);
        shadowManager.setProviderEnabled(LocationManager.GPS_PROVIDER, true);
        shadowManager.setProviderEnabled(LocationManager.NETWORK_PROVIDER, true);
    }

    @Test(expected = TestSuccessException.class)
    public void execute_No_Enabled_Provider() {
        shadowApplication.denyPermissions(ACCESS_FINE_LOCATION);
        shadowManager.setProviderEnabled(LocationManager.NETWORK_PROVIDER, false);
        new GetAndroidLocation().execute(context, null, new ActionCallback<RDFAndroidLocation>() {
            @Override
            public void onResponse(RDFAndroidLocation response) {
            }

            @Override
            public void onError(Throwable e) {
                assertTrue(e.getMessage().contains(
                        "Location service providers are not enabled"));
                throw new TestSuccessException();
            }
        });
        for (LocationListener listener : shadowManager.getRequestLocationUpdateListeners()) {
            listener.onLocationChanged(new Location(LocationManager.GPS_PROVIDER));
        }
    }

    @Test(expected = TestSuccessException.class)
    public void execute_on_Provider_Error() {
        new GetAndroidLocation().execute(context, null, new ActionCallback<RDFAndroidLocation>() {
            @Override
            public void onResponse(RDFAndroidLocation response) {
            }

            @Override
            public void onError(Throwable e) {
                assertTrue(e.getMessage().contains("status: out of service"));
                throw new TestSuccessException();
            }
        });
        for (LocationListener listener : shadowManager.getRequestLocationUpdateListeners()) {
            listener.onStatusChanged(LocationManager.GPS_PROVIDER, LocationProvider.OUT_OF_SERVICE,
                    null);
        }
        ShadowLooper shadowLooper = Shadows.shadowOf(Looper.myLooper());
        shadowLooper.getScheduler().advanceToNextPostedRunnable();
    }

    @Test(expected = TestSuccessException.class)
    public void execute_on_Provider_Timeout() {
        new GetAndroidLocation().execute(context, null, new ActionCallback<RDFAndroidLocation>() {
            @Override
            public void onResponse(RDFAndroidLocation response) {
            }

            @Override
            public void onError(Throwable e) {
                assertTrue(e.getMessage().contains("status: timeout"));
                throw new TestSuccessException();
            }
        });
        ShadowLooper shadowLooper = Shadows.shadowOf(Looper.myLooper());
        shadowLooper.getScheduler().advanceBy(10, TimeUnit.SECONDS);
    }

    @Test(expected = TestSuccessException.class)
    public void execute_Return_on_Fine_Accuracy() {
        new GetAndroidLocation().execute(context, null, response -> {
            Assert.assertEquals(100.0f, response.getHorizontalAccuracy(), 0);
            throw new TestSuccessException();
        });
        Location location = new Location(LocationManager.GPS_PROVIDER);
        for (int i = 1; i < 5; i++) {
            for (LocationListener listener : shadowManager.getRequestLocationUpdateListeners()) {
                location.setAccuracy(300f / i);
                listener.onLocationChanged(location);
            }
        }
    }

    @Test(expected = TestSuccessException.class)
    public void execute_Return_on_Coarse_Accuracy() {
        shadowManager.setProviderEnabled(LocationManager.GPS_PROVIDER, false);
        new GetAndroidLocation().execute(context, null, response -> {
            Assert.assertEquals(5000.0f, response.getHorizontalAccuracy(), 0);
            throw new TestSuccessException();
        });
        Location location = new Location(LocationManager.NETWORK_PROVIDER);
        for (int i = 1; i < 5; i++) {
            for (LocationListener listener : shadowManager.getRequestLocationUpdateListeners()) {
                location.setAccuracy(15000f / i);
                listener.onLocationChanged(location);
            }
        }
    }

    @Test(expected = TestSuccessException.class)
    public void execute() {
        Random random = new Random();
        Location location = new Location(LocationManager.GPS_PROVIDER);
        location.setTime(System.currentTimeMillis());
        location.setLatitude(random.nextDouble());
        location.setLongitude(random.nextDouble());
        location.setAltitude(random.nextDouble());
        location.setAccuracy(random.nextFloat());
        location.setSpeed(random.nextFloat());
        location.setBearing(random.nextFloat());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            location.setSpeedAccuracyMetersPerSecond(random.nextFloat());
            location.setVerticalAccuracyMeters(random.nextFloat());
            location.setBearingAccuracyDegrees(random.nextFloat());
        }
        new GetAndroidLocation().execute(context, null, response -> {
            Assert.assertEquals(location.getProvider(), response.getProvider());
            Assert.assertEquals(location.getTime(), response.getTime().asDate().getTime());
            Assert.assertEquals(location.getLatitude(), response.getLatitude(), 0);
            Assert.assertEquals(location.getLongitude(), response.getLongitude(), 0);
            Assert.assertEquals(location.getAltitude(), response.getAltitude(), 0);
            Assert.assertEquals(location.getAccuracy(), response.getHorizontalAccuracy(),
                    0);
            Assert.assertEquals(location.getSpeed(), response.getSpeed(), 0);
            Assert.assertEquals(location.getBearing(), response.getBearing(), 0);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Assert.assertEquals(location.getVerticalAccuracyMeters(),
                        response.getVerticalAccuracy(), 0);
                Assert.assertEquals(location.getSpeedAccuracyMetersPerSecond(),
                        response.getSpeedAccuracy(), 0);
                Assert.assertEquals(location.getBearingAccuracyDegrees(),
                        response.getBearingAccuracy(), 0);
            }
            throw new TestSuccessException();
        });
        for (LocationListener listener : shadowManager.getRequestLocationUpdateListeners()) {
            listener.onLocationChanged(location);
        }
    }
}