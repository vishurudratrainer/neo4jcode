from graphdatascience import GraphDataScience
import pandas as pd

# 1. Connection settings
URI = "bolt://localhost:7687"
AUTH = ("neo4j", "password")
gds = GraphDataScience(URI, auth=AUTH)

def run_city_pathfinder():
    # A. Setup: Create Data
    gds.run_cypher("""
    MERGE (ny:City {name: 'New York'})
    MERGE (ph:City {name: 'Philadelphia'})
    MERGE (dc:City {name: 'Washington DC'})
    MERGE (ba:City {name: 'Baltimore'})
    MERGE (bo:City {name: 'Boston'})
    
    MERGE (ny)-[:ROAD {cost: 95}]->(ph)
    MERGE (ph)-[:ROAD {cost: 100}]->(dc)
    MERGE (ny)-[:ROAD {cost: 215}]->(bo)
    MERGE (ph)-[:ROAD {cost: 40}]->(ba)
    MERGE (ba)-[:ROAD {cost: 40}]->(dc)
    """)

    # B. Cleanup any old projections to avoid "already exists" errors
    if gds.graph.exists("cityGraph").exists:
        gds.graph.drop(gds.graph.get("cityGraph"))

    # C. Project the Graph
    # NOTE: Using camelCase 'relationshipProperties'
    G, _ = gds.graph.project(
        "cityGraph", 
        "City", 
        "ROAD", 
        relationshipProperties="cost" 
    )
    print(f"Projected graph: {G.name()}")

    # D. Find Node IDs
    source_id = gds.find_node_id(["City"], {"name": "New York"})
    target_id = gds.find_node_id(["City"], {"name": "Washington DC"})

    # E. Run Dijkstra
    # NOTE: Using camelCase 'sourceNode', 'targetNode', and 'relationshipWeightProperty'
    df = gds.shortestPath.dijkstra.stream(
        G,
        sourceNode=source_id,
        targetNode=target_id,
        relationshipWeightProperty="cost"
    )

    # F. Display Results
    if not df.empty:
        node_ids = df["nodeIds"].iloc[0]
        path_names = [gds.util.asNode(n_id)["name"] for n_id in node_ids]
        print(f"\nPath: {' -> '.join(path_names)}")
        print(f"Total Cost: ${df['totalCost'].iloc[0]}")

    G.drop()

if __name__ == "__main__":
    try:
        run_city_pathfinder()
    finally:
        gds.close()
