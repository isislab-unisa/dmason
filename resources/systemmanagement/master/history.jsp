<%@ page language="java"
	contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8" %>

<!DOCTYPE html>

<html>
    <head>
        <jsp:include page="fragments/head-common.jsp">
			<jsp:param name="headTitle" value="DMASON - System Management"></jsp:param>
        </jsp:include>

        <%-- Import simulation elements --%>
        <link rel="import" href="custom_components/history/sim-history-grid.html">

        <%-- Import iron elements --%>
        <link rel="import" href="bower_components/iron-icons/editor-icons.html">

        <%-- Import paper elements --%>
        <link rel="import" href="bower_components/paper-badge/paper-badge.html">
    </head>
    <body unresolved>
		<!-- Page header -->
		<jsp:include page="fragments/header.jsp">
			<jsp:param name="page" value="history" />
		</jsp:include>

        <!-- Page body -->
        <div class="content content-main">
            <paper-dialog opened id="load_history_dialog" entry-animation="scale-up-animation" exit-animation="fade-out-animation" modal>
                <div class="layout horizontal center">
                    <paper-spinner class="multi" active alt="Loading history"></paper-spinner>
                    <span style="margin-left:5px;">Loading history...</span>
                </div>
            </paper-dialog>

            <template is="dom-bind" id="workbench_template">
                <neon-animated-pages id="history_animated_pages" selected="0">
                    <sim-history-grid id="sim_history_grid"></sim-history-grid>
                    <workbench></workbench>
                </neon-animated-pages>
            </template>
        </div>

        <!-- bottom buttons and warnings -->
        <paper-fab id="delete-history-button" icon="cancel" onclick="cleanSelectedHistory()"></paper-fab>
        <paper-fab id="go_to_workbenck" icon="editor:insert-chart"></paper-fab>
        <paper-toast id="miss-history-delete">You need to select some Simulations to delete</paper-toast>

		<!-- Sliding drawer menu -->
        <jsp:include page="fragments/drawer.jsp">
            <jsp:param name="pageSelected" value="2" />
        </jsp:include>
    </body>
</html>
