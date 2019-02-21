<script type="text/javascript">

$(document).ready(function(){


// pick an existing uniqueidentifier
$("div.formfield  li a").click(function(){ 
  var id = $(this).data("id")
  
  //  :-( 
  var $parent =  $(this).parent().parent().parent().parent();   
 
  $parent.find("input").first().val(id);        
});

// save the value
$("div.formfield > a.save").click(function(){ 
   var $parent =  $(this).parent();
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

});
</script>



# [{{networkName}}](/web/networks/{{networkName}}/nodes)

## {{stateName}} 

Explore data on node **{{nodeName}}**. Pick one of the query options below.

<form action="/web/networks/{{networkName}}/nodes/{{nodeName}}/apps/{{appName}}/states/{{stateName}}/all" 
      method="GET"
      class="query">
      <div class="formButtons"><input type="submit" value="Query All"></input></div>

</form>

<form action="/web/networks/{{networkName}}/nodes/{{nodeName}}/apps/{{appName}}/states/{{stateName}}/query" 
      method="GET"
      class="query">

<div class="formfield"><label for="id" >Linear Id</label>: <input name="id" class="">
  <ul>
   {{#idLookup}}
        <li><a data-id="{{first}}" href="#" class="button blueButton">#{{second}}</a></li>
   {{/idLookup}}
  </ul>    
  </span>&nbsp;
   <span class="inputName hidden"><label>Name</label><input class="name" type="text"/></span><a href="#" class="save button redButton">tag</a>
  
</div>
<div class="formButtons"><input type="submit" value="Query ID"></input></form>

</form>

<style>

div.formfield ul {
    display: inline;
    list-style-type: none;
    margin: 0;
    padding: 0;
}
              
div.formfield li {
  display: inline;               
}      

form {
  backgound-color : #fafafa;
}


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

form.query {
   background : inherit;
}

div.formButtons {
  width : 150px
  padding-left :10px;
  clear:both;
}

</style>
