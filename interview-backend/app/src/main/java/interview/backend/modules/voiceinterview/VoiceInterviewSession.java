package interview.backend.modules.voiceinterview;

import java.time.LocalDateTime;
import java.util.List;

public record VoiceInterviewSession(
        String sessionId,
        boolean paused,
        String latestTranscript,
        String currentQuestion,
        List<String> subtitles,
        List<String> aiReplies,
        long submittedTurns,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
