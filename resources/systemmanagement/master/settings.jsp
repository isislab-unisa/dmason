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
        <link rel="import" href="bower_components/paper-tooltip/paper-tooltip.html">
        <link rel="import" href="bower_components/paper-tabs/paper-tabs.html">
        <link rel="import" href="bower_components/paper-tabs/paper-tab.html">

        <link rel="import" href="bower_components/iron-icons/iron-icons.html">
        <link rel="import" href="bower_components/iron-flex-layout/iron-flex-layout-classes.html">
        <link rel="import" href="bower_components/iron-image/iron-image.html">
        <link rel="import" href="bower_components/iron-icons/image-icons.html">
        <link rel="import" href="bower_components/iron-pages/iron-pages.html">
    </head>
    <body unresolved onload="load_tiles_settings()">
        <!-- Testata pagina -->
        <jsp:include page="fragments/header.jsp">
			<jsp:param name="page" value="settings" />
		</jsp:include>

        <!-- Corpo pagina -->
        <!--<paper-tabs selected="{{selected}}">
            <paper-tab>Apache MQ</paper-tab>
            <paper-tab>Amazon AWS</paper-tab>
            <paper-tab>Microsoft Azure</paper-tab>
        </paper-tabs>-->

        <!--<iron-pages selected="{{selected}}">--><!-- TODO rimuovere paper-tabs e iron-pages -->
            <div class="content content-main">
                <div class="grid-settings" id="workers">
                    <%-- Apache ActiveMQ card --%>
                    <paper-card heading="Apache ActiveMQ" class="grid-item-settings">
                        <div class="card-image">
                            <img src="images/activemq-logo.png" alt="Apache ActiveMQ"></img>
                        </div>
                        <div class="card-content">
                            <paper-input id="activemqip" label="Server IP" auto-validate pattern="^(?:[0-9]{1,3}\.){3}[0-9]{1,3}$" error-message="Wrong IP format!"></paper-input>
                            <paper-tooltip for="activemqip" position="bottom" animation-delay="0" offset="1">Specify an Apache ActiveMQ server IP</paper-tooltip>
                            <paper-input id="activemqport" label="Access port" auto-validate pattern="^([0-9]{1,4}|[1-5][0-9]{4}|6[0-4][0-9]{3}|65[0-4][0-9]{2}|655[0-2][0-9]|6553[0-5])$" error-message="Wrong port range!"></paper-input>
                            <paper-tooltip for="activemqport" position="bottom" animation-delay="0" offset="1">Specify an access port for ActiveMQ server</paper-tooltip>
                        </div>
                        <div class="card-actions">
                            <div class="horizontal justified">
                                <paper-button class="card-button" disabled><iron-icon icon="check"></iron-icon>&nbsp;Set</paper-button>
                            </div>
                        </div>
                    </paper-card>
                    <%-- Amazon AWS EC2 card --%>
                    <paper-card heading="Amazon AWS EC2" class="grid-item-settings">
                        <div class="card-image">
                            <img src="images/amazonwebservices-logo.svg" alt="Amazon AWS logo"></img>
                        </div>
                        <div class="card-content">
                            <paper-input id="region" label="Region" auto-validate pattern="[a-z]{2}-[a-z]{4,5}-[0-9]{1}" error-message="Wrong region format!"></paper-input>
                            <paper-tooltip for="region" position="bottom" animation-delay="0" offset="1">Specify an Amazon AWS region</paper-tooltip>
                            <paper-input id="pubkey" label="Public API Key" auto-validate pattern="" error-message="Wrong public API key!"></paper-input>
                            <paper-tooltip for="pubkey" position="bottom" animation-delay="0" offset="1">Specify the public key associated to the Amazon account</paper-tooltip>
                            <paper-input id="prikey" label="Private API Key" auto-validate pattern="" error-message="Wrong private API key!"></paper-input>
                            <paper-tooltip for="prikey" position="bottom" animation-delay="0" offset="1">Specify the private key associated to the Amazon account</paper-tooltip>
                        </div>
                        <div class="card-actions">
                            <div class="horizontal justified">
                                <paper-button class="card-button" disabled><iron-icon icon="check"></iron-icon>&nbsp;Set</paper-button>
                            </div>
                        </div>
                    </paper-card>
                    <%-- Microsoft Azure card --%>
                    <paper-card heading="Microsoft Azure" class="grid-item-settings">
                        <div class="card-image">
                            <img src="images/microsoftazure.svg" alt="Microsoft Azure"></img>
                        </div>
                        <div class="card-content">
                            <p style="color: dimgray;">Coming soon!</p>
                        </div>
                        <div class="card-actions">
                            <div class="horizontal justified">
                                <paper-button class="card-button" disabled><iron-icon icon="check"></iron-icon>&nbsp;Set</paper-button>
                            </div>
                        </div>
                    </paper-card>
                </div>
            </div>
        <!--</iron-pages>-->

        <!--<script>
            var pages = document.querySelector('iron-pages');
            var tabs = document.querySelector('paper-tabs');

            tabs.addEventListener('iron-select', function() { 
                pages.selected = tabs.selected;
            });
        </script>-->

        <!-- menu laterale a scorrimento -->
        <jsp:include page="fragments/drawer.jsp">
            <jsp:param name="pageSelected" value="3" />
        </jsp:include>
    </body>
</html>
