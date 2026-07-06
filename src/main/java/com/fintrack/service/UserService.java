package com.fintrack.service;

import com.fintrack.dto.*;
import com.fintrack.entity.User;

public interface UserService {
    JwtAuthenticationResponse login(LoginRequest loginRequest);
    UserProfileResponse signup(SignupRequest signupRequest);
    UserProfileResponse getProfile(User currentUser);
    UserProfileResponse updateProfile(User currentUser, UpdateProfileRequest updateProfileRequest);
    void changePassword(User currentUser, ChangePasswordRequest changePasswordRequest);
    void forgotPassword(ForgotPasswordRequest forgotPasswordRequest);
    void deactivateAccount(User currentUser);
}
