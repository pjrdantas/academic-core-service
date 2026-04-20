package br.com.escola.academic;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"spring.docker.compose.enabled=false",
		"spring.kafka.listener.auto-startup=false"
})
class AcademicCoreServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
