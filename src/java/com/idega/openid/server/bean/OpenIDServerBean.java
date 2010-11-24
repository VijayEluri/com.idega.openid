package com.idega.openid.server.bean;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.idega.core.accesscontrol.business.LoginBusinessBean;
import com.idega.openid.server.data.AuthorizedAttribute;
import com.idega.presentation.IWContext;

@Service("openIDServerBean")
@Scope("session")
public class OpenIDServerBean {

	private String username;
	private String realm;
	private String returnUrl;
	
//	private Boolean userLoggedInAtBeginningOfProcessing;
	
	private String serverUrl;
	private Boolean doRedirect;
	private Map parameterMap;
	
	private List<AuthorizedAttribute> requiredAttributes;
	private List<AuthorizedAttribute> requestedAttributes;
	private List<AuthorizedAttribute> optionalAttributes;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getRealm() {
		return realm;
	}

	public void setRealm(String realm) {
		this.realm = realm;
	}

	public String getServerUrl() {
		return serverUrl;
	}

	public void setServerUrl(String serverUrl) {
		this.serverUrl = serverUrl;
	}

	public String getReturnUrl() {
		return returnUrl;
	}

	public void setReturnUrl(String returnUrl) {
		this.returnUrl = returnUrl;
	}

	public Boolean getDoRedirect() {
		return doRedirect;
	}

	public void setDoRedirect(Boolean doRedirect) {
		this.doRedirect = doRedirect;
	}

	public Map getParameterMap() {
		return parameterMap;
	}

	public void setParameterMap(Map parameterMap) {
		this.parameterMap = parameterMap;
	}

	public void setRequiredAttributes(List<AuthorizedAttribute> required) {
		requiredAttributes = required;
	}
	
	public List<AuthorizedAttribute> getRequiredAttributes() {
		return requiredAttributes;
	}
	
	public void setRequestedAttributes(List<AuthorizedAttribute> requested) {
		requestedAttributes = requested;
	}
	
	public List<AuthorizedAttribute> getRequestedAttributes() {
		return requestedAttributes;
	}
	
	public void setOptionalAttributes(List<AuthorizedAttribute> optional) {
		optionalAttributes = optional;
	}
	
	public List<AuthorizedAttribute> getOptionalAttributes() {
		return optionalAttributes;
	}
	
	
	public boolean isRequiredAttribute(List<AuthorizedAttribute> attribute){
		return requiredAttributes.contains(attribute);
	}
	
	
	public boolean isRequestedAttribute(List<AuthorizedAttribute> attribute){
		return requestedAttributes.contains(attribute);
	}
	
	/**
	 * May invalidate session
	 */
	public void invalidate(){
		
		username = null;
		realm = null;
		returnUrl = null;
		serverUrl = null;
		doRedirect = null;
		parameterMap = null;
		requiredAttributes = null;
		requestedAttributes = null;
		optionalAttributes = null;
		
		
		IWContext iwc = IWContext.getCurrentInstance();
		LoginBusinessBean loginBusiness = getLoginBusiness(iwc.getRequest());
		loginBusiness.logOutUser(iwc);
	}
	
	private LoginBusinessBean getLoginBusiness(HttpServletRequest request){
		return LoginBusinessBean.getLoginBusinessBean(request);
	}
	
	
	
//	/*      */
//	public boolean getIncludeAttributeInResponce(String alias,String type){
//		if(requestedAttributes != null){
//			for(AuthorizedAttribute attr : requestedAttributes){
//				ExchangeAttribute eattr = attr.getExchangeAttribute();
//				if(eattr.getName().equals(alias) && eattr.getType().equals(type)){
//					return attr.getIsAllowed();
//				}
//			}
//		}
//		return false;
//	}
	
}