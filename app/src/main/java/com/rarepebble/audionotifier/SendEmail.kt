package com.rarepebble.audionotifier

import java.util.*
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage
import kotlin.concurrent.thread


internal fun sendEmailAsync(
        smtpHost: String, port: String,
        username: String, password: String,
        from: String, to: String, subject: String, text: String,
        log: (String) -> Unit) {

    thread(start = true) {
        try {
            log("Sending message...")
            sendEmail(
                    smtpHost, port,
                    username, password,
                    from, to, subject, text)
            log("Message sent")
        } catch (e: Throwable) {
            log("SEND FAILED\n${e}")
        }
    }
}

internal fun sendEmail(
        smtpHost: String, port: String,
        username: String, password: String,
        from: String, to: String, subject: String, text: String) {

    val props = Properties()
    props.put("mail.smtp.auth", "true")
    props.put("mail.smtp.starttls.enable", "true")
    props.put("mail.smtp.host", smtpHost)
    props.put("mail.smtp.port", port)
    props.put("mail.smtp.connectiontimeout", "60000")
    props.put("mail.smtp.timeout", "60000")

    val session = Session.getInstance(
            props,
            object : javax.mail.Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(username, password)
                }
            })
    val message = MimeMessage(session)
    message.setFrom(InternetAddress(from))
    message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to))
    message.setSubject(subject)
    message.setText(text, "utf8")

//        val multipart = MimeMultipart()
//
//        val messageBodyPart = MimeBodyPart()
//
//        message.setContent(multipart)

    Transport.send(message)
}