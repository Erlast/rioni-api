# Build and run (tests run first — build fails if tests don't pass)
# Prerequisites: a running PostgreSQL instance for tests (or Testcontainers will be used)
up-prod:
	./gradlew test && docker compose -p rioni-api up --build
build-prod:
	./gradlew test && docker compose -p rioni-api build

# Build without tests (fast, for development)
build-fast:
	docker compose -p rioni-api build

# ============================================================
# Testing
# Prerequisites:
#   Start test PostgreSQL container:
#     docker run -d --name rioni-test-pg \
#       -e POSTGRES_DB=rioni_test -e POSTGRES_USER=rioni_db \
#       -e POSTGRES_PASSWORD=xDspl!pzoxI98^zLPz_02kLk \
#       -p 5433:5432 postgres:16-alpine
#
#   Clean DB before first run:
#     docker exec rioni-test-pg psql -U rioni_db -d rioni_test \
#       -c "DROP SCHEMA public CASCADE; CREATE SCHEMA public;"
# ============================================================

# Run all tests
test:
	./gradlew test

# Run a specific test class (usage: make test-specific TEST=com.rioni.lk.api.controller.DictionaryControllerTest)
test-specific:
	./gradlew test --tests "$(TEST)"

# Clean test build artifacts
test-clean:
	./gradlew cleanTest

# Run tests with verbose output
test-verbose:
	./gradlew test --info

# Run tests with HTML report generation
test-report:
	./gradlew test && open build/reports/tests/test/index.html

# Clean test database schema (recreates all tables from JPA entities)
test-db-clean:
	docker exec rioni-test-pg psql -U rioni_db -d rioni_test -c "DROP SCHEMA public CASCADE; CREATE SCHEMA public;"

# Start test PostgreSQL container (removes existing container first)
test-db-start:
	docker rm -f rioni-test-pg 2>/dev/null || true
	docker run -d --name rioni-test-pg \
		-e POSTGRES_DB=rioni_test -e POSTGRES_USER=rioni_db \
		-e POSTGRES_PASSWORD=xDspl!pzoxI98^zLPz_02kLk \
		-p 5433:5432 postgres:16-alpine

# Stop and remove test PostgreSQL container
test-db-stop:
	docker rm -f rioni-test-pg 2>/dev/null || true
