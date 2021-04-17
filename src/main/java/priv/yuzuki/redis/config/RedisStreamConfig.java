package priv.yuzuki.redis.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @program: redis
 * @author: wangzibai01
 * @create: 2021-04-15 17:36
 * @description: redis stream 配置
 **/
@Component
@ConfigurationProperties(prefix = "stream")
@Data
public class RedisStreamConfig {
	private String stream;
	private String consumerGroup;
	private String consumer;
	private Integer queueSize;
}
