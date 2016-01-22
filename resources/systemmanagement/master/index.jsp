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


<link rel="import" href="bower_components/iron-icons/iron-icons.html">
<link rel="import" href="bower_components/iron-flex-layout/iron-flex-layout.html">
<link rel="import" href="bower_components/iron-image/iron-image.html">
<link rel="import" href="bower_components/iron-icons/image-icons.html">
<link rel="import" href="bower_components/iron-form/image-form.html">



<link rel="import" href="bower_components/neon-animation/neon-animations.html">
</head>
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
					<paper-menu selected="0">
						<paper-item ><iron-icon icon="icons:flip-to-front"></iron-icon><span class="span-icon">Monitoring</span></paper-item>
						<paper-item ><iron-icon icon="image:blur-on"></iron-icon><span class="span-icon">Simulations</span></paper-item>
						<paper-item ><iron-icon icon="create"></iron-icon><span class="span-icon">Examples</span></paper-item>
						<paper-item ><iron-icon icon="history"></iron-icon><span class="span-icon">History</span></paper-item>
                        <paper-item ><iron-icon icon="settings"></iron-icon><span class="span-icon">Settings</span></paper-item>
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

                    <script>
                        var grid=document.getElementById("workers");
                        var tiles="<div class=\"grid-sizer\"></div>";
                        for (i = 0; i < 24; i++) {
                             tiles+="<div class=\"grid-item\" >1</div>";
                        }
                        grid.innerHTML=tiles;
                    </script>
                </div>
                <paper-fab id="add-simulation-to-worker-buttom" icon="add" onclick="open_dialog_setting_new_simulation()"></paper-fab>
                <paper-toast id="miss-worker-selection">You should select some workers before to assign them a partitioning</paper-toast>
                <paper-dialog id="animated-paper-dialog" entry-animation="scale-up-animation" exit-animation="fade-out-animation" with-backdrop>
                    <h2>Simulation Settings</h2>

                    <div class="horizontal-section">
                        <form is="iron-form" id="formGet" method="get" action="/">
                            <input type="file" id="simulation-jar-chooser"name="pic" accept="" onchange="startProgress()"><br>
                            <paper-progress></paper-progress>
                            <br>
                            <span>Partitioning</span>
                            <hr>
                            <paper-input name="name" label="Name" required></paper-input>
                            <paper-input name="animal" label="Favourite animal" required></paper-input>
                            <br>

                            <input type="checkbox" name="food" value="donuts" checked> I like donuts<br>
                            <input type="checkbox" name="food" value="pizza" required> I like pizza<br>
                            <paper-checkbox name="food" value="cheese" required>I like cheese</paper-checkbox><br>

                            <paper-dropdown-menu label="Cars" name="cars" required>
                                <paper-menu class="dropdown-content">
                                    <paper-item>Volvo</paper-item>
                                    <paper-item>Saab</paper-item>
                                    <paper-item>Fiat</paper-item>
                                    <paper-item>Audi</paper-item>
                                </paper-menu>
                            </paper-dropdown-menu>

                            <p>
                            Sample custom element, not required: <br>
                            <simple-element name="custom-one"></simple-element>
                            </p>

                            <p>
                            <?php phpinfo();>
                            Sample custom element, required: (look, styling!)<br>
                            <simple-element required name="custom-two"></simple-element><br>
                            </p>

                            <br><br>

                            <paper-button raised
                            onclick="submitHandler(event)">Submit</paper-button>
                            <paper-button raised
                            onclick="resetHandler(event)">Reset</paper-button>
                        </form>
                    </div>

                </paper-dialog>
            </div>

		</paper-scroll-header-panel>

	</paper-drawer-panel>

</body>
</html>
