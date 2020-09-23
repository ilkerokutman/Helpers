package com.performans.clients;

import com.performans.helpers.Utils;
import static org.junit.Assert.*;
import org.junit.Test;

public class HelperValidateEmailTest {
    @Test
    public void emailValidator_CorrectEmailSimple_ReturnsTrue() {
        assertTrue(Utils.isValidEmail("name@example.com"));
    }
}
