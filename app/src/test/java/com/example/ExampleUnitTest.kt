package com.example

import com.example.data.models.UserRole
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun testUserRoleParsing() {
    assertEquals(UserRole.CLIENTE, UserRole.fromString("cliente"))
    assertEquals(UserRole.TRABAJADOR, UserRole.fromString("tecnico"))
    assertEquals(UserRole.TRABAJADOR, UserRole.fromString("trabajador"))
    assertEquals(UserRole.ADMIN, UserRole.fromString("admin"))
    assertEquals(UserRole.CLIENTE, UserRole.fromString("unknown_role"))
    assertEquals(UserRole.CLIENTE, UserRole.fromString(null))
  }
}

