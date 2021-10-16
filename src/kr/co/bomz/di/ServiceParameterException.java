package kr.co.bomz.di;


/**
 * 
 * 	�Ķ���� ���� ������ ������ �Ǵ� �޼ҵ��� �Ķ���Ϳ�<br>
 * 
 *  �Ķ������ ���� ������ Resource ������̼��� �߸� �����Ǿ��� ��� �߻��Ѵ� 
 * 
 * @author ���ǳ�
 * @since 1.0
 * @version 1.0
 *
 */
public class ServiceParameterException extends InitializationException{

	private static final long serialVersionUID = 1384504564685031299L;

	public ServiceParameterException(){
		super();
	}
	
	public ServiceParameterException(String error){
		super(error);
	}
	
	public ServiceParameterException(Throwable throwable){
		super(throwable);
	}
	
	public ServiceParameterException(String error, Throwable throwable){
		super(error, throwable);
	}
}
