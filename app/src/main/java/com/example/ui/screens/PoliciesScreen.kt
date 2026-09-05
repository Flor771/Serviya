package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodels.MainViewModel
import com.example.ui.viewmodels.Screen

data class PolicySection(val id: Int, val title: String, val content: String, val icon: androidx.compose.ui.graphics.vector.ImageVector = Icons.Default.Info)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PoliciesScreen(viewModel: MainViewModel) {
    val policies = listOf(
        PolicySection(1, "¿QUÉ ES CHAMBA RD?", "CHAMBA RD es una plataforma digital creada para conectar personas que necesitan realizar trabajos o servicios con técnicos y trabajadores disponibles en República Dominicana.\n\nLa plataforma busca facilitar:\n- Encontrar técnicos.\n- Publicar trabajos.\n- Recibir propuestas.\n- Comparar opciones.\n- Seleccionar un técnico.\n- Mantener comunicación mediante chat.\n- Gestionar el pago.\n- Registrar la finalización del trabajo.\n- Calificar la experiencia.\n- Resolver problemas mediante reportes y disputas.\n\nCHAMBA RD funciona como plataforma de conexión y gestión entre clientes y técnicos.", Icons.Default.Info),
        PolicySection(2, "VENTAJAS PARA EL CLIENTE", "El cliente podrá disfrutar de:\n\n🔎 Buscar técnicos\nEncontrar técnicos disponibles según el tipo de servicio requerido.\n\n📋 Publicar una chamba\nPublicar gratuitamente una solicitud de trabajo.\n\n💰 Recibir propuestas\nRecibir diferentes propuestas económicas de técnicos interesados.\n\n👷 Elegir al técnico\nEl cliente mantiene la decisión final sobre qué técnico contratar.\n\n🛡️ Técnicos verificados\nCuando corresponda, los técnicos que hayan completado satisfactoriamente el proceso de verificación podrán mostrar la insignia:\n✓ TÉCNICO VERIFICADO\n\n💬 Chat\nComunicación relacionada con el servicio mediante el sistema de chat de CHAMBA RD.\n\n💳 Pago organizado\nEl cliente podrá pagar mediante el sistema de transferencia establecido por CHAMBA RD después de seleccionar al técnico y establecer el precio final.\n\n🧾 Comprobante\nEl cliente podrá subir el comprobante de transferencia para su revisión.\n\n⭐ Calificaciones\nDespués del servicio podrá calificar al técnico.\n\n⚠️ Reportes y disputas\nSi ocurre un problema, podrá reportarlo mediante las herramientas disponibles.", Icons.Default.Person),
        PolicySection(3, "VENTAJAS PARA EL TÉCNICO", "El técnico podrá:\n- Crear un perfil profesional.\n- Mostrar sus habilidades.\n- Indicar sus servicios.\n- Postularse a trabajos.\n- Enviar propuestas económicas.\n- Ser seleccionado por clientes.\n- Utilizar el chat.\n- Obtener trabajos nuevos.\n- Registrar sus datos para recibir pagos.\n- Consultar sus ganancias.\n- Recibir el 100% del precio acordado del trabajo.\n- Construir reputación mediante calificaciones.\n- Obtener insignia de técnico verificado cuando corresponda.\n\nLa comisión de CHAMBA RD no será descontada del precio del técnico.", Icons.Default.Engineering),
        PolicySection(4, "VENTAJA DEL MODELO DE COMISIÓN", "La comisión inicial de CHAMBA RD será:\n10%\n\nLa comisión se agregará al precio del trabajo y será pagada por el cliente.\n\nEjemplo:\nTrabajo: RD$5,000\nComisión CHAMBA RD: RD$500\nTotal cliente: RD$5,500\nTécnico: RD$5,000\n\nEsto significa que el técnico recibe el 100% del precio acordado por su trabajo.", Icons.Default.Percent),
        PolicySection(5, "PUBLICAR UNA CHAMBA", "Publicar una chamba no genera automáticamente un cobro.\n\nEl cliente puede:\n1. Crear la chamba.\n2. Recibir postulaciones.\n3. Revisar propuestas.\n4. Seleccionar un técnico.\n5. Establecer el precio final.\n6. Crear el contrato.\n7. Posteriormente realizar el pago.\n\nNunca cobrar al cliente solamente por publicar una chamba.", Icons.Default.PostAdd),
        PolicySection(6, "REGLAS PARA CLIENTES", "El cliente debe:\n- Proporcionar información verdadera.\n- Describir correctamente el trabajo.\n- Indicar condiciones importantes.\n- No publicar trabajos ilegales.\n- No solicitar actividades peligrosas o prohibidas.\n- No discriminar injustificadamente a los técnicos.\n- Respetar al técnico.\n- Cumplir los acuerdos establecidos.\n- No utilizar comprobantes falsos.\n- No intentar engañar a la plataforma.\n- Confirmar correctamente la finalización del trabajo.\n- Utilizar los mecanismos oficiales de reporte cuando exista un problema.", Icons.Default.Gavel),
        PolicySection(7, "REGLAS PARA TÉCNICOS", "El técnico debe:\n- Proporcionar información verdadera.\n- Utilizar sus propios datos.\n- Tener capacidad para realizar el trabajo al que se postula.\n- Presentar propuestas honestas.\n- Respetar al cliente.\n- Cumplir los acuerdos aceptados.\n- Informar si existe algún inconveniente.\n- No utilizar documentos falsos.\n- No utilizar cuentas bancarias de terceros sin autorización.\n- Mantener actualizados sus datos de pago.\n- No solicitar pagos fraudulentos.\n- No presentar comprobantes falsos.\n- No realizar actividades ilegales utilizando CHAMBA RD.", Icons.Default.Gavel),
        PolicySection(8, "REGLAS DE SEGURIDAD", "Está prohibido utilizar CHAMBA RD para:\n- Fraude.\n- Estafa.\n- Suplantación de identidad.\n- Robo.\n- Actividades ilegales.\n- Lavado de dinero.\n- Falsificación de documentos.\n- Falsificación de comprobantes.\n- Amenazas.\n- Acoso.\n- Extorsión.\n- Actividades violentas.\n- Distribución de contenido ilegal.\n\nLas cuentas involucradas podrán ser restringidas, suspendidas o canceladas según corresponda.", Icons.Default.Security),
        PolicySection(9, "REGLAS SOBRE PAGOS", "El cliente solamente deberá realizar el pago después de:\nTécnico seleccionado → Precio final acordado → Contrato creado\n\nEl sistema mostrará:\nPrecio del trabajo\nComisión CHAMBA RD\nTotal a transferir\n\nEl cliente deberá realizar la transferencia a la cuenta bancaria oficial indicada por CHAMBA RD.", Icons.Default.Payment),
        PolicySection(10, "COMPROBANTES", "El comprobante debe:\n- Ser auténtico.\n- Corresponder a la operación.\n- Mostrar información suficiente para su verificación.\n- No estar alterado.\n\nEnviar un comprobante falso constituye una violación grave de las reglas.", Icons.Default.Receipt),
        PolicySection(11, "CONFIRMACIÓN DEL PAGO", "Subir un comprobante NO significa que el pago haya sido confirmado.\n\nEl pago debe ser revisado por el administrador.\n\nEstados posibles:\n- Pendiente.\n- Comprobante subido.\n- En revisión.\n- Confirmado.\n- Rechazado.\n- Retenido.\n- Liberado.\n- Reembolsado.\n- En disputa.", Icons.Default.Verified),
        PolicySection(12, "DINERO DEL CLIENTE Y PAGO AL TÉCNICO", "Cuando el pago sea confirmado, el sistema indicará que la operación está autorizada/ retenida.\n\nEsto NO significa que el técnico ya haya recibido el dinero.\n\nDespués de que el cliente confirme:\nTRABAJO COMPLETADO\n\nel pago quedará listo para que el administrador realice el pago manual al técnico.", Icons.Default.Paid),
        PolicySection(13, "DATOS BANCARIOS DEL TÉCNICO", "El técnico deberá registrar sus datos bancarios para poder recibir sus ganancias.\n\nLos datos serán privados.\n\nEl cliente no podrá consultar:\n- Número de cuenta.\n- Información bancaria.\n- Datos privados de pago.\n\nEl administrador autorizado podrá consultar la información cuando sea necesario para realizar el pago.", Icons.Default.AccountBalance),
        PolicySection(14, "REGLAS SOBRE CANCELACIONES", "Las cancelaciones dependerán del estado de la chamba y del pago.\n\nAntes de existir un pago confirmado, la cancelación podrá gestionarse según el estado de la operación.\n\nCuando exista dinero involucrado, CHAMBA RD podrá revisar el caso antes de determinar el tratamiento correspondiente.", Icons.Default.Cancel),
        PolicySection(15, "REEMBOLSOS", "Los reembolsos serán gestionados de acuerdo con:\n- Estado del pago.\n- Motivo de la solicitud.\n- Evidencias disponibles.\n- Estado del trabajo.\n- Resultado de la revisión administrativa.\n\nTodo reembolso debe quedar registrado.", Icons.Default.MoneyOff),
        PolicySection(16, "DISPUTAS", "Cliente o técnico podrán reportar un problema.\n\nLa disputa podrá incluir:\n- Descripción del problema.\n- Evidencias.\n- Fotografías.\n- Comprobantes.\n- Conversaciones relevantes.\n- Información de la operación.\n\nCHAMBA RD podrá revisar la información disponible para tomar una decisión administrativa.", Icons.Default.ReportProblem),
        PolicySection(17, "CALIFICACIONES", "Las calificaciones deben ser honestas y basadas en una experiencia real.\n\nEstá prohibido:\n- Crear reseñas falsas.\n- Amenazar para obtener una buena calificación.\n- Manipular calificaciones.\n- Crear cuentas para aumentar artificialmente una reputación.", Icons.Default.StarRate),
        PolicySection(18, "TÉCNICOS VERIFICADOS", "La insignia:\n✓ TÉCNICO VERIFICADO\n\nsolamente podrá utilizarse cuando CHAMBA RD haya completado el proceso de verificación correspondiente.\n\nLa verificación podrá incluir documentación como:\n- Cédula.\n- Certificados.\n- INFOTEP.\n- Otros documentos requeridos por la plataforma.\n\nLa insignia no constituye una garantía absoluta sobre el resultado del trabajo.", Icons.Default.VerifiedUser),
        PolicySection(19, "RESPONSABILIDAD DEL CLIENTE", "El cliente debe proporcionar:\n- Información correcta.\n- Dirección o ubicación adecuada cuando sea necesaria.\n- Descripción clara del trabajo.\n- Acceso razonable al lugar de trabajo.\n- Condiciones de seguridad adecuadas.\n\nEl cliente es responsable de informar previamente cualquier condición especial que pueda afectar la realización del trabajo.", Icons.Default.Warning),
        PolicySection(20, "RESPONSABILIDAD DEL TÉCNICO", "El técnico debe:\n- Realizar únicamente trabajos que pueda ejecutar.\n- Utilizar herramientas y conocimientos adecuados.\n- Cumplir las normas de seguridad aplicables.\n- Informar al cliente de problemas relevantes.\n- Respetar el precio acordado salvo que ambas partes acuerden un cambio.", Icons.Default.Warning),
        PolicySection(21, "CAMBIOS DE PRECIO", "Una vez establecido el precio final, cualquier cambio debe ser acordado por las partes y registrado correctamente en la plataforma cuando la funcionalidad lo permita.\n\nNo se debe cobrar al cliente una cantidad diferente sin conocimiento y aceptación correspondiente.", Icons.Default.PriceChange),
        PolicySection(22, "COMUNICACIÓN FUERA DE LA PLATAFORMA", "CHAMBA RD recomienda utilizar las herramientas oficiales de la plataforma para mantener registro de las comunicaciones relacionadas con la operación.\n\nNo se debe utilizar la plataforma para solicitar información innecesaria o sensible de otros usuarios.", Icons.Default.Forum),
        PolicySection(23, "PROTECCIÓN DE INFORMACIÓN", "La información privada debe mantenerse protegida.\n\nNo mostrar públicamente:\n- Contraseñas.\n- Datos bancarios.\n- Documentos de identidad.\n- Comprobantes privados.\n- Información financiera privada.\n- Secretos del sistema.", Icons.Default.PrivacyTip),
        PolicySection(24, "CUENTAS", "Cada usuario debe utilizar su propia cuenta.\n\nEstá prohibido:\n- Compartir credenciales.\n- Crear cuentas fraudulentas.\n- Suplantar a otra persona.\n- Utilizar una cuenta para actividades prohibidas.", Icons.Default.AccountCircle),
        PolicySection(25, "ADMINISTRADORES", "Los administradores son usuarios internos autorizados.\n\nEl administrador podrá gestionar:\n- Usuarios.\n- Chambas.\n- Verificaciones.\n- Pagos.\n- Comisiones.\n- Cuenta bancaria oficial.\n- Atención al cliente.\n- Disputas.\n- Reportes.\n- Configuración.\n\nAdministrador NO debe aparecer como opción de registro público.\n\nTodas las acciones administrativas importantes deben quedar registradas mediante auditoría.", Icons.Default.AdminPanelSettings),
        PolicySection(26, "ATENCIÓN AL CLIENTE", "Canales iniciales:\n📞 829-837-0908\n💬 WhatsApp: +1 829-837-0908\n\nEstos datos podrán ser modificados por un administrador autorizado.", Icons.Default.SupportAgent),
        PolicySection(27, "DISPONIBILIDAD DEL SERVICIO", "CHAMBA RD intentará mantener la plataforma disponible y funcionando correctamente, pero pueden ocurrir:\n- Mantenimientos.\n- Actualizaciones.\n- Fallos técnicos.\n- Interrupciones de Internet.\n- Problemas de proveedores externos.\n\nLas funciones que dependan del servidor requieren conexión a Internet.", Icons.Default.CloudQueue),
        PolicySection(28, "PROPIEDAD DE LA CUENTA", "El usuario es responsable de proteger sus credenciales y de toda actividad realizada desde su cuenta.\n\nSi sospecha que alguien accedió a su cuenta, debe comunicarse con CHAMBA RD.", Icons.Default.Lock),
        PolicySection(29, "ACTUALIZACIÓN DE LAS POLÍTICAS", "CHAMBA RD podrá actualizar sus políticas y reglas cuando sea necesario.\n\nLa aplicación deberá mostrar la versión o fecha de actualización de las políticas.\n\nCuando corresponda, el usuario podrá recibir una notificación sobre cambios importantes.", Icons.Default.Update),
        PolicySection(30, "ACEPTACIÓN DE LAS REGLAS", "Antes de utilizar determinadas funciones importantes, el sistema podrá solicitar al usuario confirmar que ha leído y acepta las políticas correspondientes.\n\nRegistrar la aceptación cuando sea necesario.", Icons.Default.Checklist),
        PolicySection(31, "PRINCIPIOS DE CHAMBA RD", "La plataforma debe promover:\n\n🤝 Respeto\nClientes y técnicos deben tratarse con respeto.\n\n🔒 Seguridad\nLa información privada debe estar protegida.\n\n💰 Transparencia\nLos precios, comisiones y pagos deben mostrarse claramente.\n\n⚖️ Equidad\nLas operaciones deben gestionarse de manera transparente.\n\n⭐ Calidad\nSe busca fomentar buenos servicios y buenas experiencias.\n\n🇩🇴 Confianza\nCHAMBA RD busca construir una plataforma confiable para clientes y técnicos en República Dominicana.", Icons.Default.Handshake)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Políticas y Reglas", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo(Screen.Profile) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ChambaNavyPrimary)
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "SECCIÓN MAESTRA",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = ChambaNavyPrimary,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = "POLÍTICAS, REGLAS, VENTAJAS Y FUNCIONAMIENTO DE CHAMBA RD",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Esta sección es el centro oficial de información sobre las reglas, beneficios, responsabilidades y funcionamiento de CHAMBA RD.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Última actualización: Septiembre 2026",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            items(policies) { policy ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = policy.icon,
                                contentDescription = null,
                                tint = ChambaNavyPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "${policy.id}. ${policy.title}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = ChambaNavyPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = policy.content,
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = ChambaNavyPrimary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "CHAMBA RD 🇩🇴",
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Conectamos personas con soluciones.\nTrabaja. Contrata. Crece.",
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Para asistencia:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "📞 829-837-0908",
                            fontSize = 14.sp,
                            color = Color.White
                        )
                        Text(
                            text = "💬 WhatsApp +1 829-837-0908",
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
