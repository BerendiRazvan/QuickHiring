package repository.userRepository;

import domain.User;
import lombok.NonNull;

import java.util.Optional;

public interface UserRepository extends repository.Repository<Long, User> {
    Optional<User> findByMail(@NonNull String mail);
}
