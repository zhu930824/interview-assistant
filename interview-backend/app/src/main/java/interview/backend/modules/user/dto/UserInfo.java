package interview.backend.modules.user.dto;

public record UserInfo(
    Long id,
    String username,
    String role
) {}
