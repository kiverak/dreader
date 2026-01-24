package dreadernewsparser.config;

import dto.ArticleDto;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class KafkaProducerConfig {

    private final KafkaProperties kafkaProperties;
    private final ObjectProvider<SslBundles> sslBundlesProvider;

    @Value("${kafka.topics.articlesTopic}")
    private String articlesTopic;

    @Value("${kafka.topics.dlqTopic}")
    private String dlqTopic;

    @Bean
    public ProducerFactory<String, ArticleDto> producerFactory() {
        Map<String, Object> props = new HashMap<>();

        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, String.join(",", kafkaProperties.getBootstrapServers()));

        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        // Delivery guarantee
        props.put(ProducerConfig.ACKS_CONFIG, "all");

        // Idempotent producer
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 1);
        props.put(ProducerConfig.RETRIES_CONFIG, 10);

        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, ArticleDto> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    public KafkaAdmin kafkaAdmin() {
        return new KafkaAdmin(kafkaProperties.buildAdminProperties(sslBundlesProvider.getIfAvailable()));
    }

    @Bean
    public NewTopic articleTopic() {
        return TopicBuilder.name(articlesTopic)
                .partitions(3)
                .replicas(3)
                .config("retention.ms", "86400000") // 1 day
                .config("cleanup.policy", "delete")
                .build();
    }

    @Bean
    public NewTopic dlqTopic() {
        return TopicBuilder.name(dlqTopic)
                .partitions(3)
                .replicas(3)
                .build();
    }
}
