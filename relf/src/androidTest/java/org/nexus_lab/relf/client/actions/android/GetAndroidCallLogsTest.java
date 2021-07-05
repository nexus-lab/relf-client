package org.nexus_lab.relf.client.actions.android;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.provider.CallLog;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.rule.GrantPermissionRule;

import com.github.javafaker.Faker;
import org.nexus_lab.relf.client.actions.ActionCallback;
import org.nexus_lab.relf.client.exceptions.TestSuccessException;
import org.nexus_lab.relf.lib.rdfvalues.android.RDFAndroidCallLog;
import org.nexus_lab.relf.proto.AndroidCallLog;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static android.provider.CallLog.Calls.ANSWERED_EXTERNALLY_TYPE;
import static android.provider.CallLog.Calls.CONTENT_URI;
import static android.provider.CallLog.Calls.PRESENTATION_PAYPHONE;
import static android.provider.CallLog.Calls.VOICEMAIL_TYPE;
import static org.nexus_lab.relf.proto.AndroidCallLog.NumberPresentation.PRESENTATION_ALLOWED;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


public class GetAndroidCallLogsTest {
    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.WRITE_CALL_LOG
    );
    private Context context;
    private List<FakeCallLog> calls;
    private ActionCallback<RDFAndroidCallLog> callback = new ActionCallback<RDFAndroidCallLog>() {
        private int i;

        @Override
        public void onResponse(RDFAndroidCallLog actual) {
            FakeCallLog expected = calls.get(i);
            assertEquals(expected.type, actual.getType().getNumber());
            assertEquals(expected.number, actual.getPhoneNumber());
            if (!actual.getPresentation().equals(PRESENTATION_ALLOWED)) {
                assertEquals(expected.name, actual.getContactName());
            }
            assertEquals(expected.date, actual.getDate().asMicrosecondsFromEpoch() / 1000);
            assertEquals(expected.duration, actual.getDuration().getSeconds());
            assertNotNull(actual.getCountryIso());
            assertNotNull(actual.getGeocodedLocation());
            assertEquals(expected.presentation, actual.getPresentation().getNumber());
            int features = 0;
            for (AndroidCallLog.CallFeatures feature : actual.getFeatures()) {
                features |= feature.getNumber();
            }
            assertEquals(expected.features, features);
            i++;
        }

        @Override
        public void onComplete() {
            assertEquals(calls.size(), i);
            throw new TestSuccessException();
        }
    };

    private FakeCallLog createCallLog(Context context) {
        Faker faker = new Faker();
        Random random = new Random();
        FakeCallLog call = new FakeCallLog();
        int type = VOICEMAIL_TYPE;
        while (type == VOICEMAIL_TYPE) {
            type = random.nextInt(ANSWERED_EXTERNALLY_TYPE) + 1;
        }
        call.type = type;
        call.number = faker.phoneNumber().phoneNumber();
        call.name = faker.name().name();
        call.date = System.currentTimeMillis();
        call.duration = random.nextInt(300);
        call.isAcknowledged = random.nextBoolean();
        call.presentation = random.nextInt(PRESENTATION_PAYPHONE) + 1;
        int features = 0;
        for (int i = 0; i < 6; i++) {
            if (random.nextBoolean()) {
                features |= 1 << i;
            }
        }
        call.features = features;
        call.build(context);
        return call;
    }

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(CallLog.Calls.CONTENT_URI, new String[]{CallLog.Calls._ID},
                null, null, null);
        while (cursor != null && cursor.moveToNext()) {
            resolver.delete(CONTENT_URI, CallLog.Calls._ID + " = ?", new String[]{
                    String.valueOf(cursor.getLong(cursor.getColumnIndex(CallLog.Calls._ID)))});
        }
        if (cursor != null) {
            cursor.close();
        }
    }

    @Test(expected = TestSuccessException.class)
    public void execute() {
        calls = new ArrayList<>();
        context = ApplicationProvider.getApplicationContext();
        for (int i = 0; i < 10; i++) {
            calls.add(createCallLog(context));
        }
        new GetAndroidCallLogs().execute(context, null, callback);
    }

    private class FakeCallLog {
        int type;
        String number;
        String name;
        long date;
        long duration;
        boolean isAcknowledged;
        int features;
        int presentation;

        void build(Context context) {
            ContentValues values = new ContentValues();
            values.put(CallLog.Calls.TYPE, type);
            values.put(CallLog.Calls.NUMBER, number);
            values.put(CallLog.Calls.CACHED_NAME, name);
            values.put(CallLog.Calls.DATE, date);
            values.put(CallLog.Calls.DURATION, duration);
            values.put(CallLog.Calls.NEW, isAcknowledged);
            values.put(CallLog.Calls.FEATURES, features);
            values.put(CallLog.Calls.NUMBER_PRESENTATION, presentation);
            context.getContentResolver().insert(CallLog.Calls.CONTENT_URI, values);
        }
    }
}