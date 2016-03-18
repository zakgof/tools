package com.zakgof.tools.io;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import com.zakgof.tools.generic.IFunction2;
import com.zakgof.tools.generic.IFunction3;
import com.zakgof.tools.generic.IFunction4;

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

}
