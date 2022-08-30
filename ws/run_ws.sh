#!/bin/sh

echo "Downloading WhiteSource agent..."
curl -LJO https://github.com/whitesource/unified-agent-distribution/releases/latest/download/wss-unified-agent.jar

echo "Running WhiteSource scan..."
java -jar wss-unified-agent.jar -userKey ${USER_KEY} -apiKey ${API_KEY} -projectVersion ${PROJECT_VERSION} -projectToken ${PROJECT_TOKEN} -productVersion ${PRODUCT_VERSION} -productToken ${PRODUCT_TOKEN} -c ws.config -d ../