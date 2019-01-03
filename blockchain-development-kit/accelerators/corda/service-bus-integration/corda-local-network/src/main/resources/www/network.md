
# Network {{name}}

## Nodes

{{#nodes}}
* [{{organisation}}](/web/networks/{{name}}/nodes/{{organisation}}/status)
{{/nodes}}

## CorDapps

{{#cordapps}}
* <strong>{{name}}</strong> (deployed at {{deployedAt}} with length {{size}} bytes and MD5 hash of {{md5Hash}})
{{/cordapps}}


## Actions

* [Status](/web/networks/{{name}}/status)
* [Start All Nodes](/web/networks/{{name}}/start)
* [Stop All Nodes](/web/networks/{{name}}/stop)
* [Deploy a CorDapp](/web/networks/{{name}}/deploy)
* [Task History](/web/networks/{{name}}/tasks/history)

