package com.zakgof.tools.io;

import java.io.IOException;

public class SimpleClassSerializer implements ISimpleSerializer<Class<?>> {

	public static SimpleClassSerializer INSTANCE = new SimpleClassSerializer();

	private SimpleClassSerializer() {
	}

	@Override
	public void write(SimpleOutputStream out, Class<?> val) throws IOException {
		out.write(val.getName());
	}

	@Override
	public Class<?> read(SimpleInputStream in) throws IOException {
		try {
			return Class.forName(in.readString());
		} catch (ClassNotFoundException e) {
			throw new IOException(e);
		}
	}

}