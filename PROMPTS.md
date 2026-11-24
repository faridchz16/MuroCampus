# PROMPTS.md · Fase 2 · Mejora profesional asistida por IA

**Proyecto:** MuroCampus · **Rama:** `mejora-ia` · **Autor:** Farid Chávez Campos
**Herramienta:** Gemini en Android Studio (modo Agent, modelo Gemini 3.6 Flash)

> Los prompts los redacté con apoyo de Claude y los ejecuté en Gemini dentro de Android Studio. Antes de aceptar cada cambio (Keep All) revisé el código generado contra las reglas de la arquitectura MVVM + Repository y probé la app en el emulador.

## Reglas que verifiqué en cada respuesta

- Nada de Retrofit, `PostApi`, `HttpException` ni `Context` en el ViewModel.
- La pantalla nunca llama a la API ni al Repository.
- Sin `catch` vacíos, sin `GlobalScope` y sin URLs `http://`.
- `CancellationException` siempre se relanza.
- Sin dependencias nuevas que el proyecto no tenga (por ejemplo, `material-icons`).

## Registro de prompts

| # | Prompt que escribí | Qué respondió Gemini | Qué cambié o descarté | Por qué |
|---|---|---|---|---|
| 1 | «Agrega un Interceptor de OkHttp que añada el encabezado `Accept: application/json` a todas las peticiones. Modifica solo `RetrofitClient.kt`, usa `.header()` y regístralo antes del `HttpLoggingInterceptor`.» | Agregó el interceptor como lambda en `OkHttpClient.Builder()`, que copia la petición con `newBuilder().header(...)`, y lo registró antes del logging. Explicó que los interceptores se ejecutan en el orden en que se agregan. | Nada. Lo acepté tal cual. | Cambiaba un solo archivo, usaba `.header()` para no duplicar el encabezado y respetaba el orden, por eso `Accept: application/json` aparece en Logcat. |
| 2 | «Agrega un campo de búsqueda que filtre por título las publicaciones ya descargadas, sin nuevas llamadas a la red. Usa `combine` + `stateIn` en el ViewModel y no uses `material-icons`.» | Agregó `textoBusqueda`, `buscar()` y `postsVisibles` con `combine(_lista, _textoBusqueda)` y `stateIn(WhileSubscribed(5000))`. En la pantalla puso un `OutlinedTextField`, un `TextButton` "Limpiar" y el estado "Sin resultados". | Nada. Lo acepté tal cual. | Filtra en memoria (Logcat no muestra peticiones nuevas al escribir), funciona junto con el filtro por usuario y no usa una dependencia que el proyecto no tiene. |
| 3 | «Agrega borrar desde el detalle con `@DELETE("posts/{id}")` y `Response<Unit>`. Si la respuesta no es exitosa, lanza `HttpException` en el Repository. Pide confirmación con `AlertDialog` y quita el post de la lista local.» | Agregó `borrar` en las 4 capas. El Repository revisa `isSuccessful` y lanza `HttpException` dentro de `llamar`. Puso un botón rojo, un diálogo con `rememberSaveable` y `LaunchedEffect` para volver una sola vez. | Nada. Lo revisé capa por capa y lo acepté. | El ViewModel sigue sin conocer Retrofit: el error HTTP se convierte en `Result.failure` en el Repository. Se pide confirmación porque borrar no se puede deshacer. |
| 4 | «Agrega una pantalla para editar el título con `@PATCH` y `EditarPost(title)`. Actualiza la lista y el detalle locales, y Guardar se deshabilita si no hay cambios.» | Creó `PantallaEditar.kt` y agregó `editar` en las 4 capas. Actualiza `_lista` y `_detalle` con `copy()`. Explicó que `PUT` reemplaza el recurso completo y `PATCH` solo los campos enviados. | Nada. Observé que, después de guardar, el botón sigue habilitado porque compara contra el título original. | Es un detalle menor que no rompe nada. Logcat confirma que el cuerpo del `PATCH` solo lleva `{"title": ...}`. |
| 5 | «Carga las publicaciones de 20 en 20 con `@Query("_start")` y `@Query("_limit")` y un botón "Cargar más". Guarda la siguiente posición en una variable propia, no con el tamaño de la lista, y cancela la carga al cambiar de filtro.» | Agregó `listarPagina` en la API y el Repository. En el ViewModel puso `hayMas`, `cargandoMas`, `errorMas`, `siguienteInicio` y un `Job` cancelable. En la pantalla, el contador "Mostrando N" y el pie de lista. | Nada. Observé que, al llegar a 100, hace una petición extra que vuelve vacía. | Después de borrar una publicación (19) y cargar más quedaron 39, sin repetidas. Calcular la página con `lista.size` habría repetido una publicación. |
| 6 | «Guarda las publicaciones en Room (2.8.4 con KSP 2.3.10, sin kapt) con un `PostRepositoryConCache` que envuelva al remoto y lea Room solo con `IOException`. No modifiques el ViewModel ni cambies otras versiones.» | Agregó Room y KSP en Gradle. Creó `PostEntity`, `PostDao`, `MuroDatabase` (singleton) y `PostRepositoryConCache`, y conectó todo en `MainActivity`. Explicó que el ViewModel no cambia porque depende de la interface `PostRepository`. | **Revertí** un cambio no pedido: Gemini reemplazó `compileSdk { version = release(37) }` por `compileSdk = 37`. Además observé que, si falla el guardado en Room, se pierde el resultado de la red, y que los comentarios no se guardan, así que el detalle no funciona sin internet. | El prompt decía que no cambiara otras configuraciones, y un cambio así en Gradle puede romper el build sin relación con la tarea. La IA propone; yo decido. |

## Resultado de las mejoras

| Mejora | Capas que cambiaron | Commit |
|---|---|---|
| Encabezado con interceptor | RetrofitClient | `feat(red): agrega interceptor con encabezado Accept application/json` |
| Búsqueda por título | ViewModel + View | `feat(busqueda): filtra por titulo las publicaciones descargadas` |
| Borrar publicación | API + Repository + ViewModel + View | `feat(borrar): elimina publicacion con DELETE y Response<Unit>` |
| Editar con PATCH | Modelo + API + Repository + ViewModel + View | `feat(editar): modifica el titulo con PATCH y muestra la respuesta` |
| Paginación simple | API + Repository + ViewModel + View | `feat(paginacion): carga publicaciones de 20 en 20 con _start y _limit` |
| Caché en Room | Gradle + Room + Repository | `feat(cache): guarda publicaciones en Room y las muestra sin internet` |

## Conclusión

En ninguna respuesta Gemini dejó un `catch` vacío, llamó a la API desde un Composable, usó `GlobalScope` ni cambió la URL a `http://`. El único problema real fue un **cambio no solicitado en `app/build.gradle.kts`** en la mejora 6, que detecté al revisar el código y revertí antes del commit. Esto confirma que el código generado por IA siempre debe leerse y probarse antes de aceptarlo, aunque compile.