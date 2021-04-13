//package priv.yuzuki.redis.config;
//
//import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.context.properties.ConfigurationProperties;
//import org.springframework.cache.annotation.CachingConfigurerSupport;
//import org.springframework.cache.annotation.EnableCaching;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Primary;
//import org.springframework.data.redis.connection.RedisConnectionFactory;
//import org.springframework.data.redis.connection.RedisPassword;
//import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
//import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
//import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
//import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.data.redis.core.StringRedisTemplate;
//import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
//
///**
// * @program: redis
// * @author: wangzibai01
// * @create: 2021-04-12 15:42
// * @description:
// **/
//
//@Configuration
//@EnableCaching
//public class RedisConfig extends CachingConfigurerSupport {
//
//
//	/**
//	 * 配置lettuce连接池
//	 * Redis 连接池: Lettuce、Jedis 比较
//	 * Jedis:
//	 * Jedis在实现上是直接连接的redis server，如果在多线程环境下是非线程安全的，这个时候只有使用连接池，为每个Jedis实例增加物理连接
//	 * Lettuce:
//	 * Lettuce的连接是基于Netty的，连接实例（StatefulRedisConnection）可以在多个线程间并发访问，
//	 * 应为StatefulRedisConnection是线程安全的，所以一个连接实例（StatefulRedisConnection）就可以满足多线程环境下的并发访问，
//	 * 当然这个也是可伸缩的设计，一个连接实例不够的情况也可以按需增加连接实例。
//	 *
//	 * @return
//	 */
//	@Bean
//	@ConfigurationProperties(prefix = "spring.redis.lettuce.pool")
//	public GenericObjectPoolConfig redisPool() {
//		return new GenericObjectPoolConfig<>();
//	}
//
//	/**
//	 * 配置第一个数据源的
//	 *
//	 * @return
//	 */
//	@Bean
////    @ConfigurationProperties(prefix = "spring.redis")
//	public RedisStandaloneConfiguration redisConfigMaster(@Value("${spring.redis.host}") String host, @Value("${spring.redis.port}") int port
//			, @Value("${spring.redis.database}") int db, @Value("${spring.redis.password}") String password) {
//		RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration(host, port);
//		redisStandaloneConfiguration.setDatabase(db);
//		redisStandaloneConfiguration.setPassword(RedisPassword.of(password));
//		return redisStandaloneConfiguration;
//	}
//
//	/**
//	 * 配置第二个数据源
//	 *
//	 * @return
//	 */
//	@Bean
//	@ConfigurationProperties(prefix = "spring.redis.slave")
//	public RedisStandaloneConfiguration redisConfigSlave() {
//		return new RedisStandaloneConfiguration();
//	}
//
//	/**
//	 * 配置第一个数据源的连接工厂
//	 * 这里注意：需要添加@Primary 指定bean的名称，目的是为了创建两个不同名称的LettuceConnectionFactory
//	 *
//	 * @param config
//	 * @param redisConfigMaster
//	 * @return
//	 */
//	@Bean("factory")
//	@Primary
//	public LettuceConnectionFactory factory(GenericObjectPoolConfig config, RedisStandaloneConfiguration redisConfigMaster) {
//		LettuceClientConfiguration clientConfiguration = LettucePoolingClientConfiguration.builder().poolConfig(config).build();
//		return new LettuceConnectionFactory(redisConfigMaster, clientConfiguration);
//	}
//
//	@Bean("factorySlave")
//	public LettuceConnectionFactory factorySlave(GenericObjectPoolConfig config, RedisStandaloneConfiguration redisConfigSlave) {
//		LettuceClientConfiguration clientConfiguration = LettucePoolingClientConfiguration.builder().poolConfig(config).build();
//		return new LettuceConnectionFactory(redisConfigSlave, clientConfiguration);
//	}
//
//	/**
//	 * 配置第一个数据源的RedisTemplate
//	 * 注意：这里指定使用名称=factory 的 RedisConnectionFactory
//	 * 并且标识第一个数据源是默认数据源 @Primary
//	 *
//	 * @param factory
//	 * @return
//	 */
//	@Bean("redisTemplate")
//	@Primary
//	public RedisTemplate<String, String> redisTemplate(@Qualifier("factory") RedisConnectionFactory factory) {
//		return getStringStringRedisTemplate(factory);
//	}
//
//	/**
//	 * 配置第二个数据源的RedisTemplate
//	 * 注意：这里指定使用名称=factorySlave 的 RedisConnectionFactory
//	 *
//	 * @param factorySlave
//	 * @return
//	 */
//	@Bean("redisSlaveTemplate")
//	public RedisTemplate<String, String> redisSlaveTemplate(@Qualifier("factorySlave") RedisConnectionFactory factorySlave) {
//		return getStringStringRedisTemplate(factorySlave);
//	}
//
//	/**
//	 * 设置序列化方式 （这一步不是必须的）
//	 *
//	 * @param factory
//	 * @return
//	 */
//	private RedisTemplate<String, String> getStringStringRedisTemplate(RedisConnectionFactory factory) {
//		StringRedisTemplate template = new StringRedisTemplate(factory);
//		GenericJackson2JsonRedisSerializer jackson2JsonRedisSerializer = new GenericJackson2JsonRedisSerializer();
//		template.setValueSerializer(jackson2JsonRedisSerializer);
//		template.afterPropertiesSet();
//		return template;
//	}
//
//	/**
//	 * 数据字典存储redis
//	 *
//	 * @param config
//	 * @param redisDictConfig
//	 * @return
//	 */
//	@Bean("dictFactory")
//	public LettuceConnectionFactory dictFactory(GenericObjectPoolConfig config, RedisStandaloneConfiguration redisDictConfig) {
//		LettuceClientConfiguration clientConfiguration = LettucePoolingClientConfiguration.builder().poolConfig(config).build();
//		return new LettuceConnectionFactory(redisDictConfig, clientConfiguration);
//	}
//
//	@Bean("dictRedisTemplate")
//	public RedisTemplate<String, String> dictRedisTemplate(@Qualifier("dictFactory") RedisConnectionFactory dictFactory) {
//		return getStringStringRedisTemplate(dictFactory);
//	}
//
//	@Bean
//	public RedisStandaloneConfiguration redisDictConfig(@Value("${dictredis.host}") String host, @Value("${dictredis.port}") int port
//			, @Value("${dictredis.database}") int db, @Value("${dictredis.password}") String password) {
//		RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration(host, port);
//		redisStandaloneConfiguration.setDatabase(db);
//		redisStandaloneConfiguration.setPassword(RedisPassword.of(password));
//		return redisStandaloneConfiguration;
//	}
//
//}
//
