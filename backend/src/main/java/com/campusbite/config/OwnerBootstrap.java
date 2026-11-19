package com.campusbite.config;
import com.campusbite.users.*; import org.springframework.beans.factory.annotation.Value; import org.springframework.boot.ApplicationRunner; import org.springframework.context.annotation.Bean; import org.springframework.context.annotation.Configuration; import org.springframework.security.crypto.password.PasswordEncoder;
@Configuration public class OwnerBootstrap {
 @Bean ApplicationRunner createInitialOwner(UserRepository users,PasswordEncoder encoder,@Value("${campusbite.bootstrap.owner-email:}") String email,@Value("${campusbite.bootstrap.owner-password:}") String password,@Value("${campusbite.bootstrap.owner-name:CampusBite Owner}") String name){return args->{if(email!=null&&!email.isBlank()&&password!=null&&!password.isBlank()&&!users.existsByEmail(email.trim().toLowerCase()))users.save(new AppUser(name,email.trim().toLowerCase(),encoder.encode(password),Role.OWNER));};}
}
