package priv.yuzuki.redis.parameter;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @program: redis
 * @author: wangzibai01
 * @create: 2021-06-26 14:23
 * @description:
 **/
@Component
public class MockNoticeParameterStrategy implements NoticeParameterStrategy<String,String>{
	private String body;
	@Override
	public void init(String body) {
		this.body = body;
	}

	@Override
	public String getMessage() {
		return body;
	}

	@Override
	public List<String> listNotifiedPersons() {
		List<String> res = new ArrayList<>();
		res.add("yuzuki");

		return res;
	}
}
