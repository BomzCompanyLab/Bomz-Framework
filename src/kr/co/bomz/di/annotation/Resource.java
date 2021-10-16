package kr.co.bomz.di.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * 	�������� �Ķ���� �� �տ� ���ǵǰų� <br>
 * 
 *  �ʵ忡 ���ǵ� �� �ִ�<br>
 *  
 *  �ʵ忡 ���ǵǾ� ���� ��� ���� setter �޼ҵ带 �˻��ϰ�<br>
 *  
 *  setter �޼ҵ尡 �����Ǿ� ���� ���� ��� �ʵ忡 ���� �����Ѵ�<br>
 * 
 * @author ���ǳ�
 * @since 1.0
 * @version 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value={ElementType.PARAMETER, ElementType.FIELD})
public @interface Resource {
	/**
	 * 	���ҽ� ���� Ÿ���� �����Ѵ� <br>
	 * 	���� ��ü ��, �ܺ� �������� ��, �ڵ���� ���� �ԷµǴ� ��<br>
	 *  3���� �� ������ �� ������, �⺻���� ResourceType.SERVICE �̴�
	 */
	ResourceType type() default ResourceType.SERVICE;
	
	/**
	 * 	type �� ParameterType.PROPERTIES_FILE �Ǵ� ParameterType.CODE �� �����Ͽ��� ��� �Է°�
	 */
	String value() default "";
	
	/**
	 * 	type �� ParameterType.RESOURCE �� �����Ͽ��� ��쿡�� ���Ǹ� <p>
	 *  ���������� ã�� �� ���� �� defaultValue() ���� ����� ������ ���θ� �����Ѵ�<p>
	 *  �⺻���� false �̴�
	 */
	boolean useDefaultValue() default false;
	
	/**
	 * 	type �� ParameterType.RESOURCE �� �����Ͽ��� ��쿡�� ���Ǹ� <p>
	 *  ���������� ã�� �� ���� �� �⺻ ������ ����Ѵ�
	 */
	String defaultValue() default "";
	
}
