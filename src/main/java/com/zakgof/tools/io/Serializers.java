package com.zakgof.tools.io;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.zakgof.tools.generic.IFunction2;
import com.zakgof.tools.generic.IFunction3;
import com.zakgof.tools.generic.IFunction4;
import com.zakgof.tools.generic.Pair;

public class Serializers {

  public static <A, E> ISimpleSerializer<A> collection(final Function<A, Collection<E>> writer, Function <Collection<E>, A> reader, final ISimpleSerializer<E> elementSerializer) {
    
    return new ISimpleSerializer<A>() {

      @Override
      public void write(SimpleOutputStream out, A val) throws IOException {
        Collection<E> elements = writer.apply(val);
        out.write(elements.size());
        for (E element : elements)
          elementSerializer.write(out, element);
      }

      @Override
      public A read(SimpleInputStream in) throws IOException {
        int length = in.readInt();
        List<E> elements = new ArrayList<>();
        for (int i=0; i<length; i++) {
          E element = elementSerializer.read(in);
          elements.add(element);
        }
        return reader.apply(elements);
      }
    };
  }
  
  public static <A, F1> ISimpleSerializer<A> fields(final Function<A, F1> getter1, ISimpleSerializer<F1>serializer1, Function<F1, A> constructor) {
    return new ISimpleSerializer<A>() {

      @Override
      public void write(SimpleOutputStream out, A val) throws IOException {
        serializer1.write(out, getter1.apply(val));
      }

      @Override
      public A read(SimpleInputStream in) throws IOException {
        F1 field1 = serializer1.read(in);
        return constructor.apply(field1);
      }
    };
  }
  
  public static <A, F1, F2> ISimpleSerializer<A> fields(final Function<A, F1> getter1, final Function<A, F2> getter2, ISimpleSerializer<F1>serializer1, ISimpleSerializer<F2>serializer2, IFunction2<F1, F2, A> constructor) {
    return new ISimpleSerializer<A>() {

      @Override
      public void write(SimpleOutputStream out, A val) throws IOException {
        serializer1.write(out, getter1.apply(val));
        serializer2.write(out, getter2.apply(val));
      }

      @Override
      public A read(SimpleInputStream in) throws IOException {
        F1 field1 = serializer1.read(in);
        F2 field2 = serializer2.read(in);
        return constructor.get(field1, field2);
      }
    };
  }
  
  public static <A, F1, F2, F3> ISimpleSerializer<A> fields(final Function<A, F1> getter1, final Function<A, F2> getter2,   final Function<A, F3> getter3, ISimpleSerializer<F1>serializer1, ISimpleSerializer<F2>serializer2, ISimpleSerializer<F3>serializer3, IFunction3<F1, F2, F3, A> constructor) {
    return new ISimpleSerializer<A>() {

      @Override
      public void write(SimpleOutputStream out, A val) throws IOException {
        serializer1.write(out, getter1.apply(val));
        serializer2.write(out, getter2.apply(val));
        serializer3.write(out, getter3.apply(val));
      }

      @Override
      public A read(SimpleInputStream in) throws IOException {
        F1 field1 = serializer1.read(in);
        F2 field2 = serializer2.read(in);
        F3 field3 = serializer3.read(in);
        return constructor.get(field1, field2, field3);
      }
    };
  }
  
  public static <A, F1, F2, F3, F4> ISimpleSerializer<A> fields(final Function<A, F1> getter1, final Function<A, F2> getter2, final Function<A, F3> getter3, final Function<A, F4> getter4, ISimpleSerializer<F1>serializer1, ISimpleSerializer<F2>serializer2, ISimpleSerializer<F3>serializer3, ISimpleSerializer<F4>serializer4, IFunction4<F1, F2, F3, F4, A> constructor) {
    return new ISimpleSerializer<A>() {

      @Override
      public void write(SimpleOutputStream out, A val) throws IOException {
        serializer1.write(out, getter1.apply(val));
        serializer2.write(out, getter2.apply(val));
        serializer3.write(out, getter3.apply(val));
        serializer4.write(out, getter4.apply(val));
      }

      @Override
      public A read(SimpleInputStream in) throws IOException {
        F1 field1 = serializer1.read(in);
        F2 field2 = serializer2.read(in);
        F3 field3 = serializer3.read(in);
        F4 field4 = serializer4.read(in);
        return constructor.get(field1, field2, field3, field4);
      }
    };
  }

  public static <T> ISimpleSerializer<Collection<T>> collection(ISimpleSerializer<T> elementSerializer) {
    return new SimpleCollectionSerializer<T, ISimpleSerializer<T>>(elementSerializer);
  }

  public static <T> PolymorhSerializedBuilder<T> polymorph() {
    return new PolymorhSerializedBuilder<T>();
  }
  
  public static class PolymorhSerializedBuilder<T> {

    private Map<String, ISimpleSerializer<? extends T>> map = new HashMap<>();

    public <I extends T> PolymorhSerializedBuilder<T> impl(Class<I> implClass, ISimpleSerializer<? extends T> implSerializer) {
       map.put(implClass.getName(), implSerializer);
       return this;
    }

    public ISimpleSerializer<T> done() {
      return new ISimpleSerializer<T>() {

        @SuppressWarnings({ "rawtypes", "unchecked" })
        @Override
        public void write(SimpleOutputStream out, T val) throws IOException {
          Class<? extends T> implClass = (Class<? extends T>) val.getClass();
          String classname = implClass.getName();
          out.write(classname);
          ISimpleSerializer serializer = map.get(implClass.getName());
          serializer.write(out, val);
        }

        @Override
        public T read(SimpleInputStream in) throws IOException {
          String className = in.readString();
          return map.get(className).read(in);
        }
      };
    }
    
  }

  public static <T> Builder<T> builder() {
    return new Builder<T>();
  }
  
  public static class Builder<T> {

    private List<Pair<Function<T,?>, ISimpleSerializer<?>>> list = new ArrayList<>();

    public <F> Builder<T> getter(Function<T, ? extends F> getter, ISimpleSerializer<? extends F> serializer) {
      list.add(Pair.create(getter, serializer));
      return this;
    }

    public Builder<T> intGetter(Function<T, Integer> getter) {
      return getter(getter, SimpleIntegerSerializer.INSTANCE);
    }
    
    public Builder<T> stringGetter(Function<T, String> getter) {
      return getter(getter, SimpleStringSerializer.INSTANCE);
    }

    public ISimpleSerializer<T> constructor(Function<Object[], T> constructor ) {
      return new ISimpleSerializer<T>() {

        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        public void write(SimpleOutputStream out, T val) throws IOException {
          for (Pair<Function<T,?>, ISimpleSerializer<?>> entry : list) {
            Object field = entry.first().apply(val);
            ((ISimpleSerializer)entry.second()).write(out, field);
          }
        }

        @Override
        public T read(SimpleInputStream in) throws IOException {
          Object[] args = new Object[list.size()];
          for (int i=0; i<list.size(); i++) {
            args[i] = list.get(i).second().read(in);
          }
          return constructor.apply(args);
        }
        
      };
    }   
    
  }
  

}
