package kr.co.bomz.custom.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Queue;

/**
 * ť�� �ִ�ũ�⸦ �Ѿ��� �� �����Ͱ� ������ ó�� �����͸� �����ϰ� ��� ���� �����͸�
 * 
 * �������� �߰��Ѵ�.
 * 
 * 
 * @author ���ǳ�
 * @since 1.0
 * @param <T>
 * @version 1.0
 */
public class SupinanQueue<T> extends AbstractSupinanCollection<T> implements Queue<T> {
	
	private transient boolean changeQueueSize = false;
	
	private transient T data;
		
	/**
	 *	ť�� ũ�� ������ ���� ��� �����Ѵ� 
	 */
	public SupinanQueue(){
		super();
		this.changeQueueSize = true;
	}
	
	/**
	 * ť�� �ִ� ũ�⸦ �����Ѵ�<p>
	 * ť�� ũ�� �̻��� �����Ͱ� ������ ���� ������ �����͸� �����ϰ� �ֽ� �����͸� �߰��Ѵ�<p>
	 * 
	 */
	public SupinanQueue(int size){
		super(size);
	}

	public boolean offer(T obj){
			
		if( this.changeQueueSize ){
			if( super.collection.length <= super.size ){
				super.collection = super.modifyQueue();
				super.cursor = 0;
			}
		}else	 if( super.collection.length <= super.size ){
			this.poll();
		}
			
		super.collection[
           		( (super.cursor + super.size) >= super.collection.length ) ? 
           				(super.cursor + super.size) - super.collection.length : (super.cursor + super.size)  
           ] = obj;
		
		super.size++;
		
		return true;
	}

	// Ŀ�� ��ġ�� �ִ� �����͸� ����.
	// �� �������� �ʴ´�.. ������ �� peek() �� ȣ���ϸ� ���� �����Ͱ� ���۵�
	public T peek() {
		if( super.size == 0 )	return null;
		return super.collection[cursor];
	}

	// Ŀ�� ��ġ�� �ִ� �����͸� ����
	// ���� �� �ش� ������ ����
	public T poll() {
		
		if( super.size <= 0 )	return null;
		
		this.data = super.collection[cursor];
		super.collection[cursor] = null;
		
		super.size--;
		if( ++cursor >= super.collection.length )		cursor = 0;
		
		if( this.changeQueueSize && super.size > super.COLLECTION_SIZE){
			int length = super.collection.length;
			length >>= 2;
			if( length > super.size ){
				super.collection = this.modifyQueue(length);
				cursor = 0;
			}
		}
		
		return this.data;
	}

	public boolean addAll(Collection<? extends T> c) {
		
		if( c == null )	throw new NullPointerException();
		if( c == this )	throw new IllegalArgumentException();
		
		boolean flag = false;
		Iterator<? extends T> iter = c.iterator();
			
		while( iter.hasNext() ){
			if( this.offer(iter.next() ) )	flag = true;
		}
			
		return flag;
	}

	public boolean contains(Object arg0) {
		throw new UnsupportedOperationException();
	}

	public boolean containsAll(Collection<?> arg0) {
		throw new UnsupportedOperationException();
	}

	public boolean remove(Object arg0) {
		throw new UnsupportedOperationException();
	}

	public boolean removeAll(Collection<?> arg0) {
		throw new UnsupportedOperationException();
	}

	public boolean retainAll(Collection<?> arg0) {
		throw new UnsupportedOperationException();
	}

	// peek() �� ����. �����Ͱ� ���ٸ� ���� �߻�
	public T element() {
		T t = peek();
		
		if( t == null )
			throw new NoSuchElementException();
		else
			return t;
		
	}
	
	// ť�� �����Ͱ� �ִٸ� �����͸� �Ѱ��ְ� ���ٸ� ���� �߻�
	public T remove() {
		T t = poll();
		if( t == null )
			throw new NoSuchElementException();
		else
			return t;
	}
	
	// offer(T t) �� ����.. �� ���н� ���� �߻�
	public boolean add(T obj) {
		if( offer(obj) )
			return true;
		else
			throw new IllegalStateException("Queue add exception");
	}
	
}
