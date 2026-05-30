# Start(up) either the local or prodlike docker stack
up:
    @echo 'Starting prodlike stack.'
    docker compose -f docker-compose.prodlike.yaml up --build -d

# Stop(down) either the local or prodlike docker stack
down:
    @echo 'Stopping prodlike stack.'
    docker compose -f docker-compose.prodlike.yaml down
