package org.simplejavamail.api.mailer;

import org.hazlewood.connor.bottema.emailaddress.EmailAddressCriteria;
import org.hazlewood.connor.bottema.emailaddress.EmailAddressValidator;
import org.simplejavamail.MailException;
import org.simplejavamail.api.email.Email;
import org.simplejavamail.api.mailer.config.OperationalConfig;
import org.simplejavamail.api.mailer.config.ProxyConfig;
import org.simplejavamail.api.mailer.config.ServerConfig;
import org.simplejavamail.api.mailer.config.TransportStrategy;
import org.simplejavamail.api.mailer.internal.mailsender.MailSender;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.mail.Session;
import java.util.EnumSet;

/**
 * Mailing tool created exclusively using {@link MailerRegularBuilder}. This class is the facade to most Simple Java Mail functionality
 * which is related to doing things with an email (server not always relevant, like with validation, S/MIME encryption etc.).
 * <p>
 * The e-mail message structure is built to work with all e-mail clients and has been tested with many different webclients as well as some desktop
 * applications.
 * <p>
 * <a href="http://www.simplejavamail.org">simplejavamail.org</a> <hr>
 * <p>
 * On a technical note, the {@link Mailer} interface is the front facade for the public API. It limits itself to preparing for sending, but the actual
 * sending and proxy configuration is done by the internal {@link MailSender}.
 *
 * @see MailerRegularBuilder
 * @see Email
 */
public interface Mailer {
	/**
	 * In case Simple Java Mail falls short somehow, you can get a hold of the internal {@link Session} instance to debug or tweak. Please let us know
	 * why you are needing this on https://github.com/bbottema/simple-java-mail/issues.
	 */
	Session getSession();
	
	/**
	 * Delegates to {@link #testConnection(boolean)} with async == <code>false</code>.
	 */
	void testConnection();
	
	/**
	 * Tries to connect to the configured SMTP server, including (authenticated) proxy if set up.
	 * <p>
	 * Note: synchronizes on the thread for sending mails so that we don't get into race condition conflicts with emails actually being sent.
	 *
	 * @return An AsyncResponse in case of async == true, otherwise <code>null</code>.
	 */
	AsyncResponse testConnection(boolean async);
	
	/**
	 * Delegates to {@link #sendMail(Email, boolean)}, with <code>async = false</code>. This method returns only when the email has been processed by
	 * the target SMTP server.
	 */
	void sendMail(Email email);
	
	/**
	 * @see MailSender#send(Email, boolean)
	 * @see #validate(Email)
	 */
	@Nullable
	AsyncResponse sendMail(Email email, @SuppressWarnings("SameParameterValue") boolean async);
	
	/**
	 * Validates an {@link Email} instance. Validation fails if the subject is missing, content is missing, or no recipients are defined or that
	 * the addresses are missing for NPM notification flags.
	 * <p>
	 * It also checks for illegal characters that would facilitate injection attacks:
	 * <ul>
	 * <li>http://www.cakesolutions.net/teamblogs/2008/05/08/email-header-injection-security</li>
	 * <li>https://security.stackexchange.com/a/54100/110048</li>
	 * <li>https://www.owasp.org/index.php/Testing_for_IMAP/SMTP_Injection_(OTG-INPVAL-011)</li>
	 * <li>http://cwe.mitre.org/data/definitions/93.html</li>
	 * </ul>
	 *
	 * @param email The email that needs to be configured correctly.
	 *
	 * @return Always <code>true</code> (throws a {@link MailException} exception if validation fails).
	 * @throws MailException Is being thrown in any of the above causes.
	 * @see EmailAddressValidator
	 */
	@SuppressWarnings({"SameReturnValue" })
	boolean validate(Email email) throws MailException;

	/**
	 * @return The server connection details. Will be {@code null} in case a custom fixed {@link Session} instance is used.
	 * @see MailerRegularBuilder#withSMTPServer(String, Integer, String, String)
	 */
	@Nullable
	ServerConfig getServerConfig();

	/**
	 * @return The transport strategy to be used. Will be {@code null} in case a custom fixed {@link Session} instance is used.
	 * @see org.simplejavamail.api.mailer.MailerRegularBuilder#withTransportStrategy(TransportStrategy)
	 * @see EmailAddressCriteria
	 */
	@Nullable
	TransportStrategy getTransportStrategy();

	/**
	 * @return The proxy connection details. Will be empty if no proxy is required.
	 */
	@Nonnull
	ProxyConfig getProxyConfig();

	/**
	 * @return The operational parameters defined using a mailer builder. Includes general things like session timeouts, debug mode, SSL config etc.
	 */
	@Nonnull
	OperationalConfig getOperationalConfig();

	/**
	 * @return The effective validation criteria used for email validation. Returns an empty set if no validation should be done.
	 * @see MailerGenericBuilder#withEmailAddressCriteria(EnumSet)
	 * @see EmailAddressCriteria
	 */
	@Nonnull
	EnumSet<EmailAddressCriteria> getEmailAddressCriteria();
}
