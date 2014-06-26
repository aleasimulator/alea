/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package xklusac.environment.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import xklusac.environment.AleaConfiguration;
import xklusac.environment.ConfigurationWeb;

/**
 * Provides the download of the configuration file from web application.
 * 
 * @author Gabriela Podolnikova
 */
public class DownloadServlet extends HttpServlet {

    /**
     * Processes requests for both HTTP
     * <code>GET</code> and
     * <code>POST</code> methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        /*response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();*/
        ServletOutputStream out = response.getOutputStream();
        try {
            /*out.println("<!DOCTYPE html>");
             out.println("<html>");
             out.println("<head>");
             out.println("<title>Servlet DownloadServlet</title>");            
             out.println("</head>");
             out.println("<body>");
             out.println("<h1>Servlet DownloadServlet at " + request.getContextPath() + "</h1>");
             out.println("</body>");
             out.println("</html>");*/
            String filename = ConfigurationWeb.getAleaConfiguration(getServletContext()).getFileName();
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition",
                    "attachment;filename=" + AleaConfiguration.DEFAULT_FILE_NAME);

            File file = new File(filename);
            FileInputStream fileIn = new FileInputStream(file);

            byte[] outputByte = new byte[(int) file.length()];
            //copy binary contect to output stream
            while (fileIn.read(outputByte, 0, (int) file.length()) != -1) {
                out.write(outputByte, 0, (int) file.length());
            }
        } finally {
            out.close();
        }
    }

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
        processRequest(request, response);
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
        processRequest(request, response);
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
}
