package com.fintrack.service.impl;

import com.fintrack.dto.*;
import com.fintrack.entity.Role;
import com.fintrack.entity.User;
import com.fintrack.exception.BadRequestException;
import com.fintrack.exception.ResourceNotFoundException;
import com.fintrack.exception.UnauthorizedException;
import com.fintrack.repository.RoleRepository;
import com.fintrack.repository.UserRepository;
import com.fintrack.security.JwtTokenProvider;
import com.fintrack.security.UserPrincipal;
import com.fintrack.service.AuditLogService;
import com.fintrack.service.EmailService;
import com.fintrack.service.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private EmailService emailService;

    @Override
    @Transactional
    public JwtAuthenticationResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsernameOrEmail(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);
        String refreshJwt = tokenProvider.generateRefreshToken(authentication);

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<String> roles = userPrincipal.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        auditLogService.logAction(user, "LOGIN", "Successfully logged in", "0.0.0.0");

        return new JwtAuthenticationResponse(jwt, refreshJwt, userPrincipal.getId(), userPrincipal.getUsername(), userPrincipal.getEmail(), roles);
    }

    @Override
    @Transactional
    public UserProfileResponse signup(SignupRequest signupRequest) {
        if (userRepository.existsByUsername(signupRequest.getUsername())) {
            throw new BadRequestException("Username is already taken!");
        }

        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            throw new BadRequestException("Email Address already in use!");
        }

        User user = new User();
        user.setUsername(signupRequest.getUsername());
        user.setEmail(signupRequest.getEmail());
        user.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
        user.setFullName(signupRequest.getFullName());
        user.setActive(true);

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setName("ROLE_USER");
                    return roleRepository.save(role);
                });

        user.setRoles(new HashSet<>(Collections.singletonList(userRole)));
        User result = userRepository.save(user);

        auditLogService.logAction(result, "SIGNUP", "Account registered successfully", "0.0.0.0");

        sendWelcomeEmail(result);

        return modelMapper.map(result, UserProfileResponse.class);
    }

    @Override
    public UserProfileResponse getProfile(User currentUser) {
        return modelMapper.map(currentUser, UserProfileResponse.class);
    }

    @Override
    @Transactional
    public UserProfileResponse updateProfile(User currentUser, UpdateProfileRequest request) {
        if (!currentUser.getEmail().equalsIgnoreCase(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email Address already in use!");
        }

        currentUser.setFullName(request.getFullName());
        currentUser.setEmail(request.getEmail());
        User updatedUser = userRepository.save(currentUser);

        auditLogService.logAction(updatedUser, "UPDATE_PROFILE", "Updated profile metadata", "0.0.0.0");

        return modelMapper.map(updatedUser, UserProfileResponse.class);
    }

    @Override
    @Transactional
    public void changePassword(User currentUser, ChangePasswordRequest request) {
        if (!passwordEncoder.matches(request.getOldPassword(), currentUser.getPassword())) {
            throw new BadRequestException("Old password does not match");
        }

        currentUser.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(currentUser);

        auditLogService.logAction(currentUser, "CHANGE_PASSWORD", "Password modified successfully", "0.0.0.0");
    }

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email " + request.getEmail()));

        String tempPass = "TempPass123";
        user.setPassword(passwordEncoder.encode(tempPass));
        userRepository.save(user);

        auditLogService.logAction(user, "FORGOT_PASSWORD", "Reset password request. Password reset to TempPass123", "0.0.0.0");

        sendPasswordResetEmail(user, tempPass);
    }

    @Override
    @Transactional
    public void deactivateAccount(User currentUser) {
        currentUser.setActive(false);
        userRepository.save(currentUser);
        auditLogService.logAction(currentUser, "DEACTIVATE", "Account deactivated (soft delete)", "0.0.0.0");
    }

    private void sendWelcomeEmail(User user) {
        String subject = "Welcome to FinTrack!";
        String body = String.format("Hello %s,\n\n" +
                "Thank you for registering on FinTrack – Personal Finance Dashboard!\n" +
                "Your account (username: %s) is now active and ready.\n\n" +
                "Take control of your financial destiny today!\n\n" +
                "Best regards,\n" +
                "The FinTrack Team", 
                user.getFullName(), user.getUsername());
        emailService.sendEmail(user.getEmail(), subject, body);
    }

    private void sendPasswordResetEmail(User user, String tempPassword) {
        String subject = "FinTrack - Password Reset Request";
        String body = String.format("Hello %s,\n\n" +
                "We received a request to reset your FinTrack password.\n" +
                "Your password has been temporarily reset to: %s\n\n" +
                "Please log in using this temporary password and change it immediately from your profile settings for security.\n\n" +
                "Best regards,\n" +
                "The FinTrack Team", 
                user.getFullName(), tempPassword);
        emailService.sendEmail(user.getEmail(), subject, body);
    }
}
