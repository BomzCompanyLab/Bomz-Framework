package kr.co.bomz.di;

/**
 * 
 * 	Resource ������̼��� ������ �ʵ� �� �Ķ���� ���� ��Ī�Ǵ� ������ ã�� �� ���� �� �߻��Ѵ�<br>
 * 
 * @author ���ǳ�
 * @since 1.0
 * @version 1.0
 *
 */
public class NoSuchFieldTypeException extends InitializationException{

	private static final long serialVersionUID = 5943930062273637605L;

	public NoSuchFieldTypeException(){
		super();
	}
	
	public NoSuchFieldTypeException(String error){
		super(error);
	}
	
	public NoSuchFieldTypeException(Throwable throwable){
		super(throwable);
	}
	
	public NoSuchFieldTypeException(String error, Throwable throwable){
		super(error, throwable);
	}
}
