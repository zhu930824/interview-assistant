package interview.backend.modules.user.dto;

public record LoginResponse(
    String token,
    String username,
    String role
) {}
