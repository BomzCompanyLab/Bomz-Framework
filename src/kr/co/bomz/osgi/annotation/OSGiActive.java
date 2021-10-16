package kr.co.bomz.osgi.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * 	OSGi ���� �� �� ACTIVE ���·� ����� �� ȣ���� �޼ҵ� ����
 *  
 * 
 * @author ���ǳ�
 * @since 1.4
 * @version 1.4
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface OSGiActive {

}
