package com.apparriendos.arriendos_api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

// Levanta el contexto completo sin conectarse a la BD (Hikari conecta recien al primer uso).
@SpringBootTest(properties = {
        "spring.config.import=",
        "DB_PASSWORD=sin-uso-en-test",
        "JWT_SECRET=clave-de-prueba-de-al-menos-32-caracteres!!"
})
class ArriendosApiApplicationTests {

	@Test
	void contextLoads() {
	}

}
