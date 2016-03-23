function load_tiles_monitoring(){
    $('.grid-monitoring').masonry({
            itemSelector: '.grid-item-monitoring',
            columnWidth: '.grid-sizer-monitoring',
            percentPosition: true
        }
    );
}

function load_tiles_settings(){
    $('.grid-settings').masonry({
            itemSelector: '.grid-item-settings',
            columnWidth: '.grid-sizer-settings',
            percentPosition: true
        }
    );
}

function load_tiles_history(){
    $('.grid-settings').masonry({
            itemSelector: '.grid-item-history',
            columnWidth: '.grid-sizer-history',
            percentPosition: true
        }
    );
}



function open_dialog_setting_new_simulation(){
    var workerID= new Array();
    var num_slots = 0;
    var num_workers = $('.grid-item-selected').length;
    var id="";
    if(num_workers){
        $('.grid-item-selected').each(function(index){
            id =  $(this).attr("id");
            //console.log(id);

            workerID[index] = id;
            $(this).removeClass("grid-item-selected");
            slot = $("#w-slots-"+id).text();
            slot = slot.substring(slot.indexOf(":")+1,slot.length);
            num_slots += parseInt(slot.trim());
        });

        $("#head_sel_works").text(num_workers);
        $("#head_num_slots").text(num_slots);
        //passing worker selected
        var node = document.createElement("input");
        node.setAttribute("id","workerList");
        node.setAttribute("name","workers");
        node.setAttribute("value",workerID);
        node.style.display = "none";
        $("#sendSimulationForm").append(node);
    }else{
        document.querySelector('#miss-worker-selection').open();
        return;
    }

    open_dialog_by_ID("add-simulation-paper-dialog");

}

function open_dialog_by_ID(id_paper_dialog){


    var dialog = document.getElementById(id_paper_dialog);
    if (dialog) {
        dialog.open();
    }
}

function close_dialog_by_ID(id_paper_dialog){


    var dialog = document.getElementById(id_paper_dialog);
    if (dialog) {
        dialog.close();
    }
}

$(
    function(){
        //console.log(window.location.pathname)
        if(window.location.pathname=="/" || window.location.pathname=="/index.jsp")
            setInterval(function(){

                loadWorkers();
                if($('#load_workers_dialog').prop("opened"))close_dialog_by_ID("load_workers_dialog");
                load_tiles_monitoring();
            },1000);
        else
        if(window.location.pathname=="/simulations.jsp") {

            setTimeout(function () {
                setInterval(function () {
                    update_simulation_info();
                }, 1000);
            }, 5000);
        }else if(window.location.pathname=="/history.jsp") {

            setTimeout(function () {
                setInterval(function () {
                    update_history_info();
                }, 1000);
            }, 5000);
        }

    }
);

var progress,repeat,maxRepeat,animating;

function open_file_chooser(){
    $('#simulation-jar-chooser').click();
}

function nextProgress() {
    animating = true;
    if (progress.value < progress.max) {
        progress.value += (progress.step || 1);
    } else {
        if (++repeat >= maxRepeat) {
            animating = false;
            return;
        }
        progress.value = progress.min;
    }
    requestAnimationFrame(nextProgress);
}
function startProgress() {
    repeat = 0;
    maxRepeat = 20, animating = false;

    progress = document.querySelector('paper-progress');
    progress.value = progress.min;
    if (!animating) {
        progress.style.display="block";
        nextProgress();
    }
}

function loadWorkers(){
    $.ajax({url:"getWorkers",
        success: function(result){
            _loadWorkers(result);
        }});
}
//var history="";
function _loadWorkers(_message){

    var message=_message;
    var grid=document.getElementById("workers");
    // var tiles="<div class=\"grid-sizer-monitoring\"></div>";

    var obj =[];

    //console.log(message);
    if(message.length>0)
        obj = JSON.parse(message);

    var w;
    old_list = [];
    $(grid).children('div').each(function(){
        if($(this).attr("id")){
            // console.log("aggiungo "+$(this).attr("id"));
            old_list[$(this).attr("id")] = $(this);
        }
    });

    if(obj.hasOwnProperty('workers')){

        for (i = 0; i < obj.workers.length; i++) {
            w = obj.workers[i];
            var curNode = document.getElementById(w.workerID);
            if(!curNode){
                node = $("<div id="+w.workerID+" class=\"grid-item-monitoring\" onclick=\"selectItem(this)\"></div>");
                // node.append($("<div class=\"worker-system-info\"><span>Worker ID: "+ w.workerID+"</span></div>"));
                node.append($("<div class=\"worker-system-info\"><span id=\"w-cpu-"+w.workerID+"\">CPU:"+w.cpuLoad+" %</span></div>"));
                node.append($("<div class=\"worker-system-info\"><span>Heap:</span></div>"));
                node.append($("<div class=\"worker-system-info\"><span id=\"w-max-heap-"+w.workerID+"\" class=\"tab\">Max "+w.maxHeap+" MB</span></div>"));
                node.append($("<div class=\"worker-system-info\"><span id=\"w-heap-avaiable-"+w.workerID+"\" class=\"tab\">Free "+w.availableheapmemory+" MB</span></div>"));
                node.append($("<div class=\"worker-system-info\"><span id=\"w-heap-used-"+w.workerID+"\" class=\"tab\">Used "+w.busyheapmemory+" MB</span></div>"));
                node.append($("<div class=\"worker-system-info\"><span id=\"w-ip-"+w.workerID+"\">IP: "+w.ip+"</span></div>"));
                node.append($("<div class=\"worker-system-info\"><span id=\"w-slots-"+w.workerID+"\">Slots: "+ w.slots+"</span></div>"));

                $(grid).append(node);

            }else{
                delete old_list[w.workerID];
                $("#w-cpu-"+w.workerID).text("CPU:"+w.cpuLoad+" %");
                $("w-max-heap-"+w.workerID).text("Max "+w.maxHeap+" MB</span></div>");
                $("#w-heap-avaiable-"+w.workerID).text("Free "+w.availableheapmemory+" MB");
                $("#w-heap-use-"+w.workerID).text("Used "+w.busyheapmemory+" MB");
                $("#w-slots-"+w.workerID).text("Slots: "+ w.slots);
            }

            /*
             tiles+="<div id="+w.workerID+" class=\"grid-item-monitoring\" onclick=\"selectItem(this)\">"
             +"<div class=\"worker-system-info\"><span>Worker ID: "+i+"</span></div>"
             +"<div class=\"worker-system-info\"><span>CPU:"+w.cpuLoad+" %</span></div>"
             +"<div class=\"worker-system-info\"><span>JVM RAM: Free "+w.availableheapmemory+"  MB Used "+w.busyheapmemory+"  MB</span></div>"
             +"<div class=\"worker-system-info\"><span>IP: "+w.ip+"</span></div>"
             +"<div class=\"worker-system-info\"><span>#Simulations</span></div>"
             +"</div>";*/
        }
    }
    if(old_list.length > 0)
        for(id in old_list){

            $(old_list[id]).remove();
        }

    // grid.innerHTML=tiles;
    load_tiles_monitoring();
}


function selectItem(element){
    if($(element).hasClass("grid-item-selected"))
        $(element).removeClass("grid-item-selected");
    else
        $(element).addClass("grid-item-selected");

}


function change_partitioning_input_params(element){
    var buttonName =$(element).attr("name");
    //console.log(buttonName);
    switch (buttonName){
        case "uniform":
            $("#form_cells").attr("disabled",true);
            $("#form_row").attr("disabled",false);
            $("#form_col").attr("disabled",false);
            break;
        case "non-uniform":
            $("#form_cells").attr("disabled",false);
            $("#form_row").attr("disabled",true);
            $("#form_col").attr("disabled",true);
            break;
    }

}
function _validate_params(element){
    var current_element = $(element);
    var paper_input_container = current_element.children()[0];
    var id = current_element.attr("id");
    var value = document.querySelector("#"+id).value;
    var submit_btn = document.querySelector("#submit_btn");
    if(value)
        value = parseInt(value);

    if(value===0){
        paper_input_container.invalid = true;
        submit_btn.disabled = true;
        return false;
    }

    paper_input_container.invalid =false;
    submit_btn.disabled = false;



}

function _validate_slots(element){
    var current_element = $(element);
    //console.log("ci sono!");
    var  slots = ($("#head_num_slots").text()).trim();
    if(slots)
        slots = parseInt(slots);
    //console.log("Available slots "+slots);

    var row_element   = $("#form_row");
    var cols_element  = $("#form_col");
    var cells_element = $("#form_cells");

    var paper_input_container = current_element.children()[0];

    var row = document.querySelector("#"+row_element.attr("id")).value;
    var cols = document.querySelector("#"+cols_element.attr("id")).value;
    var cells = document.querySelector("#"+cells_element.attr("id")).value;


    var id = current_element.attr("id");

    var value = document.querySelector("#"+id).value;
    var cur_slot = (value)?parseInt(value):1;
    console.log("Id element "+id+" input value "+value+" cur_slot "+cur_slot);

    var submit_btn = document.querySelector("#submit_btn");

    if(cur_slot > slots || cur_slot==0){

        paper_input_container.invalid = true;

        submit_btn.disabled = true;
        return false;
    }
    else{

        switch (id){
            case "form_row":
                if(cols){
                    var int_val = parseInt(cols);
                    cur_slot *=int_val;
                    if(cur_slot > slots || cur_slot==0){
                        paper_input_container.invalid =true;
                        submit_btn.disabled = true;
                        return false;
                    }
                }
                break;
            case "form_cells":
                if(cells){
                    var int_val=parseInt(cells);
                    if(int_val > slots || cur_slot==0){
                        paper_input_container.invalid =true;
                        submit_btn.disabled = true;
                        return false;
                    }
                }
                break;
            case "form_col":
                if(row){
                    var int_val = parseInt(row);
                    cur_slot *=int_val;
                    if(cur_slot > slots || cur_slot==0){
                        paper_input_container.invalid =true;
                        submit_btn.disabled = true;
                        return false;
                    }
                }
                break;
        }
        paper_input_container.invalid =false;
        submit_btn.disabled = false;

    }

}

function abortSubmit(){
    undo_feature = true;
    var undo_toast = document.querySelector("#undo_submit_sim");
    undo_toast.close();
}

undo_feature=false;
function submitForm(){

    var form = document.getElementById("sendSimulationForm");
    if(!checkForm(form)){
        return;
    }

    startProgress();
    //var undo_toast = document.querySelector("#undo_submit_sim");
    //undo_toast.open();

    if(!undo_feature){
        $(form).unbind('submit').bind("submit",_OnsubmitSimulation);
        form.submit();
    }
}

function checkForm(form){

    var error_toast_message = document.querySelector("#missing_settings");
    var jarFile = $("#simulation-jar-chooser").val();
    var exampleSim = document.querySelector("#exampleSimulation").selectedItemLabel;
    var partitioning = document.querySelector("#partitioning").selected;

    if(!partitioning){
        $(error_toast_message).text("You should select a partitioning");
        error_toast_message.open();
        return false;
    }
    if(!jarFile && !exampleSim){
        $(error_toast_message).text("You should select an example simulation or submit a simulation jar");
        error_toast_message.open();
        return false;
    }
    var success = true;
    $("#sendSimulationForm paper-input").each(function(n,paper_input){
        if(paper_input.id.startsWith("form_")){
            if(paper_input.value==""){

                switch (paper_input.label.toLowerCase()){
                    case "cells":
                        if (partitioning.toLowerCase() == 'non-uniform') {
                            $(error_toast_message).text("You should fill " + paper_input.label);
                            error_toast_message.open();
                            success=false;
                        }
                        break;
                    case "rows":
                        if (partitioning.toLowerCase() == 'uniform') {
                            $(error_toast_message).text("You should fill " + paper_input.label);
                            error_toast_message.open();
                            success=false;
                        }
                        break;
                    case "columns":
                        if (partitioning.toLowerCase() == 'uniform') {
                            $(error_toast_message).text("You should fill " + paper_input.label);
                            error_toast_message.open();
                            success=false;
                        }
                        break;
                    default:
                        $(error_toast_message).text("You should fill " + paper_input.label);
                        error_toast_message.open();
                        success=false;
                }


                return;

            }
        }

    });
    return success;
}

function _OnsubmitSimulation(event) {

    var form = document.querySelector('form[is="iron-form"]');

    var formData = new FormData(form);

    //Workaround by https://github.com/rnicholus/ajax-form/issues/63

    var myPaperRadioGroup = document.getElementById('partitioning');
    if (!myPaperRadioGroup.selected) {
        formData.append(myPaperRadioGroup.id, "");
    } else {
        formData.append(myPaperRadioGroup.id, myPaperRadioGroup.selected);
    }


    var myDropDownMenuSampleSim = document.getElementById('exampleSimulation');
    if (!myDropDownMenuSampleSim.selectedItem) {
        formData.append(myDropDownMenuSampleSim.id, "");
    } else {
        formData.append(myDropDownMenuSampleSim.id, myDropDownMenuSampleSim.selectedItemLabel);
    }

    var myCheckBoxConnection = document.querySelector('#connectionType');
    if (myCheckBoxConnection.checked) {
        formData.append(myCheckBoxConnection.id, "mpi");
    }


    $.ajax({
        url:"submitSimulation",
        type:'POST',
        data:formData,
        cache: false,
        contentType: false,
        processData: false,
        success: function(result){
            //remove input tag added previusly
            $("#workerList").remove();
            var dialog = document.getElementById("add-simulation-paper-dialog");
            maxRepeat =0;
            resetForm(event);
            dialog.close();
            window.location="simulations.jsp";
        }
    });
}

function resetForm(event) {
    progress = document.querySelector('paper-progress');
    progress.style.display = "none";
    document.querySelector("#submit_btn").disabled = false;
    document.querySelector("#sendSimulationForm").reset();
}


function update_simulation_info(){
    $.ajax({
        url:"simulationList",
        success: function(result){
            _update_sim_info(result);
        }
    });
}

function _update_sim_info(_message){
    var scp = document.querySelector('template[is="dom-bind"]');
    var message=_message;
    var obj =[];
    //console.log(message);
    if(message.length>0)
        obj = JSON.parse(message);

    if(obj.hasOwnProperty('simulations')){
        scp.$.list_simulations.listItem = obj.simulations;
    }else{
        scp.$.list_simulations.listItem = obj;
    }

}


function getListFile(sim_id){
    $.ajax({
        url:"requestForLog",
        data:{id:sim_id},
        success: function(result){
            _getListFile(result);

        }
    });
}

function _getListFile(result){
    if(!result) return;
    var list_file = JSON.parse(result);
    if(!list_file.hasOwnProperty("files")) return;

    var scp = document.querySelector('template[is="dom-bind"]');
    var list =[];
    for(var f, i=0; f = list_file.files[i]; i++){
            //console.log(f);
            list[i] = f;
    }
    close_dialog_by_ID("load_sim_log_file");
    var scp = document.querySelector('template[is="dom-bind"]');
    scp.$.fullsize_card.listFile =list;

    scp.$.pages.selected = 1;

}


function update_history_info(){
    $.ajax({
        url:"getHistoryFolderList",
        success: function(result){
            get_history_info(result);

        }
    });
}

function get_history_info(result){
    if(!result) return;
    var list_sim = JSON.parse(result);
    if(!list_sim.hasOwnProperty("history")) return;

    var scp = document.querySelector("#sim_history_grid");
    var list =[];
    for(var f, i=0; f = list_sim.history[i]; i++){
        //console.log(f);
        list[i] = f;
    }
    scp.listSimHistory = list;
}