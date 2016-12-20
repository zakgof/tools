package com.zakgof.serialize;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

import com.zakgof.tools.io.ISimpleSerializer;
import com.zakgof.tools.io.SimpleBooleanSerializer;
import com.zakgof.tools.io.SimpleByteArraySerializer;
import com.zakgof.tools.io.SimpleByteSerializer;
import com.zakgof.tools.io.SimpleDateSerializer;
import com.zakgof.tools.io.SimpleDoubleSerializer;
import com.zakgof.tools.io.SimpleFloatSerializer;
import com.zakgof.tools.io.SimpleInputStream;
import com.zakgof.tools.io.SimpleIntegerSerializer;
import com.zakgof.tools.io.SimpleLocalDateSerializer;
import com.zakgof.tools.io.SimpleLocalDateTimeSerializer;
import com.zakgof.tools.io.SimpleLongSerializer;
import com.zakgof.tools.io.SimpleOutputStream;
import com.zakgof.tools.io.SimpleShortSerializer;
import com.zakgof.tools.io.SimpleStringSerializer;

@SuppressWarnings("rawtypes")
public class ZeSerializer implements ISerializer {

  public static final String COMPATIBLE_POJOS = "compatible.pojos";
  public static final String FORCED_HEADER = "forced.header";
  public static final String USE_OBJENESIS = "use.objenesis";
  private Map<String, ?> parameters;
  private Map<Object, Integer> knownObjects = new HashMap<>();
  private List<Object> knownObjectList = new ArrayList<>();

  public ZeSerializer(Map<String, ?> parameters) {
    this.parameters = parameters;
    initSerializers();
    pojoSerializer = (parameters.get(COMPATIBLE_POJOS) != null) ? new CompatiblePojoSerializer() : new PojoSerializer();
    objenesis = (parameters.get(USE_OBJENESIS) != null) ? new ObjenesisStd() : null;
  }

  public ZeSerializer() {
    this(new HashMap<>());
  }

  private final Objenesis objenesis;

  @Override
  public byte[] serialize(Object object) {
    // long start = System.currentTimeMillis();
    try {
      ByteArrayOutputStream baos = new ByteArrayOutputStream(256);
      serialize(object, baos);
      return baos.toByteArray();
    } catch (IOException e) {
      throw new RuntimeException(e);
    } finally {
      // long end = System.currentTimeMillis();
      // Log.w("perf", "serialize " + object.getClass().getSimpleName() + " in "
      // + (end-start));
    }
  }

  public void serialize(Object object, OutputStream os) throws IOException {
    SimpleOutputStream sos = new SimpleOutputStream(os);
    boolean hoHeader = parameters.containsKey(FORCED_HEADER) ? false : true;
    fieldSerializer.write(object, object.getClass(), sos, hoHeader);
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
    }
  }

  public static List<Field> getAllFields(Class<?> type) {
    List<Field> fields = new ArrayList<Field>();

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

  private interface IFieldSerializer<T> {
    void write(T object, Class<? extends T> clazz, SimpleOutputStream sos) throws IOException;

    T read(SimpleInputStream sis, Class<? extends T> clazz) throws IOException;
  }

  private final FieldSerializer fieldSerializer = new FieldSerializer();
  private final IFieldSerializer<Object> arraySerializer = new ArraySerializer();
  private final IFieldSerializer<Object> pojoSerializer;

  private final Map<Class<?>, ISimpleSerializer<?>> serializers = new HashMap<Class<?>, ISimpleSerializer<?>>();

  private void initSerializers() {
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
    serializers.put(HashMap.class, new HashMapSerializer());
    serializers.put(ArrayList.class, new CollectionSerializer<ArrayList>(ArrayList.class));
    serializers.put(Map.class, new HashMapSerializer());
    serializers.put(List.class, new CollectionSerializer<ArrayList>(ArrayList.class));
    serializers.put(Set.class, new CollectionSerializer<HashSet>(HashSet.class));
    serializers.put(HashSet.class, new CollectionSerializer<HashSet>(HashSet.class));
    serializers.put(LocalDate.class, SimpleLocalDateSerializer.INSTANCE);
    serializers.put(LocalDateTime.class, SimpleLocalDateTimeSerializer.INSTANCE);
  }

  private Object instantiate(Class<? extends Object> clazz) throws ReflectiveOperationException, SecurityException {
    if (objenesis != null) {
      return objenesis.getInstantiatorOf(clazz).newInstance();
    } else {
      Constructor<? extends Object> noArgConsructor = clazz.getDeclaredConstructor();
      noArgConsructor.setAccessible(true);
      return noArgConsructor.newInstance();
    }
  }

  private class PojoSerializer implements IFieldSerializer<Object> {

    @Override
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

    @Override
    public Object read(SimpleInputStream sis, Class<? extends Object> clazz) throws IOException {
      try {
        Object object = instantiate(clazz);
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

  private class CompatiblePojoSerializer implements IFieldSerializer<Object> {

    @Override
    public void write(Object object, Class<? extends Object> clazz, SimpleOutputStream sos) throws IOException {
      try {
        sos.write(getAllFields(clazz).size());
        for (Field field : getAllFields(clazz)) {
          if ((field.getModifiers() & (Modifier.TRANSIENT | Modifier.STATIC)) == 0) {
            field.setAccessible(true);
            sos.write(field.getName());
            fieldSerializer.write(field.get(object), Object.class, sos);
          }
        }
      } catch (IllegalAccessException e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public Object read(SimpleInputStream sis, Class<? extends Object> clazz) throws IOException {
      try {

        Constructor<?> constructor = clazz.getDeclaredConstructor();
        constructor.setAccessible(true);
        Object object = constructor.newInstance();

        Integer fieldCount = sis.readInt();

        for (int i = 0; i < fieldCount; i++) {
          String name = sis.readString();
          Object fieldValue = fieldSerializer.read(sis, Object.class);
          if (!assignFieldValue(clazz, object, name, fieldValue))
            System.err.println("field" + name + " with value " + fieldValue + " not found in the class " + clazz);
        }

        return object;
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

    private boolean assignFieldValue(Class<? extends Object> clazz, Object object, String name, Object fieldValue) throws IllegalAccessException {
      for (Field field : getAllFields(clazz)) {
        if ((field.getModifiers() & (Modifier.TRANSIENT | Modifier.STATIC)) == 0) {
          if (field.getName().equals(name)) {
            field.setAccessible(true);
            if (fieldValue == null || field.getType().isAssignableFrom(fieldValue.getClass()))
              field.set(object, fieldValue);
            else
              System.err.println("Field " + field.getName() + " from " + clazz + " cannot be assigned the value " + fieldValue);
            return true;
          }
        }
      }
      return false;
    }

  }

  private class ArraySerializer implements IFieldSerializer<Object> {

    @Override
    public void write(Object object, Class<? extends Object> clazz, SimpleOutputStream sos) throws IOException {
      Object[] arr = (Object[]) object;
      sos.write(arr.length);
      for (int i = 0; i < arr.length; i++) {
        Object val = arr[i];
        fieldSerializer.write(val, clazz.getComponentType(), sos);
      }
    }

    @Override
    public Object read(SimpleInputStream sis, Class<? extends Object> clazz) throws IOException {
      int length = sis.readInt();
      if (length < 0)
        throw new RuntimeException("Invalid array length");
      Class<?> componentType = clazz.getComponentType();
      Object[] instance = (Object[]) Array.newInstance(componentType, length);
      for (int i = 0; i < length; i++) {
        Object read = fieldSerializer.read(sis, componentType);
        instance[i] = read;
      }
      return instance;
    }

  }

  private class FieldSerializer implements IFieldSerializer<Object> {

	public void write(Object object, Class<?> clazz, SimpleOutputStream sos, boolean noHeader) throws IOException {
      if (!noHeader) {
        if (!clazz.isPrimitive())
          if (!writeHeader(object, clazz, sos))
            return;
      }
      
      Class<? extends Object> actualClazz = object.getClass();
      
      if (!actualClazz.isPrimitive() && object != null && !knownObjects.containsKey(object)) {
    	  knownObjectList.add(object);
    	  knownObjects.put(object, knownObjectList.size());
      }
      
      @SuppressWarnings("unchecked")
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

    @SuppressWarnings("unchecked")
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
        	  Object val = knownObjects.get(objid);
        	  return val;
          } else if (hdr == 1) {
            // Nothing, realClazz = clazz
          } else if (hdr == 2) {
            try {
            	realClazz = Class.forName(sis.readString());
            } catch (ClassNotFoundException e) {
              throw new RuntimeException(e);
            }
          } else {
        	  throw new RuntimeException("Invalid hdr");
        	  
          }
        }
      }

      ISimpleSerializer<Object> contentSerializer = (ISimpleSerializer<Object>) serializers.get(realClazz);
      if (contentSerializer != null)
        return contentSerializer.read(sis);
      else if (realClazz.getComponentType() != null)
        return arraySerializer.read(sis, realClazz);
      else
        return pojoSerializer.read(sis, realClazz);
    }

    @Override
    public Object read(SimpleInputStream sis, Class<?> clazz) throws IOException {
      Object object = read(sis, clazz, false);
      if (!clazz.isPrimitive() && object != null && !knownObjects.containsKey(object)) {
    	  knownObjectList.add(object);
    	  knownObjects.put(object, knownObjectList.size());
      }
      return object;
    }

    // TODO: check and fix circular references
    private boolean writeHeader(Object val, Class<?> clazz, SimpleOutputStream sos) throws IOException {
      if (val == null) {
        sos.write((byte) 0);
        return false;
      }

	  Integer objId = knownObjects.get(val);
	  if (objId != null) {
		sos.write((byte) 3);
		sos.write(objId.intValue());
		return false;
	  }
      
      if (val.getClass() == clazz) {
        sos.write((byte) 1);
      } else {
        sos.write((byte) 2);
        sos.write(val.getClass().getName());
      }
      return true;
    }

  }

  private class HashMapSerializer implements ISimpleSerializer<HashMap<?, ?>> {

    @Override
    public void write(SimpleOutputStream sos, HashMap<?, ?> object) throws IOException {
      sos.write(object.size());
      for (Entry<?, ?> e : object.entrySet()) {
        fieldSerializer.write(e.getKey(), Object.class, sos);
        fieldSerializer.write(e.getValue(), Object.class, sos);
      }
    }

    @SuppressWarnings("unchecked")
    @Override
    public HashMap<?, ?> read(SimpleInputStream sis) throws IOException {
      HashMap hm = new HashMap();
      int len = sis.readInt();
      for (int i = 0; i < len; i++) {
        Object key = fieldSerializer.read(sis, Object.class);
        Object value = fieldSerializer.read(sis, Object.class);
        hm.put(key, value);
      }
      return hm;
    }

  }

  private class CollectionSerializer<T extends Collection> implements ISimpleSerializer<T> {

    private final Class<T> clazz;

    CollectionSerializer(Class<T> clazz) {
      this.clazz = clazz;
    }

    @Override
    public void write(SimpleOutputStream sos, T val) throws IOException {
      sos.write(val.size());
      for (Object element : val) {
        fieldSerializer.write(element, Object.class, sos);
      }
    }

    @SuppressWarnings("unchecked")
    @Override
    public T read(SimpleInputStream sis) throws IOException {
      Collection instance = null;
      try {
        instance = clazz.newInstance();
      } catch (InstantiationException e) {
        e.printStackTrace();
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      }
      int len = sis.readInt();
      for (int i = 0; i < len; i++) {
        Object value = fieldSerializer.read(sis, Object.class);
        instance.add(value);
      }
      return (T) instance;
    }

  }
}
