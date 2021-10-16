package kr.co.bomz.di.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * 	Service ������̼��� ������ ���� ���� �� �ʱ�ȭ �۾� ����<br>
 *  
 *  ���� ������ �޼ҵ尡 ���� ��� ���ȴ�<br>
 *  
 *  �ִ� 1���� ���� �����ϸ� 2�� �̻��� ��� ServiceInitializationException �� �߻��Ѵ�
 * 
 * @author ���ǳ�
 * @since 1.0
 * @version 1.0
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ServiceInitialization {
	
}
