
# App CRM Mobile (Android + Odoo)

App Android en **Kotlin + Jetpack Compose** que consulta **Leads** y **Contactos** creados en Odoo vía endpoints JSON. Incluye 2 pestañas, paginación simple por límite y un serializador tolerante a valores `false` que Odoo devuelve en campos como `phone`.

## Arquitectura rápida

* **Odoo (módulo custom)**

  * Endpoints públicos:

    * `POST /api/v1/leads/search`
    * `POST /api/v1/contacts/search`
  * Formato JSON-RPC simple:
    `{"jsonrpc":"2.0","method":"call","params":{...}}`
  * Respuesta:
    `{"result":{"items":[...]}}`

* **Android**

  * **Networking**: OkHttp + kotlinx.serialization.
  * **UI**: Jetpack Compose. 2 tabs: *Leads* y *Contactos*.
  * **State**: ViewModel + StateFlow.
  * **Compat**: Emulador usa `http://10.0.2.2:8069`.

## Estructura de código (Android)

```
app/src/main/java/com/kumbra/crm/
├─ data/
│  └─ models.kt                    # Data classes + serializer tolerante a false
├─ net/
│  └─ Api.kt                       # Cliente HTTP, rutas y JSON-RPC
├─ ui/theme/
│  ├─ LeadsViewModel.kt            # Carga de leads
│  ├─ ContactsViewModel.kt         # Carga de contactos
│  └─ Theme.kt, Color.kt, Type.kt
└─ MainActivity.kt                 # Tabs, UI y binding a VMs
```

## Requisitos

* Android Studio (AGP 8.1x+)
* Kotlin 2.0.21
* Min SDK 24
* Odoo 16/17/18 con módulo que expone las rutas anteriores

## Configuración

### 1) Odoo

Controlador (resumen de lo que ya tienes):

* `POST /api/v1/leads/search`
  `params`: `limit` (int), `tag` (str, opcional), `email` (str), `from`/`to` (ISO 8601)
  Filtra `crm.lead` y devuelve `{"result":{"items":[Lead...]}}`.

* `POST /api/v1/contacts/search`
  `params`: `limit` (int), `email` (str, opcional)
  Filtra `res.partner` tipo contacto y devuelve `{"result":{"items":[Contact...]}}`.

> Si dejas `auth="public"`, limita por IP/proxy o añade validación mínima (token) en `params`. En producción usa `auth="user"` + API key.

### 2) Android

**`net/Api.kt`** — ajusta tu host y DB:

```kotlin
private const val BASE_URL = "http://10.0.2.2:8069" // emulador -> localhost
private const val DB = "datarod"                    // tu base Odoo
```

**Cleartext HTTP** (si usas `http://`):

`AndroidManifest.xml`

```xml
<application
    android:usesCleartextTraffic="true"
    android:networkSecurityConfig="@xml/network_security_config" ...>
```

`res/xml/network_security_config.xml`

```xml
<network-security-config>
  <base-config cleartextTrafficPermitted="true"/>
</network-security-config>
```

## Contrato de API

### Request (ambas rutas)

```json
{
  "jsonrpc": "2.0",
  "method": "call",
  "params": {
    "limit": 50,
    "tag": "Solicitud de Catálogo"
  }
}
```

### Response (ejemplo `leads`)

```json
{
  "result": {
    "items": [
      {
        "id": 45,
        "name": "Oportunidad de Rodrigo Montaño",
        "contact_name": "Rodrigo Montaño",
        "email": "monrodrigot@gmail.com",
        "phone": "+49 78306759",
        "mobile": "+49 78306759",
        "country": "Germany",
        "tags": ["Solicitud de Catálogo"],
        "create_date": "2025-09-16T16:56:22.821435"
      }
    ]
  }
}
```

## Serialización robusta

Odoo devuelve `false` en `phone/mobile/email`. En `data/models.kt` se usa:

```kotlin
@Serializable(with = StringOrFalseAsNullSerializer::class) val phone: String? = null
@Serializable(with = StringOrFalseAsNullSerializer::class) val mobile: String? = null
@Serializable(with = StringOrFalseAsNullSerializer::class) val email: String? = null
```

Esto evita errores tipo *“Unexpected JSON token … had 'f' at path …phone”*.

## Flujo en la app

1. **Tabs**: *Leads* y *Contactos*.
2. Botón **Cargar** en cada pestaña:

   * `LeadsViewModel.load()` → `Api.postLeads(LeadQuery(limit=50, tag="Solicitud de Catálogo"))`
   * `ContactsViewModel.load()` → `Api.postContacts(ContactQuery(limit=50))`
3. Render en `LazyColumn`.


> Si usas auth=user, añade Basic Auth y `?db=TU_DB` en la URL.

## Errores comunes

* **Login HTML en respuesta (200 OK)**
  Estás sin credenciales o sin `auth="public"`. Usar Basic Auth + `?db=...` o cambiar a público.

* **401 Unauthorized**
  Falta Basic Auth o API key invalida.

* **Could not send request / Invalid protocol: post http**
  En Postman, método = `POST` y URL = `http://...` sin espacios. Body en **raw JSON**.

* **Unexpected JSON token … `phone` false**
  Faltaba el `StringOrFalseAsNullSerializer`. Ya incluido.

* **DNS host tu-odoo.com**
  Cambia `BASE_URL` al dominio real o `10.0.2.2` en emulador.

## Extensiones rápidas

* Filtros UI: agrega campos para `tag`, `email`, rangos `from/to` y pásalos en `LeadQuery`.
* Paginación: agrega `offset` y ordénalo en el endpoint Odoo.
* Persistencia: cache con Room si lo necesitas offline.
* Seguridad: cambia a HTTPS y `auth="user"`. Nunca publiques `auth="public"` sin control.

