package kr.co.bomz.custom.util;

import java.util.Iterator;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 
 * ť , �� , �� ��� ���������� ����ϴ� �����͸� �����ϴ� �߻� Ŭ����
 * 
 * @author ���ǳ�
 * @since 1.0
 * @version 1.0
 *
 */
public abstract class AbstractSupinanCollection<T> {

	/**
	 * ����ڰ� ũ�⸦ ���������ʾ��� ��� �⺻ ��� ũ��
	 */
	protected static  final int DEFAULT_COLLECTION_SIZE = 20;
	
	/**
	 * ���� ������ ������� ũ��. 
	 */
	protected final int COLLECTION_SIZE;
	
	protected transient T[] collection;
	protected transient int size = 0;
	protected transient int cursor = 0;
		
	/**
	 * �⺻ ������<br>
	 * ��������� ũ�⸦ �������� �ʾ����Ƿ� �⺻ ũ����<br>
	 * DEFAULT_COLLECTION_SIZE �� ���� ����Ѵ�
	 */
	AbstractSupinanCollection(){
		this(DEFAULT_COLLECTION_SIZE);
	}
	
	/**
	 * ����� ���� ũ�� ������<br>
	 * ����ڰ� ������ ũ�⸸ŭ ��������� �����Ѵ�<br>
	 */
	@SuppressWarnings("unchecked")
	AbstractSupinanCollection(int size){
		COLLECTION_SIZE = size;
		collection = (T[])new Object[size];
	}
			
	/**
	 * 	���ο� �迭�� �����Ͽ� ���� �迭�� ���� �����Ѵ�
	 */
	protected T[] modifyQueue(){
		int queueSize = size;
		queueSize <<= 2;
		return this.modifyQueue(queueSize);
	}
	
	/**
	 * 	���ο� �迭�� �����Ͽ� ���� �迭�� ���� �����Ѵ�
	 */
	@SuppressWarnings("unchecked")
	protected T[] modifyQueue(int queueSize){
		Object[] newQueue = new Object[ (queueSize < size)?size : queueSize ];
		
		if( size == 0 ){
			// ����� 0 �� ��� ���� ������ �迭 ����
			return (T[])newQueue;
		}
		
		return this.modifyQueue(newQueue);
	}
		
	/**
	 * 	���ο� �迭�� �����Ͽ� ���� �迭�� ���� �����Ѵ�
	 */
	@SuppressWarnings("unchecked")
	protected T[] modifyQueue(Object[] newQueue){
		int tmp = (collection.length - cursor) < size ? collection.length - cursor : size;
		
		if( cursor != 0 ){
			// Ŀ���� 0 ��°�� ���� ���� ��� �޺κ� ���� ���� ����
			System.arraycopy(collection, cursor, newQueue, 0, tmp);
		}

		if( tmp < size ){
			// ���� �迭�� 0��° ���� ������ ����
			System.arraycopy(collection, 0, newQueue, tmp, size - tmp);
		}else if( tmp == size && cursor == 0 ){
			// ���� �迭�� 0��° ���� ������ ����
			System.arraycopy(collection, 0, newQueue, 0, size);
		}
		
		return (T[])newQueue;
	}
	
	/**
	 * ���� ����� �����Ͱ� �ִ��� ����
	 */
	public boolean isEmpty() {
		return size == 0;
	}
	
	/**
	 * ������� �ʱ�ȭ
	 */
	@SuppressWarnings("unchecked")
	public void clear() {
		cursor = 0;
		size = 0;
		collection = (T[])new Object[COLLECTION_SIZE];	
	}
	
	/**
	 * ����� �����͸� java.util.Iterator �������� ����
	 */
	public Iterator<T> iterator() {
		return new Iter();
	}
	
	/**
	 * ����� �������� ����
	 */
	public int size() {
		return this.size;
	}

	/**
	 * ����� �����͸� ������Ʈ �迭 �������� ����
	 */
	public Object[] toArray() {
		return this.modifyQueue(this.size);		
	}

	/**
	 * ����� �����͸� ����ڰ� �Ķ���ͷ� �Ѱ��� �迭�� ����
	 */
	@SuppressWarnings("unchecked")
	public <L> L[] toArray(L[] c){
		
		if( c == null )	throw new NullPointerException();
	
		if( c.length < this.size )
			c = (L[])java.lang.reflect.Array.newInstance(c.getClass().getComponentType(), this.size);
		
		this.modifyQueue(c);
		
		return c;
	}
	
	/**
	 * 
	 * ����� �����͸� java.util.Iterator �������� �����Ѵ�
	 * 
	 * @author �庸��
	 * 
	 * @version 1.0
	 *
	 */
	private class Iter implements Iterator<T>{
		
		private final ReentrantLock lock = new ReentrantLock();
		private int _cursor = 0;
		private int _size;
		private Object[] _queue;
		
		@SuppressWarnings("unchecked")
		public Iter(){
			this._size = size;
			this._queue = (T[])new Object[size]; 
			
			this.createIterator();
		}

		public boolean hasNext() {
			lock.lock();
			try{
				return (_cursor >= _size) ? false : true;
			}finally{
				lock.unlock();
			}
		}
		
		@SuppressWarnings("unchecked")
		public T next() {
			lock.lock();
			try{
				if( _cursor >= size )
					throw new IllegalStateException();
				
				return (T)_queue[_cursor++];
			}finally{
				lock.unlock();
			}
		}

		public void remove() {
			lock.lock();
			try{
				_cursor++;
			}finally{
				lock.unlock();
			}
		}
		
		private void createIterator(){
			this._queue = modifyQueue(size);
		}
	}
}
