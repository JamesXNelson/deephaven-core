if [[ "$1" = "--debug" || "$1" == "-d" ]]; then
    export PORTS="5005:5005"
    export DEBUG=true
fi
./gradlew prepareCompose && docker-compose build && docker-compose up
