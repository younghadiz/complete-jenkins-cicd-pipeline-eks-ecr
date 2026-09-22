package com.example;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ApplicationTest {

    @Test
    public void shouldReturnOkStatus() {
        Application application = new Application();

        assertEquals("OK", application.getStatus());
    }
}
