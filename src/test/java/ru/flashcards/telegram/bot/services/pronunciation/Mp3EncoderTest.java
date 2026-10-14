package ru.flashcards.telegram.bot.services.pronunciation;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import org.junit.jupiter.api.Test;

import ru.flashcards.telegram.bot.exception.PronunciationUnavailableException;

class Mp3EncoderTest {
    private static final double TONE_HZ = 440;
    private static final double DURATION_SECONDS = 0.5;

    private final Mp3Encoder encoder = new Mp3Encoder();

    @Test
    void encodes16BitMonoWav() throws IOException {
        byte[] mp3 = encoder.encode(wav(24_000, 1, false));

        assertMp3(mp3);
    }

    @Test
    void encodesFloatStereoWav() throws IOException {
        byte[] mp3 = encoder.encode(wav(48_000, 2, true));

        assertMp3(mp3);
    }

    @Test
    void rejectsNonWavData() {
        byte[] notWav = "{\"error\":\"model_terms_required\"}".getBytes(StandardCharsets.UTF_8);

        assertThrows(PronunciationUnavailableException.class, () -> encoder.encode(notWav));
    }

    /**
     * An MP3 starts with an ID3 tag or with a frame sync: 11 set bits.
     */
    private static void assertMp3(byte[] mp3) {
        boolean hasId3 = mp3.length > 3 && mp3[0] == 'I' && mp3[1] == 'D' && mp3[2] == '3';
        boolean hasFrameSync = mp3.length > 2 && (mp3[0] & 0xFF) == 0xFF && (mp3[1] & 0xE0) == 0xE0;
        assertTrue(mp3.length > 100 && (hasId3 || hasFrameSync), "not an MP3, length=" + mp3.length);
    }

    private static byte[] wav(float sampleRate, int channels, boolean isFloat) throws IOException {
        int sampleSizeBytes = isFloat ? 4 : 2;
        int frames = (int) (sampleRate * DURATION_SECONDS);
        ByteBuffer samples = ByteBuffer.allocate(frames * channels * sampleSizeBytes).order(ByteOrder.LITTLE_ENDIAN);
        for (int frame = 0; frame < frames; frame++) {
            double value = 0.5 * Math.sin(2 * Math.PI * TONE_HZ * frame / sampleRate);
            for (int channel = 0; channel < channels; channel++) {
                if (isFloat) {
                    samples.putFloat((float) value);
                } else {
                    samples.putShort((short) (value * Short.MAX_VALUE));
                }
            }
        }

        AudioFormat format = new AudioFormat(
                isFloat ? AudioFormat.Encoding.PCM_FLOAT : AudioFormat.Encoding.PCM_SIGNED,
                sampleRate, sampleSizeBytes * 8, channels, channels * sampleSizeBytes, sampleRate, false);
        ByteArrayOutputStream wav = new ByteArrayOutputStream();
        AudioSystem.write(new AudioInputStream(new ByteArrayInputStream(samples.array()), format, frames),
                AudioFileFormat.Type.WAVE, wav);
        return wav.toByteArray();
    }
}
