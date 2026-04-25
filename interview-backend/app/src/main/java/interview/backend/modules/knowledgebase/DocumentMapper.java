package interview.backend.modules.knowledgebase;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import interview.backend.modules.knowledgebase.model.DocumentEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DocumentMapper extends BaseMapper<DocumentEntity> {
}
