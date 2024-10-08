    package org.sommiersys.sommiersys.service.email;


    import org.springframework.beans.factory.annotation.Autowired;
    import jakarta.activation.DataSource;
    import jakarta.mail.MessagingException;
    import jakarta.mail.util.ByteArrayDataSource;
    import org.springframework.mail.SimpleMailMessage;
    import org.springframework.mail.javamail.JavaMailSender;
    import org.springframework.mail.javamail.MimeMessageHelper;
    import org.springframework.stereotype.Component;

    import java.util.List;


    @Component
    public class EmailService {



        @Autowired
        private JavaMailSender mailSender;


        public void sendEmailWithPDFAttachment(String recipientEmail, String subject, String body, byte[] pdfData, String pdfFileName) throws MessagingException {
            MimeMessageHelper helper = new MimeMessageHelper(mailSender.createMimeMessage(), true);
            helper.setTo(recipientEmail);
            helper.setSubject(subject);
            helper.setText(body);

            // Adjuntar el PDF como un archivo adjunto
            DataSource pdfAttachment = new ByteArrayDataSource(pdfData, "application/pdf");
            helper.addAttachment(pdfFileName, pdfAttachment);

            mailSender.send(helper.getMimeMessage());
        }

        public void enviarCorreo(String destinatario, String asunto, String cuerpo) {
            SimpleMailMessage mensaje = new SimpleMailMessage();
            mensaje.setTo(destinatario);
            mensaje.setSubject(asunto);
            mensaje.setText(cuerpo);
            mailSender.send(mensaje);
        }
        public void enviarCorreos(List<String> destinatario, String asunto, String cuerpo) {
            SimpleMailMessage mensaje = new SimpleMailMessage();
            mensaje.setTo(destinatario.toArray(new String[0]));
            mensaje.setSubject(asunto);
            mensaje.setText(cuerpo);
            mailSender.send(mensaje);
        }
    }

