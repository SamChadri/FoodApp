package com.example.foodapp.loginHelper;

import androidx.annotation.Nullable;

/**
 * Data validation state of the login form.
 */
class LoginFormState {
    @Nullable
    private String usernameError;
    @Nullable
    private String passwordError;
    private boolean isDataValid;

    LoginFormState(@Nullable String usernameError, @Nullable String passwordError) {
        this.usernameError = usernameError;
        this.passwordError = passwordError;
        this.isDataValid = false;
    }

    LoginFormState(boolean isDataValid) {
        this.usernameError = null;
        this.passwordError = null;
        this.isDataValid = isDataValid;
    }

    @Nullable
    String getUsernameError() {
        return usernameError;
    }

    @Nullable
    String getPasswordError() {
        return passwordError;
    }

    boolean isDataValid() {
        return isDataValid;
    }
}
