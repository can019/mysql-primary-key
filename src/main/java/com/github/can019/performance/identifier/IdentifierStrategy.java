package com.github.can019.performance.identifier;

public enum IdentifierStrategy {
    JPA_AUTO_INCREMENT("JPA AUTO INCREMENT","Jpa auto increment with hibernate"),
    JPA_SEQUENCE("JPA SEQUENCE", "Jpa sequence with hibernate"),
    UUID_V1("UUID V1","UUID V1 with external library"),
    UUID_V4("UUID V4","UUID V4 with java util"),
    UUID_V1_SEQUENTIAL("UUID V1 SEQUENTIAL", "Sequential id based on UUID V1"),

    JPA_AUTO_INCREMENT_CREATED_AT("JPA AUTO INCREMENT CREATED AT"
            , "Jpa auto increment with hibernate and entity column created at"),
    JPA_SEQUENCE_CREATED_AT("JPA SEQUENCE CREATED AT"
            ,"Jpa sequence with hibernate with hibernate and entity column created at"),
    UUID_V1_CREATED_AT("UUID V1 CREATED AT"
            ,"UUID V1 with external library"),
    UUID_V4_CREATED_AT("UUID V4 CREATED AT"
            ,"UUID V4 with java util with hibernate and entity column created at"),
    UUID_V1_SEQUENTIAL_CREATED_AT("UUID V1 SEQUENTIAL CREATED AT"
            , "Sequential id based on UUID V1 with hibernate and entity column created at");
    ;

    private final String simpleName;
    private final String description;

    IdentifierStrategy(String simpleName ,String description) {
        this.simpleName = simpleName;
        this.description = description;
    }

    public String getSimpleName(){
        return this.simpleName;
    }

    public String getDescription() {
        return description;
    }

}