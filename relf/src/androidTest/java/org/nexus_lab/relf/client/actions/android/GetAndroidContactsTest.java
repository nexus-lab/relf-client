package org.nexus_lab.relf.client.actions.android;

import static android.provider.ContactsContract.AUTHORITY;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.Manifest;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.BaseTypes;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Im;
import android.provider.ContactsContract.CommonDataKinds.Nickname;
import android.provider.ContactsContract.CommonDataKinds.Note;
import android.provider.ContactsContract.CommonDataKinds.Organization;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.SipAddress;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;
import android.provider.ContactsContract.CommonDataKinds.Website;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

import com.github.javafaker.Faker;
import org.nexus_lab.relf.client.actions.ActionCallback;
import org.nexus_lab.relf.client.exceptions.TestSuccessException;
import org.nexus_lab.relf.lib.rdfvalues.android.RDFAndroidContactInfo;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;


@RunWith(AndroidJUnit4.class)
public class GetAndroidContactsTest {
    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS
    );
    private Context context;
    private List<ContactBuilder> contacts = new ArrayList<>();
    private ActionCallback<RDFAndroidContactInfo> callback =
            new ActionCallback<RDFAndroidContactInfo>() {
                private int i;

                @Override
                public void onResponse(RDFAndroidContactInfo info) {
                    ContactBuilder contact = contacts.get(i);
                    String fullname = contact.name.getValue(StructuredName.PREFIX) + " " +
                            contact.name.getValue(StructuredName.GIVEN_NAME) + " " +
                            contact.name.getValue(StructuredName.MIDDLE_NAME) + " " +
                            contact.name.getValue(StructuredName.FAMILY_NAME) + ", " +
                            contact.name.getValue(StructuredName.SUFFIX);
                    assertEquals(fullname, info.getName());
                    String phoneticName = contact.name.getValue(
                            StructuredName.PHONETIC_FAMILY_NAME)
                            + " " + contact.name.getValue(StructuredName.PHONETIC_MIDDLE_NAME)
                            + " " +
                            contact.name.getValue(StructuredName.PHONETIC_GIVEN_NAME);
                    assertEquals(phoneticName, info.getPhoneticName());
                    assertEquals(contact.nickname.getValue(Nickname.NAME), info.getNickname());
                    assertEquals(contact.company.getValue(Organization.COMPANY),
                            info.getCompany());
                    assertEquals(contact.company.getValue(Organization.TITLE), info.getTitle());
                    assertEquals(contact.website.getValue(Website.URL), info.getWebsite());
                    assertEquals(contact.note.getValue(Note.NOTE), info.getNote());
                    assertEquals(contact.sip.getValue(SipAddress.SIP_ADDRESS), info.getSip());
                    assertEquals(contact.phoneNumbers.size(), info.getPhoneNumbers().size());
                    for (int j = 0; j < contact.phoneNumbers.size(); j++) {
                        ContactInsertOperation phoneNumber = contact.phoneNumbers.get(j);
                        assertEquals(phoneNumber.getValue(Phone.NUMBER),
                                info.getPhoneNumbers().get(j).getNumber());
                        assertEquals(phoneNumber.getValue(Phone.TYPE),
                                info.getPhoneNumbers().get(j)
                                        .getType().getNumber());
                        if (phoneNumber.getValue(Phone.LABEL) != null) {
                            assertEquals(phoneNumber.getValue(Phone.LABEL),
                                    info.getPhoneNumbers().get(j).getLabel());
                        } else {
                            assertEquals("", info.getPhoneNumbers().get(j).getLabel());
                        }
                    }
                    assertEquals(contact.emails.size(), info.getEmailAddresses().size());
                    for (int j = 0; j < contact.emails.size(); j++) {
                        ContactInsertOperation email = contact.emails.get(j);
                        assertEquals(email.getValue(Email.ADDRESS),
                                info.getEmailAddresses().get(j).getAddress());
                        assertEquals(email.getValue(Email.TYPE), info.getEmailAddresses().get(
                                j).getType().getNumber());
                        if (email.getValue(Email.LABEL) != null) {
                            assertEquals(email.getValue(Email.LABEL),
                                    info.getEmailAddresses().get(j).getLabel());
                        } else {
                            assertEquals("", info.getEmailAddresses().get(j).getLabel());
                        }
                    }
                    assertEquals(contact.postalAddresses.size(),
                            info.getPostalAddresses().size());
                    for (int j = 0; j < contact.postalAddresses.size(); j++) {
                        ContactInsertOperation postalAddress = contact.postalAddresses.get(j);
                        String address = info.getPostalAddresses().get(j).getAddress();
                        assertTrue(address.contains(
                                (String) postalAddress.getValue(StructuredPostal.COUNTRY)));
                        assertTrue(address.contains(
                                (String) postalAddress.getValue(StructuredPostal.REGION)));
                        assertTrue(address.contains(
                                (String) postalAddress.getValue(StructuredPostal.CITY)));
                        assertTrue(address.contains(
                                (String) postalAddress.getValue(StructuredPostal.POSTCODE)));
                        assertTrue(address.contains(
                                (String) postalAddress.getValue(StructuredPostal.STREET)));
                        assertEquals(postalAddress.getValue(StructuredPostal.TYPE),
                                info.getPostalAddresses().get(j).getType().getNumber());
                        if (postalAddress.getValue(StructuredPostal.LABEL) != null) {
                            assertEquals(postalAddress.getValue(StructuredPostal.LABEL),
                                    info.getPostalAddresses().get(j).getLabel());
                        } else {
                            assertEquals("", info.getPostalAddresses().get(j).getLabel());
                        }
                    }
                    assertEquals(contact.ims.size(), info.getIms().size());
                    for (int j = 0; j < contact.ims.size(); j++) {
                        ContactInsertOperation im = contact.ims.get(j);
                        assertEquals(im.getValue(Im.DATA), info.getIms().get(j).getAccount());
                        assertEquals(im.getValue(Im.TYPE), info.getIms().get(
                                j).getType().getNumber());
                        assertEquals(im.getValue(Im.PROTOCOL), info.getIms().get(j)
                                .getProtocol().getNumber());
                        if (im.getValue(Im.CUSTOM_PROTOCOL) != null) {
                            assertEquals(im.getValue(Im.CUSTOM_PROTOCOL),
                                    info.getIms().get(j).getCustomProtocol());
                        } else {
                            assertEquals("", info.getIms().get(j).getCustomProtocol());
                        }
                        if (im.getValue(Im.LABEL) != null) {
                            assertEquals(im.getValue(Im.LABEL),
                                    info.getIms().get(j).getLabel());
                        } else {
                            assertEquals("", info.getIms().get(j).getLabel());
                        }
                    }
                    i++;
                }

                @Override
                public void onComplete() {
                    assertEquals(contacts.size(), i);
                    throw new TestSuccessException();
                }
            };

    private void createContact() throws Exception {
        Faker faker = new Faker();
        Random random = new Random();
        ContactBuilder contact = new ContactBuilder();
        contact.name.with(StructuredName.GIVEN_NAME, faker.name().firstName())
                .with(StructuredName.FAMILY_NAME, faker.name().lastName())
                .with(StructuredName.MIDDLE_NAME, faker.name().lastName())
                .with(StructuredName.PREFIX, faker.name().prefix())
                .with(StructuredName.SUFFIX, faker.name().suffix())
                .with(StructuredName.PHONETIC_GIVEN_NAME, faker.name().firstName())
                .with(StructuredName.PHONETIC_FAMILY_NAME, faker.name().lastName())
                .with(StructuredName.PHONETIC_MIDDLE_NAME, faker.name().lastName());
        contact.nickname.with(Nickname.NAME, faker.name().firstName());
        contact.company.with(Organization.COMPANY, faker.company().name())
                .with(Organization.TITLE, faker.name().title());
        contact.website.with(Website.URL, faker.internet().url());
        contact.note.with(Note.NOTE, faker.lorem().sentence());
        contact.sip.with(SipAddress.SIP_ADDRESS, faker.phoneNumber().cellPhone());
        for (int i = 0; i < random.nextInt(5) + 1; i++) {
            int type = i > 0 ? random.nextInt(Phone.TYPE_MOBILE) + 1 : BaseTypes.TYPE_CUSTOM;
            contact.phoneNumber()
                    .with(Phone.NUMBER, faker.phoneNumber().cellPhone())
                    .with(Phone.TYPE, type)
                    .with(Phone.LABEL, type == BaseTypes.TYPE_CUSTOM ? faker.dog().name() : null);
        }
        for (int i = 0; i < random.nextInt(5) + 1; i++) {
            int type = i > 0 ? random.nextInt(Email.TYPE_MOBILE) + 1 : BaseTypes.TYPE_CUSTOM;
            contact.email()
                    .with(Email.ADDRESS, faker.internet().emailAddress())
                    .with(Email.TYPE, type)
                    .with(Email.LABEL, type == BaseTypes.TYPE_CUSTOM ? faker.dog().name() : null);
        }
        for (int i = 0; i < random.nextInt(5) + 1; i++) {
            int type = i > 0 ? random.nextInt(StructuredPostal.TYPE_OTHER) + 1
                    : BaseTypes.TYPE_CUSTOM;
            contact.postalAddress()
                    .with(StructuredPostal.CITY, faker.address().city())
                    .with(StructuredPostal.COUNTRY, faker.address().country())
                    .with(StructuredPostal.POSTCODE, faker.address().zipCode())
                    .with(StructuredPostal.STREET, faker.address().streetName())
                    .with(StructuredPostal.REGION, faker.address().stateAbbr())
                    .with(StructuredPostal.TYPE, type)
                    .with(StructuredPostal.LABEL,
                            type == BaseTypes.TYPE_CUSTOM ? faker.dog().name() : null);
        }
        for (int i = 0; i < random.nextInt(5) + 1; i++) {
            int protocol = i > 0 ? random.nextInt(Im.PROTOCOL_NETMEETING + 1) : Im.PROTOCOL_CUSTOM;
            int type = i > 0 ? random.nextInt(Im.TYPE_OTHER) + 1 : BaseTypes.TYPE_CUSTOM;
            contact.im()
                    .with(Im.DATA, faker.funnyName().name())
                    .with(Im.PROTOCOL, protocol)
                    .with(Im.CUSTOM_PROTOCOL, protocol == -1 ? faker.internet().domainName() : null)
                    .with(Im.TYPE, type)
                    .with(Im.LABEL, type == BaseTypes.TYPE_CUSTOM ? faker.dog().name() : null);
        }
        contacts.add(contact);
        context.getContentResolver().applyBatch(AUTHORITY, contact.build());
    }

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(Contacts.CONTENT_URI, null, null, null, null);
        while (cursor != null && cursor.moveToNext()) {
            String lookupKey = cursor.getString(cursor.getColumnIndex(Contacts.LOOKUP_KEY));
            Uri uri = Uri.withAppendedPath(Contacts.CONTENT_LOOKUP_URI, lookupKey);
            resolver.delete(uri, null, null);
        }
        if (cursor != null) {
            cursor.close();
        }
    }

    @Test(expected = TestSuccessException.class)
    public void execute() throws Exception {
        for (int i = 0; i < 3; i++) {
            createContact();
        }
        new GetAndroidContacts().execute(context, null, callback);
    }

    private class ContactInsertOperation {
        private final ContentProviderOperation.Builder builder;
        private final Map<String, Object> values = new HashMap<>();

        ContactInsertOperation(String mimetype) {
            builder = ContentProviderOperation.newInsert(Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE, mimetype);
        }

        ContactInsertOperation with(String key, Object value) {
            builder.withValue(key, value);
            values.put(key, value);
            return this;
        }

        ContentProviderOperation build() {
            return builder.build();
        }

        Object getValue(String key) {
            return values.get(key);
        }
    }

    private class ContactBuilder {
        ContactInsertOperation name = new ContactInsertOperation(StructuredName.CONTENT_ITEM_TYPE);
        ContactInsertOperation nickname = new ContactInsertOperation(Nickname.CONTENT_ITEM_TYPE);
        ContactInsertOperation company = new ContactInsertOperation(Organization.CONTENT_ITEM_TYPE);
        ContactInsertOperation website = new ContactInsertOperation(Website.CONTENT_ITEM_TYPE);
        ContactInsertOperation note = new ContactInsertOperation(Note.CONTENT_ITEM_TYPE);
        ContactInsertOperation sip = new ContactInsertOperation(SipAddress.CONTENT_ITEM_TYPE);
        List<ContactInsertOperation> phoneNumbers = new ArrayList<>();
        List<ContactInsertOperation> emails = new ArrayList<>();
        List<ContactInsertOperation> postalAddresses = new ArrayList<>();
        List<ContactInsertOperation> ims = new ArrayList<>();

        ContactInsertOperation phoneNumber() {
            ContactInsertOperation operation = new ContactInsertOperation(Phone.CONTENT_ITEM_TYPE);
            phoneNumbers.add(operation);
            return operation;
        }

        ContactInsertOperation email() {
            ContactInsertOperation operation = new ContactInsertOperation(Email.CONTENT_ITEM_TYPE);
            emails.add(operation);
            return operation;
        }

        ContactInsertOperation postalAddress() {
            ContactInsertOperation operation = new ContactInsertOperation(
                    StructuredPostal.CONTENT_ITEM_TYPE);
            postalAddresses.add(operation);
            return operation;
        }

        ContactInsertOperation im() {
            ContactInsertOperation operation = new ContactInsertOperation(Im.CONTENT_ITEM_TYPE);
            ims.add(operation);
            return operation;
        }

        ArrayList<ContentProviderOperation> build() {
            ArrayList<ContentProviderOperation> result = new ArrayList<>();
            result.add(ContentProviderOperation.newInsert(RawContacts.CONTENT_URI)
                    .withValue(RawContacts.ACCOUNT_TYPE, null)
                    .withValue(RawContacts.ACCOUNT_NAME, null)
                    .build());
            result.add(name.build());
            result.add(nickname.build());
            result.add(company.build());
            result.add(website.build());
            result.add(note.build());
            result.add(sip.build());
            for (ContactInsertOperation phoneNumber : phoneNumbers) {
                result.add(phoneNumber.build());
            }
            for (ContactInsertOperation email : emails) {
                result.add(email.build());
            }
            for (ContactInsertOperation postalAddress : postalAddresses) {
                result.add(postalAddress.build());
            }
            for (ContactInsertOperation im : ims) {
                result.add(im.build());
            }
            return result;
        }
    }
}