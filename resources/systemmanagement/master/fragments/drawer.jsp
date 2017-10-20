<%-- tag import --%>
<%@ taglib
        prefix="c"
        uri="http://java.sun.com/jsp/jstl/core" %>
<app-drawer id="drawer" slot="drawer" swipe-open>
    <app-header-layout id="side-header-panel" fixed fill>
        <!-- header del drawer -->
        <app-toolbar class="side-drawer">
            <div style="margin-right:5px;">Control Panel</div>
            <paper-icon-button icon="chevron-left" onclick="drawer.toggle()"></paper-icon-button>
        </app-toolbar>
        <!-- menu drawer -->
        <nav class="content content-side-bar">
            <paper-menu selected="${param.pageSelected}">
            	<%-- check which page is selected --%>
                <c:choose>
                    <c:when test="${pageSelected == 0}">
                        <paper-item class="selected">
                    </c:when>
                    <c:otherwise>
                        <paper-item>
                    </c:otherwise>
                </c:choose>
                    <a href="index.jsp">
                        <iron-icon icon="icons:flip-to-front" item-icon slot="item-icon"></iron-icon>
                        <span class="span-icon">Monitoring</span>
                    </a>
                </paper-item>
                <c:choose>
                    <c:when test="${pageSelected == 1}">
                        <paper-item class="selected">
                    </c:when>
                    <c:otherwise>
                        <paper-item>
                    </c:otherwise>
                </c:choose>
                    <a href="simulations.jsp">
                        <iron-icon icon="image:blur-on" item-icon slot="item-icon"></iron-icon>
                        <span class="span-icon">Simulations</span>
                    </a>
                </paper-item>
                <c:choose>
                    <c:when test="${pageSelected == 2}">
                        <paper-item class="selected">
                    </c:when>
                    <c:otherwise>
                        <paper-item>
                    </c:otherwise>
                </c:choose>
                    <a href="history.jsp">
                        <iron-icon icon="history" item-icon slot="item-icon"></iron-icon>
                        <span class="span-icon">History</span>
                    </a>
                </paper-item>
                <c:choose>
                    <c:when test="${pageSelected == 3}">
                        <paper-item class="selected">
                    </c:when>
                    <c:otherwise>
                        <paper-item>
                    </c:otherwise>
                </c:choose>
                    <a href="settings.jsp">
                        <iron-icon icon="settings" item-icon slot="item-icon"></iron-icon>
                        <span class="span-icon">Settings</span>
                    </a>
                </paper-item>
            </paper-menu>
        <nav>
    </app-header-layout>
</app-drawer>