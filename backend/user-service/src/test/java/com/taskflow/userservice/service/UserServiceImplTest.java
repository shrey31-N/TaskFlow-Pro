package com.taskflow.userservice.service;

import com.taskflow.userservice.dto.request.ChangePasswordRequest;
import com.taskflow.userservice.dto.request.LoginRequest;
import com.taskflow.userservice.dto.request.UpdateUserRequest;
import com.taskflow.userservice.dto.response.LoginResponse;
import com.taskflow.userservice.dto.response.MessageResponse;
import com.taskflow.userservice.exception.EmailAlreadyExistsException;
import com.taskflow.userservice.exception.InvalidPasswordException;
import com.taskflow.userservice.repository.UserRepository;
import com.taskflow.userservice.security.JwtService;
import com.taskflow.userservice.service.S3Service;
import com.taskflow.userservice.service.impl.UserServiceImpl;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.taskflow.userservice.dto.request.CreateUserRequest;
import com.taskflow.userservice.dto.response.UserResponse;
import com.taskflow.userservice.entity.User;
import com.taskflow.userservice.enums.Role;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.multipart.MultipartFile;
import org.mockito.InOrder;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository repository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private S3Service s3Service;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void shouldRegisterUserSuccessfully() {

        // Arrange

        CreateUserRequest request = new CreateUserRequest();

        request.setFullName("Tejass");
        request.setEmail("tejas123@test.com");
        request.setPassword("Password123");
        request.setPhone("1234567899");
        request.setRole(Role.ADMIN);

        User savedUser = new User();

        savedUser.setId(1L);
        savedUser.setFullName("Tejass");
        savedUser.setEmail("tejas123@test.com");
        savedUser.setPassword("encodedPassword");
        savedUser.setPhone("1234567899");
        savedUser.setRole(Role.ADMIN);

        when(repository.findByEmail(request.getEmail()))
                .thenReturn(Optional.empty());

        when(passwordEncoder.encode(request.getPassword()))
                .thenReturn("encodedPassword");

        when(repository.save(any(User.class)))
                .thenReturn(savedUser);

        // Act

        UserResponse response = userService.register(request);

        // Assert

        assertNotNull(response);

        assertEquals(1L, response.getId());

        assertEquals("Tejass", response.getFullName());

        assertEquals("tejas123@test.com", response.getEmail());

        assertEquals("1234567899", response.getPhone());

        assertEquals("ADMIN", response.getRole());

        verify(repository).findByEmail(request.getEmail());

        verify(passwordEncoder).encode(request.getPassword());

        verify(repository).save(any(User.class));
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadyExists() {

        // Arrange
        CreateUserRequest request = new CreateUserRequest();

        request.setFullName("Tejass");
        request.setEmail("tejas123@test.com");
        request.setPassword("Password123");
        request.setPhone("1234567899");
        request.setRole(Role.ADMIN);

        when(repository.findByEmail(request.getEmail()))
                .thenReturn(Optional.empty());

        User existingUser = new User();

        existingUser.setEmail("tejas123@test.com");

        when(repository.findByEmail(request.getEmail()))
                .thenReturn(Optional.of(existingUser));

        // Act + Assert
        EmailAlreadyExistsException exception =
                assertThrows(
                        EmailAlreadyExistsException.class,
                        () -> userService.register(request)
                );

        assertEquals(
                "Email already exists",
                exception.getMessage()
        );

        verify(repository, never()).save(any(User.class));

    }

    @Test
    void shouldLoginSuccessfully() {

        LoginRequest request = new LoginRequest();
        request.setEmail("test@test.com");
        request.setPassword("password");

        User user = new User();
        user.setId(12L);
        user.setFullName("Shami");
        user.setEmail("test@test.com");
        user.setPassword("encoded-password");
        user.setRole(Role.MEMBER);

        when(authenticationManager.authenticate(any(
                UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);

        when(repository.findByEmail("test@test.com"))
                .thenReturn(Optional.of(user));

        when(jwtService.generateToken(
                "test@test.com",
                "MEMBER"))
                .thenReturn("jwt-token");

        LoginResponse response = userService.login(request);

        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals("Bearer", response.getType());
        assertEquals(3600000L, response.getExpiresIn());

        verify(authenticationManager, times(1))
                .authenticate(any(UsernamePasswordAuthenticationToken.class));

        verify(repository, times(1))
                .findByEmail("test@test.com");

        verify(jwtService, times(1))
                .generateToken("test@test.com", "MEMBER");
    }

    @Test
    void shouldGetCurrentUserSuccessfully() {

        // Arrange
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        when(authentication.getName())
                .thenReturn("tejas123@test.com");

        when(securityContext.getAuthentication())
                .thenReturn(authentication);

        SecurityContextHolder.setContext(securityContext);

        User user = new User();

        user.setId(1L);
        user.setFullName("Tejass");
        user.setEmail("tejas123@test.com");
        user.setPhone("1234567899");
        user.setRole(Role.ADMIN);

        when(repository.findByEmail("tejas123@test.com"))
                .thenReturn(Optional.of(user));

        // Act
        UserResponse response = userService.getCurrentUser();

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Tejass", response.getFullName());
        assertEquals("tejas123@test.com", response.getEmail());
        assertEquals("1234567899", response.getPhone());
        assertEquals("ADMIN", response.getRole());

        verify(repository)
                .findByEmail("tejas123@test.com");

        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldUpdateProfileSuccessfully() {

        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        when(authentication.getName())
                .thenReturn("shreyas@test.com");

        when(securityContext.getAuthentication())
                .thenReturn(authentication);

        SecurityContextHolder.setContext(securityContext);

        UpdateUserRequest request = new UpdateUserRequest();

        request.setFullName("Shreyas N");
        request.setPhone("9999999999");

        User user = new User();

        user.setId(1L);
        user.setFullName("Old Name");
        user.setEmail("shreyas@test.com");
        user.setPhone("8888888888");
        user.setRole(Role.ADMIN);

        User updatedUser = new User();

        updatedUser.setId(1L);
        updatedUser.setFullName("Shreyas N");
        updatedUser.setEmail("shreyas@test.com");
        updatedUser.setPhone("9999999999");
        updatedUser.setRole(Role.ADMIN);

        when(repository.findByEmail("shreyas@test.com"))
                .thenReturn(Optional.of(user));

        when(repository.save(any(User.class)))
                .thenReturn(updatedUser);

        UserResponse response =
                userService.updateProfile(request);

        assertNotNull(response);

        assertEquals("Shreyas N",
                response.getFullName());

        assertEquals("9999999999",
                response.getPhone());

        assertEquals("shreyas@test.com",
                response.getEmail());

        assertEquals("ADMIN",
                response.getRole());

        verify(repository)
                .findByEmail("shreyas@test.com");

        verify(repository)
                .save(any(User.class));

        SecurityContextHolder.clearContext();

    }
    @Test
    void shouldChangePasswordSuccessfully() {

        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        when(authentication.getName()).thenReturn("shreyas@test.com");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("oldPassword");
        request.setNewPassword("newPassword");
        request.setConfirmPassword("newPassword");

        User user = new User();
        user.setId(1L);
        user.setEmail("shreyas@test.com");
        user.setPassword("encodedOldPassword");
        user.setRole(Role.ADMIN);

        when(repository.findByEmail("shreyas@test.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "oldPassword",
                "encodedOldPassword"))
                .thenReturn(true);

        when(passwordEncoder.encode("newPassword"))
                .thenReturn("encodedNewPassword");

        when(repository.save(any(User.class)))
                .thenReturn(user);

        MessageResponse response =
                userService.changePassword(request);

        assertNotNull(response);
        assertEquals(
                "Password changed successfully",
                response.getMessage()
        );

        ArgumentCaptor<User> captor =
                ArgumentCaptor.forClass(User.class);

        verify(repository).save(captor.capture());

        User savedUser = captor.getValue();

        assertEquals(
                "encodedNewPassword",
                savedUser.getPassword()
        );

        verify(passwordEncoder)
                .matches("oldPassword", "encodedOldPassword");

        verify(passwordEncoder)
                .encode("newPassword");

        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldThrowExceptionWhenCurrentPasswordIsIncorrect() {

        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        when(authentication.getName()).thenReturn("shreyas@test.com");
        when(securityContext.getAuthentication()).thenReturn(authentication);

        SecurityContextHolder.setContext(securityContext);

        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("wrongPassword");
        request.setNewPassword("newPassword");
        request.setConfirmPassword("newPassword");

        User user = new User();
        user.setEmail("shreyas@test.com");
        user.setPassword("encodedOldPassword");

        when(repository.findByEmail("shreyas@test.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "wrongPassword",
                "encodedOldPassword"))
                .thenReturn(false);

        assertThrows(
                InvalidPasswordException.class,
                () -> userService.changePassword(request)
        );

        verify(repository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(anyString());

        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldThrowExceptionWhenPasswordsDoNotMatch() {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        when(authentication.getName()).thenReturn("shreyas@test.com");
        when(securityContext.getAuthentication()).thenReturn(authentication);

        SecurityContextHolder.setContext(securityContext);

        ChangePasswordRequest request = new ChangePasswordRequest();

        request.setCurrentPassword("oldPassword");
        request.setNewPassword("newPassword123");
        request.setConfirmPassword("differentPassword");

        User user = new User();

        user.setEmail("shreyas@test.com");
        user.setPassword("encodedOldPassword");

        when(repository.findByEmail("shreyas@test.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "oldPassword",
                "encodedOldPassword"))
                .thenReturn(true);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> userService.changePassword(request)
        );

        assertEquals(
                "New password and confirm password do not match",
                exception.getMessage()
        );

        verify(repository, never()).save(any(User.class));

        verify(passwordEncoder, never()).encode(anyString());

        SecurityContextHolder.clearContext();

    }

    @Test
    void shouldUploadProfileImageSuccessfully() {

        // Arrange
        MultipartFile file = mock(MultipartFile.class);

        when(file.isEmpty()).thenReturn(false);

        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        when(authentication.getName()).thenReturn("shreyas@test.com");
        when(securityContext.getAuthentication()).thenReturn(authentication);

        SecurityContextHolder.setContext(securityContext);

        User user = new User();
        user.setId(1L);
        user.setFullName("Shreyas");
        user.setEmail("shreyas@test.com");
        user.setRole(Role.ADMIN);
        user.setProfileImageUrl("https://bucket.s3.amazonaws.com/profile/old.png");

        when(repository.findByEmail("shreyas@test.com"))
                .thenReturn(Optional.of(user));

        when(s3Service.extractKeyFromUrl(user.getProfileImageUrl()))
                .thenReturn("profile/old.png");

        when(s3Service.uploadFile(file))
                .thenReturn("https://bucket.s3.amazonaws.com/profile/new.png");

        when(repository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        UserResponse response = userService.uploadProfileImage(file);

        // Assert
        assertNotNull(response);

        assertEquals(
                "https://bucket.s3.amazonaws.com/profile/new.png",
                response.getProfileImageUrl()
        );

        // Verify S3 interactions
        verify(s3Service)
                .extractKeyFromUrl("https://bucket.s3.amazonaws.com/profile/old.png");

        verify(s3Service)
                .deleteFile("profile/old.png");

        verify(s3Service)
                .uploadFile(file);

        // Verify database save
        ArgumentCaptor<User> userCaptor =
                ArgumentCaptor.forClass(User.class);

        verify(repository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();

        assertEquals(
                "https://bucket.s3.amazonaws.com/profile/new.png",
                savedUser.getProfileImageUrl()
        );

        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldThrowExceptionWhenFileIsEmpty() {

        // Arrange
        MultipartFile file = mock(MultipartFile.class);

        when(file.isEmpty()).thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> userService.uploadProfileImage(file)
        );

        assertEquals(
                "Please select an image to upload.",
                exception.getMessage()
        );

        // Verify nothing else happened
        verify(repository, never()).findByEmail(anyString());

        verify(repository, never()).save(any(User.class));

        verify(s3Service, never()).uploadFile(any(MultipartFile.class));

        verify(s3Service, never()).deleteFile(anyString());

        verify(s3Service, never()).extractKeyFromUrl(anyString());
    }

    @Test
    void shouldThrowExceptionWhenUserNotFoundDuringImageUpload() {

        // Arrange
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);

        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        when(authentication.getName()).thenReturn("shreyas@test.com");
        when(securityContext.getAuthentication()).thenReturn(authentication);

        SecurityContextHolder.setContext(securityContext);

        when(repository.findByEmail("shreyas@test.com"))
                .thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> userService.uploadProfileImage(file)
        );

        assertEquals(
                "User not found",
                exception.getMessage()
        );

        // Verify repository
        verify(repository).findByEmail("shreyas@test.com");
        verify(repository, never()).save(any(User.class));

        // Verify S3 interactions never happened
        verify(s3Service, never()).uploadFile(any(MultipartFile.class));
        verify(s3Service, never()).deleteFile(anyString());
        verify(s3Service, never()).extractKeyFromUrl(anyString());

        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldUploadProfileImageWhenNoPreviousImageExists() {

        // Arrange
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);

        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        when(authentication.getName()).thenReturn("shreyas@test.com");
        when(securityContext.getAuthentication()).thenReturn(authentication);

        SecurityContextHolder.setContext(securityContext);

        User user = new User();
        user.setId(1L);
        user.setFullName("Shreyas");
        user.setEmail("shreyas@test.com");
        user.setRole(Role.ADMIN);

        // No previous image
        user.setProfileImageUrl(null);

        when(repository.findByEmail("shreyas@test.com"))
                .thenReturn(Optional.of(user));

        when(s3Service.uploadFile(file))
                .thenReturn("https://bucket.s3.amazonaws.com/profile/new.png");

        when(repository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        UserResponse response = userService.uploadProfileImage(file);

        // Assert
        assertNotNull(response);

        assertEquals(
                "https://bucket.s3.amazonaws.com/profile/new.png",
                response.getProfileImageUrl()
        );

        // Since there is NO previous image
        verify(s3Service, never())
                .extractKeyFromUrl(anyString());

        verify(s3Service, never())
                .deleteFile(anyString());

        verify(s3Service)
                .uploadFile(file);

        verify(repository)
                .save(any(User.class));

        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldExecuteS3OperationsInCorrectOrder() {

        // Arrange
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);

        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        when(authentication.getName()).thenReturn("shreyas@test.com");
        when(securityContext.getAuthentication()).thenReturn(authentication);

        SecurityContextHolder.setContext(securityContext);

        User user = new User();
        user.setId(1L);
        user.setEmail("shreyas@test.com");
        user.setProfileImageUrl("https://bucket.s3.amazonaws.com/profile/old.png");

        when(repository.findByEmail("shreyas@test.com"))
                .thenReturn(Optional.of(user));

        when(s3Service.extractKeyFromUrl(user.getProfileImageUrl()))
                .thenReturn("profile/old.png");

        when(s3Service.uploadFile(file))
                .thenReturn("https://bucket.s3.amazonaws.com/profile/new.png");

        when(repository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        userService.uploadProfileImage(file);

        // Assert
        InOrder inOrder = inOrder(s3Service);

        inOrder.verify(s3Service)
                .extractKeyFromUrl(anyString());

        inOrder.verify(s3Service)
                .deleteFile(anyString());

        inOrder.verify(s3Service)
                .uploadFile(file);

        SecurityContextHolder.clearContext();
    }


}