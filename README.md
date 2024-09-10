# 💸 **Sistema de Transacciones**

Este proyecto es un sistema de gestión de transacciones financieras que permite a los usuarios realizar diversas operaciones como registro, login, consulta de saldo, retiros, depósitos, transferencias y auditoría de transacciones.

## 🌟 **Características**

- 📝 Registro de nuevos usuarios.
- 🔐 Inicio de sesión para autenticación.
- 💰 Consulta de saldo disponible.
- 🏦 Operaciones de retiro, depósito y transferencia de fondos.
- 📊 Auditoría y visualización del historial de transacciones.

---

## 🔗 **Rutas Disponibles**

### 1. 📝 **Registro de Usuario**
   - **URL:** `http://localhost:8080/auth/register`
   - **Método:** `POST`
   - **Descripción:** Permite crear una nueva cuenta de usuario proporcionando datos como nombre, email y contraseña.

### 2. 🔐 **Inicio de Sesión**
   - **URL:** `http://localhost:8080/auth/login`
   - **Método:** `POST`
   - **Descripción:** Inicia sesión en el sistema utilizando credenciales válidas. Devuelve un token JWT para autenticación.

### 3. 💵 **Obtener Saldo**
   - **URL:** `http://localhost:8080/transaccion/balance`
   - **Método:** `GET`
   - **Descripción:** Muestra el saldo disponible en la cuenta del usuario autenticado.

### 4. 🏧 **Retiro de Fondos**
   - **URL:** `http://localhost:8080/transaccion/retiro`
   - **Método:** `POST`
   - **Descripción:** Permite retirar una cantidad específica de dinero de la cuenta del usuario.

### 5. 💳 **Depósito de Fondos**
   - **URL:** `http://localhost:8080/transaccion/depositar`
   - **Método:** `POST`
   - **Descripción:** Permite depositar una cantidad de dinero en la cuenta del usuario.

### 6. 🔄 **Transferencia entre Usuarios**
   - **URL:** `http://localhost:8080/transaccion/transferencia`
   - **Método:** `POST`
   - **Descripción:** Realiza una transferencia de fondos desde la cuenta del usuario autenticado hacia la cuenta de otro usuario.

### 7. 📋 **Auditoría de Transacciones (Todos los Usuarios)**
   - **URL:** `http://localhost:8080/transaccion/auditoria`
   - **Método:** `GET`
   - **Descripción:** Muestra la tabla completa de todas las transacciones realizadas por todos los usuarios del sistema (requiere permisos de administrador).

### 8. 📜 **Historial de Transacciones del Usuario**
   - **URL:** `http://localhost:8080/transaccion/historial`
   - **Método:** `GET`
   - **Descripción:** Muestra el historial de transacciones realizadas por el usuario autenticado.

---

## 🛠️ **Tecnologías Utilizadas**

- **Java Spring Boot** 🛡️
- **Maven** 📦
- **JWT** 🔑 para la autenticación
- **MySQL** 🗄️ como base de datos

---

## 🚀 **Instrucciones de Ejecución**

1. Clona el repositorio.
2. Ejecuta `mvn spring-boot:run`.
3. Accede a las rutas proporcionadas para realizar las operaciones.

---

## 📞 **Contacto**

Si tienes alguna duda o sugerencia, no dudes en contactarnos.
