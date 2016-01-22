$(document).ready(function(){
    $('.grid').masonry({
                itemSelector: '.grid-item',
                columnWidth: '.grid-sizer',
                percentPosition: true
                }
            );
        }
    );

$(function(){
    $('.grid-item').click(function(){
        if($(this).hasClass("grid-item-selected"))
            $(this).removeClass("grid-item-selected");
        else
            $(this).addClass("grid-item-selected");


        });
});

function open_dialog_setting_new_simulation(){
    if($('.grid-item-selected').length){
        $('.grid-item-selected').each(function(){
            var numsim = parseInt($(this).text());
            numsim++;
            $(this).text(numsim);
            $(this).removeClass("grid-item-selected");

        });
    }else{
        document.querySelector('#miss-worker-selection').open();
        return;
    }

    setting_new_simulation();

}
function setting_new_simulation(){


        var dialog = document.getElementById("animated-paper-dialog");
        if (dialog) {
            dialog.open();
        }
    }

var progress;
var repeat, maxRepeat = 5, animating = false;

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
    progress.value = progress.min;
    progress = document.querySelector('paper-progress');
    if (!animating) {
        nextProgress();
    }
}
