package org.remchurch.mealservice;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.security.DeclareRoles;
import javax.servlet.ServletException;
import javax.servlet.annotation.HttpConstraint;
import javax.servlet.annotation.ServletSecurity;
import javax.servlet.annotation.ServletSecurity.TransportGuarantee;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.remchurch.mealservice.util.DateTimeBasedFormat;
import org.remchurch.mealservice.util.EmailService;

// empty url pattern maps the context root, while / maps all non mapped path including static files
@WebServlet(name = "MainServlet", urlPatterns = {"/Main","","/LunchOrder"}, displayName="Main", loadOnStartup = 1,initParams = @WebInitParam(name="dsName", value="jdbc/MCOS_DS"))
@ServletSecurity(value = @HttpConstraint(transportGuarantee = TransportGuarantee.NONE, rolesAllowed={"MCOS_USER","MCOS_DEPOSIT","MCOS_ADMIN","MCOS_GUEST"}))
@DeclareRoles({"MCOS_USER","MCOS_DEPOSIT","MCOS_ADMIN","MCOS_GUEST"})
public class MainServlet extends HttpServlet {

	private static final String BUY = "Buy";
	private static final String RETURN = "Return";
	private static final String MEMBER = "member";
	private static final String BLACK = "black";
	private static final String ORANGE = "orange";
	private static final String ERROR = "Error";
	private static final String MENU_ITEMS = "menuItems";
	private static final String RED = "red";
	private static final String MESSAGE = "message";
	private static final String STATUS = "status";
	private static final String COLOR = "color";
	private static final String CHECKED_OUT = "CheckedOut";
	private static final String MODE = "mode";
	private static final String MEMBER_CODE = "memberCode";
	private static final String TXT_BARCODE = "txtBarcode";
	private static final Logger log = Logger.getLogger(MainServlet.class.getName());
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
		String action = request.getParameter("action");
		if(action==null)
			action = "default";
		if(action.equals("Logout")) {
			request.getSession().invalidate();
			response.sendRedirect(request.getContextPath());
			return;
		}else if(action.equals("default")) {
			request.setAttribute(MENU_ITEMS, ms.getMenuItems());
		}else if(action.equals("Report")) {
			String today = DateTimeBasedFormat.getCurrentDate();
			request.setAttribute("startdate", today);
			request.setAttribute("enddate", today);
		}
		resetSession(request);
		request.getRequestDispatcher("/WEB-INF/"+action+".jsp").forward(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)	throws ServletException, IOException {
		Map<String,String> status = new HashMap<>();
		request.setAttribute(STATUS, status);
		String action = request.getParameter("action");
		try {
			if(action.equals("LunchOrder")) {
				if(request.isUserInRole("MCOS_USER")||request.isUserInRole("MCOS_ADMIN"))
					processLunchOrder(request,status);
				else {
					response.sendError(403,"not authorized.");
					return;
				}
			}else if(action.equals("NewDeposit")) {
				if(request.isUserInRole("MCOS_DEPOSIT")||request.isUserInRole("MCOS_ADMIN"))
					processNewDeposit(request,status);
				else {
					response.sendError(403,"not authorized.");
					return;
				}
			}else if(action.equals("Report")) {
				if(request.isUserInRole("MCOS_ADMIN"))
					processReport(request,status);
				else {
					response.sendError(403,"not authorized.");
					return;
				}
			}else if(action.equals("Search")) {
				if(request.isUserInRole("MCOS_ADMIN"))
					processSearch(request,status);
				else {
					response.sendError(403,"not authorized.");
					return;
				}
			}else if(action.equals("NewUser")) {
				if(request.isUserInRole("MCOS_ADMIN"))
					processNewUser(request,status);
				else {
					response.sendError(403,"not authorized.");
					return;
				}
			}else if(action.equals("MyAccount")) {
				processMyAccount(request,status);
				response.setHeader("Refresh", "5; url=Main?action=MyAccount");
			}else if(action.equals("SetPassword")) {
				processSetPassword(request,status);
			}
		} catch (SQLException e) {
			log.log(Level.SEVERE, "Sql error.", e);
		}
		request.getRequestDispatcher("/WEB-INF/"+action+".jsp").forward(request, response);
	}

	private void processSetPassword(HttpServletRequest request, Map<String, String> status) throws SQLException {
		long start = System.currentTimeMillis();
		String username = (String) request.getSession().getAttribute("username");
		String oldPassword = request.getParameter("txtPasswordOld");
		String newPassword = request.getParameter("txtPassword1");
		String password2 = request.getParameter("txtPassword2");
		if (newPassword.equals(password2))
		{
			if(ms.changePassword(username,oldPassword,newPassword))
				setStatus(status, BLACK,"Success", "Password changed successfully.");              
			else
				setStatus(status, RED, ERROR, "Wrong old password.");              
		}
		else
		{
			setStatus(status, RED, ERROR, "New passwords don't match.");
		}
		status.put("time", String.valueOf(System.currentTimeMillis()-start));
	}

	private void processNewUser(HttpServletRequest request, Map<String, String> status) {
		long start = System.currentTimeMillis();
		String username = request.getParameter("username").toUpperCase();
		String member = request.getParameter(MEMBER);
		String password = request.getParameter("txtPassword");
		String role = request.getParameter("role");
		try {
			if(member.startsWith("M")) {
				member = ms.getMember(member).get(0).get("MEMBER_ID").toString();
			}
			ms.addUser(username,password,member,role);
			setStatus(status, BLACK, "Success", "Added Operator:"+username);
		}catch(SQLException e) {
			setStatus(status, RED, ERROR, e.toString());
		}
		status.put("time", String.valueOf(System.currentTimeMillis()-start));
	}

	private void processMyAccount(HttpServletRequest request, Map<String, String> status) throws SQLException {
		long start = System.currentTimeMillis();
		String barcode = request.getParameter(TXT_BARCODE).toUpperCase();
		String memberCode = request.getParameter(MEMBER_CODE);
		Map<String, Object> member = null;

		if((memberCode == null || memberCode.isBlank()) && !barcode.startsWith("M")) {
			setStatus(status, RED, ERROR, "Please Scan Member barcode.");
			return;
		}
		if(barcode.length()>1) {
			if(barcode.startsWith("M")) {
				List<Map<String, Object>> m = ms.getMember(barcode);

				if (!m.isEmpty()) {
					resetSession(request);
					member = m.get(0);
					memberCode = barcode;
					request.setAttribute(MEMBER, member);
					List<Map<String, Object>> depositHistory = ms.getMemberDepositHistory((int) member.get("FAMILY_ID"), 5);
					request.setAttribute("depositHistory",depositHistory);
					List<Map<String, Object>> orderHistory = ms.getMemberOrderHistory((int) member.get("FAMILY_ID"), 5);
					request.setAttribute("orderHistory",orderHistory);
				} else {
					setStatus(status, ORANGE, ERROR, "Your Family Account could not be found. ");
				}
			}else {
				setStatus(status, ORANGE, ERROR, "Invalid barcode. ");
			}
		}
		status.put("time", String.valueOf(System.currentTimeMillis()-start));
	}

	private void processSearch(HttpServletRequest request, Map<String, String> status) throws SQLException {
		String barcode = request.getParameter(TXT_BARCODE).trim();
		List<Map<String, Object>> result = ms.searchMember(barcode);
		request.setAttribute("result",result);
	}

	private void processReport(HttpServletRequest request, Map<String, String> status) throws SQLException {
		long start = System.currentTimeMillis();
		String startdateStr = request.getParameter("startdate");
		String enddateStr = request.getParameter("enddate");
		String reportType = request.getParameter("ReportType");
		request.setAttribute("startdate",startdateStr);
		request.setAttribute("enddate",enddateStr);
		Date startdate = new Date(DateTimeBasedFormat.parseDate(startdateStr));
		Date enddate = new Date(DateTimeBasedFormat.parseDate(enddateStr));
		List<Map<String, Object>> result;
		switch(reportType) {
		case "order":
			result = ms.getOrderReport(startdateStr,enddateStr);
			break;
		case "orderDetail":
			result = ms.getOrderDetailReport(startdateStr,enddateStr);
			break;
		case "orderSummary":
			result = ms.getOrderSummaryReport(startdateStr,enddateStr);
			break;
		case "deposit":
			result = ms.getDepositReport(startdateStr,enddateStr);
			break;
		default:
			result = new ArrayList<>();
		}
		List<String> columns = new ArrayList<>();
		List<List<Object>> values = new ArrayList<>();
		for(int i=0; i<result.size(); i++) {
			if(i==0) 
				for(String c:result.get(i).keySet()) {
					columns.add(c);
				}
			List<Object> row = new ArrayList<>();
			values.add(row);
			for(Object o:result.get(i).values()) {
				row.add(o);
			}
		}
		request.setAttribute("reportCols", columns);
		request.setAttribute("reportValues", values);
		status.put("time", String.valueOf(System.currentTimeMillis()-start));
	}

	private void processNewDeposit(HttpServletRequest request, Map<String, String> status) throws SQLException {
		long start = System.currentTimeMillis();
		String barcode = request.getParameter(TXT_BARCODE).toUpperCase();
		String memberCode = request.getParameter(MEMBER_CODE);
		Map<String, Object> member = null;

		if((memberCode == null || memberCode.isBlank()) && !barcode.startsWith("M")) {
			setStatus(status, RED, ERROR, "Please Scan Member barcode.");
			return;
		}else if(memberCode != null && !memberCode.isBlank()) {
			request.setAttribute(MEMBER_CODE, memberCode);
			List<Map<String, Object>> m = ms.getMember(memberCode);
			member = m.get(0);
			request.setAttribute(MEMBER, member);
		}
		if(barcode.length()>1) {
			if(barcode.startsWith("M")) {
				List<Map<String, Object>> m = ms.getMember(barcode);

				if (!m.isEmpty()) {
					resetSession(request);
					member = m.get(0);
					memberCode = barcode;
					request.setAttribute(MEMBER_CODE, memberCode);
					request.setAttribute(MEMBER, member);
				} else {
					setStatus(status, ORANGE, ERROR, "Your Family Account could not be found. ");
				}
			}else {
				setStatus(status, ORANGE, ERROR, "Invalid barcode. ");
			}
		}else {
			Double depositAmount = Double.valueOf(request.getParameter("txtDepositAmount"));
			String depositType = request.getParameter("DepositType");
			double balance = depositAmount+((BigDecimal)member.get("Balance")).doubleValue();
			if(balance<0) {
				setStatus(status, RED, ERROR, "Not enough fund to refund. ");
			}else {
				ms.insertDeposit(member, depositType, depositAmount, balance,request.getUserPrincipal().getName());

				request.removeAttribute(MEMBER_CODE);
				List<Map<String, Object>> m = ms.getMember(memberCode);
				Map<String, Object> member2 = m.get(0);
				request.setAttribute("member2", member2);

				request.setAttribute("confirmMessage", String.format("$%.2f added. Your Current Balance is $%.2f",depositAmount,balance));
				List<Map<String, Object>> depositHistory = ms.getMemberDepositHistory((int) member.get("FAMILY_ID"), 5);
				request.setAttribute("depositHistory",depositHistory);

				String familyEmail = (String) member.get("familyEmail");
				if(familyEmail!=null && !familyEmail.isBlank()) {
					String template = es.getMailTemplate("DepositEmail");
					String emailBody = template.replace("{MemberName}", member.get("FIRST_NAME")+" "+member.get("LAST_NAME"))
							.replace("{MemberCode}", (String)member.get("MemberCode"))
							.replace("{Deposit}", String.valueOf(depositAmount))
							.replace("{DepositType}", depositType)
							.replace("{Balance}", String.valueOf(balance));
					es.emailAsync(familyEmail, "Your REM Meal Deposit on "+DateTimeBasedFormat.getCurrentDate(), emailBody, null);
				}
			}
		}
		request.setAttribute("lblTotal", ms.getCurrentDepositTotal(request.getUserPrincipal().getName()).toString());
		status.put("time", String.valueOf(System.currentTimeMillis()-start));
	}

	private static void setStatus(Map<String, String> status, String color, String code, String message) {
		status.put(COLOR, color);
		status.put(STATUS, code);
		status.put(MESSAGE, message);
	}

	private static void resetSession(HttpServletRequest request) {
		//Reset Data
		request.getSession().removeAttribute("miList");
		request.getSession().removeAttribute("orderList");
		request.getSession().removeAttribute("orderDetailList");
		request.removeAttribute(MEMBER_CODE);
		request.removeAttribute(MEMBER);
		request.removeAttribute(MODE);
	}

	private static final String[] orderColumns = {"ItemID","Itemcode","ItemDescription","ItemPrice","quantity"};
	private void processLunchOrder(HttpServletRequest request, Map<String, String> status) throws SQLException {
		long start = System.currentTimeMillis();
		String barcode = request.getParameter(TXT_BARCODE).toUpperCase().trim();
		String memberCode = request.getParameter(MEMBER_CODE);
		String mode = request.getParameter(MODE);
		Map<String, Object> member = null;

		if((memberCode == null || memberCode.isBlank() || CHECKED_OUT.equals(mode)) && !barcode.startsWith("M")) {
			setStatus(status, RED, ERROR, "Please Scan Member barcode first.");
			return;
		}else if(memberCode != null && !memberCode.isBlank()) {
			request.setAttribute(MEMBER_CODE, memberCode);
			List<Map<String, Object>> m = ms.getMember(memberCode);
			member = m.get(0);
			request.setAttribute(MEMBER, member);
			request.setAttribute(MODE, mode);
		}

		if(request.getParameter("btnB0031.x")!=null) {
			barcode = "B0031";
		}else if(request.getParameter("btnB0033.x")!=null) {
			barcode = "B0033";
		}else if(request.getParameter("btnB0035.x")!=null) {
			barcode = "B0035";
		}else if(request.getParameter("btnB0037.x")!=null) {
			barcode = "B0037";
		}else if(request.getParameter("btnC.x")!=null) {
			barcode = "C";
		}

		List<Map<String, Object>> miList = (List<Map<String, Object>>) request.getSession().getAttribute("miList");

		if (barcode.length() == 1){
			if (barcode.equals("R")) { // Return Mode
				if (miList!=null){
					setStatus(status, RED, ERROR, "In Purchase Mode. Can't return.");
				}else {
					mode = RETURN;
					request.setAttribute(MODE, mode);

					List<Map<String, Object>> orderList = ms.getMemberOrderToday(member.get("MEMBER_ID").toString());
					request.getSession().setAttribute("orderList", orderList);

					List<Map<String, Object>> orderDetailList = new ArrayList<>();
					for(Map<String, Object> order:orderList) {
						log.fine(order.keySet().toString());
						for(Map<String, Object> orderDetail:(List<Map<String, Object>>)order.get("orderDetail")) {
							Map<String,Object> detail = new HashMap<>();
							detail.putAll(orderDetail);
							detail.putAll(ms.getMenuIdMap().get(detail.get("ItemID").toString()));
							log.fine(detail.keySet().toString());
							orderDetailList.add(detail);
						}
					}
					request.getSession().setAttribute("orderDetailList", orderDetailList);

					setStatus(status, "blue", "Returning...", "");
				}
			}else if (barcode.equals("M")) {
				resetSession(request);
			}else if (barcode.equals("C")) {
				if (CHECKED_OUT.equals(mode)) {
					setStatus(status, RED, ERROR, "already Checked Out.");
				} else{ 
					if (miList==null || miList.isEmpty()) {
						setStatus(status, RED, ERROR, "Empty Order.");
					} else {
						double total = 0;
						double balance = 0;
						for(Map<String, Object> mi:miList) {
							total += ((Integer)mi.get("quantity"))*((BigDecimal)mi.get("ItemPrice")).doubleValue();
						}
						request.setAttribute("orderAmount", total);
						balance = ((BigDecimal)member.get("Balance")).doubleValue()-total;
						if (balance < 0){
							setStatus(status, RED, ERROR, "Not enough money to check out, Please re-do.");
						}else{   
							int orderId = ms.InsertOrderDetailList(member,miList, total, balance, request.getUserPrincipal().getName());
							status.put("total", String.valueOf(total));
							status.put("balance", String.valueOf(balance));
							mode = CHECKED_OUT;
							request.setAttribute(MODE, mode);
							setStatus(status, BLACK, "Success", "Order completed.");

							String reminder = "";
							if(balance<10)
								reminder = "Your balance is less than $10.";
							String familyEmail = (String) member.get("familyEmail");
							if(familyEmail!=null && !familyEmail.isBlank()) {
								String template = es.getMailTemplate("LunchOrderEmail");
								String emailBody = template.replace("{MemberName}", member.get("FIRST_NAME")+" "+member.get("LAST_NAME"))
										.replace("{MemberCode}", (String)member.get("MemberCode"))
										.replace("{OrderID}", String.valueOf(orderId))
										.replace("{Total}", String.valueOf(total))
										.replace("{Balance}", String.valueOf(balance))
										.replace("{BalanceReminder}", reminder)
										.replace("{OrderDetails}", listToHtml(miList,orderColumns));
								es.emailAsync(familyEmail, "Your REM Meal Purchase on "+DateTimeBasedFormat.getCurrentDate(), emailBody, null);
							}
						}
					}
				}
			}else {
				status.put(COLOR, RED);
				status.put(STATUS, ERROR);
				status.put(MESSAGE, "Invalid Barcode.");
			}
		}else if(barcode.length()>1){
			String firstBarcode = barcode.substring(0, 1);

			switch (firstBarcode) {
			case "M":
				List<Map<String, Object>> m = ms.getMember(barcode);

				if (!m.isEmpty()) {
					resetSession(request);
					member = m.get(0);
					memberCode = barcode;
					request.setAttribute(MEMBER_CODE, memberCode);
					request.setAttribute(MEMBER, member);

					if (Double.valueOf(member.get("Balance").toString())<1.25) {
						setStatus(status, RED, ERROR, "Your balance is too low.");
					} else {
						setStatus(status, BLACK, "Purchase or Return?", "");
					}
				} else {
					setStatus(status, ORANGE, ERROR, "Your Family Account could not be found. ");
				}
				break;
			case "B": // Buy                    
				if (memberCode == null || memberCode.isBlank() || CHECKED_OUT.equals(mode)){
					setStatus(status, RED, ERROR, "Please Scan Meal Card first.");
				} else if (RETURN.equals(mode)) {
					request.setAttribute(MODE, mode);
					request.setAttribute(MEMBER_CODE, memberCode);
					setStatus(status, ORANGE, ERROR, "In Return Mode. Can't buy.");
				} else { // Buy
					mode = BUY;
					request.setAttribute(MODE, mode);
					request.setAttribute(MEMBER_CODE, memberCode);
					Map<String, Object> menuitem = ms.getMenuItem(barcode);

					if (menuitem == null) {
						setStatus(status, ORANGE, ERROR, "Invalid Menu Barcode.");
					} else {
						menuitem = new HashMap<>(menuitem);
						if(miList == null) {
							miList = new ArrayList<>();
							request.getSession().setAttribute("miList", miList);
						}

						boolean found = false;
						double total = 0;
						for(Map<String, Object> mi:miList) {
							if(mi.get("ItemID").equals(menuitem.get("ItemID"))) {
								mi.put("quantity", ((Integer)mi.get("quantity"))+1);
								found = true;
							}
							total += ((Integer)mi.get("quantity"))*((BigDecimal)mi.get("ItemPrice")).doubleValue();
						}
						if(!found) {
							miList.add(menuitem);
							menuitem.put("quantity", 1);
							total += ((BigDecimal)menuitem.get("ItemPrice")).doubleValue();
						}
						request.setAttribute("orderAmount", total);
						if (Double.valueOf(member.get("Balance").toString())>= total) {
							setStatus(status, BLACK, "Purchasing... ", "");
						} else {
							setStatus(status, RED, ERROR, "Your balance is too low.");
						}
					}
				}
				break;
			case "D": // Delete/Return  
				if (memberCode == null || memberCode.isBlank() || CHECKED_OUT.equals(mode)){
					setStatus(status, RED, ERROR, "Please Scan Meal Card first.");
				} else { 
					request.setAttribute(MODE, mode);
					request.setAttribute(MEMBER_CODE, memberCode);
					Map<String, Object> menuitem = ms.getMenuItem(barcode);
					if (menuitem == null) {
						setStatus(status, ORANGE, ERROR, "Invalid Menu Barcode.");
					} else {
						menuitem = new HashMap<>(menuitem);
						if(miList == null) {
							miList = new ArrayList<>();
							request.getSession().setAttribute("miList", miList);
						}
						int id = (Integer) menuitem.get("ItemID");
						if(RETURN.equals(mode)) {
							int count = 0;
							for(Map<String, Object> o:(List<Map<String, Object>>)request.getSession().getAttribute("orderDetailList")) {
								if(o.get("ItemID").equals(id-1)) {
									count+=(Integer)o.get("ItemQuantity");
								}else if(o.get("ItemID").equals(id)) {
									count-=(Integer)o.get("ItemQuantity");
								}
							}
							Map<String, Object> mi = null;
							double total = 0;
							for(Map<String, Object> o:miList) {
								if(o.get("ItemID").equals(id)) {
									count-=(Integer)o.get("quantity");
									mi = o;
								}
								total += ((Integer)o.get("quantity"))*((BigDecimal)o.get("ItemPrice")).doubleValue();
							}
							if(count>=0) {
								if(mi!=null) {
									mi.put("quantity", ((Integer)mi.get("quantity"))+1);
								}else {
									miList.add(menuitem);
									menuitem.put("quantity", 1);
								}
								total += ((BigDecimal)menuitem.get("ItemPrice")).doubleValue();
								setStatus(status, BLACK, "Returning... ", "Item returned");
							}else {
								setStatus(status, ORANGE, "Returning... ", "No more item to return");
							}
							request.setAttribute("orderAmount", total);
						}else {
							boolean found = false;
							double total = 0;
							Iterator<Map<String, Object>> i = miList.iterator();
							while(i.hasNext()) {
								Map<String, Object> o = i.next();
								if(o.get("ItemID").equals(id-1)) {
									found = true;
									int count = (Integer)o.get("quantity");
									if(count>1)
										o.put("quantity", count-1);
									else {
										o.put("quantity", 0);
										i.remove();
									}
								}
								total += ((Integer)o.get("quantity"))*((BigDecimal)o.get("ItemPrice")).doubleValue();
							}
							request.setAttribute("orderAmount", total);
							if(found) {
								setStatus(status, BLACK, "Purchasing... ", "Item removed");
							}else {
								setStatus(status, ORANGE, "Purchasing... ", "No more item to remove");
							}
						}
					}
				}
				break;
			default:
				request.setAttribute(MODE, mode);
				request.setAttribute(MEMBER_CODE, memberCode);
				setStatus(status, RED, ERROR, "Invalid Barcode.");
				break;
			}
		}else {
			if(memberCode == null || memberCode.isBlank()) {
				setStatus(status, BLACK, "Start", "Please scan member barcode");
			}else if(RETURN.equals(mode)){
				setStatus(status, BLACK, "Returning... ", "");
			}else if(BUY.equals(mode)) {
				setStatus(status, BLACK, "Purchasing... ", "");
			}
		}
		status.put("time", String.valueOf(System.currentTimeMillis()-start));
	}

	private static String listToHtml(List<Map<String, Object>> list, String[] columns) {
		StringBuilder sb = new StringBuilder();
		sb.append("<table><tr>");
		for(String c:columns)
			sb.append("<th scope=\"col\">").append(c).append("</th>");
		sb.append("</tr>\n");
		for(Map<String, Object> r:list) {
			sb.append("<tr>");
			for(String c:columns)
				sb.append("<td>").append(r.get(c)).append("</td>");
			sb.append("</tr>\n");
		}
		sb.append("</table>");
		return sb.toString();
	}

}

