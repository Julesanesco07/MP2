import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.sql.*;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Base64;

public class LoginServlet extends HttpServlet {
    private String url;
    private String dbUsername;
    private String dbPassword;
    private String dbDriver;

    // Encryption key
    private static byte[] key = {'a','n','e','s','c','o',
                                  'n','g','a','n',
                                  'm','p','2',
                                  'v','s','2'};
    
    @Override
    public void init() throws ServletException {
        url = getServletContext().getInitParameter("dbUrl");
        dbUsername = getServletContext().getInitParameter("dbUsername");
        dbPassword = getServletContext().getInitParameter("dbPassword");
        dbDriver = getServletContext().getInitParameter("dbDriver");

        try {
            Class.forName(dbDriver);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new ServletException("Database driver not found.", e);
        }
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String inputUsername = request.getParameter("username");
        String inputPassword = request.getParameter("password");

        if (inputUsername == null || inputUsername.isEmpty() || inputPassword == null || inputPassword.isEmpty()) {
            response.sendRedirect("error_4.jsp"); // NULL INPUT
            return;
        }

        try (Connection con = DriverManager.getConnection(url, dbUsername, dbPassword)) {
            if (isUserExist(con, inputUsername)) {
                String encryptedInputPassword = encrypt(inputPassword); // Encrypt input password
                String encryptedPassword = getUserEncryptedPassword(con, inputUsername);
                
                if (encryptedPassword.equals(encryptedInputPassword)) {
                    String userRole = getUserRole(con, inputUsername);
                    HttpSession session = request.getSession();
                    session.setAttribute("username", inputUsername);
                    session.setAttribute("role", userRole); 
                    response.sendRedirect("success.jsp");
                } else {
                    response.sendRedirect("error_2.jsp"); // INCORRECT PASSWORD
                }
            } else {
                response.sendRedirect("error_1.jsp"); // USERNAME NOT FOUND
            }
        } catch (SQLException sqle) {
            sqle.printStackTrace();
            // Log the exception
            response.sendRedirect("error_generic.jsp"); // Redirect to a generic error page
        }
    }

    private boolean isUserExist(Connection con, String inputUsername) throws SQLException {
        String query = "SELECT * FROM USERS WHERE email = ?";
        try (PreparedStatement pstmt = con.prepareStatement(query)) {
            pstmt.setString(1, inputUsername);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }

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

    private static String encrypt(String strToEncrypt) {
        String encryptedString = null;
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            final SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            encryptedString = Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return encryptedString;
    }
}
