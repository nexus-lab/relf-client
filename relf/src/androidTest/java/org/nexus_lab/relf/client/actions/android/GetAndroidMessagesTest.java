package org.nexus_lab.relf.client.actions.android;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

import static org.nexus_lab.relf.client.actions.android.GetAndroidMessages.ADDRESS_BCC;
import static org.nexus_lab.relf.client.actions.android.GetAndroidMessages.ADDRESS_CC;
import static org.nexus_lab.relf.client.actions.android.GetAndroidMessages.ADDRESS_FROM;
import static org.nexus_lab.relf.client.actions.android.GetAndroidMessages.ADDRESS_TO;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.Telephony.Mms;
import android.provider.Telephony.Sms;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.rule.GrantPermissionRule;

import com.github.javafaker.Faker;
import org.nexus_lab.relf.client.actions.ActionCallback;
import org.nexus_lab.relf.client.exceptions.TestSuccessException;
import org.nexus_lab.relf.lib.rdfvalues.android.RDFAndroidSmsMms;
import org.nexus_lab.relf.proto.AndroidSmsMms;
import org.nexus_lab.relf.test.R;
import org.nexus_lab.relf.utils.RandomUtils;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * @author Ruipeng Zhang
 */
public class GetAndroidMessagesTest {
    private final Faker faker = new Faker();
    private final List<FakeSms> smsList = new ArrayList<>();
    private final List<FakeMms> mmsList = new ArrayList<>();
    private final ActionCallback<RDFAndroidSmsMms> callback =
            new ActionCallback<RDFAndroidSmsMms>() {
                private int i;

                @Override
                public void onResponse(RDFAndroidSmsMms smsMms) {
                    if (i < smsList.size()) {
                        FakeSms sms = smsList.get(i);
                        GetAndroidMessagesTest.this.assertSmsEquals(sms, smsMms);
                    } else {
                        FakeMms mms = mmsList.get(i - smsList.size());
                        GetAndroidMessagesTest.this.assertMmsEquals(mms, smsMms);
                    }
                    i++;
                }

                @Override
                public void onComplete() {
                    assertEquals(smsList.size() + mmsList.size(), i);
                    throw new TestSuccessException();
                }
            };

    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(
            Manifest.permission.READ_SMS,
            "android.permission.WRITE_SMS"
    );
    private Context context;
    private ContentResolver resolver;

    private void assertSmsEquals(FakeSms sms, RDFAndroidSmsMms smsMms) {
        assertEquals(AndroidSmsMms.Type.SMS, smsMms.getType());
        assertEquals(sms.messageBox, smsMms.getMessageBox().getNumber());
        assertEquals(sms.threadId, smsMms.getThreadId().longValue());
        assertEquals(sms.status, smsMms.getStatus().intValue());
        switch (sms.messageBox) {
            case Sms.MESSAGE_TYPE_INBOX:
                assertEquals(1, smsMms.getFrom().size());
                assertEquals(sms.address, smsMms.getFrom().get(0));
                assertNull(smsMms.getTo());
                break;
            case Sms.MESSAGE_TYPE_SENT:
            case Sms.MESSAGE_TYPE_DRAFT:
            case Sms.MESSAGE_TYPE_OUTBOX:
            case Sms.MESSAGE_TYPE_FAILED:
            case Sms.MESSAGE_TYPE_QUEUED:
                assertNull(smsMms.getFrom());
                assertEquals(1, smsMms.getTo().size());
                assertEquals(sms.address, smsMms.getTo().get(0));
                break;
            case Sms.MESSAGE_TYPE_ALL:
            default:
                break;
        }
        assertNull(smsMms.getCc());
        assertNull(smsMms.getBcc());
        assertEquals(sms.dateReceived, smsMms.getDateReceived().asSecondsFromEpoch());
        assertEquals(sms.dateSent, smsMms.getDateSent().asSecondsFromEpoch());
        assertEquals(sms.subject, smsMms.getSubject());
        assertEquals(1, smsMms.getBody().size());
        assertEquals("text/plain", smsMms.getBody().get(0).getContentType());
        assertArrayEquals(sms.body.getBytes(), smsMms.getBody().get(0).getData());
        assertEquals(sms.isRead, smsMms.getIsRead());
        assertEquals(sms.isSeen, smsMms.getIsSeen());
        assertEquals(sms.isLocked, smsMms.getIsLocked());
    }

    private void assertMmsEquals(FakeMms mms, RDFAndroidSmsMms smsMms) {
        assertEquals(AndroidSmsMms.Type.MMS, smsMms.getType());
        assertEquals(mms.messageBox, smsMms.getMessageBox().getNumber());
        assertEquals(mms.threadId, smsMms.getThreadId().longValue());
        assertEquals(mms.status, smsMms.getStatus().intValue());

        List<List<String>> expectedAddresses = Arrays.asList(mms.from, mms.to, mms.cc, mms.bcc);
        List<List<String>> actualAddresses = Arrays.asList(smsMms.getFrom(), smsMms.getTo(),
                smsMms.getCc(), smsMms.getBcc());
        for (int i = 0; i < expectedAddresses.size(); i++) {
            List<String> expected = expectedAddresses.get(i);
            List<String> actual = actualAddresses.get(i);
            if (expected.size() == 0) {
                assertNull(actual);
            } else {
                assertEquals(expected.size(), actual.size());
                for (int j = 0; j < expected.size(); j++) {
                    assertEquals(expected.get(j), actual.get(j));
                }
            }
        }

        assertEquals(mms.dateReceived, smsMms.getDateReceived().asSecondsFromEpoch());
        assertEquals(mms.dateSent, smsMms.getDateSent().asSecondsFromEpoch());
        assertEquals(mms.subject, smsMms.getSubject());

        assertEquals(1, smsMms.getBody().size());
        assertEquals(mms.contentType, smsMms.getBody().get(0).getContentType());
        assertArrayEquals(mms.body, smsMms.getBody().get(0).getData());

        assertEquals(mms.isRead, smsMms.getIsRead());
        assertEquals(mms.isSeen, smsMms.getIsSeen());
        assertEquals(mms.isLocked, smsMms.getIsLocked());
    }

    private FakeSms createSms(long threadId) {
        Random random = new Random();
        FakeSms sms = new FakeSms();
        sms.messageBox = RandomUtils.oneOf(
                Sms.MESSAGE_TYPE_INBOX,
                Sms.MESSAGE_TYPE_SENT
        );
        sms.threadId = threadId;
        sms.status = RandomUtils.oneOf(
                Sms.STATUS_NONE,
                Sms.STATUS_COMPLETE,
                Sms.STATUS_PENDING,
                Sms.STATUS_FAILED);
        sms.dateSent = System.currentTimeMillis() / 1000L;
        sms.dateReceived = System.currentTimeMillis() / 1000L;
        sms.address = faker.phoneNumber().cellPhone();
        sms.subject = faker.lorem().sentence();
        sms.body = faker.lorem().paragraph();
        sms.creator = context.getPackageName();
        sms.isRead = sms.messageBox == Sms.MESSAGE_TYPE_SENT || random.nextBoolean();
        sms.isSeen = random.nextBoolean();
        sms.isLocked = random.nextBoolean();
        sms.build();
        return sms;
    }

    private FakeMms createMms(long threadId) throws Exception {
        Random random = new Random();
        FakeMms mms = new FakeMms();
        mms.messageBox = RandomUtils.oneOf(
                Mms.MESSAGE_BOX_INBOX,
                Mms.MESSAGE_BOX_SENT
        );
        mms.threadId = threadId;
        mms.status = random.nextInt(135 - 128) + 128;
        mms.dateSent = System.currentTimeMillis() / 1000L;
        mms.dateReceived = System.currentTimeMillis() / 1000L;

        mms.from = new ArrayList<>();
        for (int i = 0; i < random.nextInt(3); i++) {
            mms.from.add(random.nextBoolean() ? faker.phoneNumber().cellPhone()
                    : faker.internet().emailAddress());
        }
        mms.to = new ArrayList<>();
        for (int i = 0; i < random.nextInt(3); i++) {
            mms.to.add(random.nextBoolean() ? faker.phoneNumber().cellPhone()
                    : faker.internet().emailAddress());
        }
        mms.cc = new ArrayList<>();
        for (int i = 0; i < random.nextInt(3); i++) {
            mms.cc.add(random.nextBoolean() ? faker.phoneNumber().cellPhone()
                    : faker.internet().emailAddress());
        }
        mms.bcc = new ArrayList<>();
        for (int i = 0; i < random.nextInt(3); i++) {
            mms.bcc.add(random.nextBoolean() ? faker.phoneNumber().cellPhone()
                    : faker.internet().emailAddress());
        }

        mms.subject = faker.lorem().sentence();
        if (random.nextBoolean()) {
            mms.contentType = "image/png";
            Drawable d = context.getDrawable(R.drawable.ic_launcher);
            Bitmap bitmap = ((BitmapDrawable) d).getBitmap();
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            mms.body = stream.toByteArray();
        } else {
            mms.contentType = "text/plain";
            mms.body = faker.lorem().sentence().getBytes();
        }
        mms.creator = context.getPackageName();
        mms.isRead = mms.messageBox == Mms.MESSAGE_BOX_SENT || random.nextBoolean();
        mms.isSeen = random.nextBoolean();
        mms.isLocked = random.nextBoolean();
        mms.build();
        return mms;
    }

    private void setAsDefaultMessagingApp() throws Exception {
        if (!Sms.getDefaultSmsPackage(context).equals(context.getPackageName())) {
            Intent intent = new Intent(Sms.Intents.ACTION_CHANGE_DEFAULT);
            intent.putExtra(Sms.Intents.EXTRA_PACKAGE_NAME,
                    context.getPackageName());
            intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            long now = System.currentTimeMillis();
            while (!context.getPackageName().equals(Sms.getDefaultSmsPackage(context))) {
                if (System.currentTimeMillis() - now > 10 * 1000) {
                    throw new Exception(
                            "Default SMS package is not set to " + context.getPackageName());
                }
                Thread.sleep(100);
            }
        }
    }

    private void clearSms() {
        try (Cursor cursor = resolver.query(Sms.CONTENT_URI, new String[]{Sms._ID}, null, null,
                null)) {
            while (cursor != null && cursor.moveToNext()) {
                Uri smsUri = Sms.CONTENT_URI.buildUpon().appendEncodedPath(
                        String.valueOf(cursor.getLong(cursor.getColumnIndex(Sms._ID)))).build();
                resolver.delete(smsUri, null, null);
            }
        }
    }

    private void clearMms() {
        try (Cursor cursor = resolver.query(Mms.CONTENT_URI, new String[]{Mms._ID}, null, null,
                null)) {
            while (cursor != null && cursor.moveToNext()) {
                Uri mmsUri = Mms.CONTENT_URI.buildUpon().appendEncodedPath(
                        String.valueOf(cursor.getLong(cursor.getColumnIndex(Mms._ID)))).build();
                resolver.delete(mmsUri, null, null);
            }
        }
    }

    @Before
    public void setup() throws Exception {
        context = ApplicationProvider.getApplicationContext();
        resolver = context.getContentResolver();
        setAsDefaultMessagingApp();
        clearSms();
        clearMms();
    }

    @Test(expected = TestSuccessException.class)
    public void execute() throws Exception {
        Random random = new Random();
        for (int i = 0; i < random.nextInt(4) + 16; i++) {
            smsList.add(createSms(i % 4 + 1));
            mmsList.add(createMms(i % 4 + 1));
        }
        new GetAndroidMessages().execute(context, null, callback);
    }

    private class FakeSms {
        int messageBox;
        long threadId;
        int status;
        long dateSent;
        long dateReceived;
        String address;
        String subject;
        String body;
        String creator;
        boolean isRead;
        boolean isSeen;
        boolean isLocked;

        void build() {
            ContentValues values = new ContentValues();
            values.put(Sms.TYPE, messageBox);
            values.put(Sms.THREAD_ID, threadId);
            values.put(Sms.STATUS, status);
            values.put(Sms.DATE, dateReceived);
            values.put(Sms.DATE_SENT, dateSent);
            values.put(Sms.ADDRESS, address);
            values.put(Sms.SUBJECT, subject);
            values.put(Sms.BODY, body);
            values.put(Sms.CREATOR, creator);
            values.put(Sms.READ, isRead);
            values.put(Sms.SEEN, isSeen);
            values.put(Sms.LOCKED, isLocked);
            resolver.insert(Sms.CONTENT_URI, values);
        }
    }

    private class FakeMms {
        int messageBox;
        long threadId;
        int status;
        long dateSent;
        long dateReceived;
        List<String> from;
        List<String> to;
        List<String> cc;
        List<String> bcc;
        String subject;
        byte[] body;
        String contentType;
        String creator;
        boolean isRead;
        boolean isSeen;
        boolean isLocked;

        void buildPart(String messageId) throws Exception {
            Uri uri = Mms.CONTENT_URI.buildUpon()
                    .appendPath(String.valueOf(messageId))
                    .appendPath("part")
                    .build();
            ContentValues values = new ContentValues();
            values.put(Mms.Part.MSG_ID, messageId);
            values.put(Mms.Part.CONTENT_TYPE, contentType);
            values.put(Mms.Part.CONTENT_ID, "<" + System.currentTimeMillis() + ">");
            if ("text/plain".equals(contentType)) {
                values.put(Mms.Part.TEXT, new String(body));
            }
            Uri res = context.getContentResolver().insert(uri, values);
            if (!"text/plain".equals(contentType)) {
                try (OutputStream os = context.getContentResolver().openOutputStream(res)) {
                    try (ByteArrayInputStream is = new ByteArrayInputStream(body)) {
                        byte[] buffer = new byte[256];
                        for (int len; (len = is.read(buffer)) != -1; ) {
                            os.write(buffer, 0, len);
                        }
                    }
                }
            }
        }

        void buildAddress(String messageId, String address, int type) {
            ContentValues values = new ContentValues();
            values.put(Mms.Addr.MSG_ID, messageId);
            values.put(Mms.Addr.ADDRESS, address);
            values.put(Mms.Addr.CHARSET, 106);
            values.put(Mms.Addr.TYPE, type);
            Uri uri = Mms.CONTENT_URI.buildUpon()
                    .appendPath(String.valueOf(messageId))
                    .appendPath("addr")
                    .build();
            context.getContentResolver().insert(uri, values);
        }

        void build() throws Exception {
            ContentValues values = new ContentValues();
            values.put(Mms.MESSAGE_BOX, messageBox);
            values.put(Mms.THREAD_ID, threadId);
            values.put(Mms.STATUS, status);
            values.put(Mms.DATE, dateReceived);
            values.put(Mms.DATE_SENT, dateSent);
            values.put(Mms.SUBJECT, subject);
            values.put(Mms.SUBJECT_CHARSET, 106);
            values.put(Mms.CONTENT_TYPE, "application/vnd.wap.multipart.related");
            values.put(Mms.MESSAGE_CLASS, "personal");
            values.put(Mms.MMS_VERSION, 18);
            values.put(Mms.EXPIRY, body.length);
            values.put(Mms.PRIORITY, 129); // Low - 128; Normal - 129; High - 130
            values.put(Mms.DELIVERY_REPORT, 129); // Yes - 128; No - 129
            values.put(Mms.READ_REPORT, 129); // Yes - 128; No - 129
            values.put(Mms.TRANSACTION_ID, "T" + Long.toHexString(System.currentTimeMillis()));
            values.put(Mms.RESPONSE_STATUS, 128); // OK
            values.put(Mms.CREATOR, creator);
            values.put(Mms.READ, isRead);
            values.put(Mms.SEEN, isSeen);
            values.put(Mms.LOCKED, isLocked);
            values.put(Mms.SUBSCRIPTION_ID, 1);
            values.put(Mms.MESSAGE_SIZE, body.length);
            if (messageBox == Mms.MESSAGE_BOX_INBOX) {
                values.put(Mms.MESSAGE_TYPE, 132); // m-retrieve-conf - 132
            } else {
                values.put(Mms.MESSAGE_TYPE, 128); // m-send-req - 128
            }
            Uri res = resolver.insert(Mms.CONTENT_URI, values);
            String messageId = res.getLastPathSegment().trim();
            buildPart(messageId);
            for (String address : from) {
                buildAddress(messageId, address, ADDRESS_FROM);
            }
            for (String address : to) {
                buildAddress(messageId, address, ADDRESS_TO);
            }
            for (String address : cc) {
                buildAddress(messageId, address, ADDRESS_CC);
            }
            for (String address : bcc) {
                buildAddress(messageId, address, ADDRESS_BCC);
            }
        }
    }
}