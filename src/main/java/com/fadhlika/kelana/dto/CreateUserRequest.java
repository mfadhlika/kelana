package com.fadhlika.kelana.dto;

import com.fadhlika.kelana.exception.BadRequestException;

public record CreateUserRequest(String username, String password) {

    public void validate() {

        if (username == null || password == null || username.isBlank() || password.isBlank()) {
            throw new BadRequestException("username or password can't be empty");
        }
    }

}
