package kr.co.bomz.di.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 
 * 	DI �� ����� ���� Ŭ������ ����Ѵ�<br>
 * 
 * policy ���� �Է����� ���� ��� �⺻ ���� SINGLETON ��å���� �Ѵ� 
 * 
 * @author ���ǳ�
 * @since 1.0
 * @version 1.2
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Service {
	/**
	 * 	���� ���� ��Ģ
	 */
	ServiceType type() default ServiceType.SINGLETON;
	
	/**
	 * 	���� �ʱ�ȭ ����
	 * �⺻�� : ORDER_3
	 */
	ServiceOrder order() default ServiceOrder.ORDER_3;
	
}
