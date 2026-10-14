package ru.flashcards.telegram.bot.services.pronunciation;

import java.io.ByteArrayOutputStream;

import javax.sound.sampled.AudioFormat;

import org.springframework.stereotype.Component;

import de.sciss.jump3r.lowlevel.LameEncoder;

/**
 * Converts WAV to MP3: Telegram plays MP3 audio but not WAV.
 */
@Component
public class Mp3Encoder {
    /**
     * Enough for speech.
     */
    private static final int BITRATE_KBPS = 64;
    private static final int PCM_SAMPLE_SIZE_BITS = 16;

    /**
     * @throws ru.flashcards.telegram.bot.exception.PronunciationUnavailableException if the WAV is invalid
     */
    public byte[] encode(byte[] wav) {
        WavReader.Pcm16 pcm = WavReader.read(wav);
        AudioFormat format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, pcm.sampleRate(), PCM_SAMPLE_SIZE_BITS,
                pcm.channels(), pcm.channels() * PCM_SAMPLE_SIZE_BITS / Byte.SIZE, pcm.sampleRate(), false);
        return encodePcm(pcm.samples(), format);
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
