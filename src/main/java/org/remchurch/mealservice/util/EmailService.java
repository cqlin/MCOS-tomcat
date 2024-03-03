package org.remchurch.mealservice.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.activation.MimetypesFileTypeMap;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class EmailService {
	private static final String UTF_8 = "UTF-8";
	private static Map<String,String> mailTemplates = new HashMap<>();
	private String from = null;
	private String smtpHostName = null;
	private String smtpPort = null;
	private String user = null;
	private String password = null;
	private ExecutorService executor = Executors.newFixedThreadPool(3);//office allow up to 3 threads
	//432 4.3.2 Concurrent connections limit exceeded. Visit https://aka.ms/concurrent_sending

	static {
		try(Scanner scanner = new Scanner(EmailService.class.getResourceAsStream("/QRCodeEmail.html"), UTF_8)){
			mailTemplates.put("QRCodeEmail", scanner.useDelimiter("\\Z").next());
		}catch(Exception e1){
			e1.printStackTrace();
		}
		try(Scanner scanner = new Scanner(EmailService.class.getResourceAsStream("/DepositEmail.html"), UTF_8)){
			mailTemplates.put("DepositEmail", scanner.useDelimiter("\\Z").next());
		}catch(Exception e1){
			e1.printStackTrace();
		}
		try(Scanner scanner = new Scanner(EmailService.class.getResourceAsStream("/LunchOrderEmail.html"), UTF_8)){
			mailTemplates.put("LunchOrderEmail", scanner.useDelimiter("\\Z").next());
		}catch(Exception e1){
			e1.printStackTrace();
		}
	}

	public EmailService(String smtpHostName, String smtpPort, String from, String user, String password) {
		this.smtpHostName = smtpHostName;
		this.smtpPort = smtpPort;
		this.from = from;
		this.user = user;
		this.password = password;
	}

	public String getMailTemplate(String name) {
		return mailTemplates.get(name);
	}
	public void emailAsync(String emailTo, String subject, String emailBody, String attachment) {
		executor.execute(()->{
			email(emailTo, subject, emailBody, attachment);
		});
	}
	public void email(String emailTo, String subject, String emailBody, String attachment)            
	{
		MimeMessage msg = null;

		try {
			Properties props = new Properties();
			props.put("mail.transport.protocol", "smtp");
			props.put("mail.smtp.host", smtpHostName);
			props.put("mail.smtp.port", smtpPort);
			props.put("mail.debug", "true");
			props.put("mail.smtp.timeout", "3000");
			props.put("mail.smtp.connectiontimeout", "3000");
			//enable SSL
			props.put("mail.smtp.starttls.enable"  , true); //does turn on STARTTLS, 220 Go ahead with TLS

			Session session;
			boolean authEnabled = true;
			if(authEnabled) {
				props.put("mail.smtp.auth", "true");
				javax.mail.Authenticator auth = new javax.mail.Authenticator() {
					protected PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication(user, password);
					}
				};			
				session = Session.getInstance(props, auth);
			} else
				session = Session.getInstance(props, null);
			session.setDebug(true);

			msg = new MimeMessage(session);
			msg.setFrom(new InternetAddress(from));
			msg.setRecipients(Message.RecipientType.TO,	InternetAddress.parse(emailTo,false));
			msg.setSubject(subject,"utf-8");
			msg.setHeader("X-Mailer", "JavaMailer");
			msg.setSentDate(new Date());

			Multipart multipart = new MimeMultipart();

			MimeBodyPart messageBodyPart;

			//set attachment
			String[] attachments = null;
			if(attachment!=null)
				attachments = new String[]{attachment};
			if (attachments != null && attachments.length > 0) {
				FileDataSource source = null;
				for (int i = 0; i < attachments.length; i++) {
					if (attachments[i] != null && attachments[i].trim().length() > 0) {
						messageBodyPart = new MimeBodyPart();
						source = new FileDataSource(attachments[i].trim());
						source.setFileTypeMap(new MimetypesFileTypeMap());
						messageBodyPart.setDataHandler(new DataHandler(source));
						messageBodyPart.setFileName(source.getFile().getName()); //this should be a file name only, no path - see J2EE API
						messageBodyPart.setDisposition("inline");
						String cid = "attachment"+i;
						messageBodyPart.setContentID(cid);
						emailBody+="<img src=\"cid:" + cid + "\" alt=\"img " + cid + "\" />";
						multipart.addBodyPart(messageBodyPart);                        
					}
				}
			}

			messageBodyPart = new MimeBodyPart();

			messageBodyPart.setDataHandler(new DataHandler(new HTMLDataSource(emailBody)));
			multipart.addBodyPart(messageBodyPart);

			msg.setHeader("Content-Type","text/html; charset=\"utf-8\"");
			msg.setContent(multipart);

			Transport.send(msg);
			System.out.println(msg.getMessageID());
		}catch(Exception e){
			e.printStackTrace();
		}
	}    

	static class HTMLDataSource implements javax.activation.DataSource {
		private String html;

		public HTMLDataSource(String htmlString) {
			html = htmlString;
		}

		// Return html string in an InputStream.
		// A new stream must be returned each time.
		public InputStream getInputStream() throws IOException {
			if (html == null) throw new IOException("Null HTML");
			return new ByteArrayInputStream(html.getBytes(UTF_8));
		}

		public OutputStream getOutputStream() throws IOException {
			throw new IOException("This DataHandler cannot write HTML");
		}

		public String getContentType() {
			return "text/html";
		}

		public String getName() {
			return "JAF text/html dataSource to send e-mail only";
		}
	}	


}
