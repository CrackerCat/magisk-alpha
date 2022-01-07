package com.topjohnwu.magisk.core.utils;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class ApkSigBlock {

    private static final byte[] APK_SIG_MAGIC = {'A', 'P', 'K', ' ', 'S', 'i', 'g', ' ',
            'B', 'l', 'o', 'c', 'k', ' ', '4', '2'};

    public static byte[] getCertificate(String path) throws IOException {
        try (var apk = new RandomAccessFile(path, "r")) {
            var buffer = ByteBuffer.allocate(0x10);
            buffer.order(ByteOrder.LITTLE_ENDIAN);

            apk.seek(apk.length() - 0x6);
            apk.readFully(buffer.array(), 0x0, 0x6);
            int offset = buffer.getInt();
            if (buffer.getShort() != 0) {
                return null;
            }

            apk.seek(offset - 0x10);
            apk.readFully(buffer.array(), 0x0, 0x10);

            if (!Arrays.equals(buffer.array(), APK_SIG_MAGIC)) {
                return null;
            }

            // Read and compare size fields
            apk.seek(offset - 0x18);
            apk.readFully(buffer.array(), 0x0, 0x8);
            buffer.rewind();
            int size = (int) buffer.getLong();

            var block = ByteBuffer.allocate(size + 0x8);
            block.order(ByteOrder.LITTLE_ENDIAN);
            apk.seek(offset - block.capacity());
            apk.readFully(block.array(), 0x0, block.capacity());

            if (size != block.getLong()) {
                return null;
            }

            int length = 0;
            while (block.remaining() > 24) {
                size = (int) block.getLong();
                // v2 id
                if (block.getInt() == 0x7109871a) {
                    // signer-sequence length, signer length, signed data length
                    block.position(block.position() + 12);
                    size = block.getInt(); // digests-sequence length

                    // digests, certificates length
                    block.position(block.position() + size + 0x4);

                    length = block.getInt(); // certificate length
                    break;
                } else {
                    block.position(block.position() + size - 0x4);
                }
            }

            var certificate = new byte[length];
            for (int i = 0; i < length; i++) {
                certificate[i] = block.get();
            }
            return certificate;
        }
    }

}
