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

    open_dialog("add-simulation-paper-dialog");

}

function open_dialog(id_paper_dialog){


        var dialog = document.getElementById(id_paper_dialog);
        if (dialog) {
            dialog.open();
        }
    }

function close_dialog(id_paper_dialog){


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
                    if($('#load_workers_dialog').prop("opened"))close_dialog("load_workers_dialog");
                    load_tiles_monitoring();
            },1000);
        else
            if(window.location.pathname=="/simulations.html")

                setTimeout(function() {
                    setInterval(function(){
                        update_simulation_info();
                        },1000);
                    }, 5000);


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
            $("#form_cells").css("display","none");
            $("#form_row").css("display","block");
            $("#form_col").css("display","block");
            break;
        case "non-uniform":
            $("#form_cells").css("display","block");
            $("#form_row").css("display","none");
            $("#form_col").css("display","none");
            break;
    }

}

function _validate(element){
    current_element = $(element);
    //console.log("ci sono!");
    slots = ($("#head_num_slots").text()).trim();
    if(slots)
        slots = parseInt(slots);
    //console.log("Available slots "+slots);

    row_element   = $("#form_row");
    cols_element  = $("#form_col");
    cells_element = $("#form_cells");

    row = document.querySelector("#"+row_element.attr("id")).value;
    cols = document.querySelector("#"+cols_element.attr("id")).value;
    cells = document.querySelector("#"+cells_element.attr("id")).value;


    id = current_element.attr("id");

    value = document.querySelector("#"+id).value;
    cur_slot = (value)?parseInt(value):1;
    console.log("Id element "+id+" input value "+value+" cur_slot "+cur_slot);


    if(cur_slot > slots){
        current_element.attr("invalid","true");
        $("#submit_btn").attr("disabled","true");
        return false;
    }else{
        if(current_element.attr("invalid") == "true")
            //current_element.attr("invalid","false");
            current_element.children()[0].updateAddons({invalid: false});
    }

    //if(row * cols) > slots then invalid =true

    switch (id){
        case "form_row":
            if(cols){
                int_val = parseInt(cols);
                cur_slot *=int_val;
                if(cur_slot > slots){
                    current_element.attr("invalid","true");
                    $("#submit_btn").attr("disabled","true");
                    return false;
                }
            }
            if( $("#submit_btn").attr("disabled")=="true")
                $("#submit_btn").attr("disabled","false");

            break;
        case "form_cells":
            break;
        case "form_col":
            break;
    }


}


function submitForm(){
/*
    if(!_validate())
        return;*/
    startProgress();
    var form = document.getElementById("sendSimulationForm");
    $(form).unbind('submit').bind("submit",_OnsubmitSimulation);
    form.submit();
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

    var myDropDownMenuConnection = document.getElementById('connectionType');
    if (!myDropDownMenuConnection.selectedItem) {
        formData.append(myDropDownMenuConnection.id, "");
    } else {
        formData.append(myDropDownMenuConnection.id, myDropDownMenuConnection.selectedItemLabel);
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
            window.location="simulations.html";
        }
    });
}

function resetForm(event) {
    progress = document.querySelector('paper-progress');
    progress.style.display = "none";
    $("#submit_btn").attr("disabled",false);
    document.getElementById("sendSimulationForm").reset();
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
    }

}
