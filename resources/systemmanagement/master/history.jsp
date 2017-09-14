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

        <!-- Import simulation elements -->
        <link rel="import" href="custom_components/history/sim-history-grid.html">

        <!-- Import element -->
		<link rel="import" href="bower_components/app-layout/app-drawer-layout/app-drawer-layout.html">
		<link rel="import" href="bower_components/app-layout/app-drawer/app-drawer.html">
		<link rel="import" href="bower_components/app-layout/app-header-layout/app-header-layout.html">
		<link rel="import" href="bower_components/app-layout/app-header/app-header.html">
        <link rel="import" href="bower_components/app-layout/app-toolbar/app-toolbar.html">

        <link rel="import" href="bower_components/neon-animation/animations/scale-up-animation.html">
        <link rel="import" href="bower_components/neon-animation/animations/fade-out-animation.html">

        <link rel="import" href="bower_components/paper-styles/paper-styles.html">
        <link rel="import" href="bower_components/paper-icon-button/paper-icon-button.html">
        <link rel="import" href="bower_components/paper-menu/paper-menu.html">
        <link rel="import" href="bower_components/paper-item/paper-item.html">
        <link rel="import" href="bower_components/paper-fab/paper-fab.html">
        <link rel="import" href="bower_components/paper-toast/paper-toast.html">
        <link rel="import" href="bower_components/paper-dialog-scrollable/paper-dialog-scrollable.html">"
        <link rel="import" href="bower_components/paper-dialog/paper-dialog.html">
        <link rel="import" href="bower_components/paper-spinner/paper-spinner.html">
        <link rel="import" href="bower_components/paper-badge/paper-badge.html">

        <link rel="import" href="bower_components/iron-icons/iron-icons.html">
        <link rel="import" href="bower_components/iron-flex-layout/iron-flex-layout-classes.html">
        <link rel="import" href="bower_components/iron-image/iron-image.html">
        <link rel="import" href="bower_components/iron-icons/image-icons.html">
        <link rel="import" href="bower_components/iron-icons/editor-icons.html">
    </head>
    <body unresolved>
		<!-- Testata pagina -->
		<app-header reveals fixed slot="header">
            <app-toolbar flex id="mainToolBar" class="horizontal">
                <paper-icon-button icon="menu" onclick="drawer.toggle()" drawer-toggle></paper-icon-button>
                <div class="flex" spacer main-title><span>DMASON Master</span></div>
                <div onclick="cleanHistory()" class="cleanAllHistory">
                    <paper-icon-button icon="select-all"></paper-icon-button><span>Clean all history</span>
                </div>
            </app-toolbar>
        </app-header>

        <!-- Corpo pagina -->
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

		<!-- menu laterale a scorrimento -->
		<app-drawer id="drawer" slot="drawer" swipe-open>
            <app-header-layout id="side-header-panel" fixed fill>
                <!-- header del drawer -->
                <app-toolbar class="side-drawer">
                    <div style="margin-right:5px;">Control Panel</div>
                    <paper-icon-button icon="chevron-left" onclick="drawer.toggle()"></paper-icon-button>
                </app-toolbar>
                <!-- menu drawer -->
                <nav class="content content-side-bar">
                    <paper-menu selected="2">
                        <paper-item>
                            <a href="index.jsp">
                                <iron-icon icon="icons:flip-to-front" item-icon slot="item-icon"></iron-icon>
                                <span class="span-icon">Monitoring</span>
                            </a>
                        </paper-item>
                        <paper-item>
                            <a href="simulations.jsp">
                                <iron-icon icon="image:blur-on" item-icon slot="item-icon"></iron-icon>
                                <span class="span-icon">Simulations</span>
                            </a>
                        </paper-item>
                        <paper-item class="selected">
                            <a href="history.jsp">
                                <iron-icon icon="history" item-icon slot="item-icon"></iron-icon>
                                <span class="span-icon">History</span>
                            </a>
                        </paper-item>
                        <paper-item>
                            <a href="settings.jsp">
                                <iron-icon icon="settings" item-icon slot="item-icon"></iron-icon>
                                <span class="span-icon">Settings</span>
                            </a>
                        </paper-item>
                    </paper-menu>
                <nav>
            </app-header-layout>
        </app-drawer>

        <!-- Bottoni in fondo e messaggi di avviso -->
        <paper-fab id="delete-history-button" icon="cancel" onclick="cleanSelectedHistory()"></paper-fab>
        <paper-fab id="go_to_workbenck" icon="editor:insert-chart"></paper-fab>
        <paper-toast id="miss-history-delete">You need to select some Simulations to delete</paper-toast>
    </body>
</html>
