# 'simple-marketplace' CorDapp

## Overview

This is a CorDapp implementation of the Azure Blockchain Workbench
 [simple-marketplace](https://cegekait.visualstudio.com/_git/corda-azure-workbench?path=%2Fblockchain-development-kit%2Faccelerators%2Fcorda%2Fcordapps%2Fsimple-marketplace&version=GBdevelop),
 example
 
## Building locally 

Other project reference this CorDapp in test cases. To update them with changes 

```bash
./buildLocal.sh
```

You will also need to check <code>lib/simple-marketplace.jar</code> 
into Git  
 
## Quick start 

```bash
./gradlew clean deployNodes
build/nodes/runnodes
```

This may take a few minutes In total 3 processes will be started one for each node.

This will create the following nodes and a Notary. 

* Owner  localhost:10009
* Buyer  localhost:10005
* Notary localhost:10002 


From command line you can start the Market flow by typing the command:
flow start MarketFlow -and all the parameters required by the constructor of the MarketFlow-
Example:
flow start MarketFlow item: {description: ITEM01, price: 750}, offeredPrice: 801, otherParty: Buyer

To see that the other Nodes receive the flow, type in one of the node's shell flow watch.

