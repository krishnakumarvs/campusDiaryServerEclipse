/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.security.SecureRandom;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

/**
 *
 * @author kakes
 */
public class MailSender {

	/**
	 * @param args
	 *            the command line arguments
	 */
	private static String USER_NAME = "";
	private static String PASSWORD = "";
	// private static String RECIPIENT = "krishh_mea@yahoo.in";
	private static String RECIPIENT = "";
	private static String host = "";

	public static void main(String[] args) {
		// TODO code application logic here
		String from = USER_NAME;
		String pass = PASSWORD;

		String[] to = { "krishh_mea@yahoo.in" }; // list of recipient email
													// addresses
		String subject = "Testing email";
		String body = "Hi kk";

		// sendFromGMail(to, subject, body, (Configuration.masterPoolLocation +
		// "1472148308713_cipher.jpg"));
		sendFromGMail(to, subject, body);
	}

	public static String generatePassword(int len) {
		String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
		SecureRandom rnd = new SecureRandom();

		StringBuilder sb = new StringBuilder(len);
		for (int i = 0; i < len; i++)
			sb.append(AB.charAt(rnd.nextInt(AB.length())));
		return sb.toString();
	}

	public static void sendFromGMail(String[] to, String subject, String body) {
		Properties props = System.getProperties();

		try {
			if (true) {
				USER_NAME = "stegnographyuc@gmail.com";
				PASSWORD = "stegnographyuc123";
				String Subjects = subject;
				String smtpHost = "smtp.gmail.com";
				String smtpPort = "587";
				String trustSsl = "smtp.gmail.com";
				String smtp_auth = "0";

				props.put("mail.smtp.starttls.enable", "true");
				props.put("mail.smtp.host", smtpHost);
				props.put("mail.smtp.user", USER_NAME);
				props.put("mail.smtp.password", PASSWORD);
				props.put("mail.smtp.port", smtpPort);
				props.put("mail.smtp.auth", smtp_auth);
				props.put("mail.smtp.ssl.trust", trustSsl);
				host = smtpHost;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		Session session = Session.getDefaultInstance(props);
		MimeMessage message = new MimeMessage(session);

		try {
			// message.setFrom(new InternetAddress("stegnography@gmail.com"));
			message.setFrom(new InternetAddress(USER_NAME));
			InternetAddress[] toAddress = new InternetAddress[to.length];

			// To get the array of addresses
			for (int i = 0; i < to.length; i++) {
				toAddress[i] = new InternetAddress(to[i]);
			}

			for (int i = 0; i < toAddress.length; i++) {
				message.addRecipient(Message.RecipientType.TO, toAddress[i]);
			}

			message.setSubject(subject);
			message.setText(body);

			System.out.println("Sending.............");

			Transport transport = session.getTransport("smtp");
			transport.connect(host, USER_NAME, PASSWORD);
			transport.sendMessage(message, message.getAllRecipients());
			transport.close();
			System.out.println("SEND SUCESS");
		} catch (AddressException ae) {
			ae.printStackTrace();
		} catch (MessagingException me) {
			me.printStackTrace();
		}
	}

	public static void sendFromGMail(String[] to, String subject, String body,
			String filePath) {
		Properties props = System.getProperties();

		Dbcon dbcon = new Dbcon();
		ResultSet rs = dbcon.select("select * from tbl_smtp_configuration");
		try {
			if (rs.next()) {
				USER_NAME = rs.getString("sender_email_id");
				PASSWORD = rs.getString("sender_passsword");
				String Subjects = rs.getString("subject") + "-"
						+ System.currentTimeMillis();
				String smtpHost = rs.getString("smtp_host");
				String smtpPort = rs.getString("smtp_port");
				String trustSsl = rs.getString("smtp_trust_ssl");
				String smtp_auth = rs.getString("smtp_auth");
				host = smtpHost;
				props.put("mail.smtp.starttls.enable", "true");
				props.put("mail.smtp.host", smtpHost);
				props.put("mail.smtp.user", USER_NAME);
				props.put("mail.smtp.password", PASSWORD);
				props.put("mail.smtp.port", smtpPort);
				props.put("mail.smtp.auth", smtp_auth);
				props.put("mail.smtp.ssl.trust", trustSsl);

			}
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
		// String host = "smtp.gmail.com";
		// props.put("mail.smtp.starttls.enable", "true");
		// props.put("mail.smtp.host", host);
		// props.put("mail.smtp.user", USER_NAME);
		// props.put("mail.smtp.password", PASSWORD);
		// props.put("mail.smtp.port", "587");
		// props.put("mail.smtp.auth", "true");
		// props.put("mail.smtp.ssl.trust", "smtp.gmail.com");

		Session session = Session.getDefaultInstance(props);
		MimeMessage message = new MimeMessage(session);

		try {
			// message.setFrom(new InternetAddress("stegnography@gmail.com"));
			message.setFrom(new InternetAddress(USER_NAME));
			InternetAddress[] toAddress = new InternetAddress[to.length];

			// To get the array of addresses
			for (int i = 0; i < to.length; i++) {
				toAddress[i] = new InternetAddress(to[i]);
			}

			for (int i = 0; i < toAddress.length; i++) {
				message.addRecipient(Message.RecipientType.TO, toAddress[i]);
			}

			message.setSubject(subject);
			message.setText(body);

			MimeBodyPart messageBodyPart = new MimeBodyPart();
			Multipart multipart = new MimeMultipart();

			messageBodyPart = new MimeBodyPart();
			DataSource source = new FileDataSource(filePath);
			messageBodyPart.setDataHandler(new DataHandler(source));
			messageBodyPart.setFileName(filePath);
			multipart.addBodyPart(messageBodyPart);
			message.setContent(multipart);

			System.out.println("Sending.............");

			Transport transport = session.getTransport("smtp");
			transport.connect(host, USER_NAME, PASSWORD);
			transport.sendMessage(message, message.getAllRecipients());
			transport.close();
			System.out.println("SEND SUCESS");
		} catch (AddressException ae) {
			ae.printStackTrace();
		} catch (MessagingException me) {
			me.printStackTrace();
		}
	}
}
