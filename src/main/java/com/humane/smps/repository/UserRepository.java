package com.humane.smps.repository;

import com.humane.smps.model.User;
import com.humane.util.spring.data.QueryDslJpaExtendRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends QueryDslJpaExtendRepository<User, String> {
}