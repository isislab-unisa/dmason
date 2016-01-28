    <html>
        <head>
        <title>DMASON - System Management</title>
        <link rel="shortcut icon" type="image/png" href="images/dmason-ico.png"/>
        <!-- Polyfill Web Components for older browsers -->
        <script src="bower_components/webcomponentsjs/webcomponents-lite.min.js"></script>

        <!--polymer theme-->
        <link rel="import" href="style/dark-side/dark-side.html">

        <!-- Custom Polymer CSS -->
        <link rel="import" href="style/styles-polymer.html">

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


        <link rel="import" href="bower_components/iron-icons/iron-icons.html">
        <link rel="import" href="bower_components/iron-flex-layout/iron-flex-layout.html">
        <link rel="import" href="bower_components/iron-image/iron-image.html">
        <link rel="import" href="bower_components/iron-icons/image-icons.html">



        <link rel="import" href="bower_components/neon-animation/neon-animations.html">
        </head>
        <body unresolved onload="load_tiles_simulations()">

        <paper-drawer-panel force-narrow >
        <paper-scroll-header-panel drawer id="side-header-panel" fixed fill>
        <paper-toolbar class="side-drawer">
        <div>Control Panel</div>
        <paper-icon-button icon="chevron-left" paper-drawer-toggle ></paper-icon-button>
        </paper-toolbar>
        <div class="content content-side-bar">
        <hr>
        <app-sidebar>

        <a href="index.jsp" style="text-decoration:none;"> <paper-item ><iron-icon icon="icons:flip-to-front"></iron-icon><span class="span-icon">Monitoring</span></paper-item></a>
        <a href="simulations.jsp" style="text-decoration:none;"><paper-item class="selected"><iron-icon icon="image:blur-on"></iron-icon><span class="span-icon">Simulations</span></paper-item></a>
        <a href="examples.jsp" style="text-decoration:none;"><paper-item onclick="open_dialog('examples')"><iron-icon icon="create"></iron-icon><span class="span-icon">Examples</span></paper-item></a>
        <a href="history.jsp" style="text-decoration:none;"><paper-item onclick="open_dialog('history')"><iron-icon icon="history"></iron-icon><span class="span-icon">History</span></paper-item></a>
        <a href="settings.jsp" style="text-decoration:none;"><paper-item onclick="open_dialog('settings')"><iron-icon icon="settings"></iron-icon><span class="span-icon">Settings</span></paper-item></a>

        </app-sidebar>
        </div>
        </paper-scroll-header-panel>

        <paper-scroll-header-panel main fixed>
        <paper-toolbar flex id="mainToolBar">
        <paper-icon-button icon="menu" paper-drawer-toggle ></paper-icon-button>
        <span>DMASON Master</span>
        </paper-toolbar>

        <div class="content content-main">
                <div class="grid-simulations" id="workers">
                    <script>
                    var grid=document.getElementById("workers");
                    var tiles="<div class=\"grid-sizer-simulations\"></div>";
                    for (i = 0; i < 24; i++) {
                    tiles+="<div class=\"grid-item-simulations\" onclick=\"open_dialog('worker-paper-dialog')\">"
                            +"<div class=\"worker-system-info\"><span id=\"worker-id\">Worker ID: "+(i+1)+"</span></div>"
                            +"<div class=\"worker-system-info\"><span>CPU: %</span></div>"
                            +"<div class=\"worker-system-info\"><span>Memory: Free  % Usage  %</span></div>"
                            +"<div class=\"worker-system-info\"><span>IP: </span></div>"
                            +"<div class=\"worker-system-info\"><span>#Simulations</span></div>"
                            +"</div>";
                    }
                    grid.innerHTML=tiles;
                    </script>
                </div>
        </div>
        <paper-dialog id="worker-paper-dialog" entry-animation="scale-up-animation" exit-animation="fade-out-animation" with-backdrop>
            <h2>Worker Info</h2>
            <paper-dialog-scrollable>
                <div class="horizontal-section">

                </div>
            </paper-dialog-scrollable>
        </paper-dialog>
        </paper-scroll-header-panel>

        </paper-drawer-panel>

        </body>
        </html>
