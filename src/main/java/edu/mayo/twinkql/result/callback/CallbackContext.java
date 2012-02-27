package edu.mayo.twinkql.result.callback;

import java.util.Map;

public class CallbackContext {
	
	private Map<String,Object> queryParams;
	
	private Map<String,Object> callbackIds;

	public Map<String, Object> getQueryParams() {
		return queryParams;
	}

	public void setQueryParams(Map<String, Object> queryParams) {
		this.queryParams = queryParams;
	}

	public Map<String, Object> getCallbackIds() {
		return callbackIds;
	}

	public void setCallbackIds(Map<String, Object> callbackIds) {
		this.callbackIds = callbackIds;
	}

}
