package com.zakgof.tools.io;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class SimpleEnumSetSerializer<T extends Enum<T>> implements ISimpleSerializer<EnumSet<T>> {

	private Class<T> clazz;

	public SimpleEnumSetSerializer(Class<T> clazz) {
		this.clazz = clazz;
	}

	@Override
	public void write(SimpleOutputStream out, EnumSet<T> val) throws IOException {
		out.write(val.size());
		for (T t : val)
			out.write(t.ordinal());
	}

	@Override
	public EnumSet<T> read(SimpleInputStream in) throws IOException {
		int len = in.readInt();
		List<T> l = new ArrayList<>(len);
		for (int i=0; i<len; i++) {
			l.add(clazz.getEnumConstants()[in.readInt()]);
		}
		return EnumSet.copyOf(l);
	}

}