package com.enterprise.s3browser;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "s3.endpoint=http://localhost:9000",
    "s3.access-key=testkey",
    "s3.secret-key=testsecret",
    "s3.bucket=test-bucket",
    "s3.region=us-east-1",
    "s3.path-style-access=true",
    "jwt.secret=testSecretKeyForJwtTokenGeneration123456789",
    "jwt.expiration=86400000",
    "app.auth.username=admin",
    "app.auth.password=admin"
})
class S3BrowserApplicationTest {

    @Test
    void contextLoads() {
        // This test ensures that the Spring application context loads successfully
    }
}