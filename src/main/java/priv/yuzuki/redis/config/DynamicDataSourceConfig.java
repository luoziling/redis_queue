package priv.yuzuki.redis.config;

import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;


/**
 * @program: redis
 * @author: wangzibai01
 * @create: 2021-04-12 16:03
 * @description:
 **/

@Configuration
public class DynamicDataSourceConfig {
	/**
	 * 创建 DataSource Bean
	 * */
	@Bean
	@ConfigurationProperties("spring.datasource.druid.wdef")
	public DataSource wdefDataSource(){
		DataSource dataSource = DruidDataSourceBuilder.create().build();
		return dataSource;
	}

	@Bean
	@ConfigurationProperties("spring.datasource.druid.rdef")
	public DataSource rdefDataSource(){
		DataSource dataSource = DruidDataSourceBuilder.create().build();
		return dataSource;
	}

	/**
	 * 如果还有数据源,在这继续添加 DataSource Bean
	 * */

	@Bean
	@Primary
	public DynamicDataSource dataSource(DataSource wdefDataSource, DataSource rdefDataSource) {
		Map<Object, Object> targetDataSources = new HashMap<>(2);
		targetDataSources.put("wdef", wdefDataSource);
		targetDataSources.put("rdef", rdefDataSource);
		// 还有数据源,在targetDataSources中继续添加
		//System.out.println("DataSources:" + targetDataSources);
		return new DynamicDataSource(rdefDataSource, targetDataSources);
	}

	@Bean
	public JdbcTemplate jdbcTemplate(DataSource dataSource){
		return  new JdbcTemplate(dataSource);
	}
}
