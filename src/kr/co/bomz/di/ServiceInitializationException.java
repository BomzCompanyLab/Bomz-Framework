package kr.co.bomz.di;

/**
 * 
 *  ���� ���� �� ���� �Ǵ� ServiceInitialization ������̼��� ����� �޼ҵ尡<br>
 * 
 *  �ΰ� �̻� ����Ǿ� �ְų� ȣ�� �� ������ �߻��Ͽ��� ��� �߻��Ѵ�
 * 
 * @author ���ǳ�
 * @since 1.0
 * @version 1.0
 */
public class ServiceInitializationException extends InitializationException{

	private static final long serialVersionUID = -4435615676826486126L;

	public ServiceInitializationException(){
		super();
	}
	
	public ServiceInitializationException(String error){
		super(error);
	}
	
	public ServiceInitializationException(Throwable throwable){
		super(throwable);
	}
	
	public ServiceInitializationException(String error, Throwable throwable){
		super(error, throwable);
	}
}
