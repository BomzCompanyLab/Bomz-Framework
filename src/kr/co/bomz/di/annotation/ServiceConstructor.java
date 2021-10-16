package kr.co.bomz.di.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;


/**
 * 
 * 	���� ������ ȣ���� �����ڸ� �����Ѵ�<br>
 * 
 * �ش� ������̼��� �������� ���� ��� �⺻ �����ڸ� ȣ���ϸ�, <br>
 * 
 * �ΰ� �̻� �����Ǿ� �ְų�, �����Ǿ� ���� �ʰ� �⺻ �����ڰ� ���� ���<br>
 * 
 * ServiceConstructorException �� �߻���Ų��
 * 
 * @author ���ǳ�
 * @since 1.0
 * @version 1.0
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.CONSTRUCTOR)
public @interface ServiceConstructor {

}
