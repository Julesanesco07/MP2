import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.sql.*;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class LoginServlet extends HttpServlet {
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

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String sessionCaptcha = (String) request.getSession().getAttribute("captcha");
        String inputUsername = request.getParameter("username");
        String inputPassword = request.getParameter("password");
        String inputCaptcha = request.getParameter("captchaInput");
        
        if (inputUsername == null || inputUsername.isEmpty() || inputPassword == null || inputPassword.isEmpty()) {
            response.sendRedirect("error_4.jsp");
            return;
        }
        if (!checkCaptcha(sessionCaptcha, inputCaptcha)) {
            response.sendRedirect("error_5.jsp"); 
            return;
        }

        try (Connection con = DriverManager.getConnection(url, dbUsername, dbPassword)) {
            if (isUserExist(con, inputUsername)) {
                
                String encryptedInputPassword = encrypt(inputPassword, encryptionKey, encryptionAlgorithm); 
                String encryptedPassword = getUserEncryptedPassword(con, inputUsername);

                if (encryptedPassword != null && encryptedPassword.equals(encryptedInputPassword)) {
                
                    String userRole = getUserRole(con, inputUsername);
                    HttpSession session = request.getSession();
                    session.setAttribute("username", inputUsername);
                    session.setAttribute("role", userRole);
                    response.sendRedirect("success.jsp"); 
                } else {
                        response.sendRedirect("error_2.jsp"); 
                    }
                } else {

                    response.sendRedirect("error_1.jsp"); 
                }
        } catch (SQLException sqle) {
            sqle.printStackTrace(); 
            response.sendRedirect("error_1.jsp");
        }
    }
    
//METHODS
    
    //CHECK USER
    private boolean isUserExist(Connection con, String inputUsername) throws SQLException {
        String query = "SELECT * FROM USERS WHERE email = ?";
        try (PreparedStatement pstmt = con.prepareStatement(query)) {
            pstmt.setString(1, inputUsername);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }
    //CHECK USER ROLE
    private String getUserRole(Connection con, String inputUsername) throws SQLException {
        String query = "SELECT role FROM USERS WHERE email = ?";
        try (PreparedStatement pstmt = con.prepareStatement(query)) {
            pstmt.setString(1, inputUsername);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("role");
                }
            }
        }
        return null;
    }
    
    //GET ENCRYPTED PASSWORD
    private String getUserEncryptedPassword(Connection con, String inputUsername) throws SQLException {
        String query = "SELECT Encrypted_Password FROM USERS WHERE email = ?";
        try (PreparedStatement pstmt = con.prepareStatement(query)) {
            pstmt.setString(1, inputUsername);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("Encrypted_Password");
                }
            }
        }
        return null;
    }
    //ENCRYPT INPUT PASSWORD
    private String encrypt(String strToEncrypt, String encryptionKey, String encryptionAlgorithm) {
        String encryptedString = null;
        try {
            Cipher cipher = Cipher.getInstance(encryptionAlgorithm);
            final SecretKeySpec secretKey = new SecretKeySpec(encryptionKey.getBytes(), "AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            encryptedString = Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes()));
        } catch (Exception e) {
            e.printStackTrace(); 
        }
        return encryptedString;
    }
    
    //CHECK CAPTCHA
    private boolean checkCaptcha(String captcha, String userCaptcha) {
        return captcha.equals(userCaptcha);
    }
}
