package priv.yuzuki.redis;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import priv.yuzuki.redis.task.StreamCompensateTask;

import javax.annotation.Resource;

@SpringBootTest
class RedisApplicationTests {
	@Resource
	StreamCompensateTask streamCompensateTask;

	@Test
	void contextLoads() {
		streamCompensateTask.produceMessage();
	}

}
