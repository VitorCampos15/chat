#!/usr/bin/env bash
# Sobe o WAR no Tomcat 10 (Jakarta) baixado automaticamente pelo plugin Cargo na primeira execução.
# Uso: ./run-tomcat.sh   (ou: mvn clean package cargo:run)
# URL: http://localhost:8080/chat-client/cadastro.xhtml
# Parar: Ctrl+C
set -euo pipefail
cd "$(dirname "$0")"
mvn clean package cargo:run
