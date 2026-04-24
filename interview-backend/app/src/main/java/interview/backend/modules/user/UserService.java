package interview.backend.modules.user;

import interview.backend.common.exception.BizException;
import interview.backend.common.security.JwtUtils;
import interview.backend.modules.user.dto.LoginRequest;
import interview.backend.modules.user.dto.LoginResponse;
import interview.backend.modules.user.dto.RegisterRequest;
import interview.backend.modules.user.dto.UserInfo;
import interview.backend.modules.user.model.User;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtils jwtUtils) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
    }

    public void register(RegisterRequest request) {
        Long count = userRepository.selectCount(
                new QueryWrapper<User>().eq("username", request.username())
        );
        if (count > 0) {
            throw new BizException("用户名已存在");
        }

        String passwordHash = passwordEncoder.encode(request.password());
        User user = User.create(request.username(), passwordHash);
        userRepository.insert(user);
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.selectOne(
                new QueryWrapper<User>().eq("username", request.username())
        );
        if (user == null || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BizException("用户名或密码错误");
        }
        if (!user.isEnabled()) {
            throw new BizException("账户已被禁用");
        }

        String token = jwtUtils.generateToken(user.getId(), user.getUsername(), user.getRole().name());
        return new LoginResponse(token, user.getUsername(), user.getRole().name());
    }

    public UserInfo getCurrentUser(Long userId) {
        User user = userRepository.selectById(userId);
        if (user == null) {
            throw new BizException("用户不存在");
        }
        return new UserInfo(user.getId(), user.getUsername(), user.getRole().name());
    }
}
