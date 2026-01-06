# Dreader LLM Parser
### Запуск LLM
```
docker-compose up -d
docker exec -it ollama ollama pull gemma3:1b
docker exec -it ollama ollama pull gemma3:4b
docker exec -it ollama ollama pull deepseek-r1:7b
docker exec -it ollama ollama pull llama3.1:8b
```
### Проверить список доступных моделей
```
docker exec -it ollama ollama list
```