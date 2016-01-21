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

    $('#add-simulation-to-worker-buttom').click(
         function(){
             $('.grid-item-selected').each(function(){
                 var numsim = parseInt($(this).text());
                 numsim++;
                 $(this).text(numsim);
                 $(this).removeClass("grid-item-selected");

             });
         }
    );
});