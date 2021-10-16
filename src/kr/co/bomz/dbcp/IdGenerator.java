package kr.co.bomz.dbcp;

/**
 * ���̵� ������ ���� ����
 * 
 * @author ���ǳ�
 * @since 1.5
 * @version 1.5
 *
 */
public class IdGenerator {

	private long id = System.currentTimeMillis();
	
	long next(){
		return ++this.id;
	}
	
	long getNowId(){
		return this.id;
	}
	
	long getNextId(){
		return this.id + 1;
	}
}
