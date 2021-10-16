package kr.co.bomz.di;

/**
 * 
 * 	���� �ʱ�ȭ�� ���õ� ������ ��� �߻��Ѵ�
 * 
 * @author ���ǳ�
 * @since 1.0
 * @version 1.0
 *
 */
public class InitializationException extends RuntimeException{

	private static final long serialVersionUID = 5321284025450228377L;

	public InitializationException(){
		super();
	}
	
	public InitializationException(String error){
		super(error);
	}
	
	public InitializationException(Throwable throwable){
		super(throwable);
	}
	
	public InitializationException(String error, Throwable throwable){
		super(error, throwable);
	}
}
