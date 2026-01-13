package com.fadhlika.kelana;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import com.fadhlika.kelana.KelanaApplication;

@SpringBootTest(classes = KelanaApplication.class)
@TestPropertySource(locations = "classpath:test.properties")
class KelanaApplicationTests {

}
