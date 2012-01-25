package edu.mayo.twinkql.result.callback;

public interface Modifier<T> extends Callback {
	
	public T beforeSetting(T object);

}
