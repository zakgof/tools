package com.zakgof.tools.io;

import java.io.IOException;

public class SimpleClassSerializer implements ISimpleSerializer<Class<?>> {

	public static SimpleClassSerializer INSTANCE = new SimpleClassSerializer();


	@Override
	public void write(SimpleOutputStream out, Class<?> val) throws IOException {
		out.write(val.getName());
	}

    @Override
    public Class<?> read(SimpleInputStream in) throws IOException {
        String className = in.readString();
        try {
            switch (className) {
                case "boolean":
                    return boolean.class;
                case "byte":
                    return byte.class;
                case "short":
                    return short.class;
                case "int":
                    return int.class;
                case "long":
                    return long.class;
                case "float":
                    return float.class;
                case "double":
                    return double.class;
                case "char":
                    return char.class;
                case "void":
                    return void.class;
                default:
                    return Class.forName(className);
            }
        } catch (ClassNotFoundException e) {
            throw new IOException("Can't load class " + className);
        }
    }

}