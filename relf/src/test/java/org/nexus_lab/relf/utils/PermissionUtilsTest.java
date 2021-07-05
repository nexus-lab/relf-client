package org.nexus_lab.relf.utils;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import android.app.Application;
import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import org.nexus_lab.relf.client.actions.Action;
import org.nexus_lab.relf.client.actions.ActionCallback;
import org.nexus_lab.relf.client.actions.UsesPermission;
import org.nexus_lab.relf.lib.rdfvalues.RDFValue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.shadows.ShadowApplication;

/**
 * @author Ruipeng Zhang
 */
@RunWith(RobolectricTestRunner.class)
public class PermissionUtilsTest {
    private Context context;

    @Before
    public void setup() {
        context = ApplicationProvider.getApplicationContext();
        ShadowApplication shadowApplication = Shadows.shadowOf((Application) context);
        shadowApplication.grantPermissions(READ_EXTERNAL_STORAGE, ACCESS_COARSE_LOCATION);
    }

    @Test
    public void checkActionPermission() {
        String[][] missingPermissions = PermissionUtils.checkPermissions(context, DummyAction.class);
        assertNotNull(missingPermissions);
        assertEquals(1, missingPermissions.length);
        assertArrayEquals(new String[]{"android.permission.MANAGE_USERS"}, missingPermissions[0]);
    }

    @UsesPermission(
            allOf = {READ_EXTERNAL_STORAGE, "android.permission.MANAGE_USERS"},
            anyOf = {ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION}
    )
    public class DummyAction implements Action {
        @Override
        public void execute(Context context, RDFValue request, ActionCallback callback) {
            callback.onComplete();
        }
    }
}
