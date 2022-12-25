package com.sg.authentication.repository;

import com.sg.authentication.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    @EntityGraph(attributePaths = "authorities")
    Optional<User> findOneAuthoritiesByUsername(String username);
    User findByEmail(String email);

}

