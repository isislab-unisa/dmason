
function load_tiles_monitoring(){
    $('.grid-monitoring').masonry({
            itemSelector: '.grid-item-monitoring',
            columnWidth: '.grid-sizer-monitoring',
            percentPosition: true
        }
    );
}

function load_tiles_simulations(){
    $('.grid-simulations').masonry({
            itemSelector: '.grid-item-simulations',
            columnWidth: '.grid-sizer-simulations',
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

$(function(){
    $('.grid-item-monitoring').click(function(){
        if($(this).hasClass("grid-item-selected"))
            $(this).removeClass("grid-item-selected");
        else
            $(this).addClass("grid-item-selected");


        });
});

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

function show_simulation_info(element){
      $('.fullsize').show("slow");
      $('.inner-fullsize').show("slow");
}


function hide_simulation_info(){
    $('.fullsize').hide("slow");
    $('.inner-fullsize').hide("fast");
}

/*
$(document).load(setTimeout(function(){
    setting_new_simulation();
}, 2000)); */

