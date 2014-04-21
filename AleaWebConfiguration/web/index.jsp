<%-- 
    Document   : index
    Created on : 28.11.2013, 17:20:17
    Author     : Gabriela Podolnikova
--%>

<%@page import="xklusac.environment.ServletInputStreamProvider"%>
<%@page import="xklusac.environment.InputStreamProvider"%>
<%@page import="java.io.File"%>
<%@page import="xklusac.environment.ConfigurationWeb"%>
<%@page import="xklusac.environment.ConfigurationDescription"%>
<%@page import="xklusac.plugins.PluginConfiguration"%>
<%@page import="java.util.Set"%>
<%@page import="xklusac.environment.ConfigurationHtml"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.Enumeration"%>
<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8" %>
<%@ page import="xklusac.environment.AleaConfiguration" %>
<%@ page import="java.io.IOException" %>



<html>
    <head>
        <link rel="stylesheet" type="text/css" href="css/style.css">
    </head>
<body>
  <%  //code inside the service() method
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
      %>
      <p>Could not load configuration file!</p>
      <p>Please do one of the following:</p>
      <ul>
          <li>Set ALEA_HOME environment variable. It must point to the root directory of the Alea project.</li>
          <li>Copy the Alea configuration file into the WEB-INF folder of this web application.</li>
      </ul>
         
        </body>
        </html>
      <%  
          return;
 
      }
      InputStreamProvider inputStreamProvider = new ServletInputStreamProvider("/WEB-INF/description.properties", getServletContext());
      try {
            //description = new AleaConfiguration("C:/NBworkspace/WebApplication2/description3.properties");
            description = new AleaConfiguration(inputStreamProvider);
        } catch (IOException e) { 
        }
      if (description == null) {
  %>
      Could not load description file!
        </body>
        </html>
    <%
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
        %>
                  Could not load default configuration file!
                  </body>
                  </html>
        <%      
                  return;
              }             
          }          
      }
          
  %>
  <form method="post" id="usrform">
      <img src="images/logo1.png" alt="Alea" class="logo">
      <%
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
                  
       %>
       <tr>
           <td><%=shortDesc%></td>
           <td><%=type%></td>
           <td><textarea name="<%=param%>"  class="val<%=inputParam%>" form="usrform"><%=value%></textarea></td>
           <td><%=addInf%></td>

       </tr>
<%
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
              %>
                   <tr>
                       <td><%=resultHeader%></td>
                       <td><%=type%></td>
                       <td><input name="<%=paramR%>"  class="val<%=inputParam%>" value="<%=pluginResultValue%>"/></td>
                       <td><%=addInf%></td>
                   </tr>
                      <%
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
                  
                  %>
                  <tr>
                      <td><%=pluginKey%></td>
                      <td><input name="<%=param%>"  value="<%=pluginValue%>"/></td>
                      <td><input name="<%=ConfigurationWeb.Parameters.DEL + param%>" type="submit" value="<%=ConfigurationWeb.Values.DEL%>"/></td>
                  </tr>
                  <%
                  
              }
                  %>
                  <tr>
                      <td><input name="<%=ConfigurationWeb.Parameters.ADDED_KEY%>"  value=""/></td>
                      <td><input name="<%=ConfigurationWeb.Parameters.ADDED_VALUE%>"  value=""/></td>
                      <td><input name="<%=ConfigurationWeb.Parameters.ADD + i%>" type="submit" value="<%=ConfigurationWeb.Values.ADD%>"/></td>
                  </tr>
                  <%
          }
          
          
          out.write(ConfigurationHtml.TABLE_END);
          if (typeWasWrong) {
              %>
              <p>Wrong format!</p>
              <%
          }
          %>
          <input name="submit" type="submit" value="<%=ConfigurationWeb.Values.OK%>"/>
          <input name="submit" type="submit" value="Undo changes"/>
          <input name="submit" type="submit" value="<%=ConfigurationWeb.Values.DEFAULTS%>"/>
  </form>
</body>
</html>
