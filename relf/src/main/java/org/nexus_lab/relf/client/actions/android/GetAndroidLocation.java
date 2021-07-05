package org.nexus_lab.relf.client.actions.android;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.content.Context.LOCATION_SERVICE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.location.LocationManager.GPS_PROVIDER;
import static android.location.LocationManager.NETWORK_PROVIDER;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

import androidx.core.content.ContextCompat;

import org.nexus_lab.relf.client.actions.Action;
import org.nexus_lab.relf.client.actions.ActionCallback;
import org.nexus_lab.relf.client.actions.UsesPermission;
import org.nexus_lab.relf.lib.rdfvalues.RDFDatetime;
import org.nexus_lab.relf.lib.rdfvalues.RDFNull;
import org.nexus_lab.relf.lib.rdfvalues.android.RDFAndroidLocation;

/**
 * @author Ruipeng Zhang
 */
@UsesPermission(allOf = {ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION})
public class GetAndroidLocation implements Action<RDFNull, RDFAndroidLocation> {
    private static final int PROVIDER_TIMEOUT = -3;
    private static final int PROVIDER_DISABLED = -2;
    private static final int PROVIDER_ENABLED = -1;

    private int lastStatus = Integer.MIN_VALUE;
    private String lastProvider;
    private Location lastLocation;
    private LocationManager manager;
    private ActionCallback<RDFAndroidLocation> callback;
    private LocationListener listener = new LocationListener();
    private Handler timeoutHandler = new Handler();
    private Runnable timeoutCallback = () -> {
        if (lastStatus == Integer.MIN_VALUE) {
            lastStatus = PROVIDER_TIMEOUT;
        }
        onResponse();
    };

    private void onResponse() {
        manager.removeUpdates(listener);
        timeoutHandler.removeCallbacks(timeoutCallback);
        if (lastLocation == null) {
            String status;
            switch (lastStatus) {
                case PROVIDER_ENABLED:
                    status = "enabled";
                    break;
                case PROVIDER_DISABLED:
                    status = "disabled";
                    break;
                case PROVIDER_TIMEOUT:
                    status = "timeout";
                    break;
                case LocationProvider.AVAILABLE:
                    status = "available";
                    break;
                case LocationProvider.OUT_OF_SERVICE:
                    status = "out of service";
                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    status = "temporarily unavailable";
                    break;
                default:
                    status = "unknown";
            }
            callback.onError(new Exception("Failed to access location from " +
                    lastProvider + " provider, status: " + status));
        } else {
            RDFAndroidLocation location = new RDFAndroidLocation();
            location.setTime(new RDFDatetime(lastLocation.getTime() * 1000));
            location.setLatitude(lastLocation.getLatitude());
            location.setLongitude(lastLocation.getLongitude());
            if (lastLocation.hasAltitude()) {
                location.setAltitude(lastLocation.getAltitude());
            }
            if (lastLocation.hasAccuracy()) {
                location.setHorizontalAccuracy(lastLocation.getAccuracy());
            }
            if (lastLocation.hasSpeed()) {
                location.setSpeed(lastLocation.getSpeed());
            }
            if (lastLocation.hasBearing()) {
                location.setBearing(lastLocation.getBearing());
            }
            if (lastLocation.getExtras() != null) {
                location.setExtras(lastLocation.getExtras().toString());
            }
            location.setProvider(lastLocation.getProvider());
            location.setIsFromMockProvider(lastLocation.isFromMockProvider());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (lastLocation.hasVerticalAccuracy()) {
                    location.setVerticalAccuracy(lastLocation.getVerticalAccuracyMeters());
                }
                if (lastLocation.hasSpeedAccuracy()) {
                    location.setSpeedAccuracy(lastLocation.getSpeedAccuracyMetersPerSecond());
                }
                if (lastLocation.hasBearingAccuracy()) {
                    location.setBearingAccuracy(lastLocation.getBearingAccuracyDegrees());
                }
            }
            callback.onResponse(location);
            callback.onComplete();
        }
    }

    @SuppressLint("MissingPermission")
    private void requestLocationUpdates(String provider) {
        lastProvider = provider;
        manager.requestLocationUpdates(provider, 1000, 1, listener);
        timeoutHandler.postDelayed(timeoutCallback, 10 * 1000);
    }

    @Override
    public void execute(Context context, RDFNull request, ActionCallback<RDFAndroidLocation> callback) {
        this.callback = callback;
        manager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
        if (manager != null) {
            if (ContextCompat.checkSelfPermission(context, ACCESS_FINE_LOCATION)
                    == PERMISSION_GRANTED && manager.isProviderEnabled(GPS_PROVIDER)) {
                requestLocationUpdates(GPS_PROVIDER);
            } else if (manager.isProviderEnabled(NETWORK_PROVIDER)) {
                requestLocationUpdates(NETWORK_PROVIDER);
            } else {
                callback.onError(new Exception("Location service providers are not enabled."));
            }
        } else {
            callback.onError(new Exception("Location service does not exist."));
        }
    }

    private class LocationListener implements android.location.LocationListener {
        @Override
        public void onLocationChanged(Location location) {
            lastLocation = location;
            lastProvider = location.getProvider();
            lastStatus = LocationProvider.AVAILABLE;
            if ((GPS_PROVIDER.equals(location.getProvider()) && location.getAccuracy() <= 100)
                    || (NETWORK_PROVIDER.equals(location.getProvider())
                    && location.getAccuracy() <= 5000)) {
                onResponse();
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            lastProvider = provider;
            lastStatus = status;
        }

        @Override
        public void onProviderEnabled(String provider) {
            lastProvider = provider;
            lastStatus = PROVIDER_ENABLED;
        }

        @Override
        public void onProviderDisabled(String provider) {
            lastProvider = provider;
            lastStatus = PROVIDER_DISABLED;
        }
    }
}
