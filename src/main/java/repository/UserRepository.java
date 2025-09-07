package repository;


import com.secretnote.entity.User;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository {

    Optional<User> findByUsername(String username);
    boolean existsByEmail(String email);



}
