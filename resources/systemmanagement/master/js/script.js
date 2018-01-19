/**
 * JavaScript checker
 */
$(document).ready(function () {
    $("#noJS").hide();
    $("#container").show();
    $("#noJS").remove();
});

function load_tiles_monitoring() {
    $('.grid-monitoring').masonry({
            itemSelector: '.grid-item-monitoring',
            columnWidth: 265
        }
    );
}

function load_tiles_settings() {
    $('.grid-settings').masonry({
            itemSelector: '.grid-item-settings',
            columnWidth: 315
        }
    );
}

function load_tiles_history() {
    $('.grid-settings').masonry({
            itemSelector: '.grid-item-history',
            columnWidth: '.grid-sizer-history',
            percentPosition: true
        }
    );
}

function open_dialog_setting_new_simulation() {
    var workerIDs = [];
    var num_slots = 0;
    var num_workers = $('.grid-item-selected').length;
    var id = "";

    if (num_workers) {
        $('.grid-item-selected').each(function (index) {
            id = $(this).attr("id");
            //console.log(id);

            workerIDs[index] = id;
            $(this).removeClass("grid-item-selected");
            // untoggle the toggle as well
            var toggle = $(this).find(".toggle")[0]; // extract toggle from element
            toggle.checked = false;

            slot = $("#w-slots-" + id).text();
            slot = slot.substring(slot.indexOf(":") + 1, slot.length);
            num_slots += parseInt(slot.trim());
        });

        $("#head_sel_works").text(num_workers);
        $("#head_num_slots").text(num_slots);

        // passing worker selected
        var node = document.createElement("input");
        node.setAttribute("id", "workerList");
        node.setAttribute("name", "workers");
        node.setAttribute("value", workerIDs);
        node.style.display = "none";
        $("#sendSimulationForm").append(node);
    } else {
        document.querySelector('#miss-worker-selection').open();
        return;
    }

    //resetForm();
    open_dialog_by_ID("add-simulation-paper-dialog");
}

function open_dialog_by_ID(id_paper_dialog) {
    var dialog = document.getElementById(id_paper_dialog);
    if (dialog) {
        dialog.open();
    }
}

function close_dialog_by_ID(id_paper_dialog) {
    var dialog = document.getElementById(id_paper_dialog);
    if (dialog) {
        dialog.close();
    }
}

var dynDelay = 750; // default delay 0.75s
function loadWorkersDynamicInterval() {
    var slider = document.querySelector("#update-speed");
    setTimeout(
        function () {
            // retrieve existing workers data
            loadWorkers();

            // close loader
            if ($('#load_workers_dialog').prop("opened")) {
                close_dialog_by_ID("load_workers_dialog");
            }

            // use Masonry on workers grid
            load_tiles_monitoring();

            // retrieve update speed value
            dynDelay = slider.value;
            if (!dynDelay) {
                // set 0.75s interval while slider object loads
                dynDelay = 750;
            }

            // this last call lets setTimeout() with variable
            // to act like a setInterval() with variable delay
            loadWorkersDynamicInterval();
        },
        dynDelay
    );
}

$(function () {
    //console.log(window.location.pathname)
    if (window.location.pathname == "/" || window.location.pathname == "/index.jsp") {
        // load active workers details
        loadWorkersDynamicInterval();

        // update worker stats when
        // new workers spawn or die
        setTimeout(
            function () {
                updateWorkerStats();
            },
            4000
        );

        //loadJarsList();
    } else if (window.location.pathname == "/simulations.jsp") {
        setTimeout(
            function () {
                setInterval(
                    function () {
                        update_simulation_info();
                    },
                    1000
                );
            },
            5000
        );
    } else if (window.location.pathname == "/history.jsp") {
        setTimeout(
            function () {
                setInterval(
                    function () {
                        if ($('#load_history_dialog').prop("opened")) {
                            close_dialog_by_ID("load_history_dialog");
                        }

                        update_history_info();
                        load_tiles_history();
                    },
                    1000
                );
            },
            5000
        );
    } else if (window.location.pathname == "/settings.jsp") {
        setTimeout(
            function () {
                loadSettings();

                if ($("#load_settings_dialog").prop("opened")) {
                    close_dialog_by_ID("load_settings_dialog");
                }
            },
            4000
        );
    }
});

/**
 * This variable is associated to the submit simulation form
 * and is the same for its different events like loading a
 * simulation from external jar or submitting form.
 */
var simProgress;

function open_file_chooser() {
    // prompt the file chooser dialog
    // by clicking the 'Browse' button
    // of hidden input field
    $('#simulation-jar-chooser').click();

    //console.log("retrieving paper-progress and buttons...");
    simProgress = document.getElementById("simulation-progress");
    var simButton = document.getElementById("simulation-jar-chooser-button");
    
    // attach event for paper-progress
    $("#simulation-jar-chooser").change(function () {
        //console.log("starting the loader...");
        startProgress(simProgress, simButton);
    });

    // make the ajax request to send the JAR file
    var jarFile = document.getElementById("simulation-jar-chooser");
    $.post(
        "", // destination point
        undefined // JAR file
    ); // TODO chain success and error functions
}

function _sendJar() {

}

/* paper-progress global variables */
var globalProgress, globalButton;
var repeat, maxRepeat = 10, animating = false, tempButton;

function nextProgress() {
    //console.info("received button from startProgress: " + button);
    animating = true;

    if (globalProgress.value < globalProgress.max) {
        globalProgress.value += (globalProgress.step || 1);
    } else {
        if (++repeat >= maxRepeat) {
            animating = false;
            globalButton.disabled = false;
            return;
        }

        globalProgress.value = globalProgress.min;
    }

    requestAnimationFrame(nextProgress);
}

function startProgress(progress, button) {
    // assign parameters to global variables
    globalProgress = progress;
    globalButton = button;
    //console.log("starting progress...");
    repeat = 0;

    globalProgress.value = globalProgress.min;
    globalButton.disabled = true; // disable submit button during progress
    if (!animating) {
        //console.info("passing button to nextProgress: " + button);
        nextProgress();
    }
}

function loadWorkers() {
    $.ajax({
        url: "getWorkers",
        success: function (result) {
            _loadWorkers(result);
        }
    });
}

//var history = "";
function _loadWorkers(_message) {
    var message = _message;
    var grid = $("#workers");

    // parse workers from JSON message
    //console.log(message);
    var obj = [];
    if (message.length > 0) {
        obj = JSON.parse(message);
    }

    // collect existing workers cards in page
    var old_list = [];
    var k = 0;
    $(grid).children("paper-card").each(function () {
        var nodeId = $(this).attr("id");
        if (nodeId && nodeId != "workers-stats") { // add existing worker card into old_list
            old_list["\'w-" + nodeId + "\'"] = $(this);
//        } else if (nodeId == "workers-stats") {
//            console.info("skip #workers-stats");
        }
    });

    // check whether the received object has an array of workers
    var w;
    if (obj.hasOwnProperty("workers")) {
        // add a new card for new workers
        // or update existing ones with new data
        for (i = 0; i < obj.workers.length; i++) {
            w = obj.workers[i];
            var curNode = document.getElementById(w.workerID);
            if (!curNode && !old_list["\'w-" + w.workerID + "\'"]) { // there is no node associated to worker ID
                // prepare worker data for template injection
                var workerData = {
                    workerID: w.workerID,
                    workerCPU: w.cpuLoad,
                    workerMaxMB: w.maxHeap,
                    workerFreeMB: w.availableheapmemory,
                    workerUsedMB: w.busyheapmemory,
                    workerIP: w.ip,
                    workerSlots: w.slots
                }

                // retrieve and populate worker template
                var html;
                // this call must be synchronous or else callbacks
                // containing the same node will cumulate and insert
                // it several times
                $.ajax({
                    url: "../fragments/worker.html",
                    async: false, // DO NOT MAKE ASYNC
                    success: function (value) {
                        var workerTemplate = $.templates(value);
                        html = workerTemplate.render(workerData);

                        // inject populated worker template
                        $(grid).append(html);
                    }
                });

            } else { // update existing worker node
                // console.log("Updating worker " + w.workerID + "...");
                delete old_list["\'w-" + w.workerID + "\'"];

                $("#w-cpu-" + w.workerID).children(".worker-data").text(w.cpuLoad);
                $("#w-max-heap-" + w.workerID).children(".worker-data").text(w.maxHeap);
                $("#w-heap-available-" + w.workerID).children(".worker-data").text(w.availableheapmemory);
                $("#w-heap-use-" + w.workerID).children(".worker-data").text(w.busyheapmemory);
                $("#w-slots-" + w.workerID).children(".worker-data").text(w.slots);
            } // end if ... else ...
        } // end for
    } // end if

    // remove old existing nodes from grid
    if (Object.keys(old_list).length > 0) {
        for (var i = 0; i < old_list.length; i++) {
            $(old_list[i]).remove();
        }
    }

    // grid.innerHTML = tiles;
    load_tiles_monitoring();
    updateWorkerStats();
}

function selectAllWorkers() {
    var grid = document.getElementById("workers");
    $(grid).children("paper-card").each(function () { // workers are paper-card elements
        if ($(this).hasClass("grid-item-monitoring")) {
            //console.log($(this).attr("id"));
            selectItem($(this));
        }
    });

    updateWorkerStats();
}

function selectItem(element) {
    // if the element has got the 'grid-item-selected' class already
    // it gets removed, otherwise it gets added
    $(element).toggleClass("grid-item-selected");

    // toggle the worker switch as well
    var toggle = $(element).find(".toggle")[0]; // extract toggle from element

    if ($(element).hasClass("grid-item-selected")) {
        // toggle
        toggle.checked = true;
    } else {
        // untoggle
        toggle.checked = false;
    }

    updateWorkerStats();
}

function change_partitioning_input_params(element) {
    var buttonName = $(element).attr("name");
    //console.log(buttonName);

    switch (buttonName) {
        case "uniform":
            $("#form_cells").attr("disabled", true);
            $("#form_row").attr("disabled", false);
            $("#form_col").attr("disabled", false);
            $("#form_dep").attr("disabled", true);
            break;

        case "non-uniform":
            $("#form_cells").attr("disabled", false);
            $("#form_row").attr("disabled", true);
            $("#form_col").attr("disabled", true);
            $("#form_dep").attr("disabled", true);
            break;

        case "three-dim":
            $("#form_cells").attr("disabled", true);
            $("#form_row").attr("disabled", false);
            $("#form_col").attr("disabled", false);
            $("#form_dep").attr("disabled", false);
            break;
    }
}

function _validate_params(element) {
    var current_element = $(element);
    var paper_input_container = current_element.children()[0];
    var id = current_element.attr("id");
    var value = document.querySelector("#" + id).value;
    var submit_btn = document.querySelector("#submit_btn");
    if (value) {
        value = parseInt(value);
    }
    if (value === 0) {
        paper_input_container.invalid = true;
        submit_btn.disabled = true;
        return false;
    }

    paper_input_container.invalid = false;
    submit_btn.disabled = false;
}

function _validate_slots(element) {
    var current_element = $(element);
    //console.log("ci sono!");
    var  slots = ($("#head_num_slots").text()).trim();
    if (slots) {
        slots = parseInt(slots);
    }
    //console.log("Available slots "+slots);

    var row_element = $("#form_row");
    var cols_element = $("#form_col");
    var dep_element = $("#form_dep");
    var cells_element = $("#form_cells");

    var paper_input_container = current_element.children()[0];

    var row = document.querySelector("#" + row_element.attr("id")).value;
    var cols = document.querySelector("#" + cols_element.attr("id")).value;
    var depth = document.querySelector("#" + dep_element.attr("id")).value;
    var cells = document.querySelector("#" + cells_element.attr("id")).value;

    var id = current_element.attr("id");

    var value = document.querySelector("#" + id).value;
    var cur_slot = (value) ? parseInt(value) : 1;
    //console.log("Id element " + id + " input value " + value + " cur_slot " + cur_slot);

    var submit_btn = document.querySelector("#submit_btn");

    if (cur_slot > slots || cur_slot == 0) {
        paper_input_container.invalid = true;

        submit_btn.disabled = true;
        return false;
    } else {
        switch (id) {
            case "form_row":
                if (cols) {
                    var int_val = parseInt(cols);
                    cur_slot *= int_val;
                    if (cur_slot > slots || cur_slot == 0) {
                        paper_input_container.invalid = true;
                        submit_btn.disabled = true;
                        return false;
                    }
                }
                break;
            case "form_cells":
                if (cells) {
                    var int_val = parseInt(cells);
                    if (int_val > slots || cur_slot == 0) {
                        paper_input_container.invalid = true;
                        submit_btn.disabled = true;
                        return false;
                    }
                }
                break;
            case "form_col":
                if (row) {
                    var int_val = parseInt(row);
                    cur_slot *= int_val;
                    if (cur_slot > slots || cur_slot == 0) {
                        paper_input_container.invalid = true;
                        submit_btn.disabled = true;
                        return false;
                    }
                }
                break;
            case "form_dep":
                if (depth) {
                    var int_val = parseInt(depth);
                    cur_slot *= int_val;
                    if (cur_slot > slots || cur_slot == 0) {
                        paper_input_container.invalid = true;
                        submit_btn.disabled = true;
                        return false;
                    }
                }
                break;
        }
        paper_input_container.invalid = false;
        submit_btn.disabled = false;
    }
}

function submitJarFile() {
    var form = document.getElementById("sendSimulationJar");
    var jarFile = $("#simulation-jar-chooser").val();

    if (!jarFile) {
        $(error_toast_message).text("You should select an example simulation or submit a simulation jar first.");
        error_toast.open();
        return false;
    }

    // get the submit button and
    // start the progress animation
    submitSimulationButton = document.getElementById("simulation-jar-chooser-button");
    if (!simProgress) {
        simProgress = document.getElementById("simulation-progress");
    }
    startProgress(simProgress, submitSimulationButton);

    $(form).unbind("submit").bind("submit", _OnsubmitSimulation);
    form.submit();
}

function submitForm() {
    var form = document.getElementById("sendSimulationForm");
    if (!checkForm(form)) {
        return;
    }

    // get the submit button and
    // start the progress animation
    submitSimulationButton = document.getElementById("submit_btn");
    if (!simProgress) {
        simProgress = document.getElementById("simulation-progress");
    }
    startProgress(simProgress, submitSimulationButton);

    $(form).unbind('submit').bind("submit", _OnsubmitSimulation); // TODO finalize _onSubmitJar() function
    form.submit();
}

function _onSubmitJar(event) {
    var form = document.getElementById("sendSimulationJar");
    var jarFile; // extract JAR file from form

    var request = $.ajax({
        url: "", // TODO define backend JAR management
        type: "POST",
        data: {
            formType: "jar",
            jar: jarFile
        },
        success: function(result) {
            // TODO show some confirmation message
        },
        error: function (xhr, status, error) {
            // TODO show error notification
        }
    });
}

function checkForm(form) {
    var error_toast = document.querySelector('#error_message');
    var error_toast_message = document.querySelector("#missing_settings");
    var jarFile = $("#simulation-jar-chooser").val();
    var exampleSim = document.querySelector("#exampleSimulation").selectedItemLabel;
    var partitioning = document.querySelector("#partitioning").selected;

    if (!partitioning) {
        $(error_toast_message).text("You should select a partitioning.");
        error_toast.open();
        return false;
    }

    if (!jarFile && !exampleSim) {
        $(error_toast_message).text("You should select an example simulation or submit a simulation jar.");
        error_toast.open();
        return false;
    }

    var success = true;
    $("#sendSimulationForm paper-input").each(function (n, paper_input) {
        if (paper_input.id.startsWith("form_")) {
            if (paper_input.value == "") {
                switch (paper_input.label.toLowerCase()) {
                    case "cells":
                        if (partitioning.toLowerCase() == 'non-uniform') {
                            $(error_toast_message).html(
                                "You should fill the <strong>" + paper_input.label + "</strong> field."
                            );
                            error_toast.open();
                            success = false;
                        }
                        break;

                    case "rows":
                        if (partitioning.toLowerCase() == 'uniform') {
                            $(error_toast_message).html(
                                "You should fill the <strong>" + paper_input.label + "</strong> field."
                            );
                            error_toast.open();
                            success = false;
                        }
                        break;

                    case "columns":
                        if (partitioning.toLowerCase() == 'uniform') {
                            $(error_toast_message).html(
                                "You should fill the <strong>" + paper_input.label + "</strong> field."
                            );
                            error_toast.open();
                            success = false;
                        }
                        break;

                    case "depth":
                    if (partitioning.toLowerCase() == 'three-dim') {
                        $(error_toast_message).html(
                            "You should fill the <strong>" + paper_input.label + "</strong> field."
                        );
                        error_toast.open();
                        success = false;
                    }
                    break;

                    default:
                        $(error_toast_message).html(
                            "You should fill the <strong>" + paper_input.label + "</strong> field."
                        );
                        error_toast.open();
                        success = false;
                }

                return;
            }
        }
    });

    return success;
}

function _OnsubmitSimulation(event) {
    var form = document.getElementById('sendSimulationForm');
    var formData = new FormData(form);

    // Workaround by https://github.com/rnicholus/ajax-form/issues/63

    var partitioning = document.getElementById('partitioning');
    if (!partitioning.selected) {
        formData.append(partitioning.id, "");
    } else {
        formData.append(partitioning.id, partitioning.selected);
    }

    var exampleSimulation = document.getElementById('exampleSimulation');
    if (!exampleSimulation.selectedItem) {
        formData.append(exampleSimulation.id, "");
    } else {
        formData.append(exampleSimulation.id, exampleSimulation.selectedItemLabel);
    }

    var connectionType = document.querySelector('#connectionType');
    if (connectionType.checked) {
        formData.append(connectionType.id, "mpi");
    }

    var request = $.ajax({
        url: "submitSimulation",
        type: 'POST',
        data: formData,
        cache: false,
        contentType: false,
        processData: false,
        success: function (result) {
            // remove input tag added previously
            var dialog = document.getElementById("add-simulation-paper-dialog");
            maxRepeat = 0;
            $("#workerList").remove();
            resetForm();
            dialog.close();
            window.location = "simulations.jsp";
        },
        error: function (xhr, status, error) {
            var error_toast = document.getElementById("error_message");
            var error_toast_message = document.getElementById("missing_settings");

            $(error_toast_message).text("Error while sending the simulation request!");
            error_toast.open();
        }
    });
}

function resetForm() {
    document.querySelector("#sendSimulationForm").reset();
    var input_file = $("#simulation-jar-chooser");
    input_file.replaceWith(input_file.val('').clone(true));
    progress = document.querySelector('paper-progress');
    progress.style.display = "none";
    document.querySelector("#submit_btn").disabled = false;
}

function update_simulation_info() {
    $.ajax({
        url: "simulationList",
        success: function (result) {
            _update_sim_info(result);
        }
    });
}

function _update_sim_info(_message) {
    var scp = document.querySelector('template[is="dom-bind"]');
    var message = _message;
    var obj = [];
    //console.log(message);

    if (message.length > 0) {
        obj = JSON.parse(message);
    }
    if (obj.hasOwnProperty('simulations')) {
        scp.$.list_simulations.listItem = obj.simulations;
    } else {
        scp.$.list_simulations.listItem = obj;
    }
}

function getListFile(sim_id) {
    $.ajax({
        url: "requestForLog",
        data: {id: sim_id},
        success: function (result) {
            _getListFile(result);
        }
    });
}

function _getListFile(result) {
    if (!result) {
        return;
    }
    var list_file = JSON.parse(result);
    if (!list_file.hasOwnProperty("files")) {
        return;
    }

    var scp = document.querySelector('template[is="dom-bind"]');
    var list = [];
    for (var f, i = 0; f = list_file.files[i]; i++) {
            //console.log(f);
            list[i] = f;
    }
    close_dialog_by_ID("load_sim_log_file");
    var scp = document.querySelector('template[is="dom-bind"]');
    scp.$.fullsize_card.listFile = list;

    scp.$.pages.selected = 1;
}

function update_history_info() {
    $.ajax({
        url: "getHistoryFolderList",
        success: function (result) {
            get_history_info(result);
            close_dialog_by_ID("load_history_dialog");
        }
    });
}

function get_history_info(result) {
    if (!result) {
        return;
    }
    var list_sim = JSON.parse(result);
    if (!list_sim.hasOwnProperty("history")) {
        return;
    }

    var scp = document.querySelector("#sim_history_grid");
    var list = [];
    for (var f, i = 0; f = list_sim.history[i]; i++) {
        //console.log(f);
        list[i] = f;
    }
    scp.listSimHistory = list;
}

function shutdown() {
    var workerIDs = [];
    var num_slots = 0;
    var num_workers = $('.grid-item-selected').length;
    var id = "";
    var workers = '{ "list": [';

    // check whether workers have been selected
    if (num_workers) {
        $('.grid-item-selected').each(function (index) {
            id = $(this).attr("id");
            //console.log("Shutting worker " + id + " down...");

            workerIDs[index] = id;
            workers += '{"id": "' + id + '"},';
        });
        //console.log(workerIDs);
        workers = workers.substring(0, workers.length - 1);
        workers += ']}';
    } else {
        document.querySelector('#miss-worker-shutdown').open();
        return;
    }

    // show a loader about the shutdown
    open_dialog_by_ID('shutdown_workers_dialog');

    // remote request for workers shutdown
    $.ajax({
        url: "shutdownWorkers",
        data: {
            topics: JSON.stringify(workers)
        },
        success: function (result) {
            // remove worker cards from grid
            for (var i = 0; i < workerIDs.length; i++) {
//                console.log("Removing worker " + workerIDs[i] + " from grid...");
                $("#" + workerIDs[i]).remove();
//                console.log("Worker " + workerIDs[i] + " removed!");
            }

            updateWorkerStats()
            close_dialog_by_ID('shutdown_workers_dialog');
        }
    });
} // end shutdown()

/**
 * Delete history for selected simulation
 * #miss-history-delete'
 */
function cleanSelectedHistory() {
    var pathList = [];
    var simToDelete = $('.grid-item-history-selected').length;
    var path = "";
    var jsonPaths = '{ "paths":[';
    var scope = document.querySelector('sim-history-grid[id="sim_history_grid"]');

    if (simToDelete) {
        $('.grid-item-history-selected').each(function (index) {
            var myid = $(this).attr("id");

            scope.listSimHistory.forEach(function (arrayItem) {
                var ifd = arrayItem.simID;
                if (ifd == myid) {
                    path = arrayItem.simLogZipFile;
                }
            });
            path = path.substring(0, path.lastIndexOf("/"));
            pathList[index] = path;
            jsonPaths += '{"path":"' + path + '"},';
        });
        jsonPaths = jsonPaths.substring(0, jsonPaths.length - 1);
        jsonPaths += ']}';
        console.log(jsonPaths);
    } else {
        document.querySelector('#miss-history-delete').open();
        return;
    }

    open_dialog_by_ID('load_history_dialog');

    $.ajax({
        url: "cleanSelectedHistory",
        data: {
            paths:JSON.stringify(jsonPaths)
        },
        success: function (result) {
            close_dialog_by_ID('load_history_dialog');
        }
    });
}

/**
 * Delete all history files on file system
 */
function cleanHistory() {
    open_dialog_by_ID("load_history_dialog");

    $.ajax({
        url: "cleanHistory",
        success: function (result) {
            close_dialog_by_ID("load_history_dialog");
            location.reload();
        }
    });
}

// attach settings update logic to settings.jsp paper cards
$().ready(function () {
//    $(loadSettings); // done in a previous function
    $("#setgeneral").click(updateGeneralSettings);
    $("#setactivemq").click(updateActiveMQSettings);
    $("#setamazonaws").click(updateAmazonAWSSettings);
});

function loadSettings() {
    $.post(
        "getSettings",
        function (data) {
            if (data == null) {
                console.warn("No setting has been retrieved!");
                return;
            //} else {
            //    console.log("Settings successfully retrieved!")
            }

            // ectract data
            var performanceTrace = data.generalSettings.enablePerfTrace;
            //console.log("Performance trace: " + performanceTrace); // TODO comment after testing

            var activeMQIp = data.activeMQSettings.ip;
            var activeMQPort = data.activeMQSettings.port;
            //console.log("ActiveMQ location: " + activeMQIp + ":" + activeMQPort);

            var amazonAWSPriKey = data.amazonAWSSettings.priKey;
            var amazonAWSPubKey = data.amazonAWSSettings.pubKey;
            var amazonAWSRegion = data.amazonAWSSettings.region;

            // show general settings
            performanceTrace = performanceTrace.toLowerCase();
            var perfTraceCheckbox = document.querySelector("#enableperftrace");
            if (performanceTrace == "true") {
                perfTraceCheckbox.checked = true;
            } else if (performanceTrace == "false") {
                perfTraceCheckbox.checked = false;
            } else {
                console.error("Illegal value for performance trace setting!");
            }

            // show ActiveMQ settings
            $("#activemqip").val(activeMQIp);
            $("#activemqport").val(activeMQPort);

            // show Amazon AWS settings
            $("#curregion").val(amazonAWSRegion); // as today, paper-dropdown-menu is unsettable
            $("#pubkey").val(amazonAWSPubKey);
            $("#prikey").val(amazonAWSPriKey);
        }
    )
    .fail(function () {
        console.error("Error while retrieving current settings!");
    });
}

function updateGeneralSettings() {
    var perfTrace = document.querySelector("#enableperftrace").checked;
    console.log("Enable performance trace: " + perfTrace);

    // send POST request to server
    $.post(
        "updateSettings",
        {
            "setting": "general",
            "enableperftrace": perfTrace
        }
    )
    .fail(function () {
        console.error("Error while sending general data!")
    });
}

function updateActiveMQSettings() {
    var ip = $("#activemqip").val();
    var port = $("#activemqport").val();
    //console.log("ActiveMQ IP:port " + ip + ":" + port);

    if (ip == "" || port == "") {
        console.warn("Please provide IP and port of ActiveMQ server!");
        return;
    }

    // send POST request to server
    $.post(
        "updateSettings",
        {
            "setting": "activemq",
            "activemqip": ip,
            "activemqport": port
        }
    )
    .fail(function () {
        console.error("Error while sending Active MQ data!");
    });
}

function updateAmazonAWSSettings() {
    var region = $("#region").val();
    var pubkey = $("#pubkey").val();
    var prikey = $("#prikey").val();
    console.log("Region: " + region);

    // check parameters emptiness
    if (region == "" || pubkey == "" || prikey == "") {
        console.warn("Please provide region, public key and private key for Amazon AWS!");
        return;
    }

    // send POST request to server
    $.post(
        "updateSettings",
        {
            "setting": "amazonaws",
            "region": region,
            "pubkey": pubkey,
            "prikey": prikey
        }
    )
    .fail(function () {
        console.error("Error while sending Amazon AWS data!");
    });
}

function updateWorkerStats() {
    var tot_workers = $(".grid-item-monitoring").length;
    var num_workers = $(".grid-item-selected").length;
    var num_slots = 0;
    var id = "";

    //console.log("Selected " + num_workers + " of " + tot_workers + " workers!");

    if (num_workers) {
        // test code for values insertion
        $('.grid-item-selected').each(function (index) {
            id = $(this).attr("id");
            slot = $("#w-slots-" + id).text();
            slot = slot.substring(slot.indexOf(":") + 1, slot.length);
            num_slots += parseInt(slot.trim());
        });
        
        //console.log("Selected " + num_slots + " total slots!");
    }

    // update fields in index.jsp
    $("#availableworkers").find("span").text(tot_workers); // automatically updates the first value in array
    $("#selectedworkers").find("span").text(num_workers);
    $("#selectedslots").find("span").text(num_slots);
}

function validateEC2WorkerRequest() {
    var error_toast = document.getElementById("error_message");
    var error_toast_message = document.getElementById("missing_settings");

    var ec2Type = $("#instancetype").val();
    var ec2TypeDescription = "";
    var numInstances = $("#numinstances").val();
    var price = 0.0;

    if (ec2Type == null || ec2Type == "") {
        $(error_toast_message).text(
            "You should select a EC2 instance type."
        );
        error_toast.open();
        return false;
    }
    if (numInstances == null || numInstances == 0) {
        $(error_toast_message).text(
            "You should specify a number of EC2 instances to run."
        );
        error_toast.open();
        return false;
    }

    //console.log("Request for " + numInstances + " EC2 instance of " + ec2Type + " type");
    return true;
}

function requestEC2Worker() {
    var form = $("#createEC2Worker");

    if (!validateEC2WorkerRequest()) {
        console.warn("New EC2 worker request is invalid or incomplete!");
        return;
    }

    // activating the loader
    var ec2Progress; // TODO assign values if upper ones work
    var ec2Button;
    startProgress(ec2Progress, ec2Button); // TODO check if it works as expected

    $(form).unbind("submit").bind("submit", _onSubmitEC2WorkerRequest);
    form.submit();
}

function _onSubmitEC2WorkerRequest(event) {
    var instanceType = $("#instancetype").val();
    var numInstances = $("#numinstances").val();

    // send the request to instantiation servlet
    $.post(
        "instantiateEC2Workers",
        {
            "instancetype": instanceType,
            "numinstances": numInstances
        } 
    )
    .done(function (data, textStatus, jqXHR) {
            //console.info(data + "\nstatus: " + textStatus + ".")
            resetEC2RequestForm();
            document.getElementById("add-ec2-node-dialog").close();
    })
    .fail(function (jqXHR, textStatus, errorThrown) {
            console.error(textStatus + ": " + errorThrown + ".");
    });
}

function resetEC2RequestForm() {
    document.querySelector("#createEC2Worker").reset();
    progress = document.querySelector('paper-progress');
    progress.style.display = "none";
}
