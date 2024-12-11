$(document).ready(function (){
  function setVh() {
    let vh = window.innerHeight * 0.01;
    document.documentElement.style.setProperty('--vh', `${vh}px`);
  }

  window.addEventListener('resize', setVh);
  window.addEventListener('orientationchange', setVh);

  setVh();

	$("#main .cnt6 .tab_split .tab_btn li a").on("click", function () {
    var tgIdx=$(this).parent().index();
    $(this).parent().addClass("on").siblings().removeClass("on");
    $(this).closest(".tab_split").find(">.tab_cnt>div").eq(tgIdx).show().siblings().hide();

    return false;
  });
});