package com.example.Diallock_AI.Service;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.example.Diallock_AI.model.MyUserDetails;

@Service
public class UserService {

    public Integer getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) principal;
            return ((MyUserDetails) userDetails).getUserId(); // Corrected line
        } else if (principal instanceof String) {
            String username = (String) principal;
            return getUserIdFromUsername(username);
        }
        return null;
    }

    private Integer getUserIdFromUsername(String username) {
        //  Implement logic to retrieve the user ID from the database based on the username.
        //  This is just a placeholder.  Replace with your actual database access code.
        //  Example (using a hypothetical UserRepository):
        //  User user = userRepository.findByUsername(username);
        //  if (user != null) {
        //      return user.getId();
        //  }
        return null;
    }
}