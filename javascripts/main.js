function toggle_drawer(){
  document.querySelector("paper-drawer-panel").togglePanel();
}


function switch_content(element){
  var id = $(element).attr('href');

  if(!id.startsWith("http")) {
      $(".content_resized").children().each(function (i) {
          $(this).hide();
      });

      document.getElementById(id).style.display = "block";
  }else{
      window.open(id,"_blank");
  }
  toggle_drawer();
}