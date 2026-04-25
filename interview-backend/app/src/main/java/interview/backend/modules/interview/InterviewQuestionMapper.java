package interview.backend.modules.interview;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import interview.backend.modules.interview.model.InterviewQuestionEntity;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface InterviewQuestionMapper extends BaseMapper<InterviewQuestionEntity> {

    @Select("SELECT * FROM interview_question WHERE session_id = #{sessionId} ORDER BY sort_order")
    List<InterviewQuestionEntity> findBySessionIdOrderBySortOrder(Long sessionId);
}
