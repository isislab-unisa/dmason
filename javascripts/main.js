//$(function(){window.location="under_construction/pacman-master/index.html"});

$( function() {
        var drawer = document.querySelector("paper-drawer-panel");

        drawer.addEventListener("onChange",
            function (e) {
                $("#drawer_popup_bookmark").show();
                drawer.togglePanel();
                console.log("panel");
            });
/*
        drawer.addEventListener("iron-select", function (e) {
            $("#drawer_popup_bookmark").hide();
            drawer.openDrawer();
            console.log("select");
        });*/
    }
);


function open_drawer(){

    document.querySelector("paper-drawer-panel").openDrawer();
}