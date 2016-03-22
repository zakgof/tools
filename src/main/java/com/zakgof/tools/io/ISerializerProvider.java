package com.zakgof.tools.io;

public interface ISerializerProvider<T> {

  ISimpleSerializer<T> serializer();
}
