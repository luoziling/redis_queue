package priv.yuzuki.redis.config;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;

import javax.annotation.Resource;

/**
 * 自定义redis配置类
 *
 * @author
 */
@Configuration
public class NoticeRedisConfig {
    @Resource
    private NoticeRedisConfiguration noticeRedisConfiguration;


    /**
     * 配置lettuce连接池
     * Redis 连接池: Lettuce、Jedis 比较
     * Jedis:
     * Jedis在实现上是直接连接的redis server，如果在多线程环境下是非线程安全的，这个时候只有使用连接池，为每个Jedis实例增加物理连接
     * Lettuce:
     * Lettuce的连接是基于Netty的，连接实例（StatefulRedisConnection）可以在多个线程间并发访问，
     * 应为StatefulRedisConnection是线程安全的，所以一个连接实例（StatefulRedisConnection）就可以满足多线程环境下的并发访问，
     * 当然这个也是可伸缩的设计，一个连接实例不够的情况也可以按需增加连接实例。
     *
     * @return
     */
    @Bean
    public GenericObjectPoolConfig redisPool() {
        GenericObjectPoolConfig<Object> genericObjectPoolConfig = new GenericObjectPoolConfig<>();
        genericObjectPoolConfig.setMaxWaitMillis(noticeRedisConfiguration.getMaxWait());
        genericObjectPoolConfig.setMaxTotal(noticeRedisConfiguration.getMaxActive());
        genericObjectPoolConfig.setMaxIdle(noticeRedisConfiguration.getMaxIdle());
        genericObjectPoolConfig.setMinIdle(noticeRedisConfiguration.getMinIdle());
        return new GenericObjectPoolConfig<>();
    }

    /**
     * 配置第一个数据源的
     *
     * @return
     */
    @Bean("redisConfigMaster1")
    public RedisStandaloneConfiguration redisConfigMaster() {
        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration(noticeRedisConfiguration.getHost(), noticeRedisConfiguration.getPort());
        redisStandaloneConfiguration.setDatabase(noticeRedisConfiguration.getDatabase());
        redisStandaloneConfiguration.setPassword(RedisPassword.of(noticeRedisConfiguration.getPassword()));
        return redisStandaloneConfiguration;
    }

    /**
     * 配置第一个数据源的连接工厂
     * 这里注意：需要添加@Primary 指定bean的名称，目的是为了创建两个不同名称的LettuceConnectionFactory
     *
     * @param config
     * @param redisConfigMaster
     * @return
     */
    @Bean("factory1")
    public LettuceConnectionFactory factory(GenericObjectPoolConfig config, @Qualifier("redisConfigMaster1") RedisStandaloneConfiguration redisConfigMaster) {
        LettuceClientConfiguration clientConfiguration = LettucePoolingClientConfiguration.builder().poolConfig(redisPool()).build();
        return new LettuceConnectionFactory(redisConfigMaster, clientConfiguration);
    }

    /**
     * 配置第一个数据源的RedisTemplate
     * 注意：这里指定使用名称=factory 的 RedisConnectionFactory
     * 并且标识第一个数据源是默认数据源 @Primary
     *
     * @param factory
     * @return
     */
    @Bean("noticeStringRedisTemplate")
    public StringRedisTemplate redisTemplate(@Qualifier("factory1") RedisConnectionFactory factory) {
        return getStringStringRedisTemplate(factory);
    }

    /**
     * 设置序列化方式 （这一步不是必须的）
     *
     * @param factory
     * @return
     */
    private StringRedisTemplate getStringStringRedisTemplate(RedisConnectionFactory factory) {
        StringRedisTemplate template = new StringRedisTemplate(factory);
        GenericJackson2JsonRedisSerializer jackson2JsonRedisSerializer = new GenericJackson2JsonRedisSerializer();
        template.setValueSerializer(jackson2JsonRedisSerializer);
        template.afterPropertiesSet();
        return template;
    }

}
