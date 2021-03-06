/*
Copyright (c) 2010-2011 Graham Edgecombe
Copyright (c) 2011-2016 Major <major.emrs@gmail.com> and other apollo contributors

Permission to use, copy, modify, and/or distribute this software for any
purpose with or without fee is hereby granted, provided that the above
copyright notice and this permission notice appear in all copies.

THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package scape.editor.util;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * A utility class for performing compression/decompression.
 *
 * @author Graham
 */
public final class CompressionUtils {

    /**
     * Bzip2s the specified array, removing the header.
     *
     * @param uncompressed The uncompressed array.
     * @return The compressed array.
     * @throws IOException If there is an error compressing the array.
     */
    public static byte[] bzip2(byte[] uncompressed) throws IOException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();

        try (BZip2CompressorOutputStream os = new BZip2CompressorOutputStream(bout, 1)) {
            os.write(uncompressed);
            os.finish();

            byte[] compressed = bout.toByteArray();
            byte[] newCompressed = new byte[compressed.length - 4]; // Strip the header
            System.arraycopy(compressed, 4, newCompressed, 0, newCompressed.length);
            return newCompressed;
        }
    }

    /**
     * Debzip2s the compressed array and places the result into the decompressed array.
     *
     * @param compressed   The compressed array, <strong>without</strong> the header.
     * @param decompressed The decompressed array.
     * @throws IOException If there is an error decompressing the array.
     */
    public static void debzip2(byte[] compressed, byte[] decompressed) throws IOException {
        byte[] newCompressed = new byte[compressed.length + 4];
        newCompressed[0] = 'B';
        newCompressed[1] = 'Z';
        newCompressed[2] = 'h';
        newCompressed[3] = '1';
        System.arraycopy(compressed, 0, newCompressed, 4, compressed.length);

        try (DataInputStream is = new DataInputStream(new BZip2CompressorInputStream(new ByteArrayInputStream(newCompressed)))) {
            is.readFully(decompressed);
        }
    }

    /**
     * Degzips the compressed array and places the results into the decompressed array.
     *
     * @param compressed   The compressed array.
     * @param decompressed The decompressed array.
     * @throws IOException If an I/O error occurs.
     */
    public static void degzip(byte[] compressed, byte[] decompressed) throws IOException {
        try (DataInputStream is = new DataInputStream(new GZIPInputStream(new ByteArrayInputStream(compressed)))) {
            is.readFully(decompressed);
        }
    }

    /**
     * Degzips <strong>all</strong> of the datain the specified {@link ByteBuffer}.
     *
     * @param compressed The compressed buffer.
     * @return The decompressed array.
     * @throws IOException If there is an error decompressing the buffer.
     */
    public static byte[] degzip(ByteBuffer compressed) throws IOException {
        try (InputStream is = new GZIPInputStream(new ByteArrayInputStream(compressed.array()));
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];

            while (true) {
                int read = is.read(buffer, 0, buffer.length);
                if (read == -1) {
                    break;
                }

                out.write(buffer, 0, read);
            }

            return out.toByteArray();
        }
    }

    /**
     * Gzips the specified array.
     *
     * @param uncompressed The uncompressed array.
     * @return The compressed array.
     * @throws IOException If there is an error compressing the array.
     */
    public static byte[] gzip(byte[] uncompressed) throws IOException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();

        try (DeflaterOutputStream os = new GZIPOutputStream(bout)) {
            os.write(uncompressed);
            os.finish();
            return bout.toByteArray();
        }
    }

    public static boolean isGZipped(InputStream in) {
        if (!in.markSupported()) {
            in = new BufferedInputStream(in);
        }
        in.mark(2);
        int magic = 0;
        try {
            magic = in.read() & 0xff | ((in.read() << 8) & 0xff00);
            in.reset();
        } catch (IOException e) {
            e.printStackTrace(System.err);
            return false;
        }
        return magic == GZIPInputStream.GZIP_MAGIC;
    }

    /**
     * Default private constructor to prevent instantiation.
     */
    private CompressionUtils() {

    }

}