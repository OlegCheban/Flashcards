package ru.flashcards.telegram.bot.services.pronunciation;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

import ru.flashcards.telegram.bot.exception.PronunciationUnavailableException;

/**
 * Reads a WAV file into 16-bit signed little-endian PCM, the input of the MP3 encoder.
 * Unlike javax.sound it accepts streaming WAV whose data size is unknown (0 or 0xFFFFFFFF):
 * TTS APIs write such headers when they stream the audio.
 * Supports integer PCM of 8, 16, 24 and 32 bits and float PCM of 32 and 64 bits.
 */
final class WavReader {
    private static final int FORMAT_PCM = 1;
    private static final int FORMAT_FLOAT = 3;
    private static final int FORMAT_EXTENSIBLE = 0xFFFE;
    private static final int HEADER_SIZE = 12;
    private static final int CHUNK_HEADER_SIZE = 8;

    private WavReader() {
    }

    record Pcm16(byte[] samples, float sampleRate, int channels) {
    }

    static Pcm16 read(byte[] wav) {
        ByteBuffer buffer = ByteBuffer.wrap(wav).order(ByteOrder.LITTLE_ENDIAN);
        if (wav.length < HEADER_SIZE || !"RIFF".equals(tag(wav, 0)) || !"WAVE".equals(tag(wav, 8))) {
            throw new PronunciationUnavailableException("Not a WAV file: length=" + wav.length);
        }

        Format format = null;
        int position = HEADER_SIZE;
        while (position + CHUNK_HEADER_SIZE <= wav.length) {
            String chunkId = tag(wav, position);
            long chunkSize = Integer.toUnsignedLong(buffer.getInt(position + 4));
            int chunkStart = position + CHUNK_HEADER_SIZE;
            long available = wav.length - chunkStart;

            if ("fmt ".equals(chunkId)) {
                format = readFormat(buffer, chunkStart);
            } else if ("data".equals(chunkId)) {
                if (format == null) {
                    throw new PronunciationUnavailableException("WAV data chunk comes before the format chunk");
                }
                int length = (int) (chunkSize == 0 || chunkSize > available ? available : chunkSize);
                return new Pcm16(toPcm16(buffer, chunkStart, length, format), format.sampleRate(), format.channels());
            }

            if (chunkSize > available) {
                break;
            }
            position = chunkStart + (int) chunkSize + (int) (chunkSize % 2);
        }
        throw new PronunciationUnavailableException("WAV file has no data chunk");
    }

    private static Format readFormat(ByteBuffer buffer, int start) {
        int formatTag = Short.toUnsignedInt(buffer.getShort(start));
        int channels = Short.toUnsignedInt(buffer.getShort(start + 2));
        int sampleRate = buffer.getInt(start + 4);
        int bitsPerSample = Short.toUnsignedInt(buffer.getShort(start + 14));
        if (formatTag == FORMAT_EXTENSIBLE) {
            // the sub-format GUID starts with the actual format tag
            formatTag = Short.toUnsignedInt(buffer.getShort(start + 24));
        }

        boolean isSupported = formatTag == FORMAT_PCM && (bitsPerSample == 8 || bitsPerSample == 16
                || bitsPerSample == 24 || bitsPerSample == 32)
                || formatTag == FORMAT_FLOAT && (bitsPerSample == 32 || bitsPerSample == 64);
        if (!isSupported || channels < 1 || sampleRate <= 0) {
            throw new PronunciationUnavailableException("Unsupported WAV format: tag=" + formatTag
                    + ", bits=" + bitsPerSample + ", channels=" + channels + ", sampleRate=" + sampleRate);
        }
        return new Format(formatTag == FORMAT_FLOAT, channels, sampleRate, bitsPerSample / Byte.SIZE);
    }

    private static byte[] toPcm16(ByteBuffer buffer, int start, int length, Format format) {
        int samples = length / format.bytesPerSample();
        ByteBuffer pcm = ByteBuffer.allocate(samples * Short.BYTES).order(ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < samples; i++) {
            pcm.putShort(sample16(buffer, start + i * format.bytesPerSample(), format));
        }
        return pcm.array();
    }

    private static short sample16(ByteBuffer buffer, int offset, Format format) {
        if (format.isFloat()) {
            double value = format.bytesPerSample() == Double.BYTES ? buffer.getDouble(offset) : buffer.getFloat(offset);
            return (short) Math.round(Math.max(-1.0, Math.min(1.0, value)) * Short.MAX_VALUE);
        }
        return switch (format.bytesPerSample()) {
            case 1 -> (short) ((Byte.toUnsignedInt(buffer.get(offset)) - 128) << 8);
            case 2 -> buffer.getShort(offset);
            case 3 -> (short) ((buffer.get(offset + 1) & 0xFF) | (buffer.get(offset + 2) << 8));
            default -> (short) (buffer.getInt(offset) >> 16);
        };
    }

    private static String tag(byte[] wav, int offset) {
        return new String(wav, offset, 4, StandardCharsets.US_ASCII);
    }

    private record Format(boolean isFloat, int channels, float sampleRate, int bytesPerSample) {
    }
}
