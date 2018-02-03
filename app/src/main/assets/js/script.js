'use strict';

function test() {
var element = $(document).find(".sound__infoContent");


                $(element).find("h2").unbind();
                if($(element).find("#save").length != 0){

                  }else{


                          var links = window.location.href;

                          var $buttons = $("<input/>").attr({ type: "button", name:"btn1", value:"Download", id:"save",class:"downloadBtn", onclick:"BtnLogin.performClick('" + links + "');"});
                          var $space = $("<br />");
                          $(element).find("h2").append($space);
                          $(element).find("h2").append($buttons);
                  }

setTimeout(test, 2000);

}



