        <meta name="viewport" content="width=device-width, minimum-scale=1.0, initial-scale=1.0, user-scalable=yes">

        <title>${param.headTitle}</title>
        <link rel="shortcut icon" type="image/png" href="images/dmason-ico.png"/>

        <%-- Polyfill Web Components for older browsers --%>
        <script src="bower_components/webcomponentsjs/webcomponents-lite.min.js"></script>

        <%-- Deprecated --%>
        <link rel="import" href="bower_components/neon-animation/web-animations.html">
        <link rel="import" href="bower_components/neon-animation/animations/scale-up-animation.html">

        <%-- jQuery --%>
        <script src="bower_components/jquery/dist/jquery.min.js"></script>

        <%-- jsRender --%>
		<script src="bower_components/jsrender/jsrender.min.js"></script>

        <%-- Common imports --%>
        <jsp:include page="../fragments/iron-common.jsp"></jsp:include>
		<jsp:include page="../fragments/app-layout.jsp"></jsp:include>
		<jsp:include page="../fragments/paper-common.jsp"></jsp:include>

        <%-- Masonry lib --%>
        <script src="bower_components/masonry/dist/masonry.pkgd.min.js"></script>

        <%-- Custom Polymer CSS --%>
        <link rel="import" href="style/polymer/styles-polymer.html">

        <%-- Custom CSS --%>
        <link rel="stylesheet" type="text/css" href="style/custom-style.css">

        <%-- Custom Scripts --%>
        <script src="js/script.js"></script>
