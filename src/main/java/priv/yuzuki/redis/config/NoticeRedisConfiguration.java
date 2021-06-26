package priv.yuzuki.redis.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @program:
 * @author: wangzibai01
 * @create: 2021-06-24 14:59
 * @description: 读取配置类
 **/
@Configuration
@ConfigurationProperties("stream.redis")
@Data
public class NoticeRedisConfiguration {
	private String host;
	private Integer port;
	private String password;
	private Integer database;
	private Integer timeout;
	private Integer maxActive;
	private Integer maxIdle;
	private Integer minIdle;
	private Integer maxWait;

}
