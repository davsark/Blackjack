# 🎰 Blackjack Multijugador

Proyecto de Blackjack desarrollado en Kotlin Multiplatform con Compose Desktop.

## 📋 Características

- ✅ **Modo PVE**: Juega contra el dealer (IA)
- ✅ **Comunicación en red**: Cliente-servidor con sockets TCP
- ✅ **Sistema de records**: Guarda estadísticas de jugadores en JSON
- ✅ **Configuración desde archivos**: `server-config.properties`
- ✅ **Interfaz gráfica**: UI completa con Compose Desktop
- ✅ **Manejo de errores**: Desconexiones, timeouts, validación de datos
- ✅ **Arquitectura limpia**: KMP con código compartido (commonMain)

## 🏗️ Arquitectura

```
Blackjack/
├── composeApp/              # Cliente Desktop
│   ├── src/
│   │   ├── commonMain/      # Código compartido
│   │   │   ├── protocol/    # Mensajes y protocolos
│   │   │   ├── model/       # Deck, Hand
│   │   │   ├── game/        # Lógica de Blackjack
│   │   │   └── config/      # Configuración
│   │   └── jvmMain/         # Cliente específico
│   │       ├── network/     # GameClient (sockets)
│   │       ├── viewmodel/   # Estado del juego
│   │       └── ui/          # Pantallas Compose
│
└── server/                  # Servidor JVM
    └── src/main/
        ├── kotlin/
        │   ├── GameServer.kt       # Servidor principal
        │   ├── ClientHandler.kt    # Manejo de clientes
        │   ├── DealerAI.kt         # IA del dealer
        │   └── RecordsManager.kt   # Gestión de records
        └── resources/
            └── server-config.properties
```

## 🚀 Cómo Ejecutar

### **1. Iniciar el Servidor**

Desde la raíz del proyecto:

```bash
./gradlew :server:run
```

El servidor se iniciará en el puerto **9999** (configurable en `server-config.properties`).

Salida esperada:
```
============================================================
🎰 SERVIDOR DE BLACKJACK INICIADO
============================================================
📡 Puerto: 9999
🎮 Esperando conexiones de clientes...
🛑 Presiona Ctrl+C para detener el servidor
============================================================
```

### **2. Iniciar el Cliente**

Desde otra terminal:

```bash
./gradlew :composeApp:run
```

Se abrirá la ventana del cliente. Introduce:
- **Host**: `localhost` (o IP del servidor)
- **Puerto**: `9999`
- Haz clic en **Conectar**

### **3. Jugar**

1. Introduce tu nombre de jugador
2. Selecciona modo **PVE** (vs Dealer)
3. Haz clic en **Jugar**
4. Usa los botones:
   - **🎴 PEDIR**: Solicitar una carta
   - **✋ PLANTARSE**: Finalizar tu turno
5. El dealer jugará automáticamente después de que te plantes

## 🎮 Reglas del Juego

- **Objetivo**: Llegar a 21 o lo más cerca posible sin pasarte
- **Valores de cartas**:
  - Números: Su valor nominal (2-10)
  - Figuras (J, Q, K): Valen 10
  - As: Vale 1 u 11 (se ajusta automáticamente)
- **Blackjack natural**: As + carta de 10 con 2 cartas (ganas automáticamente)
- **Reglas del dealer**:
  - Debe pedir si tiene 16 o menos
  - Debe plantarse si tiene 17 o más

## 📊 Sistema de Records

Los records se guardan automáticamente en `records.json`. Incluyen:
- Nombre del jugador
- Victorias / Derrotas
- Número de Blackjacks naturales
- Porcentaje de victorias

Haz clic en **🏆 Records** durante el juego para ver el top de jugadores.

## ⚙️ Configuración del Servidor

Edita `server/src/main/resources/server-config.properties`:

```properties
# Puerto del servidor
server.port=9999

# Máximo de jugadores por mesa (PVP)
server.maxPlayersPerTable=4

# Habilitar modos
server.pveEnabled=true
server.pvpEnabled=true

# Timeout de conexión en segundos
server.connectionTimeout=60

# Archivo de records
server.recordsFile=records.json
```

## 🛠️ Tecnologías Utilizadas

- **Kotlin Multiplatform (KMP)**: Compartir código entre cliente y servidor
- **Compose Desktop**: Interfaz gráfica moderna
- **kotlinx.coroutines**: Concurrencia y manejo asíncrono
- **kotlinx.serialization**: Serialización JSON de mensajes
- **Java Sockets**: Comunicación TCP cliente-servidor

## 🧪 Testing

Para probar múltiples clientes simultáneamente:

```bash
# Terminal 1: Servidor
./gradlew :server:run

# Terminal 2: Cliente 1
./gradlew :composeApp:run

# Terminal 3: Cliente 2
./gradlew :composeApp:run
```

## 📝 Protocolo de Comunicación

### Mensajes Cliente → Servidor
- `JoinGame`: Unirse al juego
- `RequestCard`: Pedir carta
- `Stand`: Plantarse
- `NewGame`: Nueva partida
- `RequestRecords`: Solicitar records

### Mensajes Servidor → Cliente
- `JoinConfirmation`: Confirmación de unión
- `GameState`: Estado actual del juego
- `GameResult`: Resultado final
- `RecordsList`: Lista de records
- `Error`: Mensajes de error

Todos los mensajes son JSON serializados con kotlinx.serialization.

## 📦 Estructura de Datos

### Card (Carta)
```kotlin
data class Card(
    val rank: Rank,  // A, 2-10, J, Q, K
    val suit: Suit,  // ♥, ♦, ♣, ♠
    val hidden: Boolean = false
)
```

### GameState (Estado del Juego)
```kotlin
data class GameState(
    val playerHand: List<Card>,
    val dealerHand: List<Card>,
    val playerScore: Int,
    val dealerScore: Int,
    val gameState: GamePhase,
    val canRequestCard: Boolean,
    val canStand: Boolean
)
```

## 🐛 Solución de Problemas

### El cliente no se conecta
- Verifica que el servidor esté ejecutándose
- Comprueba que el puerto no esté bloqueado por firewall
- Asegúrate de usar `localhost` si es en la misma máquina

### Error "Address already in use"
- Hay otro proceso usando el puerto 9999
- Cambia el puerto en `server-config.properties`
- O mata el proceso: `lsof -ti:9999 | xargs kill`

### El juego se congela
- Verifica los logs en la terminal del servidor
- El cliente se desconectará automáticamente tras 60s de inactividad

## 👥 Créditos

Proyecto desarrollado como parte del curso de DAM (Desarrollo de Aplicaciones Multiplataforma).

## 📄 Licencia

Proyecto educativo - Uso libre para aprendizaje.
