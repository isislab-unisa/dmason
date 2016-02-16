
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
    if($('.grid-item-selected').length){
        $('.grid-item-selected').each(function(){
           /* var numsim = parseInt($(this).text());
            numsim++;
            $(this).text(numsim);*/
            $(this).removeClass("grid-item-selected");

        });
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

var progress,repeat,maxRepeat,animating;

function opne_file_chooser(){
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


function submitHandler(event) {
    Polymer.dom(event).localTarget.parentElement.submit();
}
function resetHandler(event) {
    Polymer.dom(event).localTarget.parentElement.reset();
}

function loadWorkers(){
    $.ajax({url:"getWorkers",
        success: function(result){
            _loadWorkers(result);
        }});
}

function _loadWorkers(_message){
    var message=_message;
    var grid=document.getElementById("workers");
    var tiles="<div class=\"grid-sizer-monitoring\"></div>";

    var obj =[];

    console.log(message);
    if(message.length>0)
        obj = JSON.parse(message);
    var w;
    if(obj.hasOwnProperty('workers'))
        for (i = 0; i < obj.workers.length; i++) {
            w = obj.workers[i];
            tiles+="<div class=\"grid-item-monitoring\" onclick=\"open_dialog('worker-paper-dialog')\">"
                +"<div class=\"worker-system-info\"><span id="+w.workerID+">Worker ID: "+i+"</span></div>"
                +"<div class=\"worker-system-info\"><span>CPU:"+w.cpuLoad+" %</span></div>"
                +"<div class=\"worker-system-info\"><span>RAM: Free "+w.availableheapmemory+"  MB Used "+w.busyheapmemory+"  MB</span></div>"
                +"<div class=\"worker-system-info\"><span>IP: "+w.ip+"</span></div>"
                +"<div class=\"worker-system-info\"><span>#Simulations</span></div>"
                +"</div>";
        }
    grid.innerHTML=tiles;
}

$(function(){ loadWorkers(); setInterval(function(){loadWorkers()},3000);});

$(function(){
    $('.grid-item-monitoring').click(function(){
        if($(this).hasClass("grid-item-selected"))
            $(this).removeClass("grid-item-selected");
        else
            $(this).addClass("grid-item-selected");


    });
});
/*
$(document).load(setTimeout(function(){
    setting_new_simulation();
}, 2000)); */

