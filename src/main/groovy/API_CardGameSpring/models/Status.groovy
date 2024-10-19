package API_CardGameSpring.models

enum Status {
 NOT_INITIALIZED("Partida nao inicializada"),
 private String code

    Status(String code) {
        this.code = code
    }

    String getCode() {
        return code
    }
}