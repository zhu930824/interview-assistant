package interview.backend.infrastructure.speech;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AlibabaSpeechService implements SpeechRecognitionService {

    private static final Logger log = LoggerFactory.getLogger(AlibabaSpeechService.class);

    public boolean isConfigured() {
        // 实时语音识别通过 WebSocket 处理，不需要此服务
        return false;
    }

    @Override
    public String transcribe(byte[] audioData, String filename) {
        throw new UnsupportedOperationException("请使用实时 WebSocket 语音识别 (/ws/realtime-speech)");
    }
}