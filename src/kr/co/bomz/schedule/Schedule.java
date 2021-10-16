package kr.co.bomz.schedule;

/**
 * 
 * @author ���ǳ�
 * @since 1.0
 * @version 1.0
 *
 */
public interface Schedule {
	
	/**
	 * 	������ �ð��� �Ǿ��� ��� ������ ������ �����Ѵ�<br>
	 *  ���� �ɸ��� �۾��� ��� ���������� �����带 �����Ͽ� �����Ͽ��� �Ѵ�.<br>
	 *  �׷��� ������ �ٸ� �������� ���ۿ� ������ ��ģ��
	 */
	void execute();
	
	/**
	 * ����ڰ� ������ �������� �����Ͽ��ų�<br>
	 * �������ڰ� �Ǿ� �������� ����Ǿ��� ��� ȣ��ȴ�
	 */
	void destroy();
}