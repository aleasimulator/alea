<%-- 
    Document   : index
    Created on : 28.11.2013, 17:20:17
    Author     : Gabriela Podolnikova
--%>

<%@page import="java.util.ArrayList"%>
<%@page import="java.util.Arrays"%>
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
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>


<html>
    <head>
        <link rel="stylesheet" type="text/css" href="./css/style.css">
        <meta http-equiv="Cache-Control" content="no-cache, no-store, must-revalidate" />
        <meta http-equiv="Pragma" content="no-cache" />
        <meta http-equiv="Expires" content="0" />
    </head>
    
<body>
  <%  //code inside the service() method
      AleaConfiguration aCfg = ConfigurationWeb.getAleaConfiguration(getServletContext());
      AleaConfiguration description = null;
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
                   String addedKey = parameterMap.get(ConfigurationWeb.Parameters.ADDED_KEY)[index];
                   String addedValue = parameterMap.get(ConfigurationWeb.Parameters.ADDED_VALUE)[index];
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
          String[] algorithmsArray =  null;
          boolean typeWasWrong = false;
          boolean match = false;
          boolean lengthWasWrong = false;
          boolean tableOpen = false;
          boolean useRam = false;
          int numOfAlgSelected = 0;
          int numOfDataSetsSelected = 0;
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
                      List<Integer> applicableAlgs = configDesc.getAlg();
                      if (!applicableAlgs.isEmpty()) {
                              int[] selectedAlgs = aCfg.getIntArray("algorithms");
                              match = false;
                              for (int sel : selectedAlgs) {
                                  if (!applicableAlgs.contains(sel)) {
                                      match = true;
                                  }
                              }
                              if (!match) {
                                  continue;
                              }
                      }
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
                      //checking if the lenght of arrays are OK 
                      if (key.equals("algorithms")) {
                          int algs[] = aCfg.getIntArray(key);
                          numOfAlgSelected = algs.length;
                          System.out.println("addInf = " + addInf);
                          
                          algorithmsArray = addInf.split(",");
                          System.out.println("algorithmsArray = " + Arrays.asList(algorithmsArray));
                      }
                      if (key.equals("data_sets")) {
                          String dataSets[] = aCfg.getStringArray(key);
                          numOfDataSetsSelected = dataSets.length;
                      }
                      if (key.equals("total_gridlet") || key.equals("skip") || key.equals("weight_of_fairness")){
                          if (aCfg.getIntArray(key).length != numOfDataSetsSelected) {
                              inputParam = " wrongFormat";
                              lengthWasWrong = true;
                          }
                      }
                      if (key.equals("use_resource_spec_packing") || key.equals("use_anti_starvation")) {
                          if (aCfg.getBooleanArray(key).length != numOfAlgSelected) {
                              inputParam = " wrongFormat";
                              lengthWasWrong = true;
                          }
                      }
                      if (key.equals("sum_multiplications")) {
                          if (aCfg.getString(keys.get(keys.indexOf("use_fairshare"))).equals("false") || aCfg.getString(keys.get(keys.indexOf("use_fairshare_RAM"))).equals("false")) {
                              match = true;
                          }
                      }
                      if (key.equals("use_resource_spec_packing")) {
                          String ram = keys.get(keys.indexOf("use_RAM"));
                          if (aCfg.getString(ram).equals("false")) {
                              useRam = true;
                          }
                      }
                  
       %>
       <tr>
           <td><%=shortDesc%></td>
           <td><%=type%></td>
           <%
           if (type.equals("boolean")) {
           %>
           <td><select name="<%=param%>" form="usrform">
                   <%
                   if (value.equals("true")) {
                   %>
               <option value="true"><%=value%></option>
               <%
                   value = "false";
               %>
               <option value="false"><%=value%></option>
               <% 
                   } else if (value.equals("false")) {
                       %>
                       <option value="false"><%=value%></option>
                       <%
                       value = "true";
                       %>
                        <option value="false"><%=value%></option>
                       <% 
                   }
               %>
               </select></td>
           <%
           } else {
           %>
           <td><textarea name="<%=param%>"  class="val<%=inputParam%>" form="usrform"><%=value%></textarea></td>           
           <%
           }
           if ((useRam && match) || (useRam && lengthWasWrong) || (useRam && match && lengthWasWrong)) {
               %>
                <td><font color="red"> Only effective when an per-node job specification is used. Job's RAM requirements should be set to true.</font></td>
              <%
               useRam = false;
           }
           if (match && lengthWasWrong) {
               %>
                <td><%=addInf%><font color="red"> Pay attention to the selected algorithms! Wrong length of arrays!</font></td>
              <%
              match = false;
              lengthWasWrong = false;
           } else if (match) {
               %>
                <td><%=addInf%><font color="red"> Pay attention to the selected algorithms!</font></td>
              <%
              match = false;
           }else if (lengthWasWrong) {
               %>
                 <td><%=addInf%><font color="red">Wrong length of arrays!</font></td>
              <%
              lengthWasWrong = false;
           } else if (useRam){
               %>
               <td><%=addInf%><font color="red"> Only effective when an per-node job specification is used. Job's RAM requirements should be set to true.</font></td>
               <%
               useRam = false;
           } else if (key.equals("algorithms")) {
               %>
               <td>
                   <%pageContext.setAttribute("aa", algorithmsArray);%>
                    <c:forEach items="${aa}" var ="alg">
                    <ul style="list-style-type:disc">
                        <li><c:out value="${alg}"/></li>
                    </ul>
                </c:forEach>
               </td>
               <%
           }
           else {
              %>
               <td><%=addInf%></td>
              <%
           }
           %>
       </tr>
        <%
           if (key.equals("algorithms")) {
                 out.write(ConfigurationHtml.TABLE_END);
                    %>
                       <input name="submit" type="submit" value="<%=ConfigurationWeb.Values.OK%>"/>
                    <%
            }
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
          <input name="submit" type="submit" value="Reset"/>
          <input name="submit" type="submit" value="<%=ConfigurationWeb.Values.DEFAULTS%>"/>
          <h2>Download/upload configuration file</h2>
  </form>
  <form action="download" method="get">
    <input type="submit" value="Download Configuration File" class="dwnl"/>
  </form>        
  <form action="upload" method="post" enctype="multipart/form-data">
    <input type="file" name="file" />
    <input type="submit" value="Upload configuration file" />
  </form>
</body>
</html>
