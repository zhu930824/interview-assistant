package interview.backend.modules.user.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("user")
public record User(
    @TableId(type = IdType.AUTO)
    Long id,
    String username,
    String passwordHash,
    UserRole role,
    boolean enabled,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static User create(String username, String passwordHash) {
        return new User(
            null,
            username,
            passwordHash,
            UserRole.USER,
            true,
            LocalDateTime.now(),
            LocalDateTime.now()
        );
    }
}
