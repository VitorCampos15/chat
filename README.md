Comando para rodar o cliente: mvn clean package cargo:run

Para alterar o protocolo que esta com problema:

chat-servidor/.../UsuariosLogadosResponse.java  → @JsonProperty
chat-cliente/.../UsuariosLogadosResponse.java   → @JsonProperty
chat-cliente/.../TcpClientService.java          → root.has(...) e json.contains(...)
chat-cliente/.../UsuarioBean.java               → json.contains(...)


