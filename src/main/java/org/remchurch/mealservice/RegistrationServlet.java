package org.remchurch.mealservice;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.remchurch.mealservice.util.EmailService;
import org.remchurch.mealservice.util.QRCodeUtil;

//empty url pattern maps the context root, while / maps all non mapped path including static files
@WebServlet(name = "RegistrationServlet", urlPatterns = {"/public/Registration"}, displayName="Registration", initParams = @WebInitParam(name="dsName", value="jdbc/MCOS_DS"))
//@MultipartConfig()
/**
 * 		jsp page directive contentType(charset), and pageEncoding only affects response encoding,
 * 	 form accept-charset also doesn't force post content-type charset header.
 * 	 	changing default application/x-www-form-urlencoded to enctype="multipart/form-data" will cause data to
 * 	 be posted as multipart and need to be read and parsed using multipart.@MultipartConfig()  HttpServletRequest.getParts()
 * 	 Then it is processed same as form field with request.getParameter
 */
public class RegistrationServlet  extends HttpServlet {
	private static final String MEMBER = "member";
	private static final String BLACK = "black";
	private static final String ORANGE = "orange";
	private static final String ERROR = "Error";
	private static final String RED = "red";
	private static final String MESSAGE = "message";
	private static final String STATUS = "status";
	private static final String COLOR = "color";
	private static final Logger log = Logger.getLogger(RegistrationServlet.class.getName());
	private static final long serialVersionUID = 1L;
	private MainService ms = null;
	private EmailService es = null;

	@Override
	public void init() throws ServletException {
		try {
			ms = MainService.getInstance(this.getInitParameter("dsName"));
			es = ms.getEmailService();
		} catch (SQLException e) {
			throw new ServletException("Error creating service.",e);
		}
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.getRequestDispatcher("/WEB-INF/Registration.jsp").forward(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)	throws ServletException, IOException {
		Map<String,String> status = new HashMap<>();
		request.setAttribute(STATUS, status);
		//Collection<Part> parts = request.getParts();
		String memberCode = request.getParameter("member").trim();
		String email = request.getParameter("email").trim();
		String phone = request.getParameter("phone").trim();
		phone = phone.replace("(", "").replace(")", "").replace(" ", "").replace("-", "");
		List<String> lastname = getParameterArray(request,"lastname");
		List<String> firstname = getParameterArray(request,"firstname");
		Map<String, Object> member = null;
		try {
			if(memberCode.startsWith("M")) {
				List<Map<String, Object>> result = ms.getMember(memberCode);
				if(!result.isEmpty())
					member = result.get(0);
				else {
					setStatus(status, RED, ERROR, "Invalid Member Code.");
				}
			}
			if(member!=null &&  member.get("familyEmail")!=null && !((String)member.get("familyEmail")).isBlank()) { //existing member
				setStatus(status, RED, ERROR, "Member Code already registered to:"+member.get("familyEmail"));
			}else if(member!=null) { //guest card
				List<Map<String, Object>> result = ms.searchMember(email);
				if(!result.isEmpty()) {
					Map<String, Object> m = result.get(0);
					setStatus(status, RED, ERROR, String.format("Email already registered to member:%s %s:%s",m.get("FIRST_NAME"), m.get("LAST_NAME"), m.get("MemberCode")));
				}else {
					ms.updateFamily((int)member.get("FAMILY_ID"), email, phone, lastname.get(0), firstname.get(0));
					for(int i=1; i<lastname.size(); i++)
						ms.insertMember((int)member.get("FAMILY_ID"), lastname.get(i), firstname.get(i));
					setStatus(status, BLACK, "Success", String.format("Associated card to member:%s %s:%s",firstname.get(0), lastname.get(0), memberCode));
				}
			}else { //new member
				List<Map<String, Object>> result = ms.searchMember(email);
				if(!result.isEmpty()) {
					member = result.get(0);
					setStatus(status, RED, ERROR, String.format("Email already registered to member:%s %s:%s",member.get("FIRST_NAME"), member.get("LAST_NAME"), member.get("MemberCode")));
				}else {
					int familyId = ms.insertFamily(email, phone, lastname.get(0), firstname.get(0));
					for(int i=0; i<lastname.size(); i++)
						if(!lastname.get(i).isBlank() && !firstname.get(i).isBlank())
							ms.insertMember(familyId, lastname.get(i), firstname.get(i));
					setStatus(status, BLACK, "Success", String.format("Registered member:%s %s:%s",firstname.get(0), lastname.get(0), email));
					sendFamilyEmail(familyId);
				}
			}
			if(member!=null)
				sendFamilyEmail((int)member.get("FAMILY_ID"));
		} catch (SQLException e) {
			log.log(Level.SEVERE, "Sql error.", e);
		}
		request.getRequestDispatcher("/WEB-INF/Registration.jsp").forward(request, response);
	}

	private void sendFamilyEmail(int familyId) throws SQLException {
		String subject = "REM Lunch Account QR code";
		List<Map<String, Object>> f = ms.getFamily(familyId);
		String tempDir = System.getProperty("java.io.tmpdir")+"/";
		URL logoUrl = RegistrationServlet.class.getResource("/rem.png");
		for(Map<String, Object> m:f) {
			String email = (String) m.get("familyEmail");
			String emailBody = es.getMailTemplate("QRCodeEmail")
					.replace("[[firstName]]", (String)m.get("FIRST_NAME"))
					.replace("[[lastName]]", (String)m.get("LAST_NAME"))
					.replace("[[memberId]]", m.get("MEMBER_ID").toString());
			System.out.println(String.format("To:%s, Subject:%s %n %s",email,subject,emailBody));

			Path p = QRCodeUtil.generateQRwithLogo(tempDir,".png",logoUrl.toString(),(String)m.get("MemberCode"),400,400,0.8f,QRCodeUtil.Colors.DARKBLUE);
			if(email!=null && email.trim().length()>3)
				es.emailAsync(email,subject,emailBody,p.toString());
		}
	}

	private static List<String> getParameterArray(HttpServletRequest request, String paramName) {
		int count = 0;
		List<String> ret = new ArrayList<>();
		do {
			String val = request.getParameter(String.format("%s[%s]",paramName,count));
			if(val!=null) {
				ret.add(val.trim());
				count++;
			}else
				count = -1;
		}while(count >= 0);
		return ret;
	}
	private static void setStatus(Map<String, String> status, String color, String code, String message) {
		status.put(COLOR, color);
		status.put(STATUS, code);
		status.put(MESSAGE, message);
	}

}
