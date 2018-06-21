package domain;

public class Stack<T> {
	
	private T[] array;
	private int top;
	
	public Stack(){
		array = (T[]) new Object[10];
		top = 0;
	}
	
	public void push(T element) {
		array[top] = element;
		top++;
	}
	
	public T pop() {
		top--;
		return array[top];
	}
	
	public boolean isEmpty() {
		return top == 0;
	}

}
