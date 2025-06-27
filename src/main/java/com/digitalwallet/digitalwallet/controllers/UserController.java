package com.digitalwallet.digitalwallet.controllers;

import com.digitalwallet.digitalwallet.exceptions.InvalidUserException;
import com.digitalwallet.digitalwallet.models.DigitalWalletResponse;
import com.digitalwallet.digitalwallet.models.ErrorDTO;
import com.digitalwallet.digitalwallet.models.User;
import com.digitalwallet.digitalwallet.models.UserCreateRequest;
import com.digitalwallet.digitalwallet.services.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/users")
@Slf4j
public class UserController {

    private final IUserService userService;

    public UserController(IUserService userService) {
        this.userService = userService;
    }

    @PostMapping("/add")
    public DigitalWalletResponse<User> addUser(@RequestBody UserCreateRequest userCreateRequest) {
        try {
            User created = userService.add(new User(userCreateRequest.getEmail(), userCreateRequest.getPhone()));
            return DigitalWalletResponse.<User>builder()
                    .response(created)
                    .build();
        } catch (Exception e) {
            log.error("Error while adding user", e);
            return DigitalWalletResponse.<User>builder()
                    .status(false)
                    .errorDTO(new ErrorDTO(e.getMessage()))
                    .build();
        }
    }

    @GetMapping("/get")
    public DigitalWalletResponse<User> getUser(@RequestParam("id") Long userId) {
        try {
            User user = userService.get(userId);
            return DigitalWalletResponse.<User>builder()
                    .response(user)
                    .build();
        } catch (InvalidUserException e) {
            return DigitalWalletResponse.<User>builder()
                    .status(false)
                    .errorDTO(new ErrorDTO(e.getMessage()))
                    .build();
        }
    }
}
