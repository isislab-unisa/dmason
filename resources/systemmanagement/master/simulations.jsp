<%@ page language="java"
	contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8" %>

<!DOCTYPE html>

<html>
    <head>
        <jsp:include page="fragments/head-common.jsp">
			<jsp:param name="headTitle" value="DMASON - System Management"></jsp:param>
		</jsp:include>

        <%-- Custom Scripts --%>
        <script src="bower_components/highcharts/highcharts.js"></script>
        <script src="bower_components/highcharts/highcharts-more.js"></script>

        <%-- Import simulation elements --%>
        <link rel="import" href="custom_components/simulation/animated-grid.html">
        <link rel="import" href="custom_components/simulation/fullsize-page-with-card.html">

        <%-- Import paper elements --%>
        <link rel="import" href="bower_components/paper-toolbar/paper-toolbar.html">
    </head>

    <!--body unresolved onload="load_tiles_simulations()"-->
    <body unresolved>
		<!-- Page header -->
		<jsp:include page="fragments/header.jsp">
			<jsp:param name="page" value="simulations" />
		</jsp:include>

        <!-- Page body -->
        <div class="content content-main">
            <template is="dom-bind" id="simulations">
                <neon-animated-pages id="pages" selected="0">
                        <animated-grid id="list_simulations" on-tile-click="_onTileClick" on-update-sim-event="_onUpdateSimEvent"></animated-grid>
                        <!--animated-grid id="list_simulations" on-tile-click="_onTileClick"></animated-grid-->
                        <fullsize-page-with-card id="fullsize_card" on-go-back="_onFullsizeClick">
                        </fullsize-page-with-card>
                </neon-animated-pages>
            </template>
            <paper-dialog id="load_sim_log_file"  entry-animation="scale-up-animation" exit-animation="fade-out-animation" modal>
                <div class="layout horizontal center">
                    <paper-spinner class="multi" active alt="Loading simulations list"></paper-spinner>
                    <span style="margin-left:5px;">Loading...</span>
                </div>
            </paper-dialog>

            <script>
                var scope = document.querySelector('template[is="dom-bind"]');

                scope.addEventListener('dom-change', function(event) {update_simulation_info()});

                scope._onTileClick = function(event) {
                    var _sim = event.detail.data;
                    this.$['fullsize_card'].sim = _sim;
                    open_dialog_by_ID("load_sim_log_file");
                    getListFile(_sim.id);
                /*  var lf = [];
                    for(i=0; i<110; i++)
                        lf[i] = {fileName:'file' + i, modifiedDate: "22/01/2016"};

                        this.$['fullsize-card'].listFile = lf;
                        this.$.pages.selected = 1;*/
                };

                scope._onUpdateSimEvent = function(event) {
                    this.$['fullsize_card'].sim = event.detail.data;
                };
                scope._onFullsizeClick = function(event) {
                    this.$.pages.selected = 0;
                };

                /*
                scope._onSubmitSim = function(event) {
                    var id = event.target.parentElement.id;
                    var op = event.detail.data;
                    id = id.substring(id.indexOf("-") + 1, id.length);

                    $.ajax({
                        url: "simulationController",
                        data: "id= " + id + "&op= " + op
                    });

                };*/
            </script>
        </div>

        <!-- Sliding drawer menu -->
        <jsp:include page="fragments/drawer.jsp">
            <jsp:param name="pageSelected" value="1" />
        </jsp:include>
    </body>
</html>
