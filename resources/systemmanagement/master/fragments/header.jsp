<%-- tag import --%>
<%@ taglib
		prefix="c"
		uri="http://java.sun.com/jsp/jstl/core" %>
<app-header reveals fixed slot="header">
	<app-toolbar flex id="mainToolBar" class="horizontal">
		<paper-icon-button icon="menu" onclick="drawer.toggle()" drawer-toggle></paper-icon-button>
		<div class="flex" spacer main-title><span>DMASON Master</span></div>
		<c:choose>
			<c:when test="${param.page == 'index' }">
				<div onclick="selectAllWorkers()" class="selectAllWorker">
					<paper-icon-button icon="select-all"></paper-icon-button><span>Select all workers</span>
				</div>
			</c:when>
			<c:when test="${param.page == 'history' }">
				<div onclick="cleanHistory()" class="cleanAllHistory">
					<paper-icon-button icon="select-all"></paper-icon-button><span>Clean all history</span>
				</div>
			</c:when>
		</c:choose>
	</app-toolbar>
</app-header>