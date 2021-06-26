package priv.yuzuki.redis.factory;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import priv.yuzuki.redis.parameter.NoticeParameterStrategy;

/**
 * @program: redis stream
 * @author: wangzibai01
 * @create: 2021-06-09 16:16
 * @description: 通知参数工厂
 **/
@Component
public class NoticeParameterFactory implements ApplicationContextAware {
	private ApplicationContext applicationContext;

	/**
	 * 获取具体惨是构造策略
	 * @param strategyCode 策略枚举的code
	 * @return
	 */
	public NoticeParameterStrategy<String,String> getStringParameterStrategy(Integer strategyCode){
		NoticeParameterStrategy<String,String> res = null;
		NoticeStrategyBeanNameEnum strategyEnum = NoticeStrategyBeanNameEnum.getEnumByCode(strategyCode);
		Object bean = applicationContext.getBean(strategyEnum.getBeanName());

		if (bean instanceof NoticeParameterStrategy){
			res = (NoticeParameterStrategy<String,String>)bean;
		}
		return res;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
}
