package com.digitalwallet.digitalwallet.services;

import com.digitalwallet.digitalwallet.exceptions.InvalidUserException;
import com.digitalwallet.digitalwallet.models.User;

public interface IUserService {
    User get(Long userId) throws InvalidUserException;

    User add(User user);
}
