package interview.backend.infrastructure.speech;

import org.springframework.stereotype.Service;

@Service
public class StubSpeechService implements SpeechRecognitionService {

    @Override
    public String transcribe(byte[] audioData, String filename) {
        // 返回模拟文本，用于测试
        return "这是语音识别的模拟结果（Stub 服务）。请配置 OpenAI API key 以启用真实语音识别。";
    }
}