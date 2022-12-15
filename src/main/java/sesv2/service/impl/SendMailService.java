package sesv2.service.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import sesv2.entity.dto.EmailTemplate;
import sesv2.exception.GenericServiceException;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.sesv2.SesV2Client;
import software.amazon.awssdk.services.sesv2.model.EmailContent;
import software.amazon.awssdk.services.sesv2.model.RawMessage;
import software.amazon.awssdk.services.sesv2.model.SendEmailRequest;

/**
 * The Class SendMessageAttachment.
 */
@Service
public class SendMailService {

	private static final Log logger = LogFactory.getLog(SendMailService.class);

	@Autowired
	private SesV2Client sesv2Client;

	
	public Boolean sendMail(final EmailTemplate emailTemplate) throws MessagingException, IOException {

		try {
			String sender = emailTemplate.getRecipient().getFromAddress();

			String recipients = emailTemplate.getRecipient().getToRecipient();
			String recipient = String.join(",", recipients);

			String subject = emailTemplate.getSubject();

			byte[] file = emailTemplate.getAttachment();

			// The HTML body of the email.
			String bodyHTML = emailTemplate.getBody();

			sendEmailAttachment(sesv2Client, sender, recipient, subject, bodyHTML, file, "doc",
					emailTemplate.isAddAttachment());
		} catch (Exception ex) {
			logger.error("Email not sent!", ex);
			return Boolean.FALSE;
		}
		return Boolean.TRUE;

	}

	
	public static void sendEmailAttachment(SesV2Client sesv2Client, String sender, String recipient, String subject,
			String bodyHTML, byte[] file, String name, boolean isAttachmentPresent)
			throws MessagingException, IOException, GenericServiceException {

		Session session = Session.getDefaultInstance(new Properties());

		// Create a new MimeMessage object.
		MimeMessage message = new MimeMessage(session);

		// Add subject and add from and to lines.
		message.setSubject(subject, "UTF-8");
		message.setFrom(new InternetAddress(sender));
		message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));

		// Create a multipart/alternative child container.
		MimeMultipart msgBody = new MimeMultipart("alternative");

		// Create a wrapper for the HTML and text parts.
		MimeBodyPart wrap = new MimeBodyPart();

		// Define the HTML part.
		MimeBodyPart htmlPart = new MimeBodyPart();
		htmlPart.setContent(bodyHTML, "text/html; charset=UTF-8");

		// Add the HTML parts to the child container.
		// msgBody.addBodyPart(textPart);
		msgBody.addBodyPart(htmlPart);

		// Add the child container to the wrapper object.
		wrap.setContent(msgBody);

		// Create a multipart/mixed parent container.
		MimeMultipart msg = new MimeMultipart("mixed");

		// Add the parent container to the message.
		message.setContent(msg);

		// Add the multipart/alternative part to the message.
		msg.addBodyPart(wrap);

		// Define the attachment.
		if (isAttachmentPresent) {
			MimeBodyPart att = new MimeBodyPart();
			DataSource fds = new ByteArrayDataSource(file, "application/pdf");
			att.setDataHandler(new DataHandler(fds));

			att.setFileName(name);

			// Add the attachment to the message.
			msg.addBodyPart(att);
		}

		logger.info("Attempting to send an email through Amazon SES using the AWS SDK for Java...");
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		message.writeTo(outputStream);

		ByteBuffer buf = ByteBuffer.wrap(outputStream.toByteArray());
		byte[] arr = new byte[buf.remaining()];
		buf.get(arr);

		SdkBytes data = SdkBytes.fromByteArray(arr);
		RawMessage rawMessage = RawMessage.builder().data(data).build();

		EmailContent emailContent = EmailContent.builder().raw(rawMessage).build();

		SendEmailRequest request = SendEmailRequest.builder().content(emailContent).build();

		sesv2Client.sendEmail(request);

		logger.info("The email message was successfully sent with an attachment");
	}
}
