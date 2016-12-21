package com.zakgof.tools.io;

import java.io.IOException;

public interface ISimpleSerializer<T> {
	
	void write(SimpleOutputStream out, T val) throws IOException;

	T read(SimpleInputStream in) throws IOException;
	
}
