package org.nexus_lab.relf.client.actions.android;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.os.LocaleList;
import android.provider.Settings;

import org.nexus_lab.relf.client.actions.Action;
import org.nexus_lab.relf.client.actions.ActionCallback;
import org.nexus_lab.relf.lib.rdfvalues.RDFNull;
import org.nexus_lab.relf.lib.rdfvalues.android.RDFAndroidSystemSettings;
import org.nexus_lab.relf.lib.rdfvalues.protodict.RDFDict;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

/**
 * @author Ruipeng Zhang
 */
public class GetAndroidSystemSettings implements Action<RDFNull, RDFAndroidSystemSettings> {
    private ContentResolver resolver;

    @SuppressWarnings("unchecked")
    private RDFDict getSettingValues(Class clazz) {
        RDFDict dict = new RDFDict();
        for (Field field : clazz.getDeclaredFields()) {
            int modifiers = field.getModifiers();
            if (field.getType().isAssignableFrom(String.class)
                    && Modifier.isPublic(modifiers)
                    && Modifier.isStatic(modifiers)) {
                for (String mn : new String[]{"getString", "getLong", "getInt", "getFloat"}) {
                    try {
                        Method m = clazz.getDeclaredMethod(mn, ContentResolver.class, String.class);
                        String name = (String) field.get(null);
                        Object value = m.invoke(null, resolver, name);
                        if (value != null) {
                            dict.put(name, value);
                            break;
                        }
                    } catch (ReflectiveOperationException | SecurityException ignored) {
                    }
                }
            }
        }
        return dict;
    }

    private void setSystemLocales(RDFAndroidSystemSettings settings) {
        RDFDict system = settings.getSystem();
        List<String> locales = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            LocaleList localeList = Resources.getSystem().getConfiguration().getLocales();
            for (int i = 0; i < localeList.size(); i++) {
                locales.add(localeList.get(i).getDisplayName());
            }
        } else {
            locales.add(Resources.getSystem().getConfiguration().locale.getDisplayName());
        }
        system.put("system_locales", locales);
    }

    private void setSystemTimezone(RDFAndroidSystemSettings settings) {
        RDFDict system = settings.getSystem();
        TimeZone tz = TimeZone.getDefault();
        system.put("timezone", tz.getID());
    }

    @Override
    public void execute(Context context, RDFNull request, ActionCallback<RDFAndroidSystemSettings> callback) {
        resolver = context.getContentResolver();
        RDFAndroidSystemSettings settings = new RDFAndroidSystemSettings();
        settings.setSystem(getSettingValues(Settings.System.class));
        settings.setGlobal(getSettingValues(Settings.Global.class));
        settings.setSecure(getSettingValues(Settings.Secure.class));
        setSystemLocales(settings);
        setSystemTimezone(settings);
        callback.onResponse(settings);
        callback.onComplete();
    }
}
