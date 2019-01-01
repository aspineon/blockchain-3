<script type="text/javascript">

$(document).ready(function(){
$("#123").click(function(){
    //$(this).hide();
    alert("Jquery is working");
  });
});


(function poll() {
console.log("running poll");
    setTimeout(function() {
        $.ajax({
            url: "/web/ajax/test",
            type: "GET",
            success: function(data) {
            console.log(data);
                console.log("polling");
            },
            //dataType: "json",
            complete: poll,
            timeout: 2000
        })
    }, 5000);
})();

</script>



# Status {{name}}

{{#status}}
## Nodes {{node}}
socketTest {{socketTest}}</br>
sshConnectionTest {{sshConnectionTest}}</br>

{{/status}}

<span id="123">click me</span>