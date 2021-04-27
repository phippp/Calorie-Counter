function updateTable(){
    var search = $( "#search-input" ).val().toLowerCase();
    var rows = $( "table > tbody > tr");
    for(var i = 0; i < rows.length; i++){
        var thisRow = rows.eq(i);

        var question = thisRow.find( "td" ).eq(1).html().replace("<span class=\"highlighter\">","").replace("</span>","");
        var answer = thisRow.find( "td" ).eq(2).html().replace("<span class=\"highlighter\">","").replace("</span>","");

        var answerIndex = answer.toLowerCase().indexOf(search);
        var questionIndex = question.toLowerCase().indexOf(search);

        if( (answerIndex >= 0 || questionIndex >= 0) && search.length > 0){
            //match and length > 0
            thisRow.css("display","table-row");
            //add highlighter span
            if(answerIndex >= 0){
                var temp = "";
                //formats string correctly
                if(answerIndex == 0){
                    temp = "<span class='highlighter'>" + answer.slice(0, search.length) + "</span>" + answer.slice(search.length);
                } else {
                     temp = answer.slice(0,answer.indexOf(search)) + "<span class='highlighter'>" + answer.slice(answer.indexOf(search),answer.indexOf(search) + search.length) + "</span>" + answer.slice(answer.indexOf(search) + search.length);
                }
                //updates html
                thisRow.find( "td" ).eq(2).html(temp);
            } else {
                thisRow.find( "td" ).eq(2).html(answer);
            }
            if(questionIndex >= 0){
                var temp = "";
                //formats string correctly
                if(questionIndex == 0){
                    temp = "<span class='highlighter'>" + question.slice(0, search.length) + "</span>" + question.slice(search.length);
                } else {
                    temp = question.slice(0,question.indexOf(search)) + "<span class='highlighter'>" + question.slice(question.indexOf(search),question.indexOf(search) + search.length) + "</span>" + question.slice(question.indexOf(search) + search.length);
                }
                //updates html
                thisRow.find( "td" ).eq(1).html(temp);
            } else {
                thisRow.find( "td" ).eq(1).html(question);
            }
        }else if(search.length == 0){
            //Remove highlighter when null
            thisRow.css("display","table-row");
            thisRow.find( "td" ).eq(1).html(question);
            thisRow.find( "td" ).eq(2).html(answer);
        }else{
            //no match and length > 0
            thisRow.css("display","none");
        }
    }
}