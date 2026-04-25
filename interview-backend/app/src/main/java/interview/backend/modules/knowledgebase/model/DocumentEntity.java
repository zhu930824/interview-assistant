package interview.backend.modules.knowledgebase.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import interview.backend.common.handler.JsonListTypeHandler;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("knowledge_document")
public class DocumentEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String fileName;
    private String filePath;
    private String summary;
    private Integer chunks;
    private Integer tokenEstimate;
    @TableField(typeHandler = JsonListTypeHandler.class)
    private List<String> keywords;
    private String fullContent;
    private Long userId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
