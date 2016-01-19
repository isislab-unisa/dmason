<html>
<head><title>First JSP</title></head>
<body>
<!-- Polyfill Web Components for older browsers -->
<script src="bower_components/webcomponentsjs/webcomponents-lite.min.js"></script>

<!-- Import element -->
<link rel="import" href="bower_components/iron-icons/iron-icons.html">
<link rel="import" href="bower_components/paper-icon-button/paper-icon-button.html">
<link rel="import" href="bower_components/paper-toolbar/paper-toolbar.html">
<link rel="import" href="bower_components/paper-drawer-panel/paper-drawer-panel.html">
<link rel="import" href="bower_components/paper-scroll-header-panel/paper-scroll-header-panel.html">
<link rel="import" href="bower_components/iron-flex-layout/iron-flex-layout.html">
<link rel="import" href="bower_components/paper-menu/paper-menu.html">
<link rel="import" href="bower_components/paper-item/paper-item.html">

 <style is="custom-style">
    paper-toolbar + paper-toolbar {
      margin-top: 20px;
    }
    paper-toolbar.red {
      --paper-toolbar-background: red;
    }
    .spacer {
      @apply(--layout-flex);
    }
    paper-scroll-header-panel {
 		 height: 100%;
	}
  </style>
<body unresolved>

 <paper-drawer-panel force-narrow>

    <div drawer>
        <app-sidebar drawer>
	        <paper-menu>
				<paper-item onclick="">1</paper-item>
				<paper-item onclick="">1</paper-item>
				             
				<paper-item onclick="">1</paper-item>
				             
				<paper-item onclick="">1</paper-item>
				             
				<paper-item onclick="">1</paper-item>
	
			</paper-menu>
        </app-sidebar>
    </div>

    <div main class="fullbleed layout vertical">

        <paper-toolbar>
            <paper-icon-button paper-drawer-toggle icon="menu" on-tap="menuAction"></paper-icon-button>
            
            <div class="clearfix">
               MASTER
            </div>

        </paper-toolbar>

        <paper-scroll-header-panel class="flex" fixed>
           <div class ="content">
		  <%
		    double num = Math.random();
		    if (num > 0.75) {
		  %>
		      <h2>You'll have a luck day!</h2><p>(<%= num %>)</p>
		  <%
		    } else {
		  %>
		      <h2>Well, life goes on ... </h2><p>(<%= num %>)</p>
		  <%
		    }
		  %>
  		<a href="<%= request.getRequestURI() %>"><h3>Try Again</h3></a></div>
        </paper-scroll-header-panel>
    </div>

</paper-drawer-panel>


</body>
</html>
