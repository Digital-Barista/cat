package com.digitalbarista.cat.servlet.filter;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Vector;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * Servlet Filter implementation class ContentTypeByExtensionFilter
 */
public class ContentTypeByExtensionFilter implements Filter {

	private HashMap<String,String> contentTypeMap = new HashMap<String,String>();
	
	public void destroy() {
	}

	/**
	 * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
	 */
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		chain.doFilter(new ContentTypeWrapper((HttpServletRequest)request), response);
	}

	/**
	 * @see Filter#init(FilterConfig)
	 */
	public void init(FilterConfig fConfig) throws ServletException {
		Enumeration<String> names = fConfig.getInitParameterNames();
		while(names.hasMoreElements())
		{
			String name = names.nextElement();
			contentTypeMap.put("."+name,fConfig.getInitParameter(name));
		}
	}

	private class ContentTypeWrapper extends HttpServletRequestWrapper
	{
		public ContentTypeWrapper(HttpServletRequest request)
		{
			super(request);
		}
		
		private String getExtension(String path)
		{
			int index = path.lastIndexOf('.');
			if(index==-1)
				return "";
			String extension = path.substring(index);
			if(contentTypeMap.get(extension) == null)
				return "";
			return extension;
		}

		public String getRequestURI()
		{
			String uri = super.getRequestURI();
			return uri.substring(0,uri.length() - getExtension(uri).length());
		}
		
		public Enumeration getHeaders(String name)
		{
			if("accept".equalsIgnoreCase(name))
			{
				String type = contentTypeMap.get(getExtension(super.getRequestURI()));
				if(type!=null)
				{
					Vector<String> values = new Vector<String>();
					values.add(type);
					return values.elements();
				}
			}
			return super.getHeaders(name);
		}

		@Override
		public StringBuffer getRequestURL() {
			String uri = super.getRequestURL().toString();
			return new StringBuffer().append(uri.substring(0,uri.length() - getExtension(uri).length()));
		}
	}
}
