import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class PDFServlet extends HttpServlet {
    private String url;
    private String dbUsername;
    private String dbPassword;
    private String dbDriver;
    private String encryptionKey;
    private String encryptionAlgorithm;

    @Override
    public void init() throws ServletException {
        url = getServletContext().getInitParameter("dbUrl");
        dbUsername = getServletContext().getInitParameter("dbUsername");
        dbPassword = getServletContext().getInitParameter("dbPassword");
        dbDriver = getServletContext().getInitParameter("dbDriver");
        encryptionKey = getServletContext().getInitParameter("encryptionKey");
        encryptionAlgorithm = getServletContext().getInitParameter("encryptionAlgorithm");
        try {
            Class.forName(dbDriver);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new ServletException("Database driver not found", e);
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        String userRole = (String) session.getAttribute("role");
        String owner = (String) session.getAttribute("username"); // Get the current user logged in

        // Check if the user role or owner is null or empty
        if (userRole == null || userRole.isEmpty() || owner == null || owner.isEmpty()) {
            response.sendRedirect("error_session.jsp");
            return;
        }

        // Fetch data from the database based on the user's role
        StringBuilder reportData = new StringBuilder();
        String reportType = "";
        int totalPages = 0;
        if (userRole.equals("admin")) {
            reportData.append(fetchGuestReportData()).append("\n"); // Append guest data first
            reportData.append(fetchAdminReportData());
            reportType = "Admin Report";
            totalPages = 3; // Set total pages for admin
        } else if (userRole.equals("guest")) {
            reportData.append(fetchGuestReportData(owner));
            reportType = "Guest Report";
            totalPages = 1; // Set total pages for guest
        }

        // Generate PDF with the fetched data and send it to the client
        generatePDF(response, reportData.toString(), reportType, owner, userRole, totalPages);
        session.invalidate();
    }

    private String fetchAdminReportData() {
        StringBuilder adminReport = new StringBuilder();
        try (Connection con = DriverManager.getConnection(url, dbUsername, dbPassword)) {
            String query = "SELECT email, role FROM USERS WHERE role = 'admin'";
            try (PreparedStatement pstmt = con.prepareStatement(query);
                 ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String email = rs.getString("email");
                    String role = rs.getString("role");
                    adminReport.append("Email: ").append(email).append("\n");
                    adminReport.append("Role: ").append(role).append("\n");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return adminReport.toString();
    }

    private String fetchGuestReportData() {
        StringBuilder guestReport = new StringBuilder();
        try (Connection con = DriverManager.getConnection(url, dbUsername, dbPassword)) {
            String query = "SELECT email, role FROM USERS WHERE role = 'guest'";
            try (PreparedStatement pstmt = con.prepareStatement(query);
                 ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String email = rs.getString("email");
                    String role = rs.getString("role");
                    guestReport.append("Email: ").append(email).append("\n");
                    guestReport.append("Role: ").append(role).append("\n");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return guestReport.toString();
    }

    private String fetchGuestReportData(String owner) {
        StringBuilder guestReport = new StringBuilder();
        try (Connection con = DriverManager.getConnection(url, dbUsername, dbPassword)) {
            String query = "SELECT email, Encrypted_Password FROM USERS WHERE email = ? AND role = 'guest'";
            try (PreparedStatement pstmt = con.prepareStatement(query)) {
                pstmt.setString(1, owner);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        String email = rs.getString("email");
                        String encryptedPassword = rs.getString("Encrypted_Password");
                        String decryptedPassword = decrypt(encryptedPassword);
                        guestReport.append("Email: ").append(email).append("\n");
                        guestReport.append("Decrypted Password: ").append(decryptedPassword).append("\n");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return guestReport.toString();
    }

    private void generatePDF(HttpServletResponse response, String reportData, String reportType, String owner, String userRole, int totalPages) {
        Document document = new Document();
        try {
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=\"report.pdf\"");

            PdfWriter writer = PdfWriter.getInstance(document, response.getOutputStream());
            PdfPageEventHelper eventHelper = new PdfPageEventHelper() {
                int currentPage = 0;

                @Override
                public void onStartPage(PdfWriter writer, Document document) {
                    currentPage++;
                }

                @Override
                public void onEndPage(PdfWriter writer, Document document) {
                    PdfContentByte cb = writer.getDirectContent();
                    Font footerFont = new Font(Font.FontFamily.HELVETICA, 10, Font.ITALIC);

                    // PAGE NUMBER
                    Phrase footer = new Phrase(String.format("Page %d of %d", currentPage, totalPages > 0 ? totalPages : currentPage), footerFont);
                    ColumnText.showTextAligned(cb, Element.ALIGN_RIGHT, footer,
                            document.right() - 50, document.bottom() - 20, 0);

                    // OWNER
                    Font italicFont = new Font(Font.FontFamily.HELVETICA, 10, Font.ITALIC);
                    Phrase ownerPhrase = new Phrase("Owner: " + owner, italicFont);
                    ColumnText.showTextAligned(cb, Element.ALIGN_LEFT, ownerPhrase,
                            document.left() + 50, document.bottom() - 20, 0);

                    // LABEL
                    Font labelFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
                    Paragraph label = new Paragraph("Email and Roles:", labelFont);
                    ColumnText.showTextAligned(cb, Element.ALIGN_LEFT, label,
                            document.left() + 50, document.top() + 20, 0);
                }
            };
            writer.setPageEvent(eventHelper);
            writer.setInitialLeading(16);
            document.open();

            // REPORT TYPE
            Font reportTypeFont = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLDITALIC);
            Paragraph reportTypeParagraph = new Paragraph(reportType, reportTypeFont);
            reportTypeParagraph.setAlignment(Paragraph.ALIGN_CENTER);
            document.add(reportTypeParagraph);

            // DATA
            document.add(new Paragraph(reportData));

            document.close();

            System.out.println("PDF generation completed.");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error occurred during PDF generation: " + e.getMessage());
        }
    }

    private String decrypt(String strToDecrypt) {
        String decryptedString = null;
        try {
            Cipher cipher = Cipher.getInstance(encryptionAlgorithm);
            final SecretKeySpec secretKey = new SecretKeySpec(encryptionKey.getBytes(), "AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decryptedBytes = Base64.getDecoder().decode(strToDecrypt);
            decryptedString = new String(cipher.doFinal(decryptedBytes));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return decryptedString;
    }
}
