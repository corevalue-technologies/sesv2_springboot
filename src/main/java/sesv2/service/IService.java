package sesv2.service;

import java.io.IOException;

import javax.mail.MessagingException;

import org.springframework.web.multipart.MultipartFile;

import sesv2.exception.GenericServiceException;

/**
 * The Interface IService.
 */
public interface IService {

	public Boolean emailDocument(String subject, String email, String message, MultipartFile file)
			throws GenericServiceException, MessagingException, IOException;

}
