package com.logikcode.fileupload.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.Adler32;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class ImageUtil {
//    public static byte[] compressImage(byte[] data) throws IOException {
//        Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION);
//        deflater.setInput(data);
//        deflater.finish();
//
//        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
//        byte[] temp = new byte[4 * 1024];
//
//        try {
//            while (!deflater.finished()) {
//                int count = deflater.deflate(temp);
//                outputStream.write(temp, 0, count);
//            }
//        } finally {
//            deflater.end();
//            outputStream.close();
//        }
//
//        return outputStream.toByteArray();
//    }



    public static byte[] compressImage(byte[] data) throws IOException {
        Adler32 checksum = new Adler32();
        checksum.update(data);

        Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION);
        deflater.setInput(data);
        deflater.finish();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length + 4);
        byte[] temp = new byte[4 * 1024];

        try {
            while (!deflater.finished()) {
                int count = deflater.deflate(temp);
                outputStream.write(temp, 0, count);
            }
        } finally {
            deflater.end();
            outputStream.close();
        }

        byte[] compressedData = outputStream.toByteArray();

        // append checksum to compressed data
        int adler32Checksum = (int) checksum.getValue();
        byte[] adler32Bytes = new byte[] {
                (byte)(adler32Checksum >> 24),
                (byte)(adler32Checksum >> 16),
                (byte)(adler32Checksum >> 8),
                (byte) adler32Checksum
        };
        ByteArrayOutputStream checksumStream = new ByteArrayOutputStream(4);
        checksumStream.write(adler32Bytes);
        checksumStream.close();
        byte[] checksumBytes = checksumStream.toByteArray();
        byte[] compressedDataWithChecksum = new byte[compressedData.length + checksumBytes.length];
        System.arraycopy(compressedData, 0, compressedDataWithChecksum, 0, compressedData.length);
        System.arraycopy(checksumBytes, 0, compressedDataWithChecksum, compressedData.length, checksumBytes.length);

        return compressedDataWithChecksum;
    }


    public static byte[] decompressImage(byte[] data) throws DataFormatException, IOException {
        Inflater inflater = new Inflater();
        inflater.setInput(data);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
        byte[] temp = new byte[4 * 1024];

        try {
            while (!inflater.finished() && !inflater.needsInput()){
                int count = inflater.inflate(temp);
                outputStream.write(temp, 0, count);
            }
        } catch (DataFormatException e) {
            // handle the data format exception here
        } finally {
            inflater.end();
            outputStream.close();
        }

        return outputStream.toByteArray();
    }

}
