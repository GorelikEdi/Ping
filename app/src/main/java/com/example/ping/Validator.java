package com.example.ping;

import com.google.android.material.textfield.TextInputLayout;
import java.util.Objects;

public class Validator {

    public static boolean validateUsername(TextInputLayout usernameTIL, String text){
        String value = Objects.requireNonNull(usernameTIL.getEditText()).getText().toString();
        String usernameRegex = "^[a-zA-Z0-9]+([._-]?[a-zA-Z0-9]+)*$";
        if (value.isEmpty()) {
            usernameTIL.setErrorEnabled(true);
            usernameTIL.setError(text + " is required");
            return false;
        }
        else if (!value.matches(usernameRegex) || 5 > value.length()|| value.length() > 15 ){
            usernameTIL.setErrorEnabled(true);
            usernameTIL.setError(
                    "Invalid " + text + ":" +
                    "\n\u2022 English letters allowed" + // \u2022 - bullet point
                    "\n\u2022 Digits allowed" +
                    "\n\u2022 Special character allowed: -_." +
                    "\n\u2022 Min length 5" +
                    "\n\u2022 Max length 15" +
                    "\n\u2022 " + text + " begins and ends with a letter or digit"
            );
            return false;
        }
        else {
            usernameTIL.setError(null);
            usernameTIL.setErrorEnabled(false);
            return true;
        }
    }

    public static boolean validateEmail(TextInputLayout emailTIL){
        String value = Objects.requireNonNull(emailTIL.getEditText()).getText().toString();
        String emailRegex = "^[a-zA-Z0-9]+([._-]?[a-zA-Z0-9]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        if (value.isEmpty()) {
            emailTIL.setErrorEnabled(true);
            emailTIL.setError("Email address is required");
            return false;
        }
        else if (!value.matches(emailRegex)){
            emailTIL.setErrorEnabled(true);
            emailTIL.setError("Invalid email address");
            return false;
        }
        else {
            emailTIL.setError(null);
            emailTIL.setErrorEnabled(false);
            return true;
        }
    }

    public static boolean validatePassword(TextInputLayout passwordTIL){
        String value = Objects.requireNonNull(passwordTIL.getEditText()).getText().toString();
        String passRegex = "^"
                + ".{6,}" // length at least 6 characters
                + "$";
        if (value.isEmpty()) {
            passwordTIL.setErrorEnabled(true);
            passwordTIL.setError("Password is required");
            return false;
        }
        else if (!value.matches(passRegex)){
            passwordTIL.setErrorEnabled(true);
            passwordTIL.setError(
                    "Password is too weak:" +
                    "\n\u2022 Length at least 6 characters"
            );
            return false;
        }
        else {
            passwordTIL.setError(null);
            passwordTIL.setErrorEnabled(false);
            return true;
        }
    }

    public static boolean validateConfirmPassword(TextInputLayout passwordTIL,
                                                  TextInputLayout confirmPasswordTIL) {

        String value = Objects.requireNonNull(passwordTIL.getEditText()).getText().toString();
        String confirmValue = Objects.requireNonNull(confirmPasswordTIL.getEditText()).getText().toString();

        if (confirmValue.isEmpty()) {
            confirmPasswordTIL.setErrorEnabled(true);
            confirmPasswordTIL.setError("Password confirmation is required");
            return false;
        }
        else if (!confirmValue.equals(value)) {
            confirmPasswordTIL.setErrorEnabled(true);
            confirmPasswordTIL.setError("Invalid confirmation password");
            return false;
        }
        else {
            confirmPasswordTIL.setError(null);
            confirmPasswordTIL.setErrorEnabled(false);
            return true;
        }
    }

    public static boolean validateAll(TextInputLayout emailTIL,
                                      TextInputLayout passwordTIL,
                                      TextInputLayout confirmPasswordTIL,
                                      TextInputLayout usernameTIL) {

        if (confirmPasswordTIL == null)
            return validateEmail(emailTIL) & validatePassword(passwordTIL);
        else if (emailTIL.getError() != null) {
            if (usernameTIL.getError() != null)
                return validatePassword(passwordTIL)
                        & validateConfirmPassword(passwordTIL, confirmPasswordTIL);
            else
                return validatePassword(passwordTIL)
                        & validateUsername(usernameTIL, "Username")
                        & validateConfirmPassword(passwordTIL, confirmPasswordTIL);
        }
        else if (usernameTIL.getError() != null)
            return validateEmail(emailTIL)
                    & validatePassword(passwordTIL)
                    & validateConfirmPassword(passwordTIL, confirmPasswordTIL);
        else
            return validateEmail(emailTIL)
                    & validatePassword(passwordTIL)
                    & validateUsername(usernameTIL, "Username")
                    & validateConfirmPassword(passwordTIL, confirmPasswordTIL);

    }
}
