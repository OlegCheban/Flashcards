package ru.flashcards.telegram.bot.services.pronunciation;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.springframework.stereotype.Component;

import de.sciss.jump3r.lowlevel.LameEncoder;

import ru.flashcards.telegram.bot.exception.PronunciationUnavailableException;

/**
 * Converts WAV to MP3: Telegram plays MP3 audio but not WAV.
 * Any PCM WAV is accepted; it is converted to 16-bit signed PCM, which the LAME encoder requires.
 */
@Component
public class Mp3Encoder {
    /**
     * Enough for speech.
     */
    private static final int BITRATE_KBPS = 64;
    private static final int PCM_SAMPLE_SIZE_BITS = 16;

    public byte[] encode(byte[] wav) {
        try (AudioInputStream source = AudioSystem.getAudioInputStream(new ByteArrayInputStream(wav))) {
            AudioFormat pcmFormat = pcm16Format(source.getFormat());
            try (AudioInputStream pcm = AudioSystem.getAudioInputStream(pcmFormat, source)) {
                return encodePcm(pcm.readAllBytes(), pcmFormat);
            }
        } catch (UnsupportedAudioFileException | IOException | IllegalArgumentException e) {
            throw new PronunciationUnavailableException("Unable to convert WAV to MP3", e);
        }
    }

    private static AudioFormat pcm16Format(AudioFormat source) {
        int frameSize = source.getChannels() * PCM_SAMPLE_SIZE_BITS / Byte.SIZE;
        return new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, source.getSampleRate(), PCM_SAMPLE_SIZE_BITS,
                source.getChannels(), frameSize, source.getSampleRate(), false);
    }

    private static byte[] encodePcm(byte[] pcm, AudioFormat format) {
        int channelMode = format.getChannels() == 1 ? LameEncoder.CHANNEL_MODE_MONO : LameEncoder.CHANNEL_MODE_AUTO;
        LameEncoder encoder = new LameEncoder(format, BITRATE_KBPS, channelMode, LameEncoder.QUALITY_HIGH, false);
        try {
            ByteArrayOutputStream mp3 = new ByteArrayOutputStream();
            byte[] buffer = new byte[encoder.getMP3BufferSize()];
            int chunkSize = encoder.getPCMBufferSize();
            for (int offset = 0; offset < pcm.length; offset += chunkSize) {
                int written = encoder.encodeBuffer(pcm, offset, Math.min(chunkSize, pcm.length - offset), buffer);
                mp3.write(buffer, 0, written);
            }
            int written = encoder.encodeFinish(buffer);
            mp3.write(buffer, 0, written);
            return mp3.toByteArray();
        } finally {
            encoder.close();
        }
    }
}
