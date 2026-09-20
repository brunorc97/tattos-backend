package com.tattos;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class TattosApplicationTests {

	@Test
	void contextLoads() {
		org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder encoder = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
		System.out.println("HASH_ADMIN: " + encoder.encode("admin123"));
		System.out.println("HASH_DEMO: " + encoder.encode("demo123"));
		System.out.println("HASH_RODRIGO: " + encoder.encode("rodrigo123"));
		System.out.println("HASH_RECEPCAO: " + encoder.encode("recepcao123"));
	}

}
