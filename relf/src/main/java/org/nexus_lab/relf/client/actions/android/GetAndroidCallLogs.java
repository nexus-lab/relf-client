package org.nexus_lab.relf.client.actions.android;

import static android.Manifest.permission.READ_CALL_LOG;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.CallLog.Calls;

import org.nexus_lab.relf.client.actions.Action;
import org.nexus_lab.relf.client.actions.ActionCallback;
import org.nexus_lab.relf.client.actions.UsesPermission;
import org.nexus_lab.relf.lib.rdfvalues.RDFNull;
import org.nexus_lab.relf.lib.rdfvalues.android.RDFAndroidCallLog;
import org.nexus_lab.relf.proto.AndroidCallLog.CallType;
import org.nexus_lab.relf.proto.AndroidCallLog.NumberPresentation;
import org.nexus_lab.relf.utils.store.ContentAdapter;

/**
 * @author Ruipeng Zhang
 */
@UsesPermission(READ_CALL_LOG)
public class GetAndroidCallLogs implements Action<RDFNull, RDFAndroidCallLog> {
    @Override
    public void execute(Context context, RDFNull request, ActionCallback<RDFAndroidCallLog> callback) {
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(Calls.CONTENT_URI, null, null, null, Calls._ID + " ASC");
        if (cursor != null) {
            ContentAdapter<RDFAndroidCallLog> adapter = new ContentAdapter<>(cursor);
            adapter.map(Calls.TYPE, CallType.class, RDFAndroidCallLog::setType);
            adapter.map(Calls.NUMBER, String.class, RDFAndroidCallLog::setPhoneNumber);
            adapter.map(Calls.CACHED_NAME, String.class, RDFAndroidCallLog::setContactName);
            adapter.map(Calls.DATE, Long.class, RDFAndroidCallLog::setDateFromEpoch);
            adapter.map(Calls.DURATION, Long.class, RDFAndroidCallLog::setDurationFromSeconds);
            adapter.map(Calls.NEW, Boolean.class, RDFAndroidCallLog::setIsAcknowledged);
            adapter.map(Calls.COUNTRY_ISO, String.class, RDFAndroidCallLog::setCountryIso);
            adapter.map(Calls.GEOCODED_LOCATION, String.class,
                    RDFAndroidCallLog::setGeocodedLocation);
            adapter.map(Calls.NUMBER_PRESENTATION, NumberPresentation.class,
                    RDFAndroidCallLog::setPresentation);
            adapter.map(Calls.FEATURES, Integer.class, RDFAndroidCallLog::setFeaturesFromBits);
            while (adapter.moveToNext()) {
                RDFAndroidCallLog callLog = new RDFAndroidCallLog();
                adapter.fill(callLog);
                callback.onResponse(callLog);
            }
            adapter.close();
        }
        callback.onComplete();
    }
}
