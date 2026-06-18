package com.example.authservice.service;

import com.example.authservice.constant.EventType;
import com.example.authservice.constant.Role;
import com.example.authservice.dto.event.EmailVerificationEvent;
import com.example.authservice.dto.request.UserRequest;
import com.example.authservice.dto.request.VerifyRequest;
import com.example.authservice.entity.EmailVerificationCode;
import com.example.authservice.entity.Outbox;
import com.example.authservice.entity.RefreshToken;
import com.example.authservice.entity.User;
import com.example.authservice.exception.auth.EmailAlreadyExistsException;
import com.example.authservice.exception.auth.EmailNotConfirmedException;
import com.example.authservice.exception.auth.IncorrectPasswordException;
import com.example.authservice.mapper.UserMapper;
import com.example.authservice.repository.EmailVerificationCodeRepository;
import com.example.authservice.repository.OutboxRepository;
import com.example.authservice.repository.RefreshTokenRepository;
import com.example.authservice.repository.UserRepository;
import com.example.authservice.security.CustomUserDetails;
import com.example.authservice.security.JwtTokenProvider;
import com.example.authservice.service.impl.AuthServiceImpl;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Tag(value = "authService")
public class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private PasswordEncoder encoder;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private EmailVerificationCodeRepository emailVerificationCodeRepository;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private OutboxRepository outboxRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private AuthServiceImpl authService;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    @Captor
    private ArgumentCaptor<EmailVerificationCode> emailCodeCaptor;

    @Captor
    private ArgumentCaptor<Outbox> outboxCaptor;

    // ========================
    // signUp tests
    // ========================

    @Test
    void signUp_shouldRegisterNewUserSuccessfully() throws Exception {
        // given
        UserRequest userRequest = new UserRequest("test@example.com", "password123");
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");
        user.setRole(Role.ROLE_USER);

        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(userMapper.toEntity(userRequest)).thenReturn(user);
        when(encoder.encode("password123")).thenReturn("encodedPassword");
        when(objectMapper.writeValueAsString(any(EmailVerificationEvent.class))).thenReturn("{\"email\":\"test@example.com\",\"code\":\"12345\"}");

        // when
        authService.signUp(userRequest);

        // then
        verify(userRepository).existsByEmail("test@example.com");
        verify(userMapper).toEntity(userRequest);
        verify(encoder).encode("password123");
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getPassword()).isEqualTo("encodedPassword");
        assertThat(userCaptor.getValue().getRole()).isEqualTo(Role.ROLE_USER);

        verify(emailVerificationCodeRepository).save(emailCodeCaptor.capture());
        assertThat(emailCodeCaptor.getValue().getCode()).hasSize(5);
        assertThat(emailCodeCaptor.getValue().getUser()).isEqualTo(user);
        assertThat(emailCodeCaptor.getValue().getExpiresAt()).isAfter(Instant.now());

        verify(outboxRepository).save(outboxCaptor.capture());
        assertThat(outboxCaptor.getValue().getEventType()).isEqualTo(EventType.EMAIL_SEND_CODE.name());
        assertThat(outboxCaptor.getValue().getPayload()).isEqualTo("{\"email\":\"test@example.com\",\"code\":\"12345\"}");

        verify(tokenProvider).createRefreshToken(user);
    }

    @Test
    void signUp_shouldThrowEmailAlreadyExistsException_whenEmailAlreadyExists() {
        // given
        UserRequest userRequest = new UserRequest("existing@example.com", "password123");
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> authService.signUp(userRequest))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessage("Данный email занят! Попробуйте другой.");

        verify(userRepository).existsByEmail("existing@example.com");
        verify(userRepository, never()).save(any());
        verify(emailVerificationCodeRepository, never()).save(any());
        verify(outboxRepository, never()).save(any());
        verify(tokenProvider, never()).createRefreshToken(any());
    }

    // ========================
    // signIn tests
    // ========================

    @Test
    void signIn_shouldReturnAccessToken_whenCredentialsAreValid() {
        // given
        UserRequest userRequest = new UserRequest("test@example.com", "password123");
        HttpServletResponse response = mock(HttpServletResponse.class);

        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");
        user.setEmailVerified(true);

        Authentication authentication = mock(Authentication.class);
        CustomUserDetails userDetails = mock(CustomUserDetails.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUser()).thenReturn(user);
        when(tokenProvider.generateAccessToken(userDetails)).thenReturn("access-token");
        when(tokenProvider.createRefreshToken(user)).thenReturn("valid-refresh-token");

        // when
        String result = authService.signIn(userRequest, response);

        // then
        assertThat(result).isEqualTo("access-token");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(tokenProvider).generateAccessToken(userDetails);
        verify(refreshTokenRepository).deleteAllByUserId(userId);
        verify(tokenProvider).createRefreshToken(user);
        verify(response).addHeader(eq(HttpHeaders.SET_COOKIE), anyString());
    }

    @Test
    void signIn_shouldThrowUsernameNotFoundException_whenUserNotFound() {
        // given
        UserRequest userRequest = new UserRequest("unknown@example.com", "password123");
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new UsernameNotFoundException("Пользователь не найден"));

        // when & then
        assertThatThrownBy(() -> authService.signIn(userRequest, response))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("Пользователь не найден");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verifyNoInteractions(tokenProvider);
    }

    @Test
    void signIn_shouldThrowBadCredentialsException_whenPasswordIsWrong() {
        // given
        UserRequest userRequest = new UserRequest("test@example.com", "wrong-password");
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // when & then
        assertThatThrownBy(() -> authService.signIn(userRequest, response))
                .isInstanceOf(BadCredentialsException.class);

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verifyNoInteractions(tokenProvider);
    }

    @Test
    void signIn_shouldThrowDisabledException_whenEmailNotVerified() {
        // given
        UserRequest userRequest = new UserRequest("test@example.com", "password123");
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new DisabledException("User is disabled"));

        // when & then
        assertThatThrownBy(() -> authService.signIn(userRequest, response))
                .isInstanceOf(DisabledException.class);

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verifyNoInteractions(tokenProvider);
    }

    // ========================
    // verifyAcc tests
    // ========================

    @Test
    void verifyAcc_shouldVerifyUserSuccessfully() {
        // given
        VerifyRequest verifyRequest = new VerifyRequest("test@example.com", "12345");
        UUID userId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);
        user.setEmail("test@example.com");
        user.setEmailVerified(false);

        EmailVerificationCode validCode = new EmailVerificationCode(
                "12345",
                user,
                Instant.now().plusSeconds(3600)
        );

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(emailVerificationCodeRepository.findAllByUserId(userId)).thenReturn(List.of(validCode));

        // when
        authService.verifyAcc(verifyRequest);

        // then
        assertThat(user.isEmailVerified()).isTrue();
        verify(userRepository).findByEmail("test@example.com");
        verify(emailVerificationCodeRepository).findAllByUserId(userId);
    }

    @Test
    void verifyAcc_shouldThrowUsernameNotFoundException_whenUserNotFound() {
        // given
        VerifyRequest verifyRequest = new VerifyRequest("unknown@example.com", "12345");

        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> authService.verifyAcc(verifyRequest))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("Пользователь не найден");

        verify(userRepository).findByEmail("unknown@example.com");
        verifyNoInteractions(emailVerificationCodeRepository);
    }

    @Test
    void verifyAcc_shouldThrowIllegalArgumentException_whenEmailAlreadyVerified() {
        // given
        VerifyRequest verifyRequest = new VerifyRequest("test@example.com", "12345");

        User user = new User();
        user.setEmail("test@example.com");
        user.setEmailVerified(true);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        // when & then
        assertThatThrownBy(() -> authService.verifyAcc(verifyRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Акканут уже подтвержден");

        verify(userRepository).findByEmail("test@example.com");
        verifyNoInteractions(emailVerificationCodeRepository);
    }

    @Test
    void verifyAcc_shouldThrowResponseStatusException_whenCodeIsInvalid() {
        // given
        VerifyRequest verifyRequest = new VerifyRequest("test@example.com", "wrong-code");
        UUID userId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);
        user.setEmail("test@example.com");
        user.setEmailVerified(false);

        EmailVerificationCode expiredCode = new EmailVerificationCode(
                "12345",
                user,
                Instant.now().minusSeconds(3600)
        );

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(emailVerificationCodeRepository.findAllByUserId(userId)).thenReturn(List.of(expiredCode));

        // when & then
        assertThatThrownBy(() -> authService.verifyAcc(verifyRequest))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessage("409 CONFLICT \"Неверный код для подтверждения акканут\"")
                .satisfies(ex -> {
                    ResponseStatusException rse = (ResponseStatusException) ex;
                    assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
                });

        assertThat(user.isEmailVerified()).isFalse();
        verify(userRepository).findByEmail("test@example.com");
        verify(emailVerificationCodeRepository).findAllByUserId(userId);
    }

    // ========================
    // newAccessToken tests
    // ========================

    @Test
    void newAccessToken_shouldReturnNewAccessToken_whenRefreshTokenIsValid() {
        // given
        String refreshTokenValue = "valid-refresh-token";
        User user = new User();
        user.setEmail("test@example.com");
        user.setRole(Role.ROLE_USER);
        user.setEmailVerified(true);

        RefreshToken validToken = new RefreshToken();
        validToken.setToken(refreshTokenValue);
        validToken.setUser(user);
        validToken.setExpiresAt(Instant.now().plusSeconds(3600));

        when(refreshTokenRepository.findByToken(refreshTokenValue)).thenReturn(List.of(validToken));
        when(tokenProvider.generateAccessToken(any(UserDetails.class))).thenReturn("new-access-token");

        // when
        String result = authService.newAccessToken(refreshTokenValue);

        // then
        assertThat(result).isEqualTo("new-access-token");
        verify(refreshTokenRepository).findByToken(refreshTokenValue);
        verify(tokenProvider).generateAccessToken(any(UserDetails.class));
    }

    @Test
    void newAccessToken_shouldThrowIllegalArgumentException_whenRefreshTokenIsEmpty() {
        // given
        String refreshTokenValue = "";

        // when & then
        assertThatThrownBy(() -> authService.newAccessToken(refreshTokenValue))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Рефреш токен отсутствует или пустой");

        verifyNoInteractions(refreshTokenRepository, tokenProvider);
    }

    @Test
    void newAccessToken_shouldThrowIllegalArgumentException_whenRefreshTokenIsNull() {
        // given
        String refreshTokenValue = null;

        // when & then
        assertThatThrownBy(() -> authService.newAccessToken(refreshTokenValue))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Рефреш токен отсутствует или пустой");

        verifyNoInteractions(refreshTokenRepository, tokenProvider);
    }

    @Test
    void newAccessToken_shouldThrowIllegalArgumentException_whenRefreshTokenIsBlank() {
        // given
        String refreshTokenValue = "   ";

        // when & then
        assertThatThrownBy(() -> authService.newAccessToken(refreshTokenValue))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Рефреш токен отсутствует или пустой");

        verifyNoInteractions(refreshTokenRepository, tokenProvider);
    }

    @Test
    void newAccessToken_shouldThrowIllegalArgumentException_whenNoValidTokenFound() {
        // given
        String refreshTokenValue = "expired-refresh-token";

        RefreshToken expiredToken = new RefreshToken();
        expiredToken.setToken(refreshTokenValue);
        expiredToken.setExpiresAt(Instant.now().minusSeconds(3600));

        when(refreshTokenRepository.findByToken(refreshTokenValue)).thenReturn(List.of(expiredToken));

        // when & then
        assertThatThrownBy(() -> authService.newAccessToken(refreshTokenValue))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("не найден токен");

        verify(refreshTokenRepository).findByToken(refreshTokenValue);
        verify(tokenProvider, never()).generateAccessToken(any());
    }

    // ========================
    // refreshVerifyCode tests
    // ========================

    @Test
    void refreshVerifyCode_shouldSendNewVerificationCodeSuccessfully() throws Exception {
        // given
        UserRequest userRequest = new UserRequest("test@example.com", "password123");
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(encoder.matches("password123", "encodedPassword")).thenReturn(true);
        when(objectMapper.writeValueAsString(any(EmailVerificationEvent.class))).thenReturn("{\"email\":\"test@example.com\",\"code\":\"67890\"}");

        // when
        authService.refreshVerifyCode(userRequest);

        // then
        verify(userRepository).findByEmail("test@example.com");
        verify(encoder).matches("password123", "encodedPassword");

        verify(emailVerificationCodeRepository).save(emailCodeCaptor.capture());
        assertThat(emailCodeCaptor.getValue().getCode()).hasSize(5);
        assertThat(emailCodeCaptor.getValue().getUser()).isEqualTo(user);
        assertThat(emailCodeCaptor.getValue().getExpiresAt()).isAfter(Instant.now());

        verify(outboxRepository).save(outboxCaptor.capture());
        assertThat(outboxCaptor.getValue().getEventType()).isEqualTo(EventType.EMAIL_SEND_CODE.name());
        assertThat(outboxCaptor.getValue().getPayload()).isEqualTo("{\"email\":\"test@example.com\",\"code\":\"67890\"}");
    }

    @Test
    void refreshVerifyCode_shouldThrowUsernameNotFoundException_whenUserNotFound() {
        // given
        UserRequest userRequest = new UserRequest("unknown@example.com", "password123");

        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> authService.refreshVerifyCode(userRequest))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("Пользователь не найден");

        verify(userRepository).findByEmail("unknown@example.com");
        verifyNoInteractions(emailVerificationCodeRepository, outboxRepository);
    }

    @Test
    void refreshVerifyCode_shouldThrowIncorrectPasswordException_whenPasswordIsWrong() {
        // given
        UserRequest userRequest = new UserRequest("test@example.com", "wrong-password");
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(encoder.matches("wrong-password", "encodedPassword")).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> authService.refreshVerifyCode(userRequest))
                .isInstanceOf(IncorrectPasswordException.class);

        verify(userRepository).findByEmail("test@example.com");
        verify(encoder).matches("wrong-password", "encodedPassword");
        verifyNoInteractions(emailVerificationCodeRepository, outboxRepository);
    }
}
