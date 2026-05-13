# How to build project
## Setup database connection

1. **Edit `postgresql.conf`**:
   - Locate the file (e.g., `/etc/postgresql/<version>/main/postgresql.conf`).
   - Ensure the line `listen_addresses` is set to:
     ```
     listen_addresses = '*'
     ```

2. **Edit `pg_hba.conf`**:
   - Add or update the following line to allow local connections:
     ```
     host    all             all             127.0.0.1/32            md5
     ```

3. **Restart PostgreSQL**:
   ```bash
   sudo systemctl restart postgresql
   ```

This will enable PostgreSQL to accept TCP/IP connections on `localhost:5432`.

## Create user and  database
```bash
sudo -u postgres psql
```

```sql
CREATE DATABASE rating_university_db;
CREATE USER rating_university_user WITH PASSWORD 'rating_university_user_password';
GRANT CREATE, USAGE ON SCHEMA public TO rating_university_user;

ALTER DATABASE rating_university_db OWNER TO rating_university_user;
CREATE DATABASE rating_university_db OWNER rating_university_user;

GRANT ALL PRIVILEGES ON DATABASE rating_university_db TO rating_university_user;
GRANT ALL PRIVILEGES ON SCHEMA public TO rating_university_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO rating_university_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO rating_university_user;
```

## Build and start project
```bash
docker build -t rating-university . 

docker run --rm -p 8080:8080 \
  --network="host" \
  -e PGHOST=localhost \
  -e PGPORT=5432 \
  -e PGDATABASE=rating_university_db \
  -e PGUSER=rating_university_user \
  -e PGPASSWORD=rating_university_user_password \
  rating-university:latest
```