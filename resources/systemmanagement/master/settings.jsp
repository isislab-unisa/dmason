    <html>
        <head>
        <title>DMASON - System Management</title>
        <link rel="shortcut icon" type="image/png" href="images/dmason-ico.png"/>
        <!-- Polyfill Web Components for older browsers -->
        <script src="bower_components/webcomponentsjs/webcomponents-lite.min.js"></script>

        <!--polymer theme-->
        <link rel="import" href="style/dark-side/dark-side.html">

        <!-- Custom Polymer CSS -->
        <link rel="import" href="style/polymer/styles-polymer.html">

        <!-- Custom CSS -->
        <link href="style/custom-style.css" rel="stylesheet" type="text/css">


        <!-- jquery -->
        <script src="js/jquery-1.12.0.min.js"></script>

        <!-- Mansory lib -->
        <script src="js/masonry.pkgd.min.js"></script>


        <!-- Custom Scripts -->
        <script src="js/script.js"></script>

        <!-- Import element -->
        <link rel="import" href="bower_components/paper-icon-button/paper-icon-button.html">
        <link rel="import" href="bower_components/paper-toolbar/paper-toolbar.html">
        <link rel="import" href="bower_components/paper-drawer-panel/paper-drawer-panel.html">
        <link rel="import" href="bower_components/paper-scroll-header-panel/paper-scroll-header-panel.html">
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
        <link rel="import" href="bower_components/paper-dialog-scrollable/paper-dialog-scrollable.html">"
        <link rel="import" href="bower_components/paper-card/paper-card.html">

        <link rel="import" href="bower_components/iron-icons/iron-icons.html">
        <link rel="import" href="bower_components/iron-flex-layout/iron-flex-layout.html">
        <link rel="import" href="bower_components/iron-image/iron-image.html">
        <link rel="import" href="bower_components/iron-icons/image-icons.html">



        <link rel="import" href="bower_components/neon-animation/neon-animations.html">
        </head>
        <body unresolved onload="load_tiles_settings()">

        <paper-drawer-panel force-narrow >
        <paper-scroll-header-panel drawer id="side-header-panel" fixed fill>
        <paper-toolbar class="side-drawer">
        <div>Control Panel</div>
        <paper-icon-button icon="chevron-left" paper-drawer-toggle ></paper-icon-button>
        </paper-toolbar>
        <div class="content content-side-bar">
        <hr>
        <paper-menu>
            <paper-item ><a style="text-decoration:none;" href="index.jsp"> <iron-icon icon="icons:flip-to-front"></iron-icon><span class="span-icon">Monitoring</span></a></paper-item>
            <paper-item><a style="text-decoration:none;" href="simulations.jsp"><iron-icon icon="image:blur-on"></iron-icon><span class="span-icon">Simulations</span></a></paper-item>
            <paper-item><a style="text-decoration:none;" href="history.jsp"><iron-icon icon="history"></iron-icon><span class="span-icon">History</span></a></paper-item>
            <paper-item class="selected"><a style="text-decoration:none;" href="settings.jsp"><iron-icon icon="settings"></iron-icon><span class="span-icon">Settings</span></a></paper-item>
        </paper-menu>
        </div>
        </paper-scroll-header-panel>

        <paper-scroll-header-panel main fixed>
        <paper-toolbar flex id="mainToolBar">
        <paper-icon-button icon="menu" paper-drawer-toggle ></paper-icon-button>
        <span>DMASON Master</span>
        </paper-toolbar>

        <div class="content content-main">
        <div class="grid-settings" id="workers">
            <script>
                var grid=document.getElementById("workers");
                var tiles="<div class=\"grid-sizer-settings\"></div>";
                for (i = 0; i < 5; i++) {
                tiles+="<div class=\"grid-item-settings\">"
                + "<paper-card image=\"images/Apache-activemq-logo.png\">"
                +"<div class=\"card-content\" >"
                +"<paper-input label=\"192.168.0.1\" auto-validate pattern=\"^(?:[0-9]{1,3}\.){3}[0-9]{1,3}$\" error-message=\"Wrong IP format!\"></paper-input>"
                +"<paper-input label=\"80\" auto-validate pattern=\"^([0-9]{1,4}|[1-5][0-9]{4}|6[0-4][0-9]{3}|65[0-4][0-9]{2}|655[0-2][0-9]|6553[0-5])$\" error-message=\"Wrong port range!\"></paper-input>"
                +"</div>"
                +"<div class=\"card-actions\" style='border-style: none'>"
                +"<paper-button style=\"float:right\" disabled><iron-icon icon=\"check\"></iron-icon>Set</paper-button>"
                +"</div>"
                +"</paper-card>"


        +"</div>";
                }
                grid.innerHTML=tiles;
            </script>
        </div>
        </div>
        </paper-scroll-header-panel>

        </paper-drawer-panel>

        </body>
        </html>
