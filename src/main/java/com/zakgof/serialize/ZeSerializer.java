package com.zakgof.serialize;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.Map.Entry;

import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

import com.annimon.stream.Collectors;
import com.annimon.stream.IntStream;
import com.annimon.stream.Stream;
import com.annimon.stream.function.Consumer;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.zakgof.tools.io.*;


@SuppressWarnings("rawtypes")
public class ZeSerializer implements ISerializer {

    // public static final String COMPATIBLE_POJOS = "compatible.pojos";
    public static final String FORCED_HEADER = "forced.header";
    public static final String USE_OBJENESIS = "use.objenesis";
    private Map<String, ?> parameters;
    private Map<Wrap, Integer> knownObjects = new HashMap<>();
    private List<Object> knownObjectList = new ArrayList<>();
    private static final Objenesis objenesis = new ObjenesisStd();

    public ZeSerializer(Map<String, ?> parameters) {
        this.parameters = parameters;
    }

    public ZeSerializer() {
        this(new HashMap<>());
    }

    @Override
    public byte[] serialize(Object object) {
        // long start = System.currentTimeMillis();
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream(256);
            serialize(object, baos);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new ZeSerializerException(e);
        } finally {
            // long end = System.currentTimeMillis();
            // Log.w("perf", "serialize " + object.getClass().getSimpleName() +
            // " in "
            // + (end-start));
        }
    }

    public void serialize(Object object, OutputStream os) throws IOException {
        try {
            SimpleOutputStream sos = new SimpleOutputStream(os);
            boolean hoHeader = parameters.containsKey(FORCED_HEADER) ? false : true;
            fieldSerializer.write(object, object.getClass(), sos, hoHeader);
        } finally {
            knownObjectList.clear();
            knownObjects.clear();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T deserialize(InputStream is, Class<T> clazz) {
        // long start = System.currentTimeMillis();
        try {
            SimpleInputStream sis = new SimpleInputStream(is);
            boolean hoHeader = parameters.containsKey(FORCED_HEADER) ? false : true;
            return (T) fieldSerializer.read(sis, clazz, hoHeader);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            // long end = System.currentTimeMillis();
            // Log.w("perf", "deserialize " + clazz.getSimpleName() + " in " +
            // (end-start));
            knownObjectList.clear();
            knownObjects.clear();
        }
    }

    public static List<Field> getAllFields(Class<?> type) {
        List<Field> fields = new ArrayList<>();

        Field[] declaredFields = type.getDeclaredFields();
        Arrays.sort(declaredFields, new Comparator<Field>() {
            @Override
            public int compare(Field f1, Field f2) {
                return f1.getName().compareTo(f2.getName());
            }
        });
        for (Field field : declaredFields) {
            if ((field.getModifiers() & (Modifier.STATIC | Modifier.TRANSIENT)) == 0)
                fields.add(field);
        }
        if (type.getSuperclass() != null)
            fields.addAll(getAllFields(type.getSuperclass()));
        return fields;
    }

    private interface IFieldSerializer {
        void write(Object object, Class<?> clazz, SimpleOutputStream sos) throws IOException;

        Object read(SimpleInputStream sis, Class<?> clazz) throws IOException;
    }

    private interface ICompositeSerializer<T> {
        void write(T object, Class<? extends T> clazz, SimpleOutputStream sos, IFieldSerializer fieldSerializer) throws IOException;

        T read(SimpleInputStream sis, Class<? extends T> clazz, IFieldSerializer fieldSerializer, Consumer<Object> rememberer) throws IOException;
    }

    private final FieldSerializer fieldSerializer = new FieldSerializer();
    private final IFieldSerializer arraySerializer = new ArraySerializer();
    private final PojoSerializer pojoSerializer = new PojoSerializer();

    private final static Map<Class<?>, ISimpleSerializer<?>> serializers = new HashMap<>();
    private final static Map<Class<?>, ICompositeSerializer<?>> compositeSerializers = new HashMap<>();

    private final static BiMap<String, Byte> classes = HashBiMap.create();

    static {
        serializers.put(byte.class, SimpleByteSerializer.INSTANCE);
        serializers.put(short.class, SimpleShortSerializer.INSTANCE);
        serializers.put(int.class, SimpleIntegerSerializer.INSTANCE);
        serializers.put(long.class, SimpleLongSerializer.INSTANCE);
        serializers.put(float.class, SimpleFloatSerializer.INSTANCE);
        serializers.put(double.class, SimpleDoubleSerializer.INSTANCE);
        serializers.put(boolean.class, SimpleBooleanSerializer.INSTANCE);
        serializers.put(Byte.class, SimpleByteSerializer.INSTANCE);
        serializers.put(Short.class, SimpleShortSerializer.INSTANCE);
        serializers.put(Integer.class, SimpleIntegerSerializer.INSTANCE);
        serializers.put(Long.class, SimpleLongSerializer.INSTANCE);
        serializers.put(Float.class, SimpleFloatSerializer.INSTANCE);
        serializers.put(Double.class, SimpleDoubleSerializer.INSTANCE);
        serializers.put(Boolean.class, SimpleBooleanSerializer.INSTANCE);
        serializers.put(String.class, SimpleStringSerializer.INSTANCE);
        serializers.put(Date.class, SimpleDateSerializer.INSTANCE);
        serializers.put(byte[].class, SimpleByteArraySerializer.INSTANCE);
        serializers.put(Class.class, SimpleClassSerializer.INSTANCE);

        compositeSerializers.put(HashMap.class, new HashMapSerializer());
        compositeSerializers.put(ArrayList.class, new CollectionSerializer<ArrayList<?>>());
        compositeSerializers.put(HashSet.class, new CollectionSerializer<HashSet<?>>());

        classes.put("java.util.HashMap", (byte) 1);
        classes.put("java.util.ArrayList", (byte) 2);
        classes.put("java.util.RegularEnumSet", (byte) 3);
        classes.put("java.util.LinkedList", (byte) 4);
        classes.put("java.util.HashSet", (byte) 5);
        classes.put("java.util.TreeMap", (byte) 6);
        classes.put("java.util.TreeSet", (byte) 7);
    }

    private Object instantiate(Class<? extends Object> clazz, Object outer) throws Exception {
        Object instance = createObject(clazz, outer);
        if (!clazz.isPrimitive()) {
            rememberObject(instance);
        }
        return instance;
    }

    private void rememberObject(Object instance) {
        if (!knownObjects.containsKey(new Wrap(instance))) {
            knownObjectList.add(instance);
            knownObjects.put(new Wrap(instance), knownObjectList.size() - 1);
        }
    }

    private Object createObject(Class<? extends Object> clazz, Object outer) throws ReflectiveOperationException, SecurityException {
        if (objenesis != null)
            return objenesis.getInstantiatorOf(clazz).newInstance();
        return clazz.newInstance();
    }

    private static Object instantiateUsingNoArgCtor(Class<?> clazz) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Constructor<? extends Object> noArgConsructor = clazz.getDeclaredConstructor();
        noArgConsructor.setAccessible(true);
        return noArgConsructor.newInstance();
    }

    private static Class<?> parseClassName(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new ZeSerializerException("Cannot find class " + className);
        }
    }

    private class PojoSerializer {

        public void write(Object object, Class<? extends Object> clazz, SimpleOutputStream sos) throws IOException {
            try {
                for (Field field : getAllFields(clazz)) {
                    if ((field.getModifiers() & (Modifier.TRANSIENT | Modifier.STATIC)) == 0) {
                        field.setAccessible(true);
                        fieldSerializer.write(field.get(object), field.getType(), sos);
                    }
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        public Object read(SimpleInputStream sis, Class<? extends Object> clazz, Object outer) throws IOException {
            try {
                Object object = instantiate(clazz, outer);
                for (Field field : getAllFields(clazz)) {
                    if ((field.getModifiers() & (Modifier.TRANSIENT | Modifier.STATIC)) == 0) {
                        field.setAccessible(true);
                        Object value = fieldSerializer.read(sis, field.getType());
                        field.set(object, value);
                    }
                }
                return object;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private class ArraySerializer implements IFieldSerializer {

        @Override
        public void write(Object object, Class<?> clazz, SimpleOutputStream sos) throws IOException {
            int length = Array.getLength(object);
            sos.write(length);
            for (int i = 0; i < length; i++) {
                Object val = Array.get(object, i);
                fieldSerializer.write(val, clazz.getComponentType(), sos);
            }
        }

        @Override
        public Object read(SimpleInputStream sis, Class<?> clazz) throws IOException {
            int length = sis.readInt();
            if (length < 0)
                throw new RuntimeException("Invalid array length");
            Class<?> componentType = clazz.getComponentType();
            Object instance = Array.newInstance(componentType, length);
            rememberObject(instance);

            for (int i = 0; i < length; i++) {
                Object read = fieldSerializer.read(sis, componentType);
                Array.set(instance,  i, read);
            }
            return instance;
        }

    }

    private class FieldSerializer implements IFieldSerializer {

        @SuppressWarnings("unchecked")
        public void write(Object object, Class<?> clazz, SimpleOutputStream sos, boolean noHeader) throws IOException {
            if (!noHeader) {
                if (!clazz.isPrimitive())
                    if (!writeHeader(object, clazz, sos))
                        return;
            }

            Class<? extends Object> actualClazz = object.getClass();

//            if (object.getClass().getEnclosingClass() != null && (object.getClass().getModifiers() & Modifier.STATIC) == 0) {
//                Object outer = null;
//                try {
//                    Field outerField = object.getClass().getDeclaredField("this$0");
//                    outerField.setAccessible(true);
//                    outer = outerField.get(object);
//                    System.err.println("Write enclosing class " + clazz.getEnclosingClass() + " instance = " + outer);
//                    fieldSerializer.write(outer, clazz.getEnclosingClass(), sos);
//                    rememberObject(outer);
//                } catch (IllegalAccessException e) {
//                    throw new ZeSerializerException(e);
//                } catch (NoSuchFieldException e) {
//                    // No nothing
//                }
//            }

            if (!clazz.isPrimitive())
                rememberObject(object);

            if (clazz.isEnum()) {
                new SimpleEnumSerializer(clazz).write(sos, (Enum) object);
                return;
            }

            ICompositeSerializer<Object> compositeSerializer = (ICompositeSerializer<Object>) compositeSerializers.get(actualClazz);
            if (compositeSerializer != null) {
                compositeSerializer.write(object, actualClazz, sos, fieldSerializer);
                return;
            }
            ISimpleSerializer<Object> contentSerializer = (ISimpleSerializer<Object>) serializers.get(actualClazz);
            if (contentSerializer != null)
                contentSerializer.write(sos, object);
            else if (actualClazz.getComponentType() != null)
                arraySerializer.write(object, actualClazz, sos);
            else
                pojoSerializer.write(object, actualClazz, sos);
        }

        @Override
        public void write(Object object, Class<?> clazz, SimpleOutputStream sos) throws IOException {
            write(object, clazz, sos, false);
        }

        public Object read(SimpleInputStream sis, Class<?> clazz, boolean noHeader) throws IOException {
            Class<?> realClazz = clazz;
            if (!noHeader) {
                if (!clazz.isPrimitive()) {
                    // READ HEADER
                    byte hdr = sis.readByte();
                    if (hdr == 0)
                        return null;
                    else if (hdr == 3) {
                        int objid = sis.readInt();
                        Object val = knownObjectList.get(objid);
                        return val;
                    } else if (hdr == 1) {
                        // Nothing, realClazz = clazz
                    } else if (hdr == 4) {
                        byte index = sis.readByte();
                        String className = classes.inverse().get(index);
                        if (className == null)
                            throw new ZeSerializerException("Unknown class id: " + index);
                        realClazz = parseClassName(className);
                    } else if (hdr == 2) {
                        String className = sis.readString();
                        realClazz = parseClassName(className);
                    } else {
                        throw new ZeSerializerException("Parsing error: invalid header value: " + hdr
                                                        + " when parsing class " + clazz.getName());
                    }
                }
            }

//            Object outer = null;
//            if (realClazz.getEnclosingClass() != null && (realClazz.getModifiers() & Modifier.STATIC) == 0) {
//                outer = read(sis, realClazz.getDeclaringClass());
//                rememberObject(outer);
//            }

            Object object = readObject(sis, realClazz, null);
            if (!realClazz.isPrimitive()) {
                rememberObject(object);
            }
            return object;
        }

        @SuppressWarnings("unchecked")
        private Object readObject(SimpleInputStream sis, Class<?> realClazz, Object outer) throws IOException {
            if (realClazz.isEnum()) {
                return new SimpleEnumSerializer(realClazz).read(sis);
            }
            ICompositeSerializer<Object> compositeSerializer = (ICompositeSerializer<Object>) compositeSerializers.get(realClazz);
            if (compositeSerializer != null)
                return compositeSerializer.read(sis, realClazz, fieldSerializer, ZeSerializer.this::rememberObject);
            ISimpleSerializer<Object> contentSerializer = (ISimpleSerializer<Object>) serializers.get(realClazz);
            if (contentSerializer != null)
                return contentSerializer.read(sis);
            else if (realClazz.getComponentType() != null)
                return arraySerializer.read(sis, realClazz);
            else
                return pojoSerializer.read(sis, realClazz, outer);
        }

        @Override
        public Object read(SimpleInputStream sis, Class<?> clazz) throws IOException {
            Object object = read(sis, clazz, false);
            return object;
        }

        // TODO: check and fix circular references
        private boolean writeHeader(Object val, Class<?> clazz, SimpleOutputStream sos) throws IOException {
            if (val == null) {
                sos.write((byte) 0);
                return false;
            }

            Integer objId = knownObjects.get(new Wrap(val));
            if (objId != null) {
                sos.write((byte) 3);
                sos.write(objId.intValue());
                return false;
            }
            if (val.getClass() == clazz) {
                sos.write((byte) 1);
            } else {
                String clname = val.getClass().getName();
                Byte index = classes.get(clname);
                if (index == null) {
                    sos.write((byte) 2);
                    sos.write(clname);
                    // System.err.println("DIFFERING class : " + clazz.getCanonicalName() + "  -->>  " + val.getClass().getCanonicalName());
                } else {
                    sos.write((byte) 4);
                    sos.write(index.byteValue());
                }
            }
            return true;
        }

    }

    private static class HashMapSerializer implements ICompositeSerializer<HashMap<?, ?>> {

        @Override
        public void write(HashMap<?, ?> object, Class<? extends HashMap<?, ?>> clazz, SimpleOutputStream sos, IFieldSerializer fieldSerializer) throws IOException {
            sos.write(object.size());
            for (Entry<?, ?> e : object.entrySet()) {
                fieldSerializer.write(e.getKey(), Object.class, sos);
                fieldSerializer.write(e.getValue(), Object.class, sos);
            }
        }

        @SuppressWarnings("unchecked")
        @Override
        public HashMap<?, ?> read(SimpleInputStream sis, Class<? extends HashMap<?, ?>> clazz, IFieldSerializer fieldSerializer, Consumer<Object> rememberer) throws IOException {
            HashMap hm = new HashMap();
            rememberer.accept(hm);
            int len = sis.readInt();
            for (int i = 0; i < len; i++) {
                Object key = fieldSerializer.read(sis, Object.class);
                Object value = fieldSerializer.read(sis, Object.class);
                hm.put(key, value);
            }
            return hm;
        }
    }

    private static class CollectionSerializer<T extends Collection> implements ICompositeSerializer<T> {

        @Override
        public void write(T val, Class<? extends T> clazz, SimpleOutputStream sos, IFieldSerializer fieldSerializer) throws IOException {
            sos.write(val.size());
            // Optimization: same class

            long classesNum = (int) Stream.of((Collection<?>)val).filter(o -> o != null).map(Object::getClass).distinct().count();
            if (classesNum < val.size() - 2) {
                sos.write((byte) 1);
                List<Class<?>> classes = Stream.of((Collection<?>)val)
                    .filter(o -> o != null)
                    .map(Object::getClass)
                    .distinct()
                    .collect(Collectors.toList());
                Map<Class<?>, Integer> map = IntStream.range(0, classes.size()).boxed().collect(Collectors.toMap(classes::get, i -> i));
                sos.write(classes.size());
                for (Class cl : classes) {
                    sos.write(cl.getName());
                }
                for (Object element : val) {
                    sos.write(map.get(element.getClass()).intValue()); // TODO : optimize by size (byte/short/int !!!)
                    fieldSerializer.write(element, element.getClass(), sos);
                }

            } else {
                sos.write((byte) 2);
                for (Object element : val) {
                    fieldSerializer.write(element, Object.class, sos);
                }
            }
        }

        @SuppressWarnings("unchecked")
        @Override
        public T read(SimpleInputStream sis, Class<? extends T> clazz, IFieldSerializer fieldSerializer, Consumer<Object> rememberer) throws IOException {
            Collection instance = null;

            try {
                instance = (Collection) instantiateUsingNoArgCtor(clazz);
                rememberer.accept(instance);
            } catch (Exception e) {
                throw new ZeSerializerException(e);
            }

            int len = sis.readInt();
            byte type = sis.readByte();
            if (type == 2) {
                for (int i = 0; i < len; i++) {
                    Object value = fieldSerializer.read(sis, Object.class);
                    instance.add(value);
                }
            } else if (type == 1) {
                int classesCount = sis.readInt();
                Class[] classes = new Class[classesCount];
                for (int i = 0; i < classesCount; i++) {
                    classes[i] = parseClassName(sis.readString());
                }
                for (int i = 0; i < len; i++) {
                    int classIndex = sis.readInt();
                    Class<?> clazzz = classes[classIndex];
                    Object value = fieldSerializer.read(sis, clazzz);
                    instance.add(value);
                }

            } else
                throw new ZeSerializerException("Wrong collection encoding type " + type);
            return (T) instance;
        }
    }

    private static class Wrap {

        private Object object;

        public Wrap(Object object) {
            this.object = object;
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(object) + 5;
        }

        @Override
        public boolean equals(Object that) {
            return this.object == ((Wrap) that).object;
        }

    }
}
