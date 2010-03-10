package com.idega.openid.client.filter;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.openid4java.OpenIDException;
import org.openid4java.consumer.ConsumerException;
import org.openid4java.consumer.ConsumerManager;
import org.openid4java.consumer.InMemoryConsumerAssociationStore;
import org.openid4java.consumer.InMemoryNonceVerifier;
import org.openid4java.discovery.DiscoveryInformation;
import org.openid4java.message.AuthRequest;
import org.openid4java.message.ax.FetchRequest;
import org.openid4java.message.sreg.SRegRequest;

import com.idega.core.accesscontrol.business.LoginBusinessBean;
import com.idega.core.accesscontrol.business.LoginState;
import com.idega.idegaweb.IWMainApplication;
import com.idega.openid.OpenIDConstants;
import com.idega.servlet.filter.BaseFilter;

public class OpenIDClientRequestFilter extends BaseFilter {

	private ConsumerManager manager;
	
	public void destroy() {
		manager = null;
	}

	public void doFilter(ServletRequest srequest, ServletResponse sresponse, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) srequest;
		HttpServletResponse response = (HttpServletResponse) sresponse;
		HttpSession session = request.getSession();

		String identifier = request.getParameter(OpenIDConstants.PARAMETER_IDENTIFIER);
		if (identifier != null) {
			try {
				// configure the return_to URL where your application will receive
				// the authentication responses from the OpenID provider
				// String returnToUrl = "http://example.com/openid";
				String returnToUrl = request.getRequestURL().toString()
						+ "?" + OpenIDConstants.PARAMETER_RETURN + "=" + Boolean.TRUE.toString();
				
				Object[] arguments = { identifier };
				String providerURL = getIWMainApplication(request).getIWApplicationContext().getApplicationSettings().getProperty(OpenIDConstants.PROPERTY_OPENID_PROVIDER, "http://www.google.com/profiles/{0}");
	
				// perform discovery on the user-supplied identifier
				List<?> discoveries = manager.discover(MessageFormat.format(providerURL, arguments));
	
				// attempt to associate with the OpenID provider
				// and retrieve one service endpoint for authentication
				DiscoveryInformation discovered = manager.associate(discoveries);
	
				// store the discovery information in the user's session
				session.setAttribute("openid-disc", discovered);
	
				// obtain a AuthRequest message to be sent to the OpenID provider
				AuthRequest authReq = manager.authenticate(discovered, returnToUrl);
	
				// Attribute Exchange example: fetching the 'email' attribute
				FetchRequest fetch = FetchRequest.createFetchRequest();
				SRegRequest sregReq = SRegRequest.createFetchRequest();
	
				sregReq.addAttribute("nickname", true);

				fetch.addAttribute("email",
						"http://schema.openid.net/contact/email", false);
				sregReq.addAttribute("email", true);

				fetch.addAttribute("fullname",
						"http://schema.openid.net/contact/fullname", false);
				sregReq.addAttribute("fullname", true);

				fetch.addAttribute("dob",
						"http://schema.openid.net/contact/dob", true);
				sregReq.addAttribute("dob", false);

				fetch.addAttribute("gender",
						"http://schema.openid.net/contact/gender", false);
				sregReq.addAttribute("gender", false);

				fetch.addAttribute("postcode",
						"http://schema.openid.net/contact/postcode", false);
				sregReq.addAttribute("postcode", false);

				fetch.addAttribute("country",
						"http://schema.openid.net/contact/country", false);
				sregReq.addAttribute("country", false);

				fetch.addAttribute("language",
						"http://schema.openid.net/contact/language", false);
				sregReq.addAttribute("language", false);

				fetch.addAttribute("timezone",
						"http://schema.openid.net/contact/timezone", false);
				sregReq.addAttribute("timezone", false);
	
				// attach the extension to the authentication request
				if (!sregReq.getAttributes().isEmpty()) {
					authReq.addExtension(sregReq);
				}
	
				// Option 1: GET HTTP-redirect to the OpenID Provider endpoint
				// The only method supported in OpenID 1.x
				// redirect-URL usually limited ~2048 bytes
				response.sendRedirect(authReq.getDestinationUrl(true));
			}
			catch (OpenIDException e) {
				LoginBusinessBean bean = new LoginBusinessBean();
				bean.internalSetState(request, LoginState.Failed);
			}
		}
		
		chain.doFilter(request, response);
	}

	public void init(FilterConfig config) throws ServletException {
		manager = (ConsumerManager) IWMainApplication.getDefaultIWApplicationContext().getApplicationAttribute(OpenIDConstants.ATTRIBUTE_CONSUMER_MANAGER);
		if (manager == null) {
			try {
				this.manager = new ConsumerManager();
				this.manager.setAssociations(new InMemoryConsumerAssociationStore());
				this.manager.setNonceVerifier(new InMemoryNonceVerifier(5000));
				IWMainApplication.getDefaultIWApplicationContext().setApplicationAttribute(OpenIDConstants.ATTRIBUTE_CONSUMER_MANAGER, manager);
			}
			catch (ConsumerException e) {
				throw new ServletException(e);
			}
		}
	}
}