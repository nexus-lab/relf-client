package org.nexus_lab.relf.utils;

import androidx.annotation.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * An utility for reflective operations
 *
 * @author Ruipeng Zhang
 */
public final class ReflectionUtils {
    private ReflectionUtils() {
    }

    /**
     * Convert a boxed type to its primitive type
     *
     * @param type Boxed type.
     * @return Primitive type.
     */
    public static Class unboxType(Class type) {
        if (Boolean.class.equals(type)) {
            return boolean.class;
        } else if (Byte.class.equals(type)) {
            return byte.class;
        } else if (Character.class.equals(type)) {
            return char.class;
        } else if (Short.class.equals(type)) {
            return short.class;
        } else if (Integer.class.equals(type)) {
            return int.class;
        } else if (Long.class.equals(type)) {
            return long.class;
        } else if (Float.class.equals(type)) {
            return float.class;
        } else if (Double.class.equals(type)) {
            return double.class;
        }
        return type;
    }

    /**
     * Convert a primitive type to its boxed type
     *
     * @param type Primitive type.
     * @return Boxed type.
     */
    public static Class boxType(Class type) {
        if (boolean.class.equals(type)) {
            return Boolean.class;
        } else if (byte.class.equals(type)) {
            return Byte.class;
        } else if (char.class.equals(type)) {
            return Character.class;
        } else if (short.class.equals(type)) {
            return Short.class;
        } else if (int.class.equals(type)) {
            return Integer.class;
        } else if (long.class.equals(type)) {
            return Long.class;
        } else if (float.class.equals(type)) {
            return Float.class;
        } else if (double.class.equals(type)) {
            return Double.class;
        }
        return type;
    }

    private static Class[] extractParameterClass(Parameter[] parameters) {
        if (parameters == null) {
            return new Class[0];
        }
        Class[] parameterTypes = new Class[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            parameterTypes[i] = parameters[i].clazz;
        }
        return parameterTypes;
    }

    private static Object[] extractParameterValue(Parameter[] parameters) {
        if (parameters == null) {
            return new Class[0];
        }
        Object[] values = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            values[i] = parameters[i].value;
        }
        return values;
    }

    /**
     * Call the constructor of a Java class.
     *
     * @param clazz      target class.
     * @param parameters array of parameter types and values.
     * @param <T>        The return type.
     * @return The return value of the method.
     * @throws ReflectiveOperationException when failed to call the constructor method.
     */
    public static <T> T callConstructor(Class<? extends T> clazz, Parameter... parameters)
            throws ReflectiveOperationException {
        Class[] parameterTypes = extractParameterClass(parameters);
        Object[] values = extractParameterValue(parameters);
        Constructor<? extends T> constructor = clazz.getDeclaredConstructor(parameterTypes);
        constructor.setAccessible(true);
        return constructor.newInstance(values);
    }

    /**
     * Get field value from a Java value.
     *
     * @param target    field owner object.
     * @param fieldName field name.
     * @param <T>       field value type.
     * @return field value.
     * @throws ReflectiveOperationException when field does not exist.
     */
    @SuppressWarnings("unchecked")
    public static <T> T getFieldValue(Object target, String fieldName)
            throws ReflectiveOperationException {
        return (T) getFieldValue(target.getClass(), target, fieldName);
    }

    /**
     * Get value of a static field from a Java class.
     *
     * @param targetClass field owner class.
     * @param fieldName   field name.
     * @param <T>         field value type.
     * @return field value.
     * @throws ReflectiveOperationException when field does not exist.
     */
    @SuppressWarnings("unchecked")
    public static <T> T getStaticFieldValue(Class targetClass, String fieldName)
            throws ReflectiveOperationException {
        return (T) getFieldValue(targetClass, null, fieldName);
    }

    /**
     * Get field value from a Java value.
     *
     * @param targetClass field owner class.
     * @param target      field owner object.
     * @param fieldName   field name.
     * @param <T>         field value type.
     * @return field value.
     * @throws ReflectiveOperationException when field does not exist or the target do not has this
     *                                      field.
     */
    @SuppressWarnings("unchecked")
    public static <T> T getFieldValue(Class targetClass, Object target, String fieldName)
            throws ReflectiveOperationException {
        Field field;
        try {
            field = targetClass.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            field = targetClass.getField(fieldName);
        }
        field.setAccessible(true);
        return (T) field.get(target);
    }

    /**
     * Get field value from a Java value.
     *
     * @param target    field owner object.
     * @param fieldName field name.
     * @param value     field value.
     * @throws ReflectiveOperationException when field does not exist.
     */
    public static void setFieldValue(Object target, String fieldName, Object value)
            throws ReflectiveOperationException {
        setFieldValue(target.getClass(), target, fieldName, value);
    }

    /**
     * Get field value from a Java value.
     *
     * @param targetClass field owner class.
     * @param target      field owner object.
     * @param fieldName   field name.
     * @param value       field value.
     * @throws ReflectiveOperationException when field does not exist or the target do not has this
     *                                      field.
     */
    public static void setFieldValue(Class targetClass, Object target, String fieldName,
            Object value) throws ReflectiveOperationException {
        Field field = targetClass.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    /**
     * Invoke a method on a Java object.
     *
     * @param target     method invocation target.
     * @param method     method name.
     * @param parameters method parameter types and values.
     * @param <T>        return value type.
     * @return method return value
     * @throws ReflectiveOperationException when method does not exists or parameters are illegal
     */
    @SuppressWarnings("unchecked")
    public static <T> T callInstanceMethod(Object target, String method,
            Parameter... parameters) throws ReflectiveOperationException {
        return (T) callInstanceMethod(target.getClass(), target, method, parameters);
    }

    /**
     * Invoke a static method on a Java class.
     *
     * @param targetClass type of the invocation target.
     * @param method      method name.
     * @param parameters  method parameter types and values.
     * @param <T>         return value type.
     * @return method return value
     * @throws ReflectiveOperationException when method does not exists or parameters are illegal
     */
    @SuppressWarnings("unchecked")
    public static <T> T callStaticMethod(Class targetClass, String method,
            Parameter... parameters) throws ReflectiveOperationException {
        return (T) callInstanceMethod(targetClass, null, method, parameters);
    }

    /**
     * Invoke a method on a Java object.
     *
     * @param targetClass type of the invocation target.
     * @param target      method invocation target.
     * @param method      method name.
     * @param parameters  method parameter types and values.
     * @param <T>         return value type.
     * @return method return value
     * @throws ReflectiveOperationException when method does not exists or parameters are illegal
     */
    @SuppressWarnings("unchecked")
    public static <T> T callInstanceMethod(Class targetClass, Object target, String method,
            Parameter... parameters) throws ReflectiveOperationException {
        Class[] parameterTypes = extractParameterClass(parameters);
        Object[] values = extractParameterValue(parameters);
        Method targetMethod;
        try {
            targetMethod = targetClass.getDeclaredMethod(method, parameterTypes);
        } catch (NoSuchMethodException e) {
            targetMethod = targetClass.getMethod(method, parameterTypes);
        }
        targetMethod.setAccessible(true);
        return (T) targetMethod.invoke(target, values);
    }

    /**
     * Get the generic types of the interface that the given class implements.
     *
     * @param clazz    the class that implements the generic interface(s).
     * @param interfaz the interface where we get the generic types from.
     * @return the generic types.
     */
    @Nullable
    public static Type[] getGenericTypes(Class clazz, Class<?> interfaz) {
        for (Type type : clazz.getGenericInterfaces()) {
            if (!(type instanceof ParameterizedType)) {
                continue;
            }
            ParameterizedType parameterType = (ParameterizedType) type;
            if (interfaz.isAssignableFrom((Class<?>) parameterType.getRawType())) {
                return parameterType.getActualTypeArguments();
            }
        }
        return null;
    }

    /**
     * Wrapper class of any parameter and its value
     */
    public static class Parameter {
        private final Class clazz;
        private final Object value;

        /**
         * @param clazz parameter type
         * @param value parameter value
         */
        public Parameter(Class clazz, Object value) {
            this.clazz = clazz;
            this.value = value;
        }

        /**
         * A helper function for creating {@link Parameter}
         *
         * @param clazz parameter type
         * @param value parameter value
         * @return {@link Parameter}
         */
        public static Parameter from(Class clazz, Object value) {
            return new Parameter(clazz, value);
        }
    }
}
