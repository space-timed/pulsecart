#!/bin/bash
set -e

# PulseCart Multi-Database Initialization Script
# Creates 15 dedicated logical databases for each persistent microservice to honor
# the Database-Per-Service ownership architecture while conserving host RAM.

function create_user_and_database() {
    local database=$1
    echo "Creating database '$database'..."
    psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
        SELECT 'CREATE DATABASE $database'
        WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = '$database')\gexec
        GRANT ALL PRIVILEGES ON DATABASE $database TO $POSTGRES_USER;
EOSQL
}

create_user_and_database "pulsecart_customer"
create_user_and_database "pulsecart_catalog"
create_user_and_database "pulsecart_pricing"
create_user_and_database "pulsecart_promotion"
create_user_and_database "pulsecart_inventory"
create_user_and_database "pulsecart_cart"
create_user_and_database "pulsecart_order"
create_user_and_database "pulsecart_payment"
create_user_and_database "pulsecart_fulfillment"
create_user_and_database "pulsecart_shipping"
create_user_and_database "pulsecart_notification"
create_user_and_database "pulsecart_search"
create_user_and_database "pulsecart_returns"
create_user_and_database "pulsecart_analytics"
create_user_and_database "pulsecart_keycloak"

echo "All 15 PulseCart microservice databases initialized successfully."
