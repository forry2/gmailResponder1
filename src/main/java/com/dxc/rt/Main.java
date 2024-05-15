package com.dxc.rt;

import javax.mail.*;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.SearchTerm;
import javax.mail.search.SubjectTerm;
import java.util.Properties;

public class Main {
    public static void main(String[] args) {
        final String username = "anonymousa555@gmail.com"; // Inserisci qui il tuo indirizzo email
        final String password = "nwwgzuigvymrgjtk";// Inserisci qui la tua password

        final String sourceAddress = "roberto.bruni@dxc.com";
        final String subjectToSearch = "compleannino quest";

        Properties props = new Properties();
        props.setProperty("mail.imap.ssl.enable", "true");
        props.setProperty("mail.imap.host", "imap.gmail.com");
        props.setProperty("mail.imap.port", "993");

        props.setProperty("mail.smtp.host", "smtp.gmail.com");
        props.setProperty("mail.smtp.port", "587");
        props.setProperty("mail.smtp.auth", "true");
        props.setProperty("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        while (true) {
            try {
                Store store = session.getStore("imap");
                store.connect();

                Folder inbox = store.getFolder("INBOX");
                inbox.open(Folder.READ_WRITE); // Apri la cartella in lettura/scrittura per consentire l'eliminazione

                // Cerca i messaggi con soggetto specifico
                SearchTerm searchTerm = new SubjectTerm(subjectToSearch);
                Message[] messages = inbox.search(searchTerm);

                for (Message message : messages) {
                    Address[] fromAddresses = message.getFrom();
                    for (Address address : fromAddresses) {
                        if (address.toString().contains(sourceAddress)) {
                            // Controlla il contenuto del messaggio
                            Object content = message.getContent();
                            if (content instanceof String) {
                                String messageContent = (String) content;
                                if (messageContent.contains("Ciao Robi")) { // Modifica qui la stringa da cercare
                                    // Invia una risposta
                                    Message reply = new MimeMessage(session);
                                    reply = message.reply(false);
                                    reply.setText("Ciao anche a te");
                                    Transport.send(reply);

                                    // Contrassegna il messaggio come eliminato
                                    message.setFlag(Flags.Flag.DELETED, true);
                                }
                            } else if (content instanceof MimeMultipart) {
                                // Se il contenuto è un MimeMultipart, gestisci le sue parti
                                handleMultipart((MimeMultipart) content, message, session);
                            }
                        }
                    }
                }

                // Elimina definitivamente i messaggi contrassegnati come eliminati
                inbox.expunge();

                inbox.close(false);
                store.close();

                // Attendi 10 secondi prima del prossimo controllo
                Thread.sleep(10000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void handleMultipart(MimeMultipart multipart, Message originalMessage, Session session) throws Exception {
        final String content1_1 = "indizio 1";
        final String content1_2 = "autogrill";
        final String replyContent1 = "Brava, hai trovato il primo indizio! Il secondo indizio è in questo video: https://www.youtube.com/watch?v=2jbQw9n0Nl8";

        int count = multipart.getCount();
        for (int i = 0; i < count; i++) {
            BodyPart part = multipart.getBodyPart(i);
            Object partContent = part.getContent();
            if (partContent instanceof String) {
                String partStringContent = ((String) partContent).toUpperCase();
                if (partStringContent.contains(content1_1.toUpperCase()) && partStringContent.contains(content1_2.toUpperCase())) {
                    // Invia una risposta
                    Message reply = new MimeMessage(session);
                    reply = originalMessage.reply(false);
                    reply.setText(replyContent1);
                    Transport.send(reply);

                    // Contrassegna il messaggio come eliminato
                    originalMessage.setFlag(Flags.Flag.DELETED, true);
                    break; // Esci dal ciclo for
                }
            } else if (partContent instanceof MimeMultipart) {
                // Se la parte è a sua volta un MimeMultipart, gestiscila ricorsivamente
                handleMultipart((MimeMultipart) partContent, originalMessage, session);
            }
        }
    }
}
