1. INSERT (Creation & Upserts)
A. Standard Create
Command: CREATE (p:Product {id: 'P-100', name: 'USB-C Hub', price: 50});

Explanation: Creates a new node with the label Product. If you run this twice, you will have two identical nodes with the same ID.

Verification: MATCH (p:Product {id: 'P-100'}) RETURN p; (Shows the created node).

B. Merge (The Upsert)
Command: ```cypher MERGE (s:Supplier {name: 'TechLogistics'}) ON CREATE SET s.joinedDate = date(), s.status = 'New' ON MATCH SET s.lastSeen = date();

* **Explanation:** Searches for a `Supplier` named 'TechLogistics'. If it doesn't exist, it creates it (`ON CREATE`). If it does exist, it updates the `lastSeen` property (`ON MATCH`).
* **Verification:** `MATCH (s:Supplier {name: 'TechLogistics'}) RETURN s;`

### C. Create with Relationship
**Command:** ```cypher
MATCH (p:Product {id: 'P-100'}), (s:Supplier {name: 'TechLogistics'})
CREATE (s)-[:SHIPS {cost: 5}]->(p);
Explanation: Finds two existing nodes and draws a directed arrow (SHIPS) from the Supplier to the Product with a property cost.

Verification: MATCH (s:Supplier)-[r:SHIPS]->(p:Product) RETURN s, r, p;

2. UPDATE (Modifying Data)
A. Update/Add Property
Command: MATCH (p:Product {id: 'P-100'}) SET p.price = 45, p.stock = 20;

Explanation: Locates the specific product and updates the price while adding a new stock property.

Verification: MATCH (p:Product {id: 'P-100'}) RETURN p.name, p.price, p.stock;

B. Bulk Update/Add (+=)
Command: MATCH (p:Product {id: 'P-100'}) SET p += {discount: 0.1, clearance: true};

Explanation: The += operator merges the new map into the existing properties without deleting the old ones (like name or price).

Verification: MATCH (p:Product {id: 'P-100'}) RETURN p;

C. Remove Property & Update Label
Command: ```cypher MATCH (p:Product {id: 'P-100'}) REMOVE p.clearance; MATCH (s:Supplier {name: 'TechLogistics'}) SET s:International;

* **Explanation:** `REMOVE` deletes the property entirely (it becomes null). `SET s:Label` adds a secondary label to the node.
* **Verification:** `MATCH (n) WHERE n:International RETURN n;`

---

## 3. DELETE (Relationship & Node Removal)

### A. Delete Relationship by Property
**Command:** `MATCH ()-[r:SHIPS]->() WHERE r.cost > 10 DELETE r;`
* **Explanation:** Finds all shipping links where the cost is over 10 and deletes the **link only**. The nodes remain safe.
* **Verification:** `MATCH ()-[r:SHIPS]->() RETURN count(r);` (Should be lower than before).

### B. Detach Delete (Node + Connections)
**Command:** `MATCH (p:Product {id: 'P-100'}) DETACH DELETE p;`
* **Explanation:** Deletes the product node and automatically removes any relationships (arrows) attached to it.
* **Verification:** `MATCH (p:Product {id: 'P-100'}) RETURN count(p);` (Returns 0).

### C. Finding and Deleting "Dangling" Nodes
**Command:** `MATCH (n) WHERE NOT (n)--() DELETE n;`
* **Explanation:** Finds "Islands"—nodes that have zero relationships—and deletes them to clean up the graph.
* **Verification:** `MATCH (n) RETURN count(n);`


---

## 4. BATCH OPERATIONS (High Volume)

### A. Batch Insert (Unwind)
**Command:** ```cypher
UNWIND $data AS row 
CALL { WITH row MERGE (p:Product {id: row.id}) SET p.name = row.name } IN TRANSACTIONS OF 2000 ROWS;
Explanation: Takes a large list ($data), processes each row, and saves the database's RAM by committing every 2,000 items.

Verification: MATCH (p:Product) RETURN count(p);

B. Batch Update (Price Increase)
Command: ```cypher MATCH (p:Product {category: 'Electronics'}) CALL { WITH p SET p.price = p.price * 1.10 } IN TRANSACTIONS OF 5000 ROWS;

* **Explanation:** Efficiently updates millions of electronic products without locking the entire database for too long.
* **Verification:** `MATCH (p:Product {category: 'Electronics'}) RETURN p.name, p.price LIMIT 5;`

---

## 🚀 The Final Cleanup Workflow
Run these in order to perfectly sanitize your database:

1.  **Remove High-Cost Links:** `MATCH ()-[r:SHIPS]->() WHERE r.cost > 10 DELETE r`
2.  **Remove Orphaned Products:** `MATCH (p:Product) WHERE NOT (p)<-[:SHIPS]-() DELETE p`
3.  **Remove Unused Suppliers:** `MATCH (s:Supplier) WHERE NOT (s)--() DELETE s`
4.  **Final Audit:** `MATCH (n) RETURN labels(n), count(*);