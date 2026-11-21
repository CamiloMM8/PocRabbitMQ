# Prueba de concepto funcional — RabbitMQ

Dos APIs independientes hablan a través de RabbitMQ:

- **productor-api** en http://localhost:8081 publica mensajes.
- **consumidor-api** en http://localhost:8082 los lee y envía el acuse.
- El broker es RabbitMQ. La consola de monitoreo queda en http://localhost:15672 (`guest` / `guest`).

Si una de las dos APIs está apagada, la otra sigue usando la cola.

## Arrancar las APIs

RabbitMQ ya queda como servicio de Windows. No hace falta tener `rabbitmqctl` ni Maven en el PATH. En dos terminales:

```powershell
java -jar "C:\Users\Camilo Marquez\Documents\Java\PocRabbitMQ\productor\target\productor-api-0.0.1-SNAPSHOT.jar"
```

```powershell
java -jar "C:\Users\Camilo Marquez\Documents\Java\PocRabbitMQ\consumidor\target\consumidor-api-0.0.1-SNAPSHOT.jar"
```
