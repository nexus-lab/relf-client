package org.nexus_lab.relf.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import java.lang.reflect.Type;

/**
 * @author Ruipeng Zhang
 */
public class ReflectionUtilsTest {
    private static final Class[] BOXED = new Class[]{
            Boolean.class,
            Byte.class,
            Character.class,
            Short.class,
            Integer.class,
            Long.class,
            Float.class,
            Double.class
    };
    private static final Class[] UNBOXED = new Class[]{
            boolean.class,
            byte.class,
            char.class,
            short.class,
            int.class,
            long.class,
            float.class,
            double.class
    };

    @Test
    public void unboxType() {
        for (int i = 0; i < BOXED.length; i++) {
            assertEquals(UNBOXED[i], ReflectionUtils.unboxType(BOXED[i]));
        }
    }

    @Test
    public void boxType() {
        for (int i = 0; i < UNBOXED.length; i++) {
            assertEquals(BOXED[i], ReflectionUtils.boxType(UNBOXED[i]));
        }
    }

    @Test
    public void callConstructor() throws ReflectiveOperationException {
        PrivateClass object = ReflectionUtils.callConstructor(
                PrivateClass.class,
                ReflectionUtils.Parameter.from(int.class, Integer.MAX_VALUE));
        assertEquals(Integer.MAX_VALUE, object.value);
    }

    @Test
    public void getFieldValue() throws ReflectiveOperationException {
        PrivateClass object = new PrivateClass(Integer.MAX_VALUE);
        int value = ReflectionUtils.getFieldValue(object, "value");
        assertEquals(Integer.MAX_VALUE, value);
    }

    @Test
    public void getFieldValueWithClass() throws ReflectiveOperationException {
        InheritClass object = new InheritClass(Integer.MAX_VALUE);
        int value = ReflectionUtils.getFieldValue(PrivateClass.class, object, "value");
        assertEquals(Integer.MAX_VALUE, value);
    }

    @Test
    public void getStaticFieldValue() throws ReflectiveOperationException {
        int value = ReflectionUtils.getStaticFieldValue(PrivateClass.class, "staticValue");
        assertEquals(Integer.MIN_VALUE, value);
    }

    @Test
    public void setFieldValue() throws ReflectiveOperationException {
        PrivateClass object = new PrivateClass(Integer.MAX_VALUE);
        ReflectionUtils.setFieldValue(object, "value", Integer.MIN_VALUE);
        assertEquals(Integer.MIN_VALUE, object.value);
    }

    @Test
    public void setFieldValueWithClass() throws ReflectiveOperationException {
        PrivateClass object = new InheritClass(Integer.MAX_VALUE);
        ReflectionUtils.setFieldValue(PrivateClass.class, object, "value", Integer.MIN_VALUE);
        assertEquals(Integer.MIN_VALUE, object.value);
    }

    @Test
    public void callInstanceMethod() throws ReflectiveOperationException {
        PrivateClass object = new PrivateClass(Integer.MAX_VALUE);
        int returnValue = ReflectionUtils.callInstanceMethod(object, "setValue",
                ReflectionUtils.Parameter.from(int.class, 0));
        assertEquals(-1, returnValue);
        assertEquals(0, object.value);
    }

    @Test
    public void callInstanceMethodWithClass() throws ReflectiveOperationException {
        PrivateClass object = new InheritClass(Integer.MAX_VALUE);
        int returnValue = ReflectionUtils.callInstanceMethod(PrivateClass.class, object, "setValue",
                ReflectionUtils.Parameter.from(int.class, 0));
        assertEquals(-1, returnValue);
        assertEquals(0, object.value);
    }

    @Test
    public void callStaticMethod() throws ReflectiveOperationException {
        int returnValue = ReflectionUtils.callStaticMethod(PrivateClass.class, "setStaticValue",
                ReflectionUtils.Parameter.from(int.class, 0));
        assertEquals(-1, returnValue);
        assertEquals(0, PrivateClass.staticValue);
    }

    @Test
    public void getGenericTypes() {
        Type[] types = ReflectionUtils.getGenericTypes(ParameterClass0.class, ParameterInterface0.class);
        assertNull(types);
        types = ReflectionUtils.getGenericTypes(ParameterClass1.class, ParameterInterface1.class);
        assertNotNull(types);
        assertEquals(1, types.length);
        assertEquals(ParameterClass0.class, types[0]);
        types = ReflectionUtils.getGenericTypes(ParameterClass2.class, ParameterInterface2.class);
        assertNotNull(types);
        assertEquals(2, types.length);
        assertEquals(ParameterClass0.class, types[0]);
        assertEquals(ParameterClass1.class, types[1]);
    }

    private interface ParameterInterface0 {
    }

    private interface ParameterInterface1<T> {
    }

    private interface ParameterInterface2<T, E> {
    }

    private static class PrivateClass {
        private static int staticValue = Integer.MIN_VALUE;
        private int value;

        private PrivateClass(int value) {
            this.value = value;
        }

        private static int setStaticValue(int value) {
            staticValue = value;
            return -1;
        }

        private int setValue(int value) {
            this.value = value;
            return -1;
        }
    }

    private static class InheritClass extends PrivateClass {
        private InheritClass(int value) {
            super(value);
        }
    }

    private static abstract class ParameterClass0 implements ParameterInterface0 {
    }

    private static abstract class ParameterClass1 implements ParameterInterface1<ParameterClass0> {
    }

    private static abstract class ParameterClass2 implements ParameterInterface2<ParameterClass0, ParameterClass1> {
    }
}