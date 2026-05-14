package src.Logic;
import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.activation.FileDataSource;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import org.apache.log4j.PropertyConfigurator;
import src.Utilities.DBConnect;

import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Logger;


public class VcbDormantAccountsAdvice {

    static Logger log = Logger.getLogger(String.valueOf(VcbDormantAccountsAdvice.class));

    public static void main(String[] args) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        PropertyConfigurator.configure("log4j.properties");
        log.info("starting to log.." + dateFormat.format(date));
        try {
            Properties prop = new Properties();
            InputStream input1 = null;
            input1 = new FileInputStream("vcbDormantconfig.properties");
            prop.load(input1);
            log.info("properties file loaded successfully:" + input1);
            DBConnect dbc = new DBConnect();
            String from = prop.getProperty("mailuser");
            String pass = prop.getProperty("mailpass");
            String subject = prop.getProperty("subject");
            String subject1 = prop.getProperty("subject1");
            String body = prop.getProperty("body");
            String body1 = prop.getProperty("body1");
            String host = prop.getProperty("mailhost");
            String tlsenable = prop.getProperty("tlsenable");
            String authenable = prop.getProperty("authenable");
            String mailport = prop.getProperty("mailport");
            String tdbody = prop.getProperty("tdbody");
            String attachmentloc = prop.getProperty("attachmentloc");
            String formname = prop.getProperty("formname");
            Connection con = dbc.prepareConn();
            log.info("Connecting to database..." + dateFormat.format(date));
            String sql = " select * from simba.accounttobedormarnt ";
            log.info("accounts about to be  dormant...");
            log.info("sql 1:" + sql + ":" + dateFormat.format(date));
            String sql1 = "select * from simba.dormantaccounts";
            log.info("accounts already dormant...");
            log.info("sql :" + sql1 + ":" + dateFormat.format(date));
            String sql3 = " select EMAIL,ORGKEY,foracid,acct_name  from  CRMUSER.PHONEEMAIL a,tbaadm.gam b,tbaadm.tam c  where A.ORGKEY=b.cif_id  and  b.acid=c.acid  and maturity_date=trunc(sysdate-15)  and  ORGKEY='R0023623'   and PHONEOREMAIL='EMAIL'   and rownum<5";
            log.info("sql :" + sql3 + ":" + dateFormat.format(date));
            PreparedStatement stmt = con.prepareStatement(sql);
            ResultSet resultSet = stmt.executeQuery();
            PreparedStatement stmt1 = con.prepareStatement(sql1);
            ResultSet resultSet1 = stmt1.executeQuery();
            PreparedStatement stmt3 = con.prepareStatement(sql3);
            ResultSet resultSet3 = stmt3.executeQuery();
            if (!resultSet.isBeforeFirst()) {
                log.info("No Account is to go dormant 15 days from today.." + dateFormat.format(date));
            } else {
                while (resultSet.next()) {
                    String to = resultSet.getString(1);
                    String account = resultSet.getString(3);
                    try {
                        sendFromGMail(from, pass, to, subject + "|ACCOUNT " + account, body, host, tlsenable, authenable, mailport, "N", attachmentloc, formname);
                        log.info("sending mail to:" + to + " for account:" + account + ":AT " + dateFormat.format(date));
                    } catch (Exception ex) {
                        log.info("Error sending mail to:" + to + ":" + "for account:" + account + ex);
                    }
                }
            }
            if (!resultSet1.isBeforeFirst()) {
                log.info("No account has gone dormant today.." + dateFormat.format(date));
            } else {
                while (resultSet1.next()) {
                    System.out.println("Sending second mail");
                    String to = resultSet1.getString(1);
                    String account1 = resultSet1.getString(3);
                    try {
                        sendFromGMail(from, pass, to, subject1 + "|ACCOUNT " + account1, body1, host, tlsenable, authenable, mailport, "Y", attachmentloc, formname);
                        log.info("sending mail to:" + to + "for account:" + account1 + ":AT " + dateFormat.format(date));
                    } catch (Exception ex) {
                        log.info("Error sending mail to:" + to + ":" + ex);
                    }
                }
            }
            dbc.closeConn(con);
        } catch (Exception ex) {
            System.out.println("Database related exception occured,Contact Oracle DBA!" + ex);
            log.info("Database related exception occured,Contact Oracle DBA!" + ex);
        }
    }

    public static void sendFromGMail(String from, String pass, String to, String subject, String body, String host, String tlsenable, String authenable, String mailport, String attach, String attachloc, String formname) {
        Properties props = System.getProperties();
        props.put("mail.smtp.starttls.enable", tlsenable);
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.user", from);
        props.put("mail.smtp.port", mailport);
        props.put("mail.smtp.auth", authenable);
        Session session = Session.getDefaultInstance(props);
        MimeMessage message = new MimeMessage(session);
        try {
            message.setFrom((Address)new InternetAddress(from));
            InternetAddress toAddress = new InternetAddress(to);
            message.addRecipient(Message.RecipientType.TO, (Address)toAddress);
            message.setSubject(subject);
            MimeMultipart mimeMultipart = new MimeMultipart();
            MimeBodyPart textBodyPart = new MimeBodyPart();
            textBodyPart.setText(body);
            message.setContent(body, "html");
            mimeMultipart.addBodyPart((BodyPart)textBodyPart);
            if (attach.equalsIgnoreCase("Y")) {
                MimeBodyPart attachmentBodyPart = new MimeBodyPart();
                FileDataSource fileDataSource = new FileDataSource(attachloc + formname);
                attachmentBodyPart.setDataHandler(new DataHandler((DataSource) fileDataSource));
                attachmentBodyPart.setFileName(formname);
                mimeMultipart.addBodyPart((BodyPart)attachmentBodyPart);
            }
            message.setContent((Multipart)mimeMultipart);
            Transport transport = session.getTransport("smtp");
            transport.connect(host, from, pass);
            transport.sendMessage((Message)message, message.getAllRecipients());
            transport.close();
        } catch (AddressException ae) {
            ae.printStackTrace();
        } catch (MessagingException me) {
            me.printStackTrace();
        }
    }
}