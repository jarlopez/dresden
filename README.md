# dresden
Operation-based conflict-free replicated data type (CRDT) implementations in Scala, using the Kompics framework.

## Overview
This repository contains Kompics components for [broadcasting abstractions](/src/main/scala-2.11/dresden/components/broadcast).

You'll also find a collection of CRDT implementations for graphs and sets, and an incomplete JSON CRDT implementation.

### Grow-Only Set
Trivial set CRDT which can only be added to. Operations are broadcast to participating nodes, which then update their local versions to be eventually consistent with the "true" set.

### TwoP Set
Set that supports element addition and removal. Once removed, however, that element can never be added again.

### Observed-Remove Set
Set implementation that supports element addition and removal by relying on a causal order broadcast for operation propagation.

### TwoP-TwoP Graph
Graph CRDT exposing operations for adding and removing vertices and edges, as well as querying for existing vertices and edges. The implementation keeps track of both additions and removals in order to guarantee eventual consistency across users.
