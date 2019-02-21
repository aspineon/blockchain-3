<script type="text/javascript">

$(document).ready(function(){

var html = `
 <div class="uuidPicker">
 <span class="pickName">
    <ul>
        <li class="hidden"></li>
        {{#idLookup}}
        <li><a data-id="{{first}}" href="#" class="button blueButton">#{{second}}</a></li>
        {{/idLookup}}
    </ul>
 </span>&nbsp;
 <a href="#" class="new button redButton">new</a>&nbsp;
 <span class="inputName hidden"><label>Name</label><input class="name" type="text"/></span><a href="#" class="save button redButton">tag</a>
 </div>
`;


// add extra feaures based on type
$("input.UniqueIdentifier").after(html); 

// create new uniqueidentifier
$("div.formfield  a.new").click(function(){ 
   var $parent =  $(this).parent().parent();
   $.ajax({url: "/web/uniqueidentifier/create", success: function(result){
           $parent.find("input").first().val(result);         
        }});                    
});


// save the value
$("div.formfield  a.save").click(function(){ 
   var $parent =  $(this).parent().parent();
   if ($parent.find(".inputName").hasClass("hidden")) {
       $parent.find(".inputName").removeClass("hidden");
   }
   else {
     // do some ajax
     var id = $parent.find("input").first().val();
     var name = $parent.find("input.name").first().val();
     
     var theUrl = "/web/uniqueidentifier/save?id=" + id + "&name=" + name;
    
     $.ajax({url: theUrl, success: function(result){
          //      alert(result);        
             }});
             
     // update view
     $parent.find(".inputName").addClass("hidden");
     $parent.find("li").last().after('<li><a data-id="' + id + '" href="#" class="button blueButton">' + name + '</a></li>');
     
   }
});


// pick an existing uniqueidentifier
$("div.formfield span.pickName a").click(function(){ 
  var id = $(this).data("id")
  
  //  :-( 
  var $parent =  $(this).parent().parent().parent().parent().parent();   
 
  $parent.find("input").val(id);        
});

});
</script>

<style>
.hidden {
  display:none;
}
span.pickName ul {
    display: inline;
    list-style-type: none;
    margin: 0;
    padding: 0;
}
              
span.pickName li {
  display: inline;               
}      

div.formfield label {
    width:18%; 
    max-width:18%;
    display:inline-block;
    text-align: right;
    padding-right:3px;
}   

div.formfield input.UniqueIdentifier {
    width:32%; 
    max-width:32%;
}

form.runFlow {
   background : inherit;
}

div.uuidPicker {
   display:inline;
   float:right;
   width: 48%;
}

div.uuidPicker a.redButton {
   width : 70px;
}

div.formButtons {
   padding-right:150px;
   clear:both;
}

</style>


# {{networkName}}

You are talking to **{{nodeName}}**. Explore [{{nodeName}}](/web/networks/{{networkName}}/nodes/{{nodeName}}). Switch [node](/web/networks/{{networkName}}/nodes)

Run flow {{flowName}} 

{{#hasDescription}}
<div>{{description}}</div>
{{/hasDescription}}


<form class="runFlow" action="run" method="POST">

{{#metadata}}
<div class="formfield"><label for="{{key}}">{{key}}:</label><input name="{{key}}" class="{{#value}}{{type}}{{/value}}"></div>
{{/metadata}}

<div class="formButtons"><input type="submit" value="Run Flow"></input></div>

</form>

