package com.zakgof.tools.io;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class SimpleOutputStream {
	
	private final OutputStream stream;

	public SimpleOutputStream(OutputStream stream) {
		this.stream = stream;
	}

	public void write(String str) throws IOException {
		byte[] bytes = str.getBytes("cp1251");
		write(bytes.length);		
		this.stream.write(bytes);
	}
	
	public void write(byte bt) throws IOException {
    this.stream.write(bt);    
  }

	public void write(int val) throws IOException {
		this.stream.write(ByteBuffer.allocate(4).putInt(val).array());		
	}
	
	public void write(double val) throws IOException {
		this.stream.write(ByteBuffer.allocate(8).putDouble(val).array());		
	}	
	
	public void write(float val) throws IOException {
    this.stream.write(ByteBuffer.allocate(6).putFloat(val).array());   
  } 
	
	public void write(long val) throws IOException {
		this.stream.write(ByteBuffer.allocate(8).putLong(val).array());		
	}

	public void flush() throws IOException {
		stream.flush();
	}
	
	public void close() {
		try {
			stream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

  public void write(byte[] bytes) throws IOException {
    write(bytes.length);    
    this.stream.write(bytes);
  }

}
