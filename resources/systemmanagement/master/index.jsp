<%@ page language="java"
	contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8" %>

<!DOCTYPE html>

<html>
	<head>
		<jsp:include page="fragments/head-common.jsp">
			<jsp:param name="headTitle" value="DMASON - System Management"></jsp:param>
		</jsp:include>

		<%-- Import simulation elements --%>
		<link rel="import" href="custom_components/simulation/simulation-example-list.html">

		<%-- Import iron elements --%>
		<link rel="import" href="bower_components/iron-form/iron-form.html">
		<link rel="import" href="bower_components/iron-icons/maps-icons.html">
        <link rel="import" href="bower_components/iron-pages/iron-pages.html">

		<%-- Import paper elements --%>
        <link rel="import" href="bower_components/paper-card/paper-card.html">
		<link rel="import" href="bower_components/paper-checkbox/paper-checkbox.html">
		<link rel="import" href="bower_components/paper-dropdown-menu/paper-dropdown-menu.html">
		<link rel="import" href="bower_components/paper-input/paper-textarea.html">
		<link rel="import" href="bower_components/paper-listbox/paper-listbox.html">
        <link rel="import" href="bower_components/paper-tabs/paper-tabs.html">
		<link rel="import" href="bower_components/paper-tabs/paper-tab.html">
		<link rel="import" href="bower_components/paper-toggle-button/paper-toggle-button.html">
        <link rel="import" href="bower_components/paper-tooltip/paper-tooltip.html">
	</head>

	<body unresolved>
		<%-- JavaScript check frame --%>
		<%--<jsp:include page="fragments/no-js.jsp"></jsp:include>--%><%-- TODO enable after page fragmentization --%>

		<%-- Page header --%>
		<jsp:include page="fragments/header.jsp">
			<jsp:param name="page" value="index" />
		</jsp:include>

		<%-- Bean MasterServer import --%>
		<jsp:useBean id="masterServer" class="it.isislab.dmason.experimentals.systemmanagement.master.MasterServer" scope="application"/>

		<%-- Page body --%>
		<div class="content content-main">
			<%-- Workers quick stats --%>
			<%--<paper-card heading="Workers statistics" id="workers-stats">
				<div class="card-content">
					<paper-datatable id="workersstats" data="{{data}}">
						<paper-datatable-column header="Available workers" property="available" align="center" type="Number" tooltip="The total number of running workers"></paper-datatable-column>
						<paper-datatable-column header="Selected workers" property="selected" align="center" type="Number" tooltip="The number of selected workers"></paper-datatable-column>
						<paper-datatable-column header="Selected slots from selected workers" property="selectedslots" align="center" type="Number" tooltip="The total number of slots for selected workers"></paper-datatable-column>
					</paper-datatable>
				</div>
			</paper-card>--%><%-- TODO reenable workers stats --%>

			<%-- Workers grid --%>
			<div id="workers" class="grid-monitoring"></div><%-- populated by script --%>

			<%-- Workers nodes loading --%>
			<paper-dialog opened id="load_workers_dialog"  entry-animation="scale-up-animation" exit-animation="fade-out-animation" modal>
				<div class="layout horizontal center">
					<paper-spinner class="multi" active alt="Loading workers list"></paper-spinner>
					<span style="margin-left:5px;">Loading workers list...</span>
				</div>
			</paper-dialog>

			<%-- Workers shutdown loader --%>
			<paper-dialog id="shutdown_workers_dialog"  entry-animation="scale-up-animation" exit-animation="fade-out-animation" modal>
				<div class="layout horizontal center">
					<paper-spinner class="multi" active alt="Shutdown selected workers"></paper-spinner>
					<span style="margin-left:5px;">Shutdown selected workers...</span>
				</div>
			</paper-dialog>

			<%-- Bottom buttons and warnings --%>
			<paper-fab id="add-aws-instance" class="fab" icon="dns" onclick="open_dialog_by_ID('add-ec2-node-dialog')"></paper-fab>
			<paper-tooltip for="add-aws-instance" position="top">Instantiate a new Amazon AWS node</paper-tooltip>
			<paper-toast id="miss-aws-instance">Problem while instantiating an EC2 instance.</paper-toast>

			<paper-fab id="add-simulation-to-worker-buttom" class="fab" icon="add" onclick="open_dialog_setting_new_simulation()"></paper-fab>
			<paper-tooltip for="add-simulation-to-worker-buttom" position="top">Start a new simulation with selected workers</paper-tooltip>
			<paper-toast id="miss-worker-selection">You should select some workers before assigning them a partitioning.</paper-toast>

			<paper-fab id="shutdown-worker-button" class="fab" icon="settings-power" onclick="shutdown()"></paper-fab>
			<paper-tooltip for="shutdown-worker-button" position="top">Shutdown selected workers</paper-tooltip>
			<paper-toast id="miss-worker-shutdown">You need to select some workers before shutting them down.</paper-toast>

			<%-- New simulation panel --%>
			<paper-dialog id="add-simulation-paper-dialog" entry-animation="scale-up-animation" exit-animation="fade-out-animation" modal with-backdrop>
				<div class="layout vertical center">
					<h1>Simulation Settings</h1>
					<h2>Worker(s) selected: <span id="head_sel_works"></span> Available slot(s): <span id="head_num_slots"></span></h2>
				</div>

            	<paper-dialog-scrollable>
                    <div class="horizontal-section">
                        <form is="iron-form" id="sendSimulationForm">
                            <table>
                                <tr>
                                    <td>
                                        <span>Select an external simulation</span><br />
                                        <paper-button raised class="custom" onclick='open_file_chooser()'>Upload<iron-icon icon="file-upload"></iron-icon></paper-button>
                                        <input type="file" class="hidden" id="simulation-jar-chooser" name="simExe">
	                                </td>
                                    <td></td>
                                    <td>
	                                    <span>Select an example simulation</span><br />
                                        <!--paper-dropdown-menu id="exampleSimulation" label="Select">
                                            <paper-listbox class="dropdown-content">
                                                <paper-item id="examplesJarlist">Examples</paper-item>
                                                <paper-item id="customsJarlist">Customs</paper-item>
                                           	</paper-listbox>
                                        </paper-dropdown-menu-->
                                      	<simulation-example-list id="loader_sims_list_example"></simulation-example-list>
                                        <!--paper-button raised class="custom">Select<iron-icon icon="receipt"></iron-icon></paper-button-->
                                    </td>
                                </tr>
                                <tr>
									<td colspan="3"><paper-progress class="red"></paper-progress></td>
								</tr>
                                <tr>
                                    <td colspan="2" style="text-align:center; text-transform: uppercase;"><span>Partitioning</span></td>
                                    <td style="text-align:center; text-transform: uppercase;"><span>extra</span></td>
                                </tr>
                                <tr>
                                    <td colspan="2" style="text-align:center">
                                        <paper-radio-group id="partitioning" selected="uniform">
                                            <paper-radio-button required name="uniform" onclick="change_partitioning_input_params(this)"><span>Uniform  <iron-icon icon="view-module"></iron-icon></span></paper-radio-button>
                                            <paper-radio-button required name="non-uniform" onclick="change_partitioning_input_params(this)"> <span>Non-Uniform <iron-icon icon="view-quilt"></iron-icon></span></paper-radio-button>
                                        </paper-radio-group>
                                    </td>
                                    <td><paper-checkbox id="connectionType" class="layout horizontal center" disabled>enable MPI boost</paper-checkbox></td>
                                </tr>
                                <tr>
                                    <td colspan="3" style="text-align:center; text-transform: uppercase;"><span>parameters</span></td>
                                </tr>
                                <tr>
                                    <td>
                                        <paper-input id="form_simName" class="submit_work_form" name="simName" label="Simulation name" allowed-pattern="[a-zA-Z0-9]"></paper-input>
                                    </td>
                                    <td>
                                        <paper-input id="form_steps" class="submit_work_form" name="step" label="Number of step" allowed-pattern="[0-9]" error-message="Illegal value" onInput="_validate_params(this)"></paper-input>
                                    </td>
                                    <td>
                                        <paper-input disabled id="form_cells" class="submit_work_form" name="cells" label="Cells" allowed-pattern="[0-9]" error-message="Cell value either it exceeds available slots or it is zero!" onInput="_validate_slots(this)"></paper-input>
                                        <!--paper-dropdown-menu id="connectionType" label="Select connection" class="submit_work_form">
                                            <paper-listbox class="dropdown-content" selected="0">
                                                <paper-item label="ActiveMQ">ActiveMQ</paper-item>
                                                <paper-item label="MPI">MPI</paper-item>
                                            </paper-listbox>
                                        </paper-dropdown-menu-->
                                    </td>
                                </tr>
                                <tr>
                                    <td>
                                        <paper-input id="form_row" class="submit_work_form" name="rows" label="Rows" allowed-pattern="[0-9]" error-message="R x C value either it exceeds available slots or it is zero!" onInput="_validate_slots(this)"></paper-input>
                                    </td>
                                    <td>
                                        <paper-input id="form_col" class="submit_work_form" name="cols" label="Columns" allowed-pattern="[0-9]" error-message="R x C value either it exceeds available slots or it is zero!" onInput="_validate_slots(this)"></paper-input>
                                    </td>
                                    <td>
										<paper-input id="form_aoi" class="submit_work_form" name="aoi" label="Area of interest" allowed-pattern="[0-9]" error-message="Illegal value" onInput="_validate_params(this)"></paper-input>
									</td>
                                </tr>
                                <tr>
                                    <td>
                                      	<paper-input id="form_width" class="submit_work_form" name="width" label="Width" allowed-pattern="[0-9]" error-message="Illegal value" onInput="_validate_params(this)"></paper-input>
									</td>
                                    <td>
                                       	<paper-input id="form_height" class="submit_work_form" name="height" label="Height" allowed-pattern="[0-9]" error-message="Illegal value" onInput="_validate_params(this)"></paper-input>
                                    </td>
                                    <td>
                                       	<paper-input id="form_numAgents" class="submit_work_form" name="numAgents" label="Number of Agents" allowed-pattern="[0-9]" error-message="Illegal value" onInput="_validate_params(this)"></paper-input>
	                                </td>
                                </tr>
                                <tr>
                                	<td></td>
                                    <td colspan="2" style="text-align:right; padding-top:50px;">
                                        <paper-button raised onclick="resetForm(event)">Reset</paper-button>
										<paper-button id="submit_btn" raised onclick="submitForm()" dialog-confirm>Submit</paper-button>
										<paper-button raised dialog-dismiss autofocus>Cancel</paper-button>
                                    </td>
	                            </tr>
                            </table>
                        </form>
                    </div>
                </paper-dialog-scrollable>
            </paper-dialog>

			<%-- New EC2 node panel --%>
			<paper-dialog id="add-ec2-node-dialog" entry-animation="scale-up-animation" exit-animation="fade-out-animation" modal with-backdrop>
				<div class="layout vertical center">
					<h1>Create EC2 instance worker</h1>
				</div>
				<paper-dialog-scrollable class="horizontal-section">
					<paper-tabs selected="0">
						<paper-tab>On demand</paper-tab>
						<paper-tab>Spot</paper-tab>
					</paper-tabs>

					<iron-pages selected="{{selected}}">
						<%-- On Demand settings --%>
						<div>
							<%-- Gather instance data --%>
							<form is="iron-form" id="createEC2Worker">
								<%-- Type of instance to run --%>
								<div style="float: left; width: 300px; padding: 5px;">
									<paper-dropdown-menu id="instancetype" name="instancetype" label="Instance type" style="width: 100%;" noink>
										<paper-listbox id="ec2type" slot="dropdown-content" class="dropdown-content" attr-for-selected="item-name" selected="t2.micro">
											<paper-item item-name="t2.micro">t2.micro</paper-item>
										</paper-listbox>
									</paper-dropdown-menu>
									<paper-textarea label="Description EC2 type" rows="3" readonly></paper-textarea>
								</div>

								<div style="float: right; width: 225px; padding: 5px;">
									<%-- Number of instances to run --%>
									<paper-input id="numinstances" name="numinstances" label="Number of EC2 instances" type="number" min="1" value="1" pattern="[0-9]{1,}" auto-validate></paper-input>

									<%-- prices --%>
									<paper-textarea label="Price" readonly></paper-textarea>
								</div>

								<div style="clear: both; padding: 8px 5px;"><%-- button wrapper for margins --%>
									<div style="margin: 8px 0;">
										<p style="color: #666;">To edit EC2 region, go to <strong><a href="settings.jsp">Settings</strong></a> and change the <em>Region</em> option.</p>
									</div>
									<div style="margin: 8px 0;">
										<paper-button raised onclick="resetForm(event)">Reset</paper-button>
										<paper-button id="submit_btn" raised onclick="requestEC2Worker()" dialog-confirm>Submit</paper-button>
										<paper-button raised dialog-dismiss autofocus>Cancel</paper-button>
									</div>
								</div>
							</form>
						</div>

						<%-- Spot settings --%>
						<div>
							<pre style="color: #666;">Coming soon!</pre>
							<paper-button raised dialog-dismiss autofocus>Cancel</paper-button>						
						</div>
			        </iron-pages>
				</paper-dialog-scrollable>

				<%-- iron-page selection logic for tabs --%>
				<script>
					var pages = document.querySelector('iron-pages');
					var tabs = document.querySelector('paper-tabs');

					tabs.addEventListener('iron-select', function() { 
						pages.selected = tabs.selected;
					});
				</script>
			</paper-dialog>

			<%-- Warning toast for missing settings in forms --%>
			<paper-toast id="error_message" class="fit-bottom" duration="6000">
				<iron-icon icon="error-outline"></iron-icon>
				<span id="missing_settings">You should fill the other field(s)</span>
				<paper-button id="dismiss_toast" onclick="error_message.toggle()" noink>CLOSE</paper-button>
			</paper-toast>
		</div>

		<%-- Sliding drawer menu --%>
		<jsp:include page="fragments/drawer.jsp">
			<jsp:param name="pageSelected" value="0" />
		</jsp:include>
	</body>
</html>
