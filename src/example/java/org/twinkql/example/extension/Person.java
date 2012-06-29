package org.twinkql.example.extension;

import java.util.List;

public class Person {
	
	private List<String> names;
	
	private String birthDate;

	public List<String> getNames() {
		return names;
	}

	public void setNames(List<String> names) {
		this.names = names;
	}

	public String getBirthDate() {
		return birthDate;
	}

	public void setBirthDate(String birthDate) {
		this.birthDate = birthDate;
	}

	@Override
	public String toString() {
		return "Person [names=" + names + ", birthDate=" + birthDate + "]";
	}

}
