<%@ page language="java"
	contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8" %>

<!DOCTYPE html>

<html>
    <head>
        <jsp:include page="fragments/head-common.jsp">
			<jsp:param name="headTitle" value="DMASON - System Management"></jsp:param>
		</jsp:include>

        <%-- Import paper elements --%>
        <link rel="import" href="bower_components/paper-card/paper-card.html">
        <link rel="import" href="bower_components/paper-checkbox/paper-checkbox.html">
        <link rel="import" href="bower_components/paper-dropdown-menu/paper-dropdown-menu.html">
        <link rel="import" href="bower_components/paper-listbox/paper-listbox.html">
        <link rel="import" href="bower_components/paper-tooltip/paper-tooltip.html">
    </head>

    <body unresolved onload="load_tiles_settings()">
        <%-- Page header --%>
        <jsp:include page="fragments/header.jsp">
			<jsp:param name="page" value="settings" />
		</jsp:include>

        <%-- Page body --%>
        <div class="content content-main">
            <%-- prompt a loading cursor --%>
            <paper-dialog opened id="load_settings_dialog" entry-animation="scale-up-animation" exit-animation="fade-out-animation" modal>
                <div class="layout horizontal center">
                    <paper-spinner class="multi" active alt="Loading settings"></paper-spinner>
                    <span style="margin-left:5px;">Loading current settings...</span>
                </div>
            </paper-dialog>

            <%-- show the card for all available settings --%>
            <div class="grid-settings" id="settings">
                <%-- General settings --%>
                <paper-card heading="General" class="grid-item-settings">
                    <div class="card-image"></div>
                    <div class="card-content">
                        <paper-checkbox id="enableperftrace">Enable performance trace</paper-checkbox>
                    </div>
                    <div class="card-actions">
                        <div class="horizontal justified">
                            <paper-button id="setgeneral" raised><iron-icon icon="check"></iron-icon>&nbsp;Set</paper-button>
                        </div>
                    </div>
                </paper-card>

                <%-- Apache ActiveMQ card --%>
                <paper-card heading="Apache ActiveMQ" class="grid-item-settings">
                    <div class="card-image">
                        <img src="images/activemq-logo.png" alt="Apache ActiveMQ"></img>
                    </div>
                    <div class="card-content">
                        <paper-input id="activemqip" label="Server IP" auto-validate pattern="^(?:[0-9]{1,3}\.){3}[0-9]{1,3}$" error-message="Wrong IP format!" char-counter maxlength="15"></paper-input>
                        <paper-tooltip for="activemqip" position="bottom" animation-delay="0" offset="1">Specify an Apache ActiveMQ server IP</paper-tooltip>
                        <paper-input id="activemqport" label="Access port" auto-validate pattern="^([0-9]{1,4}|[1-5][0-9]{4}|6[0-4][0-9]{3}|65[0-4][0-9]{2}|655[0-2][0-9]|6553[0-5])$" error-message="Wrong port range!" char-counter maxlength="5"></paper-input>
                        <paper-tooltip for="activemqport" position="bottom" animation-delay="0" offset="1">Specify an access port for ActiveMQ server</paper-tooltip>
                    </div>
                    <div class="card-actions">
                        <div class="horizontal justified">
                            <paper-button id="setactivemq" raised><iron-icon icon="check"></iron-icon>&nbsp;Set</paper-button>
                        </div>
                    </div>
                </paper-card>

                <%-- Amazon AWS EC2 card --%>
                <paper-card heading="Amazon AWS EC2" class="grid-item-settings">
                    <div class="card-image">
                        <img src="images/amazonwebservices-logo.svg" alt="Amazon AWS logo"></img>
                    </div>
                    <div class="card-content">
                        <paper-tooltip for="securitygroup" position="bottom" animation-delay="0" offset="1">Security region for created instances</paper-tooltip>
                        <paper-input id="securitygroup" label="Security group"></paper-input>
                        <paper-tooltip for="curregion" position="bottom" animation-delay="0" offset="1">Current EC2 region</paper-tooltip>
                        <paper-input id="curregion" label="Current region" readonly></paper-input>
                        <paper-dropdown-menu id="region" label="Region" noink>
                            <paper-listbox id="regionlist" slot="dropdown-content" class="dropdown-content" attr-for-selected="item-name" selected={{selectedRegion}}><%-- TODO automatically populate menu --%>
                                <paper-item class="disabled" disabled>America</paper-item>
                                <paper-item item-name="us-east-1">us-east-1</paper-item><%-- N. Virginia --%>
                                <paper-item item-name="us-east-2">us-east-2</paper-item><%-- Ohio --%>
                                <paper-item item-name="us-west-1">us-west-1</paper-item><%-- N. California --%>
                                <paper-item item-name="us-west-2">us-west-2</paper-item><%-- Oregon --%>
                                <paper-item item-name="ca-central-1">ca-central-1</paper-item><%-- Canada --%>
                                <paper-item item-name="sa-east-1">sa-east-1</paper-item><%-- S&atilde;o Paulo --%>
                                <paper-item class="disabled" disabled>Europe</paper-item>
                                <paper-item item-name="eu-west-1">eu-west-1</paper-item><%-- Ireland --%>
                                <paper-item item-name="eu-central-1">eu-central-1</paper-item><%-- Frankfurt --%>
                                <paper-item item-name="eu-west-2">eu-west-2</paper-item><%-- London --%>
                                <paper-item class="disabled" disabled>Asia</paper-item>
                                <paper-item item-name="ap-northeast-1">ap-northeast-1</paper-item><%-- Tokyo --%>
                                <paper-item item-name="ap-northeast-2">ap-northeast-2</paper-item><%-- Seoul --%>
                                <paper-item item-name="ap-southeast-1">ap-southeast-1</paper-item><%-- Singapore --%>
                                <paper-item item-name="ap-southeast-2">ap-southeast-2</paper-item><%-- Sydney --%>
                                <paper-item item-name="ap-south-1">ap-south-1</paper-item><%-- Mumbai --%>
                            </paper-listbox>
                        </paper-dropdown-menu>
                        <paper-tooltip for="region" position="bottom" animation-delay="0" offset="1">Specify an EC2 region</paper-tooltip>
                        <paper-input id="pubkey" label="Public API Key" error-message="Wrong public API key!" char-counter></paper-input><%-- TODO set a pattern validation (auto-validate-pattern) --%>
                        <paper-tooltip for="pubkey" position="bottom" animation-delay="0" offset="1">Specify the public key associated to the Amazon account</paper-tooltip>
                        <paper-input id="prikey" label="Private API Key" error-message="Wrong private API key!" char-counter></paper-input><%-- TODO set a pattern validation --%>
                        <paper-tooltip for="prikey" position="bottom" animation-delay="0" offset="1">Specify the private key associated to the Amazon account</paper-tooltip>
                    </div>
                    <div class="card-actions">
                        <div class="horizontal justified">
                            <paper-button id="setamazonaws" raised><iron-icon icon="check"></iron-icon>&nbsp;Set</paper-button>
                        </div>
                    </div>
                </paper-card>

                <%-- Microsoft Azure card --%>
                <%--<paper-card heading="Microsoft Azure" class="grid-item-settings">
                    <div class="card-image">
                        <img src="images/microsoftazure.svg" alt="Microsoft Azure"></img>
                    </div>
                    <div class="card-content">
                        <p style="color: dimgray;">Not available yet.</p>
                    </div>
                    <div class="card-actions">
                        <div class="horizontal justified">
                            <paper-button raised disabled><iron-icon icon="check"></iron-icon>&nbsp;Set</paper-button>
                        </div>
                    </div>
                </paper-card>--%>
            </div>
        </div>

        <%-- Sliding drawer menu --%>
        <jsp:include page="fragments/drawer.jsp">
            <jsp:param name="pageSelected" value="3" />
        </jsp:include>
    </body>
</html>
