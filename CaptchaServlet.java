import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

public class CaptchaServlet extends HttpServlet {

    private int captchaLength;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        ServletContext context = config.getServletContext();
        captchaLength = Integer.parseInt(context.getInitParameter("captchaLength"));
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String captcha = generateCaptcha(captchaLength);
        HttpSession session = request.getSession();
        session.setAttribute("captcha", captcha);
        response.setContentType("text/plain");
        PrintWriter out = response.getWriter();
        out.print(captcha);
        out.flush();
    }

    private String generateCaptcha(int length) {
        Random rand = new Random();
        String characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder captcha = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int index = rand.nextInt(characters.length());
            captcha.append(characters.charAt(index));
        }
        return captcha.toString();
    }
}
