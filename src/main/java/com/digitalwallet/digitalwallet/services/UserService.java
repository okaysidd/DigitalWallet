package com.digitalwallet.digitalwallet.services;

import com.digitalwallet.digitalwallet.exceptions.InvalidUserException;
import com.digitalwallet.digitalwallet.models.User;
import com.digitalwallet.digitalwallet.repositories.IUserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService implements IUserService {

    private final IUserRepository userRepository;

    public UserService(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User get(Long userId) throws InvalidUserException {
        if (userId == null)
            throw new InvalidUserException("Invalid user exception");

        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty())
            throw new InvalidUserException("User not found exception");

        return user.get();
    }

    @Override
    public User add(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        return userRepository.save(user);
    }
}
