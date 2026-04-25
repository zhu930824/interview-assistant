package interview.backend.modules.voiceinterview;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import interview.backend.modules.voiceinterview.model.VoiceSessionEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface VoiceSessionMapper extends BaseMapper<VoiceSessionEntity> {
}
