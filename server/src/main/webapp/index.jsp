<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="org.apache.taglibs.standard.functions.Functions" %><%--
  #%L
  webappRunnerSample Maven Webapp
  %%
  Copyright (C) 2016 - 2018 Frederik Kammel
  %%
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
  #L%
  --%>
<html>
<head>
    <link href="https://fonts.googleapis.com/css?family=Open+Sans|Roboto" rel="stylesheet">
    <style>
        body {
            background-color: #24A59F;
        }

        p {
            font-family: 'Roboto', sans-serif;
            text-align: center;
        }

        h1 {
            font-family: 'Open Sans', sans-serif;
            text-align: center;
        }

        img {
            display: block;
            margin-left: auto;
            margin-right: auto;
        }
    </style>
</head>
<body>
<img src="<c:url value="/resources/thumbsUpMan.svg"/>" width="auto" height="50%"/>
<h1>Tic Tac Toe - It works!</h1><br>

<p>Good news! Your server is working and you can reach it through the following ip:</p>
<p>
    <% StringBuffer url = request.getRequestURL();

        if (!url.toString().endsWith("/"))
            url.append("/");

        url.append("tictactoe");
        out.println(Functions.escapeXml(url.toString()));
    %></p>

<p>Read more on <a href="https://github.com/vatbub/tictactoe">GitHub</a>.</p>
<p><a href='https://www.freepik.com/free-vector/smiley-salesman-with-flat-design_2672650.htm'>Designed by Freepik</a>
</p>
</body>
</html>
