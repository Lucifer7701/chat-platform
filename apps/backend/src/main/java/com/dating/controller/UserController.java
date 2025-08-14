package com.dating.controller;

import com.dating.dto.LoginRequest;
import com.dating.dto.ResetPasswordRequest;
import com.dating.dto.SendCodeRequest;
import com.dating.dto.UserRegisterRequest;
import com.dating.entity.User;
import com.dating.entity.UserAuth;
import com.dating.exception.BusinessException;
import com.dating.service.SmsService;
import com.dating.service.UserAuthService;
import com.dating.service.UserService;
import com.dating.util.JwtUtil;
import com.dating.util.Result;
import com.dating.vo.UserVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/user")
@Slf4j
@Validated
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserAuthService userAuthService;

    @Autowired
    private SmsService smsService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 发送注册验证码
     */
    @PostMapping("/send-register-code")
    public Result sendRegisterCode(@Valid @RequestBody SendCodeRequest request, HttpServletRequest httpRequest) {
        try {
            // 验证手机号格式
            if (!isValidPhone(request.getPhone())) {
                return Result.error("手机号格式不正确");
            }

            // 检查手机号是否已注册
            if (userService.isPhoneRegistered(request.getPhone())) {
                return Result.error("手机号已注册");
            }

            // 获取客户端IP
            String ip = getClientIp(httpRequest);

            // 发送验证码
            if (smsService.sendSmsCode(request.getPhone(), 1, ip)) { // 1-注册
                return Result.success("验证码发送成功");
            }

            return Result.error("验证码发送失败，请稍后重试");
        } catch (Exception e) {
            log.error("发送注册验证码异常：", e);
            return Result.error("系统异常，请稍后重试");
        }
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result register(@Valid @RequestBody UserRegisterRequest request, HttpServletRequest httpRequest) {
        try {
            // 性别校验（1-男 2-女）
            if (request.getGender() == null || (request.getGender() != 1 && request.getGender() != 2)) {
                return Result.error("请选择性别");
            }

            // 验证手机号格式
            if (!isValidPhone(request.getPhone())) {
                return Result.error("手机号格式不正确");
            }

            // 验证短信验证码
//            if (!smsService.verifySmsCode(request.getPhone(), request.getSmsCode(), 1)) {
//                return Result.error("验证码错误或已过期");
//            }

            // 检查手机号是否已注册
            if (userService.isPhoneRegistered(request.getPhone())) {
                return Result.error("手机号已注册");
            }

            // 检查用户名是否已存在（如果提供了用户名）
            if (StringUtils.isNotBlank(request.getUsername()) &&
                    userService.existsByUsername(request.getUsername())) {
                return Result.error("用户名已存在");
            }

            // 注册用户
            User user = userService.registerUser(request);
            if (user != null) {
                // 生成JWT Token
                String token = jwtUtil.generateToken(user.getId().toString());

                // 返回用户基本信息（不包含敏感信息）
                UserVO userVO = convertToUserVO(user);

                return Result.success("注册成功")
                        .put("token", token)
                        .put("user", userVO);
            }

            return Result.error("注册失败，请稍后重试");
        } catch (BusinessException e) {
            log.warn("注册业务异常：{}", e.getMessage());
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("用户注册异常：", e);
            return Result.error("系统异常，请稍后重试");
        }
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        try {
            // 验证参数
            if (StringUtils.isBlank(request.getPhone()) || StringUtils.isBlank(request.getPassword())) {
                return Result.error("手机号和密码不能为空");
            }

            // 验证用户
            UserAuth userAuth = userAuthService.authenticateUser(request.getPhone(), request.getPassword());
            if (userAuth == null) {
                return Result.error("手机号或密码错误");
            }

            // 获取用户信息
            User user = userService.findById(userAuth.getUserId());
            if (user == null || user.getStatus() != 1) {
                return Result.error("用户不存在或已被冻结");
            }

            // 更新登录信息
            String ip = getClientIp(httpRequest);
            userAuthService.updateLoginInfo(userAuth.getId(), ip);

            // 生成Token
            String token = jwtUtil.generateToken(user.getId().toString());

            // 返回结果
            UserVO userVO = convertToUserVO(user);
            return Result.success("登录成功")
                    .put("token", token)
                    .put("user", userVO);

        } catch (Exception e) {
            log.error("用户登录异常：", e);
            return Result.error("登录失败，请稍后重试");
        }
    }

    /**
     * 发送找回密码验证码
     */
    @PostMapping("/send-reset-password-code")
    public Result sendResetPasswordCode(@Valid @RequestBody SendCodeRequest request, HttpServletRequest httpRequest) {
        try {
            // 验证手机号格式
            if (!isValidPhone(request.getPhone())) {
                return Result.error("手机号格式不正确");
            }

            // 检查手机号是否已注册
            if (!userService.isPhoneRegistered(request.getPhone())) {
                return Result.error("手机号未注册");
            }

            // 获取客户端IP
            String ip = getClientIp(httpRequest);

            // 发送验证码
            if (smsService.sendSmsCode(request.getPhone(), 3, ip)) { // 3-找回密码
                return Result.success("验证码发送成功");
            }

            return Result.error("验证码发送失败，请稍后重试");
        } catch (Exception e) {
            log.error("发送找回密码验证码异常：", e);
            return Result.error("系统异常，请稍后重试");
        }
    }

    /**
     * 重置密码
     */
    @PostMapping("/reset-password")
    public Result resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            // 验证手机号格式
            if (!isValidPhone(request.getPhone())) {
                return Result.error("手机号格式不正确");
            }

            // 验证短信验证码
            if (!smsService.verifySmsCode(request.getPhone(), request.getSmsCode(), 3)) {
                return Result.error("验证码错误或已过期");
            }

            // 检查手机号是否已注册
            if (!userService.isPhoneRegistered(request.getPhone())) {
                return Result.error("手机号未注册");
            }

            // 重置密码
            if (userAuthService.changePassword(request.getPhone(), request.getNewPassword())) {
                return Result.success("密码重置成功");
            }

            return Result.error("密码重置失败，请稍后重试");
        } catch (Exception e) {
            log.error("重置密码异常：", e);
            return Result.error("系统异常，请稍后重试");
        }
    }

    /**
     * 获取用户信息
     */
    @GetMapping("/profile")
    public Result getUserProfile(HttpServletRequest request) {
        try {
            // 从Token中获取用户ID
            String token = getTokenFromRequest(request);
            if (StringUtils.isBlank(token)) {
                return Result.error("未登录");
            }

            String userId = jwtUtil.getUserIdFromToken(token);
            if (StringUtils.isBlank(userId)) {
                return Result.error("Token无效");
            }

            // 查询用户信息
            User user = userService.findById(Long.valueOf(userId));
            if (user == null) {
                return Result.error("用户不存在");
            }

            UserVO userVO = convertToUserVO(user);
            return Result.success().put("user", userVO);

        } catch (Exception e) {
            log.error("获取用户信息异常：", e);
            return Result.error("获取用户信息失败");
        }
    }

    /**
     * 转换为用户VO
     */
    private UserVO convertToUserVO(User user) {
        UserVO userVO = new UserVO();
        userVO.setId(user.getId());
        userVO.setPhone(desensitizePhone(user.getPhone()));
        userVO.setUsername(user.getUsername());
        userVO.setNickname(user.getNickname());
        userVO.setAvatar(user.getAvatar());
        userVO.setGender(user.getGender());
        userVO.setCity(user.getCity());
        userVO.setRealNameVerified(user.getRealNameVerified());
        userVO.setStatus(user.getStatus());
        return userVO;
    }

    /**
     * 从请求中获取Token
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (StringUtils.isNotBlank(token) && token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        return null;
    }

    /**
     * 获取客户端IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    /**
     * 验证手机号格式
     */
    private boolean isValidPhone(String phone) {
        if (StringUtils.isBlank(phone)) {
            return false;
        }
        // 中国大陆手机号正则
        String phoneRegex = "^1[3-9]\\d{9}$";
        return phone.matches(phoneRegex);
    }

    /**
     * 手机号脱敏
     */
    private String desensitizePhone(String phone) {
        if (StringUtils.isBlank(phone) || phone.length() < 11) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }

    /**
     * 更新用户位置
     */
    @PostMapping("/update-location")
    public Result updateLocation(@RequestBody LocationUpdateRequest request, HttpServletRequest httpRequest) {
        try {
            // 从Token中获取用户ID
            String token = getTokenFromRequest(httpRequest);
            if (StringUtils.isBlank(token)) {
                return Result.error("未登录");
            }

            String userId = jwtUtil.getUserIdFromToken(token);
            if (StringUtils.isBlank(userId)) {
                return Result.error("Token无效");
            }

            // 更新位置
            int result = userService.updateUserLocation(Long.valueOf(userId), request.getLatitude(), request.getLongitude());
            if (result > 0) {
                return Result.success("位置更新成功");
            }

            return Result.error("位置更新失败");
        } catch (Exception e) {
            log.error("更新用户位置异常：", e);
            return Result.error("系统异常，请稍后重试");
        }
    }

    /**
     * 获取附近用户
     */
    @GetMapping("/nearby")
    public Result getNearbyUsers(@RequestParam(defaultValue = "20") Integer limit, HttpServletRequest request) {
        try {
            // 从Token中获取用户ID
            String token = getTokenFromRequest(request);
            if (StringUtils.isBlank(token)) {
                return Result.error("未登录");
            }

            String userId = jwtUtil.getUserIdFromToken(token);
            if (StringUtils.isBlank(userId)) {
                return Result.error("Token无效");
            }

            // 获取当前用户信息
            User currentUser = userService.findById(Long.valueOf(userId));
            if (currentUser == null || currentUser.getLatitude() == null || currentUser.getLongitude() == null) {
                return Result.error("请先更新位置信息");
            }

            List<User> nearbyUsers = userService.findNearbyUsers(Long.valueOf(userId), 
                    currentUser.getLatitude(), currentUser.getLongitude(), limit);
            
            // 脱敏处理
            nearbyUsers.forEach(user -> user.setPhone(null));

            return Result.success().put("users", nearbyUsers);
        } catch (Exception e) {
            log.error("获取附近用户异常：", e);
            return Result.error("获取附近用户失败");
        }
    }

    /**
     * 获取同城用户
     */
    @GetMapping("/same-city")
    public Result getSameCityUsers(@RequestParam(defaultValue = "20") Integer limit, HttpServletRequest request) {
        try {
            // 从Token中获取用户ID
            String token = getTokenFromRequest(request);
            if (StringUtils.isBlank(token)) {
                return Result.error("未登录");
            }

            String userId = jwtUtil.getUserIdFromToken(token);
            if (StringUtils.isBlank(userId)) {
                return Result.error("Token无效");
            }

            // 获取当前用户信息
            User currentUser = userService.findById(Long.valueOf(userId));
            if (currentUser == null || StringUtils.isBlank(currentUser.getCity())) {
                return Result.error("请先设置城市信息");
            }

            List<User> sameCityUsers = userService.findSameCityUsers(Long.valueOf(userId), 
                    currentUser.getCity(), limit);
            
            // 脱敏处理
            sameCityUsers.forEach(user -> user.setPhone(null));

            return Result.success().put("users", sameCityUsers);
        } catch (Exception e) {
            log.error("获取同城用户异常：", e);
            return Result.error("获取同城用户失败");
        }
    }

    // 位置更新请求DTO
    public static class LocationUpdateRequest {
        private BigDecimal latitude;
        private BigDecimal longitude;

        public BigDecimal getLatitude() { return latitude; }
        public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }
        public BigDecimal getLongitude() { return longitude; }
        public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }
    }

    /**
     * 更新用户资料
     */
    @PostMapping("/update-profile")
    public Result updateProfile(@RequestBody ProfileUpdateRequest request, HttpServletRequest httpRequest) {
        try {
            // 从Token中获取用户ID
            String token = getTokenFromRequest(httpRequest);
            if (StringUtils.isBlank(token)) {
                return Result.error("未登录");
            }

            String userId = jwtUtil.getUserIdFromToken(token);
            if (StringUtils.isBlank(userId)) {
                return Result.error("Token无效");
            }

            // 更新用户资料
            User user = new User();
            user.setId(Long.valueOf(userId));
            if (StringUtils.isNotBlank(request.getNickname())) {
                user.setNickname(request.getNickname());
            }
            if (StringUtils.isNotBlank(request.getIntroduction())) {
                user.setIntroduction(request.getIntroduction());
            }

            int result = userService.updateUserProfile(user);
            if (result > 0) {
                return Result.success("更新成功");
            }

            return Result.error("更新失败");
        } catch (Exception e) {
            log.error("更新用户资料异常：", e);
            return Result.error("系统异常，请稍后重试");
        }
    }

    // 资料更新请求DTO
    public static class ProfileUpdateRequest {
        private String nickname;
        private String introduction;

        public String getNickname() { return nickname; }
        public void setNickname(String nickname) { this.nickname = nickname; }
        public String getIntroduction() { return introduction; }
        public void setIntroduction(String introduction) { this.introduction = introduction; }
    }
}