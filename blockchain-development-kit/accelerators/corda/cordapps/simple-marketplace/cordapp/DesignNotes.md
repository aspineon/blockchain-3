# Design Notes

This is a basic porting of the Ethereum Solidity contract into a CorDapp. By design, it 
is as similar to the original as possible. But as many Blockchain
concepts are implemented differently in Corda, there is some re-architecting needed for a
production ready implementation.

## Organisation vs. Individual Identity

The example uses `Party` to identify users, but `Party` this is really tied to organisation. This will 
probably need to be refactored to use an identity tied to an Active Directory user, with the 
higher level Party reserved for Organisation to Organisation rules (e.g _Northwind_ only see _Contoso_ 
shipments when responsibility is transferred).

## Single State

All state is contained within a single `State` object, "AcceptedState", which matches the design of the Solidity 
contract. This 'State' contains:
* both of the Party involve in the transaction (owner,buyer)
* An available Item that is the object of the transaction
* An offeredPrice


https://docs.corda.net/head/design/reference-states/design.html

## Over signing of transactions 

All of the Party, owner and buyer,must sign the transaction for it to be valid.
only after that the object will be saved in the vault of each Party.


   