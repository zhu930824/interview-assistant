package interview.backend.modules.resume;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import interview.backend.modules.resume.model.Resume;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ResumeMapper extends BaseMapper<Resume> {
}
