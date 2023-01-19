package com.example.xcel_loader.utils;

import com.example.xcel_loader.model.User;

import java.io.File;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Currency;
import java.util.Objects;
import java.util.UUID;

public class TestHelper {

    public static User generateFakeUser() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setPhoneNumber("0789889911");
        user.setFirstName("Jackson");
        user.setLastName("Mubiru");
        user.setPermissions(Collections.singletonList("can-view"));
        return user;
    }

    public static File getSampleFile() {
        return Paths.get(Objects.requireNonNull(TestHelper.class.getClassLoader().getResource("sample.pdf")).getFile()).toFile();
    }

    public static File getXlsFile() {
        return Paths.get(Objects.requireNonNull(TestHelper.class.getClassLoader().getResource("sample.xlsx")).getFile()).toFile();
    }

}
