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
<link rel="import" href="bower_components/paper-dialog-scrollable/paper-dialog-scrollable.html">
<link rel="import" href="bower_components/paper-listbox/paper-listbox.html">
<link rel="import" href="bower_components/paper-dropdown-menu/paper-dropdown-menu.html">


<link rel="import" href="bower_components/iron-icons/iron-icons.html">
<link rel="import" href="bower_components/iron-flex-layout/iron-flex-layout.html">
<link rel="import" href="bower_components/iron-image/iron-image.html">
<link rel="import" href="bower_components/iron-icons/image-icons.html">



<link rel="import" href="bower_components/neon-animation/neon-animations.html">
</head>
<body unresolved onload="load_tiles_monitoring()">

	<paper-drawer-panel force-narrow >
		<paper-scroll-header-panel drawer id="side-header-panel" fixed fill>
			<paper-toolbar class="side-drawer">
				<div>Control Panel</div>
                <paper-icon-button icon="chevron-left" paper-drawer-toggle ></paper-icon-button>
			</paper-toolbar>
			<div class="content content-side-bar">
                <hr>
				<app-sidebar>
                        <a style="text-decoration:none;" href="index.jsp"><paper-item class="selected"> <iron-icon icon="icons:flip-to-front"></iron-icon><span class="span-icon">Monitoring</span></paper-item></a>
                        <a style="text-decoration:none;" href="simulations.jsp"><paper-item ><iron-icon icon="image:blur-on"></iron-icon><span class="span-icon">Simulations</span></paper-item></a>
                        <a style="text-decoration:none;" href="history.jsp"><paper-item><iron-icon icon="history"></iron-icon><span class="span-icon">History</span></paper-item></a>
                        <a style="text-decoration:none;" href="settings.jsp"><paper-item><iron-icon icon="settings"></iron-icon><span class="span-icon">Settings</span></paper-item></a>
				</app-sidebar>
			</div>
		</paper-scroll-header-panel>
    <jsp:useBean id="masterServer" class="it.isislab.dmason.experimentals.systemmanagement.master.MasterServer" scope="session"></jsp:useBean>
		<paper-scroll-header-panel main fixed>
			<paper-toolbar flex id="mainToolBar" class="horizontal">
				<div><paper-icon-button icon="menu" paper-drawer-toggle ></paper-icon-button></div>
                <div class="flex"><span>DMASON Master</span></div>
                <div><paper-icon-button icon="refresh" onclick="loadWorkers()"></paper-icon-button></div>
		    </paper-toolbar>

             <div class="content content-main">
                <div class="grid-monitoring" id="workers">

                </div>
                <paper-fab id="add-simulation-to-worker-buttom" icon="add" onclick="open_dialog_setting_new_simulation()"></paper-fab>
                <paper-toast id="miss-worker-selection">You should select some workers before to assign them a partitioning</paper-toast>
                <paper-dialog id="add-simulation-paper-dialog" entry-animation="scale-up-animation" exit-animation="fade-out-animation" with-backdrop>
                    <h2>Simulation Settings</h2>
                    <paper-dialog-scrollable>
                        <div class="horizontal-section">
                            <form is="iron-form" id="formGet" method="get" action="/">
                                <table>
                                    <tr>
                                        <td>
                                            <span>Select an external simulation</span><br>
                                            <paper-button raised class="custom" onclick='opne_file_chooser()'>Upload<iron-icon icon="file-upload"></iron-icon></paper-button>
                                            <input type="file" id="simulation-jar-chooser" name="sim-exe" accept="" onchange="startProgress()">
                                        </td>
                                        <td></td>
                                        <td>
                                            <span>Select an example simulation</span><br>
                                                <paper-dropdown-menu label="Select">
                                                    <paper-listbox class="dropdown-content">
                                                        <paper-item>allosaurus</paper-item>
                                                        <paper-item>brontosaurus</paper-item>
                                                        <paper-item>carcharodontosaurus</paper-item>
                                                        <paper-item>diplodocus</paper-item>
                                                    </paper-listbox>
                                                </paper-dropdown-menu>
                                            <!--paper-button raised class="custom">Select<iron-icon icon="receipt"></iron-icon></paper-button-->
                                        </td>
                                    </tr>
                                    <tr><td colspan="3"><paper-progress></paper-progress></td></tr>
                                    <tr>
                                        <td colspan="3" style="text-align:center; text-transform: uppercase;"><span>Partitioning</span></td>
                                    </tr>
                                    <tr>
                                        <td colspan="3" style="text-align:center">
                                            <paper-radio-group>
                                                <paper-radio-button name="uniform" >Uniform <iron-icon icon="view-module"></iron-icon></paper-radio-button>
                                                <paper-radio-button name="non-uniform">Non-Uniform<iron-icon icon="view-quilt"></iron-icon></paper-radio-button>
                                            </paper-radio-group>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td><paper-input class="submit_work_form" label="Rows" allowed-pattern="[0-9]"></paper-input></td>
                                        <td><paper-input class="submit_work_form" label="Columns" allowed-pattern="[0-9]"></paper-input></td>
                                        <td><paper-input class="submit_work_form" label="Area of interest" allowed-pattern="[0-9]"></paper-input></td>
                                    </tr>
                                    <tr>
                                        <td><paper-input class="submit_work_form" label="Width" allowed-pattern="[0-9]"></paper-input></td>
                                        <td><paper-input class="submit_work_form" label="Heigth" allowed-pattern="[0-9]"></paper-input></td>
                                        <td><paper-input class="submit_work_form" label="Number of Agents" allowed-pattern="[0-9]"></paper-input></td>
                                    </tr>

                                <tr><td></td>
                                <td colspan='2' style="text-align:right; padding-top:50px;"><paper-button raised
                                onclick="resetHandler(event)">Reset</paper-button>
                                <paper-button raised
                                onclick="submitHandler(event)">Submit</paper-button></td></tr>
                                </table>
                            </form>
                            <paper-dialog></paper-dialog>
                        </div>
                    </paper-dialog-scrollable>

		</paper-scroll-header-panel>

	</paper-drawer-panel>

</body>
</html>
