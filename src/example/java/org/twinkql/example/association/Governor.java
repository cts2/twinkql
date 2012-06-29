package org.twinkql.example.association;

import java.util.List;

public class Governor {
	
	private String name;
	
	private List<String> isA;
	
	private List<IsA> isAObject;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getIsA() {
		return isA;
	}

	public void setIsA(List<String> isA) {
		this.isA = isA;
	}

	public List<IsA> getIsAObject() {
		return isAObject;
	}

	public void setIsAObject(List<IsA> isAObject) {
		this.isAObject = isAObject;
	}

	@Override
	public String toString() {
		return "Governor [name=" + name + ", isA=" + isA + ", isAObject="
				+ isAObject + "]";
	}

	public static class IsA {
		
		private String isA;
	
		private List<Detail> details;

		public String getIsA() {
			return isA;
		}

		public void setIsA(String isA) {
			this.isA = isA;
		}

		public List<Detail> getDetails() {
			return details;
		}

		public void setDetails(List<Detail> details) {
			this.details = details;
		}

		@Override
		public String toString() {
			return "IsA [isA=" + isA + ", details=" + details + "]";
		}

	}
	
	public static class Detail {
		
		private String name;
		
		private List<String> values;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public List<String> getValues() {
			return values;
		}

		public void setValues(List<String> values) {
			this.values = values;
		}

		@Override
		public String toString() {
			return "Detail [name=" + name + ", values=" + values + "]";
		}	

	}
	
}
