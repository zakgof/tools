package com.zakgof.serialize;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.zakgof.tools.io.ISimpleSerializer;
import com.zakgof.tools.io.SimpleBooleanSerializer;
import com.zakgof.tools.io.SimpleByteArraySerializer;
import com.zakgof.tools.io.SimpleByteSerializer;
import com.zakgof.tools.io.SimpleClassSerializer;
import com.zakgof.tools.io.SimpleDateSerializer;
import com.zakgof.tools.io.SimpleDoubleSerializer;
import com.zakgof.tools.io.SimpleEnumSerializer;
import com.zakgof.tools.io.SimpleFloatSerializer;
import com.zakgof.tools.io.SimpleInputStream;
import com.zakgof.tools.io.SimpleIntegerSerializer;
import com.zakgof.tools.io.SimpleLongSerializer;
import com.zakgof.tools.io.SimpleOutputStream;
import com.zakgof.tools.io.SimpleShortSerializer;
import com.zakgof.tools.io.SimpleStringSerializer;

@SuppressWarnings("rawtypes")
public class ZeSerializer implements ISerializer {

    private Map<Wrap, Integer> knownObjects = new HashMap<>();
    private List<Object> knownObjectList = new ArrayList<>();
    private IUpgrader upgrader;
    private static final Objenesis objenesis = new ObjenesisStd();
    private boolean upgradeHappened;

    @Override
    public <T> byte[] serialize(T object, Class<T> clazz) {
        // long start = System.currentTimeMillis();
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream(256);
            serialize(object, baos, clazz);
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

    public <T> void serialize(T object, OutputStream os, Class<T> clazz) throws IOException {
        try {
            SimpleOutputStream sos = new SimpleOutputStream(os);
            fieldSerializer.write(object, clazz, sos);
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
            upgradeHappened = false;
            SimpleInputStream sis = new SimpleInputStream(is);
            return (T) fieldSerializer.read(sis, clazz);
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

    public void setUpgrader(IUpgrader upgrader) {
        this.upgrader = upgrader;
    }

    public boolean wasUpgraded() {
        return upgradeHappened;
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

    private Field findSerializableField(Class<? extends Object> clazz, String name, Class<?> fieldType) {
        try {
            Field field = clazz.getDeclaredField(name);
            if ((field.getModifiers() & (Modifier.STATIC | Modifier.TRANSIENT)) == 0 && fieldType.equals(field.getType()))
                return field;
        } catch (NoSuchFieldException e) {
            Class<?> parent = clazz.getSuperclass();
            if (parent != null)
                return findSerializableField(parent, name, fieldType);
        }
        return null;
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
    private final ArraySerializer arraySerializer = new ArraySerializer();
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

        compositeSerializers.put(HashMap.class, new MapSerializer<>(HashMap::new));
        compositeSerializers.put(LinkedHashMap.class, new MapSerializer<>(LinkedHashMap::new));
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

    private Object instantiate(Class<? extends Object> clazz) throws ReflectiveOperationException, SecurityException {
        Object instance = createObject(clazz);
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

    private Object createObject(Class<? extends Object> clazz) throws ReflectiveOperationException, SecurityException {
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
                throw new ZeSerializerException(e);
            }
        }

        public Object read(SimpleInputStream sis, Class<? extends Object> clazz, byte classVersion) throws IOException {
            try {
                Object object = instantiate(clazz);

                if (upgrader != null) {
                    if (upgrader.getCurrentVersionOf(clazz) != classVersion) {
                        ClassStructure cs = upgrader.getStructureFor(clazz, classVersion);
                        if (cs == null) {
                            throw new ZeSerializerException("Upgrader cannot provide " + clazz.getName() + " ver." + classVersion);
                        }
                        Map<String, Class<?>> fields = cs.getFields();
                        for (Entry<String, Class<?>> entry : fields.entrySet()) {
                            String name = entry.getKey();
                            Class<?> type = entry.getValue();
                            Object fieldValue = fieldSerializer.read(sis, type);
                            // attempt to match a field:
                            Field field = findSerializableField(clazz, name, type);
                            if (field != null) {
                                field.setAccessible(true);
                                field.set(object, fieldValue);
                            }
                        }
                        upgradeHappened = true;
                        return object;
                    }
                }

                for (Field field : getAllFields(clazz)) {
                    field.setAccessible(true);
                    Object value = fieldSerializer.read(sis, field.getType());
                    field.set(object, value);
                }
                return object;
            } catch (Exception e) {
                throw new ZeSerializerException(e);
            }
        }
    }

    private class ArraySerializer {

        public void write(Object object, Class<?> clazz, SimpleOutputStream sos) throws IOException {
            int length = Array.getLength(object);
            sos.write(length);
            for (int i = 0; i < length; i++) {
                Object val = Array.get(object, i);
                fieldSerializer.write(val, clazz.getComponentType(), sos);
            }
        }

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

        @Override
        @SuppressWarnings("unchecked")
        public void write(Object object, Class<?> clazz, SimpleOutputStream sos) throws IOException {

            if (!clazz.isPrimitive())
                if (!writeHeader(object, clazz, sos))
                    return;


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

            if (actualClazz.isEnum()) {
                new SimpleEnumSerializer(actualClazz).write(sos, (Enum) object);
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
        public Object read(SimpleInputStream sis, Class<?> clazz) throws IOException {
            Class<?> realClazz = clazz;
            byte classVersion = 0;

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
                } else if (hdr == 5) {
                    // realClazz = clazz
                    classVersion = sis.readByte();
                } else if (hdr == 4) {
                    byte index = sis.readByte();
                    String className = classes.inverse().get(index);
                    if (className == null)
                        throw new ZeSerializerException("Unknown class id: " + index);
                    realClazz = parseClassName(className);
                } else if (hdr == 2) {
                    String className = sis.readString();
                    realClazz = parseClassName(className);
                } else if (hdr == 6) {
                    String className = sis.readString();
                    realClazz = parseClassName(className);
                    classVersion = sis.readByte();
                } else {
                    throw new ZeSerializerException("Parsing error: invalid header value: " + hdr
                                                    + " when parsing class " + clazz.getName());
                }
            }

            Object object = readObject(sis, realClazz, classVersion);
            if (!realClazz.isPrimitive()) {
                rememberObject(object);
            }
            return object;
        }

        @SuppressWarnings("unchecked")
        private Object readObject(SimpleInputStream sis, Class<?> realClazz, byte classVersion) throws IOException {
            if (realClazz.isEnum()) {
                return new SimpleEnumSerializer(realClazz).read(sis); // TODO: versioning !
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
                return pojoSerializer.read(sis, realClazz, classVersion);
        }

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
            byte version = upgrader == null ? 0 : upgrader.getCurrentVersionOf(val.getClass());
            if (val.getClass() == clazz) {
                sos.write(version == 0 ? (byte) 1 : (byte) 5);
                if (version != 0) {
                    sos.write(version);
                }
            } else {
                String clname = val.getClass().getName();
                Byte index = classes.get(clname);
                if (index == null) {
                    sos.write(version == 0 ? (byte) 2 : (byte) 6);
                    sos.write(clname);
                    if (version != 0) {
                        sos.write(version);
                    }
                } else {
                    sos.write((byte) 4);
                    sos.write(index.byteValue());
                }
            }
            return true;
        }

    }

    private static class MapSerializer<T extends Map> implements ICompositeSerializer<T> {

        private Supplier<T> factory;

        MapSerializer(Supplier<T> factory) {
            this.factory = factory;
        }

        @SuppressWarnings("unchecked")
        @Override
        public void write(T object, Class<? extends T> clazz, SimpleOutputStream sos, IFieldSerializer fieldSerializer) throws IOException {
            sos.write(object.size());
            Set<Map.Entry> entrySet = object.entrySet();
            for (Entry e : entrySet) {
                fieldSerializer.write(e.getKey(), Object.class, sos); // TODO: this is very poor since we know the type
                fieldSerializer.write(e.getValue(), Object.class, sos);
            }
        }

        @SuppressWarnings("unchecked")
        @Override
        public T read(SimpleInputStream sis, Class<? extends T> clazz, IFieldSerializer fieldSerializer, Consumer<Object> rememberer) throws IOException {
            T hm = factory.get();
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

            long classesNum = (int) Stream.of(val).filter(o -> o != null).map(Object::getClass).distinct().count();
            if (classesNum < val.size() - 2) {
                sos.write((byte) 1);
                @SuppressWarnings("unchecked")
                List<Class<?>> classes = (List)val.stream().filter(o -> o != null).map(Object::getClass).distinct().collect(Collectors.toList());
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
            } catch (ReflectiveOperationException e) {
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
