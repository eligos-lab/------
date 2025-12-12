@echo off
echo 🔄 Building and running Financy App...
mvn clean package exec:java -Dexec.mainClass=org.example.Main
pause