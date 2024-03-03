package org.remchurch.mealservice.util;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;

import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestWrapper;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.authenticator.AuthenticatorBase;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.RequestFacade;
import org.apache.catalina.connector.ResponseFacade;
import org.apache.catalina.realm.GenericPrincipal;

public class TomcatAutoLoginEngine extends AuthenticatorBase {

	private Field tomcat_request  ;
	private Field tomcat_response ;

	{
		Field[] fields = null;

		fields = RequestFacade.class.getDeclaredFields ();
		tomcat_request  = filter(fields, "request" );
		tomcat_request.setAccessible (true);

		fields = ResponseFacade.class.getDeclaredFields();
		tomcat_response = filter(fields, "response");
		tomcat_response.setAccessible(true);
	}

	public void authenticate(ServletRequest rq, ServletResponse rs, String user, String password, List<String> roles) {        
		try {
			Request  req = null;
			if(rq instanceof RequestFacade) {
				req = (Request ) tomcat_request.get (rq);
			} else if(rq instanceof Request) {
				req = (Request ) rq;
			} else {
				while (rq != null && rq instanceof ServletRequestWrapper) {
					rq = ((ServletRequestWrapper) rq).getRequest();
					if(rq instanceof RequestFacade) // reached "core"
						break;
				}
				req = (Request ) tomcat_request.get (rq);
			}

			HttpServletResponse res = (HttpServletResponse) rs;
			GenericPrincipal principal = new GenericPrincipal(user,password,roles);
			register(req,res,principal,HttpServletRequest.FORM_AUTH,user,password);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private Field filter(Field[] fields, String name) {
		for (Field field : fields) {
			if (field.getName().equals(name)) {
				return field;
			}
		}
		return null;
	}

	@Override
	protected String getAuthMethod() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean authenticate(Request arg0, HttpServletResponse arg1)
			throws IOException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected boolean doAuthenticate(Request arg0, HttpServletResponse arg1)
			throws IOException {
		// TODO Auto-generated method stub
		return false;
	}
}