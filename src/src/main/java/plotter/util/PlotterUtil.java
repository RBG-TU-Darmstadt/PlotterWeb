package plotter.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import plotter.entities.Document;

public class PlotterUtil {

	private static Logger logger = Logger
			.getLogger(PlotterUtil.class.getName());

	public static String getHumanReadable(Long millis) {
		return String.format(
				"%d days, %d min",
				TimeUnit.MILLISECONDS.toDays(millis),
				TimeUnit.MILLISECONDS.toSeconds(millis)
						- TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS
								.toMinutes(millis)));
	}

	public static void sendMail(Document doc) {
		String charset = "ISO-8859-1";
		String contentType = "text/html";
		SimpleDateFormat sdf = new SimpleDateFormat(
				"EEE, d MMM yyyy, hh:mm aaa");
		String dateHumanReadable = sdf.format(doc.getPrintDate());
		String subject = "Druckbestätigung Plot " + doc.getFileName();
		String content = "<H2>Hallo "
				+ doc.getUser().getFirstName()
				+ " "
				+ doc.getUser().getLastName()
				+ ", </H2>"
				+ "Ihr Druckauftrag wird gerade bearbeitet. Sie können das ausgedruckte Dokument in ca. 20 Minuten in Raum S2|02 C210 aus dem Auffangbehälter des Plotters entnehmen. <br> <br>"
				+ "Unten finden sind die Details des Druckauftrags aufgeführt: <br> "
				+ "Dateiname: "
				+ doc.getFileName()
				+ "<br> "
				+ "Format: "
				+ doc.getFormat()
				+ "<br>"
				+ "Seiten: "
				+ doc.getPageCount()
				+ "<br>"
				+ "Druckdatum: "
				+ dateHumanReadable
				+ "<br>"
				+ "Gesamtpreis: "
				+ String.format("%.2f &euro;", doc.getPrice())
				+ "<br><br>"
				+ "Die Kosten werden an Ihre zugehörige Kostenstelle weitergeleitet. Sie müssen sich daher nicht um die Abrechnung kümmern. "
				+ "Bei Rückfragen sind wir per Mail unter plotter@rbg.informatik.tu-darmstadt.de zu erreichen. <br><br>"
				+ "Allerbeste Grüße, <br>" + "PlotterTeam";
		try {
			Properties props = System.getProperties();
			// -- Attaching to default Session, or we could start a new one --
			props.put("mail.smtp.host", "mail.rbg.informatik.tu-darmstadt.de");
			Session session = Session.getDefaultInstance(props, null);
			// -- Create a new message --
			Message msg = new MimeMessage(session);
			// set sender and receiver
			msg.setFrom(new InternetAddress(
					"plotter@rbg.informatik.tu-darmstadt.de"));
			msg.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse(doc.getUser().getEmail(), false));

			msg.setSubject(subject);

			// define first Part
			BodyPart messageBodyPart = new MimeBodyPart();
			messageBodyPart.setContent(content, contentType);
			messageBodyPart.setHeader("Content-Type", contentType
					+ "; charset=" + charset);

			// creating multipart
			MimeMultipart multipart = new MimeMultipart("related");
			multipart.addBodyPart(messageBodyPart);

			// add content and send email
			msg.setContent(multipart, contentType + "; charset=" + charset);
			msg.setSentDate(new Date());
			Transport.send(msg);
			logger.info("Sending e-mail to \"" + doc.getUser().getEmail()
					+ "\", succesfully printed file \" " + doc.getFileName()
					+ "\" on \"" + doc.getFormat() + "\".");
		} catch (AddressException e) {
			logger.severe("Couldn't parse e-mail address ("
					+ doc.getUser().getEmail() + "). E-mail was not sent.");
		} catch (MessagingException e) {
			logger.severe("Couldn't set e-mail content. E-mail was not sent.");
			e.printStackTrace();
		}

	}
}
