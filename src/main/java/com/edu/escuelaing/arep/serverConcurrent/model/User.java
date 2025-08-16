package com.edu.escuelaing.arep.serverConcurrent.model;

import lombok.Data;

@Data
public class User {
    private String name;
    private String lastName;
    private String identification;

    @Override
    public String toString() {
        return "{"
                + "\"name\": \"" + name + "\", "
                + "\"lastName\": \"" + lastName + "\", "
                + "\"identification\": \"" + identification + "\""
                + "}";
    }
}
