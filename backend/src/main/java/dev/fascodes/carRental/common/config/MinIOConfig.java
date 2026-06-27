package dev.fascodes.carRental.common.config;

import io.minio.MinioClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinIOConfig {
    @Bean
    public MinioClient minIoClient(MinIOProperties minIOProperties){
        return MinioClient.builder().
                endpoint(minIOProperties.getEndpoint()).
                credentials(
                        minIOProperties.getAccessKey(),
                        minIOProperties.getSecretKey()
                ).
                build();
    }
}
