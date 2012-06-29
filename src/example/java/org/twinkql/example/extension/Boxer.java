package org.twinkql.example.extension;

import java.util.List;

public class Boxer extends Person {
	
	private List<String> comments;

	public List<String> getComments() {
		return comments;
	}

	public void setComments(List<String> comments) {
		this.comments = comments;
	}

	@Override
	public String toString() {
		return "Boxer [comments=" + comments + "] -> extends " + super.toString();
	}

}