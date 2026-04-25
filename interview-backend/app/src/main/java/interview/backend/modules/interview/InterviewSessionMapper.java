package interview.backend.modules.interview;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import interview.backend.modules.interview.model.InterviewSessionEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface InterviewSessionMapper extends BaseMapper<InterviewSessionEntity> {
}
