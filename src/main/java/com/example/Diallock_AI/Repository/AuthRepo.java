package com.example.Diallock_AI.Repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.Diallock_AI.model.User;

public interface AuthRepo extends JpaRepository<User, Integer>{
	Optional<User> findByEmail(String email);
	
}
