package com.zakgof.serialize;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ClassStructure {

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((fieldMap == null) ? 0 : fieldMap.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ClassStructure other = (ClassStructure) obj;
        if (fieldMap == null) {
            if (other.fieldMap != null)
                return false;
        } else if (!fieldMap.equals(other.fieldMap))
            return false;
        return true;
    }

    private Map<String, Class<?>> fieldMap;

    public static ClassStructure of(Class<?> clazz) {
        return new ClassStructure(clazz);
    }

    private ClassStructure(Class<?> clazz) {
        List<Field> fields = ZeSerializer.getAllFields(clazz);
        this.fieldMap = fields.stream().collect(Collectors.toMap(Field::getName, Field::getType, (u, v) -> u, LinkedHashMap::new));
    }

    public Map<String, Class<?>> getFields() {
        return fieldMap;
    }

}
