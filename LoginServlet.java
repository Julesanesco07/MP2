import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.sql.*;

public class LoginServlet extends HttpServlet {
    private String url;
    private String dbUsername;
    private String dbPassword;
    private String dbDriver;

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
            if (checkCredentials(con, inputUsername, inputPassword) ) {
                String userRole = getUserRole(con, inputUsername);
                HttpSession session = request.getSession();
                session.setAttribute("username", inputUsername);
                session.setAttribute("role", userRole); 
                response.sendRedirect("success.jsp");
            } else {
                if (isUserExist(con, inputUsername)) {
                    response.sendRedirect("error_2.jsp"); // INCORRECT PASSWORD
                } else {
                    if (areCredentialsInvalid(con, inputUsername, inputPassword)) {
                        response.sendRedirect("error_3.jsp"); // INCORRECT CREDENTIALS
                    } else {
                        response.sendRedirect("error_1.jsp"); // USERNAME NOT FOUND
                    }
                }
            }
        } catch (SQLException sqle) {
            sqle.printStackTrace();
            response.sendRedirect("error_session.jsp");
        }
    }

    private boolean checkCredentials(Connection con, String inputUsername, String inputPassword) throws SQLException {
        String query = "SELECT * FROM USERS WHERE email = ? AND password = ?";
        try (PreparedStatement pstmt = con.prepareStatement(query)) {
            pstmt.setString(1, inputUsername);
            pstmt.setString(2, inputPassword);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
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

    private boolean areCredentialsInvalid(Connection con, String inputUsername, String inputPassword) throws SQLException {
        String query = "SELECT * FROM USERS WHERE email = ? AND password != ?";
        try (PreparedStatement pstmt = con.prepareStatement(query)) {
            pstmt.setString(1, inputUsername);
            pstmt.setString(2, inputPassword);
            try (ResultSet rs = pstmt.executeQuery()) {
                return !rs.next();
            }
        }
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
}
