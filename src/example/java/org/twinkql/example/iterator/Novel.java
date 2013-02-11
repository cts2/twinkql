package org.twinkql.example.iterator;

public class Novel {

	private String novel;
	
	private String author;
	
	private String novelAbstract;

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

	public String getNovelAbstract() {
		return novelAbstract;
	}

	public void setNovelAbstract(String novelAbstract) {
		this.novelAbstract = novelAbstract;
	}

	@Override
	public String toString() {
		return "Novel [novel=" + novel + ", author=" + author
				+ ", novelAbstract=" + novelAbstract + "]";
	}

}
