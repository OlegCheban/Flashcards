package ru.flashcards.telegram.bot.services.pronunciation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import ru.flashcards.telegram.bot.exception.PronunciationUnavailableException;

class WavReaderTest {
    private static final int FORMAT_PCM = 1;
    private static final int UNKNOWN_SIZE = 0xFFFFFFFF;

    @Test
    void readsStreamingWavWithUnknownSizes() {
        byte[] samples = shorts((short) 1000, (short) -1000, (short) 2000);
        byte[] wav = wav(FORMAT_PCM, 1, 24_000, 16, samples, UNKNOWN_SIZE);

        WavReader.Pcm16 pcm = WavReader.read(wav);

        assertEquals(24_000, pcm.sampleRate());
        assertEquals(1, pcm.channels());
        assertEquals(3, pcm.samples().length / 2);
        assertEquals(-1000, ByteBuffer.wrap(pcm.samples()).order(ByteOrder.LITTLE_ENDIAN).getShort(2));
    }

    @Test
    void converts24BitSamples() {
        // 0x123456 and -0x123456 as 24-bit little-endian
        byte[] samples = {0x56, 0x34, 0x12, (byte) 0xAA, (byte) 0xCB, (byte) 0xED};
        byte[] wav = wav(FORMAT_PCM, 1, 48_000, 24, samples, samples.length);

        ByteBuffer pcm = ByteBuffer.wrap(WavReader.read(wav).samples()).order(ByteOrder.LITTLE_ENDIAN);

        assertEquals(0x1234, pcm.getShort(0));
        assertEquals((short) 0xEDCB, pcm.getShort(2));
    }

    @Test
    void rejectsNonWavData() {
        byte[] json = "{\"error\":\"model_terms_required\"}".getBytes(StandardCharsets.UTF_8);

        assertThrows(PronunciationUnavailableException.class, () -> WavReader.read(json));
    }

    @Test
    void rejectsUnsupportedFormat() {
        byte[] wav = wav(6, 1, 8_000, 8, new byte[] {1, 2}, 2);

        assertThrows(PronunciationUnavailableException.class, () -> WavReader.read(wav));
    }

    private static byte[] shorts(short... values) {
        ByteBuffer buffer = ByteBuffer.allocate(values.length * 2).order(ByteOrder.LITTLE_ENDIAN);
        for (short value : values) {
            buffer.putShort(value);
        }
        return buffer.array();
    }

    private static byte[] wav(int formatTag, int channels, int sampleRate, int bits, byte[] samples, int dataSize) {
        int blockAlign = channels * bits / 8;
        ByteBuffer wav = ByteBuffer.allocate(44 + samples.length).order(ByteOrder.LITTLE_ENDIAN);
        int riffSize = dataSize == UNKNOWN_SIZE ? UNKNOWN_SIZE : 36 + dataSize;
        wav.put("RIFF".getBytes(StandardCharsets.US_ASCII)).putInt(riffSize)
                .put("WAVE".getBytes(StandardCharsets.US_ASCII))
                .put("fmt ".getBytes(StandardCharsets.US_ASCII)).putInt(16)
                .putShort((short) formatTag).putShort((short) channels).putInt(sampleRate)
                .putInt(sampleRate * blockAlign).putShort((short) blockAlign).putShort((short) bits)
                .put("data".getBytes(StandardCharsets.US_ASCII)).putInt(dataSize)
                .put(samples);
        return wav.array();
    }
}
