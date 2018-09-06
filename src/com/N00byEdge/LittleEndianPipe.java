package com.N00byEdge;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

class LittleEndianPipe {
    RandomAccessFile pipe;
    LittleEndianPipe(String name, String mode) throws FileNotFoundException {
        pipe = new RandomAccessFile(name, mode);
    }

    final int readByte() throws IOException {
        return pipe.readByte();
    }

    final int readInt() throws IOException {
        int b1 = readByte(), b2 = readByte(), b3 = readByte(), b4 = readByte();
        return (b4 << 24) | (b3 << 16) | (b2 << 8) | b1;
    }
}
