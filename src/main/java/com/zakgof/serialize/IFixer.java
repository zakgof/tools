package com.zakgof.serialize;

import java.util.Map;

public interface IFixer<T> {
    public T fix(T original, Map<String, Object> loadedFields);
}
