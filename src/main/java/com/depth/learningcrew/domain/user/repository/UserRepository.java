package com.depth.learningcrew.domain.user.repository;

import com.depth.learningcrew.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {

}
