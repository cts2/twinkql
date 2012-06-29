package org.twinkql.example.param;

public class Novel {

	private String novel;
	
	private String author;

	public String getNovel() {
		return novel;
	}

	public void setNovel(String novel) {
		this.novel = novel;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	@Override
	public String toString() {
		return "Novel [novel=" + novel + ", author=" + author + "]";
	}

}
