package com.digitalbarista.cat.servlet;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.digitalbarista.cat.ejb.session.EventManager;
import com.digitalbarista.cat.message.event.CATEvent;

/**
 * Servlet implementation class KannelIncomingMessageServlet
 */
public class KannelIncomingMessageServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
  
	private Logger log = LogManager.getLogger(KannelIncomingMessageServlet.class);
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public KannelIncomingMessageServlet() {
        super();
    }

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		String method = request.getMethod();
		if(!"GET".equals(method) && !"POST".equals(method))
		{
		      String errMsg = ResourceBundle.getBundle("javax.servlet.http.LocalStrings").getString("http.method_not_implemented");
		      Object[] errArgs = new Object[1];
		      errArgs[0] = method;
		      errMsg = MessageFormat.format(errMsg, errArgs);			
		      response.sendError(501, errMsg);
		      return;
		}
	
		try
		{
			String fromAddress=request.getParameter("fromAddress");
			String toAddress=request.getParameter("toAddress");
			String message=request.getParameter("message");
			fromAddress=stripExtraneousChars(fromAddress);
			toAddress=stripExtraneousChars(toAddress);
			CATEvent e = CATEvent.buildIncomingSMSEvent(fromAddress, toAddress, message);
		
			InitialContext ic = new InitialContext();
			EventManager eventManager = (EventManager)ic.lookup("ejb/cat/EventManager");
			eventManager.queueEvent(e);
			response.setStatus(200);
		}catch(Exception ex)
		{
			log.error("Unable to process message.",ex);
			response.sendError(500);
			return;
		}
	}
	
	private String stripExtraneousChars(String source)
	{
		if(source==null)
			return null;
		while(source.length()>0 && source.indexOf("+")==0)
			source=source.substring(1);
		return source;
	}
}
