/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package xklusac.environment.web;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import xklusac.environment.ConfigurationWeb;

/**
 * Provides uploading of the configuration file to the web application.
 * 
 * @author Gabriela Podolnikova
 */
@MultipartConfig
public class UploadServlet extends HttpServlet {

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP
     * <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try {
            printStart(out);
            out.println("<h1>HTTP GET method not supported.</h1>");
            printEnd(out);
        } finally {            
            out.close();
        }
        
    }

    /**
     * Handles the HTTP
     * <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Part filePart = request.getPart("file"); // Retrieves <input type="file" name="file">
        // TODO: handle filePart==null
        InputStream filecontent = filePart.getInputStream();
        Path path = FileSystems.getDefault().getPath(ConfigurationWeb.getAleaConfiguration(getServletContext()).getFileName());
        Files.copy(filecontent, path, StandardCopyOption.REPLACE_EXISTING);
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try {
            printStart(out);
            out.println("<h1>Configuration file was uploaded.</h1>");
            out.println("<form action=\".\" method=\"get\">");
            out.println("<input type=\"submit\" value=\"OK\"/>");
            out.println("</form>");
            printEnd(out);
        } finally {            
            out.close();
        }
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
    
    private void printStart(PrintWriter out) {
        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("<head>");
        out.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"css/style.css\">");
        out.println("<title>Alea Configuration Upload</title>");
        out.println("</head>");
        out.println("<body>");
    }
    
    private void printEnd(PrintWriter out) {
        out.println("</body>");
        out.println("</html>");
    }
}
