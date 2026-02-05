import uuid
from neo4j import GraphDatabase

class Neo4jApp:
    def __init__(self, uri, user, password):
        self.driver = GraphDatabase.driver(uri, auth=(user, password))

    def close(self):
        self.driver.close()

    # --- INITIALIZATION ---
    def create_constraints(self):
        """Always ensure indexes exist before performing CRUD at scale."""
        with self.driver.session() as session:
            session.run("CREATE CONSTRAINT FOR (u:User) REQUIRE u.id IS UNIQUE")
            print("✅ Constraint for :User(id) initialized.")

    # --- CREATE (Multiple Patterns) ---
    def create_user(self, name, email):
        """Standard single-record creation."""
        query = "CREATE (u:User {id: $id, name: $name, email: $email}) RETURN u.id"
        with self.driver.session() as session:
            user_id = str(uuid.uuid4())[:8]
            result = session.run(query, id=user_id, name=name, email=email)
            return result.single()[0]

    def batch_create_users(self, user_list):
        """Big Data optimized creation using UNWIND."""
        query = """
        UNWIND $batch AS user_data
        MERGE (u:User {id: user_data.id})
        SET u.name = user_data.name, u.email = user_data.email
        """
        with self.driver.session() as session:
            session.run(query, batch=user_list)
            print(f"🚀 Batched {len(user_list)} users into the database.")

    # --- READ (Search & Retrieval) ---
    def get_user_by_id(self, user_id):
        """Fetch a specific node by its unique ID."""
        query = "MATCH (u:User {id: $id}) RETURN u.name AS name, u.email AS email"
        with self.driver.session() as session:
            result = session.run(query, id=user_id)
            record = result.single()
            return record.data() if record else "User not found."

    def get_all_users(self, limit=10):
        """Fetch a list of users with a safety limit."""
        query = "MATCH (u:User) RETURN u.id AS id, u.name AS name LIMIT $limit"
        with self.driver.session() as session:
            result = session.run(query, limit=limit)
            return [record.data() for record in result]

    # --- UPDATE (Property & Label Changes) ---
    def update_user_email(self, user_id, new_email):
        """Update a specific property of a node."""
        query = "MATCH (u:User {id: $id}) SET u.email = $new_email RETURN u.name"
        with self.driver.session() as session:
            result = session.run(query, id=user_id, new_email=new_email)
            record = result.single()
            return f"Updated {record[0]}" if record else "User not found."

    # --- DELETE (Node & Relationship Cleanup) ---
    def delete_user(self, user_id):
        """Safely remove a user and all their relationships (DETACH)."""
        query = "MATCH (u:User {id: $id}) DETACH DELETE u"
        with self.driver.session() as session:
            session.run(query, id=user_id)
            print(f"🗑️ User {user_id} and all connected data removed.")

    def clear_database(self):
        """DANGER: Wipes the entire database. Use for testing only."""
        with self.driver.session() as session:
            session.run("MATCH (n) DETACH DELETE n")
            print("💥 Database cleared.")

# --- EXECUTION FLOW ---
if __name__ == "__main__":
    # 1. Connect
    app = Neo4jApp("bolt://localhost:7687", "neo4j", "your_chosen_password")
    
    try:
        # 2. Setup
        app.create_constraints()

        # 3. Create (Single and Batch)
        uid = app.create_user("Alice", "alice@example.com")
        
        test_batch = [
            {"id": "U1", "name": "Bob", "email": "bob@dev.com"},
            {"id": "U2", "name": "Charlie", "email": "charlie@dev.com"}
        ]
        app.batch_create_users(test_batch)

        # 4. Read
        print(f"Single User: {app.get_user_by_id(uid)}")
        print(f"All Users: {app.get_all_users()}")

        # 5. Update
        print(app.update_user_email("U1", "bob.new@dev.com"))

        # 6. Delete
        app.delete_user("U2")

    finally:
        app.close()
