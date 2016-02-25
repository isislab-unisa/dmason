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
    if($('.grid-item-selected').length){
        $('.grid-item-selected').each(function(index){
            workerID[index] = $(this).attr("id");
            $(this).removeClass("grid-item-selected");

        });
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
        setInterval(function(){

                loadWorkers();
                if($('#load_workers_dialog').prop("opened"))close_dialog("load_workers_dialog");
                load_tiles_monitoring();
        },2000);

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
    maxRepeat = 5, animating = false;

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
    /*    var message="";
var tmp = hash(_message);
    if(history != tmp){
        history=tmp;
        message=_message;
    }else
        return;
*/

    var message=_message;
    var grid=document.getElementById("workers");
   // var tiles="<div class=\"grid-sizer-monitoring\"></div>";

    var obj =[];

    console.log(message);
    if(message.length>0)
        obj = JSON.parse(message);

    var w;
    old_list = [];
    $(grid).children('div').each(function(){
        if($(this).attr("id")){
            console.log("aggiungo "+$(this).attr("id"));
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
                node.append($("<div class=\"worker-system-info\"><span>CPU:"+w.cpuLoad+" %</span></div>"));
                node.append($("<div class=\"worker-system-info\"><span>JVM RAM:</span></div>"));
                node.append($("<div class=\"worker-system-info\"><span class=\"tab\">Free "+w.availableheapmemory+" MB</span></div>"));
                node.append($("<div class=\"worker-system-info\"><span class=\"tab\">Used "+w.busyheapmemory+" MB</span></div>"));
                node.append($("<div class=\"worker-system-info\"><span>IP: "+w.ip+"</span></div>"));
                node.append($("<div class=\"worker-system-info\"><span>Slots: "+ w.cores+"</span></div>"));

                $(grid).append(node);

            }else
                delete old_list[w.workerID];

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
            console.log("rimuovo "+id);
            $(old_list[id]).remove();
        }

   // grid.innerHTML=tiles;
   // console.log(old_list);
    load_tiles_monitoring();
}

//$(function(){ loadWorkers(); setInterval(function(){loadWorkers()},10000);});

function selectItem(element){
        if($(element).hasClass("grid-item-selected"))
            $(element).removeClass("grid-item-selected");
        else
            $(element).addClass("grid-item-selected");

}

function hash(value){
    var hash = 0;
    if (value.length == 0) return hash;
    for (i = 0; i < value.length; i++) {
        char = value.charCodeAt(i);
        hash = ((hash<<5)-hash)+char;
        hash = hash & hash; // Convert to 32bit integer
    }
    return hash;
}


function submitForm(){
    var form = document.getElementById("sendSimulationForm");
    $(form).unbind('submit').bind("submit",_OnsubmitSimulation);
    form.submit();
}

function _OnsubmitSimulation(event) {

    var form = document.getElementById("sendSimulationForm");

    var formData = new FormData(form);

    //formData don't catch (i don't know why) some elements
    var listParams = form.serialize();
    for(key in listParams) {
        console.log(key + " "+ listParams[key]);
        if(!formData.get(key))
            formData.append(key + "", listParams[key]);
    }

    $.ajax({
        url:"submitSimulation",
        type:'POST',
        data:formData,
        cache: false,
        contentType: false,
        processData: false
    });

    //remove input added previusly
    $("#workerList").remove();
    var dialog = document.getElementById("add-simulation-paper-dialog");

    resetForm(event);
    dialog.close();

}

function resetForm(event) {
    document.getElementById("sendSimulationForm").reset();
}
