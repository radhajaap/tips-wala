# Run on Windows without Docker
1. JDK 21 (Temurin, adoptium.net, tick "Set JAVA_HOME" + "Add to PATH"), Maven (zip from maven.apache.org, unzip to C:\maven, add C:\maven\bin to PATH), PostgreSQL 16 (postgresql.org/download/windows, remember the postgres password).
2. Create the database (password must equal DB_PASSWORD in .env):
   `"C:\Program Files\PostgreSQL\16\bin\psql" -U postgres` then
   `CREATE USER marketpulse WITH PASSWORD 'same-as-DB_PASSWORD';`  `CREATE DATABASE marketpulse OWNER marketpulse;`  `\q`
3. Double-click `run-local.bat` (or run it in cmd). First start downloads Java libraries (5-10 min).
4. Open http://localhost:8080 and http://localhost:8080/admin/index.html. Redis is optional locally.
