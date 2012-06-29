package org.twinkql.example.alias;

import java.util.List;

public class Tetris {

	private List<String> comments;

	public List<String> getComments() {
		return comments;
	}

	public void setComments(List<String> comments) {
		this.comments = comments;
	}

	@Override
	public String toString() {
		return "Tetris [comments=" + comments + "]";
	}

}
