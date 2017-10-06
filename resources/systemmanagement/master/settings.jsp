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

        <!-- Import element -->
		<link rel="import" href="bower_components/app-layout/app-drawer-layout/app-drawer-layout.html">
		<link rel="import" href="bower_components/app-layout/app-drawer/app-drawer.html">
		<link rel="import" href="bower_components/app-layout/app-header-layout/app-header-layout.html">
		<link rel="import" href="bower_components/app-layout/app-header/app-header.html">
		<link rel="import" href="bower_components/app-layout/app-toolbar/app-toolbar.html">

		<link rel="import" href="bower_components/neon-animation/web-animations.html">
		<link rel="import" href="bower_components/neon-animation/animations/scale-up-animation.html">

        <link rel="import" href="bower_components/paper-dropdown-menu/paper-dropdown-menu.html">
		<link rel="import" href="bower_components/paper-styles/paper-styles.html">
        <link rel="import" href="bower_components/paper-icon-button/paper-icon-button.html">
        <link rel="import" href="bower_components/paper-menu/paper-menu.html">
        <link rel="import" href="bower_components/paper-item/paper-item.html">
        <link rel="import" href="bower_components/paper-fab/paper-fab.html">
        <link rel="import" href="bower_components/paper-toast/paper-toast.html">
        <link rel="import" href="bower_components/paper-dialog/paper-dialog.html">
        <link rel="import" href="bower_components/paper-dialog-scrollable/paper-dialog-scrollable.html">
        <link rel="import" href="bower_components/paper-button/paper-button.html">
        <link rel="import" href="bower_components/paper-radio-button/paper-radio-button.html">
        <link rel="import" href="bower_components/paper-radio-group/paper-radio-group.html">
        <link rel="import" href="bower_components/paper-input/paper-input.html">
        <link rel="import" href="bower_components/paper-progress/paper-progress.html">
        <link rel="import" href="bower_components/paper-card/paper-card.html">

        <link rel="import" href="bower_components/iron-icons/iron-icons.html">
        <link rel="import" href="bower_components/iron-flex-layout/iron-flex-layout-classes.html">
        <link rel="import" href="bower_components/iron-image/iron-image.html">
        <link rel="import" href="bower_components/iron-icons/image-icons.html">
    </head>

    <body unresolved onload="load_tiles_settings()">
        <!-- Testata pagina -->
        <app-header reveals fixed slot="header">
            <app-toolbar flex id="mainToolBar" class="horizontal">
                <paper-icon-button icon="menu" onclick="drawer.toggle()" drawer-toggle></paper-icon-button>
                <div class="flex" spacer main-title><span>DMASON Master</span></div>
            </app-toolbar>
        </app-header>

        <!-- Corpo pagina -->
        <div class="content content-main">
            <div class="grid-settings" id="workers">
                <script>
                    var grid = document.getElementById("workers");
                    var tiles = "<div class=\"grid-sizer-settings\"></div>";
                    for (i = 0; i < 1; i++) {
                        tiles += "<div class=\"grid-item-settings\" style=\"margin: 8px 0 0 8px;\">"
                        + "<paper-card image=\"images/Apache-activemq-logo.png\">"
                        + "<div class=\"card-content\" >"
                        + "<paper-input label=\"192.168.0.1\" auto-validate pattern=\"^(?:[0-9]{1,3}\.){3}[0-9]{1,3}$\" error-message=\"Wrong IP format!\"></paper-input>"
                        + "<paper-input label=\"80\" auto-validate pattern=\"^([0-9]{1,4}|[1-5][0-9]{4}|6[0-4][0-9]{3}|65[0-4][0-9]{2}|655[0-2][0-9]|6553[0-5])$\" error-message=\"Wrong port range!\"></paper-input>"
                        + "</div>"
                        + "<div class=\"card-actions\" style=\"border-style: none;\">"
                        + "<paper-button raised style=\"float:right;\" disabled><iron-icon icon=\"check\"></iron-icon>&nbsp;Set</paper-button>"
                        + "</div>"
                        + "</paper-card>"
                        + "</div>";
                    }
                    grid.innerHTML = tiles;
                </script>
            </div>
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
                    <paper-menu selected="3">
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
                        <paper-item>
                            <a href="history.jsp">
                                <iron-icon icon="history" item-icon slot="item-icon"></iron-icon>
                                <span class="span-icon">History</span>
                            </a>
                        </paper-item>
                        <paper-item class="selected">
                            <a href="settings.jsp">
                                <iron-icon icon="settings" item-icon slot="item-icon"></iron-icon>
                                <span class="span-icon">Settings</span>
                            </a>
                        </paper-item>
                    </paper-menu>
                <nav>
            </app-header-layout>
        </app-drawer>
    </body>
</html>
