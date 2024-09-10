package com.example.transacciones;

import com.example.transacciones.models.UserEntity;
import com.example.transacciones.repositories.UserRepository;
import com.example.transacciones.services.RoleService;
import org.apache.logging.log4j.LogManager;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.math.BigDecimal;
import java.util.Set;

import org.apache.logging.log4j.Logger;

@SpringBootApplication
public class TransaccionesApplication {
	private static final Logger logger = LogManager.getLogger(TransaccionesApplication.class);
	public static void main(String[] args) {
		SpringApplication.run(TransaccionesApplication.class, args);
	}
	@Bean
	CommandLineRunner init(UserRepository userRepository, RoleService role){
		return args -> {
			//USUARIO POR DEFECTO
			UserEntity userAdmin=UserEntity.builder()
					.cedula("0123456789")
					.password(new BCryptPasswordEncoder().encode("admin123"))
					.balance(BigDecimal.valueOf(1000000))
					.isEnable(true)
					.accountNoExpired(true)
					.accountNoLocked(true)
					.credentialNoExpired(true)
					.roles(Set.of(role.admin(),role.user())).build();
			userRepository.save(userAdmin);
		};

}
}
