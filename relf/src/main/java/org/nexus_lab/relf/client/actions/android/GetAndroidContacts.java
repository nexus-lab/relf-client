package org.nexus_lab.relf.client.actions.android;

import static android.Manifest.permission.READ_CONTACTS;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Im;
import android.provider.ContactsContract.CommonDataKinds.Nickname;
import android.provider.ContactsContract.CommonDataKinds.Note;
import android.provider.ContactsContract.CommonDataKinds.Organization;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.SipAddress;
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;
import android.provider.ContactsContract.CommonDataKinds.Website;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;

import org.nexus_lab.relf.client.actions.Action;
import org.nexus_lab.relf.client.actions.ActionCallback;
import org.nexus_lab.relf.client.actions.UsesPermission;
import org.nexus_lab.relf.lib.rdfvalues.RDFNull;
import org.nexus_lab.relf.lib.rdfvalues.android.RDFAndroidContactInfo;
import org.nexus_lab.relf.proto.AndroidContactInfo.AndroidEmailAddress;
import org.nexus_lab.relf.proto.AndroidContactInfo.AndroidIM;
import org.nexus_lab.relf.proto.AndroidContactInfo.AndroidPhoneNumber;
import org.nexus_lab.relf.proto.AndroidContactInfo.AndroidPostalAddress;
import org.nexus_lab.relf.proto.AndroidContactInfo.EmailType;
import org.nexus_lab.relf.proto.AndroidContactInfo.IMProtocol;
import org.nexus_lab.relf.proto.AndroidContactInfo.IMType;
import org.nexus_lab.relf.proto.AndroidContactInfo.PhoneNumberType;
import org.nexus_lab.relf.proto.AndroidContactInfo.PostalAddressType;
import org.nexus_lab.relf.utils.store.ContentAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Ruipeng Zhang
 */
@UsesPermission(READ_CONTACTS)
public class GetAndroidContacts implements Action<RDFNull, RDFAndroidContactInfo> {
    private List<Integer> getContactIds(ContentResolver resolver) {
        List<Integer> ids = new ArrayList<>();
        try (Cursor cursor = resolver.query(Contacts.CONTENT_URI, null, null, null, null)) {
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    int id = cursor.getInt(cursor.getColumnIndex(Contacts._ID));
                    String lookUp = cursor.getString(cursor.getColumnIndex(Contacts.LOOKUP_KEY));
                    Uri lookupUri = Contacts.getLookupUri(id, lookUp);
                    try (Cursor lookUpCursor = resolver.query(lookupUri, null, null, null, null)) {
                        if (lookUpCursor != null) {
                            while (lookUpCursor.moveToNext()) {
                                ids.add(lookUpCursor.getInt(
                                        lookUpCursor.getColumnIndex(Contacts._ID)));
                            }
                        }
                    }
                }
                cursor.close();
            }
        }
        Collections.sort(ids);
        return ids;
    }

    private List<AndroidPhoneNumber> createPhoneNumbers(ContentResolver resolver, int contactId) {
        List<AndroidPhoneNumber> result = new ArrayList<>();
        try (ContentAdapter<AndroidPhoneNumber.Builder> adapter = new ContentAdapter<>(resolver,
                Phone.CONTENT_URI, Phone.CONTACT_ID + " = ?", String.valueOf(contactId))) {
            adapter.map(Phone.NUMBER, String.class, AndroidPhoneNumber.Builder::setNumber);
            adapter.map(Phone.TYPE, PhoneNumberType.class, AndroidPhoneNumber.Builder::setType);
            adapter.map(Phone.LABEL, String.class, AndroidPhoneNumber.Builder::setLabel);
            while (adapter.moveToNext()) {
                AndroidPhoneNumber.Builder builder = AndroidPhoneNumber.newBuilder();
                adapter.fill(builder);
                result.add(builder.build());
            }
        }
        return result;
    }

    private List<AndroidEmailAddress> createEmailAddresses(ContentResolver resolver,
            int contactId) {
        List<AndroidEmailAddress> result = new ArrayList<>();
        try (ContentAdapter<AndroidEmailAddress.Builder> adapter = new ContentAdapter<>(resolver,
                Email.CONTENT_URI, Email.CONTACT_ID + " = ?", String.valueOf(contactId))) {
            adapter.map(Email.ADDRESS, String.class, AndroidEmailAddress.Builder::setAddress);
            adapter.map(Email.TYPE, EmailType.class, AndroidEmailAddress.Builder::setType);
            adapter.map(Email.LABEL, String.class, AndroidEmailAddress.Builder::setLabel);
            while (adapter.moveToNext()) {
                AndroidEmailAddress.Builder builder = AndroidEmailAddress.newBuilder();
                adapter.fill(builder);
                result.add(builder.build());
            }
        }
        return result;
    }

    private List<AndroidPostalAddress> createPostalAddresses(ContentResolver resolver,
            int contactId) {
        List<AndroidPostalAddress> result = new ArrayList<>();
        try (ContentAdapter<AndroidPostalAddress.Builder> adapter = new ContentAdapter<>(resolver,
                StructuredPostal.CONTENT_URI, StructuredPostal.CONTACT_ID + " = ?",
                String.valueOf(contactId))) {
            adapter.map(StructuredPostal.FORMATTED_ADDRESS, String.class,
                    AndroidPostalAddress.Builder::setAddress);
            adapter.map(StructuredPostal.TYPE, PostalAddressType.class,
                    AndroidPostalAddress.Builder::setType);
            adapter.map(StructuredPostal.LABEL, String.class,
                    AndroidPostalAddress.Builder::setLabel);
            while (adapter.moveToNext()) {
                AndroidPostalAddress.Builder builder = AndroidPostalAddress.newBuilder();
                adapter.fill(builder);
                result.add(builder.build());
            }
        }
        return result;
    }

    private List<AndroidIM> createIMs(ContentResolver resolver, int contactId) {
        List<AndroidIM> result = new ArrayList<>();
        try (ContentAdapter<AndroidIM.Builder> adapter = new ContentAdapter<>(resolver,
                Data.CONTENT_URI, Im.CONTACT_ID + " = ? AND " + Im.MIMETYPE + " = ?",
                String.valueOf(contactId), Im.CONTENT_ITEM_TYPE)) {
            adapter.map(Im.DATA, String.class, AndroidIM.Builder::setAccount);
            adapter.map(Im.PROTOCOL, IMProtocol.class, AndroidIM.Builder::setProtocol);
            adapter.map(Im.CUSTOM_PROTOCOL, String.class, AndroidIM.Builder::setCustomProtocol);
            adapter.map(Im.TYPE, IMType.class, AndroidIM.Builder::setType);
            adapter.map(Im.LABEL, String.class, AndroidIM.Builder::setLabel);
            while (adapter.moveToNext()) {
                AndroidIM.Builder builder = AndroidIM.newBuilder();
                adapter.fill(builder);
                result.add(builder.build());
            }
        }
        return result;
    }

    private RDFAndroidContactInfo createContactInfo(ContentResolver resolver, int contactId) {
        RDFAndroidContactInfo info = new RDFAndroidContactInfo();
        try (ContactInfoAdapter adapter = new ContactInfoAdapter(resolver, Contacts.CONTENT_URI,
                Contacts._ID + " = ?", String.valueOf(contactId))) {
            adapter.map(Contacts.DISPLAY_NAME, String.class, RDFAndroidContactInfo::setName);
            adapter.map(Contacts.PHONETIC_NAME, String.class,
                    RDFAndroidContactInfo::setPhoneticName);
            if (adapter.moveToNext()) {
                adapter.fill(info);
            }
        }
        try (ContactInfoAdapter adapter = new ContactInfoAdapter(resolver, Data.CONTENT_URI,
                Nickname.CONTACT_ID + " = ? AND " + Nickname.MIMETYPE + " = ?",
                String.valueOf(contactId), Nickname.CONTENT_ITEM_TYPE)) {
            adapter.map(Nickname.NAME, String.class, RDFAndroidContactInfo::setNickname);
            if (adapter.moveToNext()) {
                adapter.fill(info);
            }
        }
        try (ContactInfoAdapter adapter = new ContactInfoAdapter(resolver, Data.CONTENT_URI,
                Organization.CONTACT_ID + " = ? AND " + Organization.MIMETYPE + " = ?",
                String.valueOf(contactId), Organization.CONTENT_ITEM_TYPE)) {
            adapter.map(Organization.COMPANY, String.class, RDFAndroidContactInfo::setCompany);
            adapter.map(Organization.TITLE, String.class, RDFAndroidContactInfo::setTitle);
            if (adapter.moveToNext()) {
                adapter.fill(info);
            }
        }
        try (ContactInfoAdapter adapter = new ContactInfoAdapter(resolver, Data.CONTENT_URI,
                Website.CONTACT_ID + " = ? AND " + Website.MIMETYPE + " = ?",
                String.valueOf(contactId), Website.CONTENT_ITEM_TYPE)) {
            adapter.map(Website.URL, String.class, RDFAndroidContactInfo::setWebsite);
            if (adapter.moveToNext()) {
                adapter.fill(info);
            }
        }
        try (ContactInfoAdapter adapter = new ContactInfoAdapter(resolver, Data.CONTENT_URI,
                Note.CONTACT_ID + " = ? AND " + Note.MIMETYPE + " = ?", String.valueOf(contactId),
                Note.CONTENT_ITEM_TYPE)) {
            adapter.map(Note.NOTE, String.class, RDFAndroidContactInfo::setNote);
            if (adapter.moveToNext()) {
                adapter.fill(info);
            }
        }
        try (ContactInfoAdapter adapter = new ContactInfoAdapter(resolver, Data.CONTENT_URI,
                SipAddress.CONTACT_ID + " = ? AND " + SipAddress.MIMETYPE + " = ?",
                String.valueOf(contactId),
                SipAddress.CONTENT_ITEM_TYPE)) {
            adapter.map(SipAddress.SIP_ADDRESS, String.class, RDFAndroidContactInfo::setSip);
            if (adapter.moveToNext()) {
                adapter.fill(info);
            }
        }
        info.setPhoneNumbers(createPhoneNumbers(resolver, contactId));
        info.setEmailAddresses(createEmailAddresses(resolver, contactId));
        info.setPostalAddresses(createPostalAddresses(resolver, contactId));
        info.setIms(createIMs(resolver, contactId));
        return info;
    }

    @Override
    public void execute(Context context, RDFNull request, ActionCallback<RDFAndroidContactInfo> callback) {
        ContentResolver resolver = context.getContentResolver();
        for (Integer id : getContactIds(resolver)) {
            callback.onResponse(createContactInfo(resolver, id));
        }
        callback.onComplete();
    }

    private class ContactInfoAdapter extends ContentAdapter<RDFAndroidContactInfo> {
        ContactInfoAdapter(ContentResolver resolver, Uri uri, String selection,
                String... selectionArgs) {
            super(resolver, uri, selection, selectionArgs);
        }
    }
}
