package kr.co.bomz.custom.util;

/**
 * 
 * SupinanThreadPool �� ���� ���¿��� getThread() ȣ��� �߻��Ѵ�<p>
 * 
 * SupinanThreadPool�� close() ȣ�� �� ������°��Ǹ�<p>
 * 
 * ���� ���¸� �����ϱ� ���ؼ��� reset() �޼ҵ带 ȣ���ϸ� �ȴ�
 * 
 * @author ���ǳ�
 * @since 1.4.1
 * @version 1.4.1
 * @see kr.co.bomz.custom.util.SupinanThreadPool
 *
 */
public class SupinanThreadPoolCloseException extends RuntimeException{

	private static final long serialVersionUID = -3313910417544080802L;

	public SupinanThreadPoolCloseException(){
		super();
	}
}
