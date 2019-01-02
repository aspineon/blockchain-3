
<script type="text/javascript">
function myFunction() {
  /* Get the text field */
  var copyText = document.getElementById("sshcommand");

  /* Select the text field */
  copyText.select();

  /* Copy the text inside the text field */
  document.execCommand("copy");

  /* Alert the copied text */
  //alert("Copied the text: " + copyText.value);
}

</script>


# Node Status 

{{#status}}
### Node: <strong>{{name}}</strong>
socketTest {{socketTest}}</br>
sshConnectionTest {{sshConnectionTest}}</br>
rpcPort {{rpcPort}}</br>
sshPort {{sshPort}}</br>
legalName {{legalName}} </br>
{{/status}}

To connect via SSH, run the command below. The password is 'test'
<div>
<input type="text" 
        id="sshcommand" 
        style="width:450px"
        value="ssh -p {{#status}}{{sshPort}}{{/status}} localhost -l user1 -o UserKnownHostsFile=/dev/null">

<button onclick="myFunction()">Copy</button>
</div>
 