package edu.mayo.twinkql.template;

public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Test t = new Test();
		
		String hi = "hi";
		t.getString(hi);
		
		System.out.println(hi);
	}
	
	void getString(String s){
		this.getString2(s);
	}

	void getString2(String s){
		s = new String("asdf");
	}

}
