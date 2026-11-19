package com.ilque;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class IlqueBeApplicationTests {

	@Autowired
	private Environment environment;

	@Test
	void contextLoads() {
	}

	@Test
	void useTestDB() {
		String datasourceUrl = environment.getProperty("spring.datasource.url");

		assertThat(datasourceUrl).isNotNull();
		assertThat(datasourceUrl).contains("ilque-test.db");
		assertThat(datasourceUrl).doesNotContain("./data/ilque.db");
	}

}
