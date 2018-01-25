package fr.xebia.ldi.fighter.stream.utils;

import com.typesafe.config.Config;
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.streams.serdes.avro.GenericAvroSerde;
import org.apache.kafka.streams.StreamsConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Created by loicmdivad.
 */
public class JobConfig {

    public static final Properties properties(Config config){
        Properties prop = new Properties();
        prop.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, config.getString("kafka-clients.bootstrap.servers"));
        prop.put(StreamsConfig.APPLICATION_ID_CONFIG, config.getString("applicationid"));
        prop.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, GenericAvroSerde.class);
        prop.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, GenericAvroSerde.class);
        prop.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 100);
        prop.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG,
                config.getString("kafka-clients.schema.registry.url"));
        return prop;
    }

    public static final Map<String, String> mapProperties(Config config){
        Map<String, String> map = new HashMap<String, String>();
        Properties props = properties(config);
        for (String key : props.stringPropertyNames()) {
            map.put(key, props.getProperty(key));
        }
        return map;
    }

    public static final GenericAvroSerde configureAvroSerde(Config config){
        GenericAvroSerde serde = new GenericAvroSerde();
        Map<String, String> map = new HashMap<String, String>();
        map.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, config.getString("kafka-clients"));
        serde.configure(map, false);
        return serde;
    }

}
