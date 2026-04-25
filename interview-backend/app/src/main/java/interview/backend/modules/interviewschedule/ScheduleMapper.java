package interview.backend.modules.interviewschedule;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import interview.backend.modules.interviewschedule.model.ScheduleEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ScheduleMapper extends BaseMapper<ScheduleEntity> {
}
