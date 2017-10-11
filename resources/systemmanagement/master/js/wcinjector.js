/**
 * Global variables
 */
var library;

/**
 * Browser detector
 */
function browserDetection() {
    // look for 'Firefox' in userAgent property of navigator implicit object
    var browser = navigator.userAgent.search("Firefox");
    library = "bower_components/webcomponentsjs/webcomponents-lite.js";
    //console.log("Default webcomponentsjs library is " + library);
    if (browser >= 0) {
        // define the Firefox-compatible webcomponentsjs library
        library = "bower_components/webcomponentsjs/0.7.24/webcomponents-lite.js";
        //console.info("Firefox (" + browser + ") has been detected as browser!");
        if (library == undefined) {
            library = "https://cdn.rawgit.com/webcomponents/webcomponentsjs/v0.7.24/webcomponents-lite.js"
            //console.warn("Remote webcomponentsjs library has been set.");
        }
    }
    //console.info("The library to inject is " + library);
}

/**
 * Library injector (jQuery)
 */
$(document).ready(function() {
    browserDetection(); // set injectable library according to detected browser
    //console.log("Library chosen by detector: " + library);
    var libraryNode = $("<script></script>"); // create 'script' node
    libraryNode.attr("src", library); // set the URI in library as 'src' attribute of 'script' node
    $("head").append(libraryNode); // append libraryNode in body
});
