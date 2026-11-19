package com.campusbite.users;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List; import java.util.Optional;
public interface UserRepository extends MongoRepository<AppUser,String>{
 Optional<AppUser> findByEmail(String email); boolean existsByEmail(String email);
 List<AppUser> findByRoleInOrderByCreatedAtDesc(List<Role> roles);
 long countByRole(Role role);
}
