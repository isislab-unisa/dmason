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


        <link rel="import" href="bower_components/iron-icons/iron-icons.html">
        <link rel="import" href="bower_components/iron-flex-layout/iron-flex-layout.html">
        <link rel="import" href="bower_components/iron-image/iron-image.html">
        <link rel="import" href="bower_components/iron-icons/image-icons.html">



        <link rel="import" href="bower_components/neon-animation/neon-animated-pages.html">
        <link rel="import" href="bower_components/neon-animation/neon-animations.html">
        <link rel="import" href="bower_components/custom_components/animated-grid.html">
        <link rel="import" href="bower_components/custom_components/fullsize-page-with-card.html">

        </head>
        <!--body unresolved onload="load_tiles_simulations()"-->
        <body unresolved>

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
        <a href="history.jsp" style="text-decoration:none;"><paper-item ><iron-icon icon="history"></iron-icon><span class="span-icon">History</span></paper-item></a>
        <a href="settings.jsp" style="text-decoration:none;"><paper-item ><iron-icon icon="settings"></iron-icon><span class="span-icon">Settings</span></paper-item></a>

        </app-sidebar>
        </div>
        </paper-scroll-header-panel>

        <paper-scroll-header-panel main fixed>
        <paper-toolbar flex id="mainToolBar">
        <paper-icon-button icon="menu" paper-drawer-toggle ></paper-icon-button>
        <span>DMASON Master</span>
        </paper-toolbar>

        <div class="content content-main">
            <template is="dom-bind" id="simulations">
                <neon-animated-pages id="pages" selected="0">
                        <animated-grid id="list-simulations" on-tile-click="_onTileClick"></animated-grid>
                        <!--fullsize-page-with-card id="fullsize-card" on-click="_onFullsizeClick"-->
                        <fullsize-page-with-card id="fullsize-card" on-go-back="_onFullsizeClick">
                        </fullsize-page-with-card>
                </neon-animated-pages>
            </template>

        <script>
        <%@ page import="java.lang.System" %>
        var scope = document.querySelector('template[is="dom-bind"]');
           var list_sim =[];
            for(i=0; i<40; i++)
                    list_sim[i]={id:i, name:"flockers",start:<%=System.currentTimeMillis() %>,step:0,num_cell:(i%6)+1,num_worker:(i%5)+1,partitioning:(i%2==0)?'uniform':'non-uniform'};

            scope.addEventListener('dom-change',function(event){
                //this.$['list-simulations'].listItem = [{id:1, name:"flockers"},{id:2, name:"Ants"}];
                this.$['list-simulations'].listItem = list_sim;
            });


        scope._onTileClick = function(event) {
            this.$['fullsize-card'].sim = event.detail.data;
        var lf=[];
        for(i=0; i<10;i++)
            lf[i]={name:'file'+i,time:"22/01/2016"};

            this.$['fullsize-card'].listFile =lf;
            this.$.pages.selected = 1;
        };
        scope._onFullsizeClick = function(event) {
        this.$.pages.selected = 0;
        };
        </script>

        </div>
        </paper-scroll-header-panel>

        </paper-drawer-panel>

        </body>
        </html>
