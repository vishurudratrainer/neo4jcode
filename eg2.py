import csv
from neo4j import GraphDatabase

# --- CONFIGURATION ---
URI = "neo4j://localhost:7687"  # Use 'neo4j+s://' for Aura/SSL Clusters
AUTH = ("neo4j", "password")
DATABASE = "neo4j"
BATCH_SIZE = 5000  # Optimal for cluster stability

class BigDataImporter:
    def __init__(self, uri, auth):
        self.driver = GraphDatabase.driver(uri, auth=auth)

    def close(self):
        self.driver.close()

    # STEP 1: Setup Schema (Fastest if done before data load)
    def setup_schema(self):
        with self.driver.session(database=DATABASE) as session:
            session.execute_write(lambda tx: tx.run("CREATE CONSTRAINT if not exists FOR (s:Supplier) REQUIRE s.id IS UNIQUE"))
            session.execute_write(lambda tx: tx.run("CREATE CONSTRAINT if not exists FOR (p:Product) REQUIRE p.id IS UNIQUE"))
            print("✅ Constraints created.")

    # STEP 2: The "APOC Approach" (Recommended for extreme speed)
    # This tells Python to just trigger the server-side bulk load
    def import_via_apoc(self):
        query = """
        CALL apoc.periodic.iterate(
          "LOAD CSV WITH HEADERS FROM 'file:///supply_links.csv' AS row RETURN row",
          "MATCH (s:Supplier {id: row.supplier_id})
           MATCH (p:Product {id: row.product_id})
           MERGE (s)-[:SHIPS {cost: toFloat(row.ship_cost)}]->(p)",
          {batchSize: $batch_size, parallel: true}
        )
        """
        with self.driver.session(database=DATABASE) as session:
            result = session.execute_write(lambda tx: tx.run(query, batch_size=BATCH_SIZE))
            summary = result.consume()
            print(f"✅ APOC Import complete. Batches: {summary.counters}")

    # STEP 3: The "Driver Approach" (If you want to process data in Python first)
    # Best if your data needs complex cleaning in Python before reaching Neo4j
    def import_with_python_batching(self, csv_file_path):
        query = """
        UNWIND $batch AS row
        MERGE (s:Supplier {id: row.supplier_id})
        MERGE (p:Product {id: row.product_id})
        MERGE (s)-[r:SHIPS]->(p)
        SET r.cost = toFloat(row.ship_cost)
        """
        
        with self.driver.session(database=DATABASE) as session:
            batch = []
            with open(csv_file_path, 'r') as f:
                reader = csv.DictReader(f)
                for row in reader:
                    batch.append(row)
                    if len(batch) >= BATCH_SIZE:
                        session.execute_write(lambda tx: tx.run(query, batch=batch))
                        batch = []
                        print(f"Uploaded {BATCH_SIZE} records...")
                
                # Final partial batch
                if batch:
                    session.execute_write(lambda tx: tx.run(query, batch=batch))

# --- EXECUTION ---
if __name__ == "__main__":
    importer = BigDataImporter(URI, AUTH)
    try:
        importer.setup_schema()
        
        # Choice A: Trigger server-side load (Faster, file must be in /import)
        importer.import_via_apoc()
        
        # Choice B: Push data from local Python (Better for data cleaning)
        # importer.import_with_python_batching("supply_links.csv")
        
    finally:
        importer.close()
