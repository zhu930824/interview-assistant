package interview.backend.infrastructure.speech;

public interface SpeechRecognitionService {
    /**
     * Transcribe audio data to text.
     * @param audioData Audio file bytes (supports mp3, mp4, mpeg, mpga, m4a, wav, webm)
     * @param filename Original filename with extension
     * @return Transcribed text
     */
    String transcribe(byte[] audioData, String filename);
}
