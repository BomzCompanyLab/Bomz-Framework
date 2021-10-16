package kr.co.bomz.di;

/**
 * 
 * ServiceStore �ʱ�ȭ�� ������ �� getService(...) �޼ҵ带 ȣ���� ��� �߻��Ѵ�
 * 
 * @author ���ǳ�
 * @since 1.0
 * @version 1.0
 */
public class ServiceStoreInitializationException extends InitializationException{

	private static final long serialVersionUID = -2904122497225861967L;

	public ServiceStoreInitializationException(){
		super();
	}
	
	public ServiceStoreInitializationException(String error){
		super(error);
	}
	
	public ServiceStoreInitializationException(Throwable throwable){
		super(throwable);
	}
	
	public ServiceStoreInitializationException(String error, Throwable throwable){
		super(error, throwable);
	}
}
