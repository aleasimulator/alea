package org.apache.jsp;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.jsp.*;
import xklusac.environment.ServletInputStreamProvider;
import xklusac.environment.InputStreamProvider;
import java.io.File;
import xklusac.environment.ConfigurationWeb;
import xklusac.environment.ConfigurationDescription;
import xklusac.plugins.PluginConfiguration;
import java.util.Set;
import xklusac.environment.ConfigurationHtml;
import java.util.List;
import java.util.Map;
import java.util.Enumeration;
import xklusac.environment.AleaConfiguration;
import java.io.IOException;

public final class index_jsp extends org.apache.jasper.runtime.HttpJspBase
    implements org.apache.jasper.runtime.JspSourceDependent {

  private static final JspFactory _jspxFactory = JspFactory.getDefaultFactory();

  private static java.util.List<String> _jspx_dependants;

  private org.glassfish.jsp.api.ResourceInjector _jspx_resourceInjector;

  public java.util.List<String> getDependants() {
    return _jspx_dependants;
  }

  public void _jspService(HttpServletRequest request, HttpServletResponse response)
        throws java.io.IOException, ServletException {

    PageContext pageContext = null;
    HttpSession session = null;
    ServletContext application = null;
    ServletConfig config = null;
    JspWriter out = null;
    Object page = this;
    JspWriter _jspx_out = null;
    PageContext _jspx_page_context = null;

    try {
      response.setContentType("text/html; charset=utf-8");
      pageContext = _jspxFactory.getPageContext(this, request, response,
      			null, true, 8192, true);
      _jspx_page_context = pageContext;
      application = pageContext.getServletContext();
      config = pageContext.getServletConfig();
      session = pageContext.getSession();
      out = pageContext.getOut();
      _jspx_out = out;
      _jspx_resourceInjector = (org.glassfish.jsp.api.ResourceInjector) application.getAttribute("com.sun.appserv.jsp.resource.injector");

      out.write("\n");
      out.write("\n");
      out.write("\n");
      out.write("\n");
      out.write("\n");
      out.write("\n");
      out.write("\n");
      out.write("\n");
      out.write("\n");
      out.write("\n");
      out.write("\n");
      out.write("\n");
      out.write("\n");
      out.write("\n");
      out.write("\n");
      out.write("\n");
      out.write("\n");
      out.write("\n");
      out.write("\n");
      out.write("<html>\n");
      out.write("    <head>\n");
      out.write("        <link rel=\"stylesheet\" type=\"text/css\" href=\"css/style.css\">\n");
      out.write("    </head>\n");
      out.write("<body>\n");
      out.write("  ");
  //code inside the service() method
      AleaConfiguration aCfg = null;
      AleaConfiguration description = null;
      String aleaHome = System.getenv("ALEA_HOME");
      if (aleaHome!=null) {
          try {
            aCfg = new AleaConfiguration(aleaHome + File.separator + "configuration.properties");
          } catch (IOException e) { 
          } 
      } else {        
          try {
             InputStreamProvider inputStreamProviderForConfiguration = new ServletInputStreamProvider("/WEB-INF/configuration.properties", getServletContext());
             aCfg = new AleaConfiguration(inputStreamProviderForConfiguration);
          } catch (Exception e) {              
          }
      }
      if (aCfg == null) {
      
      out.write("\n");
      out.write("      <p>Could not load configuration file!</p>\n");
      out.write("      <p>Please do one of the following:</p>\n");
      out.write("      <ul>\n");
      out.write("          <li>Set environment property ALEA_HOME. It must point to the root directory of the Alea project.</li>\n");
      out.write("          <li>Copy the Alea configuration file into the WEB-INF folder of this web application.</li>\n");
      out.write("      </ul>\n");
      out.write("         \n");
      out.write("        </body>\n");
      out.write("        </html>\n");
      out.write("      ");
  
          return;
 
      }
      InputStreamProvider inputStreamProvider = new ServletInputStreamProvider("/WEB-INF/description.properties", getServletContext());
      try {
            //description = new AleaConfiguration("C:/NBworkspace/WebApplication2/description3.properties");
            description = new AleaConfiguration(inputStreamProvider);
        } catch (IOException e) { 
        }
      if (description == null) {
  
      out.write("\n");
      out.write("      Could not load description file!\n");
      out.write("        </body>\n");
      out.write("        </html>\n");
      out.write("    ");

          return;
      }
      if (request.getMethod().equals("POST")) {
          // deleting configuration item
          // iterate parameters and find one that starts with del_acfg
          // if there is one, delete according item from configuration
          Map<String, String[]> parameterMap = request.getParameterMap();
          String deletedKey = null;
          boolean keyAdded = false;
          for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
               String param = entry.getKey();
               if (param.startsWith(ConfigurationWeb.Parameters.DEL_ACFG)) {
                    deletedKey = param.substring(9);
                    aCfg.deleteString(deletedKey);
                    break;
               }
               // adding configuration item
               if (param.startsWith(ConfigurationWeb.Parameters.ADD)) {
                   int index = Integer.parseInt(param.substring(4));
                   String addedKey = parameterMap.get(ConfigurationWeb.Parameters.ADDED_KEY)[0];
                   String addedValue = parameterMap.get(ConfigurationWeb.Parameters.ADDED_VALUE)[0];
                   String addKey = AleaConfiguration.getPluginConfigurationKey(index, addedKey, false);
                   aCfg.setString(addKey, addedValue);
                   keyAdded = true;
                   break;
               }
          }
          
          if (deletedKey!=null || keyAdded || (ConfigurationWeb.Values.OK).equals(request.getParameter("submit"))) {
                for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
                    String param = entry.getKey();
                    if (param.startsWith(ConfigurationWeb.Parameters.ACFG)) {
                        String key = param.substring(5);
                        if (!(key.equals(deletedKey))){
                            aCfg.setString(key, entry.getValue()[0]);
                        } 
                        //out.write(entry.getKey() + "=" +entry.getValue()[0] + "<br/>");
                    }
                }
             // save changes in configuration file 
             aCfg.save();
          }
          if ((ConfigurationWeb.Values.DEFAULTS).equals(request.getParameter("submit"))) {
              try {
                    aCfg = new AleaConfiguration(System.getenv("ALEA_HOME") +File.separator + "configurationDefaults.properties");
              } catch (IOException e) { 
              }
              if (aCfg == null) {
        
      out.write("\n");
      out.write("                  Could not load default configuration file!\n");
      out.write("                  </body>\n");
      out.write("                  </html>\n");
      out.write("        ");
      
                  return;
              }             
          }          
      }
          
  
      out.write("\n");
      out.write("  <form method=\"post\" id=\"usrform\">\n");
      out.write("      <img src=\"images/logo1.png\" alt=\"Alea\" class=\"logo\">\n");
      out.write("      ");

          List<String> keys = description.getKeyList();
          boolean typeWasWrong = false;
          boolean tableOpen = false;
          for (String key : keys) {
              if (key.startsWith(AleaConfiguration.PLUGIN)) {
                  continue;
              }
                  String desc = description.getString(key);
                  if (key.startsWith(AleaConfiguration.HEADER)) {
                      if (tableOpen) {
                          out.write(ConfigurationHtml.TABLE_END);
                      }
                      out.write("<h2>" + desc + "</h2>");
                      out.write(ConfigurationHtml.TABLE_START);
                      tableOpen = true;
                  } else {
                      ConfigurationDescription configDesc = new ConfigurationDescription(desc);
                      String value = aCfg.getString(key);
                      String type = configDesc.getType();
                      boolean typeOk = AleaConfiguration.typeCheck(type, value);
                      String shortDesc = configDesc.getShortDescription();
                      String addInf = configDesc.getAdditionalInf();
                      String param = ConfigurationWeb.Parameters.ACFG + key;
                      String inputParam = "";
                      if (typeOk == false) {
                          inputParam = " wrongFormat";
                          typeWasWrong = true;
                      }
                  
       
      out.write("\n");
      out.write("       <tr>\n");
      out.write("           <td>");
      out.print(shortDesc);
      out.write("</td>\n");
      out.write("           <td>");
      out.print(type);
      out.write("</td>\n");
      out.write("           <td><textarea name=\"");
      out.print(param);
      out.write("\"  class=\"val");
      out.print(inputParam);
      out.write("\" form=\"usrform\">");
      out.print(value);
      out.write("</textarea></td>\n");
      out.write("           <td>");
      out.print(addInf);
      out.write("</td>\n");
      out.write("\n");
      out.write("       </tr>\n");

                  }
          }
          String[] plugins = aCfg.getStringArray(AleaConfiguration.PLUGINS);
          
          for (int i=0; i<plugins.length; i++) {
              Map<String, String> plugincfg = aCfg.getPluginConfiguration(i);
              // Get the basic config
              // get result_index from plugin configuration and display it...
              String pluginResultKey = aCfg.getPluginConfigurationKey(i, PluginConfiguration.RESULT_HEADER, true);
              String desc = description.getString(pluginResultKey);
              ConfigurationDescription configDesc = new ConfigurationDescription(desc);
              String type = configDesc.getType();
              String resultHeader = configDesc.getShortDescription();
              String paramR = ConfigurationWeb.Parameters.ACFG + aCfg.getPluginConfigurationKey(i, PluginConfiguration.RESULT_HEADER, false);
              String inputParam = "";
              String pluginResultValue = plugincfg.get(PluginConfiguration.RESULT_HEADER);
              String addInf = configDesc.getAdditionalInf();
              boolean typeOk = AleaConfiguration.typeCheck(type, pluginResultValue);
              if (typeOk == false) {
                   inputParam = " wrongFormat";
                   typeWasWrong = true;
              }
              if (tableOpen) {
                   out.write(ConfigurationHtml.TABLE_END);
              }
              out.write("<h3>" + plugins[i] + "</h3>");
              out.write(ConfigurationHtml.TABLE_START);
              
      out.write("\n");
      out.write("                   <tr>\n");
      out.write("                       <td>");
      out.print(resultHeader);
      out.write("</td>\n");
      out.write("                       <td>");
      out.print(type);
      out.write("</td>\n");
      out.write("                       <td><input name=\"");
      out.print(paramR);
      out.write("\"  class=\"val");
      out.print(inputParam);
      out.write("\" value=\"");
      out.print(pluginResultValue);
      out.write("\"/></td>\n");
      out.write("                       <td>");
      out.print(addInf);
      out.write("</td>\n");
      out.write("                   </tr>\n");
      out.write("                      ");

              // Take care of plugin-specific config (Ignore result_header)
              Set<String> pluginKeys = plugincfg.keySet();
              if (tableOpen) {
                  out.write(ConfigurationHtml.TABLE_END);
              }
              out.write(ConfigurationHtml.TABLE_START_PLUGIN);
              for (String pluginKey : pluginKeys) {
                  String pluginValue = plugincfg.get(pluginKey);
                  String param = ConfigurationWeb.Parameters.ACFG + aCfg.getPluginConfigurationKey(i, pluginKey, false);
                  
                  if (pluginKey.endsWith(PluginConfiguration.RESULT_HEADER)) {
                      continue;
                  }
                  
                  
      out.write("\n");
      out.write("                  <tr>\n");
      out.write("                      <td>");
      out.print(pluginKey);
      out.write("</td>\n");
      out.write("                      <td><input name=\"");
      out.print(param);
      out.write("\"  value=\"");
      out.print(pluginValue);
      out.write("\"/></td>\n");
      out.write("                      <td><input name=\"");
      out.print(ConfigurationWeb.Parameters.DEL + param);
      out.write("\" type=\"submit\" value=\"");
      out.print(ConfigurationWeb.Values.DEL);
      out.write("\"/></td>\n");
      out.write("                  </tr>\n");
      out.write("                  ");

                  
              }
                  
      out.write("\n");
      out.write("                  <tr>\n");
      out.write("                      <td><input name=\"");
      out.print(ConfigurationWeb.Parameters.ADDED_KEY);
      out.write("\"  value=\"\"/></td>\n");
      out.write("                      <td><input name=\"");
      out.print(ConfigurationWeb.Parameters.ADDED_VALUE);
      out.write("\"  value=\"\"/></td>\n");
      out.write("                      <td><input name=\"");
      out.print(ConfigurationWeb.Parameters.ADD + i);
      out.write("\" type=\"submit\" value=\"");
      out.print(ConfigurationWeb.Values.ADD);
      out.write("\"/></td>\n");
      out.write("                  </tr>\n");
      out.write("                  ");

          }
          
          
          out.write(ConfigurationHtml.TABLE_END);
          if (typeWasWrong) {
              
      out.write("\n");
      out.write("              <p>Wrong format!</p>\n");
      out.write("              ");

          }
          
      out.write("\n");
      out.write("          <input name=\"submit\" type=\"submit\" value=\"");
      out.print(ConfigurationWeb.Values.OK);
      out.write("\"/>\n");
      out.write("          <input name=\"submit\" type=\"submit\" value=\"Undo changes\"/>\n");
      out.write("          <input name=\"submit\" type=\"submit\" value=\"");
      out.print(ConfigurationWeb.Values.DEFAULTS);
      out.write("\"/>\n");
      out.write("  </form>\n");
      out.write("</body>\n");
      out.write("</html>\n");
    } catch (Throwable t) {
      if (!(t instanceof SkipPageException)){
        out = _jspx_out;
        if (out != null && out.getBufferSize() != 0)
          out.clearBuffer();
        if (_jspx_page_context != null) _jspx_page_context.handlePageException(t);
        else throw new ServletException(t);
      }
    } finally {
      _jspxFactory.releasePageContext(_jspx_page_context);
    }
  }
}
