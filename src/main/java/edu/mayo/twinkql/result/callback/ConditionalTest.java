package edu.mayo.twinkql.result.callback;

public interface ConditionalTest<T> extends Callback {
	
	public boolean test(T param);

}
