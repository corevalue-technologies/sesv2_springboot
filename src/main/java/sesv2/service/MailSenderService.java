/*
 * Copyright (c) 2019, December Technologies Corporation. All rights reserved.
 *
 * File name: MailSenderService.java
 * Author : akshat.khandpur
 * Creation Date: 13-Dec-2019
 */
package sesv2.service;

import javax.mail.MessagingException;

import sesv2.entity.dto.EmailTemplate;
import sesv2.exception.GenericServiceException;

/**
 * The Interface MailSenderService.
 */
@Deprecated
public interface MailSenderService {

	/**
	 * Send mail.
	 *
	 * @param emailTemplate
	 *            the email template
	 * @return 
	 * @throws MessagingException
	 *             the messaging exception
	 * @throws GenericServiceException
	 *             the generic service exception
	 */
	Boolean sendMail(EmailTemplate emailTemplate, String name) throws MessagingException, GenericServiceException;

}
