import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class BackServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession();
        if (session != null) {
            session.invalidate();
            System.out.println("Session invalidated successfully.");
        } else {
            System.out.println("No session found to invalidate.");
        }
        response.sendRedirect("index.jsp");
    }
}
