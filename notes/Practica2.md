Máster en Ciberseguridad & IA
Práctica 3. Del prototipo al producto
Enunciado, entregables y criterios de evaluación
Formato Trabajo en grupo. Mismos grupos que en la Práctica 1
Entregables Memoria técnica, vídeo de al menos 10 minutos y repositorio de GitHub (público o privado)
Fecha límite Viernes 16 de octubre de 2026, último día de clase
Entrega Tarea «Práctica 3» del campus. Un único envío por grupo

Contenido
No Apartado
1 Presentación de la práctica
2 Qué se espera del grupo
3 Punto de partida: la Práctica 1
4 Organización de los grupos
5 Entregable 1: memoria técnica
6 Entregable 2: vídeo de demostración
7 Entregable 3: repositorio de GitHub
8 Matriz de trazabilidad de requisitos
9 Criterios de evaluación
10 Calendario recomendado
11 Forma de entrega
12 Normas de integridad académica y uso ético
13 Preguntas frecuentes
14 Lista de comprobación antes de entregar

Evolve Academy Máster en Ciberseguridad & IA · Práctica 3

Fecha límite de entrega: 16/10/2026 Página 2
1 Presentación de la práctica
En la Práctica 1 cada grupo planteó un producto: identificó un problema, definió a quién iba dirigido, fijó
una lista de requisitos y propuso una solución. Aquello era, en la mayoría de los casos, un diseño con una
primera implementación parcial. La Práctica 3 retoma exactamente ese trabajo y le pone una exigencia
nueva: que funcione.
No se trata de empezar un proyecto distinto ni de reescribir la propuesta con otras palabras. El objetivo es
coger lo que el grupo prometió en la Práctica 1, corregir lo que se quedó corto, incorporar el feedback
recibido y llevarlo hasta un producto que se pueda instalar, usar y comprobar. Al final de la práctica tenéis
que poder sentar a alguien delante del producto y demostrarle, requisito por requisito, que hace lo que
dijisteis que haría.
Esta forma de trabajar es la habitual en cualquier equipo técnico. Una propuesta se valora por su
planteamiento; un producto se valora por lo que entrega. Entre una cosa y otra aparecen problemas que
en el papel no se ven: dependencias que no encajan, casos límite que nadie había pensado, requisitos que
resultan más caros de lo previsto. Parte de lo que se evalúa es cómo resolvéis esos problemas y cómo lo
contáis.
Objetivos de aprendizaje
Al terminar la práctica, cada integrante del grupo debería ser capaz de:
● Convertir una especificación de requisitos en un producto operativo, tomando decisiones técnicas
justificadas por el camino.
● Revisar críticamente su propio trabajo anterior y aplicar mejoras a partir del feedback recibido.
● Diseñar y ejecutar pruebas que demuestren el cumplimiento de cada requisito, no solo que el sistema
arranca.
● Aplicar criterios de seguridad al propio producto: gestión de secretos, control de accesos, validación de
entradas y tratamiento de datos.
● Documentar un sistema con el nivel de detalle que necesita otra persona para instalarlo, usarlo y
mantenerlo.
● Presentar y defender un producto ante una audiencia técnica, mostrando tanto lo que funciona como
sus limitaciones.
2 Qué se espera del grupo
La palabra clave de esta práctica es funcional. Conviene dejar claro desde el principio qué entendemos por
ello, porque es el criterio que más peso tiene en la evaluación.
Un producto es funcional cuando cumple estas condiciones
● Se puede instalar desde cero. Siguiendo el README del repositorio, una persona ajena al grupo debe
poder ponerlo en marcha en un equipo limpio o en un entorno de contenedores sin pediros ayuda.
● Los flujos principales se completan de principio a fin. No basta con pantallas o módulos sueltos. Si el
producto recoge datos, los procesa y muestra un resultado, las tres cosas tienen que ocurrir de verdad.
● No depende de maquetas ni de datos inventados sobre la marcha. Las pantallas de diseño, los
resultados escritos a mano en el código o las respuestas simuladas no cuentan como funcionalidad. Si

Evolve Academy Máster en Ciberseguridad & IA · Práctica 3

Fecha límite de entrega: 16/10/2026 Página 3
alguna integración externa no está disponible y tenéis que simularla, se indica de forma explícita en la
memoria y en el vídeo.
● Se comporta de forma razonable ante errores. Una entrada incorrecta, un servicio caído o un usuario
sin permisos no deberían tumbar el sistema ni exponer información interna.
● Cumple los requisitos de la Práctica 1. Este es el punto central. Cada requisito tiene que estar
implementado y demostrado, o bien justificado si se ha modificado o descartado.
Mejorar la Práctica 1
Mejorar no significa añadir funcionalidades por añadir. Lo primero es cerrar lo que quedó pendiente y
corregir lo que se señaló en la corrección. A partir de ahí, cualquier mejora adicional se valora, pero nunca
compensa un requisito original sin cumplir. Un producto con menos funciones que funcionen bien puntúa
más que uno con muchas funciones a medias.
Cuando durante el desarrollo descubráis que un requisito estaba mal planteado, es legítimo modificarlo. Lo
que se espera es que lo expliquéis: qué decía el requisito, por qué no era viable o no tenía sentido, qué
habéis hecho en su lugar y qué consecuencias tiene. Ocultar un requisito que no se ha cumplido penaliza
mucho más que reconocerlo y razonarlo.
En pocas palabras
Mismo proyecto, mismo grupo, mismos requisitos. Lo que cambia es el nivel de exigencia: de «esto es lo
que haríamos» a «esto es lo que hace, y aquí está la prueba».

3 Punto de partida: la Práctica 1
Antes de escribir una sola línea de código nuevo, el grupo debe recopilar y revisar el material de la Práctica
1. Es el documento de referencia contra el que se va a evaluar esta entrega, así que merece una lectura
   atenta y conjunta.
   Material que debéis reunir
   ● La memoria o documento entregado en la Práctica 1, en su versión final.
   ● La lista de requisitos funcionales y no funcionales que se definieron. Si no estaban numerados,
   numeradlos ahora (por ejemplo, RF-01, RF-02... y RNF-01, RNF-02...). Esa numeración es la que usaréis
   en toda la Práctica 3.
   ● La calificación y los comentarios recibidos del equipo docente.
   ● El código, prototipo o repositorio que existiera, aunque estuviera incompleto.
   Revisión inicial
   Recomendamos dedicar la primera sesión de trabajo a clasificar cada requisito en una de estas categorías.
   El resultado os sirve de plan de trabajo y, además, se incluye en la memoria.
   Estado del requisito Qué significa Qué hay que hacer
   Cumplido Implementado en la Práctica 1 y funciona Verificarlo con pruebas y documentar la

evidencia
Parcial Existe algo, pero incompleto o con fallos Terminarlo y probarlo

Evolve Academy Máster en Ciberseguridad & IA · Práctica 3

Fecha límite de entrega: 16/10/2026 Página 4
Estado del requisito Qué significa Qué hay que hacer
Pendiente Se definió, pero no se llegó a implementar Implementarlo desde cero
A revisar El feedback o la experiencia indican que

estaba mal planteado

Reformularlo con justificación y cumplir la
nueva versión

Descartado Se decide no implementarlo Justificarlo por escrito. Debe ser la excepción
4 Organización de los grupos
La práctica se realiza con los mismos grupos que se formaron para la Práctica 1. La continuidad es
intencionada: quien conoce el diseño original es quien mejor puede llevarlo a producción, y evaluar la
evolución del trabajo solo tiene sentido si el equipo es el mismo.
● No se admiten cambios de grupo ni incorporaciones a otros equipos salvo autorización expresa del
equipo docente.
● Si algún integrante ha causado baja en el máster, el grupo debe comunicarlo cuanto antes. Se valorará el
alcance del proyecto en función del número de personas que queden.
● Cada grupo designa un portavoz, que será el responsable de realizar la entrega y de canalizar las dudas.
● Todos los integrantes deben conocer el producto en su conjunto. Cualquiera de ellos puede ser
preguntado por cualquier parte del trabajo, no solo por la que ha desarrollado.
Reparto de roles
No imponemos una estructura de roles, pero sí pedimos que el reparto quede claro y que se refleje en la
memoria. Una distribución que suele funcionar bien en grupos de este tamaño es la siguiente, adaptándola
a vuestro caso:
Rol Responsabilidades principales
Coordinación Planificación, seguimiento de tareas, comunicación con el equipo docente y revisión final

de la entrega

Desarrollo Implementación de funcionalidades, integración de componentes y corrección de errores
Seguridad Modelo de amenazas, revisión de código, gestión de secretos y dependencias, pruebas de

seguridad

Pruebas y calidad Plan de pruebas, ejecución, registro de evidencias y matriz de trazabilidad
Documentación Memoria, README y guion del vídeo
Un rol no excluye a los demás. Lo normal es que todos desarrollen y que cada persona asuma además una responsabilidad
transversal. El historial de commits del repositorio es una de las fuentes que usaremos para ver cómo se ha repartido
realmente el trabajo.
5 Entregable 1: memoria técnica
La memoria es el documento que explica qué habéis construido, cómo, por qué y con qué resultado. Tiene
que poder leerse sin ver el vídeo ni abrir el repositorio, aunque remita a ambos para las evidencias. Se
entrega en PDF.

Evolve Academy Máster en Ciberseguridad & IA · Práctica 3

Fecha límite de entrega: 16/10/2026 Página 5
Extensión y formato
● Entre 20 y 40 páginas, sin contar portada, índice y anexos. No se valora más una memoria por ser más
larga; se valora que esté completa y que no tenga relleno.
● Tipografía legible (11 o 12 puntos para el cuerpo), páginas numeradas e índice.
● Capturas de pantalla legibles, con pie que indique qué muestran y a qué requisito corresponden.
● Los fragmentos de código, solo cuando aporten algo. El código completo está en el repositorio.
Estructura obligatoria
La memoria debe seguir este orden. Podéis añadir apartados, pero no suprimir ninguno de los siguientes.
No Apartado Contenido esperado
1 Portada Nombre del producto, número de grupo, integrantes, fecha, enlace al repositorio y

enlace al vídeo

2 Resumen ejecutivo Una página como máximo. Qué problema resuelve el producto, para quién, qué

hace y en qué estado se entrega

3 Punto de partida Resumen de la Práctica 1, feedback recibido y tabla con la clasificación inicial de

requisitos (apartado 3 de este enunciado)

4 Requisitos Lista completa y numerada de requisitos. Si alguno se ha modificado o descartado,

versión original, versión nueva y justificación

5 Arquitectura y decisiones
técnicas

Diagrama de componentes, tecnologías elegidas y el motivo de cada elección.
Cambios respecto a lo previsto en la Práctica 1

6 Funcionalidades
implementadas

Descripción de cada funcionalidad con capturas. Qué hace, cómo se usa y qué
requisitos cubre

7 Seguridad del producto Modelo de amenazas, controles aplicados, gestión de secretos, dependencias y

tratamiento de datos personales si los hay

8 Pruebas y evidencias Plan de pruebas, casos ejecutados, resultados obtenidos y pruebas que han fallado

con su explicación

9 Matriz de trazabilidad Tabla que relaciona cada requisito con su implementación, su prueba y su

evidencia (apartado 8 de este enunciado)

10 Limitaciones y trabajo
futuro

Qué no funciona, qué funciona con condiciones y qué haríais con más tiempo

11 Reparto del trabajo Quién ha hecho qué, con una estimación de horas por persona
12 Uso de herramientas de IA Declaración de qué herramientas se han usado y para qué (apartado 12 de este

enunciado)

13 Anexos Manual de instalación ampliado, credenciales de prueba, glosario y referencias

Sobre el apartado de seguridad
Estáis en un máster de ciberseguridad, y eso se va a notar en la corrección. Independientemente de qué
haga vuestro producto, esperamos que el propio sistema esté construido con criterio de seguridad. Como
mínimo, el apartado debe responder a estas preguntas:

Evolve Academy Máster en Ciberseguridad & IA · Práctica 3

Fecha límite de entrega: 16/10/2026 Página 6
● ¿Qué activos protege el sistema y quién podría querer atacarlo? Un modelo de amenazas sencillo, por
ejemplo con STRIDE, es suficiente.
● ¿Dónde están las credenciales, claves y tokens, y cómo se evita que acaben en el repositorio?
● ¿Cómo se autentica a los usuarios y cómo se controla qué puede hacer cada uno?
● ¿Qué entradas recibe el sistema y cómo se validan?
● ¿Qué dependencias de terceros usa y se ha comprobado si tienen vulnerabilidades conocidas?
Herramientas como Dependabot, npm audit, pip-audit o Trivy sirven para esto.
● ¿Trata datos personales? Si es así, cuáles, con qué finalidad, cuánto tiempo se conservan y cómo se
protegen.
Sobre el apartado de pruebas
Una prueba útil describe qué se hace, qué debería pasar y qué pasa realmente. Para cada requisito debe
existir al menos una prueba que lo verifique. Las pruebas automatizadas se valoran positivamente, pero las
manuales también son válidas si están bien documentadas.
ID
prueba
Requisit
o
Pasos Resultado
esperado

Resultado obtenido Estado

PR-07 RF-03 Iniciar sesión con usuario sin
rol de administración y
acceder a /admin

Respuesta 403 y
registro del intento

Respuesta 403. Intento
registrado con usuario, IP
y hora

Superada

PR-08 RNF-02 Enviar 200 peticiones en un
minuto al endpoint de login

Bloqueo temporal a
partir de la petición
20

Bloqueo a partir de la
petición 21

Superada
con observ
ación

Ejemplo orientativo. Los identificadores y valores son ficticios.
6 Entregable 2: vídeo de demostración
El vídeo es la prueba de que el producto funciona. Su duración mínima es de 10 minutos. No hay un
máximo estricto, pero os recomendamos no pasar de 20: un vídeo largo no es mejor si repite cosas o se
detiene en detalles sin interés.
Qué debe mostrar
El vídeo tiene que enseñar el producto funcionando de verdad, no una presentación sobre el producto. Las
diapositivas pueden servir de apoyo en la introducción y en la parte de arquitectura, pero la mayor parte
del tiempo debe verse el sistema en ejecución.
Bloque Duración
orientativa

Contenido

Presentación 1 minuto Grupo, integrantes y problema que resuelve el producto
Requisitos 1 minuto Recordatorio de los requisitos de la Práctica 1 y cambios introducidos
Arquitectura 1 a 2 minutos Componentes, tecnologías y cómo se comunican entre sí
Demostración 5 a 10 minutos Recorrido por las funcionalidades siguiendo la lista de requisitos,
indicando en cada momento cuál se está demostrando

Evolve Academy Máster en Ciberseguridad & IA · Práctica 3

Fecha límite de entrega: 16/10/2026 Página 7
Bloque Duración
orientativa

Contenido

Errores y seguridad 1 a 2 minutos Qué pasa ante entradas incorrectas, usuarios sin permisos o fallos de un

servicio. Controles de seguridad en acción
Cierre 1 minuto Limitaciones conocidas y posibles mejoras

Requisitos del vídeo
● Formato MP4, con resolución de 1080p recomendada y 720p como mínimo. El texto de la pantalla tiene
que leerse sin esfuerzo.
● Audio claro. Si grabáis con el micrófono del portátil, hacedlo en un sitio sin eco ni ruido de fondo.
● La demostración debe hacerse sobre el producto real, desplegado a partir del código entregado en el
repositorio.
● Se permiten cortes para eliminar esperas largas (instalaciones, compilaciones, procesos lentos), pero
deben indicarse en pantalla. Un corte que oculte un fallo se considera una falta grave.
● Si algo falla durante la grabación, es preferible dejarlo y explicarlo que regrabar hasta que salga bien por
casualidad.
● Se valorará que intervengan todos los integrantes del grupo.
Dónde alojarlo
Subid el vídeo a YouTube como «no listado» o a Google Drive, OneDrive u otro servicio equivalente con
permiso de visualización para cualquiera que tenga el enlace. Comprobad el enlace desde una ventana de
incógnito antes de entregar: un enlace que pide permisos equivale a un vídeo no entregado.
7 Entregable 3: repositorio de GitHub
Todo el código del producto debe estar en un repositorio de GitHub. Puede ser público o privado, a
elección del grupo.
● Repositorio privado: antes de la fecha de entrega, añadid como colaboradores con permiso de lectura
las cuentas de GitHub del equipo docente que se publicarán en el campus.
● Repositorio público: revisad que no contiene credenciales, datos personales ni información que no deba
publicarse, incluido el historial de commits.
Contenido mínimo
Elemento Qué debe incluir
README.md Descripción del producto, requisitos previos, instrucciones de instalación y ejecución paso a

paso, uso básico, estructura del proyecto y datos de acceso de prueba

Código fuente Organizado y con nombres comprensibles. Comentarios donde la lógica no sea evidente
Configuración de ejemplo Un fichero .env.example o equivalente con todas las variables necesarias y valores ficticios
Despliegue Scripts o ficheros de contenedores (Dockerfile, docker-compose.yml) si el producto los usa
Pruebas Las pruebas automatizadas, si las hay, y cómo ejecutarlas

Evolve Academy Máster en Ciberseguridad & IA · Práctica 3

Fecha límite de entrega: 16/10/2026 Página 8
Elemento Qué debe incluir
.gitignore Configurado para excluir secretos, dependencias descargadas y ficheros generados
Licencia Si el repositorio es público, indicad bajo qué licencia se publica

Normas del repositorio
● Ningún secreto real en el repositorio ni en su historial: contraseñas, claves de API, tokens o certificados.
Si alguno se subió por error, no basta con borrarlo en un commit nuevo; hay que revocarlo y limpiar el
historial.
● Marcad la versión entregada con una etiqueta (tag) llamada v1.0-practica3 sobre el commit
correspondiente. Se evaluará ese commit. Los cambios posteriores a la fecha límite no se tendrán en
cuenta.
● El historial debe reflejar el trabajo del grupo. Un repositorio con un único commit final, o con todo el
código subido por una sola persona, no permite valorar el reparto del trabajo y se tendrá en cuenta en la
evaluación.
● Si reutilizáis código de terceros, indicad su origen y respetad su licencia.
8 Matriz de trazabilidad de requisitos
La matriz de trazabilidad es la pieza que une los tres entregables. Para cada requisito de la Práctica 1 indica
dónde está implementado, cómo se ha probado y dónde puede comprobarse. Es, además, la herramienta
principal que usaremos en la corrección: iremos requisito por requisito siguiendo vuestra matriz.
Requisito Descripción Estado final Implementación Prueba Evidencia
RF-01 El usuario puede registrarse
con correo y contraseña

Cumplido src/auth/register.

py

PR-01,
PR-02

Memoria fig. 4 ·
Vídeo 03:12

RF-02 El sistema envía una alerta
cuando detecta un acceso
anómalo

Cumplido src/alerts/ PR-05 Vídeo 07:40

RF-06 Exportación de informes en

PDF

Modificado: se
exporta en CSV

src/reports/export
.py

PR-11 Memoria apdo. 4.2

RNF-03 Tiempo de respuesta inferior

a 2 segundos

Parcial No aplica PR-14 Memoria apdo. 8.3

Ejemplo orientativo. Los requisitos y rutas son ficticios; cada grupo debe usar los suyos.
Indicad en la columna de evidencia el minuto exacto del vídeo en el que se demuestra cada requisito. Esto
ahorra tiempo a quien corrige y os asegura que ningún requisito queda sin ver.
9 Criterios de evaluación
La práctica se califica sobre 10 puntos con la siguiente distribución. La nota es común para el grupo,
aunque el equipo docente puede ajustarla de forma individual cuando haya evidencias claras de un reparto
desigual del trabajo.

Evolve Academy Máster en Ciberseguridad & IA · Práctica 3

Fecha límite de entrega: 16/10/2026 Página 9
Criterio Peso Qué se valora
Producto funcional 30 % El sistema se instala siguiendo el README y los flujos principales

funcionan de principio a fin

Cumplimiento de requisitos de la
Práctica 1

20 % Grado de cumplimiento de los requisitos originales y calidad de la
justificación de los cambios

Seguridad y calidad técnica 15 % Seguridad del propio producto, calidad del código, pruebas y gestión

de dependencias

Memoria técnica 15 % Completitud, claridad, rigor y coherencia con lo que muestran el vídeo

y el repositorio

Vídeo de demostración 10 % Demostración real, cobertura de requisitos, claridad y honestidad al

mostrar limitaciones

Repositorio y trabajo en equipo 10 % Organización, README, historial de commits y reparto del trabajo

Niveles de desempeño
Criterio Sobresaliente Suficiente Insuficiente
Producto
funcional

Se instala sin incidencias y
todos los flujos funcionan,
incluidos los casos de error

Se instala con algún ajuste
menor y los flujos principales
funcionan

No se instala siguiendo el
README o los flujos
principales fallan

Requisitos Todos cumplidos o justificados
con solidez. Mejoras sobre la
Práctica 1 bien planteadas

La mayoría cumplidos. Algún
cambio con justificación débil

Requisitos sin cumplir y sin
explicación

Seguridad Modelo de amenazas,
controles aplicados y
demostrados, sin secretos
expuestos, dependencias
revisadas

Controles básicos presentes,
análisis superficial

Secretos en el repositorio o
vulnerabilidades evidentes sin
tratar

Memoria Completa, precisa, bien escrita
y coherente con el resto de
entregables

Completa con apartados poco
desarrollados

Falta algún apartado
obligatorio o contradice lo que
muestra el producto

Vídeo Demostración fluida de todos
los requisitos, con minutos
referenciados en la matriz

Demuestra lo principal, con
partes poco claras

Duración inferior a 10 minutos,
demostración sobre maquetas
o cortes que ocultan fallos

Repositorio Ordenado, README impecable,
historial que refleja el trabajo
de todos

Correcto con carencias
menores

Desordenado, sin README útil
o con un único autor

Condiciones que impiden aprobar
Falta alguno de los tres entregables. El vídeo dura menos de 10 minutos. El producto no llega a ejecutarse.
Se detecta plagio o una demostración manipulada. En cualquiera de estos casos la práctica se calificará
como no superada.

Evolve Academy Máster en Ciberseguridad & IA · Práctica 3

Fecha límite de entrega: 16/10/2026 Página 10
10 Calendario recomendado
La fecha límite es el viernes 16 de octubre de 2026, que coincide con el último día de clase. Entre la
publicación de este enunciado y la entrega hay unas cuatro semanas. El siguiente reparto es solo una
propuesta, pero os recomendamos seguirlo o hacer uno propio: los grupos que dejan la grabación del vídeo
y la memoria para los últimos dos días suelen entregar peor de lo que su producto merece.
Semana Objetivo Resultado al final de la semana
Del 21 al 25 de septiembre Revisión de la Práctica 1 y

planificación

Requisitos numerados y clasificados, feedback
analizado, tareas repartidas y repositorio
preparado

Del 28 de septiembre al 2 de
octubre

Núcleo funcional Flujos principales funcionando de principio a fin,

aunque sin pulir

Del 5 al 9 de octubre Requisitos pendientes, seguridad

y pruebas

Todos los requisitos abordados, pruebas
ejecutadas y registradas, revisión de seguridad
hecha

Del 12 al 16 de octubre Cierre y entrega Código congelado y etiquetado, vídeo grabado,
memoria terminada y entrega realizada
Un consejo práctico: congelad el código dos o tres días antes de la fecha límite. Grabad el vídeo y cerrad la memoria sobre
esa versión. Si seguís cambiando el código mientras grabáis, lo que se ve en el vídeo acabará sin coincidir con lo que hay en
el repositorio.
11 Forma de entrega
La entrega se realiza a través de la tarea «Práctica 3» del campus. La hace el portavoz del grupo en un único
envío. Si el campus permite varios envíos, solo se tendrá en cuenta el último realizado dentro de plazo. La
hora de cierre es la que figure en la propia tarea.
Qué se sube Formato Nombre del fichero
Memoria técnica PDF P3_GrupoXX_Memoria.pdf
Enlaces Dentro de la portada de la memoria y en el

comentario de la entrega

Enlace al vídeo y enlace al
repositorio

Sustituid «XX» por el número de vuestro grupo con dos dígitos. Antes de enviar, abrid los dos enlaces
desde una ventana de incógnito y comprobad que funcionan. Si el repositorio es privado, comprobad
también que el equipo docente tiene acceso.
Las entregas fuera de plazo se regirán por la normativa de evaluación del máster.
12 Normas de integridad académica y uso ético

Originalidad

Evolve Academy Máster en Ciberseguridad & IA · Práctica 3

Fecha límite de entrega: 16/10/2026 Página 11
El trabajo debe ser del grupo. Se puede reutilizar código abierto, bibliotecas y ejemplos de documentación,
siempre que se indique su origen y se respete su licencia. Presentar como propio trabajo ajeno, o compartir
código entre grupos, se considera plagio y supone la calificación de no superado para todos los implicados.
Uso de herramientas de inteligencia artificial
Podéis usar asistentes de IA como apoyo, igual que usaríais la documentación o un buscador. Lo que no se
admite es entregar algo que el grupo no entiende. Las condiciones son dos: declarar en la memoria qué
herramientas habéis usado y para qué tareas, y ser capaces de explicar cualquier parte del código o de la
memoria si se os pregunta. Si en la corrección alguien del grupo no sabe explicar una parte de su propio
trabajo, esa parte se considerará no realizada.
Uso ético y legal
Muchos de vuestros productos tendrán relación con la seguridad ofensiva o defensiva. Cualquier prueba de
seguridad, escaneo o ataque, tanto durante el desarrollo como en el vídeo, debe hacerse exclusivamente
sobre sistemas propios o sobre entornos de laboratorio preparados para ello. Queda prohibido usar el
producto contra sistemas de terceros sin autorización escrita, aunque sea para demostrar que funciona.
Tampoco deben usarse datos personales reales de terceros. Para las pruebas y el vídeo, utilizad datos
ficticios o generados. Si vuestro producto necesita tratar datos personales por su naturaleza, explicad en la
memoria cómo lo hace de forma compatible con la normativa de protección de datos.
13 Preguntas frecuentes

¿Podemos cambiar la tecnología que usamos en la Práctica 1?
Sí, si está justificado. Explicad en la memoria qué habéis cambiado y por qué. Lo que se evalúa es el
cumplimiento de los requisitos, no la fidelidad a una herramienta concreta.
¿Podemos añadir funcionalidades que no estaban en la Práctica 1?
Sí, y se valorarán como mejora. Pero no compensan requisitos originales que se hayan quedado sin
cumplir. Primero lo prometido, después lo nuevo.
Un requisito de la Práctica 1 resulta inviable. ¿Qué hacemos?
Reformularlo o descartarlo, y justificarlo en la memoria con la versión original, la nueva y el motivo. Lo
antes posible, además, consultadlo con el equipo docente.
¿El vídeo puede durar más de 10 minutos?
Sí. Los 10 minutos son el mínimo. Recomendamos no superar los 20 salvo que el producto lo necesite de
verdad.
¿Pueden salir diapositivas en el vídeo?
Sí, como apoyo en la introducción y en la arquitectura. La mayor parte del vídeo debe mostrar el producto
en funcionamiento.
¿Tenemos que desplegar el producto en internet?
No es obligatorio. Basta con que funcione en local o en contenedores siguiendo el README. Si lo
desplegáis, indicad la URL y aseguraos de que el despliegue no expone nada que no deba.

Evolve Academy Máster en Ciberseguridad & IA · Práctica 3

Fecha límite de entrega: 16/10/2026 Página 12
¿Podemos usar GitLab o Bitbucket en lugar de GitHub?
No. La entrega debe hacerse con un repositorio de GitHub, público o privado.
¿Qué pasa si un integrante no ha participado?
Comunicadlo al equipo docente antes de la entrega, no después. La nota puede ajustarse de forma
individual cuando haya evidencias de un reparto desigual.
La demo ha fallado durante la grabación. ¿Repetimos?
Podéis repetir la grabación, pero si el fallo forma parte del comportamiento real del producto, es mejor
mostrarlo y explicarlo. Ocultarlo con cortes se considera manipulación.
14 Lista de comprobación antes de entregar
Repasad esta lista en grupo el día antes de la entrega. Si alguna casilla queda sin marcar, la entrega no está
lista.
Entregable Comprobación
☐ Memoria Todos los apartados obligatorios están presentes y en orden
☐ Memoria La portada incluye integrantes, enlace al vídeo y enlace al repositorio
☐ Memoria La matriz de trazabilidad cubre todos los requisitos de la Práctica 1
☐ Memoria Se declara el uso de herramientas de IA
☐ Memoria El fichero se llama P3_GrupoXX_Memoria.pdf
☐ Vídeo Dura 10 minutos o más
☐ Vídeo Muestra el producto real en funcionamiento y cada requisito tiene su minuto en la matriz
☐ Vídeo El enlace funciona desde una ventana de incógnito
☐ Repositorio El README permite instalar y ejecutar el producto desde cero
☐ Repositorio Existe la etiqueta v1.0-practica3 sobre el commit entregado
☐ Repositorio No hay secretos reales ni en el código ni en el historial
☐ Repositorio Si es privado, el equipo docente tiene acceso de lectura
☐ Producto Una persona ajena al grupo lo ha instalado siguiendo el README
☐ Entrega El portavoz ha subido la entrega a la tarea del campus antes del cierre del 16 de octubre

Dudas
Las dudas sobre el enunciado se plantean en el foro de la asignatura, para que la respuesta llegue a todos
los grupos. Las cuestiones que afecten solo a un grupo, como bajas o cambios de requisitos, se tratan