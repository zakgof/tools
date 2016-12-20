package com.zakgof.tools.io;

import java.io.IOException;

public class SimpleEnumSerializer<T extends Enum<T>> implements ISimpleSerializer<T> {

	private Class<T> clazz;

	public SimpleEnumSerializer(Class<T> clazz) {
		this.clazz = clazz;
	}

	@Override
	public void write(SimpleOutputStream out, T val) throws IOException {
		out.write(val.ordinal());
	}

	@Override
	public T read(SimpleInputStream in) throws IOException {
		return clazz.getEnumConstants()[in.readInt()];
	}

}