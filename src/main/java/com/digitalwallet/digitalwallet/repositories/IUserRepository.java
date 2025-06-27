package com.digitalwallet.digitalwallet.repositories;

import com.digitalwallet.digitalwallet.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

@Service
public interface IUserRepository extends JpaRepository<User, Long> {
}
