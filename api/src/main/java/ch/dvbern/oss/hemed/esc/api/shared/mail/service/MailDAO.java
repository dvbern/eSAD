package ch.dvbern.oss.hemed.esc.api.shared.mail.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.fileio.FileUtil;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import io.quarkus.mailer.reactive.ReactiveMailer;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

@ApplicationScoped
@RequiredArgsConstructor
public class MailDAO {

    private final MailHelperService mailHelperService;
    private final Mailer mailer;
    private final ReactiveMailer reactiveMailer;

    public Uni<Void> sendEmail(String to, String subject, String htmlContent) {
        var mail = createMail(to, subject, htmlContent);
        return reactiveMailer.send(mail);
    }

    public void sendEmailSync(String to, String subject, String htmlContent) {
        var mail = createMail(to, subject, htmlContent);
        mailer.send(mail);
    }

    public Uni<Void> sendEmailWithAttachment(String to, String subject, String htmlContent, List<Path> attachments)
        throws IOException {
        var mail = createMail(to, subject, htmlContent, attachments);
        return reactiveMailer.send(mail);
    }

    public void sendEmailWithAttachmentSync(String to, String subject, String htmlContent, List<Path> attachments)
        throws IOException {
        var mail = createMail(
            to,
            subject,
            htmlContent,
            attachments);
        mailer.send(mail);
    }

    private Mail createMail(@NonNull String to, String subject, String htmlContent) {
        @NonNull List<String> preparedTo = mailHelperService.prepareRecipients(to);
        String preparedSubject = mailHelperService.prepareSubject(subject);
        String preparedContent = mailHelperService.prepareMessage(htmlContent, to);
        Mail mail = new Mail();
        mail.setTo(preparedTo);
        mail.setSubject(preparedSubject);
        mail.setHtml(preparedContent);
        return mail;
    }

    private Mail createMail(String to, String subject, String htmlContent, List<Path> attachments) throws IOException {

        var mail = createMail(
            to,
            subject,
            htmlContent
        );
        addAttachments(mail, attachments);
        return mail;
    }

    private void addAttachments(Mail mail, List<Path> attachments) throws IOException {
        for (var attachment : attachments) {
            mail.addAttachment(
                attachment.getFileName().toString(),
                Files.readAllBytes(attachment),
                FileUtil.parseMediaTypeFromContent(attachment).toString()
            );
        }
    }
}
