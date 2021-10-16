package kr.co.bomz.custom.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.locks.ReentrantLock;

/**
 * ť�� �ִ�ũ�⸦ �Ѿ��� �� �����Ͱ� ������ ó�� �����͸� �����ϰ� <br>
 * 
 * ��� ���� �����͸� �������� �߰��Ѵ�.
 * 
 * 
 * @author ���ǳ�
 * @since 1.0
 * @version 1.0
 */
public class SupinanBlockingQueue<T> extends SupinanQueue<T>{

	private final ReentrantLock lock = new ReentrantLock(false);

	public SupinanBlockingQueue(){
		super();
	}
	
	/**
	 * ť�� �ִ� ũ��
	 * @param size
	 */
	public SupinanBlockingQueue(int size){
		super(size);
	}

	public boolean offer(T obj){
		lock.lock();
		try{
			return super.offer(obj);
		}finally{
			lock.unlock();
		}
	}

	// Ŀ�� ��ġ�� �ִ� �����͸� ����.
	// �� �������� �ʴ´�.. ������ �� peek() �� ȣ���ϸ� ���� �����Ͱ� ���۵�
	public T peek() {
		lock.lock();
		try{
			return super.peek();
		}finally{
			lock.unlock();
		}
	}

	// Ŀ�� ��ġ�� �ִ� �����͸� ����
	// ���� �� �ش� ������ ����
	public T poll() {
		lock.lock();
		try{
			return super.poll();
		}finally{
			lock.unlock();
		}
	}

	public boolean addAll(Collection<? extends T> c) {
		lock.lock();
		
		try{
			return super.addAll(c);
		}finally{
			lock.unlock();
		}
	}

	public void clear() {
		lock.lock();
		
		try{
			super.clear();
		}finally{
			lock.unlock();
		}
	}

	public boolean isEmpty() {
		return size == 0;
	}

	public Iterator<T> iterator() {
		lock.lock();
		try{
			return super.iterator();
		}finally{
			lock.unlock();
		}
	}

	public int size() {
		return size;
	}

	public Object[] toArray() {
		lock.lock();
		
		try{
			return super.toArray();			
		}finally{
			lock.unlock();
		}
	}

	public <L> L[] toArray(L[] c) {
		lock.lock();
		
		try{
			return super.toArray(c);			
		}finally{
			lock.unlock();
		}
	}	
	
}
