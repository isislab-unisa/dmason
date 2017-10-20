<!DOCTYPE html>
<html>
    <head>
        <meta name="viewport" content="width=device-width, minimum-scale=1.0, initial-scale=1.0, user-scalable=yes">

        <title>DMASON - System Management</title>
        <link rel="shortcut icon" type="image/png" href="images/dmason-ico.png"/>

        <!-- Polyfill Web Components for older browsers -->
		<script src="bower_components/webcomponentsjs/webcomponents-lite.js"></script>

        <!-- Custom Polymer CSS -->
        <link rel="import" href="style/polymer/styles-polymer.html">

        <!-- Custom CSS -->
        <link href="style/custom-style.css" rel="stylesheet" type="text/css">

        <!-- jQuery -->
        <script src="js/jquery-1.12.4.min.js"></script>

        <!-- Masonry lib -->
        <script src="js/masonry.pkgd.min.js"></script>

        <!-- Custom Scripts -->
        <script src="js/script.js"></script>
        <script src="bower_components/highcharts/highcharts.js"></script>
        <script src="bower_components/highcharts/highcharts-more.js"></script>

        <!-- Import simulation elements -->
        <link rel="import" href="custom_components/simulation/animated-grid.html">
        <link rel="import" href="custom_components/simulation/fullsize-page-with-card.html">

        <!-- Import element -->
		<link rel="import" href="bower_components/app-layout/app-drawer-layout/app-drawer-layout.html">
		<link rel="import" href="bower_components/app-layout/app-drawer/app-drawer.html">
		<link rel="import" href="bower_components/app-layout/app-header-layout/app-header-layout.html">
		<link rel="import" href="bower_components/app-layout/app-header/app-header.html">
        <link rel="import" href="bower_components/app-layout/app-toolbar/app-toolbar.html">

        <link rel="import" href="bower_components/neon-animation/neon-animated-pages.html">
        <link rel="import" href="bower_components/neon-animation/neon-animations.html">

        <link rel="import" href="bower_components/paper-icon-button/paper-icon-button.html">
        <link rel="import" href="bower_components/paper-toolbar/paper-toolbar.html">
        <link rel="import" href="bower_components/paper-menu/paper-menu.html">
        <link rel="import" href="bower_components/paper-item/paper-item.html">
        <link rel="import" href="bower_components/paper-fab/paper-fab.html">
        <link rel="import" href="bower_components/paper-styles/paper-styles.html">
        <link rel="import" href="bower_components/paper-fab/paper-fab.html">
        <link rel="import" href="bower_components/paper-toast/paper-toast.html">
        <link rel="import" href="bower_components/paper-dialog/paper-dialog.html">
        <link rel="import" href="bower_components/paper-button/paper-button.html">
        <link rel="import" href="bower_components/paper-radio-button/paper-radio-button.html">
        <link rel="import" href="bower_components/paper-radio-group/paper-radio-group.html">
        <link rel="import" href="bower_components/paper-input/paper-input.html">
        <link rel="import" href="bower_components/paper-progress/paper-progress.html">
        <link rel="import" href="bower_components/paper-dialog-scrollable/paper-dialog-scrollable.html">
        <link rel="import" href="bower_components/paper-spinner/paper-spinner.html">

        <link rel="import" href="bower_components/iron-icons/iron-icons.html">
        <link rel="import" href="bower_components/iron-flex-layout/iron-flex-layout-classes.html">
        <link rel="import" href="bower_components/iron-image/iron-image.html">
        <link rel="import" href="bower_components/iron-icons/image-icons.html">
    </head>
    <!--body unresolved onload="load_tiles_simulations()"-->
    <body unresolved>
		<!-- Testata pagina -->
		<jsp:include page="fragments/header.jsp">
			<jsp:param name="page" value="simulations" />
		</jsp:include>

        <!-- Corpo pagina -->
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

        <!-- menu laterale a scorrimento -->
        <jsp:include page="fragments/drawer.jsp">
            <jsp:param name="pageSelected" value="1" />
        </jsp:include>
    </body>
</html>
