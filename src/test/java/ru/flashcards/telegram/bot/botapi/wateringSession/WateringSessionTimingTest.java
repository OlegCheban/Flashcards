package ru.flashcards.telegram.bot.botapi.wateringSession;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

class WateringSessionTimingTest {
    private static final long CHAT_ID = 1L;
    private static final int REPLY_TIME_SECONDS = 5;

    private final MutableClock clock = new MutableClock(Instant.parse("2026-01-01T10:00:00Z"));
    private final WateringSessionTiming timing = new WateringSessionTiming(clock);

    @Test
    void acceptsAnswerWithinReplyTime() {
        timing.start(CHAT_ID);
        clock.advance(Duration.ofSeconds(REPLY_TIME_SECONDS));

        assertTrue(timing.isAnsweredInTime(CHAT_ID, REPLY_TIME_SECONDS));
    }

    @Test
    void rejectsLateAnswer() {
        timing.start(CHAT_ID);
        clock.advance(Duration.ofSeconds(REPLY_TIME_SECONDS + 1));

        assertFalse(timing.isAnsweredInTime(CHAT_ID, REPLY_TIME_SECONDS));
    }

    @Test
    void treatsUnknownStartAsAnsweredInTime() {
        assertTrue(timing.isAnsweredInTime(CHAT_ID, REPLY_TIME_SECONDS));
    }

    private static final class MutableClock extends Clock {
        private Instant instant;

        private MutableClock(Instant instant) {
            this.instant = instant;
        }

        private void advance(Duration duration) {
            instant = instant.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return instant;
        }
    }
}
