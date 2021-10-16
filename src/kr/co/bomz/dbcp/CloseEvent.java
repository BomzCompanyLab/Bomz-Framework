package kr.co.bomz.dbcp;

/**
 * �����ͺ��̽� ���� ���� �̺�Ʈ
 * 
 * @author ���ǳ�
 * @since 1.5
 * @version 1.5
 *
 */
public interface CloseEvent {
	
	/**		���� ���̵� ���� ����		*/
	long getId();
	
	/**		�����ͺ��̽� ���� ����		*/
	void close();
}
