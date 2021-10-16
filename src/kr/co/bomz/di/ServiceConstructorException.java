package kr.co.bomz.di;

/**
 * 
 *	���� ������ �˸��� �����ڸ� ã�� �� ���� ��� �߻��Ѵ� 	
 * 
 * @author ���ǳ�
 * @since 1.0
 * @version 1.0
 *
 */
public class ServiceConstructorException extends InitializationException{

	private static final long serialVersionUID = -3392374201627127597L;

	public ServiceConstructorException(){
		super();
	}
	
	public ServiceConstructorException(String error){
		super(error);
	}
	
	public ServiceConstructorException(Throwable throwable){
		super(throwable);
	}
	
	public ServiceConstructorException(String error, Throwable throwable){
		super(error, throwable);
	}
}
