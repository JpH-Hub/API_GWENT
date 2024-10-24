package API_CardGameSpring.models

enum Format {
    PVP("PvP"), PVB("PvB"),
    private String code

    Format(String code) {
        this.code = code
    }

    String getCode() {
        return code
    }

}