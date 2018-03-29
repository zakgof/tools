package com.zakgof.serialize;

public interface IUpgrader {

    byte getCurrentVersionOf(Class<? extends Object> clazz);

    ClassStructure getStructureFor(Class<?> clazz, byte classVersion);

}
