function open_drawer(){
  document.querySelector("paper-drawer-panel").togglePanel();
}


function switch_content(element){
  var id = $(element).attr('href');
 var a = $(id);
  console.log(a);
  $(".content_resized").children().each(function(i){
    $(this).hide();
  });
  console.log($(""+id));
  $(""+id).css("display","block");
}