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
<link rel="import" href="bower_components/paper-spinner/paper-spinner.html">

<link rel="import" href="bower_components/iron-icons/iron-icons.html">
<link rel="import" href="bower_components/iron-flex-layout/iron-flex-layout.html">
<link rel="import" href="bower_components/iron-image/iron-image.html">
<link rel="import" href="bower_components/iron-icons/image-icons.html">
<link rel="import" href="bower_components/iron-form/iron-form.html">


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
                        <a style="text-decoration:none;" href="index.jsp"><paper-item class="selected"> <iron-icon icon="icons:flip-to-front"></iron-icon><span class="span-icon">Monitoring</span></paper-item></a>
                        <a style="text-decoration:none;" href="simulations.jsp"><paper-item ><iron-icon icon="image:blur-on"></iron-icon><span class="span-icon">Simulations</span></paper-item></a>
                        <a style="text-decoration:none;" href="history.jsp"><paper-item><iron-icon icon="history"></iron-icon><span class="span-icon">History</span></paper-item></a>
                        <a style="text-decoration:none;" href="settings.jsp"><paper-item><iron-icon icon="settings"></iron-icon><span class="span-icon">Settings</span></paper-item></a>
				</app-sidebar>
			</div>
		</paper-scroll-header-panel>
    <jsp:useBean id="masterServer" class="it.isislab.dmason.experimentals.systemmanagement.master.MasterServer" scope="application"/>
		<paper-scroll-header-panel main fixed>
			<paper-toolbar flex id="mainToolBar" class="horizontal">
				<div><paper-icon-button icon="menu" paper-drawer-toggle ></paper-icon-button></div>
                <div class="flex"><span>DMASON Master</span></div>
                <div><paper-icon-button icon="refresh" onclick="loadWorkers()"></paper-icon-button></div>
		    </paper-toolbar>

             <div class="content content-main">

                <paper-dialog opened id="load_workers_dialog"  entry-animation="scale-up-animation" exit-animation="fade-out-animation" modal>
                        <% masterServer.checkAllConnectedWorkers();%>
                        <div class="layout horizontal center">
                            <paper-spinner class="multi" active alt="Loading workers list"></paper-spinner>
                            <span>Loading workers list.....</span>
                        </div>

                </paper-dialog>
                <div class="grid-monitoring" id="workers">
                    <div class=\"grid-sizer-monitoring\"></div>
                    <script>
                        function load_tails_workers(){
                                <%
                                    String message = "{\"workers\":[";
                                    int startMessageSize = message.length();
                                    for(String info : masterServer.getInfoWorkers().values()){
                                        message+=info+",";
                                    }
                                    if(message.length() > startMessageSize)
                                        message=message.substring(0, message.length()-1)+"]}";
                                    else
                                        message="";
                                %>
                                <!-- tails -->
                            _loadWorkers('<%=message %>');

                        }

                    </script>
                </div>
                <paper-fab id="add-simulation-to-worker-buttom" icon="add" onclick="open_dialog_setting_new_simulation()"></paper-fab>
                <paper-toast id="miss-worker-selection">You should select some workers before to assign them a partitioning</paper-toast>
                <paper-dialog id="add-simulation-paper-dialog" entry-animation="scale-up-animation" exit-animation="fade-out-animation" with-backdrop>
                    <h2>Simulation Settings</h2>
                    <paper-dialog-scrollable>
                        <div class="horizontal-section">
                            <form is="iron-form" id="sendSimulationForm" >
                                <table>
                                    <tr>
                                        <td>
                                            <span>Select an external simulation</span><br>
                                            <paper-button raised class="custom" onclick='open_file_chooser()'>Upload<iron-icon icon="file-upload"></iron-icon></paper-button>
                                            <input type="file" class="hidden" id="simulation-jar-chooser" name="simExe" onchange="startProgress()">
                                        </td>
                                        <td></td>
                                        <td>
                                            <span>Select an example simulation</span><br>
                                                <paper-dropdown-menu id="exampleSimulation" label="Select">
                                                    <paper-listbox class="dropdown-content">
                                                        <paper-item label="allosaurus">allosaurus</paper-item>
                                                        <paper-item label="brontosaurus">brontosaurus</paper-item>
                                                        <paper-item label="carcharodontosaurus">carcharodontosaurus</paper-item>
                                                        <paper-item label="diplodocus">diplodocus</paper-item>
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
                                            <paper-radio-group id="partitioning">
                                                <paper-radio-button required name="uniform" ><span>Uniform  <iron-icon icon="view-module"></iron-icon></span></paper-radio-button>
                                                <paper-radio-button required name="non-uniform"> <span>Non-Uniform <iron-icon icon="view-quilt"></iron-icon></span></paper-radio-button>
                                            </paper-radio-group>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td colspan="3" style="text-align:center; text-transform: uppercase;"><span>parameters</span></td>
                                    </tr>
                                    <tr>
                                        <td>
                                            <paper-input name="simName" label="Simulation name" allowed-pattern="[a-zA-Z0-9]"></paper-input>
                                        </td>
                                        <td>
                                            <paper-input class="submit_work_form" name="step" label="Number of step" allowed-pattern="[0-9]"></paper-input>
                                        </td>
                                        <td>
                                            <paper-dropdown-menu id="connectionType" label="Select connection">
                                                <paper-listbox class="dropdown-content">
                                                    <paper-item label="ActiveMQ">ActiveMQ</paper-item>
                                                    <paper-item label="MPI">MPI</paper-item>
                                                </paper-listbox>
                                            </paper-dropdown-menu>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td><paper-input class="submit_work_form" name="rows" label="Rows" allowed-pattern="[0-9]"></paper-input></td>
                                        <td><paper-input class="submit_work_form" name="cols" label="Columns" allowed-pattern="[0-9]"></paper-input></td>
                                        <td><paper-input class="submit_work_form" name="aoi" label="Area of interest" allowed-pattern="[0-9]"></paper-input></td>
                                    </tr>
                                    <tr>
                                        <td><paper-input class="submit_work_form" name="width" label="Width" allowed-pattern="[0-9]"></paper-input></td>
                                        <td><paper-input class="submit_work_form" name="heigth" label="Heigth" allowed-pattern="[0-9]"></paper-input></td>
                                        <td><paper-input class="submit_work_form" name="numAgents" label="Number of Agents" allowed-pattern="[0-9]"></paper-input></td>
                                    </tr>

                                <tr><td></td>
                                    <td colspan='2' style="text-align:right; padding-top:50px;">
                                        <paper-button raised onclick="resetForm(event)">Reset</paper-button>
                                        <paper-button raised onclick="submitForm()">Submit</paper-button>
                                    </td>
                                </tr>
                                </table>
                            </form>
                        </div>
                    </paper-dialog-scrollable>
                </paper-dialog>
            </div>
		</paper-scroll-header-panel>

	</paper-drawer-panel>

</body>
</html>
