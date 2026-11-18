package com.blogchi.service;

import com.blogchi.common.Code;
import com.blogchi.dto.LoginDTO;
import com.blogchi.dto.RegisterDTO;
import com.blogchi.entity.User;
import com.blogchi.exception.BizException;
import com.blogchi.mapper.UserMapper;
import com.blogchi.security.CurrentUser;
import com.blogchi.security.JwtUtil;
import com.blogchi.vo.TokenVO;
import com.blogchi.vo.UserVO;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.blogchi.entity.table.UserTableDef.USER;

/**
 * 注册、登录、获取当前用户信息。
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public TokenVO register(RegisterDTO dto) {
        long exists = userMapper.selectCountByQuery(
                QueryWrapper.create().where(USER.USERNAME.eq(dto.getUsername())));
        if (exists > 0) {
            throw new BizException(Code.CONFLICT, "用户名已被注册");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setNickname(dto.getNickname());
        // 数据库里只存密文，明文密码不落库
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setBio(dto.getBio());
        userMapper.insertSelective(user);

        return new TokenVO(jwtUtil.createToken(user.getId(), user.getUsername()), toVO(user));
    }

    public TokenVO login(LoginDTO dto) {
        User user = userMapper.selectOneByQuery(
                QueryWrapper.create().where(USER.USERNAME.eq(dto.getUsername())));
        // 用户不存在和密码错误返回同一句提示，避免泄露账号是否存在
        if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BizException(Code.BAD_REQUEST, "用户名或密码错误");
        }
        return new TokenVO(jwtUtil.createToken(user.getId(), user.getUsername()), toVO(user));
    }

    public UserVO current() {
        User user = userMapper.selectOneById(CurrentUser.id());
        if (user == null) {
            throw new BizException(Code.UNAUTHORIZED);
        }
        return toVO(user);
    }

    private UserVO toVO(User user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setBio(user.getBio());
        return vo;
    }
}
