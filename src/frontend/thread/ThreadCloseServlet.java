package frontend.thread;

import helper.CommonHelper;
import helper.ErrorMessages;
import helper.LoggerHelper;
import mysql.MySqlConnect;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

import static helper.ErrorMessages.*;
import static helper.LoggerHelper.*;
import static main.JsonInterpreterFromRequest.getJSONFromRequest;

public class ThreadCloseServlet extends HttpServlet {
    private final Logger logger = LogManager.getLogger(ThreadCloseServlet.class.getName());

    private MySqlConnect mySqlServer;

    public ThreadCloseServlet() {
        this.mySqlServer = new MySqlConnect();
    }

    public void doPost(HttpServletRequest request,
                      HttpServletResponse response) throws ServletException, IOException {
        logger.info(start());
        mySqlServer.init();
        JSONObject req = getJSONFromRequest(request, "ThreadCloseServlet");

        short status = ok;
        String message = "";

        long threadId = 0;
        if (req.containsKey("thread")) {
            threadId = (long) req.get("thread");
        } else {
            status = notValidRequest;
            message = wrongJSONData();
        }

        int result = 0;
        String query;

        if (status == ok) {
            query = "update thread set isClosed = 1 where id = " + threadId + ";";
            result = mySqlServer.executeUpdate(query);
            logger.info(resultUpdate(), result);
            if (result == 0) {
                status = noRequestedObject;
                message = noPost();
            }
        }
        try {
            createResponse(response, status, message, threadId);
        } catch (SQLException e) {
            logger.info(responseCreating());
        }
        mySqlServer.close();
        logger.info(finish());
    }

    private void createResponse(HttpServletResponse response, short status, String message, long threadId) throws IOException, SQLException {
        CommonHelper.setResponse(response);
        JSONObject obj = new JSONObject();
        JSONObject data = new JSONObject();
        if (status != ErrorMessages.ok) {
            data.put("error", message);
        } else {
            data.put("thread", threadId);
        }
        obj.put("response", data);
        obj.put("code", status);
        logger.info(LoggerHelper.responseJSON(), obj.toString());
        response.getWriter().write(obj.toString());
    }
}