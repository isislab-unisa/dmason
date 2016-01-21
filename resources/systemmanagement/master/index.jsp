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

<link rel="import" href="bower_components/iron-icons/iron-icons.html">
<link rel="import" href="bower_components/iron-flex-layout/iron-flex-layout.html">
<link rel="import" href="bower_components/iron-image/iron-image.html">

</head>
<body unresolved>

	<paper-drawer-panel force-narrow >
		<paper-scroll-header-panel drawer id="side-header-panel" fixed>
			<paper-toolbar class="side-drawer">
				<div>Control Panel</div>
                <paper-icon-button icon="chevron-left" paper-drawer-toggle></paper-icon-button>
			</paper-toolbar>
			<div class="content content-side-bar">
                <hr>
				<app-sidebar >
					<paper-menu >
						<paper-item onclick="">1</paper-item>
						<paper-item onclick="">1</paper-item>
						<paper-item onclick="">1</paper-item>
						<paper-item onclick="">1</paper-item>
						<paper-item onclick="">1</paper-item>
                        <paper-item onclick="">1</paper-item>
                        <paper-item onclick="">1</paper-item>
                    </paper-menu>
				</app-sidebar>
			</div>
		</paper-scroll-header-panel>

		<paper-scroll-header-panel main fixed>
			<paper-toolbar flex id="mainToolBar">
				<paper-icon-button icon="menu" paper-drawer-toggle ></paper-icon-button>
                <span>DMASON Master</span>
				<!--img src="images/icoRed.png"/-->
		    </paper-toolbar>

             <div class="content content-main">
                <div class="grid" id="workers">
                    <!--div class="grid-sizer"></div-->
                    <script>
        var grid=document.getElementById("workers");
        var tiles="<div class=\"grid-sizer\"></div>";
        for (i = 0; i < 200; i++) {
             tiles+="<div class=\"grid-item\">10</div>";
        }
        grid.innerHTML=tiles;
                    </script>
                </div>
                <paper-fab id="add-simulation-to-worker-buttom" icon="add" ></paper-fab>
            </div>

		</paper-scroll-header-panel>

	</paper-drawer-panel>

</body>
</html>
