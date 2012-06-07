package edu.mayo.twinkql.result;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class UriParser {
	
	private static final Set<Character> SEPARATORS = 
		new HashSet<Character>(Arrays.asList(':', '#', '/'));

	public String getLocalPart(String uri){
		int separator = this.getSeparatorPosition(uri);
		
		return StringUtils.substring(uri, separator+1);
	}
	
	public String getNamespace(String uri){
		int separator = this.getSeparatorPosition(uri);
		
		return StringUtils.substring(uri, 0, separator+1);
	}
	
	private int getSeparatorPosition(String string){
		char[] chars = string.toCharArray();
		
		for(int i=chars.length-1;i>0;i--){
			if(SEPARATORS.contains(chars[i])){
				return i;
			}
		}
		
		throw new IllegalStateException();
	}
}
