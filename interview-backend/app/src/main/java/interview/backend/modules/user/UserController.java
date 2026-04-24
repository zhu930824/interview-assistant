package interview.backend.modules.user;

import interview.backend.common.result.ApiResponse;
import interview.backend.common.security.UserContext;
import interview.backend.modules.user.dto.LoginRequest;
import interview.backend.modules.user.dto.LoginResponse;
import interview.backend.modules.user.dto.RegisterRequest;
import interview.backend.modules.user.dto.UserInfo;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ApiResponse<Void> register(@Valid @RequestBody RegisterRequest request) {
        userService.register(request);
        return ApiResponse.success("注册成功", null);
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(userService.login(request));
    }

    @GetMapping("/me")
    public ApiResponse<UserInfo> me() {
        Long userId = UserContext.getUserId();
        return ApiResponse.success(userService.getCurrentUser(userId));
    }
}
