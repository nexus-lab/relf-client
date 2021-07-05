package org.nexus_lab.relf.client.actions.android;

import static android.Manifest.permission.READ_SMS;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Telephony.Mms;
import android.provider.Telephony.Sms;
import android.util.Log;

import org.nexus_lab.relf.client.actions.Action;
import org.nexus_lab.relf.client.actions.ActionCallback;
import org.nexus_lab.relf.client.actions.UsesPermission;
import org.nexus_lab.relf.lib.rdfvalues.RDFNull;
import org.nexus_lab.relf.lib.rdfvalues.android.RDFAndroidSmsMms;
import org.nexus_lab.relf.lib.rdfvalues.android.RDFAndroidSmsMmsBody;
import org.nexus_lab.relf.proto.AndroidSmsMms;
import org.nexus_lab.relf.utils.store.ContentAdapter;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Ruipeng Zhang
 */
@UsesPermission(READ_SMS)
public class GetAndroidMessages implements Action<RDFNull, RDFAndroidSmsMms> {
    /**
     * MMS address type: TO
     */
    public static final int ADDRESS_TO = 0x97;
    /**
     * MMS address type: FROM
     */
    public static final int ADDRESS_FROM = 0x89;
    /**
     * MMS address type: BCC
     */
    public static final int ADDRESS_BCC = 0x81;
    /**
     * MMS address type: CC
     */
    public static final int ADDRESS_CC = 0x82;

    private static final String TAG = GetAndroidMessages.class.getSimpleName();

    private ContentAdapter.ColumnMapper<RDFAndroidSmsMms, String> mapper = (target, value) -> {
        switch (target.getMessageBox()) {
            case MESSAGE_BOX_INBOX:
                target.addFromAddress(value);
                break;
            case MESSAGE_BOX_SENT:
            case MESSAGE_BOX_DRAFT:
            case MESSAGE_BOX_OUTBOX:
            case MESSAGE_BOX_FAILED:
            case MESSAGE_BOX_QUEUED:
                target.addToAddress(value);
                break;
            case MESSAGE_BOX_ALL:
            default:
                break;
        }
    };

    private ContentAdapter.ColumnMapper<RDFAndroidSmsMms, String> addrMapper = (target, value) -> {
        switch (target.getAddressType()) {
            case ADDRESS_FROM:
                target.addFromAddress(value);
                break;
            case ADDRESS_TO:
                target.addToAddress(value);
                break;
            case ADDRESS_CC:
                target.addCcAddress(value);
                break;
            case ADDRESS_BCC:
                target.addBccAddress(value);
                break;
            default:
                break;
        }
    };

    private void getSmsMessages(ContentResolver resolver, ActionCallback<RDFAndroidSmsMms> callback) {
        Cursor cursor = resolver.query(Sms.CONTENT_URI, null, null, null, Sms._ID + " ASC");
        if (cursor != null) {
            ContentAdapter<RDFAndroidSmsMms> adapter = new ContentAdapter<>(cursor);
            adapter.map(Sms.TYPE, AndroidSmsMms.MessageBox.class, RDFAndroidSmsMms::setMessageBox);
            adapter.map(Sms.THREAD_ID, Long.class, RDFAndroidSmsMms::setThreadId);
            adapter.map(Sms.STATUS, Integer.class, RDFAndroidSmsMms::setStatus);
            adapter.map(Sms.ADDRESS, String.class, mapper);
            adapter.map(Sms.DATE, Long.class, RDFAndroidSmsMms::setDateReceivedFromEpoch);
            adapter.map(Sms.DATE_SENT, Long.class, RDFAndroidSmsMms::setDateSentFromEpoch);
            adapter.map(Sms.SUBJECT, String.class, RDFAndroidSmsMms::setSubject);
            adapter.map(Sms.BODY, String.class, RDFAndroidSmsMms::addBody);
            adapter.map(Sms.READ, Boolean.class, RDFAndroidSmsMms::setIsRead);
            adapter.map(Sms.SEEN, Boolean.class, RDFAndroidSmsMms::setIsSeen);
            adapter.map(Sms.LOCKED, Boolean.class, RDFAndroidSmsMms::setIsLocked);
            while (adapter.moveToNext()) {
                RDFAndroidSmsMms message = new RDFAndroidSmsMms();
                adapter.fill(message);
                message.setType(AndroidSmsMms.Type.SMS);
                callback.onResponse(message);
            }
            adapter.close();
        }
    }

    private void getMmsAddress(ContentResolver resolver, RDFAndroidSmsMms message, long messageId) {
        Uri uri = Mms.CONTENT_URI.buildUpon().appendPath(String.valueOf(messageId))
                .appendPath("addr")
                .build();
        ContentAdapter<RDFAndroidSmsMms> adapter = new ContentAdapter<>(resolver, uri);
        adapter.map(Mms.Addr.TYPE, Integer.class, RDFAndroidSmsMms::setAddressType);
        adapter.map(Mms.Addr.ADDRESS, String.class, addrMapper);
        while (adapter.moveToNext()) {
            adapter.fill(message);
        }
        adapter.close();
    }

    private void getMmsPart(ContentResolver resolver, RDFAndroidSmsMms message, long messageId) {
        Uri uri = Mms.CONTENT_URI.buildUpon().appendPath("part").build();
        ContentAdapter<RDFAndroidSmsMmsBody> adapter = new ContentAdapter<>(resolver, uri,
                Mms.Part.MSG_ID + " = ?", String.valueOf(messageId));
        adapter.map(Mms.Part.CONTENT_TYPE, String.class, RDFAndroidSmsMmsBody::setContentType);
        adapter.map(Mms.Part.TEXT, String.class, (target, value) -> {
            if (value != null) {
                target.setData(value.getBytes());
            }
        });
        adapter.map(Mms.Part._ID, Long.class, (target, id) -> {
            try {
                if (!"text/plain".equals(target.getContentType())) {
                    Uri partUri = uri.buildUpon().appendPath(String.valueOf(id)).build();
                    InputStream stream = resolver.openInputStream(partUri);
                    if (stream != null) {
                        target.setData(IOUtils.toByteArray(stream));
                    }
                }
            } catch (IOException e) {
                Log.w(TAG, e);
            }
        });
        while (adapter.moveToNext()) {
            RDFAndroidSmsMmsBody body = new RDFAndroidSmsMmsBody();
            adapter.fill(body);
            message.addBody(body);
        }
        adapter.close();
    }

    private void getMmsMessages(ContentResolver resolver, ActionCallback callback) {
        Cursor cursor = resolver.query(Mms.CONTENT_URI, null, null, null, Mms._ID + " ASC");
        if (cursor != null) {
            ContentAdapter<RDFAndroidSmsMms> adapter = new ContentAdapter<>(cursor);
            adapter.map(Mms.MESSAGE_BOX, AndroidSmsMms.MessageBox.class,
                    RDFAndroidSmsMms::setMessageBox);
            adapter.map(Mms.THREAD_ID, Long.class, RDFAndroidSmsMms::setThreadId);
            adapter.map(Mms.STATUS, Integer.class, RDFAndroidSmsMms::setStatus);
            adapter.map(Mms.DATE, Long.class, RDFAndroidSmsMms::setDateReceivedFromEpoch);
            adapter.map(Mms.DATE_SENT, Long.class, RDFAndroidSmsMms::setDateSentFromEpoch);
            adapter.map(Mms.SUBJECT, String.class, RDFAndroidSmsMms::setSubject);
            adapter.map(Mms.READ, Boolean.class, RDFAndroidSmsMms::setIsRead);
            adapter.map(Mms.SEEN, Boolean.class, RDFAndroidSmsMms::setIsSeen);
            adapter.map(Mms.LOCKED, Boolean.class, RDFAndroidSmsMms::setIsLocked);
            adapter.map(Mms._ID, Long.class, (target, id) -> {
                getMmsAddress(resolver, target, id);
                getMmsPart(resolver, target, id);
            });
            while (adapter.moveToNext()) {
                RDFAndroidSmsMms message = new RDFAndroidSmsMms();
                adapter.fill(message);
                message.setType(AndroidSmsMms.Type.MMS);
                callback.onResponse(message);
            }
            adapter.close();
        }
    }

    @Override
    public void execute(Context context, RDFNull request, ActionCallback<RDFAndroidSmsMms> callback) {
        ContentResolver resolver = context.getContentResolver();
        getSmsMessages(resolver, callback);
        getMmsMessages(resolver, callback);
        callback.onComplete();
    }
}
