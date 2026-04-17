package ru.gr0946x.ui.tour;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Writes a Motion JPEG (MJPEG) AVI file using pure Java — no external libraries.
 * Usage:
 *   try (MjpegAviWriter w = new MjpegAviWriter(file, 1280, 720, 30)) {
 *       w.writeFrame(image);
 *   }
 */
public class MjpegAviWriter implements Closeable {

    private final RandomAccessFile raf;
    private final int width;
    private final int height;
    private final int fps;

    private int frameCount = 0;
    private int maxJpegSize = 0;
    private final List<Integer> frameOffsets = new ArrayList<>(); // offset from moviDataStart
    private final List<Integer> frameSizes = new ArrayList<>();

    // File positions to seek-and-patch later
    private long riffSizeOffset;
    private long avihTotalFramesOffset;
    private long avihMaxBytesPerSecOffset;
    private long avihSuggestedBufOffset;
    private long strhLengthOffset;
    private long strhSuggestedBufOffset;
    private long moviListSizeOffset;
    private long moviDataStart; // byte offset of 'movi' fourCC

    private final ImageWriter jpegWriter;
    private final ImageWriteParam jpegParam;

    public MjpegAviWriter(File outputFile, int width, int height, int fps) throws IOException {
        this.width = width;
        this.height = height;
        this.fps = fps;

        jpegWriter = ImageIO.getImageWritersByFormatName("JPEG").next();
        jpegParam = jpegWriter.getDefaultWriteParam();
        jpegParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        jpegParam.setCompressionQuality(0.90f);

        raf = new RandomAccessFile(outputFile, "rw");
        raf.setLength(0);
        writeHeader();
    }

    // ─── Header ────────────────────────────────────────────────────────────────

    private void writeHeader() throws IOException {
        // RIFF 'AVI '
        writeFourCC("RIFF");
        riffSizeOffset = raf.getFilePointer();
        writeInt(0); // total file size - 8, patched later
        writeFourCC("AVI ");

        // LIST 'hdrl'
        writeFourCC("LIST");
        // hdrl contents are fixed size: 4 + avih(64) + strl_list(124) = 192
        writeInt(192);
        writeFourCC("hdrl");

        writeAvih();
        writeStrl();

        // LIST 'movi'
        writeFourCC("LIST");
        moviListSizeOffset = raf.getFilePointer();
        writeInt(0); // patched later
        moviDataStart = raf.getFilePointer();
        writeFourCC("movi");
    }

    private void writeAvih() throws IOException {
        writeFourCC("avih");
        writeInt(56);
        // avih data:
        writeInt(1_000_000 / fps);          // dwMicroSecPerFrame
        avihMaxBytesPerSecOffset = raf.getFilePointer();
        writeInt(0);                         // dwMaxBytesPerSec — patched later
        writeInt(0);                         // dwPaddingGranularity
        writeInt(0x10);                      // dwFlags = AVIF_HASINDEX
        avihTotalFramesOffset = raf.getFilePointer();
        writeInt(0);                         // dwTotalFrames — patched later
        writeInt(0);                         // dwInitialFrames
        writeInt(1);                         // dwStreams
        avihSuggestedBufOffset = raf.getFilePointer();
        writeInt(0);                         // dwSuggestedBufferSize — patched later
        writeInt(width);
        writeInt(height);
        writeInt(0); writeInt(0); writeInt(0); writeInt(0); // dwReserved[4]
    }

    private void writeStrl() throws IOException {
        // LIST 'strl' — fixed size: 4('strl') + strh(64) + strf(48) = 116
        writeFourCC("LIST");
        writeInt(116);
        writeFourCC("strl");

        // strh
        writeFourCC("strh");
        writeInt(56);
        writeFourCC("vids");                 // fccType
        writeFourCC("MJPG");                 // fccHandler
        writeInt(0);                         // dwFlags
        writeShort(0);                       // wPriority
        writeShort(0);                       // wLanguage
        writeInt(0);                         // dwInitialFrames
        writeInt(1);                         // dwScale
        writeInt(fps);                       // dwRate
        writeInt(0);                         // dwStart
        strhLengthOffset = raf.getFilePointer();
        writeInt(0);                         // dwLength — patched later
        strhSuggestedBufOffset = raf.getFilePointer();
        writeInt(0);                         // dwSuggestedBufferSize — patched later
        writeInt(0xFFFFFFFF);                // dwQuality
        writeInt(0);                         // dwSampleSize
        writeShort(0); writeShort(0);        // rcFrame: left, top
        writeShort((short) width);           // right
        writeShort((short) height);          // bottom

        // strf (BITMAPINFOHEADER)
        writeFourCC("strf");
        writeInt(40);
        writeInt(40);                        // biSize
        writeInt(width);
        writeInt(height);
        writeShort(1);                       // biPlanes
        writeShort(24);                      // biBitCount
        writeFourCC("MJPG");                 // biCompression
        writeInt(width * height * 3);        // biSizeImage
        writeInt(0); writeInt(0);            // biXPelsPerMeter, biYPelsPerMeter
        writeInt(0); writeInt(0);            // biClrUsed, biClrImportant
    }

    // ─── Frame Writing ─────────────────────────────────────────────────────────

    public void writeFrame(BufferedImage frame) throws IOException {
        byte[] jpeg = encodeJpeg(frame);

        // Offset = current position - moviDataStart (measured from 'movi' fourCC)
        int offset = (int) (raf.getFilePointer() - moviDataStart);
        frameOffsets.add(offset);
        frameSizes.add(jpeg.length);

        writeFourCC("00dc");
        writeInt(jpeg.length);
        raf.write(jpeg);
        if ((jpeg.length & 1) != 0) raf.write(0); // pad to even

        if (jpeg.length > maxJpegSize) maxJpegSize = jpeg.length;
        frameCount++;
    }

    private byte[] encodeJpeg(BufferedImage img) throws IOException {
        // Ensure TYPE_INT_RGB (no alpha) for JPEG
        BufferedImage rgb = img;
        if (img.getType() != BufferedImage.TYPE_INT_RGB) {
            rgb = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
            rgb.createGraphics().drawImage(img, 0, 0, null);
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        jpegWriter.setOutput(new MemoryCacheImageOutputStream(baos));
        jpegWriter.write(null, new IIOImage(rgb, null, null), jpegParam);
        return baos.toByteArray();
    }

    // ─── Close + Patch ─────────────────────────────────────────────────────────

    @Override
    public void close() throws IOException {
        try {
            writeIdx1();
            patchHeaders();
        } finally {
            raf.close();
            jpegWriter.dispose();
        }
    }

    private void writeIdx1() throws IOException {
        writeFourCC("idx1");
        writeInt(frameCount * 16);
        for (int i = 0; i < frameCount; i++) {
            writeFourCC("00dc");
            writeInt(0x10);               // AVIIF_KEYFRAME
            writeInt(frameOffsets.get(i));
            writeInt(frameSizes.get(i));
        }
    }

    private void patchHeaders() throws IOException {
        long fileSize = raf.getFilePointer();

        // RIFF chunk size
        seekAndWriteInt(riffSizeOffset, (int) (fileSize - 8));

        // avih
        seekAndWriteInt(avihTotalFramesOffset, frameCount);
        seekAndWriteInt(avihMaxBytesPerSecOffset, maxJpegSize * fps);
        seekAndWriteInt(avihSuggestedBufOffset, maxJpegSize);

        // strh
        seekAndWriteInt(strhLengthOffset, frameCount);
        seekAndWriteInt(strhSuggestedBufOffset, maxJpegSize);

        // movi list size = 4 ('movi' fourCC) + all frame chunks
        long moviContentSize = fileSize
                - moviDataStart            // from 'movi' fourCC to end of last frame chunk
                - frameCount * 16          // idx1 entries
                - 8;                       // idx1 chunk header
        seekAndWriteInt(moviListSizeOffset, (int) moviContentSize);
    }

    // ─── Primitive Writers ─────────────────────────────────────────────────────

    private void writeFourCC(String s) throws IOException {
        raf.write(s.getBytes("US-ASCII"), 0, 4);
    }

    private void writeInt(int v) throws IOException {
        raf.write(v & 0xFF);
        raf.write((v >> 8) & 0xFF);
        raf.write((v >> 16) & 0xFF);
        raf.write((v >> 24) & 0xFF);
    }

    private void writeShort(int v) throws IOException {
        raf.write(v & 0xFF);
        raf.write((v >> 8) & 0xFF);
    }

    private void seekAndWriteInt(long pos, int v) throws IOException {
        long saved = raf.getFilePointer();
        raf.seek(pos);
        writeInt(v);
        raf.seek(saved);
    }
}
