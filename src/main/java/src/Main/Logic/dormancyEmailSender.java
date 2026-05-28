package src.Main.Logic;

import org.springframework.stereotype.Service;
import src.Main.Utilities.Configurations;
import src.Main.Utilities.DBConnect;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;

@Service
public class dormancyEmailSender {

    public void sendDormancyEmails() {

        Configurations config = new Configurations();
        Properties prop = config.getProperties();

        String beforeDormantSql = prop.getProperty("db.query.beforeDormant");
        String alreadyDormantSql = prop.getProperty("db.query.alreadyDormant");

        DBConnect db = new DBConnect();

        Connection conn = null;
        PreparedStatement ps1 = null;
        PreparedStatement ps2 = null;
        ResultSet rs1 = null;
        ResultSet rs2 = null;

        int beforeCount = 0;
        int alreadyCount = 0;
        int beforeSuccess = 0;
        int alreadySuccess = 0;
        int failed = 0;

        try {

            conn = db.dbConnection();
            System.out.println("Database connection established successfully.");

            // BEFORE DORMANT
            ps1 = conn.prepareStatement(beforeDormantSql);
            rs1 = ps1.executeQuery();

            System.out.println("Fetching accounts about to go dormant...");

            while (rs1.next()) {

                beforeCount++;

                String email = rs1.getString("EMAIL");
                String account = rs1.getString("ACCOUNT_NUMBER");

                if (email != null && !email.trim().isEmpty()) {

                    try {
                        sendEmail(prop, email,
                                "DORMANCY ALERT | ACCOUNT " + account,
                                buildBeforeDormantEmail(account));

                        beforeSuccess++;
                        System.out.println("Sent: " + email);

                    } catch (Exception e) {
                        failed++;
                        System.out.println("FAILED: " + email);
                        e.printStackTrace();
                    }
                }
            }

            System.out.println("Before-dormant processed: " + beforeCount);

            // ALREADY DORMANT
            ps2 = conn.prepareStatement(alreadyDormantSql);
            rs2 = ps2.executeQuery();

            System.out.println("Fetching already dormant accounts...");

            while (rs2.next()) {

                alreadyCount++;

                String email = rs2.getString("EMAIL");
                String account = rs2.getString("ACCOUNT_NUMBER");

                if (email != null && !email.trim().isEmpty()) {

                    try {
                        sendEmail(prop, email,
                                "DORMANT ACCOUNT NOTICE | ACCOUNT " + account,
                                buildDormantEmail(account));

                        alreadySuccess++;
                        System.out.println("Sent: " + email);

                    } catch (Exception e) {
                        failed++;
                        System.out.println("FAILED: " + email);
                        e.printStackTrace();
                    }
                }
            }

            System.out.println("Already-dormant processed: " + alreadyCount);

        } catch (Exception e) {
            System.out.println("Unexpected error");
            e.printStackTrace();

        } finally {

            try {
                if (rs1 != null) rs1.close();
                if (rs2 != null) rs2.close();
                if (ps1 != null) ps1.close();
                if (ps2 != null) ps2.close();
                if (conn != null) conn.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            System.out.println("========== SUMMARY ==========");
            System.out.println("Before sent: " + beforeSuccess);
            System.out.println("Already sent: " + alreadySuccess);
            System.out.println("Failed: " + failed);
        }
    }

    // ================= EMAIL METHOD =================
    private static void sendEmail(
            Properties prop,
            String to,
            String subject,
            String body
    ) throws Exception {

        String host = prop.getProperty("spring.mail.host");
        String username = prop.getProperty("spring.mail.username");
        String password = prop.getProperty("spring.mail.password");

        Properties props = new Properties();
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.auth", "true");

        Session session = Session.getInstance(props,
                new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(username));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject);
        message.setContent(body, "text/html");

        Transport.send(message);
    }

    private static String buildBeforeDormantEmail(String accountNumber)

     {

        return "<html>"
                + "<body style='font-family: Arial, sans-serif;'>"

                + "<p>Dear Valued Client,</p>"

                + "<h3>RE: DORMANCY ALERT</h3>"

                + "<p>"
                + "We trust this letter finds you well."
                + "</p>"

                + "<p>"
                + "Your account <b>" + accountNumber + "</b> is scheduled to become dormant in the next 30 days due to inactivity."
                + "</p>"

                + "<p>"
                + "To keep your account active, please induce a transaction through any of the following channels:"
                + "</p>"

                + "<ul>"
                + "<li>Paybill Number: 842100</li>"
                + "<li>E-Banking: RTGS, PesaLink, M-Pesa, or EFT</li>"
                + "<li>Branch Visit: Any of our branches</li>"
                + "</ul>"

                + "<p>"
                + "Once dormant, no transaction shall be processed until the account is reactivated."
                + "</p>"

                + "<p>"
                + "To prevent future dormancy, we recommend transacting at least once every six months. "
                + "If inactivity continues, your funds may be transferred to the Unclaimed Financial Assets (UFA) authority, as required by law."
                + "</p>"

                + "<p>"
                + "For reactivation support, please visit or contact your nearest branch. "
                + "Our team is ready to assist you."
                + "</p>"

                + "<ul>"
                + "<li>Email: servicedesk@vicbank.com</li>"
                + "<li>Telephone: 0709 876 000 / 221</li>"
                + "</ul>"

                + "<p>"
                + "Thank you for choosing Victoria Commercial Bank PLC."
                + "</p>"

                + "<p>"
                + "Kind Regards,<br>"
                + "<b>Victoria Commercial Bank Limited</b><br>"
                + "ELEVATING RELATIONSHIPS"
                + "</p>"

                + "</body>"
                + "</html>";
    }

    private static String buildDormantEmail(
            String accountNumber
    ) {

        return "<html>"
                + "<body style='font-family: Arial, sans-serif;'>"

                + "<p>Dear Valued Client,</p>"

                + "<h3>RE: DORMANCY ALERT</h3>"

                + "<p>"
                + "We hope this letter finds you well."
                + "</p>"

                + "<p>"
                + "We wish to inform you that account "
                + "<b>" + accountNumber + "</b> "
                + "is currently in dormant status."
                + "</p>"

                + "<p>"
                + "An account falls into dormancy if there is "
                + "no customer induced transaction for six months."
                + "</p>"

                + "<p>"
                + "To avoid this status in the future, we encourage you "
                + "to transact on your account at least once every six months."
                + "</p>"

                + "<p>"
                + "If the account remains inactive, your funds may be "
                + "transferred to the Unclaimed Financial Assets (UFA) "
                + "on the due date, as required by law."
                + "</p>"

                + "<p>"
                + "For assistance with reactivation, please visit or "
                + "contact your nearest branch."
                + "</p>"

                + "<p>"
                + "Our team is ready to support you."
                + "</p>"

                + "<h4>Contact Information:</h4>"

                + "<ul>"
                + "<li>Upper Hill Branch: upperhill@vicbank.com</li>"
                + "<li>Westlands Branch: westlands@vicbank.com</li>"
                + "<li>Lunga Lunga Branch: lungalunga@vicbank.com</li>"
                + "<li>Ruaraka Branch: ruaraka@vicbank.com</li>"
                + "<li>Two Rivers Branch: tworivers@vicbank.com</li>"
                + "<li>Nyali Branch: nyali@vicbank.com</li>"
                + "<li>servicedesk@vicbank.com | Tel: 0709 876 000/221</li>"
                + "</ul>"

                + "<p>"
                + "Also you can click the link below to activate your account:"
                + "</p>"

                + "<p>"
                + "<a href='https://www.victoriabank.co.ke/download/forms/DORMANT_ACCOUNT_REACTIVATION_FORM.pdf'>"
                + "Download Dormant Account Reactivation Form"
                + "</a>"
                + "</p>"

                + "<p>"
                + "We appreciate your prompt attention to this matter "
                + "and look forward to continuing to serve you."
                + "</p>"

                + "<p>"
                + "Kind regards,<br>"
                + "<b>Victoria Commercial Bank PLC</b><br>"
                + "ELEVATING RELATIONSHIPS"
                + "</p>"

                + "</body>"
                + "</html>";
    }
}