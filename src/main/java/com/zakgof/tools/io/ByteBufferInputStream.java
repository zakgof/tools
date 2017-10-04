package com.zakgof.tools.io;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class ByteBufferInputStream extends InputStream {
    private ByteBuffer byteBuffer;

    public ByteBufferInputStream() {
    }

    public ByteBufferInputStream(int bufferSize) {
        this(ByteBuffer.allocate(bufferSize));
        byteBuffer.flip();
    }

    public ByteBufferInputStream(ByteBuffer byteBuffer) {
        this.byteBuffer = byteBuffer;
    }

    public ByteBuffer getByteBuffer() {
        return byteBuffer;
    }

    public void setByteBuffer(ByteBuffer byteBuffer) {
        this.byteBuffer = byteBuffer;
    }

    @Override
    public int read() throws IOException {
        if (!byteBuffer.hasRemaining())
            return -1;
        return byteBuffer.get();
    }

    @Override
    public int read(byte[] bytes, int offset, int length) throws IOException {
        int count = Math.min(byteBuffer.remaining(), length);
        if (count == 0)
            return -1;
        byteBuffer.get(bytes, offset, length);
        return count;
    }

    @Override
    public int available() throws IOException {
        return byteBuffer.remaining();
    }
}