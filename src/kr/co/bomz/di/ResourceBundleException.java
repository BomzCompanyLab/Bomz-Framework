package kr.co.bomz.di;

/**
 * 
 * @author ���ǳ�
 * @since 1.0
 * @version 1.0
 *
 */
public class ResourceBundleException extends Exception{

	private static final long serialVersionUID = 2323313205828294015L;

	public ResourceBundleException(){
		super();
	}
	
	public ResourceBundleException(String error){
		super(error);
	}
	
	public ResourceBundleException(Throwable throwable){
		super(throwable);
	}
	
	public ResourceBundleException(String error, Throwable throwable){
		super(error, throwable);
	}
}
