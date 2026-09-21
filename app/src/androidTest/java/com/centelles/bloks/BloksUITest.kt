package com.centelles.bloks

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.centelles.bloks.data.GameRepository
import com.centelles.bloks.engine.logic.GameEngine
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject
import kotlinx.coroutines.runBlocking

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class BloksUITest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Inject
    lateinit var repository: GameRepository

    @Inject
    lateinit var gameEngine: GameEngine

    @Before
    fun init() {
        hiltRule.inject()
        // Configurar un estado inicial conocido usando suspend functions de nuestro repositorio real/mocked
        runBlocking {
            repository.saveHighScore(500)
            repository.addCoins(1000) // Agregar suficientes monedas para probar compras
            repository.setSoundEnabled(true)
            repository.setVibrationEnabled(true)
            repository.setActiveTheme(0)
        }
    }

    @Test
    fun testFullApplicationFlowAndNavigation() {
        // --- 1. MENÚ PRINCIPAL ---
        // Verificar que estamos en el menú principal chequeando los títulos llamativos en neón
        composeTestRule.onNodeWithText("BLOCK").assertIsDisplayed()
        composeTestRule.onNodeWithText("BLOOM").assertIsDisplayed()

        // --- 2. PANTALLA DE AJUSTES ---
        // Navegar a Ajustes
        composeTestRule.onNodeWithText("AJUSTES").assertIsDisplayed().performClick()
        
        // Verificar contenido de Ajustes
        composeTestRule.onNodeWithText("AJUSTES").assertIsDisplayed()
        composeTestRule.onNodeWithText("Efectos de Sonido").assertIsDisplayed()
        composeTestRule.onNodeWithText("Vibración (Háptica)").assertIsDisplayed()
        composeTestRule.onNodeWithText("Versión 1.0.0").assertIsDisplayed()
        
        // Regresar al menú principal
        composeTestRule.onNodeWithContentDescription("Volver").performClick()

        // --- 3. PANTALLA DE RETO DIARIO ---
        // Navegar a Reto Diario
        composeTestRule.onNodeWithText("RETO DIARIO").assertIsDisplayed().performClick()
        
        // Verificar contenido del reto diario
        composeTestRule.onNodeWithText("RETO DIARIO").assertIsDisplayed()
        composeTestRule.onNodeWithText("Reto de Hoy").assertIsDisplayed()
        composeTestRule.onNodeWithText("Limpia 15 líneas en una partida").assertIsDisplayed()
        
        // Volver al menú
        composeTestRule.onNodeWithContentDescription("Volver").performClick()

        // --- 4. PANTALLA DE LA TIENDA ---
        // Navegar a la Tienda
        composeTestRule.onNodeWithText("TIENDA").assertIsDisplayed().performClick()
        
        // Verificar contenido de la Tienda
        composeTestRule.onNodeWithText("TIENDA").assertIsDisplayed()
        composeTestRule.onNodeWithText("Monedas Gratis").assertIsDisplayed()
        composeTestRule.onNodeWithText("VER").assertIsDisplayed()
        
        // Test de compra/activación de temas personalizados usando monedas de la base de datos
        composeTestRule.onNodeWithText("Neon Glow").assertIsDisplayed()
        // Hacemos scroll si es necesario y buscamos el botón COMPRAR dentro de la tarjeta del Neon Glow
        composeTestRule.onNodeWithText("Neon Glow")
            .onParent() // Vamos al contenedor (Row o Column de la tarjeta)
            .onChildren()
            .filterToOne(hasText("COMPRAR"))
            .performClick()
        
        // Volver al menú
        composeTestRule.onNodeWithContentDescription("Volver").performClick()

        // --- 5. PANTALLA DE JUEGO (GAME LOOP) ---
        // Iniciar juego principal
        composeTestRule.onNodeWithText("JUGAR").assertIsDisplayed().performClick()

        // Verificar HUD de juego
        composeTestRule.onNodeWithText("RÉCORD: 500").assertExists()
        
        // El test termina aquí exitosamente tras navegar por toda la app
    }
}
