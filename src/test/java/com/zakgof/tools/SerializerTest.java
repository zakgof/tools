package com.zakgof.tools;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableMap;
import com.zakgof.serialize.ZeSerializer;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.junit.Test;

public class SerializerTest {

    @Test
    public void testSimple() {
        assertRestoredEquals(new Simple("str", 35));
        assertRestoredEquals(new Simple(null, 35));
    }

    @Test
    public void testSimplePolymorph() {
        assertRestoredEquals(new SimplePolymorph(42, "String"));
        assertRestoredEquals(new SimplePolymorph(42, Long.valueOf(123457L)));
        assertRestoredEquals(new SimplePolymorph(42, new SimplePolymorph(37, "Hello")));
        SimplePolymorph recursive = new SimplePolymorph(42, null);
        recursive.setObj(recursive);
        assertRestoredEquals(recursive);
    }

    @Test
    public void testArrays() {
        assertRestoredEquals(new ArraysHolder(new int[] { 0, 1, 2, 3, 4, 5 }, new Object[] { "one", Integer.valueOf(1), Long.valueOf(2) }, new String[] { "one", "two" }));
        assertRestoredEquals(new ArraysHolder(new int[] { 0, 1, 2, 3, 4, 5 }, new Object[] { null, Integer.valueOf(1), null }, null));
        ArraysHolder recursive = new ArraysHolder(new int[] { 0, 1, 2, 3, 4, 5 }, null, new String[] { "one", "two" });
        recursive.setObjects(new Object[] { null, recursive });
        // assertRestoredEquals(recursive);
    }

    @Test
    public void testEnum() {
        assertRestoredEquals(TestEnum.ONE);
        assertRestoredEquals(TestEnum.TWO);
        assertRestoredEquals(EnumSet.allOf(TestEnum.class));
        assertRestoredEquals(EnumSet.noneOf(TestEnum.class));
        assertRestoredEquals(EnumSet.of(TestEnum.ONE));
        assertRestoredEquals(EnumSet.of(TestEnum.ONE, TestEnum.TWO));
    }

    @Test
    public void testCollections() {
        assertRestoredEquals(new CollectionHolder(new HashSet<>(Arrays.asList("one", "two")), new ArrayList<>(Arrays.asList(3, 4)), new HashMap<>(ImmutableMap.of(1L, "ONE", 2L, "TWO"))));
        // assertRestoredEquals(new CollectionHolder(ImmutableMap.of("ONE", 1L, "TWO", 2L).keySet(), Arrays.asList(3, 4), ImmutableMap.of(1L, "ONE", 2L, "TWO")));
    }

    @Test
    public void testInners() {
        InnerHolder ih1 = new InnerHolder(5, "one", 10L);
        assertRestoredEquals(ih1);

        InnerHolder ih2 = new InnerHolder(37, ih1.getInner());
        assertRestoredEquals(ih2);
        assertRestoredEquals(ih1.getInner());
        assertRestoredEquals(ih2.getInner());
        assertRestoredEquals(ih1.getInner().inner2);
        assertRestoredEquals(ih2.getInner().inner2);
    }

    private <T> void assertRestoredEquals(T original) {
        @SuppressWarnings("unchecked")
        Class<T> clazz = (Class<T>) original.getClass();
        ZeSerializer ze = new ZeSerializer();
        byte[] bytes = ze.serialize(original, clazz);
        T restored = ze.deserialize(new ByteArrayInputStream(bytes), clazz);
        assertEquals(original, restored);
    }

}

@EqualsAndHashCode
@RequiredArgsConstructor
class Simple {
    final String string;
    final int integer;
}

@AllArgsConstructor
class SimplePolymorph {
    final int integer;
    @Setter
    Object obj;

    @Override
    public boolean equals(Object obj) {
        SimplePolymorph that = (SimplePolymorph)obj;
        if (this.integer != that.integer)
            return false;
        if (this.obj == this)
            return that.obj == that;
        return this.obj.equals(that.obj);
    }
}

@EqualsAndHashCode
@AllArgsConstructor
class ArraysHolder {
    final int[] integers;
    @Setter
    Object[] objects;
    String[] strings;
}

@EqualsAndHashCode
@AllArgsConstructor
class CollectionHolder {
    Set<String> strings;
    List<Integer> ints;
    Map<Long, Object> map;
}

@RequiredArgsConstructor
@Getter
enum TestEnum {
    ONE(1),
    TWO(2),
    THREE(3);
    private final int v;
}

@EqualsAndHashCode
@AllArgsConstructor
@Getter
class InnerHolder {
    InnerHolder(int integer, String s, Long l) {
        this.integer = integer;
        this.inner = new InnerLevel1(s, l);
    }
    int integer;
    InnerLevel1 inner;
    @EqualsAndHashCode
    @AllArgsConstructor
    class InnerLevel1 {
        InnerLevel1(String s, Long l) {
            this.inner2 = new InnerLevel2(l);
            this.str = s;
        }
        InnerLevel2 inner2;
        String str;
        @EqualsAndHashCode
        @AllArgsConstructor
        class InnerLevel2 {
            Long lon;
        }
    }
}
