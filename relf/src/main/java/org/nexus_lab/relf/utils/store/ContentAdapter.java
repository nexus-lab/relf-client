package org.nexus_lab.relf.utils.store;

import static org.nexus_lab.relf.utils.ReflectionUtils.Parameter.from;
import static org.nexus_lab.relf.utils.ReflectionUtils.callStaticMethod;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;

import com.google.protobuf.Internal;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fetch columns from content provider and fill the column value to a Java object.
 *
 * @param <T> type of the object to be filled with column values
 * @author Ruipeng Zhang
 */
@SuppressWarnings("unchecked")
public class ContentAdapter<T> implements Closeable {
    private Cursor cursor;
    private List<String> columns = new ArrayList<>();
    private Map<String, Class> types = new HashMap<>();
    private Map<String, ColumnMapper> mappers = new HashMap<>();

    /**
     * @param cursor content provider database cursor
     */
    public ContentAdapter(Cursor cursor) {
        this.cursor = cursor;
    }

    /**
     * @param resolver {@link android.content.Context#getContentResolver()}
     * @param uri      URI to the content data
     * @see ContentResolver#query(Uri, String[], String, String[], String)
     */
    public ContentAdapter(ContentResolver resolver, Uri uri) {
        this(resolver, uri, null);
    }

    /**
     * @param resolver      {@link android.content.Context#getContentResolver()}
     * @param uri           URI to the content data
     * @param selection     record filtering rules
     * @param selectionArgs record filtering parameters
     * @see ContentResolver#query(Uri, String[], String, String[], String)
     */
    public ContentAdapter(ContentResolver resolver, Uri uri, String selection,
            String... selectionArgs) {
        this.cursor = resolver.query(uri, columns.toArray(new String[0]), selection, selectionArgs,
                null);
    }

    /**
     * @param resolver      {@link android.content.Context#getContentResolver()}
     * @param uri           URI to the content data
     * @param selection     record filtering rules
     * @param selectionArgs record filtering parameters
     * @param sortOrder     the order results to be sorted
     * @see ContentResolver#query(Uri, String[], String, String[], String)
     */
    public ContentAdapter(ContentResolver resolver, Uri uri, String selection,
            String[] selectionArgs, String sortOrder) {
        this.cursor = resolver.query(uri, columns.toArray(new String[0]), selection, selectionArgs,
                sortOrder);
    }

    private void fillColumn(T target, String column) {
        int columnIndex = cursor.getColumnIndex(column);
        Class type = types.get(column);
        Object value = null;
        if (int.class.equals(type) || Integer.class.equals(type)) {
            value = cursor.getInt(columnIndex);
        } else if (String.class.equals(type)) {
            value = cursor.getString(columnIndex);
        } else if (long.class.equals(type) || Long.class.equals(type)) {
            value = cursor.getLong(columnIndex);
        } else if (short.class.equals(type) || Short.class.equals(type)) {
            value = cursor.getShort(columnIndex);
        } else if (float.class.equals(type) || Float.class.equals(type)) {
            value = cursor.getFloat(columnIndex);
        } else if (double.class.equals(type) || Double.class.equals(type)) {
            value = cursor.getDouble(columnIndex);
        } else if (boolean.class.equals(type) || Boolean.class.equals(type)) {
            value = cursor.getInt(columnIndex) != 0;
        } else if (type != null && Internal.EnumLite.class.isAssignableFrom(type)) {
            value = cursor.getInt(columnIndex);
            try {
                value = callStaticMethod(type, "forNumber", from(int.class, value));
            } catch (ReflectiveOperationException e) {
                value = null;
            }
        }
        if (value != null) {
            ColumnMapper mapper = mappers.get(column);
            if (mapper != null) {
                mapper.map(target, value);
            }
        }
    }

    /**
     * Set up a new column value to object field value mapping.
     *
     * @param column content database column name
     * @param type   column value type
     * @param mapper column value to object field mapping function
     * @param <V>    type of the column value
     */
    public <V> void map(String column, Class<V> type, ColumnMapper<T, V> mapper) {
        columns.add(column);
        types.put(column, type);
        mappers.put(column, mapper);
    }

    /**
     * @return true if cursor is not empty and still have next record
     */
    public boolean moveToNext() {
        return cursor != null && cursor.moveToNext();
    }

    /**
     * Set the target object field values to the content database column values.
     *
     * @param target target object
     */
    public void fill(T target) {
        for (String column : columns) {
            fillColumn(target, column);
        }
    }

    @Override
    public void close() {
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
    }


    /**
     * A function to set the object field value.
     *
     * @param <T> type of the object
     * @param <V> type of the column value
     */
    @FunctionalInterface
    public interface ColumnMapper<T, V> {
        /**
         * @param target the target object
         * @param value  the column value
         */
        void map(T target, V value);
    }
}